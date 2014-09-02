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
 */

package org.olat.presentation.filebrowser.commands;

import java.util.Collections;
import java.util.List;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.commons.filemetadata.FileMetadataInfoHelper;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.filebrowser.components.ListRenderer;
import org.olat.presentation.filebrowser.version.VersionCommentController;
import org.olat.presentation.framework.common.htmleditor.HTMLEditorController;
import org.olat.presentation.framework.common.htmleditor.WysiwygFactory;
import org.olat.presentation.framework.common.linkchooser.CustomLinkTreeModel;
import org.olat.presentation.framework.common.plaintexteditor.PlainTextEditorController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

public class CmdEditContent extends BasicController implements FolderCommand {

    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private VFSItem currentItem;
    private Controller editorc;
    private DialogBoxController lockedFiledCtr;

    private VersionCommentController unlockCtr;
    private CloseableModalController unlockDialogBox;

    protected CmdEditContent(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    /**
     * org.olat.presentation.framework.control.WindowControl, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {

        String pos = ureq.getParameter(ListRenderer.PARAM_CONTENTEDITID);
        if (!StringHelper.containsNonWhitespace(pos)) {
            // somehow parameter did not make it to us
            status = FolderCommandStatus.STATUS_FAILED;
            getWindowControl().setError(translator.translate("failed"));
            return null;
        }

        status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
        if (status == FolderCommandStatus.STATUS_SUCCESS) {
            currentItem = folderComponent.getCurrentContainerChildren().get(Integer.parseInt(pos));
            status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, ureq, currentItem);
        }
        if (status == FolderCommandStatus.STATUS_FAILED) {
            return null;
        } else if (!(currentItem instanceof VFSLeaf)) {
            throw new AssertException("Invalid file: " + folderComponent.getCurrentContainerPath() + "/" + currentItem.getName());
        }

        if (FileMetadataInfoHelper.isLocked(currentItem, ureq.getIdentity(), ureq.getUserSession().getRoles().isOLATAdmin())) {
            List<String> lockedFiles = Collections.singletonList(currentItem.getName());
            String msg = FileMetadataInfoHelper.renderLockedMessageAsHtml(translator, folderComponent.getCurrentContainer(), lockedFiles);
            List<String> buttonLabels = Collections.singletonList(translator.translate("ok"));
            lockedFiledCtr = activateGenericDialog(ureq, translator.translate("lock.title"), msg, buttonLabels, lockedFiledCtr);
            return null;
        }

        // start HTML editor with the folders root folder as base and the file
        // path as a relative path from the root directory. But first check if the
        // root directory is wirtable at all (e.g. not the case in users personal
        // briefcase), and seach for the next higher directory that is writable.
        String relFilePath = "/" + currentItem.getName();
        // add current container path if not at root level
        if (!folderComponent.getCurrentContainerPath().equals("/")) {
            relFilePath = folderComponent.getCurrentContainerPath() + relFilePath;
        }
        VFSContainer writableRootContainer = folderComponent.getRootContainer();
        Object[] result = VFSManager.findWritableRootFolderFor(writableRootContainer, relFilePath);
        if (result != null) {
            writableRootContainer = (VFSContainer) result[0];
            relFilePath = (String) result[1];
        } else {
            // use fallback that always work: current directory and current file
            relFilePath = currentItem.getName();
            writableRootContainer = folderComponent.getCurrentContainer();
        }
        // launch plaintext or html editor depending on file type
        if (relFilePath.endsWith(".html") || relFilePath.endsWith(".htm")) {
            CustomLinkTreeModel customLinkTreeModel = folderComponent.getCustomLinkTreeModel();
            if (customLinkTreeModel != null) {
                editorc = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), writableRootContainer, relFilePath, true, customLinkTreeModel);
                ((HTMLEditorController) editorc).setNewFile(false);
            } else {
                editorc = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), writableRootContainer, relFilePath, true);
                ((HTMLEditorController) editorc).setNewFile(false);
            }
        } else {
            editorc = new PlainTextEditorController(ureq, getWindowControl(), (VFSLeaf) currentItem, "utf-8", true, false, null);
        }
        listenTo(editorc);
        putInitialPanel(editorc.getInitialComponent());
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public String getFileName() {
        return currentItem.getName();
    }

    /**
	 */
    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        // nothing to do here
    }

    /**
	 */
    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == editorc) {
            if (event == Event.DONE_EVENT) {
                if (currentItem instanceof MetaTagged && ((MetaTagged) currentItem).getMetaInfo().isLocked()) {
                    unlockCtr = new VersionCommentController(ureq, getWindowControl(), true, false);
                    listenTo(unlockCtr);
                    unlockDialogBox = new CloseableModalController(getWindowControl(), translate("ok"), unlockCtr.getInitialComponent());
                    unlockDialogBox.activate();
                } else {
                    fireEvent(ureq, FOLDERCOMMAND_FINISHED);
                }
                // cleanup editor
                removeAsListenerAndDispose(editorc);
                editorc = null;
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
                // cleanup editor
                removeAsListenerAndDispose(editorc);
                editorc = null;
            }
        } else if (source == lockedFiledCtr) {
            fireEvent(ureq, FOLDERCOMMAND_FINISHED);
        } else if (source == unlockCtr) {
            if (!unlockCtr.keepLocked()) {
                MetaInfo info = ((MetaTagged) currentItem).getMetaInfo();
                info.setLocked(false);
                info.write();
            }
            cleanUpUnlockDialog();
            fireEvent(ureq, FOLDERCOMMAND_FINISHED);
        }
    }

    private void cleanUpUnlockDialog() {
        if (unlockDialogBox != null) {
            unlockDialogBox.deactivate();
            removeAsListenerAndDispose(unlockCtr);
            unlockDialogBox = null;
            unlockCtr = null;
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // auto dispose by basic controller
    }

    @Override
    public boolean runsModal() {
        return false;
    }

}
