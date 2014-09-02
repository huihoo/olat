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
package org.olat.lms.user;

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.propertyhandler.UserPropertiesConfig;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.lms.user.syntaxchecker.UserNameAndPasswordSyntaxChecker;

/**
 * TODO: Class Description for UserService2
 * 
 * <P>
 * Initial Date: 16.06.2011 <br>
 * 
 * @author guretzki
 */
public interface UserService {

    /**
     * @see org.olat.lms.user.UserService#createUser(java.lang.String, java.lang.String, java.lang.String)
     */
    public abstract User createUser(final String firstName, final String lastName, final String eMail);

    /**
     * @see org.olat.lms.user.UserService#createAndPersistUser(java.lang.String, java.lang.String, java.lang.String)
     */
    public abstract User createAndPersistUser(final String firstName, final String lastName, final String email);

    /**
     * @see org.olat.lms.user.UserService#findIdentityByEmail(java.lang.String)
     */
    public abstract Identity findIdentityByEmail(final String email);

    /**
     * @see org.olat.lms.user.UserService#findUserByEmail(java.lang.String)
     */
    public abstract User findUserByEmail(final String email);

    /**
     * @see org.olat.lms.user.UserService#userExist(java.lang.String)
     */
    public abstract boolean userExist(final String email);

    /**
     * @see org.olat.lms.user.UserService#loadUserByKey(java.lang.Long)
     */
    public abstract User loadUserByKey(final Long key);

    /**
     * @see org.olat.lms.user.UserService#updateUser(org.olat.data.user.User)
     */
    public abstract void updateUser(final User usr);

    /**
     * @see org.olat.lms.user.UserService#saveUser(org.olat.data.user.User)
     */
    public abstract void saveUser(final User user);

    /**
     * @see org.olat.lms.user.UserService#updateUserFromIdentity(org.olat.data.basesecurity.Identity)
     */
    public abstract boolean updateUserFromIdentity(final Identity identity);

    /**
     * @see org.olat.lms.user.UserService#setUserCharset(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    public abstract void setUserCharset(final Identity identity, final String charset);

    /**
     * @see org.olat.lms.user.UserService#getUserCharset(org.olat.data.basesecurity.Identity)
     */
    public abstract String getUserCharset(final Identity identity);

    /**
     * @see org.olat.lms.user.UserService#deleteUserProperties(org.olat.data.user.User)
     */
    public abstract void deleteUserProperties(User user);

    // /////////////

    /**
     * The newPassword must not be empty or null, principalName must not be empty or null, oldPassword could be empty or null.
     */
    public boolean verifyPasswordStrength(String oldPassword, String newPassword, String principalName);

    /**
     * Check if the login matches.
     * 
     * @param login
     * @return True if synatx is ok.
     */
    public abstract boolean syntaxCheckOlatLogin(final String login);

    // only package scope, used by user impl
    public abstract UserPropertiesConfig getUserPropertiesConfig();

    public abstract List<UserPropertyHandler> getUserPropertyHandlersFor(final String usageIdentifyer, final boolean isAdministrativeUser);

    /**
     * added to be usable by user-bulkChange
     * 
     * @return
     */
    public abstract List<UserPropertyHandler> getAllUserPropertyHandlers();

    public abstract boolean isMandatoryUserProperty(final String usageIdentifyer, final UserPropertyHandler propertyHandler);

    public abstract boolean isUserViewReadOnly(final String usageIdentifyer, final UserPropertyHandler propertyHandler);

    /**
     * Spring setter
     * 
     * @param userNameAndPasswordSyntaxChecker
     */
    public abstract void setUserNameAndPasswordSyntaxChecker(final UserNameAndPasswordSyntaxChecker userNameAndPasswordSyntaxChecker);

    /**
     * Spring setter
     * 
     * @param userPropertiesConfig
     */
    public abstract void setUserPropertiesConfig(final UserPropertiesConfig userPropertiesConfig);

    /**
     * Get a user property value for the given property identifyer. The local might be used to format the returned value if it is an internationalized value
     * 
     * @param propertyName
     *            The user property identifyer
     * @param locale
     *            The locale used for proper display or NULL if the default locale should be used. In many cases it is ok to use NULL in any case, e.g. the users
     *            firstname will not be internationalized in anyway. Make sure you use a locale whenever you query for a date property.
     * @return The value or NULL if no value is set
     */
    public abstract String getUserProperty(User user, String propertyName, Locale locale);

    /**
     * @param lastname
     * @param propertyName
     * @return
     */
    public String getUserProperty(User user, String propertyName);

    /**
     * Returns first and lastname of a user with a space between.
     * 
     * @param user
     * @return One string with firstname + space + lastname
     */
    public abstract String getFirstAndLastname(User user);

    /**
     * @return user identifier of institute he/she belongs to (one of property {@link UserConstants.INSTITUTIONALUSERIDENTIFIER},
     *         {@link UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER}, {@link UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER}).
     */
    public abstract String getInstitutionalIdentifier(User user);

    /**
     * Set the value for the given user property identifyer
     * 
     * @param propertyName
     *            The user property identifyer
     * @param propertyValue
     *            The new value or NULL if no value is used
     */
    public abstract void setUserProperty(final User user, final String propertyName, final String propertyValue);

    /**
     * returns the property value, which is looked up first in the db stored user properties and if not available there, if it can be found in the volatile identity
     * attributes which get set once per session during the login process.
     * <p>
     * Usage so far is during Shibboleth Login (ShibbolethDispatcher), where the shibboleth attributes are extracted and set in the identity environment.
     * 
     * @since introducing user tracking it was needed to expose the volatile identity environments attribute also in the user for the UserActivityLogger(Impl).
     * @param next
     * @param locale
     * @return
     */
    public String getPropertyOrIdentityEnvAttribute(final User user, final String propertyName, final Locale locale);
}
