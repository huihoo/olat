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
package org.olat.lms.core.notification.impl.metric;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.olat.system.commons.date.DateUtil;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * JMX service metric bean responsible for measurement success of sent emails
 * 
 * Initial Date: 21.12.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@ManagedResource(objectName = "org.olat.lms.core.notification.impl.metric:name=averageEmailSuccessRateMetric", description = "Average Email Success Rate Service Metric Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "AverageEmailSuccessRateMetric", persistName = "AverageEmailSuccessRateMetric")
public class AverageEmailSuccessRateMetric extends NotificationServiceMetric<NotifyStatistics> {

    private final AtomicInteger numberOfFailedEmails = new AtomicInteger(0);
    private final AtomicInteger numberOfSentEmails = new AtomicInteger(0);
    private final AtomicLong timeStampMillis = new AtomicLong(0);
    private final AtomicBoolean finished = new AtomicBoolean(true);

    @Override
    protected void update(NotifyStatistics notifyStatistics) {
        setNumberOfNotDeliveredEmails(notifyStatistics.getFailedCounter());
        setNumberOfSentEmails(notifyStatistics.getTotalCounter());
        if (notifyStatistics.getTimeStamp() != null) {
            timeStampMillis.set(notifyStatistics.getTimeStamp().getTime());
        }
        finished.set(notifyStatistics.isFinished());
    }

    @ManagedAttribute(description = "Average of succeeded mails")
    public double getAverageEmailSuccessRate() {
        return calculateRate();
    }

    private double calculateRate() {
        if (numberOfSentEmails.get() == 0) {
            return 1;
        }
        return new BigDecimal(1).subtract(new BigDecimal(numberOfFailedEmails.get()).divide(new BigDecimal(numberOfSentEmails.get()))).doubleValue();

    }

    private void setNumberOfNotDeliveredEmails(int numberOfFailedEmails) {
        this.numberOfFailedEmails.addAndGet(numberOfFailedEmails);
    }

    private void setNumberOfSentEmails(int numberOfSentEmails) {
        this.numberOfSentEmails.addAndGet(numberOfSentEmails);
    }

    @ManagedAttribute(description = "number of sent emails since last server start")
    public int getAllSent() {
        return numberOfSentEmails.get();
    }

    @ManagedAttribute(description = "time stamp of the last notification sent")
    public String getLastNotificationTimeStamp() {
        if (timeStampMillis.get() == 0) {
            return "no notification since server start";
        }
        long mills = timeStampMillis.get();
        return DateUtil.extractDate(new Date(mills), Locale.GERMAN) + " " + DateUtil.extractTime(new Date(mills), Locale.GERMAN);
    }

    @ManagedAttribute(description = "job was finished or not started")
    public boolean isJobFinishedOrNotStartedYet() {
        return finished.get();
    }

}
