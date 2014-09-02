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

package org.olat.lms.group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.basesecurity.SecurityGroupImpl;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.user.User;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.user.UserService;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>
 * TODO: Class Description for BusinessGroupManagerImplITCase
 * <P/>
 * Initial Date: Jul 28, 2004
 * 
 * @author patrick
 */
public class BusinessGroupManagerImplITCase extends OlatTestCase {
    //
    private static final Logger log = LoggerHelper.getLogger();

    /*
     * ::Test Setup::
     */
    private static Identity id1 = null;
    private static Identity id2 = null;
    private static Identity id3 = null;
    private static Identity id4 = null;
    // For WaitingGroup tests
    private static Identity wg1 = null;
    private static Identity wg2 = null;
    private static Identity wg3 = null;
    private static Identity wg4 = null;

    /*
     * BuddyGroup one
     */
    private static BusinessGroup one = null;
    private final String oneName = "First BuddyGroup";
    private final String oneDesc = "some short description for first buddygroup";
    // private String oneIntr = "bla blusch blip blup blep";
    /*
     * BuddyGroup two
     */
    private static BusinessGroup two = null;
    private final String twoName = "Second BuddyGroup";
    private final String twoDesc = "some short description for second buddygroup";
    // private String twoIntr = "notting";
    /*
     * BuddyGroup three
     */
    private static BusinessGroup three = null;
    private final String threeName = "Third BuddyGroup";
    private final String threeDesc = "some short description for second buddygroup";
    // private String threeIntr = "nothing more";
    private static final int NROFTESTCASES = 3;
    private static int nrOfTestCasesAlreadyRun = 0;
    private static boolean suiteIsAborted = true;
    private static BusinessGroup bgWithWaitingList = null;
    @Autowired
    private UserService userService;
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BusinessGroupContextService bgContextService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

        businessGroupService = applicationContext.getBean(BusinessGroupService.class);
        // Identities
        id1 = JunitTestHelper.createAndPersistIdentityAsUser("bg_id1");
        id2 = JunitTestHelper.createAndPersistIdentityAsUser("bg_id2");
        id3 = JunitTestHelper.createAndPersistIdentityAsUser("bg_id3");
        id4 = JunitTestHelper.createAndPersistIdentityAsUser("bg_id4");
        // buddyGroups without waiting-list: groupcontext is null
        List l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
        if (l.size() == 0) {
            one = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id1, oneName, oneDesc, null, null, false, false, null);
        } else {
            one = (BusinessGroup) businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null).get(0);
        }
        l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        if (l.size() == 0) {
            two = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id2, twoName, twoDesc, null, null, false, false, null);
            final SecurityGroup twoPartips = two.getPartipiciantGroup();
            baseSecurity.addIdentityToSecurityGroup(id3, twoPartips);
            baseSecurity.addIdentityToSecurityGroup(id4, twoPartips);
        } else {
            two = (BusinessGroup) businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null).get(0);
        }
        l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
        if (l.size() == 0) {
            three = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, id3, threeName, threeDesc, null, null, false, false, null);
            final SecurityGroup threeOwner = three.getOwnerGroup();
            final SecurityGroup threeOPartips = three.getPartipiciantGroup();
            baseSecurity.addIdentityToSecurityGroup(id2, threeOPartips);
            baseSecurity.addIdentityToSecurityGroup(id1, threeOwner);
        } else {
            three = (BusinessGroup) businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null).get(0);
        }
        /*
         * Membership in ParticipiantGroups............................. id1 owns BuddyGroup one with participiantGroup:={}........... id2 owns BuddyGroup two with
         * participiantGroup:={id3,id4} id3 owns BuddyGroup three participiantGroup:={id2}, ownerGroup:={id3,id1}
         */

        DBFactory.getInstance().closeSession();

        setupWaitingList(businessGroupService);
        /*
         * phuuu finally initialized
         */
    }

    @Test
    public void testCheckIfNamesExistsInContext() throws Exception {
        suiteIsAborted = true;

        final BGContext ctxA = bgContextService.createAndPersistBGContext("DefaultA", "Empty", BusinessGroup.TYPE_LEARNINGROUP, id1, true);
        final BGContext ctxB = bgContextService.createAndPersistBGContext("DefaultB", "Empty", BusinessGroup.TYPE_LEARNINGROUP, id1, true);

        final String[] namesInCtxA = new String[] { "A-GroupOne", "A-GroupTwo", "A-GroupThree", "A-GroupFour", "A-GroupFive", "A-GroupSix" };
        final String[] namesInCtxB = new String[] { "B-GroupAAA", "B-GroupBBB", "B-GroupCCC", "B-GroupDDD", "B-GroupEEE", "B-GroupFFF" };
        final BusinessGroup[] ctxAgroups = new BusinessGroup[namesInCtxA.length];
        final BusinessGroup[] ctxBgroups = new BusinessGroup[namesInCtxB.length];

        for (int i = 0; i < namesInCtxA.length; i++) {
            ctxAgroups[i] = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, namesInCtxA[i], null, 0, 0, false, false, ctxA);
        }
        for (int i = 0; i < namesInCtxB.length; i++) {
            ctxBgroups[i] = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, namesInCtxB[i], null, 0, 0, false, false, ctxB);
        }
        // first click created two context and each of them containg groups
        // evict all created and search
        System.out.println("Test: ctxAgroups.length=" + ctxAgroups.length);
        for (int i = 0; i < ctxAgroups.length; i++) {
            System.out.println("Test: i=" + i);
            System.out.println("Test: ctxAgroups[i]=" + ctxAgroups[i]);
            DBFactory.getInstance().closeSession();
        }
        for (int i = 0; i < ctxBgroups.length; i++) {
            DBFactory.getInstance().closeSession();
        }
        // next click needs to check of a set of groupnames already exists.
        final Set subsetOkInA = new HashSet() {
            {
                add("A-GroupTwo");
                add("A-GroupThree");
                add("A-GroupFour");
            }
        };
        final Set subsetNOkInA = new HashSet() {
            {
                add("A-GroupTwo");
                add("NOT-IN-A");
                add("A-GroupThree");
                add("A-GroupFour");
            }
        };
        final Set subsetOkInB = new HashSet() {
            {
                add("B-GroupCCC");
                add("B-GroupDDD");
                add("B-GroupEEE");
                add("B-GroupFFF");
            }
        };
        final Set subsetNOkInB = new HashSet() {
            {
                add("B-GroupCCC");
                add("NOT-IN-B");
                add("B-GroupEEE");
                add("B-GroupFFF");
            }
        };
        final Set setSpansAandBNok = new HashSet() {
            {
                add("B-GroupCCC");
                add("A-GroupTwo");
                add("A-GroupThree");
                add("B-GroupEEE");
                add("B-GroupFFF");
            }
        };

        boolean allExist = false;
        allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxA);
        assertTrue("Three A-Group.. should find all", allExist);
        // Check : one name does not exist, 3 exist
        assertTrue("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetNOkInA, ctxA));
        // Check : no name exist in context
        assertFalse("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxA));
        //
        allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInB, ctxB);
        assertTrue("Three B-Group.. should find all", allExist);
        // Check : one name does not exist, 3 exist
        assertTrue("A 'NOT-IN-B'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetNOkInB, ctxB));
        // Check : no name exist in context
        assertFalse("A 'NOT-IN-A'.. should not find all", businessGroupService.checkIfOneOrMoreNameExistsInContext(subsetOkInA, ctxB));
        // Mix A (2x) and B (3x)
        allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxA);
        assertTrue("Groupnames spanning two context... should not find all in context A", allExist);
        // Mix A (2x) and B (3x)
        allExist = businessGroupService.checkIfOneOrMoreNameExistsInContext(setSpansAandBNok, ctxB);
        assertTrue("Groupnames spanning two context... should not find all in context B", allExist);
        //

        suiteIsAborted = false;
        nrOfTestCasesAlreadyRun++;
    }

    /**
     * Test existence of BuddyGroups inserted in the setUp phase................ this test rather tests the findXXX methods............................... so if the setup
     * was ok, and this test also fulfilled, then it means that createAndPersistBuddyGroup works, and also the findXXX methods.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateAndPersistBuddyGroup() throws Exception {
        suiteIsAborted = true;
        /*
		 * 
		 */
        List sqlRes;
        BusinessGroup found;
        /*
         * id1
         */
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
        assertTrue("2 BuddyGroups owned by id1", sqlRes.size() == 2);
        for (int i = 0; i < sqlRes.size(); i++) {
            assertTrue("It's a BuddyGroup Object", sqlRes.get(i) instanceof BusinessGroup);
            found = (BusinessGroup) sqlRes.get(i);
            // equality by comparing PersistenObject.getKey()!!!
            final boolean ok = one.getKey().longValue() == found.getKey().longValue() || three.getKey().longValue() == found.getKey().longValue();
            assertTrue("It's the correct BuddyGroup", ok);

        }
        sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id1, null);
        assertTrue("0 BuddyGroup where id1 is partipicating", sqlRes.size() == 0);

        /*
         * id2
         */
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
        assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
        found = (BusinessGroup) sqlRes.get(0);
        // equality by comparing PersistenObject.getKey()!!!
        assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());
        sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        assertTrue("1 BuddyGroup where id2 is partipicating", sqlRes.size() == 1);
        assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
        found = (BusinessGroup) sqlRes.get(0);
        assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());

        /*
         * id3
         */
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
        assertTrue("1 BuddyGroup owned by id3", sqlRes.size() == 1);
        assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
        found = (BusinessGroup) sqlRes.get(0);
        // equality by comparing PersistenObject.getKey()!!!
        assertTrue("It's the correct BuddyGroup", three.getKey().longValue() == found.getKey().longValue());
        sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id3, null);
        assertTrue("1 BuddyGroup where id3 is partipicating", sqlRes.size() == 1);
        assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
        found = (BusinessGroup) sqlRes.get(0);
        assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());

        /*
         * id4
         */
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
        assertTrue("0 BuddyGroup owned by id4", sqlRes.size() == 0);
        //
        sqlRes = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, id4, null);
        assertTrue("1 BuddyGroup where id4 is partipicating", sqlRes.size() == 1);
        assertTrue("It's a BuddyGroup Object", sqlRes.get(0) instanceof BusinessGroup);
        found = (BusinessGroup) sqlRes.get(0);
        assertTrue("It's the correct BuddyGroup", two.getKey().longValue() == found.getKey().longValue());
        /*
		 * 
		 */
        suiteIsAborted = false;
        nrOfTestCasesAlreadyRun++;
    }

    /**
     * checks if tools can be enabled disabled or checked against being enabled. TOols are configured with the help of the generic properties storage.
     * 
     * @throws Exception
     */
    public void testEnableDisableAndCheckForTool() throws Exception {
        suiteIsAborted = true;
        /*
		 * 
		 */
        List sqlRes;
        BusinessGroup found;

        /*
         * id2
         */

        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        found = (BusinessGroup) sqlRes.get(0);
        final CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            final String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
            final boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
            // all tools are disabled by default exept the news tool
            assertTrue(msg, !enabled);

        }
        //
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
        }
        //
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            final String msg = "Tool " + CollaborationTools.TOOLS[i] + " is enabled";
            final boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
            assertTrue(msg, enabled);

        }
        //
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], false);
        }
        //
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            final String msg = "Tool " + CollaborationTools.TOOLS[i] + " is disabled";
            final boolean enabled = myCTSMngr.isToolEnabled(CollaborationTools.TOOLS[i]);
            assertTrue(msg, !enabled);

        }
        /*
		 * 
		 */
        suiteIsAborted = false;
        nrOfTestCasesAlreadyRun++;
    }

    /**
     * test if removing a BuddyGroup really deletes everything it should.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteBuddyGroup() throws Exception {
        suiteIsAborted = true;
        /*
		 * 
		 */
        List sqlRes;
        BusinessGroup found;

        /*
         * id2
         */
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        assertTrue("1 BuddyGroup owned by id2", sqlRes.size() == 1);
        found = (BusinessGroup) sqlRes.get(0);
        final CollaborationTools myCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(found);
        //
        for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
            myCTSMngr.setToolEnabled(CollaborationTools.TOOLS[i], true);
        }
        /*
		 * 
		 */
        businessGroupService.deleteBusinessGroup(found);
        sqlRes = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, id2, null);
        assertTrue("0 BuddyGroup owned by id2", sqlRes.size() == 0);
        /*
		 * 
		 */
        suiteIsAborted = false;
        nrOfTestCasesAlreadyRun++;
    }

    // Test for WaitingList
    // /////////////////////
    /**
     * Add 3 idenities to the waiting list and check the position. before test Waitinglist=[]<br>
     * after test Waitinglist=[wg2,wg3,wg4]
     */
    @Test
    public void testAddToWaitingListAndFireEvent() throws Exception {
        System.out.println("testAddToWaitingListAndFireEvent: start...");

        // Add wg2
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        List<Identity> identities = new ArrayList<Identity>();
        identities.add(wg2);
        businessGroupService.addToWaitingListAndFireEvent(wg2, identities, bgWithWaitingList, flags);
        // Add wg3
        identities = new ArrayList<Identity>();
        identities.add(wg3);
        businessGroupService.addToWaitingListAndFireEvent(wg3, identities, bgWithWaitingList, flags);
        // Add wg4
        identities = new ArrayList<Identity>();
        identities.add(wg4);
        businessGroupService.addToWaitingListAndFireEvent(wg4, identities, bgWithWaitingList, flags);
        System.out.println("testAddToWaitingListAndFireEvent: 3 user added to waiting list");

        // Check position of 'wg2'
        int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
        System.out.println("testAddToWaitingListAndFireEvent: wg2 pos=" + pos);
        assertTrue("pos must be 1, bit is=" + pos, pos == 1);
        // Check position of 'wg3'
        pos = businessGroupService.getPositionInWaitingListFor(wg3, bgWithWaitingList);
        System.out.println("testAddToWaitingListAndFireEvent wg3: pos=" + pos);
        assertTrue("pos must be 2, bit is=" + pos, pos == 2);
        // Check position of 'wg4'
        pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
        System.out.println("testAddToWaitingListAndFireEvent wg4: pos=" + pos);
        assertTrue("pos must be 3, bit is=" + pos, pos == 3);
    }

    /**
     * Remove identity 2 (wg3) from the waiting list and check the position of identity 2. before test Waitinglist=[wg2,wg3,wg4]<br>
     * after test Waitinglist=[wg2,wg4]
     */
    @Test
    public void testRemoveFromWaitingListAndFireEvent() throws Exception {
        System.out.println("testRemoveFromWaitingListAndFireEvent: start...");
        // Remove wg3
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        final List<Identity> identities = new ArrayList<Identity>();
        identities.add(wg3);
        businessGroupService.removeFromWaitingListAndFireEvent(wg1, identities, bgWithWaitingList, flags);
        // Check position of 'wg2'
        int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
        System.out.println("testRemoveFromWaitingListAndFireEvent: wg2 pos=" + pos);
        assertTrue("pos must be 1, bit is=" + pos, pos == 1);
        // Check position of 'wg4'
        pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
        System.out.println("testRemoveFromWaitingListAndFireEvent wg4: pos=" + pos);
        assertTrue("pos must be 2, bit is=" + pos, pos == 2);

    }

    /**
     * Move identity 4 (wg4) from the waiting list to participant list. before test Waitinglist=[wg2,wg4]<br>
     * after test Waitinglist=[wg2]<br>
     * participant-list=[wg4]
     */
    @Test
    public void testMoveIdenityFromWaitingListToParticipant() throws Exception {
        System.out.println("testMoveIdenityFromWaitingListToParticipant: start...");
        // Check that 'wg4' is not in participant list
        assertFalse("Identity is allready in participant-list, remove it(dbsetup?)", businessGroupService.isIdentityInBusinessGroup(wg4, bgWithWaitingList));

        // Move wg4 from waiting-list to participant
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        final List<Identity> identities = new ArrayList<Identity>();
        identities.add(wg4);
        businessGroupService.moveIdentitiesFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
        // Check position of 'wg2'
        final int pos = businessGroupService.getPositionInWaitingListFor(wg2, bgWithWaitingList);
        System.out.println("testMoveIdenityFromWaitingListToParticipant: wg2 pos=" + pos);
        assertTrue("pos must be 1, bit is=" + pos, pos == 1);
        // Check if 'wg4' is in participant-list
        assertTrue("Identity is not in participant-list", businessGroupService.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
    }

    @Test
    public void testMoveRegisteredIdentityFromWaitingToParticipant() throws Exception {
        System.out.println("testMoveRegisteredIdentityFromWaitingToParticipant: start...");
        // Add a user to waiting-list which is allready in participant-list and try
        // and try to move this user => user will be removed from waiting-list
        // Add again wg2
        final BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
        List<Identity> identities = new ArrayList<Identity>();
        identities.add(wg1);
        businessGroupService.addToWaitingListAndFireEvent(wg4, identities, bgWithWaitingList, flags);
        identities = new ArrayList<Identity>();
        identities.add(wg4);
        businessGroupService.moveIdentitiesFromWaitingListToParticipant(identities, wg1, bgWithWaitingList, flags);
        // Check position of 'wg4'
        final int pos = businessGroupService.getPositionInWaitingListFor(wg4, bgWithWaitingList);
        System.out.println("testMoveIdenityFromWaitingListToParticipant: wg4 pos=" + pos);
        assertTrue("pos must be 0, bit is=" + pos, pos == 0);
        // Check if 'wg4' is still in participant-list
        assertTrue("Identity is not in participant-list", businessGroupService.isIdentityInBusinessGroup(wg4, bgWithWaitingList));
    }

    @Test
    public void testDeleteBusinessGroupWithWaitingGroup() {
        doTestDeleteBusinessGroup(true);
    }

    @Test
    public void testDeleteBusinessGroupWithoutWaitingGroup() {
        doTestDeleteBusinessGroup(false);
    }

    private void doTestDeleteBusinessGroup(final boolean withWaitingList) {
        final BGContext groupContext = bgContextService.createAndPersistBGContext("c1delete", "c1delete", BusinessGroup.TYPE_LEARNINGROUP, null, true);

        final BusinessGroup deleteTestGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, "deleteTestGroup-1",
                "deleteTestGroup-1", null, null, withWaitingList, true, groupContext);

        final Long ownerGroupKey = deleteTestGroup.getOwnerGroup().getKey();
        final Long partipiciantGroupKey = deleteTestGroup.getPartipiciantGroup().getKey();
        final Long waitingGroupKey = deleteTestGroup.getWaitingGroup().getKey();

        assertNotNull("Could not find owner-group", DBFactory.getInstance().findObject(SecurityGroupImpl.class, ownerGroupKey));
        assertNotNull("Could not find partipiciant-group", DBFactory.getInstance().findObject(SecurityGroupImpl.class, partipiciantGroupKey));
        assertNotNull("Could not find waiting-group", DBFactory.getInstance().findObject(SecurityGroupImpl.class, waitingGroupKey));
        businessGroupService.deleteBusinessGroup(deleteTestGroup);
        assertNull("owner-group still exist after delete", DBFactory.getInstance().findObject(SecurityGroupImpl.class, ownerGroupKey));
        assertNull("partipiciant-group still exist after delete", DBFactory.getInstance().findObject(SecurityGroupImpl.class, partipiciantGroupKey));
        assertNull("waiting-group still exist after delete", DBFactory.getInstance().findObject(SecurityGroupImpl.class, waitingGroupKey));
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        try {
            if (suiteIsAborted || (NROFTESTCASES - nrOfTestCasesAlreadyRun) == 0) {
                // DB.getInstance().clearDatabase();
            }
            DBFactory.getInstance().closeSession();

        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    // Helper methods
    // ///////////////
    private void setupWaitingList(final BusinessGroupService businessGroupService) {
        if (businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, id1, null).size() == 0) {
            // create business-group with waiting-list
            final String bgWithWaitingListName = "BG with WaitingList";
            final String bgWithWaitingListDesc = "some short description for BG with WaitingList";
            final Boolean enableWaitinglist = new Boolean(true);
            final Boolean enableAutoCloseRanks = new Boolean(true);
            final BGContext groupContext = bgContextService.createAndPersistBGContext("c1name", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
            System.out.println("testAddToWaitingListAndFireEvent: groupContext=" + groupContext);
            bgWithWaitingList = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, bgWithWaitingListName, bgWithWaitingListDesc,
                    null, null, enableWaitinglist, enableAutoCloseRanks, groupContext);
            bgWithWaitingList.setMaxParticipants(new Integer(2));
            // create mock objects
            final String PACKAGE = PackageUtil.getPackageName(BusinessGroupManagerImplITCase.class);
            // Identities
            final User UserWg1 = userService.createAndPersistUser("FirstName_wg1", "LastName_wg1", "wg1_junittest@olat.uzh.ch");
            wg1 = baseSecurity.createAndPersistIdentity("bg_wg1", UserWg1, AUTHENTICATION_PROVIDER_OLAT, "bg_wg1", Encoder.bCryptEncode("wg1"));
            final User UserWg2 = userService.createAndPersistUser("FirstName_wg2", "LastName_wg2", "wg2_junittest@olat.uzh.ch");
            wg2 = baseSecurity.createAndPersistIdentity("bg_wg2", UserWg2, AUTHENTICATION_PROVIDER_OLAT, "bg_wg2", Encoder.bCryptEncode("wg2"));
            final User UserWg3 = userService.createAndPersistUser("FirstName_wg3", "LastName_wg3", "wg3_junittest@olat.uzh.ch");
            wg3 = baseSecurity.createAndPersistIdentity("bg_wg3", UserWg3, AUTHENTICATION_PROVIDER_OLAT, "bg_wg3", Encoder.bCryptEncode("wg3"));
            final User UserWg4 = userService.createAndPersistUser("FirstName_wg4", "LastName_wg4", "wg4_junittest@olat.uzh.ch");
            wg4 = baseSecurity.createAndPersistIdentity("bg_wg4", UserWg4, AUTHENTICATION_PROVIDER_OLAT, "bg_wg4", Encoder.bCryptEncode("wg4"));
        }

    }

}
