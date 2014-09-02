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
package org.olat.presentation.group.securitygroup.confirmation;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo;
import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public abstract class AbstractGroupConfirmationSender<K extends AbstractGroupConfirmationSenderInfo, T extends AbstractGroupConfirmationInfo> {

    protected K confirmationSenderInfo;

    protected AbstractGroupConfirmationSender(K confirmationSenderInfo) {
        this.confirmationSenderInfo = confirmationSenderInfo;
    }

    public void sendAddUserConfirmation(List<Identity> recipients) {
        sendConfirmation(recipients, getAddUserGroupConfirmationType());
    }

    public void sendRemoveUserConfirmation(List<Identity> recipients) {
        sendConfirmation(recipients, getRemoveUserGroupConfirmationType());
    }

    abstract protected AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE getAddUserGroupConfirmationType();

    abstract protected AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE getRemoveUserGroupConfirmationType();

    protected void sendConfirmation(List<Identity> recipients, AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE groupConfirmationType) {
        removeOriginatorFromRecipients(recipients);
        if (!recipients.isEmpty()) {
            List<RecipientInfo> recipientInfos = getConfirmationLearnService().createRecipientInfos(recipients);
            T confirmationInfo = getConfirmationInfo(recipientInfos, groupConfirmationType);
            getConfirmationLearnService().sendGroupConfirmation(confirmationInfo);
        }
    }

    protected ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    abstract protected T getConfirmationInfo(List<RecipientInfo> recipientInfos, GROUP_CONFIRMATION_TYPE groupConfirmationType);

    private void removeOriginatorFromRecipients(List<Identity> recipients) {
        Identity originator = confirmationSenderInfo.getOriginatorIdentity();
        for (Identity recipient : recipients) {
            if (originator.equals(recipient)) {
                recipients.remove(recipient);
                break;
            }
        }
    }

}
