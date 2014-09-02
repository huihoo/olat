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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.lms.core.notification.service.MailMessage;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.support.mail.service.TemplateWithAttachmentMailTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mail template builder.
 * 
 * Initial Date: 28.09.2012 <br>
 * 
 * @author lavinia
 */
@Component
public class MailMessageEmailBuilder {

    @Autowired
    private MailBuilderCommons mailBuilderCommons;

    static final String MAIL_BODY_TEMPLATE_PROPERTY = "mailBody";
    static final String MAIL_FOOTER_TEMPLATE_PROPERTY = "footer";

    static final String MAIL_MESSAGE_TEMPLATE_PATH = "org/olat/lms/core/notification/impl/_content/mailWithFooterTemplate.html";

    /**
     * Builds a TemplateWithAttachmentMailTO template from a MailMessage.
     */
    public TemplateWithAttachmentMailTO getMailTemplate(String toEmailAddress, MailMessage message) {
        TemplateWithAttachmentMailTO template = TemplateWithAttachmentMailTO.getValidInstance(toEmailAddress, getFromEmailAddress(message), message.getSubject(),
                message.getBody(), MAIL_MESSAGE_TEMPLATE_PATH);

        List<File> attachments = message.getAttachments();
        for (File attachment : attachments) {
            template.addAttachment(attachment);
        }
        template.setCcMailAddress(message.getCCEmailAddress());

        String mailBodyAsHTML = StringHelper.convertLineBreaksToHTML(message.getBody());
        template.addTemplateProperty(MAIL_BODY_TEMPLATE_PROPERTY, mailBodyAsHTML);

        String footer = getFooter(message.getLocale(), message.getFromFirstLastOlatUserName());
        template.addTemplateProperty(MAIL_FOOTER_TEMPLATE_PROPERTY, footer);
        return template;
    }

    private String getFromEmailAddress(MailMessage message) {
        String fromEmailAddress = message.getFromEmailAddress();
        if (fromEmailAddress == null || fromEmailAddress.equals("")) {
            fromEmailAddress = mailBuilderCommons.getSystemMailFromAddress();
        }
        return fromEmailAddress;
    }

    private String getFooter(Locale locale, String sender) {
        Translator translator = mailBuilderCommons.getEmailTranslator(MailMessageEmailBuilder.class, locale);
        if (sender != null) {
            String[] translatorArguments = new String[2];
            translatorArguments[0] = sender;
            translatorArguments[1] = mailBuilderCommons.getOlatUrlAsHtmlHref();
            return translator.translate("mail.message.default.footer", translatorArguments);
        } else {
            String[] translatorArguments = new String[1];
            translatorArguments[0] = mailBuilderCommons.getOlatUrlAsHtmlHref();
            return translator.translate("mail.footer.confirmation", translatorArguments);
        }
    }
}
