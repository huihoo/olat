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

import java.util.List;

/**
 * Initial Date: 06.01.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class AverageEmailSuccessRateMetricTestHelper {

    private AverageEmailSuccessRateMetric averageEmailSuccessRateMetric;
    private List<NotifyStatistics> notifyStatistics;

    public AverageEmailSuccessRateMetricTestHelper() {
        averageEmailSuccessRateMetric = new AverageEmailSuccessRateMetric();
    }

    public void setNotifyStatistics(List<NotifyStatistics> notifyStatistics) {
        this.notifyStatistics = notifyStatistics;
    }

    public double processStatistics() {

        for (NotifyStatistics notifyStatistic : notifyStatistics) {
            averageEmailSuccessRateMetric.update(notifyStatistic);
        }

        return averageEmailSuccessRateMetric.getAverageEmailSuccessRate();
    }

}
