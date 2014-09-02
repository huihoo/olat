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

import org.olat.lms.core.notification.service.ConfirmationInfo.CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.TopicChangeConfirmationInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Nov 6, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class TopicChangeConfirmationEmailBuilder extends ConfirmationEmailBuilder<TopicChangeConfirmationInfo> {

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.TOPIC_CHANGE;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, TopicChangeConfirmationInfo confirmationInfo) {
        String[] subjectVariables = new String[] { confirmationInfo.getTopicName() };
        String subjectTranslationKey = "";
        if (TopicChangeConfirmationInfo.TYPE.EDIT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.topic.edit";
        } else if (TopicChangeConfirmationInfo.TYPE.DELETE.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.topic.delete";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey, subjectVariables);
    }

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //OriginatorFirstLastName <br/>
     * variables[1] //URL to topic <br/>
     * variables[2] //URL to course <br/>
     * variables[3] //date <br/>
     * variables[4] //time <br/>
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, TopicChangeConfirmationInfo confirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[7];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        variables[3] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[4] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);

        String uriToTopic = uriBuilder.getUriToTopicAssignmentDescriptionTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                confirmationInfo.getTopicId());

        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[2] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());

        // choose the right translation key, the variables are the same for each template
        if (TopicChangeConfirmationInfo.TYPE.EDIT.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(uriToTopic, confirmationInfo.getTopicName());
            content = translator.translate("mail.body.confirmation.topic.edit", variables);
        } else if (TopicChangeConfirmationInfo.TYPE.DELETE.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedString(confirmationInfo.getTopicName());
            content = translator.translate("mail.body.confirmation.topic.delete", variables);
        } else {
            content = "INVALID CONFIRMATION CONTENT"; // UNKNOWN CONFIRMATION TYPE
        }

        greeting = translator.translate("mail.body.greeting");
        greetingFrom = translator.translate("mail.body.greeting.from");

        String olatUrlAsHtmlHref = mailBuilderCommons.getOlatUrlAsHtmlHref();
        footer = translator.translate("mail.footer.confirmation", getStringArray(olatUrlAsHtmlHref));
        return new ConfirmationMailBody(content, greeting, greetingFrom, footer);
    }

}
