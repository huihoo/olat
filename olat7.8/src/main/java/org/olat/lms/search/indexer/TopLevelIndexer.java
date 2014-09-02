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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 26.03.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public abstract class TopLevelIndexer extends Indexer {

    static final String STATISTIC_FILE_SUFFIX = "csv";

    private static final Logger log = LoggerHelper.getLogger();

    protected int numberOfThreads = 1;

    protected int threadPriority = Thread.NORM_PRIORITY;

    protected volatile boolean stopRequested;

    protected IndexerStatus status = IndexerStatus.INITIAL;

    private long startTime;

    private long endTime;

    private ConcurrentMap<Object, ItemIndexingResult> itemIndexingResults = new ConcurrentHashMap<Object, ItemIndexingResult>();

    private volatile int indexingCounter;

    private volatile int processedCounter;

    private volatile int successCounter;

    private volatile int timeoutCounter;

    private volatile int failureCounter;

    private volatile long processingTimeOverall;

    private volatile long processingTimeMax;

    /**
     * starts indexing
     */
    public void startIndexing(OlatFullIndexer indexWriter) {
        startTime = System.currentTimeMillis();
        doIndexing(indexWriter);
        endTime = System.currentTimeMillis();
    }

    /**
     * implementation method for specific indexing
     */
    protected abstract void doIndexing(OlatFullIndexer indexWriter);

    public void stopIndexing() {
        stopRequested = true;
    }

    public TopLevelIndexerStatus getStatus() {
        return new TopLevelIndexerStatus();
    }

    public void resetStatus() {
        stopRequested = false;
        status = IndexerStatus.INITIAL;
        itemIndexingResults.clear();
        indexingCounter = 0;
        processedCounter = 0;
        successCounter = 0;
        timeoutCounter = 0;
        failureCounter = 0;
        processingTimeOverall = 0;
        processingTimeMax = 0;
    }

    protected abstract int getNumberOfItemsToBeIndexed();

    // for multi threaded TopLevelIndexer implementations this method is usually called from a thread pool -> therefore made it synchronized
    protected synchronized void indexingItemStarted(Object itemId) {
        indexingCounter++;
        itemIndexingResults.put(itemId, new ItemIndexingResult(itemId));
    }

    // for multi threaded TopLevelIndexer implementations this method is usually called from a thread pool -> therefore made it synchronized
    protected synchronized void indexingItemFinished(Object itemId) {
        indexingCounter--;
        processedCounter++;
        successCounter++;

        final ItemIndexingResult itemIndexingResult = itemIndexingResults.get(itemId);
        final long endTime = System.currentTimeMillis();
        itemIndexingResult.setEndTime(endTime);

        final long processingTime = endTime - itemIndexingResult.startTime;
        processingTimeOverall += processingTime;
        if (processingTime > processingTimeMax) {
            processingTimeMax = processingTime;
        }
    }

    // for multi threaded TopLevelIndexer implementations this method is usually called from a thread pool -> therefore made it synchronized
    protected synchronized void indexingItemTimedOut(Object itemId) {
        indexingCounter--;
        processedCounter++;
        timeoutCounter++;
        final ItemIndexingResult itemIndexingResult = itemIndexingResults.get(itemId);
        itemIndexingResult.setEndTime(System.currentTimeMillis());
    }

    // for multi threaded TopLevelIndexer implementations this method is usually called from a thread pool -> therefore made it synchronized
    protected synchronized void indexingItemFailed(Object itemId, Exception ex) {
        indexingCounter--;
        processedCounter++;
        failureCounter++;
        final ItemIndexingResult itemIndexingResult = itemIndexingResults.get(itemId);
        itemIndexingResult.setEndTime(System.currentTimeMillis());
        itemIndexingResult.failure = ex;
    }

    /**
     * Creates a CSV statistics file containing all indexed items.
     */
    public void createStatisticsFile(File destDir) {
        final char csvSeparator = ';';
        final String lineSeparator = System.getProperty("line.separator");
        DateFormat dateFormat = DateFormat.getDateTimeInstance();

        final File statisticsFile = new File(destDir, getClass().getSimpleName() + "Statistics." + STATISTIC_FILE_SUFFIX);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(statisticsFile), Charset.forName("ISO-8859-1"));
            List<ItemIndexingResult> results = new ArrayList<ItemIndexingResult>(itemIndexingResults.values());
            Collections.sort(results, new ItemIndexingResultSorter());
            for (ItemIndexingResult itemIndexingResult : results) {
                writer.append(itemIndexingResult.itemId.toString()).append(csvSeparator);
                writer.append(itemIndexingResult.threadName).append(csvSeparator);
                writer.append(dateFormat.format(new Date(itemIndexingResult.startTime))).append(csvSeparator);
                if (itemIndexingResult.duration == Integer.MAX_VALUE) {
                    // timeout
                    writer.append("n.a.").append(csvSeparator);
                    writer.append("timeout").append(lineSeparator);
                } else {
                    writer.append(FullIndexerStatus.convertToHourMinuteSecond(itemIndexingResult.duration, false)).append(csvSeparator);
                    if (itemIndexingResult.failure == null) {
                        writer.append("success").append(lineSeparator);
                    } else {
                        writer.append("failure").append(csvSeparator);
                        writer.append(itemIndexingResult.failure.toString()).append(lineSeparator);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            log.error("Couldn't create indexer statistics file: " + statisticsFile.getAbsolutePath(), ex);
        } catch (IOException ex) {
            log.error("Problem writing to indexer statistics file: " + statisticsFile.getAbsolutePath(), ex);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                log.warn("Couldn't flush/close indexer statistics file: " + statisticsFile.getAbsolutePath(), ex);
            }
        }
    }

    public final class TopLevelIndexerStatus {

        public IndexerStatus getStatus() {
            return status;
        }

        public int getNumberOfItems() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }
            return getNumberOfItemsToBeIndexed();
        }

        public int getIndexingItems() {
            return indexingCounter;
        }

        public int getIndexedItems() {
            return successCounter;
        }

        public int getTimedOutItems() {
            return timeoutCounter;
        }

        public int getFailedItems() {
            return failureCounter;
        }

        public int getProgress() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }

            // if there is nothing to do we have already reached 100% ...
            if (getNumberOfItems() == 0) {
                return 100;
            }

            return (int) ((float) processedCounter * 100 / getNumberOfItems());
        }

        public long getTimeElapsed() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }
            if (endTime == 0) {
                return System.currentTimeMillis() - startTime;
            }
            return endTime - startTime;
        }

        public long getAverageProcessingTime() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }

            if (processedCounter == 0) {
                return 0;
            }

            return processingTimeOverall / processedCounter;
        }

        public long getMaxProcessingTime() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }
            return processingTimeMax;
        }

        public long getTimeRemaining() {
            if (status == IndexerStatus.INITIAL) {
                return 0;
            }
            return (getNumberOfItems() - processedCounter) * getAverageProcessingTime() / numberOfThreads;
        }

        public int getNumberOfThreads() {
            return numberOfThreads;
        }
    }

    private static final class ItemIndexingResult {

        private final Object itemId;

        private final String threadName;

        private final long startTime;

        private int duration = Integer.MAX_VALUE;

        private Exception failure;

        private ItemIndexingResult(Object id) {
            itemId = id;
            startTime = System.currentTimeMillis();
            threadName = Thread.currentThread().getName();
        }

        private void setEndTime(long endTime) {
            duration = (int) (endTime - startTime);
        }
    }

    // sorts the results by failure/duration desc => failures/long durations appear on top of statistic files
    private static final class ItemIndexingResultSorter implements Comparator<ItemIndexingResult> {

        @Override
        public int compare(ItemIndexingResult o1, ItemIndexingResult o2) {
            if (o1.failure != null && o2.failure == null) {
                return -1;
            }
            if (o1.failure == null && o2.failure != null) {
                return 1;
            }
            if (o1.duration > o2.duration) {
                return -1;
            }
            if (o1.duration < o2.duration) {
                return 1;
            }
            return 0;
        }

    }

}
