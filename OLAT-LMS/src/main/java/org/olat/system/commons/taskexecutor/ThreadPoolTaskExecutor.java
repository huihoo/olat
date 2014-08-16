package org.olat.system.commons.taskexecutor;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.olat.system.commons.configuration.Destroyable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.util.StopWatch;

class ThreadPoolTaskExecutor implements Destroyable {
    private static final Logger log = LoggerHelper.getLogger();

    ThreadPoolExecutor threadPool = null;
    // The queue all the tasks get filled in and are taken from, if the queue is full the server starts to reject new tasks
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5000);

    /**
     * @param poolSize
     * @param maxPoolSize
     * @param keepAliveTime
     */
    private ThreadPoolTaskExecutor(int poolSize, int maxPoolSize, int keepAliveTime) {
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
    }

    public void runTask(Runnable task) {
        StopWatch watch = null;
        if (log.isDebugEnabled()) {
            watch = new StopWatch();
            watch.start();
        }
        threadPool.execute(task);

        if (log.isDebugEnabled())
            watch.stop();
        if (log.isDebugEnabled())
            log.debug("Current size of queue is: " + queue.size() + ". Running last task took (ms): " + watch.getTotalTimeMillis());
    }

    /**
     * @see org.olat.system.commons.configuration.Destroyable#destroy()
     */
    @Override
    public void destroy() {
        // Initiates orderly shutdown and don't accept creation of new threads
        threadPool.shutdown();
        if (threadPool.getActiveCount() > 0) {
            // Stop actively executing threads NOW!
            List<Runnable> stoppedThreads = threadPool.shutdownNow();
            for (Runnable runnable : stoppedThreads) {
                log.info("Shutting down acive thread: " + runnable.toString());
            }
        }

    }

}
