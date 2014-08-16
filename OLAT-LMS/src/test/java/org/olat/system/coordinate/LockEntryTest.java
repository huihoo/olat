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
package org.olat.system.coordinate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * TODO: patrickb Class Description for LockEntryTest
 * <P>
 * Initial Date: 13.07.2010 <br>
 * 
 * @author patrickb
 */
public class LockEntryTest {

    private LockEntry leOne;
    private LockEntry sameAsLeOne;
    private LockEntry leThree;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        OLATPrincipal olatPrincipal = Mockito.mock(OLATPrincipal.class);

        // the first lock entry instance
        leOne = new LockEntry("1234@subkey", System.currentTimeMillis(), olatPrincipal);

        // a second lock entry instance, which must be equal to the first one.
        sameAsLeOne = new LockEntry("1234@subkey", System.currentTimeMillis(), olatPrincipal);

        // a third lock entry instance, which is not equal to the other ones.
        leThree = new LockEntry("another@subkey", System.currentTimeMillis(), olatPrincipal);

    }

    /**
     * Test method for {@link org.olat.system.coordinate.LockEntry#hashCode()}.
     */
    @Test
    public void testHashCode() {
        assertFalse("Wrong equals implementation, different types are recognized as equals ", leOne.equals(new Integer(1)));
        assertFalse("Wrong equals implementation, different users are recognized as equals ", leOne.equals(leThree));
        assertFalse("Wrong equals implementation, null value is recognized as equals ", leOne.equals(null));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", leOne.equals(leOne));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", leOne.equals(sameAsLeOne));
    }

    /**
     * Test method for {@link org.olat.system.coordinate.LockEntry#equals(java.lang.Object)}.
     */
    @Test
    public void testEqualsObject() {
        assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ", leOne.hashCode() == leOne.hashCode());
        assertFalse("Wrong hashCode implementation, different users have same hash-code", leOne.hashCode() == leThree.hashCode());
        assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ", leOne.hashCode() == sameAsLeOne.hashCode());
    }

}
