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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.GroupDeletionDao;
import org.olat.data.group.context.BGContext;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.notifications.Publisher;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.commons.change.ChangeManager;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.LockResult;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 05.10.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class BusinessGroupEBL {

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    BusinessGroupService businessGroupService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    CalendarService calendarService;
    @Autowired
    LockingService lockingService;

    /**
     * Removes user from businessGroup owner and participant group. If no other owner are found the user won't be removed from the owner group.
     * 
     * @param identity
     * @param businessGroup
     * @return
     */
    public boolean leaveBuddyGroup(Identity identity, BusinessGroup businessGroup) {
        boolean cannotRemoveLastOwner = false;
        final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
        // 1) remove as owner
        final SecurityGroup owners = businessGroup.getOwnerGroup();

        if (baseSecurity.isIdentityInSecurityGroup(identity, owners)) {
            final List ownerList = baseSecurity.getIdentitiesOfSecurityGroup(owners);
            if (ownerList.size() > 1) {
                businessGroupService.removeOwnerAndFireEvent(identity, identity, businessGroup, flags, false);
            } else {
                // he is the last owner, but there must be at least one oner
                // give him a warning, as long as he tries to leave, he gets
                // this warning.
                cannotRemoveLastOwner = true;
                return cannotRemoveLastOwner;
            }
        }
        // if identity was also owner it must have successfully removed to end here.
        // now remove the identity also as participant.
        // 2) remove as participant
        final List<Identity> identities = new ArrayList<Identity>(1);
        identities.add(identity);
        businessGroupService.removeParticipantsAndFireEvent(identity, identities, businessGroup, flags);
        return cannotRemoveLastOwner;
    }

    /**
     * deletes businessGroup, if and only if identity is in owner group, otherwise does nothing.
     * 
     * @param identity
     * @param businessGroup
     * @param ownersListTitle
     * @param participantsListTitle
     * @param subscriptionContext
     * @return
     */
    public MailerResult deleteBuddyGroup(Identity identity, BusinessGroup businessGroup, String ownersListTitle, String participantsListTitle,
            SubscriptionContext subscriptionContext) {
        final ContactList owners = new ContactList(ownersListTitle);
        final List ow = baseSecurity.getIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup());
        owners.addAllIdentites(ow);
        final ContactList participants = new ContactList(participantsListTitle);
        participants.addAllIdentites(baseSecurity.getIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup()));
        // check if user is in owner group (could fake link in table)

        if (!PersistenceHelper.listContainsObjectByKey(ow, identity)) {
            return null;
        }

        final List everybody = new ArrayList();
        everybody.add(owners);
        everybody.add(participants);
        // inform Indexer about change
        ChangeManager.changed(ChangeManager.ACTION_DELETE, businessGroup);
        // 3) delete the group
        businessGroup = businessGroupService.loadBusinessGroup(businessGroup);

        // change state of publisher so that notifications of deleted group calendars make no problems
        final Publisher pub = notificationService.getPublisher(subscriptionContext);
        if (pub != null) {
            pub.setState(1); // int 0 is OK -> all other is not OK
        }

        MailerResult mailerResult = businessGroupService.deleteBusinessGroupWithMail(businessGroup, everybody, identity);
        return mailerResult;
    }

    /**
     * try to acquire edit lock on business group
     * 
     * @param identity
     */
    public LockResult acquireGroupLockForEditing(Identity identity, BusinessGroup businessGroup) {
        final String locksubkey = "groupEdit";
        LockResult lockEntry = lockingService.acquireLock(businessGroup, identity, locksubkey);
        if (lockEntry.isSuccess()) {
            businessGroup.setLastUsage(new Date(System.currentTimeMillis()));
            LifeCycleManager.createInstanceFor(businessGroup).deleteTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);
            businessGroupService.updateBusinessGroup(businessGroup);
        }
        return lockEntry;
    }

    public void releaseGroupLock(LockResult lockEntry) {
        // release lock
        lockingService.releaseLock(lockEntry);
    }

    /**
     * @param waitingListEnabled
     * @param businessGroupTransferObject
     */
    public BusinessGroup updateBusinessGroup(final Boolean waitingListEnabled, BusinessGroupTransferObject businessGroupTransferObject, BusinessGroup businessGroup) {
        // refresh group to prevent stale object exception and context proxy issues
        BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(businessGroup);

        reloadedBusinessGroup.setName(businessGroupTransferObject.getName());
        reloadedBusinessGroup.setDescription(businessGroupTransferObject.getDescription());
        reloadedBusinessGroup.setMaxParticipants(businessGroupTransferObject.getMaxParticipants());
        reloadedBusinessGroup.setMinParticipants(businessGroupTransferObject.getMinParticipants());
        reloadedBusinessGroup.setWaitingListEnabled(businessGroupTransferObject.isWaitingListEnabled());
        if (waitingListEnabled.booleanValue() && (reloadedBusinessGroup.getWaitingGroup() == null)) {
            // Waitinglist is enabled but not created => Create waitingGroup
            final SecurityGroup waitingGroup = baseSecurity.createAndPersistSecurityGroup();
            reloadedBusinessGroup.setWaitingGroup(waitingGroup);
        }
        reloadedBusinessGroup.setAutoCloseRanksEnabled(businessGroupTransferObject.isCloseRanksEnabled());
        reloadedBusinessGroup.setLastUsage(businessGroupTransferObject.getLastUsageDate());
        LifeCycleManager.createInstanceFor(reloadedBusinessGroup).deleteTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);

        businessGroupService.updateBusinessGroup(reloadedBusinessGroup);
        return reloadedBusinessGroup;
    }

    public List<Identity> getParticipants(final BusinessGroup group) {
        List<Identity> participants = baseSecurity.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
        return participants;
    }

    public List<Identity> getOwners(final BusinessGroup group) {
        List<Identity> owners = baseSecurity.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
        return owners;
    }

    public boolean isOwner(final Identity identity, final BusinessGroup group) {
        return baseSecurity.isIdentityInSecurityGroup(identity, group.getOwnerGroup());
    }

    public boolean isWaiting(final Identity identity, final BusinessGroup businessGroup) {
        return baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getWaitingGroup());
    }

    public int countWaiting(final BusinessGroup businessGroup) {
        return baseSecurity.countIdentitiesOfSecurityGroup(businessGroup.getWaitingGroup());
    }

    public boolean isParticipant(final Identity identity, final BusinessGroup businessGroup) {
        return baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getPartipiciantGroup());
    }

    public boolean isContextOwnerOrAdmin(Identity identity, Roles roles, BGContext groupContext) {
        boolean isContextOwner = baseSecurity.isIdentityInSecurityGroup(identity, groupContext.getOwnerGroup());
        boolean isContextOwnerOrAdmin = isContextOwner || roles.isOLATAdmin();
        return isContextOwnerOrAdmin;
    }

    /**
     * TODO: move this to BaseSecurityEBL?
     */
    public boolean isResourceableOwner(Identity identity, OLATResourceable group) {
        boolean isOwner = false;
        if (group != null) {
            isOwner = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, group);
        }
        return isOwner;
    }

    public List<Identity> getWaitingIdentities(final BusinessGroup group) {
        List<Identity> identities = baseSecurity.getIdentitiesOfSecurityGroup(group.getWaitingGroup());
        return identities;
    }

    public List<Identity> getSelectedIdentities(final SecurityGroup securityGroup, List<Long> selectedIdentitiesKeys) {
        final List<Identity> memberList = baseSecurity.getIdentitiesOfSecurityGroup(securityGroup);
        final List<Identity> changeableMemberList = baseSecurity.getIdentitiesOfSecurityGroup(securityGroup);
        for (final Identity identity : memberList) {
            boolean keyIsSelected = false;
            for (final Long key : selectedIdentitiesKeys) {
                if (key.equals(identity.getKey())) {
                    keyIsSelected = true;
                    break;
                }
            }
            if (!keyIsSelected) {
                changeableMemberList.remove(changeableMemberList.indexOf(identity));
            }
        }
        return changeableMemberList;
    }

    public boolean isGroupManagementAdmin(final Identity identity, final BusinessGroup businessGroup, final BGConfigFlags flags) {
        final boolean isOwner = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, businessGroup);
        return isOwner || flags.isEnabled(BGConfigFlags.IS_GM_ADMIN);
    }

    /**
     * Check if an identity is in certain security-group.
     * 
     * @param businessGroup
     * @param identity
     * @return true: Found identity in PartipiciantGroup or WaitingGroup.
     */
    public boolean isEnrolledIn(final BusinessGroup businessGroup, final Identity identity) {
        if (baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getPartipiciantGroup())
                || baseSecurity.isIdentityInSecurityGroup(identity, businessGroup.getWaitingGroup())) {
            return true;
        }
        return false;
    }

    /**
     * TODO: LD: check this out! For some reason could not get to see if a user is only in waiting list.
     * 
     * @param identity
     * @return
     */
    public List<GroupMembershipParameter> getBusinessGroupMembership(Identity identity) {
        List<GroupMembershipParameter> groupMembershipParameters = new ArrayList<GroupMembershipParameter>();

        // loop over all kind of groups with all possible memberships
        final List<String> bgTypes = new ArrayList<String>();
        bgTypes.add(BusinessGroup.TYPE_BUDDYGROUP);
        bgTypes.add(BusinessGroup.TYPE_LEARNINGROUP);
        bgTypes.add(BusinessGroup.TYPE_RIGHTGROUP);
        for (final String bgType : bgTypes) {
            final List<BusinessGroup> ownedGroups = businessGroupService.findBusinessGroupsOwnedBy(bgType, identity, null);
            final List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroupsAttendedBy(bgType, identity, null);
            final List<BusinessGroup> waitingGroups = businessGroupService.findBusinessGroupsWithWaitingListAttendedBy(bgType, identity, null);
            // using HashSet to remove duplicate entries
            final HashSet<BusinessGroup> allGroups = new HashSet<BusinessGroup>();
            allGroups.addAll(ownedGroups);
            allGroups.addAll(attendedGroups);
            allGroups.addAll(waitingGroups);

            final Iterator<BusinessGroup> iter = allGroups.iterator();
            while (iter.hasNext()) {
                final BusinessGroup group = iter.next();
                Date joinDate = null;
                if (attendedGroups.contains(group) && ownedGroups.contains(group)) {
                    joinDate = baseSecurity.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
                    groupMembershipParameters.add(new GroupMembershipParameter(group.getType(), group, "attende.and.owner", joinDate));
                } else if (attendedGroups.contains(group)) {
                    joinDate = baseSecurity.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
                    groupMembershipParameters.add(new GroupMembershipParameter(group.getType(), group, "attende", joinDate));
                } else if (ownedGroups.contains(group)) {
                    joinDate = baseSecurity.getSecurityGroupJoinDateForIdentity(group.getOwnerGroup(), identity);
                    groupMembershipParameters.add(new GroupMembershipParameter(group.getType(), group, "owner", joinDate));
                } else if (waitingGroups.contains(group)) {
                    final int waitingListPosition = businessGroupService.getPositionInWaitingListFor(identity, group);
                    joinDate = baseSecurity.getSecurityGroupJoinDateForIdentity(group.getWaitingGroup(), identity);
                    GroupMembershipParameter groupMembershipParameter = new GroupMembershipParameter(group.getType(), group, "waiting", joinDate);
                    groupMembershipParameter.setRoleTranslationArgument(String.valueOf(waitingListPosition));
                    groupMembershipParameters.add(groupMembershipParameter);
                }
            }
        }
        return groupMembershipParameters;
    }

}
