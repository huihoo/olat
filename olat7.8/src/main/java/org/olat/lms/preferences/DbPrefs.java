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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.lms.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.commons.UsedByXstream;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * <P>
 * Initial Date: 21.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class DbPrefs implements Preferences, UsedByXstream {

    protected static final String ALIAS = "org.olat.preferences.DbPrefs";
    protected static final String ALIAS_SHORT = "DbPrefs";
    // keys: prefs-keys; values: any Prefs-Objects
    private final Map<String, Object> prefstore = new HashMap<String, Object>();
    // simply to indicate preferences version (serialized in the xstream, do not remove!)
    private final int version = 1;
    private transient Identity owner;
    // true: don't save to disk, only in ram
    transient boolean isTransient = false;
    transient PropertyImpl dbProperty = null;
    private transient XStream xStream;
    private transient PropertyManager propertyManager;
    private boolean isInitialized = false;

    protected DbPrefs() {
        //
    }

    public void init(PropertyManager propertyManager) {
        this.propertyManager = propertyManager;
        // must have a default constructor for serialization!
        xStream = XStreamHelper.createXStreamInstance();
        xStream.alias(ALIAS, DbPrefs.class);
        xStream.alias(ALIAS_SHORT, DbPrefs.class);
        isInitialized = true;
    }

    @Override
    public void save() {
        if (!isTransient && isInitialized) {
            // generate x-stream serialization of this object
            final String props = xStream.toXML(this);
            if (this.dbProperty == null) {
                // save as new property
                this.dbProperty = propertyManager.createPropertyInstance(owner, null, null, null, PreferencesServiceImpl.USER_PROPERTY_KEY, null, null, null, props);
                propertyManager.saveProperty(this.dbProperty);
            } else {
                // update existing property
                this.dbProperty.setTextValue(props);
                propertyManager.updateProperty(this.dbProperty);
            }
        }
    }

    /**
     * @param attributedClass
     * @param key
     * @return Object
     */
    @Override
    public Object get(final Class attributedClass, final String key) {
        return prefstore.get(attributedClass.getSimpleName() + "::" + key);
    }

    /**
	 */
    @Override
    public Object get(final Class attributedClass, final String key, final Object defaultValue) {
        final Object value = get(attributedClass, key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * @param attributedClass
     * @param key
     * @param value
     *            TODO: make value not object, but basetypemap or such?
     */
    @Override
    public void put(final Class attributedClass, final String key, final Object value) {
        prefstore.put(attributedClass.getSimpleName() + "::" + key, value);
    }

    /**
     * @param identity
     */
    void setIdentity(final Identity identity) {
        this.owner = identity;
    }

    /**
	 */
    @Override
    public void putAndSave(final Class attributedClass, final String key, final Object value) {
        put(attributedClass, key, value);
        save();
    }

    /**
	 */
    @Override
    public Object findPrefByKey(final String partOfKey) {
        for (final Iterator iterator = prefstore.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            if (key.endsWith(partOfKey)) {
                return prefstore.get(key);
            }
        }
        return null;
    }

}
