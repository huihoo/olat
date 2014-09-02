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
package org.olat.lms.course.access.notification.job;

import org.apache.log4j.Logger;
import org.olat.lms.course.access.CourseAccessManager;
import org.olat.lms.course.access.notification.NotificationCourseAccessManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.ChainedJobBean;
import org.olat.system.spring.CoreSpringFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * @author Branislav Balaz
 */
public class NotifySynchronizePublishersJob extends ChainedJobBean {
    private static final Logger log = LoggerHelper.getLogger();

    @Override
    protected final void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("NotifySynchronizePublishersJob started");
        long startTime = System.currentTimeMillis();
        getCourseAccessManager().execute();
        long durationTimeInMillis = System.currentTimeMillis() - startTime;
        log.info("NotifySynchronizePublishersJob finished, durationTimeInMillis=" + durationTimeInMillis);

        executeNextJob(jobExecutionContext);
    }

    // TODO: How we could inject the notification-service directly
    private CourseAccessManager getCourseAccessManager() {
        return CoreSpringFactory.getBean(NotificationCourseAccessManager.class);
    }

}
