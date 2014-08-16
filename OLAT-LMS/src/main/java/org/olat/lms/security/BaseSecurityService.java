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

import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;

/**
 * This is just a temporary facade for the BaseSecurity.
 * 
 * <P>
 * Initial Date: 09.06.2011 <br>
 * 
 * @author lavinia
 */
@Deprecated
public interface BaseSecurityService {

    public Identity createAndPersistIdentityAndUser(final String username, final User user, final String provider, final String authusername, final String credential);

    public Identity createAndPersistIdentity(final String username, final User user, final String provider, final String authusername, final String credential);

    /**
     * Returns the anonymous identity for a given locale, normally used to log in as guest user
     * 
     * @param locale
     * @return The identity
     */
    public Identity getAndUpdateAnonymousUserForLanguage(Locale locale, final String firstName);
}
