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

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: Mar 10, 2004
 * 
 * @author Mike Stock Comment:
 */
@Deprecated
public class NarrowedPropertyManager {

    private final OLATResourceable resourceable;
    private final PropertyManager pm;

    /**
	 * 
	 */
    private NarrowedPropertyManager(final OLATResourceable resourceable) {
        this.resourceable = resourceable;
        pm = PropertyManager.getInstance();
    }

    /**
     * Get an instance of a narrowed property manager for this olat resource
     * 
     * @param resourceable
     *            The resource
     * @return The narrowed property manager
     */
    public static NarrowedPropertyManager getInstance(final OLATResourceable resourceable) {
        if (resourceable == null) {
            throw new AssertException("resourceable cannot be null");
        }
        return new NarrowedPropertyManager(resourceable);
    }

    /**
     * Create a property
     * 
     * @param identity
     * @param group
     * @param category
     * @param name
     * @param floatValue
     * @param longValue
     * @param stringValue
     * @param textValue
     * @return The created property
     */
    public PropertyImpl createPropertyInstance(final Identity identity, final BusinessGroup group, final String category, final String name, final Float floatValue,
            final Long longValue, final String stringValue, final String textValue) {
        return pm.createPropertyInstance(identity, group, resourceable, category, name, floatValue, longValue, stringValue, textValue);
    }

    /**
     * Delete a property from the database
     * 
     * @param p
     */
    public void deleteProperty(final PropertyImpl p) {
        pm.deleteProperty(p);
    }

    /**
     * Save a property in the database
     * 
     * @param p
     */
    public void saveProperty(final PropertyImpl p) {
        pm.saveProperty(p);
    }

    /**
     * Save or update a property
     * 
     * @param p
     */
    public void updateProperty(final PropertyImpl p) {
        pm.updateProperty(p);
    }

    /**
     * Generic method. Returns a list of Property objects. This is an inexact match i.e. parameters with null values will not be included in the query.
     * 
     * @param identity
     * @param grp
     * @param category
     * @param name
     * @return a list of Property objects
     */
    public List listProperties(final Identity identity, final BusinessGroup grp, final String category, final String name) {
        return pm.listProperties(identity, grp, resourceable, category, name);
    }

    /**
     * Generic find method. Returns a list of Property objects. This is an exact match i.e. if you pass-on null values, null values will be included in the query.
     * 
     * @param identity
     * @param grp
     * @param category
     * @param name
     * @return a list of Property objects
     */
    public List findProperties(final Identity identity, final BusinessGroup grp, final String category, final String name) {
        return pm.findProperties(identity, grp, resourceable, category, name);
    }

    /**
     * Generic find method.
     * 
     * @param identity
     * @param grp
     * @param category
     * @param name
     * @return The property or null if no property found
     * @throws AssertException
     *             if more than one property matches.
     */
    public PropertyImpl findProperty(final Identity identity, final BusinessGroup grp, final String category, final String name) {
        return pm.findProperty(identity, grp, resourceable, category, name);
    }

    /**
     * deletes all properties of this resourceable
     */
    public void deleteAllProperties() {
        // delete all properties belonging to this forum
        pm.deleteProperties(null, null, resourceable, null, null);
    }

    /**
     * Delete properties. IMPORTANT: if an argument is null, then it will be not considered in the delete statement, which means not only the record having a "null" value
     * will be deleted, but all. At least one of the arguments must be not null, otherwhise an assert exception will be thrown. If you want to delete all properties of
     * this ressource, then use the deleteAllProperties() method.
     * 
     * @param identity
     * @param group
     * @param category
     * @param name
     */
    public void deleteProperties(final Identity identity, final BusinessGroup group, final String category, final String name) {
        if (identity == null && group == null && category == null && name == null) {
            throw new AssertException("deleteProperties musst have at least one non-null parameter. Seems to be a programm bug");
        }
        pm.deleteProperties(identity, group, resourceable, category, name);
    }

}
