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
package org.olat.presentation.portal.zsuz;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.Persistable;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.system.exception.AssertException;
import org.olat.system.security.PrincipalAttributes;

class ZentralstelleZentrum implements Identity {

    private UserService userService;

    protected ZentralstelleZentrum() {
        //
    }

    @Override
    public Long getKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SuppressWarnings("unused")
    public boolean equalsByPersistableKey(final Persistable arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public Date getLastModified() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getCreationDate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SuppressWarnings("unused")
    public void setStatus(final Integer arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    @SuppressWarnings("unused")
    public void setLastLogin(final Date arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getUser() {
        return new User() {
            Map<String, String> data = new HashMap<String, String>();
            {
                data.put(UserConstants.FIRSTNAME, "Zsuz Zentrum");
                data.put(UserConstants.LASTNAME, "Druckerei Zentrum");
                data.put(UserConstants.EMAIL, "druckz@zsuz.uzh.ch");
                data.put(UserConstants.INSTITUTIONALNAME, "Zentralstelle UZH");
                data.put(UserConstants.INSTITUTIONALEMAIL, "druckz@zsuz.uzh.ch");
            }

            @Override
            public Long getKey() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            @SuppressWarnings("unused")
            public boolean equalsByPersistableKey(final Persistable persistable) {
                // TODO Auto-generated method stub
                return false;
            }

            public Date getLastModified() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Date getCreationDate() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            @SuppressWarnings("unused")
            public void setPreferences(final Preferences prefs) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setIdentityEnvironmentAttributes(final Map<String, String> identEnvAttribs) {
                throw new AssertException("SETTER not yet implemented, not used in case of ZentralstellePortlet");
            }

            @Override
            public Preferences getPreferences() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Map<String, String> getIdentityEnvironmentAttributes() {
                return null;
            }

            @Override
            public String getRawUserProperty(String propertyKey) {
                return data.get(propertyKey);
            }

        };
    }

    @Override
    public Integer getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "zentralstelle_druckerei_zentrum";
    }

    @Override
    public Date getLastLogin() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setName(final String loginName) {
        // TODO Auto-generated method stub

    }

    @Override
    public PrincipalAttributes getAttributes() {
        return new PrincipalAttributesImpl(userService);
    }

    private class PrincipalAttributesImpl implements PrincipalAttributes {

        private UserService userService;

        /**
         * @param userService2
         */
        public PrincipalAttributesImpl(UserService userService) {
            this.userService = userService;
        }

        /**
         * @see org.olat.system.security.PrincipalAttributes#getEmail()
         */
        @Override
        public String getEmail() {
            return userService.getUserProperty(getUser(), UserConstants.EMAIL);
        }

        /**
         * @see org.olat.system.security.PrincipalAttributes#getInstitutionalEmail()
         */
        @Override
        public String getInstitutionalEmail() {
            return userService.getUserProperty(getUser(), UserConstants.INSTITUTIONALEMAIL);
        }

        /**
         * 
         * @see org.olat.system.security.PrincipalAttributes#isEmailDisabled()
         */
        public boolean isEmailDisabled() {
            String value = userService.getUserProperty(getUser(), UserConstants.EMAILDISABLED);
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
            return userService.getUserProperty(getUser(), UserConstants.FIRSTNAME);
        }

        /**
         * @see org.olat.system.security.PrincipalAttributes#getLastName()
         */
        @Override
        public String getLastName() {
            return userService.getUserProperty(getUser(), UserConstants.LASTNAME);
        }

    }

}
