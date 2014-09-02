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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.olat.data.notification.DaoObjectMother.createForumPublisher;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.commons.date.DateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 30.11.2011 <br>
 * 
 * @author lavinia
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class NotificationServiceImplITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;

    // TODO: 2.2.2012/cg remove publisherDao here, it should be used only via service
    @Autowired
    PublisherDao publisherDao;

    // TODO: 2.2.2012/cg remove subscriberDao here, it should be used only via service
    @Autowired
    SubscriberDao subscriberDao;

    // TODO: 2.2.2012/cg remove subscriptionDao here, it should be used only via service
    @Autowired
    SubscriptionDao subscriptionDao;

    // TODO: 2.2.2012/cg remove notificationEventDao here, it should be used only via service
    @Autowired
    protected NotificationEventDao notificationEventDao;

    @Autowired
    BaseSecurityNotificationMock baseSecurity;

    Identity subscriberIdentity;
    Identity publisherIdentity;
    Long sourceId;
    Long sourceId2;
    Long contextId;
    Long subcontextId;
    Long messageId;
    NotificationSubscriptionContext subscriptionContext;

    Identity alf;
    Identity berta;
    Identity clara;

    @Autowired
    private DaoObjectMother daoObjectMother;
    @Autowired
    private UriBuilder uriBuilder;

    @Before
    public void setup() {

        subscriberIdentity = daoObjectMother.createAndSaveIdentity("test_subscriber");
        publisherIdentity = daoObjectMother.createAndSaveIdentity("test_publisher");
        sourceId = daoObjectMother.getSourceIdOne();
        sourceId2 = daoObjectMother.getSourceIdTwo();
        contextId = daoObjectMother.getContextId();
        subcontextId = daoObjectMother.getSubcontextIdOne();
        messageId = new Long(111);

        subscriptionContext = new NotificationSubscriptionContext(subscriberIdentity, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId);
        uriBuilder.serverContextPathURI = "/test";

    }

    @Test
    public void subscribe() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        assertNotNull(subscription);
    }

    @Test
    public void subscribe_twiceWithSameContext() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        assertNotNull(subscription);
        Subscription subscription2 = notificationServiceImpl.subscribe(subscriptionContext);
        assertNotNull(subscription2);
    }

    @Test
    public void subscribe_withSameContextIdButDifferentSourceId() {
        NotificationSubscriptionContext subscriptionContext2 = new NotificationSubscriptionContext(subscriberIdentity, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE,
                sourceId2, Publisher.ContextType.COURSE, contextId, subcontextId);
        Subscription subscription1 = notificationServiceImpl.subscribe(subscriptionContext);
        Subscription subscription2 = notificationServiceImpl.subscribe(subscriptionContext2);
        assertFalse(subscription1.getPublisher().equals(subscription2.getPublisher()));
    }

    @Test
    public void isSubscribed_publisherNotExists() {
        assertFalse("Publisher should not exists", notificationServiceImpl.isSubscribed(new NotificationSubscriptionContext(subscriberIdentity,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, Publisher.ContextType.COURSE, contextId, subcontextId)));
    }

    // TODO: TEST-FAILED 5.3.2012 : Brano is working on this task
    @Test
    public void isSubscribed_subscriberNotExists() {
        publisherDao.savePublisher(createForumPublisher(contextId, subcontextId, sourceId));
        assertFalse("Subscriber should not exists", notificationServiceImpl.isSubscribed(new NotificationSubscriptionContext(subscriberIdentity,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, Publisher.ContextType.COURSE, contextId, subcontextId)));
    }

    // TODO: TEST-FAILED 5.3.2012 : Brano is working on this task
    @Test
    public void isSubscribed_subscriptionNotExists() {
        publisherDao.savePublisher(createForumPublisher(contextId, subcontextId, sourceId));
        subscriberDao.createAndSaveSubscriber(subscriberIdentity);
        assertFalse("Subscription should not exists", notificationServiceImpl.isSubscribed(new NotificationSubscriptionContext(subscriberIdentity,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, Publisher.ContextType.COURSE, contextId, subcontextId)));
    }

    @Test
    public void isSubscribed_subscriptionExists() {
        Publisher publisher = publisherDao.savePublisher(createForumPublisher(contextId, subcontextId, sourceId));
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(subscriberIdentity);
        subscriptionDao.createOrReuseSubscription(subscriber, publisher);
        assertTrue("Subscription should exists", notificationServiceImpl.isSubscribed(new NotificationSubscriptionContext(subscriberIdentity,
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId, Publisher.ContextType.COURSE, contextId, null)));
    }

    @Test
    public void unSubscribe_WithoutSubscription() {
        notificationServiceImpl.unsubscribe(subscriptionContext);
    }

    @Test
    public void unSubscribe() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        assertTrue("Should have a subscription", notificationServiceImpl.isSubscribed(subscriptionContext));
        notificationServiceImpl.unsubscribe(subscriptionContext);
        assertFalse("Should not have a subscription after unsubscribe", notificationServiceImpl.isSubscribed(subscriptionContext));
    }

    @Test
    public void notifySubscribers_creatorSameAsSubscriber_noNotificationIssued() {
        notificationServiceImpl.notifySubscribers();
        assertTrue(notificationServiceImpl.notifySubscribers().getTotalCounter() == 0);

        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        PublishEventTO publishEventTO = daoObjectMother.createFirstPublishEventTO(subscriberIdentity);
        assertTrue(notificationServiceImpl.publishEvent(publishEventTO) == 0);

        notificationServiceImpl.notifySubscribers();
        assertTrue(notificationServiceImpl.notifySubscribers().getTotalCounter() == 0);

    }

    @Test
    public void notifySubscribers_differentCreatorAndSubscriber_oneSubscriber_onePublisher_oneEvent_notifyOne() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        PublishEventTO publishEventTO = daoObjectMother.createFirstPublishEventTO(publisherIdentity);
        assertTrue(notificationServiceImpl.publishEvent(publishEventTO) == 1);

        // assertTrue(notificationServiceImpl.notifySubscribers().getNumberOfSentEmails() == 1);

    }

    @Test
    public void notifySubscribers_subscribeThreeToOnePublisher_oneEvent_notifyTwo() {
        int eventCounter = createTreeIdentities_subscribeToOnePublisher_publishOneEvent();
        assertTrue(eventCounter == 2);
        int notificationCounter = notificationServiceImpl.notifySubscribers().getTotalCounter();
        // assertEquals("Wrong number of sent emails", 2, notificationCounter);
    }

    @Test
    public void notifySubscribers_subscribeTreeToOnePublisher_twoEvents_notifyTwo() {
        int eventCounter = createTreeIdentities_subscribeToOnePublisher_publishOneEvent();
        assertTrue(eventCounter == 2);

        PublishEventTO publishEventTO_2 = daoObjectMother.createFirstPublishEventTO(clara);
        eventCounter = notificationServiceImpl.publishEvent(publishEventTO_2);
        assertTrue(eventCounter == 2);

        // send 2 emails to alf and berta
        int notificationCounter = notificationServiceImpl.notifySubscribers().getTotalCounter();

        // assertEquals("Wrong number of notifications: ", 2, notificationCounter);

        // if call notifySubscribers again, no events found - no events waiting, all are delivered
        assertTrue(notificationServiceImpl.notifySubscribers().getTotalCounter() == 0);

        // the LastNotifiedDate on alf's subscription should have been updated
        List<Subscription> subscriptions = notificationServiceImpl.getSubscriptions(alf);
        assertTrue(subscriptions.size() == 1);

        // assertNotNull(subscriptions.get(0).getLastNotifiedDate());
        // assertFalse(subscriptions.get(0).getLastNotifiedDate().equals(new Date()));
    }

    /**
     * TODO: Replaced with method from daoObjectMother
     */
    @Deprecated
    private int createTreeIdentities_subscribeToOnePublisher_publishOneEvent() {
        alf = daoObjectMother.createAndSaveIdentity("alf");
        berta = daoObjectMother.createAndSaveIdentity("berta");
        clara = daoObjectMother.createAndSaveIdentity("clara");

        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(alf, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId));
        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(berta, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId));
        // TODO change the notification interval
        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(clara, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId));

        PublishEventTO publishEventTO = daoObjectMother.createFirstPublishEventTO(clara);
        int eventCounter = notificationServiceImpl.publishEvent(publishEventTO);
        return eventCounter;
    }

    @Test
    public void publishEvent_oneEvent() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        PublishEventTO publishEventTO = daoObjectMother.createFirstPublishEventTO(this.publisherIdentity);
        assertTrue(notificationServiceImpl.publishEvent(publishEventTO) == 1);
    }

    @Test
    public void getNews() throws InterruptedException {

        Date dateFrom = new Date();
        Thread.sleep(2000);

        createTreeIdentities_subscribeToOnePublisher_publishOneEvent();
        Thread.sleep(2000);
        Date dateTo = new Date();

        List<UserNotificationEventTO> news = notificationServiceImpl.getNews(alf, new DateFilter(dateFrom, dateTo));
        assertEquals(1, news.size());
        UserNotificationEventTO event = news.get(0);
        assertEquals(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, event.getSourceType());
        assertTrue(event.getCreationDate().after(dateFrom) && event.getCreationDate().before(dateTo));
        assertNotNull(event.getContextTitle());
        assertNotNull(event.getCreatorFirstLastName());
        assertNotNull(event.getEventType());
        assertNotNull(event.getSourceTitle());
        assertNotNull(event.getSourceType());

    }

    @Test
    public void getNotificationCourseAccessTOs() {
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = prepareNotificationSubscriptionContexts();
        assertEquals(1, notificationCourseAccessTOs.size());
        assertEquals(subcontextId, notificationCourseAccessTOs.get(0).getSubcontextId());
        assertEquals(subscriberIdentity, notificationCourseAccessTOs.get(0).getIdentity());
    }

    @Test
    public void deleteUnaccesibleSubscriptions() {
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = prepareNotificationSubscriptionContexts();
        notificationServiceImpl.invalidateUnaccesibleSubscriptions(notificationCourseAccessTOs);
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForIdentity(subscriberIdentity);
        assertEquals(0, subscriptions.size());
    }

    private List<NotificationSubscriptionContext> prepareNotificationSubscriptionContexts() {
        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(subscriberIdentity, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId));
        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(publisherIdentity, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceId,
                Publisher.ContextType.COURSE, contextId, subcontextId));
        List<NotificationSubscriptionContext> notificationCourseAccessTOs = notificationServiceImpl.getNotificationSubscriptionContexts(subscriberDao.findSubscriber(
                subscriberIdentity).getId());
        return notificationCourseAccessTOs;
    }

    @Test
    public void setNotificationIntervalFor() {
        Subscription subscription = notificationServiceImpl.subscribe(subscriptionContext);
        notificationServiceImpl.setNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.DAILY);
        assertTrue("Could not set NotificationInterval to DAILY",
                notificationServiceImpl.isNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.DAILY));
        notificationServiceImpl.setNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.NEVER);
        assertTrue("Could not set NotificationInterval to NEVER",
                notificationServiceImpl.isNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.NEVER));

    }

    @Test
    public void isNotificationIntervalFor_withoutSubscriber_DAILY() {
        assertTrue("Without a subscriber, NotificationInterval must be DAILY",
                notificationServiceImpl.isNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.DAILY));
    }

    @Test
    public void isNotificationIntervalFor_withoutSubscriber_NOT_NEVER() {
        assertFalse("Without a subscriber, NotificationInterval could not be NEVER",
                notificationServiceImpl.isNotificationIntervalFor(subscriberIdentity, Subscriber.NotificationInterval.NEVER));
    }

}
