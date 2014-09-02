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
package org.olat.presentation.portal.campus;

import org.olat.system.event.MultiUserEvent;

/**
 * Initial Date: 26.11.2013 <br>
 * 
 * @author aabouc
 */
@SuppressWarnings("serial")
public class CampusCourseEvent extends MultiUserEvent {

    private Long campusCourseId;
    private int status;

    public static final int CREATED = 1;
    public static final int DELETED = 2;

    public CampusCourseEvent() {
        super("campusCourse_event");

    }

    public CampusCourseEvent(Long campusCourseId, int status) {
        this();
        this.campusCourseId = campusCourseId;
        this.status = status;
    }

    public Long getCampusCourseId() {
        return campusCourseId;
    }

    public void setCampusCourseId(Long campusCourseId) {
        this.campusCourseId = campusCourseId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
