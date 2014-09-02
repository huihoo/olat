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

package org.olat.presentation.framework.layout;

import org.olat.lms.course.CourseFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.popup.PopupBrowserWindow;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: patrickb Class Description for OlatUpperRightCorner
 * <P>
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatGuestTopNavController extends BasicController {
    private final VelocityContainer topNavVC;
    private final Link helpLink;
    private final Link loginLink;

    public OlatGuestTopNavController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        topNavVC = createVelocityContainer("guesttopnav");

        // the help link
        helpLink = LinkFactory.createLink("topnav.help", topNavVC, this);
        helpLink.setTooltip("topnav.help.alt", false);
        helpLink.setTarget("_help");
        loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
        loginLink.setTooltip("topnav.login.alt", false);

        //
        putInitialPanel(topNavVC);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == loginLink) {
            AuthHelper.doLogout(ureq);
        } else if (source == helpLink) {
            final ControllerCreator ctrlCreator = new ControllerCreator() {
                @Override
                public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                    return CourseFactory.createHelpCourseLaunchController(lureq, lwControl);
                }
            };
            // wrap the content controller into a full header layout
            final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
            // open in new browser window
            final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
            pbw.open(ureq);
            //
        }
    }

    @Override
    protected void doDispose() {
        // controllers disposed by BasicController:
    }

}
