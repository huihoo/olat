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

package org.olat.data.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Initial Date: Mar 11, 2004
 * 
 * @author Mike Stock Comment:
 */
public class LifeCycleManagerITCase extends OlatTestCase implements OLATResourceable {

    private static final Logger log = LoggerHelper.getLogger();
    private final long RESOURCE_ID = 144;
    private final String RESOURCE_TYPE = "org.olat.data.lifecycle.LifeCycleManagerITCase";
    private static Identity identity = null;
    private static org.olat.data.resource.OLATResource res = null;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() {
        // identity with null User should be ok for test case
        res = OLATResourceManager.getInstance().createOLATResourceInstance(this);
        OLATResourceManager.getInstance().saveOLATResource(res);
        identity = JunitTestHelper.createAndPersistIdentityAsUser("foo");
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() {
        try {
            OLATResourceManager.getInstance().deleteOLATResource(res);
            log.info("tearDown: DB.getInstance().closeSession()");
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

    /**
     * Test creation of LifeCycleManager.
     */
    @Test
    public void testCreateInstanceFor() {
        final LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(identity);
        final LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
        assertNotSame("testCreateInstanceFor should NOT return the same instance", lcm1, lcm2);
    }

    /**
     * Test: mark two timestamp in different context.
     */
    @Test
    public void testMarkTimestampFor() {
        final String action = "doTest";
        final LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(identity);
        final LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
        lcm1.markTimestampFor(action);
        lcm2.markTimestampFor(action);
        DBFactory.getInstance().closeSession();
        final LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
        assertNotNull("Does not found LifeCycleEntry", lce);
        assertEquals("Invalid action", lce.getAction(), action);
        // try second instance of LifeCycleManager
        final LifeCycleEntry lce2 = lcm2.lookupLifeCycleEntry(action, null);
        assertNotNull("Does not found LifeCycleEntry", lce2);
        assertNotSame("LifeCycleEntry have not the same reference", lce2.getPersistentRef(), lce.getPersistentRef());
        assertNotSame("LifeCycleEntry have not the same type-name", lce2.getPersistentTypeName(), lce.getPersistentTypeName());
    }

    /**
     * Test: Delete Timestamp for certain action
     */
    @Test
    public void testDeleteTimestampFor() {
        final String action = "doTestDelete";
        final LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(identity);
        lcm1.markTimestampFor(action);
        final LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
        assertNotNull("Does not found LifeCycleEntry", lce);
        lcm1.deleteTimestampFor(action);
        final LifeCycleEntry lce2 = lcm1.lookupLifeCycleEntry(action, null);
        assertNull("Found deleted LifeCycleEntry", lce2);

    }

    // ////////////////////////////
    // Implements OLATResourceable
    // ////////////////////////////
    @Override
    public String getResourceableTypeName() {
        return RESOURCE_TYPE;
    }

    @Override
    public Long getResourceableId() {
        return new Long(RESOURCE_ID);
    }
}
