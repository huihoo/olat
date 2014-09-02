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
import org.olat.lms.core.notification.service.TaskConfirmation;
import org.olat.lms.core.notification.service.TaskConfirmation.TYPE;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 12.10.2012 <br>
 * 
 * @author lavinia
 */
@Component
public class TaskConfirmationEmailBuilder extends ConfirmationEmailBuilder<TaskConfirmation> {

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //OriginatorFirstLastName <br/>
     * variables[1] //URL to the course node <br/>
     * variables[2] //date <br/>
     * variables[3] //time <br/>
     * variables[4] //URL to the course <br/>
     * variables[5] //task name <br/>
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, TaskConfirmation confirmationInfo) {

        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[6];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        String urlToCourseNode = uriBuilder.getURIToCourseNode(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId());
        variables[1] = getQuotedHtmlHref(urlToCourseNode, confirmationInfo.getFileName());
        variables[2] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[3] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);
        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[4] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());

        // choose the right translation key, the variables are the same for each template
        if (TYPE.TASK_DROP_STUDENT.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.student.drop", variables);
        } else if (TYPE.TASK_DROP_TUTOR.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(uriBuilder.getUriToAssessmentDetail(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getOriginatorIdentity()
                    .getKey(), confirmationInfo.getCourseNodeId()), confirmationInfo.getFileName());
            content = translator.translate("mail.body.confirmation.tutor.drop", variables);
        } else if (TYPE.TASK_RETURN_STUDENT.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(
                    uriBuilder.getUriToTopicAssignmentFolderTab(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId(), Long.valueOf(0)),
                    confirmationInfo.getFileName());
            content = translator.translate("mail.body.confirmation.student.return", variables);
        } else if (TYPE.TASK_DELETE_STUDENT.equals(confirmationInfo.getSubtype())) {
            variables[1] = getQuotedHtmlHref(urlToCourseNode, confirmationInfo.getCourseNodeTitle());
            variables[5] = confirmationInfo.getFileName();
            content = translator.translate("mail.body.confirmation.task.deleted", variables);
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
    protected String getSubjectForConfirmation(Locale recipientsLocale, TaskConfirmation confirmationInfo) {
        String[] nameVariable = new String[2];
        nameVariable[0] = confirmationInfo.getOriginatorFirstLastName();
        nameVariable[1] = confirmationInfo.getCourseName();
        String subjectTranslationKey = "";
        if (TYPE.TASK_DROP_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.student.drop";
        } else if (TYPE.TASK_DROP_TUTOR.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.tutor.drop";
        } else if (TYPE.TASK_RETURN_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.student.return";
        } else if (TYPE.TASK_DELETE_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.task.deleted";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey, nameVariable);
    }

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.TASK;
    }
}
