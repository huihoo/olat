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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.taskexecutor.TaskExecutorService;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br />
 * Manager class for creation and manipulation of the instant messaging client for each user.
 * <P>
 * Initial Date: 14.10.2004
 * 
 * @author Guido Schnider
 */
public class ClientManagerImpl extends BasicManager implements ClientManager {

    private static final Logger log = LoggerHelper.getLogger();

    // each client gets stored in a map under the username
    // Cluster_OK as only one client per cluster (single session assumption)
    private final Map<String, InstantMessagingClient> clients = Collections.synchronizedMap(new HashMap<String, InstantMessagingClient>());
    Map<String, GenericEventListener> listeners = Collections.synchronizedMap(new HashMap<String, GenericEventListener>());
    @Autowired
    TaskExecutorService taskExecutorService;
    @Autowired
    UserService userService;
    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    InstantMessaging imService;

    /**
     * empty constructor
     */
    private ClientManagerImpl() {
        // nothing to do
    }

    /**
     * Creates an new instant messaging client and connects automatically to the server. This method should only be used in a constructor and not be triggered by GUI
     * events, otherwise clients that lost connections may get recreated again.
     * 
     * @param username
     * @return JabberClient even if IM service is down
     */
    @Override
    public InstantMessagingClient getInstantMessagingClient(final String username) {
        // no need to sync over whole method (get/put) since only called once per
        // UserRequest and username.
        // we do not sync since "new InstantMessagingClient(...)" may last quite
        // long.
        InstantMessagingClient client;
        client = clients.get(username);
        if (client == null) {
            final String password = getInstantMessagingCredentialsForUser(username);
            Identity identity = baseSecurity.findIdentityByName(username);

            IMUserInfo info = new IMUserInfo(identity, password);
            info.setEmail(userService.getUserProperty(identity.getUser(), UserConstants.EMAIL));
            info.setFullUserName(userService.getFirstAndLastname(identity.getUser()));

            final ImPreferences prefs = ImPrefsManager.getInstance().loadOrCreatePropertiesFor(identity);
            info.setRosterDefaultStatus(prefs.getRosterDefaultStatus());

            client = new InstantMessagingClient(info, taskExecutorService, imService);
            clients.put(username, client);
            return client;
        } else {
            return client;
        }
    }

    /**
     * Check whether a user has already an IM client running Use this method when fetching clients outside a controller constructor as users may have several sessions to
     * avoid reconnection of an duplicate session
     * 
     * @param username
     * @return
     */
    @Override
    public boolean hasActiveInstantMessagingClient(final String username) {
        return clients.containsKey(username);
    }

    /**
     * @param username
     * @param listener
     * @param listenToAllMessages
     *            - only the main controller needs to listen to all messages
     */
    @Override
    public void registerEventListener(final String username, final GenericEventListener listener, final boolean listenToAllMessages) {
        if (listenToAllMessages) {
            listeners.put(username, listener);
        } else {
            listeners.put(username + listener.hashCode(), listener);
        }
    }

    @Override
    public GenericEventListener getRegisteredEventListeners(final String username, final Controller controller) {
        return listeners.get(username + controller.hashCode());
    }

    @Override
    public void deregisterControllerListener(final String username, final Controller controller) {
        listeners.remove(username + controller.hashCode());
    }

    @Override
    public Chat createChat(final String username, final String chatPartnerJid, final Controller controller) {
        final ChatManager chatmanager = getInstantMessagingClient(username).getConnection().getChatManager();
        final Chat chat = chatmanager.createChat(chatPartnerJid, new MessageListener() {

            @Override
            public void processMessage(final Chat chat, final Message message) {
                message.setProperty("receiveTime", new Long(new Date().getTime()));
                final GenericEventListener listener = listeners.get(username + controller.hashCode());
                listener.event(new InstantMessagingEvent(message, "chatmessage"));
            }
        });
        return chat;
    }

    /**
     * @param username
     */
    @Override
    public void addMessageListener(final String username) {
        final PacketListener packetListener = new PacketListener() {
            @Override
            public void processPacket(final Packet packet) {
                final Message jabbmessage = (Message) packet;
                // TODO:gs:b see issue: http://bugs.olat.org/jira/browse/OLAT-2966
                // filter <script> msg. out - security risk of cross site scripting!
                // or may user ext.util.strip script tag method on client side
                jabbmessage.setProperty("receiveTime", new Long(new Date().getTime()));
                final GenericEventListener listener = listeners.get(username);
                if (listener != null) {
                    listener.event(new InstantMessagingEvent(packet, "message"));
                    if (log.isDebugEnabled()) {
                        log.debug("routing message event to controller of: " + packet.getTo());
                    }
                } else {
                    log.warn("could not find listener for IM message for username: " + username, null);
                }
            }
        };
        getInstantMessagingClient(username).getConnection().addPacketListener(packetListener, new PacketTypeFilter(Message.class));
    }

    /**
     * @param username
     */
    @Override
    public void addPresenceListener(final String username) {
        final PacketListener packetListener = new PacketListener() {
            @Override
            public void processPacket(final Packet packet) {
                try {
                    final GenericEventListener listener = listeners.get(username);
                    if (listener == null) {
                        log.warn("could not route presence event as presence listener is null for user: " + username, null);
                    } else {
                        listener.event(new InstantMessagingEvent(packet, "presence"));
                        final Presence presence = (Presence) packet;
                        if (log.isDebugEnabled()) {
                            log.debug("routing presence event to controller of: " + presence.getTo());
                        }
                    }
                } catch (final Throwable th) {
                    log.warn("Presence package", th);
                }
            }
        };
        getInstantMessagingClient(username).getConnection().addPacketListener(packetListener, new PacketTypeFilter(Presence.class));
    }

    /**
     * helper method to trigger a presence update even if the server does not send a presence packet itself (e.g. entering a test but no other buddies are online)
     * 
     * @param username
     */
    @Override
    public void sendPresenceEvent(final Presence.Type type, final String username) {
        final Presence presence = new Presence(type);
        presence.setTo(username);
        final GenericEventListener listener = listeners.get(username);
        if (listener != null) {
            listener.event(new InstantMessagingEvent(presence, "presence"));
        }
    }

    /**
     * Looks if user has credentials for IM. If not (auth == null) a new accounts with a random generated password gets created otherwise the password gets returned.
     * 
     * @param username
     *            the OLAT username
     * @return the password used for instant messaging
     */
    @Override
    public String getInstantMessagingCredentialsForUser(final String username) {
        final Identity identity = baseSecurity.findIdentityByName(username);
        if (identity == null) {
            // OLAT-3556: masking this error temporarily
            // @TODO
            // @FIXME
            log.warn("Identity not found for username=" + username);
            return null;
        }
        // synchronized: not needed here, since the credentials are only created once for a user (when that user logs in for the very first time).
        // And a user will almost! never log in at the very same time from two different machines.
        Authentication auth = baseSecurity.findAuthentication(identity, PROVIDER_INSTANT_MESSAGING);
        final InstantMessaging im = InstantMessagingModule.getAdapter();
        if (auth == null) { // create new authentication for provider and also a new IM-account

            // if account exists on IM server but not on OLAT delete it first
            if (im.hasAccount(username)) {
                im.deleteAccount(username);
            }

            final String pw = RandomStringUtils.randomAlphanumeric(6);
            if (im.createAccount(username, pw, userService.getFirstAndLastname(identity.getUser()), userService.getUserProperty(identity.getUser(), UserConstants.EMAIL))) {
                auth = baseSecurity.createAndPersistAuthentication(identity, PROVIDER_INSTANT_MESSAGING, identity.getName().toLowerCase(), pw);
                log.info("Audit:New instant messaging authentication account created for user:" + username);
                return auth.getCredential();
            } else {
                log.warn("new instant messaging account creation failed for user: " + username);
                return null;
            }
        }
        /**
         * this does not decouple IM from the loginprocess, move account recreations in background thread somewhere else maybe to the login background thread...
         */
        // else {
        // //user has IM account credentials on OLAT, check whether account on IM server side exists
        // if (!im.hasAccount(username)) {
        // boolean success = im.createAccount(username, auth.getCredential(), getFullname(identity), identity.getUser().getProperty(UserConstants.EMAIL, null));
        // if (success) {
        // log.info("Audit:New instant messaging authentication account created for user:" + username);
        // } else {
        // log.warn("new instant messaging account creation failed for user: " + username);
        // }
        // }
        // }
        return auth.getCredential();
    }

    /**
     * When a user logs out of olat we logout the client from the jabber server and free the ressource
     * 
     * @param username
     */
    @Override
    public void destroyInstantMessagingClient(final String username) {
        InstantMessagingClient client;
        client = clients.get(username);
        if (client != null) {
            listeners.remove(username);
            client.closeConnection(false);
            clients.remove(username);
        }
    }

    /**
     * returns the map (its iterator is safe)
     * 
     * @return map
     */
    @Override
    public Map<String, InstantMessagingClient> getClients() {
        final HashMap<String, InstantMessagingClient> hm = new HashMap<String, InstantMessagingClient>(clients);
        return hm;
    }

}
