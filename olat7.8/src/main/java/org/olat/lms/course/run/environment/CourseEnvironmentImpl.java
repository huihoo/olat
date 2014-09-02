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

package org.olat.lms.course.run.environment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.PersistingCourseImpl;
import org.olat.lms.course.Structure;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.assessment.NewCachePersistingAssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.auditing.UserNodeAuditManagerImpl;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.properties.PersistingCoursePropertyManager;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 09.03.2004
 * 
 * @author Felix Jost
 */
public class CourseEnvironmentImpl implements CourseEnvironment {

    private final PersistingCourseImpl course;
    private final CoursePropertyManager propertyManager;
    private final AssessmentManager assessmentManager;
    private UserNodeAuditManager auditManager;

    /**
     * Constructor for the course environment
     * 
     * @param course
     *            The course
     */
    public CourseEnvironmentImpl(final PersistingCourseImpl course) {
        OLATResourceManager.getInstance().findOrPersistResourceable(course);
        this.course = course;
        this.propertyManager = PersistingCoursePropertyManager.getInstance(course);
        this.assessmentManager = NewCachePersistingAssessmentManager.getInstance(course);
    }

    /**
	 */
    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
	 */
    @Override
    public boolean isNoOpMode() {
        return false;
    }

    /**
	 */
    @Deprecated
    // CourseGroupManager is now stateless, acccess via spring autowire
    @Override
    public CourseGroupManager getCourseGroupManager() {
        return (CourseGroupManager) CoreSpringFactory.getBean("persistingCourseGroupManager");
    }

    /**
	 */
    @Override
    public Long getCourseResourceableId() {
        return course.getResourceableId();
    }

    /**
	 */
    @Override
    public CoursePropertyManager getCoursePropertyManager() {
        return propertyManager;
    }

    /**
	 */
    @Override
    public AssessmentManager getAssessmentManager() {
        return this.assessmentManager;
    }

    /**
	 */
    @Override
    public UserNodeAuditManager getAuditManager() {
        /**
         * staring audit manager due to early caused problem with fresh course imports (demo courses!) on startup
         */
        if (this.auditManager == null) {
            this.auditManager = new UserNodeAuditManagerImpl(course);
        }
        return this.auditManager;
    }

    /**
	 */
    @Override
    public Structure getRunStructure() {
        final Structure runStructure = course.getRunStructure();
        if (runStructure == null) {
            throw new AssertException("asked for runstructure, but icourse's runstructure is still null");
        }
        return runStructure;
    }

    /**
	 */
    @Override
    public String getCourseTitle() {
        return course.getCourseTitle();
    }

    /**
	 */
    @Override
    public CourseConfig getCourseConfig() {
        return course.getCourseConfig();
    }

    @Override
    public VFSContainer getCourseFolderContainer() {
        return course.getCourseFolderContainer();
    }

    @Override
    public OlatRootFolderImpl getCourseBaseContainer() {
        return course.getCourseBaseContainer();
    }

    @Override
    public OLATResourceable getCourseOLATResourceable() {
        return course;
    }

    @Override
    public List<Identity> getCourseOwners() {
        final List<Identity> identities = new ArrayList<Identity>();
        final RepositoryEntry repositoryEntry = getRepositoryService().lookupRepositoryEntry(
                OresHelper.createOLATResourceableInstance(CourseModule.class, getCourseResourceableId()), false);
        Set<Identity> identitiesSet = new HashSet<Identity>();
        identitiesSet.addAll(getBaseSecurity().getIdentitiesOfSecurityGroup(repositoryEntry.getOwnerGroup()));
        identities.addAll(identitiesSet);
        return identities;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryService.class);
    }

    @Override
    public Long getRepositoryEntryId() {
        return getRepositoryService().getRepositoryEntryIdFromResourceable(getCourseResourceableId(), "CourseModule");
    }

}
