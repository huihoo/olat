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

package org.olat.lms.course.run.preview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewCoursePropertyManager extends BasicManager implements CoursePropertyManager {

    /**
     * Hashmap contains hasmaps
     */
    private final Map properties = new HashMap();

    /**
     * Creates a new course proprerty manager that stores properties per instance.
     */
    public PreviewCoursePropertyManager() {
        //
    }

    /**
     * org.olat.data.group.BusinessGroup, java.lang.String, java.lang.Float, java.lang.Long, java.lang.String, java.lang.String)
     */
    @Override
    public PropertyImpl createCourseNodePropertyInstance(final CourseNode node, final Identity identity, final BusinessGroup group, final String name,
            final Float floatValue, final Long longValue, final String stringValue, final String textValue) {
        final PropertyImpl p = PropertyManager.getInstance().createProperty();
        p.setCategory(buildCourseNodePropertyCategory(node));
        p.setIdentity(identity);
        p.setGrp(null);
        p.setName(name);
        p.setLongValue(longValue);
        p.setFloatValue(floatValue);
        p.setStringValue(stringValue);
        p.setTextValue(textValue);
        return p;
    }

    /**
	 */
    @Override
    public void deleteProperty(final PropertyImpl p) {
        final List propertyList = getListOfProperties(p);
        for (int i = 0; i < propertyList.size(); i++) {
            final PropertyImpl propertyElement = (PropertyImpl) propertyList.get(i);
            if (propertyElement.getLongValue().equals(p.getLongValue()) && propertyElement.getFloatValue().equals(p.getFloatValue())
                    && propertyElement.getStringValue().equals(p.getStringValue()) && propertyElement.getTextValue().equals(p.getTextValue())) {
                propertyList.remove(i);
                break;
            }
        }
    }

    /**
	 */
    @Override
    public void saveProperty(final PropertyImpl p) {
        final List propertyList = getListOfProperties(p);
        // since this is a save (only done once after creation) we
        // can safely add it to the list without looking for duplicates
        propertyList.add(p);
    }

    /**
	 */
    @Override
    public void updateProperty(final PropertyImpl p) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
     * org.olat.data.group.BusinessGroup, java.lang.String)
     */
    @Override
    public List listCourseNodeProperties(final CourseNode node, final Identity identity, final BusinessGroup grp, final String name) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
     * org.olat.data.group.BusinessGroup, java.lang.String)
     */
    @Override
    public List findCourseNodeProperties(final CourseNode node, final Identity identity, final BusinessGroup grp, final String name) {
        List propertiesList = (List) properties.get(buildPropertyHashKey(buildCourseNodePropertyCategory(node), (identity == null ? "" : identity.getName()), grp, name));
        if (propertiesList == null) {
            propertiesList = new ArrayList();
        }
        return propertiesList;
    }

    /**
     * org.olat.data.group.BusinessGroup, java.lang.String)
     */
    @Override
    public PropertyImpl findCourseNodeProperty(final CourseNode node, final Identity identity, final BusinessGroup grp, final String name) {
        final List propertyList = (List) properties.get(buildPropertyHashKey(buildCourseNodePropertyCategory(node), (identity == null ? "" : identity.getName()), grp,
                name));
        if (propertyList == null) {
            return null;
        }
        return (PropertyImpl) propertyList.get(0);
    }

    /**
	 */
    @Override
    public void deleteNodeProperties(final CourseNode courseNode, final String name) {
        final String category = buildCourseNodePropertyCategory(courseNode);
        final Object[] keys = properties.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            final String key = (String) keys[i];
            if (key.startsWith(category) && key.endsWith(name)) {
                properties.remove(key);
            }
        }
    }

    /**
     * A property key consists of Category, Identity, Group and Name. Each property can have multiple values for the same given key. This returns the list of properties
     * with the same key.
     * 
     * @param p
     * @return list of properties with the same key
     */
    private List getListOfProperties(final PropertyImpl p) {
        final String propertyKey = buildPropertyHashKey(p);
        // get the list of properties for this key...
        List propertyList = (List) properties.get(propertyKey);
        if (propertyList == null) {
            propertyList = new ArrayList();
            properties.put(propertyKey, propertyList);
        }
        return propertyList;
    }

    private String buildPropertyHashKey(final PropertyImpl p) {
        return buildPropertyHashKey(p.getCategory(), (p.getIdentity() == null) ? "" : p.getIdentity().getName(), p.getGrp(), p.getName());
    }

    private String buildPropertyHashKey(final String category, final String identityName, final BusinessGroup group, final String name) {
        return (category + identityName + (group == null ? "" : group.getKey().toString()) + name);
    }

    private String buildCourseNodePropertyCategory(final CourseNode node) {
        final String type = (node.getType().length() > 4 ? node.getType().substring(0, 4) : node.getType());
        return ("NID:" + type + "::" + node.getIdent());
    }

    /**
	 */
    @Override
    public String getAnonymizedUserName(final Identity identity) {
        throw new AssertException("Not implemented for preview.");
    }

    /**
	 */
    @Override
    public void deleteAllCourseProperties() {
        throw new AssertException("Not implemented for preview.");
    }

    @Override
    public PropertyImpl createCourseNodeNamePropertyInstance(CourseNode node, String name, Long longValue) {
        throw new AssertException("Not implemented for preview.");
    }
}
