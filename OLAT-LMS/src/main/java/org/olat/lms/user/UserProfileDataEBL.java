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
package org.olat.lms.user;

import org.olat.data.basesecurity.Identity;

/**
 * return value object for presentation layer
 * 
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class UserProfileDataEBL {

    private final Identity identity;
    private final boolean emailChanged;
    private final boolean userUpdated;
    private final boolean olatAdmin;
    private final boolean olatManager;
    private final String currentEmail;
    private final String changedEmail;

    /**
     * @param identity
     * @param emailChanged
     * @param userUpdated
     */
    public UserProfileDataEBL(Identity identity, boolean emailChanged, boolean userUpdated, boolean olatAdmin, boolean olatManager, String currentEmail,
            String changedEmail) {
        this.identity = identity;
        this.emailChanged = emailChanged;
        this.userUpdated = userUpdated;
        this.olatAdmin = olatAdmin;
        this.olatManager = olatManager;
        this.currentEmail = currentEmail;
        this.changedEmail = changedEmail;
    }

    public Identity getIdentity() {
        return identity;
    }

    public boolean isEmailChanged() {
        return emailChanged;
    }

    public boolean isUserUpdated() {
        return userUpdated;
    }

    public boolean isOlatAdmin() {
        return olatAdmin;
    }

    public boolean isOlatManager() {
        return olatManager;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public String getChangedEmail() {
        return changedEmail;
    }
}
