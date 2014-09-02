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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.exception.OLATRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * <P>
 * Initial Date: 21.06.2006 <br>
 * 
 * @author Felix Jost
 */
@Repository
public class PreferencesServiceImpl implements PreferencesService {

    static final String USER_PROPERTY_KEY = "v2guipreferences";
    private XStream xStream;
    @Autowired
    PropertyManager propertyManager;

    /**
     * [spring]
     */
    private PreferencesServiceImpl() {
        xStream = XStreamHelper.createXStreamInstance();
        xStream.alias(DbPrefs.ALIAS, DbPrefs.class);
        xStream.alias(DbPrefs.ALIAS_SHORT, DbPrefs.class);
    }

    @Override
    public Preferences getPreferencesFor(final Identity identity, final boolean useTransientPreferences) {
        if (useTransientPreferences) {
            return createEmptyDbPrefs(identity, true);
        } else {
            return getPreferencesFor(identity);
        }
    }

    /**
     * search x-stream serialization in properties table, create new if not found
     * 
     * @param identity
     * @return
     */
    private Preferences getPreferencesFor(final Identity identity) {
        final PropertyImpl guiProperty = propertyManager.findProperty(identity, null, null, null, USER_PROPERTY_KEY);
        if (guiProperty == null) {
            return createEmptyDbPrefs(identity, false);
        } else {
            return getPreferencesForProperty(identity, guiProperty);
        }
    }

    private Preferences getPreferencesForProperty(final Identity identity, final PropertyImpl guiProperty) {
        DbPrefs prefs;
        try {
            prefs = createDbPrefsFrom(identity, guiProperty, guiProperty.getTextValue());
        } catch (final Exception e) {
            throw new OLATRuntimeException("loading pref data failed:", e);
        }
        return prefs;
    }

    private DbPrefs createEmptyDbPrefs(final Identity identity, final boolean isTransient) {
        final DbPrefs prefs = new DbPrefs();
        prefs.init(propertyManager);
        prefs.setIdentity(identity);
        prefs.isTransient = isTransient;
        return prefs;
    }

    private DbPrefs createDbPrefsFrom(final Identity identity, final PropertyImpl guiProperty, final String textValue) {
        final DbPrefs prefs = (DbPrefs) xStream.fromXML(textValue);
        prefs.init(propertyManager);
        prefs.setIdentity(identity); // reset transient value
        prefs.dbProperty = guiProperty; // set property for later use
        return prefs;
    }

}
