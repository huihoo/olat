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
package org.olat.lms.core.course.campus;

import org.apache.commons.lang.StringUtils;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simple campus-course configuration with Spring properties (olat.local.properties).
 * 
 * @author cg
 */
@Component
public class CampusConfiguration {

    private static final Identity NO_IDENTITY = null;
    private static final BusinessGroup NO_GROUP = null;
    private static final OLATResourceable NO_RESOURCEABLE = null;

    private static final String CAMPUS_COURSE_PROPERTY_CATEGORY = "campus.course.property";
    private static final String TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY = "_template.course.resourceable.id";

    @Value("${campus.template.course.resourceable.id}")
    private String defaultTemplateCourseResourcableId;

    @Value("${campus.template.supportedLanguages}")
    private String templateSupportedLanguages;

    @Value("${campus.template.course.groupA.name}")
    private String courseGroupAName;

    @Value("${campus.template.course.groupB.name}")
    private String courseGroupBName;

    @Value("${campus.course.default.co.owner.usernames}")
    private String defaultCoOwnerUserNames;

    @Value("${campus.description.startsWith.string}")
    private String descriptionStartWithString;

    @Value("${campus.enable.synchronizeTitleAndDescription}")
    private boolean synchronizeTitleAndDescription;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Value("${campus.import.process.sap.files.suffix}")
    private String sapFilesSuffix;

    @Autowired
    PropertyManager propertyManager;

    // @Value("${campus.start.autumn.semester}")
    // private String startDateAutumnSemester;
    //
    // @Value("${campus.start.spring.semester}")
    // private String startDateSpringSemester;

    public String getTemplateLanguage(String language) {
        if (StringUtils.isBlank(language) || !StringUtils.contains(getTemplateSupportedLanguages(), language)) {
            language = getDefaultTemplateLanguage();
        }
        return language;
    }

    public Long getTemplateCourseResourcableId(String language) {
        language = getTemplateLanguage(language);

        String propertyStringValue = null;
        try {
            propertyStringValue = getPropertyOrDefaultValue(language.concat(TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY), defaultTemplateCourseResourcableId);
            return Long.valueOf(propertyStringValue);
        } catch (NumberFormatException ex) {
            throw new AssertException("Could not convert to Long-value '" + propertyStringValue + "' , check properties");
        }
    }

    String getPropertyOrDefaultValue(String propertyKey, String defaultValue) {
        PropertyImpl property = findCampusProperty(propertyKey);
        if (property == null) {
            return defaultValue;
        } else {
            return property.getStringValue();
        }
    }

    private PropertyImpl findCampusProperty(String propertyKey) {
        return propertyManager.findProperty(NO_IDENTITY, NO_GROUP, NO_RESOURCEABLE, CAMPUS_COURSE_PROPERTY_CATEGORY, propertyKey);
    }

    public void saveTemplateCourseResourcableId(Long templateCourseResourcableId, String language) {
        saveCampusProperty(language.concat(TEMPLATE_COURSE_RESOURCEABLE_ID_PROPERTY_KEY), templateCourseResourcableId.toString());
    }

    void saveCampusProperty(String propertyKey, String propertyValue) {
        PropertyImpl property = findCampusProperty(propertyKey);
        if (property == null) {
            property = propertyManager.createPropertyInstance(NO_IDENTITY, NO_GROUP, NO_RESOURCEABLE, CAMPUS_COURSE_PROPERTY_CATEGORY, propertyKey, null, null,
                    propertyValue, null);
            propertyManager.saveProperty(property);
        } else {
            property.setStringValue(propertyValue);
            propertyManager.updateProperty(property);
        }
    }

    public String getCourseGroupAName() {
        return courseGroupAName;
    }

    public String getCourseGroupBName() {
        return courseGroupBName;
    }

    public String getDefaultCoOwnerUserNames() {
        return defaultCoOwnerUserNames;
    }

    public boolean isSynchronizeTitleAndDescriptionEnabled() {
        return synchronizeTitleAndDescription;
    }

    public String getDescriptionStartWithString() {
        return descriptionStartWithString;
    }

    public String[] getDescriptionStartWithStringAsArray() {
        String[] splittArray = null;
        if (!StringUtils.isBlank(descriptionStartWithString)) {
            splittArray = descriptionStartWithString.split(",");
        }
        return splittArray;
    }

    public String getTemplateSupportedLanguages() {
        return templateSupportedLanguages;
    }

    public String getDefaultTemplateLanguage() {
        return defaultTemplateLanguage;
    }

    public String getSapFilesSuffix() {
        return sapFilesSuffix;
    }

    // TODO: How to handle start-date of semester ?
    // public String getStartDateAutumnSemester() {
    // return startDateAutumnSemester;
    // }
    //
    // public String getStartDateSpringSemester() {
    // return startDateSpringSemester;
    // }

    // TODO: Configuration via Admin-GUI/JMX: Add setter methods and use PropertyManager to save values.
    // The Spring properties values can be used as default values.

}
