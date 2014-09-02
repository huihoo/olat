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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO: Class Description for LockingServiceITCase
 * 
 * <P>
 * Initial Date: 11.07.2011 <br>
 * 
 * @author lavinia
 */
public class LockingServiceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    LockingService lockingService;
    @Autowired
    BaseSecurity baseSecurity;

    private static OLATResourceable ores_1;
    private static OLATResourceable ores_2;
    private static OLATResourceable ores_3;
    private static OLATPrincipal principal_1;
    private static OLATPrincipal principal_2;

    private static boolean initialized;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        if (!initialized) {
            ores_1 = OresHelper.createOLATResourceableInstanceWithoutCheck(LockingServiceITCase.class.getName(), new Long(111));
            ores_2 = OresHelper.createOLATResourceableInstanceWithoutCheck(LockingServiceITCase.class.getName(), new Long(112));
            ores_3 = OresHelper.createOLATResourceableInstanceWithoutCheck(LockingServiceITCase.class.getName(), new Long(113));

            principal_1 = baseSecurity.createAndPersistIdentity("user_freddy_LockingService", null, AUTHENTICATION_PROVIDER_OLAT, "user_freddy_LockingService", "test1");
            principal_2 = baseSecurity.createAndPersistIdentity("user_ana_LockingService", null, AUTHENTICATION_PROVIDER_OLAT, "user_ana_LockingService", "test1");
            initialized = true;
        }
    }

    @Test
    public void acquireAndReleaseLock() {
        // uses ores_1

        LockResult lockResult = lockingService.acquireLock(ores_1, principal_1, "blaBla");
        assertTrue(lockResult.isSuccess());
        assertTrue(lockResult.getOwner().equals(principal_1));

        LockResult lockResultReentrant = lockingService.acquireLock(ores_1, principal_1, "blaBla");
        assertTrue(lockResultReentrant.isSuccess());
        assertTrue(lockResultReentrant.getOwner().equals(principal_1));

        LockResult lockResult_2 = lockingService.acquireLock(ores_1, principal_2, "blaBla");
        assertFalse(lockResult_2.isSuccess());

        lockingService.releaseLock(lockResult);
        lockResult_2 = lockingService.acquireLock(ores_1, principal_2, "blaBla");
        assertTrue(lockResult_2.isSuccess());
    }

    @Ignore
    @Test
    public void releaseAllLocksForPrincipal() {
        // uses ores_2

        LockResult lockResultTemp = lockingService.acquireLock(ores_2, principal_1, "blaBla");
        assertTrue(lockResultTemp.isSuccess());
        assertTrue(lockResultTemp.getOwner().equals(principal_1));
        assertTrue(lockingService.isLocked(ores_2, "blaBla"));

        LockResult lockResultPersistent = lockingService.aquirePersistentLock(ores_2, principal_1, "cucu");
        assertTrue(lockResultPersistent.isSuccess());
        assertTrue(lockResultPersistent.getOwner().equals(principal_1));

        lockingService.releaseAllLocksForPrincipal(principal_1);
        assertFalse(lockingService.isLocked(ores_2, "blaBla"));
        DBFactory.getInstance().closeSession();

        // proof that the persistent lock was released for ores_2, and that another principal is able to acquire it
        LockResult lockResultPersistent2 = lockingService.aquirePersistentLock(ores_2, principal_2, "cucu");
        assertTrue(lockResultPersistent2.isSuccess());
        assertTrue(lockResultPersistent2.getOwner().equals(principal_2));
    }

    @Ignore
    @Test
    public void acquireAndReleasePersistentLock() {
        LockResult lockResultPersistent = lockingService.aquirePersistentLock(ores_3, principal_1, "bau");
        assertTrue(lockResultPersistent.isSuccess());
        assertTrue(lockResultPersistent.getOwner().equals(principal_1));

        LockResult lockResultPersistent2 = lockingService.aquirePersistentLock(ores_3, principal_2, "bau");
        assertFalse(lockResultPersistent2.isSuccess());

        lockingService.releasePersistentLock(lockResultPersistent);

        LockResult lockResultPersistent3 = lockingService.aquirePersistentLock(ores_3, principal_2, "bau");
        assertTrue(lockResultPersistent3.isSuccess());
    }

    @After
    public void tearDown() throws Exception {
        try {
            // TODO: delete principals

            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("tearDown failed: ", e);
        }
    }

}
