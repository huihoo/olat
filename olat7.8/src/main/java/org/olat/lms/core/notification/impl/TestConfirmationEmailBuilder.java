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
import org.olat.lms.core.notification.service.TestConfirmation;
import org.olat.lms.core.notification.service.TestConfirmation.TYPE;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 15.10.2012 <br>
 * 
 * @author lavinia
 */
@Component
public class TestConfirmationEmailBuilder extends ConfirmationEmailBuilder<TestConfirmation> {

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //OriginatorFirstLastName <br/>
     * variables[1] //URL to the course node <br/>
     * variables[2] //date <br/>
     * variables[3] //time <br/>
     * variables[4] //URL to the course <br/>
     * variables[5] //UriToAssessmentDetail <br/>
     * variables[6] //UriToAssessmentView <br/>
     * 
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, TestConfirmation confirmationInfo) {

        String content;
        String greeting;
        String greetingFrom;
        String footer;

        UriBuilder uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[7];
        variables[0] = confirmationInfo.getOriginatorFirstLastName();

        String urlToCourseNode = uriBuilder.getURIToCourseNode(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getCourseNodeId());
        variables[1] = getQuotedHtmlHref(urlToCourseNode, confirmationInfo.getCourseNodeTitle());
        // System.out.println("urlToCourseNode: " + variables[1]);

        variables[2] = DateUtil.extractDate(confirmationInfo.getDateTime(), recipientsLocale);
        variables[3] = DateUtil.extractTime(confirmationInfo.getDateTime(), recipientsLocale);
        String uRIToCourse = uriBuilder.getURIToCourse(confirmationInfo.getCourseRepositoryEntryId());
        variables[4] = getQuotedHtmlHref(uRIToCourse, confirmationInfo.getCourseName());
        // System.out.println("course url: " + variables[4]);

        // choose the right translation key, the variables are the same for each template
        if (TYPE.TEST_SUBMIT_STUDENT.equals(confirmationInfo.getSubtype())) {
            content = translator.translate("mail.body.confirmation.student.test", variables);
        } else if (TYPE.TEST_SUBMIT_TUTOR.equals(confirmationInfo.getSubtype())) {
            variables[5] = getQuotedHtmlHref(uriBuilder.getUriToAssessmentDetail(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getOriginatorIdentity()
                    .getKey(), confirmationInfo.getCourseNodeId()), translator.translate("mail.body.assessment.detail.link"));
            variables[6] = getQuotedHtmlHref(
                    uriBuilder.getUriToAssessmentView(confirmationInfo.getCourseRepositoryEntryId(), confirmationInfo.getOriginatorIdentity().getKey()),
                    translator.translate("mail.body.assessment.view.link"));
            content = translator.translate("mail.body.confirmation.tutor.test", variables);
        } else if (TYPE.TEST_REPLACED.equals(confirmationInfo.getSubtype())) {
            variables[5] = confirmationInfo.getCourseNodeTitle();
            content = translator.translate("mail.body.confirmation.test.replaced", variables);
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
    protected String getSubjectForConfirmation(Locale recipientsLocale, TestConfirmation confirmationInfo) {
        String[] nameVariable = new String[2];
        nameVariable[0] = confirmationInfo.getOriginatorFirstLastName();
        nameVariable[1] = confirmationInfo.getCourseName();
        String subjectTranslationKey = "";
        if (TYPE.TEST_SUBMIT_STUDENT.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.student.test";
        } else if (TYPE.TEST_SUBMIT_TUTOR.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.tutor.test";
        } else if (TYPE.TEST_REPLACED.equals(confirmationInfo.getSubtype())) {
            subjectTranslationKey = "mail.subject.confirmation.test.replaced";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey, nameVariable);
    }

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.TEST;
    }
}
