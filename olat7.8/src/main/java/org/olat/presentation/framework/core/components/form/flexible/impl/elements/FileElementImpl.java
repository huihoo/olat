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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.framework.core.components.form.flexible.impl.elements;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.lms.activitylogging.CoreLoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.validation.ValidationStatus;
import org.olat.lms.commons.validation.ValidationStatusImpl;
import org.olat.presentation.filebrowser.FolderLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormItemImpl;
import org.olat.presentation.framework.core.control.Disposable;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * <h3>Description:</h3>
 * <p>
 * Implementation of the file element. See the interface for more documentation.
 * <p>
 * The class implements the disposable interface to cleanup temporary files on form disposal.
 * <p>
 * Initial Date: 08.12.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class FileElementImpl extends FormItemImpl implements FileElement, Disposable {
    private static final Logger log = LoggerHelper.getLogger();

    protected FileElementComponent component;
    //
    private File initialFile, tempUploadFile;
    private Set<String> mimeTypes;
    private int maxUploadSizeKB = UPLOAD_UNLIMITED;
    private String uploadFilename;
    private String uploadMimeType;
    //
    private boolean checkForMaxFileSize = false;
    private boolean checkForMimeTypes = false;
    // error keys
    private String i18nErrMandatory;
    private String i18nErrMaxSize;
    private String i18nErrMimeType;
    private String[] i18nErrMaxSizeArgs;
    private String[] i18nErrMimeTypeArgs;

    /**
     * Constructor for a file element. Use the limitToMimeType and setter methods to configure the element
     * 
     * @param name
     */
    public FileElementImpl(String name) {
        super(name);
        this.component = new FileElementComponent(this);
    }

    /**
	 */
    @Override
    public void evalFormRequest(UserRequest ureq) {
        Set<String> keys = getRootForm().getRequestMultipartFilesSet();
        if (keys.size() > 0 && keys.contains(component.getFormDispatchId())) {
            // Remove old files first
            if (tempUploadFile != null && tempUploadFile.exists()) {
                tempUploadFile.delete();
            }
            // Move file from a temporary request scope location to a location
            // with a
            // temporary form item scope. The file must be moved later using the
            // moveUploadFileTo() method to the final destination.
            tempUploadFile = new File(WebappHelper.getUserDataRoot() + "/tmp/" + CodeHelper.getGlobalForeverUniqueID());
            File tmpRequestFile = getRootForm().getRequestMultipartFile(component.getFormDispatchId());
            // Move file to internal temp location
            boolean success = tmpRequestFile.renameTo(tempUploadFile);
            if (!success) {
                // try to move file by copying it, command above might fail
                // when source and target are on different volumes
                FileUtils.copyFileToFile(tmpRequestFile, tempUploadFile, true);
            }

            uploadFilename = getRootForm().getRequestMultipartFileName(component.getFormDispatchId());
            uploadMimeType = getRootForm().getRequestMultipartFileMimeType(component.getFormDispatchId());
            if (uploadMimeType == null) {
                // use fallback: mime-type form file name
                uploadMimeType = WebappHelper.getMimeType(uploadFilename);
            }
            // Mark associated component dirty, that it gets rerendered
            component.setDirty(true);
        }
    }

    /**
	 */
    @Override
    protected Component getFormItemComponent() {
        return this.component;
    }

    /**
	 */
    @Override
    public void reset() {
        if (tempUploadFile != null && tempUploadFile.exists()) {
            tempUploadFile.delete();
            tempUploadFile = null;
        }
        uploadFilename = null;
        uploadMimeType = null;
    }

    /**
	 */
    @Override
    protected void rootFormAvailable() {
        //
    }

    /**
	 */
    @Override
    public void setMandatory(boolean mandatory, String i18nErrKey) {
        super.setMandatory(mandatory);
        this.i18nErrMandatory = i18nErrKey;
    }

    /**
	 */
    @Override
    public void validate(List validationResults) {
        int lastFormError = getRootForm().getLastRequestError();
        if (lastFormError == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
            // check if total upload limit is exceeded (e.g. sum of files)
            setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;

            // check for a general error
        } else if (lastFormError == Form.REQUEST_ERROR_GENERAL) {
            setErrorKey("file.element.error.general", null);
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;

            // check if uploaded at all
        } else if (isMandatory()
                && ((initialFile == null && (tempUploadFile == null || !tempUploadFile.exists())) || (initialFile != null && tempUploadFile != null && !tempUploadFile
                        .exists()))) {
            setErrorKey(i18nErrMandatory, null);
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;

            // check for file size of current file
        } else if (checkForMaxFileSize && tempUploadFile != null && tempUploadFile.exists() && tempUploadFile.length() > maxUploadSizeKB * 1024l) {
            setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
            validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
            return;

            // check for mime types
        } else if (checkForMimeTypes && tempUploadFile != null && tempUploadFile.exists()) {
            boolean found = false;
            // Fix problem with upload mimetype: if the mimetype differs from the
            // mimetype the webapp helper generates from the file name the match won't work
            String mimeFromWebappHelper = WebappHelper.getMimeType(uploadFilename);
            if (uploadMimeType != null || mimeFromWebappHelper != null) {
                for (String validType : mimeTypes) {
                    if (validType.equals(uploadMimeType) || validType.equals(mimeFromWebappHelper)) {
                        // exact match: image/jpg
                        found = true;
                        break;
                    } else if (validType.endsWith("/*")) {
                        // wildcard match: image/*
                        if (uploadMimeType != null && uploadMimeType.startsWith(validType.substring(0, validType.length() - 2))) {
                            found = true;
                            break;
                        } else if (mimeFromWebappHelper != null && mimeFromWebappHelper.startsWith(validType.substring(0, validType.length() - 2))) {
                            // fallback to mime type from filename
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                setErrorKey(i18nErrMimeType, i18nErrMimeTypeArgs);
                validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
                return;
            }
        }
        // No error, clear errors from previous attempts
        clearError();
    }

    /**
	 */
    @Override
    public void setInitialFile(File initialFile) {
        this.initialFile = initialFile;
    }

    /**
	 */
    @Override
    public File getInitialFile() {
        return this.initialFile;
    }

    /**
	 */
    @Override
    public void limitToMimeType(Set<String> mimeTypes, String i18nErrKey, String[] i18nArgs) {
        this.mimeTypes = mimeTypes;
        this.checkForMimeTypes = true;
        this.i18nErrMimeType = i18nErrKey;
        this.i18nErrMimeTypeArgs = i18nArgs;
    }

    /**
	 */
    @Override
    public Set<String> getMimeTypeLimitations() {
        if (mimeTypes == null)
            mimeTypes = new HashSet<String>();
        return mimeTypes;
    }

    /**
	 */
    @Override
    public void setMaxUploadSizeKB(int maxUploadSizeKB, String i18nErrKey, String[] i18nArgs) {
        this.maxUploadSizeKB = maxUploadSizeKB;
        this.checkForMaxFileSize = (maxUploadSizeKB == UPLOAD_UNLIMITED ? false : true);
        this.i18nErrMaxSize = i18nErrKey;
        this.i18nErrMaxSizeArgs = i18nArgs;
    }

    /**
	 */
    @Override
    public int getMaxUploadSizeKB() {
        return this.maxUploadSizeKB;
    }

    /**
     * The upload transaction should be already terminated when this is called, but the clients have specific rules <br>
     * to decide if they keep the uploaded file or not, this is why we cannot log the upload here!
     * <p>
     * 
     * The upload should be logged by calling the <code>logUpload()</code>
     * 
     * @Returns true if the upload is successfully.
     */
    @Override
    public boolean isUploadSuccess() {
        if (tempUploadFile != null && tempUploadFile.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public void logUpload() {
        ThreadLocalUserActivityLogger.log(FolderLoggingAction.FILE_UPLOADED, this.getClass(), CoreLoggingResourceable.wrapUploadFile(uploadFilename));
    }

    /**
	 */
    @Override
    public String getUploadFileName() {
        return this.uploadFilename;
    }

    /**
	 */
    @Override
    public String getUploadMimeType() {
        return this.uploadMimeType;
    }

    /**
	 */
    @Override
    public File getUploadFile() {
        return this.tempUploadFile;
    }

    /**
	 */
    @Override
    public InputStream getUploadInputStream() {
        if (this.tempUploadFile == null)
            return null;
        try {
            return new FileInputStream(this.tempUploadFile);
        } catch (FileNotFoundException e) {
            log.error("Could not open stream for file element::" + getName(), e);
        }
        return null;
    }

    /**
	 */
    @Override
    public long getUploadSize() {
        if (tempUploadFile != null && tempUploadFile.exists()) {
            return tempUploadFile.length();
        } else if (initialFile != null && initialFile.exists()) {
            return initialFile.length();
        } else {
            return 0;
        }
    }

    /**
	 */
    @Override
    /* TODO: ORID-1007 'File' this method should be supporting service */
    public File moveUploadFileTo(File destinationDir) {
        if (tempUploadFile != null && tempUploadFile.exists()) {
            destinationDir.mkdirs();
            // Check if such a file does already exist, if yes rename new file
            File existsFile = new File(destinationDir, uploadFilename);
            if (existsFile.exists()) {
                // Use standard rename policy
                FileRenamePolicy frp = new DefaultFileRenamePolicy();
                File tmpF = new File(uploadFilename);
                uploadFilename = frp.rename(tmpF).getName();
            }
            // Move file now
            File targetFile = new File(destinationDir, uploadFilename);
            if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) {
                return targetFile;
            }
        }
        return null;
    }

    /**
	 */
    @Override
    public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
        VFSLeaf targetLeaf = null;
        if (tempUploadFile != null && tempUploadFile.exists()) {
            // Check if such a file does already exist, if yes rename new file
            VFSItem existsChild = destinationContainer.resolve(uploadFilename);
            if (existsChild != null) {
                // Use standard rename policy
                FileRenamePolicy frp = new DefaultFileRenamePolicy();
                File tmpF = new File(uploadFilename);
                uploadFilename = frp.rename(tmpF).getName();
                if (log.isDebugEnabled()) {
                    log.debug("FileElement rename policy::" + tmpF.getName() + " -> " + uploadFilename);
                }
            }
            // Create target leaf file now and delete original temp file
            if (destinationContainer instanceof LocalFolderImpl) {
                // Optimize for local files (don't copy, move instead)
                LocalFolderImpl folderContainer = (LocalFolderImpl) destinationContainer;
                File destinationDir = folderContainer.getBasefile();
                File targetFile = new File(destinationDir, uploadFilename);
                if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) {
                    targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
                    if (targetLeaf == null) {
                        log.error("Error after copying content from temp file, cannot resolve copied file::" + (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
                                + (targetFile == null ? "NULL" : targetFile), null);
                    }
                } else {
                    log.error("Error after copying content from temp file, cannot copy file::" + (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
                            + (targetFile == null ? "NULL" : targetFile), null);
                }
            } else {
                // Copy stream in case the destination is a non-local container
                VFSLeaf leaf = destinationContainer.createChildLeaf(uploadFilename);
                boolean success = false;
                try {
                    success = VFSManager.copyContent(new FileInputStream(tempUploadFile), leaf);
                } catch (FileNotFoundException e) {
                    log.error("Error while copying content from temp file::" + (tempUploadFile == null ? "NULL" : tempUploadFile.getAbsolutePath()), e);
                }
                if (success) {
                    // Delete original temp file after copy to simulate move behavior
                    tempUploadFile.delete();
                    targetLeaf = leaf;
                }
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Error while copying content from temp file, no temp file::" + (tempUploadFile == null ? "NULL" : tempUploadFile.getAbsolutePath()));
        }
        return targetLeaf;

    }

    /**
	 */
    @Override
    public void dispose() {
        if (tempUploadFile != null && tempUploadFile.exists()) {
            tempUploadFile.delete();
        }
    }

}
