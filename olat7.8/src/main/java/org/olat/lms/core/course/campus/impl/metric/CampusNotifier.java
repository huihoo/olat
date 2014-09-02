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
package org.olat.lms.core.course.campus.impl.metric;

import java.util.List;

import org.olat.lms.core.CoreBaseService;
import org.olat.lms.core.course.campus.impl.mapper.OverallUserMapperStatistic;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 11.07.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class CampusNotifier extends CoreBaseService<CampusServiceMetric<CampusServiceContext>, CampusServiceContext> {

    @Autowired
    @Override
    protected void setMetrics(List<CampusServiceMetric<CampusServiceContext>> metrics) {
        for (CampusServiceMetric<CampusServiceContext> metric : metrics) {
            attach(metric);
        }
    }

    public List<CampusServiceMetric<CampusServiceContext>> getMetrics() {
        return metrics;
    }

    public void notifyStartOfImportProcess() {
        notifyMetrics(new CampusStatistics(true));
    }

    public void notifyExportStatus(CampusStatistics.EXPORT_STATUS exportStatus) {
        notifyMetrics(new CampusStatistics(exportStatus));
    }

    public void notifyJobExecution(JobExecution jobExecution) {
        notifyMetrics(new CampusStatistics(jobExecution));
    }

    public void notifyStepExecution(StepExecution stepExecution) {
        notifyMetrics(new CampusStatistics(stepExecution));
    }

    public void notifyUserMapperStatistic(OverallUserMapperStatistic overallUserMapperStatistic) {
        notifyMetrics(new CampusStatistics(overallUserMapperStatistic));
    }

}
