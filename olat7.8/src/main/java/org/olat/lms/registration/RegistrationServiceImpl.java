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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.registration.RegistrationDao;
import org.olat.data.registration.TemporaryKey;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.system.commons.Settings;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

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

    @Override
    public String getChangePasswordLink(final String emailAdress, String localeString, final String ip) {
        TemporaryKey tk = loadTemporaryKeyByEmail(emailAdress);
        if (tk == null) {
            tk = createTemporaryKeyByEmail(emailAdress, ip, RegistrationService.PW_CHANGE);
        }

        String dummyKey = Encoder.encrypt(emailAdress);
        String linkTemplate = getPasswordChangeLinkTemplate(dummyKey, localeString);
        return linkTemplate.replace(dummyKey, tk.getRegistrationKey());
    }

    /**
     * Warning: hard coded link!
     */
    private String getPasswordChangeLinkTemplate(String dummyKey, String localeString) {
        final String serverpath = Settings.getServerContextPathURI();
        return serverpath + "/dmz/pwchange/index.html?key=" + dummyKey + "&amp;lang=" + localeString;
    }

    @Override
    public String getRegistrationLink(final String emailAddress, String localeString, final String ip) {
        TemporaryKey tk = loadTemporaryKeyByEmail(emailAddress);
        if (tk == null) {
            tk = createTemporaryKeyByEmail(emailAddress, ip, RegistrationModule.REGISTRATION);
        }
        return getRegistrationLinkTemplate(tk.getRegistrationKey(), localeString);
    }

    /**
     * Warning: hard coded link!
     */
    private String getRegistrationLinkTemplate(String registrationKey, String localeString) {
        final String serverpath = Settings.getServerContextPathURI();
        return serverpath + "/dmz/registration/index.html?key=" + registrationKey + "&amp;lang=" + localeString;
    }

    @Override
    public String getChangeEmailLink(final Locale locale, TemporaryKey tk) {
        final String serverpath = Settings.getServerContextPathURI();
        return serverpath + "/dmz/emchange/index.html?key=" + tk.getRegistrationKey() + "&lang=" + locale.getLanguage();
    }

    @Override
    public TemporaryKey loadOrCreateChangeEmailTemporaryKey(final String currentEmail, final String changedEmail, final String ip) {
        // load or create temporary key
        final HashMap<String, String> mailMap = new HashMap<String, String>();
        mailMap.put("currentEMail", currentEmail);
        mailMap.put("changedEMail", changedEmail);

        final XStream xml = new XStream();
        final String serMailMap = xml.toXML(mailMap);

        TemporaryKey tk = loadCleanTemporaryKey(serMailMap);
        if (tk == null) {
            tk = createTemporaryKeyByEmail(serMailMap, ip, RegistrationService.EMAIL_CHANGE);
        } else {
            deleteTemporaryKeyWithId(tk.getRegistrationKey());
            tk = createTemporaryKeyByEmail(serMailMap, ip, RegistrationService.EMAIL_CHANGE);
        }
        return tk;
    }

    /**
     * Load and clean temporary keys with action "EMAIL_CHANGE".
     * 
     * @param serMailMap
     * @return
     */
    private TemporaryKey loadCleanTemporaryKey(final String serMailMap) {
        TemporaryKey tk = loadTemporaryKeyByEmail(serMailMap);
        if (tk == null) {
            final XStream xml = new XStream();
            final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(serMailMap);
            final String currentEMail = mails.get("currentEMail");
            List<TemporaryKey> tks = loadTemporaryKeyByAction(RegistrationService.EMAIL_CHANGE);
            if (tks != null) {
                synchronized (tks) {
                    tks = loadTemporaryKeyByAction(RegistrationService.EMAIL_CHANGE);
                    int countCurrentEMail = 0;
                    for (final TemporaryKey temporaryKey : tks) {
                        final HashMap<String, String> tkMails = (HashMap<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
                        if (tkMails.get("currentEMail").equals(currentEMail)) {
                            if (countCurrentEMail > 0) {
                                // clean
                                deleteTemporaryKeyWithId(temporaryKey.getRegistrationKey());
                            } else {
                                // load
                                tk = temporaryKey;
                            }
                            countCurrentEMail++;
                        }
                    }
                }
            }
        }
        return tk;
    }

}
