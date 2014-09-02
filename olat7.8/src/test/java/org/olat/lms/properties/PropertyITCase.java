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

package org.olat.lms.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Initial Date: Mar 11, 2004
 * 
 * @author Mike Stock Comment:
 */
public class PropertyITCase extends OlatTestCase implements OLATResourceable {

    private final long RESOURCE_ID = 42;
    private final String RESOURCE_TYPE = "org.olat.data.properties.PropertyITCase";
    private static final Logger log = LoggerHelper.getLogger();

    private static Identity identity = null, id2 = null, id3 = null, id4 = null;
    private static BusinessGroup group = null;
    private static org.olat.data.resource.OLATResource res = null;
    private static PropertyManager pm;
    private BusinessGroupService businessGroupService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() {
        businessGroupService = applicationContext.getBean(BusinessGroupService.class);
        pm = PropertyManager.getInstance();
        // identity with null User should be ok for test case
        identity = JunitTestHelper.createAndPersistIdentityAsUser("foo");
        id2 = JunitTestHelper.createAndPersistIdentityAsUser("eis");
        id3 = JunitTestHelper.createAndPersistIdentityAsUser("zwoi");
        id4 = JunitTestHelper.createAndPersistIdentityAsUser("drp");
        final OLATResourceManager orm = OLATResourceManager.getInstance();
        res = orm.findResourceable(this);
        if (res == null) {
            res = OLATResourceManager.getInstance().createOLATResourceInstance(this);
            OLATResourceManager.getInstance().saveOLATResource(res);
        }
        final List l = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
        if (l.size() == 0) {
            group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, identity, "a buddygroup", "a desc", null, null,
                    null/* enableWaitinglist */, null/* enableAutoCloseRanks */, null);
        } else {
            group = (BusinessGroup) l.get(0);
        }

    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() {
        try {
            log.info("tearDown: DB.getInstance().closeSession()");
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    /**
     * testGenericInsertFindDelete
     */
    @Test
    public void testGenericInsertFindDelete() {
        PropertyImpl p = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        DBFactory.getInstance().closeSession();
        p = pm.findProperty(identity, group, res, "cat", "TestProperty");
        assertNotNull(p);
        assertEquals(p.getStringValue(), "stringValue");
        assertEquals(p.getFloatValue(), new Float(1.1));
        assertEquals(p.getLongValue(), new Long(123456));
        assertEquals(p.getTextValue(), "textValue");
        pm.deleteProperty(p);
        p = pm.findProperty(identity, group, res, "cat", "TestProperty");
        assertNull(p);
    }

    /**
     * testGetAllResourceTypeNames
     */
    @Test
    public void testGetAllResourceTypeNames() {
        final PropertyImpl p = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        try {
            log.info("p='" + p + "'");
            log.info("res=" + res);
            log.info("res.getResourceableTypeName=" + res.getResourceableTypeName());
            pm.saveProperty(p);
            DBFactory.getInstance().closeSession();
            log.info("p='" + p + "'");
            log.info("res=" + res);
            log.info("res.getResourceableTypeName=" + res.getResourceableTypeName());
            final String resTypeName = p.getResourceTypeName();
            log.info("resTypeName=" + resTypeName);
            final List resTypeNames = pm.getAllResourceTypeNames();
            log.info("resTypeNames=" + resTypeNames);
            assertTrue(resTypeNames.contains(resTypeName));
        } finally {
            pm.deleteProperty(p);
        }
    }

    /**
     * testListProperties
     */
    @Test
    public void testListProperties() {
        final String resTypeName = res.getResourceableTypeName();
        final Long resTypeId = res.getResourceableId();
        final PropertyImpl p = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        final List entries = PropertyManager.getInstance().listProperties(identity, group, resTypeName, resTypeId, "cat", "TestProperty");
        if (entries.size() == 1) {
            final PropertyImpl prop = (PropertyImpl) entries.get(0);
            assertTrue(resTypeName.equals(prop.getResourceTypeName()));
            assertTrue(resTypeId.equals(prop.getResourceTypeId()));
        }

        pm.deleteProperty(p);
    }

    /**
     * testUserInsertFindDelete
     */
    @Test
    public void testUserInsertFindDelete() {
        PropertyImpl p = pm.createUserPropertyInstance(identity, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        DBFactory.getInstance().closeSession();
        p = pm.findUserProperty(identity, "cat", "TestProperty");
        assertNotNull(p);
        assertEquals(p.getStringValue(), "stringValue");
        assertEquals(p.getFloatValue(), new Float(1.1));
        assertEquals(p.getTextValue(), "textValue");
        pm.deleteProperty(p);
        p = pm.findUserProperty(identity, "cat", "TestProperty");
        assertNull(p);
    }

    /**
     * Performance test of 500 propertycreations per type. Rename to testPerf500Properties to include this test in the test suit.
     */
    public void rewqtestPerf500Properties() {

        long start, stop;
        final long count = 500;

        // create generic proerties
        System.out.println("----------------------------------------------------------------");
        System.out.println("Performance test startet. Running " + count + " cycles per test.");
        System.out.println("CREATE generic property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty" + i, new Float(1.1), new Long(123456), "stringValue",
                    "textValue");
            pm.saveProperty(p);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("CREATE generic property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");
        // some find identitites tests
        final List ids = pm.findIdentitiesWithProperty(null, null, "cat", "TestProperty", false);
        assertTrue(ids.size() == count);

        // create course and user properties
        System.out.println("Preparing user/group properties test. Creating additional properties..");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl pUser = pm.createUserPropertyInstance(identity, "cat", "TestProperty" + i, new Float(1.1), new Long(123456), "stringValue", "textValue");
            pm.saveProperty(pUser);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("Ready : " + (stop - start) + " ms (" + (2 * count * 1000 / (stop - start)) + "/sec)");
        System.out.println("Starting find tests. DB holds " + count * 3 + " records.");

        // find generic property test
        System.out.println("FIND generic property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.findProperty(identity, group, res, "cat", "TestProperty" + i);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("FIND generic property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");

        // find user property test
        System.out.println("FIND user property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.findUserProperty(identity, "cat", "TestProperty" + i);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("FIND user property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");

        // find & delete
        System.out.println("FIND and DELETE generic property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.findUserProperty(identity, "cat", "TestProperty" + i);
            pm.deleteProperty(p);
        }
        stop = System.currentTimeMillis();
        System.out.println("FIND and DELETE generic property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");

        // create additional db entries
        createAddlUserProperties(count, count * 5);
        System.out.println("DB holds " + count * 6 + " records.");

        // find user property test
        System.out.println("FIND user property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.findUserProperty(identity, "cat", "TestProperty" + i);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("FIND user property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");

        // create additional db entries
        createAddlUserProperties(count * 5, count * 11);
        System.out.println("DB holds " + count * 12 + " records.");

        // find user property test
        System.out.println("FIND user property test started...");
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final PropertyImpl p = pm.findUserProperty(identity, "cat", "TestProperty" + i);
            DBFactory.getInstance().closeSession();
        }
        stop = System.currentTimeMillis();
        System.out.println("FIND user property test: " + (stop - start) + " ms (" + (count * 1000 / (stop - start)) + "/sec)");

        System.out.println("----------------------------------------------------------------");
        System.out.println("Performance test finished.");
    }

    /**
     * testFloatValues THIS test does only success when you run it against hsqldb or mysql with FLOAT(65,30). FLOAT(65,30) is mysql specific and if you let hibernate
     * generate the tables automatic it will result in FLOAT only and this test will fail. So this means that you have to populate the tables yourself via the sql file.
     */
    @Test
    public void testFloatValues() {
        PropertyImpl original, copy;
        // gs:changed to FLOAT(65,30) to be compatible with hsqldb and auto generated ddl
        // Define my own MAX float value because the db precision changed from DECIMAL(78,36) to DECIMAL(65,30)
        final double floatMaxValue = 1E34; // 1E35 does failed with mysql 5.0.x
        original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(1234534343424213.1324534533456), new Long(123456), "stringValue",
                "textValue");
        pm.saveProperty(original);
        // DBFactory.getInstance().evict(original);
        DBFactory.getInstance().closeSession();
        copy = pm.findProperty(identity, group, res, "cat", "TestProperty");
        final float f1F = original.getFloatValue().floatValue();
        final float copyF = copy.getFloatValue().floatValue();
        assertTrue("values differ:" + f1F + ", and " + copyF, f1F == copyF);
        pm.deleteProperties(identity, group, res, "cat", "TestProperty");

        // note: on mysql 5.0, the standard installation is strict mode, which reports any data truncation error as a real jdbc error.
        // -1e35 seems out of range for DECIMAL(65,30) so data truncation occurs???
        // +-> 30 digits to the right side of decimal point
        // From mysql:
        // The declaration syntax for a DECIMAL column is DECIMAL(M,D). The ranges of values for the arguments in MySQL 5.1 are as follows:
        // M is the maximum number of digits (the precision). It has a range of 1 to 65. (Older versions of MySQL allowed a range of 1 to 254.)
        // D is the number of digits to the right of the decimal point (the scale). It has a range of 0 to 30 and must be no larger than M.

        // TODO:investigate here
        // if you want to suppress this issue, disable jdbc4 compliant truncation checking by e.g.
        // db.jdbc.url=jdbc:mysql://${db.host}/${db.name}?.....&amp;jdbcCompliantTruncation=false
        //
        original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(-floatMaxValue), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(original);
        DBFactory.getInstance().closeSession();
        copy = pm.findProperty(identity, group, res, "cat", "TestProperty");// this one failes at the moment for hsqldb with: incompatible data type in
        // conversion: from
        // SQL type DECIMAL to java.lang.Double, value:
        // -9999999790214767953607394487959552.000000000000000000000000000000
        assertTrue(original.getFloatValue().floatValue() == copy.getFloatValue().floatValue());
        pm.deleteProperties(identity, group, res, "cat", "TestProperty");

        original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(floatMaxValue), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(original);
        DBFactory.getInstance().closeSession();
        copy = pm.findProperty(identity, group, res, "cat", "TestProperty");
        assertTrue(original.getFloatValue().floatValue() == copy.getFloatValue().floatValue());
        pm.deleteProperties(identity, group, res, "cat", "TestProperty");

        original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(Long.MAX_VALUE), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(original);
        DBFactory.getInstance().closeSession();
        copy = pm.findProperty(identity, group, res, "cat", "TestProperty");
        assertTrue(original.getFloatValue().floatValue() == copy.getFloatValue().floatValue());
        pm.deleteProperties(identity, group, res, "cat", "TestProperty");

        original = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(Long.MIN_VALUE), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(original);
        DBFactory.getInstance().closeSession();
        copy = pm.findProperty(identity, group, res, "cat", "TestProperty");
        assertTrue(original.getFloatValue().floatValue() == copy.getFloatValue().floatValue());
        pm.deleteProperties(identity, group, res, "cat", "TestProperty");

    }

    @Test
    public void testRealWorldScoreFloatValues() {
        // delete everything from previous tests
        pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue1");
        pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue2");
        // Test setting and getting of a normal float value
        PropertyImpl prop1 = pm.createPropertyInstance(null, null, null, "test", "TestPropertyFloatValue1", new Float(0.9), null, null, null);
        pm.saveProperty(prop1);
        PropertyImpl prop2 = pm.createPropertyInstance(null, null, null, "test", "TestPropertyFloatValue2", new Float(0.1), null, null, null);
        pm.saveProperty(prop2);
        DBFactory.getInstance().closeSession();
        prop1 = pm.findProperty(null, null, null, "test", "TestPropertyFloatValue1");
        prop2 = pm.findProperty(null, null, null, "test", "TestPropertyFloatValue2");
        // In course assessment 0.1 + 0.9 must exactly give 1 as a result
        assertEquals(1, prop1.getFloatValue().floatValue() + prop2.getFloatValue().floatValue(), 0);
        pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue1");
        pm.deleteProperties(null, null, null, "test", "TestPropertyFloatValue2");
    }

    @Test
    public void testFindIdentities() {
        PropertyImpl p = pm.createPropertyInstance(identity, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        p = pm.createPropertyInstance(id2, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        p = pm.createPropertyInstance(id3, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        p = pm.createPropertyInstance(id4, group, res, "cat", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        p = pm.createPropertyInstance(identity, group, res, "cat2", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);
        p = pm.createPropertyInstance(id2, group, res, "cat2", "TestProperty", new Float(1.1), new Long(123456), "stringValue", "textValue");
        pm.saveProperty(p);

        DBFactory.getInstance().closeSession();
        // now find identities
        List ids = pm.findIdentitiesWithProperty(res, "cat", "TestProperty", false);
        assertTrue(ids.size() == 4);
        ids = pm.findIdentitiesWithProperty(res, "cat", "TestProperty", true);
        assertTrue(ids.size() == 4);
        ids = pm.findIdentitiesWithProperty(null, "cat", "TestProperty", false);
        assertTrue(ids.size() == 4);
        ids = pm.findIdentitiesWithProperty(null, "cat", "TestProperty", true);
        assertTrue(ids.size() == 0);
        ids = pm.findIdentitiesWithProperty(null, "cat2", "TestProperty", false);
        assertTrue(ids.size() == 2);
        ids = pm.findIdentitiesWithProperty(null, null, "TestProperty", false);
        assertTrue(ids.size() == 4); // not 6, must be distinct
        ids = pm.findIdentitiesWithProperty(null, null, "TestProperty", true);
        assertTrue(ids.size() == 0);
    }

    @Test
    public void testFindProperties() {
        final String category = "cat3";
        final String propertyName = "TestProperty3";
        final String textValue = "textValue3";
        PropertyImpl p = pm.createPropertyInstance(identity, group, res, category, propertyName, new Float(1.1), new Long(123456), "stringValue", textValue);
        pm.saveProperty(p);
        p = pm.createPropertyInstance(id2, group, res, category, propertyName, new Float(1.1), new Long(123456), "stringValue", textValue);
        pm.saveProperty(p);
        final List propertyList = pm.findProperties(identity, group, res.getResourceableTypeName(), res.getResourceableId(), category, propertyName);
        assertEquals(1, propertyList.size());
        assertEquals(propertyName, ((PropertyImpl) propertyList.get(0)).getName());
        assertEquals(textValue, ((PropertyImpl) propertyList.get(0)).getTextValue());
        pm.deleteProperties(identity, group, res, category, propertyName);
        pm.deleteProperties(id2, group, res, category, propertyName);
    }

    private void createAddlUserProperties(final long count, final long l) {
        System.out.println("Creating additional properties...");
        final long start = System.currentTimeMillis();
        for (long i = count; i < l; i++) {
            final PropertyImpl pUser = pm.createUserPropertyInstance(identity, "cat", "TestProperty" + i, new Float(1.1), new Long(123456), "stringValue", "textValue");
            pm.saveProperty(pUser);
            DBFactory.getInstance().closeSession();
        }
        final long stop = System.currentTimeMillis();
        System.out.println("Ready : " + (stop - start) + " ms (" + ((l - count) * 1000 / (stop - start)) + "/sec)");
    }

    @Override
    public String getResourceableTypeName() {
        return RESOURCE_TYPE;
    }

    @Override
    public Long getResourceableId() {
        return new Long(RESOURCE_ID);
    }
}
