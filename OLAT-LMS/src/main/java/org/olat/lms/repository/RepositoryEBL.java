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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for RepositoryEBL
 * 
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class RepositoryEBL {

    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private BaseSecurityEBL baseSecurityEBL;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private OLATResourceManager oLATResourceManager;

    private RepositoryEBL() {
        // spring
    }

    /**
     * @param data
     *            .identity
     * @param dispName
     * @param resName
     * @return
     */
    public RepositoryEntry createRepositoryEntryWithOresAndOwnerGroup(final RepositoryEntryInputData repositoryEntryInput, final RepositoryHandler repositoryHandler) {
        RepositoryEntry newRepositoryEntry = repositoryService.createRepositoryEntryInstance(repositoryEntryInput.getIdentity().getName());
        newRepositoryEntry.setCanDownload(false);
        newRepositoryEntry.setCanLaunch(repositoryHandler.supportsLaunch(newRepositoryEntry));
        newRepositoryEntry.setDisplayname(repositoryEntryInput.getDisplayName());
        newRepositoryEntry.setResourcename(repositoryEntryInput.getResourceName());
        // Do set access for owner at the end, because unfinished course should be invisible
        // addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
        newRepositoryEntry.setAccess(0);// Access for nobody

        // Set the resource on the repository entry and save the entry.
        OLATResourceable resourceable = repositoryEntryInput.getResourceable();
        final OLATResource ores = oLATResourceManager.findOrPersistResourceable(resourceable);
        newRepositoryEntry.setOlatResource(ores);

        final SecurityGroup newGroup = baseSecurityEBL.createOwnerGroupWithIdentity(repositoryEntryInput.getIdentity());
        newRepositoryEntry.setOwnerGroup(newGroup);

        repositoryService.saveRepositoryEntry(newRepositoryEntry);
        return newRepositoryEntry;
    }

    /**
     * @param srcEntry
     * @param newDispalyname
     * @param resName
     * @param identity
     * @return
     */
    public RepositoryEntry copyRepositoryEntry(final RepositoryEntry srcEntry, final RepositoryEntryInputData repositoryEntryInputData) {
        final RepositoryEntry preparedEntry = repositoryService.createRepositoryEntryInstance(repositoryEntryInputData.getIdentity().getName());
        preparedEntry.setCanDownload(srcEntry.getCanDownload());
        preparedEntry.setCanLaunch(srcEntry.getCanLaunch());
        preparedEntry.setDisplayname(repositoryEntryInputData.getDisplayName());
        preparedEntry.setDescription(srcEntry.getDescription());
        preparedEntry.setResourcename(repositoryEntryInputData.getResourceName());

        if (repositoryEntryInputData.getResourceable() != null) {
            final OLATResource ores = oLATResourceManager.findOrPersistResourceable(repositoryEntryInputData.getResourceable());
            preparedEntry.setOlatResource(ores);
            final SecurityGroup newGroup = baseSecurityEBL.createOwnerGroupWithIdentity(repositoryEntryInputData.getIdentity());
            preparedEntry.setOwnerGroup(newGroup);

            repositoryService.saveRepositoryEntry(preparedEntry);
            /* STATIC_METHOD_REFACTORING */
            // copy image if available
            repositoryService.copyImage(srcEntry, preparedEntry);
        } else {
            return null;
        }
        return preparedEntry;
    }

    /**
	 * 
	 */
    public void deleteRepositoryEntryAndItsOwnerGroupIfCopyWasInterrupted(final RepositoryEntry repositoryEntry) {
        // load newEntry again from DB because it could be changed (Exception object modified)
        if (repositoryEntry != null) {
            final RepositoryEntry reloadedRepositoryEntry = repositoryService.lookupRepositoryEntry(repositoryEntry.getKey());
            if (reloadedRepositoryEntry != null) {
                // it used to be reloaded but should not be necessary since it is a brand new repositoryEntry which is not visible yet to other users
                // final RepositoryEntry reloadedRepositoryEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(repositoryEntry, true);

                final RepositoryHandler repositoryHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(reloadedRepositoryEntry);
                repositoryHandler.cleanupOnDelete(reloadedRepositoryEntry.getOlatResource());
                repositoryService.deleteRepositoryEntry(reloadedRepositoryEntry);

                final SecurityGroup secGroup = reloadedRepositoryEntry.getOwnerGroup();
                baseSecurity.deleteSecurityGroup(secGroup);
            }
        }
    }

    public boolean checkIsRepositoryEntryLaunchable(final Identity identity, final Roles roles, final RepositoryEntry repositoryEntry) {
        final RepositoryHandler type = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repositoryEntry);

        if (repositoryService.isAllowedToLaunch(identity, roles, repositoryEntry) || (type.supportsLaunch(repositoryEntry) && roles.isOLATAdmin())) {
            return true;
        }
        return false;
    }

    public void setStatusClosed(RepositoryEntry repositoryEntry) {
        repositoryEntry = repositoryService.loadRepositoryEntry(repositoryEntry);
        repositoryEntry.setStatusCode(RepositoryEntryStatus.REPOSITORY_STATUS_CLOSED);
        repositoryService.updateRepositoryEntry(repositoryEntry);
    }

    public List<Identity> getOwnersWhenInOwnerGroup(RepositoryEntry repositoryEntry, Identity requestIdentity) {
        final SecurityGroup ownerGroup = repositoryEntry.getOwnerGroup();
        if (baseSecurity.isIdentityInSecurityGroup(requestIdentity, ownerGroup)) {
            return baseSecurity.getIdentitiesOfSecurityGroup(ownerGroup);
        }
        return new ArrayList<Identity>();
    }

    public void addOwnersToRepositoryEntry(List<Identity> owners, List<RepositoryEntry> repoEntries) {
        for (final RepositoryEntry entry : repoEntries) {
            final SecurityGroup secGroup = entry.getOwnerGroup();
            for (final Identity identity : owners) {
                if (!baseSecurity.isIdentityInSecurityGroup(identity, secGroup)) {
                    baseSecurity.addIdentityToSecurityGroup(identity, secGroup);
                }
            }
        }
    }
}
