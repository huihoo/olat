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

package org.olat.lms.commons;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial Date: Dec 8, 2003
 * 
 * @author gnaegi
 */
public class ModuleConfiguration implements Serializable {
    /**
     * Configuration flag for the configuration version. The configuration version is stored using an Integer in the module configuration.
     */
    private static final String CONFIG_VERSION = "configversion";

    private final Map config;

    /** configuration key: repository sof key reference to qti file */
    public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repoSoftkey";

    public static final String CONFIG_KEY_REPOSITORY_REF = "reporef";

    /**
     * Default constructor.
     */
    public ModuleConfiguration() {
        config = new HashMap();
    }

    /**
     * Set a key to a value. Important: Value must be serailizable.
     * 
     * @param key
     * @param value
     */
    public void set(final String key, final Object value) {
        if (!(value instanceof Serializable) && value != null) {
            throw new RuntimeException("ModuleConfiguration only accepts cloneable values.");
        }
        config.put(key, value);
    }

    /**
     * Get value by key.
     * 
     * @param key
     * @return value or null if no such key
     */
    public Object get(final String key) {
        return config.get(key);
    }

    /**
     * Remove key/value.
     * 
     * @param key
     * @return value of removed key
     */
    public Object remove(final String key) {
        return config.remove(key);
    }

    /**
     * Copy all entrys from the given module configuration into this module configuration.
     * 
     * @param theConfig
     */
    public void putAll(final ModuleConfiguration theConfig) {
        config.putAll(theConfig.config);
    }

    /**
	 */
    @Override
    public String toString() {
        return config.entrySet() + ", " + super.toString();
    }

    /**
     * return a config value as a Boolean
     * 
     * @param config_key
     *            the key
     * @return null if no such key, or true if there is a entry under 'key' of type string with value "true", or false otherwise
     */
    public Boolean getBooleanEntry(final String config_key) {
        // boolean are stored either as null (no val yet), "true", or "false" (Strings)
        final String val = (String) get(config_key);
        if (val == null) {
            return null;
        }
        final Boolean set = val.equals("true") ? Boolean.TRUE : Boolean.FALSE;
        return set;
    }

    /**
     * @param config_key
     * @return false if the key is "false" or does not exist, true otherwise
     */
    public boolean getBooleanSafe(final String config_key) {
        final Boolean b = getBooleanEntry(config_key);
        return (b == null ? false : b.booleanValue());
    }

    public boolean getBooleanSafe(final String config_key, final boolean defaultvalue) {
        final Boolean b = getBooleanEntry(config_key);
        return (b == null ? defaultvalue : b.booleanValue());
    }

    public int getIntegerSafe(final String config_key, final int defaultValue) {
        // ints are stored as Integer
        final Integer val = (Integer) get(config_key);
        if (val == null) {
            return defaultValue;
        } else {
            return val.intValue();
        }
    }

    public void setIntValue(final String config_key, final int value) {
        set(config_key, new Integer(value));
    }

    /**
     * Set a string value to the config
     * 
     * @param config_key
     * @param value
     */
    public void setStringValue(final String config_key, final String value) {
        set(config_key, value);
    }

    /**
     * Get a string value from the config. Returns false when the config key does not exist
     * 
     * @param config_key
     * @return
     */
    public String getStringValue(final String config_key) {
        return (String) get(config_key);
    }

    /**
     * Get a string value from the config. Returns the defaultValue when the config key does not exist
     * 
     * @param config_key
     * @param defaultValue
     * @return
     */
    public String getStringValue(final String config_key, final String defaultValue) {
        String value = getStringValue(config_key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * @param config_key
     * @param value
     */
    public void setBooleanEntry(final String config_key, final boolean value) {
        // boolean are stored either as null (no val yet), "true", or "false" (Strings)
        final String val = (value ? "true" : "false");
        set(config_key, val);
    }

    /**
     * Get the version of this module configuration. The version specifies which configuration attributes are available for this course node. If no version has been set
     * so far version=1 will be returned
     * 
     * @return integer representing the version
     */
    public int getConfigurationVersion() {
        Integer version = (Integer) get(CONFIG_VERSION);
        if (version == null) {
            version = new Integer(1);
            set(CONFIG_VERSION, version);
        }
        return version.intValue();
    }

    /**
     * Set the configuration version to a specific value
     * 
     * @param version
     */
    public void setConfigurationVersion(final int version) {
        set(CONFIG_VERSION, new Integer(version));
    }
}
