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

package org.olat.lms.course.run.preview;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.Persistable;
import org.olat.data.user.Preferences;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.system.exception.AssertException;
import org.olat.system.security.PrincipalAttributes;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewIdentity implements Identity {

    /**
	 */
    @Override
    public String getName() {
        return "JaneDoe";
    }

    /**
	 */
    @Override
    public User getUser() {
        return new User() {
            Map<String, String> data = new HashMap<String, String>();
            private Map<String, String> envAttrs;
            {
                data.put(UserConstants.FIRSTNAME, "Jane");
                data.put(UserConstants.LASTNAME, "Doe");
                data.put(UserConstants.EMAIL, "jane.doe@testmail.com");
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
                this.envAttrs = identEnvAttribs;
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

    /**
	 */
    @Override
    public Date getCreationDate() {
        throw new AssertException("unsupported");
    }

    /**
	 */
    public Date getLastModified() {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public Long getKey() {
        throw new AssertException("unsupported");
    }

    /**
	 */
    @Override
    public boolean equalsByPersistableKey(final Persistable persistable) {
        throw new AssertException("unsupported");
    }

    @Override
    public Date getLastLogin() {
        throw new AssertException("unsupported");
    }

    @Override
    public void setLastLogin(final Date loginDate) {
        throw new AssertException("unsupported");
    }

    @Override
    public Integer getStatus() {
        throw new AssertException("unsupported");
    }

    @Override
    public void setStatus(final Integer newStatus) {
        throw new AssertException("unsupported");
    }

    public Date getDeleteEmailDate() {
        throw new AssertException("unsupported");
    }

    public void setDeleteEmailDate(final Date newDeleteEmail) {
        throw new AssertException("unsupported");
    }

    @Override
    public void setName(final String loginName) {

    }

    /**
     * @see org.olat.system.security.OLATPrincipal#getAttributes()
     */
    @Override
    public PrincipalAttributes getAttributes() {
        throw new AssertException("unsupported");
    }

}
