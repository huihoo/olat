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

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.event.Event;

/**
 * Initial Date: Nov 23 2006
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com Comment: The MultiIdentityChosenEvent is fired when multiple identites are selected for whatever purpose.
 *         it contains a List of identites that can be recalled
 */
public class MultiIdentityChosenEvent extends Event {

    private final List<Identity> identities;

    /**
     * @param identities
     *            the List of choosen identities
     */
    public MultiIdentityChosenEvent(final List<Identity> identities) {
        super("MultiIdentitiesFound");
        this.identities = identities;
    }

    /**
     * Can be used from classes which extends the MultiIdentityChosenEvent class.
     * 
     * @param identities
     *            The List of choosen identities
     * @param command
     *            Command name from super class.
     */
    protected MultiIdentityChosenEvent(final List<Identity> identities, final String command) {
        super(command);
        this.identities = identities;
    }

    /**
     * @return Returns the list of choosen identities.
     */
    public List<Identity> getChosenIdentities() {
        return identities;
    }

}
