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

import java.util.Locale;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.security.authentication.ldap.LDAPError;

/**
 * AuthenticationService
 * 
 * <P>
 * Initial Date: 08.04.2011 <br>
 * 
 * @author guido
 */
public interface AuthenticationService {

    public enum Provider {
        OLAT, LDAP, WEBDAV
    };

    /**
     * 
     * @param login
     * @param pass
     * @return null or the Identity if login was successful
     */
    public Identity authenticate(final String login, final String pass, Provider provider);

    public boolean authenticate(final String pass, final Authentication auth);

    /**
     * 
     * @param identity
     * @param provider
     * @return
     */
    public Authentication findAuthentication(Identity identity, Provider provider);

    /**
     * 
     * @param changingIdentity
     * @param identityToBeChanged
     * @param newPwd
     * @return
     */
    public boolean changePassword(final Identity changingIdentity, Identity identityToBeChanged, final String newPwd);

    /*
     * STATIC_METHOD_REFACTORING moved from LDAPAuthenticationController - added new interface - think about using authenticate method above - DRY clean code principle
     */
    /**
     * 
     * @param username
     * @param pwd
     * @param ldapError
     * @return Identity object
     */
    public Identity authenticate(final String username, final String pwd, final LDAPError ldapError);

    /**
     * This checks that the Identity has an OLAT authentication but the password is too weak/old/no new credential.
     */
    public boolean isPasswordTooOld(Identity identity, String authProvider);

    /**
     * @return Date representation depending on locale.
     */
    public String getChangePasswordDeadlineDate(Locale locale);

    /**
     * @return number of days remained until deadline.
     */
    public int getDaysToChangePasswordDeadline();

    /**
     * Authentication is valid if it has a new credential, or if it doesn't have a new credential if the deadline is not passed.
     */
    public boolean isAuthenticationValid(Authentication auth);

}
