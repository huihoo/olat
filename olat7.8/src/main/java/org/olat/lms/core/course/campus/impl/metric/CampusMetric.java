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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.olat.connectors.campus.CampusProcessJob;
import org.olat.connectors.campus.CampusProcessStep;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 11.07.2012 <br>
 * 
 * @author aabouc
 */
@Component
@ManagedResource(objectName = "org.olat.lms.core.course.campus.metric:name=campusMetric", description = "Campus-Import Service Metric Bean", log = true, logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200, persistLocation = "campusMetric", persistName = "CampusMetric")
public class CampusMetric extends CampusServiceMetric<CampusStatistics> {

    private String exportStatus;

    private Map<String, JobExecution> jobs = new LinkedHashMap<String, JobExecution>();
    private Map<String, StepExecution> steps = new LinkedHashMap<String, StepExecution>();

    private String lecturersMappingStatistic;
    private String studentsMappingStatistic;

    @Override
    protected void update(CampusStatistics campusStatistics) {
        if (campusStatistics.getExportStatus() != null) {
            setExportStatus(campusStatistics.getExportStatus().name());
        }

        if (campusStatistics.isStartOfImport()) {
            steps.clear();
            // importStep2Status.clear();
        }

        if (campusStatistics.getJobExecution() != null) {
            jobs.put(campusStatistics.getJobExecution().getJobInstance().getJobName().toLowerCase(), campusStatistics.getJobExecution());
        }

        if (campusStatistics.getStepExecution() != null) {
            steps.put(campusStatistics.getStepExecution().getStepName().toLowerCase(), campusStatistics.getStepExecution());
        }

        if (campusStatistics.getOverallUserMapperStatistic() != null) {
            if (campusStatistics.getOverallUserMapperStatistic().getLecturersMappingStatistic() != null) {
                setUserMappingProcessStatisticForLecturers(campusStatistics.getOverallUserMapperStatistic().getLecturersMappingStatistic().toStringForLecturerMapping());
            }

            if (campusStatistics.getOverallUserMapperStatistic().getStudentMappingStatistic() != null) {
                setUserMappingProcessStatisticForStudents(campusStatistics.getOverallUserMapperStatistic().getStudentMappingStatistic().toStringForStudentMapping());
            }
        }
    }

    @ManagedAttribute(description = "The Status of the export")
    public String getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(String exportStatus) {
        this.exportStatus = exportStatus;
    }

    @ManagedAttribute(description = "The Job of the Import Process")
    public String getImportProcessJob() {
        return (formatJobExecution(jobs.get(CampusProcessJob.IMPORTJOB.name().toLowerCase())));
    }

    @ManagedAttribute(description = "The Steps of the Import Process")
    public String getImportProcessSteps() {
        return getStep2StausForJob(CampusProcessJob.IMPORTJOB.name().toLowerCase()).toString();
    }

    @ManagedAttribute(description = "The job for the UserMapping and Syncronisation Process")
    public String getUserMappingProcessJob() {
        return (formatJobExecution(jobs.get(CampusProcessJob.USERMAPPINGJOB.name().toLowerCase())));
    }

    @ManagedAttribute(description = "The Steps of the UserMapping Process")
    public String getUserMappingProcessSteps() {
        return getStep2StausForJob(CampusProcessJob.USERMAPPINGJOB.name().toLowerCase()).toString();
    }

    private Map<String, String> getStep2StausForJob(String jobName) {
        Collection<StepExecution> stepExecutions = jobs.get(jobName).getStepExecutions();

        Map<String, String> Step2Status = new LinkedHashMap<String, String>();

        for (StepExecution se : stepExecutions) {
            Step2Status.put(se.getStepName(), se.getStatus().toString());
        }

        return Step2Status;
    }

    @ManagedAttribute(description = "The importProcessStep regarding import of controlFile")
    public String getImportProcessStepImportControlFile() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_CONTROLFILE.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The importProcessStep regarding import of courses")
    public String getImportProcessStepImportCourses() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_COURSES.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The importProcessStep regarding the import of students")
    public String getImportProcessStepImportStudents() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_STUDENTS.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The importProcessStep regarding the import of lecturers")
    public String getImportProcessStepImportLecturers() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_LECTURERS.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The importProcessStep regarding the import of texts")
    public String getImportProcessStepImportTexts() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_TEXTS.name().toLowerCase()));
    }

    // @ManagedAttribute(description = "The importProcessStep regarding the import of events")
    // public String getImportProcessStepImportEvents() {
    // return formatStepExecution(steps.get(CampusProcessStep.IMPORT_EVENTS.name().toLowerCase()));
    // }

    @ManagedAttribute(description = "The importProcessStep regarding the import of the lecturersCourses booking")
    public String getImportProcessStepImportLecturersCourses() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_LECTURERS_COURSES.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The importProcessStep regarding the import of the studentsCourses booking")
    public String getImportProcessStepImportStudentsCourses() {
        return formatStepExecution(steps.get(CampusProcessStep.IMPORT_STUDENTS_COURSES.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The UserMappingProcessStep regarding the synchronisation")
    public String getUserMappingProcessStepSynchronisation() {
        return formatStepExecution(steps.get(CampusProcessStep.CAMPUSSYNCHRONISATION.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The UserMappingProcessStep regarding the StudentMapping")
    public String getUserMappingProcessStepStudentMapping() {
        return formatStepExecution(steps.get(CampusProcessStep.STUDENTMAPPING.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The UserMappingProcessStep regarding the LecturerMapping")
    public String getUserMappingProcessStepLecturerMapping() {
        return formatStepExecution(steps.get(CampusProcessStep.LECTURERMAPPING.name().toLowerCase()));
    }

    @ManagedAttribute(description = "The statisic of the lecturers user mapping")
    public String getUserMappingProcessStatisticForLecturers() {
        return lecturersMappingStatistic;
    }

    public void setUserMappingProcessStatisticForLecturers(String lecturersMappingStatistic) {
        this.lecturersMappingStatistic = lecturersMappingStatistic;
    }

    @ManagedAttribute(description = "The statisic of the students user mapping )")
    public String getUserMappingProcessStatisticForStudents() {
        return studentsMappingStatistic;
    }

    public void setUserMappingProcessStatisticForStudents(String studentsMappingStatistic) {
        this.studentsMappingStatistic = studentsMappingStatistic;
    }

    private String formatJobExecution(JobExecution je) {
        return String.format("status=%s, startTime=%s, endTime=%s", je.getStatus(), je.getStartTime(), je.getEndTime());
    }

    private String formatStepExecution(StepExecution se) {
        return String.format("status=%s, startTime=%s, endTime=%s, readSkipCount=%s, writeSkipCount=%s, processSkipCount=%s", se.getStatus(), se.getStartTime(),
                se.getEndTime(), se.getReadSkipCount(), se.getWriteSkipCount(), se.getProcessSkipCount());
    }

}
