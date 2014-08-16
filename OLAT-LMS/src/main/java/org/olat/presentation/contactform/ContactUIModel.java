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
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.Emailer;
import org.olat.system.security.OLATPrincipal;

/**
 * @author patrick
 *
 */
public class ContactUIModel {

	private ContactMessage contactMessage;
	private List<File> attachements;
	
	//package visibility for testing
	ExceptionHandlingMailSendingTemplate toRecipientsSendTemplate = new ExceptionHandlingMailSendingTemplate() {
		@Override
		protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
			return emailer.sendEmail(getTheToContactLists(), getSubject(), getBodyText(), getAttachements());
		}

		@Override
		protected Identity getFromIdentity() {
			return getFrom();
		}

		@Override
		protected List<ContactList> getTheToContactLists() {
			return getEmailToContactLists();
		}
	};
	//package visibility for testing
	ExceptionHandlingMailSendingTemplate copyToSenderSendTemplate = new ExceptionHandlingMailSendingTemplate() {
		@Override
		protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
			String emailFromAsString = getFromIdentity().getAttributes().getEmail();//this mail is differently extracted in ContactForm
			return emailer.sendEmailCC(emailFromAsString, getSubject(), getBodyText(), getAttachements());
		}
		@Override
		protected Identity getFromIdentity() {
			return getFrom();
		}

		@Override
		protected List<ContactList> getTheToContactLists() {
			return getEmailToContactLists();
		}
	};
	
	

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

	public void setContactMessage(ContactMessage newContactMessage){
		this.contactMessage = newContactMessage;
	}

	public void setAttachements(List<File> attachments) {
		this.attachements = attachments;
	}
	
	public List<File> getAttachements(){
		return attachements;
	}
	
	public MessageSendStatus sendCurrentMessageToRecipients() {
		return toRecipientsSendTemplate.send();
	}
	
	public MessageSendStatus sendCurrentMessageAsCopyToSender() {
		return copyToSenderSendTemplate.send();
	}
	
}
