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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concordion.api.Resource;
import org.concordion.api.ResultSummary;
import org.concordion.internal.ConcordionBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.dao.GenericDao;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.SubscriptionDao;
import org.olat.lms.core.notification.impl.channel.MailChannel;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ibm.icu.text.SimpleDateFormat;

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
    private Map<String, ActionEntry> actions = new HashMap<String, ActionEntry>();
    private Map<String, PublisherEntry> publishers = new HashMap<String, PublisherEntry>();

    @Autowired
    private NotificationServiceImpl notificationServiceImpl;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private SubscriberDao subscriberDao;
    @Autowired
    private SubscriptionDao subscriptionDao;
    @Autowired
    private DaoObjectMother daoObjectMother;
    @Autowired
    private NotificationEventDao notificationEventDao;
    @Autowired
    private GenericDao<NotificationEvent> genericNotificationEventDao;
    @Autowired
    private MailServiceConcordionMock mailServiceConcordionMock;
    @Autowired
    private UriBuilder uriBuilder;

    @Autowired
    MailChannel mailChannel;

    private Map<String, MailServiceConcordionMock.UserNotifyStatistic> userStatisticMap;

    @Before
    public void setup() {
        uriBuilder.serverContextPathURI = "/test";
        mailChannel.setMailService(mailServiceConcordionMock);
        notificationServiceImpl.notifyDelegate.channelChain.setMailChannel(mailChannel);
    }

    @Ignore
    @Test
    public void run() throws Exception {
        ResultSummary resultSummary = new ConcordionBuilder().build().process(
                new Resource("/specification/notification/subscription/forum/ForumSubscriptionScenariosNewEntry.html"), this);
        resultSummary.print(System.out, this);
        resultSummary.assertIsSatisfied(this);
    }

    public void setSubscriber(String id, String name, String isSubscription, String notificationInterval) {
        Identity identity = daoObjectMother.createAndSaveIdentity(name);
        Subscriber subscriber = subscriberDao.createAndSaveSubscriber(identity);
        subscriber.setInterval(getNotificationInterval(notificationInterval));
        subscribers.put(id, new SubscriberEntry(isSubscription, subscriber));
    }

    public void setAction(String subscriberId, String publisherId, String eventType, String title, String messageId, String eventDate) {
        actions.put(messageId, new ActionEntry(subscriberId, publisherId, title, eventType, messageId, eventDate));
    }

    public void setPublisher(String id, String contextType, String contextId, String contextTitle, String sourceType, String sourceId, String sourceTitle,
            String subcontextId) {
        Publisher publisher = publisherDao.createAndSavePublisher(Long.valueOf(contextId), getContextType(contextType), Long.valueOf(sourceId),
                getSourceType(sourceType), Long.valueOf(subcontextId));
        publishers.put(id, new PublisherEntry(publisher, contextTitle, sourceTitle));
    }

    public void setSubscription(String subscriberId, String publisherId) {
        if (convertStringToBoolean(subscribers.get(subscriberId).isSubscription)) {
            subscriptionDao.createOrReuseSubscription(subscribers.get(subscriberId).subscriber, publishers.get(publisherId).publisher);
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
        public final String eventDate;

        public ActionEntry(String subscriberId, String publisherId, String title, String eventType, String messageId, String eventDate) {
            this.subscriberId = subscriberId;
            this.publisherId = publisherId;
            this.title = title;
            this.eventType = eventType;
            this.messageId = messageId;
            this.eventDate = eventDate;
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

    public void executeTest() {
        publishEvents();
        updateNotificationEventDate(actions);
        notificationServiceImpl.notifySubscribers();
        userStatisticMap = mailServiceConcordionMock.getUserStatisticMap();
        String body = ((MailServiceConcordionMock.UserNotifyStatistic) userStatisticMap.get("Alf")).mailBody;
    }

    public String getMailResult(String userName) {

        if (isNotMailSuccess(userName)) {
            return "erhält keine E-Mail";
        } else {
            return "erhält E-Mail";
        }

    }

    public String getMailFromAddress(String userName) {

        if (isNotMailSuccess(userName)) {
            return "";
        } else {
            return userStatisticMap.get(userName).mailFromAddress;
        }

    }

    public String getMailToAddress(String userName) {

        if (isNotMailSuccess(userName)) {
            return "";
        } else {
            return userStatisticMap.get(userName).mailToAddress;
        }

    }

    public String getMailSubject(String userName) {

        if (isNotMailSuccess(userName)) {
            return "";
        } else {
            return userStatisticMap.get(userName).mailSubject;
        }

    }

    public String getMailBody(String userName) {

        if (isNotMailSuccess(userName)) {
            return "";
        } else {
            return userStatisticMap.get(userName).mailBody;
        }

    }

    private boolean isNotMailSuccess(String userName) {
        return !userStatisticMap.containsKey(userName) || !userStatisticMap.get(userName).isMailSuccess;
    }

    private void publishEvents() {
        for (ActionEntry action : actions.values()) {

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

    private String getSourceType(String type) {

        String sourceType;

        if ("FORUM".equals(type)) {
            sourceType = ForumNotificationTypeHandler.FORUM_SOURCE_TYPE;
        } else {
            throw new AssertionError("unknown source type");
        }

        return sourceType;
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

    private Subscriber.NotificationInterval getNotificationInterval(String interval) {

        Subscriber.NotificationInterval notificationInterval;

        if ("DAILY".equals(interval)) {
            notificationInterval = Subscriber.NotificationInterval.DAILY;
        } else if ("IMMEDIATELY".equals(interval)) {
            notificationInterval = Subscriber.NotificationInterval.IMMEDIATELY;
        } else if ("NEVER".equals(interval)) {
            notificationInterval = Subscriber.NotificationInterval.NEVER;
        } else {
            throw new AssertionError("unknown notification interval");
        }

        return notificationInterval;

    }

    private void updateNotificationEventDate(Map<String, ActionEntry> actions) {

        List<NotificationEvent> events = notificationEventDao.findNotificationEvents(NotificationEvent.Status.WAITING);
        for (NotificationEvent event : events) {
            String date = ((ActionEntry) actions.get(event.getAttributes().get(NotificationEvent.Attribute.SOURCE_ENTRY_ID.name()))).eventDate;
            Date eventDate = getEventDate(date);
            event.setCreationDate(eventDate);
            genericNotificationEventDao.update(event);
        }

    }

    private Date getEventDate(String date) {

        SimpleDateFormat format = new SimpleDateFormat("DD.MM.YYYY HH:mm");
        try {
            return format.parse(date);
        } catch (ParseException e) {
            throw new AssertionError("wrong date format");
        }

    }

}
