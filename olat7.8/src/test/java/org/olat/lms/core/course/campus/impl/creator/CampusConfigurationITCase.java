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
package org.olat.lms.core.course.campus.impl.creator;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
public class CampusConfigurationITCase extends OlatTestCase {

    @Autowired
    private CampusConfiguration campusConfiguration;

    @Value("${campus.template.course.resourceable.id}")
    String defaultValue;

    @Value("${campus.template.defaultLanguage}")
    private String defaultTemplateLanguage;

    @Before
    public void setup() {
    }

    @Test
    public void getTemplateCourseResourcableId_DefaultValue() {
        Long configValue = campusConfiguration.getTemplateCourseResourcableId(null);
        assertEquals("Wrong default value, config-value is different to value in olat.properties", defaultValue, configValue.toString());
    }

    @Test
    public void saveTemplateCourseResourcableId() {
        Long oldValue = campusConfiguration.getTemplateCourseResourcableId(defaultTemplateLanguage);
        Long newValue = 1234L;
        campusConfiguration.saveTemplateCourseResourcableId(newValue, defaultTemplateLanguage);
        Long configValue = campusConfiguration.getTemplateCourseResourcableId(defaultTemplateLanguage);
        assertEquals("Get wrong config-value after save new value", newValue, configValue);
        campusConfiguration.saveTemplateCourseResourcableId(oldValue, defaultTemplateLanguage);
    }
}
