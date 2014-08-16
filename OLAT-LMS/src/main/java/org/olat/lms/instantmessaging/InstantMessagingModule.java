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

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.event.Event;
import org.olat.system.event.FrameworkStartedEvent;
import org.olat.system.event.FrameworkStartupEventChannel;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: <br />
 * For configuration see olat.properties and put overwrite values in olat.local.properties or directly edit spring config of instant messaging
 * <P>
 * Initial Date: 14.10.2004
 * 
 * @author Guido Schnider
 */
@Component
public class InstantMessagingModule implements Initializable, UserDataDeletable, GenericEventListener {

    private static ConnectionConfiguration connConfig;
    // FIXME: used for legacy access
    private static InstantMessaging instantMessaingStatic;
    private IMConfig config;
    private static final String PROPERTY_NAME_PREFIX = "InstantMessagingModule"; // stored in DB
    private static boolean enabled = false;
    private static final String CONFIG_SYNCED_BUDDY_GROUPS = "issynced";
    private static final String CONFIG_SYNCED_LEARNING_GROUPS = "syncedlearninggroups";
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private PropertyManager propertyManager;
    @Autowired
    private DB database;
    // via setter
    private InstantMessaging instantMessaging;
    @Autowired
    private FrameworkStartupEventChannel frameworkStartupEventChannel;
    @Autowired
    private BaseSecurity baseSecurity;

    /**
     * [used by spring]
     */
    private InstantMessagingModule() {
        super();
    }

    /**
     * @param instantMessaging
     */
    @Autowired(required = true)
    public void setInstantMessaing(final InstantMessaging instantMessaging) {
        this.instantMessaging = instantMessaging;
        instantMessaingStatic = instantMessaging;
    }

    /**
     * [spring]
     * 
     * @param config
     */
    @Autowired(required = true)
    public void setIMConfig(final IMConfig config) {
        this.config = config;
        enabled = config.isEnabled();
    }

    /**
	 */
    @Override
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {

        if (config.isEnabled()) {

            frameworkStartupEventChannel.registerForStartupEvent(this);
            // create test accounts local and on the IM server
            if (config.generateTestUsers()) {
                checkAndCreateTestUsers();
            }
        }

    }

    /**
     * Internal helper to create a property name for a class configuration property
     * 
     * @param clazz
     * @param configurationName
     * @return String
     */
    private String createPropertyName(final String configurationName) {
        return PROPERTY_NAME_PREFIX + "::" + configurationName;
    }

    /**
     * if enabled in the configuration some testusers for IM are created in the database. It has nothing to do with accounts on the jabber server itself.
     */
    private void checkAndCreateTestUsers() {
        Identity identity;
        Authentication auth;

        identity = baseSecurity.findIdentityByName("author");
        auth = baseSecurity.findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
        if (auth == null) { // create new authentication for provider
            baseSecurity.createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(), "test");
            instantMessaging.createAccount("author", "test", "Aurich Throw", "author@olat-newinstallation.org");
        }

        identity = baseSecurity.findIdentityByName("administrator");
        auth = baseSecurity.findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
        if (auth == null) { // create new authentication for provider
            baseSecurity.createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(), "olat");
            instantMessaging.createAccount("administrator", "olat", "Administrator", "administrator@olat-newinstallation.org");
        }

        identity = baseSecurity.findIdentityByName("learner");
        auth = baseSecurity.findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
        if (auth == null) { // create new authentication for provider
            baseSecurity.createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(), "test");
            instantMessaging.createAccount("learner", "test", "Leise Arnerich", "learner@olat-newinstallation.org");
        }

        identity = baseSecurity.findIdentityByName("test");
        auth = baseSecurity.findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
        if (auth == null) { // create new authentication for provider
            baseSecurity.createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(), "test");
            instantMessaging.createAccount("test", "test", "Thomas Est", "test@olat-newinstallation.org");
        }
    }

    /**
     * @return the adapter instance
     */
    public static InstantMessaging getAdapter() {
        return instantMessaingStatic;
    }

    /**
     * @return Returns the enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * @return a reused connection configuration for connecting to the im server
     */
    protected static ConnectionConfiguration getConnectionConfiguration() {
        if (connConfig == null) {
            // 5222 is the default unsecured jabber server port
            connConfig = new ConnectionConfiguration(instantMessaingStatic.getConfig().getServername(), 5222);
            connConfig.setNotMatchingDomainCheckEnabled(false);
            connConfig.setSASLAuthenticationEnabled(false);
            connConfig.setReconnectionAllowed(false);
        }
        return connConfig;
    }

    /**
	 */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        if (instantMessaging.getConfig().isEnabled()) {
            final String imUsername = instantMessaging.getIMUsername(identity.getName());
            instantMessaging.deleteAccount(imUsername);
            log.debug("Deleted IM account for identity=" + identity);
        }
    }

    /**
     * @return Returns the iDLE_POLLTIME.
     */
    public static int getIDLE_POLLTIME() {
        return instantMessaingStatic.getConfig().getIdlePolltime();
    }

    /**
     * @param idle_polltime
     *            The iDLE_POLLTIME to set.
     */
    public static void setIDLE_POLLTIME(final int idle_polltime) {
        instantMessaingStatic.getConfig().setIdlePolltime(idle_polltime);
    }

    /**
     * @return Returns the cHAT_POLLTIME.
     */
    public static int getCHAT_POLLTIME() {
        return instantMessaingStatic.getConfig().getChatPolltime();
    }

    /**
     * @param chat_polltime
     *            The cHAT_POLLTIME to set.
     */
    public static void setCHAT_POLLTIME(final int chat_polltime) {
        instantMessaingStatic.getConfig().setChatPolltime(chat_polltime);
    }

    public static boolean isSyncLearningGroups() {
        return instantMessaingStatic.getConfig().isEnabled() && instantMessaingStatic.getConfig().isSyncLearningGroups();
    }

    @Override
    public void event(final Event event) {
        // synchronistion of learning groups needs the whole olat course stuff loaded

        // synchronizing of existing buddygroups with the instant messaging
        // server
        // if done we set a property (it gets only done once, to reactivate delete
        // entry in table o_property)
        /**
         * delete from o_property where name='org.olat.instantMessaging.InstantMessagingModule::syncedbuddygroups';
         */

        if (event instanceof FrameworkStartedEvent) {
            boolean success = false;
            try {
                syncGroupsOfCertainType(CONFIG_SYNCED_BUDDY_GROUPS);
                syncGroupsOfCertainType(CONFIG_SYNCED_LEARNING_GROUPS);
                database.commitAndCloseSession();
                success = true;
            } finally {
                if (!success) {
                    database.rollbackAndCloseSession();
                }
            }
        }
    }

    private void syncGroupsOfCertainType(String groupType) {
        final List props = propertyManager.findProperties(null, null, null, "classConfig", createPropertyName(groupType));
        if (props.size() == 0) {
            if (config.isSyncLearningGroups() && groupType.equals(CONFIG_SYNCED_LEARNING_GROUPS)) {
                instantMessaging.synchronizeLearningGroupsWithIMServer();
            }
            if (config.isSyncPersonalGroups() && groupType.equals(CONFIG_SYNCED_BUDDY_GROUPS)) {
                instantMessaging.synchronizeAllBuddyGroupsWithIMServer();
            }
            final PropertyImpl property = propertyManager.createPropertyInstance(null, null, null, "classConfig", createPropertyName(groupType), null, null,
                    Boolean.toString(true), null);
            propertyManager.saveProperty(property);
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
