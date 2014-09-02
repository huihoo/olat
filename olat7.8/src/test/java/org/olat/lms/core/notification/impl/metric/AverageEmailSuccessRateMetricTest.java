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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date: 22.12.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class AverageEmailSuccessRateMetricTest {

    private AverageEmailSuccessRateMetricTestHelper averageEmailSuccessRateMetricTestHelper;

    @Before
    public void setup() {
        averageEmailSuccessRateMetricTestHelper = new AverageEmailSuccessRateMetricTestHelper();

    }

    @Test
    public void averageEmailSuccessRate_oneUpdate_correct_41Percent() {
        List<NotifyStatistics> notifyStatistics = new ArrayList<NotifyStatistics>();
        NotifyStatistics statistics = new NotifyStatistics();
        statistics.setFailedCounter(59);
        statistics.setDeliveredCounter(41);
        notifyStatistics.add(statistics);
        averageEmailSuccessRateMetricTestHelper.setNotifyStatistics(notifyStatistics);
        assertTrue(0.41d == averageEmailSuccessRateMetricTestHelper.processStatistics());
    }

    @Test
    public void averageEmailSuccessRate_threeUpdates_correct_95Percent() {
        List<NotifyStatistics> notifyStatistics = new ArrayList<NotifyStatistics>();

        notifyStatistics.add(createStatistic(95, 5));
        notifyStatistics.add(createStatistic(177, 23));
        notifyStatistics.add(createStatistic(298, 2));
        averageEmailSuccessRateMetricTestHelper.setNotifyStatistics(notifyStatistics);
        assertTrue(0.95d == averageEmailSuccessRateMetricTestHelper.processStatistics());
    }

    @Test
    public void averageEmailSuccessRate_ZeroEmailsSent() {
        List<NotifyStatistics> notifyStatistics = new ArrayList<NotifyStatistics>();
        averageEmailSuccessRateMetricTestHelper.setNotifyStatistics(notifyStatistics);
        assertTrue(1.0d == averageEmailSuccessRateMetricTestHelper.processStatistics());
    }

    private NotifyStatistics createStatistic(int delivered, int failed) {
        NotifyStatistics statistics = new NotifyStatistics();
        statistics.setDeliveredCounter(delivered);
        statistics.setFailedCounter(failed);

        return statistics;
    }

}
