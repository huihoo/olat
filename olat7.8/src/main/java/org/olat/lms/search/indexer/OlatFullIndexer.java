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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.search.SearchModule;
import org.olat.lms.search.document.AbstractOlatDocument;
import org.olat.lms.search.indexer.Indexer.IndexerStatus;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Controls the whole generation of a full-index. Runs in own thread.
 * 
 * @author Christian Guretzki
 */
public class OlatFullIndexer implements Runnable {

    private static final Logger log = LoggerHelper.getLogger();

    private final Index index;

    private final MainIndexer mainIndexer;

    private final SearchModule searchModuleConfig;

    private boolean testMode;

    private volatile Thread indexingThread = null;

    /** Flag to stop indexing. */
    private AtomicBoolean stopIndexing = new AtomicBoolean();

    private IndexWriter indexWriter = null;

    /** Used to build number of indexed documents per minute. */
    private long lastMinute;

    private int currentMinuteCounter;

    /** Queue to pass documents from indexer to index-writers. Only in multi-threaded mode. */
    private final BlockingQueue<Document> documentQueue;
    private IndexWriterWorker[] indexWriterWorkers;

    /* Counts added documents in indexInterval. */
    int documentsPerInterval;

    /* List of Integer objects to count number of docs for each type. Key = document-type. */
    private ConcurrentMap<String, Integer> documentCounters;
    private ConcurrentMap<String, Integer> fileTypeCounters;
    /** Current status of full-indexer. */
    private FullIndexerStatus fullIndexerStatus;

    private final List<String> timedOutFolders = new ArrayList<String>();

    private final List<String> timedOutFiles = new ArrayList<String>();

    private final Map<String, Exception> errorFiles = new HashMap<String, Exception>();

    /**
     * @param tempIndexPath
     *            Absolute file path to temporary index directory.
     * @param index
     *            Reference to index object.
     * @param restartInterval
     *            Restart interval in milliseconds.
     * @param indexInterval
     *            Sleep time in milliseconds between adding documents.
     */
    public OlatFullIndexer(final Index index, final MainIndexer mainIndexer, final SearchModule searchModuleConfig) {
        this.index = index;
        this.mainIndexer = mainIndexer;
        this.searchModuleConfig = searchModuleConfig;
        documentQueue = new ArrayBlockingQueue<Document>(searchModuleConfig.getIndexerDocumentQueueSize());

        resetStatusInformation();
    }

    public SearchModule getSearchModuleConfig() {
        return searchModuleConfig;
    }

    public int getTimeoutSeconds() {
        return searchModuleConfig.getTimeoutSeconds();
    }

    /**
     * Start full indexer thread.
     */
    public void startIndexing(final boolean testMode) {
        if (indexingThread != null) {
            log.debug("indexing not started since it is already running");
            return;
        }

        log.info("start full indexing thread...");
        this.testMode = testMode;
        stopIndexing.getAndSet(false);
        resetStatusInformation();

        indexingThread = new Thread(new ThreadGroup("Indexer"), this, "FullIndexer");
        indexingThread.setPriority(searchModuleConfig.getIndexerPrio());
        indexingThread.start();
    }

    /**
     * Stop full indexer thread asynchronous.
     */
    public void stopIndexing() {
        if (log.isDebugEnabled()) {
            log.debug("Indexing interrupted via admin page.");
        }
        if (indexingThread != null) {
            stopIndexing.set(true);
            // this will cause mainIndexer.startIndexing() to return prematurely
            mainIndexer.stopIndexing();
            mainIndexer.reportItemIndexing(new File(searchModuleConfig.getTempSearchIndexPath()));
            createStatisticFiles();
        }
    }

    @Override
    public void run() {
        log.info("full indexing starts... Lucene-version:" + LucenePackage.get().getImplementationVersion());

        fullIndexerStatus.indexingStarted(testMode);
        final IndexerStatus indexerStatus = doIndex();
        switch (indexerStatus) {
        case COMPLETED:
            fullIndexerStatus.indexingFinished();
            break;
        case INTERRUPTED:
            fullIndexerStatus.indexingStopped();
            break;
        case TIMEOUT:
            fullIndexerStatus.indexingTimedOut();
            break;
        }

        log.info("full indexing done in " + fullIndexerStatus.getIndexingTime() + "ms");

        // OLAT-5630 - dump more infos about the indexer run - for analysis later
        final FullIndexerStatus status = getStatus();
        log.info("full indexing summary: started:           " + status.getFullIndexStartedAt());
        log.info("full indexing summary: counter:           " + status.getDocumentCount());
        log.info("full indexing summary: index.per.minute:  " + status.getIndexPerMinute());
        log.info("full indexing summary: finished:          " + status.getLastFullIndexTime());
        log.info("full indexing summary: time:              " + status.getIndexingTime() + " ms");
        log.info("full indexing summary: size:              " + status.getIndexSize());

        log.info("full indexing summary: document counters: " + status.getDocumentCounters());
        log.info("full indexing summary: file type counters:" + status.getFileTypeCounters());
        log.info("full indexing summary: excluded counter:  " + status.getExcludedDocumentCount());

        indexingThread = null;

        log.info("quit indexing run.");
    }

    /**
     * Create index-writer object. In multi-threaded mode creates an array of index-workers. Start indexing with main-index as root object. Index recursive all elements.
     * At the end optimize and close new index. The new index is stored in [temporary-index-path]/main
     * 
     * @throws InterruptedException
     */
    private IndexerStatus doIndex() {
        // delete all temp data of previous indexer runs
        final File tempSearchIndexDir = new File(searchModuleConfig.getTempSearchIndexPath());
        if (tempSearchIndexDir.exists()) {
            FileUtils.deleteDirsAndFiles(tempSearchIndexDir, true, false);
        }
        tempSearchIndexDir.mkdirs();

        // create IndexWriterWorker
        int numberIndexWriter = searchModuleConfig.getIndexerWriterNumber();
        final Directory[] partIndexDirs = new Directory[numberIndexWriter];
        if (!testMode) {
            log.info("Running with " + numberIndexWriter + " IndexerWriterWorker");
            indexWriterWorkers = new IndexWriterWorker[numberIndexWriter];
            for (int i = 0; i < numberIndexWriter; i++) {
                final IndexWriterWorker indexWriterWorker = new IndexWriterWorker(i, tempSearchIndexDir, this);
                indexWriterWorkers[i] = indexWriterWorker;
                indexWriterWorkers[i].start();
                partIndexDirs[i] = indexWriterWorkers[i].getIndexDir();
            }
        }

        // start indexer
        log.info("doIndex start. OlatFullIndexer with Debug output");
        final IndexerStatus indexerStatus = mainIndexer.startIndexing(this);

        // report details about indexed items and folder/file timeouts/errors
        mainIndexer.reportItemIndexing(new File(searchModuleConfig.getTempSearchIndexPath()));
        createStatisticFiles();

        if (!testMode) {
            // clear document queue if stop requested
            if (stopIndexing.get()) {
                documentQueue.clear();
            }
            for (int i = 0; i < numberIndexWriter; i++) {
                // send each index writer the stop indicating document
                documentQueue.add(IndexWriterWorker.STOP_INDEXING_DOCUMENT);
            }
            log.info("Wait until every indexworker is finished");
            for (int i = 0; i < numberIndexWriter; i++) {
                indexWriterWorkers[i].waitUntilFinished();
            }

            // define index writer
            try {
                final Directory indexPath = FSDirectory.open(new File(tempSearchIndexDir, "main"));
                final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
                indexWriter = new IndexWriter(indexPath, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);

                // for better performance (see lucene docu 'how to make indexing faster")
                indexWriter.setMergeFactor(searchModuleConfig.getIndexerWriterMergeFactor());
                log.info("IndexWriter config MergeFactor=" + indexWriter.getMergeFactor());
                indexWriter.setRAMBufferSizeMB(searchModuleConfig.getIndexerWriterRamBuffer());
                log.info("IndexWriter config RAMBufferSizeMB=" + indexWriter.getRAMBufferSizeMB());
                indexWriter.setUseCompoundFile(false);
            } catch (final Exception e) {
                log.warn("Can not create IndexWriter, indexname=" + searchModuleConfig.getTempSearchIndexPath(), e);
            }

            try {
                if (indexerStatus == IndexerStatus.TIMEOUT || stopIndexing.get() || testMode) {
                    indexWriter.close();
                } else {
                    // Merge all partIndex
                    if (partIndexDirs.length > 0) {
                        log.info("Start merging part Indexes");
                        indexWriter.addIndexesNoOptimize(partIndexDirs);
                        log.info("Added all part Indexes");
                    }
                    fullIndexerStatus.setIndexSize(indexWriter.maxDoc());
                    indexWriter.optimize();
                    indexWriter.close();

                    index.activateNewIndex();
                }
            } catch (final Exception e) {
                log.warn("Can not close IndexWriter, indexname=" + searchModuleConfig.getTempSearchIndexPath(), e);
            }
        }

        return indexerStatus;
    }

    /**
     * @param document
     *            Lucene document to be added to index
     * @throws IOException
     */
    public void addDocument(final Document document) throws IOException {
        // in test mode don't feed index writer
        if (!testMode) {
            if (searchModuleConfig.getIndexerWriterNumber() == 0) {
                indexWriter.addDocument(document);
            } else {
                try {
                    // this is synchronized by BlockingQueue implementation
                    documentQueue.put(document);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                fullIndexerStatus.setDocumentQueueSize(documentQueue.size());
                if (log.isDebugEnabled()) {
                    log.debug("documentQueue.add size=" + documentQueue.size());
                }
            }
        }

        fullIndexerStatus.incrementDocumentCount();
        incrementDocumentTypeCounter(document);
        incrementFileTypeCounter(document);
        countIndexPerMinute();
    }

    private void incrementFileTypeCounter(final Document document) {
        final String fileType = document.get(AbstractOlatDocument.FILETYPE_FIELD_NAME);
        if ((fileType != null) && (!fileType.equals(""))) {
            int intValue = 0;
            if (fileTypeCounters.containsKey(fileType)) {
                final Integer fileCounter = fileTypeCounters.get(fileType);
                intValue = fileCounter.intValue();
            }
            intValue++;
            fileTypeCounters.put(fileType, new Integer(intValue));
        }
    }

    private void incrementDocumentTypeCounter(final Document document) {
        final String documentType = document.get(AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME);
        int intValue = 0;
        if (documentCounters.containsKey(documentType)) {
            final Integer docCounter = documentCounters.get(documentType);
            intValue = docCounter.intValue();
        }
        intValue++;
        documentCounters.put(documentType, new Integer(intValue));
    }

    private void countIndexPerMinute() {
        final long currentTime = System.currentTimeMillis();
        if (lastMinute + 60000 > currentTime) {
            // it is the same minute
            currentMinuteCounter++;
        } else {
            fullIndexerStatus.setIndexPerMinute(currentMinuteCounter);
            currentMinuteCounter = 0;
            if (lastMinute + 120000 > currentTime) {
                lastMinute = lastMinute + 60000;
            } else {
                lastMinute = currentTime;
            }
        }
    }

    /**
     * @return Return current full-indexer status.
     */
    public FullIndexerStatus getStatus() {
        if (indexWriterWorkers != null) {
            // IndexWorker exists => set current document-counter
            for (int i = 0; i < searchModuleConfig.getIndexerWriterNumber(); i++) {
                fullIndexerStatus.setPartDocumentCount(indexWriterWorkers[i].getDocCount(), i);
            }
        }
        fullIndexerStatus.setDocumentCounters(new HashMap<String, Integer>(documentCounters));
        fullIndexerStatus.setFileTypeCounters(new HashMap<String, Integer>(fileTypeCounters));
        fullIndexerStatus.setDocumentQueueSize(documentQueue.size());
        fullIndexerStatus.setIndexerStatus(mainIndexer.getStatus());
        return fullIndexerStatus;
    }

    /**
     * @return Return document-queue which is used in multi-threaded mode.
     */
    public BlockingQueue<Document> getDocumentQueue() {
        return documentQueue;
    }

    private void resetStatusInformation() {
        documentCounters = new ConcurrentHashMap<String, Integer>();
        fileTypeCounters = new ConcurrentHashMap<String, Integer>();
        fullIndexerStatus = new FullIndexerStatus(searchModuleConfig.getIndexerWriterNumber());
    }

    public synchronized void reportFolderTimeout(String folder) {
        timedOutFolders.add(folder);
    }

    public synchronized void reportFileTimeout(String file) {
        timedOutFiles.add(file);
    }

    public synchronized void reportFileIndexingError(String file, Exception ex) {
        errorFiles.put(file, ex);
    }

    private void createStatisticFiles() {
        createFolderTimeoutStatisticFile();
        createFileTimeoutStatisticFile();
        createFileErrorStatisticFile();
    }

    private void createFolderTimeoutStatisticFile() {
        final StatisticFile statisticFile = new StatisticFile("FolderTimeoutStatistics");
        statisticFile.writeLine("Folder timeouts: " + searchModuleConfig.getTimeoutFolderSeconds() + "s");
        for (String folder : timedOutFolders) {
            statisticFile.writeLine(folder);
        }
        statisticFile.close();
    }

    private void createFileTimeoutStatisticFile() {
        final StatisticFile statisticFile = new StatisticFile("FileTimeoutStatistics");
        statisticFile.writeLine("File timeouts: " + searchModuleConfig.getTimeoutFileSeconds() + "s");
        for (String folder : timedOutFiles) {
            statisticFile.writeLine(folder);
        }
        statisticFile.close();
    }

    private void createFileErrorStatisticFile() {
        final StatisticFile statisticFile = new StatisticFile("FileErrorStatistics");
        statisticFile.writeLine("File errors:");
        for (String file : errorFiles.keySet()) {
            statisticFile.writeLine(file, errorFiles.get(file).getMessage());
        }
        statisticFile.close();
    }

    private final class StatisticFile {

        private static final char csvSeparator = ';';

        private final String lineSeparator = System.getProperty("line.separator");

        private final String name;

        private final Writer writer;

        private File statisticFile;

        private StatisticFile(String name) {
            this.name = name;
            this.writer = createStatisticFile();
        }

        private Writer createStatisticFile() {
            statisticFile = new File(OlatFullIndexer.this.searchModuleConfig.getTempSearchIndexPath(), name + ".csv");
            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(statisticFile), Charset.forName("ISO-8859-1"));
            } catch (FileNotFoundException ex) {
                log.error("Couldn't create " + name + " statistics file: " + statisticFile.getAbsolutePath(), ex);
            }
            return writer;
        }

        private void writeLine(String... content) {
            try {
                for (int i = 0; i < content.length - 1; i++) {
                    writer.append(content[i]).append(csvSeparator);
                }
                writer.append(content[content.length - 1]).append(lineSeparator);
            } catch (IOException ex) {
                log.error("Problem writing to " + name + " statistics file: " + statisticFile.getAbsolutePath(), ex);
            }
        }

        private void close() {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                log.warn("Couldn't flush/close " + name + " statistics file: " + statisticFile.getAbsolutePath(), ex);
            }
        }

    }
}
