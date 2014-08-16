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
package org.olat.data.coordinate;

import org.olat.data.coordinate.jms.ClusterEventBus;
import org.olat.system.coordinate.Cacher;
import org.olat.system.coordinate.Coordinator;
import org.olat.system.coordinate.Locker;
import org.olat.system.coordinate.Syncer;
import org.olat.system.coordinate.cache.cluster.ClusterConfig;
import org.olat.system.event.EventBus;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Coordinator implementation for the olat cluster mode
 * <P>
 * Initial Date: 21.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterCoordinator implements Coordinator {
    private Syncer syncer;
    private EventBus eventBus;
    private Locker locker;
    private Cacher cacher;
    private ClusterConfig clusterConfig;

    /**
     * [used by spring]
     */
    private ClusterCoordinator() {
        //
    }

    /**
     * to be used only by the cluster admin controller!
     * 
     * @return
     */
    public ClusterEventBus getClusterEventBus() {
        return (ClusterEventBus) eventBus;
    }

    /**
	 */
    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
	 */
    @Override
    public Syncer getSyncer() {
        return syncer;
    }

    /**
     * do not call normally, reserved for internal calls
     * 
     * @return
     */
    @Override
    public EventBus createSingleUserInstance() {
        // take the normal singlevm event bus, since this is only
        // for within one user-session, which is in one vm
        return (EventBus) CoreSpringFactory.getBean("singleUserEventBus");
    }

    @Override
    public Locker getLocker() {
        return locker;
    }

    /**
     * [used by spring]
     * 
     * @param eventBus
     */
    public void setEventBus(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * [used by spring]
     * 
     * @param locker
     */
    public void setLocker(final Locker locker) {
        this.locker = locker;
    }

    /**
     * [used by spring]
     * 
     * @param syncer
     */
    public void setSyncer(final Syncer syncer) {
        this.syncer = syncer;
    }

    @Override
    public Cacher getCacher() {
        return cacher;
    }

    public void setCacher(final Cacher cacher) {
        this.cacher = cacher;
    }

    @Override
    public Integer getNodeId() {
        return clusterConfig.getNodeId();
    }

    /**
     * [used by spring]
     */
    public void setClusterConfig(final ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }

    /**
	 */
    @Override
    public boolean isClusterMode() {
        return true;
    }

}
