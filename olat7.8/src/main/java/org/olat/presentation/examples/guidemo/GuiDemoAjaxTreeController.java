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
 * Copyright (c) 2008 frentix GmbH,<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.presentation.examples.guidemo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.presentation.framework.core.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.presentation.framework.core.control.generic.ajax.tree.MoveTreeNodeEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeModifiedEvent;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;

/**
 * Description:<br>
 * Demo of the ajax based menu tree
 * <P>
 * Initial Date: 29.05.2008 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoAjaxTreeController extends BasicController {
    private TreeController treeCtr;
    private AjaxTreeModel treeModel;
    private VelocityContainer contentVC;
    private final Link sortLink, inlineEditLink, selectNodeLink, removeNodeLink;
    private boolean isSorted = false, isInlineEdit = false;

    public GuiDemoAjaxTreeController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // Main view is a velocity container
        contentVC = createVelocityContainer("guidemo-ajaxtree");

        // Build tree model
        treeModel = buildTreeModel();

        // Create the ajax tree controller, add it to your main view
        treeCtr = new TreeController(ureq, getWindowControl(), "Time machine", treeModel, "myjsCallback");
        treeCtr.setTreeSorting(false, false, false);
        listenTo(treeCtr);
        contentVC.put("treeCtr", treeCtr.getInitialComponent());
        // Add link for sorting
        sortLink = LinkFactory.createButton("GuiDemoAjaxTreeController.sortlink", contentVC, this);
        // Add link for inline editing
        inlineEditLink = LinkFactory.createButton("GuiDemoAjaxTreeController.editlink", contentVC, this);
        // Start with no sorting and not inline editing
        contentVC.contextPut("isSorted", Boolean.valueOf(isSorted));
        contentVC.contextPut("isInlineEdit", Boolean.valueOf(isInlineEdit));
        // Add link to select certain node
        selectNodeLink = LinkFactory.createLink("GuiDemoAjaxTreeController.selectlink", contentVC, this);
        // Add link to remove a certain node
        removeNodeLink = LinkFactory.createLink("GuiDemoAjaxTreeController.removelink", contentVC, this);

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentVC);
        contentVC.put("sourceview", sourceview.getInitialComponent());

        putInitialPanel(contentVC);
    }

    /**
     * Internal helper to build a dummy tree model which displays some time codes
     * 
     * @return
     */
    private AjaxTreeModel buildTreeModel() {
        final AjaxTreeModel model = new AjaxTreeModel("demomodelsdf") {
            @Override
            public List<AjaxTreeNode> getChildrenFor(final String nodeId) {
                final List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
                AjaxTreeNode child;
                try {
                    String ajaxTreeNodeText = "A wonderful day today " + "<script>alert('XSS_in_ajax_tree_node');</script>" + Calendar.getInstance().getTime().toString();
                    child = new AjaxTreeNode(nodeId + ".1", ajaxTreeNodeText);
                    // Setting some node attributes - see the Treenode or the extjs
                    // documentation on what else you could use
                    child.put(AjaxTreeNode.CONF_LEAF, true);// leafs can't be opened
                    child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
                    child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
                    child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
                    child.put(AjaxTreeNode.CONF_QTIP, ajaxTreeNodeText);
                    children.add(child);
                    String ajaxTreeNodeText2 = " Hello World " + Calendar.getInstance().getTime().toString();
                    child = new AjaxTreeNode(nodeId + ".2", ajaxTreeNodeText2);
                    child.put(AjaxTreeNode.CONF_LEAF, false);
                    child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, false); // sort folders above leafs
                    child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
                    child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
                    child.put(AjaxTreeNode.CONF_QTIP, ajaxTreeNodeText2);
                    children.add(child);
                    child = new AjaxTreeNode(nodeId + ".3", "I'm number two " + Calendar.getInstance().getTime().toString());
                    child.put(AjaxTreeNode.CONF_LEAF, true); // leafs can't be opened
                    child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
                    child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "b_filetype_doc"); // a custom icon css class
                    child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
                    child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
                    children.add(child);
                    child = new AjaxTreeNode(nodeId + ".4", "Folder " + Calendar.getInstance().getTime().toString());
                    child.put(AjaxTreeNode.CONF_LEAF, false);
                    child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, false); // sort folders above leafs
                    child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
                    child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
                    children.add(child);
                } catch (final JSONException e) {
                    throw new OLATRuntimeException("Error while creating gui demo ajax tree model", e);
                }
                return children;
            }
        };
        // Set a custom icon for the root node
        model.setCustomRootIconCssClass("o_st_icon");
        return model;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        contentVC = null;
        treeModel = null;
        // Controllers auto disposed by basic controller
        treeCtr = null;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == treeCtr) {
            // Catch move tree event. Here on the server side we can still prevent the
            // move operation to happen...
            if (event instanceof MoveTreeNodeEvent) {
                final MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
                getWindowControl().setInfo(
                        "Node::" + moveEvent.getNodeId() + " moved to new parent::" + moveEvent.getNewParentNodeId() + " at position::" + moveEvent.getPosition());
                // Set status: allow move or don't allow move. For this demo we just say yes...
                // See also the js code in the guidemo-ajaxtree.html file!
                moveEvent.setResult(true, null, null);

            } else if (event instanceof TreeNodeClickedEvent) {
                final TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                getWindowControl().setInfo("Node::" + clickedEvent.getNodeId() + " got clicked!");

            } else if (event instanceof TreeNodeModifiedEvent) {
                final TreeNodeModifiedEvent modifiedEvent = (TreeNodeModifiedEvent) event;
                getWindowControl().setInfo("Node::" + modifiedEvent.getNodeId() + " got modified, new value is \"" + modifiedEvent.getModifiedValue() + "\"!");
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == sortLink) {
            // change sort order to opposite
            isSorted = !isSorted;
            treeCtr.setTreeSorting(isSorted, isSorted, isSorted);
            contentVC.contextPut("isSorted", Boolean.valueOf(isSorted));
        } else if (source == inlineEditLink) {
            isInlineEdit = !isInlineEdit;
            treeCtr.setTreeInlineEditing(isInlineEdit, null, null);
            contentVC.contextPut("isInlineEdit", Boolean.valueOf(isInlineEdit));
        } else if (source == selectNodeLink) {
            // create a path to a node and select this one
            treeCtr.selectPath("/demomodelsdf/demomodelsdf.4/demomodelsdf.4.2");
        } else if (source == removeNodeLink) {
            // create a path to a node and remove this one
            treeCtr.removePath("/demomodelsdf/demomodelsdf.4/demomodelsdf.4.2");
        }
    }

}
