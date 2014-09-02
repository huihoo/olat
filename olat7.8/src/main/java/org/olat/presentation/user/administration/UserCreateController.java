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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.registration.UserPropertyParameter;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Jul 31, 2003
 * 
 * @author gnaegi Comment: Displays a form to create a new user on the OLAT plattform
 */
public class UserCreateController extends BasicController {

    private final NewUserForm createUserForm;

    /**
     * @param ureq
     * @param wControl
     */
    public UserCreateController(final UserRequest ureq, final WindowControl wControl, final boolean canCreateOLATPassword) {
        super(ureq, wControl);

        boolean isPermittedOnUserCreateController = getBaseSecurityEBL().isCreateUserPermitted(ureq.getIdentity());
        if (!isPermittedOnUserCreateController) {
            throw new OLATSecurityException("Insufficient permissions to access UserCreateController");
        }

        final Translator pT = getUserService().getUserPropertiesConfig().getTranslator(getTranslator());
        createUserForm = new NewUserForm(ureq, wControl, canCreateOLATPassword, pT);
        this.listenTo(createUserForm);

        final VelocityContainer newUserVC = this.createVelocityContainer("newuser");
        newUserVC.put("createUserForm", createUserForm.getInitialComponent());
        this.putInitialPanel(newUserVC);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // empty
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == createUserForm) {
            if (event instanceof SingleIdentityChosenEvent) {
                showInfo("new.user.successful");
                fireEvent(ureq, event);
            } else if (event == Event.FAILED_EVENT) {
                fireEvent(ureq, event);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}

/**
 * <pre>
 * 
 *  Initial Date:  Jul 31, 2003
 * 
 *  @author gnaegi
 *  
 *  Comment:  
 *  Form for creating new a new user as administrator
 * 
 * </pre>
 */

class NewUserForm extends FormBasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String formIdentifyer = NewUserForm.class.getCanonicalName();
    private static final String PASSWORD_NEW1 = "passwordnew1";
    private static final String PASSWORD_NEW2 = "passwordnew2";
    private static final String LOGINNAME = "loginname";
    private static final String USER_CREATE_SUCCESS = "user successfully created: ";
    private List<UserPropertyHandler> userPropertyHandlers;
    private boolean showPasswordFields = false;

    private TextElement emailTextElement;
    private TextElement usernameTextElement;
    private TextElement psw1TextElement;
    private TextElement psw2TextElement;
    private SingleSelection languageSingleSelection;
    private SelectionElement authCheckbox;
    private UserService userService;

    /**
     * @param ureq
     * @param wControl
     * @param showPasswordFields
     *            : true the password fields are used, the user can enter a password for the new user; false: the passwort is not used at all
     */
    public NewUserForm(final UserRequest ureq, final WindowControl wControl, final boolean showPasswordFields, final Translator translator) {
        super(ureq, wControl);
        this.userService = CoreSpringFactory.getBean(UserService.class);
        this.showPasswordFields = showPasswordFields;
        this.setTranslator(translator);
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        uifactory.addStaticTextElement("heading1", null, translate("new.form.please.enter"), formLayout);
        usernameTextElement = uifactory.addTextElement(LOGINNAME, "username", 128, "", formLayout);
        usernameTextElement.setMandatory(true);
        usernameTextElement.setDisplaySize(30);

        userPropertyHandlers = userService.getUserPropertyHandlersFor(formIdentifyer, true);
        // Add all available user fields to this form
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            if (userPropertyHandler == null) {
                continue;
            }
            final FormItem formItem = userPropertyHandler.addFormItem(ureq.getLocale(), null, formIdentifyer, true, formLayout);
            // special case to handle email field
            if (userPropertyHandler.getName().equals(UserConstants.EMAIL)) {
                emailTextElement = (TextElement) formItem;
            }
        }

        final Map<String, String> languages = I18nManager.getInstance().getEnabledLanguagesTranslated();
        final String[] langKeys = StringHelper.getMapKeysAsStringArray(languages);
        final String[] langValues = StringHelper.getMapValuesAsStringArray(languages);
        ArrayHelper.sort(langKeys, langValues, false, true, false);
        // Build css classes for reference languages
        final String[] langCssClasses = I18nManager.getInstance().createLanguageFlagsCssClasses(langKeys, "b_with_small_icon_left");
        languageSingleSelection = uifactory.addDropdownSingleselect("new.form.language", formLayout, langKeys, langValues, langCssClasses);
        // select default language in form
        languageSingleSelection.select(I18nModule.getDefaultLocale().toString(), true);

        // add password fields!!!
        if (showPasswordFields) {
            uifactory.addStaticTextElement("heading2", null, translate("new.form.please.enter.pwd"), formLayout);

            // checkBox: generate user with OLAT authentication or not
            final String[] authKeys = { "xx" };
            final String[] authValues = { translate("new.form.auth.true") };
            authCheckbox = uifactory.addCheckboxesHorizontal("new.form.auth", formLayout, authKeys, authValues, null);
            authCheckbox.select("xx", showPasswordFields);
            authCheckbox.addActionListener(this, FormEvent.ONCLICK);

            // if OLAT authentication is used, use the pwd below
            psw1TextElement = uifactory.addPasswordElement(PASSWORD_NEW1, "new.form.password.new1", 255, "", formLayout);
            psw1TextElement.setMandatory(true);
            psw1TextElement.setDisplaySize(30);
            psw1TextElement.setVisible(showPasswordFields);

            psw2TextElement = uifactory.addPasswordElement(PASSWORD_NEW2, "new.form.password.new2", 255, "", formLayout);
            psw2TextElement.setMandatory(true);
            psw2TextElement.setDisplaySize(30);
            psw2TextElement.setVisible(showPasswordFields);
        }

        uifactory.addFormSubmitButton("save", "submit.save", formLayout);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (showPasswordFields && source == authCheckbox) {
            psw1TextElement.setVisible(authCheckbox.isSelected(0));
            psw2TextElement.setVisible(authCheckbox.isSelected(0));
        }
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        // validate if username does match the syntactical login requirements
        final String loginName = usernameTextElement.getValue();
        if (usernameTextElement.isEmpty() || !userService.syntaxCheckOlatLogin(loginName)) {
            usernameTextElement.setErrorKey("new.error.loginname.empty", new String[] {});
            return false;
        }

        boolean loginNameAlreadyInUse = getBaseSecurityEBL().isUsernameAlreadyUsed(loginName);
        if (loginNameAlreadyInUse) {
            usernameTextElement.setErrorKey("new.error.loginname.choosen", new String[] {});
            return false;
        }
        usernameTextElement.clearError();

        // validate special rules for each user property
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            // we assume here that there are only textElements for the user properties
            final FormItem formItem = this.flc.getFormComponent(userPropertyHandler.getName());
            if (!userPropertyHandler.isValid(formItem, null) || formItem.hasError()) {
                return false;
            }
            formItem.clearError();
        }
        // special test on email address: validate if email is already used
        if (emailTextElement != null) {
            final String email = emailTextElement.getValue();
            // Check if email is not already taken

            // TODO:fj offer a method in basesecurity to threadsafely generate a new
            // user!!!

            final Identity exists = userService.findIdentityByEmail(email);
            if (exists != null) {
                // Oups, email already taken, display error
                emailTextElement.setErrorKey("new.error.email.choosen", new String[] {});
                return false;
            }
        }

        // validate if new password does match the syntactical password requirements

        // password fields depend on form configuration
        if (showPasswordFields && psw1TextElement != null && psw2TextElement != null && authCheckbox.isSelected(0)) {
            final String pwd = psw1TextElement.getValue();
            if (psw1TextElement.isEmpty("new.form.mandatory") || psw1TextElement.hasError()) {
                return false;
            }

            boolean isNewPasswordOK = getUserService().verifyPasswordStrength("", pwd, loginName);
            if (!isNewPasswordOK) {
                psw1TextElement.setErrorKey("new.error.password.characters", new String[] {});
                return false;
            }

            psw1TextElement.clearError();
            if (psw2TextElement.isEmpty("new.form.mandatory") || psw2TextElement.hasError()) {
                return false;
            }
            // validate that both passwords are the same
            if (!pwd.equals(psw2TextElement.getValue())) {
                psw2TextElement.setErrorKey("new.error.password.nomatch", new String[] {});
                return false;
            }
            psw2TextElement.clearError();
        }
        // all checks passed
        return true;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        // Create user on database
        final Identity s = doCreateAndPersistIdentity();
        if (s != null) {
            log.info(USER_CREATE_SUCCESS + s.getName());
            fireEvent(ureq, new SingleIdentityChosenEvent(s));
        } else {
            // Could not save form, display error
            getWindowControl().setError(translate("new.user.unsuccessful"));
            fireEvent(ureq, Event.FAILED_EVENT);
        }
    }

    @Override
    protected void formResetted(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    private Identity doCreateAndPersistIdentity() {
        final String lang = languageSingleSelection.getSelectedKey();
        final String username = usernameTextElement.getValue();
        String pwd = null;
        // use password only when configured to do so
        if (showPasswordFields && authCheckbox.isSelected(0)) {
            pwd = psw1TextElement.getValue();
            if (!StringHelper.containsNonWhitespace(pwd)) {
                // treat white-space passwords as no-password. This is fine, a password can be set later on
                pwd = null;
            }
        }

        List<UserPropertyParameter> parameters = getUserPropertyParameters(userPropertyHandlers);
        return getBaseSecurityEBL().createUser(lang, username, pwd, parameters);
    }

    private List<UserPropertyParameter> getUserPropertyParameters(List<UserPropertyHandler> userPropertyHandlers) {
        List<UserPropertyParameter> parameters = new ArrayList<UserPropertyParameter>();
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            final FormItem propertyItem = this.flc.getFormComponent(userPropertyHandler.getName());
            parameters.add(new UserPropertyParameter(userPropertyHandler, propertyItem));
        }
        return parameters;
    }

    @Override
    protected void doDispose() {
        // empty
    }
}
