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

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.notification.service.ConfirmationInfo.CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.RepositoryEntriesConfirmationInfo;
import org.olat.presentation.framework.core.translator.Translator;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: Oct 23, 2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class RepositoryEntriesConfirmationEmailBuilder extends ConfirmationEmailBuilder<RepositoryEntriesConfirmationInfo> {

    private static final String MAIL_CONFIRMATION_TEMPLATE_PATH = "org/olat/lms/core/notification/impl/_content/resourceEntriesMailTemplate.html";

    @Override
    public CONFIRMATION_TYPE getConfirmationInfoType() {
        return CONFIRMATION_TYPE.REPOSITORY_ENTRIES;
    }

    @Override
    protected String getSubjectForConfirmation(Locale recipientsLocale, RepositoryEntriesConfirmationInfo repositoryEntriesConfirmationInfo) {
        String subjectTranslationKey = "";
        if (RepositoryEntriesConfirmationInfo.REPOSITORY_ENTRIES_CONFIRMATION_TYPE.DELETE_REPOSITORY_ENTRIES.equals(repositoryEntriesConfirmationInfo
                .getRepositoryEntriesConfirmationType())) {
            subjectTranslationKey = "mail.subject.confirmation.delete.repository.entries";
        } else {
            subjectTranslationKey = "INVALID.CONFIRMATION.SUBJECT.KEY"; // UNKNOWN CONFIRMATION TYPE
        }
        return getTranslator(recipientsLocale).translate(subjectTranslationKey);
    }

    /**
     * Builds the mail body by using for each type different translation key, and passing the variables into the translated string. <br/>
     * The variables is an array of parameters specific to each type. Please use exactly this order while defining new translation keys. <br/>
     * <p>
     * variables[0] //number of months to filter inactive repository entries <br/>
     * variables[1] //number of days till when inactive repository entries will be deleted <br/>
     */
    @Override
    protected ConfirmationMailBody getMailBody(Locale recipientsLocale, RepositoryEntriesConfirmationInfo repositoryEntriesConfirmationInfo) {
        String content;
        String greeting;
        String greetingFrom;
        String footer;

        Translator translator = mailBuilderCommons.getEmailTranslator(ConfirmationMailBody.class, recipientsLocale);

        String[] variables = new String[] { String.valueOf(repositoryEntriesConfirmationInfo.getNumberOfMonths()),
                String.valueOf(repositoryEntriesConfirmationInfo.getNumberOfDays()) };

        // choose the right translation key, the variables are the same for each template
        if (RepositoryEntriesConfirmationInfo.REPOSITORY_ENTRIES_CONFIRMATION_TYPE.DELETE_REPOSITORY_ENTRIES.equals(repositoryEntriesConfirmationInfo
                .getRepositoryEntriesConfirmationType())) {
            content = translator.translate("mail.body.confirmation.delete.repository.entries", variables);
        } else {
            content = "INVALID CONFIRMATION CONTENT"; // UNKNOWN CONFIRMATION TYPE
        }

        greeting = translator.translate("mail.body.greeting");
        greetingFrom = translator.translate("mail.body.greeting.from");

        String olatUrlAsHtmlHref = mailBuilderCommons.getOlatUrlAsHtmlHref();
        footer = translator.translate("mail.footer.confirmation", getStringArray(olatUrlAsHtmlHref));
        List<String> repositoryEntries = getRepositoryEntries(repositoryEntriesConfirmationInfo.getRepositoryEntries(), translator);
        Collections.sort(repositoryEntries);
        return new ResourceEntriesConfirmationMailBody(content, greeting, greetingFrom, footer, repositoryEntries);
    }

    private List<String> getRepositoryEntries(List<RepositoryEntry> repositoryEntries, Translator translator) {
        List<String> repositories = new ArrayList<String>();
        for (RepositoryEntry repositoryEntry : repositoryEntries) {
            repositories.add(getRepositoryEntry(repositoryEntry, translator));
        }
        return repositories;
    }

    private String getRepositoryEntry(RepositoryEntry repositoryEntry, Translator translator) {
        String uriToRepositoryEntry = mailBuilderCommons.getUriBuilder().getURIToCourse(repositoryEntry.getKey());
        String[] variables = new String[] { getQuotedHtmlHref(uriToRepositoryEntry, repositoryEntry.getDisplayname()) };
        return translator.translate("mail.body.repository.entry", variables);
    }

    protected String getConfirmationTemplatePath() {
        return MAIL_CONFIRMATION_TEMPLATE_PATH;
    }
}
