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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationTestDataGenerator;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationService;
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
public class NotifySubscribersPerformance extends AbstractJUnit4SpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private NotificationService notificationServiceImpl;
    @Autowired
    private NotificationTestDataGenerator notificationTestDataGenerator;
    @Autowired
    private HibernateTransactionManager transactionManager;
    @Autowired
    NotifyDelegate notifyDelegate;

    @Autowired
    private UriBuilder uriBuilder;

    private Identity creatorIdentity;

    private List<Publisher> publishers;

    private final int NUMBER_OF_SUBSCRIBERS = 10000;
    private final int NUMBER_OF_PUBLISHERS = 5000;
    private final int NUMBER_OF_SUBSCRIPTIONS_FOR_ONE_SUBSCRIBER = 3;

    private final int NUMBER_OF_CREATOR_IDENTITIES = 1;
    private final long MAX_EXECUTION_TIME_IN_SECONDS = 3600;

    @Before
    public void setup() {
        log.info("setup started");

        uriBuilder.setServerContextPathURI("/test");

        List<Subscriber> subscribers = generateSubscribers();
        publishers = generatePublishers();
        generateSubscriptionsForSubscribers(subscribers, publishers);

        creatorIdentity = generateIdentity();
        int i = 0;
        for (Publisher publisher : publishers) {
            generateEvents(publisher, i++);
        }
        log.info("setup finished");
    }

    private Identity generateIdentity() {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<Identity>() {
            public Identity doInTransaction(TransactionStatus status) {
                return notificationTestDataGenerator.generateIdentities(NUMBER_OF_CREATOR_IDENTITIES).get(0);
            }
        });
    }

    private List<Subscriber> generateSubscribers() {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<List<Subscriber>>() {
            public List<Subscriber> doInTransaction(TransactionStatus status) {
                List<Subscriber> subscribers = notificationTestDataGenerator.generateSubscribers(NUMBER_OF_SUBSCRIBERS);
                log.info("generateSubscribers - subscribers.size: " + subscribers.size());
                return subscribers;
            }
        });
    }

    private List<Publisher> generatePublishers() {
        return new TransactionTemplate(transactionManager).execute(new TransactionCallback<List<Publisher>>() {
            public List<Publisher> doInTransaction(TransactionStatus status) {
                List<Publisher> publishers = notificationTestDataGenerator.generatePublishers(NUMBER_OF_PUBLISHERS);
                log.info("generatePublishers - publishers.size: " + publishers.size());
                return publishers;
            }
        });
    }

    private void generateSubscriptionsForSubscribers(final List<Subscriber> subscribers, final List<Publisher> publishers) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                notificationTestDataGenerator.generateRandomSubscriptions(subscribers, publishers, NUMBER_OF_SUBSCRIPTIONS_FOR_ONE_SUBSCRIBER);
            }
        });
    }

    private void generateEvents(final Publisher publisher, final int counter) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                int numOfEvents = notificationServiceImpl.publishEvent(getPublishEventTO(publisher, counter));
                log.info("generateEvents - numOfEvents: " + numOfEvents);
            }
        });
    }

    @Test
    public void notifySubscribers() {

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                long startTime = System.currentTimeMillis();

                NotifyStatistics notifyStatistics = notificationServiceImpl.notifySubscribers();
                log.info("notifyStatistics.getDeliveredCounter(): " + notifyStatistics.getDeliveredCounter());
                log.info("notifyStatistics.getFailedCounter(): " + notifyStatistics.getFailedCounter());
                log.info("notifyStatistics.getTotalCounter(): " + notifyStatistics.getTotalCounter());

                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime) / 1000;
                log.info("TOTAL EXECUTION_TIME_IN_SECONDS[" + duration + "]  NEEDED FOR THE NOTIFY PROCESS TO NOTIFY [" + NUMBER_OF_SUBSCRIBERS
                        + "] SUBSCRIBERS WITH THE APPROPRIATE [" + (NUMBER_OF_SUBSCRIBERS * NUMBER_OF_SUBSCRIPTIONS_FOR_ONE_SUBSCRIBER) + "] EVENTS ");
                assertTrue("maximal execution time for " + NUMBER_OF_SUBSCRIBERS + " subscriptions exceeded: maximal time in seconds: " + MAX_EXECUTION_TIME_IN_SECONDS
                        + ", actual duration in seconds: " + duration, duration < MAX_EXECUTION_TIME_IN_SECONDS);

            }
        });

    }

    private PublishEventTO getPublishEventTO(Publisher publisher, int counter) {
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(publisher.getContextType(), publisher.getContextId(), "", publisher.getSubcontextId(),
                publisher.getSourceType(), publisher.getSourceId(), "", "", creatorIdentity, PublishEventTO.EventType.NEW);
        publishEventTO.setSourceEntryId(getSourceEntryId(publisher, counter));
        return publishEventTO;
    }

    private String getSourceEntryId(Publisher publisher, int index) {
        int generatedSourceEntryId = publisher.getSourceId().intValue() + index;
        System.out.println("getSourceEntryId: " + generatedSourceEntryId);
        return String.valueOf(generatedSourceEntryId);
    }

    @After
    public void cleanUp() {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                notificationTestDataGenerator.cleanupNotificationTestData();
            }
        });
    }

}
