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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.course.ICourse;
import org.olat.lms.group.learn.CourseGroupManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author cg
 */
public class CampusGroupHelper {
    private static final Logger log = LoggerHelper.getLogger();

    public static BusinessGroup lookupCampusGroup(ICourse course, String campusGruppe) {
        CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
        // TODO: Possible problem to many groups => Solution : lookup only in default context
        List foundCampusGroups = courseGroupManager.getLearningGroupsFromAllContexts(campusGruppe, course);
        if (foundCampusGroups.isEmpty()) {
            log.error("Found no course-group with name=" + campusGruppe);
            throw new AssertException("Found no course-group with name=" + campusGruppe);
        }
        if (foundCampusGroups.size() > 1) {
            log.error("Found more than one course-group with name=" + campusGruppe);
            throw new AssertException("Found more than one course-group with name=" + campusGruppe);
        }
        return (BusinessGroup) foundCampusGroups.get(0);
    }

}
