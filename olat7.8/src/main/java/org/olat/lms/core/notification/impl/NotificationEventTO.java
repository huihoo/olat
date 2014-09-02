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

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * This contains all notification info required to notify the wrapped event: recipientEmail, <br/>
 * recipientFirstLastName, creatorFirstLastName, contextTitle, etc. <br/>
 * This is also provides sorting support: Comparable implementation and a Comparator implementation.
 * 
 * Initial Date: 07.12.2011 <br>
 * 
 * @author lavinia
 */
public class NotificationEventTO implements Comparable<NotificationEventTO> {

    private static final Logger log = LoggerHelper.getLogger();

    private NotificationEvent event;

    public NotificationEvent getEvent() {
        return this.event;
    }

    private String recipientEmail; // for email channel

    private Locale recipientsLocale;
    private static Collator collator;

    private String creatorFirstLastName;

    private PublishEventTO.EventType eventType;

    public PublishEventTO.EventType getEventType() {
        return eventType;
    }

    private String contextTitle; // e.g. course title
    private String sourceTitle; // e.g. forum title of the event source
    private String sourceEntryTitle; // e.g. message title of the new/changed/deleted message

    private String sourceEntryId;
    private Date eventDate;
    private Subscriber.NotificationInterval notificationInterval;

    boolean isEventEligible;

    private String sourceType;

    NotificationEventTO(NotificationEvent event) {

        this.event = event;

        Identity recipientIdentity = event.getSubscription().getSubscriber().getIdentity();
        this.recipientEmail = recipientIdentity.getAttributes().getEmail();
        this.recipientsLocale = getLocaleForIdentity(recipientIdentity);
        collator = Collator.getInstance(recipientsLocale);

        this.creatorFirstLastName = event.getAttributes().get(NotificationEvent.Attribute.CREATOR_FIRST_LAST_NAME.name());

        this.eventType = PublishEventTO.EventType.valueOf(event.getAttributes().get(NotificationEvent.Attribute.EVENT_TYPE.name()));

        this.contextTitle = event.getAttributes().get(NotificationEvent.Attribute.CONTEXT_TITLE.name());
        this.sourceTitle = event.getAttributes().get(NotificationEvent.Attribute.SOURCE_TITLE.name());
        this.sourceType = getPublisher().getSourceType();
        this.sourceEntryTitle = event.getAttributes().get(NotificationEvent.Attribute.SOURCE_ENTRY_TITLE.name());
        this.sourceEntryId = event.getAttributes().get(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name());

        this.eventDate = event.getCreationDate();
        this.notificationInterval = event.getSubscription().getSubscriber().getInterval();

        init(event);
    }

    private Locale getLocaleForIdentity(Identity identity) {
        Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
        if (locale == null) {
            locale = new Locale("DE");
            log.warn("getLocaleForIdentity could not find the locale for identity, so uses the default one.");
        }
        return locale;
    }

    private void init(NotificationEvent event) {
        isEventEligible = isEventEligible(event);
    }

    /**
     * Checks if this event is eligible to be delivered: <br/>
     * If the notification interval is not NEVER, but IMMEDIATELY or DAILY (it is not checked if the last notified date is more than one day old, since the
     * notifySubscribers job runs only once a day).
     * 
     */
    private boolean isEventEligible(NotificationEvent event) {
        boolean isEligible = false;
        if (Subscriber.NotificationInterval.NEVER.equals(notificationInterval)) {
            isEligible = false;
        } else {
            isEligible = true;
        }
        return isEligible;
    }

    /**
     * Returns true if event is eligible to be sent in the next notification interval.
     */
    public boolean isEventEligible() {
        return isEventEligible;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getCreatorFirstLastName() {
        return creatorFirstLastName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public Subscriber.NotificationInterval getNotificationInterval() {
        return notificationInterval;
    }

    public Publisher getPublisher() {
        return event.getSubscription().getPublisher();
    }

    public boolean hasSamePublisher(NotificationEventTO theOther) {
        return this.getPublisher().equals(theOther.getPublisher());
    }

    public String getSourceEntryId() {
        return sourceEntryId;
    }

    @Override
    /**
     * sort events of the same publisher - last first
     */
    public int compareTo(NotificationEventTO theOther) {
        if (this.getPublisher().equals(theOther.getPublisher())) {
            return -(this.getEventDate().compareTo(theOther.getEventDate()));
        }

        return 0;
    }

    public Locale getLocale() {
        return recipientsLocale;
    }

    public String getSourceEntryTitle() {
        return sourceEntryTitle;
    }

    /**
     * If same context (e.g. same course) sort after source title (e.g. forum title), <br/>
     * else if different contexts sort after context title (e.g. course title).
     */
    static class NotificationEventTOComparator implements Comparator<NotificationEventTO> {
        @Override
        public int compare(NotificationEventTO o1, NotificationEventTO o2) {
            // The publisher is the same
            if (o1.getPublisher().equals(o2.getPublisher())) {
                return -(o1.getEventDate().compareTo(o2.getEventDate()));
            }
            // The publisher is not the same, so try to sort after context id
            else {
                // The context id
                if (o1.getPublisher().getContextId().equals(o2.getPublisher().getContextId())) {
                    String sourceTitle1 = o1.getSourceTitle();
                    String sourceTitle2 = o2.getSourceTitle();
                    if (sourceTitle1 == null)
                        return -1;
                    if (sourceTitle2 == null)
                        return 1;
                    // delegate to collator
                    return collator.compare(sourceTitle1, sourceTitle2);
                } else {
                    // The context id is not the same, so sort after contextTitle
                    String contextTitle1 = o1.getContextTitle();
                    String contextTitle2 = o2.getContextTitle();
                    if (contextTitle1 == null)
                        return -1;
                    if (contextTitle2 == null)
                        return 1;
                    // delegate to collator
                    return collator.compare(contextTitle1, contextTitle2);
                }
            }
        }

    }

    public String getSourceTYpe() {
        return sourceType;
    }

}
