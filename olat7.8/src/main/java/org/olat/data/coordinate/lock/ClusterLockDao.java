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
package org.olat.data.coordinate.lock;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.jfree.util.Log;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.security.OLATPrincipal;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Provides the database implementation for the Locker (used only in cluster mode)
 * <P>
 * Initial Date: 10.12.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Service
public class ClusterLockDao extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * [spring]
     */
    private ClusterLockDao() {
        //
    }

    LockImpl findLock(final String asset) {
        if (Log.isDebugEnabled()) {
            log.debug("findLock: " + asset + " START");
        }
        final DBQuery q = DBFactory.getInstance().createQuery(
                "select alock from " + LockImpl.class.getName() + " as alock inner join fetch alock.owner where alock.asset = :asset");
        q.setParameter("asset", asset);
        final List res = q.list();
        if (res.size() == 0) {
            log.debug("findLock: null END");
            return null;
        } else {
            if (Log.isDebugEnabled()) {
                log.debug("findLock: " + res.get(0) + " END");
            }
            return (LockImpl) res.get(0);
        }
    }

    LockImpl createLockImpl(final String asset, final OLATPrincipal owner) {
        if (Log.isDebugEnabled()) {
            log.debug("createLockImpl: " + asset + " by " + owner);
        }
        return new LockImpl(asset, (Identity) owner);
    }

    void saveLock(final LockImpl alock) {
        if (Log.isDebugEnabled()) {
            log.debug("saveLock: " + alock + " START");
        }
        DBFactory.getInstance().saveObject(alock);
        if (Log.isDebugEnabled()) {
            log.debug("saveLock: " + alock + " END");
        }
    }

    void deleteLock(final LockImpl li) {
        if (Log.isDebugEnabled()) {
            log.debug("deleteLock: " + li + " START");
        }
        DBFactory.getInstance().deleteObject(li);
        if (Log.isDebugEnabled()) {
            log.debug("deleteLock: " + li + " END");
        }
    }

    @SuppressWarnings("unchecked")
    List<LockImpl> getAllLocks() {
        log.debug("getAllLocks START");
        final DBQuery q = DBFactory.getInstance().createQuery("select alock from " + LockImpl.class.getName() + " as alock inner join fetch alock.owner");
        final List<LockImpl> res = q.list();
        if (Log.isDebugEnabled()) {
            log.debug("getAllLocks END. res.length:" + (res == null ? "null" : res.size()));
        }
        return res;
    }

    /**
     * @param identName
     *            the name of the identity to release all locks for (only the non-persistent locks in cluster mode, -not- the persistent locks!)
     */
    public void releaseAllLocksFor(final String identName) {
        if (Log.isDebugEnabled()) {
            log.debug("releaseAllLocksFor: " + identName + " START");
        }

        final Identity ident = findIdentityByName(identName);

        DBFactory.getInstance().delete("from " + LockImpl.class.getName() + " as alock inner join fetch " + "alock.owner as owner where owner.key = ?", ident.getKey(),
                Hibernate.LONG);
        // cluster:: can we save a query (and is it appropriate considering encapsulation)
        // here by saying: alock.owner as owner where owner.name = ? (using identName parameter)
        if (Log.isDebugEnabled()) {
            log.debug("releaseAllLocksFor: " + identName + " END");
        }
    }

    /**
     * to resolve a cirqular dependency in spring startup (baseSecurity was used in this dao, which need coordinator which was not yet ready...) copied method as private
     * to this dao
     * 
     * @param identityName
     * @return
     */
    private Identity findIdentityByName(final String identityName) {
        final List identities = DBFactory.getInstance().find("select ident from org.olat.data.basesecurity.IdentityImpl as ident where ident.name = ?",
                new Object[] { identityName }, new Type[] { Hibernate.STRING });
        final int size = identities.size();
        if (size == 0) {
            return null;
        }
        if (size != 1) {
            throw new AssertException("non unique name in identites: " + identityName);
        }
        final Identity identity = (Identity) identities.get(0);
        return identity;
    }

}
