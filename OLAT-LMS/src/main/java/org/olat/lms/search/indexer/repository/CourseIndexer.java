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

package org.olat.lms.search.indexer.repository;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.TreeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryStatus;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.Indexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.course.CourseNodeIndexer;
import org.olat.lms.search.indexer.repository.course.CourseNodeIndexerFactory;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.course.run.navigation.NavigationHandler;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index a hole course.
 * 
 * @author Christian Guretzki
 */
public class CourseIndexer implements Indexer {
    private static final Logger log = LoggerHelper.getLogger();

    public final static String TYPE = "type.repository.entry.CourseModule";

    private final RepositoryService repositoryManager;

    public CourseIndexer() {
        repositoryManager = RepositoryServiceImpl.getInstance();
    }

    /**
	 * 
	 */
    @Override
    public String getSupportedTypeName() {
        return CourseModule.getCourseTypeName();
    }

    /**
	 */

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final Object parentObject, final OlatFullIndexer indexWriter) {
        final RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
        if (log.isDebugEnabled()) {
            log.debug("Analyse Course... repositoryEntry=" + repositoryEntry);
        }
        try {
            final RepositoryEntryStatus status = RepositoryServiceImpl.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode());
            if (status.isClosed()) {
                if (log.isDebugEnabled()) {
                    log.debug("Course not indexed because it's closed: repositoryEntry=" + repositoryEntry);
                }
                return;
            }

            final ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
            // course.getCourseTitle(); // do not index title => index root-node
            parentResourceContext.setParentContextType(TYPE);
            parentResourceContext.setParentContextName(course.getCourseTitle());
            doIndexCourse(parentResourceContext, course, course.getRunStructure().getRootNode(), indexWriter);
        } catch (final Exception ex) {
            log.warn("Can not index repositoryEntry=" + repositoryEntry, ex);
        }
    }

    public void doIndexCourse(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, InterruptedException {
        // loop over all child nodes
        final int childCount = courseNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final INode childCourseNode = courseNode.getChildAt(i);
            if (childCourseNode instanceof CourseNode) {
                if (log.isDebugEnabled()) {
                    log.debug("Analyse CourseNode child ... childCourseNode=" + childCourseNode);
                }
                // go further with resource
                final CourseNodeIndexer courseNodeIndexer = CourseNodeIndexerFactory.getInstance().getCourseNodeIndexer((CourseNode) childCourseNode);
                if (courseNodeIndexer != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("courseNodeIndexer=" + courseNodeIndexer);
                    }
                    try {
                        courseNodeIndexer.doIndex(repositoryResourceContext, course, (CourseNode) childCourseNode, indexWriter);
                    } catch (final Exception e) {
                        log.warn("Can not index course node=" + childCourseNode.getIdent(), e);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No CourseNodeIndexer for " + childCourseNode);
                    }
                    // go further, index my child nodes
                    doIndexCourse(repositoryResourceContext, course, (CourseNode) childCourseNode, indexWriter);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("ChildNode is no CourseNode, " + childCourseNode);
                }
            }
        }
    }

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List<CourseNodeIndexer> indexerList) {
        if (indexerList == null) {
            throw new AssertException("null value for indexerList not allowed.");
        }

        try {
            for (final CourseNodeIndexer courseNodeIndexer : indexerList) {
                CourseNodeIndexerFactory.getInstance().registerIndexer(courseNodeIndexer);
                if (log.isDebugEnabled()) {
                    log.debug("Adding indexer from configuraton: ");
                }
            }
        } catch (final ClassCastException cce) {
            throw new StartupException("Configured indexer is not of type RepositoryEntryIndexer", cce);
        }
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final ContextEntry bcContextEntry = businessControl.popLauncherContextEntry();
        if (bcContextEntry == null) {
            // no context-entry anymore, the repository entry itself is the context entry,
            // not a course node of course we have access to the course metadata
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Start identity=" + identity + "  roles=" + roles);
        }
        final Long repositoryKey = contextEntry.getOLATResourceable().getResourceableId();
        final RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryKey);
        if (log.isDebugEnabled()) {
            log.debug("repositoryEntry=" + repositoryEntry);
        }

        final Long nodeId = bcContextEntry.getOLATResourceable().getResourceableId();
        if (log.isDebugEnabled()) {
            log.debug("nodeId=" + nodeId);
        }

        final ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
        final IdentityEnvironment ienv = new IdentityEnvironment();
        ienv.setIdentity(identity);
        ienv.setRoles(roles);
        final UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
        if (log.isDebugEnabled()) {
            log.debug("userCourseEnv=" + userCourseEnv + "ienv=" + ienv);
        }

        final CourseNode rootCn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();

        final String nodeIdS = nodeId.toString();
        final CourseNode courseNode = course.getRunStructure().getNode(nodeIdS);
        if (log.isDebugEnabled()) {
            log.debug("courseNode=" + courseNode);
        }

        final TreeEvaluation treeEval = new TreeEvaluation();
        final NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval);
        if (log.isDebugEnabled()) {
            log.debug("rootNodeEval=" + rootNodeEval);
        }

        final TreeNode newCalledTreeNode = treeEval.getCorrespondingTreeNode(courseNode);
        if (newCalledTreeNode == null) {
            // TreeNode no longer visible
            return false;
        }
        // go further
        final NodeEvaluation nodeEval = (NodeEvaluation) newCalledTreeNode.getUserObject();
        if (log.isDebugEnabled()) {
            log.debug("nodeEval=" + nodeEval);
        }
        if (nodeEval.getCourseNode() != courseNode) {
            throw new AssertException("error in structure");
        }
        if (!nodeEval.isVisible()) {
            throw new AssertException("node eval not visible!!");
        }
        if (log.isDebugEnabled()) {
            log.debug("call mayAccessWholeTreeUp...");
        }
        final boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(nodeEval);
        if (log.isDebugEnabled()) {
            log.debug("call mayAccessWholeTreeUp=" + mayAccessWholeTreeUp);
        }

        if (mayAccessWholeTreeUp) {
            final CourseNodeIndexer courseNodeIndexer = CourseNodeIndexerFactory.getInstance().getCourseNodeIndexer(courseNode);
            return courseNodeIndexer.checkAccess(bcContextEntry, businessControl, identity, roles);
        } else {
            return false;
        }
    }

}
