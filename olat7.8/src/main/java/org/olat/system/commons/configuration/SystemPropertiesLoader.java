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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Class responsible for loading and storing various OLAT properties. Following property locations are considered:
 * <p>
 * <table>
 * <tr>
 * <th>Precedence</th>
 * <th align="left">Ressource</th></th>
 * <tr>
 * <td align="center">1</td>
 * <td>olat.properties</td>
 * </tr>
 * <tr>
 * <td align="center">2</td>
 * <td>olat.local.properties</td>
 * </tr>
 * <tr>
 * <td align="center">3</td>
 * <td>maven.build.properties</td>
 * </tr>
 * <tr>
 * <td align="center">4</td>
 * <td>${userdata.dir}/system/configuration/GUIManaged.properties</td>
 * </tr>
 * </table>
 * <p>
 * Properties defined in ressources with higher precedence overwrite those with lower precedence!
 * 
 * <P>
 * Initial Date: 26.05.2011 <br>
 * 
 * @author guido
 * @author obuehler, oliver.buehler@agility-informatik.ch, Agility Informatik GmbH
 */

@Component
public class SystemPropertiesLoader implements Initializable {

    public static final String USERDATA_DIR_DEFAULT = System.getProperty("java.io.tmpdir") + File.separator + "olatdata";

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VALUES = ".values";
    private static final String COMMENT = ".comment";

    // protected just for testing
    protected static final String SYSTEM_CONFIGURATION_DIR = "/system/configuration/";
    protected static final String SYSTEM_CONFIGURATION_FILE = "GUIManaged.properties";

    private final Properties defaultProperties = new Properties(); // olat.properties
    private final Properties overwriteProperties = new Properties(); // olat.local.properties
    private final Properties mavenProperties = new Properties(); // maven.build.properties
    private Properties systemConfigurationProperties; // olatdata/system/configuration...

    private final Map<String, OLATProperty> overwritePropertiesSorted = new TreeMap<String, OLATProperty>();
    private final Map<String, OLATProperty> systemConfigurationPropertiesSorted = new TreeMap<String, OLATProperty>();
    private final Map<String, OLATProperty> mavenPropertiesSorted = new TreeMap<String, OLATProperty>();

    private final Map<String, OLATProperty> mergedProps = new TreeMap<String, OLATProperty>();

    private SystemPropertiesPersister SystemPropertyPersister;
    private ClassPathResource overwritePropertiesRes;

    private String olatdataPath = USERDATA_DIR_DEFAULT;

    /**
     * [spring]
     */
    protected SystemPropertiesLoader() {
        super();
    }

    /**
     * use this constr. if you like to set the path to the olat data manually like in a unit test
     */
    protected SystemPropertiesLoader(String olatdataPath) {
        this.olatdataPath = olatdataPath;
    }

    @PostConstruct
    @Override
    public void init() {
        Resource olatDefaultPropertiesRes = new ClassPathResource("/serviceconfig/olat.properties");
        overwritePropertiesRes = new ClassPathResource("olat.local.properties");
        Resource mavenPropertiesRes = new ClassPathResource("maven.build.properties");

        try {
            defaultProperties.load(olatDefaultPropertiesRes.getInputStream());
            overwriteProperties.load(overwritePropertiesRes.getInputStream());
            mavenProperties.load(mavenPropertiesRes.getInputStream());
        } catch (IOException e) {
            log.error("Could not load properties files from classpath", e);
        }

        if (olatdataPath == null) {
            checkForNonDefaultDataDir();
        }

        if (olatdataPath.equals(USERDATA_DIR_DEFAULT)) {
            final String olatDataPathLocal = overwriteProperties.getProperty(PropertyLocator.USERDATA_DIR.getPropertyName());
            if (StringHelper.containsNonWhitespace(olatDataPathLocal)) {
                olatdataPath = olatDataPathLocal;
            } else {
                final String olatDataPathDefault = defaultProperties.getProperty(PropertyLocator.USERDATA_DIR.getPropertyName());
                if (StringHelper.containsNonWhitespace(olatDataPathDefault)) {
                    olatdataPath = olatDataPathLocal;
                }
            }
        }

        // Load configured system properties file
        SystemPropertyPersister = new SystemPropertiesPersister(new File(olatdataPath + File.separator + SYSTEM_CONFIGURATION_DIR, SYSTEM_CONFIGURATION_FILE));
        systemConfigurationProperties = SystemPropertyPersister.loadProperties();

        analyzeProperties();
    }

    private void checkForNonDefaultDataDir() {
        // check where we have to look for the gui manged properties file
        olatdataPath = defaultProperties.getProperty(PropertyLocator.USERDATA_DIR.getPropertyName());
        String temp = overwriteProperties.getProperty(PropertyLocator.USERDATA_DIR.getPropertyName());
        if (temp != null && StringHelper.containsNonWhitespace(temp)) {
            olatdataPath = temp;
        }
    }

    /**
     * 
     * @return
     */
    protected List<OLATProperty> getMergedProperties() {
        return new ArrayList<OLATProperty>(mergedProps.values());
    }

    protected Map<String, OLATProperty> getMergedPropertiesMap() {
        return mergedProps;
    }

    protected List<OLATProperty> getOverwriteProperties() {
        return new ArrayList<OLATProperty>(overwritePropertiesSorted.values());
    }

    protected List<OLATProperty> getOlatdataOverwriteProperties() {
        return new ArrayList<OLATProperty>(systemConfigurationPropertiesSorted.values());
    }

    protected List<OLATProperty> getMavenProperties() {
        return new ArrayList<OLATProperty>(mavenPropertiesSorted.values());
    }

    protected String getOverwritePropertiesUrl() {
        try {
            return overwritePropertiesRes.getURL().toString();
        } catch (IOException e) {
            return "overwrite properties URL not found: " + e.getMessage();
        }
    }

    private void analyzeProperties() {
        // read default properties and set overwritten values
        for (Object key : defaultProperties.keySet()) {
            String keyValue = (String) key;
            OLATProperty prop = new OLATProperty(keyValue, defaultProperties.getProperty(keyValue));
            if (overwriteProperties.containsKey(keyValue)) {
                prop.setOverwriteValue(overwriteProperties.getProperty(keyValue));
            }
            if (systemConfigurationProperties.containsKey(keyValue)) {
                prop.setOverwriteValue(systemConfigurationProperties.getProperty(keyValue));
            }
            if (mavenProperties.containsKey(keyValue)) {
                prop.setOverwriteValue(mavenProperties.getProperty(keyValue));
            }
            // load comments if available
            if (defaultProperties.getProperty(keyValue + COMMENT) != null) {
                prop.setComment(defaultProperties.getProperty(keyValue + COMMENT));
            }
            // load available values if there are
            if (defaultProperties.getProperty(keyValue + VALUES) != null) {
                prop.setAvailableValues(defaultProperties.getProperty(keyValue + VALUES));
            }
            // TODO constant extraction
            if (!keyValue.endsWith(COMMENT) && !keyValue.endsWith(VALUES)) {
                mergedProps.put(keyValue, prop);
            }
        }

        /**
         * load all other properties into TreeMaps in order to for displaying and managing with GUI
         */

        // load overwrite properties
        for (Object key : overwriteProperties.keySet()) {
            String keyValue = (String) key;
            OLATProperty prop = new OLATProperty(keyValue.trim(), overwriteProperties.getProperty(keyValue).trim());
            overwritePropertiesSorted.put(keyValue, prop);
        }

        // load system configuration properties
        for (Object key : systemConfigurationProperties.keySet()) {
            String keyValue = (String) key;
            OLATProperty prop = new OLATProperty(keyValue.trim(), systemConfigurationProperties.getProperty(keyValue).trim());
            systemConfigurationPropertiesSorted.put(keyValue, prop);
        }

        // load maven properties
        for (Object key : mavenProperties.keySet()) {
            String keyValue = (String) key;
            OLATProperty prop = new OLATProperty(keyValue.trim(), mavenProperties.getProperty(keyValue).trim());
            mavenPropertiesSorted.put(keyValue, prop);
        }

    }

    /**
     * access the merged properties (reflects the full overwrite chain)
     * 
     * @param key
     * @return
     */
    protected String getProperty(String key) {
        OLATProperty prop = mergedProps.get(key);
        if (prop == null) {
            return null;
        }
        return prop.getValue();
    }

    protected boolean containsProperty(String key) {
        return mergedProps.containsKey(key);
    }

    /**
     * 
     * @param key
     * @param value
     */
    protected void setProperty(String key, String value) {
        OLATProperty prop = mergedProps.get(key);
        if (prop != null) {
            prop.setOverwriteValue(value);
            SystemPropertyPersister.saveProperty(key, value);
        } else {
            log.warn("Trying to overwrite a property which does not exist: key: value:", null);
        }
    }

}
