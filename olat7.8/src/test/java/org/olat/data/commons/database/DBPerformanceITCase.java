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
package org.olat.data.commons.database;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * DBPerformanceITCase contains the performance test(s) from DBITCase
 * 
 * <P>
 * Initial Date: Jun 15, 2011 <br>
 * 
 * @author patrick
 */
public class DBPerformanceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    @Test
    public void testDbPerf() {
        final int loops = 1000;
        long timeWithoutTransction = 0;
        log.info("start testDbPerf with loops=" + loops);
        try {
            final long startTime = System.currentTimeMillis();
            for (int loopCounter = 0; loopCounter < loops; loopCounter++) {
                final String propertyKey = "testDbPerfKey-" + loopCounter;
                final DB db = DBFactory.getInstance();
                final PropertyManager pm = PropertyManager.getInstance();
                final String testValue = "testDbPerfValue-" + loopCounter;
                final PropertyImpl p = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
                pm.saveProperty(p);
                // forget session cache etc.
                db.closeSession();
                pm.deleteProperty(p);
            }
            final long endTime = System.currentTimeMillis();
            timeWithoutTransction = endTime - startTime;
            log.info("testDbPerf without transaction takes :" + timeWithoutTransction + "ms");
        } catch (final Exception ex) {
            fail("Exception in testDbPerf without transaction ex=" + ex);
        }

        try {
            final long startTime = System.currentTimeMillis();
            for (int loopCounter = 0; loopCounter < loops; loopCounter++) {
                final String propertyKey = "testDbPerfKey-" + loopCounter;
                DB db = DBFactory.getInstance();
                final PropertyManager pm = PropertyManager.getInstance();
                final String testValue = "testDbPerfValue-" + loopCounter;
                final PropertyImpl p = pm.createPropertyInstance(null, null, null, null, propertyKey, null, null, testValue, null);
                pm.saveProperty(p);
                // forget session cache etc.
                db.closeSession();
                db = DBFactory.getInstance();
                pm.deleteProperty(p);
            }
            final long endTime = System.currentTimeMillis();
            final long timeWithTransction = endTime - startTime;
            log.info("testDbPerf with transaction takes :" + timeWithTransction + "ms");
            log.info("testDbPerf diff between transaction and without transaction :" + (timeWithTransction - timeWithoutTransction) + "ms");
        } catch (final Exception ex) {
            fail("Exception in testDbPerf with transaction ex=" + ex);
        }
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        DBFactory.getInstance().closeSession();
    }

}
