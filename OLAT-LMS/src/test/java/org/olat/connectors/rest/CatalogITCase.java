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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.catalog.CatalogEntryVO;
import org.olat.connectors.rest.user.UserVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.catalog.CatalogService;
import org.olat.lms.course.CourseModule;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the catalog RESt API
 * <P>
 * Initial Date: 6 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class CatalogITCase extends OlatJerseyTestCase {

    private Identity admin, id1;
    private CatalogEntry root1, entry1, entry2, subEntry11, subEntry12;
    private CatalogService catalogService;

    @Autowired
    private BaseSecurity securityManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-catalog-one");
        JunitTestHelper.createAndPersistIdentityAsUser("rest-catalog-two");

        admin = securityManager.findIdentityByName("administrator");

        // create a catalog
        final CatalogService catalogService = CoreSpringFactory.getBean(CatalogService.class);
        root1 = (CatalogEntry) catalogService.getRootCatalogEntries().get(0);

        entry1 = catalogService.createCatalogEntry();
        entry1.setType(CatalogEntry.TYPE_NODE);
        entry1.setName("Entry-1");
        entry1.setDescription("Entry-description-1");
        entry1.setOwnerGroup(securityManager.createAndPersistSecurityGroup());
        catalogService.addCatalogEntry(root1, entry1);

        DBFactory.getInstance().intermediateCommit();
        entry1 = catalogService.loadCatalogEntry(entry1);
        securityManager.addIdentityToSecurityGroup(admin, entry1.getOwnerGroup());

        subEntry11 = catalogService.createCatalogEntry();
        subEntry11.setType(CatalogEntry.TYPE_NODE);
        subEntry11.setName("Sub-entry-11");
        subEntry11.setDescription("Sub-entry-description-11");
        catalogService.addCatalogEntry(entry1, subEntry11);

        subEntry12 = catalogService.createCatalogEntry();
        subEntry12.setType(CatalogEntry.TYPE_NODE);
        subEntry12.setName("Sub-entry-12");
        subEntry12.setDescription("Sub-entry-description-12");
        catalogService.addCatalogEntry(entry1, subEntry12);

        entry2 = catalogService.createCatalogEntry();
        entry2.setType(CatalogEntry.TYPE_NODE);
        entry2.setName("Entry-2");
        entry2.setDescription("Entry-description-2");
        catalogService.addCatalogEntry(root1, entry2);

        DBFactory.getInstance().intermediateCommit();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        DBFactory.getInstance().closeSession();
    }

    @Test
    public void testGetRoots() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<CatalogEntryVO> vos = parseEntryArray(body);
        assertNotNull(vos);
        assertEquals(1, vos.size());// Root-1
    }

    @Test
    public void testGetChild() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);
        assertEquals(entry1.getName(), vo.getName());
        assertEquals(entry1.getDescription(), vo.getDescription());
    }

    @Test
    public void testGetChildren() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(root1.getKey().toString()).path("children").build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<CatalogEntryVO> vos = parseEntryArray(body);
        assertNotNull(vos);
        assertTrue(vos.size() >= 2);
    }

    @Test
    public void testPutCategoryJson() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final CatalogEntryVO subEntry = new CatalogEntryVO();
        subEntry.setName("Sub-entry-1");
        subEntry.setDescription("Sub-entry-description-1");
        subEntry.setType(CatalogEntry.TYPE_NODE);
        final String entity = stringuified(subEntry);

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        final RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(requestEntity);

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final List<CatalogEntry> children = catalogService.getChildrenOf(entry1);
        boolean saved = false;
        for (final CatalogEntry child : children) {
            if (vo.getKey().equals(child.getKey())) {
                saved = true;
                break;
            }
        }

        assertTrue(saved);
    }

    @Test
    public void testPutCategoryQuery() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("name", "Sub-entry-2"), new NameValuePair("description", "Sub-entry-description-2"),
                new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)) });

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final List<CatalogEntry> children = catalogService.getChildrenOf(entry1);
        boolean saved = false;
        for (final CatalogEntry child : children) {
            if (vo.getKey().equals(child.getKey())) {
                saved = true;
                break;
            }
        }

        assertTrue(saved);
    }

    @Test
    public void testPutCatalogEntryJson() throws IOException {
        final RepositoryEntry re = createRepository("put-cat-entry-json", 6458438l);

        final HttpClient c = loginWithCookie("administrator", "olat");

        final CatalogEntryVO subEntry = new CatalogEntryVO();
        subEntry.setName("Sub-entry-1");
        subEntry.setDescription("Sub-entry-description-1");
        subEntry.setType(CatalogEntry.TYPE_NODE);
        subEntry.setRepositoryEntryKey(re.getKey());
        final String entity = stringuified(subEntry);

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        final RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(requestEntity);

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final List<CatalogEntry> children = catalogService.getChildrenOf(entry1);
        CatalogEntry ce = null;
        for (final CatalogEntry child : children) {
            if (vo.getKey().equals(child.getKey())) {
                ce = child;
                break;
            }
        }

        assertNotNull(ce);
        assertNotNull(ce.getRepositoryEntry());
        assertEquals(re.getKey(), ce.getRepositoryEntry().getKey());
    }

    @Test
    public void testPutCatalogEntryQuery() throws IOException {
        final RepositoryEntry re = createRepository("put-cat-entry-query", 6458439l);

        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("name", "Sub-entry-2"), new NameValuePair("description", "Sub-entry-description-2"),
                new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)), new NameValuePair("repoEntryKey", re.getKey().toString()) });

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final List<CatalogEntry> children = catalogService.getChildrenOf(entry1);
        CatalogEntry ce = null;
        for (final CatalogEntry child : children) {
            if (vo.getKey().equals(child.getKey())) {
                ce = child;
                break;
            }
        }

        assertNotNull(ce);
        assertNotNull(ce.getRepositoryEntry());
        assertEquals(re.getKey(), ce.getRepositoryEntry().getKey());
    }

    @Test
    public void testUpdateCatalogEntryJson() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final CatalogEntryVO entry = new CatalogEntryVO();
        entry.setName("Entry-1-b");
        entry.setDescription("Entry-description-1-b");
        entry.setType(CatalogEntry.TYPE_NODE);
        final String entity = stringuified(entry);

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
        final RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
        method.setRequestEntity(requestEntity);

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final CatalogEntry updatedEntry = catalogService.loadCatalogEntry(entry1);
        assertEquals("Entry-1-b", updatedEntry.getName());
        assertEquals("Entry-description-1-b", updatedEntry.getDescription());
    }

    @Test
    public void testUpdateCatalogEntryQuery() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
        final PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("name", "Entry-2-b"), new NameValuePair("description", "Entry-description-2-b"),
                new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)) });

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final CatalogEntry updatedEntry = catalogService.loadCatalogEntry(entry2);
        assertEquals("Entry-2-b", updatedEntry.getName());
        assertEquals("Entry-description-2-b", updatedEntry.getDescription());
    }

    @Test
    public void testUpdateCatalogEntryForm() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
        final PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
        method.addParameters(new NameValuePair[] { new NameValuePair("name", "Entry-2-c"), new NameValuePair("description", "Entry-description-2-c"),
                new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)) });

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
        assertNotNull(vo);

        final CatalogEntry updatedEntry = catalogService.loadCatalogEntry(entry2);
        assertEquals("Entry-2-c", updatedEntry.getName());
        assertEquals("Entry-description-2-c", updatedEntry.getDescription());
    }

    @Test
    public void testDeleteCatalogEntry() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
        final DeleteMethod method = createDelete(uri, MediaType.APPLICATION_JSON, true);

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        method.releaseConnection();

        final List<CatalogEntry> entries = catalogService.getChildrenOf(root1);
        for (final CatalogEntry entry : entries) {
            assertFalse(entry.getKey().equals(entry2.getKey()));
        }
    }

    @Test
    public void testGetOwners() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);

        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final List<UserVO> voes = parseUserArray(body);
        assertNotNull(voes);

        final CatalogEntry entry = catalogService.loadCatalogEntry(entry1.getKey());
        final List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
        assertNotNull(identities);
        assertEquals(identities.size(), voes.size());
    }

    @Test
    public void testGetOwner() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // admin is owner
        URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").path(admin.getKey().toString()).build();
        GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);

        int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final UserVO vo = parse(body, UserVO.class);
        assertNotNull(vo);

        // id1 is not owner
        uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").path(id1.getKey().toString()).build();
        method = createGet(uri, MediaType.APPLICATION_JSON, true);

        code = c.executeMethod(method);
        assertEquals(404, code);
    }

    @Test
    public void testAddOwner() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").path(id1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);

        final int code = c.executeMethod(method);
        method.releaseConnection();
        assertEquals(200, code);

        final CatalogEntry entry = catalogService.loadCatalogEntry(entry1.getKey());
        final List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
        boolean found = false;
        for (final Identity identity : identities) {
            if (identity.getKey().equals(id1.getKey())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testRemoveOwner() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").path(id1.getUser().getKey().toString())
                .build();
        final DeleteMethod method = createDelete(uri, MediaType.APPLICATION_JSON, true);

        final int code = c.executeMethod(method);
        method.releaseConnection();
        assertEquals(200, code);

        final CatalogEntry entry = catalogService.loadCatalogEntry(entry1.getKey());
        final List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
        boolean found = false;
        for (final Identity identity : identities) {
            if (identity.getKey().equals(id1.getKey())) {
                found = true;
            }
        }
        assertFalse(found);
    }

    @Test
    public void testBasicSecurityPutCall() throws IOException {
        final HttpClient c = loginWithCookie("rest-catalog-two", "A6B7C8");

        final URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("name", "Not-sub-entry-3"), new NameValuePair("description", "Not-sub-entry-description-3"),
                new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)) });

        final int code = c.executeMethod(method);
        assertEquals(401, code);
        method.releaseConnection();

        final List<CatalogEntry> children = catalogService.getChildrenOf(entry1);
        boolean saved = false;
        for (final CatalogEntry child : children) {
            if ("Not-sub-entry-3".equals(child.getName())) {
                saved = true;
                break;
            }
        }

        assertFalse(saved);
    }

    protected List<CatalogEntryVO> parseEntryArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<CatalogEntryVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private RepositoryEntry createRepository(final String name, final Long resourceableId) {
        final OLATResourceable resourceable = new OLATResourceable() {
            public String getResourceableTypeName() {
                return CourseModule.ORES_TYPE_COURSE;
            }

            public Long getResourceableId() {
                return resourceableId;
            }
        };

        final OLATResourceManager rm = OLATResourceManager.getInstance();
        // create course and persist as OLATResourceImpl

        OLATResource r = rm.findResourceable(resourceable);
        if (r == null) {
            r = rm.createOLATResourceInstance(resourceable);
        }
        DBFactory.getInstance().saveObject(r);
        DBFactory.getInstance().intermediateCommit();

        RepositoryEntry d = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(resourceable, false);
        if (d == null) {
            d = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance("Stéphane Rossé", name, "Repo entry");
            d.setOlatResource(r);
            d.setDisplayname(name);
            DBFactory.getInstance().saveObject(d);
        }
        DBFactory.getInstance().intermediateCommit();
        return d;
    }
}
