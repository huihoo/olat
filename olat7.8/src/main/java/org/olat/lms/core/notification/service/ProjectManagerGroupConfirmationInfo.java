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
 * Contains the info for sending a confirmation about an action regarding project manager group (add, remove project manager) <br/>
 * 
 * Initial Date: Oct 30, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class ProjectManagerGroupConfirmationInfo extends AbstractGroupConfirmationInfo {

    private final Long projectId;
    private final String projectName;
    private final Long courseNodeId;

    private ProjectManagerGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType, List<RecipientInfo> allRecipientInfos, Identity originatorIdentity,
            Long courseRepositoryEntryId, String courseName, Date dateTime, Long projectId, String projectName, Long courseNodeId) {
        super(groupConfirmationType, allRecipientInfos, originatorIdentity, courseRepositoryEntryId, courseName, dateTime);
        this.projectId = projectId;
        this.projectName = projectName;
        this.courseNodeId = courseNodeId;
    }

    public static ProjectManagerGroupConfirmationInfo createProjectManagerGroupConfirmationInfo(GROUP_CONFIRMATION_TYPE groupConfirmationType,
            List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, Long courseRepositoryEntryId, String courseName, Date dateTime, Long projectId,
            String projectName, Long courseNodeId) {
        return new ProjectManagerGroupConfirmationInfo(groupConfirmationType, allRecipientInfos, originatorIdentity, courseRepositoryEntryId, courseName, dateTime,
                projectId, projectName, courseNodeId);
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return CONFIRMATION_TYPE.PROJECT_MANAGERS;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getCourseNodeId() {
        return courseNodeId;
    }

}
