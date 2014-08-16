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
package specification.notification.subscription;

import java.util.ArrayList;
import java.util.List;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.olat.lms.core.notification.impl.metric.AverageEmailSuccessRateMetricTestHelper;
import org.olat.lms.core.notification.impl.metric.NotifyStatistics;

/**
 * Initial Date: Nov 24, 2011 <br>
 * 
 * @author patrick
 */
@RunWith(ConcordionRunner.class)
public class SubscriptionTest {

    private List<NotifyStatistics> notifyStatisticsSetup = new ArrayList<NotifyStatistics>();

    public void addNotifyStatistics(String numberOfSent, String numberOfFailed) {
        int intValueNumberOfSent = Integer.valueOf(numberOfSent).intValue();
        int intValueNumberOfFailed = Integer.valueOf(numberOfFailed).intValue();
        NotifyStatistics statistics = new NotifyStatistics();
        statistics.setFailedCounter(intValueNumberOfFailed);
        statistics.setDeliveredCounter(intValueNumberOfSent - intValueNumberOfFailed);
        notifyStatisticsSetup.add(statistics);
    }

    public String getEmailSuccessRate() {
        AverageEmailSuccessRateMetricTestHelper averageEmailSuccessRateMetricTestHelper = new AverageEmailSuccessRateMetricTestHelper();
        averageEmailSuccessRateMetricTestHelper.setNotifyStatistics(notifyStatisticsSetup);
        notifyStatisticsSetup = new ArrayList<NotifyStatistics>();
        return String.valueOf(averageEmailSuccessRateMetricTestHelper.processStatistics());
    }

}
