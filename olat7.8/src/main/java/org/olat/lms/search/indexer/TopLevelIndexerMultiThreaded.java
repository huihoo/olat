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

/**
 * Initial Date: 14.06.2013 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public abstract class TopLevelIndexerMultiThreaded extends TopLevelIndexer {

    protected IndexerWorkerPool threadPool;

    protected int getNumberOfThreads() {
        return super.numberOfThreads;
    }

    /**
     * [spring managed]
     */
    public void setNumberOfThreads(int numberOfThreads) {
        super.numberOfThreads = numberOfThreads;
    }

    protected int getThreadPriority() {
        return super.threadPriority;
    }

    /**
     * [spring managed]
     */
    public void setThreadPriority(int threadPriority) {
        super.threadPriority = threadPriority;
        if (threadPriority < Thread.MIN_PRIORITY || threadPriority > Thread.MAX_PRIORITY) {
            // warning is logged in SearchModule if thread prio is out of range
            super.threadPriority = Thread.NORM_PRIORITY;
        }
    }

    @Override
    protected void doIndexing(OlatFullIndexer indexWriter) {
        threadPool = new IndexerWorkerPool(getNumberOfThreads(), threadPriority, getThreadPoolName());
    }

    @Override
    public void stopIndexing() {
        super.stopIndexing();
        threadPool.terminate();
        status = IndexerStatus.INTERRUPTED;
    }

    protected abstract String getThreadPoolName();

}
