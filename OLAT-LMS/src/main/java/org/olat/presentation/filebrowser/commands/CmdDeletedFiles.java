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
package org.olat.presentation.filebrowser.commands;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.filebrowser.version.DeletedFileListController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Open a panel with the list of deleted files of the selected container. The panel can delete definitively a deleted and versioned file and restore them.
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class CmdDeletedFiles extends BasicController implements FolderCommand {

    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private DeletedFileListController deletedFileListCtr;
    private VFSContainer currentContainer;

    public CmdDeletedFiles(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
        if (deletedFileListCtr != null) {
            removeAsListenerAndDispose(deletedFileListCtr);
        }

        currentContainer = folderComponent.getCurrentContainer();
        if (!VFSManager.exists(currentContainer)) {
            status = FolderCommandStatus.STATUS_FAILED;
            getWindowControl().setError(translator.translate("FileDoesNotExist"));
            return null;
        }

        deletedFileListCtr = new DeletedFileListController(ureq, wControl, currentContainer);
        listenTo(deletedFileListCtr);
        putInitialPanel(deletedFileListCtr.getInitialComponent());
        return deletedFileListCtr;
    }

    @Override
    protected void doDispose() {
        // auto-disposed by basic controller
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        //
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public boolean runsModal() {
        return false;
    }
}
