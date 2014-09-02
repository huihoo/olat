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

package org.olat.data.properties;

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.ModifiedInfo;
import org.olat.data.commons.database.PersistentObject;
import org.olat.data.group.BusinessGroup;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: Mar 10, 2004
 * 
 * @author Mike Stock Comment:
 */
public class PropertyImpl extends PersistentObject implements ModifiedInfo {

    /** max length of a category */
    public static int CATEGORY_MAX_LENGHT = 33;

    private Identity identity;
    private BusinessGroup grp;
    private String resourceTypeName;
    private Long resourceTypeId;
    private String category;
    private String name;
    private Float floatValue;
    private Long longValue;
    private String stringValue;
    private String textValue;
    private Date lastModified;

    private static final int RESOURCETYPENAME_MAXLENGTH = 50;

    /**
	 * 
	 */
    PropertyImpl() {
        // notthing to do
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return string value
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * @return identity
     */
    public Identity getIdentity() {
        return identity;
    }

    /**
     * @return text value
     */
    public String getTextValue() {
        return textValue;
    }

    /**
     * @param string
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * @param string
     *            maximal 255 chars long; for longer strings use text value
     */
    public void setStringValue(final String string) {
        stringValue = string;
    }

    /**
     * @param identity
     */
    public void setIdentity(final Identity identity) {
        this.identity = identity;
    }

    /**
     * @param string
     *            for longer strings (saved as TEXT or BLOB in your database)
     */
    public void setTextValue(final String string) {
        textValue = string;
    }

    /**
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return Long
     */
    public Long getLongValue() {
        return longValue;
    }

    /**
     * @param longValue
     */
    public void setLongValue(final Long longValue) {
        this.longValue = longValue;
    }

    /**
     * @return float value
     */
    public Float getFloatValue() {
        return floatValue;
    }

    /**
     * @return group
     */
    public BusinessGroup getGrp() {
        return grp;
    }

    /**
     * @param string
     *            , maximal length 33 characters
     */
    public void setCategory(final String string) {
        if (string != null && string.length() > CATEGORY_MAX_LENGHT) {
            throw new RuntimeException("Property.category too long. Max is " + CATEGORY_MAX_LENGHT);
        }
        category = string;
    }

    /**
     * @param f
     */
    public void setFloatValue(final Float f) {
        floatValue = f;
    }

    /**
     * @param group
     */
    public void setGrp(final BusinessGroup group) {
        this.grp = group;
    }

    /**
     * @return resource type ID
     */
    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    /**
     * @return resource type name
     */
    public String getResourceTypeName() {
        return resourceTypeName;
    }

    /**
     * @param long1
     */
    public void setResourceTypeId(final Long long1) {
        resourceTypeId = long1;
    }

    /**
     * @param string
     */
    public void setResourceTypeName(final String string) {
        if (string != null && string.length() > RESOURCETYPENAME_MAXLENGTH) {
            throw new AssertException("resourcetypename of o_property too long");
        }
        resourceTypeName = string;
    }

    /**
	 */
    @Override
    public Date getLastModified() {
        return lastModified;
    }

    /**
	 */
    @Override
    public void setLastModified(final Date date) {
        this.lastModified = date;
    }

}
