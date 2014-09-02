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

import org.olat.data.basesecurity.Identity;
import org.olat.lms.instantmessaging.ConncectedUsersHelper;
import org.olat.lms.instantmessaging.InstantMessagingModule;
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
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.instantmessaging.ConnectedClientsListController;
import org.olat.system.commons.Settings;
import org.olat.system.event.Event;

/**
 * Overrides the default footer of the webapplication framework showing the
 * <ul>
 * <li>connected users</li>
 * <li>username</li>
 * <li>powered by</li>
 * </ul>
 * <P>
 * Initial Date: 16.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatGuestFooterController extends BasicController {
    private final VelocityContainer olatFootervc;
    private final Link showOtherUsers, loginLink;

    public OlatGuestFooterController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        olatFootervc = createVelocityContainer("olatGuestFooter");
        //
        final Identity identity = ureq.getIdentity();
        // 6.1.0 Code => Nullpointer @ OlatGuestFooterController.java:65
        // Boolean isGuest = (identity == null ? Boolean.TRUE : new Boolean(ureq.getUserSession().getRoles().isGuestOnly()));
        boolean isGuest = true;
        if ((identity == null) || (ureq.getUserSession() == null) || (ureq.getUserSession().getRoles() == null)) {
            isGuest = true;
        } else {
            isGuest = ureq.getUserSession().getRoles().isGuestOnly();
        }
        // some variables displayed in the footer
        olatFootervc.contextPut("username",
                identity != null ? getTranslator().translate("username", new String[] { identity.getName() }) : getTranslator().translate("not.logged.in"));
        // is user guest or not looged in?
        olatFootervc.contextPut("isGuest", isGuest);

        showOtherUsers = LinkFactory.createLink("other.users.online", olatFootervc, this);
        showOtherUsers.setAjaxEnabled(false);
        showOtherUsers.setTarget("_blanc");
        if (isGuest) {
            showOtherUsers.setEnabled(false);
        }

        loginLink = LinkFactory.createLink("footer.login", olatFootervc, this);

        olatFootervc.contextPut("olatversion", Settings.getFullVersionInfo() + " " + Settings.getNodeInfo());
        // instant messaging awareness
        olatFootervc.contextPut("instantMessagingEnabled", new Boolean(InstantMessagingModule.isEnabled()));
        if (InstantMessagingModule.isEnabled()) {
            olatFootervc.contextPut("connectedUsers", new ConncectedUsersHelper());
        }
        //
        putInitialPanel(olatFootervc);
    }

    /**
     * org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == showOtherUsers) {
            // show list of other online users that are connected to jabber server
            final ControllerCreator ctrlCreator = new ControllerCreator() {
                @Override
                public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                    return new ConnectedClientsListController(lureq, lwControl);
                }
            };
            // wrap the content controller into a full header layout
            final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
            // open in new browser window
            final PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
            pbw.open(ureq);
            //
        } else if (source == loginLink) {
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
        }

    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
