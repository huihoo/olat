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

package org.olat.data.coordinate.singlevm;

import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.AbstractEventBus;
import org.olat.system.event.EventLogger;
import org.olat.system.event.MultiUserEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * implementation of the olat system bus within one vm
 * 
 * @author Felix Jost
 */
public class SingleVMEventBus extends AbstractEventBus {

    @Autowired
    private EventLogger eventLogger;

    /**
     * [spring only]
     */
    private SingleVMEventBus() {
        //
    }

    /**
     * this impl directly fires the event to all listeners.
     */
    @Override
    public void fireEventToListenersOf(final MultiUserEvent event, final OLATResourceable ores) {
        doFire(event, ores, eventLogger);
    }

    /**
     * this imple simply counts the identities on this bus.
     */
    @Override
    public int getListeningIdentityCntFor(final OLATResourceable ores) {
        return getListeningIdentityNamesFor(ores).size();
    }

}
