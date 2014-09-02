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

package org.olat.presentation.course.nodes.projectbroker;

import java.util.HashMap;

import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.presentation.course.editor.NodeEditController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * @author guretzki
 */

public class ProjectEventFormController extends FormBasicController {
    private final String KEY_EVENT_TABLE_VIEW_ENABLED = "event.table.view.enabled";

    private final ProjectBrokerModuleConfiguration config;
    private final HashMap<Project.EventType, MultipleSelectionElement> projectEventElementList;

    private FormSubmit formSubmit;

    /**
     * Modules selection form.
     * 
     * @param name
     * @param config
     */
    public ProjectEventFormController(final UserRequest ureq, final WindowControl wControl, final ProjectBrokerModuleConfiguration config) {
        super(ureq, wControl);
        this.config = config;
        projectEventElementList = new HashMap<Project.EventType, MultipleSelectionElement>();
        initForm(this.flc, this, ureq);
    }

    /**
     * Initialize form.
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        int index = 1;
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (index++ > 1) {
                uifactory.addSpacerElement("event_spacer" + index, formLayout, false);
            }
            final String[] keys = new String[] { "event.enabled", KEY_EVENT_TABLE_VIEW_ENABLED };
            final String[] values = new String[] { translate(eventType.getI18nKey() + ".label"), translate(KEY_EVENT_TABLE_VIEW_ENABLED) };
            final boolean isEventEnabled = config.isProjectEventEnabled(eventType);
            final boolean isTableViewEnabled = config.isProjectEventTableViewEnabled(eventType);
            final MultipleSelectionElement projectEventElement = uifactory.addCheckboxesVertical(eventType.toString(), null, formLayout, keys, values, null, 1);
            projectEventElement.select(keys[0], isEventEnabled);
            projectEventElement.setVisible(keys[1], isEventEnabled);
            projectEventElement.select(keys[1], isTableViewEnabled);
            projectEventElement.addActionListener(listener, FormEvent.ONCLICK);
            projectEventElementList.put(eventType, projectEventElement);
        }
        formSubmit = uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        for (final Project.EventType eventType : projectEventElementList.keySet()) {
            final boolean isEventEnabled = projectEventElementList.get(eventType).isSelected(0);
            config.setProjectEventEnabled(eventType, isEventEnabled);
            if (isEventEnabled) {
                config.setProjectEventTableViewEnabled(eventType, projectEventElementList.get(eventType).isSelected(1));
            } else {
                config.setProjectEventTableViewEnabled(eventType, false);
            }
            projectEventElementList.get(eventType).setEnabled(KEY_EVENT_TABLE_VIEW_ENABLED, isEventEnabled);
        }
        fireEvent(ureq, Event.DONE_EVENT);
        fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        for (final Project.EventType eventType : projectEventElementList.keySet()) {
            final boolean isEventEnabled = projectEventElementList.get(eventType).isSelected(0);
            projectEventElementList.get(eventType).setVisible(KEY_EVENT_TABLE_VIEW_ENABLED, isEventEnabled);
        }
        this.flc.setDirty(true);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
    }

    @Override
    protected void doDispose() {
        // nothing
    }

}
