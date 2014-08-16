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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.olat.data.basesecurity.Identity;
import org.olat.data.coordinate.singlevm.SingleVMLocker;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.coordinate.PersistentLockManager;
import org.olat.system.event.EventBus;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SingleVMLockerTest
 * <P>
 * Initial Date: 12.07.2010 <br>
 * 
 * @author patrickb
 */
public class SingleVMLockerTest {

    private final String TEST_LOCK = "testLock";

    /**
     * see also http://bugs.olat.org/jira/browse/OLAT-5522
     */
    @Test
    public void testLockSeqenceAcquireAcquireRelease() {
        //
        final SingleVMLocker svml = new SingleVMLocker() {

            @Override
            public PersistentLockManager getPersistentLockManager() {
                return null;
            }
        };
        final EventBus eventBus = Mockito.mock(EventBus.class);
        svml.setEventBus(eventBus);

        final OLATResourceable ores = OresHelper.createOLATResourceableInstance("Course", Long.valueOf(1234566235));
        final Identity identity = Mockito.mock(Identity.class);
        Mockito.when(identity.getKey()).thenReturn(Long.valueOf(987654321));
        Mockito.when(identity.getName()).thenReturn("uniquename");

        // course editor open
        final LockResult acquiredLockForCourseEditor = svml.acquireLock(ores, identity, TEST_LOCK);
        assertTrue("Lock acquired", acquiredLockForCourseEditor.isSuccess());
        // course editor does only check if the acquired lock is successfully - no prior check isLocked!
        // hence if the same user acquires the lock again, she can enter the course editor
        assertTrue("ores not locked", svml.isLocked(ores, TEST_LOCK));

        // try to copy
        final LockResult secondAcquiredLockForCopyingCourse = svml.acquireLock(ores, identity, TEST_LOCK);
        assertTrue("Lock acquired", secondAcquiredLockForCopyingCourse.isSuccess());
        // the copy workflow checks if the resource is locked and in this case the
        // warning is shown, although the lock may be successfully re-acquired by the same user
        assertTrue("ores not locked", svml.isLocked(ores, TEST_LOCK));

        // close editor
        svml.releaseLock(acquiredLockForCourseEditor);
        // release lock should remove the lock from the hashmap, as the isLocked function
        // checks only for the presence of a lockentry in the hashmap.
        // implementing the hashMap and equals methods on LockEntry fixed a problem in the case
        // of the SingleVMLocker Implementation. This assertion failed in case of the issue OLAT-5522
        assertFalse("ores still locked", svml.isLocked(ores, TEST_LOCK));

        // try to copy
        final LockResult thirdAcquiredLock = svml.acquireLock(ores, identity, TEST_LOCK);
        assertTrue("Lock acquired", thirdAcquiredLock.isSuccess());
        assertTrue("ores not locked", svml.isLocked(ores, TEST_LOCK));

    }

}
