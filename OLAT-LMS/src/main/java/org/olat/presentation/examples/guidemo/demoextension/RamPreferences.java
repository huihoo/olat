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
package org.olat.presentation.examples.guidemo.demoextension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.lms.preferences.Preferences;

/**
 * Description:<br>
 * TODO: Felix Class Description for RamPreferences
 * <P>
 * Initial Date: 13.10.2006 <br>
 * 
 * @author Felix
 */
public class RamPreferences implements Preferences {
    private final Map store = new HashMap();

    /**
	 */
    @Override
    public Object get(final Class attributedClass, final String key) {
        return store.get(getCompoundKey(attributedClass, key));
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
	 */
    @Override
    public void put(final Class attributedClass, final String key, final Object value) {
        store.put(getCompoundKey(attributedClass, key), value);
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
    public void save() {
        // nothing to do for ram

    }

    private String getCompoundKey(final Class attributedClass, final String key) {
        return attributedClass.getName() + ":" + key;
    }

    /**
	 */
    @Override
    public Object findPrefByKey(final String partOfKey) {
        for (final Iterator iterator = store.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            if (key.endsWith(partOfKey)) {
                return store.get(key);
            }
        }
        return null;
    }

}
