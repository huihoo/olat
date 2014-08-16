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

package org.olat.presentation.sharedfolder;

import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.event.Event;

/**
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class SharedFolderEditorController extends DefaultController {
    private static final String PACKAGE = PackageUtil.getPackageName(SharedFolderEditorController.class);
    private static final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(PACKAGE);

    private final Translator translator;
    private final VelocityContainer vcEdit;
    private final Link previewButton;

    private final RepositoryEntry re;
    private final OlatNamedContainerImpl sharedFolder;
    private FolderRunController folderRunController;
    private CloseableModalController cmc;
    private Controller controller;
    private SharedFolderDisplayController sfdCtr;

    /**
     * @param res
     * @param ureq
     * @param wControl
     */
    public SharedFolderEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        super(wControl);

        translator = new PackageTranslator(PACKAGE, ureq.getLocale());

        vcEdit = new VelocityContainer("main", VELOCITY_ROOT + "/index.html", translator, this);
        previewButton = LinkFactory.createButtonSmall("command.preview", vcEdit, this);

        re = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(res, true);
        sharedFolder = SharedFolderManager.getInstance().getNamedSharedFolder(re);
        folderRunController = new FolderRunController(sharedFolder, true, true, ureq, getWindowControl());
        vcEdit.put("folder", folderRunController.getInitialComponent());

        setInitialComponent(vcEdit);

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == previewButton) {
            final VFSContainer sharedFolderPreview = SharedFolderManager.getInstance().getNamedSharedFolder(re);
            sfdCtr = new SharedFolderDisplayController(ureq, getWindowControl(), sharedFolderPreview, re, true);
            cmc = new CloseableModalController(getWindowControl(), translator.translate("close"), sfdCtr.getInitialComponent());
            cmc.activate();
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        if (folderRunController != null) {
            folderRunController.dispose();
            folderRunController = null;
        }
        if (controller != null) {
            sfdCtr.dispose();
            sfdCtr = null;
        }
        if (cmc != null) {
            cmc.dispose();
            cmc = null;
        }
    }
}
