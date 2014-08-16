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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.repository.course.CoursesWebService;
import org.olat.connectors.rest.support.vo.LinkVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.course.ICourse;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore("ignored to be in sync with pom.xml")
public class CoursesResourcesFoldersITCase extends OlatJerseyTestCase {

    private static ICourse course1;
    private static Identity admin;

    @Autowired
    private BaseSecurity securityManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        admin = securityManager.findIdentityByName("administrator");
        course1 = CoursesWebService.createEmptyCourse(admin, "course1", "course1 long name", null);

        // copy a couple of files in the resource folder
        final VFSContainer container = course1.getCourseFolderContainer();
        copyFileInResourceFolder(container, "singlepage.html", "1_");
        copyFileInResourceFolder(container, "cp-demo.zip", "1_");
        final VFSContainer subContainer = container.createChildContainer("SubDir");
        copyFileInResourceFolder(subContainer, "singlepage.html", "2_");
        final VFSContainer subSubContainer = subContainer.createChildContainer("SubSubDir");
        copyFileInResourceFolder(subSubContainer, "singlepage.html", "3_");

        DBFactory.getInstance().intermediateCommit();
    }

    private void copyFileInResourceFolder(final VFSContainer container, final String filename, final String prefix) {
        final InputStream pageStream = RepositoryEntriesITCase.class.getResourceAsStream(filename);
        final VFSLeaf item = container.createChildLeaf(prefix + filename);
        final OutputStream outStream = item.getOutputStream(false);
        FileUtils.copy(pageStream, outStream);
        FileUtils.closeSafely(pageStream);
        FileUtils.closeSafely(outStream);
    }

    @Test
    public void testGetFiles() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final URI uri = UriBuilder.fromUri(getCourseFolderURI()).build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);

        final String body = method.getResponseBodyAsString();
        final List<LinkVO> links = parseLinkArray(body);
        assertNotNull(links);
        assertEquals(3, links.size());
    }

    @Test
    public void testGetFilesDeeper() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir").build();
        final GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);

        final String body = method.getResponseBodyAsString();
        final List<LinkVO> links = parseLinkArray(body);
        assertNotNull(links);
        assertEquals(1, links.size());
        assertEquals("3_singlepage.html", links.get(0).getTitle());
    }

    @Test
    public void testGetFileDeep() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");
        final URI uri = UriBuilder.fromUri(getCourseFolderURI()).path("SubDir").path("SubSubDir").path("SubSubSubDir").path("3_singlepage.html").build();
        final GetMethod method = createGet(uri, "*/*", true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);

        final String body = method.getResponseBodyAsString();
        assertNotNull(body);
        assertTrue(body.startsWith("<html>"));

        String contentType = null;
        for (final Header header : method.getResponseHeaders()) {
            if ("Content-Type".equals(header.getName())) {
                contentType = header.getValue();
                break;
            }
        }
        assertNotNull(contentType);
        assertEquals("text/html", contentType);
    }

    private URI getCourseFolderURI() {
        return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString()).path("resourcefolders").path("coursefolder")
                .build();
    }
}
