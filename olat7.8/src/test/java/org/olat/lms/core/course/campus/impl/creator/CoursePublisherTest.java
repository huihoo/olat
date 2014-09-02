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
package org.olat.lms.core.course.campus.impl.creator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.lms.core.course.campus.impl.creator.CoursePublisher;
import org.olat.lms.course.tree.CourseEditorTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeNode;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CoursePublisherTest {

    private CoursePublisher coursePublisherTestObject;

    private String rootNodeIdent = "rootNodeIdent";
    String firstChildNodeIdent = "childIdent1";
    String secondChildNodeIdent = "childIdent2";
    private CourseEditorTreeModel editorTreeModel;

    @Before
    public void setup() {
        coursePublisherTestObject = new CoursePublisher();

        GenericTreeNode rootNode = new GenericTreeNode("rootNode", null);
        rootNode.setIdent(rootNodeIdent);
        editorTreeModel = mock(CourseEditorTreeModel.class);
        when(editorTreeModel.getRootNode()).thenReturn(rootNode);
    }

    @Test
    public void getAllPublishNodeIds_onlyRootNode() {
        List<String> allNodeIds = coursePublisherTestObject.getAllPublishNodeIds(editorTreeModel);
        assertEquals("Wrong size of nodes", 1, allNodeIds.size());
        allNodeIds.contains(rootNodeIdent);
    }

    @Test
    public void getAllPublishNodeIds_twoChildNodesWithtwoChildNodes() {
        appendChildNodeWithSubChildNode(editorTreeModel.getRootNode(), firstChildNodeIdent);
        appendChildNodeWithSubChildNode(editorTreeModel.getRootNode(), secondChildNodeIdent);

        List<String> allNodeIds = coursePublisherTestObject.getAllPublishNodeIds(editorTreeModel);

        assertEquals("Wrong size of nodes", 7, allNodeIds.size());
        assertTrue(allNodeIds.contains(rootNodeIdent));
        assertTrue(allNodeIds.contains(firstChildNodeIdent));
        assertTrue(allNodeIds.contains(secondChildNodeIdent));
        assertTrue(allNodeIds.contains(firstChildNodeIdent + 1));
        assertTrue(allNodeIds.contains(firstChildNodeIdent + 2));
        assertTrue(allNodeIds.contains(secondChildNodeIdent + 1));
        assertTrue(allNodeIds.contains(secondChildNodeIdent + 2));
    }

    private void appendChildNodeWithSubChildNode(TreeNode rootNode, String childNodeIdent) {
        GenericTreeNode childNode = new GenericTreeNode(childNodeIdent, null);
        childNode.setIdent(childNodeIdent);
        rootNode.addChild(childNode);
        appendSubChildNode(childNode, childNodeIdent + 1);
        appendSubChildNode(childNode, childNodeIdent + 2);
    }

    private void appendSubChildNode(GenericTreeNode childNode, String childNodeIdent) {
        GenericTreeNode subChild = new GenericTreeNode(childNodeIdent, null);
        subChild.setIdent(childNodeIdent);
        childNode.addChild(subChild);
    }

}
