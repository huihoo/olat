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
 * Copyright (c) 1999-2008 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.filebrowser.commands;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileNameValidator;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.core.notification.service.PublishEventTO.EventType;
import org.olat.lms.core.notification.service.Subscribed;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.folder.FolderNotificationService;
import org.olat.lms.folder.FolderNotificationTypeHandler;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.common.htmleditor.HTMLEditorController;
import org.olat.presentation.framework.common.htmleditor.WysiwygFactory;
import org.olat.presentation.framework.common.plaintexteditor.PlainTextEditorController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 13.12.2005
 * 
 * @author Florian Gnägi Description: A panel with a FolderComponent and a CreateFileForm. TODO: LD: check status to show if an error occurred.
 */
public class CmdCreateFile extends BasicController implements FolderCommand {
    private static final Logger log = LoggerHelper.getLogger();

    private int status = FolderCommandStatus.STATUS_SUCCESS;
    private FolderComponent folderComponent;
    private VelocityContainer mainVC;
    private Panel mainPanel;

    private CreateFileForm createFileForm;
    private Controller editorCtr;
    private String fileName;
    private String relFilePath;
    private FolderNotificationService folderNotificationService;
    private FolderNotificationTypeHandler folderNotificationTypeHandler;

    private static Map<String, String> i18nkeyMap;

    static {
        i18nkeyMap = new HashMap<String, String>();
        i18nkeyMap.put(AbstractCreateItemForm.TEXT_ELEM_I18N_KEY, "cfile.name");
        i18nkeyMap.put(AbstractCreateItemForm.SUBMIT_ELEM_I18N_KEY, "cfile.create");
        i18nkeyMap.put(AbstractCreateItemForm.RESET_ELEM_I18N_KEY, "cancel");
    }

    protected CmdCreateFile(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        folderNotificationService = CoreSpringFactory.getBean(FolderNotificationService.class);
        folderNotificationTypeHandler = CoreSpringFactory.getBean(FolderNotificationTypeHandler.class);
    }

    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
        this.setTranslator(translator);
        if (folderComponent.getCurrentContainer().canWrite() != VFSConstants.YES) {
            throw new AssertException("Illegal attempt to create file in: " + folderComponent.getCurrentContainerPath());
        }

        mainVC = this.createVelocityContainer("createFilePanel");
        mainPanel = this.putInitialPanel(mainVC);

        this.folderComponent = folderComponent;
        mainVC.put("foldercomp", folderComponent);

        createFileForm = new CreateFileForm(ureq, wControl, translator, folderComponent);
        this.listenTo(createFileForm);
        mainVC.put("createFileForm", createFileForm.getInitialComponent());

        // check for quota
        long quotaLeft = VFSManager.getQuotaLeftKB(folderComponent.getCurrentContainer());
        if (quotaLeft <= 0 && quotaLeft != -1) {
            String supportAddr = WebappHelper.getMailConfig("mailSupport");
            String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
            this.getWindowControl().setError(msg);
            return null;
        }

        return this;
    }

    @Override
    protected void doDispose() {

    }

    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        // empty
    }

    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == editorCtr) {
            if (event == Event.DONE_EVENT) {
                // we're done, notify listerers
                fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, fileName));
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
                notifyFileCreated(fileName, relFilePath, ureq.getIdentity());
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
            }
        } else if (source == createFileForm) {
            if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
            } else if (event == Event.FAILED_EVENT) {
                status = FolderCommandStatus.STATUS_FAILED;
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
            } else if (event == Event.DONE_EVENT) {
                // start HTML or plain text editor after file has been created
                // OLAT-3026: previous version checked here for writable container which caused this bug (file has been created but editor couldn't be opened).
                relFilePath = fileName;
                VFSContainer writableRootContainer = folderComponent.getCurrentContainer();
                if (relFilePath.endsWith(".html") || relFilePath.endsWith(".htm")) {
                    editorCtr = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), writableRootContainer, relFilePath, true);
                    ((HTMLEditorController) editorCtr).setNewFile(true);
                } else {
                    editorCtr = new PlainTextEditorController(ureq, getWindowControl(), (VFSLeaf) writableRootContainer.resolve(relFilePath), "utf-8", true, true, null);
                }

                this.listenTo(editorCtr);

                mainPanel.setContent(editorCtr.getInitialComponent());
            }
        }
    }

    private void notifyFileCreated(String fileName, String relFilePath, Identity creator) {
        VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
        if (secCallback instanceof Subscribed) {
            SubscriptionContext subsContext = ((Subscribed) secCallback).getSubscriptionContext();
            if (subsContext != null) {
                System.out.println("CmdCreateFile relFilePath=" + relFilePath);
                PublishEventTO publishEventTO = folderNotificationTypeHandler.createPublishEventTO(subsContext, folderComponent.courseNodeId(), creator, relFilePath,
                        fileName, EventType.NEW);
                folderNotificationService.publishEvent(publishEventTO);
            }
        } else {
            log.info("CmdCreateFile.notifyFileCreated: secCallback is not instanceof Subscribed for fileName=" + fileName + "  relFilePath=" + relFilePath + "  creator="
                    + creator);
        }
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int getStatus() {
        return status;
    }

    /**
     * Description:<br>
     * CreateFileForm implementation.
     * <P>
     * Initial Date: 28.01.2008 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class CreateFileForm extends AbstractCreateItemForm {

        public CreateFileForm(UserRequest ureq, WindowControl wControl, Translator translator, FolderComponent folderComponent) {
            super(ureq, wControl, translator, i18nkeyMap);
            textElement.setExampleKey("cfile.name.example", null);
        }

        @Override
        protected void formOK(UserRequest ureq) {
            // create the file
            VFSContainer currentContainer = folderComponent.getCurrentContainer();
            VFSItem item = currentContainer.createChildLeaf(getItemName());
            if (item == null) {
                this.fireEvent(ureq, Event.FAILED_EVENT);
                return;
            }
            if (item instanceof OlatRelPathImpl) {
                // update meta data
                FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
                MetaInfo meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) item);

                meta.setAuthor(ureq.getIdentity().getName());
                meta.write();
            }
            fileName = getItemName();
            fireEvent(ureq, Event.DONE_EVENT);
        }

        @Override
        protected boolean validateFormLogic(UserRequest ureq) {
            boolean isInputValid = true;
            String fileName = textElement.getValue();
            if (fileName == null || fileName.trim().equals("")) {
                textElement.setErrorKey("cfile.name.empty", new String[0]);
                isInputValid = false;
            } else {
                fileName = fileName.toLowerCase();
                // check if there are any unwanted path denominators in the name
                if (!validateFileName(fileName)) {
                    textElement.setErrorKey("cfile.name.notvalid", new String[0]);
                    isInputValid = false;
                    return isInputValid;
                } else if (!fileName.endsWith(".html") && !fileName.endsWith(".htm") && !fileName.endsWith(".txt") && !fileName.endsWith(".css")) {
                    // add html extension if missing
                    fileName = fileName + ".html";
                }
                // ok, file name is sanitized, let's see if a file with this name already exists
                VFSContainer currentContainer = folderComponent.getCurrentContainer();
                VFSItem item = currentContainer.resolve(fileName);
                if (item != null) {
                    textElement.setErrorKey("cfile.already.exists", new String[] { fileName });
                    isInputValid = false;
                } else {
                    isInputValid = true;
                    setItemName(fileName);
                }
            }
            return isInputValid;
        }

        private boolean validateFileName(String name) {
            return FileNameValidator.validate(name);
        }
    }

    @Override
    public boolean runsModal() {
        return false;
    }

}
