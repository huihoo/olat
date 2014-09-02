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

import java.io.File;
import java.io.IOException;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.ta.Dropbox_EBL;
import org.olat.lms.course.nodes.ta.Returnbox_EBL;
import org.olat.lms.course.nodes.ta.Solution_EBL;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Indexer for TA (task) course-node.
 * 
 * @author Christian Guretzki
 */
public class TACourseNodeIndexer extends CourseNodeIndexer {
    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private final static String TYPE = "type.course.node.ta";
    private final static String TYPE_TASK = "type.course.node.ta.task";
    private final static String TYPE_DROPBOX = "type.course.node.ta.dropbox";
    private final static String TYPE_RETURNBOX = "type.course.node.ta.returnbox";
    private final static String TYPE_SOLUTIONBOX = "type.course.node.ta.solutionbox";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.TACourseNode";

    private Solution_EBL solutionEbl;
    private Dropbox_EBL dropboxEbl;
    private Returnbox_EBL returnboxEbl;

    public TACourseNodeIndexer() {
        dropboxEbl = CoreSpringFactory.getBean(Dropbox_EBL.class);
        returnboxEbl = CoreSpringFactory.getBean(Returnbox_EBL.class);
        solutionEbl = CoreSpringFactory.getBean(Solution_EBL.class);
    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, FolderIndexerTimeoutException {

        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);

        // Index Task
        final File fTaskfolder = new File(FolderConfig.getCanonicalRoot() + TACourseNode.getTaskFolderPathRelToFolderRoot(course.getCourseEnvironment(), courseNode));
        final VFSContainer taskRootContainer = new LocalFolderImpl(fTaskfolder);
        courseNodeResourceContext.setDocumentType(TYPE_TASK);
        FolderIndexer.indexVFSContainer(courseNodeResourceContext, taskRootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);

        // Index Dropbox
        final String dropboxFilePath = FolderConfig.getCanonicalRoot() + dropboxEbl.getDropboxRootFolder(course.getCourseEnvironment(), courseNode);
        final File fDropboxFolder = new File(dropboxFilePath);
        final VFSContainer dropboxRootContainer = new LocalFolderImpl(fDropboxFolder);
        courseNodeResourceContext.setDocumentType(TYPE_DROPBOX);
        FolderIndexer.indexVFSContainer(courseNodeResourceContext, dropboxRootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);

        // Index Returnbox
        final String returnboxFilePath = FolderConfig.getCanonicalRoot() + returnboxEbl.getReturnboxRootFolder(course.getCourseEnvironment(), courseNode);
        final File fResturnboxFolder = new File(returnboxFilePath);
        final VFSContainer returnboxRootContainer = new LocalFolderImpl(fResturnboxFolder);
        courseNodeResourceContext.setDocumentType(TYPE_RETURNBOX);
        FolderIndexer.indexVFSContainer(courseNodeResourceContext, returnboxRootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);

        // Index Solutionbox
        final String solutionFilePath = FolderConfig.getCanonicalRoot() + solutionEbl.getSolutionRootFolder(course.getCourseEnvironment(), courseNode);
        final File fSolutionFolder = new File(solutionFilePath);
        final VFSContainer solutionRootContainer = new LocalFolderImpl(fSolutionFolder);
        courseNodeResourceContext.setDocumentType(TYPE_SOLUTIONBOX);
        FolderIndexer.indexVFSContainer(courseNodeResourceContext, solutionRootContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);
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
