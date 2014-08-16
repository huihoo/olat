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
package org.olat.presentation.instantmessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.connectors.instantmessaging.InstantMessagingSessionItems;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.instantmessaging.ClientHelper;
import org.olat.lms.instantmessaging.ConnectedUsersListEntry;
import org.olat.lms.instantmessaging.ImPreferences;
import org.olat.lms.instantmessaging.ImPrefsManager;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.session.UserSession;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * TODO: guido Class Description for ConnectedUsersLocal
 * <P>
 * Initial Date: 06.08.2008 <br>
 * 
 * @author guido
 */
public class ConnectedUsersLocal implements InstantMessagingSessionItems {

    private static final Logger log = LoggerHelper.getLogger();

    private CacheWrapper sessionItemsCache;
    private final ImPrefsManager imPrefsManager;
    @Autowired
    private UserService userService;

    /**
     * [spring managed]
     * 
     * @param imPrefsManager
     */
    protected ConnectedUsersLocal(final ImPrefsManager imPrefsManager) {
        this.imPrefsManager = imPrefsManager;
    }

    /**
	 */
    @Override
    public List<ConnectedUsersListEntry> getConnectedUsers(final Identity currentUser) {
        String username;
        if (currentUser != null) {
            username = currentUser.getName();
        } else {
            username = "";
        }
        /**
         * create a cache for the entries as looping over a few hundred entries need too much time. Every node has its own cache and therefore no need to inform each
         * other o_clusterOK by guido
         */
        if (sessionItemsCache == null) {
            synchronized (this) {
                sessionItemsCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "items");
            }
        }

        final List<ConnectedUsersListEntry> entries = new ArrayList<ConnectedUsersListEntry>();
        final Map<String, Long> lastActivity = new HashMap<String, Long>();
        final Set<String> usernames = InstantMessagingModule.getAdapter().getUsernamesFromConnectedUsers();
        final List<UserSession> authSessions = new ArrayList(UserSession.getAuthenticatedUserSessions());

        for (final Iterator<UserSession> iter = authSessions.iterator(); iter.hasNext();) {
            final UserSession userSession = iter.next();
            long lastAccTime = 0;
            try {
                lastAccTime = userSession.getSessionInfo().getSession().getLastAccessedTime();
                lastActivity.put(userSession.getIdentity().getName(), Long.valueOf(lastAccTime));
            } catch (final RuntimeException e) {
                // getAuthenticatedUserSessions delivers sessions that are
                // sometimes already invalid.
                log.warn("Tried to get LastAccessTime from session that became in the meantime invalid", null);
            }

        }

        for (final Iterator<String> iter = usernames.iterator(); iter.hasNext();) {
            final String olatusername = iter.next();

            ConnectedUsersListEntry entry = (ConnectedUsersListEntry) sessionItemsCache.get(olatusername);
            if (entry != null && !olatusername.equals(username)) {
                entries.add(entry);
                if (log.isDebugEnabled()) {
                    log.debug("loading item from cache: " + olatusername);
                }

            } else {
                // item not in cache
                Identity identity = UserSession.getSignedOnIdentity(olatusername);
                if (identity != null) {
                    try {
                        final ImPreferences imPrefs = imPrefsManager.loadOrCreatePropertiesFor(identity);
                        if ((imPrefs != null)) {
                            final ClientHelper clientHelper = new ClientHelper(olatusername, null, null, null);
                            entry = new ConnectedUsersListEntry(olatusername, identity.getUser().getPreferences().getLanguage());
                            entry.setName(userService.getUserProperty(identity.getUser(), UserConstants.LASTNAME));
                            entry.setPrename(userService.getUserProperty(identity.getUser(), UserConstants.FIRSTNAME));
                            entry.setShowAwarenessMessage(imPrefs.isAwarenessVisible());
                            entry.setShowOnlineTime(imPrefs.isOnlineTimeVisible());
                            entry.setAwarenessMessage(clientHelper.getStatusMsg());
                            entry.setInstantMessagingStatus(clientHelper.getStatus());
                            entry.setLastActivity(lastActivity.get(olatusername));
                            entry.setOnlineTime(clientHelper.getOnlineTime());
                            entry.setJabberId(clientHelper.getJid());
                            entry.setVisibleToOthers(imPrefs.isVisibleToOthers());
                            entries.add(entry);

                            // put in cache
                            sessionItemsCache.put(olatusername, entry);
                        }
                    } catch (final AssertException ex) {
                        log.warn("Can not load IM-Prefs for identity=" + identity, ex);
                    }
                }
            }
        }// end loop
        return entries;
    }

}
