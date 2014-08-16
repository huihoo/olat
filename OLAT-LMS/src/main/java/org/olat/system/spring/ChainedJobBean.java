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
package org.olat.system.spring;

import org.apache.log4j.Logger;
import org.olat.system.logging.log4j.LoggerHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * This is a Job which expects to schedule a nextJob.
 * 
 * Initial Date: 08.05.2012 <br>
 * 
 * @author lavinia
 */
public abstract class ChainedJobBean extends QuartzJobBean {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String JOB_DATA_MAP_NEXT_JOB_KEY = "nextJob";

    /**
     * get the next job, if any configured via jobDataAsMap property, and schedule it.
     */
    protected void executeNextJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String[] keys = jobExecutionContext.getMergedJobDataMap().getKeys();
        log.info("ChainedJobBean - keys: " + keys.length);

        if (hasNextJob(keys)) {
            String nextJobClassName = (String) jobExecutionContext.getMergedJobDataMap().get(JOB_DATA_MAP_NEXT_JOB_KEY);
            try {
                Class<?> theClass = Class.forName(nextJobClassName);
                QuartzJobBean quartzJobBean = (QuartzJobBean) theClass.newInstance();
                log.info("ChainedJobBean - execute next job");
                quartzJobBean.execute(jobExecutionContext);

            } catch (ClassNotFoundException e) {
                log.error(e);
            } catch (InstantiationException e) {
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error(e);
            }
        }

    }

    private boolean hasNextJob(String[] keys) {
        boolean hasNextJob = false;
        for (String key : keys) {
            if (JOB_DATA_MAP_NEXT_JOB_KEY.equals(key)) {
                hasNextJob = true;
                break;
            }
        }
        return hasNextJob;
    }
}
