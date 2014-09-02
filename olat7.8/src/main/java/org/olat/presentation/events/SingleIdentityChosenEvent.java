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

package org.olat.presentation.events;

import org.olat.data.basesecurity.Identity;
import org.olat.system.event.Event;

/**
 * Initial Date: Feb 19, 2004
 * 
 * @author jeger Comment: The SingleIdentityChosenEvent has an additional field that tells which identity has been found
 */
public class SingleIdentityChosenEvent extends Event {

    private final Identity identity;

    /**
     * Event of type 'SingleIdentityChosenEvent' with extra parameter, the identity itself
     * 
     * @param identity
     *            Must not be NULL
     */
    public SingleIdentityChosenEvent(final Identity identity) {
        super("IdentityFound");
        this.identity = identity;
    }

    /**
     * @return Returns the identity.
     */
    public Identity getChosenIdentity() {
        return identity;
    }

}
