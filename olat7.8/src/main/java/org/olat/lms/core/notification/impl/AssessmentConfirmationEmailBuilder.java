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

import org.olat.lms.core.notification.service.AssessmentConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationInfo.CONFIRMATION_TYPE;
import org.olat.presentation.framework.core.translator.Translator;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Nov 7, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class AssessmentConfirmationEmailBuilder extends ConfirmationEmailBuilder<AssessmentConfirmationInfo> {

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.ASSESSMENT;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, AssessmentConfirmationInfo confirmationInfo) {
        String subjectTranslationKey = "";
        if (isCorrectType(confirmationInfo)) {
            subjectTranslationKey = "mail.subject.confirmation.assessment";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey);
    }

    private boolean isCorrectType(AssessmentConfirmationInfo confirmationInfo) {
        if (AssessmentConfirmationInfo.TYPE.ASSESSMENT.equals(confirmationInfo.getSubtype())
                || AssessmentConfirmationInfo.TYPE.PORTFOLIO.equals(confirmationInfo.getSubtype())
                || AssessmentConfirmationInfo.TYPE.TASK.equals(confirmationInfo.getSubtype())
                || AssessmentConfirmationInfo.TYPE.TEST.equals(confirmationInfo.getSubtype())) {
            return true;
        }
        return false;
    }

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //URL to the course node <br/>
     * variables[1] //URL to the course <br/>
     */

    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, AssessmentConfirmationInfo confirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[2];

        String uriToCourseNode = uriBuilder.getURIToCourseNode(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId());
        variables[0] = getQuotedHtmlHref(uriToCourseNode, confirmationInfo.getCourseNodeTitle());

        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[1] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());

        // choose the right translation key, the variables are the same for each template
        if (AssessmentConfirmationInfo.TYPE.ASSESSMENT.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.assessment.assessment", variables);
        } else if (AssessmentConfirmationInfo.TYPE.PORTFOLIO.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.assessment.portfolio", variables);
        } else if (AssessmentConfirmationInfo.TYPE.TASK.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.assessment.task", variables);
        } else if (AssessmentConfirmationInfo.TYPE.TEST.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.assessment.test", variables);
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
