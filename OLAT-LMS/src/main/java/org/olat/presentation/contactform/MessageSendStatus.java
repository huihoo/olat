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

import javax.mail.Address;

import org.olat.presentation.framework.core.translator.Translator;

/**
 * Most of the send stati are warn, hence following defaults are used during creation of a MessageSendStatus
 * <ul>
 * <li>severity is set to warn</li>
 * <li>one is forced to implement a <code>createInfoMessageWith</code></li>
 * </ul>
 * In order to generate an error message one has to..
 * <ul>
 * <li>delegate <code>createInfoMessageWith()</code> implementation to the helper <code>defaultCreateInfoMessage()</code></li>
 * <li>override <code>createErrorMessageWith()</code></li>
 * <li>call <code>setSeverityError()</code></li>
 * </ul>
 * @author patrick
 *
 */
public abstract class MessageSendStatus {
	private MessageSendStatusCode statusCode;
	private Address[] invalidAddresses;
	private boolean isSeverityWarn = true;
	private boolean isSeverityError = false;
	
	MessageSendStatus(MessageSendStatusCode statusCode, Address[] invalidAddresses){
		this.statusCode = statusCode;
		this.invalidAddresses = invalidAddresses;
	}
	
	MessageSendStatus(MessageSendStatusCode statusCode){
		this(statusCode, new Address[0]);
	}

	MessageSendStatus(){
		this(MessageSendStatusCode.SUCCESSFULL_SENT_EMAILS);
		setNoSeverity();
	}
	
	public MessageSendStatusCode getStatusCode(){
		return this.statusCode;
	}
	
	public Address[] getInvalidAddresses(){
		return invalidAddresses;
	}
	

	/**
	 * converts an Address[] to an HTML ordered list
	 * 
	 * @param invalidAdr Address[] with invalid addresses
	 * @return StringBuilder
	 */
	protected StringBuilder addressesArr2HtmlOList(final Address[] invalidAdr) {
		final StringBuilder iAddressesSB = new StringBuilder();
		if (invalidAdr != null && invalidAdr.length > 0) {
			iAddressesSB.append("<ol>");
			for (int i = 0; i < invalidAdr.length; i++) {
				iAddressesSB.append("<li>");
				iAddressesSB.append(invalidAdr[i].toString());
				iAddressesSB.append("</li>");
			}
			iAddressesSB.append("</ol>");
		}
		return iAddressesSB;
	}
	
	public boolean isSeverityWarn(){
		return isSeverityWarn;
	}
	public boolean isSeverityError(){
		return isSeverityError;
	}
	protected void setSeverityWarn(){
		isSeverityWarn = true;
		isSeverityError = false;
	}
	protected void setSeverityError(){
		isSeverityWarn = false;
		isSeverityError = true;
	}
	protected void setNoSeverity(){
		isSeverityWarn = false;
		isSeverityError = false;
	}
	
	
	public String createErrorMessageWith(Translator translator){
		if(isSeverityWarn() && !isSeverityError){
			throw new IllegalStateException("Status has not severity ERROR, hence the ERROR message can not be accessed!");
		}
		return "must be overridden by the developer to generate something meaningful";
	}
	
	/**
	 * If you implement an error message, delegate to this helper from the MUST implementation of createInfoMessageWith(..)
	 **/
	protected String defaultCreateInfoMessage(){
		if(isSeverityError() && !isSeverityWarn){
			throw new IllegalStateException("Status has not severity WARB, hence the WARN message can not be accessed!");
		}
		return "this return value should not appear";
	}
	
	abstract public String createInfoMessageWith(Translator translator);
	abstract public boolean canProceedWithWorkflow();
	
	
	public enum MessageSendStatusCode {
		SMTP_AUTHENTICATION_FAILED,
		SEND_FAILED_DUE_INVALID_DOMAIN_NAME_553,
		SEND_FAILED_DUE_INVALID_ADDRESSES_550,
		SEND_FAILED_DUE_NO_RECIPIENTS_503,
		SEND_FAILED_DUE_UNKNOWN_SMTP_HOST,
		SEND_FAILED_DUE_COULD_NOT_CONNECT_TO_SMTP_HOST,
		MAIL_PARTIALLY_NOT_SENT,
		MAIL_CONTENT_NOK,
		SENDER_OR_RECIPIENTS_NOK_553,
		SUCCESSFULL_SENT_EMAILS;
		
		public boolean isSendFailedCase() {
			boolean isSendFailed = this == SEND_FAILED_DUE_COULD_NOT_CONNECT_TO_SMTP_HOST;
			isSendFailed = isSendFailed || this == SEND_FAILED_DUE_INVALID_ADDRESSES_550;
			isSendFailed = isSendFailed || this == SEND_FAILED_DUE_INVALID_DOMAIN_NAME_553;
			isSendFailed = isSendFailed || this == SEND_FAILED_DUE_NO_RECIPIENTS_503;
			isSendFailed = isSendFailed || this == SEND_FAILED_DUE_UNKNOWN_SMTP_HOST;
			isSendFailed = isSendFailed || this == SMTP_AUTHENTICATION_FAILED;
			return isSendFailed;
		}
		
		public boolean isMessageFailedCase() {
			return this == MAIL_CONTENT_NOK;		}
		
		public boolean isSuccessfullSentMails() {
			return this == SUCCESSFULL_SENT_EMAILS;
		}
		
		public boolean isAddressFailedCase() {
			boolean isAddressFailure = this == MAIL_PARTIALLY_NOT_SENT;
			isAddressFailure = isAddressFailure || this == SENDER_OR_RECIPIENTS_NOK_553;
			return isAddressFailure;
		}
	}
}