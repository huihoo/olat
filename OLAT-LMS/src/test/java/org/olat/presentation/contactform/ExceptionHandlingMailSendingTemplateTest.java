package org.olat.presentation.contactform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.NoSuchProviderException;
import javax.mail.ReadOnlyFolderException;
import javax.mail.SendFailedException;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.ParseException;
import javax.mail.search.SearchException;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.olat.data.basesecurity.Identity;
import org.olat.presentation.contactform.MessageSendStatus.MessageSendStatusCode;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.Emailer;
import org.olat.system.mail.MailTemplate;
import org.olat.system.mail.ObjectMother;
import org.olat.system.security.OLATPrincipal;

@RunWith(Theories.class)
public class ExceptionHandlingMailSendingTemplateTest {
	
	private static final String BY_OLAT_UNHANDLED_TEXT = "outer exception text unimportant in OLAT";
	private List<ContactList> theContactLists;
	private Identity theFromIdentity;
	private MailTemplate emptyMailTemplate = new MailTemplate("Empty Subject", "Empty Body","Empty Footer", null) {
		@Override
		public void putVariablesInMailContext(VelocityContext context,	OLATPrincipal recipient) {
			// 
		}
	};
	private Object testI18nManagerInitializer;
	private Translator translator;
	
	@Before
	public void setUp(){
		theContactLists = new ArrayList<ContactList>();
		theFromIdentity = ObjectMother.getIdentityFrom(ObjectMother.createHeidiBirkenstockPrincipal());
		testI18nManagerInitializer = org.olat.lms.commons.i18n.ObjectMother.setupI18nManagerForFallbackLocale(Locale.ENGLISH);
		translator = PackageUtil.createPackageTranslator(this.getClass(), Locale.ENGLISH);
	}
	
	@Test
	public void testSendWithoutException() {
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithoutException = new ExceptionHandlingMailSendingTemplate() {
			

			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				return true;
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithoutException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SUCCESSFULL_SENT_EMAILS, sendStatus.getStatusCode());
		assertTrue(sendStatus.getStatusCode().isSuccessfullSentMails());
		assertFalse(sendStatus.isSeverityError());
		assertFalse(sendStatus.isSeverityWarn());
		assertTrue(sendStatus.canProceedWithWorkflow());
	}
	
	@Test
	public void shouldFailWithAddressExcpetion(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithAddressException = new ExceptionHandlingMailSendingTemplate() {
						@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				throw new AddressException();
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithAddressException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SENDER_OR_RECIPIENTS_NOK_553, sendStatus.getStatusCode());
		verifyStatusCodeIndicateAddressExceptionOnly(sendStatus);
		verifySendStatusIsWarn(sendStatus);
		assertFalse(sendStatus.canProceedWithWorkflow());	
	}

	private void verifySendStatusIsError(MessageSendStatus sendStatus) {
		assertFalse(sendStatus.isSeverityError());
		assertTrue(sendStatus.isSeverityWarn());
		sendStatus.createInfoMessageWith(translator);
	}
	
	private void verifySendStatusIsWarn(MessageSendStatus sendStatus) {
		assertTrue(sendStatus.isSeverityError());
		assertFalse(sendStatus.isSeverityWarn());
		sendStatus.createErrorMessageWith(translator);
	}
	
	private void verifyStatusCodeIndicateAddressExceptionOnly(MessageSendStatus sendStatus) {
		assertTrue(sendStatus.getStatusCode().isAddressFailedCase());
		assertFalse(sendStatus.getStatusCode().isSendFailedCase());
		assertFalse(sendStatus.getStatusCode().isMessageFailedCase());
	}
	
	@Test
	public void shouldFailWithSendFailedExceptionWithDomainErrorCode553(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("553 some domain error message from the mailsystem");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SEND_FAILED_DUE_INVALID_DOMAIN_NAME_553, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertFalse(sendStatus.canProceedWithWorkflow());
	}

	private void verifyStatusCodeIndicateSendFailedOnly(MessageSendStatus sendStatus) {
		assertFalse(sendStatus.getStatusCode().isAddressFailedCase());
		assertTrue(sendStatus.getStatusCode().isSendFailedCase());
		assertFalse(sendStatus.getStatusCode().isMessageFailedCase());
	}
	

	@Test
	public void shouldFailWithSendFailedExceptionWithInvalidAddresses(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("Invalid Addresses <followed by a list of invalid addresses>");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SEND_FAILED_DUE_INVALID_ADDRESSES_550, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertFalse(sendStatus.canProceedWithWorkflow());
	}
	

	@Test
	public void shouldFailWithSendFailedExceptionBecauseNoRecipient(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("503 5.0.0 .... ");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SEND_FAILED_DUE_NO_RECIPIENTS_503, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertFalse(sendStatus.canProceedWithWorkflow());
	}
	

	@Test
	public void shouldFailWithSendFailedExceptionBecauseUnknownSMTPHost(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("Unknown SMTP host");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SEND_FAILED_DUE_UNKNOWN_SMTP_HOST, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertTrue(sendStatus.canProceedWithWorkflow());
	}
	

	@Test
	public void shouldFailWithSendFailedExceptionBecauseCouldNotConnectToSMTPHost(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("Could not connect to SMTP host");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SEND_FAILED_DUE_COULD_NOT_CONNECT_TO_SMTP_HOST, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertTrue(sendStatus.canProceedWithWorkflow());
	}
	

	@Test
	public void shouldFailWithSendFailedExceptionWithAnAuthenticationFailedException(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new AuthenticationFailedException("<some authentication failed message from the mailsystem>");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};	
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();
		
		//verify
		assertEquals(MessageSendStatusCode.SMTP_AUTHENTICATION_FAILED, sendStatus.getStatusCode());
		verifyStatusCodeIndicateSendFailedOnly(sendStatus);
		verifySendStatusIsError(sendStatus);
		assertTrue(sendStatus.canProceedWithWorkflow());
	}

	@Test
	public void shouldFailWithOLATRuntimeExceptionWithAnUnhandletSendFailedException(){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				MessagingException firstInnerException = new SendFailedException("unhandled sendfail exception");
				throw new SendFailedException(BY_OLAT_UNHANDLED_TEXT, firstInnerException);
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
	
		//exercise
		try{	
			MessageSendStatus sendStatus = doSendWithSendFailedException.send();
			fail("OLATRuntime exception is not thrown.");
		}catch(OLATRuntimeException ore){
			assertNotNull(ore);
		}
		
	}
	
	//these are the MessagingExceptions within the javax.mail package which are generally handled as error by OLAT.
	@DataPoint public static FolderClosedException fce = new FolderClosedException(Mockito.mock(Folder.class), "a message");
	@DataPoint public static FolderNotFoundException fnfe = new FolderNotFoundException(Mockito.mock(Folder.class), "a message");
	@DataPoint public static IllegalWriteException iwe = new IllegalWriteException("a message");
	@DataPoint public static MessageRemovedException mre = new MessageRemovedException("a message");
	@DataPoint public static MethodNotSupportedException mnse = new MethodNotSupportedException("a message");
	@DataPoint public static NoSuchProviderException nspe = new NoSuchProviderException("a message");
	@DataPoint public static ParseException pe = new ParseException("a message");
	@DataPoint public static ReadOnlyFolderException rofe = new ReadOnlyFolderException(Mockito.mock(Folder.class), "a message");
	@DataPoint public static SearchException se = new SearchException("some parse message");
	@DataPoint public static StoreClosedException sce = new StoreClosedException(Mockito.mock(Store.class),"some parse message");
	@Theory
	public void shouldFailWithErrorWithAnyOtherMessagingException(final MessagingException me){
		//setup
		ExceptionHandlingMailSendingTemplate doSendWithSendFailedException = new ExceptionHandlingMailSendingTemplate() {
			@Override
			protected boolean doSend(Emailer emailer) throws AddressException, SendFailedException, MessagingException {
				assertNotNull("emailer was constructed", emailer);
				throw me;
			}
			
			@Override
			protected MailTemplate getMailTemplate() {
				return emptyMailTemplate;
			};
			
			@Override
			protected List<ContactList> getTheToContactLists() {
				return theContactLists;
			}
			
			@Override
			protected Identity getFromIdentity() {
				return theFromIdentity;
			}
		};
		
		//exercise
		MessageSendStatus sendStatus = doSendWithSendFailedException.send();					

		//verify
		assertEquals(MessageSendStatusCode.MAIL_CONTENT_NOK, sendStatus.getStatusCode());
		verifyStatusCodeIndicateMessagingExcpetionOnly(sendStatus);
		verifySendStatusIsWarn(sendStatus);
		assertFalse(sendStatus.canProceedWithWorkflow());
		
	}

	private void verifyStatusCodeIndicateMessagingExcpetionOnly(MessageSendStatus sendStatus) {
		assertFalse(sendStatus.getStatusCode().isAddressFailedCase());
		assertFalse(sendStatus.getStatusCode().isSendFailedCase());
		assertTrue(sendStatus.getStatusCode().isMessageFailedCase());
	}
	
}
