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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Used in multi-threaded mode. Reads lucene documents from document-queue in main-indexer and add this documents to index-writer. The index-writer works with his own
 * 'part-[ID]' index-directory. Initial Date: 10.10.2006 <br>
 * 
 * @author guretzki
 */
public class IndexWriterWorker implements Runnable {

    public static final Document STOP_INDEXING_DOCUMENT = new Document();

    private static final Logger log = LoggerHelper.getLogger();

    private final int id;
    private final File indexPartDir;
    private final OlatFullIndexer fullIndexer;

    private final AtomicBoolean finished = new AtomicBoolean();

    private IndexWriter indexWriter;

    private int documentCounter;

    /**
     * @param id
     *            Unique index ID. Is used to generate unique directory name.
     * @param tempIndexPath
     *            Absolute directory-path where the temporary index can be generated.
     * @param fullIndexer
     *            Reference to full-index
     */
    public IndexWriterWorker(final int id, final File tempIndexDir, final OlatFullIndexer fullIndexer) {
        this.id = id;
        this.indexPartDir = new File(tempIndexDir, "part" + id);
        this.fullIndexer = fullIndexer;
        try {
            final Directory luceneIndexPartDir = FSDirectory.open(indexPartDir);
            indexWriter = new IndexWriter(luceneIndexPartDir, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
            indexWriter.setMergeFactor(fullIndexer.getSearchModuleConfig().getIndexerWriterMergeFactor());
            log.info("IndexWriter config MergeFactor=" + indexWriter.getMergeFactor());
            indexWriter.setRAMBufferSizeMB(fullIndexer.getSearchModuleConfig().getIndexerWriterRamBuffer());
            log.info("IndexWriter config RAMBufferSizeMB=" + indexWriter.getRAMBufferSizeMB());
            indexWriter.setUseCompoundFile(false);
        } catch (final IOException e) {
            log.warn("Can not create IndexWriter");
        }
    }

    /**
     * Create and start a new index-writer thread.
     */
    public void start() {
        final Thread indexerWriterThread = new Thread(this, "indexWriter-" + id);
        indexerWriterThread.setPriority(fullIndexer.getSearchModuleConfig().getIndexerPrio());
        indexerWriterThread.setDaemon(true);
        indexerWriterThread.start();
    }

    /**
     * Check if document-queue of main-indexer has elements. Get document from queue and add it to index-writer.
     * 
     */
    @Override
    public void run() {
        final BlockingQueue<Document> documentQueue = fullIndexer.getDocumentQueue();
        while (true) {
            Document document = null;
            try {
                document = documentQueue.take();
                if (document == STOP_INDEXING_DOCUMENT) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }

            try {
                indexWriter.addDocument(document);
                documentCounter++;
                if (log.isDebugEnabled()) {
                    log.debug("documentQueue.remove size=" + documentQueue.size());
                    log.debug("IndexWriter docCount=" + indexWriter.maxDoc());
                }
            } catch (final Exception ex) {
                log.warn("Exception in run", ex);
            }
        }

        try {
            indexWriter.close();
            finished.set(true);
            if (log.isDebugEnabled()) {
                log.debug("IndexWriter " + id + " finished.");
            }
        } catch (final IOException e) {
            log.warn("Can not close IndexWriter.", e);
        }
        log.info("IndexWriter " + id + " end of run.");
    }

    /**
     * @return Lucene Directory object of index-writer.
     */
    public Directory getIndexDir() {
        return indexWriter.getDirectory();
    }

    /**
     * @return Return number of added documents.
     */
    public int getDocCount() {
        return documentCounter;
    }

    public void waitUntilFinished() {
        while (!finished.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return;
    }

}
