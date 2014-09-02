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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.context.BGContext;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;

/**
 * TODO: Class Description for BusinessGroupService
 * 
 * <P>
 * Initial Date: 14.06.2011 <br>
 * 
 * @author guido
 */
public interface BusinessGroupService {

    /**
     * @param group
     * @return
     */
    public BusinessGroup loadBusinessGroup(BusinessGroup group);

    public BusinessGroup loadBusinessGroup(Long groupKey, boolean strict);

    public BusinessGroup loadBusinessGroup(BusinessGroup businessGroup, boolean forceReloadFromDB);

    /**
     * @param originalGroup
     * @param bgName
     * @param bgDesc
     * @param bgMin
     * @param bgMax
     * @param bgContext
     * @param object
     * @param copyAreas
     * @param copyTools
     * @param copyRights
     * @param copyOwners
     * @param copyParticipants
     * @param copyMembersVisibility
     * @param copyWaitingList
     * @return
     */
    BusinessGroup copyBusinessGroup(BusinessGroup sourceBusinessGroup, String targetName, String targetDescription, Integer targetMin, Integer targetMax,
            BGContext targetBgContext, Map areaLookupMap, boolean copyAreas, boolean copyCollabToolConfig, boolean copyRights, boolean copyOwners,
            boolean copyParticipants, boolean copyMembersVisibility, boolean copyWaitingList);

    /**
     * Remove a user from a group as owner and does all the magic that needs to be done: - remove from secgroup (optional) - remove from jabber roster - fire multi user
     * event
     * 
     * @param wControl
     * @param ureq
     *            the user request of the user who initiates the action
     * @param trans
     *            used for mail text
     * @param identity
     *            the user who should be removed
     * @param group
     * @param flags
     *            the group configuration flag
     * @param logger
     *            the user activity logger or null if nothing should be logged
     * @param doOnlyPostRemovingStuff
     *            true: user has already been removed from the security group, do only the other stuff, false: remove user from security group first
     */
    void removeOwnerAndFireEvent(Identity identity, Identity identity2, BusinessGroup currBusinessGroup, BGConfigFlags flags, boolean b);

    /**
     * @param identity
     * @param identities
     * @param currBusinessGroup
     * @param flags
     */
    void removeParticipantsAndFireEvent(Identity identity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * @param typeBuddygroup
     * @param identity
     * @param bgName
     * @param bgDesc
     * @param bgMin
     * @param bgMax
     * @param object
     * @param object2
     * @param object3
     * @return
     */
    BusinessGroup createAndPersistBusinessGroup(String typeBuddygroup, Identity identity, String bgName, String bgDesc, Integer bgMin, Integer bgMax,
            Boolean enableWaitinglist, Boolean enableAutoCloseRanks, BGContext groupContext);

    public void addOwnerAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags, boolean doOnlyPostAddingStuff);

    public BusinessGroup findBusinessGroup(SecurityGroup secGroup);

    public List<BusinessGroup> findBusinessGroupsOwnedBy(String type, Identity identity, BGContext bgContext);

    public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, BGContext bgContext);

    public List<String> getDependingDeletablableListFor(BusinessGroup currentGroup, Locale locale);

    public void deleteBusinessGroup(BusinessGroup businessGroupTodelete);

    public void deleteBusinessGroups(List<BusinessGroup> groups);

    /**
     * @param identity
     * @param businessGroup
     * @return true if the given identity is in one or both security groups (participants, owners)
     */
    public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup);

    /**
     * @param identity
     * @param groupName
     * @param context
     * @return
     */
    public boolean isIdentityInBusinessGroup(Identity identity, String groupName, BGContext context);

    /**
     * @param currBusinessGroup
     */
    public void updateBusinessGroup(BusinessGroup currBusinessGroup);

    /**
     * Add a list of identity as owner to a business-group.
     * 
     * @param identity
     * @param addIdentities
     * @param currBusinessGroup
     * @param flags
     * @return
     */
    public BusinessGroupAddResponse addOwnersAndFireEvent(Identity identity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * Add a list of identity as participant to a business-group.
     * 
     * @param identity
     * @param addIdentities
     * @param currBusinessGroup
     * @param flags
     * @return
     */
    public BusinessGroupAddResponse addParticipantsAndFireEvent(Identity identity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * Add a list of identity to waiting-list of a business-group.
     * 
     * @param ureqIdentity
     *            This identity triggered the method (typically identity of user-request).
     * @param addIdentities
     *            List of identity
     * @param currBusinessGroup
     *            Add list of identity to this business-group.
     * @param flags
     *            Business-group configuration flags.
     * @param userActivityLogger
     *            Use this logger to log event.
     * @return
     */
    public BusinessGroupAddResponse addToWaitingListAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * @param identity
     * @param identities
     * @param currBusinessGroup
     * @param flags
     */
    public void removeOwnersAndFireEvent(Identity identity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * @param identity
     * @param identities
     * @param currBusinessGroup
     * @param flags
     */
    public void removeFromWaitingListAndFireEvent(Identity identity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

    /**
     * Move users from a waiting-list to participant-list.
     * 
     * @param chosenIdentities
     * @param identity
     * @param currBusinessGroup
     * @param flags
     * @return
     */
    public BusinessGroupAddResponse moveIdentitiesFromWaitingListToParticipant(List<Identity> chosenIdentities, Identity identity, BusinessGroup currBusinessGroup,
            BGConfigFlags flags);

    /**
     * @param currBusinessGroup
     */
    public void setLastUsageFor(BusinessGroup currBusinessGroup);

    /**
     * @param allNames
     * @param bgContext
     * @param bgDesc
     * @param bgMin
     * @param bgMax
     * @param enableWaitinglist
     * @param enableAutoCloseRanks
     * @return
     */
    public Set<BusinessGroup> createUniqueBusinessGroupsFor(Set<String> allNames, BGContext bgContext, String bgDesc, Integer bgMin, Integer bgMax,
            Boolean enableWaitinglist, Boolean enableAutoCloseRanks);

    /**
     * @param group
     * @param fExportFile
     */
    void exportGroup(BusinessGroup group, File fExportFile);

    /**
     * @param context
     * @param fExportFile
     */
    void exportGroups(BGContext context, File fExportFile);

    /**
     * @param names
     * @param groupContext
     * @return
     */
    public boolean checkIfOneOrMoreNameExistsInContext(Set names, BGContext groupContext);

    /**
     * Adds a user to a group as participant and does all the magic that needs to be done: - add to secgroup (optional) - add to jabber roster - fire multi user event
     * 
     * @param actionIdentity
     * @param identity
     * @param projectGroup
     * @param flags
     * @param b
     */
    public void addParticipantAndFireEvent(Identity actionIdentity, Identity identity, BusinessGroup projectGroup, BGConfigFlags flags, boolean b);

    /**
     * Extension-point method to register objects which have deletable group-data. Listener will be called in method deleteBusinessGroup.
     */
    public void registerDeletableGroupDataListener(DeletableGroupData deletableGroupData);

    /**
     * @param businessGroup
     * @return list of identities on waiting list (sorted by date added to it).
     */
    public List<Identity> getWaitingListFor(BusinessGroup businessGroup);

    /**
     * @param identity
     * @param businessGroup
     * @return
     */
    public int getPositionInWaitingListFor(Identity identity, BusinessGroup businessGroup);

    /**
     * @param identity
     * @param identity2
     * @param enrolledGroup
     * @param flags
     * @param b
     */
    public void removeParticipantAndFireEvent(Identity identity, Identity identity2, BusinessGroup enrolledGroup, BGConfigFlags flags, boolean b);

    /**
     * Remove a user from a waiting-list as participant and does all the magic that needs to be done: - remove from secgroup (optional) send notification email - fire
     * multi user event
     * 
     * @param identity
     * @param identity2
     * @param enrolledWaitingListGroup
     * @param b
     */
    public void removeFromWaitingListAndFireEvent(Identity identity, Identity identity2, BusinessGroup enrolledWaitingListGroup, boolean b);

    /**
     * Adds a user to a waiting-list of a group and does all the magic that needs to be done: - add to secgroup (optional) - add to jabber roster - send notification
     * email - fire multi user event
     * 
     * @param identity
     * @param identity2
     * @param group
     * @param b
     */
    public void addToWaitingListAndFireEvent(Identity identity, Identity identity2, BusinessGroup group, boolean b);

    /**
     * @return
     */
    public List<BusinessGroup> getAllBusinessGroups();

    public List<Long> getAllBusinessGroupIds();

    /**
     * @param bgType
     * @param identity
     * @param object
     * @return
     */
    public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(String bgType, Identity identity, BGContext bgContext);

    /**
     * @param context
     * @param file
     */
    public void archiveGroups(BGContext context, File file);

    /**
     * 
     * @param businessGroup
     * @param archiveFile
     */
    public void archiveGroup(final BusinessGroup businessGroup, final File archiveFile);

    /**
     * 
     * @param context
     * @param columnList
     * @param groupList
     * @param archiveType
     * @param ureq
     * @return
     */
    public CleanupAfterDeliveryFileMediaResource archiveGroupMembers(final BGContext context, final List<String> columnList, final List<BusinessGroup> groupList,
            final String archiveType, final Locale locale, final String userCharset);

    /**
     * 
     * @param context
     * @param columnList
     * @param areaList
     * @param archiveType
     * @param ureq
     * @return
     */
    public CleanupAfterDeliveryFileMediaResource archiveAreaMembers(final BGContext context, final List<String> columnList, final List<BGArea> areaList,
            final String archiveType, final Locale locale, final String userCharset);

    /**
     * @param bgContext
     * @param resource
     */
    void addBGContextToResource(BGContext bgContext, OLATResource resource);

    /**
     * @param contextName
     * @param resource
     * @param groupType
     * @param initialOwner
     * @param defaultContext
     * @return
     */
    public BGContext createAndAddBGContextToResource(String contextName, OLATResource resource, String groupType, Identity initialOwner, boolean defaultContext);

    /**
     * 
     * @param contextName
     * @param resource
     * @param originalBgContext
     * @return
     */
    public BGContext copyAndAddBGContextToResource(final String contextName, final OLATResource resource, final BGContext originalBgContext);

    /**
     * delete group and send email to all group members
     * 
     * @param businessGroupTodelete
     * @param wControl
     * @param mailLocale
     * @param trans
     * @param contactLists
     * @param mailIdentity
     */
    void deleteBusinessGroupWithMail(BusinessGroup businessGroupTodelete, List contactLists, Identity mailIdentity);

    RepositoryEntry getCourseRepositoryEntryForBusinessGroup(BusinessGroup group);

    Project getProjectForBusinessGroup(Long businessGroupId);

}
