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

package org.olat.lms.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.User;
import org.olat.lms.instantmessaging.ChangeInstantMessagingStatusJob;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.framework.layout.fullWebApp.util.GlobalStickyMessage;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.presentation.user.administration.UserAdminContextEntryControllerCreator;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<BR>
 * The administration module takes care of loading and unloading administration specific configuration.
 * <P>
 * Initial Date: Apr 13, 2005
 * 
 * @author gnaegi
 */
public class AdminModule extends AbstractOLATModule {

    private static final Logger log = LoggerHelper.getLogger();

    /** Category for system properties **/
    public static String SYSTEM_PROPERTY_CATEGORY = "_o3_";
    private static final String PROPERTY_MAINTENANCE_MESSAGE = "maintenanceMessageToken";
    private static final String PROPERTY_SESSION_ADMINISTRATION = "sessionAdministrationToken";
    private static final String CONFIG_ADMIN_MAX_SESSION = "maxNumberOfSessions";
    private final PropertyManager propertyManager;

    /**
     * [used by spring]
     */
    protected AdminModule(final PropertyManager propertyManager) {
        super();
        this.propertyManager = propertyManager;
    }

    /**
     * Check if system property for maintenance message exists, create one if it doesn't This generated token is used by the remote http maintenance message setting
     * mechanism, see method below
     * 
     * @param tokenPropertyName
     */
    private void initializeSystemTokenProperty(final String tokenPropertyName) {
        PropertyImpl p = propertyManager.findProperty(null, null, null, SYSTEM_PROPERTY_CATEGORY, tokenPropertyName);
        if (p == null) {
            final String token = RandomStringUtils.randomAlphanumeric(8);
            p = propertyManager.createPropertyInstance(null, null, null, SYSTEM_PROPERTY_CATEGORY, tokenPropertyName, null, null, token, null);
            propertyManager.saveProperty(p);
        }
    }

    /**
     * Sets the new maintenance message based on a http parameter. The request must use a valid token. The token can be looked up in the properties table. The maintenance
     * message itself is managed by the OLATContext from the brasato core
     * 
     * @param message
     */
    public static void setMaintenanceMessage(final String message) {
        GlobalStickyMessage.setGlobalStickyMessage(message, true);
    }

    public static boolean checkMaintenanceMessageToken(final HttpServletRequest request, final HttpServletResponse response) {
        return checkToken(request, PROPERTY_MAINTENANCE_MESSAGE);
    }

    public static boolean checkSessionAdminToken(final HttpServletRequest request, final HttpServletResponse response) {
        return checkToken(request, PROPERTY_SESSION_ADMINISTRATION);
    }

    private static boolean checkToken(final HttpServletRequest request, final String tokenPropertyName) {
        final String submittedToken = request.getParameter("token");
        if (submittedToken == null) {
            log.info("Audit:Trying to set maintenance message without using a token. Remote address::" + request.getRemoteAddr());
            return false;
        }
        // get token and compate
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, null, AdminModule.SYSTEM_PROPERTY_CATEGORY, tokenPropertyName);
        final String token = (p == null ? "" : p.getStringValue());
        if (token.matches(submittedToken)) { // limit access to token
            return true;
        } else {
            log.info("Audit:Trying to set maintenance message using a wrong token. Remote address::" + request.getRemoteAddr());
            return false;
        }
    }

    /**
     * Does not allow any further login except administrator-logins.
     * 
     * @param newLoginBlocked
     */
    public static void setLoginBlocked(final boolean newLoginBlocked) {
        log.info("Audit:Session administration: Set login-blocked=" + newLoginBlocked);
        AuthHelper.setLoginBlocked(newLoginBlocked);
    }

    /**
     * Check if login is blocked
     * 
     * @return true = login is blocked
     */
    public static boolean isLoginBlocked() {
        return AuthHelper.isLoginBlocked();
    }

    /**
     * Set the rejectDMZRequests flag - if true this will reject all requests to dmz to other nodes
     * 
     * @param rejectDMZRequests
     */
    public static void setRejectDMZRequests(final boolean rejectDMZRequests) {
        log.info("Audit:Session administration: Set rejectDMZRequests=" + rejectDMZRequests);
        AuthHelper.setRejectDMZRequests(rejectDMZRequests);
    }

    /**
     * Check if requests to DMZ are rejected resulting in clients to go to another node
     * 
     * @return true = reject all requests to dmz (to other nodes)
     */
    public static boolean isRejectDMZRequests() {
        return AuthHelper.isRejectDMZRequests();
    }

    /**
     * Set limit for session. The login-process check this number and allows only this number of sessions. 0 = unlimited number of sessions
     * 
     * @param maxSession
     */
    public static void setMaxSessions(final int maxSession) {
        log.info("Audit:Session administration: Set maxSession=" + maxSession);
        AuthHelper.setMaxSessions(maxSession);
    }

    /**
     * Invalidated all session except administrator-sessions.
     * 
     * @return Number of invalidated sessions
     */
    public static int invalidateAllSessions() {
        log.info("Audit:Session administration: Invalidate all sessions.");
        return UserSession.invalidateAllSessions();
    }

    /**
     * Invalidate a number of oldest (last-click time) sessions.
     * 
     * @param nbrSessions
     * @return Number of invalidated sessions
     */
    public static int invalidateOldestSessions(final int nbrSessions) {
        log.info("Audit:Session administration: Invalidate oldest sessions Nbr-Sessions=" + nbrSessions);
        return UserSession.invalidateOldestSessions(nbrSessions);
    }

    /**
     * @return Current session timeout in sec.
     */
    public static int getSessionTimeout() {
        return UserSession.getGlobalSessionTimeout();
    }

    /**
     * Set global session timeout in sec.
     * 
     * @param sessionTimeout
     */
    public static void setSessionTimeout(final int sessionTimeout) {
        log.info("Audit:Session administration: Set session-timeout=" + sessionTimeout);
        UserSession.setGlobalSessionTimeout(sessionTimeout);
    }

    /**
     * @return Current session-limit.
     */
    public static int getMaxSessions() {
        return AuthHelper.getMaxSessions();
    }

    /**
     * Enable hibernate-statistics (for JMX interface).
     */
    public void enableHibernateStatistics(final boolean enableStatistics) {
        if (enableStatistics) {
            // clear statistics when enable it
            DBFactory.getInstance().getStatistics().clear();
        }
        DBFactory.getInstance().getStatistics().setStatisticsEnabled(enableStatistics);
    }

    @Override
    public void initialize() {
        initializeSystemTokenProperty(PROPERTY_MAINTENANCE_MESSAGE);
        initializeSystemTokenProperty(PROPERTY_SESSION_ADMINISTRATION);

        // Add controller factory extension point to launch groups
        NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(), new UserAdminContextEntryControllerCreator());
        NewControllerFactory.getInstance().addContextEntryControllerCreator("NewIdentityCreated", new UserAdminContextEntryControllerCreator());
    }

    @Override
    protected void initDefaultProperties() {
        final int maxNumberOfSessions = getIntConfigParameter(CONFIG_ADMIN_MAX_SESSION, 0);
        AuthHelper.setMaxSessions(maxNumberOfSessions);
    }

    @Override
    protected void initFromChangedProperties() {
        // TODO Auto-generated method stub

    }

}
