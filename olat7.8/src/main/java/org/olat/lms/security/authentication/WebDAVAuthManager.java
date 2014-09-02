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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.security.authentication;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Authentication provider for WebDAV
 * <P>
 * Initial Date: 13 apr. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class WebDAVAuthManager {

    public static final String PROVIDER_WEBDAV = "WEBDAV";

    private static final Logger log = LoggerHelper.getLogger();

    private static BaseSecurity baseSecurity;

    /**
     * Change the WEBDAV-Password of an identity
     * 
     * @param doer
     *            Identity who is changing the password
     * @param identity
     *            Identity who's password is being changed.
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

        identity = (Identity) DBFactory.getInstance().loadObject(identity);

        return changeWebDAVPassword(doer, identity, hashedPwd);
    }

    private static boolean changeWebDAVPassword(final Identity doer, final Identity identity, final String hashedPwd) {
        Authentication auth = getBaseSecurity().findAuthentication(identity, PROVIDER_WEBDAV);
        if (auth == null) { // create new authentication for provider OLAT
            auth = getBaseSecurity().createAndPersistAuthentication(identity, PROVIDER_WEBDAV, identity.getName(), hashedPwd);
            log.info(doer.getName() + " created new WebDAV authenticatin for identity: " + identity.getName());
        }

        auth.setNewCredential(hashedPwd);
        DBFactory.getInstance().updateObject(auth);
        log.info(doer.getName() + " set new WebDAV password for identity: " + identity.getName());
        return true;
    }

    private static BaseSecurity getBaseSecurity() {
        if (baseSecurity == null) {
            baseSecurity = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
        }
        return baseSecurity;
    }

    /**
     * Authenticate against the WEBDAV Authentication provider.
     * 
     * @param login
     * @param pass
     * @return Identity if authentication was successful, null otherwise.
     */
    public static Identity authenticate(final String login, final String pass) {
        final Identity ident = getBaseSecurity().findIdentityByName(login);
        if (ident == null) {
            return null;
        }
        final boolean visible = getBaseSecurity().isIdentityVisible(login);
        if (!visible) {
            return null;
        }

        // find WEBDAV authentication provider
        final Authentication auth = getBaseSecurity().findAuthentication(ident, PROVIDER_WEBDAV);

        boolean successfullyAuthenticated = authenticate(pass, auth);

        if (successfullyAuthenticated) {
            return ident;
        }

        // fallback to OLAT authentication provider
        AuthenticationService authenticationService = (AuthenticationService) CoreSpringFactory.getBean(AuthenticationService.class);
        return authenticationService.authenticate(login, pass, Provider.OLAT);
    }

    /**
     * Authenticate against the new credential, if available, else against the old one.
     */
    public static boolean authenticate(final String pass, final Authentication auth) {
        boolean successfullyAuthenticated = false;
        // check the new credential first, if no new credential available check the old one
        if (auth != null && auth.getNewCredential() != null && Encoder.matches(pass, auth.getNewCredential())) {
            successfullyAuthenticated = true;
        } else if (auth != null && auth.getNewCredential() == null && auth.getCredential() != null && auth.getCredential().equals(Encoder.encrypt(pass))) {
            successfullyAuthenticated = true;
        }
        return successfullyAuthenticated;
    }

}
