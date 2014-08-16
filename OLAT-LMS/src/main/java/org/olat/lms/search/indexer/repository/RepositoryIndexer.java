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
import java.util.Iterator;
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
import org.olat.lms.search.indexer.Indexer;
import org.olat.lms.search.indexer.OlatFullIndexer;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Index the whole OLAT-repository.
 * 
 * @author Christian Guretzki
 */
public class RepositoryIndexer implements Indexer {

    private static final Logger log = LoggerHelper.getLogger();

    private final RepositoryService repositoryManager;
    private final List<Long> repositoryBlackList;

    /**
     * [used by spring]
     * 
     * @param repositoryManager
     */
    private RepositoryIndexer(final RepositoryService repositoryManager, final SearchModule searchModule) {
        this.repositoryManager = repositoryManager;
        this.repositoryBlackList = searchModule.getRepositoryBlackList();
    }

    /**
     * Loops over all repository-entries. Index repository meta data. Go further with repository-indexer for certain type if available.
     * 
     * org.olat.lms.search.indexer.OlatFullIndexer)
     */
    @Override
    public void doIndex(final SearchResourceContext parentResourceContext, final Object businessObj, final OlatFullIndexer indexWriter) throws IOException,
            InterruptedException {
        final Roles roles = new Roles(true, true, true, true, false, true, false);
        int counter = 0;
        final List repositoryList = repositoryManager.genericANDQueryWithRolesRestriction(null, null, null, null, roles, null);
        if (log.isDebugEnabled()) {
            log.debug("RepositoryIndexer repositoryList.size=" + repositoryList.size());
        }
        // loop over all repository-entries
        final Iterator iter = repositoryList.iterator();
        RepositoryEntry repositoryEntry = null;

        // committing here to make sure the loadBusinessGroup below does actually
        // reload from the database and not only use the session cache
        // (see org.hibernate.Session.get():
        // If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
        DBFactory.getInstance().commitAndCloseSession();

        while (iter.hasNext()) {
            try {
                repositoryEntry = (RepositoryEntry) iter.next();

                // reload the repositoryEntry here before indexing it to make sure it has not been deleted in the meantime
                final RepositoryEntry reloadedRepositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryEntry.getKey());
                if (reloadedRepositoryEntry == null) {
                    log.info("doIndex: repositoryEntry was deleted while we were indexing. The deleted repositoryEntry was: " + repositoryEntry);
                    continue;
                }
                repositoryEntry = reloadedRepositoryEntry;

                if (log.isDebugEnabled()) {
                    log.debug("Index repositoryEntry=" + repositoryEntry + "  counter=" + counter++ + " with ResourceableId="
                            + repositoryEntry.getOlatResource().getResourceableId());
                }
                if (!isOnBlacklist(repositoryEntry.getOlatResource().getResourceableId())) {
                    final SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
                    searchResourceContext.setBusinessControlFor(repositoryEntry);
                    final Document document = RepositoryEntryDocument.createDocument(searchResourceContext, repositoryEntry);
                    indexWriter.addDocument(document);
                    // Pass created-date & modified-date in context to child indexer because the child have no dates
                    // TODO:chg: Check ob courseNode keine Daten hat
                    searchResourceContext.setLastModified(repositoryEntry.getLastModified());
                    searchResourceContext.setCreatedDate(repositoryEntry.getCreationDate());
                    // go further with resource
                    final Indexer repositoryEntryIndexer = RepositoryEntryIndexerFactory.getInstance().getRepositoryEntryIndexer(repositoryEntry);
                    if (repositoryEntryIndexer != null) {
                        repositoryEntryIndexer.doIndex(searchResourceContext, repositoryEntry, indexWriter);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("No RepositoryEntryIndexer for " + repositoryEntry.getOlatResource());
                        }
                    }
                } else {
                    log.warn("RepositoryEntry is on black-list and excluded from search-index , repositoryEntry=" + repositoryEntry);
                }
            } catch (final Throwable ex) {
                // create meaninfull debugging output to find repo entry that is somehow broken
                String entryDebug = "NULL";
                if (repositoryEntry != null) {
                    entryDebug = "resId::" + repositoryEntry.getResourceableId() + " resTypeName::" + repositoryEntry.getResourceableTypeName() + " resName::"
                            + repositoryEntry.getResourcename();
                }
                log.warn("Exception=" + ex.getMessage() + " for repo entry " + entryDebug, ex);
                DBFactory.getInstance(false).rollbackAndCloseSession();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("RepositoryIndexer finished.  counter=" + counter);
        }
    }

    private boolean isOnBlacklist(final Long key) {
        return repositoryBlackList.contains(key);

    }

    /**
     * Bean setter method used by spring.
     * 
     * @param indexerList
     */
    public void setIndexerList(final List indexerList) {
        if (indexerList == null) {
            throw new AssertException("null value for indexerList not allowed.");
        }

        try {
            for (final Iterator iter = indexerList.iterator(); iter.hasNext();) {
                final Indexer reporsitoryEntryIndexer = (Indexer) iter.next();
                RepositoryEntryIndexerFactory.getInstance().registerIndexer(reporsitoryEntryIndexer);
                if (log.isDebugEnabled()) {
                    log.debug("Adding indexer from configuraton:: ");
                }
            }
        } catch (final ClassCastException cce) {
            throw new StartupException("Configured indexer is not of type RepositoryEntryIndexer", cce);
        }
    }

    /**
	 */
    @Override
    public String getSupportedTypeName() {
        return OresHelper.calculateTypeName(RepositoryEntry.class);
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
        final RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryKey);
        if (repositoryEntry != null) {
            final boolean isOwner = repositoryManager.isOwnerOfRepositoryEntry(identity, repositoryEntry);
            boolean isAllowedToLaunch = false;
            if (!isOwner) {
                if (repositoryEntry.getOwnerGroup() == null) {
                    // FIXME:chg: Inconsistent RepositoryEntry without owner-group, should not exit => Workaround no access
                    return false;
                }
                isAllowedToLaunch = repositoryManager.isAllowedToLaunch(identity, roles, repositoryEntry);
            }
            if (log.isDebugEnabled()) {
                log.debug("isOwner=" + isOwner + "  isAllowedToLaunch=" + isAllowedToLaunch);
            }
            if (isOwner || isAllowedToLaunch) {
                final Indexer repositoryEntryIndexer = RepositoryEntryIndexerFactory.getInstance().getRepositoryEntryIndexer(repositoryEntry);
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

}
