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

import java.util.Date;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Subscriber.SubscriptionOption;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Initial Date: 15.02.2012 <br>
 * 
 * This class simulates StaleObjectStateException by concurrent updates (different Hibernate sessions and transactions) for Subscription entity
 * 
 * @author Branislav Balaz
 */
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class MultithreadedDaoTestNgITCaseNew extends AbstractTestNGSpringContextTests {

    @Autowired
    GenericDao<IdentityImpl> daoIdentity;

    @Autowired
    GenericDao<Subscriber> daoSubscriber;

    @Autowired
    GenericDao<Publisher> daoPublisher;

    @Autowired
    GenericDao<Subscription> daoSubscription;

    @Autowired
    PlatformTransactionManager txManager;

    private final String IDENTITY_NAME = "testIdentity";
    private final Long CONTEXT_ID = Long.valueOf(1);
    private final Long SOURCE_ID = Long.valueOf(2);
    private final Long SUBCONTEXT_ID = Long.valueOf(3);

    @BeforeClass
    public void setup() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                daoIdentity.setType(IdentityImpl.class);
                daoSubscriber.setType(Subscriber.class);
                daoPublisher.setType(Publisher.class);
                daoSubscription.setType(Subscription.class);

                createSubscription(createPublisher(), createSubscriber(createIdentity(IDENTITY_NAME)));

            }
        });

    }

    @AfterClass
    public void cleanUp() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                daoSubscription.delete(daoSubscription.findAll().get(0));
                daoPublisher.delete(daoPublisher.findAll().get(0));
                daoSubscriber.delete(daoSubscriber.findAll().get(0));
                daoIdentity.delete(daoIdentity.findAll().get(0));
            }
        });

    }

    @Test(threadPoolSize = 2, invocationCount = 2)
    public void testSubscriptionUpdate() {

        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                Subscription subscription = daoSubscription.findAll().get(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Date date = new Date();
                System.out.println("Thread " + Thread.currentThread().getId() + " date:" + date.toString());
                subscription.setLastNotifiedDate(date);
                daoSubscription.update(subscription);

            }
        });

    }

    private Publisher createPublisher() {
        Publisher publisher = daoPublisher.create();
        publisher.setContextId(CONTEXT_ID);
        publisher.setContextType(Publisher.ContextType.COURSE);
        publisher.setSourceId(SOURCE_ID);
        publisher.setSourceType(ForumNotificationTypeHandler.FORUM_SOURCE_TYPE);
        publisher.setSubcontextId(SUBCONTEXT_ID);
        daoPublisher.save(publisher);
        return publisher;
    }

    private IdentityImpl createIdentity(String username) {
        IdentityImpl identity = daoIdentity.create();
        identity.setName(username);
        identity = daoIdentity.save(identity);
        return identity;
    }

    private Subscriber createSubscriber(Identity identity) {
        Subscriber subscriber = daoSubscriber.create();
        subscriber.setIdentity(identity);
        subscriber.setInterval(Subscriber.NotificationInterval.DAILY);
        subscriber.setOption(SubscriptionOption.ALL);
        daoSubscriber.save(subscriber);
        return subscriber;
    }

    private Subscription createSubscription(Publisher publisher, Subscriber subscriber) {

        Subscription subscription = daoSubscription.create();
        subscription.setPublisher(publisher);
        subscription.setSubscriber(subscriber);
        subscription.setCreationDate(new Date());
        daoSubscription.save(subscription);
        return subscription;
    }
}
