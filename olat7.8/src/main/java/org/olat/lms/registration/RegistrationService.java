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
package org.olat.lms.registration;

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKey;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.User;

/**
 * TODO: Class Description for RegistrationService
 * 
 * <P>
 * Initial Date: 11.07.2011 <br>
 * 
 * @author guretzki
 */
public interface RegistrationService {

    public static final String EMAIL_CHANGE = "EMAIL_CHANGE";
    public static final String PW_CHANGE = "PW_CHANGE";

    /**
     * creates a new user and identity with the data of the temporary key (email) and other supplied user data (within myUser)
     * 
     * @param login
     *            Login name
     * @param pwd
     *            Password
     * @param myUser
     *            Not yet persisted user object
     * @param tk
     *            Temporary key
     * @return the newly created subject or null
     */
    public abstract Identity createNewUserAndIdentityFromTemporaryKey(final String login, final String pwd, final User myUser, final TemporaryKeyImpl tk);

    /**
     * A temporary key is created
     * 
     * @param email
     *            address of new user
     * @param ip
     *            address of new user
     * @param action
     *            REGISTRATION or PWCHANGE
     * @return TemporaryKey
     */
    public abstract TemporaryKeyImpl createTemporaryKeyByEmail(final String email, final String ip, final String action);

    /**
     * returns an existing TemporaryKey by a given email address or null if none found
     * 
     * @param email
     * @return the found temporary key or null if none is found
     */
    public abstract TemporaryKeyImpl loadTemporaryKeyByEmail(final String email);

    /**
     * returns an existing list of TemporaryKey by a given action or null if none found
     * 
     * @param action
     * @return the found temporary key or null if none is found
     */
    public abstract List<TemporaryKey> loadTemporaryKeyByAction(final String action);

    /**
     * Looks for a TemporaryKey by a given registrationkey
     * 
     * @param regkey
     *            the encrypted registrationkey
     * @return the found TemporaryKey or null if none is found
     */
    public abstract TemporaryKeyImpl loadTemporaryKeyByRegistrationKey(final String regkey);

    /**
     * Creates a TemporaryKey and saves it permanently
     * 
     * @param emailaddress
     * @param ipaddress
     * @param action
     *            REGISTRATION or PWCHANGE
     * @return newly created temporary key
     */
    public abstract TemporaryKeyImpl register(final String emailaddress, final String ipaddress, final String action);

    /**
     * Delete a temporary key.
     * 
     * @param keyValue
     */
    public abstract void deleteTemporaryKeyWithId(final String keyValue);

    /**
     * Evaluates whether the given identity needs to accept a disclaimer before logging in or not.
     * 
     * @param identity
     * @return true: user must accept the disclaimer; false: user did already accept or must not accept a disclaimer
     */
    public abstract boolean needsToConfirmDisclaimer(final Identity identity);

    /**
     * Marks the given identity to have confirmed the disclaimer. Note that this method does not check if the disclaimer does already exist, do this by calling
     * needsToConfirmDisclaimer() first!
     * 
     * @param identity
     */
    public abstract void setHasConfirmedDislaimer(final Identity identity);

    /**
     * Remove all disclaimer confirmations. This means that every user on the system must accept the disclaimer again.
     */
    public abstract void revokeAllconfirmedDisclaimers();

    /**
     * Remove the disclaimer confirmation for the specified identity. This means that this user must accept the disclaimer again.
     * 
     * @param identity
     */
    public abstract void revokeConfirmedDisclaimer(final Identity identity);

    /**
     * Get a list of all users that did already confirm the disclaimer
     * 
     * @return
     */
    public abstract List<Identity> getIdentitiesWithConfirmedDisclaimer();

    String getChangePasswordLink(final String emailAdress, String localeString, final String ip);

    String getRegistrationLink(final String emailAdress, String localeString, final String ip);

    String getChangeEmailLink(final Locale locale, TemporaryKey tk);

    TemporaryKey loadOrCreateChangeEmailTemporaryKey(final String currentEmail, final String changedEmail, final String ip);

}
