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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.notification.Subscriber;
import org.olat.lms.core.notification.impl.EmailBuilder;
import org.olat.lms.core.notification.impl.NotificationEventTO;
import org.olat.system.commons.Retryable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 16.03.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class MailChannel implements Channel {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private MailService mailService;
    @Autowired
    private EmailBuilder emailBuilder;

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setEmailBuilder(EmailBuilder emailBuilder) {
        this.emailBuilder = emailBuilder;
    }

    /**
     * @Retryable since it could throw a MailException if the sendMailWithTemplate fails, so we might want to retry. <br/>
     *            However, if an "Invalid Addresses" is the reason for the failure, is useless to retry, <br/>
     *            so we throw a new exception (InvalidAddressException) which is not catched by the TransactionRetryer.
     */
    @Retryable
    public void send(Subscriber subscriber, List<NotificationEventTO> events) throws Exception {
        String emailAddress = subscriber.getIdentity().getAttributes().getEmail();
        try {
            mailService.sendMailWithTemplate(emailBuilder.getTemplateMailTO(emailAddress, events));
        } catch (MailException e) {
            String message = e.getMessage();
            if (message != null && !message.isEmpty() && message.indexOf("Invalid Addresses") > 0) {
                // bypass the retry
                log.error("Invalid Addresses detected !!! ", e);
                throw new InvalidAddressException(e, emailAddress);
            }
            throw e;
        }
    }

    public Subscriber.Channel getChannelName() {
        return Subscriber.Channel.EMAIL;
    }

}
