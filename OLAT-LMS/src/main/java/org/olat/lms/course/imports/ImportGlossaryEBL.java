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
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.glossary.GlossaryManager;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.handlers.RepositoryHandler;
import org.olat.lms.repository.handlers.RepositoryHandlerFactory;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for ImportGlossaryEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class ImportGlossaryEBL {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private OLATResourceManager oLATResourceManager;
    @Autowired
    private ReferenceService referenceService;
    @Autowired
    private BaseSecurityEBL baseSecurityEBL;

    private ImportGlossaryEBL() {

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
    public RepositoryEntry doImport(final RepositoryEntryImportExport importExport, final ICourse course, final boolean keepSoftkey, final Identity owner) {

        final GlossaryResource resource = createAndImportGlossaryResource(importExport);

        final RepositoryEntry importedRepositoryEntry = createRepositoryEntry(importExport, keepSoftkey, owner, resource);
        final SecurityGroup newGroup = baseSecurityEBL.createOwnerGroupWithIdentity(owner);
        importedRepositoryEntry.setOwnerGroup(newGroup);
        repositoryService.saveRepositoryEntry(importedRepositoryEntry);

        if (!keepSoftkey) {
            addReferenceAndUpdateCourseConfig(course, importedRepositoryEntry);
        }

        return importedRepositoryEntry;
    }

    /**
     * Creates the GlossaryResource and unzips the exported file into the glossary container.
     * 
     * @param importExport
     * @return
     */
    private GlossaryResource createAndImportGlossaryResource(final RepositoryEntryImportExport importExport) {
        final GlossaryManager gm = GlossaryManager.getInstance();
        final GlossaryResource resource = gm.createGlossary();
        if (resource == null) {
            log.error("Error adding glossary directry during repository reference import: " + importExport.getDisplayName());
            return null;
        }

        // unzip contents
        final VFSContainer glossaryContainer = gm.getGlossaryRootFolder(resource);
        final File fExportedFile = importExport.getExportedFile();
        if (fExportedFile.exists()) {
            ZipUtil.unzip(new LocalFileImpl(fExportedFile), glossaryContainer);
        } else {
            log.warn("The actual contents of the glossary were not found in the export.");
        }
        return resource;
    }

    private RepositoryEntry createRepositoryEntry(final RepositoryEntryImportExport importExport, final boolean keepSoftkey, final Principal owner,
            final OLATResourceable resourceable) {
        // create repository entry
        final RepositoryEntry importedRepositoryEntry = repositoryService.createRepositoryEntryInstance(owner.getName());
        importedRepositoryEntry.setDisplayname(importExport.getDisplayName());
        importedRepositoryEntry.setResourcename(importExport.getResourceName());
        importedRepositoryEntry.setDescription(importExport.getDescription());
        if (keepSoftkey) {
            importedRepositoryEntry.setSoftkey(importExport.getSoftkey());
        }

        // Set the resource on the repository entry.
        final OLATResource ores = oLATResourceManager.findOrPersistResourceable(resourceable);
        importedRepositoryEntry.setOlatResource(ores);
        final RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
        importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));
        return importedRepositoryEntry;
    }

    private void addReferenceAndUpdateCourseConfig(final ICourse course, final RepositoryEntry importedRepositoryEntry) {
        // set the new glossary reference
        final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
        courseConfig.setGlossarySoftKey(importedRepositoryEntry.getSoftkey());
        referenceService.addReference(course, importedRepositoryEntry.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);
        CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
    }

}
