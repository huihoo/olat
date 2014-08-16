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
package org.olat.lms.coordinate;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockEntry;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.Locker;
import org.olat.system.coordinate.PersistentLockManager;
import org.olat.system.security.OLATPrincipal;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for LockingServiceImpl
 * 
 * <P>
 * Initial Date: 06.07.2011 <br>
 * 
 * @author lavinia
 */
@Service
public class LockingServiceImpl implements LockingService {

    private Locker locker;

    /**
     * Spring
     */
    private LockingServiceImpl() {
        locker = CoordinatorManager.getInstance().getCoordinator().getLocker();
    }

    @Override
    public LockResult acquireLock(OLATResourceable ores, OLATPrincipal principal, String locksubkey) {
        return locker.acquireLock(ores, principal, locksubkey);
    }

    @Override
    public void releaseLock(LockResult le) {
        locker.releaseLock(le);

    }

    @Override
    public boolean isLocked(OLATResourceable ores, String locksubkey) {
        return locker.isLocked(ores, locksubkey);
    }

    @Override
    public LockResult aquirePersistentLock(OLATResourceable ores, OLATPrincipal principal, String locksubkey) {
        return locker.aquirePersistentLock(ores, principal, locksubkey);
    }

    @Override
    public void releasePersistentLock(LockResult le) {
        locker.releasePersistentLock(le);
    }

    @Override
    public List<LockEntry> adminOnlyGetLockEntries() {
        return locker.adminOnlyGetLockEntries();
    }

    @Override
    public void releaseLockEntry(LockEntry lock) {
        locker.releaseLockEntry(lock);

    }

    @Override
    public void deleteUserData(Identity identity, String newDeletedUserName) {
        locker.releaseAllLocksForPrincipal(identity);

    }

    @Override
    public void releaseAllLocksForPrincipal(OLATPrincipal principal) {
        locker.releaseAllLocksForPrincipal(principal);
    }

    @Override
    @Deprecated
    public PersistentLockManager getPersistentLockManager() {
        throw new UnsupportedOperationException();
    }

}
