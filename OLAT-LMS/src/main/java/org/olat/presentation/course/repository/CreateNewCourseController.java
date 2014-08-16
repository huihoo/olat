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

package org.olat.presentation.course.repository;

import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.imports.CourseRepository_EBL;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.repository.IAddController;
import org.olat.presentation.repository.RepositoryAddCallback;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR/>
 * Implementation of the repository add controller for OLAT courses
 * <P/>
 * Initial Date: Oct 12, 2004
 * 
 * @author gnaegi
 */
public class CreateNewCourseController extends BasicController implements IAddController {

    // private static final String PACKAGE_REPOSITORY = Util.getPackageName(RepositoryManager.class);
    private final OLATResource newCourseResource;
    private ICourse course;// o_clusterOK: creation process
    private CourseRepository_EBL createCourseEbl;

    /**
     * Constructor for the add course controller
     * 
     * @param addCallback
     * @param ureq
     */
    public CreateNewCourseController(final RepositoryAddCallback addCallback, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        createCourseEbl = CoreSpringFactory.getBean(CourseRepository_EBL.class);
        setBasePackage(RepositoryAddCallback.class);
        // do prepare course now
        newCourseResource = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
        if (addCallback != null) {
            addCallback.setResourceable(newCourseResource);
            addCallback.setDisplayName(translate(newCourseResource.getResourceableTypeName()));
            addCallback.setResourceName("-");
            addCallback.finished(ureq);
        }
    }

    /**
	 */
    @Override
    public Component getTransactionComponent() {
        return getInitialComponent();
    }

    /**
	 */
    @Override
    public boolean transactionFinishBeforeCreate() {
        // Create course and persist course resourceable.
        course = CourseFactory.createEmptyCourse(newCourseResource, "New Course", "New Course", "");
        // initialize course groupmanagement
        final CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
        cgm.createCourseGroupmanagement(course.getResourceableId().toString(), course);

        return true;
    }

    /**
	 */
    @Override
    public void transactionAborted() {
        // Nothing to do here... no course has been created yet.
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to listen to
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        // nothing to listen to
    }

    @Override
    public void repositoryEntryCreated(final RepositoryEntry re) {
        getBaseSecurityEBL().createCourseAdminPolicy(re);
        course = CourseFactory.openCourseEditSession(re.getOlatResource().getResourceableId());
        createCourseEbl.setShortAndLongTitle(re.getDisplayname(), course);
        createCourseEbl.saveCourseAndCloseEditSession(course);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do here
    }
}
