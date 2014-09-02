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

package org.olat.lms.search.indexer.repository.course;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.FOCourseNode;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.Status;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.ForumIndexerHelper;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Indexer for FO (forum) course-node.
 * 
 * @author Christian Guretzki
 */
public class FOCourseNodeIndexer extends CourseNodeIndexer {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private final static String TYPE = "type.course.node.forum.message";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.FOCourseNode";

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        doIndexForum(courseNodeResourceContext, course, courseNode, indexWriter);
    }

    @Override
    public String getDocumentTypeName() {
        return TYPE;
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        final ContextEntry messageContextEntry = businessControl.popLauncherContextEntry();
        // we have no nested (message) context => apply access rules for course node
        if (messageContextEntry == null) {
            return true;
        }

        // apply access rules for message
        final Long resourceableId = messageContextEntry.getOLATResourceable().getResourceableId();
        final Message message = getForumService().loadMessage(resourceableId);
        Message threadtop = message.getThreadtop();
        if (threadtop == null) {
            threadtop = message;
        }
        final boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden();
        if (isMessageHidden) {
            // assumes that if is owner then is moderator so it is allowed to see the hidden forum threads
            return isCourseOwner;
        }

        return true;
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");
    }

    /**
     * Index a forum in a course.
     * 
     * @param parentResourceContext
     * @param course
     * @param courseNode
     * @param indexWriter
     * @throws IOException
     */
    private void doIndexForum(final SearchResourceContext parentResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Index Course Forum...");
        }
        final ForumService fom = getForumService();
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();

        final PropertyImpl forumKeyProperty = cpm.findCourseNodeProperty(courseNode, null, null, FOCourseNode.FORUM_KEY);
        // Check if forum-property exist
        if (forumKeyProperty != null) {
            final Long forumKey = forumKeyProperty.getLongValue();
            final Forum forum = fom.loadForum(forumKey);
            // SearchResourceContext forumSearchResourceContext = new SearchResourceContext(parentResourceContext);
            // forumSearchResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLFORUM); // TODO:chg: Must be an other Class e.g.
            // CourseRunMainController
            parentResourceContext.setDocumentType(TYPE);
            parentResourceContext.setDocumentContext(course.getResourceableId() + " " + courseNode.getIdent() + " " + forumKey);
            ForumIndexerHelper.doIndexAllMessages(parentResourceContext, forum, indexWriter, true);
        }
    }

}
