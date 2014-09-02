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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.ProjectBrokerProjectDocument;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for project-broker course-node.
 * 
 * @author Christian Guretzki
 */
public class ProjectBrokerCourseNodeIndexer extends CourseNodeIndexer {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private static final String TYPE = "type.course.node.projectbroker";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.ProjectBrokerCourseNode";

    // private final static String TYPE_DROPBOX = "type.course.node.projectbroker.dropbox";
    // private final static String TYPE_RETURNBOX = "type.course.node.projectbroker.returnbox";

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE);

        // go further, index my projects
        final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
        final Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, courseNode);
        if (projectBrokerId != null) {
            final List<Project> projects = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId);
            for (final Iterator<Project> iterator = projects.iterator(); iterator.hasNext();) {
                final Project project = iterator.next();
                final Document document = ProjectBrokerProjectDocument.createDocument(courseNodeResourceContext, project);
                indexWriter.addDocument(document);
                doIndexFolders(courseNodeResourceContext, project, indexWriter);
            }
        } else {
            log.debug("projectBrokerId is null, courseNode=" + courseNode + " , course=" + course);
        }
    }

    private void doIndexFolders(final SearchResourceContext searchResourceContext, final Project project, final OlatFullIndexer indexWriter) throws IOException {
        log.debug("DOES NOT INDEX DROPBOX AND RETURNBOX");
        // RPOBLEM : How we could check access to the projects in checkAccess method (missing courseNode to get project-broker)
        // Index Dropbox
        // String dropboxFilePath = FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
        // File fDropboxFolder = new File(dropboxFilePath);
        // VFSContainer dropboxRootContainer = new LocalFolderImpl(fDropboxFolder);
        // projectResourceContext.setDocumentType(TYPE_DROPBOX);
        // doIndexVFSContainer(projectResourceContext, dropboxRootContainer, indexWriter, "");

        // Index Returnbox
        // String returnboxFilePath = FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), courseNode);
        // File fResturnboxFolder = new File(returnboxFilePath);
        // VFSContainer returnboxRootContainer = new LocalFolderImpl(fResturnboxFolder);
        // projectResourceContext.setDocumentType(TYPE_RETURNBOX);
        // doIndexVFSContainer(projectResourceContext, returnboxRootContainer, indexWriter, "");
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
        return true;
    }

}
