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
package org.olat.lms.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;

/**
 * Initial Date: 24.05.2011 <br>
 * 
 * @author lavinia
 */
public class NotificationServiceImplTest {

    private NotificationServiceImpl notificationService;

    private Locale LOCALE_EN = Locale.ENGLISH;
    private Date LATEST_EMAILED_DATE;;
    private ObjectMother notificationWorld;

    @Before
    public void setUp() throws Exception {
        notificationWorld = (new ObjectMother()).createNotificationsWorld();
        LATEST_EMAILED_DATE = notificationWorld.getLatestEmailDate();
        notificationService = notificationWorld.getNotificationService();
    }

    @Test
    public void matches_AnyValidPublisher() {
        // Setup
        Publisher anyPublisher = notificationWorld.getAnyPublisher();
        SubscriptionContext subscriptionContext = notificationWorld.getSubscriptionContext();
        // Exercise
        boolean result = notificationService.matches(anyPublisher, subscriptionContext);
        // Verify
        assertFalse(result);
    }

    @Test
    public void matches_NullPublisher() {
        // Setup
        SubscriptionContext subscriptionContext = notificationWorld.getSubscriptionContext();
        // Exercise
        boolean result = notificationService.matches(null, subscriptionContext);
        // verify
        assertFalse(result);
    }

    @Test(expected = RuntimeException.class)
    public void matches_NullSubscriptionContext() {
        // Setup
        Publisher thePublisher = notificationWorld.getThePublisher();
        // Exercise
        notificationService.matches(thePublisher, null);
        // Verify
    }

    @Test
    public void matches_ThisContextPublisher() {
        // Setup
        Publisher thePublisher = notificationWorld.getThePublisher();
        SubscriptionContext subscriptionContext = notificationWorld.getSubscriptionContext();
        // Exercise
        boolean result = notificationService.matches(thePublisher, subscriptionContext);
        // Verify
        assertTrue(result);
    }

    @Test
    public void getNotificationsHandler_ForTheRightPublisher() {
        // Setup
        Publisher thePublisher = notificationWorld.getThePublisher();
        // Exercise
        NotificationsHandler notificationsHandler = notificationService.getNotificationsHandler(thePublisher);
        // Verify
        assertNotNull(notificationsHandler);
    }

    @Test(expected = RuntimeException.class)
    public void getNotificationsHandler_ForNullPublisher() {
        // Setup
        // Exercise
        notificationService.getNotificationsHandler(null);
        // Verify
    }

    @Test
    public void createSubscriptionItem_ForTheRightSubscriber_WithNews() {
        // Setup
        Subscriber theSubscriber = notificationWorld.getTheSubscriber();
        SubscriptionInfo subscriptionInfo = notificationWorld.getSubscriptionInfo();
        NotificationServiceImpl notificationServiceSpy = spy(notificationService);
        doReturn("TITLE").when(notificationServiceSpy).getFormatedTitle(subscriptionInfo, theSubscriber, LOCALE_EN, "");
        // Exercise
        SubscriptionItem subscriptionItem = notificationServiceSpy.createSubscriptionItem(theSubscriber, LOCALE_EN, "", "", LATEST_EMAILED_DATE);
        // Verify
        assertNotNull(subscriptionItem);
    }

    @Test
    public void createSubscriptionItem_ForNullSubcriber() {
        // Setup
        // Exercise
        // triggers an ERROR in the Log
        SubscriptionItem subscriptionItem = notificationService.createSubscriptionItem(null, LOCALE_EN, "", "");
        // Verify
        assertNull(subscriptionItem);
    }

    @Test
    public void getNoSubscriptionInfo() {
        // Setup
        // Exercise
        SubscriptionInfo noSubscriptionInfo = notificationService.getNoSubscriptionInfo();
        // Verify
        assertNotNull(noSubscriptionInfo);
    }

    @Test
    public void getSubscriptionInfos_ForNullIdentityAndPublisherType() {
        // Setup
        // Exercise
        List<SubscriptionInfo> subscriptionInfoList = notificationService.getSubscriptionInfos(null, null);
        // Verify
        assertTrue(subscriptionInfoList.isEmpty());
    }

    @Test
    public void getSubscriptionInfos_ForIdentityAndPublisherType() {
        // Setup
        NotificationServiceImpl notificationServiceSpy = spy(notificationService);
        doReturn(LATEST_EMAILED_DATE).when(notificationServiceSpy).getDefaultCompareDate();
        Identity theIdentity = notificationWorld.getTheIdendity();

        // Exercise
        List<SubscriptionInfo> subscriptionInfoList = notificationServiceSpy.getSubscriptionInfos(theIdentity, ObjectMother.PUBLISHER_TYPE);
        // Verify
        boolean isEmpty = subscriptionInfoList.isEmpty();
        assertFalse(isEmpty);
    }

    @Test
    public void getSubscriptionInfos_ForIdentityAndWrongPublisherType() {
        // Setup
        NotificationServiceImpl notificationServiceSpy = spy(notificationService);
        doReturn(LATEST_EMAILED_DATE).when(notificationServiceSpy).getDefaultCompareDate();
        Identity theIdentity = notificationWorld.getTheIdendity();
        // Exercise
        List<SubscriptionInfo> subscriptionInfoList = notificationServiceSpy.getSubscriptionInfos(theIdentity, "NOT_KNOWN_TYPE");
        // Verify
        assertTrue(subscriptionInfoList.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void setNotificationIntervals_NullIntervals() {
        // Setup
        // Exercise
        notificationService.setNotificationIntervals(null);
        // Verify
    }

    @Test
    public void setNotificationIntervals_IntervalsOK() {
        // Setup
        final Map<String, Boolean> intervals = new HashMap<String, Boolean>();
        intervals.put("daily", true);
        notificationService.setNotificationIntervals(intervals);
        // Exercices
        List<String> list = notificationService.getEnabledNotificationIntervals();
        // Verify
        assertTrue(list.size() == 1);
        assertEquals(list.get(0), "daily");
    }

    @Test
    public void setNotificationIntervals_IntervalsTooLong() {
        // Setup
        final Map<String, Boolean> intervals = new HashMap<String, Boolean>();
        intervals.put("some_impossible_long_key", true);
        notificationService.setNotificationIntervals(intervals);
        // Exercise
        List<String> list = notificationService.getEnabledNotificationIntervals();
        // Verify
        assertTrue(list.size() == 0);
    }

    @Test
    public void setNotificationIntervals_IntervalsOKButFalse() {
        // Setup
        final Map<String, Boolean> intervals = new HashMap<String, Boolean>();
        intervals.put("daily", false);
        notificationService.setNotificationIntervals(intervals);
        // Exercise
        List<String> list = notificationService.getEnabledNotificationIntervals();
        // Verify
        assertTrue(list.size() == 0);
    }

}
