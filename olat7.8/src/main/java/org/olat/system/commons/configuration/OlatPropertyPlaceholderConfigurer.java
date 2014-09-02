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

import java.util.Properties;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Custom implementation of the spring placeholder which also includes the GUI managed properties in the overwrite chain when resolving a property
 * 
 * uses the following files where the later overwrites the former olat.properties olat.local.properties maven.build.properties
 * olatdata/system/configuration/GUIManaged.properties
 * <P>
 * 
 * Initial Date: 30.05.2011 <br>
 * 
 * @author guido
 */
public class OlatPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements Initializable {

    private static final Logger log = LoggerHelper.getLogger();

    private SystemPropertiesLoader propLoader;

    /**
     * [spring]
     */
    private OlatPropertyPlaceholderConfigurer() {
        //
    }

    /**
	 */
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        super.resolvePlaceholder(placeholder, props);
        if (propLoader.containsProperty(placeholder)) {
            props.setProperty(placeholder, propLoader.getProperty(placeholder));
            return propLoader.getProperty(placeholder);
        }
        return props.getProperty(placeholder);
    }

    /**
     * @see org.olat.system.commons.configuration.Initializable#init()
     */
    @Override
    public void init() {
        propLoader = new SystemPropertiesLoader();
        propLoader.init();

    }

}
