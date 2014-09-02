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
package org.olat.data.repository;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.resource.OLATResource;
import org.olat.system.exception.AssertException;

/**
 * RepositoryDao
 * 
 * <P>
 * Initial Date: 11.07.2011 <br>
 * 
 * @author guido
 */
public interface RepositoryDao {

    /**
     * @param initialAuthor
     * @return A repository instance which has not been persisted yet.
     */
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor);

    /**
     * @param initialAuthor
     * @param resourceName
     * @param description
     * @return A repository instance which has not been persisted yet, initialized with given data.
     */
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor, final String resourceName, final String description);

    /**
     * Save repo entry.
     * 
     * @param re
     */
    public void saveRepositoryEntry(final RepositoryEntry re);

    /**
     * Update repo entry.
     * 
     * @param re
     */
    public void updateRepositoryEntry(final RepositoryEntry re);

    /**
     * Delete repo entry.
     * 
     * @param re
     */
    public void deleteRepositoryEntry(RepositoryEntry re);

    /**
     * Lookup repo entry by key.
     * 
     * @param the
     *            repository entry key (not the olatresourceable key)
     * @return Repo entry represented by key or null if no such entry or key is null.
     */
    public RepositoryEntry lookupRepositoryEntry(final Long key);

    /**
     * Lookup the repository entry which references the given olat resourceable.
     * 
     * @param resourceable
     * @param strict
     *            true: throws exception if not found, false: returns null if not found
     * @return the RepositorEntry or null if strict=false
     * @throws AssertException
     *             if the softkey could not be found (strict=true)
     */
    public RepositoryEntry lookupRepositoryEntry(final OLATResource resource, final boolean strict);

    /**
     * Lookup a repository entry by its softkey.
     * 
     * @param softkey
     * @param strict
     *            true: throws exception if not found, false: returns null if not found
     * @return the RepositorEntry or null if strict=false
     * @throws AssertException
     *             if the softkey could not be found (strict=true)
     */
    public RepositoryEntry lookupRepositoryEntryBySoftkey(final String softkey, final boolean strict);

    /**
     * Convenience method to access the repositoryEntry displayname by the referenced OLATResourceable id. This only works if a repository entry has an referenced olat
     * resourceable like a course or an content package repo entry
     * 
     * @param resId
     * @return the repositoryentry displayname or null if not found
     */
    public String lookupDisplayNameByOLATResourceableId(final Long resId);

    /**
     * Count by type, limit by role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Number of repo entries
     */
    public int countByTypeLimitAccess(final String restrictedType, final int restrictedAccess);

    /**
     * Query by type without any other limitations
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    public List queryByType(final String restrictedType);

    /**
     * Query by type, limit by ownership or role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    public List queryByTypeLimitAccess(final String restrictedType, final Roles roles);

    /**
     * Query by type, limit by ownership or role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    public List queryByTypeLimitAccess(final String restrictedType, String institution, Roles roles);

    public List queryByOwner(final Identity identity, final String[] limitTypes);

    /**
     * Query by initial-author
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    public List queryByInitialAuthor(final String initialAuthor);

    /**
     * Search for resources that can be referenced by an author. This is the case: 1) the user is the owner of the resource 2) the user is author and the resource is at
     * least visible to authors (BA) and the resource is set to canReference
     * 
     * @param identity
     *            The user initiating the query
     * @param roles
     *            The current users role set
     * @param resourceTypes
     *            Limit search result to this list of repo types. Can be NULL
     * @param displayName
     *            Limit search to this repo title. Can be NULL
     * @param author
     *            Limit search to this user (Name, firstname, loginname). Can be NULL
     * @param desc
     *            Limit search to description. Can be NULL
     * @return List of repository entries
     */
    public List queryReferencableResourcesLimitType(final Identity identity, final Roles roles, List resourceTypes, String displayName, String author, String desc);

    /**
     * Query by ownership, limit by access.
     * 
     * @param identity
     * @param limitAccess
     * @return Results
     */
    public List queryByOwnerLimitAccess(final Identity identity, final int limitAccess);

    /**
     * Query repository If any input data contains "*", then it replaced by "%" (search me*er -> sql: me%er).
     * 
     * @param ureq
     * @param displayName
     *            null -> no restriction
     * @param author
     *            null -> no restriction
     * @param desc
     *            null -> no restriction
     * @param resourceTypes
     *            NOTE: for null -> no restriction, or a list of resourceTypeNames
     * @param roles
     *            The calling user's roles
     * @param institution
     *            null -> no restriction
     * @return Results as List containing RepositoryEntries
     */
    public List<RepositoryEntry> genericANDQueryWithRolesRestriction(String displayName, String author, String desc, final List resourceTypes, final Roles roles,
            final String institution);

    public RepositoryEntry loadRepositoryEntry(RepositoryEntry repositoryEntry);

    /**
     * Special-query for Upgrade-6.2.0.
     */
    public List<RepositoryEntry> getAllRepositoryEntries();

}
