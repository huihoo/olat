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
import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.NotificationEventDao;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.system.commons.date.DateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * class responsible for getting all relevant new notification events for user
 * 
 * Initial Date: 08.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class NewsBuilder {

    @Autowired
    NotificationEventDao notificationEventDao;

    @Autowired
    UriBuilder uriBuilder;

    /**
     * Gets the news (latest - dependent on the dateFilter - events) for this identity.
     */
    List<UserNotificationEventTO> getUserNotificationEventTOList(Identity identity, DateFilter dateFilter) {

        List<NotificationEvent> notificationEvents = notificationEventDao.getEventsForIdentity(identity, dateFilter);
        List<UserNotificationEventTO> newsList = new ArrayList<UserNotificationEventTO>();

        for (NotificationEvent notificationEvent : notificationEvents) {
            newsList.add(createUserNotificationEventTO(notificationEvent));
        }

        return newsList;
    }

    private UserNotificationEventTO createUserNotificationEventTO(NotificationEvent notificationEvent) {

        String contextTitle = notificationEvent.getAttributes().get(NotificationEvent.Attribute.CONTEXT_TITLE.name());
        String sourceType = notificationEvent.getSubscription().getPublisher().getSourceType();
        String sourceTitle = notificationEvent.getAttributes().get(NotificationEvent.Attribute.SOURCE_TITLE.name());
        String sourceEntryTitle = notificationEvent.getAttributes().get(NotificationEvent.Attribute.SOURCE_ENTRY_TITLE.name());

        PublishEventTO.EventType eventType = PublishEventTO.EventType.valueOf(notificationEvent.getAttributes().get(NotificationEvent.Attribute.EVENT_TYPE.name()));

        String creatorFirstLastName = notificationEvent.getAttributes().get(NotificationEvent.Attribute.CREATOR_FIRST_LAST_NAME.name());

        Date eventDate = notificationEvent.getCreationDate();

        final String contextUrl = uriBuilder.getURIToContext(notificationEvent.getSubscription().getPublisher());
        final String eventSourceUrl = uriBuilder.getURIToEventSource(notificationEvent.getSubscription().getPublisher());
        final String eventSourceEntryUrl = uriBuilder.getURIToSourceEntry(notificationEvent.getSubscription().getPublisher(),
                notificationEvent.getAttributes().get(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name()));

        return new UserNotificationEventTO(contextTitle, contextUrl, sourceType, sourceTitle, sourceEntryTitle, eventSourceUrl, eventSourceEntryUrl, eventType,
                creatorFirstLastName, eventDate);
    }

}
