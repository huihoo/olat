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
package org.olat.lms.core.course.campus.impl.syncer;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.course.campus.DaoManager;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.syncer.statistic.SynchronizedGroupStatistic;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Synchronize the hole course with groups, title, description.
 * 
 * @author cg
 */
@Component
public class CourseSynchronizer {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CampusCourseGroupSynchronizer courseGroupSynchronizer;
    @Autowired
    DaoManager campusDaoManager;
    @Autowired
    CourseAttributeSynchronizer courseAttributeSynchronizer;
    @Autowired
    CampusConfiguration campusConfiguration;

    public SynchronizedGroupStatistic synchronizeCourse(CampusCourseImportTO sapCourse) {
        if (sapCourse != null) {
            long resourceableId = sapCourse.getOlatResourceableId();
            log.debug("synchronizeCourse sapCourseId=" + sapCourse.getSapCourseId() + "  resourceableId=" + resourceableId);
            ICourse course = CourseFactory.loadCourse(resourceableId);
            commitDBImplTransaction();
            log.debug("synchronizeCourse start for course=" + course.getCourseTitle());
            log.debug("synchronizeCourse Lecturer size=" + sapCourse.getLecturers().size());
            log.debug("synchronizeCourse Participants size=" + sapCourse.getParticipants().size());

            courseGroupSynchronizer.addAllLecturesAsOwner(course, sapCourse.getLecturers());
            // SynchronizedGroupStatistic groupStatistic = courseGroupSynchronizer.synchronizeCourseGroupsForStudentsOnly(course, sapCourse);
            SynchronizedGroupStatistic groupStatistic = courseGroupSynchronizer.synchronizeCourseGroups(course, sapCourse);
            commitDBImplTransaction();
            log.debug("synchronizeCourse statistic=" + groupStatistic);
            if (campusConfiguration.isSynchronizeTitleAndDescriptionEnabled()) {
                log.debug("SynchronizeTitleAndDescription is enabled");
                courseAttributeSynchronizer.synchronizeTitleAndDescription(sapCourse.getSapCourseId(), sapCourse);
                commitDBImplTransaction();
            }
            return groupStatistic;
        } else {
            return SynchronizedGroupStatistic.createEmptyStatistic();
        }
    }

    private void commitDBImplTransaction() {
        DBFactory.getInstance(false).intermediateCommit();
    }
}
