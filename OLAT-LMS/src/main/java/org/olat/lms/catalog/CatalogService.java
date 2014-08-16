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
package org.olat.lms.catalog;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.catalog.CatalogEntry;
import org.olat.data.repository.RepositoryEntry;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * TODO: Class Description for CatalogService
 * 
 * <P>
 * Initial Date: 11.05.2011 <br>
 * 
 * @author guretzki
 */
public interface CatalogService {

    /**
     * Default value for the catalog root <code>CATALOGROOT</code>
     */
    public static final String CATALOGROOT = "CATALOG ROOT";
    /**
     * Resource identifyer for catalog entries
     */
    public static final String CATALOGENTRY = "CatalogEntry";

    /**
     * @return transient catalog entry object
     */
    public abstract CatalogEntry createCatalogEntry();

    /**
     * @param toBeAddedEntry
     * @param parentNodeIdent
     * @return
     */
    public abstract CatalogEntry createCatalogEntryLeaf(RepositoryEntry toBeAddedEntry, String parentNodeIdent);

    /**
     * @param re
     * @param currentCatalogEntry
     * @return
     */
    public abstract CatalogEntry createCatalogEntryLeafAndAddParent(RepositoryEntry re, CatalogEntry parentCatalogEntry);

    /**
     * Children of this CatalogEntry as a list of CatalogEntries
     * 
     * @param ce
     * @return List of catalog entries that are childern entries of given entry
     */
    public abstract List<CatalogEntry> getChildrenOf(final CatalogEntry ce);

    /**
     * Returns a list catalog categories
     * 
     * @return List of catalog entries of type CatalogEntry.TYPE_NODE
     */
    public abstract List<CatalogEntry> getAllCatalogNodes();

    /**
     * Checks if the given catalog entry has any child of the given type. The query will be cached.
     * 
     * @param ce
     * @param type
     *            CatalogEntry.TYPE_LEAF or CatalogEntry.TYPE_NODE
     * @return true: entry has at least one child of type node
     */
    public abstract boolean hasChildEntries(final CatalogEntry ce, final int type);

    /**
     * Filters all catalog entries of type leaf that are owned by the given user
     * 
     * @param identity
     * @param catalogEntries
     *            List of catalog entries to be filtered
     * @return List of catalog entries
     */
    public abstract List filterOwnedLeafs(final Identity identity, final List catalogEntries);

    /**
     * Reload the given catalog entry from db or from hibernate second level cache
     * 
     * @param catalogEntry
     * @return reloaded catalog entry
     */
    public abstract CatalogEntry loadCatalogEntry(final CatalogEntry catalogEntry);

    /**
     * Load the catalog entry by the given ID
     * 
     * @param catEntryId
     * @return
     */
    public abstract CatalogEntry loadCatalogEntry(final Long catEntryId);

    /**
     * persist catalog entry
     * 
     * @param ce
     */
    public abstract void saveCatalogEntry(final CatalogEntry ce);

    /**
     * update catalog entry on db
     * 
     * @param ce
     */
    public abstract void updateCatalogEntry(final CatalogEntry ce);

    /**
     * delete a catalog entry and a potentially referenced substructure from db. Be aware of how to use this deletion, as all the referenced substructure is deleted.
     * 
     * @param ce
     */
    public abstract void deleteCatalogEntry(final CatalogEntry ce);

    /**
     * find all catalog categorie that the given repository entry is a child of
     * 
     * @param repoEntry
     * @return List of catalog entries
     */
    public abstract List getCatalogCategoriesFor(final RepositoryEntry repoEntry);

    /**
     * Find catalog entries for certain identity
     * 
     * @param name
     * @return List of catalog entries
     */
    public abstract List getCatalogEntriesOwnedBy(final Identity identity);

    /**
     * add a catalog entry to the specified parent
     * 
     * @param parent
     * @param newEntry
     */
    public abstract void addCatalogEntry(final CatalogEntry parent, final CatalogEntry newEntry);

    /**
     * Find all CatalogEntries which can act as catalog roots. Frankly speaking only one is found up to now, but for later stages one can think of getting more such
     * roots. An empty list indicates an error.
     * 
     * @return List of catalog entries
     */
    public abstract List getRootCatalogEntries();

    /**
     * Move the given catalog entry to the new parent
     * 
     * @param toBeMovedEntry
     * @param newParentEntry
     *            return true: success; false: failure
     */
    public abstract boolean moveCatalogEntry(CatalogEntry toBeMovedEntry, CatalogEntry newParentEntry);

    /**
     * @param repositoryEntry
     */
    public abstract void resourceableDeleted(final RepositoryEntry repositoryEntry);

    /**
     * Remove identity as owner of catalog-entry. If there is no other owner, the olat-administrator (define in spring config) will be added as owner.
     * 
     */
    public abstract void deleteUserData(final Identity identity, final String newDeletedUserName);

    /**
     * checks if the given catalog entry is within one of the given catalog categories
     * 
     * @param toBeCheckedEntry
     * @param entriesList
     * @return
     */
    public abstract boolean isEntryWithinCategory(final CatalogEntry toBeCheckedEntry, final List<CatalogEntry> entriesList);

    /**
     * Create a volatile OLATResourceable for a given catalog entry that can be used to create a bookmark to this catalog entry
     * 
     * @param currentCatalogEntry
     * @return
     */
    public abstract OLATResourceable createOLATResouceableFor(final CatalogEntry currentCatalogEntry);

    /**
     * @param ident
     * @return
     */
    public abstract boolean hasParentAllreadyCatalogEntryAsChild(String nodeIdent, RepositoryEntry toBeAddedEntry);

    public abstract CatalogEntry importStructure();

    /**
     * @param ce
     * @param identity
     * @return
     */
    public abstract boolean isOwner(CatalogEntry catalogEntry, Identity identity);

    /**
     * @param addIdentities
     */
    public abstract void addOwners(CatalogEntry catalogEntry, List<Identity> addIdentities);

    /**
     * @param currentCatalogEntry
     * @param removedIdentities
     */
    public abstract void removeOwners(CatalogEntry ce, List<Identity> removedIdentities);

    /**
     * @return
     */
    public abstract CatalogEntry createCatalogEntryWithoutRepositoryEntry(CatalogEntry parentCatalogEntry);

    /**
     * @param currentCatalogEntry
     */
    public abstract CatalogEntry setEmptyOwnerGroup(CatalogEntry currentCatalogEntry);

    /**
     * @param historyStack
     * @return
     */
    public abstract List<Identity> getCaretakerFormCatalogEntryList(List<CatalogEntry> historyStack);

}
