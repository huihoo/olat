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
package org.olat.lms.core.notification.impl;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryStatus;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.image.ImageComponent;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: 11.01.2012 <br>
 * 
 * @author lavinia
 */
public class RepositoryServiceNotificationMock implements RepositoryService {

    @Override
    public RepositoryEntry createRepositoryEntryInstance(String initialAuthor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry createRepositoryEntryInstance(String initialAuthor, String resourceName, String description) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntryStatus createRepositoryEntryStatus(int repositoryEntryStatusCode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveRepositoryEntry(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateRepositoryEntry(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRepositoryEntry(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRepositoryEntryAndBasesecurity(RepositoryEntry entry) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean deleteRepositoryEntryWithAllData(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RepositoryEntry lookupRepositoryEntry(Long key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry lookupRepositoryEntry(OLATResourceable resourceable, boolean strict) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry lookupRepositoryEntryBySoftkey(String softkey, boolean strict) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String lookupDisplayNameByOLATResourceableId(Long resId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAllowedToLaunch(Identity identity, Roles roles, RepositoryEntry re) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void incrementLaunchCounter(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void incrementDownloadCounter(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLastUsageNowFor(RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAccess(RepositoryEntry re, int access) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDescriptionAndName(RepositoryEntry re, String displayName, String description) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProperties(RepositoryEntry re, boolean canCopy, boolean canReference, boolean canLaunch, boolean canDownload) {
        // TODO Auto-generated method stub

    }

    @Override
    public int countByTypeLimitAccess(String restrictedType, int restrictedAccess) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List queryByType(String restrictedType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByTypeLimitAccess(String restrictedType, Roles roles) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByTypeLimitAccess(String restrictedType, UserRequest ureq, String institution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByOwner(Identity identity, String limitType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByOwner(Identity identity, String[] limitTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByInitialAuthor(String initialAuthor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryReferencableResourcesLimitType(Identity identity, Roles roles, List resourceTypes, String displayName, String author, String desc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List queryByOwnerLimitAccess(Identity identity, int limitAccess) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOwnerOfRepositoryEntry(Identity identity, RepositoryEntry entry) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List genericANDQueryWithRolesRestriction(String displayName, String author, String desc, List resourceTypes, Roles roles, String institution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addOwners(Identity ureqIdentity, IdentitiesAddEvent iae, RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeOwners(Identity ureqIdentity, List<Identity> removeIdentities, RepositoryEntry re) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInstitutionalRessourceManagerFor(RepositoryEntry repositoryEntry, Identity identity) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<RepositoryEntry> getLearningResourcesAsStudent(Identity identity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<RepositoryEntry> getLearningResourcesAsTeacher(Identity identity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry updateDisplaynameDescriptionOfRepositoryEntry(RepositoryEntry repositoryEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry updateNewRepositoryEntry(RepositoryEntry repositoryEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean copyImage(RepositoryEntry src, RepositoryEntry target) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ImageComponent getImageComponentForRepositoryEntry(String componentName, RepositoryEntry repositoryEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RepositoryEntry loadRepositoryEntry(RepositoryEntry repositoryEntry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getRepositoryEntryIdFromResourceable(Long resourceableId, String resourceableTypeName) {
        // TODO Auto-generated method stub
        return null;
    }

}
