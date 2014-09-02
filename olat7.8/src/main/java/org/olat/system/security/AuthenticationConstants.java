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
package org.olat.system.security;

/**
 * Constants commonly used in the system, access them via static import
 * 
 * e.g.: import static org.olat.system.commons.constants.Constants.AUTHENTICATION_PROVIDER_LDAP;
 * 
 * <P>
 * Initial Date: 06.04.2011 <br>
 * 
 * @author guido
 */
public class AuthenticationConstants {

    /**
     * Authentication provider strings which are used to match specific auth. info persisted in the database
     */
    public static final String AUTHENTICATION_PROVIDER_LDAP = "LDAP";
    public static final String AUTHENTICATION_PROVIDER_OLAT = "OLAT";
    public static final String AUTHENTICATION_PROVIDER_WEBDAV = "WEBDAV";

    public static final String AUTHENTICATION_PROVIDER_SHIB = "Shib";

}
