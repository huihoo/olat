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
package org.olat.presentation.framework.common.htmleditor;

import java.util.Date;

import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.lms.framework.htmleditor.HTMLEditor_EBL;
import org.olat.lms.framework.htmleditor.HtmlPage;
import org.olat.presentation.framework.common.linkchooser.CustomLinkTreeModel;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.RichTextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.Formatter;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The HTMLEditorController provides a full-fledged WYSIWYG HTML editor with support for media and link browsing based on a VFS item. The editor will keep any header
 * information such as references to CSS or JS files, but those will not be active while editing the file.
 * <p>
 * Keep in mind that this editor might be destructive when editing files that have been created with an external, more powerful editor.
 * <p>
 * Use the WYSIWYGFactory to create an instance.
 * <P>
 * Initial Date: 08.05.2009 <br>
 * 
 * @author gnaegi
 */
public class HTMLEditorController extends FormBasicController {

    private String preface; // null if no head, otherwise head is kept in memory
    private String body; // Content of body tag
    private String charSet = HTMLEditor_EBL.UTF_8; // default for first parse attempt

    private String fileName, fileRelPath;
    private LockResult lock;

    private RichTextElement htmlElement;
    private VFSContainer baseContainer;
    private VFSLeaf fileLeaf;
    private FormLink cancel, save, saveClose;
    private CustomLinkTreeModel customLinkTreeModel;

    private VelocityContainer metadataVC;
    private boolean newFile = true;
    private boolean editorCheckEnabled = true; // default

    private HTMLEditor_EBL htlmEditorEbl;

    /**
     * Factory method to create a file based HTML editor instance that uses locking to prevent two people editing the same file.
     * 
     * @param ureq
     * @param wControl
     * @param baseContainer
     *            the baseContainer (below that folder all images can be chosen)
     * @param relFilePath
     *            the file e.g. "index.html"
     * @param userActivityLogger
     *            the userActivity Logger if used
     * @param customLinkTreeModel
     *            Model for internal-link tree e.g. course-node tree with link information
     * @param editorCheckEnabled
     *            true: check if file has been created with another tool and warn user about potential data loss; false: ignore other authoring tools
     * @return Controller with internal-link selector
     */
    protected HTMLEditorController(UserRequest ureq, WindowControl wControl, VFSContainer baseContainer, String relFilePath, CustomLinkTreeModel customLinkTreeModel,
            boolean editorCheckEnabled) {
        super(ureq, wControl, "htmleditor");
        htlmEditorEbl = CoreSpringFactory.getBean(HTMLEditor_EBL.class);
        // set some basic variables
        this.baseContainer = baseContainer;
        this.fileRelPath = relFilePath;
        this.customLinkTreeModel = customLinkTreeModel;
        this.editorCheckEnabled = editorCheckEnabled;
        // make sure the filename doesn't start with a slash
        this.fileName = ((relFilePath.charAt(0) == '/') ? relFilePath.substring(1) : relFilePath);
        this.fileLeaf = (VFSLeaf) baseContainer.resolve(fileName);
        if (fileLeaf == null)
            throw new AssertException("file::" + htlmEditorEbl.getFileDebuggingPath(baseContainer, relFilePath) + " does not exist!");
        // check if someone else is already editing the file
        if (fileLeaf instanceof LocalFileImpl) {
            this.lock = htlmEditorEbl.getLockFor(fileLeaf, baseContainer, relFilePath, ureq.getIdentity());
            VelocityContainer vc = (VelocityContainer) flc.getComponent();
            if (!lock.isSuccess()) {
                vc.contextPut("locked", Boolean.TRUE);
                vc.contextPut("lockOwner", lock.getOwner().getName());
                return;
            } else {
                vc.contextPut("locked", Boolean.FALSE);
            }
        }
        // Parse the content of the page
        HtmlPage htmlPage = htlmEditorEbl.parsePage(fileLeaf, baseContainer, fileRelPath);
        this.body = htmlPage.getBody();
        this.preface = htmlPage.getPreface();

        if (this.editorCheckEnabled && htmlPage.isHasNoGeneratorMetaData()) {
            showWarning("warn.foreigneditor");
        }
        // load form now
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        releaseLock();
    }

    public void releaseLock() {
        htlmEditorEbl.releaseLock(lock);
    }

    /**
	 */
    @Override
    protected void formOK(UserRequest ureq) {
        saveDataAndUpdateView();
        // override dirtyness of form layout container to prevent redrawing of editor
        this.flc.setDirty(false);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        super.formInnerEvent(ureq, source, event);
        if (source == htmlElement) {
            // nothing to catch
        } else if (source == save && lock != null) {
            saveDataAndUpdateView();
        } else if (source == saveClose && lock != null) {
            saveDataAndUpdateView();
            fireEvent(ureq, Event.DONE_EVENT);
            htlmEditorEbl.releaseLock(lock);
        } else if (source == cancel) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
            htlmEditorEbl.releaseLock(lock);
        }
    }

    /**
     * 
     */
    private void saveDataAndUpdateView() {
        htlmEditorEbl.saveData(htmlElement.getRawValue(), fileLeaf, preface, charSet, getIdentity());
        updateView(htmlElement.getRawValue());
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        htmlElement = uifactory.addRichTextElementForFileData("rtfElement", null, body, -1, -1, false, baseContainer, fileName, customLinkTreeModel, formLayout,
                ureq.getUserSession(), getWindowControl());
        //
        // Add resize handler
        RichTextConfiguration editorConfiguration = htmlElement.getEditorConfiguration();
        editorConfiguration.addOnInitCallbackFunction("b_resizetofit_htmleditor");
        editorConfiguration.setNonQuotedConfigValue(RichTextConfiguration.HEIGHT, "b_initialEditorHeight()");
        //
        // The buttons
        save = uifactory.addFormLink("savebuttontext", formLayout, Link.BUTTON);
        save.addActionListener(this, FormEvent.ONCLICK);
        cancel = uifactory.addFormLink("cancel", formLayout, Link.BUTTON);
        cancel.addActionListener(this, FormEvent.ONCLICK);
        saveClose = uifactory.addFormLink("saveandclosebuttontext", formLayout, Link.BUTTON);
        saveClose.addActionListener(this, FormEvent.ONCLICK);
        //
        // Add some file metadata
        VelocityContainer vc = (VelocityContainer) formLayout.getComponent();
        metadataVC = createVelocityContainer("metadata");
        vc.put("metadata", metadataVC);
        long lm = fileLeaf.getLastModified();
        metadataVC.contextPut("lastModified", Formatter.getInstance(ureq.getLocale()).formatDateAndTime(new Date(lm)));
        metadataVC.contextPut("charSet", charSet);
        metadataVC.contextPut("fileName", fileName);
    }

    /**
     * Optional configuration option to display the save button below the HTML editor form. This will not disable the save button in the tinyMCE bar (if available).
     * Default: true
     * 
     * @param buttonEnabled
     *            true: show save button; false: hide save button
     */
    public void setSaveButtonEnabled(boolean buttonEnabled) {
        save.setVisible(buttonEnabled);
    }

    /**
     * Optional configuration option to display the save-and-close button below the HTML editor form. This will not disable the save button in the tinyMCE bar (if
     * available). Default: true
     * 
     * @param buttonEnabled
     *            true: show save-and-close button; false: hide save-and-close button
     */
    public void setSaveCloseButtonEnabled(boolean buttonEnabled) {
        saveClose.setVisible(buttonEnabled);
    }

    /**
     * Optional configuration option to display the cancel button below the HTML editor form. This will not disable the cancel button in the tinyMCE bar (if available).
     * Default: true
     * 
     * @param buttonEnabled
     *            true: show cancel button; false: hide cancel button
     */
    public void setCancelButtonEnabled(boolean buttonEnabled) {
        cancel.setVisible(buttonEnabled);
    }

    /**
     * Optional configuration to show the file name, file encoding and last modified date in a header bar. Default: true
     * 
     * @param metadataEnabled
     *            true: show metadata; false: hide metadata
     */
    public void setShowMetadataEnabled(boolean metadataEnabled) {
        VelocityContainer vc = (VelocityContainer) this.flc.getComponent();
        if (metadataEnabled) {
            vc.put("metadata", metadataVC);
        } else {
            vc.remove(metadataVC);
        }
    }

    /**
     * Get the rich text config object. This can be used to fine-tune the editor features, e.g. to enable additional buttons or to remove available buttons
     * 
     * @return
     */
    public RichTextConfiguration getRichTextConfiguration() {
        return htmlElement.getEditorConfiguration();
    }

    private void updateView(String newContent) {
        // Update last modified date in view
        long lm = fileLeaf.getLastModified();
        metadataVC.contextPut("lastModified", Formatter.getInstance(getLocale()).formatDateAndTime(new Date(lm)));
        // Set new content as default value in element
        htmlElement.setNewOriginalValue(newContent);
    }

    public boolean isNewFile() {
        return newFile;
    }

    public void setNewFile(boolean newFile) {
        this.newFile = newFile;
    }
}
