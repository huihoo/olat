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
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.CourseIndexer;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Indexer for BC (content-package) course-node.
 * 
 * @author Christian Guretzki
 */
public class CPCourseNodeIndexer extends FolderIndexer implements CourseNodeIndexer {

    private static final Logger log = LoggerHelper.getLogger();
    // Must correspond with LocalString_xx.properties
    // Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_'
    public final static String TYPE = "type.course.node.cp";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.CPCourseNode";

    private final CourseIndexer courseNodeIndexer;

    private int courseNodeCounter = 0;

    public CPCourseNodeIndexer() {
        courseNodeIndexer = new CourseIndexer();

    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Index Content Package... courseNodeCounter=" + courseNodeCounter++);
        }

        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(TYPE);

        final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
        if (re == null) {
            throw new AssertException("configurationkey 'CONFIG_KEY_REPOSITORY_SOFTKEY' of BB CP was missing");
        }
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
        if (cpRoot == null) {
            throw new AssertException("file of repository entry " + re.getKey() + "was missing");
        }

        final VFSContainer rootContainer = new LocalFolderImpl(cpRoot);
        doIndexVFSContainer(courseNodeResourceContext, rootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);

        // go further, index my child nodes
        courseNodeIndexer.doIndexCourse(repositoryResourceContext, course, courseNode, indexWriter);
    }

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        return true;
    }

}
