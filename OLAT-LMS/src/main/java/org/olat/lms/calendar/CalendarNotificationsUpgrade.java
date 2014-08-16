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

package org.olat.lms.calendar;

import org.apache.log4j.Logger;
import org.olat.data.notifications.Publisher;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.notifications.NotificationsUpgrade;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Upgrade publisher of calendars
 * <P>
 * Initial Date: 7 jan. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class CalendarNotificationsUpgrade implements NotificationsUpgrade {

    private static final Logger log = LoggerHelper.getLogger();

    protected CalendarNotificationsUpgrade() {
    }

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public Publisher ugrade(final Publisher publisher) {
        final String type = publisher.getResName();
        String businessPath = publisher.getBusinessPath();
        if (businessPath != null && businessPath.startsWith("[")) {
            return null;
        }

        if ("CalendarManager.course".equals(type)) {
            try {
                final ICourse course = CourseFactory.loadCourse(publisher.getResId());
                final RepositoryEntry re = repositoryService.lookupRepositoryEntry(course, true);
                businessPath = "[RepositoryEntry:" + re.getKey() + "]";
            } catch (final Exception e) {
                // if something went wrong, like error while loading course...
                log.error("error while processing resid: " + publisher.getResId(), e);
            }
        } else if ("CalendarManager.group".equals(type)) {
            businessPath = "[BusinessGroup:" + publisher.getResId() + "][action.calendar.group:0]";
        }

        if (businessPath != null) {
            publisher.setBusinessPath(businessPath);
            return publisher;
        }
        return null;
    }

    @Override
    public String getType() {
        return "CalendarManager";
    }
}
