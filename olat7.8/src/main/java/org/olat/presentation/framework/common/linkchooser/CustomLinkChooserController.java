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

package org.olat.presentation.framework.common.linkchooser;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tree.SelectionTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeController;
import org.olat.presentation.framework.core.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Generates internal link. Show a tree-model to select an internal link. The user can select a course-node for which an internal link will be generated (gotoNode-link).
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>URLChoosenEvent(URL) containing the selected file URL
 * <li>Event.CANCELLED_EVENT
 * </ul>
 * 
 * @author Christian Guretzki
 */
public class CustomLinkChooserController extends DefaultController {

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(CustomLinkChooserController.class);

    private Translator trans;
    private VelocityContainer mainVC;

    private SelectionTree jumpInSelectionTree;
    private CustomLinkTreeModel customLinkTreeModel;

    private TreeController ajaxTreeController;
    private Link chooseLink, cancelLink;
    private String selectedAjaxTreePath;

    /**
     * Constructor
     */
    public CustomLinkChooserController(UserRequest ureq, WindowControl wControl, CustomLinkTreeModel customLinkTreeModel) {
        super(wControl);
        trans = PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale());
        mainVC = new VelocityContainer("mainVC", VELOCITY_ROOT + "/internallinkchooser.html", trans, this);

        this.customLinkTreeModel = customLinkTreeModel;
        boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
        if (ajax) {
            // For real browsers we use the cool ajax tree
            ajaxTreeController = new TreeController(ureq, getWindowControl(), customLinkTreeModel.getRootNode().getTitle(), customLinkTreeModel, null);
            ajaxTreeController.addControllerListener(this);
            mainVC.put("internalLinkTree", ajaxTreeController.getInitialComponent());
            // choose and cancel link
            chooseLink = LinkFactory.createButton("selectfile", mainVC, this);
            cancelLink = LinkFactory.createButton("cancel", mainVC, this);
        } else {
            // Legacy mode with old selection component
            jumpInSelectionTree = new SelectionTree("internalLinkTree", trans);
            jumpInSelectionTree.setTreeModel(customLinkTreeModel);
            jumpInSelectionTree.addListener(this);
            jumpInSelectionTree.setFormButtonKey("select");
            mainVC.put("internalLinkTree", jumpInSelectionTree);
        }
        setInitialComponent(mainVC);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == jumpInSelectionTree) { // Events from the legacy selection tree
            TreeEvent te = (TreeEvent) event;
            if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
                // create something like imagepath="javascript:parent.gotonode(<nodeId>)"
                // notify parent controller
                String url = customLinkTreeModel.getInternalLinkUrlFor(jumpInSelectionTree.getSelectedNode().getIdent());
                fireEvent(ureq, new URLChoosenEvent(url));

            } else if (te.getCommand().equals(TreeEvent.COMMAND_CANCELLED)) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else // Events from ajax tree view
        if (source == chooseLink) {
            if (selectedAjaxTreePath != null) {
                String url = customLinkTreeModel.getInternalLinkUrlFor(selectedAjaxTreePath);
                fireEvent(ureq, new URLChoosenEvent(url));
            } else {
                fireEvent(ureq, Event.FAILED_EVENT);
            }
        } else if (source == cancelLink) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }

    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == ajaxTreeController) {
            if (event instanceof TreeNodeClickedEvent) {
                // get the clicked node and resolve the corresponding file
                TreeNodeClickedEvent clickedEvent = (TreeNodeClickedEvent) event;
                selectedAjaxTreePath = clickedEvent.getNodeId();
                // enable link, set dirty button class and trigger redrawing
                chooseLink.setEnabled(true);
                chooseLink.setCustomEnabledLinkCSS("b_button b_button_dirty");
                chooseLink.setDirty(true);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
    }
}
