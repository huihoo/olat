/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.lms.instantmessaging;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Presence;
import org.olat.lms.commons.scheduler.JobWithDB;
import org.olat.presentation.commons.session.UserSession;
import org.olat.system.logging.log4j.LoggerHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br />
 * changes the IM presence to away if a user has not clicked in the olat gui for a certain time
 * <P>
 * Initial Date: 18.05.2007 <br />
 * 
 * @author guido
 */
public class ChangeInstantMessagingStatusJob extends JobWithDB {

    private static final Logger log = LoggerHelper.getLogger();
    /**
     * TODO:gs:a make this properties writable by jmx or by setting it via admin gui that we can kick out users to free resources if needed
     */
    private long idleWaitTime;

    @Override
    public void executeWithDB(final JobExecutionContext arg0) throws JobExecutionException {
        if (!InstantMessagingModule.isEnabled()) {
            log.debug("ChangeInstantMessagingStatusJob ignored since InstanceMessaging is disabled.");
            return;
        }

        try {
            log.debug("ChangeInstantMessagingStatusJob started...");
            final long timeNow = System.currentTimeMillis();

            for (final UserSession session : UserSession.getAuthenticatedUserSessions()) {
                long lastAccessTime = 0;
                String username = null;
                InstantMessagingClient client = null;
                boolean isWebDav = false;
                try {
                    lastAccessTime = session.getSessionInfo().getLastClickTime();
                    username = session.getIdentity().getName();
                    isWebDav = session.getSessionInfo().isWebDAV();
                } catch (final Exception e) {
                    log.info("Tried to get LastAccessTime from session that became invalid in the meantime" + e.toString());
                }

                // leave webdav sessions untouched
                if (isWebDav) {
                    continue;
                }

                // TODO: that presence change stuff should also be moved to the
                // IMManager...
                // invalidation triggers dispose of controller chain and closes
                // IM and removes IM client

                // avoid reconnection of dead or duplicate sessions
                final ClientManager mgr = InstantMessagingModule.getAdapter().getClientManager();
                if (username != null && mgr.hasActiveInstantMessagingClient(username)) {
                    client = mgr.getInstantMessagingClient(username);
                }
                if (log.isDebugEnabled()) {
                    if (client != null) {
                        log.debug("Fetched im client via mangager. Connections status is - connected=" + client.isConnected() + " for user: " + client.getUsername());
                    } else {
                        log.debug("Could not fetch IM client for user: " + username);
                    }
                }

                if (session != null) {
                    if ((timeNow - lastAccessTime) > idleWaitTime) {
                        /**
                         * user is back on track send presence message available to inform
                         */
                        if ((client != null && client.isConnected())
                                && (client.getPresenceMode() == Presence.Mode.available || client.getPresenceMode() == Presence.Mode.chat)) {
                            client.sendPresenceAutoStatusIdle();
                            // inform the GUI
                            InstantMessagingModule.getAdapter().getClientManager().sendPresenceEvent(Presence.Type.available, username);
                            if (log.isDebugEnabled()) {
                                log.debug("change presence for user " + client.getUsername() + " to away.");
                            }
                        }
                    } else {
                        /**
                         * uses makes a brake last access was more than five minutes ago so set instant messaging presence to away
                         */
                        if ((client != null && client.isConnected()) && (client.getPresenceMode() == Presence.Mode.away || client.getPresenceMode() == Presence.Mode.xa)) {
                            client.sendPresence(Presence.Type.available, null, 0, Presence.Mode.valueOf(client.getRecentPresenceStatusMode()));
                            // inform the GUI
                            InstantMessagingModule.getAdapter().getClientManager().sendPresenceEvent(Presence.Type.available, username);
                            if (log.isDebugEnabled()) {
                                log.debug("change presence for user " + client.getUsername() + " back to recent presence.");
                            }
                        }
                        // check if we can switch user back to available mode
                    }
                }
            }
        } catch (final Throwable th) {
            log.error("ChangeInstantMessagingStatusJob failed.", th);
        }
    }

    /**
     * @@org.springframework.jmx.export.metadata.ManagedAttribute (description="Set the time a users instant messaging status gets automatically switched into idle
     *                                                            status")
     * @param idleWaitTime
     */
    public void setIdleWaitTime(final long idleWaitTime) {
        this.idleWaitTime = idleWaitTime;
    }

}
