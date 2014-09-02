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
package org.olat.lms.core.notification.impl;

import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.Preferences;
import org.olat.data.user.TestUserFactory;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertiesConfig;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.lms.user.syntaxchecker.UserNameAndPasswordSyntaxChecker;

/**
 * Initial Date: 09.01.2012 <br>
 * 
 * @author guretzki
 */
public class UserServiceNotificationMock implements UserService {

    @Override
    public User createUser(String firstName, String lastName, String eMail) {
        User newUser = TestUserFactory.createTestUserForJunit(firstName, lastName, eMail);
        // TODO: extract the following code in UserServiceImpl and re-use it
        Preferences prefs = newUser.getPreferences();

        Locale loc = Locale.GERMAN;
        prefs.setLanguage(loc.toString());
        prefs.setFontsize("normal");
        prefs.setPresenceMessagesPublic(false);
        prefs.setInformSessionTimeout(false);
        return newUser;
    }

    @Override
    public String getUserProperty(User user, String propertyName) {
        System.out.println("### TEST UserServiceNotificationMock user=" + user);
        return user.getRawUserProperty(propertyName);
    }

    @Override
    public User createAndPersistUser(String firstName, String lastName, String email) {
        return null;
    }

    @Override
    public Identity findIdentityByEmail(String email) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return null;
    }

    @Override
    public boolean userExist(String email) {
        return false;
    }

    @Override
    public User loadUserByKey(Long key) {
        return null;
    }

    @Override
    public void updateUser(User usr) {
        // empty
    }

    @Override
    public void saveUser(User user) {
        // empty
    }

    @Override
    public boolean updateUserFromIdentity(Identity identity) {
        return false;
    }

    @Override
    public void setUserCharset(Identity identity, String charset) {
        // empty
    }

    @Override
    public String getUserCharset(Identity identity) {
        return null;
    }

    @Override
    public void deleteUserProperties(User user) {
        // empty
    }

    @Override
    public boolean verifyPasswordStrength(String oldPassword, String newPassword, String principalName) {
        return false;
    }

    @Override
    public boolean syntaxCheckOlatLogin(String login) {
        return false;
    }

    @Override
    public UserPropertiesConfig getUserPropertiesConfig() {
        return null;
    }

    @Override
    public List<UserPropertyHandler> getUserPropertyHandlersFor(String usageIdentifyer, boolean isAdministrativeUser) {
        return null;
    }

    @Override
    public List<UserPropertyHandler> getAllUserPropertyHandlers() {
        return null;
    }

    @Override
    public boolean isMandatoryUserProperty(String usageIdentifyer, UserPropertyHandler propertyHandler) {
        return false;
    }

    @Override
    public boolean isUserViewReadOnly(String usageIdentifyer, UserPropertyHandler propertyHandler) {
        return false;
    }

    @Override
    public void setUserNameAndPasswordSyntaxChecker(UserNameAndPasswordSyntaxChecker userNameAndPasswordSyntaxChecker) {
        // empty
    }

    @Override
    public void setUserPropertiesConfig(UserPropertiesConfig userPropertiesConfig) {
        // empty
    }

    @Override
    public String getUserProperty(User user, String propertyName, Locale locale) {
        return null;
    }

    @Override
    public String getFirstAndLastname(User user) {
        return null;
    }

    @Override
    public String getInstitutionalIdentifier(User user) {
        return null;
    }

    @Override
    public void setUserProperty(User user, String propertyName, String propertyValue) {
        // empty
    }

    @Override
    public String getPropertyOrIdentityEnvAttribute(User user, String propertyName, Locale locale) {
        return null;
    }

}
