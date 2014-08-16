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

import org.olat.data.basesecurity.Identity;

/**
 * Initial Date: 21.10.2011 <br>
 * 
 * @author lavinia
 */
public class BulkPasswordChangeParameter {

    private final String[] usernames;
    private final String password;
    private final boolean isDisclaimerAccepted;
    private final boolean isLanguageDE;
    private final Identity initiatorIdentity;

    /**
     * @param usernames
     * @param password
     * @param autodisc
     * @param langGerman
     * @param initiatorIdentity
     */
    public BulkPasswordChangeParameter(String[] usernames, String password, boolean isDisclaimerAccepted, boolean isLanguageDE, Identity initiatorIdentity) {
        super();
        this.usernames = usernames;
        this.password = password;
        this.isDisclaimerAccepted = isDisclaimerAccepted;
        this.isLanguageDE = isLanguageDE;
        this.initiatorIdentity = initiatorIdentity;
    }

    public String[] getUsernames() {
        return usernames;
    }

    public String getPassword() {
        return password;
    }

    public boolean isDisclaimerAccepted() {
        return isDisclaimerAccepted;
    }

    public boolean isLanguageDE() {
        return isLanguageDE;
    }

    public Identity getInitiatorIdentity() {
        return initiatorIdentity;
    }

}
