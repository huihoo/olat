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
package org.olat.system.commons.configuration;

import java.util.List;

/**
 * TODO: Class Description for SystemPropertiesService
 * 
 * <P>
 * Initial Date: 24.05.2011 <br>
 * 
 * @author guido
 */
public interface SystemPropertiesService {

    /**
     * sets a property and if available in the defaults overwrites, stores and propagates the change to the cluster
     * 
     * @param propertyName
     * @param value
     */
    public void setProperty(PropertyLocator propertyName, String value);

    /**
     * 
     * @param propertyName
     * @return
     */
    public String getStringProperty(PropertyLocator propertyName);

    /**
     * 
     * @param propertyName
     * @return
     */
    public int getIntProperty(PropertyLocator propertyName);

    /**
     * 
     * @param propertyName
     * @return
     */
    public boolean getBooleanProperty(PropertyLocator propertyName);

    /**
     * 
     * @param propertyName
     * @return
     */
    public List<String> getAvaliableValues(PropertyLocator propertyName);

    /**
     * 
     * @param propertyName
     * @return
     */
    public String getSystemDefaultValue(PropertyLocator propertyName);

    /**
     * @return all default properties as listed in olat.properties
     */
    public List<OLATProperty> getDefaultProperties();

    /**
     * @return all properties that overwrite the default props as listed in olat.local.properties
     */
    public List<OLATProperty> getOverwriteProperties();

    /**
     * @return all properties that overwrite the already overwritten props as listed in olatdata/system/configuration/overwritePersisted.properties
     */
    public List<OLATProperty> getOlatdataOverwriteProperties();

    /**
     * @return
     */
    public List<OLATProperty> getMavenProperties();

    /**
     * @return
     */
    public String getOverwritePropertiesURL();

    /**
     * @return
     */
    public String getOlatdataOverwritePropertiesURL();

    /**
     * @return
     */
    public String getMavenPropertiesURL();

}
