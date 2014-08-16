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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.notification.NotificationEvent;
import org.olat.data.notification.NotificationEventDao;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.lms.core.notification.impl.channel.ChannelChain;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;

/**
 * Initial Date: 21.03.2012 <br>
 * 
 * @author aabouc
 */
public class NotifyDelegateTest {

    private NotifyDelegate notifyDelegateTestObject;

    private Subscriber subscriberMock;
    private Long subscriberId = new Long(1);
    // private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    private List<Long> subscriberIDs = new ArrayList<Long>();

    private NotificationEvent eventMock;
    private List<NotificationEvent> events = new ArrayList<NotificationEvent>();

    private List<NotificationEventTO> eventTOs = new ArrayList<NotificationEventTO>();

    private NotifyStatistics statisticsMock;

    @Before
    public void setup() {
        notifyDelegateTestObject = new NotifyDelegate();

        // Test Subscribers
        subscriberMock = new Subscriber();
        subscriberMock.setIdentity(ObjectMother.createIdentity("testUser"));
        subscriberMock.addChannel(Subscriber.Channel.EMAIL);
        // subscribers.add(subscriberMock);
        subscriberIDs.add(subscriberId);

        // Test Subscription
        Subscription subscriptionMock = new Subscription();
        subscriptionMock.setPublisher(new Publisher());
        subscriptionMock.setSubscriber(subscriberMock);

        // Test Event
        eventMock = mock(NotificationEvent.class);
        eventMock.setSubscription(subscriptionMock);
        when(eventMock.getSubscription()).thenReturn(subscriptionMock);
        events.add(eventMock);

        // Test NotificationEventTO
        NotificationEventTO eventTOMock = mock(NotificationEventTO.class);
        when(eventTOMock.getEvent()).thenReturn(eventMock);
        // when(eventTOMock.getEvent().getSubscription()).thenReturn(subscriptionMock);
        eventTOs.add(eventTOMock);

        // Test for NotifyStatistics
        statisticsMock = new NotifyStatistics();

        // Mock for SubscriberDao
        SubscriberDao subscriberDaoMock = mock(SubscriberDao.class);
        notifyDelegateTestObject.subscriberDao = subscriberDaoMock;
        when(subscriberDaoMock.getSubscriberIDsByEventStatus(NotificationEvent.Status.WAITING)).thenReturn(subscriberIDs);

        // Mock for NotificationEventDao
        NotificationEventDao notificationEventDaoMock = mock(NotificationEventDao.class);
        notifyDelegateTestObject.eventDao = notificationEventDaoMock;
        when(notificationEventDaoMock.findNotificationEventsBySubscriber(subscriberId)).thenReturn(events);

        // Mock for Converter
        NotificationConverter converterMock = mock(NotificationConverter.class);
        notifyDelegateTestObject.converter = converterMock;
        when(converterMock.toEventTOList(events)).thenReturn(eventTOs);

        // Mock for ChannelChain
        ChannelChain channelChainMock = mock(ChannelChain.class);
        notifyDelegateTestObject.channelChain = channelChainMock;
    }

    @Test
    public void notifySubscriber_nullSubscriber() {
        NotifyStatistics statistics = notifyDelegateTestObject.notifySubscriber(null);
        assertNotNull(statistics);
    }

    @Test
    public void notifySubscriber_noSubscriberChannel() {
        subscriberMock.getChannels().remove(Subscriber.Channel.EMAIL);
        NotifyStatistics statistics = notifyDelegateTestObject.notifySubscriber(subscriberId);
        assertNull(statistics);
    }

    @Test
    public void notifySubscriber_noEvents() {
        events.remove(eventMock);
        NotifyStatistics statistics = notifyDelegateTestObject.notifySubscriber(subscriberId);
        assertNotNull(statistics);
    }

    @Ignore
    @Test
    public void notifySubscriber_delivered() {
        statisticsMock.getChannel2StatusMap().put(Subscriber.Channel.EMAIL, Boolean.TRUE);
        when(notifyDelegateTestObject.channelChain.send(subscriberMock, eventTOs)).thenReturn(statisticsMock);
        NotifyStatistics statistics = notifyDelegateTestObject.notifySubscriber(subscriberId);
        assertTrue("Wrong NotifyStatistic", statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
    }

    @Ignore
    @Test
    public void notifySubscriber_failed() {
        statisticsMock.getChannel2StatusMap().put(Subscriber.Channel.EMAIL, Boolean.FALSE);
        when(notifyDelegateTestObject.channelChain.send(subscriberMock, eventTOs)).thenReturn(statisticsMock);
        NotifyStatistics statistics = notifyDelegateTestObject.notifySubscriber(subscriberId);
        assertFalse("Wrong NotifyStatistic", statistics.getChannel2StatusMap().get(Subscriber.Channel.EMAIL));
    }

}
