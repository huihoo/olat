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

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.Attributes;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.registration.TemporaryKey;
import org.olat.data.user.UserDao;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.security.authentication.ldap.LDAPError;
import org.olat.lms.security.authentication.ldap.LDAPLoginManager;
import org.olat.lms.security.authentication.ldap.LDAPLoginModule;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * AuthenticationService
 * 
 * <P>
 * Initial Date: 08.04.2011 <br>
 * 
 * @author guido
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerHelper.getLogger();

    protected AuthenticationServiceImpl() {
    }

    @Autowired
    private RegistrationService registrationManager;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private LoginModule loginModule;
    @Autowired
    private UserDao userManager;

    /**
	 */
    @Override
    public Identity authenticate(final String login, final String pass, final Provider provider) {

        if (pass == null)
            return null; // do never accept empty passwords

        Identity ident = baseSecurity.findIdentityByName(login);

        // check for email instead of username if ident is null
        if (ident == null && loginModule.allowLoginUsingEmail()) {
            if (MailHelper.isValidEmailAddress(login)) {
                ident = userManager.findIdentityByEmail(login);
            }
            // check for email changed with verification workflow
            if (ident == null) {
                ident = findIdentInChangingEmailWorkflow(login);
            }
        }

        if (ident == null)
            return null;

        // find OLAT authentication provider
        final Authentication auth = baseSecurity.findAuthentication(ident, provider.toString());

        if (auth != null && auth.getCredential().equals(Encoder.encrypt(pass))) {
            return ident;
        }

        log.info("Audit:Error authenticating user " + login + " via provider OLAT");
        return null;

    }

    private Identity findIdentInChangingEmailWorkflow(final String login) {
        final List<TemporaryKey> tk = registrationManager.loadTemporaryKeyByAction(RegistrationService.EMAIL_CHANGE);
        if (tk != null) {
            for (final TemporaryKey temporaryKey : tk) {
                final XStream xml = new XStream();
                final HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
                if (login.equals(mails.get("changedEMail"))) {
                    return baseSecurity.findIdentityByName(mails.get("currentEMail"));
                }
            }
        }
        return null;
    }

    /**
	 */
    @Override
    public Authentication findAuthentication(Identity identity, Provider provider) {
        throw new NotImplementedException();
    }

    /**
	 */
    @Override
    public boolean changePassword(Identity changingIdentity, Identity identityToBeChanged, String newPwd) {
        throw new NotImplementedException();
    }

    /* STATIC_METHOD_REFACTORING - check CLEAN CODE principle do not return NULL */
    @Override
    public Identity authenticate(final String username, final String pwd, final LDAPError ldapError) {

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
        final Attributes attrs = ldapManager.bindUser(username, pwd, ldapError);

        if (ldapError.isEmpty() && attrs != null) {
            Identity identity = ldapManager.findIdentyByLdapAuthentication(username, ldapError);
            if (!ldapError.isEmpty()) {
                return null;
            }
            if (identity == null) {
                // User authenticated but not yet existing - create as new OLAT
                // user
                ldapManager.createAndPersistUser(attrs);
                identity = ldapManager.findIdentyByLdapAuthentication(username, ldapError);
            } else {
                // User does already exist - just sync attributes
                final Map<String, String> olatProToSync = ldapManager.prepareUserPropertyForSync(attrs, identity);
                if (olatProToSync != null) {
                    ldapManager.syncUser(olatProToSync, identity);
                }
            }
            // Add or update an OLAT authentication token for this user if
            // configured in the module
            if (identity != null && LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()) {
                final Authentication auth = baseSecurity.findAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT);
                if (auth == null) {
                    // Reuse exising authentication token
                    baseSecurity.createAndPersistAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT, username, Encoder.encrypt(pwd));
                } else {
                    // Create new authenticaten token
                    auth.setCredential(Encoder.encrypt(pwd));
                    DBFactory.getInstance().updateObject(auth);
                }
            }
            return identity;
        }
        return null;
    }

}
