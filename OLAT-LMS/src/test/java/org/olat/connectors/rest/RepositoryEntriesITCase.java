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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
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
import org.olat.connectors.rest.support.vo.RepositoryEntryVO;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseModule;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatJerseyTestCase;

@Ignore("ignored to be in sync with pom.xml")
public class RepositoryEntriesITCase extends OlatJerseyTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    public RepositoryEntriesITCase() {
        super();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        DBFactory.getInstance().intermediateCommit();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        DBFactory.getInstance().commitAndCloseSession();
    }

    @Test
    public void testGetEntries() throws HttpException, IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("repo/entries", MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();

        final List<RepositoryEntryVO> entryVoes = parseRepoArray(body);
        assertNotNull(entryVoes);
    }

    @Test
    public void testGetEntry() throws HttpException, IOException {
        final RepositoryEntry re = createRepository("Test GET repo entry", 83911l);

        final HttpClient c = loginWithCookie("administrator", "olat");

        final GetMethod method = createGet("repo/entries/" + re.getKey(), MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();

        final RepositoryEntryVO entryVo = parse(body, RepositoryEntryVO.class);
        assertNotNull(entryVo);
    }

    @Test
    public void testImportCp() throws HttpException, IOException, URISyntaxException {
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("cp-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final HttpClient c = loginWithCookie("administrator", "olat");
        final PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", cp), new StringPart("filename", "cp-demo.zip"), new StringPart("resourcename", "CP demo"),
                new StringPart("displayname", "CP demo") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
        assertNotNull(vo);

        final Long key = vo.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("CP demo", re.getDisplayname());
    }

    @Test
    public void testImportTest() throws HttpException, IOException, URISyntaxException {
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("qti-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final HttpClient c = loginWithCookie("administrator", "olat");
        final PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", cp), new StringPart("filename", "qti-demo.zip"), new StringPart("resourcename", "QTI demo"),
                new StringPart("displayname", "QTI demo") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
        assertNotNull(vo);

        final Long key = vo.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("QTI demo", re.getDisplayname());
        log.info(re.getOlatResource().getResourceableTypeName());
    }

    @Test
    public void testImportQuestionnaire() throws HttpException, IOException, URISyntaxException {
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("questionnaire-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final HttpClient c = loginWithCookie("administrator", "olat");
        final PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", cp), new StringPart("filename", "questionnaire-demo.zip"), new StringPart("resourcename", "Questionnaire demo"),
                new StringPart("displayname", "Questionnaire demo") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
        assertNotNull(vo);

        final Long key = vo.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("Questionnaire demo", re.getDisplayname());
        log.info(re.getOlatResource().getResourceableTypeName());
    }

    @Test
    public void testImportWiki() throws HttpException, IOException, URISyntaxException {
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("wiki-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final HttpClient c = loginWithCookie("administrator", "olat");
        final PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", cp), new StringPart("filename", "wiki-demo.zip"), new StringPart("resourcename", "Wiki demo"),
                new StringPart("displayname", "Wiki demo") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
        assertNotNull(vo);

        final Long key = vo.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("Wiki demo", re.getDisplayname());
        log.info(re.getOlatResource().getResourceableTypeName());
    }

    @Test
    public void testImportBlog() throws HttpException, IOException, URISyntaxException {
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("blog-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final HttpClient c = loginWithCookie("administrator", "olat");
        final PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", cp), new StringPart("filename", "blog-demo.zip"), new StringPart("resourcename", "Blog demo"),
                new StringPart("displayname", "Blog demo") };
        method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int code = c.executeMethod(method);
        assertTrue(code == 200 || code == 201);

        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
        assertNotNull(vo);

        final Long key = vo.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("Blog demo", re.getDisplayname());
        log.info(re.getOlatResource().getResourceableTypeName());
    }

    protected List<RepositoryEntryVO> parseRepoArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<RepositoryEntryVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RepositoryEntry createRepository(final String name, final Long resourceableId) {
        final OLATResourceable resourceable = new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return CourseModule.ORES_TYPE_COURSE;
            }

            @Override
            public Long getResourceableId() {
                return resourceableId;
            }
        };

        RepositoryEntry d = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(resourceable, false);
        if (d != null) {
            return d;
        }

        final OLATResourceManager rm = OLATResourceManager.getInstance();
        // create course and persist as OLATResourceImpl

        final OLATResource r = rm.createOLATResourceInstance(resourceable);
        DBFactory.getInstance().saveObject(r);
        DBFactory.getInstance().intermediateCommit();

        d = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance("Stéphane Rossé", name, "Repo entry");
        d.setOlatResource(r);
        d.setDisplayname(name);
        DBFactory.getInstance().saveObject(d);
        DBFactory.getInstance().intermediateCommit();
        return d;
    }
}
