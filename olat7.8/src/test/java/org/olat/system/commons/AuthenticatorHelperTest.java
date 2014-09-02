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
package org.olat.system.commons;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.owasp.esapi.errors.AuthenticationException;

/**
 * Tests if the AuthenticatorHelper.verifyPasswordStrength is OK for OLAT. <br>
 * The password strength is length times char sets (6*3=18 > 16)
 * 
 * Initial Date: 06.03.2014 <br>
 * 
 * @author lavinia
 */
public class AuthenticatorHelperTest {

    @Test
    public void testVerifyPasswordStrength_OK() throws AuthenticationException {
        assertTrue(AuthenticatorHelper.verifyPasswordStrength("test2", "aBc_123_X", "test2"));
    }

    @Test
    public void testVerifyPasswordStrength_OK_newUser() throws AuthenticationException {
        assertTrue(AuthenticatorHelper.verifyPasswordStrength("", "aBc_123_X", "username"));
    }

    @Test
    public void testVerifyPasswordStrength_OK_8Chars() throws AuthenticationException {
        assertTrue(AuthenticatorHelper.verifyPasswordStrength("test2", "aBcdef_1", "test2"));
    }

    @Test
    public void testVerifyPasswordStrength_OK_specialChars() throws AuthenticationException {
        for (char special : AuthenticatorHelper.CHAR_SPECIALS) {
            String password = "aB178cd" + special;
            boolean isOK = AuthenticatorHelper.verifyPasswordStrength("test2", password, "test2");
            System.out.println("password: " + password + " is OK");
            if (!isOK) {
                System.out.println("password: " + password + " is NOK");
            }
            assertTrue(isOK);
        }
    }

    @Test
    public void testVerifyPasswordStrength_NOK_onlyLowerCase() {
        boolean isOK = AuthenticatorHelper.verifyPasswordStrength("test2", "abcdefghij", "test2");
        if (!isOK) {
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerCase - oldPassword: " + "test2");
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerCase - newPassword: " + "abcdefghij");
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_only7Chars_tooShort() {
        boolean isOK = AuthenticatorHelper.verifyPasswordStrength("test2", "aB_4efg", "test2");
        if (!isOK) {
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerAndUpperCase7Chars_tooShort: " + "aB_4efg");

            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_passwordMatchesUsernameIrrespectiveOfCase() {
        boolean isOK = AuthenticatorHelper.verifyPasswordStrength("abc", "t_ST2134", "t_st2134");
        if (!isOK) {
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithUsername - username: " + "test2134");
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithUsername - newPassword: " + "teST2134");
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword() throws AuthenticationException {
        boolean isOK = AuthenticatorHelper.verifyPasswordStrength("test2", "_tesT21_", "theusername");
        if (!isOK) {
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword - oldPassword: " + "test2");
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword - newPassword: " + "12tesT21");
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_OK_butNoUserAvailable() throws AuthenticationException {
        assertTrue(AuthenticatorHelper.verifyPasswordStrength("test2", "aBcdef_1", ""));
    }

    @Test
    public void testVerifyCharSets() {
        assertTrue(AuthenticatorHelper.verifyCharSets("_1aB"));
    }

    @Test
    public void testVerifyCharSets_noUppercase() {
        assertFalse(AuthenticatorHelper.verifyCharSets("_1a"));
    }

}
