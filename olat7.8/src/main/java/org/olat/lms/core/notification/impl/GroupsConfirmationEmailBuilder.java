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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.data.group.BusinessGroup;
import org.olat.lms.core.notification.service.ConfirmationInfo.CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.GroupsConfirmationInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: Oct 18, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class GroupsConfirmationEmailBuilder extends ConfirmationEmailBuilder<GroupsConfirmationInfo> {

    private static final String MAIL_CONFIRMATION_TEMPLATE_PATH = "org/olat/lms/core/notification/impl/_content/resourceEntriesMailTemplate.html";

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.GROUPS;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, GroupsConfirmationInfo groupsConfirmationInfo) {
        String subjectTranslationKey = "";
        if (GroupsConfirmationInfo.GROUPS_CONFIRMATION_TYPE.DELETE_GROUPS.equals(groupsConfirmationInfo.getGroupsConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.delete.groups";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey);
    }

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //number of months to filter inactive groups <br/>
     * variables[1] //number of days till when inactive groups will be deleted <br/>
     */
    @Override
    protected ResourceEntriesConfirmationMailBody getMailBody(Locale recipientsLocale, GroupsConfirmationInfo groupsConfirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[] { String.valueOf(groupsConfirmationInfo.getNumberOfMonths()), String.valueOf(groupsConfirmationInfo.getNumberOfDays()) };

        // choose the right translation key, the variables are the same for each template
        if (GroupsConfirmationInfo.GROUPS_CONFIRMATION_TYPE.DELETE_GROUPS.equals(groupsConfirmationInfo.getGroupsConfirmationType())) {
            content = translator.translate("mail.body.confirmation.delete.groups", variables);
        } else {
            content = "INVALID CONFIRMATION CONTENT"; // UNKNOWN CONFIRMATION TYPE
        }

        greeting = translator.translate("mail.body.greeting");
        greetingFrom = translator.translate("mail.body.greeting.from");

        String olatUrlAsHtmlHref = mailBuilderCommons.getOlatUrlAsHtmlHref();
        footer = translator.translate("mail.footer.confirmation", getStringArray(olatUrlAsHtmlHref));
        List<String> groupEntries = getGroupEntries(groupsConfirmationInfo.getGroups(), translator);
        Collections.sort(groupEntries);
        return new ResourceEntriesConfirmationMailBody(content, greeting, greetingFrom, footer, groupEntries);
    }

    private List<String> getGroupEntries(List<BusinessGroup> groups, Translator translator) {
        List<String> groupEntries = new ArrayList<String>();
        for (BusinessGroup group : groups) {
            groupEntries.add(getGroupEntry(group, translator));
        }
        return groupEntries;
    }

    private String getGroupEntry(BusinessGroup group, Translator translator) {
        String uriToGroup = mailBuilderCommons.getUriBuilder().getURIToGroup(group.getKey());
        String[] variables = new String[] { getQuotedHtmlHref(uriToGroup, group.getName()) };
        return translator.translate("mail.body.group.entry", variables);
    }

    protected String getConfirmationTemplatePath() {
        return MAIL_CONFIRMATION_TEMPLATE_PATH;
    }

}
