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

import java.util.List;
import java.util.Locale;

import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builds the notification email template.
 * 
 * Initial Date: 22.12.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class EmailBuilder {

    private static final String MAIL_TEMPLATE_PATH = "org/olat/lms/core/notification/impl/_content/notificationMailTemplate.html";
    private static final String MAIL_BODY_TEMPLATE_PROPERTY = "mailBody";

    @Value("${notification.mail.from.address}")
    private String mailFormAddress;

    @Autowired
    private MailBuilderCommons mailBuilderCommons;

    String getSubjectForImmediatelyNotification(Locale locale) {
        return mailBuilderCommons.getEmailTranslator(EmailBuilder.class, locale).translate("mail.subject");
    }

    public TemplateMailTO getTemplateMailTO(String recipient, List<NotificationEventTO> eventToList) {
        Locale locale = getUserLocaleFromListOfEvents(eventToList);
        TemplateMailTO templateMailTO = TemplateMailTO.getValidInstance(recipient, mailFormAddress, getSubjectForImmediatelyNotification(locale), MAIL_TEMPLATE_PATH);
        MailBody mailBody = getMailBody(eventToList);
        templateMailTO.addTemplateProperty(MAIL_BODY_TEMPLATE_PROPERTY, mailBody);
        return templateMailTO;
    }

    private MailBody getMailBody(List<NotificationEventTO> eventToList) {
        Locale locale = getUserLocaleFromListOfEvents(eventToList);
        return new MailBody(eventToList, mailBuilderCommons, locale);
    }

    private Locale getUserLocaleFromListOfEvents(List<NotificationEventTO> eventToList) {
        return ((NotificationEventTO) eventToList.get(0)).getLocale();
    }

}
