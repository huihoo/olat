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

import java.util.Date;
import java.util.List;

import org.olat.lms.core.notification.service.AbstractGroupConfirmationInfo.GROUP_CONFIRMATION_TYPE;
import org.olat.lms.core.notification.service.RecipientInfo;
import org.olat.lms.core.notification.service.TopicUserGroupConfirmationInfo;

/**
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class TopicUserGroupConfirmationSender extends AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicUserGroupConfirmationInfo> {

    public TopicUserGroupConfirmationSender(TopicGroupConfirmationSenderInfo confirmationSenderInfo) {
        super(confirmationSenderInfo);
    }

    @Override
    protected GROUP_CONFIRMATION_TYPE getAddUserGroupConfirmationType() {
        return GROUP_CONFIRMATION_TYPE.ADD_USER_TO_TOPIC;
    }

    @Override
    protected GROUP_CONFIRMATION_TYPE getRemoveUserGroupConfirmationType() {
        return GROUP_CONFIRMATION_TYPE.REMOVE_USER_FROM_TOPIC;
    }

    @Override
    protected TopicUserGroupConfirmationInfo getConfirmationInfo(List<RecipientInfo> recipientInfos, GROUP_CONFIRMATION_TYPE groupConfirmationType) {
        TopicUserGroupConfirmationInfo topicUserGroupConfirmationInfo = TopicUserGroupConfirmationInfo.createTopicUserGroupConfirmationInfo(groupConfirmationType,
                recipientInfos, confirmationSenderInfo.getOriginatorIdentity(), confirmationSenderInfo.getRepositoryEntry().getKey(), confirmationSenderInfo
                        .getRepositoryEntry().getDisplayname(), new Date(), confirmationSenderInfo.getProject().getProjectBroker().getKey(), confirmationSenderInfo
                        .getCourseNode().getShortName(), Long.valueOf(confirmationSenderInfo.getCourseNode().getIdent()), confirmationSenderInfo.getProject().getKey(),
                confirmationSenderInfo.getProject().getTitle());
        return topicUserGroupConfirmationInfo;
    }

}
