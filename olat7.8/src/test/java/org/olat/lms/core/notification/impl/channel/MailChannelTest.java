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
package org.olat.lms.core.notification.impl.channel;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.EmailBuilder;
import org.olat.lms.core.notification.impl.NotificationEventTO;
import org.olat.lms.core.notification.impl.ObjectMother;
import org.olat.system.support.mail.service.MailService;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;

/**
 * Initial Date: 28.03.2012 <br>
 * 
 * @author aabouc
 */
public class MailChannelTest {

    private MailChannel mailChannelTestObject;

    private MailService mailServiceMock;

    private Subscriber subscriber;
    private List<NotificationEventTO> eventTOs = new ArrayList<NotificationEventTO>();

    @Before
    public void setup() {
        mailChannelTestObject = new MailChannel();

        // Mock for MailService
        mailServiceMock = mock(MailService.class);
        mailChannelTestObject.setMailService(mailServiceMock);

        // Test Subscriber
        subscriber = new Subscriber();
        subscriber.setIdentity(ObjectMother.createIdentity("testUser"));

        // Mock for TemplateMailTO
        TemplateMailTO templateMailTO = mock(TemplateMailTO.class);

        // Mock for EmailBuilder
        EmailBuilder emailBuilderMock = mock(EmailBuilder.class);
        when(emailBuilderMock.getTemplateMailTO(subscriber.getIdentity().getAttributes().getEmail(), eventTOs)).thenReturn(templateMailTO);
        mailChannelTestObject.setEmailBuilder(emailBuilderMock);
    }

    @Test
    public void send_successfull() throws Exception {
        doNothing().when(mailServiceMock).sendMailWithTemplate(any(TemplateMailTO.class));
        mailChannelTestObject.send(subscriber, eventTOs);
    }

    @Test(expected = MailException.class)
    public void send_faild() throws Exception {
        doThrow(new MailSendException("MailService: Test Exception")).when(mailServiceMock).sendMailWithTemplate(any(TemplateMailTO.class));
        mailChannelTestObject.send(subscriber, eventTOs);
    }

}
