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
     * 
     */
    Subscription subscribe(NotificationSubscriptionContext subscriptionContext);

    /**
     * Un-subscribe a subscription of an identity for certain resource. The subscription will be invalidated.
     * 
     * @param subscriptionContext
     * 
     */
    void unsubscribe(NotificationSubscriptionContext subscriptionContext);

    /**
     * Invalidates this subscription.
     */
    void unsubscribe(Subscription subscription);

    /**
     * Get list of all subscription for an identity.
     */
    List<Subscription> getSubscriptions(Identity identity);

    /**
     * Creates and saves notification events based on the publisher and its subscriptions. <br/>
     * Call this method when something changes in the publisher. (e.g. a new forum message stored)
     * 
     */
    int publishEvent(PublishEventTO publishEventTO);

    /**
     * Notify all subscribers. Is deprecated because the transaction could take too long.
     * 
     * @Deprecated, use getSubscribersIds + notifySubscriber instead. <br/>
     *              Send to all subscriber pending notifications over the chosen channel (email). This method will be called normally from a cron-job.
     */
    @Deprecated
    NotifyStatistics notifySubscribers();

    /**
     * @return the list with subscriber ids which have WAITING events.
     */
    List<Long> getSubscribersIds();

    /**
     * @return the list with all subscriber ids.
     */
    List<Long> getAllSubscriberKeys();

    /**
     * Notify one subscriber. <br>
     * This method will be called repeatedly normally from a cron-job, for each <code> subscriberId </code>.
     */
    NotifyStatistics notifySubscriber(Long subscriberId);

    /**
     * Sets a flag at the start of the NotificationJob, for MBean purposes. <br>
     * Should be called just before first <code>notifySubscriber</code> call, it only changes a flag in a MBean.
     */
    void startNotificationJob();

    /**
     * Sets a flag at the end of the NotificationJob, for MBean purposes. <br>
     * Should be called after the last call to <code>notifySubscriber</code>, it only changes a flag in a MBean.
     */
    void finishNotificationJob();

    /**
     * Check if an identity has a subscription for a certain subscription-context.
     * 
     * @param subscriptionContext
     * 
     */
    boolean isSubscribed(NotificationSubscriptionContext subscriptionContext);

    /**
     * @return a list with all relevant notification events for this identity and for this dateFilter. <br/>
     *         Business decision: the event are stored about one month, they get deleted by calling <code>removeOldEvents</code> <br>
     *         The notification.news.days property in olat.properties configures how long an event is still new.
     */
    List<UserNotificationEventTO> getNews(Identity identity, DateFilter dateFilter);

    /**
     * @return the list with all subscriptions for this subscriber.
     */
    List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Long subscriberId);

    /**
     * Invalidates this list of subscriptions, because they are no more relevant (e.g. a user has lost the right to see/access this course node).
     */
    void invalidateUnaccesibleSubscriptions(List<NotificationSubscriptionContext> notificationSubscriptionContextOs);

    /**
     * Checks if this identity has the mentioned notificationInterval.
     */
    boolean isNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    /**
     * Sets the notificationInterval for this identity. For now the GUI uses only NotificationInterval.DAILY and NotificationInterval.NEVER.
     */
    void setNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval);

    /**
     * Removes all publishers for this context. For now a context is always a course. (e.g. if a course is deleted we want to remove all its publishers)
     */
    void removePublishers(Set<Long> contextIds);

    /**
     * Removes all events older than the notificationNewsDate. <br>
     * The notification.news.days property in olat.properties configures this value.
     */
    void removeOldEvents(Date notificationNewsDate);

    /**
     * Deletes all invalid subscribers. An invalid subscriber is a subscriber for a deleted identity.
     */
    void deleteInvalidSubscribers();

}
