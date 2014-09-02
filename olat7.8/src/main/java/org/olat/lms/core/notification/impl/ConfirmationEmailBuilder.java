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
package org.olat.lms.core.notification.impl;

import java.util.Locale;

import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Different confirmation types have different emails, this is the base class for the email builder of all confirmation types: dependent in the
 * <code>ConfirmationInfo</code> type.
 * 
 * Initial Date: 19.09.2012 <br>
 * 
 * @author Branislav Balaz
 */
public abstract class ConfirmationEmailBuilder<T extends ConfirmationInfo> {

    static final String MAIL_BODY_TEMPLATE_PROPERTY = "mailBody";
    private static final String MAIL_CONFIRMATION_TEMPLATE_PATH = "org/olat/lms/core/notification/impl/_content/confirmationMailTemplate.html";

    @Autowired
    protected MailBuilderCommons mailBuilderCommons;

    public abstract ConfirmationInfo.CONFIRMATION_TYPE getConfirmationInfoType();

    protected Translator getTranslator(Locale locale) {
        return mailBuilderCommons.getEmailTranslator(this.getClass(), locale);
    }

    /**
     * @return the mail template filled in with the input parameters.
     */
    public TemplateMailTO getTemplateMailTO(RecipientInfo currentRecipientInfo, T confirmationInfo) {
        TemplateMailTO templateMailTO = null;
        if (currentRecipientInfo != null) {
            String subject = getSubjectForConfirmation(currentRecipientInfo.getRecipientsLocale(), confirmationInfo);
            templateMailTO = TemplateMailTO.getValidInstance(currentRecipientInfo.getRecipientsEmail(), mailBuilderCommons.getSystemMailFromAddress(), subject,
                    getConfirmationTemplatePath());
            ConfirmationMailBody mailBody = getMailBody(currentRecipientInfo.getRecipientsLocale(), confirmationInfo);
            templateMailTO.addTemplateProperty(ConfirmationEmailBuilder.MAIL_BODY_TEMPLATE_PROPERTY, mailBody);
        }
        return templateMailTO;
    }

    protected String[] getStringArray(String htmlHref) {
        String[] variables = new String[] { htmlHref };
        return variables;
    }

    protected String getQuotedHtmlHref(String url, String title) {
        return "&quot;" + Formatter.getHtmlHref(url, title) + "&quot;";
    }

    protected String getQuotedString(String string) {
        return "&quot;" + string + "&quot;";
    }

    /**
     * The subject is usually dependent on the ConfirmationInfo type.
     */
    abstract protected String getSubjectForConfirmation(Locale recipientsLocale, T confirmationInfo);

    /**
     * The mail body is also dependent on the ConfirmationInfo type.
     */
    abstract protected ConfirmationMailBody getMailBody(Locale recipientsLocale, T confirmationInfo);

    protected String getConfirmationTemplatePath() {
        return MAIL_CONFIRMATION_TEMPLATE_PATH;
    }

}
