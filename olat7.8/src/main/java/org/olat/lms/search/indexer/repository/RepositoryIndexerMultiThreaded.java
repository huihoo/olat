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
import org.apache.lucene.document.Document;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.search.SearchModule;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.document.RepositoryEntryDocument;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.indexer.FolderIndexerTimeoutException;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.lms.search.indexer.SubLevelIndexer;
import org.olat.lms.search.indexer.TopLevelIndexerMultiThreaded;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index the whole OLAT-repository with a multithreaded Executor thread pool.
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class RepositoryIndexerMultiThreaded extends TopLevelIndexerMultiThreaded {

    private static final Logger log = LoggerHelper.getLogger();

    private final RepositoryService repositoryService;

    private final List<Long> repositoryBlackList;

    private List<RepositoryEntry> repositoryList;

    /**
     * [used by spring]
     * 
     * @param repositoryManager
     */
    protected RepositoryIndexerMultiThreaded(final RepositoryService repositoryService, final SearchModule searchModule) {
        this.repositoryService = repositoryService;
        this.repositoryBlackList = searchModule.getRepositoryBlackList();
    }

    @Override
    public String getSupportedTypeName() {
        return OresHelper.calculateTypeName(RepositoryEntry.class);
    }

    @Override
    protected void doIndexing(final OlatFullIndexer indexWriter) {
        super.doIndexing(indexWriter);
        repositoryList = loadRepoEntriesList();

        status = IndexerStatus.RUNNING;

        for (RepositoryEntry repoEntry : repositoryList) {
            threadPool.executeIndexerTask(new RepositoryIndexerTask(indexWriter, repoEntry.getKey()));
        }

        status = threadPool.waitForCompletion(indexWriter.getTimeoutSeconds());
    }

    @Override
    protected int getNumberOfItemsToBeIndexed() {
        return repositoryList.size();
    }

    @Override
    protected String getThreadPoolName() {
        return "RepositoryIndexer";
    }

    /**
     * org.olat.data.basesecurity.Identity, org.olat.data.basesecurity.Roles)
     */
    @Override
    public boolean checkAccess(final ContextEntry contextEntry, final BusinessControl businessControl, final Identity identity, final Roles roles) {
        if (log.isDebugEnabled()) {
            log.debug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
        }
        final Long repositoryKey = contextEntry.getOLATResourceable().getResourceableId();
        final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(repositoryKey);
        if (repositoryEntry != null) {
            final boolean isOwner = repositoryService.isOwnerOfRepositoryEntry(identity, repositoryEntry);
            boolean isAllowedToLaunch = false;
            if (!isOwner) {
                if (repositoryEntry.getOwnerGroup() == null) {
                    // FIXME:chg: Inconsistent RepositoryEntry without owner-group, should not exit => Workaround no access
                    return false;
                }
                isAllowedToLaunch = repositoryService.isAllowedToLaunch(identity, roles, repositoryEntry);
            }
            if (log.isDebugEnabled()) {
                log.debug("isOwner=" + isOwner + "  isAllowedToLaunch=" + isAllowedToLaunch);
            }
            if (isOwner || isAllowedToLaunch) {
                final SubLevelIndexer<RepositoryEntry> repositoryEntryIndexer = getSubIndexer(repositoryEntry.getOlatResource().getResourceableTypeName());
                if (log.isDebugEnabled()) {
                    log.debug("repositoryEntryIndexer=" + repositoryEntryIndexer);
                }
                if (repositoryEntryIndexer != null) {
                    return repositoryEntryIndexer.checkAccess(contextEntry, businessControl, identity, roles);
                } else {
                    // No Indexer => no access
                    return false;
                }
            } else {
                return false;
            }
        } else {
            log.warn("Can not found RepositoryEntry with key=" + repositoryKey);
            return false;
        }
    }

    private List<RepositoryEntry> loadRepoEntriesList() {
        try {
            final Roles roles = new Roles(true, true, true, true, false, true, false);
            final List<RepositoryEntry> repositoryList = repositoryService.genericANDQueryWithRolesRestriction(null, null, null, null, roles, null);
            if (log.isDebugEnabled()) {
                log.debug("RepositoryIndexer repositoryList.size=" + repositoryList.size());
            }
            return repositoryList;
        } finally {
            // committing here to make sure repository entries get reloaded
            // from the database and not only from the session cache
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private void indexRepoEntry(Long key, OlatFullIndexer indexWriter) throws IOException, DocumentException, FolderIndexerTimeoutException {
        try {
            // reload the repositoryEntry here before indexing it to make sure it has not been deleted in the meantime
            final RepositoryEntry repositoryEntry = repositoryService.lookupRepositoryEntry(key);
            if (repositoryEntry == null) {
                log.info("doIndex: repositoryEntry was deleted while we were indexing. The deleted repositoryEntry was: " + repositoryEntry);
                return;
            }

            if (isOnBlacklist(repositoryEntry.getOlatResource().getResourceableId())) {
                log.warn("RepositoryEntry is on black-list and excluded from search-index , repositoryEntry=" + repositoryEntry);
                return;
            }

            // index generic repository entry data
            final SearchResourceContext searchResourceContext = new SearchResourceContext();
            searchResourceContext.setBusinessControlFor(repositoryEntry);
            final Document document = RepositoryEntryDocument.createDocument(searchResourceContext, repositoryEntry);
            indexWriter.addDocument(document);
            // Pass created-date & modified-date in context to child indexer because the child have no dates
            // TODO:chg: Check ob courseNode keine Daten hat
            searchResourceContext.setLastModified(repositoryEntry.getLastModified());
            searchResourceContext.setCreatedDate(repositoryEntry.getCreationDate());

            // index repository entry by specific sub indexer
            final SubLevelIndexer<RepositoryEntry> repositoryEntryIndexer = getSubIndexer(repositoryEntry.getOlatResource().getResourceableTypeName());
            if (repositoryEntryIndexer != null) {
                repositoryEntryIndexer.doIndex(searchResourceContext, repositoryEntry, indexWriter);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No RepositoryEntryIndexer for " + repositoryEntry.getOlatResource());
                }
            }
        } finally {
            DBFactory.getInstance(false).commitAndCloseSession();
        }
    }

    private boolean isOnBlacklist(final Long key) {
        return repositoryBlackList.contains(key);
    }

    private final class RepositoryIndexerTask implements Runnable {

        private final OlatFullIndexer indexWriter;

        private final Long repoEntryId;

        private RepositoryIndexerTask(final OlatFullIndexer indexWriter, final Long repoEntryId) {
            this.indexWriter = indexWriter;
            this.repoEntryId = repoEntryId;
        }

        @Override
        public void run() {
            try {
                indexingItemStarted(repoEntryId);
                indexRepoEntry(repoEntryId, indexWriter);
                indexingItemFinished(repoEntryId);
            } catch (FolderIndexerTimeoutException ex) {
                indexingItemTimedOut(repoEntryId);
                log.warn("Timeout indexing RepositoryEntry: " + repoEntryId);
            } catch (Exception ex) {
                indexingItemFailed(repoEntryId, ex);
                log.warn("Error indexing RepositoryEntry: " + repoEntryId, ex);
            }
        }
    }

}
