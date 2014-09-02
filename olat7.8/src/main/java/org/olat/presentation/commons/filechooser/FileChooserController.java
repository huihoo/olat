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

package org.olat.presentation.commons.filechooser;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.data.filebrowser.FolderModule;
import org.olat.lms.user.PersonalFolderManager;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.framework.common.filechooser.FileChoosenEvent;
import org.olat.presentation.framework.common.filechooser.FileChooserUIFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.CodeHelper;
import org.olat.system.event.Event;

/**
 * Description:
 * <p>
 * This Controller offers first to choose from local folder or to upload files. Then the two subworkflows are triggered.
 * <p>
 * This workflow should not be used in any new workflows, it is very confusing, needs to be reimplemented from sratch with better navigation (e.g. crumb path)
 * <p>
 * 
 * @author Felix Jost
 * @author Florian Gnägi
 * @deprecated
 */
@Deprecated
public class FileChooserController extends BasicController {

    private static final String CMD_SELECT = "select";

    private static final String PARAM_SELECT_RADIO = "sel";

    private static final String PACKAGE = PackageUtil.getPackageName(FileChooserController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private boolean fileFromUpload = false;
    private VFSContainer selectedContainer = null;
    private VFSLeaf selectedFile = null;
    private File uploadedFile = null;
    private VFSLeaf uploadedVFSFile = null;
    private String uploadedFileName;

    private final VelocityContainer main;

    private final int uploadLimitKB;
    private final List<String> folderNames;
    private final List<VFSContainer> containerRefs;
    private VFSItemFilter suffixFilter;
    private org.olat.presentation.framework.common.filechooser.FileChooserController fileChooserCtr;
    private final Boolean showCancelButton;
    private final Link foldersButton;
    private final Link uploadButton;
    private final Link cancelButton;

    private FileUploadController fileUploadCtr;
    private final Panel panel;
    private final File uploadDir;
    private final VFSContainer uploadContainer;

    /**
     * @param ureq
     * @param wControl
     * @param cel
     * @param uploadLimitKB
     */
    public FileChooserController(final UserRequest ureq, final WindowControl wControl, final int uploadLimitKB, final boolean showCancelButton) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(FolderModule.class, ureq.getLocale()));

        this.uploadLimitKB = uploadLimitKB;
        this.showCancelButton = new Boolean(showCancelButton);

        main = createVelocityContainer("index");
        foldersButton = LinkFactory.createButton("folders", main, this);
        uploadButton = LinkFactory.createButton("upload", main, this);
        cancelButton = LinkFactory.createButton("cancel", main, this);
        // tmp upload container
        uploadDir = new File(FolderConfig.getCanonicalTmpDir() + "/" + CodeHelper.getGlobalForeverUniqueID());
        uploadContainer = new LocalFolderImpl(uploadDir);

        folderNames = new ArrayList<String>(3);
        containerRefs = new ArrayList<VFSContainer>(3);
        // folderNames.add("groupFolder1");
        // containerRefs.add(new Path("/groups/gr1"));
        main.contextPut("folderList", folderNames);
        main.contextPut("maxUpload", new Long(uploadLimitKB * 1024));
        main.contextPut("showcancelbutton", this.showCancelButton);
        //
        panel = putInitialPanel(main);
    }

    /**
	 */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    /**
	 * 
	 */
    public void release() {
        if (uploadedFile != null && uploadedFile.exists()) {
            uploadedFile.delete();
        }
        if (uploadDir != null && uploadDir.exists()) {
            FileUtils.deleteDirsAndFiles(uploadDir, true, true);
        }
    }

    /**
     * @param suffixFilter
     */
    public void setSuffixFilter(final VFSItemFilter suffixFilter) {
        this.suffixFilter = suffixFilter;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == main) {
            if (event.getCommand().equals(CMD_SELECT)) {
                if (ureq.getParameter("cancel") != null) {
                    notifyCanceled(ureq);
                    return;
                } else {
                    int selectedFolder;
                    try {
                        selectedFolder = Integer.parseInt(ureq.getParameter(PARAM_SELECT_RADIO));
                    } catch (final NumberFormatException nfe) {
                        getWindowControl().setError(translate("general.error"));
                        return;
                    }
                    selectedContainer = null;
                    if (selectedFolder == 0) { // personal folder
                        selectedContainer = PersonalFolderManager.getInstance().getContainer(ureq.getIdentity());
                    } else { // process other folders
                        selectedContainer = containerRefs.get(selectedFolder - 1);
                    }
                    initializeSelectionTree(ureq, selectedContainer);
                    main.setPage(VELOCITY_ROOT + "/selectfile.html");

                    return;
                }
            }

        } else if (source == foldersButton) {
            main.setPage(VELOCITY_ROOT + "/folders.html");
            main.contextPut("showcancelbutton", this.showCancelButton);

        } else if (source == uploadButton) {
            // Delegate upload process to file upload controller
            removeAsListenerAndDispose(fileUploadCtr);
            fileUploadCtr = new FileUploadController(getWindowControl(), uploadContainer, ureq, uploadLimitKB, Quota.UNLIMITED, null, false);
            listenTo(fileUploadCtr);
            panel.setContent(fileUploadCtr.getInitialComponent());

        } else if (source == cancelButton) {
            notifyCanceled(ureq);
            return;

        }
        main.contextPut("folderList", folderNames);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == fileUploadCtr) {
            // catch upload event
            if (event instanceof FolderEvent && event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
                final FolderEvent folderEvent = (FolderEvent) event;
                // Get file from temp folder location
                uploadedFileName = folderEvent.getFilename();
                final VFSItem file = uploadContainer.resolve(uploadedFileName);
                if (file instanceof VFSLeaf) {
                    // remove old files first from a previous upload
                    if (uploadedFile != null && uploadedFile.exists()) {
                        uploadedFile.delete();
                    }
                    // We knot it is a local file, cast is necessary to get file reference
                    uploadedFile = ((LocalFileImpl) file).getBasefile();
                    uploadedVFSFile = (VFSLeaf) file;
                    fileFromUpload = true;
                    notifyFinished(ureq);
                } else {
                    showError("error.general");
                }
            }
            // in any case move back to main view
            panel.setContent(main);

        } else if (source == fileChooserCtr) {
            if (event instanceof FileChoosenEvent) {
                final String selectedPath = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, selectedContainer, null);
                selectedFile = (VFSLeaf) selectedContainer.resolve(selectedPath);
                notifyFinished(ureq);
                return;
            } else {
                notifyCanceled(ureq);
                return;
            }
        }
    }

    private void notifyFinished(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    private void notifyCanceled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    private void initializeSelectionTree(final UserRequest ureq, final VFSContainer selContainer) {
        removeAsListenerAndDispose(fileChooserCtr);
        fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), selContainer, suffixFilter, true);
        listenTo(fileChooserCtr);
        main.put("selectionTree", fileChooserCtr.getInitialComponent());
    }

    /**
     * @return
     */
    public boolean isFileFromUpload() {
        return fileFromUpload;
    }

    /**
     * @return
     */
    public boolean isFileFromFolder() {
        return !fileFromUpload;
    }

    /**
     * @return
     */
    public VFSLeaf getFileSelection() {
        return selectedFile;
    }

    /**
     * @return
     */
    public File getUploadedFile() {
        return uploadedFile;
    }

    /**
     * @return
     */
    public VFSLeaf getUploadedVFSFile() {
        return uploadedVFSFile;
    }

    /**
     * @return
     */
    public String getUploadedFileName() {
        return uploadedFileName;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        release();
    }

}
