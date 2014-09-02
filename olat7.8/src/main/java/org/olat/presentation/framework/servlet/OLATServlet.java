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

package org.olat.presentation.framework.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLoggerInstaller;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.util.LogRequestInfoFactory;
import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.dispatcher.Dispatcher;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.system.event.FrameworkStartupEventChannel;
import org.olat.system.logging.Tracing;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.logging.threadlog.RequestBasedLogLevelManager;
import org.olat.system.logging.threadlog.UserBasedLogLevelManager;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Apr 28, 2004
 * 
 * @author Mike Stock Comment:
 */
public class OLATServlet extends HttpServlet {

    private static final long serialVersionUID = 4146352020009404834L;
    private static final Logger log = LoggerHelper.getLogger();

    private Dispatcher dispatcher;
    private RequestBasedLogLevelManager requestBasedLogLevelManager;

    /**
	 */
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
        if (requestBasedLogLevelManager == null) {
            log.info("init: RequestBasedLogLevelManager is not configured on this system.");
        } else {
            log.info("init: RequestBasedLogLevelManager is configured and will be used.");
        }
        if (UserBasedLogLevelManager.getInstance() == null) {
            log.info("init: UserBasedLogLevelManager is not configured on this system.");
        } else {
            log.info("init: UserBasedLogLevelManager is configured and will be used.");
        }

        // the servlet.init method gets called after the spring stuff and all the stuff in web.xml is done
        log.info("Framework has started, sending event to listeners of FrameworkStartupEventChannel");
        FrameworkStartupEventChannel.fireEvent();
        log.info("FrameworkStartupEvent processed by alle listeners. Webapp has started.");
    }

    /**
	 */
    @Override
    public void destroy() {
        log.info("*** Destroying OLAT servlet.");
        log.info("*** Shutting down the logging system - do not use logger after this point!");
        LogManager.shutdown();
    }

    /**
     * Called when the HTTP request method is GET. This method just calls the doPost() method.
     * 
     * @param request
     *            The HTTP request
     * @param response
     *            The HTTP response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Called when the HTTP request method is POST. This method provides the main control logic.
     * 
     * @param request
     *            The HTTP request
     * @param response
     *            The HTTP response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        GUIInterna.begin(request);
        // initialize tracing with request, this allows debugging information as IP, User-Agent.
        Tracing.setLogRequestInfo(LogRequestInfoFactory.createFrom(request));
        I18nManager.attachI18nInfoToThread(request);
        ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);

        try {
            if (requestBasedLogLevelManager != null) {
                requestBasedLogLevelManager.activateRequestBasedLogLevel(request);
            }
            if (dispatcher == null) {
                dispatcher = CoreSpringFactory.getBean(DispatcherAction.class);
            }
            dispatcher.execute(request, response, null);
        } finally {
            if (requestBasedLogLevelManager != null) {
                requestBasedLogLevelManager.deactivateRequestBasedLogLevel();
            }
            ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
            I18nManager.remove18nInfoFromThread();
            Tracing.clearLogRequestInfo();
            GUIInterna.end(request);
            DBFactory.getInstanceForClosing().cleanUpSession();
        }
    }

}
