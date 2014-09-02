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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.lms.admin.AdminModule;
import org.olat.lms.commons.mediaresource.ServletUtil;

/**
 * This dispatcher acts as proxy to receive the message.
 * 
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 * @author christian guretzki
 */
public class AdminModuleDispatcher implements Dispatcher {

    private final static String PARAMETER_CMD = "cmd";
    private final static String PARAMETER_MSG = "msg";
    private final static String PARAMETER_MAX_MESSAGE = "maxsessions";
    private final static String PARAMETER_NBR_SESSIONS = "nbrsessions";
    private final static String PARAMETER_SESSIONTIMEOUT = "sec";

    private final static String CMD_SET_MAINTENANCE_MESSAGE = "setmaintenancemessage";
    private final static String CMD_SET_LOGIN_BLOCKED = "setloginblocked";
    private final static String CMD_SET_LOGIN_NOT_BLOCKED = "setloginnotblocked";
    private final static String CMD_SET_MAX_SESSIONS = "setmaxsessions";
    private final static String CMD_INVALIDATE_ALL_SESSIONS = "invalidateallsessions";
    private final static String CMD_INVALIDATE_OLDEST_SESSIONS = "invalidateoldestsessions";
    private final static String CMD_SET_SESSIONTIMEOUT = "sessiontimeout";

    /**
	 */

    protected AdminModuleDispatcher() {
    }

    @Override
    public void execute(final HttpServletRequest request, final HttpServletResponse response, final String uriPrefix) {
        final String cmd = request.getParameter(PARAMETER_CMD);
        if (cmd.equalsIgnoreCase(CMD_SET_MAINTENANCE_MESSAGE)) {
            handleSetMaintenanceMessage(request, response);
        } else {
            if (AdminModule.checkSessionAdminToken(request, response)) {
                handleSessionsCommand(request, response, cmd);
            } else {
                DispatcherAction.sendForbidden(request.getPathInfo(), response);
            }
        }
    }

    /**
     * Handle session-administration commands (setLoginBlocked, setLoginNotBlocked, setMaxSession, invalidateAllSessions, ïnvalidateOldestSessions).
     */
    private void handleSessionsCommand(final HttpServletRequest request, final HttpServletResponse response, final String cmd) {
        if (cmd.equalsIgnoreCase(CMD_SET_LOGIN_BLOCKED)) {
            AdminModule.setLoginBlocked(true);
            ServletUtil.serveStringResource(request, response, "Ok, login blocked");
        } else if (cmd.equalsIgnoreCase(CMD_SET_LOGIN_NOT_BLOCKED)) {
            AdminModule.setLoginBlocked(false);
            ServletUtil.serveStringResource(request, response, "Ok, login no more blocked");
        } else if (cmd.equalsIgnoreCase(CMD_SET_MAX_SESSIONS)) {
            handleSetMaxSessions(request, response);
        } else if (cmd.equalsIgnoreCase(CMD_INVALIDATE_ALL_SESSIONS)) {
            AdminModule.invalidateAllSessions();
            ServletUtil.serveStringResource(request, response, "Ok, Invalidated all sessions");
        } else if (cmd.equalsIgnoreCase(CMD_INVALIDATE_OLDEST_SESSIONS)) {
            handleInvidateOldestSessions(request, response);
        } else if (cmd.equalsIgnoreCase(CMD_SET_SESSIONTIMEOUT)) {
            handleSetSessiontimeout(request, response);
        } else {
            ServletUtil.serveStringResource(request, response, "NOT OK, unknown command=" + cmd);
        }
    }

    /**
     * Handle setMaxSessions command, extract parameter maxsessions form request and call method on AdminModule.
     * 
     * @param request
     * @param response
     */
    private void handleSetMaxSessions(final HttpServletRequest request, final HttpServletResponse response) {
        final String maxSessionsString = request.getParameter(PARAMETER_MAX_MESSAGE);
        if (maxSessionsString == null || maxSessionsString.equals("")) {
            ServletUtil.serveStringResource(request, response, "NOT_OK, missing parameter " + PARAMETER_MAX_MESSAGE);
        } else {
            try {
                final int maxSessions = Integer.parseInt(maxSessionsString);
                AdminModule.setMaxSessions(maxSessions);
                ServletUtil.serveStringResource(request, response, "Ok, max-session=" + maxSessions);
            } catch (final NumberFormatException nbrException) {
                ServletUtil.serveStringResource(request, response, "NOT_OK, parameter " + PARAMETER_MAX_MESSAGE + " must be a number");
            }
        }
    }

    private void handleSetSessiontimeout(final HttpServletRequest request, final HttpServletResponse response) {
        final String paramStr = request.getParameter(PARAMETER_SESSIONTIMEOUT);
        if (paramStr == null || paramStr.equals("")) {
            ServletUtil.serveStringResource(request, response, "NOT_OK, missing parameter " + PARAMETER_SESSIONTIMEOUT);
        } else {
            try {
                final int sessionTimeout = Integer.parseInt(paramStr);
                AdminModule.setSessionTimeout(sessionTimeout);
                ServletUtil.serveStringResource(request, response, "Ok, sessiontimeout=" + sessionTimeout);
            } catch (final NumberFormatException nbrException) {
                ServletUtil.serveStringResource(request, response, "NOT_OK, parameter " + PARAMETER_SESSIONTIMEOUT + " must be a number");
            }
        }
    }

    /**
     * Handle invalidateOldestSessions command, extract parameter nbrsessions form request and call method on AdminModule.
     * 
     * @param request
     * @param response
     */
    private void handleInvidateOldestSessions(final HttpServletRequest request, final HttpServletResponse response) {
        final String nbrSessionsString = request.getParameter(PARAMETER_NBR_SESSIONS);
        if (nbrSessionsString == null || nbrSessionsString.equals("")) {
            ServletUtil.serveStringResource(request, response, "NOT_OK, missing parameter " + PARAMETER_NBR_SESSIONS);
        } else {
            try {
                final int nbrSessions = Integer.parseInt(nbrSessionsString);
                AdminModule.invalidateOldestSessions(nbrSessions);
                ServletUtil.serveStringResource(request, response, "Ok, Invalidated oldest sessions, nbrSessions=" + nbrSessions);
            } catch (final NumberFormatException nbrException) {
                ServletUtil.serveStringResource(request, response, "NOT_OK, parameter " + PARAMETER_NBR_SESSIONS + " must be a number");
            }
        }
    }

    /**
     * Handle setMaintenanceMessage command, extract parameter msg form request and call method on AdminModule.
     * 
     * @param request
     * @param response
     */
    private void handleSetMaintenanceMessage(final HttpServletRequest request, final HttpServletResponse response) {
        if (AdminModule.checkMaintenanceMessageToken(request, response)) {
            final String message = request.getParameter(PARAMETER_MSG);
            AdminModule.setMaintenanceMessage(message);
            ServletUtil.serveStringResource(request, response, "Ok, new maintenanceMessage is::" + message);
        } else {
            DispatcherAction.sendForbidden(request.getPathInfo(), response);
        }
    }

}
