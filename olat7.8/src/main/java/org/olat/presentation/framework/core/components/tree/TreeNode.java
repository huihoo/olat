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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.framework.core.components.tree;

import org.olat.lms.commons.tree.INode;

/**
 * Description: <br>
 * TreeNode is a read-only Node which is used for rendering trees
 * 
 * @author Felix Jost
 */
public interface TreeNode extends INode {

    /**
     * @return
     */
    public String getTitle();

    /**
     * @return
     */
    public String getAltText();

    /**
     * @return
     */
    public boolean isAccessible();

    /**
     * @return
     */
    public Object getUserObject();

    /**
     * gets the css class to be used for rendering. if null, the default class is used. this is a convenient way to allow customized treenodes (one images and css
     * classes) so we can avoid a celltree_renderer
     * 
     * @return
     */
    public String getCssClass();

    public String getIconCssClass();

    public String getIconDecorator1CssClass();

    public String getIconDecorator2CssClass();

    public String getIconDecorator3CssClass();

    public String getIconDecorator4CssClass();

    /**
     * only matters for treenode selection: if not null, the treenode returned is the actual target of a click
     * 
     * @return
     */
    public TreeNode getDelegate();

    /**
     * Allows to preselect this node.
     * 
     * @param selected
     */
    public void setSelected(boolean selected);

    /**
     * Wether this node is preselected.
     * 
     * @return
     */
    public boolean isSelected();
}
