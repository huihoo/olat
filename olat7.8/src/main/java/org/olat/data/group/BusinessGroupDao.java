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

package org.olat.data.group;

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.context.BGContext;

/**
 * Description: <br>
 * <b>Workflows within BusinessGroupManager: </b>
 * <ul>
 * <i><b>create a Buddy Group: </b> <br>
 * a new buddy group instance is created, made persistent. Owner is the <code>Identity</code>, which triggered the creation. The Buddy Group must be initialised with a
 * title and Description, null is not allowed. </i> <i> <b>find Buddy Groups: </b> <br>
 * find all the Buddy Groups associated with the given Identity. </i> <i> <b>update a Buddy Group: </b> <br>
 * </ul>
 * Initial Date: Jul 27, 2004
 * 
 * @author patrick
 */

public interface BusinessGroupDao {

    /*
     * NOTES: find(...) -> may return empty list find used if subsequent steps follow, i.e. choosing get(...) -> one XXXobject, or null fetches object, which is directly
     * used further.
     */

    /**
     * find the BusinessGroups list of type <code>type</code> associated with the supplied identity, where the identity is an Owner.
     * 
     * @param type
     *            Restrict find to this group type or null if not restricted to a specific type
     * @param identity
     * @param bgContext
     *            Context or null if no context restriction should be applied
     * @return list of BusinessGroups, may be an empty list.
     */
    public List<BusinessGroup> findBusinessGroupsOwnedBy(String type, Identity identity, BGContext bgContext);

    /**
     * find the list of BuddyGroups associated with the supplied identity, where the identity is a Participiant.
     * 
     * @param type
     *            Restrict find to this group type or null if not restricted to a specific type
     * @param identity
     * @param bgContext
     *            Context or null if no context restriction should be applied
     * @return list of BuddyGroups, may be an empty list.
     */
    public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, BGContext bgContext);

    /**
     * @param currBusinessGroup
     * @return The group or null if not found
     */
    public BusinessGroup findBusinessGroup(SecurityGroup secGroup);

    public List<BusinessGroup> findAllBusinessGroupsOwnedBy(final Identity identity);

    public List<BusinessGroup> findAllBusinessGroupsAttendedBy(final Identity identity);

    public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(final Identity identity);

    /**
     * commit the changes on a BusinessGroup instance to the persistence store
     * 
     * @param updatedBusinessGroup
     */
    public void updateBusinessGroup(BusinessGroup updatedBusinessGroup);

    /**
     * delete a businessgroup from the persistence store
     * 
     * @param businessGroupTodelete
     */
    public void deleteBusinessGroup(BusinessGroup businessGroupTodelete);

    /**
     * delete all business groups from this list
     * 
     * @param businessGroups
     */
    public void deleteBusinessGroups(List businessGroups);

    /**
     * Checks if an identity is in a specific business group, either as owner or as participant
     * 
     * @param identity
     *            The Identity
     * @param groupName
     *            The group name
     * @param groupContext
     *            The group context or null if group does not belong to a group context (e.g. buddygroups)
     * @return true if identity is in group, false otherwise
     */
    public boolean isIdentityInBusinessGroup(Identity identity, String groupName, BGContext groupContext);

    /**
     * @param currBusinessGroup
     * @return The reloaded group
     */
    public BusinessGroup loadBusinessGroup(BusinessGroup currBusinessGroup);

    /**
     * @param groupKey
     *            The group database key
     * @param strict
     *            true: will throw exception if load failed false: will return null if not found
     * @return THe loaded group
     */
    public BusinessGroup loadBusinessGroup(Long groupKey, boolean strict);

    public BusinessGroup loadBusinessGroup(BusinessGroup businessGroup, boolean forceReloadFromDB);

    /**
     * Find all business-groups where the idenity is on the waiting-list.
     * 
     * @param groupType
     * @param identity
     * @param bgContext
     * @return List of BusinessGroup objects
     */
    public List findBusinessGroupsWithWaitingListAttendedBy(String groupType, Identity identity, BGContext bgContext);

    /**
     * Get all business-groups.
     * 
     * @return List of BusinessGroup objects
     */
    public List<BusinessGroup> getAllBusinessGroups();

    /**
     * @return List of BusinessGroup IDs
     */
    public List<Long> getAllBusinessGroupIds();

    /**
     * Set certain business-group as active (set last-usage and delete time stamp for 'SEND_DELETE_EMAIL_ACTION' in LifeCycleManager):
     * 
     * @param currBusinessGroup
     */
    public void setLastUsageFor(BusinessGroup currBusinessGroup);

    public boolean checkIfOneOrMoreNameExistsInContext(final Set names, final BGContext groupContext);

    /**
     * @param name
     * @param type
     * @param groupContext
     * @return
     */
    boolean testIfGroupAlreadyExists(String name, String type, BGContext groupContext);

}
