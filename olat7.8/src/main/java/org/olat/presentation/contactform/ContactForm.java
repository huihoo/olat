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

package org.olat.presentation.contactform;

/* TODO: ORID-1007 'File' */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.FileElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.util.CSSHelper;
import org.olat.system.event.Event;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.MailHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * highly configurable contact form. Depending on each field value the corresponding fields become editable or not.
 * <p>
 * By creation time the defaults are: a FROM: field containing the logged in users e-mail, an empty TO: field not editable, empty SUBJECT: and BODY: fields both editable,
 * and last but not least a submit cancel buttons pair.
 * <P>
 * <ul>
 * Send-Limitations:
 * <li>maxLength for 'body' TextAreaElement -> 10000 characters, about 4 pages</li>>
 * <li>maxLength for 'to' TextAreaElement -> 30000 characters, enough space for a lot of mail group names, each mail group containing umlimited ammount of e-mail
 * addresses</li>
 * </ul>
 * Initial Date: Jul 19, 2004
 * 
 * @author patrick
 */

class ContactForm extends FormBasicController {
    //
    private final static String NLS_CONTACT_TO = "contact.to";
    private TextElement tto = null;
    private TextElement ttoBig = null;
    private final static String NLS_CONTACT_FROM = "contact.from";
    private TextElement tfrom;
    private final static String NLS_CONTACT_SUBJECT = "contact.subject";
    private TextElement tsubject;
    private final static String NLS_CONTACT_BODY = "contact.body";
    private TextElement tbody;
    private final static String NLS_CONTACT_ATTACHMENT = "contact.attachment";
    private final static String NLS_CONTACT_ATTACHMENT_EXPL = "contact.attachment.maxsize";
    private int contactAttachmentMaxSizeInMb = 5;
    private FileElement attachmentEl;
    private final List<FormLink> attachmentLinks = new ArrayList<FormLink>();
    private FormLayoutContainer uploadCont;
    private boolean recipientsAreEditable = false;
    private final static int emailCols = 100;
    private boolean readOnly = false;
    private boolean hasMsgCancel = false;
    private final static String NLS_CONTACT_SEND_CP_FROM = "contact.cp.from";
    private SelectionElement tcpfrom;
    private final Identity emailFrom;
    private File attachementTempDir;
    private long attachmentSize = 0l;
    private final Map<String, String> attachmentCss = new HashMap<String, String>();
    private final Map<String, String> attachmentNames = new HashMap<String, String>();
    private final Map<String, ContactList> contactLists = new Hashtable<String, ContactList>();

    public ContactForm(final UserRequest ureq, final WindowControl wControl, final Identity emailFrom, final boolean readOnly, final boolean isCancellable,
            final boolean hasRecipientsEditable) {
        super(ureq, wControl);
        this.emailFrom = emailFrom;
        this.readOnly = readOnly;
        this.recipientsAreEditable = hasRecipientsEditable;
        this.hasMsgCancel = isCancellable;
        this.contactAttachmentMaxSizeInMb = MailHelper.getMaxSizeForAttachement();
        initForm(ureq);
    }

    /**
     * @param defaultSubject
     */
    protected void setSubject(final String defaultSubject) {
        tsubject.setValue(defaultSubject);
        tsubject.setEnabled(!readOnly);
        tsubject.setMandatory(tsubject.isEnabled());
    }

    /**
     * add a ContactList as EmailTo:
     * 
     * @param emailList
     */
    protected void addEmailTo(final ContactList emailList) {
        if (contactLists.containsKey(emailList.getName())) {
            // there is already a ContactList with this name...
            final ContactList existing = contactLists.get(emailList.getName());
            // , merge their values.
            existing.add(emailList);
            // the form itself must not be updated, because it already displays
            // the name.
        } else {
            // a new ContactList, put it into contactLists
            contactLists.put(emailList.getName(), emailList);
            // and add its name in the form
            addContactFormEmailTo("<" + emailList.getName() + ">");
        }
    }

    /**
     * @param defaultEmailTo
     */
    private void addContactFormEmailTo(String defaultEmailTo) {

        defaultEmailTo += tto.getValue();
        tto.setValue(defaultEmailTo);
        ttoBig.setValue(defaultEmailTo);

        tto.setVisible(!recipientsAreEditable);
        ttoBig.setVisible(recipientsAreEditable);
    }

    /**
     * @param defaultBody
     */
    protected void setBody(final String defaultBody) {
        tbody.setValue(defaultBody);
        tbody.setEnabled(!readOnly);
        tbody.setVisible(true);
        tbody.setMandatory(!readOnly);
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {

        if (readOnly) {
            return true;
        }
        final boolean subjectOk = !tsubject.isEmpty("error.field.not.empty");
        final boolean bodyOk = !tbody.isEmpty("error.field.not.empty");
        // the body message may not be longer than about 4 pages or 10000
        // characters
        // bodyOk = bodyOk && tbody.notLongerThan(10000, "input.toolong");
        boolean toOk = false;
        if (tto != null) {
            toOk = !tto.isEmpty("error.field.not.empty");
        } else {
            toOk = !ttoBig.isEmpty("error.field.not.empty");
            // limit of recipients about 700 (1 emailaddress medial 40
            // characters)
            // toOk = toOk && ttoBig.notLongerThan(30000, "input.toolong");
        }
        final boolean fromOk = !tfrom.isEmpty("error.field.not.empty");
        return subjectOk && bodyOk && toOk && fromOk;
    }

    /**
     * @return
     */
    protected String getEmailFrom() {
        final String ccMail = getUserService().getUserProperty(emailFrom.getUser(), UserConstants.EMAIL);
        return ccMail;
    }

    /**
     * a List with ContactLists as elements is returned
     * 
     * @return
     */
    protected List<ContactList> getEmailToContactLists() {
        final List<ContactList> retVal = new ArrayList<ContactList>();
        retVal.addAll(contactLists.values());
        return retVal;
    }

    /**
     * retrieve the contact list names from the to field, and map them back to the stored contact lists names.
     * 
     * @return
     */
    protected String getEmailTo() {
        String retVal = "";
        String value;
        if (tto != null) {
            value = tto.getValue();
        } else {
            value = ttoBig.getValue();
        }

        String sep = "";
        int i = 0;
        int j = -1;
        i = value.indexOf("<", j + 1);
        j = value.indexOf(">", j + 2);
        while (i > -1 && j > 0) {
            final String contactListName = value.substring(i + 1, j);
            i = value.indexOf("<", j + 1);
            j = value.indexOf(">", j + 2);
            if (contactLists.containsKey(contactListName)) {
                final ContactList found = contactLists.get(contactListName);
                retVal += sep + found.toString();
                sep = ", ";
            }
        }
        return retVal;
    }

    /**
     * @return
     */
    protected String getSubject() {
        return tsubject.getValue();
    }

    /**
     * @return email body text
     */
    protected String getBody() {
        return tbody.getValue();
    }

    protected List<File> getAttachments() {
        final List<File> attachments = new ArrayList<File>();
        for (final FormLink removeLink : attachmentLinks) {
            attachments.add((File) removeLink.getUserObject());
        }
        return attachments;
    }

    protected void cleanUpAttachments() {
        if (attachementTempDir != null && attachementTempDir.exists()) {
            FileUtils.deleteDirsAndFiles(attachementTempDir, true, true);
            attachementTempDir = null;
        }
    }

    protected boolean isTcpFrom() {
        return tcpfrom.isSelected(0);
    }

    protected void setDisplayOnly(final boolean readOnly) {
        this.readOnly = readOnly;
        if (readOnly) {
            flc.setEnabled(false);
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == attachmentEl && attachmentEl.isUploadSuccess()) {
            final String filename = attachmentEl.getUploadFileName();
            if (attachementTempDir == null) {
                attachementTempDir = FileUtils.createTempDir("attachements", null, null);
            }

            final long size = attachmentEl.getUploadSize();
            if (size + attachmentSize > (contactAttachmentMaxSizeInMb * 1024 * 1024)) {
                showWarning(NLS_CONTACT_ATTACHMENT_EXPL, Integer.toString(contactAttachmentMaxSizeInMb));
                attachmentEl.reset();
            } else {
                final File attachment = attachmentEl.moveUploadFileTo(attachementTempDir);
                attachmentEl.logUpload();
                attachmentEl.reset();
                attachmentSize += size;
                final FormLink removeFile = uifactory.addFormLink(attachment.getName(), "delete", null, uploadCont, Link.BUTTON_SMALL);
                removeFile.setUserObject(attachment);
                attachmentLinks.add(removeFile);
                // pretty labels
                uploadCont.setLabel(NLS_CONTACT_ATTACHMENT, null);
                attachmentNames.put(attachment.getName(), filename);
                attachmentCss.put(attachment.getName(), CSSHelper.createFiletypeIconCssClassFor(filename));
                uploadCont.contextPut("attachments", attachmentLinks);
                uploadCont.contextPut("attachmentNames", attachmentNames);
                uploadCont.contextPut("attachmentCss", attachmentCss);
                attachmentEl.setLabel(null, null);
            }
        } else if (attachmentLinks.contains(source)) {
            final File uploadedFile = (File) source.getUserObject();
            if (uploadedFile != null && uploadedFile.exists()) {
                attachmentSize -= uploadedFile.length();
                uploadedFile.delete();
            }
            attachmentLinks.remove(source);
            uploadCont.remove(source);
            if (attachmentLinks.isEmpty()) {
                uploadCont.setLabel(null, null);
                attachmentEl.setLabel(NLS_CONTACT_ATTACHMENT, null);
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("header.newcntctmsg");

        tfrom = uifactory.addTextElement("ttfrom", NLS_CONTACT_FROM, 255, "", formLayout);
        tfrom.setValue(emailFrom.getAttributes().getEmail());
        tfrom.setEnabled(false);
        tfrom.setDisplaySize(emailCols);

        tto = uifactory.addTextElement("tto", NLS_CONTACT_TO, 255, "", formLayout);
        tto.setEnabled(false);
        tto.setVisible(false);
        tto.setDisplaySize(emailCols);

        ttoBig = uifactory.addTextAreaElement("ttoBig", NLS_CONTACT_TO, -1, 2, emailCols, true, "", formLayout);
        ttoBig.setEnabled(false);
        ttoBig.setVisible(false);

        tsubject = uifactory.addTextElement("tsubject", NLS_CONTACT_SUBJECT, 255, "", formLayout);
        tsubject.setDisplaySize(emailCols);
        tbody = uifactory.addTextAreaElement("tbody", NLS_CONTACT_BODY, -1, 10, emailCols, true, "", formLayout);
        tbody.setEnabled(!readOnly);

        final String VELOCITY_ROOT = PackageUtil.getPackageVelocityRoot(this.getClass());
        uploadCont = FormLayoutContainer.createCustomFormLayout("file_upload_inner", getTranslator(), VELOCITY_ROOT + "/attachments.html");
        uploadCont.setRootForm(mainForm);
        formLayout.add(uploadCont);

        attachmentEl = uifactory.addFileElement("file_upload_1", NLS_CONTACT_ATTACHMENT, formLayout);
        attachmentEl.setLabel(NLS_CONTACT_ATTACHMENT, null);
        attachmentEl.addActionListener(this, FormEvent.ONCHANGE);
        attachmentEl.setExampleKey(NLS_CONTACT_ATTACHMENT_EXPL, new String[] { Integer.toString(contactAttachmentMaxSizeInMb) });

        tcpfrom = uifactory.addCheckboxesVertical("tcpfrom", "", formLayout, new String[] { "xx" }, new String[] { translate(NLS_CONTACT_SEND_CP_FROM) }, null, 1);

        final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);

        uifactory.addFormSubmitButton("msg.save", buttonGroupLayout);
        if (hasMsgCancel) {
            uifactory.addFormCancelButton("msg.cancel", buttonGroupLayout, ureq, getWindowControl());
        }
    }

    @Override
    protected void doDispose() {
        cleanUpAttachments();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
