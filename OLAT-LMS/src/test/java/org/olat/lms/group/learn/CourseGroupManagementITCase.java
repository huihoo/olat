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

package org.olat.lms.group.learn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.right.BGRightManager;
import org.olat.lms.group.right.BGRightManagerImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class CourseGroupManagementITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private Identity id1, id2, id3;
    private OLATResource course1;
    private BusinessGroupService businessGroupService;
    private CourseGroupManager gm;

    @Autowired
    private BaseSecurity securityManager;

    @Before
    public void setUp() {
        businessGroupService = applicationContext.getBean(BusinessGroupService.class);
        gm = (CourseGroupManager) applicationContext.getBean("persistingCourseGroupManager");
        try {
            id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
            id2 = JunitTestHelper.createAndPersistIdentityAsUser("twoo");
            id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");

            final OLATResourceManager rm = OLATResourceManager.getInstance();
            // create course and persist as OLATResourceImpl
            final OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse", System.currentTimeMillis());
            course1 = rm.createOLATResourceInstance(resourceable);
            DBFactory.getInstance().saveObject(course1);

            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
            e.printStackTrace();
            throw e;
        }
    }

    /** rights tests */
    @Test
    public void testHasRightIsInMethods() {
        final BGContextDao cm = BGContextDaoImpl.getInstance();

        final BGRightManager rm = BGRightManagerImpl.getInstance();
        final BGAreaDao am = BGAreaDaoImpl.getInstance();

        // 1) context one: learning groups
        final BGContext c1 = businessGroupService.createAndAddBGContextToResource("c1name", course1, BusinessGroup.TYPE_LEARNINGROUP, id1, true);
        // create groups without waitinglist
        final BusinessGroup g1 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0), new Integer(10),
                false, false, c1);
        final BusinessGroup g2 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0), new Integer(10),
                false, false, c1);
        // members
        securityManager.addIdentityToSecurityGroup(id1, g2.getOwnerGroup());
        securityManager.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id2, g2.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
        // areas
        final BGArea a1 = am.createAndPersistBGAreaIfNotExists("a1", "desca1", c1);
        final BGArea a2 = am.createAndPersistBGAreaIfNotExists("a2", null, c1);
        final BGArea a3 = am.createAndPersistBGAreaIfNotExists("a3", null, c1);
        am.addBGToBGArea(g1, a1);
        am.addBGToBGArea(g2, a1);
        am.addBGToBGArea(g1, a2);
        am.addBGToBGArea(g2, a3);

        // 2) context two: right groups
        final BGContext c2 = businessGroupService.createAndAddBGContextToResource("c2name", course1, BusinessGroup.TYPE_RIGHTGROUP, id2, true);
        // groups
        final BusinessGroup g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, null, null,
                null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
        final BusinessGroup g4 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g4", null, null, null,
                null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
        // members
        securityManager.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id1, g4.getPartipiciantGroup());
        securityManager.addIdentityToSecurityGroup(id3, g4.getPartipiciantGroup());
        // rights
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
        rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g4);
        rm.addBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g4);

        DBFactory.getInstance().closeSession(); // simulate user clicks

        // test groups
        assertTrue(gm.isIdentityInLearningGroup(id1, g1.getName(), course1));
        assertTrue(gm.isIdentityInLearningGroup(id1, g2.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id1, g3.getName(), course1)); // not a learning group
        assertFalse(gm.isIdentityInLearningGroup(id1, g4.getName(), course1)); // not a learning group

        assertTrue(gm.isIdentityInLearningGroup(id2, g1.getName(), course1));
        assertTrue(gm.isIdentityInLearningGroup(id2, g2.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id2, g3.getName(), course1)); // not a learning group
        assertFalse(gm.isIdentityInLearningGroup(id2, g4.getName(), course1)); // not a learning group

        DBFactory.getInstance().closeSession();
        assertTrue(gm.isIdentityInLearningGroup(id3, g1.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id3, g2.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id3, g3.getName(), course1)); // not a learning group
        assertFalse(gm.isIdentityInLearningGroup(id3, g4.getName(), course1)); // not a learning group

        assertTrue(gm.isIdentityInLearningGroup(id1, g1.getName(), c1.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id1, g1.getName(), c2.getName(), course1));
        assertTrue(gm.isIdentityInLearningGroup(id3, g1.getName(), c1.getName(), course1));
        assertFalse(gm.isIdentityInLearningGroup(id3, g1.getName(), c2.getName(), course1));

        // test areas
        DBFactory.getInstance().closeSession();
        assertTrue(gm.isIdentityInLearningArea(id1, a1.getName(), course1));
        assertTrue(gm.isIdentityInLearningArea(id1, a2.getName(), course1));
        assertTrue(gm.isIdentityInLearningArea(id1, a3.getName(), course1));

        assertTrue(gm.isIdentityInLearningArea(id2, a1.getName(), course1));
        assertTrue(gm.isIdentityInLearningArea(id2, a2.getName(), course1));
        assertTrue(gm.isIdentityInLearningArea(id2, a3.getName(), course1));

        DBFactory.getInstance().closeSession();
        assertTrue(gm.isIdentityInLearningArea(id3, a1.getName(), course1));
        assertTrue(gm.isIdentityInLearningArea(id3, a2.getName(), course1));
        assertFalse(gm.isIdentityInLearningArea(id3, a3.getName(), course1));

        DBFactory.getInstance().closeSession();
        assertTrue(gm.getLearningAreasOfGroupFromAllContexts(g1.getName(), course1).size() == 2);
        assertTrue(gm.getLearningAreasOfGroupFromAllContexts(g2.getName(), course1).size() == 2);

        // test rights
        DBFactory.getInstance().closeSession();
        assertTrue(gm.hasRight(id1, CourseRights.RIGHT_ARCHIVING, course1));
        assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, course1));
        assertTrue(gm.hasRight(id1, CourseRights.RIGHT_GROUPMANAGEMENT, course1));
        assertFalse(gm.hasRight(id1, CourseRights.RIGHT_ASSESSMENT, course1));
        assertTrue(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, c2.getName(), course1));
        assertFalse(gm.hasRight(id1, CourseRights.RIGHT_COURSEEDITOR, c1.getName(), course1));
        assertFalse(gm.hasRight(id2, CourseRights.RIGHT_COURSEEDITOR, course1));

        // test context
        DBFactory.getInstance().closeSession();
        assertTrue(gm.isIdentityInGroupContext(id1, c1.getName(), course1));
        assertTrue(gm.isIdentityInGroupContext(id1, c2.getName(), course1));
        assertTrue(gm.isIdentityInGroupContext(id2, c1.getName(), course1));
        assertFalse(gm.isIdentityInGroupContext(id2, c2.getName(), course1));
        assertTrue(gm.isIdentityInGroupContext(id3, c1.getName(), course1));
        assertTrue(gm.isIdentityInGroupContext(id3, c2.getName(), course1));
    }

}
