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

package org.olat.lms.course.condition;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.course.Structure;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @deprecated
 */
@Deprecated
public class NoOpCourseEnvironment implements CourseEnvironment {

    /**
     * Default constructor for the No Op course environment. This is only used for for syntax validating
     */
    public NoOpCourseEnvironment() {
        // nothig special to do
    }

    /**
	 */
    @Override
    public boolean isNoOpMode() {
        return true;
    }

    /**
	 */

    @Override
    public long getCurrentTimeMillis() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public CourseGroupManager getCourseGroupManager() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public Long getCourseResourceableId() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public CoursePropertyManager getCoursePropertyManager() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public AssessmentManager getAssessmentManager() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public UserNodeAuditManager getAuditManager() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public Structure getRunStructure() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    @Override
    public String getCourseTitle() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public CourseConfig getCourseConfig() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    /**
	 */
    public void setCourseConfig(final CourseConfig cc) {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public VFSContainer getCourseFolderContainer() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public OlatRootFolderImpl getCourseBaseContainer() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public OLATResourceable getCourseOLATResourceable() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public List<Identity> getCourseOwners() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

    @Override
    public Long getRepositoryEntryId() {
        throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
    }

}
