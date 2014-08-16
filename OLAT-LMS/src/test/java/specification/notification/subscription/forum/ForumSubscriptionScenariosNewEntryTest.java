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
package specification.notification.subscription.forum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concordion.api.ResultSummary;
import org.concordion.internal.ConcordionBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.impl.NotificationServiceImpl;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: Nov 24, 2011 <br>
 * 
 * @author patrick
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/data/notification/_spring/notificationContextTest.xml",
        "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml", "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class ForumSubscriptionScenariosNewEntryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private Map<String, SubscriberEntry> subscribers = new HashMap<String, SubscriberEntry>();
    private List<ActionEntry> actions = new ArrayList<ActionEntry>();
    private Map<String, PublisherEntry> publishers = new HashMap<String, PublisherEntry>();
    private List<SubscriptionEntry> subscriptions = new ArrayList<SubscriptionEntry>();

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private SubscriberDao subscriberDao;
    @Autowired
    private SubscriptionDao subscriptionDao;
    @Autowired
    GenericDao<IdentityImpl> identityDao;

    @Test
    @Ignore
    public void run() throws Exception {
        ResultSummary resultSummary = new ConcordionBuilder().build().process(this);
        resultSummary.print(System.out, this);
        resultSummary.assertIsSatisfied(this);
    }

    public void setSubscriber(String id, String name, String isSubscription, String notificationInterval) {
        IdentityImpl identity = identityDao.create();
        identity.setName(name);
        identity = identityDao.save(identity);
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(identity);
        subscribers.put(id, new SubscriberEntry(isSubscription, subscriber));
    }

    public void setAction(String subscriberId, String publisherId, String eventType, String title, String messageId) {
        actions.add(new ActionEntry(subscriberId, publisherId, title, eventType, messageId));
    }

    public void setPublisher(String id, String contextType, String contextId, String contextTitle, String sourceType, String sourceId, String sourceTitle,
            String subcontextId) {
        Publisher publisher = publisherDao.createAndSavePublisher(Long.valueOf(contextId), getContextType(contextType), Long.valueOf(sourceId), sourceType,
                Long.valueOf(subcontextId));
        publishers.put(id, new PublisherEntry(publisher, contextTitle, sourceTitle));
    }

    public void setSubscription(String subscriberId, String publisherId) {
        if (convertStringToBoolean(subscribers.get(subscriberId).isSubscription)) {
            Subscription subscription = subscriptionDao.createOrReuseSubscription(subscribers.get(subscriberId).subscriber, publishers.get(publisherId).publisher);
            subscriptions.add(new SubscriptionEntry(subscriberId, publisherId, subscription));
        }
    }

    private static class SubscriberEntry {

        public final String isSubscription;
        public final Subscriber subscriber;

        public SubscriberEntry(String isSubscription, Subscriber subscriber) {
            this.isSubscription = isSubscription;
            this.subscriber = subscriber;
        }

    }

    private static class ActionEntry {

        public final String subscriberId;
        public final String publisherId;
        public final String title;
        public final String eventType;
        public final String messageId;

        public ActionEntry(String subscriberId, String publisherId, String title, String eventType, String messageId) {
            this.subscriberId = subscriberId;
            this.publisherId = publisherId;
            this.title = title;
            this.eventType = eventType;
            this.messageId = messageId;
        }
    }

    private static class PublisherEntry {

        public final Publisher publisher;
        public final String contextTitle;
        public final String sourceTitle;

        public PublisherEntry(Publisher publisher, String contextTitle, String sourceTitle) {
            this.publisher = publisher;
            this.contextTitle = contextTitle;
            this.sourceTitle = sourceTitle;
        }

    }

    private static class SubscriptionEntry {

        public final String subscriberId;
        public final String publisherId;
        public final Subscription subscription;

        public SubscriptionEntry(String subscriberId, String publisherId, Subscription subscription) {
            this.subscriberId = subscriberId;
            this.publisherId = publisherId;
            this.subscription = subscription;
        }

    }

    public void executeTest() {
        publishEvents();
        notificationServiceImpl.notifySubscribers();
    }

    private void publishEvents() {
        for (ActionEntry action : actions) {

            PublishEventTO publishEventTO = PublishEventTO.getValidInstance(publishers.get(action.publisherId).publisher.getContextType(),
                    publishers.get(action.publisherId).publisher.getContextId(), publishers.get(action.publisherId).contextTitle,
                    publishers.get(action.publisherId).publisher.getSubcontextId(), publishers.get(action.publisherId).publisher.getSourceType(),
                    publishers.get(action.publisherId).publisher.getSourceId(), publishers.get(action.publisherId).sourceTitle, action.title,
                    subscribers.get(action.subscriberId).subscriber.getIdentity(), getEventType(action.eventType));
            publishEventTO.setSourceEntryId(action.messageId);
            notificationServiceImpl.publishEvent(publishEventTO);
        }
    }

    private Publisher.ContextType getContextType(String type) {

        Publisher.ContextType contextType;

        if ("COURSE".equals(type)) {
            contextType = Publisher.ContextType.COURSE;
        } else {
            throw new AssertionError("unknown context type");
        }

        return contextType;
    }

    private boolean convertStringToBoolean(String stringValue) {

        boolean value;

        if ("true".equalsIgnoreCase(stringValue)) {
            value = true;
        } else if ("false".equalsIgnoreCase(stringValue)) {
            value = false;
        } else {
            throw new AssertionError("unknown boolean string value");
        }

        return value;
    }

    private PublishEventTO.EventType getEventType(String type) {

        PublishEventTO.EventType eventType;

        if ("NEW".equals(type)) {
            eventType = PublishEventTO.EventType.NEW;
        } else if ("CHANGED".equals(type)) {
            eventType = PublishEventTO.EventType.CHANGED;
        } else if ("DELETED".equals(type)) {
            eventType = PublishEventTO.EventType.DELETED;
        } else {
            throw new AssertionError("unknown event type");
        }

        return eventType;
    }
}
