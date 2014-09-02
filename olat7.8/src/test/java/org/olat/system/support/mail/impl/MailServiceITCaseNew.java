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
package org.olat.system.support.mail.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.system.support.mail.service.SimpleMailTO;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 
 * @author Christian Guretzki
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/olat/system/support/mail/impl/_spring/mailContext.xml" })
public class MailServiceITCaseNew extends AbstractJUnit4SpringContextTests {

    @Autowired
    MailServiceImpl mailService;
    private MailObjectMother mailObjectMother;
    private TestMailSender testMailSender;

    @Before
    public void setup() {
        mailObjectMother = new MailObjectMother();
        testMailSender = new TestMailSender();
        mailService.mailSender = testMailSender;
    }

    @Test
    public void sendSimpleMail() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        mailService.sendSimpleMail(simpleMailTO);
        assertNotNull("no message send", testMailSender.getLastSendSimpleMessage());
        //
        assertEquals("Wrong to-address in send message", mailObjectMother.toAddress, testMailSender.getLastSendSimpleMessage().getTo()[0]);
        assertEquals("Wrong subject in send message", mailObjectMother.subject, testMailSender.getLastSendSimpleMessage().getSubject());
        assertEquals("Wrong from-address in send message", mailObjectMother.fromMailAddress, testMailSender.getLastSendSimpleMessage().getFrom());
        assertEquals("Wrong body-text-address in send message", mailObjectMother.bodyText, testMailSender.getLastSendSimpleMessage().getText());
    }

    @Test
    public void sendSimpleMail_withCc() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        simpleMailTO.setCcMailAddress(mailObjectMother.ccAddress);
        mailService.sendSimpleMail(simpleMailTO);
        assertNotNull("no message send", testMailSender.getLastSendSimpleMessage());
        assertEquals("Wrong cc-address in send message", mailObjectMother.ccAddress, testMailSender.getLastSendSimpleMessage().getCc()[0]);
    }

    @Test
    public void sendSimpleMail_withReplyTo() {
        SimpleMailTO simpleMailTO = mailObjectMother.createSimpleMailTO();
        simpleMailTO.setReplyTo(mailObjectMother.replyAddress);
        mailService.sendSimpleMail(simpleMailTO);
        assertNotNull("no message send", testMailSender.getLastSendSimpleMessage());
        assertEquals("Wrong cc-address in send message", mailObjectMother.replyAddress, testMailSender.getLastSendSimpleMessage().getReplyTo());
    }

    @Test
    public void sendMailWithTemplate() {
        TemplateMailTO templateMailParameters = mailObjectMother.createTemplateMailTO();

        mailService.sendMailWithTemplate(templateMailParameters);

        assertNotNull("no message send", testMailSender.getLastSendMimeMessagePreparator());
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            testMailSender.getLastSendMimeMessagePreparator().prepare(mimeMessage);
            assertEquals("Wrong to-address in send message", mailObjectMother.toAddress, mimeMessage.getAllRecipients()[0].toString());
            assertEquals("Wrong subject in send message", mailObjectMother.subject, mimeMessage.getSubject());
            assertEquals("Wrong from-address in send message", mailObjectMother.fromMailAddress, mimeMessage.getFrom()[0].toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail("throw exception:" + e);
        }
    }

    @Test
    public void sendMailWithTemplate_WithTemplateProperties() {
        TemplateMailTO templateMailParameters = mailObjectMother.createTemplateMailTO();
        String emailAddressValue = "test_template@test.tst";
        templateMailParameters.addTemplateProperty("key_user_emailAddress", emailAddressValue);
        String userNameValue = "test_username";
        templateMailParameters.addTemplateProperty("key_user_userName", userNameValue);

        mailService.sendMailWithTemplate(templateMailParameters);

        assertNotNull("no message send", testMailSender.getLastSendMimeMessagePreparator());
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            testMailSender.getLastSendMimeMessagePreparator().prepare(mimeMessage);
            assertEquals("Wrong to-address in send message", mailObjectMother.toAddress, mimeMessage.getAllRecipients()[0].toString());
            assertEquals("Wrong subject in send message", mailObjectMother.subject, mimeMessage.getSubject());
            assertEquals("Wrong from-address in send message", mailObjectMother.fromMailAddress, mimeMessage.getFrom()[0].toString());
            assertTrue(mimeMessage.getContent().toString().contains(emailAddressValue));
            assertTrue(mimeMessage.getContent().toString().contains(userNameValue));
        } catch (Exception e) {
            e.printStackTrace();
            fail("throw exception:" + e);
        }
    }

}

class TestMailSender implements JavaMailSender {

    private SimpleMailMessage lastSendSimpleMessage;
    private MimeMessagePreparator lastSendMimeMessagePreparator;

    @Override
    public void send(SimpleMailMessage message) throws MailException {
        this.lastSendSimpleMessage = message;
    }

    public SimpleMailMessage getLastSendSimpleMessage() {
        return lastSendSimpleMessage;
    }

    @Override
    public void send(SimpleMailMessage[] arg0) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public MimeMessage createMimeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream arg0) throws MailException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void send(MimeMessage arg0) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(MimeMessage[] arg0) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(MimeMessagePreparator messagePreperator) throws MailException {
        lastSendMimeMessagePreparator = messagePreperator;
    }

    public MimeMessagePreparator getLastSendMimeMessagePreparator() {
        return lastSendMimeMessagePreparator;
    }

    @Override
    public void send(MimeMessagePreparator[] arg0) throws MailException {
        // TODO Auto-generated method stub

    }

}
