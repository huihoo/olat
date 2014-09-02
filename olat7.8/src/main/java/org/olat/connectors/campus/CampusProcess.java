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
package org.olat.connectors.campus;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

/**
 * This class serves as a generic targetObject for the quartz MethodInvokingJobDetailFactoryBean. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusProcess {

    private static final Logger LOG = LoggerHelper.getLogger();

    private JobLauncher jobLauncher;

    private Job job;

    private Map<String, JobParameter> parameters;

    private final String PROCESS_DISABLED = "disabled";

    /**
     * Sets the Map of the JobParameters needed to run the job
     * 
     * @param parameters
     *            the map of the JobParameters
     */
    public void setParameters(Map<String, JobParameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the JobLauncher needed to run the job
     * 
     * @param jobLauncher
     *            the JobLauncher
     */
    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    /**
     * Sets the job needed to be run
     * 
     * @param job
     *            the Job
     */
    public void setJob(Job job) {
        this.job = job;
    }

    @PostConstruct
    public void init() {
        LOG.info("JobParameters: [" + parameters + "]");
    }

    /**
     * Delegates the actual launching of the given job to the given jobLauncher <br>
     * only in the case that the process is in the enabled status.
     * 
     * @param status
     *            the status indicating whether the job is enabled or disabled
     * @param campusProcess
     *            the name of the process
     */
    public void process(String status, String campusProcess) throws Exception {
        LOG.info("THE " + campusProcess + " IS: [" + status + "]");

        if (PROCESS_DISABLED.equalsIgnoreCase(status)) {
            return;
        }
        parameters.put("run.ts", new JobParameter(System.currentTimeMillis()));

        jobLauncher.run(job, new JobParameters(parameters));

    }
}
