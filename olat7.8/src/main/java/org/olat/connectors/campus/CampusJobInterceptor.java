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

import org.olat.lms.core.course.campus.impl.metric.CampusNotifier;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 08.01.2014 <br>
 * 
 * @author aabouc
 */
public class CampusJobInterceptor implements JobExecutionListener {

    @Autowired
    private CampusNotifier campusNotifier;

    @Override
    public void afterJob(JobExecution je) {
        campusNotifier.notifyJobExecution(je);
    }

    @Override
    public void beforeJob(JobExecution je) {
    }

}
