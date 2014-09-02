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

package org.olat.data.user;

import org.olat.data.basesecurity.Identity;

/**
 * <h3>Description:</h3> The user manager provides methods to handle user objects. This includes some search methods and methods for the users property handling.
 * <p>
 * Most search methods are not implemented on the user manager but rather on the security manager from the base security package. See ManagerFactory.getManager()
 * <p>
 * Initial Date: Jun 23, 2004 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com, Christian Guretzki
 */
public interface UserDao {

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @return a new non-persisted User.
     */
    public User createUser(final String firstName, final String lastName, final String eMail);

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @return a persistant User.
     */
    public User createAndPersistUser(String firstName, String lastName, String email);

    /**
     * Find the identity (and the user) that match the given email address. The match is an exact match
     * 
     * @param email
     *            The email search parameter
     * @return The identity found for this email or null if not found
     */
    public Identity findIdentityByEmail(String email);

    /**
     * Find user by its email
     * 
     * @param email
     *            that has to be searched
     * @return User if the user has been found or null if not found
     * @deprecated use findIdentityByEmail() instead
     */
    @Deprecated
    public User findUserByEmail(String email);

    /**
     * Check if a user already used the e-mail address
     * 
     * @param email
     * @return
     */
    public boolean userExist(String email);

    /**
     * Find user by its key (database primary key)
     * 
     * @param key
     *            the primary key
     * @return User if the user has been found or null if not found
     */
    public User loadUserByKey(Long key);

    /**
     * Updates a user in the database.
     * 
     * @param usr
     *            The user object to be updated
     * @return The true if successfully updated
     */
    public void updateUser(User usr);

    /**
     * @param user
     *            The user to be saved
     */
    public void saveUser(User user);

    /**
     * Updates the user object for a given identity
     * 
     * @param identity
     * @return true if successful.
     */
    public boolean updateUserFromIdentity(Identity identity);

}
