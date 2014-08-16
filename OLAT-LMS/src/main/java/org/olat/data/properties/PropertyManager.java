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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.properties.PropertyManagerEBL;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: Mar 10, 2004
 * 
 * @author Mike Stock Comment:
 */
@Repository
public class PropertyManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static PropertyManager INSTANCE;
    @Autowired
    private DB database;

    /**
     * [used by spring]
     */
    private PropertyManager() {
        INSTANCE = this;
    }

    /**
     * @return Singleton.
     */
    @Deprecated
    public static PropertyManager getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a new Property
     * 
     * @param identity
     * @param group
     * @param olatResourceable
     * @param category
     * @param name
     * @param floatValue
     * @param longValue
     * @param stringValue
     * @param textValue
     * @return property instance.
     */
    public PropertyImpl createPropertyInstance(final Identity identity, final BusinessGroup group, final OLATResourceable olatResourceable, final String category,
            final String name, final Float floatValue, final Long longValue, final String stringValue, final String textValue) {

        final PropertyImpl p = new PropertyImpl();
        p.setIdentity(identity);
        p.setGrp(group);
        if (olatResourceable != null) {
            p.setResourceTypeName(olatResourceable.getResourceableTypeName());
            p.setResourceTypeId(olatResourceable.getResourceableId());
        }
        p.setCategory(category);
        p.setName(name);
        p.setFloatValue(floatValue);
        p.setLongValue(longValue);
        p.setStringValue(stringValue);
        p.setTextValue(textValue);
        return p;
    }

    /**
     * Create a user proprety. Grp, course and node a re set to null.
     * 
     * @param identity
     * @param category
     * @param name
     * @param floatValue
     * @param longValue
     * @param stringValue
     * @param textValue
     * @return property instance limited to a specific user.
     */
    public PropertyImpl createUserPropertyInstance(final Identity identity, final String category, final String name, final Float floatValue, final Long longValue,
            final String stringValue, final String textValue) {
        return createPropertyInstance(identity, null, null, category, name, floatValue, longValue, stringValue, textValue);
    }

    /**
     * Deletes a property on the database
     * 
     * @param p
     *            the property
     */
    public void deleteProperty(final PropertyImpl p) {
        database.deleteObject(p);
    }

    /**
     * Save a property
     * 
     * @param p
     */
    public void saveProperty(final PropertyImpl p) {
        p.setLastModified(new Date());
        database.saveObject(p);
    }

    /**
     * Update a property
     * 
     * @param p
     */
    public void updateProperty(final PropertyImpl p) {
        p.setLastModified(new Date());
        database.updateObject(p);
    }

    /**
     * Find a user property.
     * 
     * @param identity
     * @param category
     * @param name
     * @return Found property or null if no match.
     */
    public PropertyImpl findUserProperty(final Identity identity, final String category, final String name) {

        final List props = database
                .find("from v in class org.olat.data.properties.PropertyImpl where v.identity=? and v.category=? and v.name=? and v.grp is null and v.resourceTypeName is null and v.resourceTypeId is null",
                        new Object[] { identity.getKey(), category, name }, new Type[] { Hibernate.LONG, Hibernate.STRING, Hibernate.STRING });

        if (props == null || props.size() != 1) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find property: " + name);
            }
            return null;
        }

        return (PropertyImpl) props.get(0);
    }

    /**
     * Generic method. Returns a list of Property objects. This is an inexact match i.e. parameters with null values will not be included in the query.
     * 
     * @param identity
     * @param grp
     * @param resourceable
     * @param category
     * @param name
     * @return a list of Property objects
     */
    public List<PropertyImpl> listProperties(final Identity identity, final BusinessGroup grp, final OLATResourceable resourceable, final String category,
            final String name) {
        if (resourceable == null) {
            return listProperties(identity, grp, null, null, category, name);
        } else {
            return listProperties(identity, grp, resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name);
        }
    }

    /**
     * Only to use if no OLATResourceable Object is available.
     * 
     * @param identity
     * @param grp
     * @param resourceTypeName
     * @param resourceTypeId
     * @param category
     * @param name
     * @return a list of Property objects
     */
    public List listProperties(final Identity identity, final BusinessGroup grp, final String resourceTypeName, final Long resourceTypeId, final String category,
            final String name) {
        final StringBuilder query = new StringBuilder();
        final ArrayList objs = new ArrayList();
        final ArrayList types = new ArrayList();
        query.append("from v in class org.olat.data.properties.PropertyImpl where ");

        boolean previousParam = false;
        if (identity != null) {
            query.append("v.identity = ?");
            objs.add(identity.getKey());
            types.add(Hibernate.LONG);
            previousParam = true;
        }

        if (grp != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.grp = ?");
            objs.add(grp.getKey());
            types.add(Hibernate.LONG);
            previousParam = true;
        }

        if (resourceTypeName != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.resourceTypeName = ?");
            objs.add(resourceTypeName);
            types.add(Hibernate.STRING);
            previousParam = true;
        }

        if (resourceTypeId != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append(" v.resourceTypeId = ?");
            objs.add(resourceTypeId);
            types.add(Hibernate.LONG);
            previousParam = true;
        }

        if (category != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.category = ?");
            objs.add(category);
            types.add(Hibernate.STRING);
            previousParam = true;
        }

        if (name != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.name = ?");
            objs.add(name);
            types.add(Hibernate.STRING);
        }

        return database.find(query.toString(), objs.toArray(), (Type[]) types.toArray(new Type[types.size()]));

    }

    /**
     * deletes properties. IMPORTANT: if an argument is null, then it will be not considered in the delete statement, which means not only the record having a "null"
     * value will be deleted, but all.
     * 
     * @param identity
     * @param grp
     * @param resourceable
     * @param category
     * @param name
     */
    public void deleteProperties(final Identity identity, final BusinessGroup grp, final OLATResourceable resourceable, final String category, final String name) {
        final StringBuilder query = new StringBuilder();
        final ArrayList objs = new ArrayList();
        final ArrayList types = new ArrayList();
        query.append("from v in class org.olat.data.properties.PropertyImpl where ");

        boolean previousParam = false;
        if (identity != null) {
            query.append("v.identity = ?");
            objs.add(identity.getKey());
            types.add(Hibernate.LONG);
            previousParam = true;
        }

        if (grp != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.grp = ?");
            objs.add(grp.getKey());
            types.add(Hibernate.LONG);
            previousParam = true;
        }

        if (resourceable != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.resourceTypeName = ?");
            objs.add(resourceable.getResourceableTypeName());
            types.add(Hibernate.STRING);

            query.append(" and v.resourceTypeId");
            if (resourceable.getResourceableId() == null) {
                query.append(" is null");
            } else {
                query.append(" = ?");
                objs.add(resourceable.getResourceableId());
                types.add(Hibernate.LONG);
            }
            previousParam = true;
        }

        if (category != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.category = ?");
            objs.add(category);
            types.add(Hibernate.STRING);
            previousParam = true;
        }

        if (name != null) {
            if (previousParam) {
                query.append(" and ");
            }
            query.append("v.name = ?");
            objs.add(name);
            types.add(Hibernate.STRING);
        }

        database.delete(query.toString(), objs.toArray(), (Type[]) types.toArray(new Type[types.size()]));
    }

    /**
     * Generic find method. Returns a list of Property objects. This is an exact match i.e. if you pass-on null values, null values will be included in the query.
     * 
     * @param identity
     * @param grp
     * @param resourceable
     * @param category
     * @param name
     * @return a list of Property objects.
     */
    public List findProperties(final Identity identity, final BusinessGroup grp, final OLATResourceable resourceable, final String category, final String name) {
        if (resourceable == null) {
            return findProperties(identity, grp, null, null, category, name);
        } else {
            return findProperties(identity, grp, resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name);
        }
    }

    /**
     * Only to use if no OLATResourceable Object is available.
     * 
     * @param identity
     * @param grp
     * @param resourceTypeName
     * @param resourceTypeId
     * @param category
     * @param name
     * @return List of properties
     */
    public List findProperties(final Identity identity, final BusinessGroup grp, final String resourceTypeName, final Long resourceTypeId, final String category,
            final String name) {
        final StringBuilder query = new StringBuilder();
        final ArrayList objs = new ArrayList();
        final ArrayList types = new ArrayList();
        query.append("from v in class org.olat.data.properties.PropertyImpl where ");

        if (identity != null) {
            query.append("v.identity = ?");
            objs.add(identity.getKey());
            types.add(Hibernate.LONG);
        } else {
            query.append("v.identity is null");
        }

        query.append(" and ");
        if (grp != null) {
            query.append("v.grp = ?");
            objs.add(grp.getKey());
            types.add(Hibernate.LONG);
        } else {
            query.append("v.grp is null");
        }

        query.append(" and ");
        if (resourceTypeName != null) {
            query.append("v.resourceTypeName = ?");
            objs.add(resourceTypeName);
            types.add(Hibernate.STRING);
        } else {
            query.append("v.resourceTypeName is null");
        }

        query.append(" and ");
        if (resourceTypeId != null) {
            query.append("v.resourceTypeId = ?");
            objs.add(resourceTypeId);
            types.add(Hibernate.LONG);
        } else {
            query.append("v.resourceTypeId is null");
        }

        query.append(" and ");
        if (category != null) {
            query.append("v.category = ?");
            objs.add(category);
            types.add(Hibernate.STRING);
        } else {
            query.append("v.category is null");
        }

        query.append(" and ");
        if (name != null) {
            query.append("v.name = ?");
            objs.add(name);
            types.add(Hibernate.STRING);
        } else {
            query.append("v.name is null");
        }

        return database.find(query.toString(), objs.toArray(), (Type[]) types.toArray(new Type[types.size()]));
    }

    /**
     * Get a list of identities that have properties given the restricting values
     * 
     * @param resourceable
     *            Search restricted to this resourcable
     * @param category
     *            Search restricted to this property category
     * @param name
     *            Search restricted to this property name
     * @param matchNullValues
     *            true: null values in the above restricting values will be added as null values to the query; false: null values in the restricting values will be
     *            ignored in the query
     * @return List of identities
     */
    public List findIdentitiesWithProperty(final OLATResourceable resourceable, final String category, final String name, final boolean matchNullValues) {
        if (resourceable == null) {
            return findIdentitiesWithProperty(null, null, category, name, matchNullValues);
        } else {
            return findIdentitiesWithProperty(resourceable.getResourceableTypeName(), resourceable.getResourceableId(), category, name, matchNullValues);
        }
    }

    /**
     * Get a list of identities that have properties given the restricting values
     * 
     * @param resourceTypeName
     *            Search restricted to this resource type name
     * @param resourceTypeId
     *            Search restricted to this resource type id
     * @param category
     *            Search restricted to this property category
     * @param name
     *            Search restricted to this property name
     * @param matchNullValues
     *            true: null values in the above restricting values will be added as null values to the query; false: null values in the restricting values will be
     *            ignored in the query
     * @return List of identities
     */
    public List findIdentitiesWithProperty(final String resourceTypeName, final Long resourceTypeId, final String category, final String name,
            final boolean matchNullValues) {
        final StringBuilder query = new StringBuilder();
        final ArrayList objs = new ArrayList();
        final ArrayList types = new ArrayList();
        query.append("select distinct i from org.olat.data.basesecurity.IdentityImpl as i");
        query.append(", org.olat.data.properties.PropertyImpl as p");
        query.append(" where p.identity = i");

        if (resourceTypeName != null) {
            query.append(" and ");
            query.append("p.resourceTypeName = ?");
            objs.add(resourceTypeName);
            types.add(Hibernate.STRING);
        } else if (matchNullValues) {
            query.append(" and p.resourceTypeName is null");
        }

        if (resourceTypeId != null) {
            query.append(" and ");
            query.append("p.resourceTypeId = ?");
            objs.add(resourceTypeId);
            types.add(Hibernate.LONG);
        } else if (matchNullValues) {
            query.append(" and p.resourceTypeId is null");
        }

        if (category != null) {
            query.append(" and ");
            query.append("p.category = ?");
            objs.add(category);
            types.add(Hibernate.STRING);
        } else if (matchNullValues) {
            query.append(" and p.category is null");
        }

        if (name != null) {
            query.append(" and ");
            query.append("p.name = ?");
            objs.add(name);
            types.add(Hibernate.STRING);
        } else if (matchNullValues) {
            query.append(" and p.name is null");
        }

        return database.find(query.toString(), objs.toArray(), (Type[]) types.toArray(new Type[types.size()]));
    }

    /**
     * Generic find method.
     * 
     * @param identity
     * @param grp
     * @param resourceable
     * @param category
     * @param name
     * @return Property if found or null
     * @throws AssertException
     *             if more than one match found
     */
    public PropertyImpl findProperty(final Identity identity, final BusinessGroup grp, final OLATResourceable resourceable, final String category, final String name) {

        final List props = findProperties(identity, grp, resourceable, category, name);
        if (props == null || props.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find property: " + name);
            }
            return null;
        } else if (props.size() > 1) {
            throw new AssertException("findProperty found more than one properties for identity::" + identity + ", group::" + grp + ", resourceable::" + resourceable
                    + ", category::" + category + ", name::" + name);
        }
        return (PropertyImpl) props.get(0);
    }

    /**
     * @return a list of all available resource type names
     */
    public List getAllResourceTypeNames() {
        return database.find("select distinct v.resourceTypeName from org.olat.data.properties.PropertyImpl as v where v.resourceTypeName is not null");
    }

    public PropertyImpl createProperty() {
        final PropertyImpl p = new PropertyImpl();
        return p;
    }

    /**
     * Special-query for Upgrade-6.2.0.
     */
    public List<PropertyImpl> getCollaborationNewsProperties() {
        final StringBuilder query = new StringBuilder();
        query.append("from v in class org.olat.data.properties.PropertyImpl where ");
        query.append("v.category = '").append(PropertyManagerEBL.PROP_CAT_BG_COLLABTOOLS).append("'");
        query.append(" and ");
        query.append("v.name = 'news'");
        return database.find(query.toString());
    }

}
