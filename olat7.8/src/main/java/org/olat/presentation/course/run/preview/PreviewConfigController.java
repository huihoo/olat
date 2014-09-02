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

package org.olat.presentation.course.run.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.group.area.BGArea;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.AssessmentManager;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.course.run.preview.PreviewAssessmentManager;
import org.olat.lms.course.run.preview.PreviewAuditManager;
import org.olat.lms.course.run.preview.PreviewCourseEnvironment;
import org.olat.lms.course.run.preview.PreviewCourseGroupManager;
import org.olat.lms.course.run.preview.PreviewCoursePropertyManager;
import org.olat.lms.course.run.preview.PreviewIdentity;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.system.event.Event;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class PreviewConfigController extends MainLayoutBasicController {

    private final VelocityContainer configVc;
    private final PreviewSettingsForm psf;
    private PreviewRunController prc;

    private IdentityEnvironment simIdentEnv;
    private CourseEnvironment simCourseEnv;
    /*
     * default role is student
     */
    private boolean isGlobalAuthor = false;
    private boolean isGuestOnly = false;
    private boolean isCoach = false;
    private boolean isCourseAdmin = false;
    private String role = PreviewSettingsForm.ROLE_STUDENT;
    private final LayoutMain3ColsPreviewController previewLayoutCtr;
    private final ICourse course;

    /**
     * Constructor for the run main controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     * @param course
     *            the real course/courseEnvironment (this controller will generate a preview-Environment)
     */
    public PreviewConfigController(final UserRequest ureq, final WindowControl wControl, final ICourse course) {
        super(ureq, wControl);
        this.course = course;
        psf = new PreviewSettingsForm(ureq, wControl, course);
        listenTo(psf);

        configVc = createVelocityContainer("config");

        configVc.put("previewsettingsform", psf.getInitialComponent());
        // Use layout wrapper for proper display. Use col3 as main column
        previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, wControl, null, null, configVc, null);
        listenTo(previewLayoutCtr); // for later auto disposal

    }

    public void activate() {
        // init preview view
        previewLayoutCtr.activate();
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == prc && event.getCommand().equals("command.config")) {
            // use config form in preview controller
            previewLayoutCtr.setCol1(null);
            previewLayoutCtr.setCol2(null);
            previewLayoutCtr.setCol3(configVc);

        } else if (source == previewLayoutCtr && event == Event.BACK_EVENT) {
            fireEvent(ureq, Event.DONE_EVENT);

        } else if (source == psf && event == Event.DONE_EVENT) {
            // start preview as soon as we have valid values
            generateEnvironment();

            removeAsListenerAndDispose(prc);
            prc = new PreviewRunController(ureq, getWindowControl(), simIdentEnv, simCourseEnv, role, previewLayoutCtr);
            listenTo(prc);

        }
    }

    private void generateEnvironment() {
        final String sGroups = psf.getGroup();
        List groups;
        // only do a split if we really have something to split, otherwise we'll get
        // an empty object
        if (sGroups.length() == 0) {
            groups = new ArrayList();
        } else {
            groups = Arrays.asList(psf.getGroup().split(","));
        }

        final String sAreas = psf.getArea();
        List tmpAreas;
        // only do a split if we really have something to split, otherwise we'll get
        // an empty object
        if (sAreas.length() == 0) {
            tmpAreas = new ArrayList();
        } else {
            tmpAreas = Arrays.asList(psf.getArea().split(","));
        }

        // get learning areas for groups
        final Set areas = new HashSet();
        areas.addAll(tmpAreas);
        final ICourse course = CourseFactory.loadCourse(this.course);
        for (final Iterator iter = groups.iterator(); iter.hasNext();) {
            final String groupName = (String) iter.next();
            final List newAreas = course.getCourseEnvironment().getCourseGroupManager().getLearningAreasOfGroupFromAllContexts(groupName, course);
            for (final Iterator iterator = newAreas.iterator(); iterator.hasNext();) {
                final BGArea newArea = (BGArea) iterator.next();
                areas.add(newArea.getName());
            }
        }
        role = psf.getRole();
        // default is student
        isGlobalAuthor = false;
        isGuestOnly = false;
        isCoach = false;
        isCourseAdmin = false;
        /*
         * if (role.equals(PreviewSettingsForm.ROLE_STUDENT)) { } else
         */
        if (role.equals(PreviewSettingsForm.ROLE_GUEST)) {
            isGuestOnly = true;
        } else if (role.equals(PreviewSettingsForm.ROLE_COURSECOACH)) {
            isCoach = true;
        } else if (role.equals(PreviewSettingsForm.ROLE_COURSEADMIN)) {
            isCourseAdmin = true;
        } else if (role.equals(PreviewSettingsForm.ROLE_GLOBALAUTHOR)) {
            isGlobalAuthor = true;
        }

        final CourseGroupManager cgm = new PreviewCourseGroupManager(groups, new ArrayList(areas), isCoach, isCourseAdmin);
        final UserNodeAuditManager auditman = new PreviewAuditManager();
        final AssessmentManager am = new PreviewAssessmentManager();
        final CoursePropertyManager cpm = new PreviewCoursePropertyManager();

        simCourseEnv = new PreviewCourseEnvironment(course, psf.getDate(), cpm, cgm, auditman, am);
        simIdentEnv = new IdentityEnvironment();
        simIdentEnv.setRoles(new Roles(false, false, false, isGlobalAuthor, isGuestOnly, false, false));
        final Identity ident = new PreviewIdentity();
        simIdentEnv.setIdentity(ident);
        // identity must be set before attributes OLAT-4811
        simIdentEnv.setAttributes(psf.getAttributesMap());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
