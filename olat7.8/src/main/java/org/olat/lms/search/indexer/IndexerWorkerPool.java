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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.lms.search.indexer.Indexer.IndexerStatus;

/**
 * Initial Date: 11.03.2013
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public final class IndexerWorkerPool {

    private final int threadPrio;

    private final ExecutorService threadPool;

    public IndexerWorkerPool(int poolSize, int threadPrio, String threadNamePrefix) {
        this.threadPrio = threadPrio;
        threadPool = Executors.newFixedThreadPool(poolSize, new IndexerWorkerThreadFactory(threadNamePrefix));
    }

    public void executeIndexerTask(Runnable task) {
        threadPool.execute(task);
    }

    public <T> Future<T> submitIndexerTask(Callable<T> task) {
        return threadPool.submit(task);
    }

    public IndexerStatus waitForCompletion(int timeout) {
        threadPool.shutdown();
        try {
            return threadPool.awaitTermination(timeout, TimeUnit.SECONDS) ? IndexerStatus.COMPLETED : IndexerStatus.TIMEOUT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return IndexerStatus.INTERRUPTED;
        }
    }

    public void terminate() {
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final class IndexerWorkerThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger();

        private final String threadNamePrefix;

        private IndexerWorkerThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            final String threadName = threadNamePrefix + "-WorkerThread-" + threadNumber.incrementAndGet();
            Thread t = new Thread(r, threadName);
            t.setDaemon(false);
            t.setPriority(threadPrio);
            return t;
        }
    }

}
