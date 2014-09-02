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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.lms.core.notification.service.ContextInfo;
import org.olat.lms.core.notification.service.NotificationServiceDependencies;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adapter between the old and new NotificationService domain.
 * 
 * Initial Date: 04.04.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class NotificationSubscriptionContextFactory {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String FOLDERS_PREFIX = "foldernodes";
    private static final String COURSE_CONTEXT_NAME = "CourseModule";

    @Autowired
    protected PublisherTypeHandlerSelector typeHandlerSelector;
    @Autowired
    NotificationServiceDependencies notificationServiceDependencies;

    /**
     * SubscriptionContext and PublisherData used to belong to the presentation package. Adapts these to the notification service business class.
     */
    public NotificationSubscriptionContext createNotificationSubscriptionContext(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData) {

        Long sourceId = null;
        Long subcontextId = null;
        Long contextId = null;
        Publisher.ContextType contextType = null;

        if (isPublisherContextACourse(subscriptionContext)) {
            sourceId = getSourceIdFrom(publisherData.getIdentifier());
            subcontextId = getSubContextIdFrom(subscriptionContext.getSubidentifier());
            contextType = Publisher.ContextType.COURSE;
            contextId = subscriptionContext.getContextId();
        } else {
            log.error("Unkown contextType " + subscriptionContext.getResName());
            contextType = Publisher.ContextType.UNKNOWN;
            contextId = 0L;
            sourceId = 0L;
        }

        String publisherType = getPublisherSourceType(publisherData);
        return new NotificationSubscriptionContext(identity, publisherType, sourceId, contextType, contextId, subcontextId);

    }

    private boolean isPublisherContextACourse(SubscriptionContext subscriptionContext) {
        return (subscriptionContext != null) && COURSE_CONTEXT_NAME.equals(subscriptionContext.getResName());
    }

    // Package visibility for testing
    /**
     * data could be something like: "\course\85778090095916/foldernodes/85778090097183" or just "85778090097183"
     */
    Long getSourceIdFrom(String data) {
        if (data.indexOf(FOLDERS_PREFIX) > 0) {
            data = data.substring(data.indexOf(FOLDERS_PREFIX) + FOLDERS_PREFIX.length() + 1);
        }
        return Long.valueOf(data);
    }

    // Package visibility for testing
    public Long getSubContextIdFrom(String subidentifier) {
        String subidentifierDelimiter = ":";
        if (subidentifier.contains(subidentifierDelimiter)) {
            subidentifier = subidentifier.substring(0, subidentifier.indexOf(subidentifierDelimiter));
        }
        return Long.valueOf(subidentifier);
    }

    /**
     * Maps the PublisherData type to the new Publisher.SourceType.
     */
    private String getPublisherSourceType(PublisherData publisherData) {
        return typeHandlerSelector.getTypeHandlerFrom(publisherData).getSourceType();
    }

    public ContextInfo createContextInfoFrom(SubscriptionContext subsContext) {
        Long contextId = null;
        Long subContextId = null;
        Publisher.ContextType contextType = null;

        if (isPublisherContextACourse(subsContext)) {
            subContextId = getSubContextIdFrom(subsContext.getSubidentifier());
            contextType = Publisher.ContextType.COURSE;
            contextId = notificationServiceDependencies.getRepositoryService().getRepositoryEntryIdFromResourceable(Long.valueOf(subsContext.getResId()),
                    subsContext.getResName());
        } else {
            log.error("Unkown contextType " + subsContext.getResName());
            contextType = Publisher.ContextType.UNKNOWN;
            contextId = 0L;
        }
        return new ContextInfo(contextType, contextId, subContextId);
    }

}
