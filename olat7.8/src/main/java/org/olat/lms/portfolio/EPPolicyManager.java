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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.lms.portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.Policy;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.group.BusinessGroup;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.data.resource.OLATResource;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.portfolio.EPMapPolicy.Type;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * manager for all map share and policy handling
 * <P>
 * Initial Date: 30.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Service
public class EPPolicyManager extends BasicManager {

    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private BusinessGroupService groupService;

    private EPPolicyManager() {
        super();
    }

    /**
     * Return a list of wrapper containing the read policies of the map
     * 
     * @param map
     */
    public List<EPMapPolicy> getMapPolicies(final PortfolioStructureMap map) {
        final OLATResource resource = map.getOlatResource();
        final List<EPMapPolicy> policyWrappers = new ArrayList<EPMapPolicy>();
        final List<Policy> policies = securityManager.getPoliciesOfResource(resource, null);
        for (final Policy policy : policies) {
            if (!policy.getPermission().contains(Constants.PERMISSION_READ)) {
                continue;
            }

            EPMapPolicy wrapper = getWrapperWithSamePolicy(policy, policyWrappers);
            if (wrapper == null) {
                wrapper = new EPMapPolicy();
                wrapper.setTo(policy.getTo());
                wrapper.setFrom(policy.getFrom());
                policyWrappers.add(wrapper);
            }

            final String permission = policy.getPermission();
            final SecurityGroup secGroup = policy.getSecurityGroup();
            if (permission.startsWith(EPMapPolicy.Type.user.name())) {
                final List<Identity> identities = securityManager.getIdentitiesOfSecurityGroup(secGroup);
                wrapper.addPolicy(policy);
                wrapper.setType(EPMapPolicy.Type.user);
                wrapper.addIdentities(identities);
            } else if (permission.startsWith(EPMapPolicy.Type.group.name())) {
                wrapper.addPolicy(policy);
                final BusinessGroup group = groupService.findBusinessGroup(policy.getSecurityGroup());
                wrapper.addGroup(group);
                wrapper.setType(EPMapPolicy.Type.group);
            } else if (permission.startsWith(EPMapPolicy.Type.invitation.name())) {
                wrapper.addPolicy(policy);
                final Invitation invitation = securityManager.findInvitation(policy.getSecurityGroup());
                wrapper.setInvitation(invitation);
                wrapper.setType(EPMapPolicy.Type.invitation);
            } else if (permission.startsWith(EPMapPolicy.Type.allusers.name())) {
                wrapper.addPolicy(policy);
                wrapper.setType(EPMapPolicy.Type.allusers);
            }
        }

        return policyWrappers;
    }

    private EPMapPolicy getWrapperWithSamePolicy(final Policy policy, final List<EPMapPolicy> policyWrappers) {
        final Date to = policy.getTo();
        final Date from = policy.getFrom();
        final String permission = policy.getPermission();

        a_a: for (final EPMapPolicy wrapper : policyWrappers) {
            for (final Policy p : wrapper.getPolicies()) {
                if (!permission.equals(p.getPermission())) {
                    continue a_a;
                }
                if (from == null && p.getFrom() == null || (from != null && p.getFrom() != null && from.equals(p.getFrom()))) {
                    if (to == null && p.getTo() == null || (to != null && p.getTo() != null && to.equals(p.getTo()))) {
                        return wrapper;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Update the map policies of a map. The missing policies are deleted!
     * 
     * @param map
     * @param policyWrappers
     */
    public void updateMapPolicies(final PortfolioStructureMap map, final List<EPMapPolicy> mapPolicies) {
        final List<Policy> currentPolicies = securityManager.getPoliciesOfResource(map.getOlatResource(), null);
        final List<Policy> savedPolicies = new ArrayList<Policy>();
        for (final EPMapPolicy mapPolicy : mapPolicies) {
            savedPolicies.addAll(saveMapPolicy(map, mapPolicy, currentPolicies));
        }

        for (final Policy currentPolicy : currentPolicies) {
            boolean inUse = false;
            for (final Policy savedPolicy : savedPolicies) {
                if (currentPolicy.equalsByPersistableKey(savedPolicy)) {
                    inUse = true;
                    break;
                }
            }

            if (!inUse && currentPolicy.getPermission().contains(Constants.PERMISSION_READ)) {
                deletePolicy(currentPolicy);
            }
        }
    }

    private void deletePolicy(final Policy policy) {
        if (policy.getPermission().contains(Constants.PERMISSION_READ)) {
            final String permission = policy.getPermission();
            // TODO delete SecurityGroup for non invitations as well
            securityManager.deletePolicy(policy.getSecurityGroup(), permission, policy.getOlatResource());

            final String invitationPermission = Type.invitation + "_" + Constants.PERMISSION_READ;
            if (invitationPermission.equals(permission)) {
                final Invitation invitation = securityManager.findInvitation(policy.getSecurityGroup());
                deleteInvitation(invitation);
            }
        }
    }

    private void deleteInvitation(Invitation invitation) {
        List<Identity> identitiesOfSecurityGroup = securityManager.getIdentitiesOfSecurityGroup(invitation.getSecurityGroup());
        if (identitiesOfSecurityGroup.size() > 1) {
            throw new AssertException("None or 1 identity expected to be attached to invitation security group (was " + identitiesOfSecurityGroup.size()
                    + " for security group " + invitation.getSecurityGroup().getKey() + ").");
        }
        if (identitiesOfSecurityGroup.size() == 1) {
            UserDeletionManager.getInstance().deleteIdentity(identitiesOfSecurityGroup.get(0));
        }

        securityManager.deleteInvitation(invitation);
    }

    /**
     * Cleans expired invitations (deletion in database)
     * 
     * @param limitDate
     *            Expired invitations are cleaned up if they'd been created before <code>limitDate</code>
     */
    public void cleanUpInvitations(Date limitDate) {
        for (Invitation invitation : securityManager.findExpiredInvitations(limitDate)) {
            deleteInvitation(invitation);
        }
    }

    private List<Policy> saveMapPolicy(final PortfolioStructureMap map, final EPMapPolicy mapPolicy, final List<Policy> currentPolicies) {
        final List<Policy> policies = mapPolicy.getPolicies();
        final List<Policy> savedPolicies = new ArrayList<Policy>();
        switch (mapPolicy.getType()) {
        case user:
            Policy policy = (policies == null || policies.isEmpty()) ? null : policies.get(0);
            if (policy == null) {
                final SecurityGroup secGroup = securityManager.createAndPersistSecurityGroup();
                policy = securityManager.createAndPersistPolicy(secGroup, mapPolicy.getType() + "_" + Constants.PERMISSION_READ, mapPolicy.getFrom(), mapPolicy.getTo(),
                        map.getOlatResource());
            } else {
                final Policy currentPolicy = reusePolicyInSession(policy, currentPolicies);
                securityManager.updatePolicy(currentPolicy, mapPolicy.getFrom(), mapPolicy.getTo());
            }
            final SecurityGroup secGroup = policy.getSecurityGroup();
            final List<Object[]> allIdents = securityManager.getIdentitiesAndDateOfSecurityGroup(secGroup);
            for (final Object[] objects : allIdents) {
                final Identity identity = (Identity) objects[0];
                securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
            }
            for (final Identity identity : mapPolicy.getIdentities()) {
                if (!securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
                    securityManager.addIdentityToSecurityGroup(identity, secGroup);
                }
            }
            savedPolicies.add(policy);
            break;
        case group:
            for (final BusinessGroup group : mapPolicy.getGroups()) {
                savedPolicies.add(applyPolicyTo(group.getPartipiciantGroup(), mapPolicy, map));
                savedPolicies.add(applyPolicyTo(group.getOwnerGroup(), mapPolicy, map));
            }
            break;
        case invitation:
            savedPolicies.add(saveMapPolicyInvitation(map, mapPolicy));
            break;
        case allusers:
            final Policy allUsersPolicy = applyPolicyToAllUsers(mapPolicy, map);
            savedPolicies.add(allUsersPolicy);
            break;
        }
        return savedPolicies;
    }

    private Policy applyPolicyToAllUsers(final EPMapPolicy wrapper, final PortfolioStructureMap map) {
        final SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        final List<Policy> currentPolicies = securityManager.getPoliciesOfResource(map.getOlatResource(), allUsers);
        if (!currentPolicies.isEmpty()) {
            final Policy currentPolicy = currentPolicies.get(0);
            securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
            return currentPolicy;
        }

        final Policy policy = securityManager.createAndPersistPolicy(allUsers, wrapper.getType() + "_" + Constants.PERMISSION_READ, wrapper.getFrom(), wrapper.getTo(),
                map.getOlatResource());
        return policy;
    }

    private Policy saveMapPolicyInvitation(final PortfolioStructureMap map, final EPMapPolicy mapPolicy) {
        final Invitation invitation = mapPolicy.getInvitation();
        final List<Policy> currentPolicies = securityManager.getPoliciesOfSecurityGroup(invitation.getSecurityGroup());
        for (Policy currentPolicy : currentPolicies) {
            if (currentPolicy.getOlatResource().equalsByPersistableKey(map.getOlatResource())) {
                currentPolicy = reusePolicyInSession(currentPolicy, currentPolicies);
                securityManager.updatePolicy(currentPolicy, mapPolicy.getFrom(), mapPolicy.getTo());
                securityManager.updateInvitation(invitation);
                return currentPolicy;
            }
            throw new AssertException("At least one policy expected for security group of existing invitation.");
        }

        Invitation persistedInvitation = securityManager.saveInvitation(invitation);
        final SecurityGroup secGroup = persistedInvitation.getSecurityGroup();
        final Policy policy = securityManager.createAndPersistPolicy(secGroup, mapPolicy.getType() + "_" + Constants.PERMISSION_READ, mapPolicy.getFrom(),
                mapPolicy.getTo(), map.getOlatResource());

        return policy;
    }

    /**
     * Hibernate doesn't allow to update an object if the same object is already in the current hibernate session.
     * 
     * @param policy
     * @param currentPolicies
     * @return
     */
    private Policy reusePolicyInSession(final Policy policy, final List<Policy> currentPolicies) {
        for (final Policy currentPolicy : currentPolicies) {
            if (policy.equalsByPersistableKey(currentPolicy)) {
                return currentPolicy;
            }
        }
        return policy;
    }

    private Policy applyPolicyTo(final SecurityGroup secGroup, final EPMapPolicy wrapper, final PortfolioStructureMap map) {
        final List<Policy> currentPolicies = securityManager.getPoliciesOfSecurityGroup(secGroup);
        for (Policy currentPolicy : currentPolicies) {
            if (currentPolicy.getOlatResource().equalsByPersistableKey(map.getOlatResource())) {
                currentPolicy = reusePolicyInSession(currentPolicy, currentPolicies);
                securityManager.updatePolicy(currentPolicy, wrapper.getFrom(), wrapper.getTo());
                return currentPolicy;
            }
        }

        final Policy policy = securityManager.createAndPersistPolicy(secGroup, wrapper.getType() + "_read", wrapper.getFrom(), wrapper.getTo(), map.getOlatResource());
        return policy;
    }

}
