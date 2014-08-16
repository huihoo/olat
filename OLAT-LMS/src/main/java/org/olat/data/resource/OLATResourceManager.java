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

package org.olat.data.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * A <b>SecurityResourceManager</b> is
 * 
 * @author Andreas Ch. Kapp
 */
@Repository("resourceManager")
public class OLATResourceManager extends BasicManager {

    private static final Logger log = LoggerHelper.getLogger();

    private static OLATResourceManager INSTANCE;
    @Autowired
    private DB db;
    @Autowired
    private CoordinatorManager coordinatorManager;

    /**
     * @return Singleton
     */
    @Deprecated
    public static OLATResourceManager getInstance() {
        return INSTANCE;
    }

    /**
     * [used by spring]
     */
    private OLATResourceManager() {
        INSTANCE = this;
    }

    /**
     * Creates a new OLATResource instance (but does not persist the instance)
     * 
     * @param resource
     * @return OLATResource
     */
    public OLATResource createOLATResourceInstance(final OLATResourceable resource) {
        return new OLATResourceImpl(resource);
    }

    /**
     * Creates a new OLATResource instance (but does not persist the instance)
     * 
     * @param typeName
     * @return OLATResource
     */
    public OLATResource createOLATResourceInstance(final String typeName) {
        final Long id = new Long(CodeHelper.getForeverUniqueID());
        return new OLATResourceImpl(id, typeName);
    }

    /**
     * Creates a new OLATResource instance (but does not persist the instance)
     * 
     * @param aClass
     * @return OLATResource
     */
    public OLATResource createOLATResourceInstance(final Class aClass) {
        final String typeName = OresHelper.calculateTypeName(aClass);
        return createOLATResourceInstance(typeName);
    }

    /**
     * Saves a resource.
     * 
     * @param resource
     * @return True upon success.
     */
    public void saveOLATResource(final OLATResource resource) {
        if (resource.getResourceableTypeName().length() > 50) {
            throw new AssertException("OlatResource: type length may not exceed 50 chars");
        }
        db.saveObject(resource);
    }

    /**
     * Delete an existing resource.
     * 
     * @param resource
     * @return True upon success.
     */
    public void deleteOLATResource(final OLATResource resource) {
        db.deleteObject(resource);
    }

    /**
     * @param resourceable
     * @return true if resourceable was found and deleted, false if it was not found.
     */
    public void deleteOLATResourceable(final OLATResourceable resourceable) {
        final OLATResource ores = findResourceable(resourceable);
        if (ores == null) {
            return;
        }
        deleteOLATResource(ores);
    }

    /**
     * Find the OLATResource for the resourceable. If not found, a new OLATResource is created and returned.
     * 
     * @param resourceable
     * @return an OLATResource representing the resourceable.
     */
    public OLATResource findOrPersistResourceable(final OLATResourceable resourceable) {
        if (resourceable.getResourceableTypeName() == null) {
            throw new AssertException("typename of olatresourceable can not be null");
        }
        // First try to find resourceable without synchronization
        OLATResource ores = findResourceable(resourceable);
        if (ores != null) {
            return ores;
        }
        // Second there exists no resourcable => try to find and create(if no exists) in a synchronized block
        // o_clusterOK by:cg
        ores = coordinatorManager.getCoordinator().getSyncer().doInSync(resourceable, new SyncerCallback<OLATResource>() {
            @Override
            public OLATResource execute() {
                log.debug("start synchronized-block in findOrPersistResourceable");
                OLATResource oresSync = findResourceable(resourceable);
                // if not found, persist it.
                if (oresSync == null) {
                    log.info("Creating olatresource for type: " + resourceable.getResourceableTypeName() + " Ores Id: " + resourceable.getResourceableId());
                    oresSync = createOLATResourceInstance(resourceable);
                    saveOLATResource(oresSync);
                }
                return oresSync;
            }
        });
        return ores;
    }

    /**
     * Find a resourceanle
     * 
     * @param resourceable
     * @return OLATResource object or null if not found.
     */
    public OLATResource findResourceable(final OLATResourceable resourceable) {
        final String type = resourceable.getResourceableTypeName();
        if (type == null) {
            throw new AssertException("typename of olatresourceable must not be null");
        }
        final Long id = resourceable.getResourceableId();

        return doQueryResourceable(id, type);
    }

    /**
     * Find a resourceable
     * 
     * @param resourceableId
     * @return OLATResource object or null if not found.
     */
    public OLATResource findResourceable(final Long resourceableId, final String resourceableTypeName) {
        return doQueryResourceable(resourceableId, resourceableTypeName);
    }

    private OLATResource doQueryResourceable(Long resourceableId, final String type) {
        if (resourceableId == null) {
            resourceableId = OLATResourceImpl.NULLVALUE;
        }

        final String s = new String("from " + OLATResourceImpl.class.getName() + " ori where ori.resName = :resname and ori.resId = :resid");
        DBQuery query = null;
        query = db.createQuery(s);
        query.setString("resname", type);
        query.setLong("resid", resourceableId.longValue());
        query.setCacheable(true);

        final List resources = query.list();
        // if not found, it is an empty list
        if (resources.size() == 0) {
            return null;
        }
        return (OLATResource) resources.get(0);
    }

    /**
     * @param db
     */
    public void setDbInstance(final DB db) {
        this.db = db;
    }

}
