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
package org.olat.lms.learn.notification.service;

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.Subscription;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.lms.learn.LearnService;
import org.olat.system.commons.date.DateFilter;

/**
 * This is called from presentation.
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
public interface NotificationLearnService extends LearnService {

    boolean isSubscribed(NotificationSubscriptionContext subscriptionContext);

    Subscription subscribe(NotificationSubscriptionContext subscriptionContext);

    void unSubscribe(NotificationSubscriptionContext subscriptionContext);

    void unSubscribe(Subscription subscription);

    /**
     * Adapter between the old and new NotificationService domain.
     */
    NotificationSubscriptionContext createNotificationSubscriptionContext(Identity identity, SubscriptionContext subscriptionContext, PublisherData publisherData);

    List<UserNotificationEventTO> getNews(Identity identity, DateFilter dateFilter);

    /**
     * Synchronization with course access rules.
     */
    List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Long subscriberId);

    List<Long> getAllSubscriberKeys();

    void invalidateUnaccesibleSubscriptions(List<NotificationSubscriptionContext> notificationSubscriptionContexts);

    void removePublishers(Set<Long> contextIds);

    /**
     * Subscription administration
     */
    List<SubscriptionTO> getSubscriptions(Identity identity);

    boolean isNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    void setNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    int getNumberOfNewsDays();

    /**
     * @Deprecated, use getSubscribersIds + notifySubscriber instead. <br/>
     *              Send to all subscriber pending notifications over the chosen channel (email). This method could be called from Administration tab.
     */
    @Deprecated
    NotifyStatistics notifySubscribers();

    List<Long> getSubscribersIds();

    NotifyStatistics notifySubscriber(Long subscriberId);

    void removeOldEvents();

    void deleteInvalidSubscribers();

}
