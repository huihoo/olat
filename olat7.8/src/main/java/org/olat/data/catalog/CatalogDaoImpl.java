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
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.repository.RepositoryEntry;
import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * The CatalogDaoImpl is responsible for the persistence of CatalogEntries.
 * 
 * 
 * @author Christian Guretzki
 */
@Repository
public class CatalogDaoImpl extends BasicManager implements CatalogDao {

    @Autowired
    private DB database;

    /**
     * [spring]
     * 
     * 
     */
    private CatalogDaoImpl() {
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#createCatalogEntry()
     */
    @Override
    public CatalogEntry createCatalogEntry() {
        return new CatalogEntryImpl();
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getChildrenOf(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public List<CatalogEntry> getChildrenOf(final CatalogEntry ce) {
        final String sqlQuery = "select cei from org.olat.data.catalog.CatalogEntryImpl as cei " + " where cei.parent = :parent order by cei.name ";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setEntity("parent", ce);
        // cache this query
        dbQuery.setCacheable(true);
        return dbQuery.list();
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getAllCatalogNodes()
     */
    @Override
    public List<CatalogEntry> getAllCatalogNodes() {
        final String sqlQuery = "select cei from org.olat.data.catalog.CatalogEntryImpl as cei " + " where cei.type= :type ";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setInteger("type", CatalogEntry.TYPE_NODE);
        // cache this query
        dbQuery.setCacheable(true);
        return dbQuery.list();
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#hasChildEntries(org.olat.data.catalog.CatalogEntry, int)
     */
    @Override
    public boolean hasChildEntries(final CatalogEntry ce, final int type) {
        final String sqlQuery = "select count(cei) from org.olat.data.catalog.CatalogEntryImpl as cei " + " where cei.parent = :parent AND cei.type= :type ";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setEntity("parent", ce);
        dbQuery.setInteger("type", type);
        // cache this query
        dbQuery.setCacheable(true);
        final List res = dbQuery.list();
        final Long cntL = (Long) res.get(0);
        return (cntL.longValue() > 0);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#loadCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public CatalogEntry loadCatalogEntry(final CatalogEntry catalogEntry) {
        return (CatalogEntry) database.loadObject(catalogEntry);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#loadCatalogEntry(java.lang.Long)
     */
    @Override
    public CatalogEntry loadCatalogEntry(final Long catEntryId) {
        return (CatalogEntry) database.loadObject(CatalogEntryImpl.class, catEntryId);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#saveCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void saveCatalogEntry(final CatalogEntry ce) {
        database.saveObject(ce);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#updateCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void updateCatalogEntry(final CatalogEntry ce) {
        database.updateObject(ce);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#deleteCatalogEntry(org.olat.data.catalog.CatalogEntry)
     */
    @Override
    public void deleteCatalogEntry(final CatalogEntry ce) {
        database.deleteObject(ce);
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getCatalogEntriesReferencing(org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public List getCatalogEntriesReferencing(final RepositoryEntry repoEntry) {
        final String sqlQuery = "select cei from " + " org.olat.data.catalog.CatalogEntryImpl as cei " + " ,RepositoryEntry as re "
                + " where cei.repositoryEntry = re AND re.key= :reKey ";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setCacheable(true);
        dbQuery.setLong("reKey", repoEntry.getKey().longValue());
        final List resSet = dbQuery.list();
        return resSet;
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getCatalogCategoriesFor(org.olat.data.repository.RepositoryEntry)
     */
    @Override
    public List getCatalogCategoriesFor(final RepositoryEntry repoEntry) {
        final String sqlQuery = "select distinct parent from org.olat.data.catalog.CatalogEntryImpl as parent " + ", org.olat.data.catalog.CatalogEntryImpl as cei "
                + ", RepositoryEntry as re " + " where cei.repositoryEntry = re " + " and re.key= :reKey " + " and cei.parent = parent ";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setCacheable(true);
        dbQuery.setLong("reKey", repoEntry.getKey().longValue());
        final List resSet = dbQuery.list();
        return resSet;
    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getCatalogEntriesByName(java.lang.String)
     */
    @Override
    public List getCatalogEntriesByName(final String name) {
        final String sqlQuery = "select cei from org.olat.data.catalog.CatalogEntryImpl as cei where cei.name = :name";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setString("name", name);
        dbQuery.setCacheable(true);
        return dbQuery.list();

    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getCatalogEntriesOwnedBy(org.olat.data.basesecurity.Identity)
     */
    @Override
    public List getCatalogEntriesOwnedBy(final Identity identity) {
        final String sqlQuery = "select cei from org.olat.data.catalog.CatalogEntryImpl as cei inner join fetch cei.ownerGroup, "
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi" + " where " + " cei.ownerGroup = sgmsi.securityGroup and"
                + " sgmsi.identity = :identity";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setEntity("identity", identity);
        dbQuery.setCacheable(true);
        return dbQuery.list();

    }

    /**
     * @see org.olat.data.catalog.CatalogDao#getRootCatalogEntries()
     */
    @Override
    public List getRootCatalogEntries() {
        final String sqlQuery = "select cei from org.olat.data.catalog.CatalogEntryImpl as cei where cei.parent is null";
        final DBQuery dbQuery = database.createQuery(sqlQuery);
        dbQuery.setCacheable(true);
        return dbQuery.list();
    }

}
