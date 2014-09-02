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
package org.olat.lms.core.notification.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.PermissionOnResourceable;
import org.olat.data.basesecurity.Policy;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.resource.OLATResource;
import org.olat.data.user.User;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 11.01.2012 <br>
 * 
 * @author lavinia
 */
public class BaseSecurityNotificationMock implements BaseSecurity {

    @Autowired
    DaoObjectMother daoObjectMother;

    @Override
    public void init() {
        // not implemented
    }

    @Override
    public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable) {
        return false;
    }

    @Override
    public Roles getRoles(Identity identity) {
        return null;
    }

    @Override
    public boolean isIdentityPermittedOnResourceable(Identity identity, String permission, OLATResourceable olatResourceable, boolean checkTypeRight) {
        return false;
    }

    @Override
    public boolean isIdentityInSecurityGroup(Identity identity, SecurityGroup secGroup) {
        return false;
    }

    @Override
    public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesOfSecurityGroup(SecurityGroup secGroup, boolean sortedByAddDate) {
        return null;
    }

    @Override
    public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup) {
        return null;
    }

    @Override
    public List<Object[]> getIdentitiesAndDateOfSecurityGroup(SecurityGroup secGroup, boolean sortedByAddDate) {
        return null;
    }

    @Override
    public Date getSecurityGroupJoinDateForIdentity(SecurityGroup secGroup, Identity identity) {
        return null;
    }

    @Override
    public SecurityGroup findSecurityGroupByName(String securityGroupName) {
        return null;
    }

    @Override
    public void deleteNamedGroup(String string) {
        // not implemented
    }

    @Override
    public Identity findIdentityByName(String identityName) {
        return daoObjectMother.getEventCreatorIdentity();
    }

    @Override
    public List<Identity> getDeletedIdentitiesByName(String identityName) {
        return null;
    }

    @Override
    public Identity loadIdentityByKey(Long identityKey) {
        return null;
    }

    @Override
    public Identity loadIdentityByKey(Long identityKey, boolean strict) {
        return null;
    }

    @Override
    public Long countUniqueUserLoginsSince(Date lastLoginLimit) {
        return null;
    }

    @Override
    public int countIdentitiesOfSecurityGroup(SecurityGroup secGroup) {
        return 0;
    }

    @Override
    public Identity createAndPersistIdentity(String username, User user, String provider, String authusername, String credential) {
        return null;
    }

    @Override
    public Identity createAndPersistIdentityAndUser(String username, User user, String provider, String authusername, String credential) {
        return null;
    }

    @Override
    public Identity createAndPersistIdentityAndUserWithUserGroup(String loginName, String pwd, User newUser) {
        return null;
    }

    @Override
    public List<Authentication> getAuthentications(Identity identity) {
        return null;
    }

    @Override
    public Authentication findAuthentication(Identity identity, String provider) {
        return null;
    }

    @Override
    public Authentication createAndPersistAuthentication(Identity identity, String provider, String authUsername, String credential) {
        return null;
    }

    @Override
    public void deleteAuthentication(Authentication authentication) {
        // not implemented
    }

    @Override
    public SecurityGroup createAndPersistSecurityGroup() {
        return null;
    }

    @Override
    public SecurityGroup createAndPersistNamedSecurityGroup(String groupName) {
        return null;
    }

    @Override
    public void deleteSecurityGroup(SecurityGroup secGroup) {
        // not implemented
    }

    @Override
    public void addIdentityToSecurityGroup(Identity identity, SecurityGroup secGroup) {
        // not implemented
    }

    @Override
    public void removeIdentityFromSecurityGroup(Identity identity, SecurityGroup secGroup) {
        // not implemented
    }

    @Override
    public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable) {
        return null;
    }

    @Override
    public Policy createAndPersistPolicy(SecurityGroup secGroup, String permission, Date from, Date to, OLATResourceable olatResourceable) {
        return null;
    }

    @Override
    public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, OLATResource olatResource) {
        return null;
    }

    @Override
    public Policy createAndPersistPolicyWithResource(SecurityGroup secGroup, String permission, Date from, Date to, OLATResource olatResource) {
        return null;
    }

    @Override
    public Invitation saveInvitation(Invitation invitation) {
        return null;
    }

    @Override
    public void updateInvitation(Invitation invitation) {
        // not implemented
    }

    @Override
    public boolean hasInvitationPolicies(String token, Date atDate) {
        return false;
    }

    @Override
    public Invitation findInvitation(SecurityGroup secGroup) {
        return null;
    }

    @Override
    public Invitation findInvitation(String token) {
        return null;
    }

    @Override
    public boolean isIdentityInvited(Identity identity) {
        return false;
    }

    @Override
    public void deleteInvitation(Invitation invitation) {
        // not implemented
    }

    @Override
    public void deletePolicy(SecurityGroup secGroup, String permission, OLATResourceable olatResourceable) {
        // not implemented
    }

    @Override
    public List<Policy> getPoliciesOfSecurityGroup(SecurityGroup secGroup) {
        return null;
    }

    @Override
    public List<Policy> getPoliciesOfResource(OLATResourceable resource, SecurityGroup securityGroup) {
        return null;
    }

    @Override
    public void updatePolicy(Policy policy, Date from, Date to) {
        // not implemented
    }

    @Override
    public List<SecurityGroup> getGroupsWithPermissionOnOlatResourceable(String permission, OLATResourceable olatResourceable) {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesWithPermissionOnOlatResourceable(String permission, OLATResourceable olatResourceable) {
        return null;
    }

    @Override
    public List<Identity> getPoliciesOfIdentity(Identity identity) {
        return null;
    }

    @Override
    public Authentication findAuthenticationByAuthusername(String authusername, String provider) {
        return null;
    }

    @Override
    public List<Identity> getVisibleIdentitiesByPowerSearch(String login, Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch,
            SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter, Date createdBefore) {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesByPowerSearch(String login, Map<String, String> userProperties, boolean userPropertiesAsIntersectionSearch,
            SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders, Date createdAfter, Date createdBefore, Date userLoginAfter,
            Date userLoginBefore, Integer status) {
        return null;
    }

    @Override
    public List<Long> getActiveIdentityIds() {
        return null;
    }

    @Override
    public void saveIdentityStatus(Identity identity, Integer status) {
        // not implemented
    }

    @Override
    public boolean isIdentityVisible(String identityName) {
        return false;
    }

    @Override
    public List<SecurityGroup> getSecurityGroupsForIdentity(Identity identity) {
        return null;
    }

    @Override
    public List<Invitation> findExpiredInvitations(Date limitDate) {
        return null;
    }

}
