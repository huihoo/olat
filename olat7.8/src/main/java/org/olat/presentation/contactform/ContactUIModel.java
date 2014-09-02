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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.system.mail.ContactList;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author patrick
 * 
 */
public class ContactUIModel {

    private ContactMessage contactMessage;
    private List<File> attachements;

    public ContactUIModel(ContactMessage contactMessage) {
        this.contactMessage = contactMessage;
    }

    public boolean hasAtLeastOneAddress() {
        return contactMessage.hasAtLeastOneAddress();
    }

    public List<OLATPrincipal> getDisabledIdentities() {
        return contactMessage.getDisabledIdentities();
    }

    public String getBodyText() {
        return contactMessage.getBodyText();
    }

    public String getSubject() {
        return contactMessage.getSubject();
    }

    public List<ContactList> getEmailToContactLists() {
        return contactMessage.getEmailToContactLists();
    }

    public Identity getFrom() {
        return contactMessage.getFrom();
    }

    public void setContactMessage(ContactMessage newContactMessage) {
        this.contactMessage = newContactMessage;
    }

    public void setAttachements(List<File> attachments) {
        this.attachements = attachments;
    }

    public List<File> getAttachements() {
        return attachements;
    }

    public boolean sendCurrentMessageToRecipients(boolean withCCToSender) {
        return sendMessage(withCCToSender);
    }

    private boolean sendMessage(boolean withCCToSender) {
        List<ContactList> contactList = this.getEmailToContactLists();

        List<String> recipients = new ArrayList<String>();
        for (ContactList contact : contactList) {
            recipients.addAll(contact.getEmailsAsStrings());
        }

        if (recipients.size() > 0) {
            MailMessage mailMessage = new MailMessage(recipients, getFrom(), withCCToSender, getSubject(), getBodyText(), getAttachements());
            return getMailMessageLearnService().sendMessage(mailMessage);
        }

        return false;
    }

    private MailMessageLearnService getMailMessageLearnService() {
        return CoreSpringFactory.getBean(MailMessageLearnService.class);
    }

}
