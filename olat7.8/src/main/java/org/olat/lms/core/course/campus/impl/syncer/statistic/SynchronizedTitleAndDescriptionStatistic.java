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
package org.olat.lms.core.course.campus.impl.syncer.statistic;

/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class SynchronizedTitleAndDescriptionStatistic {

    private String courseTitle;
    private SynchronizedSecurityGroupStatistic ownerGroupStatistic;
    private SynchronizedSecurityGroupStatistic participantGroupStatistic;

    /**
     * @param courseTitle
     * @param ownerGroupStatistic
     * @param participantGroupStatistic
     */
    public SynchronizedTitleAndDescriptionStatistic(String courseTitle, SynchronizedSecurityGroupStatistic ownerGroupStatistic,
            SynchronizedSecurityGroupStatistic participantGroupStatistic) {
        this.courseTitle = courseTitle;
        this.ownerGroupStatistic = ownerGroupStatistic;
        this.participantGroupStatistic = participantGroupStatistic;
    }

    /**
     * @param string
     */
    public SynchronizedTitleAndDescriptionStatistic(String title) {
        this.courseTitle = title;
        this.ownerGroupStatistic = new SynchronizedSecurityGroupStatistic(0, 0);
        this.participantGroupStatistic = new SynchronizedSecurityGroupStatistic(0, 0);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SynchronizedGroupStatistic: ");
        builder.append("course='");
        builder.append(courseTitle);
        builder.append("' ");
        builder.append("Owner-Group: ");
        builder.append(ownerGroupStatistic);
        builder.append("Participant-Group: ");
        builder.append(participantGroupStatistic);
        return builder.toString();
    }

    public SynchronizedSecurityGroupStatistic getOwnerGroupStatistic() {
        return ownerGroupStatistic;
    }

    public SynchronizedSecurityGroupStatistic getParticipantGroupStatistic() {
        return participantGroupStatistic;
    }

    public static SynchronizedTitleAndDescriptionStatistic createEmptyStatistic(long sapCourseId) {
        return new SynchronizedTitleAndDescriptionStatistic("EmptyStatistic sapCourseId=" + sapCourseId);
    }
}
