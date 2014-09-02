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

import java.io.File;

import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.commons.vfs.version.VersionsManager;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class SharedFolderManager extends BasicManager {

    private static final SharedFolderManager INSTANCE = new SharedFolderManager();
    /**
     * name of the folder on the filesystem, not visible for the user
     */
    private static final String FOLDER_NAME = "_sharedfolder_";

    private SharedFolderManager() {
        // singleton
    }

    @Deprecated
    public static SharedFolderManager getInstance() {
        return INSTANCE;
    }

    public OlatNamedContainerImpl getNamedSharedFolder(final RepositoryEntry re) {
        return new OlatNamedContainerImpl(Formatter.makeStringFilesystemSave(re.getDisplayname()), getSharedFolder(re.getOlatResource()));
    }

    public OlatRootFolderImpl getSharedFolder(final OLATResourceable res) {
        final OlatRootFolderImpl rootFolderImpl = (OlatRootFolderImpl) FileResourceManager.getInstance().getFileResourceRootImpl(res)
                .resolve(SharedFolderManager.FOLDER_NAME);
        if (rootFolderImpl == null) {
            return null;
        }
        rootFolderImpl.setLocalSecurityCallback(new SharedFolderSecurityCallback(rootFolderImpl.getRelPath()));
        return rootFolderImpl;
    }

    public MediaResource getAsMediaResource(final OLATResourceable res) {
        final String exportFileName = res.getResourceableId() + ".zip";
        final File fExportZIP = new File(FolderConfig.getCanonicalTmpDir() + "/" + exportFileName);
        final VFSContainer sharedFolder = SharedFolderManager.getInstance().getSharedFolder(res);

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        ZipUtil.zip(sharedFolder.getItems(), new LocalFileImpl(fExportZIP), false);
        return new CleanupAfterDeliveryFileMediaResource(fExportZIP);
    }

    public boolean exportSharedFolder(final String sharedFolderSoftkey, final File exportedDataDir) {
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(sharedFolderSoftkey, false);
        if (re == null) {
            return false;
        }
        final File fExportBaseDirectory = new File(exportedDataDir, "sharedfolder");
        if (!fExportBaseDirectory.mkdir()) {
            return false;
        }

        // OLAT-5368: do intermediate commit to avoid transaction timeout
        // discussion intermediatecommit vs increased transaction timeout:
        // pro intermediatecommit: not much
        // pro increased transaction timeout: would fix OLAT-5368 but only move the problem
        // @TODO OLAT-2597: real solution is a long-running background-task concept...
        DBFactory.getInstance().intermediateCommit();

        // export properties
        final RepositoryEntryImportExport reImportExport = new RepositoryEntryImportExport(re, fExportBaseDirectory);
        return reImportExport.exportDoExport();
    }

    public RepositoryEntryImportExport getRepositoryImportExport(final File importDataDir) {
        final File fImportBaseDirectory = new File(importDataDir, "sharedfolder");
        return new RepositoryEntryImportExport(fImportBaseDirectory);
    }

    public void deleteSharedFolder(final OLATResourceable res) {
        final VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(res);
        VersionsManager.getInstance().delete(rootContainer, true);
        FileResourceManager.getInstance().deleteFileResource(res);
    }

    public SharedFolderFileResource createSharedFolder() {
        final SharedFolderFileResource resource = new SharedFolderFileResource();
        final VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
        if (rootContainer.createChildContainer(FOLDER_NAME) == null) {
            return null;
        }
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        final OLATResource ores = rm.createOLATResourceInstance(resource);
        rm.saveOLATResource(ores);
        return resource;
    }

    public boolean validate(final File f) {
        final String name = f.getName();
        if (name.equals(FOLDER_NAME) || name.equals(FOLDER_NAME + ".zip")) {
            return true;
        } else {
            return false;
        }
    }

    public String archive(final String archivFilePath, final RepositoryEntry repoEntry) {
        final String exportFileName = "del_sharedfolder_" + repoEntry.getOlatResource().getResourceableId() + ".zip";
        final String fullFilePath = archivFilePath + File.separator + exportFileName;
        final File fExportZIP = new File(fullFilePath);
        final VFSContainer sharedFolder = SharedFolderManager.getInstance().getSharedFolder(repoEntry.getOlatResource());
        ZipUtil.zip(sharedFolder.getItems(), new LocalFileImpl(fExportZIP), true);
        return fullFilePath;
    }

}
