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

package org.olat.presentation.collaboration;

import org.apache.log4j.Logger;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<BR>
 * The singleton used for retrieving a collaboration tools suite associated with the supplied OLATResourceable. Supports caching of elements as access to properties can
 * get slow (table can become very large)
 * <P>
 * Initial Date: 2004/10/12 12:45:53
 * 
 * @author Felix Jost
 * @author guido
 */
public class CollaborationToolsFactory {
    private static CollaborationToolsFactory instance;
    CacheWrapper cache;
    private static final Logger log = LoggerHelper.getLogger();

    private final CoordinatorManager coordinatorManager;

    /**
     * [used by spring]
     */
    private CollaborationToolsFactory(final CoordinatorManager coordinatorManager) {
        this.coordinatorManager = coordinatorManager;
        instance = this;
    }

    /**
     * it is a singleton.
     * 
     * @return CollaborationToolsFactory
     */
    public static CollaborationToolsFactory getInstance() {
        return instance;
    }

    /**
     * create a collaborative toolsuite for the specified OLATResourcable
     * 
     * @param ores
     * @return CollaborationTools
     */
    public CollaborationTools getOrCreateCollaborationTools(final OLATResourceable ores) {
        if (ores == null) {
            throw new AssertException("Null is not allowed here, you have to provide an existing ores here!");
        }
        final String cacheKey = Long.valueOf(ores.getResourceableId()).toString();
        // sync operation cluster wide
        return coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<CollaborationTools>() {

            @Override
            public CollaborationTools execute() {
                if (cache == null) {
                    cache = coordinatorManager.getCoordinator().getCacher().getOrCreateCache(CollaborationToolsFactory.class, "tools");
                }
                final CollaborationTools collabTools = (CollaborationTools) cache.get(cacheKey);
                if (collabTools != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("loading collabTool from cache. Ores: " + ores.getResourceableId());
                    }

                    if (collabTools.isDirty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("CollabTools were in cache but dirty. Creating new ones. Ores: " + ores.getResourceableId());
                        }
                        final CollaborationTools tools = new CollaborationTools(ores);
                        // update forces clusterwide invalidation of this object
                        cache.update(cacheKey, tools);
                        return tools;
                    }

                    return collabTools;

                }
                if (log.isDebugEnabled()) {
                    log.debug("collabTool not in cache. Creating new ones. Ores: " + ores.getResourceableId());
                }
                final CollaborationTools tools = new CollaborationTools(ores);
                cache.put(cacheKey, tools);
                return tools;
            }
        });
    }

    /**
     * if you are sure that the cache is populated with the latest version of the collabtools you can use this method to avoid nested do in sync on the cluster
     * 
     * @param ores
     * @return
     */
    public CollaborationTools getCollaborationToolsIfExists(final OLATResourceable ores) {
        final String cacheKey = Long.valueOf(ores.getResourceableId()).toString();
        return (CollaborationTools) cache.get(cacheKey);
    }

}
