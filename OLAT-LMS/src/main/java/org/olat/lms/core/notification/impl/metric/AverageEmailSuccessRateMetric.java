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
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 21.12.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
@ManagedResource(objectName = "org.olat.lms.core.notification.impl.metric:name=averageEmailSuccessRateMetric", description = "Average Email Success Rate Service Metric Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "AverageEmailSuccessRateMetric", persistName = "AverageEmailSuccessRateMetric")
public class AverageEmailSuccessRateMetric extends NotificationServiceMetric<NotifyStatistics> {

    private final AtomicInteger numberOfFailedEmails = new AtomicInteger(0);
    private final AtomicInteger numberOfSentEmails = new AtomicInteger(0);

    @Override
    protected void update(NotifyStatistics notifyStatistics) {
        setNumberOfNotDeliveredEmails(notifyStatistics.getFailedCounter());
        setNumberOfSentEmails(notifyStatistics.getTotalCounter());
    }

    @ManagedAttribute(description = "Average of succeed mails")
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
        for (int i = 0; i < numberOfFailedEmails; i++) {
            this.numberOfFailedEmails.incrementAndGet();
        }
    }

    private void setNumberOfSentEmails(int numberOfSentEmails) {
        for (int i = 0; i < numberOfSentEmails; i++) {
            this.numberOfSentEmails.incrementAndGet();
        }
    }

}
