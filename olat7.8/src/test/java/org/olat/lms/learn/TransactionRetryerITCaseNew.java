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
package org.olat.lms.learn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.data.basesecurity.Identity;
import org.olat.data.notification.DaoObjectMother;
import org.olat.data.notification.Publisher;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.EmailBuilder;
import org.olat.lms.core.notification.impl.NotificationEventTO;
import org.olat.lms.core.notification.impl.NotificationServiceImpl;
import org.olat.lms.core.notification.impl.NotifyDelegate;
import org.olat.lms.core.notification.impl.ObjectMother;
import org.olat.lms.core.notification.impl.channel.MailChannel;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.learn.notification.impl.NotificationLearnServiceImpl;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.mail.service.MailService;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Initial Date: 07.03.2012 <br>
 * 
 * @author lavinia
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/lms/learn/_spring/lmsLearnTestContext.xml",
        "classpath:org/olat/data/notification/_spring/notificationContextTest.xml", "classpath:org/olat/data/notification/_spring/notificationDatabaseContextTest.xml" })
public class TransactionRetryerITCaseNew extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    NotificationLearnServiceImpl learnServiceImpl;

    @Autowired
    NotificationServiceImpl notificationService;

    @Autowired
    MailChannel mailChannel;

    @Autowired
    TransactionRetryer transactionRetryer;

    @Autowired
    DaoObjectMother daoObjectMother;

    private String IDENTITY_NAME_PREFIX = "testuser";
    private static Identity identity_1 = null;
    private String IDENTITY_NAME_1;
    NotificationSubscriptionContext notificationSubscriptionContext1;

    private static Identity identity_2 = null;
    private String IDENTITY_NAME_2;
    NotificationSubscriptionContext notificationSubscriptionContext2;

    private final Long CONTEXT_ID = Long.valueOf(1);
    private final Long SOURCE_ID = Long.valueOf(2);
    private final Long SUBCONTEXT_ID = Long.valueOf(3);

    @Before
    public void setup() {
        IDENTITY_NAME_1 = IDENTITY_NAME_PREFIX + System.currentTimeMillis();
        System.out.println("IDENTITY_NAME_1: " + IDENTITY_NAME_1);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        IDENTITY_NAME_2 = IDENTITY_NAME_PREFIX + System.currentTimeMillis();
        System.out.println("IDENTITY_NAME_2: " + IDENTITY_NAME_2);

        identity_1 = daoObjectMother.createAndSaveIdentity(IDENTITY_NAME_1);
        identity_2 = daoObjectMother.createAndSaveIdentity(IDENTITY_NAME_2);
        notificationSubscriptionContext1 = new NotificationSubscriptionContext(identity_1, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, SOURCE_ID,
                Publisher.ContextType.COURSE, CONTEXT_ID, SUBCONTEXT_ID);
        notificationSubscriptionContext2 = new NotificationSubscriptionContext(identity_2, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, SOURCE_ID,
                Publisher.ContextType.COURSE, CONTEXT_ID, SUBCONTEXT_ID);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribe_withNullNotificationSubscriptionContext() {
        log.info("testSubscribe_withNullNotificationSubscriptionContext");
        System.out.println("TransactionRetryerTest - called");

        learnServiceImpl.subscribe(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void testSubscribe_IfCoreServiceThrowsException() {

        NotificationService mockNotificationService = mock(NotificationService.class);
        when(mockNotificationService.subscribe(notificationSubscriptionContext1)).thenThrow(new ConstraintViolationException(IDENTITY_NAME_1, null, IDENTITY_NAME_1));
        learnServiceImpl.setNotificationService(mockNotificationService);

        learnServiceImpl.subscribe(notificationSubscriptionContext1);
    }

    @Test
    public void testSubscribe_IfCoreServiceThrowsException_checkNumberOfRetries() {

        NotificationService mockNotificationService = mock(NotificationService.class);
        when(mockNotificationService.subscribe(notificationSubscriptionContext1)).thenThrow(new ConstraintViolationException(IDENTITY_NAME_1, null, IDENTITY_NAME_1));
        learnServiceImpl.setNotificationService(mockNotificationService);

        try {
            verify(learnServiceImpl.subscribe(notificationSubscriptionContext1));
        } catch (Exception e) {
            System.out.println("catch to check for number of retries");
        }
        // the number of retries for ConstraintViolationException is configured via maxRetriesPerException bean property in lmsLearnTestContext.xml
        verify(mockNotificationService, times(2)).subscribe(notificationSubscriptionContext1);

    }

    @Test
    @Ignore
    // 18.04.2012/cg Hudson failed => LD or AA
    // TODO: THIS METHOD HAS TO BE REMOVED; IT'S REPLACED WITH THE NEXT ONE
    public void testNotifySubscribers_IfNotifyDelegateThrowsException_checkNumberOfRetriesForMailSendException() {

        Long subscriberId = new Long(1);
        NotifyDelegate mockNotifyDelegate = mock(NotifyDelegate.class);
        when(mockNotifyDelegate.notifySubscriber(subscriberId)).thenThrow(new MailSendException("mock exception"));
        notificationService.setNotifyDelegate(mockNotifyDelegate);

        try {
            verify(notificationService.notifySubscribers());
        } catch (Exception e) {
            System.out.println("catch to check for number of retries");
        }

        // the number of retries for MailSendException is configured via maxRetriesPerException bean property in lmsLearnTestContext.xml
        verify(mockNotifyDelegate, times(4)).notifySubscriber(subscriberId);
    }

    @Test
    public void testSend_IfMailServiceThrowsException_checkNumberOfRetriesForMailSendException() throws Exception {
        List<NotificationEventTO> eventTOs = new ArrayList<NotificationEventTO>();
        // Test Subscriber
        Subscriber subscriber = new Subscriber();
        subscriber.setIdentity(ObjectMother.createIdentity("testUser"));

        // Mock for TemplateMailTO
        TemplateMailTO templateMailTO = mock(TemplateMailTO.class);

        // Mock for EmailBuilder
        EmailBuilder emailBuilderMock = mock(EmailBuilder.class);
        when(emailBuilderMock.getTemplateMailTO(subscriber.getIdentity().getAttributes().getEmail(), eventTOs)).thenReturn(templateMailTO);
        mailChannel.setEmailBuilder(emailBuilderMock);

        // Mock for MailService
        MailService mailServiceMock = mock(MailService.class);
        doThrow(new MailSendException("MailService: Test Exception")).when(mailServiceMock).sendMailWithTemplate(templateMailTO);
        mailChannel.setMailService(mailServiceMock);

        try {
            mailChannel.send(subscriber, eventTOs);
        } catch (Exception e) {
            System.out.println("catch to check for number of retries");
        }

        verify(mailServiceMock, times(4)).sendMailWithTemplate(templateMailTO);
    }

    @Test
    public void addOrIncrementRetries() {
        Map<String, Long> retriesPerException = new HashMap<String, Long>();
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertTrue(retriesPerException.containsKey(ConstraintViolationException.class.getName()));

        Long counter = retriesPerException.get(ConstraintViolationException.class.getName());
        assertEquals(0, counter.longValue());

        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        counter = retriesPerException.get(ConstraintViolationException.class.getName());
        assertEquals(1, counter.longValue());
    }

    @Test
    public void isRetryStillAllowed_oneExceptionType_oneRetryAllowed() {
        Map<String, Long> retriesPerException = new HashMap<String, Long>();
        assertTrue(transactionRetryer.isRetryStillAllowed(retriesPerException));

        // retry once allowed
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertTrue(transactionRetryer.isRetryStillAllowed(retriesPerException));

        // retry second time no more allowed
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertFalse(transactionRetryer.isRetryStillAllowed(retriesPerException));

        // retry no more allowed
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertFalse(transactionRetryer.isRetryStillAllowed(retriesPerException));
    }

    /**
     * Assumes that ConstraintViolationException could be retried once while MailSendException could be retried 3 times, see lmsLearnTestContext.xml for
     * maxRetriesPerException.
     */
    @Test
    public void isRetryStillAllowed_twoExceptionTypes_moreRetriesAllowed() {
        Map<String, Long> retriesPerException = new HashMap<String, Long>();
        assertTrue(transactionRetryer.isRetryStillAllowed(retriesPerException));

        // retry for ConstraintViolationException
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertTrue(transactionRetryer.isRetryStillAllowed(retriesPerException));

        for (int i = 0; i < 3; i++) {
            // retry for MailSendException
            transactionRetryer.addOrIncrementRetries(retriesPerException, MailSendException.class.getName());
            assertTrue(transactionRetryer.isRetryStillAllowed(retriesPerException));
        }

        // retry for MailSendException - isRetryStillAllowed should return false since MailSendException is configured to be retried 3 times
        transactionRetryer.addOrIncrementRetries(retriesPerException, MailSendException.class.getName());
        boolean isMailSendExceptionRetryAllowed = transactionRetryer.isRetryStillAllowed(retriesPerException);

        // retry second time no more allowed - throw error further
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        boolean isConstraintViolationExceptionAllowed = transactionRetryer.isRetryStillAllowed(retriesPerException);

        assertFalse(isMailSendExceptionRetryAllowed);
        assertFalse(isConstraintViolationExceptionAllowed);

        // retry no more allowed
        transactionRetryer.addOrIncrementRetries(retriesPerException, ConstraintViolationException.class.getName());
        assertFalse(transactionRetryer.isRetryStillAllowed(retriesPerException));
    }
}
