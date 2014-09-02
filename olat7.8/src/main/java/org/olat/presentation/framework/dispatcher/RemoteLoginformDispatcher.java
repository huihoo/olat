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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.presentation.framework.dispatcher;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mediaresource.ServletUtil;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Window;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.exception.MsgFactory;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.framework.core.render.URLBuilder;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.presentation.security.authentication.OLATAuthenticationController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> Login dispatcher that takes a username and password provided by some HTTP parameters to authenticat the user. In case of success the user will be
 * redirected to the home, in case of failure, the OLAT loginscreen appears and an error message is triggered.
 * <p>
 * The external form must submit the data using the HTTP POST method. GET is not supported to prevent logging of logfiles into apache logfiles or caching in proxy
 * servers.
 * <p>
 * The following parameters are used to transport the data:
 * <ul>
 * <li>username: username</li>
 * <li>password: pwd</li>
 * </ul>
 * <p>
 * The following example will open OLAT in a new window without browser toolbars: <br>
 * <form method="post" action="http://office.frentix.com:8080/branch/remotelogin/"
 * onsubmit="var olat = window.open('','OLAT','location=no,menubar=no,resizable=yes,toolbar=no,statusbar=no,scrollbars=yes')" olat.focus();" target="OLAT"> <input
 * type="text" name="username"> <input type="password" name="pwd"> <button>login</button> </form>
 * <p>
 * An optional parameter named redirect allow a redirect using a businesspath or a jumping url: <input type="hidden" name="redirect"
 * value="/olat/url/RepositoryEntry/917504/CourseNode/81254724902921"/>
 * <p>
 * Initial Date: 05.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class RemoteLoginformDispatcher implements Dispatcher {

    private static final String METHOD_POST = "POST";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "pwd";
    private static final Logger log = LoggerHelper.getLogger();

    protected RemoteLoginformDispatcher() {
    }

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Tries to login the user with the parameters from the POST request and redirects to the home screen in case of success. In case of failure, redirects to the login
     * screen.
     * 
     * @param request
     * @param response
     * @param uriPrefix
     */
    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, final String uriPrefix) {
        UserRequest ureq = null;

        try {
            ureq = new UserRequest(uriPrefix, request, response);

            if (!request.getMethod().equals(METHOD_POST)) {
                log.warn("Wrong HTTP method, only POST allowed, but current method::" + request.getMethod());
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            }
            final String userName = ureq.getParameter(PARAM_USERNAME);
            if (!StringHelper.containsNonWhitespace(userName)) {
                log.warn("Missing username parameter, use '" + PARAM_USERNAME + "' to submit the login name");
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            }
            final String pwd = ureq.getParameter(PARAM_PASSWORD);
            if (!StringHelper.containsNonWhitespace(pwd)) {
                log.warn("Missing password parameter, use '" + PARAM_PASSWORD + "' to submit the password");
                DispatcherAction.redirectToDefaultDispatcher(response);
                return;
            }

            // Authenticate user
            final Identity identity = authenticationService.authenticate(userName, pwd, Provider.OLAT);
            if (identity == null) {
                log.info("Could not authenticate user '" + userName + "', wrong password or user name");
                // redirect to OLAT loginscreen, add error parameter so that the loginform can mark itself as errorfull
                final String loginUrl = WebappHelper.getServletContextPath() + DispatcherAction.getPathDefault() + "?" + OLATAuthenticationController.PARAM_LOGINERROR
                        + "=true";
                DispatcherAction.redirectTo(response, loginUrl);
                return;
            }

            // Login user, set up everything
            final int loginStatus = AuthHelper.doLogin(identity, AUTHENTICATION_PROVIDER_OLAT, ureq);
            if (loginStatus == AuthHelper.LOGIN_OK) {
                // redirect to authenticated environment

                final String origUri = request.getRequestURI();
                String restPart = origUri.substring(uriPrefix.length());
                if (request.getParameter("redirect") != null) {
                    // redirect parameter like: /olat/url/RepositoryEntry/917504/CourseNode/81254724902921
                    final String redirect = request.getParameter("redirect");
                    DispatcherAction.redirectTo(response, redirect);
                } else if (StringHelper.containsNonWhitespace(restPart)) {
                    // redirect like: http://www.frentix.com/olat/remotelogin/RepositoryEntry/917504/CourseNode/81254724902921
                    try {
                        restPart = URLDecoder.decode(restPart, "UTF8");
                    } catch (final UnsupportedEncodingException e) {
                        log.error("Unsupported encoding", e);
                    }

                    final String[] split = restPart.split("/");
                    assert (split.length % 2 == 0);
                    String businessPath = "";
                    for (int i = 0; i < split.length; i = i + 2) {
                        String key = split[i];
                        if (key != null && key.startsWith("path=")) {
                            key = key.replace("~~", "/");
                        }
                        final String value = split[i + 1];
                        businessPath += "[" + key + ":" + value + "]";
                    }

                    final UserSession usess = UserSession.getUserSession(request);
                    usess.putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_BUSINESSPATH, businessPath);
                    final String url = getRedirectToURL(usess);
                    DispatcherAction.redirectTo(response, url);
                } else {
                    // redirect
                    ServletUtil.serveResource(request, response, ureq.getDispatchResult().getResultingMediaResource());
                }
            } else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                DispatcherAction.redirectToServiceNotAvailable(response);
            } else {
                // error, redirect to login screen
                DispatcherAction.redirectToDefaultDispatcher(response);
            }

        } catch (final Throwable th) {
            try {
                final ChiefController msgcc = MsgFactory.createMessageChiefController(ureq, th);
                // the controller's window must be failsafe also
                msgcc.getWindow().dispatchRequest(ureq, true);
                // do not dispatch (render only), since this is a new Window created as
                // a result of another window's click.
            } catch (final Throwable t) {
                log.error("Sorry, can't handle this remote login request....", t);
            }
        }
    }

    private String getRedirectToURL(final UserSession usess) {
        final ChiefController cc = (ChiefController) Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");
        final Window w = cc.getWindow();

        final URLBuilder ubu = new URLBuilder("", w.getInstanceId(), String.valueOf(w.getTimestamp()), null);
        final StringOutput sout = new StringOutput(30);
        ubu.buildURI(sout, null, null);

        return WebappHelper.getServletContextPath() + DispatcherAction.PATH_AUTHENTICATED + sout.toString();
    }
}
