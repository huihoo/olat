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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.PublisherDao;
import org.olat.data.notification.Subscriber;
import org.olat.data.notification.SubscriberDao;
import org.olat.data.notification.Subscription;
import org.olat.lms.core.notification.impl.metric.AverageEmailSuccessRateMetric;
import org.olat.lms.core.notification.impl.metric.NotificationServiceContext;
import org.olat.lms.core.notification.impl.metric.NotificationServiceMetric;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.course.ICourse;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.springframework.mail.MailSendException;

/**
 * Initial Date: 28.11.2011 <br>
 * 
 * @author guretzki
 */
public class NotificationServiceImplTest {

    private NotificationServiceImpl notificationServiceImpl;

    private PublisherDao publisherDaoMock;
    private SubscriberDao subscriberDaoMock;

    private Identity identity;
    private Long subscriberId = new Long(1);
    private ICourse courseMock;
    private ICourse secondCourseMock;

    @Before
    public void setUp() throws Exception {
        notificationServiceImpl = new NotificationServiceImpl();

        publisherDaoMock = mock(PublisherDao.class);
        subscriberDaoMock = mock(SubscriberDao.class);
        SubscriptionDelegate subcriptionDelegateMock = mock(SubscriptionDelegate.class);
        PublishDelegate publishDelegateMock = mock(PublishDelegate.class);
        NotifyDelegate notifyDelegateMock = mock(NotifyDelegate.class);

        notificationServiceImpl.publisherDao = publisherDaoMock;
        notificationServiceImpl.subscriberDao = subscriberDaoMock;
        notificationServiceImpl.subscriptionDelegate = subcriptionDelegateMock;
        notificationServiceImpl.publishDelegate = publishDelegateMock;
        notificationServiceImpl.notifyDelegate = notifyDelegateMock;

        Subscriber subscriber_1 = mock(Subscriber.class);
        when(subscriber_1.getId()).thenReturn(subscriberId);

        Iterator<Subscriber> mockIter = mock(Iterator.class);
        when(mockIter.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockIter.next()).thenReturn(subscriber_1);
        when(notificationServiceImpl.notifyDelegate.getSubscribersIterator()).thenReturn(mockIter);

        identity = mock(Identity.class);

        Long courseId = new Long(1);
        courseMock = mock(ICourse.class);
        when(courseMock.getResourceableId()).thenReturn(courseId);

        Long secondCourseId = new Long(2);
        secondCourseMock = mock(ICourse.class);
        when(secondCourseMock.getResourceableId()).thenReturn(secondCourseId);
    }

    @Test
    public void testSubscribe() {
        Long courseId = courseMock.getResourceableId();
        notificationServiceImpl.subscribe(new NotificationSubscriptionContext(identity, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, new Long(123),
                Publisher.ContextType.COURSE, courseId, null));
    }

    @Test
    public void testGetSubscriptions_emptyList() {
        List<Subscription> subscriptions = notificationServiceImpl.getSubscriptions(identity);
        assertNotNull("Should not return null", subscriptions);
        assertTrue(subscriptions.isEmpty());
    }

    @Test
    public void testNotifySubscribers_emptyNotifyStatistics() {
        when(notificationServiceImpl.notifyDelegate.notifySubscriber(subscriberId)).thenReturn(null);
        NotifyStatistics statistics = notificationServiceImpl.notifySubscribers();
        assertEquals("Should not return null", statistics.getTotalCounter(), 0);
    }

    @Test
    public void testNotifySubscribers_notEmptyNotifyStatistics() {
        // Statistic for Subscriber 1
        NotifyStatistics statistic1 = new NotifyStatistics();
        statistic1.addChannelResponse(Subscriber.Channel.EMAIL, true);
        // Statistic for Subscriber 2
        NotifyStatistics statistic2 = new NotifyStatistics();
        statistic2.addChannelResponse(Subscriber.Channel.EMAIL, false);

        when(notificationServiceImpl.notifyDelegate.notifySubscriber(subscriberId)).thenReturn(statistic1).thenReturn(statistic2);
        NotifyStatistics statistics = notificationServiceImpl.notifySubscribers();
        assertEquals("Wrong number of delivered counter", 1, statistics.getDeliveredCounter());
        assertEquals("Wrong number of failed counter", 1, statistics.getFailedCounter());
        assertEquals("Wrong number of failed counter", 2, statistics.getTotalCounter());
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void notifySubscribersFailed() {
        boolean finished = true;
        List<NotificationServiceMetric<NotificationServiceContext>> metrics = new ArrayList<NotificationServiceMetric<NotificationServiceContext>>();
        NotificationServiceMetric<? extends NotificationServiceContext> notifySuccessMetric = new AverageEmailSuccessRateMetric();
        metrics.add((NotificationServiceMetric<NotificationServiceContext>) notifySuccessMetric);
        notificationServiceImpl.setMetrics(metrics);
        when(notificationServiceImpl.notifyDelegate.notifySubscriber(any(Long.class))).thenThrow(new MailSendException("mock exception"));
        try {
            notificationServiceImpl.startNotificationJob();
            notificationServiceImpl.notifySubscriber(Long.valueOf(1));
            notificationServiceImpl.finishNotificationJob();
        } catch (MailSendException e) {

            for (NotificationServiceMetric<?> metric : metrics) {
                if (metric instanceof AverageEmailSuccessRateMetric) {
                    finished = ((AverageEmailSuccessRateMetric) metric).isJobFinishedOrNotStartedYet();
                }
            }
        }
        assertTrue(finished == false);
    }
}
