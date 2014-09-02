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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.group.learn.CourseRights;
import org.olat.lms.group.right.BGRightManager;
import org.olat.lms.group.right.BGRightManagerImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    private Identity id1, id2, id3, id4;
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private BGAreaDao areaManager;
    @Autowired
    private CourseGroupManager courseGroupManager;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private DB database;
    @Autowired
    private BusinessGroupContextService bgContextService;
    @Autowired
    private BGContextDao bgContextDao;
    private static OLATResource course1 = null;

    /**
     * SetUp is called before each test.
     */
    @Before
    public void setUp() {
        try {
            id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
            id2 = JunitTestHelper.createAndPersistIdentityAsUser("twoo");
            id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");
            id4 = JunitTestHelper.createAndPersistIdentityAsUser("four");

            if (course1 == null) {

                final OLATResourceManager rm = OLATResourceManager.getInstance();
                // create course and persist as OLATResourceImpl
                final OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse", new Long(456));
                course1 = rm.createOLATResourceInstance(resourceable);
                database.saveObject(course1);

                database.closeSession();
            }
        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
            e.printStackTrace();
        }
    }

    /**
     * TearDown is called after each test.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        try {
            // OLATResourceManager.getInstance().deleteOLATResource(course1);
            database.closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
            e.printStackTrace();
            throw e;
        }
    }

    /** BGContextManagerImpl:createAndPersistBGContext * */
    @Test
    public void testCreateAndPersistBGContext() {
        final BGContext c1 = bgContextService.createAndPersistBGContext("c1name", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
        assertNotNull(c1);
        final BGContext c2 = bgContextService.createAndPersistBGContext("c2name", "c2desc", BusinessGroup.TYPE_LEARNINGROUP, id1, false);
        assertNotNull(c2);
        try {
            bgContextService.createAndPersistBGContext("name", "desc", null, id2, false);
            fail("context groupType can not be null");
        } catch (final AssertException e) {
            // expected exception
            assertTrue(true);
        }
        try {
            bgContextService.createAndPersistBGContext(null, "desc", BusinessGroup.TYPE_LEARNINGROUP, id2, false);
            fail("context name can not be null");
        } catch (final AssertException e) {
            // expected exception
            assertTrue(true);
        }
    }

    /** BGContextManagerImpl:deleteBGContext() * */
    @Test
    public void testDeleteBGContext() {
        final BGContext c1 = bgContextService.createAndPersistBGContext("c1name1", "c1desc1", BusinessGroup.TYPE_LEARNINGROUP, null, true);
        final BGContext c2 = bgContextService.createAndPersistBGContext("c2name1", "c2desc1", BusinessGroup.TYPE_RIGHTGROUP, id1, false);

        database.closeSession(); // simulate user clicks

        final BusinessGroup g1 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0), new Integer(10),
                false, false, c1);
        final BusinessGroup g2 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0), new Integer(10),
                false, false, c1);
        final BusinessGroup g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, new Integer(0), new Integer(10),
                false, false, c2);

        database.closeSession(); // simulate user clicks
        baseSecurity.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
        baseSecurity.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
        baseSecurity.addIdentityToSecurityGroup(id4, g2.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id4, g1.getOwnerGroup());
        baseSecurity.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id2, g3.getPartipiciantGroup());

        final BGRightManagerImpl rm = BGRightManagerImpl.getInstance();
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
        rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
        database.closeSession(); // simulate user clicks

        assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id1, c2));
        assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));

        database.closeSession(); // simulate user clicks
        final BGArea a1 = areaManager.createAndPersistBGAreaIfNotExists("a1-delete", "desca1", c1);
        final BGArea a2 = areaManager.createAndPersistBGAreaIfNotExists("a2-delete", null, c1);
        final BGArea a3 = areaManager.createAndPersistBGAreaIfNotExists("a3-delete", "desca3", c1);
        final BGArea a4 = areaManager.createAndPersistBGAreaIfNotExists("a4-delete", "desca4", c1);
        areaManager.addBGToBGArea(g1, a1);
        areaManager.addBGToBGArea(g2, a1);
        areaManager.addBGToBGArea(g1, a2);
        areaManager.addBGToBGArea(g2, a3);
        areaManager.addBGToBGArea(g1, a4);
        database.closeSession(); // simulate user clicks

        // test isIdentityInBGArea
        assertTrue(areaManager.isIdentityInBGArea(id1, "a1-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id1, "a2-delete", c1));
        assertFalse(areaManager.isIdentityInBGArea(id1, "a3-delete", c1)); // not in group g2
        assertTrue(areaManager.isIdentityInBGArea(id1, "a4-delete", c1));
        assertFalse(areaManager.isIdentityInBGArea(id1, "xx", c1)); // wrong area name
        assertFalse(areaManager.isIdentityInBGArea(id1, "a1-delete", c2)); // wrong context
        assertTrue(areaManager.isIdentityInBGArea(id2, "a1-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id2, "a2-delete", c1));
        assertFalse(areaManager.isIdentityInBGArea(id2, "a3-delete", c1)); // not in group g2
        assertTrue(areaManager.isIdentityInBGArea(id2, "a4-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id3, "a1-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id3, "a2-delete", c1));
        assertFalse(areaManager.isIdentityInBGArea(id3, "a3-delete", c1)); // not in group g2
        assertTrue(areaManager.isIdentityInBGArea(id3, "a4-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id4, "a1-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id4, "a2-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id4, "a3-delete", c1));
        assertTrue(areaManager.isIdentityInBGArea(id4, "a4-delete", c1));

        database.closeSession(); // simulate user clicks
        assertTrue(areaManager.findBGAreasOfBusinessGroup(g1).size() == 3);
        assertTrue(areaManager.findBGAreasOfBusinessGroup(g2).size() == 2);
        assertTrue(areaManager.findBGAreasOfBusinessGroup(g3).size() == 0);

        database.closeSession(); // simulate user clicks
        assertTrue(areaManager.findBGAreasOfBGContext(c1).size() == 4);
        assertTrue(areaManager.findBGAreasOfBGContext(c2).size() == 0);

        database.closeSession(); // simulate user clicks
        assertTrue(areaManager.findBusinessGroupsOfArea(a1).size() == 2);
        assertTrue(areaManager.findBusinessGroupsOfArea(a2).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfArea(a3).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfArea(a4).size() == 1);

        database.closeSession(); // simulate user clicks
        assertTrue(areaManager.countBGAreasOfBGContext(c1) == 4);
        assertTrue(areaManager.countBGAreasOfBGContext(c2) == 0);

        database.closeSession(); // simulate user clicks
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id1, "a1-delete", c1).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id1, "a2-delete", c1).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id1, "a3-delete", c1).size() == 0);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id1, "a4-delete", c1).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id4, "a1-delete", c1).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id4, "a2-delete", c1).size() == 0);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id4, "a3-delete", c1).size() == 1);
        assertTrue(areaManager.findBusinessGroupsOfAreaAttendedBy(id4, "a4-delete", c1).size() == 0);

        bgContextService.deleteCompleteBGContext(c1);
        // assertNull(DB.getInstance().findObject(BGContextImpl.class,
        // c1.getKey()));

        bgContextService.deleteCompleteBGContext(c2);
        // assertNull(DB.getInstance().findObject(BGContextImpl.class,
        // c2.getKey()));

        assertTrue(areaManager.findBGAreasOfBGContext(c1).size() == 0);
        assertNull(areaManager.findBGArea("a1-delete", c1));
        assertTrue(areaManager.findBGAreasOfBusinessGroup(g1).size() == 0);
        assertTrue(areaManager.findBGAreasOfBGContext(c2).size() == 0);
        assertNull(areaManager.findBGArea("a2-delete", c1));
        assertTrue(areaManager.findBusinessGroupsOfArea(a1).size() == 0);
        assertTrue(areaManager.findBusinessGroupsOfArea(a2).size() == 0);
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id1, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));
    }

    /** BGContextManagerImpl:copyBGContext() * */
    @Test
    public void testCopyBGContext() {
        final BGContext c1 = bgContextService.createAndPersistBGContext("c1name2", "c1desc2", BusinessGroup.TYPE_LEARNINGROUP, null, true);
        final BGContext c2 = bgContextService.createAndPersistBGContext("c2name2", "c2desc2", BusinessGroup.TYPE_RIGHTGROUP, id1, false);

        database.closeSession(); // simulate user clicks
        final BusinessGroup g1 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0), new Integer(10),
                false, false, c1);
        final BusinessGroup g2 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0), new Integer(10),
                false, false, c1);
        final BusinessGroup g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, new Integer(0), new Integer(10),
                false, false, c2);

        database.closeSession(); // simulate user clicks
        baseSecurity.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
        baseSecurity.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id3, g1.getOwnerGroup());
        baseSecurity.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id2, g3.getPartipiciantGroup());

        database.closeSession(); // simulate user clicks
        final BGRightManagerImpl rm = BGRightManagerImpl.getInstance();
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g3);
        rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);

        database.closeSession(); // simulate user clicks
        final BGArea a1 = areaManager.createAndPersistBGAreaIfNotExists("a1-copy", "desca1", c1);
        final BGArea a2 = areaManager.createAndPersistBGAreaIfNotExists("a2-copy", null, c1);
        areaManager.addBGToBGArea(g1, a1);
        areaManager.addBGToBGArea(g2, a1);
        areaManager.addBGToBGArea(g1, a2);
        database.closeSession(); // simulate user clicks

        final BGContext c1copy = businessGroupService.copyAndAddBGContextToResource(c1.getName(), course1, c1);
        database.closeSession(); // simulate user clicks
        try {
            businessGroupService.copyAndAddBGContextToResource(c2.getName(), course1, c2);
            fail("expecting exeption");
        } catch (final AssertException e) {
            // ok, passed
        }
        database.closeSession(); // simulate user clicks

        assertTrue(areaManager.findBGAreasOfBGContext(c1copy).size() == 2);
        assertNotNull(areaManager.findBGArea("a1-copy", c1));
        assertNotNull(areaManager.findBGArea("a2-copy", c1));
        assertNotNull(bgContextDao.findGroupOfBGContext(g1.getName(), c1copy));
        assertNotNull(bgContextDao.findGroupOfBGContext(g2.getName(), c1copy));
        assertTrue(bgContextDao.getGroupsOfBGContext(c1copy).size() == 2);
        bgContextService.deleteCompleteBGContext(c1copy);
    }

    /** BGContextManagerImpl:deleteBGContext() * */
    @Test
    public void testBGRights() {
        final BGContext c1 = bgContextService.createAndPersistBGContext("c1name3", "c1desc3", BusinessGroup.TYPE_RIGHTGROUP, null, true);
        final BGContext c2 = bgContextService.createAndPersistBGContext("c2name3", "c2desc3", BusinessGroup.TYPE_RIGHTGROUP, id1, false);

        final BusinessGroup g1 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g1", null, null, null, false, false, c1);
        final BusinessGroup g2 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g2", null, null, null, false, false, c1);
        final BusinessGroup g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "g3", null, null, null, false, false, c2);

        baseSecurity.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());
        baseSecurity.addIdentityToSecurityGroup(id3, g3.getPartipiciantGroup());

        final BGRightManager rm = BGRightManagerImpl.getInstance();
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g1);
        rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
        rm.addBGRight(CourseRights.RIGHT_ARCHIVING, g2);
        rm.addBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);
        database.closeSession(); // simulate user clicks

        // baseSecurity.createAndPersistPolicy(rightGroup.getPartipiciantGroup(), bgRight,
        // rightGroup.getGroupContext());
        final List groups = baseSecurity.getGroupsWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, g1.getGroupContext());
        assertTrue(groups.size() == 2);

        final List identities = baseSecurity.getIdentitiesWithPermissionOnOlatResourceable(CourseRights.RIGHT_ARCHIVING, g1.getGroupContext());
        assertTrue(identities.size() == 2);

        final List policies = baseSecurity.getPoliciesOfSecurityGroup(g1.getPartipiciantGroup());
        assertTrue(policies.size() == 3); // read, archiving, courseeditor

        database.closeSession(); // simulate user clicks
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c2));
        assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
        assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, id2, c1));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id3, c2));
        assertTrue(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c1));

        /*
         * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g1)); assertTrue(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, g1));
         * assertTrue(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, g2)); assertFalse(rm.hasBGRight(CourseRights.RIGHT_GROUPMANAGEMENT, g1));
         */
        assertTrue(rm.findBGRights(g1).size() == 2);
        assertTrue(rm.findBGRights(g2).size() == 1);

        database.closeSession(); // simulate user clicks
        rm.removeBGRight(CourseRights.RIGHT_ARCHIVING, g1);
        rm.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g1);
        rm.removeBGRight(CourseRights.RIGHT_ARCHIVING, g2);
        rm.removeBGRight(CourseRights.RIGHT_COURSEEDITOR, g3);

        database.closeSession(); // simulate user clicks
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id1, c1));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_ARCHIVING, id2, c1));
        assertFalse(rm.hasBGRight(CourseRights.RIGHT_COURSEEDITOR, id3, c2));

        assertTrue(rm.findBGRights(g1).size() == 0);
        assertTrue(rm.findBGRights(g2).size() == 0);
    }

    /** BGContextManagerImpl:getGroupsOfBGContext and countGroupsOfBGContext* */
    @Test
    public void testGroupsOfBGContext() {
        final BGContext c1 = bgContextService.createAndPersistBGContext("c1name4", "c1desc", BusinessGroup.TYPE_LEARNINGROUP, null, true);
        final BGContext c2 = bgContextService.createAndPersistBGContext("c2name4", "c2desc", BusinessGroup.TYPE_LEARNINGROUP, id1, false);

        database.closeSession(); // simulate user clicks
        assertTrue(bgContextDao.getGroupsOfBGContext(c1).size() == 0);
        assertTrue(bgContextDao.countGroupsOfBGContext(c1) == 0);

        database.closeSession(); // simulate user clicks
        final BusinessGroup g1 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g1", null, new Integer(0), new Integer(10),
                false, false, c1);
        assertNotNull(g1);
        final BusinessGroup g2 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0), new Integer(10),
                false, false, c1);
        assertNotNull(g2);
        final BusinessGroup g3 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g3", null, new Integer(0), new Integer(10),
                false, false, c2);
        assertNotNull(g3);

        final BusinessGroup g2douplicate = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0),
                new Integer(10), false, false, c1);
        assertNull(g2douplicate); // name douplicate names allowed per group context

        final BusinessGroup g4 = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "g2", null, new Integer(0), new Integer(10),
                false, false, c2);
        assertNotNull(g4); // name douplicate in other context allowed

        database.closeSession(); // simulate user clicks
        assertTrue(bgContextDao.getGroupsOfBGContext(c1).size() == 2);
        assertTrue(bgContextDao.countGroupsOfBGContext(c1) == 2);
    }

    /** BGContext2ResourceManager tests */
    @Test
    public void testFindContextMethods() {
        final BGContext c1 = businessGroupService.createAndAddBGContextToResource("c1name5", course1, BusinessGroup.TYPE_LEARNINGROUP, null, true);
        businessGroupService.createAndAddBGContextToResource("c2name5", course1, BusinessGroup.TYPE_LEARNINGROUP, id4, false);
        businessGroupService.createAndAddBGContextToResource("c3name5", course1, BusinessGroup.TYPE_RIGHTGROUP, id2, false);

        database.closeSession(); // simulate user clicks
        assertTrue(bgContextDao.findBGContextsForResource(course1, true, true).size() == 3);
        assertTrue(bgContextDao.findBGContextsForResource(course1, true, false).size() == 1);
        assertTrue(bgContextDao.findBGContextsForResource(course1, false, true).size() == 2);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_LEARNINGROUP, true, true).size() == 2);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_RIGHTGROUP, true, true).size() == 1);

        assertTrue(bgContextDao.findBGContextsForIdentity(id4, true, true).size() == 1);
        assertTrue(bgContextDao.findBGContextsForIdentity(id4, true, false).size() == 0);
        assertTrue(bgContextDao.findBGContextsForIdentity(id4, false, true).size() == 1);

        database.closeSession(); // simulate user clicks
        bgContextService.removeBGContextFromResource(c1, course1);
        assertTrue(bgContextDao.findBGContextsForResource(course1, true, true).size() == 2);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_LEARNINGROUP, true, true).size() == 1);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_RIGHTGROUP, true, true).size() == 1);

        database.closeSession(); // simulate user clicks
        // cm.removeAllBGContextsFromResource(course1);
        courseGroupManager.deleteCourseGroupmanagement(course1);
        assertTrue(bgContextDao.findBGContextsForResource(course1, true, true).size() == 0);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_LEARNINGROUP, true, true).size() == 0);
        assertTrue(bgContextDao.findBGContextsForResource(course1, BusinessGroup.TYPE_RIGHTGROUP, true, true).size() == 0);
    }

    @Test
    public void testRemoveBGFromArea() {
        final BGContext bgContext = businessGroupService.createAndAddBGContextToResource("c2name6", course1, BusinessGroup.TYPE_LEARNINGROUP, null, true);
        assertEquals(1, bgContextDao.findBGContextsForResource(course1, true, true).size());
        bgContextService.removeBGContextFromResource(bgContext, course1);
        assertEquals(0, bgContextDao.findBGContextsForResource(course1, true, true).size());
    }

}
