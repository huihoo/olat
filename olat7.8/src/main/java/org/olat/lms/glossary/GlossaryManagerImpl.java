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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.lms.glossary;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.reference.Reference;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceImpl;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.lms.commons.fileresource.GlossaryResource;
import org.olat.lms.commons.mediaresource.CleanupAfterDeliveryFileMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.config.CourseConfig;
import org.olat.lms.reference.ReferenceService;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Manager to create, delete etc. glossary learning resources. The OLAT glossary functionality is based on the core framework glossary / textmarker functions.
 * <P>
 * <P>
 * Initial Date: 15.01.2009 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
@Service("glossaryManager")
public class GlossaryManagerImpl extends GlossaryManager {

    private static final String EXPORT_FOLDER_NAME = "glossary";
    @Autowired
    private ReferenceService referenceManager;

    /**
     * [used by spring]
     */
    private GlossaryManagerImpl() {
        INSTANCE = this;
    }

    /**
     * Returns the internal glossary folder.
     * 
     * @param res
     * @return
     */
    @Override
    public OlatRootFolderImpl getGlossaryRootFolder(final OLATResourceable res) {
        final OlatRootFolderImpl resRoot = FileResourceManager.getInstance().getFileResourceRootImpl(res);
        if (resRoot == null) {
            return null;
        }
        VFSItem glossaryRoot = resRoot.resolve(INTERNAL_FOLDER_NAME);
        if (glossaryRoot == null) {
            // Glossary has been imported but not yet renamed to the internal folder.
            // This is ugly but no other way to do since the add resource callback
            // somehow does not provide this hook?
            final VFSItem unzipped = resRoot.resolve(FileResourceManager.ZIPDIR);
            if (unzipped == null) {
                // Should not happen, but since we have no unzipped folder we better
                // continue with an empty glossary than crashing
                resRoot.createChildContainer(INTERNAL_FOLDER_NAME);
            } else {
                // We do not use the unzipped folder anymore, this was only for import.
                // We rename it to the internal glossary folder and use it from now on
                unzipped.rename(INTERNAL_FOLDER_NAME);
            }
            glossaryRoot = resRoot.resolve(INTERNAL_FOLDER_NAME);
        }
        return (OlatRootFolderImpl) glossaryRoot;
    }

    /**
     * Exports the glossary resource to the given export directory
     * 
     * @param glossarySoftkey
     * @param exportedDataDir
     * @return
     */
    @Override
    public boolean exportGlossary(final String glossarySoftkey, final File exportedDataDir) {
        final RepositoryEntry re = RepositoryServiceImpl.getInstance().lookupRepositoryEntryBySoftkey(glossarySoftkey, false);
        if (re == null) {
            return false;
        }
        final File fExportBaseDirectory = new File(exportedDataDir, EXPORT_FOLDER_NAME);
        if (!fExportBaseDirectory.mkdir()) {
            return false;
        }
        // export properties
        final RepositoryEntryImportExport reImportExport = new RepositoryEntryImportExport(re, fExportBaseDirectory);
        return reImportExport.exportDoExport();
    }

    /**
     * Export the glossary as a media resource. The resource name is set to the resources display name
     * 
     * @param res
     * @return
     */
    @Override
    public MediaResource getAsMediaResource(final OLATResourceable res) {
        final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, false);
        final String exportFileName = repoEntry.getDisplayname() + ".zip";
        final File fExportZIP = new File(FolderConfig.getCanonicalTmpDir() + "/" + exportFileName);
        final VFSContainer glossaryRoot = getGlossaryRootFolder(res);
        ZipUtil.zip(glossaryRoot.getItems(), new LocalFileImpl(fExportZIP), false);
        return new CleanupAfterDeliveryFileMediaResource(fExportZIP);
    }

    /**
     * Creates a glossary resource and creates the necessary folders on disk. The glossary will be placed in the resources _unizipped dir to make import / export easier
     * 
     * @return
     */
    @Override
    public GlossaryResource createGlossary() {
        final GlossaryResource resource = new GlossaryResource();
        final VFSContainer rootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
        if (rootContainer == null) {
            return null;
        }
        if (rootContainer.createChildContainer(INTERNAL_FOLDER_NAME) == null) {
            return null;
        }
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        final OLATResource ores = rm.createOLATResourceInstance(resource);
        rm.saveOLATResource(ores);
        return resource;
    }

    /**
     * The import export data container used for course import
     * 
     * @param importDataDir
     * @return
     */
    @Override
    public RepositoryEntryImportExport getRepositoryImportExport(final File importDataDir) {
        final File fImportBaseDirectory = new File(importDataDir, EXPORT_FOLDER_NAME);
        return new RepositoryEntryImportExport(fImportBaseDirectory);
    }

    // TODO:RH:gloss change courseconfig, to keep more than 1 single glossary as a list
    /**
     * @param res
     *            glossary to be deleted
     */
    @Override
    public void deleteGlossary(final OLATResourceable res) {
        // first remove all references
        final List repoRefs = referenceManager.getReferencesTo(res);
        for (final Iterator iter = repoRefs.iterator(); iter.hasNext();) {
            final Reference ref = (Reference) iter.next();
            if (ref.getUserdata().equals(GLOSSARY_REPO_REF_IDENTIFYER)) {
                // remove the reference from the course configuration
                // TODO:RH:improvement: this should use a callback method or send a general delete
                // event so that the course can take care of this rather than having it
                // here hardcoded
                final OLATResourceImpl courseResource = ref.getSource();
                // ICourse course = CourseFactory.loadCourse(courseResource);
                final ICourse course = CourseFactory.openCourseEditSession(courseResource.getResourceableId());
                final CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
                cc.setGlossarySoftKey(null);
                CourseFactory.setCourseConfig(course.getResourceableId(), cc);
                CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
                // remove reference from the references table
                referenceManager.delete(ref);
            }
        }
        // now remove the resource itself
        FileResourceManager.getInstance().deleteFileResource(res);
    }

    /**
     * Creates a copy of a glossary
     * 
     * @param res
     * @param ureq
     * @return the copy
     */
    @Override
    public OLATResourceable createCopy(final OLATResourceable res, final Identity identity) {
        final FileResourceManager frm = FileResourceManager.getInstance();
        final OLATResourceable copy = frm.createCopy(res, INTERNAL_FOLDER_NAME);
        return copy;
    }

    @Override
    public String archive(final String archivFilePath, final RepositoryEntry repoEntry) {
        final String exportFileName = "del_glossar_" + repoEntry.getOlatResource().getResourceableId() + ".zip";
        final String fullFilePath = archivFilePath + File.separator + exportFileName;
        final File fExportZIP = new File(fullFilePath);
        final VFSContainer glossaryRoot = getGlossaryRootFolder(repoEntry.getOlatResource());
        ZipUtil.zip(glossaryRoot.getItems(), new LocalFileImpl(fExportZIP), true);
        return fullFilePath;
    }

}
