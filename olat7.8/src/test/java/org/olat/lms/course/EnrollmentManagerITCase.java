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

package org.olat.lms.course;

// um click emulieren:
/*
 * 1) generiere Persistentes Object 2) -> DB...evict() entferne Instanz aus HibernateSession 3) aktionen testen, z.b. update failed, falls object nicht in session
 */
// DB.getInstance().evict();
// DB.getInstance().loadObject(); püft ob schon in hibernate session.
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.nodes.ENCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>
 * TODO: Class Description for BusinessGroupManagerImplTest
 * <P/>
 * Initial Date: Jul 28, 2004
 * 
 * @author patrick
 */

public class EnrollmentManagerITCase extends OlatTestCase {
    //
    private static final Logger log = LoggerHelper.getLogger();

    /*
     * ::Test Setup::
     */
    private static Identity id1 = null;
    // For WaitingGroup tests
    private static Identity wg1 = null;
    private static Identity wg2 = null;
    private static Identity wg3 = null;

    // For WaitingGroup tests
    // private static Translator testTranslator = null;
    private static BusinessGroup bgWithWaitingList = null;
    private BusinessGroupService businessGroupService;
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private BusinessGroupContextService bgContextService;
    private BGContext groupContext;
    @Autowired
    OLATResourceManager resourceManager;
    @Autowired
    RepositoryService repositoryService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        businessGroupService = applicationContext.getBean(BusinessGroupService.class);

        // Identities
        id1 = JunitTestHelper.createAndPersistIdentityAsUser("id1");
        DBFactory.getInstance().closeSession();
        // create business-group with waiting-list
        final String bgWithWaitingListName = "Group with WaitingList";
        final String bgWithWaitingListDesc = "some short description for Group with WaitingList";
        final Boolean enableWaitinglist = new Boolean(true);
        final Boolean enableAutoCloseRanks = new Boolean(true);
        groupContext = bgContextService.createAndPersistBGContext("c1name", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
        System.out.println("testAddToWaitingListAndFireEvent: groupContext=" + groupContext);
        bgWithWaitingList = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, id1, bgWithWaitingListName, bgWithWaitingListDesc, null,
                null, enableWaitinglist, enableAutoCloseRanks, groupContext);
        bgWithWaitingList.setMaxParticipants(new Integer(2));
        System.out.println("TEST bgWithWaitingList=" + bgWithWaitingList);
        System.out.println("TEST bgWithWaitingList.getMaxParticipants()=" + bgWithWaitingList.getMaxParticipants());
        System.out.println("TEST bgWithWaitingList.getWaitingListEnabled()=" + bgWithWaitingList.getWaitingListEnabled());
        // create mock objects

        // Identities
        wg1 = JunitTestHelper.createAndPersistIdentityAsUser("wg1");
        wg2 = JunitTestHelper.createAndPersistIdentityAsUser("wg2");
        wg3 = JunitTestHelper.createAndPersistIdentityAsUser("wg3");
        DBFactory.getInstance().closeSession();

    }

    // Test for WaitingList
    // /////////////////////
    /**
     * Enroll 3 idenities (group with max-size=2 and waiting-list). Cancel enrollment. Check size after each step.
     */
    @Test
    public void testEnroll() throws Exception {
        System.out.println("testEnroll: start...");
        final EnrollmentManager enrollmentManager = EnrollmentManager.getInstance();
        final ENCourseNode enNode = new ENCourseNode();
        final OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck("CourseModule", 1L);
        final CourseEnvironment cenv = CourseFactory.createEmptyCourse(ores, "Test", "Test", "learningObjectives").getCourseEnvironment();
        RepositoryEntry repositoryEntry = repositoryService.createRepositoryEntryInstance("test");
        OLATResource resource = resourceManager.findResourceable(1L, "CourseModule");
        repositoryEntry.setOlatResource(resource);
        repositoryEntry.setOwnerGroup(bgWithWaitingList.getOwnerGroup());
        repositoryEntry.setDisplayname("Test Course");
        repositoryService.saveRepositoryEntry(repositoryEntry);
        bgContextService.addBGContextToResource(groupContext, resource);
        // 1. enroll wg1 user
        IdentityEnvironment ienv = new IdentityEnvironment();
        ienv.setIdentity(wg1);
        UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        CoursePropertyManager coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        System.out.println("enrollmentManager=" + enrollmentManager);
        System.out.println("bgWithWaitingList=" + bgWithWaitingList);
        assertTrue("bgWithWaitingList is null", bgWithWaitingList != null);
        System.out.println("userCourseEnv=" + userCourseEnv);
        System.out.println("userCourseEnv.getCourseEnvironment()=" + userCourseEnv.getCourseEnvironment());
        enrollmentManager.doEnroll(ores, wg1, bgWithWaitingList, enNode, coursePropertyManager, new ArrayList()/* enrollableGroupNames */,
                new ArrayList()/* enrollableAreaNames */, userCourseEnv.getCourseEnvironment().getCourseGroupManager());
        assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants," + participantsCounter, participantsCounter == 1);
        // 2. enroll wg2 user
        ienv = new IdentityEnvironment();
        ienv.setIdentity(wg2);
        userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        enrollmentManager.doEnroll(ores, wg2, bgWithWaitingList, enNode, coursePropertyManager, new ArrayList()/* enrollableGroupNames */,
                new ArrayList()/* enrollableAreaNames */, userCourseEnv.getCourseEnvironment().getCourseGroupManager());
        assertTrue("Enrollment failed, user='wg2'", businessGroupService.isIdentityInBusinessGroup(wg2, bgWithWaitingList));
        assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants," + participantsCounter, participantsCounter == 2);
        // 3. enroll wg3 user => list is full => waiting-list
        ienv = new IdentityEnvironment();
        ienv.setIdentity(wg3);
        userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        enrollmentManager.doEnroll(ores, wg3, bgWithWaitingList, enNode, coursePropertyManager, new ArrayList()/* enrollableGroupNames */,
                new ArrayList()/* enrollableAreaNames */, userCourseEnv.getCourseEnvironment().getCourseGroupManager());
        assertFalse("Wrong enrollment, user='wg3' is in PartipiciantGroup, must be on waiting-list",
                businessGroupService.isIdentityInBusinessGroup(wg3, bgWithWaitingList));
        assertFalse("Wrong enrollment, user='wg3' is in PartipiciantGroup, must be on waiting-list",
                securityManager.isIdentityInSecurityGroup(wg3, bgWithWaitingList.getPartipiciantGroup()));
        assertTrue("Wrong enrollment, user='wg3' must be on waiting-list", securityManager.isIdentityInSecurityGroup(wg3, bgWithWaitingList.getWaitingGroup()));
        assertTrue("Enrollment failed, user='wg2'", businessGroupService.isIdentityInBusinessGroup(wg2, bgWithWaitingList));
        assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants," + participantsCounter, participantsCounter == 2);
        int waitingListCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getWaitingGroup());
        assertTrue("Wrong number of waiting-list, must be 1, is " + waitingListCounter, waitingListCounter == 1);
        // cancel enrollment for wg2 => transfer wg3 from waiting-list to participants
        ienv = new IdentityEnvironment();
        ienv.setIdentity(wg2);
        userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        enrollmentManager.doCancelEnrollment(wg2, bgWithWaitingList, enNode, coursePropertyManager);
        assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2, bgWithWaitingList));
        assertTrue("Enrollment failed, user='wg3'", businessGroupService.isIdentityInBusinessGroup(wg3, bgWithWaitingList));
        assertTrue("Enrollment failed, user='wg1'", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants, must be 2, is " + participantsCounter, participantsCounter == 2);
        waitingListCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getWaitingGroup());
        assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter, waitingListCounter == 0);
        // cancel enrollment for wg1
        ienv = new IdentityEnvironment();
        ienv.setIdentity(wg1);
        userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        enrollmentManager.doCancelEnrollment(wg1, bgWithWaitingList, enNode, coursePropertyManager);
        assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2, bgWithWaitingList));
        assertFalse("Cancel enrollment failed, user='wg1' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        assertTrue("Enrollment failed, user='wg3'", businessGroupService.isIdentityInBusinessGroup(wg3, bgWithWaitingList));
        participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants, must be 1, is " + participantsCounter, participantsCounter == 1);
        waitingListCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getWaitingGroup());
        assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter, waitingListCounter == 0);
        // cancel enrollment for wg3
        ienv = new IdentityEnvironment();
        ienv.setIdentity(wg3);
        userCourseEnv = new UserCourseEnvironmentImpl(ienv, cenv);
        coursePropertyManager = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        enrollmentManager.doCancelEnrollment(wg3, bgWithWaitingList, enNode, coursePropertyManager);
        assertFalse("Cancel enrollment failed, user='wg3' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg3, bgWithWaitingList));
        assertFalse("Cancel enrollment failed, user='wg2' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg2, bgWithWaitingList));
        assertFalse("Cancel enrollment failed, user='wg1' is still participants.", businessGroupService.isIdentityInBusinessGroup(wg1, bgWithWaitingList));
        participantsCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getPartipiciantGroup());
        assertTrue("Wrong number of participants, must be 0, is " + participantsCounter, participantsCounter == 0);
        waitingListCounter = securityManager.countIdentitiesOfSecurityGroup(bgWithWaitingList.getWaitingGroup());
        assertTrue("Wrong number of waiting-list, must be 0, is " + waitingListCounter, waitingListCounter == 0);

        System.out.println("testEnroll: done...");
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

}
