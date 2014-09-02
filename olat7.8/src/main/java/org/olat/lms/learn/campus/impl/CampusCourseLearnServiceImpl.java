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
package org.olat.lms.learn.campus.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.core.course.campus.service.CampusCourseCoreService;
import org.olat.lms.learn.LearnBaseService;
import org.olat.lms.learn.campus.service.CampusCourseLearnService;
import org.olat.lms.learn.campus.service.SapCampusCourseTo;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CampusCourseLearnServiceImpl extends LearnBaseService implements CampusCourseLearnService {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CampusCourseCoreService campusCourseCoreService;

    @Override
    protected void setMetrics(List metrics) {
        // TODO metrics
    }

    @Override
    public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
        return campusCourseCoreService.checkDelegation(sapCampusCourseId, creator);
    }

    @Override
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) {
        return campusCourseCoreService.createCampusCourseFromTemplate(courseResourceableId, sapCampusCourseId, creator);
    }

    @Override
    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator) {
        return campusCourseCoreService.continueCampusCourse(courseResourceableId, sapCampusCourseId, courseTitle, creator);
    }

    @Override
    public List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType) {
        List<SapCampusCourseTo> courseList = new ArrayList<SapCampusCourseTo>();
        Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithoutResourceableId(identity, userType);
        for (Course sapCampusCourse : sapCampusCourses) {
            courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitle(), sapCampusCourse.getId(), null));
        }
        Collections.sort(courseList);
        return courseList;
    }

    @Override
    public List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapOlatUser.SapUserType userType) {
        List<SapCampusCourseTo> courseList = new ArrayList<SapCampusCourseTo>();
        Set<Course> sapCampusCourses = campusCourseCoreService.getCampusCoursesWithResourceableId(identity, userType);
        for (Course sapCampusCourse : sapCampusCourses) {
            courseList.add(new SapCampusCourseTo(sapCampusCourse.getTitle(), sapCampusCourse.getId(), sapCampusCourse.getResourceableId()));
        }
        Collections.sort(courseList);
        return courseList;
    }

    @Override
    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId) {
        return campusCourseCoreService.getRepositoryEntryFor(sapCourseId);
    }

    @Override
    public void createDelegation(Identity delegator, Identity delegatee) {
        campusCourseCoreService.createDelegation(delegator, delegatee);
    }

    @Override
    public boolean existDelegation(Identity delegator, Identity delegatee) {
        return campusCourseCoreService.existDelegation(delegator, delegatee);
    }

    @Override
    public boolean existResourceableId(Long resourceableId) {
        return campusCourseCoreService.existResourceableId(resourceableId);
    }

    public List<Long> getAllCreatedSapCourcesResourceableIds() {
        return campusCourseCoreService.getAllCreatedSapCourcesResourceableIds();
    }

    public List getDelegatees(Identity delegator) {
        return campusCourseCoreService.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        campusCourseCoreService.deleteDelegation(delegator, delegatee);
    }

}
