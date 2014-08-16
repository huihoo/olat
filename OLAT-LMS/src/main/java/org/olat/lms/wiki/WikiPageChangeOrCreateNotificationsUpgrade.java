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

package org.olat.lms.wiki;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.olat.data.group.BusinessGroup;
import org.olat.data.notifications.Publisher;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.notifications.NotificationsUpgrade;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * update the publisher to business path
 * <P>
 * Initial Date: 6 jan. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class WikiPageChangeOrCreateNotificationsUpgrade implements NotificationsUpgrade {

    private static final Logger log = LoggerHelper.getLogger();

    protected WikiPageChangeOrCreateNotificationsUpgrade() {
    }

    @Autowired
    private BusinessGroupService businessGroupService;

    @Override
    public Publisher ugrade(final Publisher publisher) {
        String businessPath = publisher.getBusinessPath();
        if (businessPath != null && businessPath.startsWith("[")) {
            return null;
        }

        Long resId = publisher.getResId();
        if (publisher.getResName().equals(CourseModule.getCourseTypeName())) {
            // resId = CourseResourceableId p.getSubidentifier() = wikiCourseNode.getIdent()
            CourseNode courseNode = null;
            try {
                final ICourse course = CourseFactory.loadCourse(resId);
                final CourseEnvironment cenv = course.getCourseEnvironment();
                courseNode = cenv.getRunStructure().getNode(publisher.getSubidentifier());
            } catch (final Exception e) {
                Log.warn("Could not load course with resid: " + resId, e);
                return null;
            }
            if (courseNode == null) {
                log.info("deleting publisher with key; " + publisher.getKey(), null);
                // NotificationServiceProvider.getNotificationService().delete(publisher);
                // return nothing available
                return null;
            }
            final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
            resId = re.getOlatResource().getResourceableId();
            businessPath = "[RepositoryEntry:" + re.getKey().toString() + "]" + "[CourseNode:" + publisher.getSubidentifier() + "]";
        } else {
            // resName = 'BusinessGroup' or 'FileResource.WIKI'
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(publisher.getResName(), resId);
            final BusinessGroup bGroup = businessGroupService.loadBusinessGroup(resId, false);
            if (bGroup == null) {
                // Wiki as Repo-Ressource
                final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(ores, false);
                if (re != null) {
                    businessPath = "[RepositoryEntry:" + re.getKey().toString() + "]" + "[" + re.getOlatResource().getResourceableTypeName() + ":"
                            + re.getResourceableId() + "]";
                } else {
                    // repo entry not found, delete publisher
                    log.info("deleting publisher with key; " + publisher.getKey(), null);
                    // NotificationServiceProvider.getNotificationService().delete(publisher);
                    return null;
                }
            } else {
                businessPath = "[BusinessGroup:" + bGroup.getKey().toString() + "][wiki:0]";
            }
        }
        publisher.setBusinessPath(businessPath);
        return publisher;
    }

    @Override
    public String getType() {
        return "WikiPage";
    }
}
