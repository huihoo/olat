package org.olat.presentation.contactform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.presentation.framework.core.PresentationFrameworkTestContext;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;

@RunWith(Theories.class)
public class ContactFormControllerTest {
	private Identity theFromIdentity;
	private ContactMessage contactMessage;
	private ContactList contactList;
	private UserRequest ureq;
	private ContactUIModel contactUIModel;
	private WindowControl wControl;
	private ContactFormView contactFormView;
	private Controller source;
	private Component initialComponent;
	private MessageSendStatus sendStatus;

	private ArgumentCaptor<String> wControlMessageCaptor;
	private List<ContactList> toContactLists;


	@Before
	public void setup(){
		wControlMessageCaptor = ArgumentCaptor.forClass(String.class);
		
		Locale locale = Locale.ENGLISH;				
		org.olat.lms.commons.i18n.ObjectMother.setupI18nManagerForFallbackLocale(locale);
		
		
		toContactLists = new ArrayList<ContactList>();
		contactList = new ContactList("simple");
		contactList.add(ObjectMother.createPeterBichselPrincipal());
		contactList.add(ObjectMother.createHeidiBirkenstockPrincipal());
		contactList.add(ObjectMother.createNicolas33Principal());
		toContactLists.add(contactList);
		
		theFromIdentity = ObjectMother.getIdentityFrom(ObjectMother.createHeidiBirkenstockPrincipal());
		contactMessage = new ContactMessage(theFromIdentity);
		contactMessage.addEmailTo(contactList);
		
		contactUIModel = new ContactUIModel(contactMessage);
		contactUIModel.copyToSenderSendTemplate = mock(ExceptionHandlingMailSendingTemplate.class);
		contactUIModel.toRecipientsSendTemplate = mock(ExceptionHandlingMailSendingTemplate.class);
		configureSendTemplatesForSuccess();
		
		PresentationFrameworkTestContext guiTestContext = org.olat.presentation.framework.core.ObjectMother.createPresentationFrameworkEnvironment(locale);
		ureq = guiTestContext.getUserRequest();
		wControl = guiTestContext.getWindowControl();
		
		contactFormView = mock(ContactFormView.class);
		when(contactFormView.getUreq()).thenReturn(ureq);
		when(contactFormView.getWindowControl()).thenReturn(wControl);
		

		source = mock(Controller.class);
		initialComponent = mock(Component.class);
		when(contactFormView.getInitialComponent(any(DefaultController.class))).thenReturn(initialComponent);
		when(contactFormView.is(source)).thenReturn(true);
		
	}
	
	@Test
	public void shouldSendMailToRecipientsOnlyWithoutException() {
		//setup
		configureViewForSuccessfullSentMessage();
		setCopyToSenderCheckbox(false);
		
		//exercise
		ContactFormController contactFormController = new ContactFormController(contactFormView, contactUIModel);	
		contactFormController.event(ureq, source, Event.DONE_EVENT);

		//verify
		verifySendingToRecipientsOnly();
		verifySuccessFullSendingInfoMessageSet();
	}
	
	@Test
	public void shouldSendMailToRecipientsAndCopyToSenderWithoutException() {
		//setup
		configureViewForSuccessfullSentMessage();
		setCopyToSenderCheckbox(true);
		
		//exercise
		ContactFormController contactFormController = new ContactFormController(contactFormView, contactUIModel);	
		contactFormController.event(ureq, source, Event.DONE_EVENT);

		//verify
		verifySendingToRecipientsAndCopyToSender();
		verifySuccessFullSendingInfoMessageSet();
	}
	

	@Theory
	public void shouldShowErrorMessageWhileSendingMailToRecipientsAndCopyToSenderWithFailure(SendStatusWithErrorMessage sendStatusWrapper) {
		//setup
		sendStatus = sendStatusWrapper.sendStatusWithErrorMessage;
		configureViewForSuccessfullSentMessage();
		when(contactUIModel.copyToSenderSendTemplate.send()).thenReturn(sendStatus);
		when(contactUIModel.toRecipientsSendTemplate.send()).thenReturn(sendStatus);	
		setCopyToSenderCheckbox(true);
		
		//exercise
		ContactFormController contactFormController = new ContactFormController(contactFormView, contactUIModel);	
		contactFormController.event(ureq, source, Event.DONE_EVENT);

		//verify
		verifySendingToRecipientsAndCopyToSender();
		verifyFailedSendingErrorMessageSet();
	}
	

	@Theory
	public void shouldShowInfoMessageWhileSendingMailToRecipientsAndCopyToSenderWithFailure(SendStatusWithInfoMessage sendStatusWrapper) {
		//setup
		sendStatus = sendStatusWrapper.sendStatusWithInfoMessage;
		configureViewForSuccessfullSentMessage();
		when(contactUIModel.copyToSenderSendTemplate.send()).thenReturn(sendStatus);
		when(contactUIModel.toRecipientsSendTemplate.send()).thenReturn(sendStatus);
		setCopyToSenderCheckbox(true);
		
		//exercise
		ContactFormController contactFormController = new ContactFormController(contactFormView, contactUIModel);	
		contactFormController.event(ureq, source, Event.DONE_EVENT);

		//verify
		verifySendingToRecipientsAndCopyToSender();
		verifyFailedSendingInfoMessageSet();
	}
	
	/*
	 * the following construct is needed to have different sets of DataPoints to run with several @Theory annotated tests.
	 * In this case some MessageSendStatus are handled with an error message, others with an info message. To verify this 
	 * different messages while avoiding code duplication and/or separate testclasses, the wrapping construct is choosen.
	 * see also http://stackoverflow.com/questions/7554458/how-to-attach-a-datapoint-with-a-theory 
	 */
	public static class SendStatusWithErrorMessage {
		public MessageSendStatus sendStatusWithErrorMessage;
		public SendStatusWithErrorMessage(MessageSendStatus sendStatusWithErrorMessage){
			this.sendStatusWithErrorMessage = sendStatusWithErrorMessage;
		}
	}

	@DataPoint public static SendStatusWithErrorMessage messageFailed = 
			new SendStatusWithErrorMessage(ExceptionHandlingMailSendingTemplate.handleMessagingException());
	@DataPoint public static SendStatusWithErrorMessage addressFailedOne = 
			new SendStatusWithErrorMessage(ExceptionHandlingMailSendingTemplate.createSendProblemsCausedByAddresses(false));
	@DataPoint public static SendStatusWithErrorMessage addressFailedTwo = 
			new SendStatusWithErrorMessage(ExceptionHandlingMailSendingTemplate.createPartialyNotSentMessageSendStatus(false));

	
	public static class SendStatusWithInfoMessage {
		public MessageSendStatus sendStatusWithInfoMessage;
		public SendStatusWithInfoMessage(MessageSendStatus sendStatusWithInfoMessage){
			this.sendStatusWithInfoMessage = sendStatusWithInfoMessage;
		}
	}
	@DataPoint public static SendStatusWithInfoMessage couldNotConnect = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createCouldNotConnectToSmtpHostMessageSendStatus());
	@DataPoint public static SendStatusWithInfoMessage unknownSmtp = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createUnknownSMTPHost());
	@DataPoint public static SendStatusWithInfoMessage noRecipients = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createNoRecipientMessageSendStatus());
	private static Address[] invalidAddresses = null;
	{
		try {
			invalidAddresses = InternetAddress.parse("invalid1, invalid2, invalid3", false);
		} catch (AddressException e) {
			throw new RuntimeException(e);
		}
	}
	@DataPoint public static SendStatusWithInfoMessage invalidAddr = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createInvalidAddressesMessageSendStatus(invalidAddresses));
	@DataPoint public static SendStatusWithInfoMessage invalidDomain = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createInvalidDomainMessageSendStatus());
	@DataPoint public static SendStatusWithInfoMessage smtpAuthFailed = 
			new SendStatusWithInfoMessage(ExceptionHandlingMailSendingTemplate.createAuthenticationFailedMessageSendStatus());	
	

	private void configureSendTemplatesForSuccess() {
		sendStatus = ExceptionHandlingMailSendingTemplate.createSuccessfullSentEmailMessageStatus();
		when(contactUIModel.copyToSenderSendTemplate.send()).thenReturn(sendStatus);
		when(contactUIModel.toRecipientsSendTemplate.send()).thenReturn(sendStatus);
	}
	
	private void verifySuccessFullSendingInfoMessageSet() {
		verify(wControl,times(1)).setInfo(wControlMessageCaptor.capture());
		String infoMessage = wControlMessageCaptor.getValue();
		assertEquals("Your message was sent successfully.", infoMessage);
	}
	
	private void verifyFailedSendingErrorMessageSet(){
		verify(wControl,times(1)).setError(wControlMessageCaptor.capture());
		String errorMessage = wControlMessageCaptor.getValue();
		
		boolean isExpectedErrorMessage = errorMessage.startsWith("Your message could only be sent partially");
		isExpectedErrorMessage = isExpectedErrorMessage || errorMessage.startsWith("Your message could not be sent");
		
		assertTrue(isExpectedErrorMessage);
	}
	
	private void verifyFailedSendingInfoMessageSet(){
		verify(wControl,times(1)).setInfo(wControlMessageCaptor.capture());
		String infoMessage = wControlMessageCaptor.getValue();
		
		boolean isExpectedInfoMessage = infoMessage.startsWith("Sender's and/or recipient's address incorrect.");
		isExpectedInfoMessage = isExpectedInfoMessage || infoMessage.startsWith("Your message could not be sent");
		
		assertTrue(isExpectedInfoMessage);
	}

	private void verifySendingToRecipientsOnly() {
		verify(contactUIModel.copyToSenderSendTemplate,never()).send();
		verify(contactUIModel.toRecipientsSendTemplate,times(1)).send();
	}
	
	private void verifySendingToRecipientsAndCopyToSender(){
		verify(contactUIModel.copyToSenderSendTemplate,times(1)).send();
		verify(contactUIModel.toRecipientsSendTemplate,times(1)).send();
	}
	
	private void setCopyToSenderCheckbox(boolean enabled) {
		when(contactFormView.isTcpFrom()).thenReturn(enabled);
	}
	
	private void configureViewForSuccessfullSentMessage() {
		when(contactFormView.getSubject()).thenReturn("Subject from form");
		when(contactFormView.getBody()).thenReturn("My complete new body text as mail.");
		when(contactFormView.getAttachments()).thenReturn(null);
		when(contactFormView.getEmailToContactLists()).thenReturn(toContactLists);
	}
	
}
