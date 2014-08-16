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

package org.olat.data.group.context;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.resource.OLATResource;

/**
 * Description:<BR/>
 * Manager to manipulate business group contexts. A business group context is a collection of business groups of the same business group type and of business group areas
 * that area associated with the business groups. A business group context can be associated with multiple courses. Every course has at least two business group contexts,
 * the default learning group context and the default right group context
 * <p>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public interface BGContextDao {

    /**
     * Creates a busines group context object and persists the object in the database
     * 
     * @param name
     *            Display name of the group context
     * @param description
     * @param ownerGroup
     *            Business group type that this business group context can contain
     * @param groupType
     *            The initial owner, the users who can manage the business group context using the group context management tool
     * @param defaultContext
     *            true: create as a default context, false: create as a regular context
     * @return The persisted business group context
     */
    public BGContext createAndPersistBGContext(String name, String description, SecurityGroup ownerGroup, String groupType, boolean defaultContext);

    /**
     * Updates a business group context in the database
     * 
     * @param bgContext
     */
    public void updateBGContext(BGContext bgContext);

    /**
     * Deletes a business group context from the database
     * 
     * @param bgContext
     */
    public void deleteBGContext(BGContext bgContext);

    /**
     * Find all groups from a business group context
     * 
     * @param bgContext
     *            the business group context or null to find groups that are not within a business group context (e.b. buddygroups)
     * @return A list containing business group contexts
     */
    public List<BusinessGroup> getGroupsOfBGContext(BGContext bgContext);

    /**
     * Count the number of groups within a business group context
     * 
     * @param bgContext
     * @return The number of groups
     */
    public int countGroupsOfBGContext(BGContext bgContext);

    /**
     * Count the number of groups of a certain group type
     * 
     * @param groupType
     * @return
     */
    public int countGroupsOfType(String groupType);

    /**
     * Find the identities that are owners of any group in the given business group context
     * 
     * @param bgContext
     * @return A list of identities
     */
    public List getBGOwnersOfBGContext(BGContext bgContext);

    /**
     * Count the number of identities that are owner of any group in the given business group context
     * 
     * @param bgContext
     * @return The number of identities
     */
    public int countBGOwnersOfBGContext(BGContext bgContext);

    /**
     * Find the identities that are participants of any group in the given business group context
     * 
     * @param bgContext
     * @return A list of identities
     */
    public List getBGParticipantsOfBGContext(BGContext bgContext);

    /**
     * Count the number of identities that are participants of any group in the given business group context
     * 
     * @param bgContext
     * @return The number of identities
     */
    public int countBGParticipantsOfBGContext(BGContext bgContext);

    /**
     * Check if the given identity is in this business group context
     * 
     * @param identity
     * @param bgContext
     * @param asOwner
     *            Flag to check if the user is in any group as owner
     * @param asParticipant
     *            Flag to check if the user is in any group as participant
     * @return true if user is in any group with ghe given role, false otherwhise
     */
    public boolean isIdentityInBGContext(Identity identity, BGContext bgContext, boolean asOwner, boolean asParticipant);

    /**
     * Find a business group in the given business group context
     * 
     * @param groupName
     * @param bgContext
     * @return The business group or null if no group found
     */
    public BusinessGroup findGroupOfBGContext(String groupName, BGContext bgContext);

    /**
     * Find a business group in the given business group context where the given user is in the group as participant
     * 
     * @param identity
     * @param groupName
     * @param context
     * @return The business group or null if no group found
     */
    public BusinessGroup findGroupAttendedBy(Identity identity, String groupName, BGContext context);

    /**
     * Find all business group contexts for the given OLATResource defaultContexts and nonDefaultContexts can both be true or partly be true, but not be both false
     * 
     * @param resource
     * @param defaultContexts
     *            true: find default contexts
     * @param nonDefaultContexts
     *            true: find non-default contexts
     * @return A list of business group contexts
     */
    public List findBGContextsForResource(OLATResource resource, boolean defaultContexts, boolean nonDefaultContexts);

    /**
     * Find all business group contexts for the given OLATResource with the given group type defaultContexts and nonDefaultContexts can both be true or partly be true,
     * but not be both false
     * 
     * @param resource
     * @param groupType
     * @param defaultContexts
     *            true: find default contexts
     * @param nonDefaultContexts
     *            true: find non-default contexts
     * @return A list of business group contexts
     */
    public List findBGContextsForResource(OLATResource resource, String groupType, boolean defaultContexts, boolean nonDefaultContexts);

    /**
     * Find all business group contexts for a specific user. This will find all contexts where the user is in the owner group and all context where the user is in the
     * owner group of the olat resource that uses this context. defaultContexts and nonDefaultContexts can both be true or partly be true, but not be both false
     * 
     * @param identity
     * @param defaultContexts
     *            true: find default contexts
     * @param nonDefaultContexts
     *            true: find non-default contexts
     * @return A list of business group contexts
     */
    public List findBGContextsForIdentity(Identity identity, boolean defaultContexts, boolean nonDefaultContexts);

    /**
     * Find all OLATResources that are associated with the given business group context
     * 
     * @param bgContext
     * @return A list of OLATResources
     */
    public List findOLATResourcesForBGContext(BGContext bgContext);

    /**
     * Remove the given business group context from this OLATResource
     * 
     * @param bgContext
     * @param resource
     */
    public void removeBGContextFromResource(BGContext bgContext, OLATResource resource);

    /**
     * Refresh the given bgContext
     * 
     * @param bgContext
     * @return BGContext the updated context
     */
    public BGContext loadBGContext(BGContext bgContext);

    /**
     * @param resource
     * @param bgContext
     * @return
     */
    public BGContext2Resource getBGContext2ResourceAndSave(OLATResource resource, BGContext bgContext);

    /**
     * Special-query for Upgrade-6.2.0.
     */
    public List<BGContext> getAllBGContext();
}
