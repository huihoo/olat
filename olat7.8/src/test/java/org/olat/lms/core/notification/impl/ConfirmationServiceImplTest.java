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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.lms.core.notification.impl.channel.InvalidAddressException;
import org.olat.lms.core.notification.impl.metric.ConfirmationServiceContext;
import org.olat.lms.core.notification.impl.metric.ConfirmationServiceMetric;
import org.olat.lms.core.notification.impl.metric.ConfirmationSuccessRateMetric;
import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.system.support.mail.service.MailService;
import org.olat.system.support.mail.service.TemplateWithAttachmentMailTO;
import org.springframework.mail.MailSendException;

/**
 * Initial Date: 09.11.2012 <br>
 * 
 * @author lavinia
 */
public class ConfirmationServiceImplTest {

    private ConfirmationServiceImpl confirmationServiceImpl;

    private ConfirmationDelegate confirmationDelegateMock;
    private ConfirmationInfo confirmationInfoMock_sent;
    private ConfirmationInfo confirmationInfoMock_notSent;

    private MailService mailServiceMock;
    private MailMessageEmailBuilder emailBuilderMock;
    private MailMessage mailMessageMock;
    private String okEmailAddress = "okEmailAddress@gmail.com";
    private MailMessage mailMessageMock_nok;
    private String wrongEmailAddress = "wrongEmailAddress@gmail.com";

    private MailMessage mailMessageWithTwoRecipientsMock; // two recipients: a good and a bad one

    @Before
    public void setUp() throws Exception {
        confirmationServiceImpl = new ConfirmationServiceImpl();
        confirmationDelegateMock = mock(ConfirmationDelegate.class);
        confirmationServiceImpl.confirmationDelegate = confirmationDelegateMock;
        confirmationInfoMock_sent = mock(ConfirmationInfo.class);
        when(confirmationDelegateMock.send(confirmationInfoMock_sent)).thenReturn(true);

        confirmationInfoMock_notSent = mock(ConfirmationInfo.class);
        when(confirmationDelegateMock.send(confirmationInfoMock_notSent)).thenReturn(false);

        // set up mock message and service
        mailServiceMock = mock(MailService.class);
        mailMessageMock = mock(MailMessage.class);
        List<String> recipientsList = new ArrayList<String>();
        recipientsList.add(okEmailAddress);
        when(mailMessageMock.getToEmailAddresses()).thenReturn(recipientsList);

        mailMessageMock_nok = mock(MailMessage.class);
        emailBuilderMock = mock(MailMessageEmailBuilder.class);

        TemplateWithAttachmentMailTO template = mock(TemplateWithAttachmentMailTO.class);
        when(emailBuilderMock.getMailTemplate(okEmailAddress, mailMessageMock)).thenReturn(template);

        TemplateWithAttachmentMailTO template_nok = mock(TemplateWithAttachmentMailTO.class);
        when(emailBuilderMock.getMailTemplate(wrongEmailAddress, mailMessageMock_nok)).thenReturn(template_nok);

        confirmationServiceImpl.mailService = mailServiceMock;
        confirmationServiceImpl.emailBuilder = emailBuilderMock;

        doThrow(new MailSendException("MailService: Invalid Addresses")).when(mailServiceMock).sendMailWithAttachments(template_nok);

        // setup mock message with two recipients: a good and bad one
        mailMessageWithTwoRecipientsMock = mock(MailMessage.class);
        List<String> twoRecipientsList = new ArrayList<String>();
        twoRecipientsList.add(wrongEmailAddress);
        twoRecipientsList.add(okEmailAddress);
        when(mailMessageWithTwoRecipientsMock.getToEmailAddresses()).thenReturn(twoRecipientsList);

        TemplateWithAttachmentMailTO template_nok2 = mock(TemplateWithAttachmentMailTO.class);
        when(emailBuilderMock.getMailTemplate(wrongEmailAddress, mailMessageWithTwoRecipientsMock)).thenReturn(template_nok2);

        doThrow(new MailSendException("MailService: Invalid Addresses")).when(mailServiceMock).sendMailWithAttachments(template_nok2);
    }

    @Test
    public void testMetrics_sendConfirmation_OnceSuccessful() {
        setNewMetrics();

        boolean isSent = confirmationServiceImpl.sendConfirmation(confirmationInfoMock_sent);
        assertTrue("send confirmation should be successful", isSent);

        double successRate = getSuccessRate();
        assertEquals(1.0, successRate, 0);

        assertEquals(1, getAllSent());
        assertEquals(0, getSentFailed());
    }

    private void setNewMetrics() {
        List<ConfirmationServiceMetric<ConfirmationServiceContext>> newMetrics = new ArrayList<ConfirmationServiceMetric<ConfirmationServiceContext>>();
        ConfirmationServiceMetric<? extends ConfirmationServiceContext> confirmationSuccessMetric = new ConfirmationSuccessRateMetric();
        newMetrics.add((ConfirmationServiceMetric<ConfirmationServiceContext>) confirmationSuccessMetric);
        confirmationServiceImpl.setMetrics(newMetrics);
    }

    private double getSuccessRate() {
        List<ConfirmationServiceMetric<ConfirmationServiceContext>> metrics = confirmationServiceImpl.getMetrics();
        double successRate = 0;
        for (ConfirmationServiceMetric<?> metric : metrics) {
            successRate = ((ConfirmationSuccessRateMetric) metric).getAverageConfirmationSuccessRate();
        }
        return successRate;
    }

    private int getSentFailed() {
        List<ConfirmationServiceMetric<ConfirmationServiceContext>> metrics = confirmationServiceImpl.getMetrics();
        int numberOfFailed = 0;
        for (ConfirmationServiceMetric<?> metric : metrics) {
            numberOfFailed = ((ConfirmationSuccessRateMetric) metric).getSentFailed();
        }
        return numberOfFailed;
    }

    private int getAllSent() {
        List<ConfirmationServiceMetric<ConfirmationServiceContext>> metrics = confirmationServiceImpl.getMetrics();
        int numberOfAllSent = 0;
        for (ConfirmationServiceMetric<?> metric : metrics) {
            numberOfAllSent = ((ConfirmationSuccessRateMetric) metric).getAllSent();
        }
        return numberOfAllSent;
    }

    @Test
    public void testMetrics_sendConfirmation_OnceSuccessful_OnceUnsuccessful() {
        setNewMetrics();

        boolean isSent = confirmationServiceImpl.sendConfirmation(confirmationInfoMock_sent);
        assertTrue("send confirmation should be successful", isSent);
        confirmationServiceImpl.sendConfirmation(confirmationInfoMock_notSent);

        double successRate = getSuccessRate();
        assertEquals(0.5, successRate, 0);

        assertEquals(2, getAllSent());
        assertEquals(1, getSentFailed());
    }

    @Test
    public void testMetrics_sendConfirmation_TwiceUnsuccessful() {
        setNewMetrics();

        boolean isSent = confirmationServiceImpl.sendConfirmation(confirmationInfoMock_notSent);
        assertFalse("send confirmation should be unsuccessful", isSent);
        confirmationServiceImpl.sendConfirmation(confirmationInfoMock_notSent);

        double successRate = getSuccessRate();
        assertEquals(0, successRate, 0);

        assertEquals(2, getAllSent());
        assertEquals(2, getSentFailed());
    }

    @Test
    public void testMetrics_sendMessage_OneMessage() {
        setNewMetrics();

        try {
            boolean isSent = confirmationServiceImpl.sendMessage(mailMessageMock);
            assertTrue("send message should be successful", isSent);

            double successRate = getSuccessRate();
            assertEquals(1.0, successRate, 0);

            assertEquals(1, getAllSent());
            assertEquals(0, getSentFailed());
        } catch (InvalidAddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testMetrics_sendTwoMessages_sendTwoConfirmations() {
        setNewMetrics();

        try {
            boolean isSent = confirmationServiceImpl.sendMessage(mailMessageMock);
            assertTrue("send message should be successful", isSent);
            confirmationServiceImpl.sendConfirmation(confirmationInfoMock_sent);
            confirmationServiceImpl.sendMessage(mailMessageMock);
            confirmationServiceImpl.sendConfirmation(confirmationInfoMock_sent);

            double successRate = getSuccessRate();
            assertEquals(1.0, successRate, 0);

            assertEquals(4, getAllSent());
            assertEquals(0, getSentFailed());
        } catch (InvalidAddressException e) {
            // no need to show the exception
        }
    }

    @Test
    public void testMetrics_sendMessage_invalidAddress() {
        setNewMetrics();

        try {
            confirmationServiceImpl.sendMessage(mailMessageMock_nok);
        } catch (InvalidAddressException e) {
            // no need to show the exception

            double successRate = getSuccessRate();
            assertEquals(0.0, successRate, 0);
            assertEquals(1, getAllSent());
            assertEquals(1, getSentFailed());
        }
    }

    @Test
    public void testMetrics_sendMessageToTwoRecipients() {
        setNewMetrics();

        try {
            boolean isSent = confirmationServiceImpl.sendMessage(mailMessageWithTwoRecipientsMock);
            assertTrue("last sent message should be successful", isSent);

            double successRate = getSuccessRate();
            assertEquals(0.5, successRate, 0);

            assertEquals(2, getAllSent());
            assertEquals(1, getSentFailed());
        } catch (InvalidAddressException e) {
            // no need to show the exception
        }
    }

}
