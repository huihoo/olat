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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.user.administration;

import java.util.Locale;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.Preferences;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.registration.RegistrationController;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Form to send a email to the user with a link to change its password.
 * <P>
 * Initial Date: 26 mai 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class SendTokenToUserForm extends FormBasicController {

    private final Identity user;

    public SendTokenToUserForm(final UserRequest ureq, final WindowControl wControl, final Identity treatedIdentity) {
        super(ureq, wControl);
        user = treatedIdentity;
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("form.token.new.title");
        setFormDescription("form.token.new.description");
        uifactory.addFormSubmitButton("submit", "form.token.new.title", formLayout);
    }

    @Override
    protected void doDispose() {
        // auto disposed by basic controller
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        sendToken(ureq);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    public FormItem getInitialFormItem() {
        return flc;
    }

    private void sendToken(final UserRequest ureq) {

        // check if user has an OLAT provider token, otherwhise a pwd change makes no sense
        final Authentication auth = getBaseSecurityEBL().findOlatAuthentication(user);
        if (auth == null) {
            showWarning("password.cantchange");
            return;
        }

        final Preferences prefs = user.getUser().getPreferences();
        final Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
        final String emailAdress = getUserService().getUserProperty(user.getUser(), UserConstants.EMAIL, locale);
        final Translator userTrans = PackageUtil.createPackageTranslator(RegistrationController.class, locale);

        final String link = getChangePasswordLink(ureq, emailAdress, locale.toString());
        String linkAsHtml = Formatter.getHtmlHref(link, userTrans.translate("pwchange.link.title")); // TODO: translate

        final String subject = userTrans.translate("pwchange.subject");
        final String bodyTemplate = userTrans.translate("pwchange.body", new String[] { linkAsHtml });

        MailMessage mailMessage = new MailMessage(emailAdress, subject, bodyTemplate, locale);
        getMailMessageLearnService().sendMessage(mailMessage);
    }

    private String getChangePasswordLink(final UserRequest ureq, final String emailAdress, String localeString) {
        final String ip = ureq.getHttpReq().getRemoteAddr();
        return getRegistrationService().getChangePasswordLink(emailAdress, localeString, ip);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
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
