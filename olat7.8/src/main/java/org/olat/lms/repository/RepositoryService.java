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
package org.olat.lms.repository;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: 04.04.2012 <br>
 * 
 * @author guretzki
 */
public interface RepositoryService {

    public abstract RepositoryEntry createRepositoryEntryInstance(final String initialAuthor);

    public abstract RepositoryEntry createRepositoryEntryInstance(final String initialAuthor, final String resourceName, final String description);

    /**
     * @param repositoryEntryStatusCode
     */
    public abstract RepositoryEntryStatus createRepositoryEntryStatus(final int repositoryEntryStatusCode);

    public abstract void saveRepositoryEntry(final RepositoryEntry re);

    public abstract void updateRepositoryEntry(final RepositoryEntry re);

    public abstract void deleteRepositoryEntry(RepositoryEntry re);

    /**
     * @param addedEntry
     */
    public abstract void deleteRepositoryEntryAndBasesecurity(RepositoryEntry entry);

    /**
     * clean up a repo entry with all children and associated data like bookmarks and user references to it
     * 
     * @param ureq
     * @param wControl
     * @param entry
     * @return FIXME: we need a delete method without ureq, wControl for manager use. In general, very bad idea to pass ureq and wControl down to the manger layer.
     */
    public abstract boolean deleteRepositoryEntryWithAllData(final UserRequest ureq, final WindowControl wControl, RepositoryEntry entry);

    public abstract RepositoryEntry lookupRepositoryEntry(final Long key);

    public abstract RepositoryEntry lookupRepositoryEntry(final OLATResourceable resourceable, final boolean strict);

    public abstract RepositoryEntry lookupRepositoryEntryBySoftkey(final String softkey, final boolean strict);

    public abstract String lookupDisplayNameByOLATResourceableId(final Long resId);

    /**
     * Test a repo entry if identity is allowed to launch.
     * 
     * @param identity
     * @param roles
     * @param re
     * @return True if current identity is allowed to launch the given repo entry.
     */
    public abstract boolean isAllowedToLaunch(final Identity identity, final Roles roles, final RepositoryEntry re);

    /**
     * Increment the launch counter.
     * 
     * @param re
     */
    public abstract void incrementLaunchCounter(final RepositoryEntry re);

    /**
     * Increment the download counter.
     * 
     * @param re
     */
    public abstract void incrementDownloadCounter(final RepositoryEntry re);

    /**
     * Set last-usage date to to now for certain repository-entry.
     * 
     * @param
     */
    public abstract void setLastUsageNowFor(final RepositoryEntry re);

    public abstract void setAccess(final RepositoryEntry re, final int access);

    public abstract void setDescriptionAndName(final RepositoryEntry re, final String displayName, final String description);

    public abstract void setProperties(final RepositoryEntry re, final boolean canCopy, final boolean canReference, final boolean canLaunch, final boolean canDownload);

    public abstract int countByTypeLimitAccess(final String restrictedType, final int restrictedAccess);

    public abstract List queryByType(final String restrictedType);

    public abstract List queryByTypeLimitAccess(final String restrictedType, final Roles roles);

    public abstract List queryByTypeLimitAccess(final String restrictedType, final UserRequest ureq, String institution);

    /**
     * Query by ownership, optionally limit by type.
     * 
     * @param identity
     * @param limitType
     * @return Results
     */
    public abstract List queryByOwner(final Identity identity, final String limitType);

    public abstract List queryByOwner(final Identity identity, final String[] limitTypes);

    public abstract List queryByInitialAuthor(final String initialAuthor);

    public abstract List queryReferencableResourcesLimitType(final Identity identity, final Roles roles, List resourceTypes, String displayName, String author,
            String desc);

    public abstract List queryByOwnerLimitAccess(final Identity identity, final int limitAccess);

    /**
     * check ownership of identiy for a resource
     * 
     * @return true if the identity is member of the security group of the repository entry
     */
    public abstract boolean isOwnerOfRepositoryEntry(final Identity identity, final RepositoryEntry entry);

    public abstract List<RepositoryEntry> genericANDQueryWithRolesRestriction(String displayName, String author, String desc, final List resourceTypes,
            final Roles roles, final String institution);

    /**
     * add provided list of identities as owners to the repo entry. silently ignore if some identities were already owners before.
     * 
     * @param ureqIdentity
     * @param addIdentities
     * @param re
     * @param userActivityLogger
     */
    public abstract void addOwners(final Identity ureqIdentity, final IdentitiesAddEvent iae, final RepositoryEntry re);

    /**
     * remove list of identities as owners of given repository entry.
     * 
     * @param ureqIdentity
     * @param removeIdentities
     * @param re
     * @param logger
     */
    public abstract void removeOwners(final Identity ureqIdentity, final List<Identity> removeIdentities, final RepositoryEntry re);

    /**
     * has one owner of repository entry the same institution like the resource manager
     * 
     * @param RepositoryEntry
     *            repositoryEntry
     * @param Identity
     *            identity
     */
    public abstract boolean isInstitutionalRessourceManagerFor(final RepositoryEntry repositoryEntry, final Identity identity);

    /**
     * Gets all learning resources where the user is in a learning group as participant.
     * 
     * @param identity
     * @return list of RepositoryEntries
     */
    public abstract List<RepositoryEntry> getLearningResourcesAsStudent(final Identity identity);

    /**
     * Gets all learning resources where the user is coach of a learning group or where he is in a rights group or where he is in the repository entry owner group (course
     * administrator)
     * 
     * @param identity
     * @return list of RepositoryEntries
     */
    public abstract List<RepositoryEntry> getLearningResourcesAsTeacher(final Identity identity);

    public abstract RepositoryEntry updateDisplaynameDescriptionOfRepositoryEntry(final RepositoryEntry repositoryEntry);

    /**
     * @param displayName
     * @param description
     */
    public abstract RepositoryEntry updateNewRepositoryEntry(final RepositoryEntry repositoryEntry);

    /* STATIC_METHOD_REFACTORING moved from RepositoryEntryImageController */
    /**
     * Copy the repo entry image from the source to the target repository entry. If the source repo entry does not exists, nothing will happen
     * 
     * @param src
     * @param target
     * @return
     */
    public abstract boolean copyImage(final RepositoryEntry src, final RepositoryEntry target);

    /* STATIC_METHOD_REFACTORING moved from RepositoryEntryImageController */
    /**
     * Check if the repo entry does have an images and if yes create an image component that displays the image of this repo entry.
     * 
     * @param componentName
     * @param repositoryEntry
     * @return The image component or NULL if the repo entry does not have an image
     */
    public abstract ImageComponent getImageComponentForRepositoryEntry(final String componentName, final RepositoryEntry repositoryEntry);

    /**
     * attach object to Hibernate session
     * 
     * @param repositoryEntry
     * @return attached Hibernate object
     */
    public abstract RepositoryEntry loadRepositoryEntry(final RepositoryEntry repositoryEntry);

    public abstract Long getRepositoryEntryIdFromResourceable(Long resourceableId, String resourceableTypeName);

}
