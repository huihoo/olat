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
package org.olat.lms.course.assessment;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * TODO: Class Description for AssessmentEBL
 * 
 * <P>
 * Initial Date: 06.09.2011 <br>
 * 
 * @author lavinia
 */
public class AssessmentEBL {

    private static final Logger log = LoggerHelper.getLogger();

    public void loadAssessmentCacheForGroupIdentities(List<Identity> identities, Map<Long, UserCourseEnvironment> localUserCourseEnvironmentCache,
            final OLATResourceable ores) {
        boolean success = false;
        try {
            final ICourse course = CourseFactory.loadCourse(ores);
            // 1) preload assessment cache with database properties
            long start = 0;
            if (log.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            course.getCourseEnvironment().getAssessmentManager().preloadCache();
            // 2) preload controller local user environment cache
            start = System.currentTimeMillis();

            for (final Iterator<Identity> iter = identities.iterator(); iter.hasNext();) {
                final Identity identity = iter.next();
                AssessmentHelper.wrapIdentity(identity, localUserCourseEnvironmentCache, course, null);
                if (Thread.interrupted()) {
                    break;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Preloading of user course environment cache for course::" + course.getResourceableId() + " for " + localUserCourseEnvironmentCache.size()
                        + " user course environments. Loading time::" + (System.currentTimeMillis() - start) + "ms");
            }
            // TODO: cg(04.09.2008): replace 'commit/closeSession' with doInManagedBlock
            // finished in this thread, close database session of this thread!
            DBFactory.getInstance(false).commitAndCloseSession();
            success = true;
        } finally {
            if (!success) {
                DBFactory.getInstance(false).rollbackAndCloseSession();
            }
        }
    }

}
