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

package org.olat.data.basesecurity;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.data.resource.OLATResourceManager;
import org.olat.data.user.User;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.date.DateUtil;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.icu.util.Calendar;

/**
 * <h3>Description:</h3> The PersistingManager implements the security manager and provide methods to manage identities and user objects based on a database persistence
 * mechanism using hibernate.
 * <p>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class BaseSecurityManager extends BasicManager implements BaseSecurity, Initializable {

    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    private OLATResourceManager orm;
    private String dbVendor = "";
    @Autowired
    private DB database;

    /**
     * [used by spring]
     */
    protected BaseSecurityManager() {
        //
    }

    /**
	 */
    @Override
    public void init() { // called only once at startup and only from one thread
        // init the system level groups and its policies
        initSysGroupAdmin();
        database.intermediateCommit();
        initSysGroupAuthors();
        database.intermediateCommit();
        initSysGroupGroupmanagers();
        database.intermediateCommit();
        initSysGroupUsermanagers();
        database.intermediateCommit();
        initSysGroupUsers();
        database.intermediateCommit();
        initSysGroupAnonymous();
        database.intermediateCommit();
        initSysGroupInstitutionalResourceManager();
        database.intermediateCommit();
    }

    /**
     * OLAT system administrators, root, good, whatever you name it...
     */
    private void initSysGroupAdmin() {
        SecurityGroup adminGroup = findSecurityGroupByName(Constants.GROUP_ADMIN);
        if (adminGroup == null) {
            adminGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_ADMIN);
        }

        // we check everthing by policies, so we must give admins the hasRole
        // permission on the type resource "Admin"
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN);

        // admins have role "authors" by default
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);

        // admins have a groupmanager policy and access permissions to groupmanaging tools
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);

        // admins have a usemanager policy and access permissions to usermanagement tools
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);

        // admins are also regular users
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERS);

        // olat admins have access to all security groups
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, Constants.ORESOURCE_SECURITYGROUPS);

        // and to all courses
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ADMIN, Constants.ORESOURCE_COURSES);

        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.SysinfoController));
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.UserAdminController));
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.UserChangePasswordController));
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.UserCreateController));
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.QuotaController));
        createAndPersistPolicyIfNotExists(adminGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.GenericQuotaEditController));
    }

    /**
     * is not private just to be tested.
     * 
     * @param type
     * @return
     */
    OLATResourceable createOLATResourceable(SecurityResourceTypeEnum type) {
        return OresHelper.createOLATResourceableType(type.name());
    }

    /**
     * Every active user that is an active user is in the user group. exceptions: logonDenied and anonymous users
     */
    private void initSysGroupUsers() {
        SecurityGroup olatuserGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        if (olatuserGroup == null) {
            olatuserGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_OLATUSERS);
        }

        // users have a user policy
        createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERS);

        createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.ChangePasswordController));
        createAndPersistPolicyIfNotExists(olatuserGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.PersonalSettingsController));
    }

    /**
     * Users with access to group context management (groupmanagement that can be used in multiple courses
     */
    private void initSysGroupGroupmanagers() {
        SecurityGroup olatGroupmanagerGroup = findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
        if (olatGroupmanagerGroup == null) {
            olatGroupmanagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_GROUPMANAGERS);
        }
        // gropumanagers have a groupmanager policy and access permissions to groupmanaging tools
        createAndPersistPolicyIfNotExists(olatGroupmanagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
    }

    /**
     * Users with access to user management
     */
    private void initSysGroupUsermanagers() {
        SecurityGroup olatUsermanagerGroup = findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
        if (olatUsermanagerGroup == null) {
            olatUsermanagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_USERMANAGERS);
        }
        // gropumanagers have a groupmanager policy and access permissions to groupmanaging tools
        createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);
        createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.UserAdminController));
        createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS,
                createOLATResourceable(SecurityResourceTypeEnum.UserChangePasswordController));
        createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.UserCreateController));
        createAndPersistPolicyIfNotExists(olatUsermanagerGroup, Constants.PERMISSION_ACCESS, createOLATResourceable(SecurityResourceTypeEnum.GenericQuotaEditController));
    }

    /**
     * Users with access to the authoring parts of the learning ressources repository
     */
    private void initSysGroupAuthors() {
        SecurityGroup olatauthorGroup = findSecurityGroupByName(Constants.GROUP_AUTHORS);
        if (olatauthorGroup == null) {
            olatauthorGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_AUTHORS);
        }
        // authors have a author policy and access permissions to authoring tools
        createAndPersistPolicyIfNotExists(olatauthorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
    }

    /**
     * Users with access to the authoring parts of the learning ressources repository (all resources in his university)
     */
    private void initSysGroupInstitutionalResourceManager() {
        SecurityGroup institutionalResourceManagerGroup = findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
        if (institutionalResourceManagerGroup == null) {
            institutionalResourceManagerGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_INST_ORES_MANAGER);
        }
        // manager have a author policy and access permissions to authoring tools
        createAndPersistPolicyIfNotExists(institutionalResourceManagerGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_INSTORESMANAGER);
        createAndPersistPolicyIfNotExists(institutionalResourceManagerGroup, Constants.PERMISSION_ACCESS,
                createOLATResourceable(SecurityResourceTypeEnum.QuotaController));
        createAndPersistPolicyIfNotExists(institutionalResourceManagerGroup, Constants.PERMISSION_ACCESS,
                createOLATResourceable(SecurityResourceTypeEnum.GenericQuotaEditController));
    }

    /**
     * Unknown users with guest only rights
     */
    private void initSysGroupAnonymous() {
        SecurityGroup guestGroup = findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
        if (guestGroup == null) {
            guestGroup = createAndPersistNamedSecurityGroup(Constants.GROUP_ANONYMOUS);
        }
        // guest(=anonymous) have a guest policy
        createAndPersistPolicyIfNotExists(guestGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
    }

    /**
	 */
    @Override
    public List getGroupsWithPermissionOnOlatResourceable(final String permission, final OLATResourceable olatResourceable) {
        Long oresid = olatResourceable.getResourceableId();
        if (oresid == null) {
            oresid = new Long(0); // TODO: make a method in
        }
        // OLATResorceManager, since this
        // is implementation detail
        final String oresName = olatResourceable.getResourceableTypeName();

        final List res = database.find("select sgi from" + " org.olat.data.basesecurity.SecurityGroupImpl as sgi," + " org.olat.data.basesecurity.PolicyImpl as poi, "
                + OLATResourceImpl.class.getName() + " as ori" + " where poi.securityGroup = sgi and poi.permission = ?" + " and poi.olatResource = ori"
                + " and (ori.resId = ? or ori.resId = 0) and ori.resName = ?", new Object[] { permission, oresid, oresName }, new Type[] { Hibernate.STRING,
                Hibernate.LONG, Hibernate.STRING });
        return res;
    }

    /**
	 */
    @Override
    public List getIdentitiesWithPermissionOnOlatResourceable(final String permission, final OLATResourceable olatResourceable) {
        Long oresid = olatResourceable.getResourceableId();
        if (oresid == null) {
            oresid = new Long(0); // TODO: make a method in
        }
        // OLATResorceManager, since this
        // is implementation detail
        final String oresName = olatResourceable.getResourceableTypeName();

        // if the olatResourceable is not persisted as OLATResource, then the answer
        // is false,
        // therefore we can use the query assuming there is an OLATResource
        final List res = database.find("select distinct im from" + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi,"
                + " org.olat.data.basesecurity.IdentityImpl as im," + " org.olat.data.basesecurity.PolicyImpl as poi, " + OLATResourceImpl.class.getName() + " as ori"
                + " where im = sgmsi.identity" + " and sgmsi.securityGroup = poi.securityGroup" + " and poi.permission = ?" + " and poi.olatResource = ori"
                + " and (ori.resId = ? or ori.resId = 0) and ori.resName = ?",
        // if you have a type right, you autom. have all instance rights
                new Object[] { permission, oresid, oresName }, new Type[] { Hibernate.STRING, Hibernate.LONG, Hibernate.STRING });
        return res;
    }

    /**
	 */
    @Override
    public List getPoliciesOfSecurityGroup(final SecurityGroup secGroup) {
        final List res = database.find("select poi from org.olat.data.basesecurity.PolicyImpl as poi where poi.securityGroup = ?", new Object[] { secGroup.getKey() },
                new Type[] { Hibernate.LONG });
        return res;
    }

    /**
	 */
    @Override
    public List<Policy> getPoliciesOfResource(final OLATResourceable olatResourceable, final SecurityGroup secGroup) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select poi from ").append(PolicyImpl.class.getName()).append(" poi where ").append("(poi.olatResource.resId=:resId or poi.olatResource.resId=0)")
                .append(" and poi.olatResource.resName=:resName");
        if (secGroup != null) {
            sb.append(" and poi.securityGroup=:secGroup");
        }

        final DBQuery query = database.createQuery(sb.toString());
        query.setLong("resId", olatResourceable.getResourceableId());
        query.setString("resName", olatResourceable.getResourceableTypeName());
        if (secGroup != null) {
            query.setEntity("secGroup", secGroup);
        }
        return query.list();
    }

    /**
	 */
    /*
     * MAY BE USED LATER - do not remove please public boolean isGroupPermittedOnResourceable(SecurityGroup secGroup, String permission, OLATResourceable
     * olatResourceable) { Long oresid = olatResourceable.getResourceableId(); if (oresid == null) oresid = new Long(0); //TODO: make a method in OLATResorceManager,
     * since this is implementation detail String oresName = olatResourceable.getResourceableTypeName(); List res = DB.getInstance().find("select count(poi) from"+ "
     * org.olat.data.basesecurity.SecurityGroupImpl as sgi,"+ " org.olat.data.basesecurity.PolicyImpl as poi,"+ " OLATResourceImpl as ori"+ " where sgi.key = ?" + " and
     * poi.securityGroup = sgi and poi.permission = ?"+ " and poi.olatResource = ori" + " and (ori.resId = ? or ori.resId = 0) and ori.resName = ?", new Object[] {
     * secGroup.getKey(), permission, oresid, oresName }, new Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.LONG, Hibernate.STRING } ); Integer cntI =
     * (Integer)res.get(0); int cnt = cntI.intValue(); return (cnt > 0); // can be > 1 if identity is in more the one group having the permission on the olatresourceable
     * }
     */

    @Override
    public boolean isIdentityPermittedOnResourceable(final Identity identity, final String permission, final OLATResourceable olatResourceable) {
        return isIdentityPermittedOnResourceable(identity, permission, olatResourceable, true);
    }

    /**
     * boolean)
     */
    @Override
    public boolean isIdentityPermittedOnResourceable(final Identity identity, final String permission, final OLATResourceable olatResourceable,
            final boolean checkTypeRight) {
        final IdentityImpl iimpl = getImpl(identity);
        Long oresid = olatResourceable.getResourceableId();
        if (oresid == null) {
            oresid = new Long(0); // TODO: make a method in
        }
        // OLATResorceManager, since this
        // is implementation detail
        final String oresName = olatResourceable.getResourceableTypeName();
        // if the olatResourceable is not persisted as OLATResource, then the answer
        // is false,
        // therefore we can use the query assuming there is an OLATResource

        String queryString;
        if (checkTypeRight) {
            queryString = "select count(poi) from" + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi,"
                    + " org.olat.data.basesecurity.PolicyImpl as poi, " + OLATResourceImpl.class.getName() + " as ori"
                    + " where sgmsi.identity = :identitykey and sgmsi.securityGroup =  poi.securityGroup"
                    + " and poi.permission = :permission and poi.olatResource = ori" + " and (ori.resId = :resid or ori.resId = 0) and ori.resName = :resname";
        } else {
            queryString = "select count(poi) from" + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi,"
                    + " org.olat.data.basesecurity.PolicyImpl as poi, " + OLATResourceImpl.class.getName() + " as ori"
                    + " where sgmsi.identity = :identitykey and sgmsi.securityGroup =  poi.securityGroup"
                    + " and poi.permission = :permission and poi.olatResource = ori" + " and (ori.resId = :resid) and ori.resName = :resname";
        }
        final DBQuery query = database.createQuery(queryString);
        query.setLong("identitykey", iimpl.getKey());
        query.setString("permission", permission);
        query.setLong("resid", oresid);
        query.setString("resname", oresName);
        query.setCacheable(true);
        final List res = query.list();
        final Long cntL = (Long) res.get(0);
        return (cntL.longValue() > 0); // can be > 1 if identity is in more the one group having
        // the permission on the olatresourceable
    }

    /**
	 */
    @Override
    public Roles getRoles(final Identity identity) {
        final boolean isAdmin = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN);
        final boolean isAuthor = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
        final boolean isGroupManager = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
        final boolean isUserManager = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);
        final boolean isGuestOnly = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
        final boolean isInstitutionalResourceManager = isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_INSTORESMANAGER);
        final boolean isInvitee = isIdentityInvited(identity);
        final Roles roles = new Roles(isAdmin, isUserManager, isGroupManager, isAuthor, isGuestOnly, isInstitutionalResourceManager, isInvitee);
        return roles;
    }

    /**
     * scalar query : select sgi, poi, ori
     * 
     * @param identity
     * @return List of policies
     */
    @Override
    public List getPoliciesOfIdentity(final Identity identity) {
        final IdentityImpl iimpl = getImpl(identity);
        final List res = database.find("select sgi, poi, ori from" + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi,"
                + " org.olat.data.basesecurity.SecurityGroupImpl as sgi," + " org.olat.data.basesecurity.PolicyImpl as poi, " + OLATResourceImpl.class.getName()
                + " as ori" + " where sgmsi.identity = ? and sgmsi.securityGroup = sgi" + " and poi.securityGroup = sgi and poi.olatResource = ori",
                new Object[] { iimpl.getKey() }, new Type[] { Hibernate.LONG });
        // scalar query, we get a List of Object[]'s
        return res;
    }

    @Override
    public void updatePolicy(final Policy policy, final Date from, final Date to) {
        ((PolicyImpl) policy).setFrom(from);
        ((PolicyImpl) policy).setTo(to);
        database.updateObject(policy);
    }

    /**
	 */
    @Override
    public boolean isIdentityInSecurityGroup(final Identity identity, final SecurityGroup secGroup) {
        if (secGroup == null || identity == null) {
            return false;
        }
        final String queryString = "select count(sgmsi) from  org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi where sgmsi.identity = :identitykey and sgmsi.securityGroup = :securityGroup";
        final DBQuery query = database.createQuery(queryString);
        query.setLong("identitykey", identity.getKey());
        query.setLong("securityGroup", secGroup.getKey());
        query.setCacheable(true);
        final List res = query.list();
        final Long cntL = (Long) res.get(0);
        if (cntL.longValue() != 0 && cntL.longValue() != 1) {
            throw new AssertException("unique n-to-n must always yield 0 or 1");
        }
        return (cntL.longValue() == 1);
    }

    /**
	 */
    @Override
    public SecurityGroup createAndPersistSecurityGroup() {
        final SecurityGroupImpl sgi = new SecurityGroupImpl();
        database.saveObject(sgi);
        return sgi;
    }

    /**
     * Checks for null input.
     */
    @Override
    public void deleteSecurityGroup(SecurityGroup secGroup) {
        if (secGroup == null)
            return;
        // we do not use hibernate cascade="delete", but implement our own, why???
        secGroup = (SecurityGroup) database.loadObject(SecurityGroupImpl.class, secGroup.getKey());
        // 1) delete associated users (need to do it manually, hibernate knows
        // nothing about
        // the membership, modeled manually via many-to-one and not via set)
        database.delete("from org.olat.data.basesecurity.SecurityGroupMembershipImpl as msi where msi.securityGroup.key = ?", new Object[] { secGroup.getKey() },
                new Type[] { Hibernate.LONG });
        // 2) delete all policies
        database.delete("from org.olat.data.basesecurity.PolicyImpl as poi where poi.securityGroup = ?", new Object[] { secGroup.getKey() },
                new Type[] { Hibernate.LONG });
        // 3) delete security group
        database.deleteObject(secGroup);
    }

    /**
	 */
    @Override
    public void addIdentityToSecurityGroup(final Identity identity, final SecurityGroup secGroup) {
        // log.info("identity=" + identity + " secGroup=" + secGroup);
        final SecurityGroupMembershipImpl sgmsi = new SecurityGroupMembershipImpl();
        sgmsi.setIdentity(identity);
        sgmsi.setSecurityGroup(secGroup);
        sgmsi.setLastModified(new Date());
        LOG.info("sgmsi=" + sgmsi);
        database.saveObject(sgmsi);
    }

    /**
	 */
    @Override
    public void removeIdentityFromSecurityGroup(final Identity identity, final SecurityGroup secGroup) {
        final IdentityImpl iimpl = getImpl(identity);
        database.delete("from org.olat.data.basesecurity.SecurityGroupMembershipImpl as msi where msi.identity.key = ? and msi.securityGroup.key = ?", new Object[] {
                iimpl.getKey(), secGroup.getKey() }, new Type[] { Hibernate.LONG, Hibernate.LONG });
    }

    /**
	 */
    @Override
    public Policy createAndPersistPolicy(final SecurityGroup secGroup, final String permission, final OLATResourceable olatResourceable) {
        return createAndPersistPolicy(secGroup, permission, null, null, olatResourceable);
    }

    /**
     * org.olat.system.commons.resource.OLATResourceable)
     */
    @Override
    public Policy createAndPersistPolicy(final SecurityGroup secGroup, final String permission, final Date from, final Date to, final OLATResourceable olatResourceable) {
        final OLATResource olatResource = orm.findOrPersistResourceable(olatResourceable);
        return createAndPersistPolicyWithResource(secGroup, permission, from, to, olatResource);
    }

    /**
     * org.olat.data.resource.OLATResource)
     */
    @Override
    public Policy createAndPersistPolicyWithResource(final SecurityGroup secGroup, final String permission, final OLATResource olatResource) {
        return createAndPersistPolicyWithResource(secGroup, permission, null, null, olatResource);
    }

    /**
     * Creates a policy and persists on the database
     * 
     * java.util.Date, org.olat.data.resource.OLATResource)
     */
    @Override
    public Policy createAndPersistPolicyWithResource(final SecurityGroup secGroup, final String permission, final Date from, final Date to,
            final OLATResource olatResource) {
        final PolicyImpl pi = new PolicyImpl();
        pi.setSecurityGroup(secGroup);
        pi.setOlatResource(olatResource);
        pi.setPermission(permission);
        pi.setFrom(from);
        pi.setTo(to);
        database.saveObject(pi);
        return pi;
    }

    /**
     * Helper method that only creates a policy only if no such policy exists in the database
     * 
     * @param secGroup
     * @param permission
     * @param olatResourceable
     * @return Policy
     */
    private Policy createAndPersistPolicyIfNotExists(final SecurityGroup secGroup, final String permission, final OLATResourceable olatResourceable) {
        final OLATResource olatResource = orm.findOrPersistResourceable(olatResourceable);
        final Policy existingPolicy = findPolicy(secGroup, permission, olatResource);
        if (existingPolicy == null) {
            return createAndPersistPolicy(secGroup, permission, olatResource);
        }
        return existingPolicy;
    }

    Policy findPolicy(final SecurityGroup secGroup, final String permission, final OLATResource olatResource) {
        final Long secKey = secGroup.getKey();
        final Long orKey = olatResource.getKey();

        final List res = database.find(" select poi from org.olat.data.basesecurity.PolicyImpl as poi"
                + " where poi.permission = ? and poi.olatResource = ? and poi.securityGroup = ?", new Object[] { permission, orKey, secKey }, new Type[] {
                Hibernate.STRING, Hibernate.LONG, Hibernate.LONG });
        if (res.size() == 0) {
            return null;
        } else {
            final PolicyImpl poi = (PolicyImpl) res.get(0);
            return poi;
        }
    }

    private void deletePolicy(final Policy policy) {
        database.deleteObject(policy);
    }

    /**
	 */
    @Override
    public void deletePolicy(final SecurityGroup secGroup, final String permission, final OLATResourceable olatResourceable) {
        final OLATResource olatResource = orm.findResourceable(olatResourceable);
        if (olatResource == null) {
            throw new AssertException("cannot delete policy of a null olatresourceable!");
        }
        final Policy p = findPolicy(secGroup, permission, olatResource);
        // fj: introduced strict testing here on purpose
        if (p == null) {
            throw new AssertException("findPolicy returned null, cannot delete policy");
        }
        deletePolicy(p);
    }

    @Override
    public Invitation saveInvitation(final Invitation invitation) {
        database.saveObject(invitation.getSecurityGroup());
        database.saveObject(invitation);
        return invitation;
    }

    /**
	 */
    @Override
    public void updateInvitation(final Invitation invitation) {
        database.updateObject(invitation);
    }

    /**
	 */
    @Override
    public boolean hasInvitationPolicies(final String token, final Date atDate) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(policy) from ").append(PolicyImpl.class.getName()).append(" as policy, ").append(InvitationImpl.class.getName())
                .append(" as invitation ").append(" inner join policy.securityGroup secGroup ").append(" where invitation.securityGroup=secGroup ")
                .append(" and invitation.token=:token");
        if (atDate != null) {
            sb.append(" and (policy.from is null or policy.from<=:date)").append(" and (policy.to is null or policy.to>=:date)");
        }

        final DBQuery query = database.createQuery(sb.toString());
        query.setString("token", token);
        if (atDate != null) {
            query.setDate("date", atDate);
        }

        final Number counter = (Number) query.uniqueResult();
        return counter.intValue() > 0;
    }

    /**
	 */
    @Override
    public Invitation findInvitation(final SecurityGroup secGroup) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ").append(" where invitation.securityGroup=:secGroup ");

        final DBQuery query = database.createQuery(sb.toString());
        query.setEntity("secGroup", secGroup);

        final List<Invitation> invitations = query.list();
        if (invitations.isEmpty()) {
            return null;
        }
        return invitations.get(0);
    }

    /**
	 */
    @Override
    public Invitation findInvitation(final String token) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ").append(" where invitation.token=:token");

        final DBQuery query = database.createQuery(sb.toString());
        query.setString("token", token);

        final List<Invitation> invitations = query.list();
        if (invitations.isEmpty()) {
            return null;
        }
        return invitations.get(0);
    }

    /**
	 */
    @Override
    public boolean isIdentityInvited(final Identity identity) {
        final StringBuilder sb = new StringBuilder();
        sb.append("select count(invitation) from ").append(InvitationImpl.class.getName()).append(" as invitation ")
                .append("inner join invitation.securityGroup secGroup ").append("where secGroup in (").append(" select membership.securityGroup from ")
                .append(SecurityGroupMembershipImpl.class.getName()).append(" as membership").append("  where membership.identity=:identity").append(")");

        final DBQuery query = database.createQuery(sb.toString());
        query.setEntity("identity", identity);

        final Number invitations = (Number) query.uniqueResult();
        return invitations.intValue() > 0;
    }

    /**
	 */
    @Override
    public void deleteInvitation(final Invitation invitation) {
        database.deleteObject(invitation);
        deleteSecurityGroup(invitation.getSecurityGroup());
    }

    /**
	 */
    @Override
    public List<Invitation> findExpiredInvitations(Date limitDate) {
        final Date currentTime = Calendar.getInstance().getTime();
        final StringBuilder sb = new StringBuilder();
        sb.append("select invitation from ").append(InvitationImpl.class.getName()).append(" as invitation ").append(" inner join invitation.securityGroup secGroup ")
                .append(" where invitation.creationDate<=:dateLimit")
                // someone can create an invitation but not add it to a policy within millisecond
                .append(" and secGroup not in (")
                // select all valid policies from this security group
                .append("  select policy.securityGroup from ").append(PolicyImpl.class.getName()).append(" as policy ")
                .append("   where (policy.from is null or policy.from<=:currentDate)").append("   and (policy.to is null or policy.to>=:currentDate)").append("  )");

        final DBQuery query = database.createQuery(sb.toString());
        query.setTimestamp("currentDate", currentTime);
        query.setTimestamp("dateLimit", limitDate);
        @SuppressWarnings("unchecked")
        final List<Invitation> oldInvitations = query.list();
        return oldInvitations;
    }

    private IdentityImpl getImpl(final Identity identity) {
        // since we are a persistingmanager, we also only accept real identities
        if (!(identity instanceof IdentityImpl)) {
            throw new AssertException("identity was not of type identityimpl, but " + identity.getClass().getName());
        }
        final IdentityImpl iimpl = (IdentityImpl) identity;
        return iimpl;
    }

    /**
     * @param username
     *            the username
     * @param user
     *            the presisted User
     * @param authusername
     *            the username used as authentication credential (=username for provider "OLAT")
     * @param provider
     *            the provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
     * @param credential
     *            the credentials or null if not used
     * @return Identity
     */
    @Override
    public Identity createAndPersistIdentity(final String username, final User user, final String provider, final String authusername, final String credential) {
        final IdentityImpl iimpl = new IdentityImpl(username, user);
        database.saveObject(iimpl);
        if (provider != null) {
            createAndPersistAuthentication(iimpl, provider, authusername, credential);
        }
        return iimpl;
    }

    /**
     * @param username
     *            the username
     * @param user
     *            the unpresisted User
     * @param authusername
     *            the username used as authentication credential (=username for provider "OLAT")
     * @param provider
     *            the provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
     * @param credential
     *            the credentials or null if not used
     * @return Identity
     */
    @Override
    public Identity createAndPersistIdentityAndUser(final String username, final User user, final String provider, final String authusername, final String credential) {
        LOG.info("createAndPersistIdentityAndUser - username: " + username);
        database.saveObject(user);
        final IdentityImpl iimpl = new IdentityImpl(username, user);
        // setLastLoginForDays_JustForTesting(iimpl, 1000);
        database.saveObject(iimpl);
        if (provider != null) {
            createAndPersistAuthentication(iimpl, provider, authusername, credential);
        }
        return iimpl;
    }

    /**
     * TODO: LD: remove this a.s.p. Used to add users with last login date older than 2 years ago, used for delete users workflow.
     */
    private void setLastLoginForDays_JustForTesting(final IdentityImpl iimpl, int numOfDays) {
        Date threeYearsAgo = DateUtil.getDayBefore(new Date(), numOfDays);
        String dateString = DateUtil.extractDate(threeYearsAgo, new Locale("DE"));
        // System.out.println("dateString : " + dateString);
        iimpl.setLastLogin(threeYearsAgo);
    }

    @Override
    public List<Identity> getIdentitiesOfSecurityGroup(final SecurityGroup secGroup) {
        return getIdentitiesOfSecurityGroup(secGroup, false);
    }

    public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup, boolean sortedByAddDate) {
        if (secGroup == null) {
            throw new AssertException("getIdentitiesOfSecurityGroup: ERROR secGroup was null !!");
        }
        final StringBuilder queryString = new StringBuilder();
        queryString.append("select ii from" + " org.olat.data.basesecurity.IdentityImpl as ii inner join fetch ii.user as iuser, "
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi " + " where sgmsi.securityGroup = ? and sgmsi.identity = ii");
        if (sortedByAddDate) {
            queryString.append(" order by sgmsi.lastModified");
        }
        @SuppressWarnings("unchecked")
        final List<Identity> idents = database.find(queryString.toString(), new Object[] { secGroup.getKey() }, new Type[] { Hibernate.LONG });
        return idents;
    }

    @Override
    public List getIdentitiesAndDateOfSecurityGroup(final SecurityGroup secGroup) {
        return getIdentitiesAndDateOfSecurityGroup(secGroup, false);
    }

    /**
     * @param sortedByAddDate
     *            true= return list of idenities sorted by added date
     */
    @Override
    public List getIdentitiesAndDateOfSecurityGroup(final SecurityGroup secGroup, final boolean sortedByAddDate) {
        final StringBuilder queryString = new StringBuilder();
        queryString.append("select ii, sgmsi.lastModified from" + " org.olat.data.basesecurity.IdentityImpl as ii inner join fetch ii.user as iuser, "
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi " + " where sgmsi.securityGroup = ? and sgmsi.identity = ii");
        if (sortedByAddDate) {
            queryString.append(" order by sgmsi.lastModified");
        }
        final List identAndDate = database.find(queryString.toString(), new Object[] { secGroup.getKey() }, new Type[] { Hibernate.LONG });
        return identAndDate;
    }

    /**
	 */
    @Override
    public Date getSecurityGroupJoinDateForIdentity(final SecurityGroup secGroup, final Identity identity) {
        final String query = "select creationDate from " + "  org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgi "
                + " where sgi.securityGroup = :secGroup and sgi.identity = :identId";

        final DBQuery dbq = database.createQuery(query);
        dbq.setLong("identId", identity.getKey().longValue());
        dbq.setLong("secGroup", secGroup.getKey());
        final List result = dbq.list();
        if (result.size() == 0) {
            return null;
        } else {
            return (Date) result.get(0);
        }
    }

    /**
	 */
    @Override
    public int countIdentitiesOfSecurityGroup(final SecurityGroup secGroup) {
        final String q = "select count(sgm) from org.olat.data.basesecurity.SecurityGroupMembershipImpl sgm where sgm.securityGroup = :group";
        final DBQuery query = database.createQuery(q);
        query.setEntity("group", secGroup);
        final int result = ((Long) query.list().get(0)).intValue();
        return result;
    }

    /**
	 */
    @Override
    public SecurityGroup createAndPersistNamedSecurityGroup(final String groupName) {
        final SecurityGroup secG = createAndPersistSecurityGroup();
        final NamedGroupImpl ngi = new NamedGroupImpl(groupName, secG);
        database.saveObject(ngi);
        return secG;
    }

    /**
	 */
    @Override
    public SecurityGroup findSecurityGroupByName(final String securityGroupName) {
        final List group = database.find("select sgi from" + " org.olat.data.basesecurity.NamedGroupImpl as ngroup,"
                + " org.olat.data.basesecurity.SecurityGroupImpl as sgi" + " where ngroup.groupName = ? and ngroup.securityGroup = sgi",
                new Object[] { securityGroupName }, new Type[] { Hibernate.STRING });
        final int size = group.size();
        if (size == 0) {
            return null;
        }
        if (size != 1) {
            throw new AssertException("non unique name in namedgroup: " + securityGroupName);
        }
        final SecurityGroup sg = (SecurityGroup) group.get(0);
        return sg;
    }

    /**
     * Used only in OLATUpgrade_5_2_0
     * 
     * @deprecated
     */
    public void deleteNamedGroup(String groupName) {
        database.delete("from org.olat.data.basesecurity.NamedGroupImpl as ngroup where ngroup.groupName = ?", new Object[] { groupName },// so far:
                new Type[] { Hibernate.STRING });
    }

    /**
	 */
    @Override
    public Identity findIdentityByName(final String identityName) {
        if (identityName == null) {
            throw new AssertException("findIdentitybyName: name was null");
        }
        final List identities = database.find("select ident from org.olat.data.basesecurity.IdentityImpl as ident where ident.name = ?", new Object[] { identityName },
                new Type[] { Hibernate.STRING });
        final int size = identities.size();
        if (size == 0) {
            return null;
        }
        if (size != 1) {
            throw new AssertException("non unique name in identites: " + identityName);
        }
        final Identity identity = (Identity) identities.get(0);
        return identity;
    }

    /**
     * Used only in OLATUpgrade_6_1_1
     * 
     * @deprecated
     */
    public List<Identity> getDeletedIdentitiesByName(String identityName) {
        final List identities = database.find("select ident from org.olat.data.basesecurity.IdentityImpl as ident where ident.name like ? and ident.status = ?",
                new Object[] { identityName, Identity.STATUS_DELETED }, new Type[] { Hibernate.STRING, Hibernate.INTEGER });
        return identities;
    }

    /**
	 */
    @Override
    public Identity loadIdentityByKey(final Long identityKey) {
        if (identityKey == null) {
            throw new AssertException("findIdentitybyKey: key is null");
        }
        if (identityKey.equals(Long.valueOf(0))) {
            return null;
        }
        return (Identity) database.loadObject(IdentityImpl.class, identityKey);
    }

    /**
	 */
    @Override
    public Identity loadIdentityByKey(final Long identityKey, final boolean strict) {
        if (strict) {
            return loadIdentityByKey(identityKey);
        }

        final String queryStr = "select ident from org.olat.data.basesecurity.IdentityImpl as ident where ident.key=:identityKey";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setLong("identityKey", identityKey);
        final List<Identity> identities = dbq.list();
        if (identities.size() == 1) {
            return identities.get(0);
        }
        return null;
    }

    /**
	 */
    @Override
    public Long countUniqueUserLoginsSince(final Date lastLoginLimit) {
        final String queryStr = "Select count(ident) from org.olat.data.basesecurity.Identity as ident where "
                + "ident.lastLogin > :lastLoginLimit and ident.lastLogin != ident.creationDate";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("lastLoginLimit", lastLoginLimit);
        final List res = dbq.list();
        final Long cntL = (Long) res.get(0);
        return cntL;
    }

    /**
	 */
    @Override
    public List<Authentication> getAuthentications(final Identity identity) {
        return database.find("select auth from  org.olat.data.basesecurity.AuthenticationImpl as auth " + "inner join fetch auth.identity as ident where ident.key = ?",
                new Object[] { identity.getKey() }, new Type[] { Hibernate.LONG });
    }

    /**
	 */
    @Override
    public Authentication createAndPersistAuthentication(final Identity ident, final String provider, final String authUserName, final String credential) {
        final AuthenticationImpl authImpl = new AuthenticationImpl(ident, provider, authUserName, credential);
        database.saveObject(authImpl);
        return authImpl;
    }

    /**
	 */
    @Override
    public Authentication findAuthentication(final Identity identity, final String provider) {
        if (identity == null) {
            throw new IllegalArgumentException("identity must not be null");
        }
        final List results = database.find("select auth from org.olat.data.basesecurity.AuthenticationImpl as auth where auth.identity.key = ? and auth.provider = ?",
                new Object[] { identity.getKey(), provider }, new Type[] { Hibernate.LONG, Hibernate.STRING });
        if (results == null || results.size() == 0) {
            return null;
        }
        if (results.size() > 1) {
            throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
        }
        return (Authentication) results.get(0);
    }

    /**
	 */
    @Override
    public void deleteAuthentication(final Authentication auth) {
        database.deleteObject(auth);
    }

    /**
	 */
    @Override
    public Authentication findAuthenticationByAuthusername(final String authusername, final String provider) {
        final List results = database.find("from org.olat.data.basesecurity.AuthenticationImpl as auth where auth.provider = ? and auth.authusername = ?", new Object[] {
                provider, authusername }, new Type[] { Hibernate.STRING, Hibernate.STRING });
        if (results.size() == 0) {
            return null;
        }
        if (results.size() != 1) {
            throw new AssertException(
                    "more than one entry for the a given authusername and provider, should never happen (even db has a unique constraint on those columns combined) ");
        }
        final Authentication auth = (Authentication) results.get(0);
        return auth;
    }

    /**
     * org.olat.data.basesecurity.PermissionOnResourceable[], java.lang.String[], java.util.Date, java.util.Date)
     */
    @Override
    public List getVisibleIdentitiesByPowerSearch(final String login, final Map<String, String> userproperties, final boolean userPropertiesAsIntersectionSearch,
            final SecurityGroup[] groups, final PermissionOnResourceable[] permissionOnResources, final String[] authProviders, final Date createdAfter,
            final Date createdBefore) {
        return getIdentitiesByPowerSearch(login, userproperties, userPropertiesAsIntersectionSearch, groups, permissionOnResources, authProviders, createdAfter,
                createdBefore, null, null, Identity.STATUS_VISIBLE_LIMIT);
    }

    /**
     * org.olat.data.basesecurity.PermissionOnResourceable[], java.lang.String[], java.util.Date, java.util.Date, java.lang.Integer)
     */
    @Override
    public List getIdentitiesByPowerSearch(String login, final Map<String, String> userproperties, final boolean userPropertiesAsIntersectionSearch,
            final SecurityGroup[] groups, final PermissionOnResourceable[] permissionOnResources, final String[] authProviders, final Date createdAfter,
            final Date createdBefore, final Date userLoginAfter, final Date userLoginBefore, final Integer status) {
        final boolean hasGroups = (groups != null && groups.length > 0);
        final boolean hasPermissionOnResources = (permissionOnResources != null && permissionOnResources.length > 0);
        final boolean hasAuthProviders = (authProviders != null && authProviders.length > 0);

        // select identity and inner join with user to optimize query
        StringBuilder sb = new StringBuilder();
        if (hasAuthProviders) {
            // I know, it looks wrong but I need to do the join reversed since it is not possible to
            // do this query with a left join that starts with the identity using hibernate HQL. A left
            // or right join is necessary since it is totally ok to have null values as authentication
            // providers (e.g. when searching for users that do not have any authentication providers at all!).
            // It took my quite a while to make this work, so think twice before you change anything here!
            sb = new StringBuilder("select distinct ident from org.olat.data.basesecurity.AuthenticationImpl as auth right join auth.identity as ident ");
        } else {
            sb = new StringBuilder("select distinct ident from org.olat.data.basesecurity.Identity as ident ");
        }
        // In any case join with the user. Don't join-fetch user, this breaks the query
        // because of the user fields (don't know exactly why this behaves like
        // this)
        sb.append(" join ident.user as user ");

        if (hasGroups) {
            // join over security group memberships
            sb.append(" ,org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi ");

        }
        if (hasPermissionOnResources) {
            // join over policies
            sb.append(" ,org.olat.data.basesecurity.SecurityGroupMembershipImpl as policyGroupMembership ");
            sb.append(" ,org.olat.data.basesecurity.PolicyImpl as policy, ");
            sb.append(OLATResourceImpl.class.getName()).append(" as resource ");
        }

        // complex where clause only when values are available
        if (login != null || (userproperties != null && !userproperties.isEmpty()) || createdAfter != null || createdBefore != null || hasAuthProviders || hasGroups
                || hasPermissionOnResources || status != null) {
            sb.append(" where ");
            boolean needsAnd = false;
            boolean needsUserPropertiesJoin = false;

            // treat login and userProperties as one element in this query
            if (login != null && (userproperties != null && !userproperties.isEmpty())) {
                sb.append(" ( ");
            }
            // append queries for user attributes
            if (login != null) {
                login = makeFuzzyQueryString(login);
                if (login.contains("_") && (dbVendor.equals("hsqldb") || dbVendor.equals("oracle"))) {
                    // hsqldb needs special ESCAPE sequence to search for escaped strings
                    sb.append(" ident.name like :login ESCAPE '\\'");
                } else {
                    sb.append(" ident.name like :login");
                }
                // if user fields follow a join element is needed
                needsUserPropertiesJoin = true;
                // at least one user field used, after this and is required
                needsAnd = true;
            }

            // append queries for user fields
            if (userproperties != null && !userproperties.isEmpty()) {
                final HashMap<String, String> emailProperties = new HashMap<String, String>();
                final HashMap<String, String> otherProperties = new HashMap<String, String>();

                // split the user fields into two groups
                for (final String key : userproperties.keySet()) {
                    if (key.toLowerCase().contains("email")) {
                        emailProperties.put(key, userproperties.get(key));
                    } else {
                        otherProperties.put(key, userproperties.get(key));
                    }
                }

                // handle email fields special: search in all email fields
                if (!emailProperties.isEmpty()) {
                    needsUserPropertiesJoin = checkIntersectionInUserProperties(sb, needsUserPropertiesJoin, userPropertiesAsIntersectionSearch);
                    final boolean moreThanOne = emailProperties.size() > 1;
                    if (moreThanOne) {
                        sb.append("(");
                    }
                    boolean needsOr = false;
                    for (final String key : emailProperties.keySet()) {
                        if (needsOr) {
                            sb.append(" or ");
                        }
                        sb.append(" user.properties['").append(key).append("'] like :").append(key).append("_value ");
                        if (dbVendor.equals("hsqldb") || dbVendor.equals("oracle")) {
                            sb.append(" escape '\\'");
                        }
                        needsOr = true;
                    }
                    if (moreThanOne) {
                        sb.append(")");
                    }
                    // cleanup
                    emailProperties.clear();
                }

                // add other fields
                for (final String key : otherProperties.keySet()) {
                    needsUserPropertiesJoin = checkIntersectionInUserProperties(sb, needsUserPropertiesJoin, userPropertiesAsIntersectionSearch);
                    sb.append(" user.properties['").append(key).append("'] like :").append(key).append("_value ");
                    if (dbVendor.equals("hsqldb") || dbVendor.equals("oracle")) {
                        sb.append(" escape '\\'");
                    }
                    needsAnd = true;
                }
                // cleanup
                otherProperties.clear();
                // at least one user field used, after this and is required
                needsAnd = true;
            }
            // end of user fields and login part
            if (login != null && (userproperties != null && !userproperties.isEmpty())) {
                sb.append(" ) ");
            }
            // now continue with the other elements. They are joined with an AND connection

            // append query for named security groups
            if (hasGroups) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" (");
                for (int i = 0; i < groups.length; i++) {
                    sb.append(" sgmsi.securityGroup=:group_").append(i);
                    if (i < (groups.length - 1)) {
                        sb.append(" or ");
                    }
                }
                sb.append(") ");
                sb.append(" and sgmsi.identity=ident ");
            }

            // append query for policies
            if (hasPermissionOnResources) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" (");
                for (int i = 0; i < permissionOnResources.length; i++) {
                    sb.append(" (");
                    sb.append(" policy.permission=:permission_").append(i);
                    sb.append(" and policy.olatResource = resource ");
                    sb.append(" and resource.resId = :resourceId_").append(i);
                    sb.append(" and resource.resName = :resourceName_").append(i);
                    sb.append(" ) ");
                    if (i < (permissionOnResources.length - 1)) {
                        sb.append(" or ");
                    }
                }
                sb.append(") ");
                sb.append(" and policy.securityGroup=policyGroupMembership.securityGroup ");
                sb.append(" and policyGroupMembership.identity=ident ");
            }

            // append query for authentication providers
            if (hasAuthProviders) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" (");
                for (int i = 0; i < authProviders.length; i++) {
                    // special case for null auth provider
                    if (authProviders[i] == null) {
                        sb.append(" auth is null ");
                    } else {
                        sb.append(" auth.provider=:authProvider_").append(i);
                    }
                    if (i < (authProviders.length - 1)) {
                        sb.append(" or ");
                    }
                }
                sb.append(") ");
            }

            // append query for creation date restrictions
            if (createdAfter != null) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" ident.creationDate >= :createdAfter ");
            }
            if (createdBefore != null) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" ident.creationDate <= :createdBefore ");
            }
            if (userLoginAfter != null) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" ident.lastLogin >= :lastloginAfter ");
            }
            if (userLoginBefore != null) {
                needsAnd = checkAnd(sb, needsAnd);
                sb.append(" ident.lastLogin <= :lastloginBefore ");
            }

            if (status != null) {
                if (status.equals(Identity.STATUS_VISIBLE_LIMIT)) {
                    // search for all status smaller than visible limit
                    needsAnd = checkAnd(sb, needsAnd);
                    sb.append(" ident.status < :status ");
                } else {
                    // search for certain status
                    needsAnd = checkAnd(sb, needsAnd);
                    sb.append(" ident.status = :status ");
                }
            }
        }

        // create query object now from string
        final String query = sb.toString();
        final DBQuery dbq = database.createQuery(query);

        // add user attributes
        if (login != null) {
            dbq.setString("login", login);
        }

        // add user properties attributes
        if (userproperties != null && !userproperties.isEmpty()) {
            for (final String key : userproperties.keySet()) {
                String value = userproperties.get(key);
                value = makeFuzzyQueryString(value);
                dbq.setString(key + "_value", value.toLowerCase());
            }
        }

        // add named security group names
        if (hasGroups) {
            for (int i = 0; i < groups.length; i++) {
                final SecurityGroupImpl group = (SecurityGroupImpl) groups[i]; // need to work with impls
                dbq.setEntity("group_" + i, group);
            }
        }

        // add policies
        if (hasPermissionOnResources) {
            for (int i = 0; i < permissionOnResources.length; i++) {
                final PermissionOnResourceable permissionOnResource = permissionOnResources[i];
                dbq.setString("permission_" + i, permissionOnResource.getPermission());
                final Long id = permissionOnResource.getOlatResourceable().getResourceableId();
                dbq.setLong("resourceId_" + i, (id == null ? 0 : id.longValue()));
                dbq.setString("resourceName_" + i, permissionOnResource.getOlatResourceable().getResourceableTypeName());
            }
        }

        // add authentication providers
        if (hasAuthProviders) {
            for (int i = 0; i < authProviders.length; i++) {
                final String authProvider = authProviders[i];
                if (authProvider != null) {
                    dbq.setString("authProvider_" + i, authProvider);
                }
                // ignore null auth provider, already set to null in query
            }
        }

        // add date restrictions
        if (createdAfter != null) {
            dbq.setDate("createdAfter", createdAfter);
        }
        if (createdBefore != null) {
            dbq.setDate("createdBefore", createdBefore);
        }
        if (userLoginAfter != null) {
            dbq.setDate("lastloginAfter", userLoginAfter);
        }
        if (userLoginBefore != null) {
            dbq.setDate("lastloginBefore", userLoginBefore);
        }

        if (status != null) {
            dbq.setInteger("status", status);
        }
        // execute query
        return dbq.list();
    }

    @Override
    public List<Long> getActiveIdentityIds() {
        final String query = "select key from org.olat.data.basesecurity.Identity as ident where ident.status = :status";
        final DBQuery dbq = database.createQuery(query);
        dbq.setInteger("status", Identity.STATUS_ACTIV);
        return dbq.list();
    }

    /**
     * @param dbVendor
     */
    public void setDbVendor(final String dbVendor) {
        this.dbVendor = dbVendor;
    }

    /**
	 */
    @Override
    public boolean isIdentityVisible(final String identityName) {
        if (identityName == null) {
            throw new AssertException("findIdentitybyName: name was null");
        }
        final String queryString = "select count(ident) from org.olat.data.basesecurity.IdentityImpl as ident where ident.name = :identityName and ident.status < :status";
        final DBQuery dbq = database.createQuery(queryString);
        dbq.setString("identityName", identityName);
        dbq.setInteger("status", Identity.STATUS_VISIBLE_LIMIT);
        final List res = dbq.list();
        final Long cntL = (Long) res.get(0);
        return (cntL.longValue() > 0);
    }

    private boolean checkAnd(final StringBuilder sb, final boolean needsAnd) {
        if (needsAnd) {
            sb.append(" and ");
        }
        return true;
    }

    private boolean checkIntersectionInUserProperties(final StringBuilder sb, final boolean needsJoin, final boolean userPropertiesAsIntersectionSearch) {
        if (needsJoin) {
            if (userPropertiesAsIntersectionSearch) {
                sb.append(" and ");
            } else {
                sb.append(" or ");
            }
        }
        return true;
    }

    /**
     * Helper method that replaces * with % and appends and prepends % to the string to make fuzzy SQL match when using like
     * 
     * @param email
     * @return fuzzized string
     */
    private String makeFuzzyQueryString(String string) {
        // By default only fuzzyfy at the end. Usually it makes no sense to do a
        // fuzzy search with % at the beginning, but it makes the query very very
        // slow since it can not use any index and must perform a fulltext search.
        // User can always use * to make it a really fuzzy search query
        string = string.replace('*', '%');
        string = string + "%";
        // with 'LIKE' the character '_' is a wildcard which matches exactly one character.
        // To test for literal instances of '_', we have to escape it.
        string = string.replace("_", "\\_");
        return string;
    }

    /**
	 */
    @Override
    public void saveIdentityStatus(Identity identity, final Integer status) {
        // FIXME: cg: would be nice if the updated identity is returned. no loading required afterwards.
        identity = (Identity) database.loadObject(identity.getClass(), identity.getKey());
        identity.setStatus(status);
        database.updateObject(identity);
    }

    @Override
    public List<SecurityGroup> getSecurityGroupsForIdentity(final Identity identity) {
        final List<SecurityGroup> secGroups = database.find("select sgi from" + " org.olat.data.basesecurity.SecurityGroupImpl as sgi,"
                + " org.olat.data.basesecurity.SecurityGroupMembershipImpl as sgmsi " + " where sgmsi.securityGroup = sgi and sgmsi.identity = ?",
                new Object[] { identity.getKey() }, new Type[] { Hibernate.LONG });
        return secGroups;
    }

    /**
     * Persists the given user, creates an identity for it and adds the user to the users system group
     * 
     * @param loginName
     * @param pwd
     *            null: no OLAT authentication is generated. If not null, the password will be encrypted and and an OLAT authentication is generated.
     * @param newUser
     *            unpersisted users
     * @return Identity
     */
    public Identity createAndPersistIdentityAndUserWithUserGroup(final String loginName, final String pwd, final User newUser) {
        final Identity ident = createAndPersistIdentityAndUser(loginName, pwd, newUser);
        // Add user to system users group
        final SecurityGroup olatuserGroup = findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        addIdentityToSecurityGroup(ident, olatuserGroup);
        return ident;
    }

    /**
     * Persists the given user and creates an identity for it
     * 
     * @param loginName
     * @param pwd
     *            null: no OLAT authentication is generated. If not null, the password will be encrypted and and an OLAT authentication is generated.
     * @param newUser
     *            unpersisted user
     * @return Identity
     */
    private Identity createAndPersistIdentityAndUser(final String loginName, final String pwd, final User newUser) {
        Identity ident = null;
        if (pwd == null) {
            // when no password is used the provider must be set to null to not generate
            // an OLAT authentication token. See method doku.
            ident = createAndPersistIdentityAndUser(loginName, newUser, null, null, null);
        } else {
            ident = createAndPersistIdentityAndUser(loginName, newUser, AUTHENTICATION_PROVIDER_OLAT, loginName, Encoder.bCryptEncode(pwd));
        }
        // TODO: Tracing message
        return ident;
    }

}
