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
package org.olat.lms.instantmessaging;

import org.olat.data.basesecurity.Identity;

/**
 * Initial Date: 25.10.2011 <br>
 * 
 * @author guido
 */
public class IMUserInfo {

    private Identity identity;
    private String password;
    private String fullUserName;
    private String email;
    private String rosterDefaultStatus;

    /**
     * @param identity
     * @param password
     */
    public IMUserInfo(Identity identity, String password) {
        this.identity = identity;
        this.password = password;
    }

    public String getFullUserName() {
        return fullUserName;
    }

    public void setFullUserName(String fullUserName) {
        this.fullUserName = fullUserName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return identity.getName();
    }

    public String getIMPassword() {
        return password;
    }

    public String getRosterDefaultStatus() {
        return rosterDefaultStatus;
    }

    public void setRosterDefaultStatus(String rosterDefaultStatus) {
        this.rosterDefaultStatus = rosterDefaultStatus;

    }

}
