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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.user;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.data.user.UserDao;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.user.propertyhandler.UserPropertiesConfig;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.lms.user.syntaxchecker.UserNameAndPasswordSyntaxChecker;
import org.olat.system.commons.AuthenticatorHelper;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * @author Christian Guretzki
 */
/* @Service currently define in userContext.xml */
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerHelper.getLogger();

    // used to save user data in the properties table
    static final String CHARSET = "charset";

    @Autowired
    UserDao userDao;

    // injected by spring configuration
    protected UserPropertiesConfig userPropertiesConfig;
    // injected by spring configuration
    UserNameAndPasswordSyntaxChecker userNameAndPasswordSyntaxChecker;
    @Autowired
    PropertyManager propertyManager;

    /**
     * [spring]
     */
    UserServiceImpl() {
    }

    /**
     * @see org.olat.lms.user.UserService#createUser(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public User createUser(final String firstName, final String lastName, final String eMail) {
        User newUser = userDao.createUser(firstName, lastName, eMail);
        Preferences prefs = newUser.getPreferences();

        Locale loc;
        // for junit test case: use German Locale
        if (Settings.isJUnitTest()) {
            loc = Locale.GERMAN;
        } else {
            loc = I18nModule.getDefaultLocale();
        }
        // Locale loc
        prefs.setLanguage(loc.toString());
        prefs.setFontsize("normal");
        prefs.setPresenceMessagesPublic(false);
        prefs.setInformSessionTimeout(false);
        return newUser;
    }

    /**
     * @see org.olat.lms.user.UserService#createAndPersistUser(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public User createAndPersistUser(final String firstName, final String lastName, final String email) {
        return userDao.createAndPersistUser(firstName, lastName, email);
    }

    /**
     * @see org.olat.lms.user.UserService#findIdentityByEmail(java.lang.String)
     */
    @Override
    public Identity findIdentityByEmail(final String email) {
        return userDao.findIdentityByEmail(email);
    }

    /**
     * @see org.olat.lms.user.UserService#findUserByEmail(java.lang.String)
     */
    @Override
    public User findUserByEmail(final String email) {
        return userDao.findUserByEmail(email);
    }

    /**
     * @see org.olat.lms.user.UserService#userExist(java.lang.String)
     */
    @Override
    public boolean userExist(final String email) {
        return userDao.userExist(email);
    }

    /**
     * @see org.olat.lms.user.UserService#loadUserByKey(java.lang.Long)
     */
    @Override
    public User loadUserByKey(final Long key) {
        return userDao.loadUserByKey(key);
    }

    /**
     * @see org.olat.lms.user.UserService#updateUser(org.olat.data.user.User)
     */
    @Override
    public void updateUser(final User usr) {
        userDao.updateUser(usr);
    }

    /**
     * @see org.olat.lms.user.UserService#saveUser(org.olat.data.user.User)
     */
    @Override
    public void saveUser(final User user) {
        userDao.saveUser(user);
    }

    /**
     * @see org.olat.lms.user.UserService#updateUserFromIdentity(org.olat.data.basesecurity.Identity)
     */
    @Override
    public boolean updateUserFromIdentity(final Identity identity) {
        return userDao.updateUserFromIdentity(identity);
    }

    /**
     * @see org.olat.lms.user.UserService#setUserCharset(org.olat.data.basesecurity.Identity, java.lang.String)
     */
    @Override
    public void setUserCharset(final Identity identity, final String charset) {
        final PropertyImpl p = propertyManager.findProperty(identity, null, null, null, CHARSET);

        if (p != null) {
            p.setStringValue(charset);
            propertyManager.updateProperty(p);
        } else {
            final PropertyImpl newP = propertyManager.createUserPropertyInstance(identity, null, CHARSET, null, null, charset, null);
            propertyManager.saveProperty(newP);
        }
    }

    /**
     * @see org.olat.lms.user.UserService#getUserCharset(org.olat.data.basesecurity.Identity)
     */
    @Override
    public String getUserCharset(final Identity identity) {
        String charset;
        charset = WebappHelper.getDefaultCharset();
        final PropertyImpl p = propertyManager.findProperty(identity, null, null, null, CHARSET);
        if (p != null) {
            charset = p.getStringValue();
            // if after migration the system does not support the charset choosen by a
            // user
            // (a rather rare case)
            if (!Charset.isSupported(charset)) {
                charset = WebappHelper.getDefaultCharset();
            }
        } else {
            charset = WebappHelper.getDefaultCharset();
        }
        return charset;
    }

    /**
     * @see org.olat.lms.user.UserService#deleteUserProperties(org.olat.data.user.User)
     */
    @Override
    public void deleteUserProperties(User user) {
        // prevent stale objects, reload first
        user = loadUserByKey(user.getKey());
        // loop over user fields and remove them form the database if they are
        // deletable
        final List<UserPropertyHandler> propertyHandlers = userPropertiesConfig.getAllUserPropertyHandlers();
        for (final UserPropertyHandler propertyHandler : propertyHandlers) {
            final String fieldName = propertyHandler.getName();
            if (propertyHandler.isDeletable()) {
                setUserProperty(user, fieldName, null);
            }
        }
        // persist changes
        userDao.updateUser(user);
        if (log.isDebugEnabled()) {
            log.debug("Delete all user-attributtes for user=" + user);
        }
    }

    // /////////////
    /**
     * @see org.olat.lms.user.UserService#syntaxCheckOlatPassword(java.lang.String)
     */
    // @Override
    private boolean syntaxCheckOlatPassword(final String password) {
        return userNameAndPasswordSyntaxChecker.syntaxCheckOlatPassword(password);
    }

    /**
     * The newPassword must not be empty or null, principalName must not be empty or null, oldPassword could be empty or null. <br>
     * The newPassword should be at lest 8 chars length, and uses <code>FileBasedAuthenticator</code> to <code>verifyPasswordStrength</code>.
     */
    @Override
    public boolean verifyPasswordStrength(String oldPassword, String newPassword, String principalName) {
        return AuthenticatorHelper.verifyPasswordStrength(oldPassword, newPassword, principalName);
    }

    /**
     * @see org.olat.lms.user.UserService#syntaxCheckOlatLogin(java.lang.String)
     */
    @Override
    public boolean syntaxCheckOlatLogin(final String login) {
        return userNameAndPasswordSyntaxChecker.syntaxCheckOlatLogin(login);
    }

    // only package scope, used by user impl
    /**
     * @see org.olat.lms.user.UserService#getUserPropertiesConfig()
     */
    @Override
    public UserPropertiesConfig getUserPropertiesConfig() {
        return userPropertiesConfig;
    }

    /**
     * @see org.olat.lms.user.UserService#getUserPropertyHandlersFor(java.lang.String, boolean)
     */
    @Override
    public List<UserPropertyHandler> getUserPropertyHandlersFor(final String usageIdentifyer, final boolean isAdministrativeUser) {
        return userPropertiesConfig.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
    }

    /**
     * @see org.olat.lms.user.UserService#getAllUserPropertyHandlers()
     */
    @Override
    public List<UserPropertyHandler> getAllUserPropertyHandlers() {
        return userPropertiesConfig.getAllUserPropertyHandlers();
    }

    /**
     * @see org.olat.lms.user.UserService#isMandatoryUserProperty(java.lang.String, org.olat.lms.user.propertyhandler.UserPropertyHandler)
     */
    @Override
    public boolean isMandatoryUserProperty(final String usageIdentifyer, final UserPropertyHandler propertyHandler) {
        return userPropertiesConfig.isMandatoryUserProperty(usageIdentifyer, propertyHandler);
    }

    /**
     * @see org.olat.lms.user.UserService#isUserViewReadOnly(java.lang.String, org.olat.lms.user.propertyhandler.UserPropertyHandler)
     */
    @Override
    public boolean isUserViewReadOnly(final String usageIdentifyer, final UserPropertyHandler propertyHandler) {
        return userPropertiesConfig.isUserViewReadOnly(usageIdentifyer, propertyHandler);
    }

    /**
     * [spring]
     * 
     * @see org.olat.lms.user.UserService#setUserNameAndPasswordSyntaxChecker(org.org.olat.lms.user.syntaxchecker.UserNameAndPasswordSyntaxChecker)
     */
    @Override
    public void setUserNameAndPasswordSyntaxChecker(final UserNameAndPasswordSyntaxChecker userNameAndPasswordSyntaxChecker) {
        this.userNameAndPasswordSyntaxChecker = userNameAndPasswordSyntaxChecker;
    }

    /**
     * [spring]
     * 
     * @see org.olat.lms.user.UserService#setUserPropertiesConfig(org.olat.lms.user.propertyhandler.UserPropertiesConfig)
     */
    @Override
    public void setUserPropertiesConfig(final UserPropertiesConfig userPropertiesConfig) {
        this.userPropertiesConfig = userPropertiesConfig;
    }

    /**
     * @see org.olat.lms.user.UserService#getUserProperty(org.olat.data.user.User, java.lang.String)
     */
    @Override
    public String getUserProperty(User user, String propertyName, Locale locale) {
        UserPropertyHandler propertyHandler = getUserPropertiesConfig().getPropertyHandler(propertyName);
        if (propertyHandler == null) {
            return null;
        }
        return propertyHandler.getUserProperty(user, locale);
    }

    /**
     * @see org.olat.lms.user.UserService#getUserProperty(org.olat.data.user.User, java.lang.String)
     */
    @Override
    public String getUserProperty(User user, String propertyName) {
        return getUserProperty(user, propertyName, null);
    }

    /**
     * @see org.olat.lms.user.UserService#getFirstAndLastname(org.olat.data.user.User)
     */
    @Override
    public String getFirstAndLastname(User user) {
        return getUserProperty(user, UserConstants.FIRSTNAME) + " " + getUserProperty(user, UserConstants.LASTNAME);
    }

    @Override
    public String getInstitutionalIdentifier(User user) {
        // uzh.ch uses matriculation/employee number instead of generic institutional identifier
        String institutionalIdentifier = getUserProperty(user, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER);
        if (institutionalIdentifier == null) {
            institutionalIdentifier = getUserProperty(user, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER);
        }
        // leave institutional identifier as fallback
        if (institutionalIdentifier == null) {
            institutionalIdentifier = getUserProperty(user, UserConstants.INSTITUTIONALUSERIDENTIFIER);
        }
        return institutionalIdentifier;
    }

    @Override
    public void setUserProperty(final User user, final String propertyName, final String propertyValue) {
        final UserPropertyHandler propertyHandler = getUserPropertiesConfig().getPropertyHandler(propertyName);
        propertyHandler.setUserProperty(user, propertyValue);
    }

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
    @Override
    public String getPropertyOrIdentityEnvAttribute(final User user, final String propertyName, final Locale locale) {
        String retVal = getUserProperty(user, propertyName, locale);
        if (retVal == null && user.getIdentityEnvironmentAttributes() != null) {
            retVal = user.getIdentityEnvironmentAttributes().get(propertyName);
        }
        return retVal;
    }

}
