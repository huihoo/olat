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

import java.util.HashMap;
import java.util.Map;

import org.olat.data.commons.database.PersistentObject;

/**
 * Desciption:
 * <p>
 * The user represents an known OLAT user. A user can log into OLAT and user the system.
 * <p>
 * The user properties are wrapped in UserPropertyHandler objects that wrap the logic needed to display and edit those properties in forms. Use the UserManager to access
 * those wrappers. For your convinience you can set and get user field values directly from this user implementation, you don't have to use the manager for this.
 * <p>
 * Note that setting any values on the user object does not persist anything. Whenever a field is modified use the UserManager to save the object.
 * <p>
 * 
 * @author Florian Gnägi
 */
public class UserImpl extends PersistentObject implements User {

    private Preferences preferences;

    // o_clusterOK by:cg add diInSync in ChangeProfileController and notifiy about change via event-bus
    // hibernate mapped properties, get stored to the DB
    private Map<String, String> properties;

    // the volatile attributes which are set during log in
    // but must be made available to the usertracking LoggingObject
    private Map<String, String> identEnvAttribs;

    /**
     * Default constructor (needed by hibernate). User has always a preferences and an address object, however the entries in the preferences and the address object are
     * still null.
     */
    protected UserImpl() {
        super();
        this.preferences = new PreferencesImpl();
    }

    UserImpl(final String firstName, final String lastName, final String eMail) {
        super();
        if (firstName != null) {
            getProperties().put(UserConstants.FIRSTNAME, firstName);
        }
        if (lastName != null) {
            getProperties().put(UserConstants.LASTNAME, lastName);
        }
        if (eMail != null) {
            getProperties().put(UserConstants.EMAIL, eMail);
        }
        this.preferences = new PreferencesImpl();
    }

    /**
     * Two users are equal if their key is equal.
     * 
     * @param obj
     * @return true if users are the same
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        // object must be UserImpl at this point
        final UserImpl user = (UserImpl) obj;
        return this.getKey().equals(user.getKey());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash;
        hash = 31 * hash + (null == this.getKey() ? 0 : this.getKey().hashCode());
        return hash;
    }

    /**
	 */
    @Override
    public Preferences getPreferences() {
        return this.preferences;
    }

    /**
	 */
    @Override
    public void setPreferences(final Preferences prefs) {
        this.preferences = prefs;
    }

    /**
     * @return String user info
     */
    @Override
    public String toString() {
        String quickinfo = "UserImpl(" + getKey() + ")";
        return quickinfo + "," + super.toString();
    }

    /**
     * Do not use this method to access the users properties. Use the get Property method insead and the methods offered in the user manager!
     * 
     * @return Map containing the raw properties data
     */
    public Map<String, String> getProperties() {
        if (properties == null) {
            setProperties(new HashMap<String, String>());
        }
        return properties;
    }

    /**
     * Hibernate setter
     * 
     * @param fields
     */
    private void setProperties(final Map<String, String> fields) {
        this.properties = fields;
    }

    /**
	 */
    @Override
    public void setIdentityEnvironmentAttributes(final Map<String, String> identEnvAttribs) {
        this.identEnvAttribs = identEnvAttribs;
    }

    public Map<String, String> getIdentityEnvironmentAttributes() {
        return identEnvAttribs;
    }

    public String getRawUserProperty(String propertyKey) {
        return getProperties().get(propertyKey);
    }
}
