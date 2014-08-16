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
package org.olat.lms.course.imports;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.SharedFolderFileResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.reference.ReferenceEnum;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for ImportSharedFolderEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class ImportSharedFolderEBL {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private ReferenceService referenceService;
    @Autowired
    private BaseSecurityEBL baseSecurityEBL;

    private ImportSharedFolderEBL() {

    }

    /**
     * Import a referenced repository entry.
     * 
     * @param importExport
     * @param node
     * @param importMode
     *            Type of import.
     * @param keepSoftkey
     *            If true, no new softkey will be generated.
     * @param owner
     * @return
     */
    // TODO: ORID-1007, Code Duplication (RepositoryEBL, ImportReferencesEBL)
    public RepositoryEntry doImport(final RepositoryEntryImportExport importExport, final ICourse course, final boolean keepSoftkey, final Identity owner) {
        final SharedFolderManager sfm = SharedFolderManager.getInstance();
        final SharedFolderFileResource resource = sfm.createSharedFolder();
        if (resource == null) {
            log.error("Error adding file resource during repository reference import: " + importExport.getDisplayName());
            return null;
        }

        // unzip contents
        final VFSContainer sfContainer = sfm.getSharedFolder(resource);
        final File fExportedFile = importExport.getExportedFile();
        if (fExportedFile.exists()) {
            ZipUtil.unzip(new LocalFileImpl(fExportedFile), sfContainer);
        } else {
            log.warn("The actual contents of the shared folder were not found in the export.");
        }

        // create repository entry
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final RepositoryEntry importedRepositoryEntry = rm.createRepositoryEntryInstance(owner.getName());
        importedRepositoryEntry.setDisplayname(importExport.getDisplayName());
        importedRepositoryEntry.setResourcename(importExport.getResourceName());
        importedRepositoryEntry.setDescription(importExport.getDescription());
        if (keepSoftkey) {
            importedRepositoryEntry.setSoftkey(importExport.getSoftkey());
        }

        // Set the resource on the repository entry.
        final OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(resource);
        importedRepositoryEntry.setOlatResource(ores);
        final RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
        importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));

        final SecurityGroup newGroup = baseSecurityEBL.createOwnerGroupWithIdentity(owner);
        importedRepositoryEntry.setOwnerGroup(newGroup);
        rm.saveRepositoryEntry(importedRepositoryEntry);

        if (!keepSoftkey) {
            // set the new shared folder reference
            final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
            courseConfig.setSharedFolderSoftkey(importedRepositoryEntry.getSoftkey());
            referenceService.updateRefTo(importedRepositoryEntry.getOlatResource(), course, ReferenceEnum.SHARE_FOLDER_REF.getValue());
            CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
        }

        return importedRepositoryEntry;
    }

}
