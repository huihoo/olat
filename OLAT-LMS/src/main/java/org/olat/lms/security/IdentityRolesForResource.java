/**
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
package org.olat.lms.security;

import org.olat.data.basesecurity.Roles;

/**
 * Used in EBL.
 * <P>
 * Initial Date: 09.09.2011 <br>
 * 
 * @author lavinia
 */
public final class IdentityRolesForResource extends Roles {

    private boolean isOwner; // owner of a resource

    /**
     * @param isOwner
     * @param isAuthor
     * @param isOlatAdmin
     * @param isGuestOnly
     */
    public IdentityRolesForResource(boolean isOwner, boolean isAuthor, boolean isOlatAdmin, boolean isGuestOnly) {
        super(isOlatAdmin, false, false, isAuthor, isGuestOnly, false, false);
        this.isOwner = isOwner;

    }

    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setAuthor(boolean isAuthor) {
        this.isAuthor = isAuthor;
    }

    public void setOlatAdmin(boolean isOlatAdmin) {
        this.isOLATAdmin = isOlatAdmin;
    }

    public void setGuestOnly(boolean isGuestOnly) {
        this.isGuestOnly = isGuestOnly;
    }

    /**
     * @return
     */
    public boolean isOwnerOrAdmin() {
        return isOwner() || isAuthor();
    }

    /**
     * @return
     */
    public boolean isOwnerOrAuthor() {
        return isOwnerOrAdmin();
    }

}
