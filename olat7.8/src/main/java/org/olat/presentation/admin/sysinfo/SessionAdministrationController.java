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

package org.olat.presentation.admin.sysinfo;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.admin.AdminModule;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.text.TextFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.commons.Formatter;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Christian Guretzki
 */

public class SessionAdministrationController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(SysinfoController.class);

    private final VelocityContainer myContent;
    private final Link invalidateAllSsssionLink;
    private DialogBoxController invalidateAllConfirmController;
    private final Link blockLoginLink;
    private final Link allowLoginLink;
    private DialogBoxController blockLoginConfirmController;
    private Link rejectDMZReuqestsLink;
    private Link allowDMZRequestsLink;
    private DialogBoxController rejectDMZRequestsConfirmController;
    private final SessionAdminForm sessionAdminForm;
    private final SessionAdminOldestSessionForm sessionAdminOldestSessionForm;

    /**
     * Controlls user session in admin view.
     * 
     * @param ureq
     * @param wControl
     */
    public SessionAdministrationController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        myContent = createVelocityContainer("sessionadministration");

        invalidateAllSsssionLink = LinkFactory.createButton("session.admin.invalidate.all.link", myContent, this);
        myContent.contextPut("loginBlocked", AdminModule.isLoginBlocked());
        blockLoginLink = LinkFactory.createButton("session.admin.block.login.link", myContent, this);

        final boolean showRejectLink = CoordinatorManager.getInstance().getCoordinator().isClusterMode();
        myContent.contextPut("showRejectDMZRequestsLink", showRejectLink);
        if (showRejectLink) {
            myContent.contextPut("rejectingDMZRequests", AuthHelper.isRejectDMZRequests());

            TextFactory.createTextComponentFromI18nKey("session.admin.reject.dmz.requests.intro", "session.admin.reject.dmz.requests.intro", getTranslator(), null, true,
                    myContent);
            TextFactory.createTextComponentFromI18nKey("session.admin.allow.dmz.requests.intro", "session.admin.allow.dmz.requests.intro", getTranslator(), null, true,
                    myContent);
            rejectDMZReuqestsLink = LinkFactory.createButton("session.admin.reject.dmz.requests.link", myContent, this);
            allowDMZRequestsLink = LinkFactory.createButton("session.admin.allow.dmz.requests.link", myContent, this);
        }
        allowLoginLink = LinkFactory.createButton("session.admin.allow.login.link", myContent, this);
        sessionAdminOldestSessionForm = new SessionAdminOldestSessionForm(ureq, wControl, getTranslator());
        listenTo(sessionAdminOldestSessionForm);
        myContent.put("session.admin.oldest.session.form", sessionAdminOldestSessionForm.getInitialComponent());
        sessionAdminForm = new SessionAdminForm(ureq, wControl, getTranslator(), AdminModule.getSessionTimeout(), AdminModule.getMaxSessions());
        listenTo(sessionAdminForm);
        myContent.put("session.admin.form", sessionAdminForm.getInitialComponent());
        myContent.contextPut("usersessions", getUsersSessionAsString(ureq));
        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == invalidateAllSsssionLink) {
            invalidateAllConfirmController = activateYesNoDialog(ureq, null, translate("invalidate.all.sure"), invalidateAllConfirmController);
            return;
        } else if (source == blockLoginLink) {
            blockLoginConfirmController = activateYesNoDialog(ureq, null, translate("block.login.sure"), invalidateAllConfirmController);
        } else if (source == rejectDMZReuqestsLink) {
            rejectDMZRequestsConfirmController = activateYesNoDialog(ureq, null, translate("reject.dmz.requests.sure"), rejectDMZRequestsConfirmController);
        } else if (source == allowDMZRequestsLink) {
            AdminModule.setRejectDMZRequests(false);
            myContent.contextPut("rejectingDMZRequests", AdminModule.isRejectDMZRequests());
            showInfo("allow.dmz.requests.done");
        } else if (source == allowLoginLink) {
            AdminModule.setLoginBlocked(false);
            myContent.contextPut("loginBlocked", Boolean.FALSE);
            showInfo("allow.login.done");
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == invalidateAllConfirmController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final int nbrOfInvalidatedSessions = AdminModule.invalidateAllSessions();
                showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
            }
        } else if (source == blockLoginConfirmController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                AdminModule.setLoginBlocked(true);
                myContent.contextPut("loginBlocked", Boolean.TRUE);
                showInfo("block.login.done");
            }
        } else if (source == rejectDMZRequestsConfirmController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                AdminModule.setRejectDMZRequests(true);
                myContent.contextPut("rejectingDMZRequests", AuthHelper.isRejectDMZRequests());
                showInfo("reject.dmz.requests.done");
            }
        } else if (source == sessionAdminOldestSessionForm) {
            final int nbrOfInvalidatedSessions = AdminModule.invalidateOldestSessions(sessionAdminOldestSessionForm.getNbrSessions());
            showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
        } else if (source == sessionAdminForm && event == Event.DONE_EVENT) {
            AdminModule.setSessionTimeout(sessionAdminForm.getSessionTimeout());
            AdminModule.setMaxSessions(sessionAdminForm.getMaxSessions());
        }

    }

    @Override
    protected void doDispose() {
        // DialogBoxController and TableController get disposed by BasicController
    }

    private String getUsersSessionAsString(final UserRequest ureq) {
        final StringBuilder sb = new StringBuilder();
        final int ucCnt = UserSession.getUserSessionsCnt();
        final Set usesss = UserSession.getAuthenticatedUserSessions();
        final int contcnt = DefaultController.getControllerCount();
        sb.append("total usersessions (auth and non auth): " + ucCnt + "<br />auth usersessions: " + usesss.size()
                + "<br />Total Controllers (active, not disposed) of all users:" + contcnt + "<br /><br />");
        final Formatter f = Formatter.getInstance(ureq.getLocale());
        for (final Iterator iter = usesss.iterator(); iter.hasNext();) {
            final UserSession usess = (UserSession) iter.next();
            final Identity iden = usess.getIdentity();
            sb.append("authusersession (").append(usess.hashCode()).append(") of ");
            if (iden != null) {
                sb.append(iden.getName()).append(" ").append(iden.getKey());
            } else {
                sb.append(" - ");
            }
            sb.append("<br />");
            final Windows ws = Windows.getWindows(usess);
            for (final Iterator iterator = ws.getWindowIterator(); iterator.hasNext();) {
                final Window window = (Window) iterator.next();
                sb.append("- window ").append(window.getDispatchID()).append(" ").append(window.getLatestDispatchComponentInfo()).append("<br />");
            }
            sb.append("<br />");
        }
        return sb.toString();
    }

}
