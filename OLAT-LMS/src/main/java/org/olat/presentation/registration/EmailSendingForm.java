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

package org.olat.presentation.registration;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.mail.MailHelper;

/**
 * description of first registration form for email-address
 * 
 * @author Sabina Jeger
 */
public class EmailSendingForm extends FormBasicController {

    private TextElement mail;

    public EmailSendingForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    /**
     * Initialize the form
     */

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        mail = uifactory.addTextElement("mail", "email.address", 255, "", formLayout);
        mail.setMandatory(true);

        // Button layout
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("submit.speichernUndweiter", buttonLayout);
        uifactory.addFormCancelButton("submit.cancel", buttonLayout, ureq, getWindowControl());
    }

    protected String getEmailAddress() {
        return mail.getValue().trim();
    }

    @Override
    public boolean validateFormLogic(final UserRequest ureq) {

        if (mail.isEmpty("email.address.maynotbeempty")) {
            return false;
        }
        if (!MailHelper.isValidEmailAddress(mail.getValue())) {
            mail.setErrorKey("email.address.notregular", null);
            return false;
        }
        return true;
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {
        //
    }
}
