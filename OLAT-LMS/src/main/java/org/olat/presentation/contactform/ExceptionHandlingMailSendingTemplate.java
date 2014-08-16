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

import java.util.List;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.presentation.contactform.MessageSendStatus.MessageSendStatusCode;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.WebappHelper;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.Emailer;
import org.olat.system.mail.MailTemplate;


/**
 * send mail template to allow sending to recipients and CC to sender itself, without duplication of try catch block (DRY, no duplicates).
 * @author patrick
 *
 */
abstract class ExceptionHandlingMailSendingTemplate {

	public MessageSendStatus send(){

		final boolean useInstitutionalEmail = false; 
		final Emailer emailer = new Emailer(getFromIdentity(), useInstitutionalEmail, getMailTemplate());
		
		MessageSendStatus messageSendStatus = createSuccessfullSentEmailMessageStatus();
		boolean sendEmailSuccess = false;
		try {
			sendEmailSuccess = doSend(emailer);
		} catch (final AddressException e) {
			// error in recipient email address(es)
			// TODO:discuss: sendEmailSuccess should always be false if the addressexcpetion happens 
			messageSendStatus = handleAddressException(sendEmailSuccess);
		} catch (final SendFailedException e) {
			// error in sending message
			// CAUSE: sender email address invalid
			messageSendStatus = handleSendFailedException(e);
		} catch (final MessagingException e) {
			// error in message-subject || .-body
			messageSendStatus = handleMessagingException();
		}
		return messageSendStatus;
	}

	abstract protected Identity getFromIdentity();
	
	abstract protected List<ContactList> getTheToContactLists();
	
	abstract protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException;

	
	protected MailTemplate getMailTemplate() {
		return MailTemplateHelper.getMailTemplateWithFooterWithUserData(getFromIdentity());
	}
	
	/*
	 * The methods for MessageSendStatus creation have:
	 * default visibility and static modifier for access in Unittesting
	 */
	static MessageSendStatus handleMessagingException() {
		return new MessageSendStatus(MessageSendStatusCode.MAIL_CONTENT_NOK) {

			{
				setSeverityError();
			}
			
			@Override
			public String createErrorMessageWith(Translator translator) {
				super.createErrorMessageWith(translator);
				
				String errorMessage = translator.translate("error.msg.send.nok");
				errorMessage += "<br />";
				errorMessage += translator.translate("error.msg.content.nok");
				return errorMessage;
			}
			
			@Override
			public boolean canProceedWithWorkflow() {
				return false;
			}
			
			@Override
			public String createInfoMessageWith(Translator translator) {
				return defaultCreateInfoMessage();
			}
		};
		
		
		
	}
	
	static MessageSendStatus createSuccessfullSentEmailMessageStatus() {
		return new MessageSendStatus() {		
			
			@Override
			public String createInfoMessageWith(Translator translator) {
				return defaultCreateInfoMessage();
			}
			
			@Override
			public boolean canProceedWithWorkflow() {
				return true;
			}
		};
	}
	
	


	/**
	 * @param success
	 */
	private MessageSendStatus handleAddressException(final boolean success) {	
		if (success) {
			return createPartialyNotSentMessageSendStatus(success);
		} else {
			return createSendProblemsCausedByAddresses(success);
		}
		///this.getWindowControl().setError(errorMessage.toString());
	}

	static MessageSendStatus createSendProblemsCausedByAddresses(final boolean success) {
		return new MessageSendStatus(MessageSendStatusCode.SENDER_OR_RECIPIENTS_NOK_553) {
			
			{
				setSeverityError();
			}
			
			@Override
			public String createErrorMessageWith(Translator translator) {
				super.createErrorMessageWith(translator);
				final StringBuilder errorMessage = new StringBuilder();
				errorMessage.append(translator.translate("error.msg.send.nok"));
				errorMessage.append("<br />");
				errorMessage.append(translator.translate("error.msg.send.553"));
				return errorMessage.toString();
			}
			
			@Override
			public boolean canProceedWithWorkflow() {
				return success;
			}

			@Override
			public String createInfoMessageWith(Translator translator) {
				return defaultCreateInfoMessage();
			}
		};

	}	
		


	static MessageSendStatus createPartialyNotSentMessageSendStatus(final boolean success) {
		return new MessageSendStatus(MessageSendStatusCode.MAIL_PARTIALLY_NOT_SENT) {

			{
				setSeverityError();
			}
			
			@Override
			public String createErrorMessageWith(Translator translator) {
				super.createErrorMessageWith(translator);
				
				final StringBuilder errorMessage = new StringBuilder();
				errorMessage.append(translator.translate("error.msg.send.partially.nok"));
				errorMessage.append("<br />");
				errorMessage.append(translator.translate("error.msg.send.invalid.rcps"));
				return errorMessage.toString();
			}
			
			@Override
			public boolean canProceedWithWorkflow() {
				return success;
			}

			@Override
			public String createInfoMessageWith(Translator translator) {
				return defaultCreateInfoMessage();
			}
		};
	}

	/**
	 * handles the sendFailedException
	 * <p>
	 * creates a MessageSendStatus which contains a translateable info or error message, and the knowledge if the user can proceed with its action. 
	 * 
	 * @param e
	 * @throws OLATRuntimeException return MessageSendStatus
	 */
	private MessageSendStatus handleSendFailedException(final SendFailedException e) {
		// get wrapped excpetion
		MessageSendStatus messageSendStatus = null;
		
		final MessagingException me = (MessagingException) e.getNextException();
		if (me instanceof AuthenticationFailedException) {
			messageSendStatus = createAuthenticationFailedMessageSendStatus();
			return messageSendStatus;
		}
		
		final String message = me.getMessage();
		if (message.startsWith("553")) {
			messageSendStatus = createInvalidDomainMessageSendStatus();
		} else if (message.startsWith("Invalid Addresses")) {
			messageSendStatus = createInvalidAddressesMessageSendStatus(e.getInvalidAddresses());
		} else if (message.startsWith("503 5.0.0")) {
			messageSendStatus = createNoRecipientMessageSendStatus();
		} else if (message.startsWith("Unknown SMTP host")) {
			messageSendStatus = createUnknownSMTPHost();
		} else if (message.startsWith("Could not connect to SMTP host")) {
			messageSendStatus = createCouldNotConnectToSmtpHostMessageSendStatus();
		} else {
			List<ContactList> emailToContactLists = getTheToContactLists();
			String exceptionMessage = "";
			for (ContactList contactList : emailToContactLists) {
				exceptionMessage += contactList.toString();
			}
			throw new OLATRuntimeException(ContactUIModel.class, exceptionMessage, me);
		}
		return messageSendStatus;
	}


	static MessageSendStatus createCouldNotConnectToSmtpHostMessageSendStatus() {
		return new MessageSendStatus(MessageSendStatusCode.SEND_FAILED_DUE_COULD_NOT_CONNECT_TO_SMTP_HOST) {
			
			@Override
			public String createInfoMessageWith(Translator translator) {
				// could not connect to smtp host, no connection or connection timeout
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.nok"));
				infoMessage.append("<br />");
				infoMessage.append(translator.translate("error.msg.notconnectto.smtp", new String[]{WebappHelper.getMailConfig("mailhost")}));
				//this.getWindowControl().setInfo(infoMessage.toString());
				//log.warn(null, e);
				// message could not be sent, however let user proceed with his action
				return infoMessage.toString();
			}
			
			@Override
			public boolean canProceedWithWorkflow() {
				return true;
			}
		};		
	}

	static MessageSendStatus createUnknownSMTPHost() {
		return new MessageSendStatus(MessageSendStatusCode.SEND_FAILED_DUE_UNKNOWN_SMTP_HOST){

			@Override
			public String createInfoMessageWith(Translator translator) {
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.nok"));
				infoMessage.append("<br />");
				infoMessage.append(translator.translate("error.msg.unknown.smtp", new String[]{WebappHelper.getMailConfig("mailFrom")}));
				///this.getWindowControl().setInfo(infoMessage.toString());
				///log.warn("Mail message could not be sent: ", e);
				return infoMessage.toString();
			}

			@Override
			public boolean canProceedWithWorkflow() {
				return true;
			}
		};
	}

	static MessageSendStatus createNoRecipientMessageSendStatus() {
		return new MessageSendStatus(MessageSendStatusCode.SEND_FAILED_DUE_NO_RECIPIENTS_503){

			@Override
			public String createInfoMessageWith(Translator translator) {
				// message:503 5.0.0 Need RCPT (recipient) ,javax.mail.MessagingException
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.nok"));
				infoMessage.append("<br />");
				infoMessage.append(translator.translate("error.msg.send.no.rcps"));
				//this.getWindowControl().setInfo(infoMessage.toString());
				return infoMessage.toString();
			}

			@Override
			public boolean canProceedWithWorkflow() {
				return false;
			}
		};
	}

	static MessageSendStatus createInvalidAddressesMessageSendStatus(Address[] invalidAddresses) {
		return new MessageSendStatus(MessageSendStatusCode.SEND_FAILED_DUE_INVALID_ADDRESSES_550, invalidAddresses){

			@Override
			public String createInfoMessageWith(Translator translator) {

				// javax.mail.SendFailedException: Sending failed;
				// nested exception is:
				// class javax.mail.SendFailedException: Invalid Addresses;
				// nested exception is:
				// class javax.mail.SendFailedException: 550 5.1.1 <dfgh>... User
				// unknownhandleSendFailedException
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.nok"));
				infoMessage.append("<br />");
				infoMessage.append(translator.translate("error.msg.send.invalid.rcps"));
				infoMessage.append(addressesArr2HtmlOList(getInvalidAddresses()));
				//this.getWindowControl().setInfo(infoMessage.toString());
				return infoMessage.toString();
			}

			@Override
			public boolean canProceedWithWorkflow() {
				return false;
			}
		};
	}

	static MessageSendStatus createInvalidDomainMessageSendStatus() {
		return new MessageSendStatus(MessageSendStatusCode.SEND_FAILED_DUE_INVALID_DOMAIN_NAME_553){

			@Override
			public String createInfoMessageWith(Translator translator) {
				// javax.mail.MessagingException: 553 5.5.4 <invalid>... Domain name
				// required for sender address invalid@id.uzh.ch
				// javax.mail.MessagingException: 553 5.1.8 <invalid@invalid.>...
				// Domain of sender address invalid@invalid does not exist
				// ...
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.553"));
				///showInfo(infoMessage.toString());
				return infoMessage.toString();
			}

			@Override
			public boolean canProceedWithWorkflow() {
				return false;
			}
			
		};
		
	}

	static MessageSendStatus createAuthenticationFailedMessageSendStatus() {
		return new MessageSendStatus(MessageSendStatusCode.SMTP_AUTHENTICATION_FAILED) {
			@Override
			public String createInfoMessageWith(Translator translator) {
				// catch this one separately, this kind of exception has no message
				// as the other below
				final StringBuilder infoMessage = new StringBuilder();
				infoMessage.append(translator.translate("error.msg.send.nok"));
				infoMessage.append("<br />");
				infoMessage.append(translator.translate("error.msg.smtp.authentication.failed"));
				/// this.getWindowControl().setInfo(infoMessage.toString());
				/// log.warn("Mail message could not be sent: ", e);
				// message could not be sent, however let user proceed with his action
				return infoMessage.toString();
			}

			@Override
			public boolean canProceedWithWorkflow() {
				return true;
			}
		};
	}
}

