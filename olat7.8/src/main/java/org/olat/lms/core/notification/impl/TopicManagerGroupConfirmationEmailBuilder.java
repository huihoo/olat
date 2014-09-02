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

import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationInfo.CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.TopicManagerGroupConfirmationInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class TopicManagerGroupConfirmationEmailBuilder extends ConfirmationEmailBuilder<TopicManagerGroupConfirmationInfo> {

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.TOPIC_MANAGERS;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, TopicManagerGroupConfirmationInfo confirmationInfo) {
        String[] subjectVariables = new String[] { confirmationInfo.getTopicName() };
        String subjectTranslationKey = "";
        if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.ADD_USER_TO_TOPIC_MANAGERS.equals(confirmationInfo.getGroupConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.add.topic.manager";
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.REMOVE_USER_FROM_TOPIC_MANAGERS.equals(confirmationInfo.getGroupConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.remove.topic.manager";
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
     * variables[1] //date <br/>
     * variables[2] //time <br/>
     * variables[3] //URL to topic<br/>
     * variables[4] //URL to topic assignment <br/>
     * variables[5] //URL to course <br/>
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, TopicManagerGroupConfirmationInfo confirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);
        // TODO: it should be only 6 elements
        String[] variables = new String[7];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        variables[1] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[2] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);

        String uriToTopic = uriBuilder.getUriToTopicAssignmentDescriptionTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                confirmationInfo.getTopicId());
        variables[3] = getQuotedHtmlHref(uriToTopic, confirmationInfo.getTopicName());

        String uriToProject = uriBuilder.getURIToProject(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                confirmationInfo.getProjectId());
        variables[4] = getQuotedHtmlHref(uriToProject, confirmationInfo.getProjectName());

        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[5] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());

        // choose the right translation key, the variables are the same for each template
        if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.ADD_USER_TO_TOPIC_MANAGERS.equals(confirmationInfo.getGroupConfirmationType())) {
            content = translator.translate("mail.body.confirmation.add.topic.manager", variables);
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.REMOVE_USER_FROM_TOPIC_MANAGERS.equals(confirmationInfo.getGroupConfirmationType())) {
            content = translator.translate("mail.body.confirmation.remove.topic.manager", variables);
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
