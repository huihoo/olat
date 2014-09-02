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
package org.olat.lms.core.course.campus.impl.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.DaoManager;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.course.campus.StudentCourse;
import org.olat.data.course.campus.StudentCoursePK;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.syncer.CampusCourseGroupSynchronizer;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.course.tree.PublishTreeModel;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 30.05.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class CourseCreateCoordinator {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CampusConfiguration campusConfiguration;
    @Autowired
    CourseDescriptionBuilder courseDescriptionBuilder;
    @Autowired
    CoursePublisher coursePublisher;
    @Autowired
    CourseTemplate courseTemplate;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    CampusCourseGroupSynchronizer campusCourseGroupSynchronizer;
    @Autowired
    OLATResourceManager olatResourceManager;
    @Autowired
    DaoManager daoManager;

    public CampusCourse continueCampusCourse(Long courseResourceableId, CampusCourse campusCourse, CampusCourseImportTO campusCourseImportData, Identity creator) {
        RepositoryEntry repositoryEntry = campusCourse.getRepositoryEntry();
        // TITLE
        String oldTitle = repositoryEntry.getDisplayname();
        String newTitle = campusCourseImportData.getTitle();
        String displayName = StringUtils.left(newTitle, 4).concat("/").concat(StringUtils.left(oldTitle, 4)).concat(StringUtils.substring(newTitle, 4));
        campusCourse.setTitle(displayName);
        // Description
        String campusCourseSemester = oldTitle.concat("<br>").concat(newTitle);
        campusCourse.setDescription(courseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, campusCourseSemester, campusCourseImportData.getLanguage()));

        repositoryService.updateDisplaynameDescriptionOfRepositoryEntry(campusCourse.getRepositoryEntry());

        List<StudentCourse> studentCourses = new ArrayList<StudentCourse>();
        for (Identity identity : campusCourseGroupSynchronizer.getCampusGroupAParticipants(campusCourse)) {
            SapOlatUser sapOlatUser = daoManager.getStudentSapOlatUserByOlatUserName(identity.getName());
            if (sapOlatUser != null) {
                if (daoManager.getStudentById(sapOlatUser.getSapUserId()) != null) {
                    StudentCourse studentCourse = new StudentCourse(new StudentCoursePK(sapOlatUser.getSapUserId(), campusCourseImportData.getSapCourseId()));
                    studentCourses.add(studentCourse);
                }
            }
        }

        campusCourse.getRepositoryEntry().setInitialAuthor(creator.getName());

        if (!studentCourses.isEmpty()) {
            daoManager.saveStudentCourses(studentCourses);
        }

        return campusCourse;
    }

    public CampusCourse createCampusCourse(Long courseResourceableId, CampusCourseImportTO campusCourseImportData, Identity creator) {
        final Long templateCourseResourceableId;

        boolean defaultTemplate = (courseResourceableId == null);
        if (defaultTemplate) {
            templateCourseResourceableId = campusConfiguration.getTemplateCourseResourcableId(campusCourseImportData.getLanguage());
            // THE CASE THAT NO TEMPLATE WAS FOUND
            if (templateCourseResourceableId == null) {
                return null;
            }
            final ICourse defaultTemplateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
            final PublishTreeModel publishTreeModel = new PublishTreeModel(defaultTemplateCourse.getEditorTreeModel(), defaultTemplateCourse.getRunStructure(), null);

            if (publishTreeModel.hasPublishableChanges()) {
                log.warn("Campuskurs template course " + defaultTemplateCourse.getCourseTitle() + " (" + defaultTemplateCourse.getResourceableId()
                        + ") is not published completely.");
                return null;
            }
        } else {
            templateCourseResourceableId = courseResourceableId;
        }

        CampusCourse campusCourse = null;
        try {
            // COPY THE CampusCourse FROM THE APPROPIRATE TEMPLATE (default or custom)
            campusCourse = courseTemplate.createCampusCourseFromTemplate(templateCourseResourceableId, creator);
            String lvLanguage = campusConfiguration.getTemplateLanguage(campusCourseImportData.getLanguage());
            campusCourse.setTranslator(new PackageTranslator(PackageUtil.getPackageName(this.getClass()), new Locale(lvLanguage)));
            campusCourse.setDefaultTemplate(defaultTemplate);
            campusCourse.setTitle(campusCourseImportData.getTitle());
            campusCourse.setCourseTitleAndLearningObjectivesInCourseModel(campusCourseImportData.getTitle());
            campusCourse.setDescription(courseDescriptionBuilder.buildDescriptionFrom(campusCourseImportData, lvLanguage));

            if (!defaultTemplate) {
                // CampusGroupHelper.addCampusGroups(campusCourse.getCourse());
                campusCourse.SetBusinessGroups(creator);
            }

            campusCourseGroupSynchronizer.addAllLecturesAsOwner(campusCourse, campusCourseImportData.getLecturers());
            campusCourseGroupSynchronizer.addDefaultCoOwnersAsOwner(campusCourse);
            campusCourseGroupSynchronizer.synchronizeCourseGroups(campusCourse.getCourse(), campusCourseImportData);
            repositoryService.saveRepositoryEntry(campusCourse.getRepositoryEntry());

            // ADD ADMIN RIGHTS TO OWNER GROUP
            // CoreSpringFactory.getBean(BaseSecurityEBL.class).createCourseAdminPolicy(campusCourse.getRepositoryEntry());
            final BaseSecurity securityManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            securityManager.createAndPersistPolicy(campusCourse.getRepositoryEntry().getOwnerGroup(), Constants.PERMISSION_ADMIN, campusCourse.getCourse());

            if (defaultTemplate) {
                // SET THE BARG
                campusCourse.setRepositoryAccessRights(RepositoryEntry.ACC_USERS_GUESTS);
                // PUBLISH THE CREATED CampusCourse
                coursePublisher.publish(campusCourse.getCourse(), creator);
            }

            return campusCourse;

        } catch (Exception ex) {
            // CLEAN UP TO ENSURE CONSISTENT STATE
            if (campusCourse != null) {
                if (campusCourse.getRepositoryEntry() != null) {
                    try {
                        repositoryService.deleteRepositoryEntryAndBasesecurity(campusCourse.getRepositoryEntry());
                    } catch (Exception e) {
                        // we tried best to delete entry - ignore exceptions during deletion
                    }
                }
                if (campusCourse.getCourse() != null) {
                    try {
                        olatResourceManager.deleteOLATResourceable(campusCourse.getCourse());
                    } catch (Exception e) {
                        // we tried best to delete entry - ignore exceptions during deletion
                    }
                }
            }
            return null;
        }
    }
}
