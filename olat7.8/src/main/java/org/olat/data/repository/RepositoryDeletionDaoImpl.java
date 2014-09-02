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

package org.olat.data.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.reference.ReferenceDao;
import org.olat.data.resource.OLATResourceManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Initial Date: 1.11.2011
 * 
 * @author Christian Guretzki
 */
@Repository
public class RepositoryDeletionDaoImpl implements RepositoryDeletionDao {

    private static final Logger log = LoggerHelper.getLogger();
    @Autowired
    BaseSecurity baseSecurity;

    private RepositoryDeletionDaoImpl() {
    }

    @Override
    public List<RepositoryEntry> getDeletableRepositoryEntries(final int lastLoginDuration) {
        final Calendar lastUsageLimit = Calendar.getInstance();
        lastUsageLimit.add(Calendar.MONTH, -lastLoginDuration);
        log.debug("lastLoginLimit=" + lastUsageLimit);

        // 1. get all ReprositoryEntries with lastusage > x
        String query = "select re from RepositoryEntry as re " + " where (re.lastUsage = null or re.lastUsage < :lastUsage)" + " and re.olatResource != null ";
        DBQuery dbq = DBFactory.getInstance().createQuery(query);
        dbq.setDate("lastUsage", lastUsageLimit.getTime());
        @SuppressWarnings("unchecked")
        final List<RepositoryEntry> reprositoryEntries = dbq.list();
        // 2. get all ReprositoryEntries in deletion-process (email send)
        query = "select re from RepositoryEntry as re" + " , org.olat.data.lifecycle.LifeCycleEntry as le" + " where re.key = le.persistentRef "
                + " and re.olatResource != null " + " and le.persistentTypeName ='" + RepositoryEntry.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' ";
        dbq = DBFactory.getInstance().createQuery(query);
        @SuppressWarnings("unchecked")
        final List<RepositoryEntry> groupsInProcess = dbq.list();
        // 3. Remove all ReprositoryEntries in deletion-process from all unused-ReprositoryEntries
        reprositoryEntries.removeAll(groupsInProcess);
        return filterRepositoryWithReferences(reprositoryEntries);
    }

    private List filterRepositoryWithReferences(final List repositoryList) {
        log.debug("filterRepositoryWithReferences repositoryList.size=" + repositoryList.size());
        final List filteredList = new ArrayList();
        int loopCounter = 0;
        for (final Iterator iter = repositoryList.iterator(); iter.hasNext();) {
            final RepositoryEntry repositoryEntry = (RepositoryEntry) iter.next();
            log.debug("filterRepositoryWithReferences repositoryEntry=" + repositoryEntry);
            log.debug("filterRepositoryWithReferences repositoryEntry.getOlatResource()=" + repositoryEntry.getOlatResource());
            if (OLATResourceManager.getInstance().findResourceable(repositoryEntry.getOlatResource()) != null) {
                if (ReferenceDao.getInstance().getReferencesTo(repositoryEntry.getOlatResource()).size() == 0) {
                    filteredList.add(repositoryEntry);
                    log.debug("filterRepositoryWithReferences add to filteredList repositoryEntry=" + repositoryEntry);
                } else {
                    // repositoryEntry has references, can not be deleted
                    log.debug("filterRepositoryWithReferences Do NOT add to filteredList repositoryEntry=" + repositoryEntry);
                    if (LifeCycleManager.createInstanceFor(repositoryEntry).lookupLifeCycleEntry(SEND_DELETE_EMAIL_ACTION) != null) {
                        LifeCycleManager.createInstanceFor(repositoryEntry).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
                        log.info("filterRepositoryWithReferences: found repositoryEntry with references, remove from deletion-process repositoryEntry=" + repositoryEntry);
                    }
                }
            } else {
                log.error("filterRepositoryWithReferences, could NOT found Resourceable for repositoryEntry=" + repositoryEntry, null);
            }
            if (loopCounter++ % 100 == 0) {
                DBFactory.getInstance().intermediateCommit();
            }
        }
        log.debug("filterRepositoryWithReferences filteredList.size=" + filteredList.size());
        return filteredList;
    }

    @Override
    public List<RepositoryEntry> getRepositoryEntriesInDeletionProcess(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select re from RepositoryEntry as re" + " , org.olat.data.lifecycle.LifeCycleEntry as le" + " where re.key = le.persistentRef "
                + " and re.olatResource != null " + " and le.persistentTypeName ='" + RepositoryEntry.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp >= :deleteEmailDate ";
        final DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        return filterRepositoryWithReferences(dbq.list());
    }

    @Override
    public List<RepositoryEntry> getRepositoryEntriesReadyToDelete(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select re from RepositoryEntry as re" + " , org.olat.data.lifecycle.LifeCycleEntry as le" + " where re.key = le.persistentRef "
                + " and re.olatResource != null " + " and le.persistentTypeName ='" + RepositoryEntry.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp < :deleteEmailDate ";
        final DBQuery dbq = DBFactory.getInstance().createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        return filterRepositoryWithReferences(dbq.list());
    }

}
