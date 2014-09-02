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
package org.olat.lms.commons.taskexecutor;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.commons.taskexecutor.TaskExecutorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TaskExecutorServiceImpl
 * 
 * <P>
 * Initial Date: 03.05.2011 <br>
 * 
 * @author guido
 */
@Service
public class TaskExecutorServiceImpl implements TaskExecutorService {

    protected TaskExecutorServiceImpl() {
    }

    @Autowired
    TaskExecutorManager taskExecutorManager;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * @see org.olat.lms.commons.taskexecutor.TaskExecutorService#runTask(java.lang.Runnable)
     */
    @Override
    public void runTask(final Runnable task) {
        // wrap call to the task here to catch all errors that are may not catched yet in the task itself
        // like outOfMemory or other system errors.
        Runnable safetask = new Runnable() {

            @Override
            public void run() {
                try {
                    task.run();
                    DBFactory.getInstance().commitAndCloseSession();
                } catch (Throwable e) {
                    DBFactory.getInstance().rollbackAndCloseSession();
                    log.error("Error while running task in a separate thread.", e);
                }
            }
        };
        taskExecutorManager.runTask(safetask);
    }

}
