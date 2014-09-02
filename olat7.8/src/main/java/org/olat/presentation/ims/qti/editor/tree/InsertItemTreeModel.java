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

package org.olat.presentation.ims.qti.editor.tree;

import org.olat.lms.commons.tree.TreePosition;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;

/**
 * Initial Date: Jan 05, 2005 <br>
 * 
 * @author mike
 */
public class InsertItemTreeModel extends GenericTreeModel {

    /**
     * Comment for <code>INSTANCE_ASSESSMENT</code>
     */
    public static final int INSTANCE_ASSESSMENT = 0;
    /**
     * Comment for <code>INSTANCE_SECTION</code>
     */
    public static final int INSTANCE_SECTION = 1;
    /**
     * Comment for <code>INSTANCE_ASSESSMENT</code>
     */
    public static final int INSTANCE_ITEM = 2;

    private final int appendToInstancesOf;

    /**
     * @param treeModel
     * @param appendToInstancesOf
     */
    public InsertItemTreeModel(final TreeModel treeModel, final int appendToInstancesOf) {
        this.appendToInstancesOf = appendToInstancesOf;
        final GenericQtiNode cnRoot = (GenericQtiNode) treeModel.getRootNode();
        final TreeNode ctn = buildNode(cnRoot);
        setRootNode(ctn);
    }

    private TreeNode buildNode(final GenericQtiNode parent) {
        int parentInstance = INSTANCE_ASSESSMENT;
        if (parent instanceof SectionNode) {
            parentInstance = INSTANCE_SECTION;
        }
        if (parent instanceof ItemNode) {
            parentInstance = INSTANCE_ITEM;
        }

        final GenericTreeNode ctn = new GenericTreeNode(parent.getTitle(), parent);
        ctn.setIconCssClass(parent.getIconCssClass());
        ctn.setAccessible(false);

        final int childcnt = parent.getChildCount();
        for (int i = 0; i < childcnt; i++) {
            if (parentInstance == appendToInstancesOf) { // add insert pos
                final GenericTreeNode gtn = new GenericTreeNode();
                gtn.setAccessible(true);
                gtn.setTitle("");
                gtn.setAltText("");
                gtn.setUserObject(new TreePosition(parent, i));
                ctn.addChild(gtn);
            }
            // add child itself
            final GenericQtiNode cchild = (GenericQtiNode) parent.getChildAt(i);
            final TreeNode ctchild = buildNode(cchild);
            ctn.addChild(ctchild);
        }
        if (parentInstance == appendToInstancesOf) {
            // add last insert position
            final GenericTreeNode gtn = new GenericTreeNode();
            gtn.setAccessible(true);
            gtn.setTitle("");
            gtn.setAltText("");
            gtn.setUserObject(new TreePosition(parent, childcnt));
            ctn.addChild(gtn);
        }
        return ctn;
    }

    /**
     * @param nodeId
     * @return TreePosition
     */
    public TreePosition getTreePosition(final String nodeId) {
        final TreeNode n = getNodeById(nodeId);
        final GenericTreeNode gtn = (GenericTreeNode) n;
        final TreePosition tp = (TreePosition) gtn.getUserObject();
        return tp;
    }
}
