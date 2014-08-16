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
package org.olat.lms.course.nodes.projectbroker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;

/**
 * TODO: Class Description for ProjectBrokerManagerTest
 * 
 * <P>
 * Initial Date: 05.09.2011 <br>
 * 
 * @author guretzki
 */
public class ProjectBrokerManagerTest {

    private ProjectBrokerManagerImpl projectBrokerManager;
    private Project mockProject;
    Long projectKey = new Long(1111);
    String courseNodeIdent = "2222";
    private CourseNode mockCourseNode;
    String basePath = "/basePath";
    private CourseEnvironment mockCourseEnvironment;

    String expectedDropboxBasePath = basePath + File.separator + "dropboxes" + File.separator + courseNodeIdent;
    String expectedReturnboxBasePath = basePath + File.separator + "returnboxes" + File.separator + courseNodeIdent;

    @Before
    public void setUp() throws Exception {
        projectBrokerManager = new ProjectBrokerManagerImpl();
        createMockProject();
        createMockCourseNode();
        createMockCourseEnvironment();

    }

    private void createMockCourseEnvironment() {
        mockCourseEnvironment = mock(CourseEnvironment.class);
        OlatRootFolderImpl baseContainer = mock(OlatRootFolderImpl.class);
        when(mockCourseEnvironment.getCourseBaseContainer()).thenReturn(baseContainer);
        when(baseContainer.getRelPath()).thenReturn(basePath);
    }

    private void createMockCourseNode() {
        mockCourseNode = mock(CourseNode.class);
        when(mockCourseNode.getIdent()).thenReturn(courseNodeIdent);
    }

    private void createMockProject() {
        mockProject = mock(Project.class);
        when(mockProject.getKey()).thenReturn(projectKey);
    }

    @Test
    public void testGetDropboxRootFolder() {
        String actuelPath = projectBrokerManager.getDropboxRootFolder(mockCourseEnvironment, mockCourseNode);
        assertEquals(expectedDropboxBasePath, actuelPath);
    }

    @Test
    public void testGetDropboxBasePathForProject() {
        String actuelPath = projectBrokerManager.getDropboxBasePathForProject(mockProject, mockCourseEnvironment, mockCourseNode);
        String expectedPath = expectedDropboxBasePath + File.separator + projectKey.toString();
        assertEquals(expectedPath, actuelPath);
    }

    @Test
    public void testGetReturnboxRootFolder() {
        String actuelPath = projectBrokerManager.getReturnboxRootFolder(mockCourseEnvironment, mockCourseNode);
        assertEquals(expectedReturnboxBasePath, actuelPath);

    }

    @Test
    public void testGetReturnboxBasePathForProject() {
        String actuelPath = projectBrokerManager.getReturnboxBasePathForProject(mockProject, mockCourseEnvironment, mockCourseNode);
        String expectedPath = expectedReturnboxBasePath + File.separator + projectKey.toString();
        assertEquals(expectedPath, actuelPath);
    }

}
