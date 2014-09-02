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

import org.olat.lms.ims.qti.process.AssessmentInstance;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.course.nodes.iq.AssessmentEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.instantmessaging.groupchat.GroupChatManagerController;
import org.olat.presentation.search.SearchController;
import org.olat.presentation.search.SearchServiceUIFactory;
import org.olat.presentation.search.SearchServiceUIFactory.DisplayOption;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for OlatTopNavController
 * <P>
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatTopNavController extends BasicController /*
                                                           * TODO:PB:OLAT-4047 implements GenericEventListener
                                                           */implements GenericEventListener {
    private static final String ACTION_LOGOUT = "logout";
    private final VelocityContainer topNavVC;
    private Controller imController;
    private GroupChatManagerController groupChatController;
    private SearchController searchC;

    // TODO:PB:OLAT-4047 private Link permLink;
    // TODO:PB:OLAT-4047 private VelocityContainer permsharp;
    private Link loginLink;

    private EventBus singleUserEventCenter;
    private OLATResourceable ass;

    public OlatTopNavController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        topNavVC = createVelocityContainer("topnav");

        // instant messaging area, only if user is not a guest and not an invited user
        if (InstantMessagingModule.isEnabled() && !ureq.getUserSession().getRoles().isGuestOnly() && !ureq.getUserSession().getRoles().isInvitee()) {
            imController = InstantMessagingModule.getAdapter().createClientController(ureq, this.getWindowControl());
            listenTo(imController);
            topNavVC.put("imclient", imController.getInitialComponent());
            groupChatController = InstantMessagingModule.getAdapter().getGroupChatManagerController(ureq);
            listenTo(groupChatController);
            topNavVC.put("groupchatcontroller", groupChatController.getGroupChatContainer());
        }

        // login link
        if (ureq.getIdentity() == null) {
            topNavVC.contextPut("isGuest", Boolean.TRUE);
            loginLink = LinkFactory.createLink("topnav.login", topNavVC, this);
            loginLink.setTooltip("topnav.login.alt", false);
        }

        // search functionality, only if user is not a guest and not an invited user
        if (!ureq.getUserSession().getRoles().isGuestOnly() && !ureq.getUserSession().getRoles().isInvitee()) {
            final SearchServiceUIFactory searchUIFactory = (SearchServiceUIFactory) CoreSpringFactory.getBean(SearchServiceUIFactory.class);
            searchC = searchUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
            searchC.setResourceContextEnable(false);
            topNavVC.put("search_input", searchC.getInitialComponent());
        }

        // TODO:PB:OLAT-4047 permLink =
        // LinkFactory.createLink("topnav.permlink", topNavVC, this);
        // TODO:PB:OLAT-4047 permLink.setTarget("_permlink");
        // TODO:PB:OLAT-4047 permsharp = createVelocityContainer("permsharp");
        // TODO:PB:OLAT-4047 Panel p = new Panel("refreshpermlink");
        // TODO:PB:OLAT-4047 p.setContent(permsharp);
        // TODO:PB:OLAT-4047 topNavVC.put("refreshpermlink",p);

        // TODO:PB:OLAT-4047
        // getWindowControl().getWindowBackOffice().addCycleListener(this);//receive
        // events to adjust URL

        if (ureq.getIdentity() != null) {
            ass = OresHelper.createOLATResourceableType(AssessmentEvent.class);
            singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
            singleUserEventCenter.registerFor(this, getIdentity(), ass);
        }

        putInitialPanel(topNavVC);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == topNavVC) {
            // System.out.println(event.getCommand());
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final String command = event.getCommand();
        if (source == loginLink) {
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
        } /*
           * //TODO:PB:OLAT-4047 else if (source == permLink){ WindowControl current = (WindowControl)getWindowControl().getWindowBackOffice().getWindow
           * ().getAttribute("BUSPATH"); String buspath = current != null ? JumpInManager.getRestJumpInUri(current.getBusinessControl()) : "NONE"; String postUrl =
           * Settings.getServerContextPathURI()+"/url/"+buspath; String deliciousPost = "http://del.icio.us/post?url="+postUrl; ControllerCreator ctrl =
           * BaseFullWebappPopupLayoutFactory.createRedirectingPopup(ureq, deliciousPost); openInNewBrowserWindow(ureq, ctrl); return; }
           */
        if (source == topNavVC) {
            if (command.equals(ACTION_LOGOUT)) {
                AuthHelper.doLogout(ureq);
            }
        }
    }

    @Override
    protected void doDispose() {
        // controllers are disposed by BasicController
        // im header controller mus be disposed last - content or navigation
        // control
        // controller
        // might try to send a IM presence message which would lazy generate a
        // new
        // IM client.
        // the IM client gets disposed in the header controller

        if (singleUserEventCenter != null) {
            singleUserEventCenter.deregisterFor(this, ass);
        }
    }

    @Override
    public void event(final Event event) {
        if (event instanceof AssessmentEvent) {
            if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
                topNavVC.contextPut("inAssessment", true);
                return;
            }
            if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
                final OLATResourceable a = OresHelper.createOLATResourceableType(AssessmentInstance.class);
                if (singleUserEventCenter.getListeningIdentityCntFor(a) < 1) {
                    topNavVC.contextPut("inAssessment", false);
                }
                return;
            }
        }
    }

    /*
     * TODO:PB:OLAT-4047 public void event(Event event) { if (event == Window.BEFORE_INLINE_RENDERING) { // create jump in path from the active main content WindowControl
     * WindowControl tmp = (WindowControl)getWindowControl ().getWindowBackOffice().getWindow().getAttribute("BUSPATH"); String buspath = tmp != null ?
     * JumpInManager.getRestJumpInUri(tmp.getBusinessControl()) : "NONE"; buspath = "/url/"+buspath; String postUrl =
     * Settings.getServerContextPathURI()+buspath;//TODO:PB:2009-06-02: move /url/ String to Spring config //udpate URL for the addthis javascript box in the topnav
     * velocity permsharp.contextPut("myURL", postUrl); permsharp.contextPut("buspath", buspath); } }
     */

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
