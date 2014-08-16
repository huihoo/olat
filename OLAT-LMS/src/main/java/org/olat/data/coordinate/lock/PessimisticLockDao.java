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

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.configuration.Initializable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * implementation for pessimistic locking.<br>
 * Do not use this class directly. please use Syncer or Locker via CoordinatorManager!
 * <P>
 * Initial Date: 25.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Repository("lockManager")
public class PessimisticLockDao extends BasicManager implements Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private final String ASSET_INSERT_LOCK = "SYS_plock_global";
    @Autowired
    DB db;

    /**
     * [used by spring]
     */
    private PessimisticLockDao() {
        //
    }

    @Override
    @PostConstruct
    public void init() {
        // make sure that the resource (= row in our table) to lock the creation
        // of new assets exists
        PLock gLock = findPLock(ASSET_INSERT_LOCK);
        if (gLock == null) {
            // need to create it
            gLock = createPLock(ASSET_INSERT_LOCK);
            savePLock(gLock);
        }
        db.intermediateCommit();
    }

    private PLock findPLock(final String asset) {
        final DBQuery q = db.createQuery("select plock from " + PLockImpl.class.getName() + " as plock where plock.asset = :asset");
        q.setParameter("asset", asset);
        q.setLockMode("plock", LockMode.UPGRADE);
        final List res = q.list();
        if (res.size() == 0) {
            return null;
        } else {
            return (PLock) res.get(0);
        }
    }

    private PLock createPLock(final String asset) {
        return new PLockImpl(asset);
    }

    private void savePLock(final PLock plock) {
        db.saveObject(plock);
    }

    /**
     * do not use this class directly. please use Syncer or Locker via CoordinatorManager!
     * 
     * @param asset
     * @return
     */
    public PLock findOrPersistPLock(final String asset) {
        if (log.isDebugEnabled()) {
            log.debug("findOrPersistPLock START asset=" + asset);
        }
        PLock plock = findPLock(asset);
        if (log.isDebugEnabled()) {
            if (plock == null) {
                log.debug("findOrPersistPLock PLock not found");
            } else {
                log.debug("findOrPersistPLock found and locked PLock: " + plock);
            }
        }
        // if not found, persist it.
        if (plock == null) {
            // synchronize the findOrCreate by using the special row with the
            // global-lock-asset
            // locks the global lock - which is only used to sync creation of
            // new resource entries, so that those can later be locked.
            findPLock(ASSET_INSERT_LOCK);
            if (log.isDebugEnabled()) {
                log.debug("findOrPersistPLock global insert lock locked");
            }
            // need to read again within the protected region
            plock = findPLock(asset);
            if (plock == null) {
                if (log.isDebugEnabled()) {
                    log.debug("findOrPersistPLock creating new plock: " + asset);
                }
                plock = createPLock(asset);
                if (log.isDebugEnabled()) {
                    log.debug("findOrPersistPLock created new plock: " + asset);
                }
                savePLock(plock);
                if (log.isDebugEnabled()) {
                    log.debug("findOrPersistPLock saved new plock: " + asset);
                }
            } // else plock got created by another thread in the meantime

            // some notes:
            // takes advantage of the fact that the select for update blocks a
            // transaction when a lock is already acquired.
            //

            // since we have concurrent access here, we could have many threads
            // which try to create
            // the entry to later lock upon.

            // we therefore could
            // a) lock on a olat-wide lock
            // or b) catch the exception - and continue, since we know that the
            // row already exists
            // even c) start a new connection and set serializable isolation
            // level..

            // in a cluster, each vm syncs via synchronized() first, so that
            // there is only one concurrent access to the PLockManager from one
            // node at a given time.
            // -> we have maximal num-of-cluster concurrent accesses, e.g. 3-5

            // a: performance, should only occur once for a resource: the first
            // time a lock for a certain resource is accessed.
            // b) is the transaction still safe to continue? what about
            // hibernate first level cache etc. hibernate docs says in general
            // we'd need to close the session.

            // -> go for solution a.

        } // else found
        return plock;
    }
}
