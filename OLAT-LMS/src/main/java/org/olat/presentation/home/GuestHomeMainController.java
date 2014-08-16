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

package org.olat.presentation.home;

import org.apache.log4j.Logger;
import org.olat.lms.commons.tree.INode;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.portal.Portal;
import org.olat.presentation.framework.core.control.generic.portal.PortalMainController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <!--**************--> <h3>Responsability:</h3> display the first page the guest sees after she logged in successfully. Registered users login have their own
 * {@link org.olat.presentation.home.HomeMainController first page} !
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * display guest portal.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3> The guest is a special role inside the learning management system, hence the registered user is handled by a different
 * {@link org.olat.presentation.home.HomeMainController controller}.
 * <P>
 * Initial Date: Apr 27, 2004
 * 
 * @author gnaegi
 */
public class GuestHomeMainController extends MainLayoutBasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(GuestHomeMainController.class);
    private final MenuTree olatMenuTree;
    private final VelocityContainer welcome;
    private LayoutMain3ColsController columnLayoutCtr;
    private Controller myPortal;

    /**
     * Constructor of the guest home main controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     */
    public GuestHomeMainController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel();
        olatMenuTree.setTreeModel(tm);
        olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
        olatMenuTree.addListener(this);

        welcome = createVelocityContainer("guestwelcome");

        // add portal
        Portal portal = ((Portal) CoreSpringFactory.getBean("guestportal"));
        myPortal = new PortalMainController(ureq, getWindowControl(), portal);
        listenTo(myPortal);
        welcome.put("myPortal", myPortal.getInitialComponent());

        // Activate correct position in menu
        final INode firstNode = tm.getRootNode().getChildAt(0);
        olatMenuTree.setSelectedNodeId(firstNode.getIdent());

        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, welcome, null);
        listenTo(columnLayoutCtr); // cleanup on dispose
        // add background image to home site
        columnLayoutCtr.addCssClassToMain("o_home");

        putInitialPanel(columnLayoutCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == olatMenuTree) {
            // process menu commands
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                final TreeNode selTreeNode = olatMenuTree.getSelectedNode();
                final String cmd = (String) selTreeNode.getUserObject();
                if (cmd.equals("root") || cmd.equals("guestwelcome")) {
                    welcome.setPage(VELOCITY_ROOT + "/guestwelcome.html");
                } else if (cmd.equals("guestinfo")) {
                    welcome.setPage(VELOCITY_ROOT + "/guestinfo.html");
                }
            }
        } else {
            log.warn("Unhandled olatMenuTree event: " + event.getCommand());
        }
    }

    private TreeModel buildTreeModel() {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(translate("menu.guest"));
        root.setUserObject("guest");
        root.setAltText(translate("menu.guest.alt"));
        gtm.setRootNode(root);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.guestwelcome"));
        gtn.setUserObject("guestwelcome");
        gtn.setAltText(translate("menu.guestwelcome.alt"));
        root.addChild(gtn);
        root.setDelegate(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.guestinfo"));
        gtn.setUserObject("guestinfo");
        gtn.setAltText(translate("menu.guestinfo.alt"));
        root.addChild(gtn);

        return gtm;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers disposed by basicController
    }

}
