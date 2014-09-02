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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.indexer.FolderIndexer;
import org.olat.lms.search.indexer.FolderIndexerAccess;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index a repository entry of type shared folder.
 * 
 * @author Christian Guretzki
 */
public class SharedFolderRepositoryIndexer extends SubLevelIndexer<RepositoryEntry> {

    // Must correspond with org/olat/presentation/search/_i18n/LocalStrings_xx.properties
    // Do not use '_' because Lucene has problems with it
    public final static String TYPE = "type.repository.entry.sharedfolder";

    public final static String ORES_TYPE_SHAREDFOLDER = SharedFolderFileResource.TYPE_NAME;

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public String getSupportedTypeName() {
        return ORES_TYPE_SHAREDFOLDER;
    }

    @Override
    public void doIndex(final SearchResourceContext resourceContext, final RepositoryEntry repositoryEntry, final OlatFullIndexer indexWriter) throws IOException,
            FolderIndexerTimeoutException {
        if (log.isDebugEnabled()) {
            log.debug("Analyse Shared Folder RepositoryEntry...");
        }

        resourceContext.setDocumentType(TYPE);
        resourceContext.setParentContextType(TYPE);
        resourceContext.setParentContextName(repositoryEntry.getDisplayname());

        final OlatRootFolderImpl sfContainer = SharedFolderManager.getInstance().getSharedFolder(repositoryEntry.getOlatResource());
        FolderIndexer.indexVFSContainer(resourceContext, sfContainer, indexWriter, FolderIndexerAccess.FULL_ACCESS);

    }

    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        // check for accessing RepoEntry is done in RepositoryIndexerMultiThreaded
        return true;
    }

}
