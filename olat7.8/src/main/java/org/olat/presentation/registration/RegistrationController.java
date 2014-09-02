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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.mediaresource.RedirectMediaResource;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.lms.registration.RegisterUserParameter;
import org.olat.lms.registration.RegistrationModule;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.registration.RegistrationServiceEBL;
import org.olat.lms.registration.UserPropertyParameter;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardInfoController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controls the registration workflow.
 * <P>
 * 
 * @author Sabina Jeger
 */
public class RegistrationController extends BasicController {

    private VelocityContainer myContent;
    private final Panel regarea;
    private final WizardInfoController wizardController;
    private DisclaimerController dclController;
    private EmailSendingForm emailSendingForm;
    private RegistrationForm2 registrationForm;

    private final String regKey;
    private TemporaryKeyImpl tempKey;

    /**
     * Controller implementing registration work flow.
     * 
     * @param ureq
     * @param wControl
     */
    public RegistrationController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        if (!RegistrationModule.isSelfRegistrationEnabled()) {
            throw new OLATRuntimeException(RegistrationController.class, "Registration controller launched but self registration is turned off in the config file", null);
        }
        // override language when not the same as in ureq and add fallback to
        // property handler translator for user properties
        final String lang = ureq.getParameter("lang");
        if (lang != null && !lang.equals(I18nManager.getInstance().getLocaleKey(getLocale()))) {
            final Locale loc = I18nManager.getInstance().getLocaleOrDefault(lang);
            ureq.getUserSession().setLocale(loc);
            setLocale(loc, true);
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(PackageUtil.createPackageTranslator(this.getClass(), loc)));
        } else {
            // set fallback only
            setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        }

        // construct content
        myContent = createVelocityContainer("reg");
        wizardController = new WizardInfoController(ureq, 4);
        listenTo(wizardController);
        myContent.put("regwizard", wizardController.getInitialComponent());
        regarea = new Panel("regarea");
        myContent.put("regarea", regarea);
        regKey = ureq.getHttpReq().getParameter("key");
        if (regKey == null || regKey.equals("")) {
            // no temporary key is given, we assume step 1. If this is the case, we
            // render in a modal dialog, no need to add the 3cols layout controller wrapper
            createEmailForm(ureq);
            putInitialPanel(myContent);
        } else {
            // we check if given key is a valid temporary key
            tempKey = getRegistrationService().loadTemporaryKeyByRegistrationKey(regKey);
            // if key is not valid we redirect to first page
            if (tempKey == null) {
                // error, there should be an entry
                showError("regkey.missingentry");
                createEmailForm(ureq);
            } else {
                wizardController.setCurStep(2);
                myContent.contextPut("pwdhelp", translate("pwdhelp"));
                myContent.contextPut("loginhelp", translate("loginhelp"));
                myContent.contextPut("text", translate("step3.reg.text"));
                myContent.contextPut("email", tempKey.getEmailAddress());

                final Map<String, String> userAttrs = new HashMap<String, String>();
                userAttrs.put("email", tempKey.getEmailAddress());

                if (RegistrationModule.getUsernamePresetBean() != null) {
                    final UserNameCreationInterceptor interceptor = RegistrationModule.getUsernamePresetBean();
                    final String proposedUsername = interceptor.getUsernameFor(userAttrs);
                    if (proposedUsername == null) {
                        if (interceptor.allowChangeOfUsername()) {
                            createRegForm2(ureq, null, false, false);
                        } else {
                            myContent = setErrorPage("reg.error.no_username", wControl);
                        }
                    } else {
                        final Identity identity = getBaseSecurity().findIdentityByName(proposedUsername);
                        if (identity != null) {
                            if (interceptor.allowChangeOfUsername()) {
                                createRegForm2(ureq, proposedUsername, true, false);
                            } else {
                                myContent = setErrorPage("reg.error.user_in_use", wControl);
                            }
                        } else if (interceptor.allowChangeOfUsername()) {
                            createRegForm2(ureq, proposedUsername, false, false);
                        } else {
                            createRegForm2(ureq, proposedUsername, false, true);
                        }
                    }
                } else {
                    createRegForm2(ureq, null, false, false);
                }
            }
            // load view in layout
            final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, myContent, null);
            listenTo(layoutCtr);
            putInitialPanel(layoutCtr.getInitialComponent());
        }
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private VelocityContainer setErrorPage(final String errorKey, final WindowControl wControl) {
        final String error = getTranslator().translate(errorKey);
        wControl.setError(error);
        final VelocityContainer errorContainer = createVelocityContainer("error");
        errorContainer.contextPut("errorMsg", error);
        return errorContainer;
    }

    private void createRegForm2(final UserRequest ureq, final String proposedUsername, final boolean userInUse, final boolean usernameReadonly) {
        registrationForm = new RegistrationForm2(ureq, getWindowControl(), I18nManager.getInstance().getLocaleKey(getLocale()), proposedUsername, userInUse,
                usernameReadonly);
        listenTo(registrationForm);
        regarea.setContent(registrationForm.getInitialComponent());
    }

    /**
     * just needed for creating EmailForm
     */
    private void createEmailForm(final UserRequest ureq) {
        removeAsListenerAndDispose(emailSendingForm);
        emailSendingForm = new EmailSendingForm(ureq, getWindowControl());
        listenTo(emailSendingForm);

        myContent.contextPut("text", translate("step1.reg.text"));
        regarea.setContent(emailSendingForm.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == emailSendingForm) {
            if (event == Event.DONE_EVENT) { // form
                // validation was ok
                wizardController.setCurStep(1);
                // Email requested for tempkey
                // save the fields somewhere
                final String email = emailSendingForm.getEmailAddress();
                boolean isMailSent = sendRegistrationEmail(ureq, email);
                if (isMailSent) {
                    myContent.contextPut("email", email);
                    myContent.contextPut("text", translate("step2.reg.text", email));
                    regarea.setVisible(false);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == registrationForm) {
            // Userdata entered
            if (event == Event.DONE_EVENT) {
                final String lang = registrationForm.getLangKey();
                // change language if different then current language
                if (!lang.equals(I18nManager.getInstance().getLocaleKey(ureq.getLocale()))) {
                    final Locale loc = I18nManager.getInstance().getLocaleOrDefault(lang);
                    ureq.getUserSession().setLocale(loc);
                    getTranslator().setLocale(loc);
                }

                wizardController.setCurStep(3);
                myContent.contextPut("pwdhelp", "");
                myContent.contextPut("loginhelp", "");
                myContent.contextPut("text", translate("step4.reg.text"));

                removeAsListenerAndDispose(dclController);
                dclController = new DisclaimerController(ureq, getWindowControl());
                listenTo(dclController);

                regarea.setContent(dclController.getInitialComponent());
            } else if (event == Event.CANCELLED_EVENT) {
                ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
            }
        } else if (source == dclController) {
            if (event == Event.DONE_EVENT) {

                wizardController.setCurStep(4);
                myContent.contextRemove("text");
                myContent.contextPut("pwdhelp", "");
                myContent.contextPut("loginhelp", "");
                myContent.contextPut("disclaimer", "");

                registrationForm.freeze();
                regarea.setContent(registrationForm.getInitialComponent());

                final String login = registrationForm.getLogin();
                final String pwd = registrationForm.getPassword();
                String firstName = registrationForm.getFirstName();
                String lastName = registrationForm.getLastName();

                Locale locale = I18nManager.getInstance().getLocaleOrDefault(registrationForm.getLangKey());
                // create user with mandatory fields from registrationform
                Identity persistedIdentity = getRegistrationServiceEBL().registerUser(
                        new RegisterUserParameter(login, pwd, firstName, lastName, locale, tempKey, getUserPropertyParameters()));
                if (persistedIdentity == null) {
                    showError("user.notregistered");
                } else {
                    // show last screen
                    myContent.contextPut("text", getTranslator().translate("step5.reg.text", new String[] { WebappHelper.getServletContextPath(), login }));
                }

            } else if (event == Event.CANCELLED_EVENT) {
                ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
            }
        }
    }

    /**
     * @param ureq
     * @param email
     * @return
     */
    private boolean sendRegistrationEmail(final UserRequest ureq, final String email) {
        final boolean foundUser = getUserService().userExist(email);
        // get remote address
        final String ip = ureq.getHttpReq().getRemoteAddr();
        String body = null;

        boolean isMailSent = false;
        if (!foundUser) {
            final String link = getRegistrationService().getRegistrationLink(email, ureq.getLocale().toString(), ip);
            String linkAsHtml = Formatter.getHtmlHref(link, translate("registration.link.title"));
            body = getTranslator().translate("reg.body", new String[] { linkAsHtml });

            MailMessage mailMessage = new MailMessage(email, translate("reg.subject"), body, ureq.getLocale());
            isMailSent = getMailMessageLearnService().sendMessage(mailMessage);
        } else {
            // a user exists, this is an error in the registration page
            // send email
            final Identity identity = getUserService().findIdentityByEmail(email);
            body = translate("login.body", identity.getName());
            MailMessage mailMessage = new MailMessage(email, translate("login.subject"), body, ureq.getLocale());
            isMailSent = getMailMessageLearnService().sendMessage(mailMessage);
        }
        return isMailSent;
    }

    private RegistrationServiceEBL getRegistrationServiceEBL() {
        return CoreSpringFactory.getBean(RegistrationServiceEBL.class);
    }

    private List<UserPropertyParameter> getUserPropertyParameters() {
        List<UserPropertyParameter> parameters = new ArrayList<UserPropertyParameter>();

        final List<UserPropertyHandler> userPropertyHandlers = getUserService().getUserPropertyHandlersFor(RegistrationForm2.USERPROPERTIES_FORM_IDENTIFIER, false);
        for (final UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
            final FormItem fi = registrationForm.getPropFormItem(userPropertyHandler.getName());
            parameters.add(new UserPropertyParameter(userPropertyHandler, fi));
        }
        return parameters;
    }

    @Override
    protected void doDispose() {
        //
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

    private MailMessageLearnService getMailMessageLearnService() {
        return CoreSpringFactory.getBean(MailMessageLearnService.class);
    }

}
