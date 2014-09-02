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
package org.olat.lms.search.indexer.repository;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.ScormCPFileResource;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Index the SCORM package
 * <P>
 * Initial Date: 11 déc. 2009 <br>
 * 
 * @author srosse
 */
public class ScormRepositoryIndexer extends SubLevelIndexer<RepositoryEntry> {

    private static final Logger log = LoggerHelper.getLogger();

    public final static String TYPE = "type.repository.entry.scorm";
    public final static String ORES_TYPE_SCORM = ScormCPFileResource.TYPE_NAME;

    @Override
    public String getSupportedTypeName() {
        return ORES_TYPE_SCORM;
    }

    @Override
    public void doIndex(final SearchResourceContext resourceContext, final RepositoryEntry repositoryEntry, final OlatFullIndexer indexWriter) throws IOException,
            FolderIndexerTimeoutException, DocumentException {
        if (log.isDebugEnabled()) {
            log.debug("Index Scorm package...");
        }

        final OLATResource ores = repositoryEntry.getOlatResource();
        final File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);

        resourceContext.setDocumentType(TYPE);
        resourceContext.setTitle(repositoryEntry.getDisplayname());
        // unescape HTML entities from rich text field input
        resourceContext.setDescription(StringHelper.unescapeHtml(repositoryEntry.getDescription()));
        resourceContext.setParentContextType(TYPE);
        resourceContext.setParentContextName(repositoryEntry.getDisplayname());
        ScormIndexerHelper.doIndex(resourceContext, indexWriter, cpRoot);
    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check for accessing RepoEntry is done in RepositoryIndexerMultiThreaded
        return true;
    }

}
