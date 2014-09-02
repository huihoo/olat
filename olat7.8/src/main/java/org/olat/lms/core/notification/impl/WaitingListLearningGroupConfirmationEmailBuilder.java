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
import org.olat.lms.core.notification.service.WaitingListLearningGroupConfirmationInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class WaitingListLearningGroupConfirmationEmailBuilder extends ConfirmationEmailBuilder<WaitingListLearningGroupConfirmationInfo> {

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.LEARNING_WAITING_LIST;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, WaitingListLearningGroupConfirmationInfo confirmationInfo) {
        String[] subjectVariables = new String[] { confirmationInfo.getGroupName() };
        String subjectTranslationKey = "";
        if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.ADD_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.add.to.waiting.list";
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.REMOVE_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.remove.from.waiting.list";
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.MOVE_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.add.to.group";
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
     * variables[3] //URL or string to group <br/>
     * variables[4] //URL to course <br/>
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, WaitingListLearningGroupConfirmationInfo confirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[7];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        variables[1] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[2] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);

        String uriToGroup = uriBuilder.getURIToGroup(confirmationInfo.getGroupId());

        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[4] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());

        // choose the right translation key, the variables are the same for each template
        if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.ADD_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            variables[3] = getQuotedString(confirmationInfo.getGroupName());
            content = translator.translate("mail.body.confirmation.add.to.group.waiting.list", variables);
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.REMOVE_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            variables[3] = getQuotedString(confirmationInfo.getGroupName());
            content = translator.translate("mail.body.confirmation.remove.from.group.waiting.list", variables);
        } else if (AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE.MOVE_USER_WAITING_LIST_LEARNING_GROUP.equals(confirmationInfo.getGroupConfirmationType())) {
            variables[3] = getQuotedHtmlHref(uriToGroup, confirmationInfo.getGroupName());
            content = translator.translate("mail.body.confirmation.move.group.waiting.list", variables);
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
