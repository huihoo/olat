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

package org.olat.lms.search.indexer;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.file.FileDocumentFactory;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * 
 * @author Christian Guretzki
 * @author oliver.buehler@agility-informatik.ch
 */
public final class FolderIndexer {

    private static final Logger log = LoggerHelper.getLogger();

    private static final int TIMEOUT_FOLDER_MS = SearchServiceFactory.getService().getSearchModuleConfig().getTimeoutFolderSeconds() * 1000;

    private static final boolean TIMEOUT_FOLDER_CHECK = TIMEOUT_FOLDER_MS != 0;

    private static final int TIMEOUT_FILE_MS = SearchServiceFactory.getService().getSearchModuleConfig().getTimeoutFileSeconds() * 1000;

    private static final boolean TIMEOUT_FILE_CHECK = TIMEOUT_FILE_MS != 0;

    private FolderIndexer() {
        super();

    }

    public static void indexVFSContainer(final SearchResourceContext resourceContext, final VFSContainer container, final OlatFullIndexer indexer,
            final FolderIndexerAccess accessRule) throws FolderIndexerTimeoutException {
        if (log.isDebugEnabled()) {
            log.debug("Index VFSContainer: " + container.getPath());
        }
        if (TIMEOUT_FOLDER_CHECK) {
            doIndexVFSContainer(resourceContext, container, indexer, accessRule, System.currentTimeMillis(), container, "");
        } else {
            doIndexVFSContainer(resourceContext, container, indexer, accessRule, 0L, null, "");
        }
    }

    private static void doIndexVFSContainer(final SearchResourceContext resourceContext, final VFSContainer container, final OlatFullIndexer indexer,
            final FolderIndexerAccess accessRule, long startTime, final VFSContainer rootContainer, final String subFolderPath) throws FolderIndexerTimeoutException {
        for (final VFSItem item : container.getItems()) {
            if (TIMEOUT_FOLDER_CHECK) {
                if (System.currentTimeMillis() - startTime > TIMEOUT_FOLDER_MS) {
                    indexer.reportFolderTimeout(rootContainer.getPath());
                    log.info("Indexer folder timeout (>" + indexer.getSearchModuleConfig().getTimeoutFolderSeconds() + "s): " + rootContainer.getPath());
                    throw new FolderIndexerTimeoutException(rootContainer, indexer.getSearchModuleConfig().getTimeoutFolderSeconds());
                }
            }
            if (Thread.interrupted()) {
                return;
            }

            if (item instanceof VFSContainer) {
                if (log.isDebugEnabled()) {
                    log.debug(item.getPath() + " is a VFSContainer => go further ");
                }
                if (accessRule.allowed(item)) {
                    final String newSubFolderPath = subFolderPath + "/" + ((VFSContainer) item).getName();
                    doIndexVFSContainer(resourceContext, (VFSContainer) item, indexer, accessRule, startTime, rootContainer, newSubFolderPath);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(item.getPath() + " is a VFSLeaf => analyse file");
                }
                if (accessRule.allowed(item)) {
                    indexVFSLeaf(resourceContext, (VFSLeaf) item, indexer, subFolderPath);
                }
            }
        }
    }

    public static void indexVFSLeaf(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final OlatFullIndexer indexer) {
        indexVFSLeaf(leafResourceContext, leaf, indexer, "");
    }

    private static void indexVFSLeaf(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final OlatFullIndexer indexer, String subFolderPath) {
        if (log.isDebugEnabled()) {
            log.debug("Analyse VFSLeaf=" + leaf.getName());
        }
        try {
            if (SearchServiceFactory.getFileDocumentFactory().isFileSupported(leaf)) {
                if (subFolderPath.endsWith("/")) {
                    leafResourceContext.setFilePath(subFolderPath + leaf.getName());
                } else {
                    leafResourceContext.setFilePath(subFolderPath + "/" + leaf.getName());
                }

                Document document = null;
                if (TIMEOUT_FILE_CHECK) {
                    long startTime = System.currentTimeMillis();
                    document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
                    if (System.currentTimeMillis() - startTime > TIMEOUT_FILE_MS) {
                        indexer.reportFileTimeout(leaf.getPath());
                    }
                } else {
                    document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
                }

                if (document != null) {
                    indexer.addDocument(document);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Documenttype not supported. file=" + leaf.getName());
                }
            }
        } catch (final Exception ex) {
            log.warn("Exception while indexing leaf=" + leaf.getPath(), ex);
            indexer.reportFileIndexingError(leaf.getPath(), ex);
        }
    }

}
