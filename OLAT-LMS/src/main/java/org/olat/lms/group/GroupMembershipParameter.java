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
package org.olat.lms.group;

import java.util.Date;

import org.olat.data.group.BusinessGroup;

/**
 * Used for EBL.
 * 
 * Initial Date: 19.10.2011 <br>
 * 
 * @author lavinia
 */
public class GroupMembershipParameter {

    private final String groupType; // translation key
    private final BusinessGroup businessGroup;
    private final String userRoleTranslationKey; // translation key
    private String roleTranslationArgument;

    private final Date joinDate;

    /**
     * @param groupType
     * @param businessGroup
     * @param userRole
     * @param joinDate
     */
    public GroupMembershipParameter(String groupType, BusinessGroup businessGroup, String userRole, Date joinDate) {

        this.groupType = groupType;
        this.businessGroup = businessGroup;
        this.userRoleTranslationKey = userRole;
        this.joinDate = joinDate;
    }

    public String getRoleTranslationArgument() {
        return roleTranslationArgument;
    }

    public void setRoleTranslationArgument(String roleTranslationArgument) {
        this.roleTranslationArgument = roleTranslationArgument;
    }

    public String getGroupType() {
        return groupType;
    }

    public BusinessGroup getBusinessGroup() {
        return businessGroup;
    }

    public String getUserRole() {
        return userRoleTranslationKey;
    }

    public Date getJoinDate() {
        return joinDate;
    }
}
