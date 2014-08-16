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
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 11.10.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class RegistrationServiceEBL {

    @Autowired
    RegistrationService registrationService;
    @Autowired
    UserService userService;

    public TemporaryKey getOrCreateTemporaryKey(final String ip, Identity identity, final Locale locale) {
        final String emailAdress = userService.getUserProperty(identity.getUser(), UserConstants.EMAIL, locale);
        TemporaryKey tk = registrationService.loadTemporaryKeyByEmail(emailAdress);
        if (tk == null) {
            tk = registrationService.createTemporaryKeyByEmail(emailAdress, ip, RegistrationService.PW_CHANGE);
        }
        return tk;
    }

    /**
     * @param ip
     * @param email
     * @return
     */
    public TemporaryKey getOrCreateTemporaryKey(final String ip, final String email) {
        TemporaryKey tk = registrationService.loadTemporaryKeyByEmail(email);
        if (tk == null) {
            tk = registrationService.createTemporaryKeyByEmail(email, ip, RegistrationModule.REGISTRATION);
        }
        return tk;
    }

    public Identity registerUser(RegisterUserParameter parameterObject) {

        final User volatileUser = userService.createUser(parameterObject.firstName, parameterObject.lastName, parameterObject.tempKey.getEmailAddress());
        final Preferences preferences = volatileUser.getPreferences();
        preferences.setLanguage(parameterObject.locale);
        volatileUser.setPreferences(preferences);

        final Identity persistedIdentity = registrationService.createNewUserAndIdentityFromTemporaryKey(parameterObject.login, parameterObject.pwd, volatileUser,
                parameterObject.tempKey);
        if (persistedIdentity != null) {
            updateUserProperties(persistedIdentity.getUser(), parameterObject.userPropertyParameters);
            // persist changes in db
            userService.updateUserFromIdentity(persistedIdentity);
            // send notification mail to sys admin
            final String notiEmail = RegistrationModule.getRegistrationNotificationEmail();
            if (notiEmail != null) {
                registrationService.sendNewUserNotificationMessage(notiEmail, persistedIdentity);
            }

            // tell system that this user did accept the disclaimer
            registrationService.setHasConfirmedDislaimer(persistedIdentity);
        }
        return persistedIdentity;
    }

    /**
     * @param userPropertyParameters
     */
    private void updateUserProperties(final User user, final List<UserPropertyParameter> userPropertyParameters) {
        for (final UserPropertyParameter userPropertyParameter : userPropertyParameters) {
            userPropertyParameter.userPropertyHandler.updateUserFromFormItem(user, userPropertyParameter.formItem);
        }
    }

}
