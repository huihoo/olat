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
package org.olat.lms.core.course.campus;

import java.util.List;

import org.olat.data.basesecurity.Identity;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CampusCourseImportTO {

    private String title;
    private String semester;
    private String language;
    private List<Identity> lecturers;
    private List<Identity> delegatees;
    private List<Identity> participants;
    private String eventDescription;
    private Long resourceableId;
    private Long sapCourseId;

    public CampusCourseImportTO(String title, String semester, List<Identity> lecturers, List<Identity> participants, String eventDescription, Long resourceableId) {
        this.title = title;
        this.semester = semester;
        this.lecturers = lecturers;
        this.participants = participants;
        this.eventDescription = eventDescription;
        this.resourceableId = resourceableId;

    }

    public CampusCourseImportTO(String title, String semester, List<Identity> lecturers, List<Identity> participants, String eventDescription, Long resourceableId,
            String language) {
        this.title = title;
        this.semester = semester;
        this.lecturers = lecturers;
        this.participants = participants;
        this.eventDescription = eventDescription;
        this.resourceableId = resourceableId;
        this.language = language;
    }

    public CampusCourseImportTO(String title, String semester, List<Identity> lecturers, List<Identity> delegatees, List<Identity> participants, String eventDescription,
            Long resourceableId, Long sapCourseId, String language) {
        this.title = title;
        this.semester = semester;
        this.lecturers = lecturers;
        this.delegatees = delegatees;
        this.participants = participants;
        this.eventDescription = eventDescription;
        this.resourceableId = resourceableId;
        this.sapCourseId = sapCourseId;
        this.language = language;

    }

    public String getSemester() {
        return semester;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getTitle() {
        return title;
    }

    public List<Identity> getLecturers() {
        return lecturers;
    }

    public List<Identity> getDelegatees() {
        return delegatees;
    }

    public List<Identity> getParticipants() {
        return participants;
    }

    public Long getOlatResourceableId() {
        return resourceableId;
    }

    public boolean isOlatResourceableIdUndefined() {
        return getOlatResourceableId() == null;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public String getLanguage() {
        return language;
    }

    public List<Identity> getLecturersAndDelegatees() {
        List<Identity> lecturersAndDelegatees = getLecturers();
        lecturersAndDelegatees.addAll(getDelegatees());
        return lecturersAndDelegatees;
    }

}
