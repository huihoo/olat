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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * Initial Date: 30.04.2012 <br>
 * 
 * @author aabouc
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class UpdateEventsITCaseNew extends AbstractJUnit4SpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private NotificationTestDataGenerator notificationTestDataGenerator;
    @Autowired
    private HibernateTransactionManager transactionManager;

    private List<Publisher> publishers;
    private List<Subscriber> subscribers;

    private final int NUMBER_OF_SUBSCRIBERS = 1;
    private final int NUMBER_OF_PUBLISHERS = 2;

    @Autowired
    private NotificationEventDao eventDao;

    @Before
    public void setup() {

        subscribers = generateSubscribers();
        publishers = generatePublishers();
        for (Publisher publisher : publishers) {
            generateSubscriptions(subscribers, publisher);
        }

        for (Publisher publisher : publishers) {
            generateEvents(publisher);
        }

    }

    private List<Subscriber> generateSubscribers() {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<List<Subscriber>>() {
            public List<Subscriber> doInTransaction(TransactionStatus status) {
                return notificationTestDataGenerator.generateSubscribers(NUMBER_OF_SUBSCRIBERS);
            }
        });
    }

    private List<Publisher> generatePublishers() {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<List<Publisher>>() {
            public List<Publisher> doInTransaction(TransactionStatus status) {
                return notificationTestDataGenerator.generatePublishers(NUMBER_OF_PUBLISHERS);
            }
        });
    }

    private void generateSubscriptions(final List<Subscriber> subscribers, final Publisher publisher) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                notificationTestDataGenerator.generateSubscriptionsForListSubscribersAndOnePublisher(subscribers, publisher);
            }
        });
    }

    private void generateEvents(final Publisher publisher) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.NEW.name());
                attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_100");

                eventDao.createAndSaveNotificationEvents(publisher, attributes);

                attributes.put(NotificationEvent.Attribute.EVENT_TYPE.name(), PublishEventTO.EventType.CHANGED.name());

                eventDao.createAndSaveNotificationEvents(publisher, attributes);

                attributes.put(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name(), "message_200");

            }
        });
    }

    @Test
    /** TODO: set to ignore because cleanUp does not work */
    public void updateEvents() {

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<NotificationEvent> allEvents = eventDao.findAllNotificationEvents();

                assertEquals(4, allEvents.size());

                for (NotificationEvent event : allEvents) {
                    assertEquals(NotificationEvent.Status.WAITING.name(), event.getStatus().name());
                }
                List<Long> waitingEventsIds = new ArrayList<Long>();
                for (Subscriber subscriber : subscribers) {
                    List<NotificationEvent> waitingEvents = eventDao.findNotificationEventsBySubscriber(subscriber.getId());
                    for (NotificationEvent event : waitingEvents) {
                        waitingEventsIds.add(event.getId());
                    }
                }
                List<Long> eventsToUpdateForSubscriberIds = eventDao.getEventsForUpdate(waitingEventsIds);
                eventDao.updateEventsByIds(new HashSet<Long>(eventsToUpdateForSubscriberIds), NotificationEvent.Status.DELIVERED);

            }
        });

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<NotificationEvent> allEvents = eventDao.findAllNotificationEvents();
                for (NotificationEvent event : allEvents) {
                    assertEquals(NotificationEvent.Status.DELIVERED.name(), event.getStatus().name());
                }
            }
        });

    }

    @After
    public void cleanUp() {

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.info("cleanUp started");
                notificationTestDataGenerator.deleteAllSubscribers();

            }
        });

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                notificationTestDataGenerator.deleteAllPublishers();
                log.info("cleanUp finished");
            }
        });
    }

}
