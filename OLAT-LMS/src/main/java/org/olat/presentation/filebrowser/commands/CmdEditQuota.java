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

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.presentation.admin.quota.QuotaControllerFactory;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

public class CmdEditQuota extends DefaultController implements FolderCommand, ControllerEventListener {

    private final int status = FolderCommandStatus.STATUS_SUCCESS;
    private Controller quotaEditController;
    private VFSSecurityCallback currentSecCallback = null;

    protected CmdEditQuota(final WindowControl wControl) {
        super(wControl);
    }

    @Override
    public Controller execute(final FolderComponent folderComponent, final UserRequest ureq, final WindowControl wControl, final Translator translator) {

        final VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getCurrentContainer());
        if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
            getWindowControl().setWarning(translator.translate("editQuota.nop"));
            return null;
        }

        currentSecCallback = inheritingContainer.getLocalSecurityCallback();
        // cleanup old controller first
        if (quotaEditController != null) {
            quotaEditController.dispose();
        }
        // create a edit controller
        quotaEditController = QuotaControllerFactory.getQuotaEditorInstance(ureq, wControl, currentSecCallback.getQuota().getPath(), true);
        quotaEditController.addControllerListener(this);
        if (quotaEditController != null) {
            setInitialComponent(quotaEditController.getInitialComponent());
            return this;
        } else {
            // do nothing, quota can't be edited
            wControl.setWarning("No quota editor available in briefcase, can't use this function!");
            return null;
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.system.event.control.Event)
     */
    @Override
    public void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == quotaEditController) {
            if (event == Event.CHANGED_EVENT) {
                // update quota
                final Quota newQuota = QuotaManager.getInstance().getCustomQuota(currentSecCallback.getQuota().getPath());
                if (newQuota != null) {
                    currentSecCallback.setQuota(newQuota);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                // do nothing
            }
            fireEvent(ureq, FOLDERCOMMAND_FINISHED);
        }
    }

    @Override
    public void event(final UserRequest ureq, final Component source, final Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doDispose() {
        if (quotaEditController != null) {
            quotaEditController.dispose();
            quotaEditController = null;
        }
    }

    @Override
    public boolean runsModal() {
        return false;
    }

}
