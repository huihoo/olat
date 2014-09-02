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

import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.Identity;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.data.user.Preferences;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.wizard.WizardInfoController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controlls the change password workflow.
 * <P>
 * 
 * @author Sabina Jeger
 */
public class PwChangeController extends BasicController {

    private static final Logger LOG = LoggerHelper.getLogger();

    private final VelocityContainer myContent;
    private final Panel pwarea;
    private WizardInfoController wic;
    private final String pwKey;
    private PwChangeForm pwf;
    private TemporaryKeyImpl tempKey;
    private EmailOrUsernameFormController emailOrUsernameCtr;
    private Link pwchangeHomelink;

    /**
     * Controller to change a user's password.
     * 
     * @param ureq
     * @param wControl
     */
    public PwChangeController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        myContent = createVelocityContainer("pwchange");
        wic = new WizardInfoController(ureq, 4);
        myContent.put("pwwizard", wic.getInitialComponent());
        pwarea = new Panel("pwarea");
        myContent.put("pwarea", pwarea);
        pwKey = ureq.getHttpReq().getParameter("key");
        if (pwKey == null || pwKey.equals("")) {
            // no temporarykey is given, we assume step 1
            createEmailForm(ureq, wControl);
            putInitialPanel(myContent);
        } else {
            // we check if given key is a valid temporary key
            tempKey = getRegistrationService().loadTemporaryKeyByRegistrationKey(pwKey);
            // if key is not valid we redirect to first page
            if (tempKey == null) {
                // error, there should be an entry
                getWindowControl().setError(translate("pwkey.missingentry"));
                createEmailForm(ureq, wControl);
                // load view in layout
                final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, myContent, null);
                putInitialPanel(layoutCtr.getInitialComponent());
            } else {
                wic.setCurStep(3);
                pwf = new PwChangeForm(ureq, wControl, tempKey);
                listenTo(pwf);
                myContent.contextPut("pwdhelp", translate("pwdhelp"));
                myContent.contextPut("text", translate("step3.pw.text"));
                pwarea.setContent(pwf.getInitialComponent());
                // load view in layout
                final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, myContent, null);
                putInitialPanel(layoutCtr.getInitialComponent());
            }
        }
    }

    /**
     * just needed for creating EmailForm
     */
    private void createEmailForm(final UserRequest ureq, final WindowControl wControl) {
        myContent.contextPut("title", translate("step1.pw.title"));
        myContent.contextPut("text", translate("step1.pw.text"));
        removeAsListenerAndDispose(emailOrUsernameCtr);
        emailOrUsernameCtr = new EmailOrUsernameFormController(ureq, wControl);
        listenTo(emailOrUsernameCtr);
        pwarea.setContent(emailOrUsernameCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == pwchangeHomelink) {
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == pwf) {
            // pwchange Form was clicked
            if (event == Event.DONE_EVENT) { // form
                // validation was ok
                wic.setCurStep(4);
                myContent.contextPut("pwdhelp", "");
                myContent.contextPut("text", translate("step4.pw.text"));
                pwchangeHomelink = LinkFactory.createLink("pwchange.homelink", myContent, this);
                // pwf.setVisible(false);
                pwarea.setVisible(false);
                final Identity identToChange = getUserService().findIdentityByEmail(tempKey.getEmailAddress());
                if (identToChange == null || !pwf.saveFormData(identToChange)) {
                    getWindowControl().setError(translate("pwchange.failed"));
                }
                getRegistrationService().deleteTemporaryKeyWithId(tempKey.getRegistrationKey());
            } else if (event == Event.CANCELLED_EVENT) {
                getWindowControl().setInfo(translate("pwform.cancelled"));
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        } else if (source == emailOrUsernameCtr) {
            // eMail Form was clicked
            if (event == Event.DONE_EVENT) { // form
                // Email requested for tempkey save the fields somewhere
                String emailOrUsername = emailOrUsernameCtr.getEmailOrUsername();
                emailOrUsername = emailOrUsername.trim();

                Identity identity = getBaseSecurityEBL().findIdentityByNameOrEmail(emailOrUsername);
                if (identity != null) {
                    // check if user has an OLAT provider token, otherwhise a pwd change makes no sense
                    final Authentication auth = getBaseSecurityEBL().findOlatAuthentication(identity);
                    if (auth == null) {
                        getWindowControl().setWarning(translate("password.cantchange"));
                        return;
                    }
                    final Preferences prefs = identity.getUser().getPreferences();
                    final Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
                    ureq.getUserSession().setLocale(locale);
                    myContent.contextPut("locale", locale);

                    boolean isSent = sendEmail(ureq, identity, locale);
                    if (isSent) {
                        // prepare next step
                        showStep2();
                    }
                } else {
                    showStep2();
                    LOG.info("Change password request failed for the username or email: " + emailOrUsername);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    private void showStep2() {
        wic.setCurStep(2);
        myContent.contextPut("text", translate("step2.pw.text"));
        emailOrUsernameCtr.getInitialComponent().setVisible(false);
    }

    /**
     * @return
     */
    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private boolean sendEmail(final UserRequest ureq, Identity identity, Locale locale) {
        boolean isEmailSent = false;
        final String ip = ureq.getHttpReq().getRemoteAddr();
        String emailAdress = identity.getAttributes().getEmail();
        final String link = getRegistrationService().getChangePasswordLink(emailAdress, locale.toString(), ip);
        String linkAsHtml = Formatter.getHtmlHref(link, translate("pwchange.link.title"));

        String body = translate("pwchange.body", new String[] { linkAsHtml });
        String subject = translate("pwchange.subject");
        Locale controllerLocale = this.getTranslator().getLocale();
        if (!controllerLocale.getLanguage().equals(locale.getLanguage())) {
            Translator userTranslator = PackageUtil.createPackageTranslator(this.getClass(), locale, getTranslator());
            linkAsHtml = Formatter.getHtmlHref(link, userTranslator.translate("pwchange.link.title"));
            body = userTranslator.translate("pwchange.body", new String[] { linkAsHtml });
            subject = userTranslator.translate("pwchange.subject");
        }

        MailMessage mailMessage = new MailMessage(emailAdress, subject, body, locale);
        isEmailSent = getMailMessageLearnService().sendMessage(mailMessage);
        return isEmailSent;
    }

    @Override
    protected void doDispose() {
        if (wic != null) {
            wic.dispose();
            wic = null;
        }
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
