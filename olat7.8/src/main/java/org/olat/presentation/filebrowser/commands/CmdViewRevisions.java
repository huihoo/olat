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

import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.version.Versionable;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.filebrowser.components.ListRenderer;
import org.olat.presentation.filebrowser.version.RevisionListController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * Open a panel with the list of revisions of the selected file <br>
 * Events:
 * <ul>
 * <li>FOLDERCOMMAND_FINISHED</li>
 * </ul>
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class CmdViewRevisions extends BasicController implements FolderCommand {

    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private RevisionListController revisionListCtr;
    private VelocityContainer mainVC;
    private VFSItem currentItem;

    public CmdViewRevisions(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
        if (revisionListCtr != null) {
            removeAsListenerAndDispose(revisionListCtr);
        }

        String pos = ureq.getParameter(ListRenderer.PARAM_VERID);
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
        }
        if (!(currentItem instanceof Versionable)) {
            status = FolderCommandStatus.STATUS_FAILED;
            getWindowControl().setError(translator.translate("failed"));
            return null;
        }

        setTranslator(translator);
        mainVC = createVelocityContainer("revisions");

        revisionListCtr = new RevisionListController(ureq, wControl, (Versionable) currentItem);
        listenTo(revisionListCtr);
        mainVC.put("revisionList", revisionListCtr.getInitialComponent());

        putInitialPanel(mainVC);
        return this;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public boolean runsModal() {
        return false;
    }

    @Override
    protected void doDispose() {
        // auto-disposed by basic controller
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // nothing to do here
    }

    /**
	 */
    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == revisionListCtr) {
            if (event == Event.DONE_EVENT) {
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            } else if (event == FOLDERCOMMAND_FINISHED) {
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            }
        }
    }
}
