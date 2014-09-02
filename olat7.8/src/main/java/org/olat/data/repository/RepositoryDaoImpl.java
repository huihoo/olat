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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.system.commons.StringHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * TODO: Class Description for RepositoryDao
 * 
 * <P>
 * Initial Date: 05.07.2011 <br>
 * 
 * @author guido
 */
@Repository
public class RepositoryDaoImpl implements RepositoryDao {

    private static final Logger log = LoggerHelper.getLogger();

    protected RepositoryDaoImpl() {
    }

    @Autowired
    private DB database;

    /**
     * @param initialAuthor
     * @return A repository instance which has not been persisted yet.
     */
    @Override
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor) {
        assert initialAuthor != null;
        return createRepositoryEntryInstance(initialAuthor, null, null);
    }

    /**
     * @param initialAuthor
     * @param resourceName
     * @param description
     * @return A repository instance which has not been persisted yet, initialized with given data.
     */
    @Override
    public RepositoryEntry createRepositoryEntryInstance(final String initialAuthor, final String resourceName, final String description) {
        final RepositoryEntry re = new RepositoryEntry();
        re.setInitialAuthor(initialAuthor);
        re.setResourcename(resourceName == null ? "" : resourceName);
        re.setDescription(description == null ? "" : description);
        re.setLastUsage(new Date());
        return re;
    }

    /**
     * Save repo entry.
     * 
     * @param re
     */
    @Override
    public void saveRepositoryEntry(final RepositoryEntry re) {
        if (re.getOwnerGroup() == null) {
            throw new AssertException("try to save RepositoryEntry without owner-group! Plase initialize owner-group.");
        }
        re.setLastModified(new Date());
        database.saveObject(re);
    }

    /**
     * Update repo entry.
     * 
     * @param re
     */
    @Override
    public void updateRepositoryEntry(final RepositoryEntry re) {
        re.setLastModified(new Date());
        database.updateObject(re);
    }

    /**
     * Delete repo entry.
     * 
     * @param re
     */
    @Override
    public void deleteRepositoryEntry(RepositoryEntry re) {
        re = (RepositoryEntry) database.loadObject(re, true);
        database.deleteObject(re);
    }

    /**
     * Lookup repo entry by key.
     * 
     * @param the
     *            repository entry key (not the olatresourceable key)
     * @return Repo entry represented by key or null if no such entry or key is null.
     */
    @Override
    public RepositoryEntry lookupRepositoryEntry(final Long key) {
        if (key == null) {
            return null;
        }
        return (RepositoryEntry) database.findObject(RepositoryEntry.class, key);
    }

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
    @Override
    public RepositoryEntry lookupRepositoryEntry(final OLATResource resource, final boolean strict) {

        final String query = "select v from RepositoryEntry v" + " inner join fetch v.olatResource as ores" + " where ores.key = :oreskey";
        final DBQuery dbQuery = database.createQuery(query);
        dbQuery.setLong("oreskey", resource.getKey().longValue());
        dbQuery.setCacheable(true);

        final List result = dbQuery.list();
        final int size = result.size();
        if (strict) {
            if (size != 1) {
                throw new AssertException("Repository resourceable lookup returned zero or more than one result: " + size);
            }
        } else { // not strict -> return null if zero entries found
            if (size > 1) {
                throw new AssertException("Repository resourceable lookup returned more than one result: " + size);
            }
            if (size == 0) {
                return null;
            }
        }
        return (RepositoryEntry) result.get(0);
    }

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
    @Override
    public RepositoryEntry lookupRepositoryEntryBySoftkey(final String softkey, final boolean strict) {
        final String query = "select v from RepositoryEntry v" + " inner join fetch v.olatResource as ores" + " where v.softkey = :softkey";

        final DBQuery dbQuery = database.createQuery(query);
        dbQuery.setString("softkey", softkey);
        dbQuery.setCacheable(true);
        final List result = dbQuery.list();

        final int size = result.size();
        if (strict) {
            if (size != 1) {
                throw new AssertException("Repository softkey lookup returned zero or more than one result: " + size + ", softKey = " + softkey);
            }
        } else { // not strict -> return null if zero entries found
            if (size > 1) {
                throw new AssertException("Repository softkey lookup returned more than one result: " + size + ", softKey = " + softkey);
            }
            if (size == 0) {
                return null;
            }
        }
        return (RepositoryEntry) result.get(0);
    }

    /**
     * Convenience method to access the repositoryEntry displayname by the referenced OLATResourceable id. This only works if a repository entry has an referenced olat
     * resourceable like a course or an content package repo entry
     * 
     * @param resId
     * @return the repositoryentry displayname or null if not found
     */
    @Override
    public String lookupDisplayNameByOLATResourceableId(final Long resId) {
        final String query = "select v from RepositoryEntry v" + " inner join fetch v.olatResource as ores" + " where ores.resId = :resid";
        final DBQuery dbQuery = database.createQuery(query);
        dbQuery.setLong("resid", resId.longValue());
        dbQuery.setCacheable(true);

        final List<RepositoryEntry> result = dbQuery.list();
        final int size = result.size();
        if (size > 1) {
            throw new AssertException("Repository lookup returned zero or more than one result: " + size);
        } else if (size == 0) {
            return null;
        }
        final RepositoryEntry entry = result.get(0);
        return entry.getDisplayname();
    }

    /**
     * Count by type, limit by role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Number of repo entries
     */
    @Override
    public int countByTypeLimitAccess(final String restrictedType, final int restrictedAccess) {
        final StringBuilder query = new StringBuilder(400);
        query.append("select count(*) from" + " RepositoryEntry v, " + OLATResourceImpl.class.getName() + " res "
                + " where v.olatResource = res and res.resName= :restrictedType and v.access >= :restrictedAccess ");
        final DBQuery dbquery = database.createQuery(query.toString());
        dbquery.setString("restrictedType", restrictedType);
        dbquery.setInteger("restrictedAccess", restrictedAccess);
        dbquery.setCacheable(true);
        return ((Long) dbquery.list().get(0)).intValue();
    }

    /**
     * Query by type without any other limitations
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    @Override
    public List queryByType(final String restrictedType) {
        final String query = "select v from" + " RepositoryEntry v" + " inner join fetch v.olatResource as res" + " where res.resName= :restrictedType";
        final DBQuery dbquery = database.createQuery(query);
        dbquery.setString("restrictedType", restrictedType);
        dbquery.setCacheable(true);
        return dbquery.list();
    }

    /**
     * Query by type, limit by ownership or role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    @Override
    public List queryByTypeLimitAccess(final String restrictedType, final Roles roles) {
        final StringBuilder query = new StringBuilder(400);
        query.append("select distinct v from" + " RepositoryEntry v" + " inner join fetch v.olatResource as res" + " where res.resName= :restrictedType and v.access >= ");

        if (roles.isOLATAdmin()) {
            query.append(RepositoryEntry.ACC_OWNERS); // treat admin special b/c admin is author as well
        } else {
            if (roles.isAuthor()) {
                query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
            } else if (roles.isGuestOnly()) {
                query.append(RepositoryEntry.ACC_USERS_GUESTS);
            } else {
                query.append(RepositoryEntry.ACC_USERS);
            }
        }
        final DBQuery dbquery = database.createQuery(query.toString());
        dbquery.setString("restrictedType", restrictedType);
        dbquery.setCacheable(true);
        return dbquery.list();
    }

    /**
     * Query by type, limit by ownership or role accessability.
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    @Override
    public List queryByTypeLimitAccess(final String restrictedType, String institution, Roles roles) {

        if (!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager()) {
            final StringBuilder query = new StringBuilder(400);
            query.append("select distinct v from RepositoryEntry v inner join fetch v.olatResource as res"
                    + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi" + ", org.olat.data.basesecurity.IdentityImpl identity"
                    + ", org.olat.data.user.UserImpl user " + " where sgmsi.securityGroup = v.ownerGroup" + " and sgmsi.identity = identity"
                    + " and identity.user = user" + " and user.properties['institutionalName']= :institutionCourseManager "
                    + " and res.resName= :restrictedType and v.access = 1");

            final DBQuery dbquery = database.createQuery(query.toString());
            dbquery.setString("restrictedType", restrictedType);
            dbquery.setString("institutionCourseManager", institution);
            dbquery.setCacheable(true);

            long start = System.currentTimeMillis();
            final List result = dbquery.list();
            final long timeQuery1 = System.currentTimeMillis() - start;
            log.info("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery1);
            start = System.currentTimeMillis();
            result.addAll(queryByTypeLimitAccess(restrictedType, roles));
            final long timeQuery2 = System.currentTimeMillis() - start;
            log.info("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery2);
            return result;

        } else {
            final long start = System.currentTimeMillis();
            final List result = queryByTypeLimitAccess(restrictedType, roles);
            final long timeQuery3 = System.currentTimeMillis() - start;
            log.info("Repo-Perf: queryByTypeLimitAccess#3 takes " + timeQuery3);
            return result;
        }
    }

    @Override
    public List queryByOwner(final Identity identity, final String[] limitTypes) {
        if (identity == null) {
            throw new AssertException("identity can not be null!");
        }
        final StringBuffer query = new StringBuffer(400);
        query.append("select v from" + " org.olat.data.repository.RepositoryEntry v inner join fetch v.olatResource as res,"
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi" + " where " + " v.ownerGroup = sgmsi.securityGroup and"
                + " sgmsi.identity = :identity");
        if (limitTypes != null && limitTypes.length > 0) {
            for (int i = 0; i < limitTypes.length; i++) {
                final String limitType = limitTypes[i];
                if (i == 0) {
                    query.append(" and ( res.resName= '" + limitType + "'");
                } else {
                    query.append(" or res.resName= '" + limitType + "'");
                }

            }
            query.append(" )");
        }
        final DBQuery dbquery = database.createQuery(query.toString());
        dbquery.setEntity("identity", identity);
        return dbquery.list();
    }

    /**
     * Query by initial-author
     * 
     * @param restrictedType
     * @param roles
     * @return Results
     */
    @Override
    public List queryByInitialAuthor(final String initialAuthor) {
        final String query = "select v from" + " RepositoryEntry v" + " where v.initialAuthor= :initialAuthor";
        final DBQuery dbquery = database.createQuery(query);
        dbquery.setString("initialAuthor", initialAuthor);
        dbquery.setCacheable(true);
        return dbquery.list();
    }

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
    @Override
    public List queryReferencableResourcesLimitType(final Identity identity, final Roles roles, List resourceTypes, String displayName, String author, String desc) {
        if (identity == null) {
            throw new AssertException("identity can not be null!");
        }
        if (!roles.isAuthor()) {
            // if user has no author right he can not reference to any resource at all
            return new ArrayList();
        }

        // cleanup some data: use null values if emtpy
        if (resourceTypes != null && resourceTypes.size() == 0) {
            resourceTypes = null;
        }
        if (!StringHelper.containsNonWhitespace(displayName)) {
            displayName = null;
        }
        if (!StringHelper.containsNonWhitespace(author)) {
            author = null;
        }
        if (!StringHelper.containsNonWhitespace(desc)) {
            desc = null;
        }

        // Build the query
        // 1) Joining tables
        final StringBuilder query = new StringBuilder(400);
        query.append("select distinct v from");
        query.append(" RepositoryEntry v inner join fetch v.olatResource as res");
        query.append(", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi");
        if (author != null) {
            query.append(", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi2");
            query.append(", org.olat.data.basesecurity.IdentityImpl identity");
            query.append(", org.olat.data.user.UserImpl user ");
        }
        // 2) where clause
        query.append(" where ");
        // the join of v.ownerGropu and sgmsi.securityGroup mus be outside the sgmsi.identity = :identity
        // otherwhise the join is not present in the second part of the or clause and the cross product will
        // be to large (does not work when more than 100 repo entries present!)
        query.append(" v.ownerGroup = sgmsi.securityGroup");
        // restrict on ownership or referencability flag
        query.append(" and ( sgmsi.identity = :identity ");
        query.append(" or ");
        query.append(" (v.access >= :access and v.canReference = true) )");
        // restrict on type
        if (resourceTypes != null) {
            query.append(" and res.resName in (:resourcetypes)");
        }
        // restrict on author
        if (author != null) { // fuzzy author search
            author = author.replace('*', '%');
            author = '%' + author + '%';
            query.append(" and (sgmsi2.securityGroup = v.ownerGroup and " + "sgmsi2.identity = identity and " + "identity.user = user and "
                    + "(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author))");
        }
        // restrict on resource name
        if (displayName != null) {
            displayName = displayName.replace('*', '%');
            displayName = '%' + displayName + '%';
            query.append(" and v.displayname like :displayname");
        }
        // restrict on resource description
        if (desc != null) {
            desc = desc.replace('*', '%');
            desc = '%' + desc + '%';
            query.append(" and v.description like :desc");
        }

        // create query an set query data
        final DBQuery dbquery = database.createQuery(query.toString());
        dbquery.setEntity("identity", identity);
        dbquery.setInteger("access", RepositoryEntry.ACC_OWNERS_AUTHORS);
        if (author != null) {
            dbquery.setString("author", author);
        }
        if (displayName != null) {
            dbquery.setString("displayname", displayName);
        }
        if (desc != null) {
            dbquery.setString("desc", desc);
        }
        if (resourceTypes != null) {
            dbquery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
        }
        return dbquery.list();

    }

    /**
     * Query by ownership, limit by access.
     * 
     * @param identity
     * @param limitAccess
     * @return Results
     */
    @Override
    public List queryByOwnerLimitAccess(final Identity identity, final int limitAccess) {
        final String query = "select v from" + " RepositoryEntry v inner join fetch v.olatResource as res,"
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi" + " where" + " v.ownerGroup = sgmsi.securityGroup "
                + " and sgmsi.identity = :identity and v.access >= :limitAccess";

        final DBQuery dbquery = database.createQuery(query);
        dbquery.setEntity("identity", identity);
        dbquery.setInteger("limitAccess", limitAccess);
        return dbquery.list();
    }

    /**
     * Query repository If any input data contains "*", then it replaced by "%" (search me*er -> sql: me%er)
     * 
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
     * @return Results as List containing RepositoryEntries
     */
    private List runGenericANDQueryWithRolesRestriction(String displayName, String author, String desc, final List resourceTypes, final Roles roles) {
        final StringBuilder query = new StringBuilder(400);

        final boolean var_author = (author != null && author.length() != 0);
        final boolean var_displayname = (displayName != null && displayName.length() != 0);
        final boolean var_desc = (desc != null && desc.length() != 0);
        final boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);

        // Use two different select prologues...
        if (var_author) { // extended query for user search
            query.append("select distinct v from" + " RepositoryEntry v inner join fetch v.olatResource as res,"
                    + " org.olat.data.basesecurity.SecurityGroupMembershipImpl sgmsi, " + " org.olat.data.basesecurity.IdentityImpl identity,"
                    + " org.olat.data.user.UserImpl user ");
        } else { // simple query
            query.append("select distinct v from" + " RepositoryEntry v " + " inner join fetch v.olatResource as res  ");
        }

        boolean isFirstOfWhereClause = false;
        query.append("where v.access != 0 "); // access == 0 means invalid repo-entry (not complete created)
        if (var_author) { // fuzzy author search
            author = author.replace('*', '%');
            author = '%' + author + '%';
            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("sgmsi.securityGroup = v.ownerGroup and " + "sgmsi.identity = identity and " + "identity.user = user and "
                    + "(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author)");
            isFirstOfWhereClause = false;
        }

        if (var_displayname) {
            displayName = displayName.replace('*', '%');
            displayName = '%' + displayName + '%';
            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("v.displayname like :displayname");
            isFirstOfWhereClause = false;
        }

        if (var_desc) {
            desc = desc.replace('*', '%');
            desc = '%' + desc + '%';
            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("v.description like :desc");
            isFirstOfWhereClause = false;
        }

        if (var_resourcetypes) {
            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("res.resName in (:resourcetypes)");
            isFirstOfWhereClause = false;
        }

        // finally limit on roles, if not olat admin
        if (!roles.isOLATAdmin()) {
            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("v.access >= ");
            if (roles.isAuthor()) {
                query.append(RepositoryEntry.ACC_OWNERS_AUTHORS);
            } else if (roles.isGuestOnly()) {
                query.append(RepositoryEntry.ACC_USERS_GUESTS);
            } else {
                query.append(RepositoryEntry.ACC_USERS);
            }
            isFirstOfWhereClause = false;
        }

        final DBQuery dbQuery = database.createQuery(query.toString());
        if (var_author) {
            dbQuery.setString("author", author);
        }
        if (var_displayname) {
            dbQuery.setString("displayname", displayName);
        }
        if (var_desc) {
            dbQuery.setString("desc", desc);
        }
        if (var_resourcetypes) {
            dbQuery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
        }
        return dbQuery.list();
    }

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
    @Override
    public List<RepositoryEntry> genericANDQueryWithRolesRestriction(String displayName, String author, String desc, final List resourceTypes, final Roles roles,
            final String institution) {
        if (!roles.isOLATAdmin() && institution != null && institution.length() > 0 && roles.isInstitutionalResourceManager()) {
            final StringBuilder query = new StringBuilder(400);
            if (author == null || author.length() == 0) {
                author = "*";
            }
            final boolean var_author = true;
            final boolean var_displayname = (displayName != null && displayName.length() != 0);
            final boolean var_desc = (desc != null && desc.length() != 0);
            final boolean var_resourcetypes = (resourceTypes != null && resourceTypes.size() > 0);
            // Use two different select prologues...
            if (var_author) { // extended query for user search
                query.append("select distinct v from" + " RepositoryEntry v inner join fetch v.olatResource as res,"
                        + " org.olat.data.basesecurity.SecurityGroupMembershipImpl sgmsi, " + " org.olat.data.basesecurity.IdentityImpl identity,"
                        + " org.olat.data.user.UserImpl user ");
            } else { // simple query
                query.append("select distinct v from" + " RepositoryEntry v " + " inner join fetch v.olatResource as res  ");
            }
            boolean isFirstOfWhereClause = false;
            query.append("where v.access != 0 "); // access == 0 means invalid repo-entry (not complete created)
            if (var_author) { // fuzzy author search
                author = author.replace('*', '%');
                author = '%' + author + '%';
                if (!isFirstOfWhereClause) {
                    query.append(" and ");
                }
                query.append("sgmsi.securityGroup = v.ownerGroup and " + "sgmsi.identity = identity and " + "identity.user = user and "
                        + "(user.properties['firstName'] like :author or user.properties['lastName'] like :author or identity.name like :author)");
                isFirstOfWhereClause = false;
            }
            if (var_displayname) {
                displayName = displayName.replace('*', '%');
                displayName = '%' + displayName + '%';
                if (!isFirstOfWhereClause) {
                    query.append(" and ");
                }
                query.append("v.displayname like :displayname");
                isFirstOfWhereClause = false;
            }
            if (var_desc) {
                desc = desc.replace('*', '%');
                desc = '%' + desc + '%';
                if (!isFirstOfWhereClause) {
                    query.append(" and ");
                }
                query.append("v.description like :desc");
                isFirstOfWhereClause = false;
            }
            if (var_resourcetypes) {
                if (!isFirstOfWhereClause) {
                    query.append(" and ");
                }
                query.append("res.resName in (:resourcetypes)");
                isFirstOfWhereClause = false;
            }

            if (!isFirstOfWhereClause) {
                query.append(" and ");
            }
            query.append("v.access = 1 and user.properties['institutionalName']= :institution ");
            isFirstOfWhereClause = false;

            final DBQuery dbQuery = database.createQuery(query.toString());
            dbQuery.setString("institution", institution);
            if (var_author) {
                dbQuery.setString("author", author);
            }
            if (var_displayname) {
                dbQuery.setString("displayname", displayName);
            }
            if (var_desc) {
                dbQuery.setString("desc", desc);
            }
            if (var_resourcetypes) {
                dbQuery.setParameterList("resourcetypes", resourceTypes, Hibernate.STRING);
            }
            final List result = dbQuery.list();
            result.addAll(runGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles));
            return result;
        } else {
            return runGenericANDQueryWithRolesRestriction(displayName, author, desc, resourceTypes, roles);
        }
    }

    public RepositoryEntry loadRepositoryEntry(RepositoryEntry repositoryEntry) {
        return (RepositoryEntry) database.loadObject(repositoryEntry);
    }

    /**
     * Special-query for Upgrade-6.2.0.
     */
    // TODO remove that from standard API
    public List<RepositoryEntry> getAllRepositoryEntries() {
        final StringBuilder q = new StringBuilder();
        q.append(" select repoEntry from RepositoryEntry as repoEntry");
        final DBQuery query = database.createQuery(q.toString());
        return query.list();
    }

}
