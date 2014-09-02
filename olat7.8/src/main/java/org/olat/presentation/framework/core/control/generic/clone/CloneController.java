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

package org.olat.presentation.framework.core.control.generic.clone;

import org.apache.log4j.Logger;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 05.01.2006
 * 
 * @author Felix Jost
 */
public class CloneController extends BasicController {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CMD_CLONE = "cl";
    private VelocityContainer mainVC;

    private CloneableController readyToCloneC;
    private CloneLayoutControllerCreatorCallback layoutCreator;

    /**
     * @param ureq
     *            UserRequest
     * @param wControl
     *            windowControl
     * @param readyToCloneC
     *            the controller which will be cloned when the user shows the "open in popup" button/icon
     * @param useMinimalLayout
     *            if true, the popupwindow will only have a "close" icon, if false: the popup window will be a normal popup window with olat headers/footers
     * @param the
     *            layout creator used to wrap the content controller
     */
    public CloneController(UserRequest ureq, WindowControl wControl, CloneableController readyToCloneC, CloneLayoutControllerCreatorCallback layoutCreator) {
        super(ureq, wControl);
        this.readyToCloneC = readyToCloneC;
        this.layoutCreator = layoutCreator;

        mainVC = createVelocityContainer("offerclone");
        mainVC.put("cloneableCC", readyToCloneC.getInitialComponent());

        mainVC.contextPut("winid", "w" + mainVC.getDispatchID());
        putInitialPanel(mainVC);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == mainVC) {
            if (event.getCommand().equals(CMD_CLONE)) { // clone request
                // TODO:pb: -> link component add new method / rename setTarget() to setPopupeable()
                // setPopup() -> deactivate AJAX on link
                ControllerCreator cloneControllerCreator = new ControllerCreator() {
                    @Override
                    @SuppressWarnings("synthetic-access")
                    public Controller createController(UserRequest lureq, WindowControl lwControl) {
                        return readyToCloneC.cloneController(lureq, lwControl);
                    }
                };

                ControllerCreator newWindowContent;
                if (layoutCreator != null) {
                    // wrap with layout
                    newWindowContent = layoutCreator.createLayoutControllerCreator(ureq, cloneControllerCreator);
                } else {
                    // use default layout
                    newWindowContent = cloneControllerCreator;
                }
                // open in new window
                openInNewBrowserWindow(ureq, newWindowContent);
                return;
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // delete the initial controller, but -not- the clones (they appear in a
        // independent browser window)
        if (readyToCloneC != null)
            readyToCloneC.dispose();
    }

}
