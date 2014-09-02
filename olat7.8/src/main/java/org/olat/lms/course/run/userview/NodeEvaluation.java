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

package org.olat.lms.course.run.userview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.lms.commons.tree.GenericNode;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.CourseNodeFactory;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeNode;

/**
 * Description:<br>
 * 
 * <pre>
 *  the nodeeval determines the treenode!
 *  first new()...
 *  sec: put,put,put
 *  thrd: build
 *  4th: addChildren
 * </pre>
 * 
 * @author Felix Jost
 */
public class NodeEvaluation extends GenericNode {

    private final CourseNode courseNode;
    private GenericTreeNode gtn = null;

    private final Map accesses = new HashMap(4);

    private boolean visible = false;
    private boolean atLeastOneAccessible = false;

    public NodeEvaluation(final CourseNode courseNode) {
        this.courseNode = courseNode;
    }

    public void putAccessStatus(final String capabilityName, final boolean mayAccess) {
        accesses.put(capabilityName, new Boolean(mayAccess));
    }

    public boolean isCapabilityAccessible(final String capabilityName) {
        if (accesses.get(capabilityName) != null) {
            return ((Boolean) accesses.get(capabilityName)).booleanValue();
        }
        return false;
    }

    public void addNodeEvaluationChild(final NodeEvaluation chdNodeEval) {
        addChild(chdNodeEval);
        final TreeNode chTn = chdNodeEval.getTreeNode();
        gtn.addChild(chTn);
    }

    public NodeEvaluation getNodeEvaluationChildAt(final int pos) {
        return (NodeEvaluation) getChildAt(pos);
    }

    /**
     * @return boolean
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible.
     * 
     * @param visible
     *            The visible to set
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * 1. Calculate if the node should be accessible at all. <br/>
     * 2. If the coursenode is visible, build a treenode.
     */
    public void build() {
        // if at least one access capability is true
        for (final Iterator iter = accesses.values().iterator(); iter.hasNext();) {
            final Boolean entry = (Boolean) iter.next();
            atLeastOneAccessible = atLeastOneAccessible || entry.booleanValue();
        }

        // if the coursenode is visible, build a treenode
        if (isVisible()) {
            gtn = new GenericTreeNode();
            gtn.setTitle(courseNode.getShortTitle());
            gtn.setAltText(courseNode.getLongTitle());
            final String nodeCssClass = CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType()).getIconCSSClass();
            gtn.setIconCssClass(nodeCssClass);
            gtn.setUserObject(this); // the current NodeEval is set into the treenode
                                     // as the userobject
            // all treenodes added here are set to be visible/accessible, since the
            // invisible are not pushed by convention
            gtn.setAccessible(true);
        }
        // else treenode is null
    }

    /**
     * upon first call, the result is cached. Therefore first put all AccessStati, and then calculate the overall accessibility
     * 
     * @return
     */
    public boolean isAtLeastOneAccessible() {
        return atLeastOneAccessible;
    }

    /**
     * @return CourseNode
     */
    public CourseNode getCourseNode() {
        return courseNode;
    }

    /**
     * @return GenericTreeNode
     */
    public TreeNode getTreeNode() {
        return gtn;
    }

    /**
     * Only for special cases!!! like overriding coursenodes-children accessibility!! Sets the atLeastOneAccessible.
     * 
     * @param atLeastOneAccessible
     *            The atLeastOneAccessible to set
     */
    public void setAtLeastOneAccessible(final boolean atLeastOneAccessible) {
        this.atLeastOneAccessible = atLeastOneAccessible;
    }

}
