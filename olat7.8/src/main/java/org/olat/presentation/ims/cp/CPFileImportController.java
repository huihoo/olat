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
package org.olat.presentation.ims.cp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.VFSManager;
import org.olat.data.commons.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.data.commons.vfs.filters.VFSItemFilter;
import org.olat.lms.ims.cp.CPManager;
import org.olat.lms.ims.cp.CPPage;
import org.olat.lms.ims.cp.ContentPackage;
import org.olat.presentation.filebrowser.FileUploadController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * File import controller. Supported files: - html - zip (including folders and html files)
 * <p>
 * Fires: NewCPPageEvent
 * <P>
 * Initial Date: May 5, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPFileImportController extends FormBasicController {
    private static final String ALL = "all";
    private static final String[] prefixes = new String[] { "." };
    private static final VFSItemFilter excludeMetaFilesFilter = new VFSItemExcludePrefixFilter(prefixes);
    private static final List<String> extensions = new ArrayList<String>();

    private FileElement file;
    private FormLink cancelButton;
    private MultipleSelectionElement checkboxes;
    private final ContentPackage cp;
    private final CPPage currentPage;
    private CPPage pageToBeSelected = null;
    private boolean isSingleFile;

    /**
     * @param ureq
     * @param control
     */
    public CPFileImportController(final UserRequest ureq, final WindowControl control, final ContentPackage cp, final CPPage currentPage) {
        super(ureq, control);

        // need a translation from FileUploadController (avoiding key-duplicates)
        setTranslator(new PackageTranslator(FileUploadController.class.getPackage().getName(), getLocale(), getTranslator()));

        this.cp = cp;
        this.currentPage = currentPage;
        initForm(ureq);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        String fileName = file.getUploadFileName();
        if (fileName == null) {
            file.setErrorKey("NoFileChosen", null);
            return false;
        }
        return super.validateFormLogic(ureq);
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        this.setFormTitle("cpfileuploadcontroller.form.title");
        this.setFormDescription("cpfileuploadcontroller.form.description");

        file = uifactory.addFileElement("file", this.flc);
        file.setLabel("cpfileuploadcontroller.import.text", null);
        file.addActionListener(this, FormEvent.ONCHANGE);

        // checkboxes
        final String[] keys = { "htm", "pdf", "doc", "xls", "ppt", ALL };
        final String[] values = { "HTML", "PDF", "Word", "Excel", "PowerPoint", translate("cpfileuploadcontroller.form.all.types") };
        checkboxes = uifactory.addCheckboxesVertical("checkboxes", "cpfileuploadcontroller.form.file.types", this.flc, keys, values, null, 1);
        checkboxes.setVisible(false);

        // Submit and cancel buttons
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        this.flc.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", "cpfileuploadcontroller.import.button", buttonLayout);
        cancelButton = uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

    /**
	 */
    @Override
    protected void formOK(final UserRequest ureq) {
        uploadFiles();
        final Event pageImported = new NewCPPageEvent("pages imported", pageToBeSelected());
        this.fireEvent(ureq, pageImported);
    }

    /**
     * Upload the selected files and import them into the content package.
     */
    private void uploadFiles() {
        final VFSContainer root = cp.getRootDir();
        final String filenName = file.getUploadFileName();
        file.logUpload();
        // don't check on mimetypes - some browser use different mime types when sending files (OLAT-4547)
        if (filenName.toLowerCase().endsWith(".zip")) {
            // unzip and add files
            final VFSLeaf archive = new LocalFileImpl(file.getUploadFile());
            final String archiveName = file.getUploadFileName();
            String unzipDirName = archiveName.substring(0, archiveName.toLowerCase().indexOf(".zip"));
            unzipDirName = VFSManager.similarButNonExistingName(root, unzipDirName);
            final VFSContainer unzipDir = root.createChildContainer(unzipDirName);
            ZipUtil.unzip(archive, unzipDir);
            // add items of unzipDir to tree
            pageToBeSelected = addItem(unzipDir, currentPage.getIdentifier(), true);
            ((CPManager) CoreSpringFactory.getBean(CPManager.class)).writeToFile(cp);

        } else {
            // Single file
            final VFSLeaf uploadedItem = new LocalFileImpl(file.getUploadFile());
            uploadedItem.rename(file.getUploadFileName());
            // rename to unique name in root folder
            renameToNonExistingDesignation(root, uploadedItem);
            // move item to root folder
            root.copyFrom(uploadedItem);
            pageToBeSelected = addItem(uploadedItem, currentPage.getIdentifier(), false);
            ((CPManager) CoreSpringFactory.getBean(CPManager.class)).writeToFile(cp);
        }
    }

    /**
     * Adds all vfs items of parent to the menu tree item identified by parentId.
     * 
     * @param root
     * @param parent
     * @param parentId
     */
    private void addSubItems(final VFSContainer parent, final String parentId) {
        for (final VFSItem item : parent.getItems(excludeMetaFilesFilter)) {
            addItem(item, parentId, false);
        }
    }

    /**
     * Adds the vfs item to the menu tree below the parentId item.
     * 
     * @param item
     * @param parentId
     */
    private CPPage addItem(final VFSItem item, final String parentId, final boolean isRoot) {
        final CPManager cpMgr = (CPManager) CoreSpringFactory.getBean(CPManager.class);

        // Make an item in the menu tree only if the item is a container that
        // contains any items to be added or its type is selected in the form.
        // Upload any files in case they are referenced to.

        // Get the file types that should be added as items in the menu tree
        final Set<String> menuItemTypes = checkboxes.getSelectedKeys();
        if (menuItemTypes.contains("htm")) {
            menuItemTypes.add("html");
        }

        // If item is the root node and it doesn't contain any items to be added,
        // show info.
        if (isRoot && item instanceof VFSContainer && !containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
            showInfo("cpfileuploadcontroller.no.files.imported");
        }

        CPPage newPage = null;
        if (isSingleFile || item instanceof VFSLeaf && isToBeAdded((VFSLeaf) item, menuItemTypes) || item instanceof VFSContainer
                && containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
            // Create the menu item
            final String newId = cpMgr.addBlankPage(cp, item.getName(), parentId);
            newPage = new CPPage(newId, cp);
            if (item instanceof VFSLeaf) {
                final VFSLeaf leaf = (VFSLeaf) item;
                newPage.setFile(leaf);
            }
            cpMgr.updatePage(cp, newPage);
        }

        // Add any sub items
        if (item instanceof VFSContainer && containsItemsToAdd((VFSContainer) item, menuItemTypes)) {
            final VFSContainer dir = (VFSContainer) item;
            addSubItems(dir, newPage.getIdentifier());
        }
        return newPage;
    }

    /**
     * Breadth-first search for leafs inside the container that are to be added to the tree.
     * 
     * @param container
     * @param menuItemTypes
     * @return true if there is a leaf inside container that should be added
     */
    private boolean containsItemsToAdd(final VFSContainer container, final Set<String> menuItemTypes) {
        final LinkedList<VFSItem> queue = new LinkedList<VFSItem>();
        // enqueue root node
        queue.add(container);
        do {
            // dequeue and exmaine
            final VFSItem item = queue.poll();
            if (item instanceof VFSLeaf) {
                if (isToBeAdded((VFSLeaf) item, menuItemTypes)) {
                    // node found, return
                    return true;
                }
            } else {
                // enqueue successors
                final VFSContainer parent = (VFSContainer) item;
                queue.addAll(parent.getItems());
            }
        } while (!queue.isEmpty());
        return false;
    }

    /**
     * @param item
     * @param menuItemTypes
     * @return true if the item is to be added to the tree
     */
    private boolean isToBeAdded(final VFSLeaf item, final Set<String> menuItemTypes) {
        String extension = null;
        if (!menuItemTypes.contains(ALL)) {
            final String name = item.getName();
            final int dotIndex = name.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = name.substring(dotIndex + 1);
            }
        }
        return menuItemTypes.contains(ALL) || menuItemTypes.contains(extension);
    }

    /**
     * Renames a file to a non existing file designation.
     * 
     * @param root
     * @param item
     */
    private void renameToNonExistingDesignation(final VFSContainer root, final VFSItem item) {
        final String newName = VFSManager.similarButNonExistingName(root, item.getName());
        item.rename(newName);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == cancelButton && event.wasTriggerdBy(FormEvent.ONCLICK)) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        } else if (source == file && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
            // If a zip file was selected show import options. Else hide'em.
            if (file.getUploadFileName().endsWith(".zip")) {
                checkboxes.setVisible(true);
                checkboxes.selectAll();
                checkboxes.select(ALL, false);
                isSingleFile = false;
            } else {
                checkboxes.setVisible(false);
                // If a single file is selected, it is added to the menu tree no matter
                // what type it is.
                isSingleFile = true;
            }
            // Needed since checkbox component wasn't initially rendered
            this.flc.setDirty(true);
        }
    }

    /**
     * @return The file element of this form
     */
    public CPPage pageToBeSelected() {
        return pageToBeSelected != null ? pageToBeSelected : currentPage;
    }
}
