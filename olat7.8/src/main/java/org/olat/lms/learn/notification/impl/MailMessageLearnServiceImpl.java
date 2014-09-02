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
package org.olat.lms.learn.notification.impl;

import org.apache.log4j.Logger;
import org.olat.lms.core.notification.service.ConfirmationService;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 27.09.2012 <br>
 * 
 * @author lavinia
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailMessageLearnServiceImpl implements MailMessageLearnService {

    private static final Logger LOG = LoggerHelper.getLogger();

    @Autowired
    ConfirmationService mailMessageService;

    @Override
    public boolean sendMessage(MailMessage message) {
        try {
            return mailMessageService.sendMessage(message);
        } catch (Exception e) { // catch all exception, and log them
            LOG.error("sendMessage failed, catch the exception and give up", e);
        }
        return false;
    }

}
