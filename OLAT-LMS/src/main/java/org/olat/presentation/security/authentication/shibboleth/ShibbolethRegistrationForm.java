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

package org.olat.presentation.security.authentication.shibboleth;

import org.olat.lms.user.UserModule;
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
 * Initial Date: 09.08.2004
 * 
 * @author Mike Stock Comment:
 */

public class ShibbolethRegistrationForm extends FormBasicController {

    private TextElement login;
    private final String proposedUsername;

    /**
     * @param name
     * @param translator
     */

    public ShibbolethRegistrationForm(final UserRequest ureq, final WindowControl wControl, final String proposedUsername) {
        super(ureq, wControl);
        this.proposedUsername = proposedUsername;
        initForm(ureq);
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (login.isEmpty("srf.error.loginempty")) {
            return false;
        }
        if (!getUserService().syntaxCheckOlatLogin(getLogin())) {
            login.setErrorKey("srf.error.loginregexp", null);
            return false;
        }
        if (UserModule.isLoginOnBlacklist(getLogin())) {
            login.setErrorKey("srf.error.blacklist", null);
            return false;
        }
        return true;
    }

    /**
     * @return Login field.
     */
    protected String getLogin() {
        return login.getValue();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final String initialValue = proposedUsername == null ? "" : proposedUsername;
        login = uifactory.addTextElement("srf_login", "srf.login", 128, initialValue, formLayout);
        login.setExampleKey("srf.login.example", null);
        uifactory.addFormSubmitButton("save", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
