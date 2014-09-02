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
package org.olat.lms.admin.jmx;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.lms.commons.scheduler.JobWithDB;
import org.olat.system.logging.log4j.LoggerHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br>
 * a job which regularly dumps jmx data to the log
 * 
 * @author Felix Jost
 */
public class DumpJMXJob extends JobWithDB {

    private static final Logger log = LoggerHelper.getLogger();

    @Override
    public void executeWithDB(final JobExecutionContext context) throws JobExecutionException {
        final boolean enabled = context.getMergedJobDataMap().getBooleanFromString("enabled");
        final String[] keys = context.getMergedJobDataMap().getKeys();
        // loop over all
        for (int i = 0; i < keys.length; i++) {
            final String key = keys[i];
            if (key.endsWith("Bean")) {
                // ok, key is a bean name => dump this bean
                final String beanName = context.getMergedJobDataMap().getString(key);
                if (enabled) {
                    final List<String> jmxDumpList = JMXManager.getInstance().dumpJmx(beanName);
                    final StringBuilder buf = new StringBuilder();
                    for (final Iterator iterator = jmxDumpList.iterator(); iterator.hasNext();) {
                        final String jmxDump = (String) iterator.next();
                        buf.append(jmxDump);
                        buf.append(";");
                    }
                    log.info(key + ":" + buf.toString());
                }

            }
        }
    }

}
