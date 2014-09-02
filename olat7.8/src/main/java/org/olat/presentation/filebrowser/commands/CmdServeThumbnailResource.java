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

import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;

public class CmdServeThumbnailResource implements FolderCommand {

    private int status = FolderCommandStatus.STATUS_SUCCESS;

    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
        VFSSecurityCallback inheritedSecCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
        if (inheritedSecCallback != null && !inheritedSecCallback.canRead())
            throw new RuntimeException("Illegal read attempt: " + folderComponent.getCurrentContainerPath());

        // extract file
        String path = ureq.getModuleURI();
        MediaResource mr = null;
        VFSLeaf vfsfile = (VFSLeaf) folderComponent.getRootContainer().resolve(path);
        if (vfsfile instanceof MetaTagged) {
            MetaInfo info = ((MetaTagged) vfsfile).getMetaInfo();
            if (info != null) {
                VFSLeaf thumbnail = info.getThumbnail(200, 200);
                if (thumbnail != null) {
                    mr = new VFSMediaResource(thumbnail);
                }
            }
        }
        if (mr == null) {
            mr = new NotFoundMediaResource(path);
        }

        ureq.getDispatchResult().setResultingMediaResource(mr);
        return null;
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
