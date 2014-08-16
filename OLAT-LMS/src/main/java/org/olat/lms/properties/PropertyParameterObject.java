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
package org.olat.lms.properties;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * serves as parameter object to transfer input values related to PropertyManager API instances of classes are created via Builder pattern
 * 
 * <P>
 * Initial Date: 16.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class PropertyParameterObject {

    private final Identity identity;
    private final BusinessGroup group;
    private final OLATResourceable resourceable;
    private final String category;
    private final String name;
    private final Float floatValue;
    private final Long longValue;
    private final String stringValue;
    private final String textValue;
    private final Long resourceTypeId;
    private final String resourceTypeName;

    private PropertyParameterObject(Builder builder) {
        identity = builder.identity;
        group = builder.group;
        resourceable = builder.resourceable;
        category = builder.category;
        name = builder.name;
        floatValue = builder.floatValue;
        longValue = builder.longValue;
        stringValue = builder.stringValue;
        textValue = builder.textValue;
        resourceTypeId = builder.resourceTypeId;
        resourceTypeName = builder.resourceTypeName;

    }

    public Identity getIdentity() {
        return identity;
    }

    public BusinessGroup getGroup() {
        return group;
    }

    public OLATResourceable getResourceable() {
        return resourceable;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public static class Builder {

        private Identity identity;
        private BusinessGroup group;
        private OLATResourceable resourceable;
        private String category;
        private String name;
        private Float floatValue;
        private Long longValue;
        private String stringValue;
        private String textValue;
        private Long resourceTypeId;
        private String resourceTypeName;

        public Builder identity(Identity value) {
            identity = value;
            return this;
        }

        public Builder group(BusinessGroup value) {
            group = value;
            return this;
        }

        public Builder resourceable(OLATResourceable value) {
            resourceable = value;
            return this;
        }

        public Builder category(String value) {
            category = value;
            return this;
        }

        public Builder name(String value) {
            name = value;
            return this;
        }

        public Builder floatValue(Float value) {
            floatValue = value;
            return this;
        }

        public Builder longValue(Long value) {
            longValue = value;
            return this;
        }

        public Builder stringValue(String value) {
            stringValue = value;
            return this;
        }

        public Builder textValue(String value) {
            textValue = value;
            return this;
        }

        public Builder resourceTypeId(Long value) {
            resourceTypeId = value;
            return this;
        }

        public Builder resourceTypeName(String value) {
            resourceTypeName = value;
            return this;
        }

        public PropertyParameterObject build() {
            return new PropertyParameterObject(this);
        }

    }

}
