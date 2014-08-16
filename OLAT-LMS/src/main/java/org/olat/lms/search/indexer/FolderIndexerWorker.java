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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.search.SearchResourceContext;
import org.olat.lms.search.SearchServiceFactory;
import org.olat.lms.search.document.file.DocumentAccessException;
import org.olat.lms.search.document.file.DocumentException;
import org.olat.lms.search.document.file.DocumentNotImplementedException;
import org.olat.lms.search.document.file.FileDocumentFactory;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * 
 * @author Christian Guretzki
 */
public class FolderIndexerWorker implements Runnable {

    private static final Logger log = LoggerHelper.getLogger();

    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;

    private Thread folderIndexer = null;

    private SearchResourceContext parentResourceContext;
    private VFSContainer container;
    private OlatFullIndexer indexWriter;
    private String filePath;
    private FolderIndexerAccess accessRule;

    private final String threadId;

    private int state = 0;

    public FolderIndexerWorker(final int threadId) {
        this.threadId = Integer.toString(threadId);
    }

    public void start() {
        folderIndexer = new Thread(this, "folderIndexer-" + threadId);
        folderIndexer.setPriority(Thread.MIN_PRIORITY);
        folderIndexer.setDaemon(true);
        folderIndexer.start();
        state = STATE_RUNNING;
    }

    @Override
    public void run() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("folderIndexer-" + threadId + " run...");
            }
            doIndexVFSContainer(parentResourceContext, container, indexWriter, filePath, accessRule);
            if (log.isDebugEnabled()) {
                log.debug("folderIndexer-" + threadId + " finished");
            }
        } catch (final IOException e) {
            log.warn("IOException in run", e);
        } catch (final InterruptedException e) {
            // Can happen if indexing is interrupted
            if (log.isDebugEnabled()) {
                log.debug("InterruptedException in run");
            }
        } catch (final Exception e) {
            log.warn("Exception in run", e);
        } finally {
            // db session a saved in a thread local
            DBFactory.getInstance().commitAndCloseSession();
            FolderIndexerWorkerPool.getInstance().release(this);
        }
        log.debug("folderIndexer-" + threadId + " end of run");
        state = STATE_FINISHED;
    }

    protected void doIndexVFSContainer(final SearchResourceContext resourceContext, final VFSContainer cont, final OlatFullIndexer writer, final String fPath,
            final FolderIndexerAccess aRule) throws IOException, InterruptedException {
        // Items: List of VFSContainer & VFSLeaf
        final String myFilePath = fPath;
        for (final VFSItem item : cont.getItems()) {
            if (item instanceof VFSContainer) {
                // ok it is a container go further
                if (log.isDebugEnabled()) {
                    log.debug(item.getName() + " is a VFSContainer => go further ");
                }
                if (aRule.allowed(item)) {
                    doIndexVFSContainer(resourceContext, (VFSContainer) item, writer, myFilePath + "/" + ((VFSContainer) item).getName(), aRule);
                }
            } else if (item instanceof VFSLeaf) {
                // ok it is a file => analyse it
                if (log.isDebugEnabled()) {
                    log.debug(item.getName() + " is a VFSLeaf => analyse file");
                }
                if (aRule.allowed(item)) {
                    doIndexVFSLeaf(resourceContext, (VFSLeaf) item, writer, myFilePath);
                }
            } else {
                log.warn("Unkown element in item-list class=" + item.getClass());
            }
        }
    }

    protected void doIndexVFSLeaf(final SearchResourceContext leafResourceContext, final VFSLeaf leaf, final OlatFullIndexer writer, final String fPath) {
        if (log.isDebugEnabled()) {
            log.debug("Analyse VFSLeaf=" + leaf.getName());
        }
        try {
            if (SearchServiceFactory.getFileDocumentFactory().isFileSupported(leaf)) {
                final String myFilePath = fPath + "/" + leaf.getName();
                leafResourceContext.setFilePath(myFilePath);
                final Document document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
                writer.addDocument(document);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Documenttype not supported. file=" + leaf.getName());
                }
            }
        } catch (final DocumentAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("Can not access document." + e.getMessage());
            }
        } catch (final DocumentNotImplementedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Documenttype not implemented." + e.getMessage());
            }
        } catch (final InterruptedException e) {
            if (log.isDebugEnabled()) {
                log.debug("InterruptedException: Can not index leaf=" + leaf.getName() + ";" + e.getMessage());
            }
        } catch (final DocumentException dex) {
            log.debug("DocumentException: Can not index leaf=" + leaf.getName() + " , exception=" + dex);
        } catch (final IOException ioEx) {
            log.warn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
        } catch (final Exception ex) {
            log.warn("Exception: Can not index leaf=" + leaf.getName(), ex);
        }
    }

    public void setParentResourceContext(final SearchResourceContext newParentResourceContext) {
        this.parentResourceContext = newParentResourceContext;
    }

    public void setContainer(final VFSContainer newContainer) {
        this.container = newContainer;
    }

    public void setIndexWriter(final OlatFullIndexer newIndexWriter) {
        this.indexWriter = newIndexWriter;
    }

    public void setFilePath(final String newFilePath) {
        this.filePath = newFilePath;
    }

    public void setAccessRule(final FolderIndexerAccess accessRule) {
        this.accessRule = accessRule;
    }

    public String getId() {
        return threadId;
    }

    /**
     * @return Returns the state.
     */
    public int getState() {
        if ((folderIndexer != null) && folderIndexer.isAlive()) {
            return STATE_RUNNING;
        }
        return state;
    }
}
