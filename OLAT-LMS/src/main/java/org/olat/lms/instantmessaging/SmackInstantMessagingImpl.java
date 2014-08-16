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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.olat.connectors.instantmessaging.AdminUserConnection;
import org.olat.connectors.instantmessaging.InstantMessagingGroupSynchronisation;
import org.olat.connectors.instantmessaging.InstantMessagingServerPluginVersion;
import org.olat.connectors.instantmessaging.InstantMessagingSessionCount;
import org.olat.connectors.instantmessaging.InstantMessagingSessionItems;
import org.olat.connectors.instantmessaging.RemoteAccountCreation;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.instantmessaging.task.CountSessionsOnServerTask;
import org.olat.lms.instantmessaging.task.GroupChatJoinTask;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.AutoCreator;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.instantmessaging.groupchat.GroupChatManagerController;
import org.olat.presentation.instantmessaging.rosterandchat.InstantMessagingMainController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.taskexecutor.TaskExecutorManager;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the InstantMessaging Interface based on the SMACK instant messaging library from jivesoftware.org
 * <P>
 * Initial Date: 18.01.2005 <br />
 * 
 * @author guido
 */
public class SmackInstantMessagingImpl implements InstantMessaging {

    private static final Logger log = LoggerHelper.getLogger();

    private IMConfig config;
    private InstantMessagingGroupSynchronisation buddyGroupService;
    private InstantMessagingSessionCount sessionCountService;
    private InstantMessagingSessionItems sessionItemsService;
    private RemoteAccountCreation accountService;
    ClientManager clientManager;
    private IMNameHelper nameHelper;
    private AdminUserConnection adminConnecion;
    private String clientVersion;
    private InstantMessagingServerPluginVersion pluginVersion;
    private AutoCreator actionControllerCreator;
    private volatile int[] sessionCount = new int[1];
    private long timeOfLastSessionCount;
    @Autowired(required = false)
    private TaskExecutorManager taskExecutorService;

    @Autowired
    private BaseSecurity baseSecurity;

    /**
     * [spring]
     */
    private SmackInstantMessagingImpl() {
        //
    }

    /**
     * org.olat.presentation.framework.control.WindowControl)
     */
    @Override
    public Controller createClientController(final UserRequest ureq, final WindowControl wControl) {
        final InstantMessagingClient client = clientManager.getInstantMessagingClient(ureq.getIdentity().getName());
        // there are two versions of the controller, either join the course chat
        // automatically or upon request
        client.setGroupChatManager((GroupChatManagerController) actionControllerCreator.createController(ureq, wControl));
        return new InstantMessagingMainController(ureq, wControl);
    }

    /**
     * [used by spring]
     */
    public void setActionController(final ControllerCreator actionControllerCreator) {
        this.actionControllerCreator = (AutoCreator) actionControllerCreator;
    }

    /**
	 */
    @Override
    public GroupChatManagerController getGroupChatManagerController(final UserRequest ureq) {
        return clientManager.getInstantMessagingClient(ureq.getIdentity().getName()).getGroupChatManagerController();
    }

    /**
     * java.lang.String, java.lang.String, java.lang.String) o_clusterOK by:fj - nodes can access the IM server concurrently but only one thread should add a users to a
     * group at the same time. Sync over whole clazz, not time critical as accessed by backgrounded threads
     */
    // TODO:gs does this need to be synchronized?
    @Override
    public synchronized boolean addUserToFriendsRoster(String groupOwnerUsername, final String groupId, final String groupname, String addedUsername) {
        // we have to make sure the user has an account on the instant messaging
        // server
        // by calling this it gets created if not yet exists.
        addedUsername = nameHelper.getIMUsernameByOlatUsername(addedUsername);
        groupOwnerUsername = nameHelper.getIMUsernameByOlatUsername(groupOwnerUsername);

        final boolean hasAccount = accountService.hasAccount(addedUsername);
        if (!hasAccount) {
            clientManager.getInstantMessagingCredentialsForUser(addedUsername);
        }
        // we do not check whether a group already exists, we create it each
        // time
        final List<String> list = new ArrayList<String>();
        list.add(groupOwnerUsername);
        buddyGroupService.createSharedGroup(groupId, groupname, list);

        log.debug("Adding user to roster group::" + groupId + " username: " + addedUsername);

        return buddyGroupService.addUserToSharedGroup(groupId, addedUsername);
    }

    /**
     * java.lang.String)
     */
    @Override
    public boolean removeUserFromFriendsRoster(final String groupId, final String username) {
        final String imUsername = nameHelper.getIMUsernameByOlatUsername(username);

        log.debug("Deleting user from roster group::" + groupId + " username: " + imUsername);

        return buddyGroupService.removeUserFromSharedGroup(groupId, imUsername);
    }

    /**
	 */
    @Override
    public boolean deleteRosterGroup(final String groupId) {
        // groupId is already converted to single/multiple instance version

        log.debug("Deleting roster group from instant messaging server::" + groupId);

        return buddyGroupService.deleteSharedGroup(groupId);

    }

    /**
     * @param groupId
     * @param displayName
     */
    @Override
    public boolean renameRosterGroup(final String groupId, final String displayName) {

        log.debug("Renaming roster group on instant messaging server::" + groupId);

        return buddyGroupService.renameSharedGroup(groupId, displayName);
    }

    /**
     * java.lang.String)
     */
    @Override
    public void sendStatus(final String username, final String message) {
        // only send status if client is active otherwise course dispose may
        // recreate an connection
        if (clientManager.hasActiveInstantMessagingClient(username)) {
            final InstantMessagingClient imc = clientManager.getInstantMessagingClient(username);
            final String recentStatus = imc.getStatus();
            // awareness presence packets get only sended if not "unavailable".
            // Otherwise the unavailable status gets overwritten by an available
            // one.
            if (!recentStatus.equals(InstantMessagingConstants.PRESENCE_MODE_UNAVAILABLE)) {
                imc.sendPresence(Presence.Type.available, message, 0, Presence.Mode.valueOf(imc.getStatus()));
            }
        }
    }

    /**
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean createAccount(final String username, final String password, final String fullname, final String email) {
        boolean success;
        success = accountService.createAccount(nameHelper.getIMUsernameByOlatUsername(username), password, fullname, email);

        log.debug("Creating new user account on IM server for user:" + username + " returned: " + success);

        return success;
    }

    /**
	 */
    @Override
    public boolean deleteAccount(final String username) {
        boolean success;
        success = accountService.deleteAccount(nameHelper.getIMUsernameByOlatUsername(username));

        log.debug("Deleting user account on IM server for user:" + username + " returned: " + success);

        return success;
    }

    /**
	 */
    @Override
    public String getIMPassword(final String username) {
        return clientManager.getInstantMessagingClient(username).getPassword();

    }

    /**
     * @return Set containing the usernames
     */
    @Override
    public Set getUsernamesFromConnectedUsers() {
        return new HashSet<String>(getClients().keySet());
    }

    /**
	 */
    @Override
    public Map getClients() {
        return clientManager.getClients();
    }

    /**
	 */
    @Override
    public void enableChat(final String username) {
        clientManager.getInstantMessagingClient(username).enableCollaboration();

        log.debug("Enabling chat for user::" + username);

    }

    /**
     * @param username
     * @param reason
     *            A resason why the chat is disabled like "Doing test" java.lang.String)
     */
    @Override
    public void disableChat(final String username, final String reason) {
        clientManager.getInstantMessagingClient(username).disableCollaboration(reason);

        log.debug("Disabling chat for user::" + username + "and reason" + reason);

    }

    /**
	 */
    @Override
    public int countConnectedUsers() {
        final long now = System.currentTimeMillis();
        if ((now - timeOfLastSessionCount) > 30000) { // only grab session count
                                                      // every 30s
            log.debug("Getting session count from IM server");
            try {
                taskExecutorService.runTask(new CountSessionsOnServerTask(sessionCountService, sessionCount));
            } catch (final RejectedExecutionException e) {
                log.error("countConnectedUsers: TaskExecutorManager rejected execution of CountSessionsOnServerTask. Cannot update user count", e);
            }
            timeOfLastSessionCount = System.currentTimeMillis();
        }
        return sessionCount[0];
    }

    /**
	 */
    @Override
    public boolean synchonizeBuddyRoster(final BusinessGroup group) {

        final SecurityGroup owners = group.getOwnerGroup();
        final SecurityGroup participants = group.getPartipiciantGroup();
        final List<Identity> users = baseSecurity.getIdentitiesOfSecurityGroup(owners);
        users.addAll(baseSecurity.getIdentitiesOfSecurityGroup(participants));

        int counter = 0;
        final List<String> usernames = new ArrayList<String>();
        for (final Iterator<Identity> iter = users.iterator(); iter.hasNext();) {
            final Identity ident = iter.next();
            log.debug("getting im credentials for user::" + ident.getName());
            // as jive only adds users to a group that already exist we have to
            // make
            // sure they have an account.
            clientManager.getInstantMessagingCredentialsForUser(ident.getName());
            usernames.add(nameHelper.getIMUsernameByOlatUsername(ident.getName()));
            if (counter % 6 == 0) {
                DBFactory.getInstance().intermediateCommit();
            }
            counter++;
        }
        final String groupId = InstantMessagingModule.getAdapter().createChatRoomString(group);
        if (users.size() > 0) { // only sync groups with users
            if (!buddyGroupService.createSharedGroup(groupId, group.getName(), usernames)) {
                log.error("could not create shared group: " + groupId, null);
            }
            log.debug("synchronizing group::" + group.toString());
        } else {
            log.debug("empty group: not synchronizing group::" + group.toString());
        }
        // when looping over all buddygroups and learninggroups close
        // transaction after each group
        DBFactory.getInstance().intermediateCommit();
        return true;
    }

    /**
	 */
    @Override
    public boolean synchronizeLearningGroupsWithIMServer() {
        log.info("Starting synchronisation of LearningGroups with IM server");
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final BGContextDao contextManager = BGContextDaoImpl.getInstance();
        // pull as admin
        final Roles roles = new Roles(true, true, true, true, false, true, false);
        final List<RepositoryEntry> allCourses = rm.queryByTypeLimitAccess(CourseModule.getCourseTypeName(), roles);
        int counter = 0;
        for (final Iterator<RepositoryEntry> iterator = allCourses.iterator(); iterator.hasNext();) {
            final RepositoryEntry entry = iterator.next();
            ICourse course = null;
            try {
                course = CourseFactory.loadCourse(entry.getOlatResource());
            } catch (final Exception e) {
                log.error("Could not load Course! OlatResourcable: " + entry.getOlatResource(), e);
                continue;
            }

            final CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
            final List<BusinessGroup> groups = groupManager.getAllLearningGroupsFromAllContexts(course);
            for (final Iterator<BusinessGroup> iter = groups.iterator(); iter.hasNext();) {
                final BusinessGroup group = iter.next();

                final boolean syncLearn = InstantMessagingModule.getAdapter().getConfig().isSyncLearningGroups();
                final boolean isLearn = group.getType().equals(BusinessGroup.TYPE_LEARNINGROUP);

                if (isLearn && !syncLearn) {
                    final String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
                    if (deleteRosterGroup(groupID)) {
                        log.info("deleted unwanted group: " + group.getResourceableTypeName() + " " + groupID, null);
                    }
                    counter++;
                    if (counter % 6 == 0) {
                        DBFactory.getInstance(false).intermediateCommit();
                    }
                    continue;
                }

                if (!synchonizeBuddyRoster(group)) {
                    log.error("couldn't sync group: " + group.getResourceableTypeName(), null);
                }
                counter++;
                if (counter % 6 == 0) {
                    DBFactory.getInstance(false).intermediateCommit();
                }
            }

            if (counter % 6 == 0) {
                DBFactory.getInstance(false).intermediateCommit();
            }
        }
        log.info("Ended synchronisation of LearningGroups with IM server: Synched " + counter + " groups");
        return true;
    }

    /**
     * Synchronize the groups with the IM system To synchronize buddygroups, use the null-context. Be aware that this action might take some time!
     * 
     * @param groupContext
     * @return true if successfull, false if IM server is not running
     */
    @Override
    public boolean synchronizeAllBuddyGroupsWithIMServer() {
        log.info("Started synchronisation of BuddyGroups with IM server.");
        final BGContextDao cm = BGContextDaoImpl.getInstance();
        // null as argument pulls all buddygroups
        final List<BusinessGroup> groups = cm.getGroupsOfBGContext(null);
        int counter = 0;
        for (final Iterator<BusinessGroup> iter = groups.iterator(); iter.hasNext();) {
            final BusinessGroup group = iter.next();
            synchonizeBuddyRoster(group);
            counter++;
            if (counter % 6 == 0) {
                DBFactory.getInstance(false).intermediateCommit();
            }
        }
        log.info("Ended synchronisation of BuddyGroups with IM server: Synched " + counter + " groups");
        return true;
    }

    /**
     * .core.id.OLATResourceable
     */
    @Override
    public String createChatRoomString(final OLATResourceable ores) {
        final String roomName = ores.getResourceableTypeName() + "-" + ores.getResourceableId();
        return nameHelper.getGroupnameForOlatInstance(roomName);
    }

    @Override
    public String createChatRoomJID(final OLATResourceable ores) {
        return createChatRoomString(ores) + "@" + config.getConferenceServer();
    }

    /**
	 */
    @Override
    public List<ConnectedUsersListEntry> getAllConnectedUsers(final Identity currentUser) {
        return sessionItemsService.getConnectedUsers(currentUser);
    }

    /**
     * [used by spring]
     * 
     * @param sessionCountService
     */
    public void setSessionCountService(final InstantMessagingSessionCount sessionCountService) {
        this.sessionCountService = sessionCountService;
    }

    /**
     * [used by spring]
     * 
     * @param sessionCountService
     */
    public void setBuddyGroupService(final InstantMessagingGroupSynchronisation buddyGroupService) {
        this.buddyGroupService = buddyGroupService;
    }

    /**
     * [used by spring]
     * 
     * @param sessionItemsService
     */
    public void setSessionItemsService(final InstantMessagingSessionItems sessionItemsService) {
        this.sessionItemsService = sessionItemsService;
    }

    /**
     * [used by spring]
     * 
     * @param accountService
     */
    public void setAccountService(final RemoteAccountCreation accountService) {
        this.accountService = accountService;
    }

    /**
     * [used by spring]
     * 
     * @param clientManager
     */
    public void setClientManager(final ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * @return client manager where you have access to the IM client itself
     */
    @Override
    public ClientManager getClientManager() {
        return clientManager;
    }

    @Override
    public IMConfig getConfig() {
        return config;
    }

    public void setConfig(final IMConfig config) {
        this.config = config;
    }

    /**
	 */
    @Override
    public boolean hasAccount(final String username) {
        return accountService.hasAccount(nameHelper.getIMUsernameByOlatUsername(username));
    }

    /**
	 */
    @Override
    public String getUserJid(final String username) {
        return nameHelper.getIMUsernameByOlatUsername(username) + "@" + config.getServername();
    }

    /**
	 */
    @Override
    public String getUsernameFromJid(final String jid) {
        return nameHelper.extractOlatUsername(jid);
    }

    @Override
    public String getIMUsername(final String username) {
        return nameHelper.getIMUsernameByOlatUsername(username);
    }

    @Override
    public void setNameHelper(final IMNameHelper nameHelper) {
        this.nameHelper = nameHelper;
    }

    /**
     * [spring]
     * 
     * @param adminConnection
     */
    public void setAdminConnection(final AdminUserConnection adminConnection) {
        this.adminConnecion = adminConnection;
    }

    /**
	 */
    @Override
    public void resetAdminConnection() {
        this.adminConnecion.resetAndReconnect();
    }

    /**
     * [spring]
     * 
     * @param clientVersion
     */
    public void setClientVersion(final String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public void setServerPluginVersion(final InstantMessagingServerPluginVersion pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    /**
	 */
    @Override
    public String checkServerPlugin() {
        if (clientVersion.equals(pluginVersion.getPluginVersion())) {
            return "<b>Jupee!</b> Server plugin and OLAT client run on the same version: " + pluginVersion.getPluginVersion();
        } else if (pluginVersion.getPluginVersion() == null) {
            return "The server does not respond with a version. Do you have the plugin installed? Does the admin user have a running connection to the IM server?";
        }
        return "OLAT runs on client version: " + clientVersion + " but the server version is: " + pluginVersion.getPluginVersion() + "<br/><b>Plese upgrade!</b>";
    }

    @Override
    public IMNameHelper getNameHelper() {
        return nameHelper;
    }

    /**
	 *
	 */
    @Override
    public GroupChatJoinTask joinGroupChatAsyc(OLATResourceable ores, MultiUserChat muc, XMPPConnection connection, String roomJID, String username, String roomName,
            GenericEventListener listener) {
        GroupChatJoinTask roomJoinTask = new GroupChatJoinTask(ores, muc, connection, roomJID, username, roomName, listener);
        taskExecutorService.runTask(roomJoinTask);
        return roomJoinTask;
    }

}
