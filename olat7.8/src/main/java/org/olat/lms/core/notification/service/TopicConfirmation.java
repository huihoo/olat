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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.data.basesecurity.Identity;

/**
 * Contains the info for sending a confirmation about an action which happened in a topic (a.k.a project broker) element. <br/>
 * 
 * Initial Date: 12.10.2012 <br>
 * 
 * @author lavinia
 */
public class TopicConfirmation extends ConfirmationInfo {

    private final TYPE subtype;
    private final String fileName; // what
    private final Long courseNodeId; // used for building the url to the file
    private final Long projectId;

    /**
     * These are the supported confirmation subtypes: <br/>
     * (The name schema is: CONTEXT_ACTION_RECIPIENT) <br/>
     * 
     */
    public static enum TYPE {
        TOPIC_DROP_STUDENT, TOPIC_DROP_TUTOR, TOPIC_RETURN_STUDENT
    }

    /**
     * @param allRecipientInfos
     * @param originatorIdentity
     * @param courseName
     * @param courseRepositoryEntryId
     * @param dateTime
     */
    private TopicConfirmation(List<RecipientInfo> recipientInfos, Identity originatorIdentity, String courseName, Long courseRepositoryEntryId, Long courseNodeId,
            Long projectId, String fileName, TYPE type, Date dateTime) {

        super(recipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime);

        this.fileName = fileName;
        this.courseNodeId = courseNodeId;
        this.projectId = projectId;
        this.subtype = type;

    }

    public static TopicConfirmation createTaskConfirmation(List<RecipientInfo> recipientInfos, Identity originatorIdentity, String courseName,
            Long courseRepositoryEntryId, Long courseNodeId, Long projectId, String fileName, TYPE type, Date dateTime) {
        return new TopicConfirmation(recipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, courseNodeId, projectId, fileName, type, dateTime);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.TOPIC;
    }

    public TYPE getSubtype() {
        return subtype;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getCourseNodeId() {
        return courseNodeId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        if (allRecipientInfos.size() > 0) {
            builder.append("first recipientsEmail", allRecipientInfos.get(0).getRecipientsEmail());
            builder.append("first recipientsLocale", allRecipientInfos.get(0).getRecipientsLocale());
        }
        builder.append("originatorsFirstLastName", originatorFirstLastName);
        builder.append("courseName", courseName);
        builder.append("dateTime", dateTime);
        builder.append("fileName", fileName);
        return builder.toString();
    }

}
