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
package org.olat.lms.core.notification.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.Subscription;
import org.olat.lms.core.CoreService;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.system.commons.date.DateFilter;

/**
 * The notification-service is responsible for subscription management and the notification about changes.<br>
 * Initial Date: 23.11.2011 <br>
 * 
 * @author lavinia
 */
public interface NotificationService extends CoreService {

    /**
     * Add subscription of an identity for certain resource.
     * 
     * @param subscriptionContext
     *            source-type and id, context-type and id, sub-context id
     */
    Subscription subscribe(NotificationSubscriptionContext subscriptionContext);

    /**
     * Un-subscribe a subscription of an identity for certain resource. The subscription will be removed.
     * 
     * @param subscriptionContext
     *            source-type and id, context-type and id, sub-context id
     */
    void unsubscribe(NotificationSubscriptionContext subscriptionContext);

    void unsubscribe(Subscription subscription);

    /**
     * Get list of all subscription for an identity.
     */
    List<Subscription> getSubscriptions(Identity identity);

    /**
     * Create and save notification events based on publisher and his subscriptions. Call this method when something change in the resource.
     * 
     */
    int publishEvent(PublishEventTO publishEventTO);

    /**
     * @Deprecated, use getSubscribersIds + notifySubscriber instead. <br/>
     *              Send to all subscriber pending notifications over the chosen channel (email). This method will be called normally from a cron-job.
     */
    @Deprecated
    NotifyStatistics notifySubscribers();

    List<Long> getSubscribersIds();

    NotifyStatistics notifySubscriber(Long subscriberId);

    /**
     * Check if an identity has a subscription for certain subscription-context.
     * 
     * @param subscriptionContext
     *            source-type and id, context-type and id, sub-context id
     */
    boolean isSubscribed(NotificationSubscriptionContext subscriptionContext);

    /**
     * Returns a list with all relevant (not the obsolete ones) notification events for this identity and for this dateFilter. <br/>
     * TODO: find out from RE - how long should the notification events be stored in DB? <br/>
     * I expect that they should not be held longer than a week.
     */
    List<UserNotificationEventTO> getNews(Identity identity, DateFilter dateFilter);

    List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Long subscriberId);

    List<Long> getAllSubscriberKeys();

    void invalidateUnaccesibleSubscriptions(List<NotificationSubscriptionContext> notificationSubscriptionContextOs);

    boolean isNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    void setNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    void removePublishers(Set<Long> contextIds);

    void removeOldEvents(Date notificationNewsDate);

    void deleteInvalidSubscribers();

}
