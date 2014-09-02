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

import java.io.File;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.SupportBaseService;
import org.olat.system.support.mail.service.MailService;
import org.olat.system.support.mail.service.SimpleMailTO;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.olat.system.support.mail.service.TemplateWithAttachmentMailTO;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class MailServiceImpl extends SupportBaseService implements MailService {
    private static final Logger log = LoggerHelper.getLogger();

    JavaMailSender mailSender;
    private VelocityEngine velocityEngine;

    /**
     * used by Spring
     */
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * used by Spring
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * @throws MailException
     *             general super class for all mail-exception when send failed
     * @throws MailParseException
     *             in case of failure when parsing the message
     * @throws MailAuthenticationException
     *             in case of authentication failure
     * @throws MailSendException
     *             in case of failure when sending the message
     */
    @Override
    public void sendSimpleMail(SimpleMailTO mailParameters) throws MailException {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(mailParameters.getToMailAddress());
        msg.setFrom(mailParameters.getFromMailAddress());
        msg.setSubject(mailParameters.getSubject());
        msg.setText(mailParameters.getBodyText());
        if (mailParameters.hasCcMailAddress()) {
            msg.setCc(mailParameters.getCcMailAddress());
        }
        if (mailParameters.hasReplyTo()) {
            msg.setReplyTo(mailParameters.getReplyTo());
        }
        mailSender.send(msg);
    }

    @Override
    public void sendMailWithTemplate(final TemplateMailTO mailParameters) throws MailException {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

                message.setTo(mailParameters.getToMailAddress());
                message.setFrom(mailParameters.getFromMailAddress());
                message.setSubject(mailParameters.getSubject());
                if (mailParameters.hasCcMailAddress()) {
                    message.setCc(mailParameters.getCcMailAddress());
                }
                if (mailParameters.hasReplyTo()) {
                    message.setReplyTo(mailParameters.getReplyTo());
                }
                String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, mailParameters.getTemplateLocation(), mailParameters.getTemplateProperties());
                log.debug("*** TEST text='" + text + "'");
                message.setText(text, true);
                message.setValidateAddresses(true);

            }
        };
        this.mailSender.send(preparator);
    }

    @Override
    public void sendMailWithAttachments(final TemplateWithAttachmentMailTO template) throws MailException {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);

                message.addTo(template.getToMailAddress());
                message.setFrom(template.getFromMailAddress());
                message.setSubject(template.getSubject());
                if (template.hasCcMailAddress()) {
                    message.setCc(template.getCcMailAddress());
                }
                if (template.hasReplyTo()) {
                    message.setReplyTo(template.getReplyTo());
                }

                // add attachments if any
                List<File> attachments = template.getAttachments();
                for (File file : attachments) {
                    message.addAttachment(file.getName(), file);
                }

                String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template.getTemplateLocation(), template.getTemplateProperties());
                message.setText(text, true);
                message.setValidateAddresses(true);
            }
        };
        this.mailSender.send(preparator);
    }

    @Override
    protected void setMetrics(List metrics) {

    }
}
