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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.data.basesecurity.Identity;

/**
 * A confirmation is a mail with a fix template, sent automatically upon completion of an user action. <br/>
 * This should contain everything you need to send a confirmation: recipients list, who, where, when and what changed.
 * 
 * Initial Date: 24.07.2012 <br>
 * 
 * @author lavinia
 */
public abstract class ConfirmationInfo {

    protected final List<RecipientInfo> allRecipientInfos;
    protected final String courseName; // where
    protected final Long courseRepositoryEntryId; // used for building the url to the course
    protected final Date dateTime; // when
    protected final Identity originatorIdentity; // identity of student which has solved test
    protected final String originatorFirstLastName;

    public static enum CONFIRMATION_TYPE {
        GROUP, TASK, TOPIC, TEST, GROUPS, TOPIC_GROUP, REPOSITORY_ENTRIES, BUDDY_GROUP, RIGHT_LEARNING_GROUP, LEARNING_WAITING_LIST, PROJECT_MANAGERS, TOPIC_USERS, TOPIC_MANAGERS, TOPIC_CANDIDATES, TOPIC_ENROLLMENT, ASSESSMENT, TOPIC_CHANGE;
    }

    protected ConfirmationInfo(List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, String courseName, Long courseRepositoryEntryId, Date dateTime) {

        this.allRecipientInfos = allRecipientInfos;
        this.courseName = courseName;
        this.courseRepositoryEntryId = courseRepositoryEntryId;
        this.dateTime = dateTime;
        this.originatorIdentity = originatorIdentity;
        originatorFirstLastName = getFirstLastName(originatorIdentity);
    }

    private String getFirstLastName(Identity identity) {
        if (identity != null) {
            return identity.getAttributes().getFirstName() + " " + identity.getAttributes().getLastName();
        }
        return "";
    }

    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        if (allRecipientInfos.size() > 0) {
            builder.append("first recipientsEmail", allRecipientInfos.get(0).getRecipientsEmail());
            builder.append("first recipientsLocale", allRecipientInfos.get(0).getRecipientsLocale());
        }
        builder.append("originatorIdentity", originatorIdentity.getName());
        builder.append("courseName", courseName);
        builder.append("dateTime", dateTime);

        return builder.toString();
    }

    public String getCourseName() {
        return courseName;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public Iterator<RecipientInfo> getRecipientInfoIterator() {
        return allRecipientInfos.iterator();
    }

    public Long getCourseRepositoryEntryId() {
        return courseRepositoryEntryId;
    }

    public Identity getOriginatorIdentity() {
        return originatorIdentity;
    }

    public String getOriginatorFirstLastName() {
        return originatorFirstLastName;
    }

    public abstract CONFIRMATION_TYPE getType();

}
