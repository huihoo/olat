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
import org.olat.lms.core.notification.service.TopicConfirmation;
import org.olat.lms.core.notification.service.TopicConfirmation.TYPE;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 12.10.2012 <br>
 * 
 * @author lavinia
 */
@Component
public class TopicConfirmationEmailBuilder extends ConfirmationEmailBuilder<TopicConfirmation> {
    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //OriginatorFirstLastName <br/>
     * variables[1] //URL to the course node/topic/folder <br/>
     * variables[2] //date <br/>
     * variables[3] //time <br/>
     * variables[4] //URL to the course <br/>
     * 
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, TopicConfirmation confirmationInfo) {

        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[5];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        String urlToCourseNode = uriBuilder.getURIToCourseNode(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId());
        variables[1] = getQuotedHtmlHref(urlToCourseNode, confirmationInfo.getFileName());
        // System.out.println("urlToCourseNode: " + variables[1]);

        variables[2] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[3] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);
        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[4] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());
        // System.out.println("course url: " + variables[4]);

        // choose the right translation key, the variables are the same for each template
        if (TYPE.TOPIC_DROP_STUDENT.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(
                    uriBuilder.getUriToTopicAssignmentFolderTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                            confirmationInfo.getProjectId()), confirmationInfo.getFileName());
            content = translator.translate("mail.body.confirmation.student.drop", variables);
        } else if (TYPE.TOPIC_DROP_TUTOR.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(
                    uriBuilder.getUriToTopicAssignmentFolderTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                            confirmationInfo.getProjectId()), confirmationInfo.getFileName());
            content = translator.translate("mail.body.confirmation.tutor.drop", variables);
        } else if (TYPE.TOPIC_RETURN_STUDENT.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(
                    uriBuilder.getUriToTopicAssignmentFolderTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(),
                            confirmationInfo.getProjectId()), confirmationInfo.getFileName());
            content = translator.translate("mail.body.confirmation.student.return", variables);
        } else {
            content = "INVALID CONFIRMATION CONTENT"; // UNKNOWN CONFIRMATION TYPE
        }

        greeting = translator.translate("mail.body.greeting");
        greetingFrom = translator.translate("mail.body.greeting.from");

        String olatUrlAsHtmlHref = mailBuilderCommons.getOlatUrlAsHtmlHref();
        footer = translator.translate("mail.footer.confirmation", getStringArray(olatUrlAsHtmlHref));
        return new ConfirmationMailBody(content, greeting, greetingFrom, footer);
    }

    /**
     * Subject is dependent on confirmation type.
     */
    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, TopicConfirmation confirmationInfo) {
        String[] nameVariable = new String[2];
        nameVariable[0] = confirmationInfo.getOriginatorFirstLastName();
        nameVariable[1] = confirmationInfo.getCourseName();
        String subjectTranslationKey = "";
        if (TYPE.TOPIC_DROP_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.student.drop";
        } else if (TYPE.TOPIC_DROP_TUTOR.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.tutor.drop";
        } else if (TYPE.TOPIC_RETURN_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.student.return";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey, nameVariable);
    }

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.TOPIC;
    }
}
