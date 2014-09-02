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
package org.olat.lms.core.notification.impl.job;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * responsible for executing email notification job
 * 
 * @author Christian Guretzki
 */
public class NotifySubscriberJob extends QuartzJobBean {
    private static final Logger log = LoggerHelper.getLogger();

    @Override
    protected final void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
        log.info("NotifySubscriberJob started");
        long startTime = System.currentTimeMillis();
        getNotificationService().startNotificationJob();

        NotifyStatistics notifyStatistics = new NotifyStatistics();
        List<Long> subscriberIds = getNotificationService().getSubscribersIds();
        Iterator<Long> subscribersIdsIter = subscriberIds.iterator();
        while (subscribersIdsIter.hasNext()) {
            notifyStatistics.add(getNotificationService().notifySubscriber(subscribersIdsIter.next()));
        }

        getNotificationService().finishNotificationJob();
        int delivered = notifyStatistics.getDeliveredCounter();
        long durationTimeInMillis = System.currentTimeMillis() - startTime;
        log.info("NotifySubscriberJob finished, delivered: " + delivered + " durationTimeInMillis=" + durationTimeInMillis);
    }

    // TODO: How we could inject the notification-service directly
    private NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

}
