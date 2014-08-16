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

package org.olat.data.user.delete;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO for user-deletion.
 * 
 * @author Christian Guretzki
 */
@Repository
public class UserDeletionDaoImpl extends BasicManager implements UserDeletionDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    CoordinatorManager coordinatorManager;
    @Autowired
    DB database;

    /**
     * [used by spring]
     */
    private UserDeletionDaoImpl() {
    }

    @Override
    public void markSendEmailEvent(Identity identity) {
        identity = (Identity) database.loadObject(identity);
        LifeCycleManager.createInstanceFor(identity).markTimestampFor(SEND_DELETE_EMAIL_ACTION);
        database.updateObject(identity);
    }

    /**
     * Return list of identities which have last-login older than 'lastLoginDuration' parameter. This user are ready to start with user-deletion process.
     * 
     * @param lastLoginDuration
     *            last-login duration in month
     * @return List of Identity objects
     */
    @Override
    public List getDeletableIdentities(final int lastLoginDuration) {
        final Calendar lastLoginLimit = Calendar.getInstance();
        lastLoginLimit.add(Calendar.MONTH, -lastLoginDuration);
        log.debug("lastLoginLimit=" + lastLoginLimit);
        // 1. get all 'active' identities with lastlogin > x
        String queryStr = "from org.olat.data.basesecurity.Identity as ident where ident.status=" + Identity.STATUS_ACTIV
                + " and (ident.lastLogin = null or ident.lastLogin < :lastLogin)";
        DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("lastLogin", lastLoginLimit.getTime());
        final List identities = dbq.list();
        // 2. get all 'active' identities in deletion process
        queryStr = "select ident from org.olat.data.basesecurity.Identity as ident" + " , org.olat.data.lifecycle.LifeCycleEntry as le"
                + " where ident.key = le.persistentRef " + " and le.persistentTypeName ='" + IdentityImpl.class.getSimpleName() + "'" + " and le.action ='"
                + SEND_DELETE_EMAIL_ACTION + "' ";
        dbq = database.createQuery(queryStr);
        final List identitiesInProcess = dbq.list();
        // 3. Remove all identities in deletion-process from all inactive-identities
        identities.removeAll(identitiesInProcess);
        return identities;
    }

    /**
     * Return list of identities which are in user-deletion-process. user-deletion-process means delete-announcement.email send, duration of waiting for response is not
     * expired.
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    @Override
    public List getIdentitiesInDeletionProcess(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select ident from org.olat.data.basesecurity.Identity as ident" + " , org.olat.data.lifecycle.LifeCycleEntry as le"
                + " where ident.key = le.persistentRef " + " and ident.status = " + Identity.STATUS_ACTIV + " and le.persistentTypeName ='"
                + IdentityImpl.class.getSimpleName() + "'" + " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp >= :deleteEmailDate ";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        return dbq.list();
    }

    /**
     * Return list of identities which are ready-to-delete in user-deletion-process. (delete-announcement.email send, duration of waiting for response is expired).
     * 
     * @param deleteEmailDuration
     *            Duration of user-deletion-process in days
     * @return List of Identity objects
     */
    @Override
    public List getIdentitiesReadyToDelete(final int deleteEmailDuration) {
        final Calendar deleteEmailLimit = Calendar.getInstance();
        deleteEmailLimit.add(Calendar.DAY_OF_MONTH, -(deleteEmailDuration - 1));
        log.debug("deleteEmailLimit=" + deleteEmailLimit);
        final String queryStr = "select ident from org.olat.data.basesecurity.Identity as ident" + " , org.olat.data.lifecycle.LifeCycleEntry as le"
                + " where ident.key = le.persistentRef " + " and ident.status = " + Identity.STATUS_ACTIV + " and le.persistentTypeName ='"
                + IdentityImpl.class.getSimpleName() + "'" + " and le.action ='" + SEND_DELETE_EMAIL_ACTION + "' and le.lcTimestamp < :deleteEmailDate ";
        final DBQuery dbq = database.createQuery(queryStr);
        dbq.setDate("deleteEmailDate", deleteEmailLimit.getTime());
        return dbq.list();
    }

    /**
     * Re-activate an identity, lastLogin = now, reset deleteemaildate = null.
     * 
     * @param identity
     */
    @Override
    public void setIdentityAsActiv(final Identity anIdentity) {
        coordinatorManager.getCoordinator().getSyncer()
                .doInSync(OresHelper.createOLATResourceableInstance(anIdentity.getClass(), anIdentity.getKey()), new SyncerExecutor() {
                    @Override
                    public void execute() {
                        // o_clusterOK by:fj : must be fast
                        final Identity identity = (Identity) database.loadObject(anIdentity, true);
                        if (log.isDebugEnabled()) {
                            log.debug("setIdentityAsActiv beginSingleTransaction identity=" + identity);
                        }
                        identity.setLastLogin(new Date());
                        final LifeCycleManager lifeCycleManagerForIdenitiy = LifeCycleManager.createInstanceFor(identity);
                        if (lifeCycleManagerForIdenitiy.lookupLifeCycleEntry(SEND_DELETE_EMAIL_ACTION) != null) {
                            log.info("Audit:User-Deletion: Remove from delete-list identity=" + identity);
                            lifeCycleManagerForIdenitiy.deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("setIdentityAsActiv updateObject identity=" + identity);
                        }
                        database.updateObject(identity);
                        if (log.isDebugEnabled()) {
                            log.debug("setIdentityAsActiv committed identity=" + identity);
                        }
                    }
                });
    }

}
