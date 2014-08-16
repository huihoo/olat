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

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.repository.course.CoursesWebService;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the security of a course
 * <P>
 * Initial Date: 6 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class CourseSecurityITCase extends OlatJerseyTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private Identity admin, id1, auth1, auth2;
    private ICourse course;
    @Autowired
    private BaseSecurity securityManager;

    /**
     * SetUp is called before each test.
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        try {
            // create course and persist as OLATResourceImpl
            admin = securityManager.findIdentityByName("administrator");
            id1 = JunitTestHelper.createAndPersistIdentityAsUser("id-c-s-0");
            auth1 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-1");
            auth2 = JunitTestHelper.createAndPersistIdentityAsAuthor("id-c-s-2");

            course = CoursesWebService.createEmptyCourse(admin, "course-security-2", "Test course for the security test", null);
            DBFactory.getInstance().intermediateCommit();

            final RepositoryService rm = RepositoryServiceImpl.getInstance();
            final RepositoryEntry re = rm.lookupRepositoryEntry(course, false);
            final IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(Collections.singletonList(auth2));
            rm.addOwners(admin, identitiesAddEvent, re);

            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
        }
    }

    @Test
    public void testAdminCanEditCourse() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").build();
        final PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("position", "0"), new NameValuePair("shortTitle", "Structure-admin-0"),
                new NameValuePair("longTitle", "Structure-long-admin-0"), new NameValuePair("objectives", "Structure-objectives-admin-0") });
        final int code = c.executeMethod(method);
        assertEquals(200, code);
    }

    @Test
    public void testIdCannotEditCourse() throws IOException {
        final HttpClient c = loginWithCookie("id-c-s-0", "A6B7C8");

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").build();
        final PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("position", "0"), new NameValuePair("shortTitle", "Structure-id-0"),
                new NameValuePair("longTitle", "Structure-long-id-0"), new NameValuePair("objectives", "Structure-objectives-id-0") });
        final int code = c.executeMethod(method);
        assertEquals(401, code);
    }

    @Test
    public void testAuthorCannotEditCourse() throws IOException {
        // author but not owner
        final HttpClient c = loginWithCookie("id-c-s-1", "A6B7C8");

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").build();
        final PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("position", "0"), new NameValuePair("shortTitle", "Structure-id-0"),
                new NameValuePair("longTitle", "Structure-long-id-0"), new NameValuePair("objectives", "Structure-objectives-id-0") });
        final int code = c.executeMethod(method);
        assertEquals(401, code);
    }

    @Test
    public void testAuthorCanEditCourse() throws IOException {
        // author and owner
        final HttpClient c = loginWithCookie("id-c-s-2", "A6B7C8");

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").build();
        final PutMethod method = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
        method.setQueryString(new NameValuePair[] { new NameValuePair("position", "0"), new NameValuePair("shortTitle", "Structure-id-0"),
                new NameValuePair("longTitle", "Structure-long-id-0"), new NameValuePair("objectives", "Structure-objectives-id-0") });
        final int code = c.executeMethod(method);
        assertEquals(200, code);
    }

    private UriBuilder getCoursesUri() {
        return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
    }

    private UriBuilder getElementsUri(final ICourse c) {
        return getCoursesUri().path(c.getResourceableId().toString()).path("elements");
    }
}
