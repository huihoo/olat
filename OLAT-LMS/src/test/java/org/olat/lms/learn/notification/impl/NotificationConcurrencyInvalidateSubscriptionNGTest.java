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
package org.olat.lms.learn.notification.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.notification.Subscription;
import org.olat.lms.core.notification.impl.NotificationServiceImpl;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Concurrency test. <br/>
 * 
 * Test case: <br/>
 * E) publishEvents() + invalidateUnaccesibleSubscriptions() concurrently <br/>
 * F) TODO: unsubscribe + invalidateUnaccesibleSubscriptions() concurrently <br/>
 * 
 * Run with testng_concurrency.xml.
 * 
 * Initial Date: 19.04.2012 <br>
 * 
 * @author lavinia
 */
@ContextConfiguration(locations = { "classpath:org/olat/lms/learn/_spring/lmsLearnTestContext.xml",
        "classpath:org/olat/data/notification/_spring/notificationContextTest.xml", "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class NotificationConcurrencyInvalidateSubscriptionNGTest extends AbstractTestNGSpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    GenericDao<IdentityImpl> daoIdentity;

    @Autowired
    NotificationLearnServiceImpl learnServiceImpl;

    @Autowired
    NotificationServiceImpl coreServiceImpl;

    @Autowired
    DaoObjectMother daoObjectMother;

    final int numberOfUsers = 2;

    private final String IDENTITY_NAME_PREFIX = "testIdentity";
    // publisher info
    private final Long CONTEXT_ID = Long.valueOf(1);
    private final Long SOURCE_ID = Long.valueOf(2);
    private final Long SUBCONTEXT_ID = Long.valueOf(3);
    private String forumSourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
    private ContextType courseContextType = Publisher.ContextType.COURSE;

    List<Identity> identities = new ArrayList<Identity>();// unique identities
    Identity eventCreator;
    List<NotificationSubscriptionContext> contextsOfSecondIdentity;

    @BeforeClass
    public void setup() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                daoIdentity.setType(IdentityImpl.class);

                // generate a list of unique identities
                int counter = 0;
                while (counter++ < numberOfUsers) {
                    Identity identity = createUniqueIdentityWithSystemCurrentTimeMills();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    identities.add(identity);
                }

                eventCreator = createUniqueIdentityWithSystemCurrentTimeMills();

                System.out.println("setup finished");
                log.info("setup finished");
            }
        });

    }

    @AfterClass
    public void cleanUp() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                log.info("cleanUp finished");
            }
        });

    }

    private Identity createUniqueIdentityWithSystemCurrentTimeMills() {
        String uniqueUsername = IDENTITY_NAME_PREFIX + System.currentTimeMillis();
        System.out.println("uniqueUsername - " + uniqueUsername);
        return daoObjectMother.createAndSaveIdentity(uniqueUsername);
    }

    public void subscribe(List<Identity> identities) {
        for (Identity identity : identities) {
            NotificationSubscriptionContext notificationSubscriptionContext = new NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID,
                    courseContextType, CONTEXT_ID, SUBCONTEXT_ID);
            System.out.println("Thread " + Thread.currentThread().getId() + " --- identity.getName - " + identity.getName());

            try { // temporary add delays between threads
                int timeToSleep = 1000;
                Thread.sleep(timeToSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Subscription subscription = learnServiceImpl.subscribe(notificationSubscriptionContext);
        }
    }

    @Test(invocationCount = 1)
    public void subscribeAll() {
        subscribe(identities);
    }

    /**
     * E)
     */
    /** TODO: REVIEW CONCURRENCY: bb/07.06.2012 no concurrency problems should appear **/
    @Test(invocationCount = 1, dependsOnMethods = "subscribeAll")
    public void invalidateUnaccesibleSubscriptions_concurrentWithPublishEvent() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- invalidateUnaccesibleSubscriptions_concurrentWithPublishEvent started");

        List<NotificationSubscriptionContext> contexts = getNotificationSubscriptionContexts(identities.get(0));
        Assert.assertTrue(contexts.size() == 1);
        learnServiceImpl.invalidateUnaccesibleSubscriptions(contexts);

        System.out.println("Thread " + Thread.currentThread().getId() + "  --- invalidateUnaccesibleSubscriptions_concurrentWithPublishEvent finished");
    }

    /**
     * E)
     */
    @Test(invocationCount = 1, dependsOnMethods = "subscribeAll")
    public void publishEvent_concurrentWithInvalidateUnaccesibleSubscriptions() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishEvent_concurrentWithInvalidateUnaccesibleSubscriptions started");
        int numberOfEvents = publishEventForDefaultPublisher();
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishEvent_concurrentWithInvalidateUnaccesibleSubscriptions finished");
    }

    private int publishEventForDefaultPublisher() {
        // publish only one event
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(courseContextType, CONTEXT_ID, "contextTitle", SUBCONTEXT_ID, forumSourceType, SOURCE_ID,
                "sourceTitle", "sourceEntryTitle", eventCreator, EventType.NEW);
        int numberOfEvents = coreServiceImpl.publishEvent(publishEventTO);
        return numberOfEvents;
    }

    /**
     * returns NotificationSubscriptionContext list for this identity, or null if nothing found.
     */
    private List<NotificationSubscriptionContext> getNotificationSubscriptionContexts(Identity identity) {
        List<Long> subscriberIds = learnServiceImpl.getAllSubscriberKeys();
        System.out.println("subscriberIds: " + subscriberIds.size());
        // Assert.assertTrue(subscriberIds.size() == identities.size());

        Iterator<Long> idsIterator = subscriberIds.iterator();
        while (idsIterator.hasNext()) {
            Long subscriberId = idsIterator.next();
            System.out.println("subscriberId: " + subscriberId);
            List<NotificationSubscriptionContext> notificationCourseAccessTOs = learnServiceImpl.getNotificationSubscriptionContexts(subscriberId);
            if (notificationCourseAccessTOs != null && notificationCourseAccessTOs.size() > 0) {
                if (notificationCourseAccessTOs.get(0).getIdentity().getName().equals(identity.getName())) {
                    return notificationCourseAccessTOs;
                }
            }
        }
        return null;
    }

    /**
     * runs before F)
     */
    @Test(invocationCount = 1, dependsOnMethods = "invalidateUnaccesibleSubscriptions_concurrentWithPublishEvent")
    public void getNotificationSubscriptionContexts() {
        contextsOfSecondIdentity = getNotificationSubscriptionContexts(identities.get(1));
        Assert.assertTrue(contextsOfSecondIdentity.size() == 1);
    }

    /**
     * F)
     */
    @Test(invocationCount = 1, dependsOnMethods = "getNotificationSubscriptionContexts")
    public void invalidateUnaccesibleSubscriptions_concurrentWithUnsubscribe() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- invalidateUnaccesibleSubscriptions_concurrentWithUnsubscribe started");

        List<NotificationSubscriptionContext> contexts = getNotificationSubscriptionContexts(identities.get(1));
        Assert.assertTrue(contexts == null);
        // even contexts == null, still call invalidateUnaccesibleSubscriptions for the contextsOfSecondIdentity got within the previous test.
        learnServiceImpl.invalidateUnaccesibleSubscriptions(contextsOfSecondIdentity);

        System.out.println("Thread " + Thread.currentThread().getId() + "  --- invalidateUnaccesibleSubscriptions_concurrentWithUnsubscribe finished");
    }

    /**
     * F)
     */
    @Test(invocationCount = 1, dependsOnMethods = "getNotificationSubscriptionContexts")
    public void unsubscribe_concurrentWithInvalidateUnaccesibleSubscriptions() {
        Identity identity = identities.get(1);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unsubscribe_concurrentWithInvalidateUnaccesibleSubscriptions started for identity: "
                + identity.getName());

        NotificationSubscriptionContext notificationSubscriptionContext = new NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID, courseContextType,
                CONTEXT_ID, SUBCONTEXT_ID);
        learnServiceImpl.unSubscribe(notificationSubscriptionContext);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unsubscribe_concurrentWithInvalidateUnaccesibleSubscriptions finished");
    }

}
