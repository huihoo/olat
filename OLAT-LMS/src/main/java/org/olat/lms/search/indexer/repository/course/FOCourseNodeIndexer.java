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
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
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
import org.olat.lms.search.indexer.ForumIndexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.CourseIndexer;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Indexer for FO (forum) course-node.
 * 
 * @author Christian Guretzki
 */
public class FOCourseNodeIndexer extends ForumIndexer implements CourseNodeIndexer {
    private static final Logger log = LoggerHelper.getLogger();

    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE = "type.course.node.forum.message";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.FOCourseNode";

    private final CourseIndexer courseNodeIndexer;

    public FOCourseNodeIndexer() {
        courseNodeIndexer = new CourseIndexer();
    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter) {
        try {
            final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
            courseNodeResourceContext.setBusinessControlFor(courseNode);
            courseNodeResourceContext.setTitle(courseNode.getShortTitle());
            courseNodeResourceContext.setDescription(courseNode.getLongTitle());
            doIndexForum(courseNodeResourceContext, course, courseNode, indexWriter);
            // go further, index my child nodes
            courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
        } catch (final Exception ex) {
            log.error("Exception indexing courseNode=" + courseNode, ex);
        } catch (final Error err) {
            log.error("Error indexing courseNode=" + courseNode, err);
        }
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final ContextEntry ce = businessControl.popLauncherContextEntry();
        final Long resourceableId = ce.getOLATResourceable().getResourceableId();
        final Message message = getForumService().loadMessage(resourceableId);
        Message threadtop = message.getThreadtop();
        if (threadtop == null) {
            threadtop = message;
        }
        final boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden();
        // assumes that if is owner then is moderator so it is allowed to see the hidden forum threads
        // TODO: (LD) fix this!!! - the contextEntry is not the right context for this check
        final boolean isOwner = getBaseSecurity().isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, contextEntry.getOLATResourceable());
        if (isMessageHidden && !isOwner) {
            return false;
        }
        return true;
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
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
            throws IOException, InterruptedException {
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
            doIndexAllMessages(parentResourceContext, forum, indexWriter);
        }
    }

}
