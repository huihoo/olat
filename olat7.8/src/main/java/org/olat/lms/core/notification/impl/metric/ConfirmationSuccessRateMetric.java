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

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 09.11.2012 <br>
 * 
 * @author lavinia
 */
@Component
@ManagedResource(objectName = "org.olat.lms.core.notification.impl.metric:name=confirmationSuccessRateMetric", description = "Confirmation Emails Success Rate Service Metric Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "ConfirmationSuccessRateMetric", persistName = "ConfirmationSuccessRateMetric")
public class ConfirmationSuccessRateMetric extends ConfirmationServiceMetric<ConfirmationStatistics> {

    private final AtomicInteger numberOfFailedEmails = new AtomicInteger(0);
    private final AtomicInteger numberOfSentEmails = new AtomicInteger(0);

    @Override
    protected void update(ConfirmationStatistics serviceContext) {
        addNumberOfSentEmails(serviceContext.getTotalCounter());
        addNumberOfNotDeliveredEmails(serviceContext.getFailedCounter());
    }

    private void addNumberOfNotDeliveredEmails(int delta) {
        numberOfFailedEmails.addAndGet(delta);
    }

    private void addNumberOfSentEmails(int delta) {
        numberOfSentEmails.addAndGet(delta);
    }

    @ManagedAttribute(description = "Average of succeed mails")
    public double getAverageConfirmationSuccessRate() {
        return calculateRate();
    }

    private double calculateRate() {
        if (numberOfSentEmails.get() == 0) {
            return 1; // is 1 better?
        }
        // double rate = new BigDecimal(1).subtract(new BigDecimal(numberOfFailedEmails.get()).divide(new BigDecimal(numberOfSentEmails.get()))).doubleValue();
        double rate = Double.valueOf(numberOfSentEmails.get() - numberOfFailedEmails.get()) / Double.valueOf(numberOfSentEmails.get());
        return rate;
    }

    @ManagedAttribute(description = "number of failed mails")
    public int getSentFailed() {
        return numberOfFailedEmails.get();
    }

    @ManagedAttribute(description = "number of all sent mails")
    public int getAllSent() {
        return numberOfSentEmails.get();
    }

}
