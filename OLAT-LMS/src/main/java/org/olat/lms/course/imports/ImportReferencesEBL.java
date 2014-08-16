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
import java.security.Principal;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.AddingResourceException;
import org.olat.lms.commons.fileresource.FileResource;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for ImportReferencesEBL
 * 
 * <P>
 * Initial Date: 31.08.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class ImportReferencesEBL {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FileResourceManager fileResourceManager;
    @Autowired
    private OLATResourceManager oLATResourceManager;
    @Autowired
    private BaseSecurityEBL baseSecurityEBL;

    private ImportReferencesEBL() {

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
    // TODO: ORID-1007, Code Duplication (see RepositoryEBL)
    public RepositoryEntry doImport(final RepositoryEntryImportExport importExport, final CourseNode node, final boolean keepSoftkey, final Identity owner) {

        FileResource fileResource = createAndImportFileResource(importExport);
        if (fileResource == null) {
            return null;
        }

        final RepositoryEntry importedRepositoryEntry = createRepositoryEntry(importExport, keepSoftkey, owner, fileResource);
        final SecurityGroup newGroup = baseSecurityEBL.createOwnerGroupWithIdentity(owner);
        importedRepositoryEntry.setOwnerGroup(newGroup);
        repositoryService.saveRepositoryEntry(importedRepositoryEntry);

        if (!keepSoftkey) {
            setReference(importedRepositoryEntry, node);
        }

        return importedRepositoryEntry;
    }

    private FileResource createAndImportFileResource(final RepositoryEntryImportExport importExport) {
        final File fExportedFile = importExport.getExportedFile();
        FileResource fileResource = null;
        try {
            fileResource = fileResourceManager.addFileResource(fExportedFile, fExportedFile.getName());
        } catch (final AddingResourceException e) {
            // e.printStackTrace();
            if (fileResource == null) {
                log.warn("Error adding file resource during repository reference import: " + importExport.getDisplayName());
                return null;
            }
        }
        return fileResource;
    }

    private RepositoryEntry createRepositoryEntry(final RepositoryEntryImportExport importExport, final boolean keepSoftkey, final Principal owner,
            final FileResource fileResource) {
        final RepositoryEntry importedRepositoryEntry = repositoryService.createRepositoryEntryInstance(owner.getName());
        importedRepositoryEntry.setDisplayname(importExport.getDisplayName());
        importedRepositoryEntry.setResourcename(importExport.getResourceName());
        importedRepositoryEntry.setDescription(importExport.getDescription());
        if (keepSoftkey) {
            final String theSoftKey = importExport.getSoftkey();
            if (repositoryService.lookupRepositoryEntryBySoftkey(theSoftKey, false) != null) {
                /*
                 * keepSoftKey == true -> is used for importing in unattended mode. "Importing and keeping the soft key" only works if there is not an already existing
                 * soft key. In the case both if's are taken the respective IMS resource is not imported. It is important to be aware that the resource which triggered
                 * the import process still keeps the soft key reference, and thus points to the already existing resource.
                 */
                return null;
            }
            importedRepositoryEntry.setSoftkey(importExport.getSoftkey());
        }

        // Set the resource on the repository entry.
        final OLATResource ores = oLATResourceManager.findOrPersistResourceable(fileResource);
        importedRepositoryEntry.setOlatResource(ores);
        final RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
        importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));
        return importedRepositoryEntry;
    }

    public void setReference(final RepositoryEntry re, final CourseNode node) {
        node.setRepositoryReference(re);
    }
}
