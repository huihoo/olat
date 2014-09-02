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
 * Contains the info for sending a confirmation about an action which happened in a test element. <br/>
 * 
 * Initial Date: 15.10.2012 <br>
 * 
 * @author lavinia
 */
public class TestConfirmation extends ConfirmationInfo {

    private final TYPE subtype;
    private final Long courseNodeId;
    private final String courseNodeTitle;

    /**
     * These are the supported confirmation subtypes: <br/>
     * (The name schema is: CONTEXT_ACTION_RECIPIENT) <br/>
     * 
     */
    public static enum TYPE {
        TEST_SUBMIT_STUDENT, TEST_SUBMIT_TUTOR, TEST_REPLACED
    }

    /**
     * @param allRecipientInfos
     * @param originatorIdentity
     * @param courseName
     * @param courseRepositoryEntryId
     * @param dateTime
     */
    private TestConfirmation(List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, String courseName, Long courseRepositoryEntryId, Long courseNodeId,
            String courseNodeTitle, Date dateTime, TYPE type) {
        super(allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime);

        this.subtype = type;
        this.courseNodeId = courseNodeId;
        this.courseNodeTitle = courseNodeTitle;
    }

    public static TestConfirmation createTestConfirmation(List<RecipientInfo> recipientInfos, Identity originatorIdentity, String courseName,
            Long courseRepositoryEntryId, Long courseNodeId, String courseNodeTitle, Date dateTime, TYPE type) {
        return new TestConfirmation(recipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, courseNodeId, courseNodeTitle, dateTime, type);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.TEST;
    }

    public TYPE getSubtype() {
        return subtype;
    }

    public Long getCourseNodeId() {
        return courseNodeId;
    }

    public String getCourseNodeTitle() {
        return courseNodeTitle;
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
        return builder.toString();
    }
}
