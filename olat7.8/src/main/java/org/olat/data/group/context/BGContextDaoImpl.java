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

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.resource.OLATResource;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description: <BR>
 * Implementation of the business group context manager.
 * <P>
 * Initial Date: Aug 19, 2004 <br>
 * 
 * @author gnaegi
 */
@Repository
public class BGContextDaoImpl extends BasicManager implements BGContextDao {

    private static BGContextDao INSTANCE;
    @Autowired
    DB db;
    @Autowired
    BGAreaDao bgAreaDao;

    /**
     * @return singleton instance
     */
    @Deprecated
    public static BGContextDao getInstance() {
        return INSTANCE;
    }

    private BGContextDaoImpl() {
        INSTANCE = this;
    }

    /**
     * boolean)
     */
    @Override
    public BGContext createAndPersistBGContext(final String name, final String description, final SecurityGroup ownerGroup, final String groupType,
            final boolean defaultContext) {
        final BGContext bgContext = new BGContextImpl(name, description, ownerGroup, groupType, defaultContext);
        db.saveObject(bgContext);
        return bgContext;
    }

    /**
	 */
    @Override
    public void updateBGContext(final BGContext bgContext) {
        db.updateObject(bgContext);
    }

    /**
	 */
    @Override
    public void deleteBGContext(BGContext bgContext) {
        bgContext = (BGContext) db.loadObject(bgContext);
        db.deleteObject(bgContext);
    }

    /**
	 */
    @Override
    public List<BusinessGroup> getGroupsOfBGContext(final BGContext bgContext) {
        DBQuery query;
        if (bgContext == null) {
            final String q = "select bg from org.olat.data.group.BusinessGroupImpl bg where bg.groupContext is null";
            query = db.createQuery(q);
        } else {
            final String q = "select bg from org.olat.data.group.BusinessGroupImpl bg where bg.groupContext = :context";
            query = db.createQuery(q);
            query.setEntity("context", bgContext);
        }
        return query.list();
    }

    /**
	 */
    @Override
    public int countGroupsOfBGContext(final BGContext bgContext) {
        final String q = "select count(bg) from org.olat.data.group.BusinessGroupImpl bg where bg.groupContext = :context";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        return ((Long) query.list().get(0)).intValue();
    }

    /**
	 */
    @Override
    public int countGroupsOfType(final String groupType) {
        final String q = "select count(bg) from org.olat.data.group.BusinessGroupImpl bg where bg.type = :type";
        final DBQuery query = db.createQuery(q);
        query.setString("type", groupType);
        return ((Long) query.list().get(0)).intValue();
    }

    /**
	 */
    @Override
    public BusinessGroup findGroupOfBGContext(final String groupName, final BGContext bgContext) {
        final String q = "select bg from org.olat.data.group.BusinessGroupImpl bg where bg.groupContext = :context and bg.name = :name";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        query.setString("name", groupName);
        final List results = query.list();
        if (results.size() == 0) {
            return null;
        }
        return (BusinessGroup) results.get(0);
    }

    /**
	 */
    @Override
    public BusinessGroup findGroupAttendedBy(final Identity identity, final String groupName, final BGContext bgContext) {
        final String query = "select bgi from " + "  org.olat.data.group.BusinessGroupImpl as bgi " + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmi"
                + " where bgi.name = :name " + " and bgi.partipiciantGroup =  sgmi.securityGroup" + " and sgmi.identity = :identId" + " and bgi.groupContext = :context";
        final DBQuery dbq = db.createQuery(query);
        dbq.setEntity("identId", identity);
        dbq.setString("name", groupName);
        dbq.setEntity("context", bgContext);
        final List res = dbq.list();
        if (res.size() == 0) {
            return null;
        } else if (res.size() > 1) {
            throw new AssertException("more than one result row found for (identity, groupname, context) (" + identity.getName() + ", " + groupName + ", "
                    + bgContext.getName());
        }
        return (BusinessGroup) res.get(0);
    }

    /**
	 */
    @Override
    public List getBGOwnersOfBGContext(final BGContext bgContext) {
        final String q = "select distinct id from org.olat.data.basesecurity.IdentityImpl as id inner join fetch id.user as iuser"
                + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.data.group.BusinessGroupImpl bg" + " where bg.groupContext = :context"
                + " and bg.ownerGroup = sgm.securityGroup" + " and sgm.identity = id";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        return query.list();
    }

    /**
	 */
    @Override
    public int countBGOwnersOfBGContext(final BGContext bgContext) {
        final String q = "select count(distinct id) from org.olat.data.basesecurity.IdentityImpl id" + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl sgm"
                + ", org.olat.data.group.BusinessGroupImpl bg" + " where bg.groupContext = :context" + " and bg.ownerGroup = sgm.securityGroup"
                + " and sgm.identity = id";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        final List resultList = query.list();

        int result = 0;
        // if no join/group by matches, result list size is 0 and count undefined ->
        // result is 0
        if (resultList.size() > 0) {
            final Object obj = resultList.get(0);
            if (obj == null) {
                return 0;
            }
            result = ((Long) obj).intValue();
        }
        return result;
    }

    /**
	 */
    @Override
    public List getBGParticipantsOfBGContext(final BGContext bgContext) {
        final String q = "select distinct id from org.olat.data.basesecurity.IdentityImpl as id inner join fetch id.user as iuser"
                + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl sgm" + ", org.olat.data.group.BusinessGroupImpl bg" + " where bg.groupContext = :context"
                + " and bg.partipiciantGroup = sgm.securityGroup" + " and sgm.identity = id";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        return query.list();
    }

    /**
	 */
    @Override
    public int countBGParticipantsOfBGContext(final BGContext bgContext) {
        final String q = "select count(distinct id) from org.olat.data.basesecurity.IdentityImpl id" + ", org.olat.data.basesecurity.SecurityGroupMembershipImpl sgm"
                + ", org.olat.data.group.BusinessGroupImpl bg" + " where bg.groupContext = :context" + " and bg.partipiciantGroup = sgm.securityGroup"
                + " and sgm.identity = id";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        final List resultList = query.list();
        int result = 0;
        // if no join/group by matches, result list size is 0 and count undefined ->
        // result is 0
        if (resultList.size() > 0) {
            final Object obj = resultList.get(0);
            if (obj == null) {
                return 0;
            }
            result = ((Long) obj).intValue();
        }
        return result;
    }

    /**
	 */
    @Override
    public boolean isIdentityInBGContext(final Identity identity, final BGContext bgContext, final boolean asOwner, final boolean asParticipant) {
        final StringBuilder q = new StringBuilder();

        q.append(" select count(grp) from" + " org.olat.data.group.BusinessGroupImpl as grp,"
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as secgmemb where grp.groupContext = :context" + " and ");
        // restricting where clause for participants
        final String partRestr = "(grp.partipiciantGroup = secgmemb.securityGroup and secgmemb.identity = :id) ";
        // restricting where clause for owners
        final String ownRestr = "(grp.ownerGroup = secgmemb.securityGroup and secgmemb.identity = :id)";

        if (asParticipant && asOwner) {
            q.append("(").append(partRestr).append(" or ").append(ownRestr).append(")");
        } else if (asParticipant && !asOwner) {
            q.append(partRestr);
        } else if (!asParticipant && asOwner) {
            q.append(ownRestr);
        } else {
            throw new AssertException("illegal arguments: at leas one of asOwner or asParticipant must be true");
        }

        final DBQuery query = db.createQuery(q.toString());
        query.setEntity("id", identity);
        query.setEntity("context", bgContext);
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
    public List findBGContextsForResource(final OLATResource resource, final boolean defaultContexts, final boolean nonDefaultContexts) {
        return findBGContextsForResource(resource, null, defaultContexts, nonDefaultContexts);
    }

    /**
	 */
    @Override
    public List findBGContextsForResource(final OLATResource resource, final String groupType, final boolean defaultContexts, final boolean nonDefaultContexts) {
        final StringBuilder q = new StringBuilder();
        q.append(" select context from org.olat.data.group.context.BGContextImpl as context,");
        q.append(" org.olat.data.group.context.BGContext2Resource as bgcr");
        q.append(" where bgcr.resource = :resource");
        q.append(" and bgcr.groupContext = context");
        if (groupType != null) {
            q.append(" and context.groupType = :gtype");
        }

        final boolean checkDefault = defaultContexts != nonDefaultContexts;
        if (checkDefault) {
            q.append(" and context.defaultContext = :isDefault");
        }
        final DBQuery query = db.createQuery(q.toString());
        query.setEntity("resource", resource);
        if (groupType != null) {
            query.setString("gtype", groupType);
        }
        if (checkDefault) {
            query.setBoolean("isDefault", defaultContexts ? true : false);
        }
        return query.list();
    }

    /**
	 */
    @Override
    public List findBGContextsForIdentity(final Identity identity, final boolean defaultContexts, final boolean nonDefaultContexts) {
        final StringBuilder q = new StringBuilder();
        q.append(" select context from org.olat.data.group.context.BGContextImpl as context,");
        q.append(" org.olat.data.basesecurity.SecurityGroupMembershipImpl as secgmemb");
        q.append(" where context.ownerGroup = secgmemb.securityGroup");
        q.append(" and secgmemb.identity = :identity");

        final boolean checkDefault = defaultContexts != nonDefaultContexts;
        if (checkDefault) {
            q.append(" and context.defaultContext = :isDefault");
        }
        final DBQuery query = db.createQuery(q.toString());
        query.setEntity("identity", identity);
        if (checkDefault) {
            query.setBoolean("isDefault", defaultContexts ? true : false);
        }

        return query.list();
    }

    /**
	 */
    @Override
    public List findOLATResourcesForBGContext(final BGContext bgContext) {
        final String q = " select bgcr.resource from org.olat.data.group.context.BGContext2Resource as bgcr where bgcr.groupContext = :context";
        final DBQuery query = db.createQuery(q);
        query.setEntity("context", bgContext);
        return query.list();
    }

    /**
	 */
    @Override
    public void removeBGContextFromResource(final BGContext bgContext, final OLATResource resource) {
        // 1) delete references for this resource
        final String q = " from org.olat.data.group.context.BGContext2Resource as bgcr where bgcr.groupContext = ? and bgcr.resource = ?";
        db.delete(q, new Object[] { bgContext.getKey(), resource.getKey() }, new Type[] { Hibernate.LONG, Hibernate.LONG });
    }

    /**
	 */
    @Override
    public BGContext loadBGContext(final BGContext bgContext) {
        return (BGContext) db.loadObject(bgContext);
    }

    @Override
    public BGContext2Resource getBGContext2ResourceAndSave(OLATResource resource, BGContext bgContext) {
        BGContext2Resource bgContext2Resource = new BGContext2Resource(resource, bgContext);
        db.saveObject(bgContext2Resource);
        return bgContext2Resource;
    }

    /**
     * Special-query for Upgrade-6.2.0.
     */
    // TODO remove that from standard API
    public List<BGContext> getAllBGContext() {
        final StringBuilder q = new StringBuilder();
        q.append(" select context from BGContextImpl as context");
        final DBQuery query = db.createQuery(q.toString());
        return query.list();
    }

}
