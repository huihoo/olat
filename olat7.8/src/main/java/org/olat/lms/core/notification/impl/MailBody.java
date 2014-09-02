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

import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateUtil;

/**
 * For the notification email.
 * 
 * Initial Date: 10.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailBody {

    private final String header;
    private final List<MailBodyContextEntry> contextEntries;
    private final String greeting;
    private final String greetingFrom;
    private final MailBodyInfo mailBodyInfo;
    private final String footerFirstPart;
    private final String footerSecondPart;

    private UriBuilder uriBuilder;

    public MailBody(List<NotificationEventTO> eventToList, MailBuilderCommons mailBuilderCommons, Locale locale) {
        this.uriBuilder = mailBuilderCommons.getUriBuilder();
        Translator translator = mailBuilderCommons.getEmailTranslator(MailBody.class, locale);

        header = translator.translate("mail.body.header");
        contextEntries = setContextEntries(eventToList, translator);
        greeting = translator.translate("mail.body.greeting");
        greetingFrom = translator.translate("mail.body.greeting.from");
        mailBodyInfo = new MailBodyInfo(translator, mailBuilderCommons.getOlatWebUrl());
        footerFirstPart = translator.translate("mail.body.footer.firstpart");
        footerSecondPart = translator.translate("mail.body.footer.secondpart");
    }

    public MailBodyInfo getMailBodyInfo() {
        return mailBodyInfo;
    }

    public String getHeader() {
        return header;
    }

    public List<MailBodyContextEntry> getContextEntries() {
        return contextEntries;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getGreetingFrom() {
        return greetingFrom;
    }

    List<MailBodyContextEntry> setContextEntries(List<NotificationEventTO> list, Translator translator) {

        List<MailBodyContextEntry> contextEntries = new ArrayList<MailBodyContextEntry>();
        List<MailBodySourceEntry> sourceEntries = new ArrayList<MailBodySourceEntry>();
        List<MailBodySourceContentEntry> sourceContentEntries = new ArrayList<MailBodySourceContentEntry>();
        Long actualContextId = null;
        Long actualSourceId = null;
        for (NotificationEventTO notificationEventTO : list) {

            if (isContextChanged(actualContextId, notificationEventTO.getPublisher().getContextId())) {
                actualContextId = notificationEventTO.getPublisher().getContextId();
                sourceEntries = new ArrayList<MailBodySourceEntry>();
                MailBodyContextEntry mailContextEntry = new MailBodyContextEntry(notificationEventTO.getContextTitle(), getContextUrlLink(notificationEventTO),
                        sourceEntries, translator);
                contextEntries.add(mailContextEntry);
            }

            if (isSourceChanged(actualSourceId, notificationEventTO.getPublisher().getSourceId())) {
                actualSourceId = notificationEventTO.getPublisher().getSourceId();
                sourceContentEntries = new ArrayList<MailBodySourceContentEntry>();
                MailBodySourceEntry mailBodySourceEntry = new MailBodySourceEntry(notificationEventTO.getSourceTitle(), notificationEventTO.getSourceTYpe(),
                        getSourceUrlLink(notificationEventTO), sourceContentEntries, translator);
                sourceEntries.add(mailBodySourceEntry);
            }

            MailBodySourceContentEntry mailBodySourceContentEntry = new MailBodySourceContentEntry(notificationEventTO.getSourceEntryTitle(),
                    getEntryUrlLink(notificationEventTO), getFirstPartArgs(notificationEventTO, translator), getSecondPartArgs(notificationEventTO, translator),
                    translator);
            sourceContentEntries.add(mailBodySourceContentEntry);

        }

        return contextEntries;
    }

    private String[] getSecondPartArgs(NotificationEventTO notificationEventTO, Translator translator) {
        String[] args = new String[] { DateUtil.extractDate(notificationEventTO.getEventDate(), notificationEventTO.getLocale()),
                DateUtil.extractTime(notificationEventTO.getEventDate(), notificationEventTO.getLocale()),
                resolveEntryChangeType(notificationEventTO.getEventType(), translator) };
        return args;
    }

    private String[] getFirstPartArgs(NotificationEventTO notificationEventTO, Translator translator) {
        String[] args = new String[] { notificationEventTO.getCreatorFirstLastName(), resolveEntryChangeType(notificationEventTO.getEventType(), translator) };
        return args;
    }

    private String getEntryUrlLink(NotificationEventTO notificationEventTO) {
        return uriBuilder.getURIToSourceEntry(notificationEventTO.getPublisher(), notificationEventTO.getSourceEntryId());
    }

    private String getSourceUrlLink(NotificationEventTO notificationEventTO) {
        return uriBuilder.getURIToEventSource(notificationEventTO.getPublisher());
    }

    private String getContextUrlLink(NotificationEventTO notificationEventTO) {
        return uriBuilder.getURIToContext(notificationEventTO.getPublisher());
    }

    private String resolveEntryChangeType(EventType eventType, Translator translator) {
        String entryChangeType = "";
        if (PublishEventTO.EventType.NEW.equals(eventType)) {
            entryChangeType = translator.translate("mail.body.entry.created");
        } else if (PublishEventTO.EventType.CHANGED.equals(eventType)) {
            entryChangeType = translator.translate("mail.body.entry.changed");
        } else if (PublishEventTO.EventType.DELETED.equals(eventType)) {
            entryChangeType = translator.translate("mail.body.entry.deleted");
        }

        return entryChangeType;
    }

    private boolean isSourceChanged(Long actualSourceId, Long sourceId) {
        return actualSourceId == null || !actualSourceId.equals(sourceId);
    }

    private boolean isContextChanged(Long actualContextId, Long contextId) {
        return actualContextId == null || !actualContextId.equals(contextId);
    }

    public String getNotificationSettingsUrl() {
        return uriBuilder.getUriToNotificationSettings();
    }

    public String getFooterFirstPart() {
        return footerFirstPart;
    }

    public String getFooterSecondPart() {
        return footerSecondPart;
    }

}
