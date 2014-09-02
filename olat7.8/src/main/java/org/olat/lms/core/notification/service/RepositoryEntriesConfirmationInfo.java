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
package org.olat.lms.core.notification.service;

import java.util.Date;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;

/**
 * Contains the info for sending a confirmation about delete unused repository entries. <br/>
 * 
 * Initial Date: Oct 23, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class RepositoryEntriesConfirmationInfo extends ConfirmationInfo {

    private final List<RepositoryEntry> repositoryEntries;
    private final int numberOfMonths;
    private final int numberOfDays;
    private final REPOSITORY_ENTRIES_CONFIRMATION_TYPE repositoryEntriesConfirmationType;

    public static enum REPOSITORY_ENTRIES_CONFIRMATION_TYPE {
        DELETE_REPOSITORY_ENTRIES;
    }

    public RepositoryEntriesConfirmationInfo(List<RecipientInfo> allRecipientInfos, Identity originatorIdentity, Date dateTime, List<RepositoryEntry> repositoryEntries,
            int numberOfMonths, int numberOfDays, REPOSITORY_ENTRIES_CONFIRMATION_TYPE repositoryEntriesConfirmationType) {
        super(allRecipientInfos, originatorIdentity, null, null, dateTime);
        this.repositoryEntries = repositoryEntries;
        this.numberOfDays = numberOfDays;
        this.numberOfMonths = numberOfMonths;
        this.repositoryEntriesConfirmationType = repositoryEntriesConfirmationType;
    }

    @Override
    public CONFIRMATION_TYPE getType() {
        return ConfirmationInfo.CONFIRMATION_TYPE.REPOSITORY_ENTRIES;
    }

    public List<RepositoryEntry> getRepositoryEntries() {
        return repositoryEntries;
    }

    public int getNumberOfMonths() {
        return numberOfMonths;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public REPOSITORY_ENTRIES_CONFIRMATION_TYPE getRepositoryEntriesConfirmationType() {
        return repositoryEntriesConfirmationType;
    }

}
