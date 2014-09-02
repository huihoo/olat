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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.NotificationEvent.Attribute;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates and stores new notification events.
 * 
 * Initial Date: 07.02.2012 <br>
 * 
 * @author cg
 */
@Component
public class PublishDelegate {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    PublisherDao publisherDao;
    @Autowired
    NotificationEventDao notificationEventDao;

    public int publishEvent(PublishEventTO publishEventTO) {
        Publisher publisher = getPublisher(publishEventTO);
        if (publisher == null) {
            // if there are no subscriptions, no publisher was created, that is OK
            log.info("found no publisher for context-type=" + publishEventTO.getContextType() + " context-id=" + publishEventTO.getContextId() + " source-type="
                    + publishEventTO.getSourceType() + " source-id=" + publishEventTO.getSourceId());
        } else {
            log.debug("publishEvent: create NotificationEvents for ContextTitle=" + publishEventTO.getContextTitle() + " SourceTitle=" + publishEventTO.getSourceTitle());
            List<NotificationEvent> notificationEvents = notificationEventDao.createAndSaveNotificationEvents(publisher, createAttributesFrom(publishEventTO));
            return notificationEvents.size();
        }
        return 0;
    }

    // for testing-only : Package-Visibility
    Publisher getPublisher(PublishEventTO publishEventTO) {
        return publisherDao.findPublisher(publishEventTO.getContextId(), publishEventTO.getContextType(), publishEventTO.getSourceId(), publishEventTO.getSourceType());
    }

    // for testing-only : Package-Visibility
    boolean publisherExists(PublishEventTO publishEventTO) {
        return getPublisher(publishEventTO) != null;
    }

    // for testing-only : Package-Visibility
    Map<String, String> createAttributesFrom(PublishEventTO publishEventTO) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(Attribute.EVENT_TYPE.name(), publishEventTO.getEvenType().name());
        attributes.put(Attribute.CREATOR_USERNAME.name(), publishEventTO.getCreator().getName());
        attributes.put(Attribute.CREATOR_FIRST_LAST_NAME.name(), publishEventTO.getCreatorsFirstLastName());
        attributes.put(Attribute.SOURCE_ENTRY_ID.name(), String.valueOf(publishEventTO.getSourceEntryId()));
        if (publishEventTO.getContextTitle() != null) {
            attributes.put(Attribute.CONTEXT_TITLE.name(), publishEventTO.getContextTitle());
        }
        if (publishEventTO.getSourceTitle() != null) {
            attributes.put(Attribute.SOURCE_TITLE.name(), publishEventTO.getSourceTitle());
        }
        if (publishEventTO.getSourceEntryTitle() != null) {
            attributes.put(Attribute.SOURCE_ENTRY_TITLE.name(), publishEventTO.getSourceEntryTitle());
        }
        return attributes;
    }
}
