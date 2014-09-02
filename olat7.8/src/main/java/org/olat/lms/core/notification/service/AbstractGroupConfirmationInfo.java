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
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public abstract class AbstractGroupConfirmationInfo extends ConfirmationInfo {

    protected final GROUP_CONFIRMATION_TYPE groupConfirmationType;

    public enum GROUP_CONFIRMATION_TYPE {
        ADD_USER_RIGHT_LEARNING_GROUP, ADD_USER_BUDDY_GROUP, REMOVE_USER_RIGHT_LEARNING_GROUP, REMOVE_USER_BUDDY_GROUP, ADD_USER_WAITING_LIST_LEARNING_GROUP, REMOVE_USER_WAITING_LIST_LEARNING_GROUP, MOVE_USER_WAITING_LIST_LEARNING_GROUP, MOVE_USER_FROM_CANDIDATES, ADD_USER_TO_CANDIDATES, REMOVE_USER_FROM_CANDIDATES, ADD_USER_TO_TOPIC, REMOVE_USER_FROM_TOPIC, ADD_USER_TO_PROJECT_MANAGERS, REMOVE_USER_FROM_PROJECT_MANAGERS, ADD_USER_TO_TOPIC_MANAGERS, REMOVE_USER_FROM_TOPIC_MANAGERS
    }

    protected AbstractGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity,
            Long courseRepositoryEntryId, String courseName, Date dateTime) {
        super(allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime);
        this.groupConfirmationType = groupConfirmationType;
    }

    @Override
    abstract public CONFIRMATION_TYPE getType();

    public GROUP_CONFIRMATION_TYPE getGroupConfirmationType() {
        return groupConfirmationType;
    }

}
