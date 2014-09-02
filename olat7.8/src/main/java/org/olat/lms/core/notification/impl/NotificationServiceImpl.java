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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.CoreBaseService;
import org.olat.lms.core.notification.impl.metric.NotificationServiceContext;
import org.olat.lms.core.notification.impl.metric.NotificationServiceMetric;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.system.commons.Retryable;
import org.olat.system.commons.date.DateFilter;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 28.11.2011 <br>
 * 
 * @author guretzki
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class NotificationServiceImpl extends CoreBaseService<NotificationServiceMetric<NotificationServiceContext>, NotificationServiceContext> implements
        NotificationService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    protected PublisherDao publisherDao;
    @Autowired
    protected SubscriberDao subscriberDao;
    @Autowired
    SubscriptionDao subscriptionDao;
    @Autowired
    NotificationEventDao notificationEventDao;

    @Autowired
    PublishDelegate publishDelegate;
    @Autowired
    SubscriptionDelegate subscriptionDelegate;
    @Autowired
    NotifyDelegate notifyDelegate;
    @Autowired
    NewsBuilder newsBuilder;

    /**
     * just for testing
     */
    public void setNotifyDelegate(NotifyDelegate notifyDelegate) {
        this.notifyDelegate = notifyDelegate;
    }

    @Override
    public Subscription subscribe(NotificationSubscriptionContext subscriptionContext) {
        if (isSubscribed(subscriptionContext)) {
            return subscriptionDelegate.getSubscription(subscriptionContext);
        } else {
            return subscriptionDelegate.createAndSaveSubscription(subscriptionContext);
        }
    }

    @Override
    public List<Subscription> getSubscriptions(Identity identity) {
        return subscriptionDelegate.getSubscriptions(identity);
    }

    /**
     * @Retryable since it could throw StaleObjectStateException at createAndSaveNotificationEvents, if an unsubscribe occurs concurrently.
     */
    @Override
    @Retryable
    // LD reviewed: should use Propagation.REQUIRES_NEW, else it is not possible to be successful in case of retry
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int publishEvent(PublishEventTO publishEventTO) {
        if (isPublishRelevant(publishEventTO)) {
            log.info("publishEvent");
            return publishDelegate.publishEvent(publishEventTO);
        }
        return 0;
    }

    private boolean isPublishRelevant(PublishEventTO publishEventTO) {
        return !EventType.NO_PUBLISH.equals(publishEventTO.getEvenType());
    }

    /**
     * @Retryable since it could throw a StaleObjectStateException at getEvents, if a publishEvent occurs concurrently. <br/>
     * 
     */
    @Deprecated
    @Override
    public NotifyStatistics notifySubscribers() {
        NotifyStatistics notifyStatistics = new NotifyStatistics();
        notifyStatistics.setFinished(false);
        notifyMetrics(notifyStatistics);
        Iterator<Subscriber> subscribersIter = notifyDelegate.getSubscribersIterator();

        while (subscribersIter.hasNext()) {
            notifyStatistics.add(notifyDelegate.notifySubscriber(subscribersIter.next().getId()));
        }
        notifyStatistics.setFinished(true);
        notifyMetrics(notifyStatistics);
        return notifyStatistics;
    }

    @Override
    public NotifyStatistics notifySubscriber(Long subscriberId) {
        NotifyStatistics notifyStatistics = notifyDelegate.notifySubscriber(subscriberId);
        this.notifyMetrics(notifyStatistics);
        return notifyStatistics;
    }

    @Autowired
    @Override
    protected void setMetrics(List<NotificationServiceMetric<NotificationServiceContext>> metrics) {
        for (NotificationServiceMetric<NotificationServiceContext> metric : metrics) {
            attach(metric);
        }
    }

    @Override
    public List<Long> getSubscribersIds() {
        return notifyDelegate.getSubscribersIDs();
    }

    @Override
    public void unsubscribe(NotificationSubscriptionContext subscriptionContext) {
        if (isSubscribed(subscriptionContext)) {
            subscriptionDelegate.invalidateSubscriptionForSubscriptionContext(subscriptionContext);
        }
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        subscriptionDelegate.invalidateSubscription(subscription);
    }

    @Override
    public boolean isSubscribed(NotificationSubscriptionContext subscriptionContext) {
        if (subscriptionContext == null) {
            return false;
        }
        return subscriberDao.isSubscribed(subscriptionContext.getIdentity(), subscriptionContext.getContextId(), subscriptionContext.getContextType(),
                subscriptionContext.getSourceId(), subscriptionContext.getSourceType());
    }

    List<NotificationServiceMetric<NotificationServiceContext>> getMetrics() {
        return metrics;
    }

    @Override
    public List<UserNotificationEventTO> getNews(Identity identity, DateFilter dateFilter) {
        return newsBuilder.getUserNotificationEventTOList(identity, dateFilter);
    }

    @Override
    public List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Long subscriberId) {
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForSubscriberId(subscriberId);
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = new ArrayList<NotificationSubscriptionContext>();
        for (Subscription subscription : subscriptions) {
            notificationCourseAccessTOs.add(createNotificationSubscriptionContext(subscription));
        }
        return notificationCourseAccessTOs;
    }

    private NotificationSubscriptionContext createNotificationSubscriptionContext(Subscription subscription) {
        return new NotificationSubscriptionContext(subscription.getSubscriber().getIdentity(), subscription.getPublisher().getSourceType(), subscription.getPublisher()
                .getSourceId(), Publisher.ContextType.COURSE, subscription.getPublisher().getContextId(), subscription.getPublisher().getSubcontextId());
    }

    @Override
    public List<Long> getAllSubscriberKeys() {
        return subscriberDao.getAllSubscriberIds();
    }

    @Override
    public void invalidateUnaccesibleSubscriptions(List<NotificationSubscriptionContext> notificationSubscriptionContexts) {
        for (NotificationSubscriptionContext notificationCourseAccessTO : notificationSubscriptionContexts) {
            invalidateSubscription(notificationCourseAccessTO);
        }

    }

    private void invalidateSubscription(NotificationSubscriptionContext notificationSubscriptionContext) {
        Subscriber subscriber = subscriberDao.findSubscriber(notificationSubscriptionContext.getIdentity());
        Publisher publisher = publisherDao.findPublisher(notificationSubscriptionContext.getContextId(), Publisher.ContextType.COURSE,
                notificationSubscriptionContext.getSourceId(), notificationSubscriptionContext.getSourceType());
        subscriptionDelegate.invalidateSubscription(publisher, subscriber);
    }

    @Override
    public boolean isNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval) {
        Subscriber subscriber = subscriberDao.findSubscriber(identity);
        if (subscriber != null) {
            return subscriber.getInterval() == notificationInterval;
        } else {
            return notificationInterval == Subscriber.NotificationInterval.DAILY;
        }
    }

    @Override
    public void setNotificationIntervalFor(Identity identity, Subscriber.NotificationInterval notificationInterval) {
        subscriptionDelegate.getSubscriber(identity).setInterval(notificationInterval);
    }

    @Override
    public void removePublishers(Set<Long> contextIds) {
        publisherDao.removePublishers(contextIds);
    }

    @Override
    public void removeOldEvents(Date notificationNewsDate) {
        notificationEventDao.deleteOldEvents(notificationNewsDate);
    }

    @Override
    public void deleteInvalidSubscribers() {
        subscriberDao.deleteInvalidSubscribers();
    }

    @Override
    public void startNotificationJob() {
        NotifyStatistics notifyStatistics = new NotifyStatistics();
        notifyStatistics.setFinished(false);
        notifyMetrics(notifyStatistics);
    }

    @Override
    public void finishNotificationJob() {
        NotifyStatistics notifyStatistics = new NotifyStatistics();
        notifyStatistics.setFinished(true);
        notifyMetrics(notifyStatistics);
    }

}
