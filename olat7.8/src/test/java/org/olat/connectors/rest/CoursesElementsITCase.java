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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.support.vo.CourseNodeVO;
import org.olat.connectors.rest.support.vo.CourseVO;
import org.olat.connectors.rest.support.vo.GroupVO;
import org.olat.connectors.rest.support.vo.RepositoryEntryVO;
import org.olat.connectors.rest.support.vo.elements.SurveyConfigVO;
import org.olat.connectors.rest.support.vo.elements.TaskConfigVO;
import org.olat.connectors.rest.support.vo.elements.TestConfigVO;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.BCCourseNode;
import org.olat.lms.course.nodes.COCourseNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.nodes.FOCourseNode;
import org.olat.lms.course.nodes.IQSURVCourseNode;
import org.olat.lms.course.nodes.IQTESTCourseNode;
import org.olat.lms.course.nodes.MSCourseNode;
import org.olat.lms.course.nodes.SPCourseNode;
import org.olat.lms.course.nodes.STCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.TUCourseNode;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.test.OlatJerseyTestCase;

@Ignore("ignored to be in sync with pom.xml")
public class CoursesElementsITCase extends OlatJerseyTestCase {

    @Test
    public void testCreateCoursePost() throws IOException, URISyntaxException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an empty course
        final URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CourseVO course = parse(body, CourseVO.class);
        assertNotNull(course);
        assertNotNull(course.getKey());
        assertNotNull(course.getEditorRootNodeId());

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").build();
        final PostMethod newStructureMethod = createPost(newStructureUri, MediaType.APPLICATION_JSON, true);
        newStructureMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newStructureMethod.addParameter("position", "0");
        newStructureMethod.addParameter("shortTitle", "Structure-0");
        newStructureMethod.addParameter("longTitle", "Structure-long-0");
        newStructureMethod.addParameter("objectives", "Structure-objectives-0");
        final int newStructureCode = c.executeMethod(newStructureMethod);
        assertTrue(newStructureCode == 200 || newStructureCode == 201);
        final String newStructureBody = newStructureMethod.getResponseBodyAsString();
        final CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
        assertNotNull(structureNode);
        assertNotNull(structureNode.getId());
        assertEquals(structureNode.getShortTitle(), "Structure-0");
        assertEquals(structureNode.getLongTitle(), "Structure-long-0");
        assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0");
        assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());

        // create single page
        final URL pageUrl = RepositoryEntriesITCase.class.getResource("singlepage.html");
        assertNotNull(pageUrl);
        final File page = new File(pageUrl.toURI());

        final URI newPageUri = getElementsUri(course).path("singlepage").build();
        final PostMethod newPageMethod = createPost(newPageUri, MediaType.APPLICATION_JSON, true);
        newPageMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", page), new StringPart("filename", page.getName()), new StringPart("parentNodeId", course.getEditorRootNodeId()),
                new StringPart("position", "1"), new StringPart("shortTitle", "Single-Page-0"), new StringPart("longTitle", "Single-Page-long-0"),
                new StringPart("objectives", "Single-Page-objectives-0") };
        newPageMethod.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int newPageCode = c.executeMethod(newPageMethod);
        assertTrue(newPageCode == 200 || newPageCode == 201);
        final String newPageBody = newPageMethod.getResponseBodyAsString();
        final CourseNodeVO pageNode = parse(newPageBody, CourseNodeVO.class);
        assertNotNull(pageNode);
        assertNotNull(pageNode.getId());
        assertEquals(pageNode.getShortTitle(), "Single-Page-0");
        assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
        assertEquals(pageNode.getLearningObjectives(), "Single-Page-objectives-0");
        assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());

        // create a folder node
        final URI newFolderUri = getElementsUri(course).path("folder").build();
        final PostMethod newFolderMethod = createPost(newFolderUri, MediaType.APPLICATION_JSON, true);
        newFolderMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newFolderMethod.addParameter("position", "2");
        newFolderMethod.addParameter("shortTitle", "Folder-0");
        newFolderMethod.addParameter("longTitle", "Folder-long-0");
        newFolderMethod.addParameter("objectives", "Folder-objectives-0");
        final int newFolderCode = c.executeMethod(newFolderMethod);
        assertTrue(newFolderCode == 200 || newFolderCode == 201);
        final String newFolderBody = newFolderMethod.getResponseBodyAsString();
        final CourseNodeVO folderNode = parse(newFolderBody, CourseNodeVO.class);
        assertNotNull(folderNode);
        assertNotNull(folderNode.getId());
        assertEquals(folderNode.getShortTitle(), "Folder-0");
        assertEquals(folderNode.getLongTitle(), "Folder-long-0");
        assertEquals(folderNode.getLearningObjectives(), "Folder-objectives-0");
        assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());

        // create a forum node
        final URI newForumUri = getElementsUri(course).path("forum").build();
        final PostMethod newForumMethod = createPost(newForumUri, MediaType.APPLICATION_JSON, true);
        newForumMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newForumMethod.addParameter("position", "3");
        newForumMethod.addParameter("shortTitle", "Forum-0");
        newForumMethod.addParameter("longTitle", "Forum-long-0");
        newForumMethod.addParameter("objectives", "Forum-objectives-0");
        final int newForumCode = c.executeMethod(newForumMethod);
        assertTrue(newForumCode == 200 || newForumCode == 201);
        final String newForumBody = newForumMethod.getResponseBodyAsString();
        final CourseNodeVO forumNode = parse(newForumBody, CourseNodeVO.class);
        assertNotNull(forumNode);
        assertNotNull(forumNode.getId());
        assertEquals(forumNode.getShortTitle(), "Forum-0");
        assertEquals(forumNode.getLongTitle(), "Forum-long-0");
        assertEquals(forumNode.getLearningObjectives(), "Forum-objectives-0");
        assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());

        // create a task node
        final URI newTaskUri = getElementsUri(course).path("task").build();
        final PostMethod newTaskMethod = createPost(newTaskUri, MediaType.APPLICATION_JSON, true);
        newTaskMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newTaskMethod.addParameter("position", "4");
        newTaskMethod.addParameter("shortTitle", "Task-0");
        newTaskMethod.addParameter("longTitle", "Task-long-0");
        newTaskMethod.addParameter("objectives", "Task-objectives-0");
        newTaskMethod.addParameter("points", "25");
        newTaskMethod.addParameter("text", "A very difficult test");
        final int newTaskCode = c.executeMethod(newTaskMethod);
        assertTrue(newTaskCode == 200 || newTaskCode == 201);
        final String newTaskBody = newTaskMethod.getResponseBodyAsString();
        final CourseNodeVO taskNode = parse(newTaskBody, CourseNodeVO.class);
        assertNotNull(taskNode);
        assertNotNull(taskNode.getId());
        assertEquals(taskNode.getShortTitle(), "Task-0");
        assertEquals(taskNode.getLongTitle(), "Task-long-0");
        assertEquals(taskNode.getLearningObjectives(), "Task-objectives-0");
        assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());

        // create a test node
        final URI newTestUri = getElementsUri(course).path("test").build();
        final PostMethod newTestMethod = createPost(newTestUri, MediaType.APPLICATION_JSON, true);
        newTestMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newTestMethod.addParameter("testResourceableId", course.getEditorRootNodeId());
        newTestMethod.addParameter("position", "5");
        newTestMethod.addParameter("shortTitle", "Test-0");
        newTestMethod.addParameter("longTitle", "Test-long-0");
        newTestMethod.addParameter("objectives", "Test-objectives-0");
        final int newTestCode = c.executeMethod(newTestMethod);
        assertTrue(newTestCode == 404);// must bind a real test
        /*
         * assertTrue(newTestCode == 200 || newTestCode == 201); String newTestBody = newTestMethod.getResponseBodyAsString(); CourseNodeVO testNode = parse(newTestBody,
         * CourseNodeVO.class); assertNotNull(testNode); assertNotNull(testNode.getId()); assertEquals(testNode.getShortTitle(), "Test-0");
         * assertEquals(testNode.getParentId(), course.getEditorRootNodeId());
         */

        // create an assessment node
        final URI newAssessmentUri = getElementsUri(course).path("assessment").build();
        final PostMethod newAssessmentMethod = createPost(newAssessmentUri, MediaType.APPLICATION_JSON, true);
        newAssessmentMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newAssessmentMethod.addParameter("position", "5");
        newAssessmentMethod.addParameter("shortTitle", "Assessment-0");
        newAssessmentMethod.addParameter("longTitle", "Assessment-long-0");
        newAssessmentMethod.addParameter("objectives", "Assessment-objectives-0");
        final int newAssessmentCode = c.executeMethod(newAssessmentMethod);
        assertTrue(newAssessmentCode == 200 || newAssessmentCode == 201);
        final String newAssessmentBody = newAssessmentMethod.getResponseBodyAsString();
        final CourseNodeVO assessmentNode = parse(newAssessmentBody, CourseNodeVO.class);
        assertNotNull(assessmentNode);
        assertNotNull(assessmentNode.getId());
        assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
        assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
        assertEquals(assessmentNode.getLearningObjectives(), "Assessment-objectives-0");
        assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());

        // create an contact node
        final URI newContactUri = getElementsUri(course).path("contact").build();
        final PostMethod newContactMethod = createPost(newContactUri, MediaType.APPLICATION_JSON, true);
        newContactMethod.addParameter("parentNodeId", course.getEditorRootNodeId());
        newContactMethod.addParameter("position", "6");
        newContactMethod.addParameter("shortTitle", "Contact-0");
        newContactMethod.addParameter("longTitle", "Contact-long-0");
        newContactMethod.addParameter("objectives", "Contact-objectives-0");

        final int newContactCode = c.executeMethod(newContactMethod);
        assertEquals(200, newContactCode);
        final String newContactBody = newContactMethod.getResponseBodyAsString();
        final CourseNodeVO contactNode = parse(newContactBody, CourseNodeVO.class);
        assertNotNull(contactNode);
        assertNotNull(contactNode.getId());
        assertEquals(contactNode.getShortTitle(), "Contact-0");
        assertEquals(contactNode.getLongTitle(), "Contact-long-0");
        assertEquals(contactNode.getLearningObjectives(), "Contact-objectives-0");
        assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());
    }

    @Test
    public void testCreateCoursePut() throws IOException, URISyntaxException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        // create an empty course
        final URI uri = getCoursesUri().queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(200, code);
        final String body = method.getResponseBodyAsString();
        method.releaseConnection();
        final CourseVO course = parse(body, CourseVO.class);
        assertNotNull(course);
        assertNotNull(course.getKey());
        assertNotNull(course.getEditorRootNodeId());

        try {
            final ICourse savedCourse = CourseFactory.loadCourse(course.getKey());
            assertNotNull(savedCourse);
        } catch (final Exception e) {
            assertTrue(false);
        }

        // create a learning group
        final GroupVO groupVo = new GroupVO();
        groupVo.setName("hello");
        groupVo.setDescription("hello description");
        groupVo.setMinParticipants(new Integer(-1));
        groupVo.setMaxParticipants(new Integer(-1));

        final String stringuifiedAuth = stringuified(groupVo);
        final RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
        final URI newGroupUri = getCoursesUri().path(course.getKey().toString()).path("groups").build();
        final PutMethod newGrpMethod = createPut(newGroupUri, MediaType.APPLICATION_JSON, true);
        newGrpMethod.setRequestEntity(entity);
        final int newGrpCode = c.executeMethod(newGrpMethod);
        assertEquals(200, newGrpCode);
        final String newGrpBody = newGrpMethod.getResponseBodyAsString();
        newGrpMethod.releaseConnection();
        final GroupVO group = parse(newGrpBody, GroupVO.class);
        assertNotNull(group);
        assertNotNull(group.getKey());

        // create an structure node
        final URI newStructureUri = getElementsUri(course).path("structure").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "0")
                .queryParam("shortTitle", "Structure-0").queryParam("longTitle", "Structure-long-0").queryParam("objectives", "Structure-objectives-0").build();
        final PutMethod newStructureMethod = createPut(newStructureUri, MediaType.APPLICATION_JSON, true);
        final int newStructureCode = c.executeMethod(newStructureMethod);
        assertTrue(newStructureCode == 200 || newStructureCode == 201);
        final String newStructureBody = newStructureMethod.getResponseBodyAsString();
        final CourseNodeVO structureNode = parse(newStructureBody, CourseNodeVO.class);
        assertNotNull(structureNode);
        assertNotNull(structureNode.getId());
        assertEquals(structureNode.getShortTitle(), "Structure-0");
        assertEquals(structureNode.getLongTitle(), "Structure-long-0");
        assertEquals(structureNode.getLearningObjectives(), "Structure-objectives-0");
        assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());

        // create single page
        final URL pageUrl = RepositoryEntriesITCase.class.getResource("singlepage.html");
        assertNotNull(pageUrl);
        final File page = new File(pageUrl.toURI());

        final URI newPageUri = getElementsUri(course).path("singlepage").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "1")
                .queryParam("shortTitle", "Single-Page-0").queryParam("longTitle", "Single-Page-long-0").queryParam("objectives", "Single-Page-objectives-0").build();
        final PutMethod newPageMethod = createPut(newPageUri, MediaType.APPLICATION_JSON, true);
        newPageMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] parts = { new FilePart("file", page), new StringPart("filename", page.getName()) };
        newPageMethod.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));

        final int newPageCode = c.executeMethod(newPageMethod);
        assertTrue(newPageCode == 200 || newPageCode == 201);
        final String newPageBody = newPageMethod.getResponseBodyAsString();
        final CourseNodeVO pageNode = parse(newPageBody, CourseNodeVO.class);
        assertNotNull(pageNode);
        assertNotNull(pageNode.getId());
        assertEquals(pageNode.getShortTitle(), "Single-Page-0");
        assertEquals(pageNode.getLongTitle(), "Single-Page-long-0");
        assertEquals(pageNode.getLearningObjectives(), "Single-Page-objectives-0");
        assertEquals(structureNode.getParentId(), course.getEditorRootNodeId());

        // create a folder node
        final URI newFolderUri = getElementsUri(course).path("folder").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "2")
                .queryParam("shortTitle", "Folder-0").queryParam("longTitle", "Folder-long-0").queryParam("objectives", "Folder-objectives-0").build();
        final PutMethod newFolderMethod = createPut(newFolderUri, MediaType.APPLICATION_JSON, true);
        final int newFolderCode = c.executeMethod(newFolderMethod);
        assertTrue(newFolderCode == 200 || newFolderCode == 201);
        final String newFolderBody = newFolderMethod.getResponseBodyAsString();
        final CourseNodeVO folderNode = parse(newFolderBody, CourseNodeVO.class);
        assertNotNull(folderNode);
        assertNotNull(folderNode.getId());
        assertEquals(folderNode.getShortTitle(), "Folder-0");
        assertEquals(folderNode.getLongTitle(), "Folder-long-0");
        assertEquals(folderNode.getLearningObjectives(), "Folder-objectives-0");
        assertEquals(folderNode.getParentId(), course.getEditorRootNodeId());

        // create a forum node
        final URI newForumUri = getElementsUri(course).path("forum").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "3")
                .queryParam("shortTitle", "Forum-0").queryParam("longTitle", "Forum-long-0").queryParam("objectives", "Forum-objectives-0").build();
        final PutMethod newForumMethod = createPut(newForumUri, MediaType.APPLICATION_JSON, true);
        final int newForumCode = c.executeMethod(newForumMethod);
        assertTrue(newForumCode == 200 || newForumCode == 201);
        final String newForumBody = newForumMethod.getResponseBodyAsString();
        final CourseNodeVO forumNode = parse(newForumBody, CourseNodeVO.class);
        assertNotNull(forumNode);
        assertNotNull(forumNode.getId());
        assertEquals(forumNode.getShortTitle(), "Forum-0");
        assertEquals(forumNode.getLongTitle(), "Forum-long-0");
        assertEquals(forumNode.getLearningObjectives(), "Forum-objectives-0");
        assertEquals(forumNode.getParentId(), course.getEditorRootNodeId());

        // create a task node
        final URI newTaskUri = getElementsUri(course).path("task").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "4")
                .queryParam("shortTitle", "Task-0").queryParam("longTitle", "Task-long-0").queryParam("objectives", "Task-objectives-0").queryParam("points", "25")
                .queryParam("text", "A very difficult test").build();
        final PutMethod newTaskMethod = createPut(newTaskUri, MediaType.APPLICATION_JSON, true);
        final int newTaskCode = c.executeMethod(newTaskMethod);
        assertTrue(newTaskCode == 200 || newTaskCode == 201);
        final String newTaskBody = newTaskMethod.getResponseBodyAsString();
        final CourseNodeVO taskNode = parse(newTaskBody, CourseNodeVO.class);
        assertNotNull(taskNode);
        assertNotNull(taskNode.getId());
        assertEquals(taskNode.getShortTitle(), "Task-0");
        assertEquals(taskNode.getLongTitle(), "Task-long-0");
        assertEquals(taskNode.getLearningObjectives(), "Task-objectives-0");
        assertEquals(taskNode.getParentId(), course.getEditorRootNodeId());

        // add task configuration
        final URI taskConfigUri = getElementsUri(course).path("task/" + taskNode.getId() + "/configuration").queryParam("enableAssignment", Boolean.FALSE)
                .queryParam("enableScoring", Boolean.TRUE).queryParam("grantScoring", Boolean.TRUE).queryParam("scoreMin", new Float(1.5)).queryParam("scoreMax", 10)
                .build();
        final PutMethod taskConfigMethod = createPut(taskConfigUri, MediaType.APPLICATION_JSON, true);
        int taskConfigCode = c.executeMethod(taskConfigMethod);
        assertTrue(taskConfigCode == 200 || taskConfigCode == 201);

        final GetMethod getTaskConfig = createGet(taskConfigUri, MediaType.APPLICATION_JSON, true);
        taskConfigCode = c.executeMethod(getTaskConfig);
        assertTrue(taskConfigCode == 200 || taskConfigCode == 201);
        final String taskConfigBody = getTaskConfig.getResponseBodyAsString();
        final TaskConfigVO taskConfig = parse(taskConfigBody, TaskConfigVO.class);
        assertNotNull(taskConfig);
        assertTrue(!taskConfig.getIsAssignmentEnabled());// default is true
        assertTrue(taskConfig.getIsScoringEnabled() & taskConfig.getIsScoringGranted());
        assertTrue(taskConfig.getMinScore().floatValue() == 1.5);
        assertTrue(taskConfig.getMaxScore().floatValue() == 10);

        // create an assessment node
        final URI newAssessmentUri = getElementsUri(course).path("assessment").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "5")
                .queryParam("shortTitle", "Assessment-0").queryParam("longTitle", "Assessment-long-0").queryParam("objectives", "Assessment-objectives-0").build();
        final PutMethod newAssessmentMethod = createPut(newAssessmentUri, MediaType.APPLICATION_JSON, true);
        final int newAssessmentCode = c.executeMethod(newAssessmentMethod);
        assertTrue(newAssessmentCode == 200 || newAssessmentCode == 201);
        final String newAssessmentBody = newAssessmentMethod.getResponseBodyAsString();
        final CourseNodeVO assessmentNode = parse(newAssessmentBody, CourseNodeVO.class);
        assertNotNull(assessmentNode);
        assertNotNull(assessmentNode.getId());
        assertEquals(assessmentNode.getShortTitle(), "Assessment-0");
        assertEquals(assessmentNode.getLongTitle(), "Assessment-long-0");
        assertEquals(assessmentNode.getLearningObjectives(), "Assessment-objectives-0");
        assertEquals(assessmentNode.getParentId(), course.getEditorRootNodeId());

        // create an contact node
        final URI newContactUri = getElementsUri(course).path("contact").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "6")
                .queryParam("shortTitle", "Contact-0").queryParam("longTitle", "Contact-long-0").queryParam("objectives", "Contact-objectives-0").build();
        final PutMethod newContactMethod = createPut(newContactUri, MediaType.APPLICATION_JSON, true);
        final int newContactCode = c.executeMethod(newContactMethod);
        assertEquals(200, newContactCode);
        final String newContactBody = newContactMethod.getResponseBodyAsString();
        final CourseNodeVO contactNode = parse(newContactBody, CourseNodeVO.class);
        assertNotNull(contactNode);
        assertNotNull(contactNode.getId());
        assertEquals(contactNode.getShortTitle(), "Contact-0");
        assertEquals(contactNode.getLongTitle(), "Contact-long-0");
        assertEquals(contactNode.getLearningObjectives(), "Contact-objectives-0");
        assertEquals(contactNode.getParentId(), course.getEditorRootNodeId());

        // try to create an invalid enrollment node
        URI newENUri = getElementsUri(course).path("enrollment").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "7")
                .queryParam("shortTitle", "Enrollment-0").queryParam("longTitle", "Enrollment-long-0").queryParam("objectives", "Enrollment-objectives-0").build();
        PutMethod newENMethod = createPut(newENUri, MediaType.APPLICATION_JSON, true);
        int newENCode = c.executeMethod(newENMethod);
        assertEquals(406, newENCode);

        // create an enrollment node
        newENUri = getElementsUri(course).path("enrollment").build();
        newENMethod = createPut(newENUri, MediaType.APPLICATION_JSON, true);
        newENMethod.setQueryString(new NameValuePair[] { new NameValuePair("parentNodeId", course.getEditorRootNodeId()), new NameValuePair("position", "7"),
                new NameValuePair("shortTitle", "Enrollment-0"), new NameValuePair("longTitle", "Enrollment-long-0"),
                new NameValuePair("objectives", "Enrollment-objectives-0"), new NameValuePair("groups", group.getKey().toString()),
                new NameValuePair("cancelEnabled", "true") });

        newENCode = c.executeMethod(newENMethod);
        assertEquals(200, newENCode);

        final String newENBody = newENMethod.getResponseBodyAsString();
        final CourseNodeVO enNode = parse(newENBody, CourseNodeVO.class);
        assertNotNull(enNode);
        assertNotNull(enNode.getId());
        assertEquals(enNode.getShortTitle(), "Enrollment-0");
        assertEquals(enNode.getLongTitle(), "Enrollment-long-0");
        assertEquals(enNode.getLearningObjectives(), "Enrollment-objectives-0");
        assertEquals(enNode.getParentId(), course.getEditorRootNodeId());

        // create a test node
        final URL cpUrl = RepositoryEntriesITCase.class.getResource("qti-demo.zip");
        assertNotNull(cpUrl);
        final File cp = new File(cpUrl.toURI());

        final PutMethod qtiRepoMethod = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        qtiRepoMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] qtiRepoParts = { new FilePart("file", cp), new StringPart("filename", "qti-demo.zip"), new StringPart("resourcename", "QTI demo"),
                new StringPart("displayname", "QTI demo") };
        qtiRepoMethod.setRequestEntity(new MultipartRequestEntity(qtiRepoParts, qtiRepoMethod.getParams()));

        final int qtiRepoCode = c.executeMethod(qtiRepoMethod);
        assertTrue(qtiRepoCode == 200 || qtiRepoCode == 201);

        final String qtiRepoBody = qtiRepoMethod.getResponseBodyAsString();
        qtiRepoMethod.releaseConnection();
        final RepositoryEntryVO newTestVO = parse(qtiRepoBody, RepositoryEntryVO.class);
        assertNotNull(newTestVO);

        final Long key = newTestVO.getKey();
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(key);
        assertNotNull(re);
        assertNotNull(re.getOwnerGroup());
        assertNotNull(re.getOlatResource());
        assertEquals("QTI demo", re.getDisplayname());

        final URI newTestUri = getElementsUri(course).path("test").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "8")
                .queryParam("shortTitle", "Test-0").queryParam("longTitle", "Test-long-0").queryParam("objectives", "Test-objectives-0")
                .queryParam("testResourceableId", key).build();
        final PutMethod newTestMethod = createPut(newTestUri, MediaType.APPLICATION_JSON, true);
        final int newTestCode = c.executeMethod(newTestMethod);
        assertTrue(newTestCode == 200 || newTestCode == 201);
        final String newTestBody = newTestMethod.getResponseBodyAsString();
        final CourseNodeVO testNode = parse(newTestBody, CourseNodeVO.class);
        assertNotNull(testNode);
        assertNotNull(testNode.getId());
        assertEquals(testNode.getShortTitle(), "Test-0");
        assertEquals(testNode.getParentId(), course.getEditorRootNodeId());

        // configure test node
        final URI testConfigUri = getElementsUri(course).path("test/" + testNode.getId() + "/configuration").queryParam("allowCancel", Boolean.TRUE)
                .queryParam("allowNavigation", Boolean.TRUE).queryParam("allowSuspend", Boolean.TRUE).queryParam("numAttempts", 10)
                .queryParam("sequencePresentation", AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM).queryParam("showNavigation", Boolean.TRUE)
                .queryParam("showQuestionTitle", Boolean.TRUE).queryParam("showResultsAfterFinish", Boolean.TRUE).queryParam("showResultsDependendOnDate", Boolean.TRUE)
                .queryParam("showResultsOnHomepage", key).queryParam("showScoreInfo", Boolean.TRUE).queryParam("showScoreProgress", Boolean.TRUE)
                .queryParam("showSectionsOnly", Boolean.TRUE).queryParam("summaryPresentation", AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED)
                .queryParam("startDate", new Long(1280444400))// new
                // Date(1280444400))
                .queryParam("endDate", new Long(1293667200))// new Date(1293667200))
                .build();
        final PutMethod testConfigMethod = createPut(testConfigUri, MediaType.APPLICATION_JSON, true);
        int testConfigCode = c.executeMethod(testConfigMethod);
        assertTrue(testConfigCode == 200 || testConfigCode == 201);

        final GetMethod getTestConfig = createGet(testConfigUri, MediaType.APPLICATION_JSON, true);
        testConfigCode = c.executeMethod(getTestConfig);
        assertTrue(testConfigCode == 200 || testConfigCode == 201);
        final String testConfigBody = getTestConfig.getResponseBodyAsString();
        final TestConfigVO testConfig = parse(testConfigBody, TestConfigVO.class);
        assertTrue(testConfig.getNumAttempts() == 10);
        assertTrue(testConfig.getAllowCancel());
        assertTrue(testConfig.getSummeryPresentation().equals(AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED));

        // create a survey node
        final URL newSurveyUrl = RepositoryEntriesITCase.class.getResource("questionnaire-demo.zip");
        assertNotNull(newSurveyUrl);
        final File surveyFile = new File(newSurveyUrl.toURI());

        final PutMethod surveyRepoMethod = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
        surveyRepoMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] newSurveyParts = { new FilePart("file", surveyFile), new StringPart("filename", "questionnaire-demo.zip"),
                new StringPart("resourcename", "Questionnaire demo"), new StringPart("displayname", "Questionnaire demo") };
        surveyRepoMethod.setRequestEntity(new MultipartRequestEntity(newSurveyParts, surveyRepoMethod.getParams()));
        final int surveyRepoCode = c.executeMethod(surveyRepoMethod);
        assertTrue(surveyRepoCode == 200 || surveyRepoCode == 201);

        final String surveyRepoBody = surveyRepoMethod.getResponseBodyAsString();
        final RepositoryEntryVO newSurveyVO = parse(surveyRepoBody, RepositoryEntryVO.class);
        assertNotNull(newSurveyVO);

        final Long surveyKey = newSurveyVO.getKey();
        final RepositoryEntry surveyRE = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(surveyKey);
        assertNotNull(surveyRE);
        assertNotNull(surveyRE.getOwnerGroup());
        assertNotNull(surveyRE.getOlatResource());
        assertEquals("Questionnaire demo", surveyRE.getDisplayname());

        final URI newSurveyUri = getElementsUri(course).path("survey").queryParam("parentNodeId", course.getEditorRootNodeId()).queryParam("position", "9")
                .queryParam("shortTitle", "Survey-0").queryParam("longTitle", "Survey-long-0").queryParam("objectives", "Survey-objectives-0")
                .queryParam("surveyResourceableId", surveyKey).build();
        final PutMethod newSurveyMethod = createPut(newSurveyUri, MediaType.APPLICATION_JSON, true);
        final int newSurveyCode = c.executeMethod(newSurveyMethod);
        assertTrue(newSurveyCode == 200 || newSurveyCode == 201);
        final String newSurveyBody = newSurveyMethod.getResponseBodyAsString();
        final CourseNodeVO surveyNode = parse(newSurveyBody, CourseNodeVO.class);
        assertNotNull(surveyNode);
        assertNotNull(surveyNode.getId());
        assertEquals(surveyNode.getShortTitle(), "Survey-0");
        assertEquals(surveyNode.getParentId(), course.getEditorRootNodeId());

        // configure survey node
        final URI surveykConfigUri = getElementsUri(course).path("survey/" + surveyNode.getId() + "/configuration").queryParam("allowCancel", Boolean.TRUE)
                .queryParam("allowNavigation", Boolean.TRUE).queryParam("allowSuspend", Boolean.TRUE)
                .queryParam("sequencePresentation", AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM).queryParam("showNavigation", Boolean.TRUE)
                .queryParam("showQuestionTitle", Boolean.TRUE).queryParam("showSectionsOnly", Boolean.TRUE).build();
        final PutMethod surveyConfigMethod = createPut(surveykConfigUri, MediaType.APPLICATION_JSON, true);
        int surveyConfigCode = c.executeMethod(surveyConfigMethod);
        assertTrue(surveyConfigCode == 200 || surveyConfigCode == 201);

        final GetMethod getSurveyConfig = createGet(surveykConfigUri, MediaType.APPLICATION_JSON, true);
        surveyConfigCode = c.executeMethod(getSurveyConfig);
        assertTrue(surveyConfigCode == 200 || surveyConfigCode == 201);
        final String surveyConfigBody = getSurveyConfig.getResponseBodyAsString();
        final SurveyConfigVO surveyConfig = parse(surveyConfigBody, SurveyConfigVO.class);
        assertNotNull(surveyConfig);
        assertTrue(surveyConfig.getAllowCancel());

        // create an external page node
        final URI newTUUri = getElementsUri(course).path("externalpage").build();
        final PutMethod newTUMethod = createPut(newTUUri, MediaType.APPLICATION_JSON, true);
        newTUMethod.setQueryString(new NameValuePair[] { new NameValuePair("parentNodeId", course.getEditorRootNodeId()), new NameValuePair("position", "10"),
                new NameValuePair("shortTitle", "ExternalPage-0"), new NameValuePair("longTitle", "ExternalPage-long-0"),
                new NameValuePair("objectives", "ExternalPage-objectives-0"), new NameValuePair("url", "http://www.olat.org") });

        final int newTUCode = c.executeMethod(newTUMethod);
        assertEquals(200, newTUCode);

        final String newTUBody = newTUMethod.getResponseBodyAsString();
        final CourseNodeVO tuNode = parse(newTUBody, CourseNodeVO.class);
        assertNotNull(tuNode);
        assertNotNull(tuNode.getId());
        assertEquals(tuNode.getShortTitle(), "ExternalPage-0");
        assertEquals(tuNode.getLongTitle(), "ExternalPage-long-0");
        assertEquals(tuNode.getLearningObjectives(), "ExternalPage-objectives-0");
        assertEquals(tuNode.getParentId(), course.getEditorRootNodeId());

        // summary check
        final ICourse realCourse = CourseFactory.loadCourse(course.getKey());
        final TreeNode realRoot = realCourse.getEditorTreeModel().getRootNode();
        assertEquals(11, realRoot.getChildCount());

        // structure
        CourseEditorTreeNode child = (CourseEditorTreeNode) realRoot.getChildAt(0);
        CourseNode childNode = child.getCourseNode();
        assertTrue(childNode instanceof STCourseNode);

        // single page
        child = (CourseEditorTreeNode) realRoot.getChildAt(1);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof SPCourseNode);

        // folder
        child = (CourseEditorTreeNode) realRoot.getChildAt(2);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof BCCourseNode);

        // forum
        child = (CourseEditorTreeNode) realRoot.getChildAt(3);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof FOCourseNode);

        // task
        child = (CourseEditorTreeNode) realRoot.getChildAt(4);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof TACourseNode);

        // assessment
        child = (CourseEditorTreeNode) realRoot.getChildAt(5);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof MSCourseNode);

        // contact
        child = (CourseEditorTreeNode) realRoot.getChildAt(6);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof COCourseNode);

        // enrollment
        child = (CourseEditorTreeNode) realRoot.getChildAt(7);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof ENCourseNode);

        // test
        child = (CourseEditorTreeNode) realRoot.getChildAt(8);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof IQTESTCourseNode);

        // survey
        child = (CourseEditorTreeNode) realRoot.getChildAt(9);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof IQSURVCourseNode);

        // external page
        child = (CourseEditorTreeNode) realRoot.getChildAt(10);
        childNode = child.getCourseNode();
        assertTrue(childNode instanceof TUCourseNode);

        // attach file to task
        child = (CourseEditorTreeNode) realRoot.getChildAt(4);
        childNode = child.getCourseNode();
        final URI attachTaskFileUri = getElementsUri(course).path("task").path(childNode.getIdent()).path("file").queryParam("filename", "singlepage.html").build();
        final PutMethod taskFileMethod = createPut(attachTaskFileUri, MediaType.APPLICATION_JSON, true);
        taskFileMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
        final Part[] taskFileParts = { new FilePart("file", page), new StringPart("filename", page.getName()) };
        taskFileMethod.setRequestEntity(new MultipartRequestEntity(taskFileParts, method.getParams()));
        final int taskFileCode = c.executeMethod(taskFileMethod);
        assertEquals(200, taskFileCode);
        final String taskFolderPath = TACourseNode.getTaskFolderPathRelToFolderRoot(realCourse, childNode);
        final OlatRootFolderImpl taskFolder = new OlatRootFolderImpl(taskFolderPath, null);
        final VFSLeaf singleFile = (VFSLeaf) taskFolder.resolve("/" + "singlepage.html");
        assertNotNull(singleFile);
    }

    private UriBuilder getCoursesUri() {
        return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
    }

    private UriBuilder getElementsUri(final CourseVO course) {
        return getCoursesUri().path(course.getKey().toString()).path("elements");
    }
}
