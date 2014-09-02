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
package org.olat.lms.core.notification.service;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * Contains the info for sending a confirmation about an action regarding topic candidates group (add, remove topic candidate) <br/>
 * 
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class TopicCandidateGroupConfirmationInfo extends TopicGroupConfirmationInfo {

    private TopicCandidateGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity,
            String courseName, Long courseRepositoryEntryId, Date dateTime, Long projectId, String projectName, Long topicId, String topicName, Long courseNodeId) {
        super(groupConfirmationType, allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime, projectId, projectName, topicId, topicName,
                courseNodeId);
    }

    public static TopicCandidateGroupConfirmationInfo createTopicCandidateGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType,
            List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, Long courseRepositoryEntryId, String courseName, Date dateTime, Long projectId,
            String projectName, Long courseNodeId, Long topicId, String topicName) {
        return new TopicCandidateGroupConfirmationInfo(groupConfirmationType, allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime,
                projectId, projectName, topicId, topicName, courseNodeId);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.TOPIC_CANDIDATES;
    }

}
