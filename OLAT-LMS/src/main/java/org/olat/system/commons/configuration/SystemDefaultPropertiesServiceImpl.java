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

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * TODO: Class Description for SystemDefaultPropertiesServiceImpl
 * 
 * <P>
 * Initial Date: 24.05.2011 <br>
 * 
 * @author guido
 */

@Service
public class SystemDefaultPropertiesServiceImpl implements SystemPropertiesService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private SystemPropertiesLoader propLoader;

    @Autowired
    @Qualifier("main")
    private ManagedPropertiesMessageCoordinator messageCoordinator;

    /**
	 * 
	 */
    protected SystemDefaultPropertiesServiceImpl() {
        //
    }

    @Override
    public void setProperty(PropertyLocator propertyName, String value) {
        String checkFirst = propLoader.getProperty(propertyName.getPropertyName());
        if (checkFirst != null) {
            propLoader.setProperty(propertyName.getPropertyName(), value);
            // sends message to all nodes in the cluster
            messageCoordinator.setProperty(propertyName.getPropertyName(), value);
        } else {
            // GS: For Coco 2011, remove after...
            // Translator trans = new PackageTranslator("/package/bla/", new Locale("de"));
            log.warn("Cannot set a value to a non existing property!", null);
        }
    }

    @Override
    public String getStringProperty(PropertyLocator propertyName) {
        String value = propLoader.getProperty(propertyName.getPropertyName());
        if (value != null) {
            return value;
        } else {
            log.error("No value found for string property::" + propertyName.getPropertyName() + ", using value=\"\" instead", null);
            return "";
        }
    }

    @Override
    public int getIntProperty(PropertyLocator propertyName) {
        String value = propLoader.getProperty(propertyName.getPropertyName());
        if (value == null)
            log.warn("No value found for int property::" + propertyName.getPropertyName() + ", using value=0 instead", null);
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            log.error("Cannot parse to integer property::" + propertyName.getPropertyName() + ", value=" + value, null);
        }
        return 0;
    }

    @Override
    public boolean getBooleanProperty(PropertyLocator propertyName) {
        String value = propLoader.getProperty(propertyName.getPropertyName());
        boolean returnValue = Boolean.valueOf(value.trim());
        // 3) Not even a value found in the fallback, return false
        if (!returnValue)
            log.error("No value found for boolean property::" + propertyName.getPropertyName() + ", using value=false instead", null);
        return false;
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getAvaliableValues(org.olat.system.commons.configuration.PropertyLocator)
     */
    @Override
    public List<String> getAvaliableValues(PropertyLocator propertyName) {
        return null;
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getSystemDefaultValue(org.olat.system.commons.configuration.PropertyLocator)
     */
    @Override
    public String getSystemDefaultValue(PropertyLocator propertyName) {
        return null;
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getDefaultProperties()
     */
    @Override
    public List<OLATProperty> getDefaultProperties() {
        return propLoader.getMergedProperties();
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getOverwriteProperties()
     */
    @Override
    public List<OLATProperty> getOverwriteProperties() {
        return propLoader.getOverwriteProperties();
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getOlatdataOverwriteProperties()
     */
    @Override
    public List<OLATProperty> getOlatdataOverwriteProperties() {
        return propLoader.getOlatdataOverwriteProperties();
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getMavenProperties()
     */
    @Override
    public List<OLATProperty> getMavenProperties() {
        return propLoader.getMavenProperties();
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getOverwritePropertiesURL()
     */
    @Override
    public String getOverwritePropertiesURL() {
        return propLoader.getOverwritePropertiesUrl();
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getOlatdataOverwritePropertiesURL()
     */
    @Override
    public String getOlatdataOverwritePropertiesURL() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.olat.system.commons.configuration.SystemPropertiesService#getMavenPropertiesURL()
     */
    @Override
    public String getMavenPropertiesURL() {
        // TODO Auto-generated method stub
        return null;
    }

}
