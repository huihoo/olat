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

package org.olat.presentation.user.administration;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 14, 2003
 * 
 * @author gnaegi<br>
 *         Comment: Form for changing a user password.
 */
public class ChangeUserPasswordForm extends FormBasicController {

    TextElement pass1;
    TextElement pass2;
    TextElement username;

    String password = "";

    private final Identity userIdentity;

    /**
     * Constructor for user pwd forms.
     * 
     * @param UserRequest
     * @param WindowControl
     * @param Identity
     *            of which password is to be changed
     */
    public ChangeUserPasswordForm(final UserRequest ureq, final WindowControl wControl, final Identity treatedIdentity) {
        super(ureq, wControl);
        userIdentity = treatedIdentity;
        initForm(ureq);
    }

    @Override
    public boolean validateFormLogic(final UserRequest ureq) {

        final boolean newDoesMatch = pass1.getValue().equals(pass2.getValue());
        if (!newDoesMatch) {
            pass1.setErrorKey("error.password.nomatch", null);
        }

        final boolean newIsValid = getUserService().verifyPasswordStrength("", pass1.getValue(), userIdentity.getName());
        if (!newIsValid) {
            pass1.setErrorKey("error.password.characters", null);
        }

        if (newIsValid && newDoesMatch) {
            return true;
        }

        pass1.setValue("");
        pass2.setValue("");

        return false;
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        password = pass1.getValue();
        pass1.setValue("");
        pass2.setValue("");
        fireEvent(ureq, Event.DONE_EVENT);
    }

    protected String getNewPassword() {
        return password;
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("form.password.new1");
        setFormDescription("form.please.enter.new");

        username = uifactory.addTextElement("username", "form.username", 255, userIdentity.getName(), formLayout);
        username.setEnabled(false);

        pass1 = uifactory.addPasswordElement("pass1", "form.password.new1", 255, "", formLayout);
        pass2 = uifactory.addPasswordElement("pass2", "form.password.new2", 255, "", formLayout);
        uifactory.addFormSubmitButton("submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
