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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 * Events:
 * <ul>
 * <li>FolderEvent</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 */

package org.olat.presentation.filebrowser;

import static java.util.Arrays.asList;

import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileNameValidator;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.data.commons.vfs.version.Versions;
import org.olat.data.filebrowser.FilesInfoMBean;
import org.olat.lms.commons.filemetadata.FileUploadEBL;
import org.olat.presentation.filebrowser.commands.FolderCommandStatus;
import org.olat.presentation.filebrowser.meta.MetaInfoFormController;
import org.olat.presentation.filebrowser.version.RevisionListController;
import org.olat.presentation.filebrowser.version.VersionCommentController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.StaticTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.ButtonClickedEvent;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description</h3>
 * <p>
 * This controller offers a file upload in a dedicated form. It can be configured with an upload limit, a limitation to mime types as allowed upload types and if the path
 * to the target directory should be displayed in the form.
 * <h3>Events fired by this controller</h3>
 * <ul>
 * <li>FolderEvent (whenever something like upload occures)</li>
 * <li>Event.CANCELLED_EVENT</li>
 * <li>Event.FAILED_EVENT</li>
 * <li>Event.DONE_EVENT (fired after the folder upload event)</li>
 * </ul>
 * <p>
 * Initial Date: August 15, 2005
 * 
 * @author Alexander Schneider
 * @author Florian Gnägi
 */
public class FileUploadController extends FormBasicController {

    private FileUploadEBL uploadEBL;
    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private int uploadLimitKB;
    private int remainingQuotKB;
    private Set<String> mimeTypes;
    private FilesInfoMBean fileInfoMBean;

    private RevisionListController revisionListCtr;
    private CloseableModalController revisionListDialogBox, commentVersionDialogBox, unlockDialogBox;
    private VersionCommentController commentVersionCtr;
    private VersionCommentController unlockCtr;
    private DialogBoxController overwriteDialog;
    private DialogBoxController lockedFileDialog;
    //
    // Form elements
    private FileElement fileEl;
    private MultipleSelectionElement resizeEl;
    private StaticTextElement pathEl;
    private boolean showTargetPath = false;

    private boolean fileOverwritten = false;
    private boolean resizeImg;

    // Metadata subform
    private MetaInfoFormController metaDataCtr;
    private boolean showMetadata = false;
    //
    // Cancel button
    private boolean showCancel = true; // default is to show cancel button

    /**
     * @param wControl
     * @param curContainer
     *            Path to the upload directory. Used to check for existing files with same name and for displaying the optional targetPath
     * @param ureq
     * @param upLimitKB
     *            the max upload file size in kBytes (e.g. 10*1024*1024 for 10MB)
     * @param remainingQuotKB
     *            the available space left for file upload kBytes (e.g. 10*1024*1024 for 10MB). Quota.UNLIMITED for no limitation, 0 for no more space left
     * @param mimeTypes
     *            Set of supported mime types (image/*, image/jpg) or NULL if no restriction should be applied.
     * @param showTargetPath
     *            true: show the relative path where the file will be uploaded to; false: show no path
     */
    public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, int upLimitKB, int remainingQuotKB,
            Set<String> mimeTypesRestriction, boolean showTargetPath) {
        this(wControl, curContainer, ureq, upLimitKB, remainingQuotKB, mimeTypesRestriction, showTargetPath, false, true, true);
    }

    public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, int upLimitKB, int remainingQuotKB,
            Set<String> mimeTypesRestriction, boolean showTargetPath, boolean showMetadata, boolean resizeImg, boolean showCancel) {
        super(ureq, wControl, "file_upload");
        setVariables(curContainer, upLimitKB, remainingQuotKB, mimeTypesRestriction, showTargetPath, showMetadata, resizeImg, showCancel);

        initForm(ureq);
    }

    private void setVariables(VFSContainer curContainer, int upLimitKB, int remainingQuotKB, Set<String> mimeTypesRestriction, boolean showTargetPath,
            boolean showMetadata, boolean resizeImg, boolean showCancel) {
        // use base container as upload dir
        this.uploadEBL = new FileUploadEBL(curContainer, curContainer);
        this.fileInfoMBean = (FilesInfoMBean) CoreSpringFactory.getBean("filesInfoMBean");
        this.mimeTypes = mimeTypesRestriction;
        this.showTargetPath = showTargetPath;
        // set remaining quota and max upload size
        this.uploadLimitKB = upLimitKB;
        this.remainingQuotKB = remainingQuotKB;
        this.resizeImg = resizeImg;
        this.showMetadata = showMetadata;
        this.showCancel = showCancel;
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // Trigger fieldset and title
        setFormTitle("ul.header");

        this.flc.contextPut("showMetadata", showMetadata);
        // Add file element
        FormItemContainer fileUpload;
        // the layout of the file upload depends on the metadata. if they're
        // shown, align the file upload element
        if (showMetadata) {
            fileUpload = FormLayoutContainer.createDefaultFormLayout("file_upload", getTranslator());
        } else {
            fileUpload = FormLayoutContainer.createVerticalFormLayout("file_upload", getTranslator());
        }
        formLayout.add(fileUpload);
        flc.contextPut("resizeImg", resizeImg);
        //

        if (resizeImg) {
            FormLayoutContainer resizeCont;
            if (showMetadata) {
                resizeCont = FormLayoutContainer.createDefaultFormLayout("resize_image_wrapper", getTranslator());
            } else {
                resizeCont = FormLayoutContainer.createVerticalFormLayout("resize_image_wrapper", getTranslator());
            }
            formLayout.add(resizeCont);

            String[] keys = new String[] { "resize" };
            String[] values = new String[] { translate("resize_image") };
            resizeEl = uifactory.addCheckboxesHorizontal("resize_image", resizeCont, keys, values, null);
            resizeEl.setLabel(null, null);
            resizeEl.select("resize", true);
        }

        fileEl = uifactory.addFileElement("fileEl", "ul.file", fileUpload);
        setMaxUploadSizeKB(this.uploadLimitKB);
        fileEl.setMandatory(true, "NoFileChoosen");
        if (mimeTypes != null && mimeTypes.size() > 0) {
            fileEl.limitToMimeType(mimeTypes, "WrongMimeType", new String[] { mimeTypes.toString() });
        }

        // Check remaining quota
        if (remainingQuotKB == 0) {
            fileEl.setEnabled(false);
            getWindowControl().setError(translate("QuotaExceeded"));
        }
        //
        // Add path element
        if (showTargetPath) {
            String path = uploadEBL.getUploadPathWithSpaces();
            pathEl = uifactory.addStaticTextElement("ul.target", path, fileUpload);
        }

        if (showMetadata) {
            metaDataCtr = new MetaInfoFormController(ureq, getWindowControl(), mainForm);
            formLayout.add("metadata", metaDataCtr.getFormItem());
            listenTo(metaDataCtr);
        }
        //
        // Add cancel and submit in button group layout
        FormItemContainer buttons;
        if (showMetadata) {
            buttons = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
        } else {
            buttons = FormLayoutContainer.createVerticalFormLayout("buttons", getTranslator());
        }
        formLayout.add(buttons);
        FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        buttons.add(buttonGroupLayout);
        uifactory.addFormSubmitButton("ul.upload", buttonGroupLayout);
        if (showCancel) {
            uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (fileEl.isUploadSuccess()) {
            // check for available space
            if (isUploadedFileTooLarge(fileEl, remainingQuotKB)) {
                fileEl.setErrorKey("QuotaExceeded", null);
                fileEl.getUploadFile().delete();
                return;
            }

            // check if such a filename does already exist
            String fileName = fileEl.getUploadFileName();
            uploadEBL.setExistingVFSItem(fileName);

            Identity identity = ureq.getIdentity();
            boolean mustBeResized = resizeImg && resizeEl.isSelected(0);

            if (uploadEBL.getExistingVFSItem() == null) {
                boolean success = uploadEBL.copyUploadedFileToDestination(fileEl, mustBeResized);

                if (success) {
                    String name = uploadEBL.finishSuccessfullUploadAndLog(metaDataCtr, identity);
                    notifyFileUploadEvent(ureq, name);
                    fileInfoMBean.logUpload(uploadEBL.getNewFile().getSize());
                    fireEvent(ureq, Event.DONE_EVENT);
                } else {
                    showError("failed");
                    status = FolderCommandStatus.STATUS_FAILED;
                    fireEvent(ureq, Event.FAILED_EVENT);
                }
            } else {
                String renamedFilename = uploadEBL.renameFileIfAlreadyExists();
                boolean success = uploadEBL.copyUploadedFileToExistingDestination(fileEl, renamedFilename, mustBeResized);

                if (success) {
                    boolean olatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
                    if (uploadEBL.isExistingVFSItemLocked(identity, olatAdmin)) {
                        // the file is locked and cannot be overwritten
                        removeAsListenerAndDispose(lockedFileDialog);
                        // trusted text, no need to escape
                        lockedFileDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.lockedFile.title"),
                                translate("ul.lockedFile.text", new String[] { uploadEBL.getExistingVFSItem().getName(), renamedFilename }),
                                asList(translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
                        listenTo(lockedFileDialog);

                        lockedFileDialog.activate();
                    } else if (uploadEBL.isVFSItemVersionable()) {
                        Versionable versionable = (Versionable) uploadEBL.getExistingVFSItem();
                        Versions versions = versionable.getVersions();
                        int maxNumOfRevisions = uploadEBL.getNumberOfAllowedVersions();
                        if (maxNumOfRevisions == 0) {
                            // it's possible if someone change the configuration
                            // let calling method decide what to do.
                            removeAsListenerAndDispose(overwriteDialog);
                            overwriteDialog = DialogBoxUIFactory.createGenericDialog(
                                    ureq,
                                    getWindowControl(),
                                    translate("ul.overwrite.threeoptions.title"),
                                    translate("ul.overwrite.threeoptions.text", new String[] { uploadEBL.getExistingVFSItem().getName(), renamedFilename }),
                                    asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
                                            translate("ul.overwrite.threeoptions.cancel")));
                            listenTo(overwriteDialog);

                            overwriteDialog.activate();

                        } else if (uploadEBL.isNewRevisionAllowed(versions, maxNumOfRevisions)) {
                            // let calling method decide what to do.
                            removeAsListenerAndDispose(overwriteDialog);
                            overwriteDialog = DialogBoxUIFactory.createGenericDialog(
                                    ureq,
                                    getWindowControl(),
                                    translate("ul.overwrite.threeoptions.title"),
                                    translate("ul.versionoroverwrite", new String[] { uploadEBL.getExistingVFSItem().getName(), renamedFilename }),
                                    asList(translate("ul.overwrite.threeoptions.newVersion"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
                                            translate("ul.overwrite.threeoptions.cancel")));
                            listenTo(overwriteDialog);

                            overwriteDialog.activate();

                        } else {

                            String title = translate("ul.tooManyRevisions.title",
                                    new String[] { Integer.toString(maxNumOfRevisions), Integer.toString(versions.getRevisions().size()) });
                            String description = translate("ul.tooManyRevisions.description",
                                    new String[] { Integer.toString(maxNumOfRevisions), Integer.toString(versions.getRevisions().size()) });

                            removeAsListenerAndDispose(revisionListCtr);
                            revisionListCtr = new RevisionListController(ureq, getWindowControl(), versionable, title, description);
                            listenTo(revisionListCtr);

                            removeAsListenerAndDispose(revisionListDialogBox);
                            revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent());
                            listenTo(revisionListDialogBox);

                            revisionListDialogBox.activate();
                        }
                    } else {
                        // let calling method decide what to do.
                        // for this, we put a list with "existing name" and "new name"
                        overwriteDialog = DialogBoxUIFactory.createGenericDialog(
                                ureq,
                                getWindowControl(),
                                translate("ul.overwrite.threeoptions.title"),
                                translate("ul.overwrite.threeoptions.text", new String[] { uploadEBL.getExistingVFSItem().getName(), renamedFilename }),
                                asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
                                        translate("ul.overwrite.threeoptions.cancel")));
                        listenTo(overwriteDialog);
                        overwriteDialog.activate();
                    }
                } else {
                    showError("failed");
                    status = FolderCommandStatus.STATUS_FAILED;
                    fireEvent(ureq, Event.FAILED_EVENT);
                }
            }
        } else {
            if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_GENERAL) {
                showError("failed");
            } else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_FILE_EMPTY) {
                showError("failed");
            } else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
                showError("QuotaExceeded");
            }
            status = FolderCommandStatus.STATUS_FAILED;
            fireEvent(ureq, Event.FAILED_EVENT);
        }
    }

    /**
     * @return
     */
    private boolean isUploadedFileTooLarge(FileElement fileEl, int remainingQuotKB) {
        return (remainingQuotKB != -1) && (fileEl.getUploadFile().length() / 1024 > remainingQuotKB);
    }

    private boolean askForLock(UserRequest ureq) {
        Identity identity = ureq.getIdentity();
        boolean olatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();

        return uploadEBL.askForLock(identity, olatAdmin);
    }

    /**
	 */
    @Override
    protected void formCancelled(UserRequest ureq) {
        status = FolderCommandStatus.STATUS_CANCELED;
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == overwriteDialog) {

            if (event instanceof ButtonClickedEvent) {
                ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
                if (buttonClickedEvent.getPosition() == 0) { // ok
                    if (uploadEBL.isVFSItemVersionable()) {
                        int maxNumOfRevisions = uploadEBL.getNumberOfAllowedVersions();
                        if (maxNumOfRevisions == 0) {
                            // someone play with the configuration
                            // Overwrite...
                            uploadEBL.overwriteExistingVFSItem();

                            // ... and notify listeners.
                            finishUpload(ureq);
                        } else {

                            removeAsListenerAndDispose(commentVersionCtr);
                            commentVersionCtr = new VersionCommentController(ureq, getWindowControl(), askForLock(ureq), true);
                            listenTo(commentVersionCtr);

                            removeAsListenerAndDispose(commentVersionDialogBox);
                            commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
                            listenTo(commentVersionDialogBox);

                            commentVersionDialogBox.activate();
                        }
                    } else {
                        // if the file is locked, ask for unlocking it
                        if (uploadEBL.isMetaInfoLocked()) {

                            removeAsListenerAndDispose(unlockCtr);
                            unlockCtr = new VersionCommentController(ureq, getWindowControl(), true, false);
                            listenTo(unlockCtr);

                            removeAsListenerAndDispose(unlockDialogBox);
                            unlockDialogBox = new CloseableModalController(getWindowControl(), translate("ok"), unlockCtr.getInitialComponent());
                            listenTo(unlockDialogBox);

                            unlockDialogBox.activate();

                        } else {
                            uploadEBL.overwriteExistingVFSItem();

                            // ... and notify listeners.
                            finishUpload(ureq);
                        }
                    }
                } else if (buttonClickedEvent.getPosition() == 1) { // not ok
                    // Upload renamed. Since we've already uploaded the file with a changed name, don't do anything much here...
                    this.fileOverwritten = true;

                    // ... and notify listeners.
                    finishUpload(ureq);
                } else if (buttonClickedEvent.getPosition() == 2) { // cancel
                    // Cancel. Remove the new file since it has already been uploaded. Note that we don't have to explicitly close the
                    // dialog box since it closes itself whenever something gets clicked.
                    uploadEBL.deleteNewFileAndItsVersioning();
                } else {
                    throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
                }
            }
        } else if (source == lockedFileDialog) {

            if (event instanceof ButtonClickedEvent) {
                ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
                switch (buttonClickedEvent.getPosition()) {
                case 0: {
                    // upload the file with a new name
                    this.fileOverwritten = true;
                    // ... and notify listeners.
                    finishUpload(ureq);
                    break;
                }
                case 1: {// cancel
                    uploadEBL.deleteNewFileAndItsVersioning();
                    fireEvent(ureq, Event.CANCELLED_EVENT);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
                }
            }
        } else if (source == commentVersionCtr) {
            String comment = commentVersionCtr.getComment();
            boolean keepLocked = commentVersionCtr.keepLocked();
            if (!keepLocked) {
                uploadEBL.setMetaInfoUnlockedForExistingVFSItem();
            }

            commentVersionDialogBox.deactivate();
            if (revisionListDialogBox != null) {
                revisionListDialogBox.deactivate();
            }

            Identity identity = ureq.getIdentity();
            uploadEBL.replaceExistingWithNewFileAndAddVersion(comment, identity);
            finishUpload(ureq);
        } else if (source == unlockCtr) {
            // Overwrite...

            if (!unlockCtr.keepLocked()) {
                uploadEBL.clearMetadataInfoLockForExistingVFSItem();
            }

            unlockDialogBox.deactivate();

            uploadEBL.overwriteExistingVFSItem();

            // ... and notify listeners.
            finishUpload(ureq);

        } else if (source == revisionListCtr) {
            if (FolderCommandStatus.STATUS_CANCELED == revisionListCtr.getStatus()) {

                revisionListDialogBox.deactivate();

                uploadEBL.deleteNewFileAndItsVersioning();
                fireEvent(ureq, Event.CANCELLED_EVENT);
            } else {
                if (uploadEBL.isVFSItemVersionable()) {

                    revisionListDialogBox.deactivate();

                    Versionable versionable = (Versionable) uploadEBL.getExistingVFSItem();
                    Versions versions = versionable.getVersions();
                    int maxNumOfRevisions = FolderConfig.versionsAllowed(null);
                    if (maxNumOfRevisions < 0 || maxNumOfRevisions > versions.getRevisions().size()) {

                        removeAsListenerAndDispose(commentVersionCtr);
                        commentVersionCtr = new VersionCommentController(ureq, getWindowControl(), askForLock(ureq), true);
                        listenTo(commentVersionCtr);

                        removeAsListenerAndDispose(commentVersionDialogBox);
                        commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
                        listenTo(commentVersionDialogBox);

                        commentVersionDialogBox.activate();

                    } else {

                        removeAsListenerAndDispose(revisionListCtr);
                        revisionListCtr = new RevisionListController(ureq, getWindowControl(), versionable);
                        listenTo(revisionListCtr);

                        removeAsListenerAndDispose(revisionListDialogBox);
                        revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent());
                        listenTo(revisionListDialogBox);

                        revisionListDialogBox.activate();
                    }
                }
            }
        }
    }

    private void finishUpload(UserRequest ureq) {
        // in both cases the upload must be finished and notified with a FolderEvent
        String name = uploadEBL.finishSuccessfullUploadAndLog(metaDataCtr, ureq.getIdentity());
        notifyFileUploadEvent(ureq, name);
        fileInfoMBean.logUpload(uploadEBL.getNewFile().getSize());
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
     * @param ureq
     * @param fileName
     */
    private void notifyFileUploadEvent(UserRequest ureq, String fileName) {
        // Notify listeners about upload
        fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, fileName));
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    /**
     * @return The upload status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return true: an existing file has benn overwritten; false: no file with same name existed or new file has been renamed
     */
    public boolean isExistingFileOverwritten() {
        return fileOverwritten;
    }

    /**
     * Set the max upload limit.
     * 
     * @param uploadLimitKB
     */
    public void setMaxUploadSizeKB(int uploadLimitKB) {
        this.uploadLimitKB = uploadLimitKB;
        String supportAddr = WebappHelper.getMailConfig("mailSupport");
        fileEl.setMaxUploadSizeKB(uploadLimitKB, "ULLimitExceeded", new String[] { getFormattedUploadLimit(), supportAddr });
    }

    /**
     * Reset the upload controller
     */
    public void reset() {
        uploadEBL.resetUpload();
        fileEl.reset();
    }

    /**
     * Call this to remove the fieldset and title from the form rendering. This can not be reverted. Default is to show the upload title and fieldset, after calling this
     * function no more title will be shown.
     */
    public void hideTitleAndFieldset() {
        this.setFormTitle(null);
    }

    /**
     * Set the relative path within the rootDir where uploaded files should be put into. If NULL, the root Dir is used
     * 
     * @param uploadRelPath
     */
    public void setUploadRelPath(String uploadRelPath) {
        uploadEBL.setUploadRelPathAndVFSContainer(uploadRelPath);

        // update the destination path in the GUI
        if (showTargetPath) {
            String path = uploadEBL.getUploadPathWithSpaces(uploadRelPath);
            pathEl.setValue(path);
        }
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {

        String fileName = fileEl.getUploadFileName();

        if (fileName == null) {
            fileEl.setErrorKey("NoFileChosen", null);
            return false;
        }

        boolean isFilenameValid = FileNameValidator.validate(fileName);
        if (!isFilenameValid) {
            fileEl.setErrorKey("cfile.name.notvalid", null);
            return false;
        }

        if (isUploadedFileTooLarge(fileEl, remainingQuotKB)) {
            fileEl.clearError();
            String supportAddr = WebappHelper.getMailConfig("mailSupport");
            getWindowControl().setError(translate("ULLimitExceeded", new String[] { getFormattedUploadLimit(), supportAddr }));
            return false;

        }

        return true;
    }

    /**
     * @return
     */
    private String getFormattedUploadLimit() {
        return Formatter.roundToString((uploadLimitKB + 0f) / 1000, 1);
    }

    public String getUploadRelPath() {
        String uploadRelPath = uploadEBL.getUploadPathWithSpaces();
        uploadRelPath = uploadRelPath.replace(" ", "");
        return uploadRelPath;
    }
}
