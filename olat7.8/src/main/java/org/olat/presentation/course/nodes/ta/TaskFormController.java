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
 * Copyright (c) 2009 frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.course.nodes.ta;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Provides a flexiform-based dialog for entering task assignment data, including an info message and choices for assignment type, sampling and preview.
 * 
 * @author twuersch
 */
public class TaskFormController extends FormBasicController {

    private final ModuleConfiguration config;

    private RichTextElement optionalText;

    private SingleSelection type;

    private SingleSelection deselect;

    private SingleSelection sampling;

    private SingleSelection preview;

    /**
     * Creates this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param config
     *            This task's configuration object.
     */
    public TaskFormController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.config = config;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // don't dispose anything

    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formNOK(final UserRequest ureq) {
        super.formNOK(ureq);
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // add the "optional message for users" rich text input element
        this.optionalText = uifactory.addRichTextElementForStringDataMinimalistic("form.task.text", "form.task.text",
                (String) this.config.get(TACourseNode.CONF_TASK_TEXT), 10, -1, false, formLayout, ureq.getUserSession(), getWindowControl());

        // add the task type radio buttons
        final String taskType = (String) this.config.get(TACourseNode.CONF_TASK_TYPE);
        final String typeKeys[] = new String[] { TaskController.TYPE_MANUAL, TaskController.TYPE_AUTO };
        final String typeValues[] = new String[] { translate("form.task.type.manual"), translate("form.task.type.auto") };
        this.type = uifactory.addRadiosVertical("form.task.type", formLayout, typeKeys, typeValues);
        if (taskType != null) {
            this.type.select(taskType, true);
        } else {
            this.type.select(TaskController.TYPE_MANUAL, true);
        }
        this.type.addActionListener(this, FormEvent.ONCLICK);

        // add the preview radio buttons
        Boolean taskPreview = Boolean.valueOf(false);// per default no preview
        final Object taskPreviewObj = this.config.get(TACourseNode.CONF_TASK_PREVIEW);
        if (taskPreviewObj != null) {
            taskPreview = this.config.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW);
        }
        final String previewKeys[] = new String[] { TaskController.WITH_PREVIEW, TaskController.WITHOUT_PREVIEW };
        final String previewValues[] = new String[] { translate("form.task.with.preview"), translate("form.task.without.preview") };
        this.preview = uifactory.addRadiosVertical("form.task.preview", formLayout, previewKeys, previewValues);
        if (taskPreview.booleanValue()) {
            this.preview.select(TaskController.WITH_PREVIEW, true);
        } else {
            this.preview.select(TaskController.WITHOUT_PREVIEW, true);
        }
        this.preview.addActionListener(this, FormEvent.ONCLICK);

        // add the deselect radio buttons
        Boolean taskDeselect = Boolean.valueOf(false);// per default no deselect possible
        final Object taskDeselectObj = this.config.get(TACourseNode.CONF_TASK_DESELECT);
        if (taskDeselectObj != null) {
            taskDeselect = this.config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);
        }
        final String deselectKeys[] = new String[] { TaskController.WITH_DESELECT, TaskController.WITHOUT_DESELECT };
        final String deselectValues[] = new String[] { translate("form.task.with.deselect"), translate("form.task.without.deselect") };
        this.deselect = uifactory.addRadiosVertical("form.task.deselect", formLayout, deselectKeys, deselectValues);
        if (taskDeselect.booleanValue()) {
            this.deselect.select(TaskController.WITH_DESELECT, true);
        } else {
            this.deselect.select(TaskController.WITHOUT_DESELECT, true);
        }

        // add the sampling type radio buttons
        Boolean samplingWithReplacement = (Boolean) this.config.get(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT);
        if (samplingWithReplacement == null) {
            samplingWithReplacement = Boolean.valueOf(true);
        }
        final String samplingKeys[] = new String[] { "swith", "swithout" };
        final String samplingValues[] = new String[] { translate("form.task.sampling.with"), translate("form.task.sampling.without") };
        this.sampling = uifactory.addRadiosVertical("form.task.sampling", formLayout, samplingKeys, samplingValues);
        if (samplingWithReplacement.booleanValue()) {
            this.sampling.select("swith", true);
        } else {
            this.sampling.select("swithout", true);
        }

        // Create submit button
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", buttonLayout);

        update();
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        update();
    }

    /**
     * Updates UI, replaces the RulesFactory.createShowRule/RulesFactory.createHideRule.
     */
    private void update() {
        // Preview selection only visible if type == manual
        final boolean isManualSelected = type.isSelected(0);
        preview.setVisible(isManualSelected);
        // deselect selection only visible if type == manual and preview available
        final boolean hasPreview = preview.isVisible() && preview.isSelected(0);
        deselect.setVisible(hasPreview);
    }

    /**
     * @return Task type field value.
     */
    public String getTaskType() {
        return this.type.getSelectedKey();
    }

    /**
     * @return Optional text field value.
     */
    public String getOptionalText() {
        return this.optionalText.getValue();
    }

    /**
     * @return True if task is "Sampling With Replacement"
     */
    public boolean getIsSamplingWithReplacement() {
        return this.sampling.getSelectedKey().equals("swith");
    }

    /**
     * @return True if task is "Preview Mode".
     */
    public boolean isTaskPreviewMode() {
        return this.preview.getSelectedKey().equals(TaskController.WITH_PREVIEW);
    }

    /**
     * @return true if task is "deselectable"
     */
    public boolean isTaskDeselectMode() {
        if (deselect.isVisible()) {
            return this.deselect.getSelectedKey().equals(TaskController.WITH_DESELECT);
        }
        return false;
    }
}
