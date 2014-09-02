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

import static org.olat.data.notification.DaoObjectMother.createForumPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Alternative for NotificationEventDaoITCaseNew, keep either this or NotificationEventDaoITCaseNew, not both. <br/>
 * Doesn't run in Hudson yet. <br/>
 * 
 * Initial Date: 04.05.2012 <br>
 * 
 * @author lavinia
 */
@ContextConfiguration(locations = { "classpath:org/olat/lms/learn/_spring/lmsLearnTestContext.xml",
        "classpath:org/olat/data/notification/_spring/notificationContextTest.xml", "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class NotificationEventDaoNGTest extends AbstractTestNGSpringContextTests {

    @Autowired
    PlatformTransactionManager txManager;

    @Autowired
    private NotificationEventDao eventDao;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    GenericDao<IdentityImpl> identityDao;
    @Autowired
    private SubscriptionDao subscriptionDao;
    @Autowired
    private SubscriberDao subscriberDao;

    private Publisher publisher;
    private Subscription subscription_1;

    private Subscriber subscriber_1;
    Long contextId = new Long(123);
    Long contextId_2 = new Long(223);
    Long subcontextId = new Long(222);
    Long sourceId = new Long(456);

    @BeforeClass
    public void setup() {
        new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                identityDao.setType(IdentityImpl.class);

                publisher = createForumPublisher(contextId, subcontextId, sourceId);
                publisher = publisherDao.savePublisher(publisher);

                subscriber_1 = createTestSubscriber("test1");
                subscription_1 = subscriptionDao.createOrReuseSubscription(subscriber_1, publisher);

                System.out.println("setup finished");

            }
        });

    }

    private Subscriber createTestSubscriber(String username) {
        IdentityImpl identity = identityDao.create();
        identity.setName(username);
        identity = identityDao.save(identity);
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(identity);
        return subscriber;
    }

    @Test(invocationCount = 1)
    public void createAndSaveEvent_threadPoolEecutor() {
        Map<String, String> attributes = new HashMap<String, String>();
        List<NotificationEvent> events = eventDao.createAndSaveNotificationEvents(publisher, attributes);
        String theEventString = attributes.get("event");
        System.out.println("theEventString: " + theEventString);

        Assert.assertNotNull(events);
        Assert.assertFalse(events.isEmpty());
        NotificationEvent event = events.get(0);
        Assert.assertEquals(NotificationEvent.Status.WAITING, event.getStatus());
        if (event.getSubscription().getSubscriber().getIdentity().getName().equals("test1")) {
            Assert.assertEquals(subscription_1, event.getSubscription());
        }
    }

    public void getEvents() {

    }
}
