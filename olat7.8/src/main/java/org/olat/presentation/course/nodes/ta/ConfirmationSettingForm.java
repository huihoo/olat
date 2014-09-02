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

package org.olat.presentation.course.nodes.ta;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * a.k.a. DropboxForm. <br/>
 * It contains a single checkbox to select/deselect if the confirmation email is wanted for owners&tutors.
 * 
 * Initial Date: 30.08.2004
 * 
 * @author Mike Stock
 */

public class ConfirmationSettingForm extends FormBasicController {

    private SelectionElement enableConfirmationMailCheckbox;

    private String i18n_KEY_FORM_TITLE = "fieldset.dropbox.title"; // default title
    private String i18n_KEY_CHECKBOX_LABEL = "form.dropbox.enablemail"; // default key
    private final Boolean enableMail;

    public ConfirmationSettingForm(final UserRequest ureq, final WindowControl wControl, final boolean enableMail, final String i18nKeyFormTitle,
            final String i18nKeyCheckboxLabel) {
        super(ureq, wControl);

        this.i18n_KEY_FORM_TITLE = i18nKeyFormTitle;
        this.i18n_KEY_CHECKBOX_LABEL = i18nKeyCheckboxLabel;
        this.enableMail = enableMail;

        initForm(ureq);
    }

    /**
     * @return mailEnabled field value
     */
    public boolean mailEnabled() {
        return enableConfirmationMailCheckbox.isSelected(0);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == enableConfirmationMailCheckbox) {
            validateFormLogic(ureq);
        }
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        return true;
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle(i18n_KEY_FORM_TITLE);

        enableConfirmationMailCheckbox = uifactory.addCheckboxesVertical("enablemail", i18n_KEY_CHECKBOX_LABEL, formLayout, new String[] { "xx" }, new String[] { null },
                null, 1);
        enableConfirmationMailCheckbox.select("xx", enableMail != null ? enableMail.booleanValue() : true);
        enableConfirmationMailCheckbox.addActionListener(this, FormEvent.ONCLICK);

        uifactory.addFormSubmitButton("submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
