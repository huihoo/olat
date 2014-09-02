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

import java.util.ArrayList;
import java.util.List;

import org.olat.data.commons.fileutil.FileNameValidator;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.filebrowser.metadata.tagged.MetaTagged;
import org.olat.presentation.filebrowser.FileSelection;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.filebrowser.components.FolderComponent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormReset;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * Provides a CreateItemForm and creates a zip file if input valid. TODO: LD: check status to show if an error occured.
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class CmdZip extends BasicController implements FolderCommand {

    private int status = FolderCommandStatus.STATUS_SUCCESS;

    private VelocityContainer mainVC;
    private CreateItemForm createItemForm;
    private VFSContainer currentContainer;
    private FileSelection selection;

    protected CmdZip(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
    }

    @Override
    public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
        this.setTranslator(trans);
        currentContainer = folderComponent.getCurrentContainer();
        if (currentContainer.canWrite() != VFSConstants.YES) {
            throw new AssertException("Cannot write to current folder.");
        }

        status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
        if (status == FolderCommandStatus.STATUS_FAILED) {
            return null;
        }

        selection = new FileSelection(ureq, folderComponent.getCurrentContainerPath());
        status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
        if (status == FolderCommandStatus.STATUS_FAILED) {
            return null;
        }

        mainVC = createVelocityContainer("createZipPanel");
        mainVC.contextPut("fileselection", selection);

        createItemForm = new CreateItemForm(ureq, wControl, trans);
        listenTo(createItemForm);
        mainVC.put("createItemForm", createItemForm.getInitialComponent());
        putInitialPanel(mainVC);
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
        // empty
    }

    @Override
    public void event(UserRequest ureq, Controller source, Event event) {
        if (source == createItemForm) {
            if (event == Event.CANCELLED_EVENT) {
                status = FolderCommandStatus.STATUS_CANCELED;
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
            } else if (event == Event.FAILED_EVENT) {
                // abort
                status = FolderCommandStatus.STATUS_FAILED;
                fireEvent(ureq, FOLDERCOMMAND_FINISHED);
            } else if (event == Event.DONE_EVENT) {
                // we're done, notify listerers
                fireEvent(ureq, new FolderEvent(FolderEvent.ZIP_EVENT, selection.renderAsHtml()));
                fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    /**
     * Description:<br>
     * Implementation of AbstractCreateItemForm.
     * <P>
     * Initial Date: 30.01.2008 <br>
     * 
     * @author Lavinia Dumitrescu
     */
    private class CreateItemForm extends AbstractCreateItemForm {

        public CreateItemForm(UserRequest ureq, WindowControl wControl, Translator translator) {
            super(ureq, wControl, translator);
        }

        @Override
        protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

            FormLayoutContainer horizontalLayout = FormLayoutContainer.createHorizontalFormLayout("itemLayout", getTranslator());
            formLayout.add(horizontalLayout);
            textElement = uifactory.addTextElement("fileName", "zip.name", 20, "", horizontalLayout);
            textElement.setMandatory(true);
            uifactory.addStaticTextElement("extension", null, translate("zip.extension"), horizontalLayout);

            FormLayoutContainer formButtons = FormLayoutContainer.createHorizontalFormLayout("formButton", getTranslator());
            formLayout.add(formButtons);
            createFile = new FormSubmit("submit", "zip.button");
            formButtons.add(createFile);
            reset = new FormReset("reset", "cancel");
            formButtons.add(reset);
        }

        /**
         * Creates a zipFile by using ZipUtil and fires Event.DONE_EVENT if successful.
         * 
         */
        @Override
        protected void formOK(UserRequest ureq) {
            VFSItem zipFile = currentContainer.createChildLeaf(getItemName());
            if (zipFile == null) {
                this.fireEvent(ureq, Event.FAILED_EVENT);
                return;
            }

            List<VFSItem> vfsFiles = new ArrayList<VFSItem>();
            for (String fileName : selection.getFiles()) {
                VFSItem item = currentContainer.resolve(fileName);
                if (item != null)
                    vfsFiles.add(item);
            }
            if (!ZipUtil.zip(vfsFiles, (VFSLeaf) zipFile, true)) {
                // cleanup zip file
                zipFile.delete();
                this.fireEvent(ureq, Event.FAILED_EVENT);
            } else {
                // check quota
                long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
                if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
                    // quota exceeded - rollback
                    zipFile.delete();
                    getWindowControl().setError(getTranslator().translate("QuotaExceeded"));
                    fireEvent(ureq, Event.DONE_EVENT);
                    return;
                }

                // author information
                if (zipFile instanceof MetaTagged) {
                    MetaInfo info = ((MetaTagged) zipFile).getMetaInfo();
                    if (info != null) {
                        info.setAuthor(ureq.getIdentity().getName());
                        info.write();
                    }
                }

                fireEvent(ureq, Event.DONE_EVENT);
            }
        }

        /**
         * Checks if input valid.
         * 
         */
        @Override
        protected boolean validateFormLogic(UserRequest ureq) {
            boolean isInputValid = true;
            String name = textElement.getValue();
            if (name == null || name.trim().equals("")) {
                textElement.setErrorKey("zip.name.empty", new String[0]);
                isInputValid = false;
            } else {
                if (!validateFileName(name)) {
                    textElement.setErrorKey("zip.name.notvalid", new String[0]);
                    isInputValid = false;
                    return isInputValid;
                }
                // Note: use java.io.File and not VFS to create a leaf. File must not exist upon ZipUtil.zip()
                name = name + ".zip";
                VFSItem zipFile = currentContainer.resolve(name);
                if (zipFile != null) {
                    textElement.setErrorKey("zip.alreadyexists", new String[] { name });
                    isInputValid = false;
                } else {
                    isInputValid = true;
                    setItemName(name);
                }
            }
            return isInputValid;
        }

        /**
         * Checks if filename contains any prohibited chars.
         * 
         * @param name
         * @return true if file name valid.
         */
        private boolean validateFileName(String name) {
            return FileNameValidator.validate(name);
        }
    }

    @Override
    public boolean runsModal() {
        return false;
    }

}
