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

import java.util.List;
import java.util.Locale;

import org.olat.lms.user.propertyhandler.UserPropertyHandler;

/**
 * Used for EBL;
 * 
 * Initial Date: 19.10.2011 <br>
 * 
 * @author lavinia
 */
public class ImportableUserParameter {

    private final String username;
    private String password; // not final
    private final String language;
    private final Locale locale;
    private final List<UserPropertyHandler> userPropertyHandlers;
    private final List<String> userPropertiesInput;

    public ImportableUserParameter(String username, String password, String language, Locale locale, List<UserPropertyHandler> userPropertyHandlers,
            List<String> userPropertiesInput) {

        this.username = username;
        this.password = password;
        this.language = language;
        this.locale = locale;
        this.userPropertyHandlers = userPropertyHandlers;
        this.userPropertiesInput = userPropertiesInput;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<UserPropertyHandler> getUserPropertyHandlers() {
        return userPropertyHandlers;
    }

    public List<String> getUserPropertiesInput() {
        return userPropertiesInput;
    }

}
