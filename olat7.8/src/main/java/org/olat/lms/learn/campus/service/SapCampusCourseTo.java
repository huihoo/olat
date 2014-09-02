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
package org.olat.lms.learn.campus.service;

/**
 * Initial Date: 06.06.2012 <br>
 * 
 * @author cg
 */
public final class SapCampusCourseTo implements Comparable<SapCampusCourseTo> {

    private String title;
    private boolean activated;
    private final Long sapCourseId;
    private final Long olatCourseId;

    /**
     * @param string
     */
    public SapCampusCourseTo(String title, Long sapCourseId, Long olatCourseClosed) {
        this.title = title;
        this.sapCourseId = sapCourseId;
        this.olatCourseId = olatCourseClosed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getSapCourseId() {
        return sapCourseId;
    }

    public Long getOlatCourseId() {
        return olatCourseId;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public int compareTo(SapCampusCourseTo compareSapCampusCourseTo) {
        String compareTitle = compareSapCampusCourseTo.getTitle();
        // ASCENDING ORDER
        return this.getTitle().compareTo(compareTitle);
    }
}
