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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.course.assessment;

import org.apache.log4j.Logger;
import org.olat.data.notifications.Publisher;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseModule;
import org.olat.lms.notifications.NotificationsUpgrade;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrade publisher of assessments
 * <P>
 * Initial Date: 5 jan. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class AssessmentNotificationsUpgrade implements NotificationsUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    protected AssessmentNotificationsUpgrade() {
    }

    @Override
    public Publisher ugrade(final Publisher publisher) {
        String businessPath = publisher.getBusinessPath();
        if (businessPath != null && businessPath.startsWith("[")) {
            return null;
        }

        try {
            final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(
                    OresHelper.createOLATResourceableInstance(CourseModule.class, publisher.getResId()), true);
            businessPath = "[RepositoryEntry:" + re.getKey() + "]";
        } catch (final Exception e) {
            // if something went wrong...
            log.error("error while processing resid: " + publisher.getResId(), e);
        }

        if (businessPath != null) {
            publisher.setBusinessPath(businessPath);
            return publisher;
        }
        return null;
    }

    @Override
    public String getType() {
        return "AssessmentManager";
    }
}
