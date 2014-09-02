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
package org.olat.presentation.filebrowser.meta;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileNameValidator;
import org.olat.data.commons.vfs.OlatRelPathImpl;
import org.olat.data.commons.vfs.VFSConstants;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.filebrowser.metadata.MetaInfo;
import org.olat.data.user.User;
import org.olat.lms.commons.filemetadata.FileMetadataInfoHelper;
import org.olat.lms.commons.filemetadata.FileMetadataInfoService;
import org.olat.lms.commons.filemetadata.MetaInfoProvider;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.folder.FolderHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * This is the metadata flexiform controller with or without upload capability.
 * <P>
 * Initial Date: Jun 24, 2009 <br>
 * 
 * @author gwassmann
 */
public class MetaInfoFormController extends FormBasicController implements MetaInfoProvider {
    private VFSItem item;
    private MetaInfo meta;
    private FormLink moreMetaDataLink;
    private String initialFilename;
    private TextElement filename, title, publisher, creator, source, city, pages, language, url, comment, publicationMonth, publicationYear;
    private SingleSelection locked;
    // Fields needed for upload dialog
    private boolean isSubform;
    Set<FormItem> metaFields;

    /**
     * Use this controller for editing meta data of an existing file.
     * 
     * @param ureq
     * @param control
     */
    public MetaInfoFormController(UserRequest ureq, WindowControl control, VFSItem item) {
        super(ureq, control);
        this.isSubform = false;
        this.item = item;
        // load the metainfo
        FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
        // TODO: bad solution to trust in certain implementation of VFSItem
        meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) item);
        initForm(ureq);
    }

    /**
     * Use this constructor in a subform
     * 
     * @param ureq
     * @param control
     * @param uploadLimitKB
     * @param remainingQuotaKB
     */
    public MetaInfoFormController(UserRequest ureq, WindowControl control, Form parentForm) {
        super(ureq, control, FormBasicController.LAYOUT_DEFAULT, null, parentForm);
        this.isSubform = true;
        initForm(ureq);
    }

    /**
     * Use this if you want to use this controller in a subform but for an already existing VFS item.
     * 
     * @param ureq
     *            User request
     * @param wControl
     *            Window control
     * @param parentForm
     *            Parent form
     * @param vfsItem
     *            VFS item for which you want to edit the metadata
     */
    public MetaInfoFormController(UserRequest ureq, WindowControl wControl, Form parentForm, VFSItem vfsItem) {
        super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT, null, parentForm);
        this.isSubform = true;
        this.item = vfsItem;
        FileMetadataInfoService metaInfoService = CoreSpringFactory.getBean(FileMetadataInfoService.class);
        this.meta = metaInfoService.createMetaInfoFor((OlatRelPathImpl) vfsItem);
        initForm(ureq);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing so far
    }

    /**
	 */
    @Override
    protected void formOK(UserRequest ureq) {
        // done, parent controller takes care of saving metadata...
        fireEvent(ureq, Event.DONE_EVENT);
    }

    /**
	 */
    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    /**
     * org.olat.presentation.framework.components.form.flexible.FormItem, org.olat.presentation.framework.components.form.flexible.impl.FormEvent)
     */
    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == moreMetaDataLink && event.wasTriggerdBy(FormEvent.ONCLICK)) {
            // show metadata
            // and hide link
            setMetaFieldsVisible(true);
            this.flc.setDirty(true);
            moreMetaDataLink.setVisible(false);
        }
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.presentation.framework.UserRequest)
     */
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("mf.metadata.title");
        setFormContextHelp(MetaInfoFormController.class.getPackage().getName(), "bc-metainfo.html", "chelp.bc-metainfo.hover");

        // filename
        if (!isSubform) {
            initialFilename = item.getName();
            filename = uifactory.addTextElement("filename", "mf.filename", -1, item.getName(), formLayout);
            filename.setEnabled(item.canRename() == VFSConstants.YES);
            filename.setNotEmptyCheck("mf.error.empty");
            filename.setMandatory(true);
        }

        // title
        title = uifactory.addTextElement("title", "mf.title", -1, (meta != null ? meta.getTitle() : null), formLayout);

        // comment/description
        comment = uifactory.addTextAreaElement("comment", "mf.comment", -1, 3, 1, true, (meta != null ? meta.getComment() : null), formLayout);

        // creator
        creator = uifactory.addTextElement("creator", "mf.creator", -1, (meta != null ? meta.getCreator() : null), formLayout);

        // publisher
        publisher = uifactory.addTextElement("publisher", "mf.publisher", -1, (meta != null ? meta.getPublisher() : null), formLayout);

        // source/origin
        source = uifactory.addTextElement("source", "mf.source", -1, (meta != null ? meta.getSource() : null), formLayout);

        // city
        city = uifactory.addTextElement("city", "mf.city", -1, (meta != null ? meta.getCity() : null), formLayout);

        // publish date
        FormLayoutContainer publicationDate = FormLayoutContainer.createHorizontalFormLayout("publicationDateLayout", getTranslator());
        publicationDate.setLabel("mf.publishDate", null);
        formLayout.add(publicationDate);

        String[] pubDate = (meta != null ? meta.getPublicationDate() : new String[] { "", "" });
        publicationMonth = uifactory.addTextElement("publicationMonth", "mf.month", 2, pubDate[1], publicationDate);
        publicationMonth.setMaxLength(2);
        publicationMonth.setDisplaySize(2);

        publicationYear = uifactory.addTextElement("publicationYear", "mf.year", 4, pubDate[0], publicationDate);
        publicationYear.setMaxLength(4);
        publicationYear.setDisplaySize(4);

        // number of pages
        pages = uifactory.addTextElement("pages", "mf.pages", -1, (meta != null ? meta.getPages() : null), formLayout);

        // language
        language = uifactory.addTextElement("language", "mf.language", -1, (meta != null ? meta.getLanguage() : null), formLayout);

        // url/link
        url = uifactory.addTextElement("url", "mf.url", -1, (meta != null ? meta.getUrl() : null), formLayout);

        /* static fields */
        String sizeText, typeText;
        if (item instanceof VFSLeaf) {
            sizeText = StringHelper.formatMemory(((VFSLeaf) item).getSize());
            typeText = FolderHelper.extractFileType(item.getName(), getLocale());
        } else {
            sizeText = "-";
            typeText = translate("mf.type.directory");
        }

        // Targets to hide
        metaFields = new HashSet<FormItem>();
        metaFields.add(creator);
        metaFields.add(publisher);
        metaFields.add(source);
        metaFields.add(city);
        metaFields.add(publicationDate);
        metaFields.add(pages);
        metaFields.add(language);
        metaFields.add(url);

        if (!hasMetadata()) {
            moreMetaDataLink = uifactory.addFormLink("mf.more.meta.link", formLayout, Link.LINK_CUSTOM_CSS);
            moreMetaDataLink.setCustomEnabledLinkCSS("b_link_moreinfo");
            setMetaFieldsVisible(false);
        }

        if (!isSubform) {

            if (!meta.isDirectory()) {
                Long lockedById = meta.getLockedBy();
                // locked
                String lockedTitle = getTranslator().translate("mf.locked");
                String unlockedTitle = getTranslator().translate("mf.unlocked");
                locked = uifactory.addRadiosHorizontal("locked", "mf.locked", formLayout, new String[] { "lock", "unlock" }, new String[] { lockedTitle, unlockedTitle });
                if (meta.isLocked()) {
                    locked.select("lock", true);
                } else {
                    locked.select("unlock", true);
                }
                locked.setEnabled(!FileMetadataInfoHelper.isLocked(item, ureq.getIdentity(), ureq.getUserSession().getRoles().isOLATAdmin()));

                // locked by
                String lockedDetails = "";
                if (lockedById != null) {
                    Identity lockedIdentity = meta.getLockedByIdentity();
                    String user = getUserService().getFirstAndLastname(lockedIdentity.getUser());
                    String date = "";
                    if (meta.getLockedDate() != null) {
                        date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(meta.getLockedDate());
                    }
                    lockedDetails = getTranslator().translate("mf.locked.description", new String[] { user, date });
                } else {
                    lockedDetails = getTranslator().translate("mf.unlocked.description");
                }
                uifactory.addStaticTextElement("mf.lockedBy", lockedDetails, formLayout);
            }

            // username
            uifactory.addStaticTextElement("mf.author", getHTMLFormattedAuthor(meta.getAuthorIdentity()), formLayout);

            // filesize
            uifactory.addStaticTextElement("mf.size", sizeText, formLayout);

            // last modified date
            uifactory.addStaticTextElement("mf.lastModified", StringHelper.formatLocaleDate(meta.getLastModified(), getLocale()), formLayout);

            // file type
            uifactory.addStaticTextElement("mf.type", typeText, formLayout);

            uifactory.addStaticTextElement("mf.downloads", String.valueOf(meta.getDownloadCount()), formLayout);
        }

        if (!isSubform && meta.isDirectory()) {
            // Don't show any meta data except title and comment if the item is
            // a directory.
            // Hide the metadata.
            setMetaFieldsVisible(false);
            if (moreMetaDataLink != null)
                moreMetaDataLink.setVisible(false);
        }

        // save and cancel buttons
        if (!isSubform) {
            final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
            formLayout.add(buttonLayout);
            uifactory.addFormSubmitButton("submit", buttonLayout);
            uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
        }
    }

    /**
     * @return True if one or more metadata fields are non-emtpy.
     */
    private boolean hasMetadata() {
        if (meta != null) {
            return StringHelper.containsNonWhitespace(meta.getCreator()) || StringHelper.containsNonWhitespace(meta.getPublisher())
                    || StringHelper.containsNonWhitespace(meta.getSource()) || StringHelper.containsNonWhitespace(meta.getCity())
                    || StringHelper.containsNonWhitespace(meta.getPublicationDate()[0]) || StringHelper.containsNonWhitespace(meta.getPublicationDate()[1])
                    || StringHelper.containsNonWhitespace(meta.getPages()) || StringHelper.containsNonWhitespace(meta.getLanguage())
                    || StringHelper.containsNonWhitespace(meta.getUrl());
        }
        return false;
    }

    /**
     * Shows or hides the metadata
     * 
     * @param visible
     */
    private void setMetaFieldsVisible(boolean visible) {
        for (FormItem formItem : metaFields) {
            formItem.setVisible(visible);
        }
    }

    /**
     * @return true if the item has been given a new name by the user.
     */
    public boolean isFileRenamed() {
        if (initialFilename == null || filename == null) {
            return false;
        }
        return (!initialFilename.equals(filename.getValue()));
    }

    /**
     * @return The filename
     */
    public String getFilename() {
        return filename.getValue();
    }

    /**
     * @return The updated MeatInfo object
     */
    public MetaInfo getMetaInfo() {
        meta.setCreator(creator.getValue());
        meta.setComment(comment.getValue());
        meta.setTitle(title.getValue());
        meta.setPublisher(publisher.getValue());
        meta.setPublicationDate(publicationMonth.getValue(), publicationYear.getValue());
        meta.setCity(city.getValue());
        meta.setLanguage(language.getValue());
        meta.setSource(source.getValue());
        meta.setUrl(url.getValue());
        meta.setPages(pages.getValue());
        if (!isSubform && (meta != null && !meta.isDirectory()) && (locked != null && locked.isEnabled())) {
            // isSubForm
            boolean alreadyLocked = meta.isLocked();
            boolean currentlyLocked = locked.isSelected(0);
            if (!currentlyLocked || !alreadyLocked) {
                meta.setLocked(currentlyLocked);
                if (meta.isLocked()) {
                    meta.setLockedBy(getIdentity().getKey());
                    meta.setLockedDate(new Date());
                }
            }
        }
        return meta;
    }

    /**
     * Puts the metadata of this form into the existing metaInfo object and returns it.
     * 
     * @param meta
     * @return The MetaInfo object with the attributes of the form
     */
    @Override
    public MetaInfo getMetaInfo(MetaInfo meta) {
        this.meta = meta;
        return getMetaInfo();
    }

    /**
	 */
    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean valid = true;

        // validate publication month
        String monthStr = publicationMonth.getValue();
        if (StringHelper.containsNonWhitespace(monthStr)) {
            int month = -1;
            try {
                month = Integer.valueOf(monthStr.trim());
            } catch (NumberFormatException e) {
                // monthStr is non valid integer value
            }
            if (!(month >= 1 && month <= 12)) {
                // publicationMonth is invalid
                publicationMonth.setErrorKey("mf.wrong.month.value", null);
                valid = false;
            }
        }

        // validate publication year
        String yearStr = publicationYear.getValue();
        if (StringHelper.containsNonWhitespace(yearStr)) {
            try {
                Integer.valueOf(yearStr.trim());
            } catch (NumberFormatException e) {
                // yearStr is non valid integer value
                publicationYear.setErrorKey("mf.wrong.year.value", null);
                valid = false;
            }
        }

        if (isFileRenamed()) {
            valid = FileNameValidator.validate(this.getFilename());
            if (!valid) {
                filename.setErrorKey("file.name.notvalid", new String[0]);
            }
        }

        return valid;
    }

    /**
     * Get the form item representing this form
     * 
     * @return
     */
    public FormItem getFormItem() {
        return this.flc;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    /**
     * helper method
     */
    private String getHTMLFormattedAuthor(Identity identity) {
        if (identity == null) {
            return "-";
        }
        final User user = identity.getUser();
        String formattedName = getUserService().getFirstAndLastname(user);
        formattedName = formattedName + " (" + identity.getName() + ")";
        return formattedName;
    }

}
