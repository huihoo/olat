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
package org.olat.data.coordinate;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DB;
import org.olat.data.coordinate.lock.PessimisticLockDao;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.Syncer;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.coordinate.util.DerivedStringSyncer;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 * cluster mode implementation of the Syncer
 * <P>
 * Initial Date: 21.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterSyncer implements Syncer {
    private static final Logger log = LoggerHelper.getLogger();

    private int executionTimeThreshold = 3000; // warn if the execution takes
                                               // longer than three seconds
    private final ThreadLocal<ThreadLocalClusterSyncer> data = new ThreadLocal<ThreadLocalClusterSyncer>();
    private PessimisticLockDao pessimisticLockManager;
    private DB dbInstance;

    /**
     * [used by spring]
     * 
     * @param pessimisticLockManager
     */
    private ClusterSyncer(final PessimisticLockDao pessimisticLockManager) {
        this.setPessimisticLockManager(pessimisticLockManager);
    }

    public void setDbInstance(final DB db) {
        dbInstance = db;
    }

    /**
     * org.olat.system.coordinate.SyncerCallback)
     */
    @Override
    public <T> T doInSync(final OLATResourceable ores, final SyncerCallback<T> callback) {
        Codepoint.hierarchicalCodepoint(ClusterSyncer.class, "doInSync-before-sync", 3);
        getData().setSyncObject(ores);// Store ores-object for
                                      // assertAlreadyDoInSyncFor(ores)
        final String asset = OresHelper.createStringRepresenting(ores);

        // 1. sync on vm (performance and net bandwith reason, and also for a
        // fair per-node handling of db request)
        // cluster:::: measure throughput with/without this sync
        // : maybe also measure if with a n-Semaphore (at most n concurrent
        // accesses) throughput incs or decs
        long start = 0;
        final boolean isDebug = log.isDebugEnabled();
        if (isDebug) {
            start = System.currentTimeMillis();
        }

        T res;
        final Object syncObj = DerivedStringSyncer.getInstance().getSynchLockFor(ores);
        synchronized (syncObj) {// cluster_ok is per vm only. this synchronized
                                // is needed for multi-core processors to handle
                                // memory-flushing from registers correctly.
                                // without this synchronized you could have
                                // different
                                // states of (instance-/static-)fields in
                                // different cores
            getData().incrementAndCheckNestedLevelCounter();

            // 2. sync on cluster
            // acquire a db lock with select for update which blocks other db
            // select for updates on the same record
            // until the transaction is committed or rollbacked
            try {
                pessimisticLockManager.findOrPersistPLock(asset);
                Codepoint.hierarchicalCodepoint(ClusterSyncer.class, "doInSync-in-sync", 3);

                // now execute the task, which may or may not contain further db
                // queries.
                res = callback.execute();
            } finally {
                getData().decrementNestedLevelCounter();
            }

            // clear the thread local
            if (getData().getNestedLevel() == 0) {
                data.remove();
            }

            // we used to not do a commit here but delay that to the end of the
            // dispatching-process. the comment
            // was: "the lock will be released after calling commit at the end
            // of dispatching-process
            // needed postcondition after the servlet has finished the request:
            // a commit or rollback on the db to release the lock.
            // otherwise the database will throw a "lock wait timeout exceeded"
            // message after some time and thus release the lock."
            // but realizing that this can a) cause long locking phases and b)
            // deadlocks between VMs
            // we decided to do a commit here and work with its consequence
            // which is that everything that happened
            // prior to the doInSync call is also committed. This though
            // corresponds to the OLAT 6.0.x model and
            // was acceptable there as well.
            dbInstance.commit();
        }
        if (isDebug) {
            final long stop = System.currentTimeMillis();
            if (stop - start > executionTimeThreshold) {
                log.warn("execution time exceeded limit of " + executionTimeThreshold + ": " + (stop - start), new AssertException("generate stacktrace"));
            }
        }
        Codepoint.hierarchicalCodepoint(ClusterSyncer.class, "doInSync-after-sync", 3);
        return res;
    }

    /**
     * org.olat.system.coordinate.SyncerExecutor)
     */
    @Override
    public void doInSync(final OLATResourceable ores, final SyncerExecutor executor) {
        // call the other doInSync variant to avoid duplicate code here
        doInSync(ores, new SyncerCallback<Object>() {

            @Override
            public Object execute() {
                executor.execute();
                return null;
            }

        });

    }

    /**
	 */
    @Override
    public void assertAlreadyDoInSyncFor(final OLATResourceable ores) {
        if (!getData().isEquals(ores) || (getData().getNestedLevel() == 0)) {
            throw new AssertException("This method must be called from doInSync block with ores=" + ores);
        }
    }

    /**
     * [used by spring]
     * 
     * @param executionTimeThreshold
     */
    public void setExecutionTimeThreshold(final int executionTimeThreshold) {
        this.executionTimeThreshold = executionTimeThreshold;
    }

    private void setData(final ThreadLocalClusterSyncer data) {
        this.data.set(data);
    }

    private ThreadLocalClusterSyncer getData() {
        ThreadLocalClusterSyncer tld = data.get();
        if (tld == null) {
            tld = new ThreadLocalClusterSyncer();
            setData(tld);
        }
        return tld;
    }

    public void setPessimisticLockManager(final PessimisticLockDao pessimisticLockManager) {
        this.pessimisticLockManager = pessimisticLockManager;
    }

    // ////////////
    // Inner class
    // ////////////
    /**
     * A <b>ThreadLocalClusterSyncer</b> is used as a central place to store data on a per thread basis.
     * 
     * @author Christian Guretzki
     */
    private class ThreadLocalClusterSyncer {
        private int nestedLevelCounter = 0;
        private OLATResourceable ores;

        protected void incrementAndCheckNestedLevelCounter() {
            nestedLevelCounter++;
            if (nestedLevelCounter > 1) {
                nestedLevelCounter--;
                throw new AssertException("ClusterSyncer: nested doInSync is not allowed");
            }
        }

        public int getNestedLevel() {
            return nestedLevelCounter;
        }

        protected void decrementNestedLevelCounter() {
            nestedLevelCounter--;
            if (nestedLevelCounter < 0) {
                throw new AssertException("ClusterSyncer nestedLevelCounter could not be < 0, do not call decrementNestedLevelCounter twice");
            }
        }

        protected void setSyncObject(final OLATResourceable ores) {
            this.ores = ores;
        }

        protected boolean isEquals(final OLATResourceable ores) {
            if (!this.ores.getResourceableTypeName().equals(ores.getResourceableTypeName())) {
                return false;
            }
            if (!this.ores.getResourceableId().equals(ores.getResourceableId())) {
                return false;
            }
            return true;
        }
    }

}
