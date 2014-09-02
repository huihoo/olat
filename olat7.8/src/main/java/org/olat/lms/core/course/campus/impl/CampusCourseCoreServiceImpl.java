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
package org.olat.lms.core.course.campus.impl;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.DaoManager;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.core.course.campus.impl.creator.CourseCreateCoordinator;
import org.olat.lms.core.course.campus.service.CampusCourseCoreService;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.portal.campus.CampusCourseEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
@Service
public class CampusCourseCoreServiceImpl implements CampusCourseCoreService {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    DaoManager daoManager;
    @Autowired
    CourseCreateCoordinator courseCreateCoordinator;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    CampusCourseFactory campusCourseFactory;

    @Override
    public boolean checkDelegation(Long sapCampusCourseId, Identity creator) {
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        for (Identity identity : campusCourseImportData.getLecturers()) {
            if (identity.getName().equalsIgnoreCase(creator.getName())) {
                return true;
            }
        }
        return false;

    }

    @Override
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator) {
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        if (campusCourseImportData.isOlatResourceableIdUndefined()) {
            return createCampusCourse(courseResourceableId, sapCampusCourseId, creator, campusCourseImportData);
        } else {
            return loadCampusCourse(sapCampusCourseId, campusCourseImportData.getOlatResourceableId());
        }
    }

    @Override
    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator) {
        daoManager.saveCampusCourseResoureableIdAndDisableSynchronization(sapCampusCourseId, courseResourceableId);
        CampusCourseImportTO campusCourseImportData = daoManager.getSapCampusCourse(sapCampusCourseId);
        CampusCourse campusCourse = loadCampusCourse(sapCampusCourseId, courseResourceableId);

        return courseCreateCoordinator.continueCampusCourse(courseResourceableId, campusCourse, campusCourseImportData, creator);

    }

    public CampusCourse createCampusCourse(Long resourceableId, Long sapCampusCourseId, Identity creator, CampusCourseImportTO campusCourseImportData) {
        CampusCourse campusCourse = courseCreateCoordinator.createCampusCourse(resourceableId, campusCourseImportData, creator);
        if (campusCourse != null) {
            daoManager.saveCampusCourseResoureableId(sapCampusCourseId, campusCourse.getRepositoryEntry().getOlatResource().getResourceableId());
        }
        return campusCourse;
    }

    public CampusCourse loadCampusCourse(Long sapCampusCourseId, Long resourceableId) {
        return campusCourseFactory.getCampusCourse(sapCampusCourseId, resourceableId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteResourceableIdReference(OLATResourceable res) {
        log.info("deleteResourceableIdReference for resourceableId=" + res.getResourceableId());
        daoManager.deleteResourceableId(res.getResourceableId());

        CoordinatorManager.getInstance().getCoordinator().getEventBus()
                .fireEventToListenersOf(new CampusCourseEvent(res.getResourceableId(), CampusCourseEvent.DELETED), OresHelper.lookupType(CampusCourse.class));
    }

    /**
     * @return Mapped RepositoryEntry or null when no could be found
     */
    @Override
    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId) {
        return campusCourseFactory.getRepositoryEntryFor(sapCourseId);
    }

    @Override
    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
        return daoManager.getCampusCoursesWithoutResourceableId(identity, userType);
    }

    @Override
    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType) {
        return daoManager.getCampusCoursesWithResourceableId(identity, userType);
    }

    @Override
    public void createDelegation(Identity delegator, Identity delegatee) {
        daoManager.saveDelegation(delegator, delegatee);
    }

    @Override
    public boolean existDelegation(Identity delegator, Identity delegatee) {
        return daoManager.existDelegation(delegator, delegatee);
    }

    @Override
    public boolean existResourceableId(Long resourceableId) {
        return daoManager.existResourceableId(resourceableId);
    }

    @Override
    public List<Long> getAllCreatedSapCourcesResourceableIds() {
        return daoManager.getAllCreatedSapCourcesResourceableIds();
    }

    public List getDelegatees(Identity delegator) {
        return daoManager.getDelegatees(delegator);
    }

    public void deleteDelegation(Identity delegator, Identity delegatee) {
        daoManager.deleteDelegation(delegator, delegatee);
    }

}
