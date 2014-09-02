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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates and finds subscribers and publishers. Creates, finds, and invalidates subscriptions.
 * 
 * Initial Date: 08.02.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class SubscriptionDelegate {
    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    SubscriptionDao subscriptionDao;
    @Autowired
    PublisherDao publisherDao;
    @Autowired
    SubscriberDao subscriberDao;

    /**
     * Deprecated because is not productive code, actually a subscription cannot be deleted but invalidated.
     */
    @Deprecated
    void deleteSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());

        Subscription subscription = subscriptionDao.getSubscription(subscriber, publisher);
        subscriptionDao.deleteSubscription(subscription);
    }

    /**
     * Invalidates subscriptions. Used at unsubscribe in GUI (from subscription context).
     */
    public void invalidateSubscriptionForSubscriptionContext(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());

        invalidateSubscription(publisher, subscriber);
    }

    /**
     * Invalidates subscription during the synchronize job, because this subscription is no more valid.
     */
    public void invalidateSubscription(Publisher publisher, Subscriber subscriber) {
        Subscription subscription = subscriptionDao.getSubscription(subscriber, publisher);
        invalidateSubscription(subscription);
    }

    /**
     * Invalidates subscription. Used from the subscriptions table in GUI.
     */
    public void invalidateSubscription(Subscription subscription) {
        subscription.setStatus(Subscription.Status.INVALID);
        subscriptionDao.updateSubscription(subscription);
    }

    /**
     * Finds or creates a Subscriber. IS THREAD SAFE because Subscriber is per Identity.
     */
    // for testing-only : Package-Visibility
    Subscriber getSubscriber(Identity subscriberIdentity) {
        Subscriber subscriber = subscriberDao.findSubscriber(subscriberIdentity);
        if (subscriber == null) {
            subscriber = subscriberDao.createAndSaveSubscriber(subscriberIdentity);
        }
        return subscriber;
    }

    /**
     * Finds or creates a publisher. Publisher is NOT per Identity, so we need to use <code>synchronized</code> to make it THREAD SAFE.
     */
    // for testing-only : Package-Visibility
    synchronized Publisher getPublisher(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = publisherDao.findPublisher(subscriptionContext.getContextId(), subscriptionContext.getContextType(), subscriptionContext.getSourceId(),
                subscriptionContext.getSourceType());
        if (publisher == null) {
            publisher = publisherDao.createAndSavePublisher(subscriptionContext.getContextId(), subscriptionContext.getContextType(), subscriptionContext.getSourceId(),
                    subscriptionContext.getSourceType(), subscriptionContext.getSubcontextId());
        }
        return publisher;
    }

    /**
     * Finds subscription for this subscriptionContext.
     */
    public Subscription getSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());
        return subscriptionDao.getSubscription(subscriber, publisher);
    }

    /**
     * Creates or reuses a subscription. An invalidated subscription could be reused.
     */
    public Subscription createAndSaveSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());
        return subscriptionDao.createOrReuseSubscription(subscriber, publisher);
    }

    /**
     * @return the list with all subscriptions for this identity.
     */
    public List<Subscription> getSubscriptions(Identity identity) {
        return subscriptionDao.getSubscriptionsForIdentity(identity);
    }

}
