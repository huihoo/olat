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

import java.io.InputStream;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Initial Date: 19.12.2011 <br>
 * 
 * @author guretzki
 */
public class JavaMailSenderDummyImpl implements JavaMailSender {

    private static final Logger log = LoggerHelper.getLogger();

    public JavaMailSenderDummyImpl() {
        log.info("##########################################################");
        log.info("### Mail Test-Mode, no mail will be sent               ###");
        log.info("### Java-Mail-Sender is disabled                       ###");
        log.info("##########################################################");

    }

    @Override
    public void send(SimpleMailMessage msg) throws MailException {
        // used only by test classes
        logMail(msg);
    }

    @Override
    public void send(SimpleMailMessage[] messages) throws MailException {
        for (int i = 0; i < messages.length; i++) {
            logMail(messages[i]);
        }
    }

    private void logMail(SimpleMailMessage msg) {
        log.info("Mail Test-Mode: Would send mail to=" + arrayToString(msg.getTo()) + " with subject=" + msg.getSubject());
        log.debug("Mail Test-Mode: from=" + msg.getFrom() + " , cc=" + msg.getCc() + " , reply-to=" + msg.getReplyTo());
        log.debug("Mail Test-Mode: text='" + msg.getText() + "'");
    }

    private String arrayToString(String[] to) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < to.length; i++) {
            sb.append(to[i]);
            sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessageHelper(null).getMimeMessage();
    }

    @Override
    public MimeMessage createMimeMessage(InputStream arg0) throws MailException {
        return new MimeMessageHelper(null).getMimeMessage();
    }

    @Override
    public void send(MimeMessage msg) throws MailException {
        logMail(msg);

    }

    @Override
    public void send(MimeMessage[] messages) throws MailException {
        for (int i = 0; i < messages.length; i++) {
            logMail(messages[i]);
        }
    }

    @Override
    public void send(MimeMessagePreparator msg) throws MailException {
        logMail(msg);
    }

    @Override
    public void send(MimeMessagePreparator[] messages) throws MailException {
        for (int i = 0; i < messages.length; i++) {
            logMail(messages[i]);
        }
    }

    private void logMail(MimeMessagePreparator mimeMessagePreparator) {
        try {
            MimeMessage message = new MimeMessage((Session) null);
            mimeMessagePreparator.prepare(message);
            logMail(message);
        } catch (Exception e) {
            log.error("Mail Test-Mode: couldn't send mail with MimeMessagePreparator=" + mimeMessagePreparator.toString());
        }
    }

    private void logMail(MimeMessage msg) {
        try {
            String to = "";
            Address[] toAddresses = msg.getAllRecipients();
            if (toAddresses.length == 1) {
                to = toAddresses[0].getType();
            } else if (toAddresses.length > 1) {
                to = toAddresses[0].getType() + " and  #" + (msg.getAllRecipients().length - 1) + " other";
            } else {
                to = "NO-TO-ADDRESS";
            }
            log.info("Mail Test-Mode: Would send mail to=" + to + " with subject=" + msg.getSubject());
            if (log.isDebugEnabled()) {
                log.debug("Mail Test-Mode: from=" + msg.getFrom()[0]);
                log.debug("Mail Test-Mode: content=" + msg.getContent() + "'");
            }
        } catch (Exception e) {
            log.warn("Could not extract log-info from mime-message, ", e);
        }
    }

}
