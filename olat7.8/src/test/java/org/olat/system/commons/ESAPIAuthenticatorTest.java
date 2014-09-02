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

import static org.junit.Assert.fail;

import org.junit.Test;
import org.owasp.esapi.User;
import org.owasp.esapi.errors.AuthenticationException;
import org.owasp.esapi.reference.DefaultUser;
import org.owasp.esapi.reference.FileBasedAuthenticator;

/**
 * Tests if the org.owasp.esapi.reference.FileBasedAuthenticator.verifyPasswordStrength is OK for OLAT.
 * 
 * Initial Date: 06.03.2014 <br>
 * 
 * @author lavinia
 */
public class ESAPIAuthenticatorTest {

    @Test
    public void testVerifyPasswordStrength_OK() throws AuthenticationException {
        FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "aBc_123_X", getESAPIUser("test2"));
    }

    @Test
    public void testVerifyPasswordStrength_OK_newUser() throws AuthenticationException {
        FileBasedAuthenticator.getInstance().verifyPasswordStrength("", "aBc_123_X", getESAPIUser("username"));
    }

    @Test
    public void testVerifyPasswordStrength_OK_onlyLowerAndUpperCase8Chars() throws AuthenticationException {
        FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "aBcdefgh", getESAPIUser("test2"));
    }

    /**
     * The password strength is length times char sets (6*3=18 > 16)
     */
    @Test
    public void testVerifyPasswordStrength_OK_specialChars() throws AuthenticationException {
        FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "ab.$£1", getESAPIUser("test2"));
    }

    @Test
    public void testVerifyPasswordStrength_NOK_onlyLowerCase() {
        try {
            FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "abcdefghij", getESAPIUser("test2"));
        } catch (AuthenticationException e) {
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerCase: " + e.getUserMessage());
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerCase: " + e.getLogMessage());
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_onlyLowerAndUpperCase7Chars_tooShort() {
        try {
            FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "aBcdefg", getESAPIUser("test2"));
        } catch (AuthenticationException e) {
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerAndUpperCase7Chars_tooShort: " + e.getUserMessage());
            System.out.println("testVerifyPasswordStrength_NOK_onlyLowerAndUpperCase7Chars_tooShort: " + e.getLogMessage());
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_tooSimilarWithUsername() {
        try {
            FileBasedAuthenticator.getInstance().verifyPasswordStrength("abc", "teST2134", getESAPIUser("test2134"));
        } catch (AuthenticationException e) {
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithUsername: " + e.getUserMessage());
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithUsername: " + e.getLogMessage());
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword() throws AuthenticationException {
        try {
            FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "12tesT21", getESAPIUser("theusername"));
        } catch (AuthenticationException e) {
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword: " + e.getUserMessage());
            System.out.println("testVerifyPasswordStrength_NOK_tooSimilarWithOldPassword: " + e.getLogMessage());
            return;
        }
        fail();
    }

    @Test
    public void testVerifyPasswordStrength_OK_butNoUserAvailable() throws AuthenticationException {
        FileBasedAuthenticator.getInstance().verifyPasswordStrength("test2", "aBcdefgh", getESAPIUser(""));
    }

    private User getESAPIUser(String username) {
        return new DefaultUser(username);
    }
}
