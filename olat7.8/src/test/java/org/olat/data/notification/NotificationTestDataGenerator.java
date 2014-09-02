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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.basesecurity.TestIdentityFactory;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.user.TestUserFactory;
import org.olat.data.user.UserImpl;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 11.04.2012 <br>
 * 
 * @author Branislav Balaz
 */
// TODO: REVIEW PERFORMANCE TESTS FOR PUBLISH bb/12.04.2012
@Component
public class NotificationTestDataGenerator {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private SubscriberDao subscriberDao;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private SubscriptionDao subscriptionDao;

    @Autowired
    private GenericDao<IdentityImpl> daoIdentity;
    @Autowired
    private GenericDao<UserImpl> daoUser;

    private List<UserImpl> users = new ArrayList<UserImpl>();
    private List<IdentityImpl> identities = new ArrayList<IdentityImpl>();

    private Random random = new Random();

    @PostConstruct
    void initType() {
        daoIdentity.setType(IdentityImpl.class);
        daoUser.setType(UserImpl.class);
    }

    public List<Subscriber> generateSubscribers(int numberOfSubscribers) {
        return generateSubscribersForIdentities(generateIdentities(numberOfSubscribers));
    }

    public List<Publisher> generatePublishers(int numberOfPublishers) {
        List<Publisher> publishers = new ArrayList<Publisher>();
        long contextId = 0;
        long sourceId = 0;
        long subcontextId = 0;
        for (int i = 0; i < numberOfPublishers; i++) {
            publishers.add(publisherDao.createAndSavePublisher(++contextId, Publisher.ContextType.COURSE, ++sourceId, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE,
                    ++subcontextId));
        }
        return publishers;
    }

    public List<Subscription> generateSubscriptionsForListSubscribersAndOnePublisher(List<Subscriber> subscribers, Publisher publisher) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        for (Subscriber subscriber : subscribers) {
            subscriptions.add(subscriptionDao.createOrReuseSubscription(subscriber, publisher));
        }
        return subscriptions;
    }

    public List<Subscription> generateRandomSubscriptions(List<Subscriber> subscribers, List<Publisher> publishers, int numberOfSubscriptionsForOneSubscriber) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        for (Subscriber subscriber : subscribers) {
            List<Publisher> publishersList = new ArrayList<Publisher>(publishers);
            for (int i = 0; i < numberOfSubscriptionsForOneSubscriber; i++) {
                int publisherIndex = (int) Math.floor(Math.random() * publishersList.size());
                System.out.println("publisherIndex: " + publisherIndex);
                subscriptions.add(subscriptionDao.createOrReuseSubscription(subscriber, publishersList.get(publisherIndex)));
                publishersList.remove(publisherIndex);
            }
        }
        log.info("generateRandomSubscriptions - subscriptions.size(): " + subscriptions.size());
        return subscriptions;
    }

    public List<Identity> generateIdentities(int numberOfIdentities) {
        List<Identity> identities = new ArrayList<Identity>();
        for (int i = 0; i < numberOfIdentities; i++) {
            identities.add(createAndSaveIdentity_(getUniqueIdentifier()));
        }
        return identities;
    }

    private List<Subscriber> generateSubscribersForIdentities(List<Identity> identities) {
        List<Subscriber> subscribers = new ArrayList<Subscriber>();
        for (Identity identity : identities) {
            subscribers.add(subscriberDao.createAndSaveSubscriber(identity));
        }
        return subscribers;
    }

    public String getUniqueIdentifier() {
        return "" + random.nextLong();
    }

    private IdentityImpl createAndSaveIdentity_(String username) {
        // TODO: why not reuse the following?
        // return (IdentityImpl) JunitTestHelper.createAndPersistIdentityAsUser(username);

        UserImpl user = TestUserFactory.createTestUserForJunit(username + "_FIRST", username + "_LAST", username + "@TEST.tst");
        daoUser.save(user);
        users.add(user);

        IdentityImpl identity = TestIdentityFactory.createTestIdentityForJunit(username, user);
        identity = daoIdentity.save(identity);
        identities.add(identity);
        return identity;
    }

    public void cleanupNotificationTestData() {
        deleteAllPublishers();
        deleteAllSubscribers();
        // deleteAllUsers();
        // deleteAllIdentities();
    }

    public void deleteAllPublishers() {
        log.info("deleteAllPublishers started");
        List<Publisher> publishers = publisherDao.findAll();
        for (Publisher publisher : publishers) {
            publisherDao.deletePublisher(publisher);
        }
        log.info("deleteAllPublishers finished");
    }

    public void deleteAllSubscribers() {
        log.info("deleteAllSubscribers started");
        List<Subscriber> subscribers = subscriberDao.findAll();
        for (Subscriber subscriber : subscribers) {
            subscriberDao.deleteSubscriber(subscriber);
        }
        log.info("deleteAllSubscribers finished");
    }

    private void deleteAllIdentities() {
        log.info("deleteAllIdentities started");
        for (IdentityImpl identity : identities) {
            identity = daoIdentity.findById(identity.getKey());
            daoIdentity.delete(identity);
        }
        log.info("deleteAllIdentities finished");
    }

    private void deleteAllUsers() {
        log.info("deleteAllUsers started");
        for (UserImpl user : users) {
            user = daoUser.findById(user.getKey());
            daoUser.delete(user);
        }
        log.info("deleteAllUsers finished");
    }

}
