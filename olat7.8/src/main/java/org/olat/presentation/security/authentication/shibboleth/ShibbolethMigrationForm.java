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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 09.08.2004
 * 
 * @author Mike Stock Comment:
 */

public class ShibbolethMigrationForm extends FormBasicController {

    private final Authentication authentication;
    private TextElement login;
    private TextElement password;
    private static final Logger log = LoggerHelper.getLogger();

    public ShibbolethMigrationForm(final UserRequest ureq, final WindowControl wControl, final Authentication authentication) {
        super(ureq, wControl);
        this.authentication = authentication;
        initForm(ureq);
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (authentication != null && authentication.getCredential() == null && authentication.getNewCredential() == null) {
            return false;
        }
        boolean validCredentials = authenticate(password.getValue(), authentication);
        if (validCredentials) {
            return true;
        } else {
            if (LoginModule.registerFailedLoginAttempt(login.getValue())) {
                password.setErrorKey("smf.error.blocked", null);
                log.info("Audit:Too many failed login attempts for " + login.getValue() + ". Login blocked.");
            } else {
                password.setErrorKey("smf.error.password", null);
                log.info("Audit:Invalid password in ShibbolethMigration for login: " + login.getValue());
            }
            return false;
        }
    }

    private AuthenticationService getAuthenticationService() {
        return CoreSpringFactory.getBean(AuthenticationService.class);
    }

    private boolean authenticate(String password, Authentication authentication) {
        return getAuthenticationService().authenticate(password, authentication);
    }

    /**
     * @return Authentication
     */
    protected Authentication getAuthentication() {
        return authentication;
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
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        login = uifactory.addTextElement("smf_login", "smf.login", 128, authentication.getIdentity().getName(), formLayout);
        login.setEnabled(false);

        password = uifactory.addPasswordElement("smf_password", "smf.password", 255, "", formLayout);

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

}
