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

/**
 * Initial Date: 24.05.2012 <br>
 * 
 * @author cg
 */
public final class CampusCoursePortletEntry {
    private static final int PORTLET_TITLE_MAX_LENGTH = 70;
    private final String courseTitle;
    private final Long sapCourseId;
    private final Long olatCourseId;
    private final String buttonId;

    public CampusCoursePortletEntry(String courseTitle, Long sapCourseId, Long olatCourseId, String buttonId) {
        this.courseTitle = courseTitle;
        this.sapCourseId = sapCourseId;
        this.olatCourseId = olatCourseId;
        this.buttonId = buttonId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getCourseTitleForPortlet() {
        if (getCourseTitle().length() < PORTLET_TITLE_MAX_LENGTH) {
            return getCourseTitle();
        } else {
            return courseTitle.substring(0, PORTLET_TITLE_MAX_LENGTH) + "...";
        }
    }

    public String getButtonId() {
        return buttonId;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public Long getOlatCourseId() {
        return olatCourseId;
    }

}
