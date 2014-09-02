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
package org.olat.presentation.commons.session;

import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Initial Date: 28.05.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class AutoLogOutJob extends QuartzJobBean {

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        log.debug("AutoLogOutJob started...");

        final Set<UserSession> authenticatedUserSessions = UserSession.getAuthenticatedUserSessions();
        if (!authenticatedUserSessions.isEmpty()) {
            final long timeNow = System.currentTimeMillis();

            if (log.isDebugEnabled()) {
                final String lineBreak = System.getProperty("line.separator");
                final StringBuilder sessionOverview = new StringBuilder();
                for (final UserSession session : authenticatedUserSessions) {
                    if (session.getSessionInfo() != null) {
                        try {
                            final long lastAccessTimeAgo = (timeNow - session.getSessionInfo().getSession().getLastAccessedTime()) / 1000;
                            final long lastClickTimeAgo = (timeNow - session.getSessionInfo().getLastClickTime()) / 1000;
                            sessionOverview.append("Session: ").append(session.getSessionInfo().getSession().getId()).append(", lastAccessTime: ")
                                    .append(lastAccessTimeAgo).append("s ago, lastClickTime: ").append(lastClickTimeAgo).append("s ago.").append(lineBreak);
                        } catch (IllegalStateException ex) {
                            // occurs on HttpSession.getLastAccessedTime() if session already invalidated
                            sessionOverview.append("Session: ").append(session.getSessionInfo().getSession().getId()).append(" [").append(ex.getMessage()).append("]")
                                    .append(lineBreak);
                        }
                    }
                }
                log.debug("Session overview:" + lineBreak + sessionOverview);
            }

            int sessionTimeoutSec = UserSession.getGlobalSessionTimeout();
            long sessionTimeoutMillis = sessionTimeoutSec * 1000;
            log.debug("Invalidate all UserSessions that have been inactive for more than " + sessionTimeoutSec + "s.");
            for (final UserSession session : UserSession.getAuthenticatedUserSessions()) {
                if (session.getSessionInfo() == null) {
                    continue;
                }

                // leave webdav sessions untouched
                if (session.getSessionInfo().isWebDAV()) {
                    continue;
                }

                long lastAccessTime = session.getSessionInfo().getLastClickTime();

                if ((timeNow - lastAccessTime) > sessionTimeoutMillis) {
                    // brasato:::: since tomcat doesn't do this in its normal
                    // session invalidation process,
                    // (since polling), we must do it manually.
                    // brasato:: alternative: instead of a job, generate a timer
                    // which rechecks if clicked within 5 mins ??

                    // TODO: also logout administrator sessions?

                    final SessionInfo sessionInfo = session.getSessionInfo();
                    final HttpSession httpSession = sessionInfo.getSession();
                    if (httpSession != null) {
                        log.debug("HTTP session invalidated [id=" + httpSession.getId() + ", lastAccessTime=" + new Date(lastAccessTime) + ", sessionTimeout="
                                + sessionTimeoutSec + "s].");
                        try {
                            httpSession.invalidate();
                        } catch (final IllegalStateException ise) {
                            // thrown if session already invalidated
                        }
                    }
                }
            }
        }
    }
}
