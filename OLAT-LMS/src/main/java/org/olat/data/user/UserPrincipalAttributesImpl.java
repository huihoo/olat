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
package org.olat.data.user;

import org.olat.system.security.PrincipalAttributes;

/**
 * TODO: Class Description for UserPricipalAttributesImpl
 * 
 * <P>
 * Initial Date: 28.06.2011 <br>
 * 
 * @author cg
 */
public class UserPrincipalAttributesImpl implements PrincipalAttributes {

    private User user;

    /**
     * @param user
     */
    public UserPrincipalAttributesImpl(User user) {
        this.user = user;
    }

    /**
     * @see org.olat.system.security.PrincipalAttributes#getEmail()
     */
    @Override
    public String getEmail() {
        return user.getRawUserProperty(UserConstants.EMAIL);
    }

    /**
     * @see org.olat.system.security.PrincipalAttributes#getInstitutionalEmail()
     */
    @Override
    public String getInstitutionalEmail() {
        return user.getRawUserProperty(UserConstants.INSTITUTIONALEMAIL);
    }

    /**
     * 
     * @see org.olat.system.security.PrincipalAttributes#isEmailDisabled()
     */
    public boolean isEmailDisabled() {
        String value = user.getRawUserProperty(UserConstants.EMAILDISABLED);
        if (value != null && value.equals("true")) {
            return true;
        }
        return false;
    }

    /**
     * @see org.olat.system.security.PrincipalAttributes#getFirstName()
     */
    @Override
    public String getFirstName() {
        return user.getRawUserProperty(UserConstants.FIRSTNAME);
    }

    /**
     * @see org.olat.system.security.PrincipalAttributes#getLastName()
     */
    @Override
    public String getLastName() {
        return user.getRawUserProperty(UserConstants.LASTNAME);
    }

}
