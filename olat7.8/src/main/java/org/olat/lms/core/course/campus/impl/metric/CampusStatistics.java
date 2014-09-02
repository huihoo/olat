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

import org.olat.lms.core.course.campus.impl.mapper.OverallUserMapperStatistic;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * Initial Date: 11.07.2012 <br>
 * 
 * @author aabouc
 */
public class CampusStatistics implements CampusServiceContext {

    private EXPORT_STATUS exportStatus;

    private boolean startOfImport = false;

    private OverallUserMapperStatistic overallUserMapperStatistic;

    private JobExecution jobExecution;
    private StepExecution stepExecution;

    public enum EXPORT_STATUS {
        OK, INCOMPLETE_EXPORT, NO_EXPORT;
    }

    public CampusStatistics(boolean startOfImport) {
        this.startOfImport = startOfImport;
    }

    public CampusStatistics(EXPORT_STATUS exportStatus) {
        this.exportStatus = exportStatus;
    }

    public CampusStatistics(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public CampusStatistics(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public CampusStatistics(OverallUserMapperStatistic overallUserMapperStatistic) {
        this.overallUserMapperStatistic = overallUserMapperStatistic;
    }

    public EXPORT_STATUS getExportStatus() {
        return exportStatus;
    }

    public OverallUserMapperStatistic getOverallUserMapperStatistic() {
        return overallUserMapperStatistic;
    }

    public boolean isStartOfImport() {
        return startOfImport;
    }

    public JobExecution getJobExecution() {
        return jobExecution;
    }

    public StepExecution getStepExecution() {
        return stepExecution;
    }

}
