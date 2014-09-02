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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.system.commons.taskexecutor;

import org.apache.log4j.Logger;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Generic task executor to run tasks in it's own threads. Use it to decouple stuff that might takes more time than a user may is willing to wait. The task gets executed
 * immediately by a thread pool. If you look for scheduled task search spring files for cron expressions
 * <P>
 * Initial Date: 02.05.2007 <br>
 * 
 * @author guido
 */
public class TaskExecutorManager extends BasicManager {
    ThreadPoolTaskExecutor taskExecutor;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * [used by spring]
     */
    private TaskExecutorManager(ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.taskExecutor = threadPoolTaskExecutor;
    }

    /**
     * runs the task and wraps it in a new runnable to catch uncatched errors and may close db sessions used in the task.
     * 
     * @param task
     */
    public void runTask(final Runnable task) {
        if (taskExecutor != null) {
            taskExecutor.runTask(task);
        } else {
            log.error("taskExecutor is not initialized (taskExecutor=null). Do not call 'runTask' before TaskExecutor is initialized.");
            throw new AssertException("taskExecutor is not initialized");
        }
    }

    /**
     * TODO: to be used with GUI where the programmer can start a task and set a message that will appear like
     * "your report will be generated and you will get an email if finished" or an icon that gets updated when the task is done and the user can go on with his work
     * 
     * @param task
     */
    public void runTaskWithNotificationWhenFinised(final Runnable task) {
        throw new NoSuchMethodError();
    }

}
