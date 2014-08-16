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

package org.olat.lms.course.nodes.ta;

import org.apache.log4j.Logger;
import org.olat.data.notifications.Publisher;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.notifications.NotificationsUpgrade;
import org.olat.lms.notifications.NotificationsUpgradeHelper;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrade publisher of dropbox
 * <P>
 * Initial Date: 7 jan. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class DropboxFileUploadNotificationsUpgrade implements NotificationsUpgrade {

    private static final Logger log = LoggerHelper.getLogger();

    protected DropboxFileUploadNotificationsUpgrade() {
    }

    @Override
    public Publisher ugrade(final Publisher publisher) {
        String businessPath = publisher.getBusinessPath();
        if (businessPath != null && businessPath.startsWith("[")) {
            return null;// already upgrade
        }

        final String type = publisher.getResName();
        if ("CourseModule".equals(type)) {
            final String courseNode = publisher.getSubidentifier();
            if (courseNode.indexOf(':') < 0) {
                businessPath = NotificationsUpgradeHelper.getCourseNodePath(publisher);
            } else {
                try {
                    final String courseNodeId = courseNode.substring(0, courseNode.indexOf(':'));
                    final Long resId = publisher.getResId();
                    final ICourse course = CourseFactory.loadCourse(resId);
                    final RepositoryService rm = RepositoryServiceImpl.getInstance();
                    final OLATResource rsrc = OLATResourceManager.getInstance().findResourceable(course.getResourceableId(), course.getResourceableTypeName());
                    final RepositoryEntry re = rm.lookupRepositoryEntry(rsrc, true);
                    businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + courseNodeId + "]";
                } catch (final Exception e) {
                    businessPath = null;
                    // if something went wrong, like error while loading course...
                    log.warn("error while processing resid: " + publisher.getResId(), e);
                }
            }
        }

        if (businessPath != null) {
            publisher.setBusinessPath(businessPath);
            return publisher;
        }
        return null;
    }

    @Override
    public String getType() {
        return "DropboxController";
    }
}
