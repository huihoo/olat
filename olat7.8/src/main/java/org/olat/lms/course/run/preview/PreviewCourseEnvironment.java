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

package org.olat.lms.course.run.preview;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.Structure;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: 08.02.2005
 * 
 * @author Mike Stock
 */
final public class PreviewCourseEnvironment implements CourseEnvironment {
    private final String title;
    private final Structure runStructure;
    private final OlatRootFolderImpl courseBaseContainer;
    private final VFSContainer courseFolderContainer;
    private final CoursePropertyManager coursePropertyManager;
    private final CourseGroupManager courseGroupManager;
    private final UserNodeAuditManager auditManager;
    private final AssessmentManager assessmentManager;
    private final long simulatedDateTime;
    private final Long resourceablId;
    private final ICourse course;

    public PreviewCourseEnvironment(final ICourse course, final Date simulatedDateTime, final CoursePropertyManager cpm, final CourseGroupManager cgm,
            final UserNodeAuditManager auditman, final AssessmentManager am) {
        super();
        this.course = course;
        this.title = course.getCourseTitle();
        this.courseFolderContainer = course.getCourseFolderContainer();
        this.courseBaseContainer = course.getCourseBaseContainer();
        this.runStructure = course.getEditorTreeModel().createStructureForPreview();
        this.resourceablId = course.getResourceableId();

        this.simulatedDateTime = simulatedDateTime.getTime();
        this.coursePropertyManager = cpm;
        this.courseGroupManager = cgm;
        this.auditManager = auditman;
        this.assessmentManager = am;

    }

    /**
	 */
    @Override
    public long getCurrentTimeMillis() {
        return simulatedDateTime;
    }

    /**
	 */
    @Override
    public boolean isNoOpMode() {
        return false;
    }

    /**
	 */
    @Override
    public CourseGroupManager getCourseGroupManager() {
        return courseGroupManager;
    }

    /**
	 */
    @Override
    public Long getCourseResourceableId() {
        // since OLAT 6.0.x: needed for SinglePage and hence for STCourseNode
        // introduced dependancy through iFrame refactoring of SinglePage and CP
        return resourceablId;
    }

    /**
	 */
    @Override
    public CoursePropertyManager getCoursePropertyManager() {
        return coursePropertyManager;
    }

    /**
	 */
    @Override
    public AssessmentManager getAssessmentManager() {
        return assessmentManager;
    }

    /**
	 */
    @Override
    public UserNodeAuditManager getAuditManager() {
        return auditManager;
    }

    /**
	 */
    @Override
    public Structure getRunStructure() {
        return runStructure;
    }

    /**
	 */
    @Override
    public String getCourseTitle() {
        return title;
    }

    @Override
    public CourseConfig getCourseConfig() {
        throw new UnsupportedOperationException("never to be called in preview mode");
    }

    /**
	 */
    public void setCourseConfig(final CourseConfig cc) {
        throw new UnsupportedOperationException("never to be called in preview mode");
    }

    /**
	 */
    @Override
    public VFSContainer getCourseFolderContainer() {
        return courseFolderContainer;
    }

    /**
	 */
    @Override
    public OlatRootFolderImpl getCourseBaseContainer() {
        return courseBaseContainer;
    }

    @Override
    public OLATResourceable getCourseOLATResourceable() {
        return course;
    }

    @Override
    public List<Identity> getCourseOwners() {
        throw new UnsupportedOperationException("never to be called in preview mode");
    }

    @Override
    public Long getRepositoryEntryId() {
        throw new UnsupportedOperationException("never to be called in preview mode");
    }

}
