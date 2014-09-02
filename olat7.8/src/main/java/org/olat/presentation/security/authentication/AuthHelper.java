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

package org.olat.presentation.security.authentication;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.Roles;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatLoggingAction;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.mediaresource.RedirectMediaResource;
import org.olat.lms.preferences.Preferences;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.BaseSecurityService;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.session.SessionInfo;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.WindowManager;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.chiefcontrollers.BaseChiefControllerCreator;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappController;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappControllerParts;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class AuthHelper {
    /**
     * <code>LOGOUT_PAGE</code>
     */
    public static final String LOGOUT_PAGE = "logout.html";
    public static final int LOGIN_OK = 0;
    private static final int LOGIN_FAILED = 1;
    private static final int LOGIN_DENIED = 2;
    public static final int LOGIN_NOTAVAILABLE = 3;

    private static final int MAX_SESSION_NO_LIMIT = 0;

    /**
     * whether or not requests to dmz (except those coming via 'switch-to-node' cluster feature) are rejected hence resulting the browser to go to another node. Note:
     * this is not configurable currently as it's more of a runtime choice to change this to true
     */
    private static boolean rejectDMZRequests = false;

    private static boolean loginBlocked = false;
    private static int maxSessions = MAX_SESSION_NO_LIMIT;

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Used by DMZDispatcher to do regular logins and by ShibbolethDispatcher which is somewhat special because logins are handled asynchronuous -> therefore a dedicated
     * dispatcher is needed which also has to have access to the doLogin() method.
     * 
     * @param identity
     * @param authProvider
     * @param ureq
     * @return True if success, false otherwise.
     */
    public static int doLogin(final Identity identity, final String authProvider, final UserRequest ureq) {
        final int initializeStatus = initializeLogin(identity, authProvider, ureq);
        if (initializeStatus != LOGIN_OK) {
            return initializeStatus; // login not successfull
        }

        // do logging
        ThreadLocalUserActivityLogger.log(OlatLoggingAction.OLAT_LOGIN, AuthHelper.class, LoggingResourceable.wrap(identity));

        // brasato:: fix it
        // successfull login, reregister window
        ChiefController occ;
        if (ureq.getUserSession().getRoles().isGuestOnly()) {
            occ = createGuestHome(ureq);
        } else {
            occ = createAuthHome(ureq);
        }

        final Window currentWindow = occ.getWindow();
        currentWindow.setUriPrefix(WebappHelper.getServletContextPath() + DispatcherAction.PATH_AUTHENTICATED);
        Windows.getWindows(ureq).registerWindow(currentWindow);

        // redirect to AuthenticatedDispatcher
        // IMPORTANT: windowID has changed due to re-registering current window -> do not use ureq.getWindowID() to build new URLBuilder.
        final URLBuilder ubu = new URLBuilder(WebappHelper.getServletContextPath() + DispatcherAction.PATH_AUTHENTICATED, currentWindow.getInstanceId(), "1", null);
        final StringOutput sout = new StringOutput(30);
        ubu.buildURI(sout, null, null);
        ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(sout.toString()));

        return LOGIN_OK;
    }

    /**
     * @param identity
     * @param authProvider
     * @param ureq
     * @return
     */
    public static int doHeadlessLogin(final Identity identity, final String authProvider, final UserRequest ureq) {
        final int initializeStatus = initializeLogin(identity, authProvider, ureq);
        if (initializeStatus != LOGIN_OK) {
            return initializeStatus; // login not successful
        }
        // Set session info to reflect the REST headless login
        final UserSession usess = ureq.getUserSession();
        usess.getSessionInfo().setREST(true);
        usess.getIdentityEnvironment().getAttributes().put("isrest", "true");
        //
        ThreadLocalUserActivityLogger.log(OlatLoggingAction.OLAT_LOGIN, AuthHelper.class, LoggingResourceable.wrap(identity));
        return LOGIN_OK;
    }

    /**
     * Create a base chief controller for the current anonymous user request and initialize the first screen after login. Note, the user request must be authenticated,
     * but as an anonymous user and not a known user.
     * 
     * @param ureq
     *            The authenticated user request.
     * @return The chief controller
     */
    private static ChiefController createGuestHome(final UserRequest ureq) {
        if (!ureq.getUserSession().isAuthenticated()) {
            throw new AssertException("not authenticated!");
        }

        final BaseChiefControllerCreator bbc = new BaseChiefControllerCreator();
        bbc.setContentControllerCreator(/* this is later injected by spring */new ControllerCreator() {
            @Override
            public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                final BaseFullWebappControllerParts guestSitesAndNav = new GuestBFWCParts();
                return new BaseFullWebappController(lureq, lwControl, guestSitesAndNav);
            }
        });

        final ChiefController cc = bbc.createChiefController(ureq);
        Windows.getWindows(ureq.getUserSession()).setAttribute("AUTHCHIEFCONTROLLER", cc);
        log.debug("set session-attribute 'AUTHCHIEFCONTROLLER'");
        return cc;
    }

    /**
     * Create a base chief controller for the current authenticated user request and initialize the first screen after login.
     * 
     * @param ureq
     *            The authenticated user request.
     * @return The chief controller
     */
    public static ChiefController createAuthHome(final UserRequest ureq) {
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
        Windows.getWindows(ureq.getUserSession()).setAttribute("AUTHCHIEFCONTROLLER", cc);
        log.debug("set session-attribute 'AUTHCHIEFCONTROLLER'");
        return cc;
    }

    /**
     * Logs in as anonymous user using the given language key. If the current installation does not support this language, the systems default language is used instead
     * 
     * @param ureq
     *            The user request
     * @param lang
     *            The language of the anonymous user or null if system default should be used
     * @return true if login was successful, false otherwise
     */
    public static int doAnonymousLogin(final UserRequest ureq, Locale locale) {
        final Set<String> supportedLanguages = I18nModule.getEnabledLanguageKeys();
        if (locale == null || !supportedLanguages.contains(locale.toString())) {
            locale = I18nModule.getDefaultLocale();
        }
        final Translator trans = PackageUtil.createPackageTranslator(UserInfoMainController.class, locale);
        final Identity guestIdent = getBaseSecurityService().getAndUpdateAnonymousUserForLanguage(locale, trans.translate("user.guest"));
        final int loginStatus = doLogin(guestIdent, AUTHENTICATION_PROVIDER_OLAT, ureq);
        return loginStatus;
    }

    private static BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private static BaseSecurityService getBaseSecurityService() {
        return CoreSpringFactory.getBean(BaseSecurityService.class);
    }

    public static int doInvitationLogin(final String invitationToken, final UserRequest ureq, Locale locale) {
        final Invitation invitation = getBaseSecurityEBL().isInvitationValid(invitationToken);
        if (invitation == null) {
            return LOGIN_DENIED;
        }

        // check if identity exists
        final Identity identity = getUserService().findIdentityByEmail(invitation.getMail());
        if (identity != null) {
            boolean isIdentityAlreadyAnOlatUser = getBaseSecurityEBL().isOlatUser(identity);
            if (isIdentityAlreadyAnOlatUser) {
                // already a normal olat user, cannot be invited
                return LOGIN_DENIED;
            } else {
                return doLogin(identity, AUTHENTICATION_PROVIDER_OLAT, ureq);
            }
        }

        final Set<String> supportedLanguages = I18nModule.getEnabledLanguageKeys();
        if (locale == null || !supportedLanguages.contains(locale.toString())) {
            locale = I18nModule.getDefaultLocale();
        }

        // invitation ok -> create a temporary user
        // TODO make an username beautifier???
        final Identity invited = getBaseSecurityEBL().createUserWithRandomUsername(locale, invitation);
        return doLogin(invited, AUTHENTICATION_PROVIDER_OLAT, ureq);
    }

    private static BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * ONLY for authentication provider OLAT Authenticate Identity and do the necessary work. Returns true if successfull, false otherwise.
     * 
     * @param identity
     * @param authProvider
     * @param ureq
     * @return boolean
     */
    private static int initializeLogin(final Identity identity, final String authProvider, final UserRequest ureq) {
        // continue only if user has login permission.
        if (identity == null) {
            return LOGIN_FAILED;
        }
        // test if a user may not logon, since he/she is in the PERMISSION_LOGON
        if (!getBaseSecurity().isIdentityVisible(identity.getName())) {
            log.info("Audit:was denied login");
            return LOGIN_DENIED;
        }
        // if the user sending the cookie did not log out and we are logging in
        // again, then we need to make sure everything is cleaned up. we cleanup in all cases.
        final UserSession usess = ureq.getUserSession();
        // prepare for a new user: clear all the instance vars of the userSession
        // note: does not invalidate the session, since it is reused
        usess.signOffAndClear();
        // init the UserSession for the new User
        // we can set the identity and finish the log in process
        usess.setIdentity(identity);
        setRolesFor(identity, usess);

        // check if loginDenied or maxSession (only for non-admin)
        if ((loginBlocked && !usess.getRoles().isOLATAdmin())
                || (((maxSessions != MAX_SESSION_NO_LIMIT) && (UserSession.getUserSessionsCnt() >= maxSessions)) && !usess.getRoles().isOLATAdmin())) {
            log.info("Audit:Login was blocked for username=" + usess.getIdentity().getName() + ", loginBlocked=" + loginBlocked + " NbrOfSessions="
                    + UserSession.getUserSessionsCnt());
            usess.signOffAndClear();
            return LOGIN_NOTAVAILABLE;
        }

        // set authprovider
        // usess.getIdentityEnvironment().setAuthProvider(authProvider);

        // set the language
        usess.setLocale(I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage()));
        // update fontsize in users session globalsettings
        Windows.getWindows(ureq).getWindowManager().setFontSize(Integer.parseInt(identity.getUser().getPreferences().getFontsize()));
        // calculate session info and attach it to the user session
        setSessionInfoFor(identity, authProvider, ureq);
        // confirm signedOn
        usess.signOn();
        // set users web delivery mode
        setAjaxModeFor(ureq);
        // update web delivery mode in session info
        ureq.getUserSession().getSessionInfo().setWebModeFromUreq(ureq);
        return LOGIN_OK;
    }

    /**
     * This is a convenience method to log out. IMPORTANT: This method initiates a redirect and RETURN. Make sure you return the call hierarchy gracefully. Most of all,
     * don't touch HttpServletRequest or the Session after you call this method.
     * 
     * @param ureq
     */
    public static void doLogout(final UserRequest ureq) {
        // clear session settings of replayable urls / load performance mode
        // XX:GUIInterna.setLoadPerformanceMode(null);
        final Boolean wasGuest = ureq.getUserSession().getRoles().isGuestOnly();
        final String lang = I18nManager.getInstance().getLocaleKey(ureq.getLocale());
        final HttpSession session = ureq.getHttpReq().getSession(false);
        // session.removeAttribute(SessionListener.SESSIONLISTENER_KEY);
        // TODO: i assume tomcat, after s.invalidate(), lets the GC do the work
        // if not, then do a s.removeAttribute....
        // next line fires a valueunbound event to UserSession, which does some
        // stuff on logout
        if (session != null) {
            try {
                session.invalidate();
                deleteShibsessionCookie(ureq);
            } catch (final IllegalStateException ise) {
                // thrown when session already invalidated. fine. ignore.
            }
        }

        // redirect to logout page in dmz realm, set info that DMZ is shown because of logout
        // if it was a guest user, do not set logout=true. The parameter must be evaluated
        // by the implementation of the AuthenticationProvider.
        final String setWarning = wasGuest ? "" : "&logout=true";
        ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(WebappHelper.getServletContextPath() + "/dmz/?lang=" + lang + setWarning));
    }

    private static void deleteShibsessionCookie(final UserRequest ureq) {
        // try to delete the "shibsession" cookie for this ureq, if any found
        final Cookie[] cookies = ureq.getHttpReq().getCookies();
        Cookie cookie = null;
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                /*
                 * if(log.isDebugEnabled()) { log.info("found cookie with name: " + cookies[i].getName() + " and value: " + cookies[i].getValue()); }
                 */
                if (cookies[i].getName().indexOf("shibsession") != -1) { // contains "shibsession"
                    cookie = cookies[i];
                    break;
                }
            }
        }
        if (cookie != null) {
            // A zero value causes the cookie to be deleted.
            cookie.setMaxAge(0);
            // cookie.setMaxAge(-1); //TODO: LD: check this out as well
            cookie.setPath("/");
            ureq.getHttpResp().addCookie(cookie);
            if (log.isDebugEnabled()) {
                log.info("AuthHelper - shibsession cookie deleted");
            }
        }
    }

    /**
     * Set AJAX / Web 2.0 based on User GUI-Preferences and configuration. If the "ajax feature" checkbox in the user settings is enabled, turn on ajax (do not care about
     * which browser)
     * 
     * @param ureq
     */
    private static void setAjaxModeFor(final UserRequest ureq) {

        if (GUIInterna.isLoadPerformanceMode()) {
            Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
            return;
        }

        final Preferences prefs = ureq.getUserSession().getGuiPreferences();

        final Boolean web2aEnabled = (Boolean) prefs.get(WindowManager.class, "web2a-beta-on");
        // first check for web2a mode which wants ajax off
        if (web2aEnabled != null && web2aEnabled.booleanValue()) {
            Windows.getWindows(ureq).getWindowManager().setForScreenReader(true);
            Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
            return;
        }

        final Boolean ajaxOn = (Boolean) ureq.getUserSession().getGuiPreferences().get(WindowManager.class, "ajax-beta-on");
        // if user does not have an gui preference it will be only enabled if globally on and browser is capable
        if (ajaxOn != null) {
            Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(ajaxOn.booleanValue());
        } else {
            // enable ajax if olat configured and browser matching
            Windows.getWindows(ureq).getWindowManager().setAjaxWanted(ureq, true);
        }
    }

    /**
     * Build session info
     * 
     * @param identity
     * @param authProvider
     * @param ureq
     */
    public static void setSessionInfoFor(final Identity identity, final String authProvider, final UserRequest ureq) {
        final HttpSession session = ureq.getHttpReq().getSession();
        final SessionInfo sinfo = new SessionInfo(identity.getName(), session);
        sinfo.setFirstname(getUserService().getUserProperty(identity.getUser(), UserConstants.FIRSTNAME, ureq.getLocale()));
        sinfo.setLastname(getUserService().getUserProperty(identity.getUser(), UserConstants.LASTNAME, ureq.getLocale()));
        sinfo.setFromIP(ureq.getHttpReq().getRemoteAddr());
        sinfo.setFromFQN(ureq.getHttpReq().getRemoteAddr());
        try {
            final InetAddress[] iaddr = InetAddress.getAllByName(ureq.getHttpReq().getRemoteAddr());
            if (iaddr.length > 0) {
                sinfo.setFromFQN(iaddr[0].getHostName());
            }
        } catch (final UnknownHostException e) {
            // ok, already set IP as FQDN
        }
        sinfo.setAuthProvider(authProvider);
        sinfo.setUserAgent(ureq.getHttpReq().getHeader("User-Agent"));
        sinfo.setSecure(ureq.getHttpReq().isSecure());
        sinfo.setLastClickTime();
        // set session info for this session
        final UserSession usess = ureq.getUserSession();
        usess.setSessionInfo(sinfo);
        // For Usertracking, let the User object know about some desired/specified infos from the sessioninfo
        final Map<String, String> sessionInfoForUsertracking = new HashMap<String, String>();
        sessionInfoForUsertracking.put("language", usess.getLocale().toString());
        sessionInfoForUsertracking.put("authprovider", authProvider);
        sessionInfoForUsertracking.put("iswebdav", String.valueOf(sinfo.isWebDAV()));
        usess.getIdentityEnvironment().setAttributes(sessionInfoForUsertracking);

    }

    /**
     * Set the roles (admin, author, guest)
     * 
     * @param identity
     * @param usess
     */
    private static void setRolesFor(final Identity identity, final UserSession usess) {
        final Roles roles = getBaseSecurity().getRoles(identity);
        usess.setRoles(roles);
    }

    public static void setLoginBlocked(final boolean newLoginBlocked) {
        loginBlocked = newLoginBlocked;
    }

    public static boolean isLoginBlocked() {
        return loginBlocked;
    }

    public static void setRejectDMZRequests(final boolean newRejectDMZRequests) {
        rejectDMZRequests = newRejectDMZRequests;
    }

    public static boolean isRejectDMZRequests() {
        return rejectDMZRequests;
    }

    public static void setMaxSessions(final int maxSession) {
        maxSessions = maxSession;
    }

    public static int getMaxSessions() {
        return maxSessions;
    }

    // TODO: 16.06.2011/cg: Should not be static, but the caller method is static
    private static UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
