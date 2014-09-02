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

package org.olat.lms.sharedfolder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.connectors.webdav.WebDAVProvider;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider, Gregor Wassmann
 */
public class SharedFolderWebDAVProvider implements WebDAVProvider {

    private static final Logger log = LoggerHelper.getLogger();

    private static List<String> publiclyReadableFolders;
    private static final VFSSecurityCallback readOnlyCallback = new ReadOnlyCallback();

    /**
     * Spring setter.
     * <p>
     * In /olat3/webapp/WEB-INF/olat_extensions.xml the bean 'webdav_sharedfolders' has an optional property called 'publiclyReadableFolders':
     * 
     * <pre>
     * &lt;property name=&quot;publiclyReadableFolders&quot;&gt;
     *   &lt;list&gt;
     *     &lt;value&gt;7045120&lt;/value&gt;
     *     &lt;value&gt;{another repository entry key}&lt;/value&gt;
     *   &lt;/list&gt;
     * &lt;/property&gt;
     * </pre>
     * 
     * It's a list of repositoryEntryKeys belonging to resource folders. These folders will then be displayed (in readonly mode) in WebDAV provided that the repository
     * entry allows access from all users or guests.
     * <p>
     * Alternatively, use '*' as the first value in the list to indicate that all resource folders should be listed in WebDAV.
     * 
     * @param folders
     */
    public void setPubliclyReadableFolders(final List<String> repositoryEntryKeys) {
        publiclyReadableFolders = repositoryEntryKeys;
    }

    /**
	 */
    @Override
    public String getMountPoint() {
        return "sharedfolders";
    }

    /**
	 */
    @Override
    public VFSContainer getContainer(final Identity identity) {
        final MergeSource rootContainer = new MergeSource(null, "root");

        final SharedFolderManager sfm = SharedFolderManager.getInstance();
        final RepositoryService repoManager = RepositoryServiceImpl.getInstance();
        final List<RepositoryEntry> ownerEntries = repoManager.queryByOwner(identity, SharedFolderFileResource.TYPE_NAME);
        for (final RepositoryEntry repoEntry : ownerEntries) {
            rootContainer.addContainer(sfm.getNamedSharedFolder(repoEntry));
        }

        // see /olat3/webapp/WEB-INF/olat_extensions.xml
        if (publiclyReadableFolders != null && publiclyReadableFolders.size() > 0) {
            // Temporarily save added entries. This is needed to make sure not to add
            // an
            // entry twice.
            final List<RepositoryEntry> addedEntries = new ArrayList<RepositoryEntry>(ownerEntries);
            //
            final String firstItem = publiclyReadableFolders.get(0);
            // If the first value in the list is '*', list all resource folders.
            if (firstItem != null && firstItem.equals("*")) {
                // fake role that represents normally logged in user
                final Roles registeredUserRole = new Roles(false, false, false, false, false, false, false);
                final List<RepositoryEntry> allEntries = repoManager.queryByTypeLimitAccess(SharedFolderFileResource.TYPE_NAME, registeredUserRole);
                for (final RepositoryEntry entry : allEntries) {
                    addReadonlyFolder(rootContainer, entry, sfm, addedEntries);
                }
            } else {
                // only list the specified folders
                for (final String folder : publiclyReadableFolders) {
                    try {
                        final Long repoKey = Long.parseLong(folder);
                        final RepositoryEntry entry = repoManager.lookupRepositoryEntry(repoKey);
                        if (entry != null) {
                            if (entry.getAccess() >= RepositoryEntry.ACC_USERS) {
                                // add folder (which is a repo entry) to root container if not
                                // present
                                addReadonlyFolder(rootContainer, entry, sfm, addedEntries);
                            } else {
                                log.warn("Access denied on entry::" + entry.getKey(), null);
                            }
                        } else {
                            log.warn("The repsitoryEntryId::" + folder + " does not exist.", null);
                        }
                    } catch (final NumberFormatException e) {
                        // Invalid id name
                        log.warn("The list item::" + folder + " of publiclyReadableFolders is invalid. Should be repsitoryEntryId or '*'.", e);
                    }
                }
            }
        }
        return rootContainer;
    }

    // If there is a bean property 'publiclyReadableFolders' do the following:

    /**
     * Outsourced helper method for adding an entry to the root container.
     * 
     * @param rootContainer
     * @param sfm
     * @param ownerEntries
     * @param entry
     */
    private void addReadonlyFolder(final MergeSource rootContainer, final RepositoryEntry entry, final SharedFolderManager sfm, final List<RepositoryEntry> addedEntries) {
        //
        if (addedEntries == null || !PersistenceHelper.listContainsObjectByKey(addedEntries, entry)) {
            // add the entry (readonly)
            final VFSContainer folder = sfm.getNamedSharedFolder(entry);
            folder.setLocalSecurityCallback(readOnlyCallback);
            rootContainer.addContainer(folder);
            addedEntries.add(entry);
        }
    }

}
