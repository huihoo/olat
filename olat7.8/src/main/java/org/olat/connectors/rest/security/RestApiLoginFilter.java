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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.connectors.rest.security;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.olat.connectors.rest.RestModule;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLoggerInstaller;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.util.LogRequestInfoFactory;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.Tracing;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Filter which protects the REST Api.
 * <P>
 * Initial Date: 7 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestApiLoginFilter implements Filter {

    private static final Logger log = LoggerHelper.getLogger();

    private static List<String> openUrls;
    private static String LOGIN_URL;

    /**
     * The survive time of the session used by token based authentication. For every request is a new session created.
     */
    private static int TOKEN_BASED_SESSION_TIMEOUT = 120;

    @Override
    public void init(final FilterConfig filterConfig) {
        //
    }

    @Override
    public void destroy() {
        //
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws ServletException {

        if (request instanceof HttpServletRequest) {
            try {
                final HttpServletRequest httpRequest = (HttpServletRequest) request;
                final HttpServletResponse httpResponse = (HttpServletResponse) response;

                final RestModule restModule = (RestModule) CoreSpringFactory.getBean("restModule");
                if (!restModule.isEnabled()) {
                    httpResponse.sendError(403);
                    return;
                }

                // initialize tracing with request, this allows debugging information as IP, User-Agent.
                Tracing.setLogRequestInfo(LogRequestInfoFactory.createFrom(httpRequest));
                I18nManager.attachI18nInfoToThread(httpRequest);
                ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(httpRequest);

                final UserSession uress = UserSession.getUserSessionIfAlreadySet(httpRequest);
                if (uress != null && uress.isAuthenticated()) {
                    // use the available session
                    followSession(httpRequest, httpResponse, chain);
                } else {
                    final String requestURI = httpRequest.getRequestURI();
                    if (requestURI.startsWith(getLoginUrl())) {
                        followForAuthentication(requestURI, uress, httpRequest, httpResponse, chain);
                    } else if (isRequestURIInOpenSpace(requestURI)) {
                        followWithoutAuthentication(httpRequest, httpResponse, chain);
                    } else {
                        final String token = httpRequest.getHeader(RestSecurityHelper.SEC_TOKEN);
                        final RestSecurityBean securityBean = (RestSecurityBean) CoreSpringFactory.getBean(RestSecurityBean.class);
                        if (securityBean.isTokenRegistrated(token)) {
                            followToken(token, httpRequest, httpResponse, chain);
                        } else {
                            httpResponse.sendError(401);
                        }
                    }
                }
            } catch (final Exception e) {
                log.error("", e);
            } finally {
                ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
                I18nManager.remove18nInfoFromThread();
                Tracing.clearLogRequestInfo();

                DBFactory.getInstance().commit();
                DBFactory.getInstance().closeSession();
            }
        } else {
            throw new ServletException("Only accept HTTP Request");
        }
    }

    private boolean isRequestURIInOpenSpace(final String requestURI) {
        for (final String openURI : getOpenURIs()) {
            if (requestURI.startsWith(openURI)) {
                return true;
            }
        }
        return false;
    }

    private void followForAuthentication(final String requestURI, UserSession uress, final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        // create a session for login without security check
        if (uress == null) {
            uress = UserSession.getUserSession(request);
        }
        UserRequest ureq = null;
        try {
            // upon creation URL is checked for
            ureq = new UserRequest(requestURI, request, response);
        } catch (final NumberFormatException nfe) {
            response.sendError(401);
            return;
        }

        request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
        chain.doFilter(request, response);
        if (ureq.getIdentity() == null) {
            // login is not successful
            response.sendError(401);
        }
    }

    private void followWithoutAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException,
            ServletException {
        final UserSession uress = UserSession.getUserSessionIfAlreadySet(request);
        if (uress != null && uress.isAuthenticated()) {
            // is authenticated by session cookie, follow its current session
            followSession(request, response, chain);
            return;
        }

        final String token = request.getHeader(RestSecurityHelper.SEC_TOKEN);
        final RestSecurityBean securityBean = (RestSecurityBean) CoreSpringFactory.getBean(RestSecurityBean.class);
        if (StringHelper.containsNonWhitespace(token) && securityBean.isTokenRegistrated(token)) {
            // is authenticated by token, follow its current token
            followToken(token, request, response, chain);
            return;
        }

        // no authentication, but no authentication needed, go further
        chain.doFilter(request, response);
    }

    private void followToken(final String token, final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException,
            ServletException {
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(TOKEN_BASED_SESSION_TIMEOUT);
        final UserSession uress = UserSession.getUserSession(session);
        if (uress != null) {
            UserRequest ureq = null;
            try {
                // upon creation URL is checked for
                final String requestURI = request.getRequestURI();
                ureq = new UserRequest(requestURI, request, response);
            } catch (final Exception e) {
                response.sendError(500);
                return;
            }

            request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
            final RestSecurityBean securityBean = (RestSecurityBean) CoreSpringFactory.getBean(RestSecurityBean.class);
            final Identity identity = securityBean.getIdentity(token);
            final int loginStatus = AuthHelper.doHeadlessLogin(identity, AUTHENTICATION_PROVIDER_OLAT, ureq);
            if (loginStatus == AuthHelper.LOGIN_OK) {
                response.setHeader(RestSecurityHelper.SEC_TOKEN, securityBean.renewToken(token));
                synchronized (uress) {
                    chain.doFilter(request, response);
                }
            } else {
                response.sendError(401);
            }
        } else {
            response.sendError(401);
        }
    }

    private void followSession(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final UserSession uress = UserSession.getUserSessionIfAlreadySet(request);
        if (uress != null && uress.isAuthenticated()) {
            UserRequest ureq = null;
            try {
                // upon creation URL is checked for
                final String requestURI = request.getRequestURI();
                ureq = new UserRequest(requestURI, request, response);
            } catch (final NumberFormatException nfe) {
                response.sendError(401);
                return;
            }
            request.setAttribute(RestSecurityHelper.SEC_USER_REQUEST, ureq);
            synchronized (uress) {
                chain.doFilter(request, response);
            }
        } else {
            response.sendError(401);
        }
    }

    private String getLoginUrl() {
        if (LOGIN_URL == null) {
            final String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
            LOGIN_URL = context + "/auth";
        }
        return LOGIN_URL;
    }

    private List<String> getOpenURIs() {
        if (openUrls == null) {
            final String context = (Settings.isJUnitTest() ? "/olat" : WebappHelper.getServletContextPath() + RestSecurityHelper.SUB_CONTEXT);
            openUrls = new ArrayList<String>();
            openUrls.add(context + "/i18n");
            openUrls.add(context + "/api");
            openUrls.add(context + "/ping");
            openUrls.add(context + "/application.wadl");
            openUrls.add(context + "/application.html");
            openUrls.add(context + "/wadl");
        }
        return openUrls;
    }
}
