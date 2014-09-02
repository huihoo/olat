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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
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
import org.olat.lms.repository.RepositoryEntryStatus;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.CourseNodeDocument;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.search.indexer.repository.course.CourseNodeIndexer;
import org.olat.lms.security.IdentityEnvironment;
import org.olat.presentation.course.run.navigation.NavigationHandler;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index a whole course.
 * 
 * @author Christian Guretzki
 */
public class CourseIndexer extends SubLevelIndexer<RepositoryEntry> {

    public static final String TYPE = "type.repository.entry.CourseModule";

    private static final Logger log = LoggerHelper.getLogger();

    private final RepositoryService repositoryManager;

    private final Map<String, CourseNodeIndexer> indexerMap = new HashMap<String, CourseNodeIndexer>();

    public CourseIndexer() {
        repositoryManager = RepositoryServiceImpl.getInstance();
    }

    @Override
    public String getSupportedTypeName() {
        return CourseModule.getCourseTypeName();
    }

    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final RepositoryEntry repositoryEntry, final OlatFullIndexer indexWriter) throws IOException,
            DocumentException, FolderIndexerTimeoutException {
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

            // OLAT-6885: don't index Ablageordner
            // FolderIndexer.indexVFSContainer(parentResourceContext, course.getCourseBaseContainer(), indexWriter, FolderIndexerAccess.FULL_ACCESS);

            // course.getCourseTitle(); // do not index title => index root-node
            parentResourceContext.setParentContextType(TYPE);
            parentResourceContext.setParentContextName(course.getCourseTitle());
            doIndexCourseNode(parentResourceContext, course, course.getRunStructure().getRootNode(), indexWriter);
        } finally {
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private void doIndexCourseNode(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode,
            final OlatFullIndexer indexWriter) throws IOException, FolderIndexerTimeoutException, DocumentException {
        // loop over all child nodes
        final int childCount = courseNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final INode childNode = courseNode.getChildAt(i);
            if (childNode instanceof CourseNode) {
                final CourseNode childCourseNode = (CourseNode) childNode;
                if (log.isDebugEnabled()) {
                    log.debug("Analyse CourseNode child ... childCourseNode=" + childCourseNode);
                }

                final CourseNodeIndexer courseNodeIndexer = getCourseNodeIndexer(childCourseNode);
                if (courseNodeIndexer == null) {
                    // index just basic data (short title, long title, description) for course node without a specific indexer
                    indexCourseNodeBasicData(repositoryResourceContext, childCourseNode, indexWriter, null);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("courseNodeIndexer=" + courseNodeIndexer);
                    }

                    try {
                        // index basic and other data with specific course node indexer
                        indexCourseNodeBasicData(repositoryResourceContext, childCourseNode, indexWriter, courseNodeIndexer.getDocumentTypeName());
                        courseNodeIndexer.doIndex(repositoryResourceContext, course, childCourseNode, indexWriter);
                    } catch (final IOException ex) {
                        log.error(
                                "Can not index course node '" + courseNode.getLongTitle() + "' [course='" + course.getCourseTitle() + "', id="
                                        + course.getResourceableId() + "]", ex);
                        throw ex;
                    } catch (final FolderIndexerTimeoutException ex) {
                        log.error(
                                "Folder timeout indexing course node '" + courseNode.getLongTitle() + "' [course='" + course.getCourseTitle() + "', id="
                                        + course.getResourceableId() + "]", ex);
                        throw ex;
                    } catch (DocumentException ex) {
                        log.error(
                                "Can not create document in course node '" + courseNode.getLongTitle() + "' [course='" + course.getCourseTitle() + "', id="
                                        + course.getResourceableId() + "]", ex);
                        throw ex;
                    }
                }

                // go further, index my child nodes
                doIndexCourseNode(repositoryResourceContext, course, childCourseNode, indexWriter);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("ChildNode is no CourseNode, " + childNode);
                }
            }
        }
    }

    private void indexCourseNodeBasicData(SearchResourceContext parentResourceContext, CourseNode courseNode, OlatFullIndexer indexWriter, String documentType)
            throws IOException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(parentResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        if (documentType != null && !documentType.trim().isEmpty()) {
            courseNodeResourceContext.setDocumentType(documentType);
        }
        final Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
        indexWriter.addDocument(document);
    }

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setCourseNodeIndexerList(final List<CourseNodeIndexer> indexerList) {
        if (indexerList == null) {
            throw new AssertException("null value for indexerList not allowed.");
        }

        try {
            for (CourseNodeIndexer courseNodeIndexer : indexerList) {
                indexerMap.put(courseNodeIndexer.getSupportedTypeName(), courseNodeIndexer);
            }
        } catch (final ClassCastException cce) {
            throw new StartupException("Configured indexer is not of type RepositoryEntryIndexer", cce);
        }
    }

    private CourseNodeIndexer getCourseNodeIndexer(CourseNode courseNode) {
        return indexerMap.get(courseNode.getClass().getName());
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        final ContextEntry bcContextEntry = businessControl.popLauncherContextEntry();
        if (bcContextEntry == null) {
            // no context-entry anymore, the repository entry itself is the context entry,
            // not a course node of course we have access to the course metadata
            return true;
        }

        // in case we navigate within a folder
        if (bcContextEntry.getOLATResourceable().getResourceableTypeName().startsWith("path")) {
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
        final NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval, true);
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
            final CourseNodeIndexer courseNodeIndexer = getCourseNodeIndexer(courseNode);
            if (courseNodeIndexer == null) {
                return true;
            }
            // check specific course node indexer if defined
            final boolean isOwner = repositoryManager.isOwnerOfRepositoryEntry(identity, repositoryEntry);
            return courseNodeIndexer.checkAccess(bcContextEntry, businessControl, identity, roles, isOwner);
        } else {
            return false;
        }
    }

}
