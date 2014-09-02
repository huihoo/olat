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
package org.olat.lms.course.nodes.ta;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;

/**
 * TODO: Class Description for Task_EBL_Test
 * 
 * <P>
 * Initial Date: 05.09.2011 <br>
 * 
 * @author guretzki
 */
public class Task_EBL_Test {
    private Task_EBL taskEbl;
    private Dropbox_EBL dropboxEbl;
    private Returnbox_EBL returnboxEbl;
    private Solution_EBL solutionEbl;
    private String courseNodeIdent = "2222";
    private CourseNode mockCourseNode;
    private String basePath = "/basePath";
    private CourseEnvironment mockCourseEnvironment;
    private String mockIdentityName = "testIdentity";
    private Identity mockIdentity;

    String expectedDropboxRootFolder = basePath + File.separator + "dropboxes" + File.separator + courseNodeIdent;
    String expectedReturnboxRootFolder = basePath + File.separator + "returnboxes" + File.separator + courseNodeIdent;
    String expectedSolutionRootFolder = basePath + File.separator + "solutions" + File.separator + courseNodeIdent;

    @Before
    public void setUp() throws Exception {
        taskEbl = new Task_EBL();
        dropboxEbl = new Dropbox_EBL();
        returnboxEbl = new Returnbox_EBL();
        solutionEbl = new Solution_EBL();
        mockCourseNode = createMockCourseNode();
        mockCourseEnvironment = createMockCourseEnvironment();
        mockIdentity = createMockIdentity();
    }

    private CourseEnvironment createMockCourseEnvironment() {
        mockCourseEnvironment = mock(CourseEnvironment.class);
        OlatRootFolderImpl baseContainer = mock(OlatRootFolderImpl.class);
        when(mockCourseEnvironment.getCourseBaseContainer()).thenReturn(baseContainer);
        when(baseContainer.getRelPath()).thenReturn(basePath);
        return mockCourseEnvironment;
    }

    private CourseNode createMockCourseNode() {
        mockCourseNode = mock(CourseNode.class);
        when(mockCourseNode.getIdent()).thenReturn(courseNodeIdent);
        return mockCourseNode;
    }

    private Identity createMockIdentity() {
        mockIdentity = mock(Identity.class);
        when(mockIdentity.getName()).thenReturn(mockIdentityName);
        return mockIdentity;
    }

    @Test
    public void testGetDropboxRootFolder() {
        String actuelPath = dropboxEbl.getDropboxRootFolder(mockCourseEnvironment, mockCourseNode);
        assertEquals(expectedDropboxRootFolder, actuelPath);
    }

    @Test
    public void testGetDropboxFolderForIdentity() {
        String actuelPath = dropboxEbl.getDropboxFolderForIdentity(mockCourseEnvironment, mockCourseNode, mockIdentity);
        String expectedDropboxFolderForIdentity = expectedDropboxRootFolder + File.separator + mockIdentity.getName();
        assertEquals(expectedDropboxFolderForIdentity, actuelPath);
    }

    @Test
    public void testGetSolutionRootFolder() {
        String actuelPath = solutionEbl.getSolutionRootFolder(mockCourseEnvironment, mockCourseNode);
        assertEquals(expectedSolutionRootFolder, actuelPath);
    }

    @Test
    public void testGetReturnboxRootFolder() {
        String actuelPath = returnboxEbl.getReturnboxRootFolder(mockCourseEnvironment, mockCourseNode);
        assertEquals(expectedReturnboxRootFolder, actuelPath);
    }

}
