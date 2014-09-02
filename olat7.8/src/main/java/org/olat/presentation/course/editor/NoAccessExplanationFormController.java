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

package org.olat.presentation.course.editor;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Provides a controller using FlexiForms which allows entering a text which gets displayed as an explanation when the user has no permission to access a ressource.
 * 
 * @author twuersch
 */
public class NoAccessExplanationFormController extends FormBasicController {

    /**
     * The text input form.
     */
    private RichTextElement noAccessExplanationInput;

    /**
     * Remembers whether the constructor was used to initialize the form.
     */
    private boolean constructorInitCall;

    /**
     * The message.
     */
    private final String noAccessString;

    /**
     * Initializes this controller.
     * 
     * @param ureq
     *            The user request.
     * @param wControl
     *            The window control.
     * @param noAccessString
     */
    public NoAccessExplanationFormController(final UserRequest ureq, final WindowControl wControl, final String noAccessString) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
        this.noAccessString = noAccessString;
        constructorInitCall = true;
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Don't dispose anything
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        super.formInnerEvent(ureq, source, event);
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
        fireEvent(ureq, Event.FAILED_EVENT);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        // Add the rich text element
        noAccessExplanationInput = uifactory.addRichTextElementForStringDataMinimalistic("form.noAccessExplanation", "form.noAccessExplanation",
                (noAccessString == null ? "" : noAccessString), 10, -1, false, formLayout, ureq.getUserSession(), getWindowControl());

        if (constructorInitCall) {
            // Create submit button
            final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
            formLayout.add(buttonLayout);
            uifactory.addFormSubmitButton("save", buttonLayout);
            constructorInitCall = false;
        }
    }

    /**
     * Gets the message string for the no access explanation.
     * 
     * @return String The noAccessExplenation
     */
    public String getNoAccessExplanation() {
        if (noAccessExplanationInput != null) {
            return noAccessExplanationInput.getValue();
        } else {
            return null;
        }
    }

    /**
     * Gets the message string for the no access explanation.
     * 
     * @param message
     *            The message
     */
    public void setNoAccessExplanation(final String message) {
        if (noAccessExplanationInput != null) {
            noAccessExplanationInput.setValue(message);
        }
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        boolean formOK = true;
        if (noAccessExplanationInput.getValue().length() > 4000) {
            formOK = false;
            noAccessExplanationInput.setErrorKey("input.toolong", new String[] {});
        }
        if (formOK && super.validateFormLogic(ureq)) {
            noAccessExplanationInput.clearError();
            return true;
        } else {
            return false;
        }
    }
}
