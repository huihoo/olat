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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.lms.core.CoreBaseService;
import org.olat.lms.core.notification.impl.channel.InvalidAddressException;
import org.olat.lms.core.notification.impl.metric.ConfirmationServiceContext;
import org.olat.lms.core.notification.impl.metric.ConfirmationServiceMetric;
import org.olat.lms.core.notification.impl.metric.ConfirmationStatistics;
import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.ConfirmationService;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.system.commons.Retryable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 19.09.2012 <br>
 * 
 * @author Branislav Balaz
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class ConfirmationServiceImpl extends CoreBaseService<ConfirmationServiceMetric<ConfirmationServiceContext>, ConfirmationServiceContext> implements
        ConfirmationService {

    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    ConfirmationDelegate confirmationDelegate;

    @Autowired
    MailService mailService;
    @Autowired
    MailMessageEmailBuilder emailBuilder;

    @Autowired
    @Override
    protected void setMetrics(List<ConfirmationServiceMetric<ConfirmationServiceContext>> metrics) {
        for (ConfirmationServiceMetric<ConfirmationServiceContext> metric : metrics) {
            attach(metric);
        }
    }

    List<ConfirmationServiceMetric<ConfirmationServiceContext>> getMetrics() {
        return metrics;
    }

    @Override
    public boolean sendConfirmation(ConfirmationInfo confirmationInfo) {
        boolean delivered = false;
        try {
            delivered = confirmationDelegate.send(confirmationInfo);
        } catch (Exception e) {
            LOG.error("Could not send confirmation for: " + confirmationInfo);
            delivered = false;
        } finally {
            notifyMetrics(delivered);
        }
        return delivered;
    }

    private void notifyMetrics(boolean delivered) {
        // notify/update metrics
        ConfirmationStatistics serviceContext = new ConfirmationStatistics();
        if (delivered) {
            serviceContext.incrementDelivered();
        } else {
            serviceContext.incrementFailed();
        }
        this.notifyMetrics(serviceContext);
    }

    /**
     * Business decisions: <br>
     * 1. send email to only one recipient at a time, <br>
     * 2. if <code>message.hasCC()==true</code> send only one cc: a message with same recipient and sender.
     * 
     * @return returns true if the last sent email did not produced an MailException.
     */
    @Override
    @Retryable
    public boolean sendMessage(MailMessage message) throws InvalidAddressException {
        boolean delivered = false;
        // send cc only once
        if (message.hasCC() && !message.getToEmailAddresses().contains(message.getCCEmailAddress())) {
            delivered = sendToOneRecipient(message, message.getCCEmailAddress());
        }
        // send messages without cc
        message.setNoCC();
        List<String> toEmailAddresses = message.getToEmailAddresses();
        for (String toEmailAddress : toEmailAddresses) {
            delivered = sendToOneRecipient(message, toEmailAddress);
        }

        return delivered;
    }

    private boolean sendToOneRecipient(MailMessage messageClone, String toEmailAddress) {
        boolean delivered = false;
        try {
            LOG.info("send message to: " + toEmailAddress);
            mailService.sendMailWithAttachments(emailBuilder.getMailTemplate(toEmailAddress, messageClone));
            delivered = true;
        } catch (MailException ex) {
            String exMessage = ex.getMessage();
            if (messageClone != null && !exMessage.isEmpty() && exMessage.indexOf("Invalid Addresses") > 0) {
                delivered = false;
                // bypass the retry and log the invalid address - but send for the next recipient
                LOG.error("Invalid Addresses detected !!! " + toEmailAddress, ex);
            } else {
                // retry, if no Invalid Addresses
                throw ex;
            }
        } finally {
            notifyMetrics(delivered);
        }
        return delivered;
    }

}
