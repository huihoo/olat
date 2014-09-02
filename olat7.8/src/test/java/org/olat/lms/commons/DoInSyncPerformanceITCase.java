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

package org.olat.lms.commons;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 */
public class DoInSyncPerformanceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private BaseSecurity securityManager;

    @Test
    public void testDoInSyncPerformance() {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testDoInSyncPerformance", new Long("123989456"));
        final int maxLoop = 1000;

        final RepositoryEntry re = RepositoryServiceImpl.getInstance().createRepositoryEntryInstance("test", "perfTest", "perfTest description");
        re.setDisplayname("testPerf");
        // create security group
        final SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
        re.setOwnerGroup(ownerGroup);
        RepositoryServiceImpl.getInstance().saveRepositoryEntry(re);
        DBFactory.getInstance().closeSession();

        // 1. Do job without doInSync
        System.out.println("testDoInSyncPerformance: start test with doInSync");
        final long startTimeWithoutSync = System.currentTimeMillis();
        for (int i = 0; i < maxLoop; i++) {
            doTestPerformanceJob(re);
            DBFactory.getInstance().closeSession();
        }
        final long endTimeWithoutSync = System.currentTimeMillis();

        // 2. Do job with doInSync
        System.out.println("testDoInSyncPerformance: start test with doInSync");
        final long startTimeDoInSync = System.currentTimeMillis();
        for (int i = 0; i < maxLoop; i++) {
            CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
                @Override
                public void execute() {
                    doTestPerformanceJob(re);
                }

            });// end syncerCallback
            DBFactory.getInstance().closeSession();
        }
        final long endTimeDoInSync = System.currentTimeMillis();

        // Compare time
        final long timeWithoutSync = endTimeWithoutSync - startTimeWithoutSync;
        final float perJobWithoutSync = (float) timeWithoutSync / maxLoop;
        System.out.println("testDoInSyncPerformance timeWithoutSync=" + timeWithoutSync + " ms for loop with " + maxLoop + " iterations");
        System.out.println("testDoInSyncPerformance perJobWithoutSync=" + perJobWithoutSync + " ms");

        final long timeWithDoInSync = endTimeDoInSync - startTimeDoInSync;
        final float perJobWithDoInSync = (float) timeWithDoInSync / maxLoop;
        System.out.println("testDoInSyncPerformance timeWithDoInSync=" + timeWithDoInSync + " ms for loop with " + maxLoop + " iterations");
        System.out.println("testDoInSyncPerformance perJobWithDoInSync=" + perJobWithDoInSync + " ms");

        final long timeDiffLoop = timeWithDoInSync - timeWithoutSync;
        final float timeDiffPerCall = perJobWithDoInSync - perJobWithoutSync;
        System.out.println("testDoInSyncPerformance diffLoop=" + timeDiffLoop + " ms for loop with " + maxLoop + " iterations");
        System.out.println("testDoInSyncPerformance diffPerCall=" + timeDiffPerCall + " ms");
        // Assert 10% Overhead
        assertTrue("DoInSync overhead is more than 15%", timeDiffLoop < ((timeWithoutSync * 115) / 100));
    }

    private Boolean doTestPerformanceJob(final RepositoryEntry re) {
        final RepositoryEntry reloadedRe = (RepositoryEntry) DBFactory.getInstance().loadObject(re, true);
        reloadedRe.incrementLaunchCounter();
        reloadedRe.setLastUsage(new Date());
        RepositoryServiceImpl.getInstance().updateRepositoryEntry(reloadedRe);
        return true;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

}
