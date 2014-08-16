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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.user;

import java.util.Map;

import org.olat.data.commons.database.CreateInfo;
import org.olat.data.commons.database.Persistable;

/**
 * Description:
 * <p>
 * The user represents a real world user with the following elements:
 * <ul>
 * <li>profile: a list of user properties</li>
 * <li>preferences: a list of user settings</li>
 * </ul>
 * <p>
 * 
 * @author Florian Gnägi
 */
public interface User extends CreateInfo, Persistable {

    /**
     * Get the users prefereces object
     * 
     * @return The users preferences object
     */
    public Preferences getPreferences();

    /**
     * Set the users prefereces
     * 
     * @param prefs
     *            The users new preferences
     */
    public void setPreferences(Preferences prefs);

    /**
     * internal use only.
     * 
     * @param identEnvAttribs
     */
    public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs);

    /**
     * Return list of environment-attributes, used by activity-logging.
     * 
     * @return list of environment-attributes
     */
    public Map<String, String> getIdentityEnvironmentAttributes();

    /**
     * Returns raw value of a user-property. Use PropertyHandler to get a formatted property in presentation-layer.
     * 
     * @param propertyKey
     * @return raw user-property value
     */
    public String getRawUserProperty(String propertyKey);

}
