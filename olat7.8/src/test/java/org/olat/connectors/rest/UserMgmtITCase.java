/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.connectors.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.support.vo.ErrorVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.connectors.rest.user.UserVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.data.user.UserConstants;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.user.DisplayPortraitManager;
import org.olat.lms.user.UserService;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the <code>UserWebservice</code>
 * <P>
 * Initial Date: 15 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class UserMgmtITCase extends OlatJerseyTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private Identity owner1, id1, id2;
    private BusinessGroup g1, g2, g3, g4;

    private BusinessGroupService businessGroupService;
    @Autowired
    UserService userService;
    @Autowired
    private BaseSecurity securityManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // create identities
        owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");
        id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
        id2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
        DBFactory.getInstance().intermediateCommit();
        userService.setUserProperty(id2.getUser(), UserConstants.TELMOBILE, "39847592");
        userService.setUserProperty(id2.getUser(), UserConstants.GENDER, "female");
        userService.setUserProperty(id2.getUser(), UserConstants.BIRTHDAY, "20091212");
        DBFactory.getInstance().updateObject(id2.getUser());
        DBFactory.getInstance().intermediateCommit();

        businessGroupService = applicationContext.getBean(BusinessGroupService.class);

        final OLATResourceManager rm = OLATResourceManager.getInstance();
        // create course and persist as OLATResourceImpl
        final OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse", System.currentTimeMillis());
        final OLATResource course = rm.createOLATResourceInstance(resourceable);
        DBFactory.getInstance().saveObject(course);
        DBFactory.getInstance().intermediateCommit();

        // create learn group

        final BGContextDao cm = BGContextDaoImpl.getInstance();

        // 1) context one: learning groups
        final BGContext c1 = businessGroupService.createAndAddBGContextToResource("c1name-learn", course, BusinessGroup.TYPE_LEARNINGROUP, owner1, true);
        // create groups without waiting list
        g1 = businessGroupService
                .createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g1", null, new Integer(0), new Integer(10), false, false, c1);
        g2 = businessGroupService
                .createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g2", null, new Integer(0), new Integer(10), false, false, c1);
        // members g1
        securityManager.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
        // members g2
        securityManager.addIdentityToSecurityGroup(id2, g2.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());

        // 2) context two: right groups
        final BGContext c2 = businessGroupService.createAndAddBGContextToResource("c2name-area", course, BusinessGroup.TYPE_RIGHTGROUP, owner1, true);
        // groups
        g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g3", null, null, null, null/* enableWaitinglist */,
                null/* enableAutoCloseRanks */, c2);
        g4 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g4", null, null, null, null/* enableWaitinglist */,
                null/* enableAutoCloseRanks */, c2);
        // members
        securityManager.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id2, g4.getPartipiciantGroup());
        DBFactory.getInstance().closeSession();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetUsers() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final HttpMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<UserVO> vos = parseUserArray(body);
        final List<Identity> identities = securityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null,
                Identity.STATUS_VISIBLE_LIMIT);

        assertNotNull(vos);
        assertFalse(vos.isEmpty());
        assertEquals(vos.size(), identities.size());
    }

    @Test
    public void testFindUsersByLogin() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("login", "administrator"), new NameValuePair("authProvider", "OLAT") });
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<UserVO> vos = parseUserArray(body);
        final String[] authProviders = new String[] { "OLAT" };
        final List<Identity> identities = securityManager.getIdentitiesByPowerSearch("administrator", null, true, null, null, authProviders, null, null, null, null,
                Identity.STATUS_VISIBLE_LIMIT);

        assertNotNull(vos);
        assertFalse(vos.isEmpty());
        assertEquals(vos.size(), identities.size());
        boolean onlyLikeAdmin = true;
        for (final UserVO vo : vos) {
            if (!vo.getLogin().startsWith("administrator")) {
                onlyLikeAdmin = false;
            }
        }
        assertTrue(onlyLikeAdmin);
    }

    @Test
    public void testFindUsersByProperty() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("telMobile", "39847592"), new NameValuePair("gender", "Female"),
                new NameValuePair("birthDay", "12/12/2009") });
        method.addRequestHeader("Accept-Language", "en");
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<UserVO> vos = parseUserArray(body);

        assertNotNull(vos);
        assertFalse(vos.isEmpty());
    }

    @Test
    public void testFindAdminByAuth() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("authUsername", "administrator"), new NameValuePair("authProvider", "OLAT") });
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<UserVO> vos = parseUserArray(body);

        assertNotNull(vos);
        assertFalse(vos.isEmpty());
        assertEquals(1, vos.size());
        assertEquals("administrator", vos.get(0).getLogin());
    }

    @Test
    public void testGetUser() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final UserVO vo = parse(body, UserVO.class);

        assertNotNull(vo);
        assertEquals(vo.getKey(), id1.getKey());
        assertEquals(vo.getLogin(), id1.getName());
        // are the properties there?
        assertFalse(vo.getProperties().isEmpty());
    }

    @Test
    public void testGetUserNotAdmin() throws IOException {
        final HttpClient c = loginWithCookie("rest-one", "A6B7C8");

        final HttpMethod method = createGet("/users/" + id2.getKey(), MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final UserVO vo = parse(body, UserVO.class);

        assertNotNull(vo);
        assertEquals(vo.getKey(), id2.getKey());
        assertEquals(vo.getLogin(), id2.getName());
        // no properties for security reason
        assertTrue(vo.getProperties().isEmpty());
    }

    /**
     * Only print out the raw body of the response
     * 
     * @throws IOException
     */
    @Test
    public void testGetRawJsonUsers() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final HttpMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String bodyJsons = method.getResponseBodyAsString();
        System.out.println("Users JSON");
        System.out.println(bodyJsons);
        System.out.println("Users JSON");
    }

    /**
     * Only print out the raw body of the response
     * 
     * @throws IOException
     */
    @Test
    public void testGetRawXmlUsers() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final HttpMethod method = createGet("/users", MediaType.APPLICATION_XML, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String bodyXmls = method.getResponseBodyAsString();
        System.out.println("Users XML");
        System.out.println(bodyXmls);
        System.out.println("Users XML");
    }

    /**
     * Only print out the raw body of the response
     * 
     * @throws IOException
     */
    @Test
    public void testGetRawJsonUser() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String bodyJson = method.getResponseBodyAsString();
        System.out.println("User");
        System.out.println(bodyJson);
        System.out.println("User");
    }

    /**
     * Only print out the raw body of the response
     * 
     * @throws IOException
     */
    @Test
    public void testGetRawXmlUser() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_XML, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String bodyXml = method.getResponseBodyAsString();
        System.out.println("User");
        System.out.println(bodyXml);
        System.out.println("User");
    }

    @Test
    public void testCreateUser() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final UserVO vo = new UserVO();
        final String username = UUID.randomUUID().toString();
        vo.setLogin(username);
        vo.setFirstName("John");
        vo.setLastName("Smith");
        vo.setEmail(username + "@frentix.com");
        vo.putProperty("telOffice", "39847592");
        vo.putProperty("telPrivate", "39847592");
        vo.putProperty("telMobile", "39847592");
        vo.putProperty("gender", "Female");// male or female
        vo.putProperty("birthDay", "12/12/2009");

        final String stringuifiedAuth = stringuified(vo);
        final PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(entity);
        method.addRequestHeader("Accept-Language", "en");

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final UserVO savedVo = parse(body, UserVO.class);

        final Identity savedIdent = securityManager.findIdentityByName(username);

        assertNotNull(savedVo);
        assertNotNull(savedIdent);
        assertEquals(savedVo.getKey(), savedIdent.getKey());
        assertEquals(savedVo.getLogin(), savedIdent.getName());
        assertEquals("Female", userService.getUserProperty(savedIdent.getUser(), UserConstants.GENDER, Locale.ENGLISH));
        assertEquals("39847592", userService.getUserProperty(savedIdent.getUser(), UserConstants.TELPRIVATE, Locale.ENGLISH));
        assertEquals("12/12/09", userService.getUserProperty(savedIdent.getUser(), UserConstants.BIRTHDAY, Locale.ENGLISH));
    }

    /**
     * Test machine format for gender and date
     */
    @Test
    public void testCreateUser2() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final UserVO vo = new UserVO();
        final String username = UUID.randomUUID().toString();
        vo.setLogin(username);
        vo.setFirstName("John");
        vo.setLastName("Smith");
        vo.setEmail(username + "@frentix.com");
        vo.putProperty("telOffice", "39847592");
        vo.putProperty("telPrivate", "39847592");
        vo.putProperty("telMobile", "39847592");
        vo.putProperty("gender", "female");// male or female
        vo.putProperty("birthDay", "20091212");

        final String stringuifiedAuth = stringuified(vo);
        final PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(entity);
        method.addRequestHeader("Accept-Language", "en");

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final UserVO savedVo = parse(body, UserVO.class);

        final Identity savedIdent = securityManager.findIdentityByName(username);

        assertNotNull(savedVo);
        assertNotNull(savedIdent);
        assertEquals(savedVo.getKey(), savedIdent.getKey());
        assertEquals(savedVo.getLogin(), savedIdent.getName());
        assertEquals("Female", userService.getUserProperty(savedIdent.getUser(), UserConstants.GENDER, Locale.ENGLISH));
        assertEquals("39847592", userService.getUserProperty(savedIdent.getUser(), UserConstants.TELPRIVATE, Locale.ENGLISH));
        assertEquals("12/12/09", userService.getUserProperty(savedIdent.getUser(), UserConstants.BIRTHDAY, Locale.ENGLISH));
    }

    @Test
    public void testCreateUserWithValidationError() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final UserVO vo = new UserVO();
        vo.setLogin("rest-809");
        vo.setFirstName("John");
        vo.setLastName("Smith");
        vo.setEmail("");
        vo.putProperty("gender", "lu");

        final String stringuifiedAuth = stringuified(vo);
        final PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(entity);

        final int code = c.executeMethod(method);
        assertTrue(code == 406);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<ErrorVO> errors = parseErrorArray(body);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertTrue(errors.size() >= 2);
        assertNotNull(errors.get(0).getCode());
        assertNotNull(errors.get(0).getTranslation());
        assertNotNull(errors.get(1).getCode());
        assertNotNull(errors.get(1).getTranslation());

        final Identity savedIdent = securityManager.findIdentityByName("rest-809");
        assertNull(savedIdent);
    }

    @Test
    public void testDeleteUser() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // delete an authentication token
        final String request = "/users/" + id2.getKey();
        final DeleteMethod method = createDelete(request, MediaType.APPLICATION_XML, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        method.releaseConnection();

        final Identity deletedIdent = securityManager.loadIdentityByKey(id2.getKey());
        assertNotNull(deletedIdent);// Identity aren't deleted anymore
        assertEquals(Identity.STATUS_DELETED, deletedIdent.getStatus());
    }

    @Test
    public void testUserGroup() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // retrieve all groups
        final String request = "/users/" + id1.getKey() + "/groups";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);

        final String body = method.getResponseBodyAsString();
        final List<GroupVO> groups = parseGroupArray(body);
        assertNotNull(groups);
        assertTrue(groups.size() >= 4);
    }

    @Test
    public void testPortrait() throws IOException, URISyntaxException {
        final URL portraitUrl = RepositoryEntriesITCase.class.getResource("portrait.jpg");
        assertNotNull(portraitUrl);
        final File portrait = new File(portraitUrl.toURI());

        final HttpClient c = loginWithCookie("rest-one", "A6B7C8");

        // upload portrait
        final String request = "/users/" + id1.getKey() + "/portrait";
        final PostMethod method = createPost(request, MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", portrait), new StringPart("filename", "portrait.jpg") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        method.releaseConnection();

        // check if big and small portraits exist
        final DisplayPortraitManager dps = DisplayPortraitManager.getInstance();
        final File uploadDir = dps.getPortraitDir(id1);
        assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME).exists());
        assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME).exists());

        // check get portrait
        final String getRequest = "/users/" + id1.getKey() + "/portrait";
        final GetMethod getMethod = createGet(getRequest, MediaType.APPLICATION_OCTET_STREAM, true);
        final int getCode = c.executeMethod(getMethod);
        assertEquals(getCode, 200);
        final InputStream in = getMethod.getResponseBodyAsStream();
        int b = 0;
        int count = 0;
        while ((b = in.read()) > -1) {
            count++;
        }
        assertEquals(-1, b);// up to end of file
        assertTrue(count > 1000);// enough bytes
        method.releaseConnection();
    }

    protected List<UserVO> parseUserArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<UserVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected List<GroupVO> parseGroupArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<GroupVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
