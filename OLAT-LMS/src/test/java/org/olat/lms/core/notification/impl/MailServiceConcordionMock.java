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
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.olat.system.support.mail.service.MailService;
import org.olat.system.support.mail.service.SimpleMailTO;
import org.olat.system.support.mail.service.TemplateMailTO;
import org.springframework.mail.MailException;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * Initial Date: 07.02.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class MailServiceConcordionMock implements MailService {

    private VelocityEngine velocityEngine;
    private final Map<String, UserNotifyStatistic> userStatisticMap = new HashMap<String, UserNotifyStatistic>();

    @Override
    public void sendSimpleMail(SimpleMailTO mailParameters) throws MailException {
        // TODO Auto-generated method stub

    }

    /**
     * used by Spring
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    @Override
    public void sendMailWithTemplate(final TemplateMailTO mailParameters) throws MailException {

        String emailBody = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, mailParameters.getTemplateLocation(), mailParameters.getTemplateProperties());
        userStatisticMap.put(getUserNameFromEmailAddress(mailParameters.getToMailAddress()), new UserNotifyStatistic(mailParameters.getToMailAddress(), true, emailBody,
                mailParameters.getSubject(), mailParameters.getFromMailAddress()));

    }

    public static class UserNotifyStatistic {

        public final String mailToAddress;
        public final boolean isMailSuccess;
        public final String mailBody;
        public final String mailSubject;
        public final String mailFromAddress;

        public UserNotifyStatistic(String user, boolean isMailSuccess, String mailBody, String mailSubject, String mailFromAddress) {
            this.mailToAddress = user;
            this.isMailSuccess = isMailSuccess;
            this.mailBody = mailBody;
            this.mailSubject = mailSubject;
            this.mailFromAddress = mailFromAddress;
        }

    }

    public Map<String, UserNotifyStatistic> getUserStatisticMap() {
        return userStatisticMap;
    }

    private String getUserNameFromEmailAddress(String emailAddress) {
        return emailAddress.substring(0, emailAddress.indexOf('@'));
    }

}
