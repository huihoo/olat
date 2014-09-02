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
package org.olat.presentation.portfolio.artefacts.run.details;

import javax.servlet.http.HttpServletRequest;

import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.portfolio.artefact.AbstractArtefact;
import org.olat.data.portfolio.artefact.FileArtefact;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.download.DownloadComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.presentation.framework.dispatcher.mapper.MapperRegistry;
import org.olat.presentation.portfolio.artefacts.collect.EPCreateFileArtefactStepForm00;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * show specific infos for FileArtefact allow to delete / upload a file
 * <P>
 * Initial Date: 08.10.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class FileArtefactDetailsController extends BasicController {

    private VelocityContainer vC;
    private final boolean readOnlyMode;
    MediaResource mr;
    private Link delLink;
    private DialogBoxController delDialog;
    private final FileArtefact fArtefact;
    private Controller fileUploadCtrl;
    private final EPFrontendManager ePFMgr;
    private Link uploadLink;
    private CloseableCalloutWindowController calloutCtrl;
    private final Panel viewPanel;

    public FileArtefactDetailsController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact, final boolean readOnlyMode) {
        super(ureq, wControl);
        this.readOnlyMode = readOnlyMode;
        fArtefact = (FileArtefact) artefact;
        ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

        viewPanel = new Panel("empty");
        initViewDependingOnFileExistance(ureq);

        putInitialPanel(viewPanel);
    }

    private void initViewDependingOnFileExistance(final UserRequest ureq) {
        final VFSItem file = ePFMgr.getArtefactContainer(fArtefact).resolve(fArtefact.getFilename());
        if (file != null && file instanceof VFSLeaf) {
            initFileView(file, ureq);
        } else if (!readOnlyMode) {
            initUploadView(ureq);
        }
    }

    private void initFileView(final VFSItem file, final UserRequest ureq) {
        vC = createVelocityContainer("fileDetails");
        FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
        final MetaInfo meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) file);
        vC.contextPut("meta", meta);
        final DownloadComponent downlC = new DownloadComponent("download", (VFSLeaf) file);
        vC.put("download", downlC);
        vC.contextPut("filename", fArtefact.getFilename());
        // show a preview thumbnail if possible
        if (meta.isThumbnailAvailable()) {
            final VFSLeaf thumb = meta.getThumbnail(200, 200);
            if (thumb != null) {
                mr = new VFSMediaResource(thumb);
            }
            if (mr != null) {
                final String thumbMapper = MapperRegistry.getInstanceFor(ureq.getUserSession()).register(new Mapper() {
                    @SuppressWarnings("unused")
                    @Override
                    public MediaResource handle(final String relPath, final HttpServletRequest request) {
                        return mr;
                    }
                });
                vC.contextPut("thumbMapper", thumbMapper);
            }
        }
        if (!readOnlyMode) {
            // allow to delete
            delLink = LinkFactory.createLink("delete.file", vC, this);
            delLink.setUserObject(file);
        }

        viewPanel.setContent(vC);
    }

    @SuppressWarnings("unused")
    private void initUploadView(final UserRequest ureq) {
        vC = createVelocityContainer("fileDetailsUpload");
        uploadLink = LinkFactory.createLink("upload.link", vC, this);
        viewPanel.setContent(vC);
    }

    private void popupUploadCallout(final UserRequest ureq) {
        removeAsListenerAndDispose(fileUploadCtrl);
        fileUploadCtrl = new EPCreateFileArtefactStepForm00(ureq, getWindowControl(), fArtefact);
        listenTo(fileUploadCtrl);
        removeAsListenerAndDispose(calloutCtrl);
        calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), fileUploadCtrl.getInitialComponent(), uploadLink, fArtefact.getTitle(), true, null);
        calloutCtrl.addDisposableChildController(fileUploadCtrl);
        listenTo(calloutCtrl);
        calloutCtrl.activate();
    }

    @SuppressWarnings("unused")
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == delLink) {
            delDialog = activateYesNoDialog(ureq, translate("delete.file"), translate("delete.dialog"), delDialog);
            delDialog.setUserObject(delLink.getUserObject());
        } else if (source == uploadLink) {
            popupUploadCallout(ureq);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == delDialog && DialogBoxUIFactory.isYesEvent(event)) {
            final VFSItem artefactFile = (VFSItem) delDialog.getUserObject();
            artefactFile.delete();
            fArtefact.setFilename("");
            ePFMgr.updateArtefact(fArtefact);
            initViewDependingOnFileExistance(ureq);
        } else if (source == fileUploadCtrl) {
            calloutCtrl.deactivate();
            removeAsListenerAndDispose(calloutCtrl);
            ePFMgr.updateArtefact(fArtefact);
            initViewDependingOnFileExistance(ureq);
        } else if (source == calloutCtrl && event.equals(CloseableCalloutWindowController.CLOSE_WINDOW_EVENT)) {
            removeAsListenerAndDispose(calloutCtrl);
            calloutCtrl = null;
        }
    }

    @Override
    protected void doDispose() {
        // nothing
    }

}
