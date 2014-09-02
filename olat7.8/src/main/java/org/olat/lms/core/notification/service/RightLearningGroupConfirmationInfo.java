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
 * Contains the info for sending a confirmation about an action which happened in right or learning group (e.g. add,remove user) <br/>
 * 
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class RightLearningGroupConfirmationInfo extends AbstractGroupConfirmationInfo {

    private final Long groupId;
    private final String groupName;

    private RightLearningGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity,
            Date dateTime, Long groupId, String groupName, Long courseRepositoryEntryId, String courseName) {
        super(groupConfirmationType, allRecipientInfos, originatorIdentity, courseRepositoryEntryId, courseName, dateTime);
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public static RightLearningGroupConfirmationInfo createRightLearningGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType,
            List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, Date dateTime, Long groupId, String groupName, Long courseRepositoryEntryId,
            String courseName) {
        return new RightLearningGroupConfirmationInfo(groupConfirmationType, allRecipientInfos, originatorIdentity, dateTime, groupId, groupName,
                courseRepositoryEntryId, courseName);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.RIGHT_LEARNING_GROUP;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

}
