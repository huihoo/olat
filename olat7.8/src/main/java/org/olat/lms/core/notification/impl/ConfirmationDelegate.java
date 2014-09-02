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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.lms.core.notification.service.ConfirmationInfo;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.system.commons.Retryable;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.support.mail.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

/**
 * Uses MailService to send confirmation mails and maps the ConfirmationInfo types to the EmailBuilder types.
 * 
 * @author lavinia
 */
@Component
public class ConfirmationDelegate {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private MailService mailService;
    @Autowired
    private List<ConfirmationEmailBuilder<ConfirmationInfo>> emailBuilders;
    private Map<ConfirmationInfo.CONFIRMATION_TYPE, ConfirmationEmailBuilder<ConfirmationInfo>> emailBuildersMap;

    @PostConstruct
    public void init() {
        emailBuildersMap = getConfirmationBuilderMap();
    }

    @Retryable
    public boolean send(ConfirmationInfo confirmationInfo) throws Exception {
        boolean atLeastOneSent = false;
        Iterator<RecipientInfo> recipientInfoIterator = confirmationInfo.getRecipientInfoIterator();
        while (recipientInfoIterator.hasNext()) {
            RecipientInfo currentRecipientInfo = recipientInfoIterator.next();
            try {
                log.info("send confirmation to: " + currentRecipientInfo);
                mailService.sendMailWithTemplate(getConfirmationEmailBuilder(confirmationInfo).getTemplateMailTO(currentRecipientInfo, confirmationInfo));
                atLeastOneSent = true;
            } catch (MailException e) {
                String message = e.getMessage();
                if (message != null && !message.isEmpty() && message.indexOf("Invalid Addresses") > 0) {
                    // bypass the retry and log the invalid address - but send for the next recipient
                    log.error("Invalid Addresses detected - currentRecipientInfo: " + currentRecipientInfo.getRecipientsEmail(), e);
                } else {
                    // retry, if no Invalid Addresses
                    throw e;
                }
            }
        }
        return atLeastOneSent;
    }

    /**
     * Chooses the right email builder for this confirmationInfo.
     */
    private ConfirmationEmailBuilder<ConfirmationInfo> getConfirmationEmailBuilder(ConfirmationInfo confirmationInfo) {
        if (emailBuildersMap.containsKey(confirmationInfo.getType())) {
            return emailBuildersMap.get(confirmationInfo.getType());
        }
        throw new AssertException("unknown confirmation info type");
    }

    private Map<ConfirmationInfo.CONFIRMATION_TYPE, ConfirmationEmailBuilder<ConfirmationInfo>> getConfirmationBuilderMap() {
        Map<ConfirmationInfo.CONFIRMATION_TYPE, ConfirmationEmailBuilder<ConfirmationInfo>> builders = new HashMap<ConfirmationInfo.CONFIRMATION_TYPE, ConfirmationEmailBuilder<ConfirmationInfo>>();
        for (ConfirmationEmailBuilder<ConfirmationInfo> emailBuilder : emailBuilders) {
            builders.put(emailBuilder.getConfirmationInfoType(), emailBuilder);
        }
        return builders;
    }
}
