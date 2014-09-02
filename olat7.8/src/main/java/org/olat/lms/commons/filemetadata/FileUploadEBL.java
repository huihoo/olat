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
package org.olat.lms.commons.filemetadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ImageHelper;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalImpl;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFileImpl;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.data.commons.vfs.version.Versions;
import org.olat.data.commons.vfs.version.VersionsManager;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.activitylogging.CoreLoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.presentation.filebrowser.FolderLoggingAction;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * Stateful.
 * <P>
 * Initial Date: 16.09.2011 <br>
 * 
 * @author lavinia
 */
public class FileUploadEBL {

    private final VFSContainer currentContainer;
    private VFSContainer uploadVFSContainer;

    private String uploadRelPath;
    private VFSLeaf newFile;
    private VFSItem existingVFSItem;
    private static Pattern imageExtPattern = Pattern.compile("\\b.(jpg|jpeg|png)\\b");

    /**
	 * 
	 */
    public FileUploadEBL(VFSContainer currentContainer, VFSContainer uploadVFSContainer) {
        this.currentContainer = currentContainer;
        this.uploadVFSContainer = uploadVFSContainer;
    }

    public VFSContainer getCurrentContainer() {
        return currentContainer;
    }

    public VFSContainer getUploadVFSContainer() {
        return uploadVFSContainer;
    }

    private void setUploadVFSContainer(VFSContainer uploadVFSContainer) {
        this.uploadVFSContainer = uploadVFSContainer;
    }

    private String getUploadRelPath() {
        return uploadRelPath;
    }

    private void setUploadRelPath(String uploadRelPath) {
        this.uploadRelPath = uploadRelPath;
    }

    public VFSLeaf getNewFile() {
        return newFile;
    }

    private void setNewFile(VFSLeaf newFile) {
        this.newFile = newFile;
    }

    public VFSItem getExistingVFSItem() {
        return existingVFSItem;
    }

    public void setExistingVFSItem(String fileName) {
        existingVFSItem = getUploadVFSContainer().resolve(fileName);
    }

    public int getNumberOfAllowedVersions() {
        String relPath = null;
        if (getExistingVFSItem() instanceof OlatRootFileImpl) {
            relPath = ((OlatRootFileImpl) getExistingVFSItem()).getRelPath();
        }
        int maxNumOfRevisions = FolderConfig.versionsAllowed(relPath);
        return maxNumOfRevisions;
    }

    public boolean isNewRevisionAllowed(Versions versions, int maxNumOfRevisions) {
        return versions.getRevisions().isEmpty() || maxNumOfRevisions < 0 || maxNumOfRevisions > versions.getRevisions().size();
    }

    public boolean isVFSItemVersionable() {
        return getExistingVFSItem() instanceof Versionable && ((Versionable) getExistingVFSItem()).getVersions().isVersioned();
    }

    /**
     * This is a security requirement: all successful file uploads must be logged.
     * <p>
     * The file upload is logged, after the client decides that the uploaded file is OK (e.g. not too large, file name is not already in use, etc.) and will be kept. <br>
     * <p>
     * Internal helper to add metadata and log.
     * 
     */
    public String finishSuccessfullUploadAndLog(MetaInfoProvider metaInfoProvider, Identity identity) {
        String fileName = (getUploadRelPath() == null ? "" : getUploadRelPath() + "/") + getNewFile().getName();
        VFSItem item = getCurrentContainer().resolve(fileName);
        if (item instanceof OlatRootFileImpl) {
            OlatRootFileImpl relPathItem = (OlatRootFileImpl) item;
            // create meta data
            FileMetadataInfoService metaInfoService = getFileMetadataInfoService();
            MetaInfo meta = metaInfoService.createMetaInfoFor(relPathItem);
            if (metaInfoProvider != null) {
                meta = metaInfoProvider.getMetaInfo(meta);
            }
            meta.setAuthor(identity.getName());
            meta.clearThumbnails();// if overwrite an older file
            meta.write();
        }
        ThreadLocalUserActivityLogger.log(FolderLoggingAction.FILE_UPLOADED, this.getClass(), CoreLoggingResourceable.wrapUploadFile(fileName));
        return fileName;
    }

    /**
     * @return
     */
    private FileMetadataInfoService getFileMetadataInfoService() {
        return CoreSpringFactory.getBean(FileMetadataInfoService.class);
    }

    /**
     * @param destination
     * @param uploadedFile
     * @return
     */
    public boolean copyUploadedFileToDestination(FileElement fileElement, boolean mustBeResized) {
        File uploadedFile = getScaledImageIfIsAnImage(fileElement, mustBeResized);
        String destination = fileElement.getUploadFileName();
        // save file and finish
        setNewFile(getUploadVFSContainer().createChildLeaf(destination));
        InputStream in = null;
        OutputStream out = null;
        boolean success = true;

        try {

            in = new FileInputStream(uploadedFile);
            out = getNewFile().getOutputStream(false);
            FileUtils.bcopy(in, out, "uploadTmpFileToDestFile");
            uploadedFile.delete();

        } catch (IOException e) {
            FileUtils.closeSafely(in);
            FileUtils.closeSafely(out);
            success = false;
        }
        return success;
    }

    public boolean copyUploadedFileToExistingDestination(FileElement fileElement, String renamedFilename, boolean mustBeResized) {
        File uploadedFile = getScaledImageIfIsAnImage(fileElement, mustBeResized);

        setNewFile((VFSLeaf) getUploadVFSContainer().resolve(renamedFilename));
        // Copy content to tmp file

        InputStream in = null;
        BufferedOutputStream out = null;
        boolean success = false;
        try {
            in = new FileInputStream(uploadedFile);
            out = new BufferedOutputStream(getNewFile().getOutputStream(false));
            if (in != null) {
                success = FileUtils.copy(in, out);
            }
            uploadedFile.delete();
        } catch (FileNotFoundException e) {
            success = false;
        } finally {
            FileUtils.closeSafely(in);
            FileUtils.closeSafely(out);
        }
        return success;
    }

    public String renameFileIfAlreadyExists() {
        // file already exists... upload anyway with new filename and
        // in the folder manager status.
        // rename file and ask user what to do
        FileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();
        if (!(getExistingVFSItem() instanceof LocalImpl)) {
            throw new AssertException("Can only LocalImpl VFS items, don't know what to do with file of type::" + getExistingVFSItem().getClass().getCanonicalName());
        }
        File existingFile = ((LocalImpl) getExistingVFSItem()).getBasefile();
        File tmpOrigFilename = new File(existingFile.getAbsolutePath());
        String renamedFilename = fileRenamePolicy.rename(tmpOrigFilename).getName();
        return renamedFilename;
    }

    public boolean isExistingVFSItemLocked(Identity identity, boolean olatAdmin) {
        return getExistingVFSItem() instanceof MetaTagged && FileMetadataInfoHelper.isLocked(getExistingVFSItem(), identity, olatAdmin);
    }

    public void setUploadRelPathAndVFSContainer(String uploadRelPath) {
        setUploadRelPath(uploadRelPath);
        // resolve upload dir from rel upload path
        if (uploadRelPath == null) {
            // reset to current base container
            setUploadVFSContainer(getCurrentContainer());
        } else {
            // try to resolve given rel path from current container
            VFSItem uploadDir = getCurrentContainer().resolve(uploadRelPath);
            if (uploadDir != null) {
                // make sure this is really a container and not a file!
                if (uploadDir instanceof VFSContainer) {
                    setUploadVFSContainer((VFSContainer) uploadDir);
                } else {
                    // fallback to current base
                    setUploadVFSContainer(getCurrentContainer());
                }
            } else {
                // does not yet exist - create subdir
                if (VFSConstants.YES.equals(getCurrentContainer().canWrite())) {
                    setUploadVFSContainer(getCurrentContainer().createChildContainer(uploadRelPath));
                }
            }
        }
    }

    public String getUploadPathWithSpaces(String uploadRelPath) {
        String uploadRelPathString = uploadRelPath == null ? "" : " / " + uploadRelPath;
        String path = "/ " + getCurrentContainer().getName() + uploadRelPathString;
        VFSContainer container = getCurrentContainer().getParentContainer();
        while (container != null) {
            path = "/ " + container.getName() + " " + path;
            container = container.getParentContainer();
        }
        return path;
    }

    public String getUploadPathWithSpaces() {
        String path = "/ " + getUploadVFSContainer().getName();
        VFSContainer container = getUploadVFSContainer().getParentContainer();
        while (container != null) {
            path = "/ " + container.getName() + " " + path;
            container = container.getParentContainer();
        }
        return path;
    }

    public void resetUpload() {
        setNewFile(null);
        setExistingVFSItem(null);

    }

    public void deleteNewFileAndItsVersioning() {
        getNewFile().delete();
        getVersionsManager().delete(getNewFile(), true);// force delete the auto-versioning of this temp. file
    }

    /**
     * @return
     */
    private VersionsManager getVersionsManager() {
        return VersionsManager.getInstance();
    }

    public boolean isMetaInfoLocked() {
        return getExistingVFSItem() instanceof MetaTagged && ((MetaTagged) getExistingVFSItem()).getMetaInfo().isLocked();
    }

    public void replaceExistingWithNewFileAndAddVersion(String comment, Identity identity) {
        // ok, new version of the file
        Versionable existingVersionableItem = (Versionable) getExistingVFSItem();
        boolean ok = existingVersionableItem.getVersions().addVersion(identity, comment, getNewFile().getInputStream());
        if (ok) {
            deleteNewFileAndItsVersioning();
            // what can i do if existingVFSItem is a container
            if (getExistingVFSItem() instanceof VFSLeaf) {
                setNewFile((VFSLeaf) getExistingVFSItem());
            }
        }
    }

    public void overwriteExistingVFSItem() {
        String fileName = getExistingVFSItem().getName();
        getExistingVFSItem().delete();
        getNewFile().rename(fileName);
    }

    public void setMetaInfoUnlockedForExistingVFSItem() {
        if (getExistingVFSItem() instanceof MetaTagged) {
            MetaInfo info = ((MetaTagged) getExistingVFSItem()).getMetaInfo();
            if (info.isLocked()) {
                info.setLocked(false);
                info.write();
            }
        }
    }

    public void clearMetadataInfoLockForExistingVFSItem() {
        MetaInfo info = ((MetaTagged) getExistingVFSItem()).getMetaInfo();
        info.setLocked(false);
        info.setLockedBy(null);
        info.write();
    }

    /**
     * @param item
     * @param identity
     * @param isOlatAdmin
     * @return
     */
    public boolean askForLock(Identity identity, boolean isOlatAdmin) {
        if (getExistingVFSItem() instanceof MetaTagged) {
            MetaInfo info = ((MetaTagged) getExistingVFSItem()).getMetaInfo();
            if (info.isLocked() && !FileMetadataInfoHelper.isLocked(getExistingVFSItem(), identity, isOlatAdmin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param fileName
     * @param uploadedFile
     *            TODO
     * @param mustBeResized
     *            TODO
     * @return
     */
    File getScaledImageIfIsAnImage(FileElement fileElement, boolean mustBeResized) {
        String fileName = fileElement.getUploadFileName();
        File uploadedFile = fileElement.getUploadFile();
        if (mustBeResized && fileName != null && FileUploadEBL.imageExtPattern.matcher(fileName.toLowerCase()).find()) {
            String extension = FileUtils.getFileSuffix(fileName);
            File imageScaled = new File(uploadedFile.getParentFile(), "scaled_" + uploadedFile.getName() + "." + extension);
            if (ImageHelper.scaleImage(uploadedFile, extension, imageScaled, 1280)) {
                // problem happen, special GIF's (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6358674)
                // don't try to scale if not all ok
                uploadedFile = imageScaled;
            }
        }
        return uploadedFile;
    }
}
