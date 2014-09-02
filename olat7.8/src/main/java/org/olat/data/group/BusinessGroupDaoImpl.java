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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.data.group.context.BGContext;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.exception.KnownIssueException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * Persisting implementation of the business group manager. Persists the data in the database.
 * <P>
 * Initial Date: Jul 28, 2004 <br>
 * 
 * @author patrick
 */

@Repository("businessGroupManager")
public class BusinessGroupDaoImpl extends BasicManager implements BusinessGroupDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    DB db;
    @Autowired
    CoordinatorManager coordinatorManager;

    /**
     * [used by spring]
     */
    private BusinessGroupDaoImpl() {
        //
    }

    /**
     * check if all given names in context exists.
     * 
     * @param names
     * @param groupContext
     * @return
     */
    public boolean checkIfOneOrMoreNameExistsInContext(final Set names, final BGContext groupContext) {
        final String query = "select count(bgs) from " + "  org.olat.data.group.BusinessGroupImpl as bgs " + "  where " + "  bgs.groupContext = :context"
                + " and bgs.name in (:names)";
        final DBQuery dbq = db.createQuery(query);
        dbq.setEntity("context", groupContext);
        dbq.setParameterList("names", names);
        final int result = ((Long) dbq.list().get(0)).intValue();
        // return false if none of the groups was found
        if (result == 0) {
            return false;
        }
        // true if one or more groups were found
        return true;
    }

    /**
	 */
    @Override
    public List<BusinessGroup> findBusinessGroupsOwnedBy(final String type, final Identity identityP, final BGContext bgContext) {
        // attach group context to session - maybe a proxy...
        String query = "select bgi from " + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmi," + " org.olat.data.group.BusinessGroupImpl as bgi"
                + " where bgi.ownerGroup = sgmi.securityGroup and sgmi.identity = :identId";
        if (bgContext != null) {
            query = query + " and bgi.groupContext = :context";
        }
        if (type != null) {
            query = query + " and bgi.type = :type";
        }

        final DBQuery dbq = db.createQuery(query);
        /*
         * query.append("select distinct v from" + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi," + " RepositoryEntry v" +
         * " inner join fetch v.ownerGroup as secGroup" + " inner join fetch v.olatResource as res where" + " sgmsi.securityGroup = secGroup and sgmsi.identity.key=");
         */

        dbq.setLong("identId", identityP.getKey().longValue());
        if (bgContext != null) {
            dbq.setEntity("context", bgContext);
        }
        if (type != null) {
            dbq.setString("type", type);
        }

        final List res = dbq.list();
        return res;
    }

    /**
	 */
    @Override
    public List<BusinessGroup> findBusinessGroupsAttendedBy(final String type, final Identity identityP, final BGContext bgContext) {
        String query = "select bgi from " + "  org.olat.data.group.BusinessGroupImpl as bgi " + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmi"
                + " where bgi.partipiciantGroup = sgmi.securityGroup";
        query = query + " and sgmi.identity = :identId";
        if (bgContext != null) {
            query = query + " and bgi.groupContext = :context";
        }
        if (type != null) {
            query = query + " and bgi.type = :type";
        }

        final DBQuery dbq = db.createQuery(query);
        dbq.setLong("identId", identityP.getKey().longValue());
        if (bgContext != null) {
            dbq.setEntity("context", bgContext);
        }
        if (type != null) {
            dbq.setString("type", type);
        }

        final List res = dbq.list();
        return res;
    }

    /**
	 */
    @Override
    public List<BusinessGroup> getAllBusinessGroups() {
        final DBQuery dbq = db.createQuery("select bgi from " + "  org.olat.data.group.BusinessGroupImpl as bgi");
        return dbq.list();
    }

    @Override
    public List<Long> getAllBusinessGroupIds() {
        final DBQuery dbq = db.createQuery("select key from " + "  org.olat.data.group.BusinessGroupImpl");
        return dbq.list();
    }

    /**
	 */
    @Override
    public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(final String type, final Identity identityP, final BGContext bgContext) {
        String query = "select bgi from " + "  org.olat.data.group.BusinessGroupImpl as bgi " + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmi"
                + " where bgi.waitingGroup = sgmi.securityGroup and sgmi.identity = :identId";
        if (bgContext != null) {
            query = query + " and bgi.groupContext = :context";
        }
        if (type != null) {
            query = query + " and bgi.type = :type";
        }

        final DBQuery dbq = db.createQuery(query);
        dbq.setLong("identId", identityP.getKey().longValue());
        if (bgContext != null) {
            dbq.setEntity("context", bgContext);
        }
        if (type != null) {
            dbq.setString("type", type);
        }

        final List res = dbq.list();
        return res;
    }

    /**
	 */
    @Override
    public BusinessGroup findBusinessGroup(final SecurityGroup secGroup) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" as bgi where ")
                .append("(bgi.partipiciantGroup=:secGroup or bgi.ownerGroup=:secGroup or bgi.waitingGroup=:secGroup)");

        final DBQuery query = db.createQuery(sb.toString());
        query.setEntity("secGroup", secGroup);
        final List<BusinessGroup> res = query.list();
        if (res.isEmpty()) {
            return null;
        }
        return res.get(0);
    }

    /**
	 */
    @Override
    public void updateBusinessGroup(final BusinessGroup updatedBusinessGroup) {
        updatedBusinessGroup.setLastModified(new Date());
        db.updateObject(updatedBusinessGroup);
    }

    /**
	 */
    @Override
    public void deleteBusinessGroup(BusinessGroup businessGroupTodelete) {
        try {
            // Delete the group object itself on the database
            db.deleteObject(businessGroupTodelete);
        } catch (final DBRuntimeException dbre) {
            final Throwable th = dbre.getCause();
            if ((th instanceof StaleObjectStateException) && (th.getMessage().startsWith("Row was updated or deleted by another transaction"))) {
                // known issue OLAT-3654
                log.info("Group was deleted by another user in the meantime. Known issue OLAT-3654");
                throw new KnownIssueException("Group was deleted by another user in the meantime", 3654, dbre);
            } else {
                throw dbre;
            }
        }
    }

    /**
	 */
    @Override
    public void deleteBusinessGroups(final List businessGroups) {
        final Iterator iterator = businessGroups.iterator();
        while (iterator.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iterator.next();
            deleteBusinessGroup(group);
        }
    }

    /**
	 */
    @Override
    public boolean isIdentityInBusinessGroup(final Identity identity, final String groupName, final BGContext groupContext) {
        final StringBuilder q = new StringBuilder();
        q.append(" select count(grp) from").append(" org.olat.data.group.BusinessGroupImpl as grp,")
                .append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as secgmemb").append(" where");
        if (groupContext != null) {
            q.append(" grp.groupContext = :context and");
        }
        q.append(" grp.name = :name").append(" and ((grp.partipiciantGroup = secgmemb.securityGroup").append(" and secgmemb.identity = :id) ")
                .append(" or (grp.ownerGroup = secgmemb.securityGroup").append(" and secgmemb.identity = :id)) ");
        final DBQuery query = db.createQuery(q.toString());
        query.setEntity("id", identity);
        if (groupContext != null) {
            query.setEntity("context", groupContext);
        }
        query.setString("name", groupName);
        query.setCacheable(true);
        final List result = query.list();
        if (result.size() == 0) {
            return false;
        }
        return (((Long) result.get(0)).intValue() > 0);
    }

    /**
	 */
    @Override
    public BusinessGroup loadBusinessGroup(final BusinessGroup currBusinessGroup) {
        return (BusinessGroup) db.loadObject(currBusinessGroup);
    }

    /**
	 */
    @Override
    public BusinessGroup loadBusinessGroup(final Long groupKey, final boolean strict) {
        if (strict) {
            return (BusinessGroup) db.loadObject(BusinessGroupImpl.class, groupKey);
        }
        return (BusinessGroup) db.findObject(BusinessGroupImpl.class, groupKey);
    }

    @Override
    public List<BusinessGroup> findAllBusinessGroupsOwnedBy(final Identity identity) {
        return findBusinessGroupsOwnedBy(null, identity, null);
    }

    @Override
    public List<BusinessGroup> findAllBusinessGroupsAttendedBy(final Identity identity) {
        return findBusinessGroupsAttendedBy(null, identity, null);
    }

    @Override
    public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(final Identity identity) {
        return findBusinessGroupsWithWaitingListAttendedBy(null, identity, null);
    }

    /**
	 */
    @Override
    public void setLastUsageFor(final BusinessGroup currBusinessGroup) {
        // o_clusterOK by:cg
        coordinatorManager.getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor() {
            @Override
            public void execute() {
                // force a reload from db loadObject(..., true) by evicting it from
                // hibernates session
                // cache to catch up on a different thread having commited the update of
                // the launchcounter
                final BusinessGroup reloadedBusinessGroup = loadBusinessGroup(currBusinessGroup);
                reloadedBusinessGroup.setLastUsage(new Date());
                LifeCycleManager.createInstanceFor(reloadedBusinessGroup).deleteTimestampFor(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION);
                updateBusinessGroup(reloadedBusinessGroup);
            }
        });
    }

    @Override
    public boolean testIfGroupAlreadyExists(final String name, final String type, final BGContext groupContext) {
        final String query = "select count(bgs) from " + "  org.olat.data.group.BusinessGroupImpl as bgs " + " where bgs.type = :type"
                + " and bgs.groupContext = :context" + " and bgs.name = :name";
        final DBQuery dbq = db.createQuery(query);
        dbq.setString("type", type);
        dbq.setEntity("context", groupContext);
        dbq.setString("name", name);
        final int result = ((Long) dbq.list().get(0)).intValue();
        if (result != 0) {
            return true;
        }
        return false;
    }

    @Override
    public BusinessGroup loadBusinessGroup(BusinessGroup businessGroup, boolean forceReloadFromDB) {
        return (BusinessGroup) db.loadObject(businessGroup, forceReloadFromDB);
    }
}
