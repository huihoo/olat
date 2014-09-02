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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.notification.NotificationTestDataGenerator;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.Subscriber.NotificationInterval;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.impl.NotificationServiceImpl;
import org.olat.lms.core.notification.impl.UriBuilder;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.learn.RetryerSpy;
import org.olat.lms.learn.StatelessTransactionRetryer;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Concurrency test. Automatically check if the TransactionRetryer catched and retried. <br/>
 * 
 * Use cases: <br/>
 * A) concurrent subscribe with n users. (INSERT)<br/>
 * B) publishEvent and unsubscribe concurrently. (DELETE) <br/>
 * C) notify and unsubscribe concurrently. (UPDATE) <br/>
 * D) notify and publishEvent concurrently. (UPDATE) <br/>
 * // TODO: delete subscriber + notify <br/>
 * ..... <br/>
 * 
 * 
 * 
 * Run with testng_concurrency.xml.
 * 
 * Initial Date: 13.03.2012 <br>
 * 
 * @author lavinia
 */

@ContextConfiguration(locations = { "classpath:org/olat/lms/learn/_spring/lmsLearnTestContext.xml",
        "classpath:org/olat/data/notification/_spring/notificationContextTest.xml", "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class NotificationServiceConcurrencyTestNG extends AbstractTestNGSpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    GenericDao<IdentityImpl> daoIdentity;

    @Autowired
    SubscriberDao daoSubscriber;

    @Autowired
    PublisherDao daoPublisher;

    @Autowired
    SubscriptionDao daoSubscription;

    @Autowired
    NotificationLearnServiceImpl learnServiceImpl;

    @Autowired
    NotificationServiceImpl coreServiceImpl;

    @Autowired
    private UriBuilder uriBuilder;

    @Autowired
    DaoObjectMother daoObjectMother;

    @Autowired
    StatelessTransactionRetryer transactionRetryer;
    RetryerSpy retryerSpy = new RetryerSpy();

    @Autowired
    private NotificationTestDataGenerator notificationTestDataGenerator;

    // counter for the transactionRetryer
    AtomicLong numRetriesAtomic = new AtomicLong(0);

    private final String IDENTITY_NAME = "testIdentity";
    // publisher info
    private final Long CONTEXT_ID = Long.valueOf(1);
    private final Long SOURCE_ID = Long.valueOf(2);
    private final Long SUBCONTEXT_ID = Long.valueOf(3);
    private String forumSourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
    private ContextType courseContextType = Publisher.ContextType.COURSE;

    List<Identity> identities = new ArrayList<Identity>();// unique identities
    Identity eventCreator;

    final AtomicInteger threadCounter = new AtomicInteger(0);
    final int numberThreads = 10;
    AtomicLong publisherId;
    boolean doSubscribersUpdate = false;

    @BeforeClass
    public void setup() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                transactionRetryer.setRetryerSpy(retryerSpy);
                daoIdentity.setType(IdentityImpl.class);

                // generate a list of unique identities
                int counter = 0;
                while (counter++ < numberThreads) {
                    Identity identity = createUniqueIdentityWithSystemCurrentTimeMills();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    identities.add(identity);
                }

                eventCreator = createUniqueIdentityWithSystemCurrentTimeMills();
                uriBuilder.setServerContextPathURI("/test");

                System.out.println("setup finished");
                log.info("setup finished");
            }
        });
    }

    @BeforeMethod
    public void setupBeforeMethod() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                System.out.println("setupBeforeMethod started");
                updateAllSubscribersNotificationInterval();

                System.out.println("setupBeforeMethod finished");
                log.info("setupBeforeMethod finished");
            }
        });
    }

    private void updateAllSubscribersNotificationInterval() {
        if (doSubscribersUpdate) {
            doSubscribersUpdate = false; // do it only once
            System.out.println("updateAllSubscribersNotificationInterval");

            for (Identity identity : identities) {
                Subscriber subscriber = daoSubscriber.findSubscriber(identity);
                if (subscriber != null) {
                    subscriber.setInterval(NotificationInterval.IMMEDIATELY);
                    daoSubscriber.updateSubscriber(subscriber);
                }
            }
        }
    }

    @AfterClass
    public void cleanUp() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                System.out.println("cleanUp started");

                // TODO: fix this, if really needed
                Publisher publisher = daoPublisher.findPublisher(CONTEXT_ID, courseContextType, SOURCE_ID, forumSourceType);
                Set<Subscription> subscriptionSet = publisher.getSubscriptions();
                // assertTrue("could not find numberThreads subscriptions", subscriptions.size() == numberThreads);

                // daoPublisher.deletePublisher(daoPublisher.findPublisher(CONTEXT_ID, courseContextType, SOURCE_ID, forumSourceType));
                for (Identity identity : identities) {
                    System.out.println("deleteSubscriber for identity: " + identity.getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // daoSubscriber.deleteSubscriber(identity);//TODO: LD: fix cleanup

                    // daoIdentity.delete((IdentityImpl) identity);
                }
                notificationTestDataGenerator.cleanupNotificationTestData();
                System.out.println("cleanUp finished");
            }
        });

    }

    /**
     * A) This should run first. It proofs that numberThreads users could subscribe concurrently. <br/>
     * It relies on the TransactionRetryer to retry a failed subscribe operation, configured via lmsLearnTestContext.xml. <br/>
     * Make sure that the <code>identities</code> size is greater than the numberThreads.
     */
    @Test(threadPoolSize = numberThreads, invocationCount = numberThreads)
    public void subscribeConcurrently() {

        System.out.println("Thread " + Thread.currentThread().getId() + " --- subscribeConcurrently");
        System.out.println("threadCounter: " + threadCounter.get());

        // for each thread use a new identity from the generated list
        int i = threadCounter.get();
        Identity identity = identities.get(i);
        if (i == 9) {
            System.out.println("add breakpoint to debug DB");
        }
        threadCounter.incrementAndGet();
        NotificationSubscriptionContext notificationSubscriptionContext = new NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID, courseContextType,
                CONTEXT_ID, SUBCONTEXT_ID);
        System.out.println("Thread " + Thread.currentThread().getId() + " --- identity.getName - " + identity.getName());

        /*
         * try { // temporary add delays between threads int timeToSleep = 1000 * counter; Thread.sleep(timeToSleep); } catch (InterruptedException e) {
         * e.printStackTrace(); }
         */
        Subscription subscription = learnServiceImpl.subscribe(notificationSubscriptionContext);
        System.out.println("Thread " + Thread.currentThread().getId() + "  ---  subscribeConcurrently finished for Publisher id: " + subscription.getPublisher().getId());

        Assert.assertNotNull(subscription.getPublisher().getId(), "the parent publisher id should not be null");
        if (publisherId == null) {
            publisherId = new AtomicLong(0);
            publisherId.set(subscription.getPublisher().getId());
        } else {
            Assert.assertEquals(subscription.getPublisher().getId(), new Long(publisherId.get()), "each subscription should have the same parent publisher");
        }
        if (i == 9) {
            assertRetries("ConstraintViolationException");
        }

    }

    /**
     * Asserts that at least one retry for the RuntimeException of the input type happened.
     */
    private void assertRetries(String runtimeExceptionName) {
        // assert if retries happened
        int numOfRetries = retryerSpy.getRetriesForException(runtimeExceptionName);
        // System.out.println("numOfRetries: " + numOfRetries);
        if (numOfRetries == 1) {
            numRetriesAtomic.set(numOfRetries);
        }
        System.out.println("numRetriesAtomic: " + numRetriesAtomic.get());

        // it should have retried at least once
        System.out.println("Assert numRetriesAtomic " + numRetriesAtomic.get() + " for " + runtimeExceptionName);
        Assert.assertTrue(numRetriesAtomic.get() > 0);

    }

    /**
     * B) Publish event, dependent on subscribeConcurrently, concurrently with unsubscribe. Depends on A) <br/>
     * 
     * It relies on the TransactionRetryer to retry a failed publishEvent operation, configured via lmsLearnTestContext.xml.
     */
    /** TODO: REVIEW CONCURRENCY: bb/07.06.2012 no concurrency problems should appear **/
    @Test(invocationCount = 1, dependsOnMethods = "subscribeConcurrently")
    public void publishEvent_concurrentWithUnsubscribe() {
        clearRetries();

        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishEvent");

        int numberOfEvents = publishEventForDefaultPublisher();
        System.out.println("Thread " + Thread.currentThread().getId() + "  ---  publishEvent finished - numberOfEvents: " + numberOfEvents);

        boolean isNumberOfPublishedEventsAcceptable = (numberOfEvents == numberThreads || numberOfEvents == numberThreads - 1);
        Assert.assertTrue(isNumberOfPublishedEventsAcceptable,
                "It was expected that the number of events are equals with the number of subscriptions-1, that is with the number of threads-1, since a subscription was unsubscribed");

        // outcommentd since introduced @OptimisticLock(excluded = true) for notificationEvents in Subscription
        // assertRetries("StaleObjectStateException");
    }

    private int publishEventForDefaultPublisher() {
        // publish only one event
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(courseContextType, CONTEXT_ID, "contextTitle", SUBCONTEXT_ID, forumSourceType, SOURCE_ID,
                "sourceTitle", "sourceEntryTitle", eventCreator, EventType.NEW);
        int numberOfEvents = coreServiceImpl.publishEvent(publishEventTO);
        return numberOfEvents;
    }

    private void clearRetries() {
        retryerSpy.clearRetries();
        numRetriesAtomic.set(0);
    }

    /**
     * B) Unsubscribe first identity in list, dependent on subscribeConcurrently, concurrently with publishEvent. Depends on A)
     */
    /** TODO: REVIEW CONCURRENCY: bb/07.06.2012 no concurrency problems should appear **/
    @Test(invocationCount = 1, dependsOnMethods = "subscribeConcurrently")
    public void unsubscribe_concurrentWithPublishEvent() {

        Identity identity = identities.get(0);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unSubscribe identity: " + identity.getName());

        NotificationSubscriptionContext notificationSubscriptionContext = new NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID, courseContextType,
                CONTEXT_ID, SUBCONTEXT_ID);
        learnServiceImpl.unSubscribe(notificationSubscriptionContext);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unSubscribe finished");
    }

    /**
     * C) notify and unsubscribe concurrently
     */
    @Test(invocationCount = 1, dependsOnMethods = "publishNewEvent")
    public void notifySubscribers_concurrentWithUnsubscribe() {
        clearRetries();
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- notifySubscribers_concurrentWithUnsubscribe started");

        NotifyStatistics notifyStatistics = coreServiceImpl.notifySubscribers();
        int numOfSentEmails = notifyStatistics.getTotalCounter();

        System.out.println("notifySubscribers - numOfSentEmails: " + numOfSentEmails);
        boolean isRangeOfSentEmailsAcceptable = (numOfSentEmails == 8 || numOfSentEmails == 9);
        Assert.assertTrue(isRangeOfSentEmailsAcceptable,
                "notifySubscribers should send 9 emails if it runs alone, or 8 emails if it runs concurrently with unsubscribe_concurrentWithNotifySubscribers");

        System.out.println("Thread " + Thread.currentThread().getId() + "  --- notifySubscribers_concurrentWithUnsubscribe finished");
        // assertRetries("StaleObjectStateException");
    }

    /**
     * C) notify and unsubscribe (second identity in list) concurrently
     */
    @Test(invocationCount = 1, dependsOnMethods = "publishNewEvent")
    public void unsubscribe_concurrentWithNotifySubscribers() {

        Identity identity = identities.get(1);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unsubscribe_concurrentWithNotifySubscribers identity: " + identity.getName());

        NotificationSubscriptionContext notificationSubscriptionContext = new NotificationSubscriptionContext(identity, forumSourceType, SOURCE_ID, courseContextType,
                CONTEXT_ID, SUBCONTEXT_ID);
        learnServiceImpl.unSubscribe(notificationSubscriptionContext);
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- unsubscribe_concurrentWithNotifySubscribers finished");
    }

    private Identity createUniqueIdentityWithSystemCurrentTimeMills() {
        String uniqueUsername = IDENTITY_NAME + System.currentTimeMillis();
        System.out.println("uniqueUsername - " + uniqueUsername);
        return daoObjectMother.createAndSaveIdentity(uniqueUsername);
    }

    /**
     * D) notify and publishEvent concurrently. Depends on B)
     */
    /** TODO: REVIEW CONCURRENCY: bb/07.06.2012 no concurrency problems should appear **/
    @Test(invocationCount = 1, dependsOnMethods = "publishEvent_concurrentWithUnsubscribe")
    public void publishEvent_concurrentWithNotifySubscribers() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishEvent_concurrentWithNotifySubscribers");

        int numberOfEvents = publishEventForDefaultPublisher();
        System.out.println("Thread " + Thread.currentThread().getId() + "  ---  publishEvent_concurrentWithNotifySubscribers finished - numberOfEvents: "
                + numberOfEvents);

        // int expected = numberThreads - 2; //if runs after C
        // int expected = numberThreads - 1; // if runs after B
        boolean isNumberOfPublishedEventsAcceptable = (numberOfEvents == numberThreads || numberOfEvents == numberThreads - 1);
        Assert.assertTrue(isNumberOfPublishedEventsAcceptable,
                "It was expected that the number of events are equals with the number of subscriptions-1, that is with the number of threads-1, since a subscription was unsubscribed");
    }

    /**
     * D) notify and publishEvent concurrently. Depends on B)
     */
    /** TODO: REVIEW CONCURRENCY: bb/07.06.2012 no concurrency problems should appear **/
    @Test(invocationCount = 1, dependsOnMethods = "publishEvent_concurrentWithUnsubscribe")
    public void notifySubscribers_concurrentWithPublishEvent() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- notifySubscribers_concurrentWithPublishEvent started");
        clearRetries();

        NotifyStatistics notifyStatistics = coreServiceImpl.notifySubscribers();
        int numOfSentEmails = notifyStatistics.getTotalCounter();

        System.out.println("notifySubscribers - numOfSentEmails: " + numOfSentEmails);
        boolean isRangeOfSentEmailsAcceptable = (numOfSentEmails == 8 || numOfSentEmails == 9);
        Assert.assertTrue(isRangeOfSentEmailsAcceptable,
                "notifySubscribers should send 9 emails if it runs alone, or 8 emails if it runs concurrently with unsubscribe_concurrentWithNotifySubscribers");

        // assertRetries("StaleObjectStateException");
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- notifySubscribers_concurrentWithPublishEvent finished");
        doSubscribersUpdate = true; // force subscribers update
    }

    /**
     * This creates new data (Events) for the next test. Depends on D)
     */
    @Test(invocationCount = 1, dependsOnMethods = "notifySubscribers_concurrentWithPublishEvent")
    public void publishNewEvent() {
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishNewEvent started");
        int numberOfEvents = publishEventForDefaultPublisher();
        System.out.println("Thread " + Thread.currentThread().getId() + "  --- publishNewEvent finished");
    }

}
