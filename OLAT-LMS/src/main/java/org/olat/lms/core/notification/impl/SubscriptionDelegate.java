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
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.stereotype.Component;

//TODO: REVIEW: cg/7.2.2012 Refactoring Service Logik in Objekte auslagern

/**
 * Initial Date: 08.02.2012 <br>
 * 
 * @author guretzki
 */
@Component
public class SubscriptionDelegate {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    SubscriptionDao subscriptionDao;
    @Autowired
    PublisherDao publisherDao;
    @Autowired
    SubscriberDao subscriberDao;
    @Autowired
    PublisherCreator publisherCreator;

    @Autowired
    HibernateTransactionManager transactionManager;

    public void deleteSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());

        Subscription subscription = subscriptionDao.getSubscription(subscriber, publisher);
        subscriptionDao.deleteSubscription(subscription);
    }

    public void invalidateSubscriptionForSubscriptionContext(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());

        invalidateSubscription(publisher, subscriber);
    }

    public void invalidateSubscription(Publisher publisher, Subscriber subscriber) {
        Subscription subscription = subscriptionDao.getSubscription(subscriber, publisher);
        invalidateSubscription(subscription);
    }

    public void invalidateSubscription(Subscription subscription) {
        subscription.setStatus(Subscription.Status.INVALID);
        subscriptionDao.updateSubscription(subscription);
    }

    // for testing-only : Package-Visibility
    Subscriber getSubscriber(Identity subscriberIdentity) {
        // cg/bb : IS THREAD_SAFE because Subscriber per Identity
        Subscriber subscriber = subscriberDao.findSubscriber(subscriberIdentity);
        if (subscriber == null) {
            subscriber = subscriberDao.createAndSaveSubscriber(subscriberIdentity);
        }
        return subscriber;
    }

    // for testing-only : Package-Visibility
    synchronized Publisher getPublisher(NotificationSubscriptionContext subscriptionContext) {
        // System.out.println("********** BRANO ****** session hashcode" + transactionManager.getSessionFactory().getCurrentSession().hashCode());
        // TODO: NOT THREAD_SAFE !!!!!! find/check null => Publisher NOT per Identity => NOT THREAD SAFE

        Publisher publisher = publisherDao.findPublisher(subscriptionContext.getContextId(), subscriptionContext.getContextType(), subscriptionContext.getSourceId(),
                subscriptionContext.getSourceType());
        if (publisher == null) {
            // System.out.println("getPublisher - found NOTHING!, so call createAndSavePublisher");
            publisher = publisherDao.createAndSavePublisher(subscriptionContext.getContextId(), subscriptionContext.getContextType(), subscriptionContext.getSourceId(),
                    subscriptionContext.getSourceType(), subscriptionContext.getSubcontextId());
        }
        return publisher;
    }

    public Subscription getSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());
        return subscriptionDao.getSubscription(subscriber, publisher);
    }

    public Subscription createAndSaveSubscription(NotificationSubscriptionContext subscriptionContext) {
        Publisher publisher = getPublisher(subscriptionContext);
        Subscriber subscriber = getSubscriber(subscriptionContext.getIdentity());
        return subscriptionDao.createOrReuseSubscription(subscriber, publisher);
    }

    public List<Subscription> getSubscriptions(Identity identity) {
        return subscriptionDao.getSubscriptionsForIdentity(identity);
    }

}
