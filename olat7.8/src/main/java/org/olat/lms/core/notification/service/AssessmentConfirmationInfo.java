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
 * Contains the info for sending a confirmation about an action which happened in a assessment tool. <br/>
 * 
 * Initial Date: Nov 7, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class AssessmentConfirmationInfo extends ConfirmationInfo {

    private final TYPE subtype;
    private final Long courseNodeId;
    private final String courseNodeTitle;

    public enum TYPE {
        TEST, PORTFOLIO, ASSESSMENT, TASK;
    }

    private AssessmentConfirmationInfo(TYPE subtype, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, String courseName, Long courseRepositoryEntryId,
            Date dateTime, Long courseNodeId, String courseNodeTitle) {
        super(allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime);
        this.subtype = subtype;
        this.courseNodeId = courseNodeId;
        this.courseNodeTitle = courseNodeTitle;
    }

    public static AssessmentConfirmationInfo createAssessmentConfirmationInfo(TYPE subtype, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity,
            String courseName, Long courseRepositoryEntryId, Date dateTime, Long courseNodeId, String courseNodeTitle) {
        return new AssessmentConfirmationInfo(subtype, allRecipientInfos, originatorIdentity, courseName, courseRepositoryEntryId, dateTime, courseNodeId,
                courseNodeTitle);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.ASSESSMENT;
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

}
