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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockEntry;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.LockResultImpl;
import org.olat.system.coordinate.PersistentLockManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DBPersistentLockManager
 * <P>
 * Initial Date: 21.06.2006 <br>
 * 
 * @author patrickb
 */
@Repository("persistentLockManager")
public class PersistentLockDao extends BasicManager implements PersistentLockManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CATEGORY_PERSISTENTLOCK = "o_lock";

    @Autowired
    BaseSecurity baseSecurity;
    @Autowired
    DB db;

    private PersistentLockDao() {
        // [spring]
    }

    /**
	 */
    @Override
    public LockResult aquirePersistentLock(final OLATResourceable ores, final OLATPrincipal principal, final String locksubkey) {
        // synchronisation is solved in the LockManager
        LockResult lres;
        final PropertyManager pm = PropertyManager.getInstance();
        final String derivedLockString = OresHelper.createStringRepresenting(ores, locksubkey);
        long aqTime;
        Identity lockOwner;
        boolean success;
        PropertyImpl p;

        p = pm.findProperty(null, null, null, CATEGORY_PERSISTENTLOCK, derivedLockString);
        if (p == null) {
            // no persistent lock acquired yet
            // save a property: cat = o_lock, key = derivedLockString, Longvalue = key
            // of identity acquiring the lock
            final PropertyImpl newp = pm.createPropertyInstance(null, null, null, CATEGORY_PERSISTENTLOCK, derivedLockString, null, ((Identity) principal).getKey(),
                    null, null);
            pm.saveProperty(newp);
            aqTime = System.currentTimeMillis();
            lockOwner = (Identity) principal;
            success = true;
        } else {
            // already acquired, but check on reaquiring
            aqTime = p.getLastModified().getTime();
            final Long lockOwnerKey = p.getLongValue();
            if (((Identity) principal).getKey().equals(lockOwnerKey)) {
                // reaquire ok
                success = true;
            } else {
                // already locked by an other person
                success = false;
            }
            // FIXME:fj:c find a better way to retrieve information about the
            // lock-holder
            lockOwner = baseSecurity.loadIdentityByKey(lockOwnerKey);
        }

        final LockEntry le = new LockEntry(derivedLockString, aqTime, lockOwner);
        lres = new LockResultImpl(success, le);
        return lres;

    }

    /**
	 */
    @Override
    public void releasePersistentLock(final LockResult le) {
        // synchronisation is solved in the LockManager
        final String derivedLockString = ((LockResultImpl) le).getLockEntry().getKey();
        final PropertyManager pm = PropertyManager.getInstance();
        final PropertyImpl p = pm.findProperty(null, null, null, CATEGORY_PERSISTENTLOCK, derivedLockString);
        if (p == null) {
            throw new AssertException("could not release lock: no lock in db, " + derivedLockString);
        }
        final Identity ident = (Identity) le.getOwner();
        final Long ownerKey = p.getLongValue();
        if (!ownerKey.equals(ident.getKey())) {
            throw new AssertException("user " + ident.getName() + " cannot release lock belonging to user with key " + ownerKey + " on resourcestring "
                    + derivedLockString);
        }
        pm.deleteProperty(p);
    }

    /**
     * Delete all persisting-locks for for this principal.
     * 
     * @param identity
     */
    public void releaseAllLocksForPrincipal(OLATPrincipal principal) {
        final String query = "from v in class org.olat.data.properties.PropertyImpl where v.category = ? and v.longValue = ?";
        DBFactory.getInstance().delete(query, new Object[] { CATEGORY_PERSISTENTLOCK, ((Identity) principal).getKey() }, new Type[] { Hibernate.STRING, Hibernate.LONG });
        log.debug("All db-persisting-locks deleted for principal=" + principal);
    }

}
