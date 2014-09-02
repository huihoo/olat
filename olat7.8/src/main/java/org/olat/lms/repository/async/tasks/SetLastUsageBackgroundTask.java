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
package org.olat.lms.repository.async.tasks;

import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.repository.RepositoryDeletionDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.async.AbstractBackgroundTask;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author Christian Guretzki
 */
public class SetLastUsageBackgroundTask extends AbstractBackgroundTask {

    private static final Logger log = LoggerHelper.getLogger();

    private final RepositoryEntry repositoryEntry;

    public SetLastUsageBackgroundTask(final RepositoryEntry repositoryEntry) {
        this.repositoryEntry = repositoryEntry;
    }

    @Override
    public void executeTask() {
        log.debug("SetLastUsageBackgroundTask executing with repositoryEntry=" + repositoryEntry);
        // this code must not be synchronized because in case of exception we try it again
        // this code must not have any error handling or retry, this will be done in super class
        if (RepositoryServiceImpl.getInstance().lookupRepositoryEntry(repositoryEntry.getKey()) != null) {
            final RepositoryEntry reloadedRe = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry, true);
            reloadedRe.setLastUsage(new Date());
            final LifeCycleManager lcManager = LifeCycleManager.createInstanceFor(reloadedRe);
            if (lcManager.lookupLifeCycleEntry(RepositoryDeletionDao.SEND_DELETE_EMAIL_ACTION) != null) {
                log.info("Audit:Repository-Deletion: Remove from delete-list repositoryEntry=" + reloadedRe);
                lcManager.deleteTimestampFor(RepositoryDeletionDao.SEND_DELETE_EMAIL_ACTION);
            }
            RepositoryServiceImpl.getInstance().updateRepositoryEntry(reloadedRe);
        } else {
            log.info("Could not executeTask, because repositoryEntry does no longer exist");
        }
    }

}
