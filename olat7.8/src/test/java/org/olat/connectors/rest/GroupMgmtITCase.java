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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.forum.MessageVO;
import org.olat.connectors.rest.support.vo.GroupInfoVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.connectors.rest.user.UserVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.forum.ForumService;
import org.olat.lms.group.BusinessGroupPropertyManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.properties.NarrowedPropertyManager;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the learning group web service
 * <P>
 * Initial Date: 7 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class GroupMgmtITCase extends OlatJerseyTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private Identity owner1, owner2, owner3, part1, part2, part3;
    private BusinessGroup g1, g2;
    private BusinessGroup g3, g4;
    private OLATResource course;
    private Message m1, m2, m3, m4, m5;

    private BusinessGroupService businessGroupService;
    @Autowired
    private BaseSecurity securityManager;
    /* mock for not-publish event */
    private PublishEventTO publishEventTO;

    /**
     * Set up a course with learn group and group area
     * 
     * @see org.olat.test.OlatJerseyTestCase#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        businessGroupService = applicationContext.getBean(BusinessGroupService.class);
        // create a course with learn group

        owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
        owner2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
        owner3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-three");
        part1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-four");
        part2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-five");
        part3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-six");

        final OLATResourceManager rm = OLATResourceManager.getInstance();
        // create course and persist as OLATResourceImpl
        final OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse", System.currentTimeMillis());
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance("administrator");
        re.setCanDownload(false);
        re.setCanLaunch(true);
        re.setDisplayname("rest-re");
        re.setResourcename("-");
        re.setAccess(0);// Access for nobody
        re.setOwnerGroup(null);

        // create security group
        final SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
        // member of this group may modify member's membership
        securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
        // members of this group are always authors also
        securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
        securityManager.addIdentityToSecurityGroup(owner1, newGroup);
        re.setOwnerGroup(newGroup);

        course = rm.createOLATResourceInstance(resourceable);
        DBFactory.getInstance().saveObject(course);
        DBFactory.getInstance().intermediateCommit();

        final OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(resourceable);
        re.setOlatResource(ores);
        RepositoryServiceImpl.getInstance().saveRepositoryEntry(re);
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

        // permission to see owners and participants
        final BusinessGroupPropertyManager bgpm1 = new BusinessGroupPropertyManager(g1);
        bgpm1.updateDisplayMembers(false, false, false);
        final BusinessGroupPropertyManager bgpm2 = new BusinessGroupPropertyManager(g2);
        bgpm2.updateDisplayMembers(true, true, false);

        // members g1
        securityManager.addIdentityToSecurityGroup(owner1, g1.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(owner2, g1.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(part1, g1.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(part2, g1.getPartipiciantGroup());

        // members g2
        securityManager.addIdentityToSecurityGroup(owner1, g2.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(part1, g2.getPartipiciantGroup());

        // 2) context two: right groups
        final BGContext c2 = businessGroupService.createAndAddBGContextToResource("c2name-area", course, BusinessGroup.TYPE_RIGHTGROUP, owner2, true);
        // groups
        g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g3", null, null, null, null/* enableWaitinglist */,
                null/* enableAutoCloseRanks */, c2);
        g4 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g4", null, null, null, null/* enableWaitinglist */,
                null/* enableAutoCloseRanks */, c2);
        // members
        securityManager.addIdentityToSecurityGroup(owner1, g3.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(owner2, g4.getPartipiciantGroup());

        DBFactory.getInstance().closeSession(); // simulate user clicks

        // 3) collaboration tools
        final CollaborationTools collabTools1 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
        collabTools1.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
        collabTools1.setToolEnabled(CollaborationTools.TOOL_WIKI, true);
        collabTools1.saveNews("<p>Hello world</p>");

        try {
            collabTools1.createForumController(null, null, true, false, null);
        } catch (final Exception e) {
            // will fail but generate the forum key
        }

        final CollaborationTools collabTools2 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
        collabTools2.setToolEnabled(CollaborationTools.TOOL_FORUM, true);

        DBFactory.getInstance().closeSession(); // simulate user clicks

        // 4) fill forum for g1

        final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(g1);
        final PropertyImpl forumKeyProperty = npm.findProperty(null, null, PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS, PropertyManagerEBL.KEY_FORUM);
        final ForumService fm = getForumService();
        final Forum forum = fm.loadForum(forumKeyProperty.getLongValue());

        m1 = fm.createMessage();
        m1.setTitle("Thread-1");
        m1.setBody("Body of Thread-1");
        publishEventTO = createPublishEventTO(owner1, PublishEventTO.EventType.NEW, m1);
        fm.addTopMessage(forum, m1, owner1, publishEventTO);

        m2 = fm.createMessage();
        m2.setTitle("Thread-2");
        m2.setBody("Body of Thread-2");
        publishEventTO = createPublishEventTO(owner2, PublishEventTO.EventType.NEW, m2);
        fm.addTopMessage(forum, m2, owner2, publishEventTO);

        DBFactory.getInstance().intermediateCommit();

        m3 = fm.createMessage();
        m3.setTitle("Message-1.1");
        m3.setBody("Body of Message-1.1");
        fm.replyToMessage(owner3, m1, m3, publishEventTO);

        m4 = fm.createMessage();
        m4.setTitle("Message-1.1.1");
        m4.setBody("Body of Message-1.1.1");
        fm.replyToMessage(part1, m3, m4, publishEventTO);

        m5 = fm.createMessage();
        m5.setTitle("Message-1.2");
        m5.setBody("Body of Message-1.2");
        fm.replyToMessage(part2, m1, m5, publishEventTO);

        DBFactory.getInstance().intermediateCommit();
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    private PublishEventTO createPublishEventTO(Identity creatorIdentity, PublishEventTO.EventType type, Message message) {
        return PublishEventTO.getValidInstance(ContextType.COURSE, 0L, "AAA_course", 0L, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, 0L, "a_Forum",
                message.getTitle(), creatorIdentity, type);

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
    public void testGetGroupsAdmin() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<GroupVO> groups = parseGroupArray(body);
        assertNotNull(groups);
        assertTrue(groups.size() >= 4);// g1, g2, g3 and g4 + from olat

        final Set<Long> keys = new HashSet<Long>();
        for (final GroupVO vo : groups) {
            keys.add(vo.getKey());
        }

        assertTrue(keys.contains(g1.getKey()));
        assertTrue(keys.contains(g2.getKey()));
        assertTrue(keys.contains(g3.getKey()));
        assertTrue(keys.contains(g4.getKey()));
    }

    @Test
    public void testGetGroups() throws IOException {
        final HttpClient c = loginWithCookie("rest-four", "A6B7C8");

        final String request = "/groups";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<GroupVO> groups = parseGroupArray(body);
        assertNotNull(groups);
        assertTrue(groups.size() >= 2);// g1, g2, g3 and g4 + from olat

        final Set<Long> keys = new HashSet<Long>();
        for (final GroupVO vo : groups) {
            keys.add(vo.getKey());
        }

        assertTrue(keys.contains(g1.getKey()));
        assertTrue(keys.contains(g2.getKey()));
        assertFalse(keys.contains(g3.getKey()));
        assertFalse(keys.contains(g4.getKey()));
    }

    @Test
    public void testGetGroupAdmin() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups/" + g1.getKey();
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final GroupVO vo = parse(body, GroupVO.class);
        assertNotNull(vo);
        assertEquals(vo.getKey(), g1.getKey());
    }

    @Test
    public void testGetGroupInfos() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups/" + g1.getKey() + "/infos";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final GroupInfoVO vo = parse(body, GroupInfoVO.class);
        assertNotNull(vo);
        assertEquals(Boolean.TRUE, vo.getHasWiki());
        assertEquals("<p>Hello world</p>", vo.getNews());
        assertNotNull(vo.getForumKey());
    }

    // the web service generate the forum key
    @Test
    public void testGetGroupInfos2() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups/" + g2.getKey() + "/infos";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final GroupInfoVO vo = parse(body, GroupInfoVO.class);
        assertNotNull(vo);
        assertEquals(Boolean.FALSE, vo.getHasWiki());
        assertNull(vo.getNews());
        assertNotNull(vo.getForumKey());
    }

    @Test
    public void testGetThreads() throws IOException {
        final HttpClient c = loginWithCookie("rest-one", "A6B7C8");

        final String request = "/groups/" + g1.getKey() + "/forum/threads";
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<MessageVO> messages = parseMessageArray(body);

        assertNotNull(messages);
        assertEquals(2, messages.size());
    }

    @Test
    public void testGetMessages() throws IOException {
        final HttpClient c = loginWithCookie("rest-one", "A6B7C8");

        final String request = "/groups/" + g1.getKey() + "/forum/posts/" + m1.getKey();
        final GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<MessageVO> messages = parseMessageArray(body);

        assertNotNull(messages);
        assertEquals(4, messages.size());
    }

    @Test
    public void testUpdateCourseGroup() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GroupVO vo = new GroupVO();
        vo.setKey(g1.getKey());
        vo.setName("rest-g1-mod");
        vo.setDescription("rest-g1 description");
        vo.setMinParticipants(g1.getMinParticipants());
        vo.setMaxParticipants(g1.getMaxParticipants());
        vo.setType(g1.getType());

        final String stringuifiedAuth = stringuified(vo);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        final String request = "/groups/" + g1.getKey();
        final PostMethod method = createPost(request, MediaType.APPLICATION_JSON, true);
        method.setRequestEntity(entity);
        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey(), false);
        assertNotNull(bg);
        assertEquals(bg.getKey(), vo.getKey());
        assertEquals(bg.getName(), "rest-g1-mod");
        assertEquals(bg.getDescription(), "rest-g1 description");
    }

    @Test
    public void testDeleteCourseGroup() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups/" + g1.getKey();
        final DeleteMethod method = createDelete(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);

        final BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey(), false);
        assertNull(bg);
    }

    @Test
    public void testGetParticipantsAdmin() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final String request = "/groups/" + g1.getKey() + "/participants";
        final HttpMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        final List<UserVO> participants = parseUserArray(body);
        assertNotNull(participants);
        assertEquals(participants.size(), 2);

        Long idKey1 = null;
        Long idKey2 = null;
        for (final UserVO participant : participants) {
            if (participant.getKey().equals(part1.getKey())) {
                idKey1 = part1.getKey();
            } else if (participant.getKey().equals(part2.getKey())) {
                idKey2 = part2.getKey();
            }
        }
        assertNotNull(idKey1);
        assertNotNull(idKey2);
    }

    @Test
    public void testGetParticipants() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("rest-four", "A6B7C8");

        final String request = "/groups/" + g1.getKey() + "/participants";
        final HttpMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);

        // g1 not authorized
        assertEquals(code, 401);
    }

    @Test
    public void testGetOwnersAdmin() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final String request = "/groups/" + g1.getKey() + "/owners";
        final HttpMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        final List<UserVO> owners = parseUserArray(body);
        assertNotNull(owners);
        assertEquals(owners.size(), 2);

        Long idKey1 = null;
        Long idKey2 = null;
        for (final UserVO participant : owners) {
            if (participant.getKey().equals(owner1.getKey())) {
                idKey1 = owner1.getKey();
            } else if (participant.getKey().equals(owner2.getKey())) {
                idKey2 = owner2.getKey();
            }
        }
        assertNotNull(idKey1);
        assertNotNull(idKey2);
    }

    @Test
    public void testGetOwners() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("rest-four", "A6B7C8");
        final String request = "/groups/" + g1.getKey() + "/owners";
        final HttpMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        // not authorized
        assertEquals(code, 401);
    }

    @Test
    public void testAddParticipant() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final String request = "/groups/" + g1.getKey() + "/participants/" + part3.getKey();
        final HttpMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        method.releaseConnection();

        assertTrue(code == 200 || code == 201);

        final List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(g1.getPartipiciantGroup());
        boolean found = false;
        for (final Identity participant : participants) {
            if (participant.getKey().equals(part3.getKey())) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testRemoveParticipant() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final String request = "/groups/" + g1.getKey() + "/participants/" + part2.getKey();
        final HttpMethod method = createDelete(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        method.releaseConnection();

        assertTrue(code == 200);

        final List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(g1.getPartipiciantGroup());
        boolean found = false;
        for (final Identity participant : participants) {
            if (participant.getKey().equals(part2.getKey())) {
                found = true;
            }
        }

        assertFalse(found);
    }

    @Test
    public void testAddTutor() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final String request = "/groups/" + g1.getKey() + "/owners/" + owner3.getKey();
        final HttpMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        method.releaseConnection();

        assertTrue(code == 200 || code == 201);

        final List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(g1.getOwnerGroup());
        boolean found = false;
        for (final Identity owner : owners) {
            if (owner.getKey().equals(owner3.getKey())) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testRemoveTutor() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final String request = "/groups/" + g1.getKey() + "/owners/" + owner2.getKey();
        final HttpMethod method = createDelete(request, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        method.releaseConnection();

        assertTrue(code == 200);

        final List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(g1.getOwnerGroup());
        boolean found = false;
        for (final Identity owner : owners) {
            if (owner.getKey().equals(owner2.getKey())) {
                found = true;
            }
        }

        assertFalse(found);
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

    protected List<MessageVO> parseMessageArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<MessageVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
