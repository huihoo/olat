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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * In order to get the mailing functions under test, the static references to webapphelper and mail helper have to be broken.
 * 
 * <P>
 * Initial Date:  Sep 27, 2011 <br>
 * @author patrick
 */
public interface MailPackageStaticDependenciesWrapper {

	/**
	 * @return
	 */
	String getSystemEmailAddress();

	/**
	 * @return
	 */
	boolean isEmailFunctionalityDisabled();

	/**
	 * @return
	 */
	MimeMessage createMessage(Address from, Address[] recipients, Address[] recipientsCC, Address[] recipientsBCC, String body, String subject,
			File[] attachments, MailerResult result);

	/**
	 * @param msg
	 * @param result
	 */
	void send(MimeMessage msg, MailerResult result);

	/**
	 * @param mailFromAddress
	 * @param listOfContactLists
	 * @param string
	 * @param subject
	 * @param attachmentsArray
	 * @param result
	 * @return
	 */
	MimeMessage createMessage(InternetAddress mailFromAddress, List<? extends ContactList> listOfContactLists, String string,	String subject, File[] attachmentsArray, MailerResult result)  throws AddressException, MessagingException ;

	/**
	 * @return
	 */
	String getMailhost();


}
