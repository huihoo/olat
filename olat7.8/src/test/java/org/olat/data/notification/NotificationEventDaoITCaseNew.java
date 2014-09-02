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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.olat.data.notification.DaoObjectMother.createForumPublisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.system.commons.date.DateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
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
public class NotificationEventDaoITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private NotificationEventDao eventDao;

    @Autowired
    private PublisherDao publisherDao;

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    GenericDao<IdentityImpl> identityDao;

    @Autowired
    private GenericDao<NotificationEvent> notificationEventDao;

    @Autowired
    private HibernateTransactionManager transactionManager;

    @PostConstruct
    void initType() {
        identityDao.setType(IdentityImpl.class);
        notificationEventDao.setType(NotificationEvent.class);
    }

    @Autowired
    private SubscriberDao subscriberDao;

    private Publisher publisher;
    private Publisher publisher_2;
    private Publisher publisher_3;

    private Subscription subscription_1;
    private Subscription subscription_2;
    private Subscription subscription13;
    private Subscriber subscriber_1; // subscribes to publisher and publisher_2
    private Subscriber subscriber_2; // subscribes to publisher and publisher_2
    Long contextId = new Long(123);
    Long contextId_2 = new Long(223);
    Long subcontextId = new Long(222);
    Long sourceId = new Long(456);

    String usernameOne = "test1_" + System.currentTimeMillis();
    String usernameTwo = "test2_" + System.currentTimeMillis();

    @Before
    public void setup() {

        publisher = createForumPublisher(contextId, subcontextId, sourceId);
        publisher = publisherDao.savePublisher(publisher);

        publisher_2 = createForumPublisher(contextId_2, subcontextId, sourceId);
        publisher_2 = publisherDao.savePublisher(publisher_2);

        publisher_3 = createForumPublisher(new Long(1), new Long(2), new Long(3));
        publisher_3 = publisherDao.savePublisher(publisher_3);

        subscriber_1 = createTestSubscriber(usernameOne);
        subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber_1, publisher);

        subscriber_2 = createTestSubscriber(usernameTwo);
        subscription_2 = subscriptionDao.createOrReuseSubscription(subscriber_2, publisher);

        subscriptionDao.createOrReuseSubscription(subscriber_1, publisher_2);
        subscriptionDao.createOrReuseSubscription(subscriber_2, publisher_2);

        subscription13 = subscriptionDao.createOrReuseSubscription(subscriber_1, publisher_3);

    }

    private Subscriber createTestSubscriber(String username) {
        IdentityImpl identity = identityDao.create();
        identity.setName(username);
        identity = identityDao.save(identity);
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(identity);
        return subscriber;
    }

    @Test
    public void createAndSaveEvent() {
        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, new HashMap<String, String>());
        assertNotNull(events);
        assertFalse(events.isEmpty());
        NotificationEvent event = events.get(0);
        assertEquals(NotificationEvent.Status.WAITING, event.getStatus());
        if (event.getSubscription().getSubscriber().getIdentity().getName().equals(usernameOne)) {
            assertEquals(subscription_1, event.getSubscription());
        } else if (event.getSubscription().getSubscriber().getIdentity().getName().equals(usernameTwo)) {
            assertEquals(subscription_2, event.getSubscription());
        }
    }

    @Test
    public void findNotificationEvents_waiting() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        assertTrue(events.size() == 2);

        List<NotificationEvent> persistedEvents = eventDao.findNotificationEvents(NotificationEvent.Status.WAITING);
        assertTrue(persistedEvents.size() == 2);

        for (NotificationEvent event : persistedEvents) {

            Map<String, String> persistedAttributes = event.getAttributes();
            assertTrue(persistedAttributes.size() == 2);

            Subscriber.NotificationInterval interval = event.getSubscription().getSubscriber().getInterval();
            assertNotNull(interval);
        }
    }

    @Test
    public void findAllNotificationEvents_onePublisher() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        assertTrue(events.size() == 2);

        List<NotificationEvent> allPersistedEvents = eventDao.findAllNotificationEvents();
        assertTrue(allPersistedEvents.size() == 2);
    }

    @Test
    public void deleteEvents_IfDeleteSubscription() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        assertTrue(eventDao.findAllNotificationEvents().size() == 2);
        assertTrue(eventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size() == 2);

        System.out.println("now delete the subscriptions, one by one.");
        subscriptionDao.deleteSubscription(subscription_1);
        assertTrue(eventDao.findAllNotificationEvents().size() == 1);

        System.out.println("now delete the second subscription.");
        subscriptionDao.deleteSubscription(subscription_2);
        assertTrue(eventDao.findAllNotificationEvents().size() == 0);

        assertTrue(eventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size() == 0);

    }

    @Test
    public void findAllNotificationEvents_twoPublishers() {
        // two subscribers, two publishers, two events
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "NEW");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        assertEquals("Wrong number of events for publisher:", 2, events.size());

        List<NotificationEvent> allPersistedEvents = eventDao.findAllNotificationEvents();
        assertEquals("Wrong number of events:", 2, allPersistedEvents.size());

        List<NotificationEvent> events_2 = eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        assertEquals("Wrong number of events for publisher_2:", 2, events_2.size());

        events = eventDao.findAllNotificationEvents();
        assertEquals("Wrong number of events:", 4, events.size());
        for (NotificationEvent event : events) {
            System.out.println("findAllNotificationEvents_twoPublishers - contextId: " + event.getSubscription().getPublisher().getContextId());
        }
    }

    @Test
    public void testEquals_DifferentTypes() {
        assertFalse("Wrong equals implementation, different types are recognized as equals ", createTestNotificationEvent(0).equals(new Integer(1)));
    }

    @Test
    public void testEquals_Null() {
        assertFalse("Wrong equals implementation, null value is recognized as equals ", createTestNotificationEvent(0).equals(null));
    }

    @Test
    public void testEquals_SameObjects() {
        NotificationEvent testNotificationEvent = createTestNotificationEvent(0);
        assertTrue("Wrong equals implementation, same event is not recognized as equal ", testNotificationEvent.equals(testNotificationEvent));
    }

    @Test
    public void testEquals_DifferentNotificationElements() {
        assertFalse("Wrong equals implementation, different objects are recognized as equals ", createTestNotificationEvent(0).equals(createTestNotificationEvent(1)));
    }

    private NotificationEvent createTestNotificationEvent(int index) {
        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, new HashMap<String, String>());
        assertFalse(events.isEmpty());
        NotificationEvent event = events.get(index);
        return event;
    }

    @Test
    public void testEquals_differentAttributes() {
        debugEvents();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");
        eventDao.createAndSaveNotificationEvents(publisher_3, attributes);

        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());
        eventDao.createAndSaveNotificationEvents(publisher_3, attributes);
        debugEvents();

        List<NotificationEvent> allEvents = eventDao.findAllNotificationEvents();
        assertEquals(2, allEvents.size());
        NotificationEvent event_1 = allEvents.get(0);
        NotificationEvent event_2 = allEvents.get(1);
        assertNotSame(event_1, event_2);
        assertFalse(event_1.equals(event_2));
    }

    private void debugEvents() {
        List<NotificationEvent> allEvents = eventDao.findAllNotificationEvents();
        Iterator<NotificationEvent> eventsIterator = allEvents.iterator();
        while (eventsIterator.hasNext()) {
            NotificationEvent event = eventsIterator.next();
            if (!event.getSubscription().equals(subscription13)) {
                Publisher publisher = event.getSubscription().getPublisher();
                System.out.println("----------------");
                System.out.println("publisher: " + publisher.getContextId() + " subscription13: " + subscription13.getPublisher().getContextId());
                System.out.println("publisher: " + publisher.getSourceId() + " subscription13: " + subscription13.getPublisher().getSourceId());
                System.out.println("----------------");
            }
        }

    }

    @Test
    public void getEventsForIdentity() throws InterruptedException {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");
        Date dateFrom = new Date();
        Thread.sleep(2000);
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        Thread.sleep(2000);
        Date dateTo = new Date();
        Thread.sleep(2000);
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        DateFilter dateFilter = new DateFilter(dateFrom, dateTo);
        List<NotificationEvent> events = eventDao.getEventsForIdentity(subscriber_1.getIdentity(), dateFilter);
        assertEquals(events.size(), 1);

    }

    @Test
    public void getEventsForIdentityNotFoundForDateFilter() throws InterruptedException {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");
        Date dateFrom = new Date();
        Thread.sleep(2000);
        Date dateTo = new Date();
        Thread.sleep(2000);
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        DateFilter dateFilter = new DateFilter(dateFrom, dateTo);
        List<NotificationEvent> events = eventDao.getEventsForIdentity(subscriber_1.getIdentity(), dateFilter);
        assertEquals(events.size(), 0);

    }

    @Test
    public void getEventsForIdentityInvalidSubscription() throws InterruptedException {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");
        Date dateFrom = new Date();
        Thread.sleep(2000);
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        Thread.sleep(2000);
        Date dateTo = new Date();
        Thread.sleep(2000);
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        // invalidate subscriptions
        for (Subscription subscription : subscriber_1.getSubscriptions()) {
            subscription.setStatus(Subscription.Status.INVALID);
            subscriptionDao.deleteSubscription(subscription);
        }
        DateFilter dateFilter = new DateFilter(dateFrom, dateTo);
        List<NotificationEvent> events = eventDao.getEventsForIdentity(subscriber_1.getIdentity(), dateFilter);
        assertEquals(events.size(), 0);

    }

    @Test
    public void findEventsBySubscriber() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        assertTrue(events.size() == 2);

        List<NotificationEvent> waitingEventsBySubscriber1 = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertTrue(waitingEventsBySubscriber1.size() == 1);

        List<NotificationEvent> waitingEventsBySubscriber2 = eventDao.findNotificationEventsBySubscriber(subscriber_2.getId());
        assertTrue(waitingEventsBySubscriber2.size() == 1);
    }

    @Test
    public void findEventsBySubscriberMoreEvents() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        // create events for 2 publishers with identical attributes
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);

        List<NotificationEvent> waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        // one event for each subscription
        assertTrue(waitingEvents.size() == 2);

        // change event type attribute
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());

        // create again events for 2 publishers
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);

        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        // same number of events since only the event type changed, but SOURCE_ENTRY_ID is the same
        assertTrue(waitingEvents.size() == 2);

        // change attributes type and SOURCE_ENTRY_ID
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_200");
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());

        // create events with the new attributes
        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(3, waitingEvents.size());

        eventDao.createAndSaveNotificationEvents(publisher_2, attributes);
        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(3, waitingEvents.size());

    }

    @Test
    public void findEventsBySubscriber_sortedAfterCreationDate() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        eventDao.createAndSaveNotificationEvents(publisher, attributes);

        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), "CHANGED");

        eventDao.createAndSaveNotificationEvents(publisher, attributes);

        List<NotificationEvent> allPersistedEvents = eventDao.findAllNotificationEvents();
        assertEquals(4, allPersistedEvents.size());

        for (NotificationEvent event : allPersistedEvents) {
            if (event.getAttributes().get(NotificationEvent.Attribute.EVENT_TYPE.name()).equals(PublishEventTO.EventType.CHANGED.name())) {
                // we change the creation date to be before creation date of the NEW event
                event.setCreationDate(new Date(1));
                eventDao.updateEvent(event);
            }
        }

        List<NotificationEvent> waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(1, waitingEvents.size());
        // it finds the NEW events since it sorts the events 1. after the creation date and 2. after the id
        assertEquals(PublishEventTO.EventType.NEW.name(), waitingEvents.get(0).getAttributes().get("EVENT_TYPE"));
    }

    @Test
    public void findEventsBySubscriber_ifSubscriptionInvalid() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        List<NotificationEvent> waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(1, waitingEvents.size());

        // if subscription becomes invalid - no events should be found
        subscription_1.setStatus(Subscription.Status.INVALID);
        subscriptionDao.updateSubscription(subscription_1);
        /** TODO: REVIEW GET EVENTS PERFORMANCE: bb/01.06.2012 **/
        // flush necessary while findNotificationEventsBySubscriber is native SQL query
        transactionManager.getSessionFactory().getCurrentSession().flush();

        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(0, waitingEvents.size());

        // if subscription becomes valid - the events should be found
        subscription_1.setStatus(Subscription.Status.VALID);
        subscriptionDao.updateSubscription(subscription_1);
        /** TODO: REVIEW GET EVENTS PERFORMANCE: bb/01.06.2012 **/
        // flush necessary while findNotificationEventsBySubscriber is native SQL query
        transactionManager.getSessionFactory().getCurrentSession().flush();

        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(1, waitingEvents.size());
    }

    /**
     * should we find the failed as well as waiting, but not the delivered
     */
    @Test
    public void findEventsBySubscriber_WaitingFailedAndDelivered() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        List<NotificationEvent> waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(1, waitingEvents.size());

        NotificationEvent event = waitingEvents.get(0);
        event.setStatus(NotificationEvent.Status.FAILED);
        eventDao.updateEvent(event);
        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(0, waitingEvents.size());

        event.setStatus(NotificationEvent.Status.WAITING);
        eventDao.updateEvent(event);
        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(1, waitingEvents.size());

        event.setStatus(NotificationEvent.Status.DELIVERED);
        eventDao.updateEvent(event);
        waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(0, waitingEvents.size());
    }

    // complex data structure test
    @Test
    public void findEventsBySubscriberMoreSubscriptionsMoreEventsMoreAttributes() throws InterruptedException {

        Map<String, String> attributesSameSourceId_NewEvent = new HashMap<String, String>();
        attributesSameSourceId_NewEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributesSameSourceId_NewEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_11");

        Map<String, String> attributesSameSourceId_ChangeEvent = new HashMap<String, String>();
        attributesSameSourceId_ChangeEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());
        attributesSameSourceId_ChangeEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_11");

        Map<String, String> attributesSameSourceId_2_NewEvent = new HashMap<String, String>();
        attributesSameSourceId_2_NewEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributesSameSourceId_2_NewEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_21");

        Map<String, String> attributesSameSourceId_2_ChangeEvent = new HashMap<String, String>();
        attributesSameSourceId_2_ChangeEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());
        attributesSameSourceId_2_ChangeEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_21");

        Map<String, String> attributesSameSourceId_3_NewEvent = new HashMap<String, String>();
        attributesSameSourceId_3_NewEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributesSameSourceId_3_NewEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_31");

        Map<String, String> attributesSameSourceId_3_ChangeEvent = new HashMap<String, String>();
        attributesSameSourceId_3_ChangeEvent.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());
        attributesSameSourceId_3_ChangeEvent.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_31");

        Map<String, String> attributesDifferentSourceId_1 = new HashMap<String, String>();
        attributesDifferentSourceId_1.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributesDifferentSourceId_1.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_12");

        Map<String, String> attributesDifferentSourceId_2 = new HashMap<String, String>();
        attributesDifferentSourceId_2.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributesDifferentSourceId_2.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_13");

        List<NotificationEvent> expectedEventsSubscriber_1 = new ArrayList<NotificationEvent>();
        List<NotificationEvent> expectedEventsSubscriber_2 = new ArrayList<NotificationEvent>();

        // FIRST EVENT BLOCK - expected only one event for subscriber to be returned (max creation date)
        // same source entry id - events in same second
        eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_NewEvent);
        eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_ChangeEvent);
        // these events are expected to be returned
        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_ChangeEvent);
        // adding event to expected list
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);

        // SECOND EVENT BLOCK - expected only one event for subscriber to be returned (with max creation date despite that id of this event is less as id of other event
        // with lower creation date)
        // same source entry id - events in same second
        eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_2_NewEvent);
        // these events are expected to be returned
        events = eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_2_ChangeEvent);
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);
        events = eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_2_ChangeEvent);
        // we set creation date before creation date of previous events
        for (NotificationEvent notificationEvent : events) {
            notificationEvent.setCreationDate(new Date(1));
            eventDao.updateEvent(notificationEvent);
        }

        // THIRD EVENT BLOCK - expected only one event for subscriber to be returned (max creation date)
        // same source entry id - events in different second
        eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_3_NewEvent);
        eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_3_ChangeEvent);
        Thread.sleep(3000);
        // these events are expected to be returned
        events = eventDao.createAndSaveNotificationEvents(publisher, attributesSameSourceId_3_ChangeEvent);
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);

        // FOURTH EVENT BLOCK - these events are expected to be returned
        events = eventDao.createAndSaveNotificationEvents(publisher, attributesDifferentSourceId_1);
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);

        // FIFTH EVENT BLOCK - these events are expected to be returned
        events = eventDao.createAndSaveNotificationEvents(publisher, attributesDifferentSourceId_2);
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);

        // SIXTH EVENT BLOCK - events for another publisher - these events are expected to be returned
        events = eventDao.createAndSaveNotificationEvents(publisher_2, attributesDifferentSourceId_2);
        addedEventsToExpected(expectedEventsSubscriber_1, expectedEventsSubscriber_2, events);

        List<NotificationEvent> eventsSubscriber_1 = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        List<NotificationEvent> eventsSubscriber_2 = eventDao.findNotificationEventsBySubscriber(subscriber_2.getId());

        boolean hasAllEvents = true;

        for (NotificationEvent event : expectedEventsSubscriber_1) {
            if (!eventsSubscriber_1.contains(event)) {
                hasAllEvents = false;
            }
        }
        assertTrue(hasAllEvents);

        hasAllEvents = true;

        for (NotificationEvent event : expectedEventsSubscriber_2) {
            if (!eventsSubscriber_2.contains(event)) {
                hasAllEvents = false;
            }
        }
        assertTrue(hasAllEvents);

    }

    private void addedEventsToExpected(List<NotificationEvent> expectedEventsSubscriber_1, List<NotificationEvent> expectedEventsSubscriber_2,
            List<NotificationEvent> events) {
        for (NotificationEvent notificationEvent : events) {
            if (notificationEvent.getSubscription().getSubscriber().equals(subscriber_1)) {
                expectedEventsSubscriber_1.add(notificationEvent);
            } else if (notificationEvent.getSubscription().getSubscriber().equals(subscriber_2)) {
                expectedEventsSubscriber_2.add(notificationEvent);
            }
        }
    }

    @Test
    public void deleteOldEvents() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");
        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        /** update notificationevent creationdate to 32 days in past */
        for (NotificationEvent notificationEvent : events) {
            notificationEvent.setCreationDate(new Date(System.currentTimeMillis() - 2752000000L));
            eventDao.updateEvent(notificationEvent);
        }
        attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_200");
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        attributes = new HashMap<String, String>();
        attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
        attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_300");
        eventDao.createAndSaveNotificationEvents(publisher, attributes);
        events = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(3, events.size());
        /** news date is set 31 day to past */
        eventDao.deleteOldEvents(new Date(System.currentTimeMillis() - 2666000000L));
        events = eventDao.findNotificationEventsBySubscriber(subscriber_1.getId());
        assertEquals(2, events.size());
    }
}
