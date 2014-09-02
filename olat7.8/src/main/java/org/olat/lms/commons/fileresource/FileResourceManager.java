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

package org.olat.lms.commons.fileresource;

import java.io.File;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.resource.OLATResource;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.commons.mediaresource.DownloadeableMediaResource;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.ims.cp.CPOfflineReadableManager;
import org.olat.lms.portfolio.EPTemplateMapResource;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.media.fileresource.FileDetailsForm;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: Apr 8, 2004
 * 
 * @author Mike Stock
 */
@Component("fileresourceManager")
public class FileResourceManager extends BasicManager {

    public static final String ZIPDIR = "_unzipped_";
    private static FileResourceManager INSTANCE;

    // TestFileResource is under spring control because injected validator (olat & onyx implementation)
    // TODO: Refactor static validate-method; Put all XxxxResource under spring-control (or no of theme)
    @Autowired
    private TestFileResource testFileResourceForValidation;
    // SurveyFileResource is under spring control because injected validator (olat & onyx implementation)
    // TODO: Remove CoreSpringFactory; Refactor static validate-method; Put all XxxxResource under spring-control (or no of theme)
    @Autowired
    private SurveyFileResource surveyFileResourceForValidation;

    /**
     * spring
     */
    private FileResourceManager() {
        INSTANCE = this;
    }

    /**
     * access via spring
     */
    @Deprecated
    public static final FileResourceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Add a file resource if the resource type is already known.
     * 
     * @param fResource
     * @param newName
     * @param knownResource
     *            maybe null, FileResource type will be calculated
     * @return True upon success, false otherwise.
     */
    public FileResource addFileResource(File fResource, final String newName, final FileResource knownResource) throws AddingResourceException {

        // ZIPDIR is a reserved name... check
        if (fResource.getName().equals(ZIPDIR)) {
            throw new AssertException("Trying to add FileResource with reserved name '" + ZIPDIR + "'.");
        }

        FileResource fileResource = new FileResource();
        if (knownResource != null) {
            fileResource = knownResource;
        }

        // move file to its new place
        final File fResourceFileroot = getFileResourceRoot(fileResource);
        if (!FileUtils.copyFileToDir(fResource, fResourceFileroot, "add file resource")) {
            return null;
        }

        if (!fResource.getName().equals(newName)) { // need to rename file to new
            // name
            final File fNewName = new File(fResourceFileroot, newName);
            if (!new File(fResourceFileroot, fResource.getName()).renameTo(fNewName)) {
                FileUtils.deleteDirsAndFiles(fResourceFileroot, true, true);
                return null;
            }
            fResource = fNewName;
        }

        if (knownResource == null) {
            // save resourceableID
            final Long resourceableId = fileResource.getResourceableId();
            // extract type
            try {
                if (DocFileResource.validate(fResource)) {
                    fileResource = new DocFileResource();
                } else if (XlsFileResource.validate(fResource)) {
                    fileResource = new XlsFileResource();
                } else if (PowerpointFileResource.validate(fResource)) {
                    fileResource = new PowerpointFileResource();
                } else if (PdfFileResource.validate(fResource)) {
                    fileResource = new PdfFileResource();
                } else if (ImageFileResource.validate(fResource)) {
                    fileResource = new ImageFileResource();
                } else if (MovieFileResource.validate(fResource)) {
                    fileResource = new MovieFileResource();
                } else if (SoundFileResource.validate(fResource)) {
                    fileResource = new SoundFileResource();
                } else if (AnimationFileResource.validate(fResource)) {
                    fileResource = new AnimationFileResource();
                } else if (SharedFolderFileResource.validate(fResource)) {
                    fileResource = new SharedFolderFileResource();
                } else if (WikiResource.validate(fResource)) {
                    fileResource = new WikiResource();
                } else if (PodcastFileResource.validate(fResource)) {
                    fileResource = new PodcastFileResource(fResourceFileroot, fResource);
                } else if (BlogFileResource.validate(fResource)) {
                    fileResource = new BlogFileResource(fResourceFileroot, fResource);
                } else if (GlossaryResource.validate(fResource)) {
                    fileResource = new GlossaryResource();
                } else if (EPTemplateMapResource.validate(fResource)) {
                    fileResource = new EPTemplateMapResource();
                } else if (fResource.getName().toLowerCase().endsWith(".zip")) {
                    final File fUnzippedDir = unzipFileResource(fileResource);
                    if (fUnzippedDir == null) {
                        // in case of failure we forward the error message
                        throw new AddingResourceException("resource.error.zip");
                    }
                    if (testFileResourceForValidation.validate(fUnzippedDir)) {
                        // TestFileResource is under spring control because injected validator (olat & onyx implementation)
                        // TODO: Remove CoreSpringFactory; Refactor static validate-method; Put all XxxxResource under spring-control (or any)
                        fileResource = CoreSpringFactory.getBean(TestFileResource.class);
                    } else if (WikiResource.validate(fUnzippedDir)) {
                        fileResource = new WikiResource();
                    } else if (PodcastFileResource.validate(fUnzippedDir)) {
                        fileResource = new PodcastFileResource(fResourceFileroot, fUnzippedDir);
                    } else if (BlogFileResource.validate(fUnzippedDir)) {
                        fileResource = new BlogFileResource(fResourceFileroot, fUnzippedDir);
                    } else if (surveyFileResourceForValidation.validate(fUnzippedDir)) {
                        // SurveyFileResource is under spring control because injected validator (olat & onyx implementation)
                        // TODO: Remove CoreSpringFactory; Refactor static validate-method; Put all XxxxResource under spring-control (or any)
                        fileResource = CoreSpringFactory.getBean(SurveyFileResource.class);
                    } else if (ImsCPFileResource.validate(fUnzippedDir)) {
                        fileResource = new ImsCPFileResource();
                    } else if (ScormCPFileResource.validate(fUnzippedDir)) {
                        fileResource = new ScormCPFileResource();
                    } else if (GlossaryResource.validate(fUnzippedDir)) {
                        fileResource = new GlossaryResource();
                    } else if (EPTemplateMapResource.validate(fUnzippedDir)) {
                        fileResource = new EPTemplateMapResource();
                    } else {
                        // just a generic ZIP file... we can delete the temporary unziped
                        // dir...
                        throw new AddingResourceException("doesn't matter what error key is declared");
                    }
                }
            } catch (final AddingResourceException exception) {
                // in case of failure we delete the resource and forward the error
                // message
                deleteFileResource(fileResource);
                throw exception;
            }

            fileResource.overrideResourceableId(resourceableId);
        }

        // add olat resource
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        final OLATResource ores = rm.findOrPersistResourceable(fileResource);

        // make IMS-Content-Packaging offline readable adding a html-frameset
        if (fileResource instanceof ImsCPFileResource) {
            final CPOfflineReadableManager cporm = CPOfflineReadableManager.getInstance();
            cporm.makeCPOfflineReadable(ores, newName);
        }
        return fileResource;
    }

    /**
     * @param fResource
     * @param newName
     * @return Newly created file resource
     */
    public FileResource addFileResource(final File fResource, final String newName) throws AddingResourceException {
        return addFileResource(fResource, newName, null);
    }

    /**
     * @param res
     */
    public void deleteFileResource(final OLATResourceable res) {
        // delete resources
        final File fResourceFileroot = getFileResourceRoot(res);
        FileUtils.deleteDirsAndFiles(fResourceFileroot, true, true);
        // delete resourceable
        final OLATResourceManager rm = OLATResourceManager.getInstance();
        final OLATResource ores = rm.findResourceable(res);
        if (ores != null) {
            rm.deleteOLATResource(ores);
        }
    }

    /**
     * @param res
     * @return Canonical root of file resource
     */
    public File getFileResourceRoot(final OLATResourceable res) {
        return getFileResourceRootImpl(res).getBasefile();
    }

    /**
     * @param res
     * @return olat root folder implementation of file resource
     */
    public OlatRootFolderImpl getFileResourceRootImpl(final OLATResourceable res) {
        return new OlatRootFolderImpl(FolderConfig.getRepositoryHome() + "/" + res.getResourceableId(), null);
    }

    /**
     * @param res
     * @return Get resourceable as file.
     */
    public File getFileResource(final OLATResourceable res) {
        return getFileResource(res, null);
    }

    /**
     * @param res
     * @return Get resourceable as file.
     */
    private File getFileResource(final OLATResourceable res, final String resourceFolderName) {
        final FileResource fr = getAsGenericFileResource(res);
        final File f = getFile(fr, resourceFolderName);
        if (f == null) {
            throw new OLATRuntimeException(FileResourceManager.class, "could not getFileResource for OLATResourceable " + res.getResourceableId() + ":"
                    + res.getResourceableTypeName(), null);
        }
        return f;
    }

    /**
     * Get the specified file or the first zip archive.
     * 
     * @param fr
     * @return The specified file, the first zip archive or null
     */
    private File getFile(final FileResource fr) {
        return getFile(fr, null);
    }

    /**
     * Get the specified file or the first zip archive.
     * 
     * @param fr
     * @param resourceFolderName
     * @return The specified file, the first zip archive or null
     */
    private File getFile(final FileResource fr, final String resourceFolderName) {
        final File fResourceFileroot = getFileResourceRoot(fr);
        if (!fResourceFileroot.exists()) {
            return null;
        }
        final File[] contents = fResourceFileroot.listFiles();
        File firstFile = null;
        for (int i = 0; i < contents.length; i++) {
            final File file = contents[i];
            if (file.getName().equals(ZIPDIR)) {
                continue; // skip ZIPDIR
            }

            if (resourceFolderName != null) {
                // search for specific file name
                if (file.getName().equals(resourceFolderName)) {
                    return file;
                }
            } else if (file.getName().toLowerCase().endsWith(".zip")) {
                // we use first zip file we find
                return file;
            } else if (firstFile == null) {
                // store the first file to be able to return it later. this is needed
                // for wikis.
                firstFile = file;
            }

        }
        // Neither the specified resource nor any zip file could be found. Return
        // the first file that is not ZIPDIR or null.
        return firstFile;
    }

    /**
     * @param res
     * @return File resource as downloadeable media resource.
     */
    public MediaResource getAsDownloadeableMediaResource(final OLATResourceable res) {
        final FileResource fr = getAsGenericFileResource(res);
        final File f = getFile(fr);
        if (f == null) {
            throw new OLATRuntimeException(FileResourceManager.class, "could not get File for OLATResourceable " + res.getResourceableId() + ":"
                    + res.getResourceableTypeName(), null);
        }
        return new DownloadeableMediaResource(f);
    }

    /**
     * @param res
     * @return Directory wherer unzipped files of file resourcea are located.
     */
    public String getUnzippedDirRel(final OLATResourceable res) {
        return res.getResourceableId() + "/" + ZIPDIR;
    }

    /**
     * Unzips a resource and returns the unzipped folder's root.
     * 
     * @param res
     * @return Unzip contents of ZIP file resource.
     */
    public File unzipFileResource(final OLATResourceable res) {
        final File dir = getFileResourceRoot(res);
        if (!dir.exists()) {
            return null;
        }
        File zipTargetDir = new File(dir, ZIPDIR);
        if (!zipTargetDir.exists()) {
            // if not unzipped yet, synchronize all unzipping processes
            // o_clusterOK by:ld
            zipTargetDir = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(res, new SyncerCallback<File>() {
                @Override
                public File execute() {
                    File zipTargetDir = null;
                    // now we are the only one unzipping. We
                    // only need to unzip when the previous
                    // threads that aquired this lock have not unzipped "our" version's
                    // resources yet
                    zipTargetDir = new File(dir, ZIPDIR);
                    if (!zipTargetDir.exists()) { // means I am the first to unzip this
                        // version's resource
                        zipTargetDir.mkdir();
                        final File zipFile = getFileResource(res);
                        if (!ZipUtil.unzip(zipFile, zipTargetDir)) {
                            return null;
                        }
                    }
                    return zipTargetDir;
                }
            });
        }
        return zipTargetDir;
    }

    /**
     * Deletes the contents of the last unzip operation.
     * 
     * @param res
     * @return True upon success.
     */
    public boolean deleteUnzipContent(final OLATResourceable res) {
        final File dir = getFileResourceRoot(res);
        if (!dir.exists()) {
            return false;
        }
        final File zipTargetDir = new File(dir, ZIPDIR);
        return FileUtils.deleteDirsAndFiles(zipTargetDir, true, true);
    }

    /**
     * @param res
     * @return FormBasicController
     */
    public Controller getDetailsForm(final UserRequest ureq, final WindowControl wControl, final OLATResourceable res) {
        return new FileDetailsForm(ureq, wControl, res);
    }

    private FileResource getAsGenericFileResource(final OLATResourceable res) {
        final FileResource fr = new FileResource();
        fr.overrideResourceableId(res.getResourceableId());
        return fr;
    }

    /**
     * Creates a copy of the given resourceable.
     * 
     * @param res
     * @return Copy of the given resource.
     */
    public OLATResourceable createCopy(final OLATResourceable res) {
        return createCopy(res, null);
    }

    /**
     * Creates a copy of the given resourceable.
     * 
     * @param res
     * @return Copy of the given resource.
     */
    public OLATResourceable createCopy(final OLATResourceable res, final String resourceFolderName) {
        final File fResource = getFileResource(res, resourceFolderName);
        if (fResource == null) {
            return null;
        }
        try {
            return addFileResource(fResource, fResource.getName());
        } catch (final AddingResourceException e) {
            throw new OLATRuntimeException(FileResourceManager.class, "Error while trying to copy resource with name: " + fResource.getName(), e);
        }
    }

}
