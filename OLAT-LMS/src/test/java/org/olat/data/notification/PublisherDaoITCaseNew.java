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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.core.notification.NotificationTypeHandler;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublisherTO;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
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
public class PublisherDaoITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private PublisherDao publisherDao;

    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    GenericDao<IdentityImpl> identityDao;

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    void initType() {
        identityDao.setType(IdentityImpl.class);
    }

    @Autowired
    private NotificationEventDao eventDao;

    @Autowired
    private SubscriberDao subscriberDao;

    @Autowired
    private DaoObjectMother daoObjectMother;
    private Long contextId;
    private Long sourceId_1;

    @Before
    public void setup() {
        contextId = daoObjectMother.getContextId();
        sourceId_1 = daoObjectMother.getSourceIdOne();
    }

    @Test
    public void savePublisher_equals() {
        Publisher publisher = daoObjectMother.createForumOnePublisher();
        Publisher persistentPublisher = publisherDao.savePublisher(publisher);

        assertNotNull(persistentPublisher);
        assertEquals(publisher, persistentPublisher);
        assertEquals(persistentPublisher.getContextId(), contextId);
        assertEquals(persistentPublisher.getSourceId(), daoObjectMother.getSourceIdOne());
        assertEquals(persistentPublisher.getSubcontextId(), daoObjectMother.getSubcontextIdOne());
    }

    @Test
    public void findPublisher_findsOne() {
        Publisher publisher1 = daoObjectMother.createForumOnePublisher();
        publisherDao.savePublisher(publisher1);
        Publisher publisher2 = daoObjectMother.createForumTwoPublisher();
        publisherDao.savePublisher(publisher2);
        // create publisher only with different ContextType
        Publisher publisher3 = daoObjectMother.createForumTwoPublisher();
        publisher3.setContextType(Publisher.ContextType.UNKNOWN);
        publisherDao.savePublisher(publisher3);
        // create publisher only with different SourceType
        Publisher publisher4 = daoObjectMother.createForumTwoPublisher();
        publisher4.setSourceType(NotificationTypeHandler.UNKNOWN);
        publisherDao.savePublisher(publisher4);

        Publisher persistedPublisher2 = publisherDao.findPublisher(daoObjectMother.getContextId(), Publisher.ContextType.COURSE, daoObjectMother.getSourceIdTwo(),
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        assertEquals(publisher2, persistedPublisher2);
    }

    @Test
    public void findPublisher_findsNone() {
        Publisher publisher1 = daoObjectMother.createForumOnePublisher();
        publisherDao.savePublisher(publisher1);

        Publisher publisher2 = daoObjectMother.createForumTwoPublisher();
        publisherDao.savePublisher(publisher2);

        Publisher persistedPublisher2 = publisherDao.findPublisher(daoObjectMother.getOtherContextId(), Publisher.ContextType.COURSE, daoObjectMother.getSourceIdTwo(),
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        assertNull(persistedPublisher2);
    }

    @Test
    public void findPublisher_noPublisherExist() {
        Long unkownResourceableId = new Long(987654321);
        assertNull(publisherDao.findPublisher(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, unkownResourceableId));
    }

    @Test
    public void findPublisher_publisherExist() {
        Long contextIdOne = new Long(1);
        Long sourceIdOne = new Long(1);
        Long subcontextId_1 = new Long(111);

        Publisher publisher = daoObjectMother.createForumPublisher(contextIdOne, subcontextId_1, sourceIdOne);

        Publisher persistedPublisher = publisherDao.savePublisher(publisher);

        Publisher resultPublisher = publisherDao.findPublisher(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, sourceIdOne);

        assertNotNull(resultPublisher);
        assertEquals(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, resultPublisher.getSourceType());
        assertEquals(sourceIdOne, resultPublisher.getSourceId());
        assertEquals(contextIdOne, resultPublisher.getContextId());
        assertEquals(subcontextId_1, resultPublisher.getSubcontextId());
    }

    @Test
    public void createPublisherSubscriptionAndNotificationEventTest() {
        IdentityImpl identity = daoObjectMother.createAndSaveIdentity_("test1");
        Publisher publisher = createPublisherSubscriptionAndNotificationEvent(identity);

        Publisher persistedPublisher = publisherDao.findPublisher(publisher.getContextId(), publisher.getContextType(), publisher.getSourceId(),
                publisher.getSourceType());
        // is the object graph already stored?
        Set<Subscription> subscriptions = publisher.getSubscriptions();
        assertTrue(subscriptions.size() == 1);

        Subscription subscription = subscriptions.iterator().next();
        List<NotificationEvent> events = subscription.getNotificationEvents();
        assertTrue(events.size() == 1);

        System.out.println("event id: " + events.iterator().next().getId());
        assertNotNull(events.iterator().next().getId());

    }

    @Test
    public void deletePublisher_withAllDescendants() {

        IdentityImpl identity = daoObjectMother.createAndSaveIdentity_("test1");
        Publisher publisher = createPublisherSubscriptionAndNotificationEvent(identity);

        assertEquals(1, subscriptionDao.getSubscriptionsForIdentity(identity).size());
        assertEquals(1, eventDao.findAllNotificationEvents().size());

        Publisher persistedPublisher = publisherDao.findPublisher(publisher.getContextId(), publisher.getContextType(), publisher.getSourceId(),
                publisher.getSourceType());
        publisherDao.deletePublisher(persistedPublisher);

        persistedPublisher = publisherDao.findPublisher(publisher.getContextId(), publisher.getContextType(), publisher.getSourceId(), publisher.getSourceType());
        assertTrue(persistedPublisher == null);

        assertEquals(0, subscriptionDao.getSubscriptionsForIdentity(identity).size());
        assertEquals(0, eventDao.findAllNotificationEvents().size());
    }

    @Test
    public void deletePublisher_cascadeDelete() {
        daoObjectMother.createThreeIdentities_subscribe_publishOneEvent(notificationService);
        assertEquals(2, eventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size());
        Publisher publisher = publisherDao.findPublisher(daoObjectMother.getContextId(), Publisher.ContextType.COURSE, daoObjectMother.getSourceIdOne(),
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        assertNotNull(publisher);

        Identity alf = daoObjectMother.getSubscriberIdentityAlf();
        Subscriber subscriber = subscriberDao.findSubscriber(alf);
        assertNotNull(subscriber);

        List<Subscription> subscriptionList = subscriptionDao.getSubscriptionsForIdentity(alf);
        assertEquals(1, subscriptionList.size());

        daoObjectMother.publishEventAsClara(notificationService);
        assertEquals(4, eventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size());

        publisherDao.deletePublisher(publisher);
        assertEquals(0, eventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size());

        subscriptionList = subscriptionDao.getSubscriptionsForIdentity(alf);
        assertEquals(0, subscriptionList.size());
    }

    private Publisher createPublisherSubscriptionAndNotificationEvent(IdentityImpl identity) {
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(identity);

        Publisher publisher = daoObjectMother.createForumOnePublisher();
        publisher = publisherDao.savePublisher(publisher);

        Subscription subscription = subscriptionDao.createOrReuseSubscription(subscriber, publisher);
        assertTrue(subscription.getPublisher().equals(publisher));

        publisher = publisherDao.findPublisher(daoObjectMother.getContextId(), Publisher.ContextType.COURSE, daoObjectMother.getSourceIdOne(),
                ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        // assertTrue(publisher.getSubscriptions().size() == 1);

        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, new HashMap<String, String>());
        NotificationEvent event = events.get(0);
        assertNotNull(event);
        assertNotNull(event.getId());
        assertNotNull(event.getSubscription().getId());
        assertEquals("Wrong number of events", 1, events.size());

        List<NotificationEvent> foundEvents = eventDao.findAllNotificationEvents();
        assertEquals("Wrong number of events", 1, foundEvents.size());

        assertTrue(subscriptionDao.getSubscriptionsForIdentity(identity).size() > 0);
        return publisher;
    }

    @Test
    public void deletePublisher_notExistingPublisher() {
        Publisher publisher = daoObjectMother.createForumPublisher(123L, 111L, 456L);
        publisherDao.deletePublisher(publisher);
    }

    @Test
    public void testEquals_DifferentTypes() {
        assertFalse("Wrong equals implementation, different types are recognized as equals ", createTestPublisher().equals(new Integer(1)));
    }

    @Test
    public void testEquals_Null() {
        assertFalse("Wrong equals implementation, null value is recognized as equals ", createTestPublisher().equals(null));
    }

    @Test
    public void testEquals_Same() {
        Publisher testPublisher = createTestPublisher();
        assertTrue("Wrong equals implementation, same objects are NOT recognized as equals ", testPublisher.equals(testPublisher));
    }

    @Test
    public void testEquals_DifferentPublishers() {
        assertFalse("Wrong equals implementation, different objects are recognized as equals ", createTestPublisher().equals(createOtherTestPublisher()));
    }

    private Publisher createTestPublisher() {
        return daoObjectMother.createForumOnePublisher();
    }

    private Publisher createOtherTestPublisher() {
        return daoObjectMother.createForumTwoPublisher();
    }

    @Test
    public void testHashcode_Same() {
        assertTrue("Wrong hashCode implementation, same Publishers have NOT same hash-code ", createTestPublisher().hashCode() == createTestPublisher().hashCode());
    }

    @Test
    public void testHashcode_Different() {
        assertFalse("Wrong hashCode implementation, different Publishers have same hash-code", createTestPublisher().hashCode() == createOtherTestPublisher().hashCode());
    }

    @Test
    public void createAndSavePublisher() {
        Long contextId = 111L;
        ContextType contextType = Publisher.ContextType.COURSE;
        Long sourceId = 222L;
        String sourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
        Long subcontextId = 333L;
        Publisher createdPublisher = publisherDao.createAndSavePublisher(contextId, contextType, sourceId, sourceType, subcontextId);
        assertEquals("Wrong contextId", contextId, createdPublisher.getContextId());
        assertEquals("Wrong contextType", contextType, createdPublisher.getContextType());
        assertEquals("Wrong sourceId", sourceId, createdPublisher.getSourceId());
        assertEquals("Wrong sourceType", sourceType, createdPublisher.getSourceType());
        assertEquals("Wrong subcontextId", subcontextId, createdPublisher.getSubcontextId());

    }

    @Ignore
    // ignore since removed flush after save
    @Test(expected = ConstraintViolationException.class)
    public void createAndSavePublisher_cannotCreateSamePublisherTwice() {
        Long contextId = 111L;
        ContextType contextType = Publisher.ContextType.COURSE;
        Long sourceId = 222L;
        String sourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
        Long subcontextId = 333L;
        Publisher createdPublisher = publisherDao.createAndSavePublisher(contextId, contextType, sourceId, sourceType, subcontextId);

        Publisher createdPublisher2 = publisherDao.createAndSavePublisher(contextId, contextType, sourceId, sourceType, subcontextId);

    }

    @Test
    public void removePublishers() {
        Publisher publisher1 = daoObjectMother.createForumPublisher(new Long(1), new Long(10), new Long(11));
        publisherDao.savePublisher(publisher1);
        Publisher publisher2 = daoObjectMother.createForumPublisher(new Long(2), new Long(10), new Long(11));
        publisherDao.savePublisher(publisher2);
        assertEquals(2, publisherDao.findAll().size());

        Set<Long> contextIds = new HashSet<Long>();
        contextIds.add(publisher1.getContextId());
        contextIds.add(publisher2.getContextId());
        publisherDao.removePublishers(contextIds);
        assertEquals(0, publisherDao.findAll().size());
    }

    @Test
    @Ignore
    public void deletePublisher_withPublisherTOArgument() {
        // setup
        IdentityImpl identity = daoObjectMother.createAndSaveIdentity_("test1");
        Publisher publisher = createPublisherSubscriptionAndNotificationEvent(identity);

        // check setup
        Publisher storedPublisher = publisherDao.findPublisher(publisher.getContextId(), publisher.getContextType(), publisher.getSourceId(), publisher.getSourceType());
        assertNotNull(storedPublisher);

        List<Subscription> subscriptions = subscriptionDao.getSubscriptionsForIdentity(identity);
        assertTrue("Should find one subscription", subscriptions.size() == 1);

        List<NotificationEvent> events = eventDao.findNotificationEvents(NotificationEvent.Status.WAITING);
        assertTrue("Should find one event", events.size() == 1);

        // call tested method
        PublisherTO publisherTO = PublisherTO.createNewPublisherTOInCourse(publisher.getContextId(), publisher.getSubcontextId());
        // publisherDao.deletePublisher(publisherTO);

        // asserts that the publisher with its children was deleted
        storedPublisher = publisherDao.findPublisher(publisher.getContextId(), publisher.getContextType(), publisher.getSourceId(), publisher.getSourceType());
        assertTrue("Should not find any publisher", storedPublisher == null);

        subscriptions = subscriptionDao.getSubscriptionsForIdentity(identity);
        assertTrue("Should not find any subscription", subscriptions.size() == 0);

        events = eventDao.findNotificationEvents(NotificationEvent.Status.WAITING);
        assertTrue("Should not find any event", events.size() == 0);
    }
}
