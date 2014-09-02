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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for BC (content-package) course-node.
 * 
 * @author Christian Guretzki
 */
public class CPCourseNodeIndexer extends CourseNodeIndexer {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    private final static String TYPE = "type.course.node.cp";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.CPCourseNode";

    private static final Logger log = LoggerHelper.getLogger();

    private static final FolderIndexerAccess contentPackageAccess = new ContentPackageAccess();

    private int courseNodeCounter = 0;

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, FolderIndexerTimeoutException {
        if (log.isDebugEnabled()) {
            log.debug("Index Content Package... courseNodeCounter=" + courseNodeCounter++);
        }

        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE);
        courseNodeResourceContext.setTitle(course.getCourseTitle());

        final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
        if (re == null) {
            throw new AssertException("configurationkey 'CONFIG_KEY_REPOSITORY_SOFTKEY' of BB CP was missing");
        }
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
        if (cpRoot == null) {
            throw new AssertException("file of repository entry " + re.getKey() + "was missing");
        }

        final LocalFolderImpl rootContainer = new LocalFolderImpl(cpRoot);
        FolderIndexer.indexVFSContainer(courseNodeResourceContext, rootContainer, indexWriter, contentPackageAccess);
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

    private static final class ContentPackageAccess implements FolderIndexerAccess {

        @Override
        public boolean allowed(final VFSItem item) {
            return !item.getName().endsWith(".xsd");
        }
    }

}
