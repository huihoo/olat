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
package org.olat.lms.instantmessaging;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.commons.taskexecutor.TaskExecutorService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Unit test for IM Preferences
 * <P>
 * Initial Date: 12.08.2008 <br>
 * 
 * @author guido
 */
public class IMPrefsUnitITCase extends OlatTestCase {
    private static final Logger log = LoggerHelper.getLogger();

    String testUserA = "anIdentity1";
    String testUserB = "anIdentity2";
    String testUserC = "anIdentity3";
    String testUserD = "anIdentity4";

    @Autowired
    private BaseSecurity securityManager;

    @SuppressWarnings("unchecked")
    @Test
    public void testPrefs() {
        final List usernames = new ArrayList();
        final List indentities = new ArrayList();
        usernames.add(testUserA);
        usernames.add(testUserB);
        usernames.add(testUserC);
        usernames.add(testUserD);

        for (final Iterator iterator = usernames.iterator(); iterator.hasNext();) {
            final String name = (String) iterator.next();
            final Identity ident = securityManager.findIdentityByName(name);
            assertNotNull(ident);
            indentities.add(ident);
        }
        final long start = System.currentTimeMillis();

        int runs = 0;
        while (runs < 100) {
            final double rand = Math.random() * 3;
            final int i = Long.valueOf((Math.round(rand))).intValue();
            final ImPrefsManager mgr = ImPrefsManager.getInstance();
            Identity ident = (Identity) indentities.get(i);
            final ImPreferences prefs = mgr.loadOrCreatePropertiesFor((Identity) indentities.get(i));
            assertNotNull(prefs);
            assertNotNull(prefs.getDbProperty());

            try {
                Thread.sleep(100);
            } catch (final InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            TaskExecutorService service = applicationContext.getBean(TaskExecutorService.class);
            for (final Iterator iterator = indentities.iterator(); iterator.hasNext();) {
                ident = (Identity) iterator.next();
                service.runTask(new IMPrefsTask(ident));
                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            runs++;
        }

        final long stop = System.currentTimeMillis();
        System.out.println("took time in s:" + (stop - start) / 1000);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        JunitTestHelper.createAndPersistIdentityAsUser(testUserA);
        JunitTestHelper.createAndPersistIdentityAsUser(testUserB);
        JunitTestHelper.createAndPersistIdentityAsUser(testUserC);
        JunitTestHelper.createAndPersistIdentityAsUser(testUserD);
        DBFactory.getInstance().closeSession();
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        try {
            final DB db = DBFactory.getInstance();
            db.closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

}
