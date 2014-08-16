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
package org.olat.lms.repository;

import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Used in EBL.
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author lavinia
 */
public class RepositoryEntryInputData {
    public Identity identity;
    public String resourceName;
    public String displayName;
    public OLATResourceable resourceable;

    /**
     * @param identity
     * @param resourceName
     * @param displayName
     * @param resourceable
     */
    public RepositoryEntryInputData(Identity identity, String resourceName, String displayName, OLATResourceable resourceable) {

        this.identity = identity;
        this.resourceName = resourceName;
        this.displayName = displayName;
        this.resourceable = resourceable;
    }

    public Identity getIdentity() {
        return identity;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public OLATResourceable getResourceable() {
        return resourceable;
    }
}
