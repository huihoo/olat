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

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Subscriber.Channel;
import org.olat.data.notification.Subscriber.NotificationInterval;
import org.olat.data.notification.Subscriber.SubscriptionOption;
import org.olat.lms.core.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 29.11.2011 <br>
 * 
 * @author lavinia
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class SubscriberDaoITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    GenericDao<IdentityImpl> identityDao;

    @PostConstruct
    void initType() {
        identityDao.setType(IdentityImpl.class);
    }

    @Autowired
    private SubscriberDao subscriberDao;

    @Autowired
    private DaoObjectMother daoObjectMother;
    @Autowired
    private NotificationEventDao notificationEventDao;
    @Autowired
    private NotificationService notificationService;

    private IdentityImpl testIdentity;

    @Before
    public void setup() {
        testIdentity = createAndSaveIdentity("test1");
    }

    @Test
    public void createAndSaveSubscriber() {
        Subscriber subscriberEntity = createTestSubscriber();

        assertNotNull(subscriberEntity);
        // assert that identity is ok
        assertEquals(testIdentity, subscriberEntity.getIdentity());

        // assert that default channel is ok
        assertEquals("Wrong number of default channel", 1, subscriberEntity.getChannels().size());
        assertTrue(subscriberEntity.getChannels().contains(Channel.EMAIL));

        assertEquals("Wrong default notification interval", NotificationInterval.DAILY, subscriberEntity.getInterval());

        assertEquals("Wrong default subscription option", SubscriptionOption.SELECTION, subscriberEntity.getOption());
    }

    @Ignore
    // ignore since removed flush after save
    @Test(expected = ConstraintViolationException.class)
    public void createAndSaveSubscriber_cannotCreateSameSubscriberTwice() {
        Subscriber subscriberEntity_1 = createTestSubscriber();
        Subscriber subscriberEntity_2 = createTestSubscriber();
    }

    @Test
    public void findSubscriber_notFound() {
        assertNull(subscriberDao.findSubscriber(testIdentity));
    }

    @Test
    public void findSubscriber_existing() {
        Subscriber subscriberEntity = subscriberDao.createAndSaveSubscriber(testIdentity);
        Subscriber storedSubscriber = subscriberDao.findSubscriber(testIdentity);

        assertNotNull(storedSubscriber);
        assertEquals(subscriberEntity, storedSubscriber);
    }

    @Test
    public void findSubscriber_noneFound() {
        Subscriber storedSubscriber = subscriberDao.findSubscriber(testIdentity);

        assertNull(storedSubscriber);
    }

    @Test
    public void getSubscriberIDsByEventStatus_findsNone() {
        List<Long> subscriberIDs = subscriberDao.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING);
        Iterator<Long> idsIterator = subscriberIDs.iterator();
        assertFalse(idsIterator.hasNext());
    }

    @Test
    public void getSubscriberIDsByEventStatus_findsTwo() {
        daoObjectMother.createThreeIdentities_subscribe_publishOneEvent(notificationService);
        List<Long> subscriberIDs = subscriberDao.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING);
        Iterator<Long> idsIterator = subscriberIDs.iterator();
        assertTrue(idsIterator.hasNext());
        Long subscriberId = idsIterator.next();
        assertNotNull(subscriberId);// alf
        assertTrue(idsIterator.hasNext());
        assertNotNull(idsIterator.next());// berta
        assertFalse(idsIterator.hasNext());
    }

    @Test
    public void findSubscriberById() {
        daoObjectMother.createThreeIdentities_subscribe_publishOneEvent(notificationService);
        List<Long> subscriberIDs = subscriberDao.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING);
        Iterator<Long> idsIterator = subscriberIDs.iterator();
        assertTrue(idsIterator.hasNext());
        while (idsIterator.hasNext()) {
            Subscriber subscriber = subscriberDao.findSubscriber(idsIterator.next());
            assertTrue(DaoObjectMother.USER_ALF.equals(subscriber.getIdentity().getName()) || DaoObjectMother.USER_BERTA.equals(subscriber.getIdentity().getName()));
        }
    }

    private IdentityImpl createAndSaveIdentity(String username) {
        IdentityImpl identity = identityDao.create();
        identity.setName(username);
        identity = identityDao.save(identity);
        return identity;
    }

    @Test
    public void updateSubscriber_viaSetXY() {
        Subscriber subscriberEntity = subscriberDao.createAndSaveSubscriber(testIdentity);
        subscriberEntity.setInterval(Subscriber.NotificationInterval.HOURLY);
        subscriberEntity.setOption(Subscriber.SubscriptionOption.SELECTION);

        Subscriber persistedSubscriber = subscriberDao.updateSubscriber(subscriberEntity);

        assertTrue(persistedSubscriber.getChannels().contains(Subscriber.Channel.EMAIL));
        assertTrue(persistedSubscriber.getInterval().equals(Subscriber.NotificationInterval.HOURLY));
        assertTrue(persistedSubscriber.getOption().equals(Subscriber.SubscriptionOption.SELECTION));
    }

    @Test
    public void testEquals_DifferentTypes() {
        assertFalse("Wrong equals implementation, different types are recognized as equals ", createTestSubscriber().equals(new Integer(1)));
    }

    @Test
    public void testEquals_Null() {
        assertFalse("Wrong equals implementation, null value is recognized as equals ", createTestSubscriber().equals(null));
    }

    @Test
    public void testEquals_Same() {
        Subscriber testSubscriber = createTestSubscriber();
        assertTrue("Wrong equals implementation, same objects are NOT recognized as equals ", testSubscriber.equals(testSubscriber));
    }

    private Subscriber createTestSubscriber() {
        return subscriberDao.createAndSaveSubscriber(testIdentity);
    }

    @Test
    public void testEquals_DifferentSubscribers() {
        IdentityImpl otherIdentity = createAndSaveIdentity("test2");
        Subscriber otherSubscriber = subscriberDao.createAndSaveSubscriber(otherIdentity);
        assertFalse("Wrong equals implementation, different objects are recognized as equals ", createTestSubscriber().equals(otherSubscriber));
    }

    @Test
    public void deleteSubscriber() {
        Subscriber subscriberEntity = createTestSubscriber();
        Subscriber persistedSubscriberEntity = subscriberDao.findSubscriber(testIdentity);
        assertNotNull(persistedSubscriberEntity);
        subscriberDao.deleteSubscriber(testIdentity);

        persistedSubscriberEntity = subscriberDao.findSubscriber(testIdentity);
        assertTrue(persistedSubscriberEntity == null);
    }

    @Test
    public void deleteSubscriber_cascadeDelete() {
        int numEvents = daoObjectMother.createThreeIdentities_subscribe_publishOneEvent(notificationService);
        assertTrue(numEvents > 0);
        assertEquals(3, subscriberDao.getAllSubscriberIds().size());
        assertEquals(2, notificationEventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size());

        Identity alf = daoObjectMother.getSubscriberIdentityAlf();
        Subscriber subscriber = subscriberDao.findSubscriber(alf);
        assertNotNull(subscriber);

        subscriberDao.deleteSubscriber(alf);
        assertTrue(subscriberDao.findSubscriber(alf) == null);
        assertEquals(2, subscriberDao.getAllSubscriberIds().size());
        assertEquals(1, notificationEventDao.findNotificationEvents(NotificationEvent.Status.WAITING).size());
    }

    @Test
    public void testGetAllSubscriberIds() {
        subscriberDao.createAndSaveSubscriber(createAndSaveIdentity("test11"));
        subscriberDao.createAndSaveSubscriber(createAndSaveIdentity("test12"));
        List<Long> ids = subscriberDao.getAllSubscriberIds();
        assertEquals(2, ids.size());
    }

    @Test
    public void testDeleteInvalidSubscribers_oneUserGetsDeleted() {
        subscriberDao.createAndSaveSubscriber(createAndSaveIdentity("test11"));

        IdentityImpl inactiveIdentity = createAndSaveIdentity("test12");
        inactiveIdentity.setStatus(Identity.STATUS_DELETED);
        identityDao.update(inactiveIdentity);
        subscriberDao.createAndSaveSubscriber(inactiveIdentity);

        subscriberDao.createAndSaveSubscriber(createAndSaveIdentity("test13"));

        List<Subscriber> subscribers = subscriberDao.findAll();
        assertEquals(3, subscribers.size());
        subscriberDao.deleteInvalidSubscribers();
        subscribers = subscriberDao.findAll();
        assertEquals(2, subscribers.size());
    }

    @Test
    public void testDeleteInvalidSubscribers_oneUserGetsDeleted_oneUserGetsLoginDenied() {
        subscriberDao.createAndSaveSubscriber(createAndSaveIdentity("test11"));

        IdentityImpl inactiveIdentity = createAndSaveIdentity("test12");
        inactiveIdentity.setStatus(Identity.STATUS_DELETED);
        identityDao.update(inactiveIdentity);
        subscriberDao.createAndSaveSubscriber(inactiveIdentity);

        IdentityImpl loginDeniedIdentity = createAndSaveIdentity("test13");
        loginDeniedIdentity.setStatus(Identity.STATUS_LOGIN_DENIED);
        identityDao.update(loginDeniedIdentity);
        subscriberDao.createAndSaveSubscriber(loginDeniedIdentity);

        List<Subscriber> subscribers = subscriberDao.findAll();
        assertEquals(3, subscribers.size());
        subscriberDao.deleteInvalidSubscribers();
        subscribers = subscriberDao.findAll();
        assertEquals(2, subscribers.size());
    }

}
