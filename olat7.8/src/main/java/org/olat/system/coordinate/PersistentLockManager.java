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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.system.coordinate;

import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * not to be used directly, only for spring implementations. use coordinator.getLocker() to use volatile and persistent locks.
 * <P>
 * 
 * @author Patrick Brunner
 */
public interface PersistentLockManager {

    /**
     * acquires a persistent lock
     * 
     * @param ores
     * @param ident
     * @param locksubkey
     *            may not be longer than 30 chars
     * @return
     */
    public LockResult aquirePersistentLock(OLATResourceable ores, OLATPrincipal principal, String locksubkey);

    /**
     * releases a persistent lock
     * 
     * @param lockResult
     */
    public void releasePersistentLock(LockResult lockResult);

    /**
     * Delete all locks for this principal.
     * 
     * @param principal
     */
    public void releaseAllLocksForPrincipal(OLATPrincipal principal);

}
