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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.NotificationTestDataGenerator;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Alternative for PublishEventPerformanceITCaseNew, doesn't run in Hudson at the moment. <br/>
 * TODO: LD: keep either this or PublishEventPerformanceITCaseNew, not both.
 * 
 * Initial Date: 04.05.2012 <br>
 * 
 * @author lavinia
 */
@ContextConfiguration(locations = { "classpath:org/olat/lms/learn/_spring/lmsLearnTestContext.xml",
        "classpath:org/olat/data/notification/_spring/notificationContextTest.xml", "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class PublishEventPerformanceNGTest extends AbstractTestNGSpringContextTests {

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    private NotificationService notificationServiceImpl;
    @Autowired
    private NotificationTestDataGenerator notificationTestDataGenerator;

    private Publisher publisher;
    private Identity creatorIdentity;

    private final int NUMBER_OF_SUBSCRIBERS = 300;
    private final int NUMBER_OF_PUBLISHERS = 1;
    private final int NUMBER_OF_CREATOR_IDENTITIES = 1;
    private final long MAX_EXECUTION_TIME_IN_SECONDS = 10;

    @BeforeClass
    public void setup() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                List<Subscriber> subscribers = notificationTestDataGenerator.generateSubscribers(NUMBER_OF_SUBSCRIBERS);
                publisher = notificationTestDataGenerator.generatePublishers(NUMBER_OF_PUBLISHERS).get(0);
                notificationTestDataGenerator.generateSubscriptionsForListSubscribersAndOnePublisher(subscribers, publisher);
                creatorIdentity = notificationTestDataGenerator.generateIdentities(NUMBER_OF_CREATOR_IDENTITIES).get(0);
                assertEquals(NUMBER_OF_SUBSCRIBERS, notificationServiceImpl.getAllSubscriberKeys().size());
                // setup publishing - to execute to be sure that before second publishing which is already measured also UPDATE to OBSOLETE happens
                notificationServiceImpl.publishEvent(getPublishEventTO());
                System.out.println("setup finished");

            }
        });

    }

    private PublishEventTO getPublishEventTO() {
        return PublishEventTO.getValidInstance(publisher.getContextType(), publisher.getContextId(), "", publisher.getSubcontextId(), publisher.getSourceType(),
                publisher.getSourceId(), "", "", creatorIdentity, PublishEventTO.EventType.NEW);
    }

    @Test(invocationCount = 1)
    public void publishEventManySubscriptionsForOnePublisher() {
        long startTime = System.currentTimeMillis();
        // test in case publishEvent doesn't return the number of generated events - asynchronous event creation via ThreadPoolExecutor
        notificationServiceImpl.publishEvent(getPublishEventTO());
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        assertTrue("maximal execution time for " + NUMBER_OF_SUBSCRIBERS + " subscriptions exceeded: maximal time in seconds: " + MAX_EXECUTION_TIME_IN_SECONDS
                + ", actual duration in seconds: " + duration, duration < MAX_EXECUTION_TIME_IN_SECONDS);
        System.out.println("DURATION in seconds: " + duration);
    }
}
