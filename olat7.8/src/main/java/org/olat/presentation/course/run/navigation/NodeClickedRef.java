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

package org.olat.presentation.course.run.navigation;

import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.control.Controller;

/**
 * Description: <br>
 * TODO: Felix Jost Class Description for NodeClickedResult
 * <P>
 * useful usage of this class: <br>
 * 1. handled by subtreelistener? yes? break : continue 2. check if visible -> no? genericNonVisMessage : continue (done internally here: check if accessible -> no?
 * noAccessExplanation of calledCourseNode : continue) 4. get new treemodel, new selectedNodeId, new Controller, and new calledCourseNode to build up the gui Initial
 * Date: 19.01.2005 <br>
 * 
 * @author Felix Jost
 */
public class NodeClickedRef {
    // the new treemodel that results from the user's click
    private final TreeModel treeModel;

    // whether the treenode clicked corresponds to a coursenode with visiblity
    // condition evaluated to true
    private final boolean visible;

    // the node id of the treemodel given here that is now selected. if null, then
    // no node is selected
    // (which can only be the case if even the root course node is not visible)
    private final String selectedNodeId;

    // the coursenode which was called when clicking the treenode (used only to
    // update scoring on that coursenode).
    // can be only null when selectedNodeId is null.
    private final CourseNode calledCourseNode;

    // the resulting controller, the subtreelistener, and the subtree: fetched
    // from the coursenode
    private final NodeRunConstructionResult nodeConstructionResult;

    /**
     * @param treeModel
     * @param visible
     * @param selectedNodeId
     * @param calledCourseNode
     * @param nodeConstructionResult
     *            null means that no new node controller has been created, but is was handled by the subtreemodellistener
     */
    NodeClickedRef(final TreeModel treeModel, final boolean visible, final String selectedNodeId, final CourseNode calledCourseNode,
            final NodeRunConstructionResult nodeConstructionResult) {
        this.treeModel = treeModel;
        this.visible = visible;
        this.selectedNodeId = selectedNodeId;
        this.calledCourseNode = calledCourseNode;
        this.nodeConstructionResult = nodeConstructionResult;
    }

    /**
     * @return if handled by the sublistener
     */
    public boolean isHandledBySubTreeModelListener() {
        return nodeConstructionResult == null;
    }

    /**
     * @return the coursenode that was called
     */
    public CourseNode getCalledCourseNode() {
        return calledCourseNode;
    }

    /**
     * @return the selected node id
     */
    public String getSelectedNodeId() {
        final String subNodeId = nodeConstructionResult.getSelectedTreeNodeId();
        // if subNodeId != null (e.g. a inner node of a content-packaging -> select this node
        if (subNodeId != null) {
            return subNodeId;
        } else {
            return selectedNodeId;
        }
    }

    /**
     * @return the treemodel
     */
    public TreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * @return true if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return the run controller or null
     */
    public Controller getRunController() {
        RepositoryServiceImpl.getInstance().setLastUsageNowFor(calledCourseNode.getReferencedRepositoryEntry());
        return nodeConstructionResult.getRunController();
    }

}
