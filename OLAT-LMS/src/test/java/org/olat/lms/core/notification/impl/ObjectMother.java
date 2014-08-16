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

import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.basesecurity.TestIdentityFactory;
import org.olat.data.user.Preferences;
import org.olat.data.user.TestUserFactory;
import org.olat.data.user.UserImpl;

/**
 * Initial Date: 28.03.2012 <br>
 * 
 * @author aabouc
 */
public class ObjectMother {

    public static Identity createIdentity(String username) { // TODO: alternative to the other method with the same name
        UserImpl user = createUser(username + "_FIRST", username + "_LAST", username + "@TEST.tst");
        IdentityImpl identity = TestIdentityFactory.createTestIdentityForJunit(username, user);
        return identity;
    }

    public static UserImpl createUser(String firstName, String lastName, String eMail) {
        UserImpl newUser = TestUserFactory.createTestUserForJunit(firstName, lastName, eMail);
        // TODO: extract the following code in UserServiceImpl and re-use it
        Preferences prefs = newUser.getPreferences();

        Locale loc = Locale.GERMAN;
        prefs.setLanguage(loc.toString());
        prefs.setFontsize("normal");
        prefs.setPresenceMessagesPublic(false);
        prefs.setInformSessionTimeout(false);
        return newUser;
    }

}
