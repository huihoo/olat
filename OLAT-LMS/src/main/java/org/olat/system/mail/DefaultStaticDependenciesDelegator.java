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
package org.olat.system.mail;

import java.io.File;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.olat.system.commons.WebappHelper;
import org.olat.system.security.OLATPrincipal;

/**
 * 
 * 
 * <P>
 * Initial Date: Sep 27, 2011 <br>
 * 
 * @author patrick
 */
class DefaultStaticDependenciesDelegator implements MailPackageStaticDependenciesWrapper {
	
	class StaticDelegatorToMailHelper {
		public Object getMailhost(){
			return MailHelper.getMailhost();
		}

		public MailerResult removeDisabledMailAddress(List<? extends OLATPrincipal> principals, MailerResult result) {
			return MailHelper.removeDisabledMailAddress(principals, result);
		}
	}
	
	//default visibility to allow a test access the delegates.
	StaticDelegatorToMailHelper mailHelperDelegate = new StaticDelegatorToMailHelper();
	
	
	@Override
	public String getSystemEmailAddress() {
		return WebappHelper.getMailConfig("mailFrom");
	}

	@Override
	public boolean isEmailFunctionalityDisabled() {
		boolean retVal = true;
		Object mailHost = mailHelperDelegate.getMailhost();
		
		if(mailHost != null && mailHost instanceof String){
			String mailHostAsString = (String)mailHelperDelegate.getMailhost(); 			
			retVal = mailHostAsString.equals("") || mailHostAsString.equalsIgnoreCase("disabled");
		}
		
		return retVal;
	}

	@Override
	public MimeMessage createMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body, String subject,
			File[] attachments, MailerResult result) {
		return MailHelper.createMessage(from, recipients, recipientsCC, recipientsBCC, body, subject, attachments, result);
	}

	@Override
	public void send(MimeMessage msg, MailerResult result) {
		MailHelper.sendMessage(msg, result);
	}

	@Override
	public MimeMessage createMessage(InternetAddress from, List<? extends ContactList> listOfContactLists, String body, String subject, File[] attachments, MailerResult result)  throws AddressException, MessagingException {
		MimeMessage tmpMessage = MailHelper.createMessage();
		for (ContactList tmp : listOfContactLists) {
			InternetAddress groupName[] = InternetAddress.parse(tmp.getRFC2822Name() + ";");
			InternetAddress members[] = tmp.getEmailsAsAddresses();
			tmpMessage.addRecipients(RecipientType.TO, groupName);
			tmpMessage.addRecipients(RecipientType.BCC, members);
		}
		Address recipients[] = tmpMessage.getRecipients(RecipientType.TO);
		Address recipientsBCC[] = tmpMessage.getRecipients(RecipientType.BCC);
		
		return createMessage(from, recipients, null, recipientsBCC, body, subject, attachments, result);
	}

	@Override
	public String getMailhost() {
		Object possibleMailhost = mailHelperDelegate.getMailhost();
		String mailHost = null;
		if(possibleMailhost instanceof String){
			mailHost = (String)possibleMailhost;
		}
		return mailHost;
	}

}
