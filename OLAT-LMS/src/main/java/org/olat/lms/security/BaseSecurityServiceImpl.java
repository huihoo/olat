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
package org.olat.lms.security;

import java.util.Locale;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.presentation.events.NewIdentityCreatedEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for BaseSecurityServiceImpl
 * 
 * <P>
 * Initial Date: 09.06.2011 <br>
 * 
 * @author lavinia
 */
@Service
public class BaseSecurityServiceImpl implements BaseSecurityService {

    protected BaseSecurityServiceImpl() {
    }

    @Autowired
    private BaseSecurity baseSecurity;

    @Autowired
    private UserService userService;

    private static String GUEST_USERNAME_PREFIX = "guest_";

    public static final OLATResourceable IDENTITY_EVENT_CHANNEL = OresHelper.lookupType(Identity.class);

    /**
     * @param username
     *            the username
     * @param user
     *            the presisted User
     * @param authusername
     *            the username used as authentication credential (=username for provider "OLAT")
     * @param provider
     *            the provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
     * @param credential
     *            the credentials or null if not used
     * @return Identity
     */
    @Override
    public Identity createAndPersistIdentity(final String username, final User user, final String provider, final String authusername, final String credential) {
        final Identity identity = baseSecurity.createAndPersistIdentity(username, user, provider, authusername, credential);
        notifyNewIdentityCreated(identity);
        return identity;
    }

    private void notifyNewIdentityCreated(final Identity newIdentity) {
        // Save the identity on the DB. So can the listeners of the event retrieve it
        // in cluster mode
        DBFactory.getInstance().intermediateCommit();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new NewIdentityCreatedEvent(newIdentity), IDENTITY_EVENT_CHANNEL);
    }

    /**
     * @param username
     *            the username
     * @param user
     *            the unpresisted User
     * @param authusername
     *            the username used as authentication credential (=username for provider "OLAT")
     * @param provider
     *            the provider of the authentication ("OLAT" or "AAI"). If null, no authentication token is generated.
     * @param credential
     *            the credentials or null if not used
     * @return Identity
     */
    @Override
    public Identity createAndPersistIdentityAndUser(final String username, final User user, final String provider, final String authusername, final String credential) {
        final Identity identity = baseSecurity.createAndPersistIdentityAndUser(username, user, provider, authusername, credential);
        notifyNewIdentityCreated(identity);
        return identity;
    }

    public Identity getAndUpdateAnonymousUserForLanguage(final Locale locale, final String firstName) {
        final String guestUsername = GUEST_USERNAME_PREFIX + locale.toString();
        Identity guestIdentity = baseSecurity.findIdentityByName(guestUsername);
        if (guestIdentity == null) {
            // Create it lazy on demand
            final User guestUser = userService.createUser(firstName, null, null);
            guestUser.getPreferences().setLanguage(locale.toString());
            guestIdentity = createAndPersistIdentityAndUser(guestUsername, guestUser, null, null, null);
            final SecurityGroup anonymousGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
            baseSecurity.addIdentityToSecurityGroup(guestIdentity, anonymousGroup);
            return guestIdentity;
        } else {
            // Check if guest name has been updated in the i18n tool
            if (!userService.getUserProperty(guestIdentity.getUser(), UserConstants.FIRSTNAME, locale).equals(firstName)) {
                userService.setUserProperty(guestIdentity.getUser(), UserConstants.FIRSTNAME, firstName);
                DBFactory.getInstance().updateObject(guestIdentity);
            }
            return guestIdentity;
        }
    }
}
