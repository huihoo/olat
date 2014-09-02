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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatRootFileImpl;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.Subscribed;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.folder.FolderNotificationService;
import org.olat.lms.folder.FolderNotificationTypeHandler;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.filebrowser.components.ListRenderer;
import org.olat.presentation.filebrowser.meta.MetaInfoFormController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

public class CmdEditMeta extends BasicController implements FolderCommand {

    private static final Logger log = LoggerHelper.getLogger();

    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private MetaInfoFormController metaInfoCtr;
    private VFSItem currentItem;
    private Translator translator;
    private FolderNotificationTypeHandler folderNotificationTypeHandler;
    private FolderNotificationService folderNotificationService;
    private FolderComponent folderComponent;

    protected CmdEditMeta(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    /**
     * Checks if the file/folder name is not null and valid, checks if the FolderComponent is ok, checks if the item exists and is not locked.
     * 
     * org.olat.presentation.framework.control.WindowControl, org.olat.presentation.framework.translator.Translator)
     */
    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
        this.translator = trans;
        this.folderComponent = folderComponent;
        folderNotificationService = CoreSpringFactory.getBean(FolderNotificationService.class);
        folderNotificationTypeHandler = CoreSpringFactory.getBean(FolderNotificationTypeHandler.class);
        String pos = ureq.getParameter(ListRenderer.PARAM_EDTID);
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

        if (metaInfoCtr != null)
            metaInfoCtr.dispose();
        metaInfoCtr = new MetaInfoFormController(ureq, wControl, currentItem);
        listenTo(metaInfoCtr);
        putInitialPanel(metaInfoCtr.getInitialComponent());
        return this;
    }

    @Override
    public int getStatus() {
        return status;
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
        if (source == metaInfoCtr) {
            if (event == Event.DONE_EVENT) {
                MetaInfo meta = metaInfoCtr.getMetaInfo();
                meta.write();
                String fileName = metaInfoCtr.getFilename();

                if (metaInfoCtr.isFileRenamed()) {
                    // IMPORTANT: First rename the meta data because underlying file
                    // has to exist in order to work properly on it's meta data.
                    VFSContainer container = currentItem.getParentContainer();
                    if (container.resolve(fileName) != null) {
                        getWindowControl().setError(translator.translate("TargetNameAlreadyUsed"));
                        status = FolderCommandStatus.STATUS_FAILED;
                    } else {
                        if (meta != null) {
                            meta.rename(fileName);
                        }
                        if (VFSConstants.NO.equals(currentItem.rename(fileName))) {
                            getWindowControl().setError(translator.translate("FileRenameFailed", new String[] { fileName }));
                            status = FolderCommandStatus.STATUS_FAILED;
                        }
                    }
                }
                if (currentItem instanceof OlatRootFileImpl) {
                    notifyFileChanged(ureq.getIdentity());
                }
                fireEvent(ureq, new FolderEvent(FolderEvent.EDIT_EVENT, fileName));
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);

            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            }

        }
    }

    private void notifyFileChanged(Identity creator) {
        VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
        if (secCallback instanceof Subscribed) {
            SubscriptionContext subsContext = ((Subscribed) secCallback).getSubscriptionContext();
            if (subsContext != null && currentItem != null) {
                PublishEventTO publishEventTO = folderNotificationTypeHandler.createPublishEventTO(subsContext, folderComponent.courseNodeId(), creator,
                        folderComponent.getCurrentContainerPath() + "/", currentItem.getName(), EventType.CHANGED);
                folderNotificationService.publishEvent(publishEventTO);
            }
        } else {
            log.debug("notifyFileChanged: secCallback is not instanceof Subscribed for currentItem.getName(): " + currentItem.getName());
        }
    }

    @Override
    protected void doDispose() {
        // metaInfoCtr should be auto-disposed
    }

    @Override
    public boolean runsModal() {
        return false;
    }
}
