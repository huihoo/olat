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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.lms.security.authentication.ldap;

import org.apache.log4j.Logger;
import org.olat.lms.commons.scheduler.JobWithDB;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br>
 * This Job Bean synchronizes the LDAP users
 * <P>
 * Initial Date: 20.08.2008 <br>
 * 
 * @author gnaegi
 */
public class LDAPUserSynchronizerJob extends JobWithDB {

    private static final Logger log = LoggerHelper.getLogger();

    /**
	 */
    @Override
    public void executeWithDB(final JobExecutionContext arg0) throws JobExecutionException {
        try {
            log.info("Starting LDAP user synchronize job");
            final LDAPError errors = new LDAPError();
            final LDAPLoginManager ldapLoginManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
            if (ldapLoginManager.doBatchSync(errors)) {
                log.info("LDAP user synchronize job finished successfully");
            } else {
                log.info("LDAP user synchronize job finished with errors::" + errors.get());
            }
        } catch (final Exception e) {
            // ups, something went completely wrong! We log this but continue next time
            log.error("Erron while synchronizeing LDAP users", e);
        }
        // db closed by JobWithDB class
    }

}
