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

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.owasp.esapi.EncoderConstants;
import org.owasp.esapi.errors.AuthenticationException;
import org.owasp.esapi.reference.DefaultUser;
import org.owasp.esapi.reference.FileBasedAuthenticator;

/**
 * This uses ESAPI <code> FileBasedAuthenticator </code> for password verification.
 * 
 * Initial Date: 29.04.2014 <br>
 * 
 * @author lavinia
 */
public class AuthenticatorHelper {

    private static final Logger LOG = LoggerHelper.getLogger();
    protected static final char[] CHAR_SPECIALS = { '§', '°', '+', '¦', '"', '@', '*', '#', 'ç', '%', '&', '¬', '/', '|', '(', '¢', ')', '=', '?', '\'', '^', '~', '€',
            '[', ']', '!', '{', '}', '$', '£', '<', '>', '\\', ',', ';', '.', ':', '-', '_' };

    static {
        Arrays.sort(CHAR_SPECIALS);
    }

    /**
     * The newPassword must not be empty or null, principalName must not be empty or null, oldPassword could be empty or null. <br>
     * The newPassword must be at least 8 chars length, and must contain at least one char from each char set (uppercase, lowercase, digit and specials), <br>
     * and uses <code>FileBasedAuthenticator</code> to <code>verifyPasswordStrength</code>.
     */
    public static boolean verifyPasswordStrength(String oldPassword, String newPassword, String principalName) {
        if (newPassword == null || newPassword.isEmpty()) {
            LOG.error("verifyPasswordStrength failed because newPassword or principalName is null or empty");
            return false;
        } else if (newPassword != null && newPassword.trim().length() < 8) {
            LOG.error("verifyPasswordStrength failed because newPassword is too short, should be at least 8 chars long");
            return false;
        } else if (newPassword != null && !verifyCharSets(newPassword)) {
            LOG.error("verifyPasswordStrength failed because newPassword doesn't have a char from each char set");
            return false;
        }
        if (principalName == null) {
            principalName = "";
        }
        try {
            FileBasedAuthenticator.getInstance().verifyPasswordStrength(oldPassword, newPassword, getESAPIUser(principalName));
        } catch (AuthenticationException e) {
            // e.g. if newPassword is too similar to oldPassword
            LOG.error("verifyPasswordStrength failed because: " + e.getUserMessage() + " " + e.getLogMessage());
            return false;
        }
        return true;
    }

    private static DefaultUser getESAPIUser(String username) {
        return new DefaultUser(username);
    }

    /**
     * It must contain at least one char from each char set: lowercase, uppercase, digits and specials. <br>
     * Is protected only to be tested.
     */
    protected static boolean verifyCharSets(String newPassword) {
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < newPassword.length(); i++) {
            if (!hasLowercase && Arrays.binarySearch(EncoderConstants.CHAR_LOWERS, newPassword.charAt(i)) >= 0) {
                hasLowercase = true;
            } else if (!hasUppercase && Arrays.binarySearch(EncoderConstants.CHAR_UPPERS, newPassword.charAt(i)) >= 0) {
                hasUppercase = true;
            } else if (!hasDigit && Arrays.binarySearch(EncoderConstants.CHAR_DIGITS, newPassword.charAt(i)) >= 0) {
                hasDigit = true;
            } else if (!hasSpecial && Arrays.binarySearch(CHAR_SPECIALS, newPassword.charAt(i)) >= 0) {
                hasSpecial = true;
            }
        }
        return hasLowercase && hasUppercase && hasDigit && hasSpecial;
    }

}
