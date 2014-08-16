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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.presentation.forum.ForumNotificationTypeHandler;

/**
 * Initial Date: 08.02.2012 <br>
 * 
 * @author guretzki
 */
public class SubscriptionDelegateTest {

    SubscriptionDelegate subscriptionDelegateTestObject;

    private Subscriber subscriber;
    private Publisher publisher;

    private Identity subscriberIdentity;
    private Long contextId = 1L;
    private ContextType contextType = ContextType.COURSE;
    private Long sourceId = 2L;
    private String sourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
    private Long subcontextId = 3L;

    private NotificationSubscriptionContext subscriptionContext;
    private Subscription subscription;

    @Before
    public void setup() {
        subscriptionDelegateTestObject = new SubscriptionDelegate();

        subscriberIdentity = mock(Identity.class);
        subscriber = mock(Subscriber.class);
        publisher = mock(Publisher.class);
        subscription = mock(Subscription.class);

        SubscriptionDao subscriptionDaoMock = mock(SubscriptionDao.class);
        subscriptionDelegateTestObject.subscriptionDao = subscriptionDaoMock;
        SubscriberDao subscriberDao = mock(SubscriberDao.class);
        when(subscriberDao.findSubscriber(subscriberIdentity)).thenReturn(subscriber);
        subscriptionDelegateTestObject.subscriberDao = subscriberDao;
        PublisherDao publisherDao = mock(PublisherDao.class);
        when(publisherDao.findPublisher(contextId, contextType, sourceId, sourceType)).thenReturn(publisher);
        subscriptionDelegateTestObject.publisherDao = publisherDao;
        subscriptionDelegateTestObject.publisherCreator = new PublisherCreator();
        subscriptionDelegateTestObject.publisherCreator.publisherDao = publisherDao;

        subscriptionContext = new NotificationSubscriptionContext(subscriberIdentity, sourceType, sourceId, contextType, contextId, subcontextId);

    }

    @Test
    public void getSubscriber_SubscripberExist() {
        when(subscriptionDelegateTestObject.subscriberDao.findSubscriber(subscriberIdentity)).thenReturn(subscriber);

        Subscriber resultSubscriber = subscriptionDelegateTestObject.getSubscriber(subscriberIdentity);

        assertEquals("getSubscriber return wrong subscriber", subscriber, resultSubscriber);
    }

    @Test
    public void getSubscriber_SubscripberMustBeCreated() {
        when(subscriptionDelegateTestObject.subscriberDao.findSubscriber(subscriberIdentity)).thenReturn(null);
        when(subscriptionDelegateTestObject.subscriberDao.createAndSaveSubscriber(subscriberIdentity)).thenReturn(subscriber);

        Subscriber resultSubscriber = subscriptionDelegateTestObject.getSubscriber(subscriberIdentity);

        assertEquals("getSubscriber returns wrong subscriber", subscriber, resultSubscriber);

    }

    @Test
    public void getPublisher_PublisherExist() {
        when(subscriptionDelegateTestObject.publisherDao.findPublisher(contextId, contextType, sourceId, sourceType)).thenReturn(publisher);

        Publisher resultPublisher = subscriptionDelegateTestObject.getPublisher(subscriptionContext);

        assertEquals("getPublisher returns wrong publisher", publisher, resultPublisher);

    }

    @Test
    public void getPublisher_PublisherMustBeCreated() {
        when(subscriptionDelegateTestObject.publisherDao.findPublisher(contextId, contextType, sourceId, sourceType)).thenReturn(null);
        when(subscriptionDelegateTestObject.publisherDao.createAndSavePublisher(contextId, contextType, sourceId, sourceType, subcontextId)).thenReturn(publisher);

        Publisher resultPublisher = subscriptionDelegateTestObject.getPublisher(subscriptionContext);

        assertEquals("getPublisher returns wrong publisher", publisher, resultPublisher);

    }

    @Test
    public void getSubscription() {
        when(subscriptionDelegateTestObject.subscriptionDao.getSubscription(subscriber, publisher)).thenReturn(subscription);

        Subscription returnSubscription = subscriptionDelegateTestObject.getSubscription(subscriptionContext);

        assertEquals("getSubscription returns wrong subscription", subscription, returnSubscription);
    }

    @Test
    public void createAndSaveSubscription() {
        when(subscriptionDelegateTestObject.subscriptionDao.createOrReuseSubscription(subscriber, publisher)).thenReturn(subscription);

        Subscription createdSubscription = subscriptionDelegateTestObject.createAndSaveSubscription(subscriptionContext);

        assertEquals("createAndSaveSubscription returns wrong subscription", subscription, createdSubscription);
    }

    @Test
    public void deleteSubscription() {
        when(subscriptionDelegateTestObject.subscriptionDao.getSubscription(subscriber, publisher)).thenReturn(subscription);

        subscriptionDelegateTestObject.deleteSubscription(subscriptionContext);

        verify(subscriptionDelegateTestObject.subscriptionDao).deleteSubscription(subscription);
    }

    /**
     * Test only if call is delegated to subscriptionDao.
     */
    @Test
    public void getSubscriptions() {
        subscriptionDelegateTestObject.getSubscriptions(subscriberIdentity);
        verify(subscriptionDelegateTestObject.subscriptionDao).getSubscriptionsForIdentity(subscriberIdentity);
    }

}
