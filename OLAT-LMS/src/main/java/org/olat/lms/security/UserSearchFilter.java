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
package org.olat.lms.security;

import java.util.Date;
import java.util.Map;

import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.PermissionOnResourceable;
import org.olat.data.basesecurity.Roles;

/**
 * Used for EBL.
 * 
 * Initial Date: 17.10.2011 <br>
 * 
 * @author lavinia
 */
public class UserSearchFilter {

    public final String login;
    public final Map<String, String> userproperties;
    public final Roles roles;
    public Roles excludeRoles; // null per default
    public final PermissionOnResourceable[] permissionOnResources;
    public final String[] authProviders;
    public final Date createdAfter;
    public final Date createdBefore;
    public final Date userLoginAfter;
    public final Date userLoginBefore;
    public final Integer status;

    /**
     * @param login
     * @param userproperties
     * @param userPropertiesAsIntersectionSearch
     * @param groups
     * @param permissionOnResources
     * @param authProviders
     * @param createdAfter
     * @param createdBefore
     * @param userLoginAfter
     * @param userLoginBefore
     * @param status
     */
    public UserSearchFilter(String login, Map<String, String> userproperties, Roles roles, PermissionOnResourceable[] permissionOnResources, String[] authProviders,
            Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {

        this.login = login;
        this.userproperties = userproperties;
        this.roles = roles;
        this.permissionOnResources = permissionOnResources;
        this.authProviders = authProviders;
        this.createdAfter = createdAfter;
        this.createdBefore = createdBefore;
        this.userLoginAfter = userLoginAfter;
        this.userLoginBefore = userLoginBefore;
        this.status = status;
    }

    public UserSearchFilter(Builder builder) {

        this.login = null;
        this.userproperties = null;
        this.roles = builder.roles;
        this.excludeRoles = builder.excludeRoles;
        this.permissionOnResources = builder.permissionOnResources;
        this.authProviders = builder.authProviders;
        this.createdAfter = builder.createdAfter;
        this.createdBefore = null;
        this.userLoginAfter = null;
        this.userLoginBefore = null;
        this.status = builder.status;

    }

    public static class Builder {

        private Roles roles;
        private Roles excludeRoles;
        private PermissionOnResourceable[] permissionOnResources;
        private Integer status;
        private Date createdAfter;
        private String[] authProviders;

        public Builder roles(Roles value) {
            roles = value;
            status = Identity.STATUS_VISIBLE_LIMIT;
            return this;
        }

        public Builder excludeRoles(Roles value) {
            excludeRoles = value;
            status = Identity.STATUS_VISIBLE_LIMIT;
            return this;
        }

        public Builder authorPermissions() {
            permissionOnResources = new PermissionOnResourceable[1];
            permissionOnResources[0] = new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
            status = Identity.STATUS_VISIBLE_LIMIT;
            return this;
        }

        public Builder createdAfter(Date value) {
            createdAfter = value;
            status = Identity.STATUS_VISIBLE_LIMIT;
            return this;
        }

        public Builder noAuthentication() {
            final String[] auth = { null };
            authProviders = auth;
            status = Identity.STATUS_VISIBLE_LIMIT;
            return this;
        }

        public Builder loginDenied() {
            status = Identity.STATUS_LOGIN_DENIED;
            return this;
        }

        public Builder deleted() {
            status = Identity.STATUS_DELETED;
            return this;
        }

        public UserSearchFilter build() {
            return new UserSearchFilter(this);
        }

    }

}
