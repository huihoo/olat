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
package org.olat.data.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.olat.data.notification.DaoObjectMother.createForumPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.exception.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 02.12.2011 <br>
 * 
 * @author lavinia
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class SubscriptionDaoITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private PublisherDao publisherDao;

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    GenericDao<IdentityImpl> identityDao;

    @PostConstruct
    void initType() {
        identityDao.setType(IdentityImpl.class);
    }

    @Autowired
    private SubscriberDao subscriberDao;

    @Autowired
    private NotificationEventDao eventDao;

    Long contextId = new Long(123);
    Long sourceId = new Long(456);
    Long subcontextId = new Long(222);
    Long contextIdTwo = new Long(999);
    Long sourceIdTwo = new Long(888);

    Publisher publisher_1;
    Publisher publisher_2;
    Subscriber subscriber;
    IdentityImpl identity;
    String username = "test1_" + System.currentTimeMillis();

    IdentityImpl identityTwo;
    Subscriber subscriberTwo;
    String usernameTwo = "test2_" + System.currentTimeMillis();

    @Before
    public void setup() {

        publisher_1 = createForumPublisher(contextId, subcontextId, sourceId);
        publisher_1 = publisherDao.savePublisher(publisher_1);

        publisher_2 = createForumPublisher(contextIdTwo, subcontextId, sourceIdTwo);
        publisher_2 = publisherDao.savePublisher(publisher_2);

        identity = identityDao.create();
        identity.setName(username);
        identity = identityDao.save(identity);
        subscriber = subscriberDao.createAndSaveSubscriber(identity);

        identityTwo = identityDao.create();
        identityTwo.setName(usernameTwo);
        identityTwo = identityDao.save(identityTwo);
        subscriberTwo = subscriberDao.createAndSaveSubscriber(identityTwo);
    }

    @Test
    public void createAndSaveSubscription() {
        Long beforeTime = new Date().getTime() - 1000;
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        Long afterTime = new Date().getTime() + 1000;
        assertNotNull(subscription);
        assertNotNull(subscription.getId());
        assertEquals(publisher_1, subscription.getPublisher());
        assertEquals(subscriber, subscription.getSubscriber());
        assertTrue(subscription.getCreationDate().getTime() > beforeTime);
        assertTrue(subscription.getCreationDate().getTime() < afterTime);
    }

    @Test
    public void subscriptionHasPublisher() {
        Publisher publisher = publisherDao.findPublisher(contextId, Publisher.ContextType.COURSE, sourceId, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        Subscription subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber, publisher);
        assertTrue(subscription_1.getPublisher().equals(publisher));
    }

    @Test(expected = ConstraintViolationException.class)
    @Ignore
    /** TODO: set to ignore - subscriptions are now reused **/
    public void createAndSaveSubscription_cannotCreateSameSubscriptionTwice() {
        Publisher publisher = publisherDao.findPublisher(contextId, Publisher.ContextType.COURSE, sourceId, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        Subscription subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber, publisher);
        Subscription subscription_2 = subscriptionDao.createOrReuseSubscription(subscriber, publisher);
    }

    @Test
    public void subscriberContainsSubscriptions() {
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        Subscriber persistentSubscriber = subscriberDao.findSubscriber(identity);
        assertTrue(persistentSubscriber.getSubscriptions().contains(subscription));
    }

    @Test
    public void getSubscriptions_findTwo() {
        Subscription subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        Subscription subscription_2 = subscriptionDao.createOrReuseSubscription(subscriber, publisher_2);

        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForIdentity(identity);
        assertFalse(subscriptions.isEmpty());
        assertTrue(subscriptions.size() == 2);
        Subscription subscription = subscriptions.get(0);
        assertEquals(publisher_1, subscription.getPublisher());
        assertEquals(username, subscription.getSubscriber().getIdentity().getName());
    }

    @Test
    public void getSubscription_DoesNotExist() {
        assertNull("Should return null when no subscription exist", subscriptionDao.getSubscription(subscriber, publisher_1));
    }

    @Test
    public void getSubscription() {
        Subscription createdSubscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        Subscription subscription_2 = subscriptionDao.getSubscription(subscriber, publisher_1);
        assertNotNull("Could not get subscription", subscription_2);
        assertEquals("getSubscription is not the same subscription like th ecreated one.", createdSubscription, subscription_2);
    }

    @Test
    public void getSubscriptionFrom_emptyList() {
        assertNull("Should return null when subscription-list is empty", subscriptionDao.getSubscriptionFrom(new ArrayList<Subscription>(), subscriber, publisher_1));
    }

    @Test(expected = AssertException.class)
    public void getSubscriptionFrom_moreThanOneSubscription() {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        subscriptions.add(mock(Subscription.class));
        subscriptions.add(mock(Subscription.class));
        assertNull("Should return null when subscription-list is empty", subscriptionDao.getSubscriptionFrom(subscriptions, subscriber, publisher_1));
    }

    @Test
    public void deleteSubscription_withCascadeDelete() {
        Subscription subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher_1, attributes);
        assertTrue(eventDao.findAllNotificationEvents().size() == 1);
        assertTrue(subscriptionDao.getSubscriptionsForIdentity(identity).size() == 1);

        subscriptionDao.deleteSubscription(subscription_1);
        assertTrue(eventDao.findAllNotificationEvents().size() == 0);
        assertTrue(subscriptionDao.getSubscriptionsForIdentity(identity).size() == 0);
    }

    @Test
    public void setLastNotifiedDate() {
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        assertTrue(subscription.getLastNotifiedDate() == null);

        Date lastNotifiedDate = new Date(System.currentTimeMillis());
        subscription.setLastNotifiedDate(lastNotifiedDate);

        Subscription persistentSubscription = subscriptionDao.getSubscriptionsForIdentity(identity).get(0);
        assertTrue(persistentSubscription.getLastNotifiedDate() != null);
        assertTrue(persistentSubscription.getLastNotifiedDate().equals(lastNotifiedDate));

        Subscription persistentSubscription2 = subscriptionDao.getSubscription(subscriber, publisher_1);
        assertTrue(persistentSubscription2.getLastNotifiedDate() != null);
    }

    @Test
    public void equals_DifferentTypes() {
        assertFalse("Wrong equals implementation, different types are recognized as equals ",
                subscriptionDao.createOrReuseSubscription(subscriber, publisher_1).equals(new Integer(1)));
    }

    @Test
    public void equals_Null() {
        assertFalse("Wrong equals implementation, null value is recognized as equals ", subscriptionDao.createOrReuseSubscription(subscriber, publisher_1).equals(null));
    }

    @Test
    public void equals_Same() {
        Subscription testSubscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        assertTrue("Wrong equals implementation, same objects are NOT recognized as equals ", testSubscription.equals(testSubscription));
    }

    @Test
    public void equals_DifferentPublishers() {
        assertFalse("Wrong equals implementation, different objects are recognized as equals ", subscriptionDao.createOrReuseSubscription(subscriber, publisher_1)
                .equals(subscriptionDao.createOrReuseSubscription(subscriber, publisher_2)));
    }

    @Test
    public void getNotificationEvents() {
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher_1, attributes);
        subscription.addNotificationEvent(events.get(0));

        assertTrue(subscription.getNotificationEvents().contains(events.get(0)));
    }

    @Test
    public void isSubscribed_notSubscribedWhenSubscriptionNotExists() {

        subscriptionDao.createOrReuseSubscription(subscriberTwo, publisher_2);
        assertFalse(subscriberDao.isSubscribed(identityTwo, contextId, Publisher.ContextType.COURSE, sourceId, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE));

    }

    @Test
    public void isSubscribed_subscriptionExists() {
        subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        assertTrue(subscriberDao.isSubscribed(identity, contextId, Publisher.ContextType.COURSE, sourceId, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE));

    }

    @Test
    public void updateSubscription() {
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        assertNull("New subscription must have lastNotifiedDate null", subscription.getLastNotifiedDate());
        Date lastNotifiedDate = new Date();
        subscription.setLastNotifiedDate(lastNotifiedDate);
        subscriptionDao.updateSubscription(subscription);
        Subscription updatedSubscription = subscriptionDao.getSubscription(subscriber, publisher_1);
        assertNotNull("Could not found subscription after update", updatedSubscription);
        assertEquals("Subscription is not updated", lastNotifiedDate, updatedSubscription.getLastNotifiedDate());
    }

    @Test
    public void testSubscriptionsForIdentity() {
        subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        subscriptionDao.createOrReuseSubscription(subscriber, publisher_2);
        subscriptionDao.createOrReuseSubscription(subscriberTwo, publisher_1);
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForIdentity(subscriber.getIdentity());
        assertEquals(2, subscriptions.size());
    }

    @Test
    public void testSubscriptionsForSubscriberId() {
        subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        subscriptionDao.createOrReuseSubscription(subscriber, publisher_2);
        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForSubscriberId(subscriber.getId());
        assertEquals(2, subscriptions.size());
    }

    @Test
    public void testReuseSubscription() {
        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        long subscriptionId = subscription.getId().longValue();
        assertEquals(subscription.getStatus(), Subscription.Status.VALID);
        subscription.setStatus(Subscription.Status.INVALID);
        subscription = subscriptionDao.updateSubscription(subscription);
        assertEquals(subscription.getStatus(), Subscription.Status.INVALID);
        subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher_1);
        assertEquals(subscription.getStatus(), Subscription.Status.VALID);
        assertEquals(subscriptionId, subscription.getId().longValue());
    }
}
