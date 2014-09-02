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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.presentation.commons.URIHelper;
import org.olat.presentation.commons.session.SessionInfo;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.exception.MsgFactory;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.logging.threadlog.UserBasedLogLevelManager;

/**
 * Initial Date: 28.11.2003
 * 
 * @author Mike Stock
 */
public class AuthenticatedDispatcher implements Dispatcher {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final String AUTHDISPATCHER_ENTRYURL = "AuthDispatcher:entryUrl";
    protected static final String AUTHDISPATCHER_BUSINESSPATH = "AuthDispatcher:businessPath";

    private static final String AUTHCHIEFCONTROLLER = "AUTHCHIEFCONTROLLER";
    protected static final String QUESTIONMARK = "?";
    protected static final String GUEST = "guest";
    protected static final String INVITATION = "invitation";
    protected static final String TRUE = "true";
    private static final String LANG = "lang";
    /** forces secure http connection to access olat if set to true **/
    private boolean forceSecureAccessOnly = false;
    private final UserBasedLogLevelManager userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();

    protected AuthenticatedDispatcher(final boolean forceSecureAccessOnly) {
        this.forceSecureAccessOnly = forceSecureAccessOnly;
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
        long startExecute = 0;
        if (log.isDebugEnabled()) {
            startExecute = System.currentTimeMillis();
        }
        final UserSession usess = UserSession.getUserSession(request);
        UserRequest ureq = null;
        try {
            // upon creation URL is checked for
            ureq = new UserRequest(uriPrefix, request, response);
        } catch (final NumberFormatException nfe) {
            // MODE could not be decoded
            // typically if robots with wrong urls hit the system
            // or user have bookmarks
            // or authors copy-pasted links to the content.
            // showing redscreens for non valid URL is wrong instead
            // a 404 message must be shown -> e.g. robots correct their links.
            if (log.isDebugEnabled()) {
                log.debug("Bad Request " + request.getPathInfo());
            }
            DispatcherAction.sendBadRequest(request.getPathInfo(), response);
            return;
        }

        // GUIInterna.setLoadPerformanceMode(ureq);
        // GUIInterna.setUserSession (usess);

        final boolean auth = usess.isAuthenticated();

        if (!auth) {
            if (!ureq.isValidDispatchURI()) {
                // might be a direct jump request -> remember it if not logged in yet
                final String reqUri = request.getRequestURI();
                final String query = request.getQueryString();
                final String allGet = reqUri + QUESTIONMARK + query;
                usess.putEntryInNonClearedStore(AUTHDISPATCHER_ENTRYURL, allGet);
            }
            final String guestAccess = ureq.getParameter(GUEST);
            if (guestAccess == null || !LoginModule.isGuestLoginLinksEnabled()) {
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            } else if (guestAccess.equals(TRUE)) {
                // try to log in as anonymous
                // use the language from the lang paramter if available, otherwhise use the system default locale
                final String guestLang = ureq.getParameter("lang");
                Locale guestLoc;
                if (guestLang == null) {
                    guestLoc = I18nModule.getDefaultLocale();
                } else {
                    guestLoc = I18nManager.getInstance().getLocaleOrDefault(guestLang);
                }
                final int loginStatus = AuthHelper.doAnonymousLogin(ureq, guestLoc);
                if (loginStatus != AuthHelper.LOGIN_OK) {
                    if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                        DispatcherAction.redirectToServiceNotAvailable(response);
                    }
                    DispatcherAction.redirectToDefaultDispatcher(response); // error, redirect to login screen
                    return;
                }
                // else now logged in as anonymous user, continue
            }
        }

        // authenticated!
        try {

            // kill session if not secured via SSL
            if (forceSecureAccessOnly && !request.isSecure()) {
                final SessionInfo sessionInfo = usess.getSessionInfo();
                if (sessionInfo != null) {
                    final HttpSession session = sessionInfo.getSession();
                    if (session != null) {
                        try {
                            session.invalidate();
                        } catch (final IllegalStateException ise) {
                            // thrown when session already invalidated. fine. ignore.
                        }
                    }
                }
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            }

            final SessionInfo sessionInfo = usess.getSessionInfo();
            if (sessionInfo == null) {
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            }

            if (userBasedLogLevelManager != null) {
                userBasedLogLevelManager.activateUsernameBasedLogLevel(sessionInfo.getLogin());
            }

            sessionInfo.setLastClickTime();
            final String origUrl = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_ENTRYURL);
            if (origUrl != null) {
                // we had a direct jump request
                // to avoid a endless redirect, remove the guest parameter if any
                // this can happen if a guest has cookies disabled
                final String url = new URIHelper(origUrl).removeParameter(GUEST).toString();
                DispatcherAction.redirectTo(response, url);
                return;
            }
            final String businessPath = (String) usess.removeEntryFromNonClearedStore(AUTHDISPATCHER_BUSINESSPATH);
            if (businessPath != null) {
                final BusinessControl bc = BusinessControlFactory.getInstance().createFromString(businessPath);
                final ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");

                if (cc == null) {
                    log.error("I-130611-0017 [url=" + request.getRequestURI());
                }
                final WindowControl wControl = cc.getWindowControl();
                final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
                NewControllerFactory.getInstance().launch(ureq, bwControl);
                // render the window
                final Window w = cc.getWindow();
                w.dispatchRequest(ureq, true); // renderOnly
                return;
            }

            if (ureq.isValidDispatchURI()) { // valid uri for dispatching (has timestamp, componentid and
                // windowid)
                final Windows ws = Windows.getWindows(ureq);
                final Window window = ws.getWindow(ureq);
                if (window == null) {
                    // If no window, this is probably a stale link. send not
                    // found
                    // note: do not redirect to login since this wastes a new
                    // window each time since we are in an authenticated session
                    // -> a content packaging with wrong links e.g. /css/my.css
                    // wastes all the windows
                    DispatcherAction.sendNotFound(request.getRequestURI(), response);
                    return;
                }
                long startDispatchRequest = 0;
                if (log.isDebugEnabled()) {
                    startDispatchRequest = System.currentTimeMillis();
                }
                window.dispatchRequest(ureq);
                if (log.isDebugEnabled()) {
                    final long durationDispatchRequest = System.currentTimeMillis() - startDispatchRequest;
                    log.debug("Perf-Test: window=" + window);
                    log.debug("Perf-Test: durationDispatchRequest=" + durationDispatchRequest);
                }
            }
        } catch (final Throwable th) {
            // Do not log as Warn or Error here, log as ERROR in MsgFactory => ExceptionWindowController throws an OLATRuntimeException
            log.debug("handleError in AuthenticatedDispatcher throwable=" + th);
            DispatcherAction.handleError();
            final ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
            // the controller's window must be failsafe also
            msgcc.getWindow().dispatchRequest(ureq, true);
            // do not dispatch (render only), since this is a new Window created as
            // a result of another window's click.
        } finally {
            if (userBasedLogLevelManager != null) {
                userBasedLogLevelManager.deactivateUsernameBasedLogLevel();
            }
            if (log.isDebugEnabled()) {
                final long durationExecute = System.currentTimeMillis() - startExecute;
                log.debug("Perf-Test: durationExecute=" + durationExecute);
            }
            // XX:GUIInterna.setLoadPerformanceMode(null);
        }

    }

}
