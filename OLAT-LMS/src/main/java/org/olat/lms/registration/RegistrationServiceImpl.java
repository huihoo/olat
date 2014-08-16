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

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.registration.RegistrationDao;
import org.olat.data.registration.TemporaryKey;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.registration.RegistrationController;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailHelper;
import org.olat.system.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 
 * @author Sabina Jeger, Christian Guretzki
 */
@Service
public class RegistrationServiceImpl extends BasicManager implements RegistrationService {

    private static final Logger log = LoggerHelper.getLogger();

    protected static final int REG_WORKFLOW_STEPS = 5;
    protected static final int PWCHANGE_WORKFLOW_STEPS = 4;

    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationDao registrationDao;

    private RegistrationServiceImpl() {
    }

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
    @Override
    public Identity createNewUserAndIdentityFromTemporaryKey(final String login, final String pwd, final User myUser, final TemporaryKeyImpl tk) {
        final Identity identity = baseSecurity.createAndPersistIdentityAndUserWithUserGroup(login, pwd, myUser);
        if (identity == null) {
            return null;
        }
        registrationDao.deleteTemporaryKey(tk);
        return identity;
    }

    /**
     * Send a notification messaged to the given notification email address about the registratoin of the given new identity.
     * 
     * @param notificationMailAddress
     *            Email address who should be notified. MUST NOT BE NULL
     * @param newIdentity
     *            The newly registered Identity
     */
    @Override
    public void sendNewUserNotificationMessage(final String notificationMailAddress, final Identity newIdentity) {
        Address from;
        Address[] to;
        try {
            from = new InternetAddress(WebappHelper.getMailConfig("mailFrom"));
            to = new Address[] { new InternetAddress(notificationMailAddress) };
        } catch (final AddressException e) {
            log.error("Could not send registration notification message , bad mail address");
            return;
        }
        final MailerResult result = new MailerResult();
        final User user = newIdentity.getUser();
        final Locale loc = I18nModule.getDefaultLocale();
        final String[] userParams = new String[] { newIdentity.getName(), userService.getUserProperty(user, UserConstants.FIRSTNAME, loc),
                userService.getUserProperty(user, UserConstants.LASTNAME, loc), userService.getUserProperty(user, UserConstants.EMAIL, loc),
                user.getPreferences().getLanguage(), Settings.getServerconfig("server_fqdn") + WebappHelper.getServletContextPath() };
        final Translator trans = new PackageTranslator(PackageUtil.getPackageName(RegistrationController.class), loc);
        final String subject = trans.translate("reg.notiEmail.subject", userParams);
        final String body = trans.translate("reg.notiEmail.body", userParams);

        MailHelper.sendMessage(from, to, null, null, body, subject, null, result);
        if (result.getReturnCode() != MailerResult.OK) {
            log.error("Could not send registration notification message , MailerResult was ::" + result.getReturnCode());
        }
    }

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
    @Override
    public TemporaryKeyImpl createTemporaryKeyByEmail(final String email, final String ip, final String action) {
        return registrationDao.createTemporaryKeyByEmail(email, ip, action);
    }

    /**
     * returns an existing TemporaryKey by a given email address or null if none found
     * 
     * @param email
     * @return the found temporary key or null if none is found
     */
    @Override
    public TemporaryKeyImpl loadTemporaryKeyByEmail(final String email) {
        return registrationDao.loadTemporaryKeyByEmail(email);
    }

    /**
     * returns an existing list of TemporaryKey by a given action or null if none found
     * 
     * @param action
     * @return the found temporary key or null if none is found
     */
    @Override
    public List<TemporaryKey> loadTemporaryKeyByAction(final String action) {
        return registrationDao.loadTemporaryKeyByAction(action);
    }

    /**
     * Looks for a TemporaryKey by a given registrationkey
     * 
     * @param regkey
     *            the encrypted registrationkey
     * @return the found TemporaryKey or null if none is found
     */
    @Override
    public TemporaryKeyImpl loadTemporaryKeyByRegistrationKey(final String regkey) {
        return registrationDao.loadTemporaryKeyByRegistrationKey(regkey);
    }

    /**
     * Creates a TemporaryKey and saves it permanently
     * 
     * @param emailaddress
     * @param ipaddress
     * @param action
     *            REGISTRATION or PWCHANGE
     * @return newly created temporary key
     */
    @Override
    public TemporaryKeyImpl register(final String emailaddress, final String ipaddress, final String action) {
        return registrationDao.register(emailaddress, ipaddress, action);
    }

    /**
     * Delete a temporary key.
     * 
     * @param keyValue
     */
    @Override
    public void deleteTemporaryKeyWithId(final String keyValue) {
        final TemporaryKeyImpl tKey = loadTemporaryKeyByRegistrationKey(keyValue);
        registrationDao.deleteTemporaryKey(tKey);
    }

    /**
     * Evaluates whether the given identity needs to accept a disclaimer before logging in or not.
     * 
     * @param identity
     * @return true: user must accept the disclaimer; false: user did already accept or must not accept a disclaimer
     */
    @Override
    public boolean needsToConfirmDisclaimer(final Identity identity) {
        boolean needsToConfirm = false; // default is not to confirm
        if (RegistrationModule.isDisclaimerEnabled()) {
            // don't use the discrete method to be more robust in case that more than one
            // property is found
            final List disclaimerProperties = PropertyManager.getInstance().listProperties(identity, null, null, "user", "dislaimer_accepted");
            needsToConfirm = (disclaimerProperties.size() == 0);
        }
        return needsToConfirm;
    }

    /**
     * Marks the given identity to have confirmed the disclaimer. Note that this method does not check if the disclaimer does already exist, do this by calling
     * needsToConfirmDisclaimer() first!
     * 
     * @param identity
     */
    @Override
    public void setHasConfirmedDislaimer(final Identity identity) {
        final PropertyManager propertyMgr = PropertyManager.getInstance();
        final PropertyImpl disclaimerProperty = propertyMgr.createUserPropertyInstance(identity, "user", "dislaimer_accepted", null, 1l, null, null);
        propertyMgr.saveProperty(disclaimerProperty);
    }

    /**
     * Remove all disclaimer confirmations. This means that every user on the system must accept the disclaimer again.
     */
    @Override
    public void revokeAllconfirmedDisclaimers() {
        final PropertyManager propertyMgr = PropertyManager.getInstance();
        propertyMgr.deleteProperties(null, null, null, "user", "dislaimer_accepted");
    }

    /**
     * Remove the disclaimer confirmation for the specified identity. This means that this user must accept the disclaimer again.
     * 
     * @param identity
     */
    @Override
    public void revokeConfirmedDisclaimer(final Identity identity) {
        final PropertyManager propertyMgr = PropertyManager.getInstance();
        propertyMgr.deleteProperties(identity, null, null, "user", "dislaimer_accepted");
    }

    /**
     * Get a list of all users that did already confirm the disclaimer
     * 
     * @return
     */
    @Override
    public List<Identity> getIdentitiesWithConfirmedDisclaimer() {
        return registrationDao.getIdentitiesWithConfirmedDisclaimer();
    }

}
