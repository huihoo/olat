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

package org.olat.data.coordinate.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.data.TestTable;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.exception.DBRuntimeException;
import org.olat.data.coordinate.ClusterCoordinator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockEntry;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.Locker;
import org.olat.system.event.Event;
import org.olat.system.event.SignOnOffEvent;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 */
public class LockITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();
    private static final int MAX_COUNT = 30; // at least 2
    private static final int MAX_USERS_MORE = 100; // 20; //100;

    @Test
    public void testCreateDeleteAcquire() {
        // some setup
        final List<Identity> identities = new ArrayList<Identity>();
        final BaseSecurity baseSecurityManager = applicationContext.getBean(BaseSecurity.class);
        for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) {
            final Identity i1 = baseSecurityManager.createAndPersistIdentity("u" + i, null, null, null, null);
            identities.add(i1);
        }
        DBFactory.getInstance().closeSession();

        final ClusterLockDao cm = applicationContext.getBean(ClusterLockDao.class);
        final Identity ident = identities.get(0);
        final Identity ident2 = identities.get(1);
        final OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(LockITCase.class.getName(), new Long(123456789));

        // ------------------ test the clusterlockmanager ----------------------
        // create a lock
        final String asset = OresHelper.createStringRepresenting(ores, "locktest");
        final LockImpl li = cm.createLockImpl(asset, ident);
        cm.saveLock(li);
        DBFactory.getInstance().closeSession();

        // find it
        final LockImpl l2 = cm.findLock(asset);
        assertNotNull(l2);
        assertEquals(li.getKey(), l2.getKey());

        // delete it
        cm.deleteLock(l2);
        DBFactory.getInstance().closeSession();

        // may not find it again
        final LockImpl l3 = cm.findLock(asset);
        assertNull(l3);

        // ------------------ test the clusterlocker ----------------------
        // access the cluster locker explicitely
        final Locker cl = applicationContext.getBean(ClusterCoordinator.class).getLocker();

        // acquire
        final LockResult res1 = cl.acquireLock(ores, ident, "abc");
        assertTrue(res1.isSuccess());
        DBFactory.getInstance().closeSession();

        // reacquire same identity (get from db)
        final LockResult res11 = cl.acquireLock(ores, ident, "abc");
        final long lock1Ac = res11.getLockAquiredTime();
        assertTrue(res11.isSuccess());
        DBFactory.getInstance().closeSession();

        // acquire by another identity must fail
        final LockResult res2 = cl.acquireLock(ores, ident2, "abc");
        assertFalse(res2.isSuccess());
        DBFactory.getInstance().closeSession();

        // reacquire same identity
        final LockResult res3 = cl.acquireLock(ores, ident, "abc");
        assertTrue(res3.isSuccess());
        DBFactory.getInstance().closeSession();

        // test the admin
        List<LockEntry> entries = cl.adminOnlyGetLockEntries();
        assertEquals(1, entries.size());
        final LockEntry le = entries.get(0);
        // must be original owner
        assertEquals(le.getOwner().getName(), ident.getName());

        // release lock
        cl.releaseLock(res3);
        DBFactory.getInstance().closeSession();
        // test the admin
        entries = cl.adminOnlyGetLockEntries();
        assertEquals(0, entries.size());

        // make sure it is not locked anymore
        final boolean lo = cl.isLocked(ores, "abc");
        assertFalse(lo);

        /*
         * LockResult res3 = cl.releaseLock(lockResult)acquireLock(ores, ident, "abc"); assertTrue(res3.isSuccess()); DBFactory.getInstance().closeSession();
         */

        // final SecurityGroup group2 =
        // ManagerFactory.getManager().createAndPersistSecurityGroup();
        // make sure the lock has been written to the disk (tests for
        // createOrFind see other methods)

        // PLock p1 =
        // PessimisticLockManager.getInstance().findOrPersistPLock("befinsert");
        // assertNotNull(p1);

        // try to enrol all in the same group
        /*
         * for (int i = 0; i < MAX_COUNT + MAX_USERS_MORE; i++) { final int j = i; new Thread(new Runnable(){ public void run() { try {
         * System.out.println("thread started"); Identity id = identities.get(j); // DBFactory.getInstance().beginSingleTransaction(); PLock p2 =
         * ClusterLockManager.getInstance().findOrPersistPLock("befinsert"); assertNotNull(p2); doNoLockingEnrol(id, group2); DBFactory.getInstance().commit();
         * DBFactory.getInstance().closeSession(); } catch (Exception e) { e.printStackTrace(); } }}).start(); } sleep(20000); // now count
         * DBFactory.getInstance().closeSession(); int cnt2 = ManagerFactory.getManager().countIdentitiesOfSecurityGroup(group2); assertTrue(
         * "cnt should be smaller or eq than allowed since synced with select for update. cnt:" +cnt2+", max "+MAX_COUNT, cnt2 <= MAX_COUNT); assertTrue(
         * "cnt should be eq to allowed since synced with select for update. cnt:" +cnt2+", max "+MAX_COUNT, cnt2 == MAX_COUNT); System.out.println("cnt lock "+cnt2);
         */
    }

    @Test
    public void testSaveEvent() {
        final BaseSecurity baseSecurityManager = applicationContext.getBean(BaseSecurity.class);
        final Identity identity = baseSecurityManager.createAndPersistIdentity("testSaveEvent", null, null, null, null);
        DBFactory.getInstance().closeSession();
        System.out.println("Created identity=" + identity);
        //
        final TestTable entry = new TestTable();
        entry.setField1("bar");
        entry.setField2(2221234354566776L);
        try {
            DBFactory.getInstance().saveObject(entry);
            DBFactory.getInstance().commit();
            fail("Should generate an error");
        } catch (final DBRuntimeException dre) {
            System.out.println("DB connection is in error-state");
        }
        // DB transaction must be in error state for this test
        try {

            final ClusterLocker locker = (ClusterLocker) applicationContext.getBean(ClusterCoordinator.class).getLocker();
            System.out.println("ClusterLocker created");
            final Event event = new SignOnOffEvent(identity, false);
            System.out.println("START locker.event(event)");
            locker.event(event);
            System.out.println("DONE locker.event(event)");
        } catch (final Exception ex) {
            System.err.println(ex);
            fail("BLOCKER : ClusterLocker.event is not error-safe, db exception could happen and de-register event-listener");
        }
    }

    private void sleep(final int i) {
        try {
            Thread.sleep(i);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
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
