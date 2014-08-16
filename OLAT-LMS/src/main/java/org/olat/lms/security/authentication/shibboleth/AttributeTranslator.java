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

package org.olat.lms.security.authentication.shibboleth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributeTranslator {

    // contains the mapping from the shib-attribute to the simplified shib attribute
    private Map<String, String> attributeTranslations;
    // contains predefined values that can be selected for a specific shib-attribute.
    // Note that as key the simplified shib attribute is used and not the original
    // attributes (see attributeTranslations map)
    private Map<String, List<String>> attributeSelectableValues;

    /**
     * [used by spring]
     */
    protected AttributeTranslator() {
        //
    }

    /**
     * [used by spring]
     */
    public void setAttributeTranslations(final Map<String, String> attributeTranslations) {
        this.attributeTranslations = attributeTranslations;
    }

    /**
     * [used by spring]
     */
    public void setAttributeSelectableValues(final Map<String, List<String>> attributeSelectableValues) {
        this.attributeSelectableValues = attributeSelectableValues;
    }

    public final Map<String, String> translateAttributesMap(final Map<String, String> attributesMap) {
        final Map<String, String> convertedMap = new HashMap<String, String>(attributesMap.size());
        final Iterator<String> keys = attributesMap.keySet().iterator();
        while (keys.hasNext()) {
            final String attribute = keys.next();
            final String translatedKey = translateAttribute(attribute);
            final String value = attributesMap.get(attribute);

            convertedMap.put(translatedKey, value);
        }
        return convertedMap;
    }

    /**
     * Translate Shibboleth Attributes according to configured attribute translations
     * 
     * @param inName
     * @return Translated attribute name.
     */
    public String translateAttribute(final String inName) {
        final String outName = attributeTranslations.get(inName);
        return outName != null ? outName : inName;
    }

    /**
     * Get all valid values for this attribute if such values are defined. When no values are defined, NULL will be returned.
     * 
     * @param attribute
     * @return
     */
    public String[] getSelectableValuesForAttribute(final String attribute) {
        final List<String> list = attributeSelectableValues.get(attribute);
        if (list != null) {
            // only some attributes have selectable values
            return list.toArray(new String[0]);
        }
        return new String[0];
    }

    /**
     * Get all attributes identifiers that can be translated
     * 
     * @return
     */
    public Set<String> getTranslateableAttributes() {
        return this.attributeTranslations.keySet();
    }

}
