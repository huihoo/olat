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

/**
 * Used in EBL method.
 * 
 * Initial Date: 05.10.2011 <br>
 * 
 * @author lavinia
 */
public class BusinessGroupTransferObject {

    private String name;
    private String description;
    private Integer maxParticipants;
    private Integer minParticipants;
    private Boolean isWaitingListEnabled;
    private Boolean isCloseRanksEnabled;
    private Date lastUsageDate;

    /**
     * @param name
     * @param description
     * @param maxParticipants
     * @param minParticipants
     * @param hasWaitingList
     * @param isCloseRanksEnabled
     * @param lastUsageDate
     */
    public BusinessGroupTransferObject(String name, String description, Integer maxParticipants, Integer minParticipants, Boolean hasWaitingList,
            Boolean isCloseRanksEnabled, Date lastUsageDate) {
        super();
        this.name = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.minParticipants = minParticipants;
        this.isWaitingListEnabled = hasWaitingList;
        this.isCloseRanksEnabled = isCloseRanksEnabled;
        this.lastUsageDate = lastUsageDate;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getMinParticipants() {
        return minParticipants;
    }

    public Boolean isWaitingListEnabled() {
        return isWaitingListEnabled;
    }

    public Boolean isCloseRanksEnabled() {
        return isCloseRanksEnabled;
    }

    public Date getLastUsageDate() {
        return lastUsageDate;
    }

}
