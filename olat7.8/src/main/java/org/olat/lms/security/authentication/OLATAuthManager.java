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

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_WEBDAV;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.security.authentication.ldap.LDAPError;
import org.olat.lms.security.authentication.ldap.LDAPLoginManager;
import org.olat.lms.security.authentication.ldap.LDAPLoginModule;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO:
 * <P>
 * Initial Date: 26.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class OLATAuthManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static BaseSecurity baseSecurity;

    /**
     * Change the password of an identity
     * 
     * @param doer
     *            Identity who is changing the password
     * @param identity
     *            Identity who's password is beeing changed.
     * @param newPwd
     *            New password.
     * @return True upon success.
     */
    public static boolean changePassword(final Identity doer, Identity identity, final String newPwd) {

        if (doer == null) {
            throw new AssertException("password changing identity cannot be undefined!");
        }

        if (identity.getKey() == null) {
            throw new AssertException("cannot change password on a nonpersisted identity");
        }

        final String hashedPwd = Encoder.bCryptEncode(newPwd);

        // DBFactory.getInstance().reputInHibernateSessionCache(identity);
        // o_clusterREVIEW
        identity = (Identity) DBFactory.getInstance().loadObject(identity);

        boolean allOk = false;

        final Authentication ldapAuth = getBaseSecurity().findAuthentication(identity, AUTHENTICATION_PROVIDER_LDAP);
        if (ldapAuth != null) {
            if (LDAPLoginModule.isPropagatePasswordChangedOnLdapServer()) {
                final LDAPError ldapError = new LDAPError();
                final LDAPLoginManager ldapLoginManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
                ldapLoginManager.changePassword(identity, newPwd, ldapError);
                log.info(doer.getName() + " change the password on the LDAP server for identity: " + identity.getName());
                allOk = ldapError.isEmpty();

                if (allOk && LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()) {
                    allOk &= changeOlatPassword(doer, identity, hashedPwd);
                }
            }
        } else {
            allOk = changeOlatPassword(doer, identity, hashedPwd);
        }
        return allOk;
    }

    private static BaseSecurity getBaseSecurity() {
        if (baseSecurity == null) {
            baseSecurity = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        }
        return baseSecurity;
    }

    /**
     * If a user has an AUTHENTICATION_PROVIDER_WEBDAV password as well, this must be overwritten by the AUTHENTICATION_PROVIDER_OLAT.
     */
    private static boolean changeOlatPassword(final Identity doer, final Identity identity, final String hashedPwd) {
        Authentication auth = getBaseSecurity().findAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT);
        if (auth == null) { // create new authentication for provider OLAT
            auth = getBaseSecurity().createAndPersistAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT, identity.getName(), hashedPwd);
            log.info(doer.getName() + " created new authenticatin for identity: " + identity.getName());
        }

        auth.setNewCredential(hashedPwd);
        DBFactory.getInstance().updateObject(auth);
        log.info(doer.getName() + " set new password for identity: " + identity.getName());

        try {
            // overwrite the WebDAVPassword
            changeWebDAVPassword(doer, identity, hashedPwd);
        } catch (Exception e) {
            log.error("Error at changeWebDAVPassword: ", e);
        }

        return true;
    }

    private static boolean changeWebDAVPassword(final Identity doer, final Identity identity, final String hashedPwd) {
        Authentication auth = getBaseSecurity().findAuthentication(identity, AUTHENTICATION_PROVIDER_WEBDAV);
        if (auth != null) {
            auth.setNewCredential(hashedPwd);
            DBFactory.getInstance().updateObject(auth);
            log.info(doer.getName() + " set new WebDAV password for identity: " + identity.getName());
        }
        return true;
    }

    /**
     * to change password without knowing exactly who is changing it -> change as admin
     * 
     * @param identity
     * @param newPwd
     * @return
     */
    public static boolean changePasswordAsAdmin(final Identity identity, final String newPwd) {
        final Identity adminUserIdentity = getBaseSecurity().findIdentityByName("administrator");
        return changePassword(adminUserIdentity, identity, newPwd);
    }

    /**
     * to change password by password forgotten link at login screen
     * 
     * @param identity
     * @param newPwd
     * @return
     */
    public static boolean changePasswordByPasswordForgottenLink(final Identity identity, final String newPwd) {
        return changePassword(identity, identity, newPwd);
    }

}
