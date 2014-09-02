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
package org.olat.lms.group;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.olat.connectors.instantmessaging.SyncSingleUserTask;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.BusinessGroupDao;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;
import org.olat.lms.commons.taskexecutor.TaskExecutorService;
import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.group.right.BGRightManager;
import org.olat.lms.group.right.BGRightManagerImpl;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.user.UserDataDeletable;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.AbstractWaitingListGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.RightLearningGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.WaitingListLearningGroupConfirmationSender;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for BusinessGroupServiceImpl
 * 
 * <P>
 * Initial Date: 14.06.2011 <br>
 * 
 * @author guido
 */
@Service
public class BusinessGroupServiceImpl implements BusinessGroupService, Initializable, UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    private List<DeletableGroupData> deleteListeners;
    @Autowired
    BusinessGroupDao businessGroupDAO;
    // via init as only used to have less code in this class
    BusinessGroupCreateHelper businessGroupCreate;
    @Autowired
    BaseSecurity securityManager;
    @Autowired
    BusinessGroupArchiver businessGroupArchiver;
    @Autowired
    BusinessGroupContextService businessGroupContextService;
    @Autowired
    UserDeletionManager userDeletionManager;
    @Autowired
    TaskExecutorService taskExecutorService;
    @Autowired
    GroupImporterExporter groupImporterExporter;
    @Autowired
    OLATResourceManager resourceManager;

    /**
     * [spring only]
     */
    BusinessGroupServiceImpl() {
        //
    }

    @Override
    @PostConstruct
    public void init() {
        deleteListeners = new ArrayList<DeletableGroupData>();

        // create manually as only needed as helper class
        businessGroupCreate = new BusinessGroupCreateHelper();
        businessGroupCreate.setBaseSecurity(securityManager);
        businessGroupCreate.setBusinessGroupManager(businessGroupDAO);
        businessGroupCreate.setOlatResourceManager(resourceManager);
    };

    /**
     * java.lang.Integer, java.lang.Integer, java.lang.Boolean, java.lang.Boolean, org.olat.data.group.context.BGContext)
     */
    @Override
    public BusinessGroup createAndPersistBusinessGroup(final String type, final Identity identity, final String name, final String description,
            final Integer minParticipants, final Integer maxParticipants, final Boolean enableWaitinglist, final Boolean enableAutoCloseRanks,
            final BGContext groupContext) {
        final BusinessGroup grp = businessGroupCreate.createAndPersistBusinessGroup(type, identity, name, description, minParticipants, maxParticipants,
                enableWaitinglist, enableAutoCloseRanks, groupContext);
        if (grp != null) {
            log.info("Audit:Created Business Group" + grp.toString());
        }
        // else no group created
        return grp;
    }

    /**
     * 
     * returns the reloaded business group, a hack to fix hibernate problems
     */
    @Override
    public BusinessGroup loadBusinessGroup(BusinessGroup group) {
        return businessGroupDAO.loadBusinessGroup(group);
    }

    /**
     * java.lang.Integer, org.olat.data.group.context.BGContext, java.util.Map, boolean, boolean, boolean, boolean, boolean, boolean)
     */
    @Override
    public BusinessGroup copyBusinessGroup(final BusinessGroup sourceBusinessGroup, final String targetName, final String targetDescription, final Integer targetMin,
            final Integer targetMax, final BGContext targetBgContext, final Map areaLookupMap, final boolean copyAreas, final boolean copyCollabToolConfig,
            final boolean copyRights, final boolean copyOwners, final boolean copyParticipants, final boolean copyMemberVisibility, final boolean copyWaitingList) {

        // 1. create group
        final String bgType = sourceBusinessGroup.getType();
        // create group, set waitingListEnabled, enableAutoCloseRanks like source business-group
        final BusinessGroup newGroup = createAndPersistBusinessGroup(bgType, null, targetName, targetDescription, targetMin, targetMax,
                sourceBusinessGroup.getWaitingListEnabled(), sourceBusinessGroup.getAutoCloseRanksEnabled(), targetBgContext);
        // return immediately with null value to indicate an already take groupname
        if (newGroup == null) {
            return null;
        }
        // 2. copy tools
        if (copyCollabToolConfig) {
            final CollaborationToolsFactory toolsF = CollaborationToolsFactory.getInstance();
            // get collab tools from original group and the new group
            final CollaborationTools oldTools = toolsF.getOrCreateCollaborationTools(sourceBusinessGroup);
            final CollaborationTools newTools = toolsF.getOrCreateCollaborationTools(newGroup);
            // copy the collab tools settings
            for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
                final String tool = CollaborationTools.TOOLS[i];
                newTools.setToolEnabled(tool, oldTools.isToolEnabled(tool));
            }
            final String oldNews = oldTools.lookupNews();
            newTools.saveNews(oldNews);
        }
        // 3. copy member visibility
        if (copyMemberVisibility) {
            final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(newGroup);
            bgpm.copyConfigurationFromGroup(sourceBusinessGroup);
        }
        // 4. copy areas
        if (copyAreas) {
            final BGAreaDao areaManager = BGAreaDaoImpl.getInstance();
            final List areas = areaManager.findBGAreasOfBusinessGroup(sourceBusinessGroup);
            final Iterator iterator = areas.iterator();
            while (iterator.hasNext()) {
                final BGArea area = (BGArea) iterator.next();
                if (areaLookupMap == null) {
                    // reference target group to source groups areas
                    areaManager.addBGToBGArea(newGroup, area);
                } else {
                    // reference target group to mapped group areas
                    final BGArea mappedArea = (BGArea) areaLookupMap.get(area);
                    areaManager.addBGToBGArea(newGroup, mappedArea);
                }
            }
        }
        // 5. copy owners
        if (copyOwners) {
            final List owners = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getOwnerGroup());
            final Iterator iter = owners.iterator();
            while (iter.hasNext()) {
                final Identity identity = (Identity) iter.next();
                securityManager.addIdentityToSecurityGroup(identity, newGroup.getOwnerGroup());
            }
        }
        // 6. copy participants
        if (copyParticipants) {
            final List participants = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getPartipiciantGroup());
            final Iterator iter = participants.iterator();
            while (iter.hasNext()) {
                final Identity identity = (Identity) iter.next();
                securityManager.addIdentityToSecurityGroup(identity, newGroup.getPartipiciantGroup());
            }
        }
        // 7. copy rights
        if (copyRights) {
            final BGRightManager rightManager = BGRightManagerImpl.getInstance();
            final List sourceRights = rightManager.findBGRights(sourceBusinessGroup);
            final Iterator iterator = sourceRights.iterator();
            while (iterator.hasNext()) {
                final String sourceRight = (String) iterator.next();
                rightManager.addBGRight(sourceRight, newGroup);
            }
        }
        // 8. copy waiting-lisz
        if (copyWaitingList) {
            final List waitingList = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getWaitingGroup());
            final Iterator iter = waitingList.iterator();
            while (iter.hasNext()) {
                final Identity identity = (Identity) iter.next();
                securityManager.addIdentityToSecurityGroup(identity, newGroup.getWaitingGroup());
            }
        }
        return newGroup;

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeOwnerAndFireEvent(org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags, boolean)
     */
    @Override
    public void removeOwnerAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup currBusinessGroup, BGConfigFlags flags, boolean doOnlyPostRemovingStuff) {

        if (!doOnlyPostRemovingStuff) {
            securityManager.removeIdentityFromSecurityGroup(identity, currBusinessGroup.getOwnerGroup());
        }
        // remove user from buddies rosters
        removeFromRoster(identity, currBusinessGroup, flags);

        // notify currently active users of this business group
        if (identity.getKey().equals(ureqIdentity.getKey())) {
            BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.MYSELF_ASOWNER_REMOVED_EVENT, currBusinessGroup, identity);
        } else {
            BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, currBusinessGroup, identity);
        }
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(currBusinessGroup),
                LoggingResourceable.wrap(identity));
        // send notification mail in your controller!
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeOwnersAndFireEvent(org.olat.data.basesecurity.Identity, java.util.List, org.olat.data.group.BusinessGroup,
     *      org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public void removeOwnersAndFireEvent(Identity ureqIdentity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags) {

        for (final Identity identity : identities) {
            removeOwnerAndFireEvent(ureqIdentity, identity, currBusinessGroup, flags, false);
            log.info("Audit:removed identiy '" + identity.getName() + "' from securitygroup with key " + currBusinessGroup.getOwnerGroup().getKey());
        }

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeParticipantsAndFireEvent(org.olat.data.basesecurity.Identity, java.util.List, org.olat.data.group.BusinessGroup,
     *      org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public void removeParticipantsAndFireEvent(final Identity ureqIdentity, final List<Identity> identities, final BusinessGroup currBusinessGroup,
            final BGConfigFlags flags) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                for (final Identity identity : identities) {
                    removeParticipantAndFireEvent(ureqIdentity, identity, currBusinessGroup, flags, false);
                    log.info("Audit:removed identiy '" + identity.getName() + "' from securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
                }
            }
        });
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeParticipantAndFireEvent(org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags, boolean)
     */
    @Override
    public void removeParticipantAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags, boolean doOnlyPostRemovingStuff) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        if (!doOnlyPostRemovingStuff) {
            securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());
        }
        // remove user from buddies rosters
        removeFromRoster(identity, group, flags);

        // notify currently active users of this business group
        BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
        // Check if a waiting-list with auto-close-ranks is configurated
        if (group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue()) {
            // even when doOnlyPostRemovingStuff is set to true we really transfer the first Identity here
            transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, flags);
        }
        // send notification mail in your controller!

    }

    @Override
    public void deleteBusinessGroupWithMail(final BusinessGroup businessGroupTodelete, final List contactLists, final Identity mailIdentity) {

        Codepoint.codepoint(this.getClass(), "deleteBusinessGroupWithMail");

        // collect data for mail
        final List users = new ArrayList();
        final SecurityGroup ownerGroup = businessGroupTodelete.getOwnerGroup();
        if (ownerGroup != null) {
            final List owner = securityManager.getIdentitiesOfSecurityGroup(ownerGroup);
            users.addAll(owner);
        }
        final SecurityGroup partGroup = businessGroupTodelete.getPartipiciantGroup();
        if (partGroup != null) {
            final List participants = securityManager.getIdentitiesOfSecurityGroup(partGroup);
            users.addAll(participants);
        }
        final SecurityGroup watiGroup = businessGroupTodelete.getWaitingGroup();
        if (watiGroup != null) {
            final List waiting = securityManager.getIdentitiesOfSecurityGroup(watiGroup);
            users.addAll(waiting);
        }
        // now delete the group first
        deleteBusinessGroup(businessGroupTodelete);
    }

    private void removeFromRoster(final Identity identity, final BusinessGroup group, final BGConfigFlags flags) {
        if (flags.isEnabled(BGConfigFlags.BUDDYLIST)) {
            if (InstantMessagingModule.isEnabled()) {
                // only remove user from roster if not in other security group
                if (!isIdentityInBusinessGroup(identity, group)) {
                    final String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
                    InstantMessagingModule.getAdapter().removeUserFromFriendsRoster(groupID, identity.getName());
                }
            }
        }
    }

    /**
     * Transfer first identity of waiting.list (if there is one) to the participant-list. Not thread-safe! Do call this method only from a synchronized block!
     * 
     * @param wControl
     * @param ureq
     * @param trans
     * @param identity
     * @param group
     * @param flags
     * @param logger
     * @param secMgr
     */
    // o_clusterOK by:cg call this method only from synchronized code-block (removeParticipantAndFireEvent( ).
    private void transferFirstIdentityFromWaitingToParticipant(final Identity ureqIdentity, BusinessGroup group, final BGConfigFlags flags) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        // Check if waiting-list is enabled and auto-rank-up
        if (group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue()) {
            // Check if participant is not full
            final Integer maxSize = group.getMaxParticipants();
            final int waitingPartipiciantSize = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
            if ((maxSize != null) && (waitingPartipiciantSize < maxSize.intValue())) {
                // ok it has free places => get first idenity from Waitinglist
                final List identities = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup(), true/* sortedByAddedDate */);
                int i = 0;
                boolean transferNotDone = true;
                while (i < identities.size() && transferNotDone) {
                    // It has an identity and transfer from waiting-list to participant-group is not done
                    final Object[] co = (Object[]) identities.get(i++);
                    final Identity firstWaitingListIdentity = (Identity) co[0];
                    // reload group
                    // had to be commented because of db dependency. reload somewhere else ...
                    // group = (BusinessGroup) db.loadObject(group, true);
                    // Check if firstWaitingListIdentity is not allready in participant-group
                    if (!securityManager.isIdentityInSecurityGroup(firstWaitingListIdentity, group.getPartipiciantGroup())) {
                        // move the identity from the waitinglist to the participant group

                        final ActionType formerStickyActionType = ThreadLocalUserActivityLogger.getStickyActionType();
                        try {
                            // OLAT-4955: force add-participant and remove-from-waitinglist logging actions
                            // that get triggered in the next two methods to be of ActionType admin
                            // This is needed to make sure the targetIdentity ends up in the o_loggingtable
                            ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
                            this.addParticipantAndFireEvent(ureqIdentity, firstWaitingListIdentity, group, flags, false);
                            this.removeFromWaitingListAndFireEvent(ureqIdentity, firstWaitingListIdentity, group, false);
                        } finally {
                            ThreadLocalUserActivityLogger.setStickyActionType(formerStickyActionType);
                        }
                        sendConfirmation(ureqIdentity, group, firstWaitingListIdentity);
                        transferNotDone = false;
                    }
                }
            }
        } else {
            log.warn("Called method transferFirstIdentityFromWaitingToParticipant but waiting-list or autoCloseRanks is disabled.");
        }
    }

    private void sendConfirmation(final Identity ureqIdentity, BusinessGroup group, final Identity firstWaitingListIdentity) {
        List<Identity> recipientIdentities = new ArrayList<Identity>();
        recipientIdentities.add(firstWaitingListIdentity);
        AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractWaitingListGroupConfirmationSender = getWaitingListConfirmationSender(
                ureqIdentity, group);
        abstractWaitingListGroupConfirmationSender.sendMoveUserConfirmation(recipientIdentities);
    }

    private AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> getWaitingListConfirmationSender(
            final Identity ureqIdentity, BusinessGroup group) {
        AbstractWaitingListGroupConfirmationSender<? extends AbstractGroupConfirmationSenderInfo, ? extends AbstractGroupConfirmationInfo> abstractWaitingListGroupConfirmationSender;
        RepositoryEntry repositoryEntry = getCourseRepositoryEntryForBusinessGroup(group);
        RightLearningGroupConfirmationSenderInfo confirmationSenderInfo = new RightLearningGroupConfirmationSenderInfo(ureqIdentity, group, repositoryEntry);
        abstractWaitingListGroupConfirmationSender = new WaitingListLearningGroupConfirmationSender(confirmationSenderInfo);
        return abstractWaitingListGroupConfirmationSender;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#addOwnerAndFireEvent(org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags, boolean)
     */
    @Override
    public void addOwnerAndFireEvent(final Identity ureqIdentity, final Identity identity, final BusinessGroup group, final BGConfigFlags flags,
            final boolean doOnlyPostAddingStuff) {
        if (!doOnlyPostAddingStuff) {
            securityManager.addIdentityToSecurityGroup(identity, group.getOwnerGroup());
        }
        // add user to buddies rosters
        addToRoster(ureqIdentity, identity, group, flags);
        // notify currently active users of this business group
        BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(identity));
        // send notification mail in your controller!
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#findBusinessGroupsOwnedBy(java.lang.String, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.context.BGContext)
     */
    @Override
    public List<BusinessGroup> findBusinessGroupsOwnedBy(String type, Identity identity, BGContext bgContext) {
        return businessGroupDAO.findBusinessGroupsOwnedBy(type, identity, bgContext);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#findBusinessGroupsAttendedBy(java.lang.String, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.context.BGContext)
     */
    @Override
    public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, BGContext bgContext) {
        return businessGroupDAO.findBusinessGroupsAttendedBy(type, identity, bgContext);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#getDependingDeletablableListFor(org.olat.data.group.BusinessGroup, java.util.Locale)
     */
    @Override
    public List<String> getDependingDeletablableListFor(final BusinessGroup currentGroup, final Locale locale) {
        final List<String> deletableList = new ArrayList<String>();
        for (final DeletableGroupData deleteListener : deleteListeners) {
            final DeletableReference deletableReference = deleteListener.checkIfReferenced(currentGroup, locale);
            if (deletableReference.isReferenced()) {
                deletableList.add(deletableReference.getName());
            }
        }
        return deletableList;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#deleteBusinessGroup(org.olat.data.group.BusinessGroup)
     */
    @Override
    public void deleteBusinessGroup(BusinessGroup businessGroupTodelete) {

        final OLATResourceableJustBeforeDeletedEvent delEv = new OLATResourceableJustBeforeDeletedEvent(businessGroupTodelete);
        // notify all (currently running) BusinessGroupXXXcontrollers
        // about the deletion which will occur.
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, businessGroupTodelete);

        final String type = businessGroupTodelete.getType();
        // refresh object to avoid stale object exceptions
        businessGroupTodelete = loadBusinessGroup(businessGroupTodelete);
        // 0) Loop over all deletableGroupData
        for (final DeletableGroupData deleteListener : deleteListeners) {
            Log.debug("deleteBusinessGroup: call deleteListener=" + deleteListener);
            deleteListener.deleteGroupDataFor(businessGroupTodelete);
        }
        ProjectBrokerManagerFactory.getProjectBrokerManager().deleteGroupDataFor(businessGroupTodelete);
        // 1) Delete all group properties
        final CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroupTodelete);
        ct.deleteTools(businessGroupTodelete);// deletes everything concerning properties&collabTools
        // 1.b)delete display member property
        final BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessGroupTodelete);
        bgpm.deleteDisplayMembers();
        // 2) Delete the group areas
        if (BusinessGroup.TYPE_LEARNINGROUP.equals(type)) {
            BGAreaDaoImpl.getInstance().deleteBGtoAreaRelations(businessGroupTodelete);
        }
        // 3) Delete the group object itself on the database
        businessGroupDAO.deleteBusinessGroup(businessGroupTodelete);
        // 4) Delete the associated security groups
        if (BusinessGroup.TYPE_BUDDYGROUP.equals(type) || BusinessGroup.TYPE_LEARNINGROUP.equals(type)) {
            final SecurityGroup owners = businessGroupTodelete.getOwnerGroup();
            securityManager.deleteSecurityGroup(owners);
        }
        // in all cases the participant groups
        final SecurityGroup partips = businessGroupTodelete.getPartipiciantGroup();
        securityManager.deleteSecurityGroup(partips);
        // Delete waiting-group when one exists
        if (businessGroupTodelete.getWaitingGroup() != null) {
            securityManager.deleteSecurityGroup(businessGroupTodelete.getWaitingGroup());
        }

        // delete potential jabber group roster
        if (InstantMessagingModule.isEnabled()) {
            final String groupID = InstantMessagingModule.getAdapter().createChatRoomString(businessGroupTodelete);
            InstantMessagingModule.getAdapter().deleteRosterGroup(groupID);
        }
        log.info("Audit:Deleted Business Group" + businessGroupTodelete.toString());

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#loadBusinessGroup(java.lang.Long, boolean)
     */
    @Override
    public BusinessGroup loadBusinessGroup(Long groupKey, boolean strict) {

        return businessGroupDAO.loadBusinessGroup(groupKey, strict);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#updateBusinessGroup(org.olat.data.group.BusinessGroup)
     */
    @Override
    public void updateBusinessGroup(BusinessGroup businessGroup) {
        businessGroupDAO.updateBusinessGroup(businessGroup);

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#addOwnersAndFireEvent(org.olat.data.basesecurity.Identity, java.util.List, org.olat.data.group.BusinessGroup,
     *      org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public BusinessGroupAddResponse addOwnersAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags) {

        final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
        for (final Identity identity : addIdentities) {
            currBusinessGroup = loadBusinessGroup(currBusinessGroup); // reload business group
            if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
                response.getIdentitiesWithoutPermission().add(identity);
            }
            // Check if identity is already in group. make a db query in case
            // someone in another workflow already added this user to this group. if
            // found, add user to model
            else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getOwnerGroup())) {
                response.getIdentitiesAlreadyInGroup().add(identity);
            } else {
                // identity has permission and is not already in group => add it
                addOwnerAndFireEvent(ureqIdentity, identity, currBusinessGroup, flags, false);
                response.getAddedIdentities().add(identity);
                log.info("Audit:added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getOwnerGroup().getKey());
            }
        }
        return response;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#addParticipantsAndFireEvent(org.olat.data.basesecurity.Identity, java.util.List, org.olat.data.group.BusinessGroup,
     *      org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public BusinessGroupAddResponse addParticipantsAndFireEvent(final Identity ureqIdentity, final List<Identity> addIdentities, BusinessGroup acurrBusinessGroup,
            final BGConfigFlags flags) {

        final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
        final BusinessGroup currBusinessGroup = loadBusinessGroup(acurrBusinessGroup); // reload business group
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                for (final Identity identity : addIdentities) {
                    if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
                        response.getIdentitiesWithoutPermission().add(identity);
                    }
                    // Check if identity is already in group. make a db query in case
                    // someone in another workflow already added this user to this group. if
                    // found, add user to model
                    else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getPartipiciantGroup())) {
                        response.getIdentitiesAlreadyInGroup().add(identity);
                    } else if (currBusinessGroup.getWaitingListEnabled() && securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getWaitingGroup())) {
                        response.getIdentitiesAlreadyInGroup().add(identity);
                    } else {
                        // identity has permission and is not already in group => add it
                        addParticipantAndFireEvent(ureqIdentity, identity, currBusinessGroup, flags, false);
                        response.getAddedIdentities().add(identity);
                        log.info("Audit:added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
                    }
                }
            }
        });
        return response;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeFromWaitingListAndFireEvent(org.olat.data.basesecurity.Identity, java.util.List,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public void removeFromWaitingListAndFireEvent(final Identity ureqIdentiy, final List<Identity> identities, final BusinessGroup currBusinessGroup,
            final BGConfigFlags flags) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                for (final Identity identity : identities) {
                    removeFromWaitingListAndFireEvent(ureqIdentiy, identity, currBusinessGroup, false);
                    log.info("Audit:removed identiy '" + identity.getName() + "' from securitygroup with key " + currBusinessGroup.getOwnerGroup().getKey());
                }
            }
        });
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#moveIdentitiesFromWaitingListToParticipant(java.util.List, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags)
     */
    @Override
    public BusinessGroupAddResponse moveIdentitiesFromWaitingListToParticipant(final List<Identity> chosenIdentities, final Identity ureqIdentity,
            final BusinessGroup currBusinessGroup, final BGConfigFlags flags) {

        final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                for (final Identity identity : chosenIdentities) {
                    // check if idenity is allready in participant
                    if (!securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getPartipiciantGroup())) {
                        // Idenity is not in participant-list => move idenity from waiting-list to participant-list
                        addParticipantAndFireEvent(ureqIdentity, identity, currBusinessGroup, flags, false);
                        removeFromWaitingListAndFireEvent(ureqIdentity, identity, currBusinessGroup, false);
                        response.getAddedIdentities().add(identity);
                        // notification mail is handled in controller
                    } else {
                        response.getIdentitiesAlreadyInGroup().add(identity);
                    }
                }
            }
        });
        return response;
    }

    public void moveIdenitiesFromWaitingListToParticipant(final Identity ureqIdentity, final BusinessGroup currBusinessGroup, final BGConfigFlags flags) {
        currBusinessGroup.getPartipiciantGroup();
        securityManager.getIdentitiesAndDateOfSecurityGroup(currBusinessGroup.getWaitingGroup());

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#setLastUsageFor(org.olat.data.group.BusinessGroup)
     */
    @Override
    public void setLastUsageFor(BusinessGroup currBusinessGroup) {
        businessGroupDAO.setLastUsageFor(currBusinessGroup);
    }

    @Override
    public Set<BusinessGroup> createUniqueBusinessGroupsFor(final Set<String> allNames, final BGContext bgContext, final String bgDesc, final Integer bgMin,
            final Integer bgMax, final Boolean enableWaitinglist, final Boolean enableAutoCloseRanks) {
        // o_clusterOK by:cg
        final Set<BusinessGroup> createdGroups = CoordinatorManager.getInstance().getCoordinator().getSyncer()
                .doInSync(bgContext, new SyncerCallback<Set<BusinessGroup>>() {
                    @Override
                    public Set<BusinessGroup> execute() {
                        if (businessGroupDAO.checkIfOneOrMoreNameExistsInContext(allNames, bgContext)) {
                            // set error of non existing name
                            return null;
                        } else {
                            // create bulkgroups only if there is no name which already exists.
                            final Set<BusinessGroup> newGroups = new HashSet<BusinessGroup>();
                            for (final Iterator<String> iter = allNames.iterator(); iter.hasNext();) {
                                final String bgName = iter.next();
                                final BusinessGroup newGroup = createAndPersistBusinessGroup(bgContext.getGroupType(), null, bgName, bgDesc, bgMin, bgMax,
                                        enableWaitinglist, enableAutoCloseRanks, bgContext);
                                newGroups.add(newGroup);
                            }
                            return newGroups;
                        }
                    }
                });
        return createdGroups;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#checkIfOneOrMoreNameExistsInContext(java.util.Set, org.olat.data.group.context.BGContext)
     */
    @Override
    public boolean checkIfOneOrMoreNameExistsInContext(Set names, BGContext groupContext) {
        return businessGroupDAO.checkIfOneOrMoreNameExistsInContext(names, groupContext);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#addParticipantAndFireEvent(org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, org.olat.lms.group.BGConfigFlags, boolean)
     */
    @Override
    public void addParticipantAndFireEvent(final Identity ureqIdentity, final Identity identity, final BusinessGroup group, final BGConfigFlags flags,
            final boolean doOnlyPostAddingStuff) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        if (!doOnlyPostAddingStuff) {
            securityManager.addIdentityToSecurityGroup(identity, group.getPartipiciantGroup());
        }
        // add user to buddies rosters
        addToRoster(ureqIdentity, identity, group, flags);
        // notify currently active users of this business group
        BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(identity));
        // send notification mail in your controller!
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#deleteBusinessGroups(java.util.List)
     */
    @Override
    public void deleteBusinessGroups(List<BusinessGroup> groups) {
        for (BusinessGroup group : groups) {
            deleteBusinessGroup(group);
        }

    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#registerDeletableGroupDataListener(org.olat.lms.group.DeletableGroupData)
     */
    @Override
    public void registerDeletableGroupDataListener(DeletableGroupData deletableGroupData) {
        this.deleteListeners.add(deletableGroupData);

    }

    @Override
    public List<Identity> getWaitingListFor(BusinessGroup businessGroup) {
        return securityManager.getIdentitiesOfSecurityGroup(businessGroup.getWaitingGroup(), true);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#getPositionInWaitingListFor(org.olat.data.basesecurity.Identity, org.olat.data.group.BusinessGroup)
     */
    @Override
    public int getPositionInWaitingListFor(final Identity identity, final BusinessGroup businessGroup) {
        // get position in waiting-list
        final List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(businessGroup.getWaitingGroup(), true);
        int pos = 0;
        for (int i = 0; i < waitingList.size(); i++) {
            if (waitingList.get(i).getName().equals(identity.getName())) {
                pos = i + 1;// '+1' because list begins with 0
            }
        }
        return pos;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#removeFromWaitingListAndFireEvent(org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Identity,
     *      org.olat.data.group.BusinessGroup, boolean)
     */
    @Override
    public void removeFromWaitingListAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, boolean doOnlyPostRemovingStuff) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        if (!doOnlyPostRemovingStuff) {
            securityManager.removeIdentityFromSecurityGroup(identity, group.getWaitingGroup());
        }
        // notify currently active users of this business group
        BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_FROM_WAITING_LIST_REMOVED, getClass(), LoggingResourceable.wrap(identity));
        // send notification mail in your controller!
    }

    /**
	 */
    @Override
    public void addToWaitingListAndFireEvent(final Identity ureqIdentity, final Identity identity, final BusinessGroup group, final boolean doOnlyPostAddingStuff) {
        CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
        if (!doOnlyPostAddingStuff) {
            securityManager.addIdentityToSecurityGroup(identity, group.getWaitingGroup());
        }
        // notify currently active users of this business group
        BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
        // do logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TO_WAITING_LIST_ADDED, getClass(), LoggingResourceable.wrap(identity));
        // send notification mail in your controller!
    }

    /**
	 */
    @Override
    public BusinessGroupAddResponse addToWaitingListAndFireEvent(final Identity ureqIdentity, final List<Identity> addIdentities, final BusinessGroup acurrBusinessGroup,
            final BGConfigFlags flags) {

        final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
        final BusinessGroup currBusinessGroup = loadBusinessGroup(acurrBusinessGroup); // reload business group
        CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                for (final Identity identity : addIdentities) {
                    if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
                        response.getIdentitiesWithoutPermission().add(identity);
                    }
                    // Check if identity is already in group. make a db query in case
                    // someone in another workflow already added this user to this group. if
                    // found, add user to model
                    else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getWaitingGroup())) {
                        response.getIdentitiesAlreadyInGroup().add(identity);
                    } else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getPartipiciantGroup())) {
                        response.getIdentitiesAlreadyInGroup().add(identity);
                    } else {
                        // identity has permission and is not already in group => add it
                        addToWaitingListAndFireEvent(ureqIdentity, identity, currBusinessGroup, false);
                        response.getAddedIdentities().add(identity);
                        log.info("Audit:added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
                    }
                }
            }
        });
        return response;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#getAllBusinessGroups()
     */
    @Override
    public List<BusinessGroup> getAllBusinessGroups() {
        return businessGroupDAO.getAllBusinessGroups();
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#getAllBusinessGroupIds()
     */
    @Override
    public List<Long> getAllBusinessGroupIds() {
        return businessGroupDAO.getAllBusinessGroupIds();
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#findBusinessGroupsWithWaitingListAttendedBy(java.lang.String, org.olat.data.basesecurity.Identity, java.lang.Object)
     */
    @Override
    public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(String type, Identity identity, BGContext bgContext) {
        return businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(type, identity, bgContext);
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#archiveGroups(org.olat.data.group.context.BGContext, java.io.File)
     */
    @Override
    public void archiveGroups(final BGContext context, final File exportFile) {
        businessGroupArchiver.archiveBGContext(context, exportFile);
    }

    @Override
    public void archiveGroup(final BusinessGroup businessGroup, final File archiveFile) {
        businessGroupArchiver.archiveGroup(businessGroup, archiveFile);
    }

    @Override
    public CleanupAfterDeliveryFileMediaResource archiveGroupMembers(final BGContext context, final List<String> columnList, final List<BusinessGroup> groupList,
            final String archiveType, final Locale locale, final String userCharset) {
        File archiveFile = businessGroupArchiver.archiveGroupMembers(context, columnList, groupList, archiveType, locale, userCharset);
        return new CleanupAfterDeliveryFileMediaResource(archiveFile);
    }

    @Override
    public CleanupAfterDeliveryFileMediaResource archiveAreaMembers(final BGContext context, final List<String> columnList, final List<BGArea> areaList,
            final String archiveType, final Locale locale, final String userCharset) {
        File archiveFile = businessGroupArchiver.archiveAreaMembers(context, columnList, areaList, archiveType, locale, userCharset);
        return new CleanupAfterDeliveryFileMediaResource(archiveFile);
    }

    @Override
    public void addBGContextToResource(final BGContext bgContext, final OLATResource resource) {
        businessGroupContextService.addBGContextToResource(bgContext, resource);
    }

    @Override
    public BGContext createAndAddBGContextToResource(final String contextName, final OLATResource resource, final String groupType, final Identity initialOwner,
            final boolean defaultContext) {
        return businessGroupContextService.createAndAddBGContextToResource(contextName, resource, groupType, initialOwner, defaultContext);
    }

    @Override
    public BGContext copyAndAddBGContextToResource(String contextName, OLATResource resource, BGContext originalBgContext) {
        return businessGroupContextService.copyAndAddBGContextToResource(contextName, resource, originalBgContext);
    }

    /**
     * Delete all entries as participant, owner and waiting-list for certain identity. If there is no other owner for a group, the olat-administrator (defined in spring
     * config) will be added as owner.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        // remove as Participant
        final List attendedGroups = businessGroupDAO.findAllBusinessGroupsAttendedBy(identity);
        for (final Iterator iter = attendedGroups.iterator(); iter.hasNext();) {
            securityManager.removeIdentityFromSecurityGroup(identity, ((BusinessGroup) iter.next()).getPartipiciantGroup());
        }
        log.debug("Remove partipiciant identity=" + identity + " from " + attendedGroups.size() + " groups");
        // remove from waitinglist
        final List waitingGroups = businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(identity);
        for (final Iterator iter = waitingGroups.iterator(); iter.hasNext();) {
            securityManager.removeIdentityFromSecurityGroup(identity, ((BusinessGroup) iter.next()).getWaitingGroup());
        }
        log.debug("Remove from waiting-list identity=" + identity + " in " + waitingGroups.size() + " groups");

        // remove as owner
        final List ownerGroups = businessGroupDAO.findAllBusinessGroupsOwnedBy(identity);
        for (final Iterator iter = ownerGroups.iterator(); iter.hasNext();) {
            final BusinessGroup businessGroup = (BusinessGroup) iter.next();
            securityManager.removeIdentityFromSecurityGroup(identity, businessGroup.getOwnerGroup());
            if (businessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP) && securityManager.countIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup()) == 0) {
                // Buddygroup has no owner anymore => add OLAT-Admin as owner
                securityManager.addIdentityToSecurityGroup(userDeletionManager.getAdminIdentity(), businessGroup.getOwnerGroup());
                log.info("Delete user-data, add Administrator-identity as owner of businessGroup=" + businessGroup.getName());
            }
        }
        log.debug("Remove owner identity=" + identity + " from " + ownerGroups.size() + " groups");
        log.debug("All entries in groups deleted for identity=" + identity);
    }

    private void addToRoster(final Identity ureqIdentity, final Identity identity, final BusinessGroup group, final BGConfigFlags flags) {
        if (flags.isEnabled(BGConfigFlags.BUDDYLIST)) {
            if (InstantMessagingModule.isEnabled()) {
                // evaluate whether to sync or not
                final boolean syncBuddy = InstantMessagingModule.getAdapter().getConfig().isSyncPersonalGroups();
                final boolean isBuddy = group.getType().equals(BusinessGroup.TYPE_BUDDYGROUP);

                final boolean syncLearn = InstantMessagingModule.getAdapter().getConfig().isSyncLearningGroups();
                final boolean isLearn = group.getType().equals(BusinessGroup.TYPE_LEARNINGROUP);

                // only sync when a group is a certain type and this type is configured that you want to sync it
                if ((syncBuddy && isBuddy) || (syncLearn && isLearn)) {
                    final String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
                    final String groupDisplayName = group.getName();
                    // course group enrolment is time critial so we move this in an separate thread and catch all failures
                    taskExecutorService.runTask(new SyncSingleUserTask(ureqIdentity, groupID, groupDisplayName, identity));
                }
            }
        }
    }

    /**
     * 
     * @param secGroup
     * @return
     */
    @Override
    public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
        return businessGroupDAO.findBusinessGroup(secGroup);
    }

    @Override
    public void exportGroup(BusinessGroup group, File fExportFile) {
        groupImporterExporter.exportGroup(group, fExportFile);

    }

    @Override
    public void exportGroups(BGContext context, File fExportFile) {
        groupImporterExporter.exportGroups(context, fExportFile);

    }

    /**
	 */
    @Override
    public boolean isIdentityInBusinessGroup(final Identity identity, final BusinessGroup businessGroup) {
        final SecurityGroup participants = businessGroup.getPartipiciantGroup();
        final SecurityGroup owners = businessGroup.getOwnerGroup();
        if (participants != null) {
            if (securityManager.isIdentityInSecurityGroup(identity, participants)) {
                return true;
            }
        }
        if (owners != null) {
            if (securityManager.isIdentityInSecurityGroup(identity, owners)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.olat.lms.group.BusinessGroupService#isIdentityInBusinessGroup(org.olat.data.basesecurity.Identity, java.lang.String,
     *      org.olat.data.group.context.BGContext)
     */
    @Override
    public boolean isIdentityInBusinessGroup(Identity identity, String groupName, BGContext context) {
        return businessGroupDAO.isIdentityInBusinessGroup(identity, groupName, context);
    }

    @Override
    public BusinessGroup loadBusinessGroup(BusinessGroup businessGroup, boolean forceReloadFromDB) {
        return businessGroupDAO.loadBusinessGroup(businessGroup, forceReloadFromDB);
    }

    @Override
    public RepositoryEntry getCourseRepositoryEntryForBusinessGroup(BusinessGroup group) {
        if (group.getGroupContext() == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final List<RepositoryEntry> repositoryEntries = businessGroupContextService.findRepositoryEntriesForBGContext(group.getGroupContext());
        if (repositoryEntries.isEmpty()) {
            return null;
        }
        // TODO: is possible to have more elements?
        return repositoryEntries.get(0);
    }

    @Override
    public Project getProjectForBusinessGroup(Long businessGroupId) {
        List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().findProjectsForGoup(businessGroupId);
        /** TODO: actually it is 1:n mapping but currently it is implemented as 1:1 mapping */
        if (projects.isEmpty()) {
            return null;
        }
        return projects.get(0);
    }

}
