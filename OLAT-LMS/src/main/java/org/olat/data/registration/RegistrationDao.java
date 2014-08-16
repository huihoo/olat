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
package org.olat.data.registration;

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * TODO: Class Description for RegistrationDao
 * 
 * <P>
 * Initial Date: 11.07.2011 <br>
 * 
 * @author guretzki
 */
public interface RegistrationDao {

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
     * deletes a TemporaryKey
     * 
     * @param key
     *            the temporary key to be deleted
     * @return true if successfully deleted
     */
    public abstract void deleteTemporaryKey(final TemporaryKeyImpl key);

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
     * Get a list of all users that did already confirm the disclaimer
     * 
     * @return
     */
    public abstract List<Identity> getIdentitiesWithConfirmedDisclaimer();

}
