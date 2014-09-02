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
package org.olat.lms.security.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Initial Date: 14.03.2014 <br>
 * 
 * @author lavinia
 */
public class FailedLoginTest {

    @Test
    public void testIsBlocked_no() {
        FailedLogin failedLogin = new FailedLogin(Integer.valueOf(5), null);
        assertFalse(failedLogin.isLoginBlocked(Long.valueOf(2)));
    }

    @Test
    public void testIsBlocked_yes() {
        FailedLogin failedLogin = new FailedLogin(Integer.valueOf(5), System.currentTimeMillis());
        try {
            Thread.sleep(1000); // sleep 1 mills
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(failedLogin.isLoginBlocked(Long.valueOf(1))); // blocked for one min.
        try {
            Thread.sleep(60000); // sleep 1 min.
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertFalse(failedLogin.isLoginBlocked(Long.valueOf(1)));
    }
}
