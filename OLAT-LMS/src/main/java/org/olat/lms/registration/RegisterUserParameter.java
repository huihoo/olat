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
package org.olat.lms.registration;

import java.util.List;
import java.util.Locale;

import org.olat.data.registration.TemporaryKeyImpl;

/**
 * Used for EBL.
 * 
 * Initial Date: 11.10.2011 <br>
 * 
 * @author lavinia
 */
public class RegisterUserParameter {

    public final String login;
    public final String pwd;
    public final String firstName;
    public final String lastName;
    public final Locale locale;
    public final TemporaryKeyImpl tempKey;
    public final List<UserPropertyParameter> userPropertyParameters;

    /**
     * 
     */
    public RegisterUserParameter(String login, String pwd, String firstName, String lastName, Locale locale, TemporaryKeyImpl tempKey,
            List<UserPropertyParameter> userPropertyParameters) {
        this.login = login;
        this.pwd = pwd;
        this.firstName = firstName;
        this.lastName = lastName;
        this.locale = locale;
        this.tempKey = tempKey;
        this.userPropertyParameters = userPropertyParameters;
    }
}
