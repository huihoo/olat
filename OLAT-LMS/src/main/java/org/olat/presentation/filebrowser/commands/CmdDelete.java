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

import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.commons.filemetadata.FileMetadataInfoHelper;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.presentation.filebrowser.FileSelection;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

public class CmdDelete extends BasicController implements FolderCommand {

    private static int status = FolderCommandStatus.STATUS_SUCCESS;

    private Translator translator;
    private FolderComponent folderComponent;
    private FileSelection fileSelection;

    private DialogBoxController dialogCtr;
    private DialogBoxController lockedFiledCtr;

    protected CmdDelete(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    @Override
    public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl wContr, Translator trans) {
        this.translator = trans;
        this.folderComponent = fc;
        this.fileSelection = new FileSelection(ureq, fc.getCurrentContainerPath());

        VFSContainer currentContainer = folderComponent.getCurrentContainer();
        List<String> lockedFiles = FileMetadataInfoHelper.hasLockedFiles(currentContainer, fileSelection, ureq);
        if (lockedFiles.isEmpty()) {
            String msg = trans.translate("del.confirm") + "<p>" + fileSelection.renderAsHtml() + "</p>";
            // create dialog controller
            dialogCtr = activateYesNoDialog(ureq, trans.translate("del.header"), msg, dialogCtr);
        } else {
            String msg = FileMetadataInfoHelper.renderLockedMessageAsHtml(trans, currentContainer, lockedFiles);
            List<String> buttonLabels = Collections.singletonList(trans.translate("ok"));
            lockedFiledCtr = activateGenericDialog(ureq, trans.translate("lock.title"), msg, buttonLabels, lockedFiledCtr);
        }
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public FileSelection getFileSelection() {
        return fileSelection;
    }

    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == dialogCtr) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // do delete
                VFSContainer currentContainer = folderComponent.getCurrentContainer();
                List<String> files = fileSelection.getFiles();
                if (files.size() == 0) {
                    // sometimes, browser sends empty form data...
                    getWindowControl().setError(translator.translate("failed"));
                    status = FolderCommandStatus.STATUS_FAILED;
                    fireEvent(ureq, FOLDERCOMMAND_FINISHED);
                }
                for (String file : files) {
                    VFSItem item = currentContainer.resolve(file);
                    if (item != null && (item.canDelete() == VFSConstants.YES)) {
                        if (item instanceof OlatRelPathImpl) {
                            // delete all meta info
                            FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
                            MetaInfo meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) item);
                            if (meta != null)
                                meta.deleteAll();
                        }
                        // delete the item itself
                        item.delete();
                    } else {
                        getWindowControl().setWarning(translator.translate("del.partial"));
                    }
                }

                String confirmationText = fileSelection.renderAsHtml();
                fireEvent(ureq, new FolderEvent(FolderEvent.DELETE_EVENT, confirmationText));
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            } else {
                // abort
                status = FolderCommandStatus.STATUS_CANCELED;
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            }
        }

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // no events to catch
    }

    @Override
    protected void doDispose() {
        // autodisposed by basic controller
    }

    @Override
    public boolean runsModal() {
        // this controller has its own modal dialog box
        return true;
    }

}
