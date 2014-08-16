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
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOADRESSES;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOCOACHES;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_MBODY_DEFAULT;
import static org.olat.presentation.course.nodes.co.COEditController.CONFIG_KEY_MSUBJECT_DEFAULT;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.repository.course.CoursesWebService;
import org.olat.connectors.rest.support.vo.CourseNodeVO;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test the creation and management of contact building block
 * <P>
 * Initial Date: 6 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
@Ignore("ignored to be in sync with pom.xml")
public class CoursesContactElementITCase extends OlatJerseyTestCase {

    private Identity admin;
    private ICourse course1;
    private String rootNodeId;

    @Autowired
    private BaseSecurity securityManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        admin = securityManager.findIdentityByName("administrator");
        course1 = CoursesWebService.createEmptyCourse(admin, "course-rest-contacts", "Course to test the contacts elements", null);
        DBFactory.getInstance().intermediateCommit();

        rootNodeId = course1.getEditorTreeModel().getRootNode().getIdent();
    }

    @Test
    public void testBareBoneConfig() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an contact node
        final URI newContactUri = getElementsUri(course1).path("contact").queryParam("parentNodeId", rootNodeId).queryParam("position", "0")
                .queryParam("shortTitle", "Contact-0").queryParam("longTitle", "Contact-long-0").queryParam("objectives", "Contact-objectives-0").build();
        final PutMethod method = createPut(newContactUri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();

        assertEquals(200, code);
        final CourseNodeVO contactNode = parse(body, CourseNodeVO.class);
        assertNotNull(contactNode);
        assertNotNull(contactNode.getId());
        assertEquals(contactNode.getShortTitle(), "Contact-0");
        assertEquals(contactNode.getLongTitle(), "Contact-long-0");
        assertEquals(contactNode.getLearningObjectives(), "Contact-objectives-0");
        assertEquals(contactNode.getParentId(), rootNodeId);
    }

    @Test
    public void testFullConfig() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an contact node
        final URI newContactUri = getElementsUri(course1).path("contact").queryParam("parentNodeId", rootNodeId).queryParam("position", "0")
                .queryParam("shortTitle", "Contact-1").queryParam("longTitle", "Contact-long-1").queryParam("objectives", "Contact-objectives-1")
                .queryParam("coaches", "true").queryParam("participants", "true").queryParam("groups", "").queryParam("areas", "")
                .queryParam("to", "test@frentix.com;test2@frentix.com").queryParam("defaultSubject", "Hello by contact 1")
                .queryParam("defaultBody", "Hello by contact 1 body").build();
        final PutMethod method = createPut(newContactUri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();

        // check the return values
        assertEquals(200, code);
        final CourseNodeVO contactNode = parse(body, CourseNodeVO.class);
        assertNotNull(contactNode);
        assertNotNull(contactNode.getId());

        // check the persisted value
        final ICourse course = CourseFactory.loadCourse(course1.getResourceableId());
        final TreeNode node = course.getEditorTreeModel().getNodeById(contactNode.getId());
        assertNotNull(node);
        final CourseEditorTreeNode editorCourseNode = (CourseEditorTreeNode) node;
        final CourseNode courseNode = editorCourseNode.getCourseNode();
        final ModuleConfiguration config = courseNode.getModuleConfiguration();
        assertNotNull(config);

        assertEquals(config.getBooleanEntry(CONFIG_KEY_EMAILTOCOACHES), true);
        assertEquals(config.getBooleanEntry(CONFIG_KEY_EMAILTOPARTICIPANTS), true);

        final List<String> tos = (List<String>) config.get(CONFIG_KEY_EMAILTOADRESSES);
        assertNotNull(tos);
        assertEquals(2, tos.size());

        assertEquals(config.get(CONFIG_KEY_MSUBJECT_DEFAULT), "Hello by contact 1");
        assertEquals(config.get(CONFIG_KEY_MBODY_DEFAULT), "Hello by contact 1 body");
    }

    private UriBuilder getElementsUri(final ICourse course) {
        return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course.getResourceableId().toString()).path("elements");
    }

}
