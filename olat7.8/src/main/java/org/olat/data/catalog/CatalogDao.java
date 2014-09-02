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
package org.olat.data.catalog;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;

/**
 * The CatalogDao provides an api to persist CatalogEntries.
 * 
 * @author Christian Guretzki
 */
public interface CatalogDao {

    /**
     * @return transient catalog entry object
     */
    public abstract CatalogEntry createCatalogEntry();

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
     * find all catalog entries referencing the supplied Repository Entry.
     * 
     * @param repoEntry
     * @return List of catalog entries
     */
    public abstract List getCatalogEntriesReferencing(final RepositoryEntry repoEntry);

    /**
     * find all catalog categorie that the given repository entry is a child of
     * 
     * @param repoEntry
     * @return List of catalog entries
     */
    public abstract List getCatalogCategoriesFor(final RepositoryEntry repoEntry);

    /**
     * find catalog entries by supplied name
     * 
     * @param name
     * @return List of catalog entries
     */
    public abstract List getCatalogEntriesByName(final String name);

    /**
     * Find catalog entries for certain identity
     * 
     * @param name
     * @return List of catalog entries
     */
    public abstract List getCatalogEntriesOwnedBy(final Identity identity);

    /**
     * Find all CatalogEntries which can act as catalog roots. Frankly speaking only one is found up to now, but for later stages one can think of getting more such
     * roots. An empty list indicates an error.
     * 
     * @return List of catalog entries
     */
    public abstract List getRootCatalogEntries();

}
