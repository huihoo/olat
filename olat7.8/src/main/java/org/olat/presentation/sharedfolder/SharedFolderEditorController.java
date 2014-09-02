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

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.sharedfolder.SharedFolderManager;
import org.olat.presentation.commons.DisposedRepoEntryRestartController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.repository.EntryChangedEvent;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Aug 29, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class SharedFolderEditorController extends BasicController implements GenericEventListener {

    private final OLATResourceable resourceable;
    private final RepositoryEntry sharedFolderRepoEntry;

    private final Link previewButton;

    private FolderRunController folderRunController;
    private CloseableModalController cmc;
    private SharedFolderDisplayController sfdCtr;

    /**
     * @param res
     * @param ureq
     * @param wControl
     */
    public SharedFolderEditorController(final OLATResourceable res, final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        resourceable = res;
        sharedFolderRepoEntry = CoreSpringFactory.getBean(RepositoryService.class).lookupRepositoryEntry(res, true);

        final VelocityContainer vcEdit = createVelocityContainer("index");
        previewButton = LinkFactory.createButtonSmall("command.preview", vcEdit, this);

        final OlatNamedContainerImpl sharedFolder = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolderRepoEntry);
        folderRunController = new FolderRunController(sharedFolder, true, true, ureq, getWindowControl());
        vcEdit.put("folder", folderRunController.getInitialComponent());

        putInitialPanel(vcEdit);

        // disposed message controller
        final Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
        final Translator trans = PackageUtil.createPackageTranslator(I18nPackage.SHAREDFOLDER_, ureq.getLocale());
        final Controller disposedRestartController = new DisposedRepoEntryRestartController(ureq, wControl, sharedFolderRepoEntry, trans.translate("disposed.title"),
                trans.translate("disposed.message"), trans.translate("disposed.command.restart"), trans.translate("deleted.title"), trans.translate("deleted.text"));
        final Controller layoutController = new LayoutMain3ColsController(ureq, wControl, empty, empty, disposedRestartController.getInitialComponent(),
                "disposed shared folder" + sharedFolderRepoEntry.getResourceableId());
        setDisposedMsgController(layoutController);

        // add as listener to glossary so we are being notified about events:
        // - deletion (OLATResourceableJustBeforeDeletedEvent)
        // - modification (EntryChangedEvent)
        final Identity identity = ureq.getIdentity();
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, res);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == previewButton) {
            final VFSContainer sharedFolderPreview = SharedFolderManager.getInstance().getNamedSharedFolder(sharedFolderRepoEntry);
            sfdCtr = new SharedFolderDisplayController(ureq, getWindowControl(), sharedFolderPreview, sharedFolderRepoEntry.getOlatResource(), true);
            cmc = new CloseableModalController(getWindowControl(), "close", sfdCtr.getInitialComponent());
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

        if (sfdCtr != null) {
            sfdCtr.dispose();
            sfdCtr = null;
        }

        if (cmc != null) {
            cmc.dispose();
            cmc = null;
        }
    }

    @Override
    public void event(Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
            if (ojde.targetEquals(resourceable, true)) {
                dispose();
            }
        } else if (event instanceof EntryChangedEvent) {
            final EntryChangedEvent repoEvent = (EntryChangedEvent) event;
            if (sharedFolderRepoEntry.getKey().equals(repoEvent.getChangedEntryKey()) && repoEvent.getChange() == EntryChangedEvent.MODIFIED) {
                dispose();
            }
        }
    }

    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }
}
