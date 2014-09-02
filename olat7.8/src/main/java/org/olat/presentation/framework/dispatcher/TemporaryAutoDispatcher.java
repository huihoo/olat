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

package org.olat.presentation.framework.dispatcher;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.chiefcontrollers.BaseChiefControllerCreator;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.exception.MsgFactory;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappController;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappControllerParts;
import org.olat.presentation.security.authentication.AuthBFWCParts;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Felix Jost
 */
public class TemporaryAutoDispatcher implements Dispatcher {

    private static final Logger log = LoggerHelper.getLogger();

    protected TemporaryAutoDispatcher() {
    }

    @Autowired
    private BaseSecurity baseSecurity;

    /**
     * @param ureq
     * @return chiefcontroller
     */
    private ChiefController createAuthHome(final UserRequest ureq) {
        if (!ureq.getUserSession().isAuthenticated()) {
            throw new AssertException("not authenticated!");
        }

        final BaseChiefControllerCreator bbc = new BaseChiefControllerCreator();
        bbc.setContentControllerCreator(/* this is later injected by spring */new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                final BaseFullWebappControllerParts authSitesAndNav = new AuthBFWCParts();
                return new BaseFullWebappController(lureq, lwControl, authSitesAndNav);
            }
        });

        final ChiefController cc = bbc.createChiefController(ureq);
        return cc;
    }

    /**
     * Main method called by DispatcherAction. This processess all requests for authenticated users.
     * 
     * @param request
     * @param response
     * @param uriPrefix
     */
    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, final String uriPrefix) {
        final UserSession usess = UserSession.getUserSession(request);
        UserRequest ureq = null;

        try {
            ureq = new UserRequest(uriPrefix, request, response);
            final boolean auth = usess.isAuthenticated();

            if (!auth || !ureq.isValidDispatchURI()) {
                // String lang =
                // I18nManager.getInstance().getDefaultLocale().toString();
                final Identity ident = baseSecurity.findIdentityByName("administrator");

                usess.signOffAndClear();
                usess.setIdentity(ident);
                usess.setRoles(new Roles(true, true, true, true, false, true, false));
                usess.setLocale(I18nManager.getInstance().getLocaleOrDefault(ident.getUser().getPreferences().getLanguage()));

                // brasato:: was
                // Windows.getWindows(ureq).getWindowManager().getGlobalSettings().setFontSize(
                // identity.getUser().getPreferences().getFontsize() );
                Windows.getWindows(ureq).setAttribute("fontsize", ident.getUser().getPreferences().getFontsize());

                AuthHelper.setSessionInfoFor(ident, AUTHENTICATION_PROVIDER_OLAT, ureq);
                // confirm signedOn
                usess.signOn();

                //
                Windows.getWindows(ureq).getWindowManager().setAjaxWanted(ureq, true);

                // successfull login, reregister window
                final ChiefController occ = createAuthHome(ureq);
                final Window currentWindow = occ.getWindow();
                currentWindow.setUriPrefix(WebappHelper.getServletContextPath() + "/temp/");
                Windows.getWindows(ureq).registerWindow(currentWindow);
                // render only
                currentWindow.dispatchRequest(ureq, true);

            } else {
                // auth: get window
                final Windows ws = Windows.getWindows(ureq);
                final Window window = ws.getWindow(ureq);
                window.dispatchRequest(ureq);
            }

        } catch (final Throwable th) {
            try {
                final ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
                // the controller's window must be failsafe also
                msgcc.getWindow().dispatchRequest(ureq, true);
                // do not dispatch (render only), since this is a new Window created as
                // a result of another window's click.
            } catch (final Throwable t) {
                log.error("We're fucked up....", t);
            }
        }
    }

}
