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
package org.olat.data.coordinate.singlevm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockEntry;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.LockResultImpl;
import org.olat.system.coordinate.Locker;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.SignOnOffEvent;
import org.olat.system.event.SignOnOffEventResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * implementation of the olat system bus within one vm
 * <P>
 * Initial Date: 19.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
// Must be abstract because Spring configuration of method 'getPersistentLockManager' :
// to avoid circular reference method lookup is used for dependecy injection of persistent lock manager
public abstract class SingleVMLocker implements Locker, GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    // lock for the persistent lock manager
    private final Object PERS_LOCK = new Object();

    private final Map<String, LockEntry> locks = new HashMap<String, LockEntry>(); // key, lockentry

    private EventBus eventBus;

    /**
     * [spring only]
     */
    protected SingleVMLocker() {
        //
    }

    /**
     * [used by spring]
     */
    public void init() {
        eventBus.registerFor(this, null, SignOnOffEventResourceable.getResourceable());
    }

    /**
     * [used by spring]
     * 
     * @param eventBus
     */
    public void setEventBus(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * @param ores
     *            the OLATResourceable to lock upon, e.g a repositoryentry or such
     * @param identity
     *            the identity who tries to acquire the lock
     * @param locksubkey
     *            null or any string to lock finer upon the resource (e.g. "authors", or "write", ...)
     * @return lock result
     */
    @Override
    public LockResult acquireLock(final OLATResourceable ores, final OLATPrincipal principal, final String locksubkey) {
        final String derivedLockString = OresHelper.createStringRepresenting(ores, locksubkey);
        return acquireLock(derivedLockString, principal);
    }

    /**
     * releases the lock. can also be called if the lock was not sucessfully acquired
     * 
     * @param le
     *            the LockResult received when locking
     */
    @Override
    public void releaseLock(final LockResult lockResult) {
        // if the lock has not been acquired, then nothing is to be released -
        // return silently to make cleaning up easier
        if (!lockResult.isSuccess()) {
            return;
        }
        final LockEntry le = ((LockResultImpl) lockResult).getLockEntry();
        releaseLockEntry(le);
    }

    /**
     * @param ores
     * @param locksubkey
     * @return if the olatresourceable with the subkey is already locked by someone (returns true even if locked by "myself")
     */
    @Override
    public boolean isLocked(final OLATResourceable ores, final String locksubkey) {
        final String derivedLockString = OresHelper.createStringRepresenting(ores, locksubkey);
        return isLocked(derivedLockString);
    }

    private boolean isLocked(final Object key) {
        synchronized (locks) { // o_clusterOK by:fj, by definition we are in singleVM mode
            return locks.get(key) != null;
        }
    }

    /**
     * aquires a lock
     * 
     * @param identity
     *            the identity who wishes to obtain the lock
     * @return the lockresult
     */
    private LockResult acquireLock(final String key, final OLATPrincipal principal) {
        LockResult lockResult;
        synchronized (locks) { // o_clusterOK by:fj, by definition we are in singleVM mode
            final LockEntry oldLockEntry = locks.get(key);
            if (oldLockEntry == null || principal.equals(oldLockEntry.getOwner())) {
                // no one has the lock aquired yet - or user reacquired its own
                // lock (e.g. in case of browser crash or such)
                final LockEntry lockEntry = new LockEntry(key, System.currentTimeMillis(), principal);
                locks.put(key, lockEntry);
                lockResult = new LockResultImpl(true, lockEntry);
                if (log.isDebugEnabled()) {
                    String msg;
                    if (oldLockEntry == null) {
                        msg = "identity '" + principal.getName() + "' acquired lock on " + key;
                    } else {
                        msg = "identity '" + principal.getName() + "' re-acquired lock on " + key;
                    }
                    log.info(msg);
                }
            } else {
                // already locked
                lockResult = new LockResultImpl(false, oldLockEntry);
            }
        }
        return lockResult;
    }

    @Override
    public void releaseLockEntry(final LockEntry lockEntry) {
        synchronized (locks) { // o_clusterOK by:fj, by definition we are in singleVM mode
            final boolean removed = locks.values().remove(lockEntry);
            if (removed) {
                log.info("Audit:identity '" + lockEntry.getOwner().getName() + "' released lock on " + lockEntry.getKey());
            } else {
                log.warn("identity '" + lockEntry.getOwner().getName() + "' tried to release lock a non-existing lock on " + lockEntry.getKey());
            }
        }
    }

    /**
     * receives all sign on / sign off events so it can release locks of users which have or are logged off
     * 
     */
    @Override
    public void event(final Event event) {
        final SignOnOffEvent se = (SignOnOffEvent) event;
        if (!se.isSignOn()) { // it is a "logout" event - we are only interested in logout events
            releaseAllLocksFor(se.getIdentityName());
        }
    }

    /**
     * Is synchronized.
     * 
     * @param name
     */
    private void releaseAllLocksFor(final String name) {
        // release all locks hold by the identity that has just logged out.
        synchronized (locks) { // o_clusterOK by:fj, by definition we are in singleVM mode
            for (final Iterator<Entry<String, LockEntry>> iter = locks.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, LockEntry> entry = iter.next();
                final String key = entry.getKey();
                final LockEntry le = entry.getValue();
                final OLATPrincipal owner = le.getOwner();
                if (owner.getName().equals(name)) {
                    iter.remove();
                    log.info("Audit:identity '" + name + "' signed off and thus released lock on " + key);
                }
            }
        }
    }

    /**
	 */
    @Override
    public String toString() {
        return "Lockmanager locks:" + locks.entrySet().toString();
    }

    @Override
    public LockResult aquirePersistentLock(final OLATResourceable ores, final OLATPrincipal principal, final String locksubkey) {
        synchronized (PERS_LOCK) { // o_clusterOK by:fj, by definition we are in singleVM mode
            // delegate
            return getPersistentLockManager().aquirePersistentLock(ores, principal, locksubkey);
        }
    }

    /**
	 */
    @Override
    public void releasePersistentLock(final LockResult lockResult) {
        // if the lock has not been acquired, do nothing
        if (!lockResult.isSuccess()) {
            return;
        }
        synchronized (PERS_LOCK) { // o_clusterOK by:fj, by definition we are in singleVM mode
            // delegate
            getPersistentLockManager().releasePersistentLock(lockResult);
        }
    }

    @Override
    public List<LockEntry> adminOnlyGetLockEntries() {
        return Collections.unmodifiableList(new ArrayList<LockEntry>(locks.values()));
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

    /**
     * Delete any locks hold by this principal.
     * 
     * @param principal
     */
    public void releaseAllLocksForPrincipal(OLATPrincipal principal) {
        try {
            releaseAllLocksFor(principal.getName());
            log.info("Done, released all locks managed by the this for identName=" + principal.getName());
        } catch (Exception e1) {
            log.warn("releaseAllLocksFor failed, for identName=" + principal.getName());
        }
        try {
            this.getPersistentLockManager().releaseAllLocksForPrincipal(principal);
            log.info("Done, released all locks managed by the PersistentLockManager for identName=" + principal.getName());
        } catch (Exception e) {
            log.warn("releaseAllLocksForPrincipal failed, for identName=" + principal.getName());
        }
    }

}
