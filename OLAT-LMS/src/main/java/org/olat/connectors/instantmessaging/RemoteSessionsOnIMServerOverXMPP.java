package org.olat.connectors.instantmessaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.olat.connectors.instantmessaging.IMSessionItems.Item;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.UserConstants;
import org.olat.lms.instantmessaging.ConnectedUsersListEntry;
import org.olat.lms.instantmessaging.IMConfig;
import org.olat.lms.instantmessaging.ImPreferences;
import org.olat.lms.instantmessaging.ImPrefsManager;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.user.UserService;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class RemoteSessionsOnIMServerOverXMPP implements InstantMessagingSessionItems {

    private static final Logger log = LoggerHelper.getLogger();

    private AdminUserConnection adminUser;
    private CacheWrapper sessionItemsCache;
    private final ImPrefsManager imPrefsManager;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private UserService userService;

    protected RemoteSessionsOnIMServerOverXMPP(final ImPrefsManager imPrefsManager) {
        this.imPrefsManager = imPrefsManager;
        final ProviderManager providerMgr = ProviderManager.getInstance();
        // register iq handler
        providerMgr.addIQProvider("query", SessionItems.NAMESPACE, new SessionItems.Provider());
    }

    public void setConnection(final AdminUserConnection adminUser) {
        this.adminUser = adminUser;
    }

    /**
	 */
    @Override
    public List<ConnectedUsersListEntry> getConnectedUsers(final Identity currentUser) {
        /**
         * create a cache for the entries as looping over a few hundred entries need too much time. Every node has its own cache and therefore no need to inform each
         * other o_clusterOK by guido
         */
        if (sessionItemsCache == null) {
            synchronized (this) {
                sessionItemsCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "items");
            }
        }
        final String currentUsername = currentUser.getName();
        final List<ConnectedUsersListEntry> entries = new ArrayList<ConnectedUsersListEntry>();
        final IMSessionItems imSessions = (IMSessionItems) sendPacket(new SessionItems());
        final List<IMSessionItems.Item> sessions = imSessions.getItems();

        for (final Iterator<Item> iter = sessions.iterator(); iter.hasNext();) {
            final IMSessionItems.Item item = iter.next();

            if (item.getResource().startsWith(IMConfig.RESOURCE)) {
                ConnectedUsersListEntry entry = (ConnectedUsersListEntry) sessionItemsCache.get(item.getUsername());
                if (entry != null && !item.getUsername().equals(currentUsername)) {
                    entries.add(entry);
                    log.debug("loading item from cache: " + item.getUsername());
                } else {

                    Identity identity = baseSecurity.findIdentityByName(item.getUsername());
                    if (identity != null) {
                        identity = (Identity) DBFactory.getInstance().loadObject(identity);
                        try {
                            final ImPreferences imPrefs = imPrefsManager.loadOrCreatePropertiesFor(identity);
                            if ((imPrefs != null)) {
                                entry = new ConnectedUsersListEntry(item.getUsername(), identity.getUser().getPreferences().getLanguage());
                                entry.setName(userService.getUserProperty(identity.getUser(), UserConstants.LASTNAME));
                                entry.setPrename(userService.getUserProperty(identity.getUser(), UserConstants.FIRSTNAME));
                                entry.setShowAwarenessMessage(imPrefs.isAwarenessVisible());
                                entry.setShowOnlineTime(imPrefs.isOnlineTimeVisible());
                                entry.setAwarenessMessage(item.getPresenceMsg());
                                entry.setInstantMessagingStatus(item.getPresenceStatus());
                                entry.setLastActivity(item.getLastActivity());
                                entry.setOnlineTime(item.getLoginTime());
                                entry.setJabberId(InstantMessagingModule.getAdapter().getUserJid(item.getUsername()));
                                entry.setVisibleToOthers(imPrefs.isVisibleToOthers());
                                entry.setResource(item.getResource());
                                entries.add(entry);

                                // put in cache. Sync. is done by cache
                                sessionItemsCache.put(item.getUsername(), entry);
                            }
                        } catch (final AssertException ex) {
                            log.warn("Can not load IM-Prefs for identity=" + identity, ex);
                        }
                    }
                }
            }
        } // end of loop
        return entries;
    }

    private IQ sendPacket(final IQ packet) {
        final XMPPConnection con = adminUser.getConnection();
        try {
            packet.setFrom(con.getUser());
            final PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
            con.sendPacket(packet);
            final IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            collector.cancel();

            if (response == null) {
                log.warn("Error while trying to get all sessions IM server. Response was null!");
                return null;
            }
            if (response.getError() != null) {
                log.warn("Error while trying to get all sessions IM server. " + response.getError().getMessage());
                return null;
            } else if (response.getType() == IQ.Type.ERROR) {
                // TODO:gs handle conflict case when user already exists
                // System.out.println("error response: "+response.getChildElementXML());
                log.warn("Error while trying to get all sessions at IM server");
                return null;
            }
            return response;
        } catch (final RuntimeException e) {
            log.warn("Error while trying to get all sessions at IM server");
            return null;
        } catch (final Exception e) {
            log.warn("Error while trying to get all sessions at IM server");
            return null;
        }
    }

}
