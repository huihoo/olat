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
package org.olat.lms.search.indexer.repository.course;

import java.io.File;
import java.io.IOException;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.repository.ScormIndexerHelper;

/**
 * Description:<br>
 * Index SCORM package in course
 * <P>
 * Initial Date: 11 dec. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com
 */
public class ScormCourseNodeIndexer extends CourseNodeIndexer {

    private final static String NODE_TYPE = "type.course.node.scorm";

    private final static String SUPPORTED_TYPE_NAME = "org.olat.lms.course.nodes.ScormCourseNode";

    @Override
    public String getSupportedTypeName() {
        return SUPPORTED_TYPE_NAME;
    }

    @Override
    public void doIndex(final SearchResourceContext repositoryResourceContext, final ICourse course, final CourseNode courseNode, final OlatFullIndexer indexWriter)
            throws IOException, FolderIndexerTimeoutException, DocumentException {
        final SearchResourceContext courseNodeResourceContext = new SearchResourceContext(repositoryResourceContext);
        courseNodeResourceContext.setBusinessControlFor(courseNode);
        courseNodeResourceContext.setDocumentType(NODE_TYPE);
        courseNodeResourceContext.setTitle(course.getCourseTitle());

        final RepositoryEntry repoEntry = courseNode.getReferencedRepositoryEntry();
        final OLATResource ores = repoEntry.getOlatResource();
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);

        ScormIndexerHelper.doIndex(courseNodeResourceContext, indexWriter, cpRoot);
    }

    @Override
    public String getDocumentTypeName() {
        return NODE_TYPE;
    }

    @Override
    public boolean checkAccess(ContextEntry courseNodeContextEntry, BusinessControl businessControl, Identity identity, Roles roles, boolean isCourseOwner) {
        return true;
    }
}
