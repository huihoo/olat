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

package org.olat.lms.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.course.nodes.projectbroker.CustomField;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.Project.EventType;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.system.logging.log4j.LoggerHelper;

public class ProjectBrokerModuleConfiguration {
    private static final Logger log = LoggerHelper.getLogger();

    public static final String ACCEPT_SELECTION_MANUALLY = "accept_selection_manually";
    private static final String AUTO_SIGN_OUT = "auto_sign_out";
    private static final String NBR_PARTICIPANTS_PER_TOPIC = "nbr_participants_per_topic";
    private static final String CUSTOM_FIELDS_SIZE = "custom_field_size";
    private static final String CUSTOM_FIELDS_NAME_ = "custom_field_name_";
    private static final String CUSTOM_FIELDS_VALUE_ = "custom_field_value_";
    private static final String CUSTOM_FIELDS_TABLE_VIEW_ENABLED_ = "custom_field_table_view_enabled_";

    private static final String TABLE_VIEW = "_table_view";

    public static final int NBR_PARTICIPANTS_UNLIMITED = -1;

    private final ModuleConfiguration moduleConfiguration;

    public ProjectBrokerModuleConfiguration(final ModuleConfiguration moduleConfiguration) {
        this.moduleConfiguration = moduleConfiguration;
    }

    public boolean isAcceptSelectionManually() {
        return moduleConfiguration.getBooleanSafe(ACCEPT_SELECTION_MANUALLY);
    }

    public int getNbrParticipantsPerTopic() {
        return moduleConfiguration.getIntegerSafe(NBR_PARTICIPANTS_PER_TOPIC, NBR_PARTICIPANTS_UNLIMITED);
    }

    public boolean isAutoSignOut() {
        return moduleConfiguration.getBooleanSafe(AUTO_SIGN_OUT);
    }

    public void setAcceptSelectionManaually(final boolean acceptSelectionManually) {
        moduleConfiguration.setBooleanEntry(ACCEPT_SELECTION_MANUALLY, acceptSelectionManually);
    }

    public void setNbrParticipantsPerTopic(final int nbrParticipantsPerTopic) {
        moduleConfiguration.setIntValue(NBR_PARTICIPANTS_PER_TOPIC, nbrParticipantsPerTopic);
    }

    public void setSelectionAutoSignOut(final boolean selectionAutoSignOut) {
        moduleConfiguration.setBooleanEntry(AUTO_SIGN_OUT, selectionAutoSignOut);
    }

    public List<CustomField> getCustomFields() {
        final int size = moduleConfiguration.getIntegerSafe(CUSTOM_FIELDS_SIZE, 0);
        final List<CustomField> customFields = new ArrayList<CustomField>();
        for (int i = 0; i < size; i++) {
            final String name = (String) moduleConfiguration.get(CUSTOM_FIELDS_NAME_ + i);
            log.debug("getCustomFields " + CUSTOM_FIELDS_NAME_ + i + "=" + name);
            final String value = (String) moduleConfiguration.get(CUSTOM_FIELDS_VALUE_ + i);
            log.debug("getCustomFields " + CUSTOM_FIELDS_VALUE_ + i + "=" + value);
            final boolean tableViewEnabled = moduleConfiguration.getBooleanSafe(CUSTOM_FIELDS_TABLE_VIEW_ENABLED_ + i, true);
            customFields.add(new CustomField(name, value, tableViewEnabled));
        }
        return customFields;
    }

    public void setCustomFields(final List<CustomField> customFields) {
        moduleConfiguration.setIntValue(CUSTOM_FIELDS_SIZE, customFields.size());
        for (int i = 0; i < customFields.size(); i++) {
            moduleConfiguration.set(CUSTOM_FIELDS_NAME_ + i, customFields.get(i).getName());
            log.debug("setCustomFields " + CUSTOM_FIELDS_NAME_ + i + "=" + customFields.get(i).getName());
            moduleConfiguration.set(CUSTOM_FIELDS_VALUE_ + i, customFields.get(i).getValue());
            log.debug("setCustomFields " + CUSTOM_FIELDS_VALUE_ + i + "=" + customFields.get(i).getValue());
            moduleConfiguration.setBooleanEntry(CUSTOM_FIELDS_TABLE_VIEW_ENABLED_ + i, customFields.get(i).isTableViewEnabled());
        }
    }

    public boolean isProjectEventEnabled(final Project.EventType eventType) {
        return moduleConfiguration.getBooleanSafe(eventType.toString());
    }

    public void setProjectEventEnabled(final Project.EventType eventType, final boolean value) {
        moduleConfiguration.setBooleanEntry(eventType.toString(), value);
    }

    public boolean isProjectEventTableViewEnabled(final EventType eventType) {
        return moduleConfiguration.getBooleanSafe(eventType.toString() + TABLE_VIEW);
    }

    public void setProjectEventTableViewEnabled(final Project.EventType eventType, final boolean value) {
        moduleConfiguration.setBooleanEntry(eventType.toString() + TABLE_VIEW, value);
    }

}
