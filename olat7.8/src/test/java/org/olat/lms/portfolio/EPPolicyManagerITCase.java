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
package org.olat.lms.portfolio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.InvitationImpl;
import org.olat.data.commons.database.DB;
import org.olat.data.portfolio.structure.PortfolioStructureMap;
import org.olat.lms.portfolio.EPMapPolicy.Type;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This an integration test for the {@link EPPolicyManager} (which is responsible for persisting and retrieving data defining access control for shared E-Portfolio maps).
 * <P>
 * Initial Date: 22.03.2012 <br>
 * 
 * @author obuehler, oliver.buehler@agility-informatik.ch, Agility Informatik GmbH
 */
public class EPPolicyManagerITCase extends OlatTestCase {

    private static Identity ident1, ident2, ident3;
    private static boolean isInitialized = false;

    @Autowired
    private DB dbInstance;

    @Autowired
    private BaseSecurity securityManager;

    @Autowired
    private EPPolicyManager policyManager;

    @Autowired
    private EPFrontendManager epFrontendManager;

    @Before
    public void setUp() {
        if (!isInitialized) {
            assertNotNull(dbInstance);
            assertNotNull(securityManager);
            assertNotNull(policyManager);
            assertNotNull(epFrontendManager);

            ident1 = JunitTestHelper.createAndPersistIdentityAsUser("policyManagerTest-1");
            ident2 = JunitTestHelper.createAndPersistIdentityAsUser("policyManagerTest-2");
            ident3 = JunitTestHelper.createAndPersistIdentityAsUser("policyManagerTest-3");
        }
    }

    @After
    public void tearDown() {
        dbInstance.commitAndCloseSession();
    }

    @Test
    public void testNoShare() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // policies are empty
        final List<EPMapPolicy> policies = policyManager.getMapPolicies(map);
        assertEquals(0, policies.size());// owner policy

        // check visiblity (is owner)
        assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
        // check not visible (not in policy)
        assertFalse(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
        // check not visible (not in policy)
        assertFalse(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
    }

    @Test
    public void testUserShare() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save a user policy
        final EPMapPolicy userPolicy = new EPMapPolicy();
        userPolicy.setType(Type.user);
        userPolicy.getIdentities().add(ident2);
        policyManager.updateMapPolicies(map, Collections.singletonList(userPolicy));
        dbInstance.commitAndCloseSession();

        // one policy
        final List<EPMapPolicy> policies1 = policyManager.getMapPolicies(map);
        assertEquals(1, policies1.size());

        // check visiblity (is owner)
        assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
        // check visibility (is in policy)
        assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
        // check not visible (not in policy)
        assertFalse(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
    }

    @Test
    public void testAllUsersShare() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // add all user policy
        final EPMapPolicy userPolicy = new EPMapPolicy();
        userPolicy.setType(Type.allusers);
        policyManager.updateMapPolicies(map, Collections.singletonList(userPolicy));
        dbInstance.commitAndCloseSession();

        // one policy
        final List<EPMapPolicy> policies1 = policyManager.getMapPolicies(map);
        assertEquals(1, policies1.size());

        // check visiblity (is owner)
        assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
        // check visibility (is user)
        assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
        // check not visible (is user)
        assertTrue(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
    }

    @Test
    public void testMultipleShares() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save a list of policies
        final List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();

        // user policies
        final EPMapPolicy userPolicy = new EPMapPolicy();
        userPolicy.setType(Type.user);
        userPolicy.getIdentities().add(ident2);
        userPolicy.getIdentities().add(ident3);
        policies.add(userPolicy);

        // invitation policy
        final Invitation invitation = new InvitationImpl();
        invitation.setFirstName("John");
        invitation.setLastName("Doe");
        invitation.setMail("john@doe.ch");
        final EPMapPolicy invitationPolicy = new EPMapPolicy();
        invitationPolicy.setType(Type.invitation);
        invitationPolicy.setInvitation(invitation);
        policies.add(invitationPolicy);

        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        // retrieved policies
        final List<EPMapPolicy> savedPolicies = policyManager.getMapPolicies(map);
        assertEquals(2, savedPolicies.size());

        // check visiblity (is owner)
        assertTrue(epFrontendManager.isMapVisible(ident1, map.getOlatResource()));
        // check visibility (is in policy)
        assertTrue(epFrontendManager.isMapVisible(ident2, map.getOlatResource()));
        // check visible (is in policy)
        assertTrue(epFrontendManager.isMapVisible(ident3, map.getOlatResource()));
    }

    @Test
    public void testMergeUserPolicies() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save a list of policies
        final List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
        // first user policy
        final EPMapPolicy userPolicy1 = new EPMapPolicy();
        userPolicy1.setType(Type.user);
        userPolicy1.getIdentities().add(ident2);
        userPolicy1.getIdentities().add(ident3);
        policies.add(userPolicy1);
        // second user policy
        final EPMapPolicy userPolicy2 = new EPMapPolicy();
        userPolicy2.setType(Type.user);
        userPolicy2.getIdentities().add(ident1);
        policies.add(userPolicy2);
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        // check if the policies are correctly merged
        final List<EPMapPolicy> mergedPolicies = policyManager.getMapPolicies(map);
        assertNotNull(mergedPolicies);
        assertEquals(1, mergedPolicies.size());

        final EPMapPolicy mergedPolicy = mergedPolicies.get(0);
        final List<Identity> identities = mergedPolicy.getIdentities();
        assertEquals(3, identities.size());

        int count1, count2, count3;
        count1 = count2 = count3 = 0;
        for (final Identity identity : identities) {
            if (identity.equalsByPersistableKey(ident1)) {
                count1++;
            } else if (identity.equalsByPersistableKey(ident2)) {
                count2++;
            } else if (identity.equalsByPersistableKey(ident3)) {
                count3++;
            }
        }
        assertEquals(1, count1);
        assertEquals(1, count2);
        assertEquals(1, count3);
    }

    @Test
    public void testUpdateInvitation() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save an invitation
        final List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
        Invitation invitation = new InvitationImpl();
        invitation.setFirstName("John");
        invitation.setLastName("Doe");
        invitation.setMail("john2@doe.ch");
        EPMapPolicy invitationPolicy = new EPMapPolicy();
        invitationPolicy.setType(Type.invitation);
        invitationPolicy.setInvitation(invitation);
        policies.add(invitationPolicy);
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        policies.get(0).getInvitation().setMail("john3@doe.ch");
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        final List<EPMapPolicy> updatedPolicies = policyManager.getMapPolicies(map);
        assertEquals(1, updatedPolicies.size());
        assertEquals("john3@doe.ch", updatedPolicies.get(0).getInvitation().getMail());
    }

    @Test
    public void testDeleteInvitation() {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save an invitation
        final List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
        final Invitation invitation = new InvitationImpl();
        invitation.setFirstName("John");
        invitation.setLastName("Doe");
        invitation.setMail("john2@doe.ch");
        final EPMapPolicy invitationPolicy = new EPMapPolicy();
        invitationPolicy.setType(Type.invitation);
        invitationPolicy.setInvitation(invitation);
        policies.add(invitationPolicy);
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        assertNotNull(securityManager.findInvitation(invitation.getToken()));

        // remove the policy
        policies.clear();
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        // check if the policies and the invitation are deleted
        final List<EPMapPolicy> deletedPolicies = policyManager.getMapPolicies(map);
        assertNotNull(deletedPolicies);
        assertTrue(deletedPolicies.isEmpty());
        assertTrue(securityManager.findInvitation(invitation.getToken()) == null);
    }

    @Test
    public void testCleanUpInvitations() throws Exception {
        // create a map
        final PortfolioStructureMap map = createNewMap();

        // save an invitation
        final List<EPMapPolicy> policies = new ArrayList<EPMapPolicy>();
        Invitation invitation = new InvitationImpl();
        invitation.setFirstName("John");
        invitation.setLastName("Doe");
        invitation.setMail("john2@doe.ch");
        final EPMapPolicy invitationPolicy = new EPMapPolicy();
        invitationPolicy.setType(Type.invitation);
        invitationPolicy.setInvitation(invitation);
        invitationPolicy.setTo(new Date());
        policies.add(invitationPolicy);
        policyManager.updateMapPolicies(map, policies);
        dbInstance.commitAndCloseSession();

        // invitation has no expiration date => won't be cleaned up
        policyManager.cleanUpInvitations(new Date());
        dbInstance.commitAndCloseSession();
        assertNotNull(securityManager.findInvitation(invitation.getToken()));

        // set policy of invitation expired
        Date expiredDate = new Date(System.currentTimeMillis() - 60 * 1000);
        final List<EPMapPolicy> persistedPolicies = policyManager.getMapPolicies(map);
        assertEquals(1, persistedPolicies.size());
        persistedPolicies.get(0).setTo(expiredDate);
        policyManager.updateMapPolicies(map, persistedPolicies);
        dbInstance.commitAndCloseSession();

        // invitation is expired but has not passed limit date yet => won't be cleaned up
        Date limitDate = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
        policyManager.cleanUpInvitations(limitDate);
        dbInstance.commitAndCloseSession();
        assertNotNull(securityManager.findInvitation(invitation.getToken()));

        // invitation is expired => has to be cleaned up
        policyManager.cleanUpInvitations(new Date());
        dbInstance.commitAndCloseSession();
        assertTrue(securityManager.findInvitation(invitation.getToken()) == null);
    }

    private PortfolioStructureMap createNewMap() {
        final PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "Policies", "Description");
        dbInstance.commitAndCloseSession();
        return map;
    }
}
