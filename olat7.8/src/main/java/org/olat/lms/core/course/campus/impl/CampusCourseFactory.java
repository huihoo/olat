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

import org.apache.log4j.Logger;
import org.olat.data.course.campus.DaoManager;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 20.08.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampusCourseFactory {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    DaoManager daoManager;

    public CampusCourse getCampusCourse(Long sapCampusCourseId, Long resourceableId) {
        ICourse loadedCourse = CourseFactory.loadCourse(resourceableId);
        return new CampusCourse(loadedCourse, getRepositoryEntryFor(sapCampusCourseId));
    }

    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId) {
        CampusCourseImportTO campusCourseTo = daoManager.getSapCampusCourse(sapCourseId);
        log.debug("getRepositoryEntryFor sapCourseId=" + sapCourseId + "  campusCourseTo.getOlatResourceableId()=" + campusCourseTo.getOlatResourceableId());
        if (campusCourseTo.getOlatResourceableId() == null) {
            log.warn("getRepositoryEntryFor sapCourseId=" + sapCourseId + ": no OLAT course found");
            return null;
        }
        OLATResourceable loadedCourse = CourseFactory.loadCourse(campusCourseTo.getOlatResourceableId());
        RepositoryEntry sourceRepositoryEntry = repositoryService.lookupRepositoryEntry(loadedCourse, true);
        return sourceRepositoryEntry;
    }
}
