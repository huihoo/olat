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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.olat.system.security.OLATPrincipal;

/**
 * TODO: Class Description for EmailerTest
 * 
 * <P>
 * Initial Date:  Sep 27, 2011 <br>
 * @author patrick
 */
public class EmailerTest {
	private Emailer systemEmailer;
	
	private ContactList recipients;
	private ArrayList<ContactList> recipientsContactLists;
	private List<File> emptyAttachmentsMocked;
	private List<File> fourAttachmentsMocked;
	
	private MailTemplate mailTemplateMockForSystemMailer;
	private MailPackageStaticDependenciesWrapper webappAndMailhelperMockSystemMailer;
	private ArgumentCaptor<InternetAddress> fromCaptor;
	private ArgumentCaptor<String> bodyCaptor;
	private ArgumentCaptor<String> subjectCaptor;
	private ArgumentCaptor<MailerResult> resultCaptor;
	private ArgumentCaptor<File[]> attachmentsCaptor;
	private ArgumentCaptor<List<ContactList>> contactListCaptor;

	private final int SIZE = 4;
	private Emailer peterBichselEmailer;
	private MailTemplate mailTemplateMockForPrincipalMailer;
	private MailPackageStaticDependenciesWrapper webappAndMailhelperMockPrincipalMailer;
	
	@Before
	public void setupSystemEmailer() {
		mailTemplateMockForSystemMailer = mock(MailTemplate.class);
		when(mailTemplateMockForSystemMailer.getFooterTemplate()).thenReturn(ObjectMother.MAIL_TEMPLATE_FOOTER);
		
		webappAndMailhelperMockSystemMailer = mock(MailPackageStaticDependenciesWrapper.class);
		when(webappAndMailhelperMockSystemMailer.getSystemEmailAddress()).thenReturn(ObjectMother.OLATADMIN_EMAIL);
		
		systemEmailer = new Emailer(mailTemplateMockForSystemMailer, webappAndMailhelperMockSystemMailer);
	}
	
	@Before
	public void setupMailerForAPrincipial(){
		mailTemplateMockForPrincipalMailer = mock(MailTemplate.class);
		when(mailTemplateMockForPrincipalMailer.getFooterTemplate()).thenReturn(ObjectMother.MAIL_TEMPLATE_FOOTER);
		
		webappAndMailhelperMockPrincipalMailer = mock(MailPackageStaticDependenciesWrapper.class);
		when(webappAndMailhelperMockPrincipalMailer.getSystemEmailAddress()).thenReturn(ObjectMother.OLATADMIN_EMAIL);
		
		OLATPrincipal peterBichsel = ObjectMother.createPeterBichselPrincipal();
		
		peterBichselEmailer= new Emailer(peterBichsel, true, mailTemplateMockForPrincipalMailer, webappAndMailhelperMockPrincipalMailer);
	}
	
	@Before
	public void setupAttachments(){
		emptyAttachmentsMocked = new ArrayList<File>();
		
		fourAttachmentsMocked = new ArrayList<File>(SIZE);
		for (int i = 0; i < SIZE; i++) {
			fourAttachmentsMocked.add(mock(File.class));
		}
		
	}
	
	@Before
	public void setupContactlists(){
		recipients = ObjectMother.createRecipientsContactList();	
		recipientsContactLists = new ArrayList<ContactList>();
		recipientsContactLists.add(recipients);		
	}

	@Before
	public void createCaptorsForWebappAndMailHelperMock() {
		fromCaptor = ArgumentCaptor.forClass(InternetAddress.class);
		contactListCaptor = new ArgumentCaptor<List<ContactList>>();
		bodyCaptor = ArgumentCaptor.forClass(String.class);
		subjectCaptor = ArgumentCaptor.forClass(String.class);
		attachmentsCaptor = new ArgumentCaptor<File[]>();
		resultCaptor = ArgumentCaptor.forClass(MailerResult.class);
	}
	
	@Test
	public void testSystemEmailerSetupBySendingAMessageFromAdminToInfo() throws AddressException, SendFailedException, MessagingException{
		//Setup
		//Exercise
		systemEmailer.sendEmail(ObjectMother.OLATINFO_EMAIL, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY);
		//Verify
		verify(webappAndMailhelperMockSystemMailer, times(1)).send(any(MimeMessage.class), any(MailerResult.class));
		assertEquals("system is mail sender", ObjectMother.OLATADMIN_EMAIL, systemEmailer.mailfrom);
	}

	@Test
	public void testShouldNotSendAnEmailWithDisabledMailHost() throws AddressException, SendFailedException, MessagingException{
		//Setup 
		MailPackageStaticDependenciesWrapper webappAndMailhelperMock = createWebappAndMailhelperWithDisabledMailFunctinality();
		Emailer anEmailer = new Emailer(mailTemplateMockForSystemMailer, webappAndMailhelperMock);
		//Exercise
		anEmailer.sendEmail(ObjectMother.OLATINFO_EMAIL, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY);
		//Verify
		verify(webappAndMailhelperMock, never()).send(any(MimeMessage.class), any(MailerResult.class));
	}

	private MailPackageStaticDependenciesWrapper createWebappAndMailhelperWithDisabledMailFunctinality() {
		MailPackageStaticDependenciesWrapper webappAndMailhelperMock = mock(MailPackageStaticDependenciesWrapper.class);
		when(webappAndMailhelperMock.getSystemEmailAddress()).thenReturn(ObjectMother.OLATADMIN_EMAIL);
		//see StaticDelegateToWebappAndMailHelperTest for coverage of isEmailFunctionalityDisabled
		when(webappAndMailhelperMock.isEmailFunctionalityDisabled()).thenReturn(true); 
		return webappAndMailhelperMock;
	}
	
	
	/**
	 * TODO: Emailer does not fail with using empty ContactList, how does javax.mail handle no recipients?
	 * @throws AddressException
	 * @throws MessagingException
	 */
	@Test
	public void testShouldNotFailWithEmptyContactList() throws AddressException, MessagingException{
		//Setup
		//Exercise
		systemEmailer.sendEmail(new ArrayList<ContactList>(), ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY);
		//Verify
		verify(webappAndMailhelperMockSystemMailer, times(1)).send(any(MimeMessage.class), any(MailerResult.class));
	}
	
	@Test
	public void testShouldSendMailToContactListMembers() throws AddressException, MessagingException {
		//Setup
		//Exercise
		systemEmailer.sendEmail(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY);
		//Verify
		verify(webappAndMailhelperMockSystemMailer, times(1)).send(any(MimeMessage.class), any(MailerResult.class));
		caputureAndVerifyValuesForCreateMessage(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY+ObjectMother.MAIL_TEMPLATE_FOOTER,null);
	}

	@Test
	public void testShouldSendMailToContactListMembersWithFourAttachements() throws AddressException, MessagingException {
		//Setup
		//Exercise
		systemEmailer.sendEmail(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY, fourAttachmentsMocked);
		//Verify
		verify(webappAndMailhelperMockSystemMailer, times(1)).send(any(MimeMessage.class), any(MailerResult.class));
		caputureAndVerifyValuesForCreateMessage(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY+ObjectMother.MAIL_TEMPLATE_FOOTER, fourAttachmentsMocked);
	}
	
	@Test
	public void testShouldSendMailToContactListMembersWithEmptyAttachements() throws AddressException, MessagingException {
		//Setup
		//Exercise
		systemEmailer.sendEmail(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY, emptyAttachmentsMocked);
		//Verify
		verify(webappAndMailhelperMockSystemMailer, times(1)).send(any(MimeMessage.class), any(MailerResult.class));
		caputureAndVerifyValuesForCreateMessage(recipientsContactLists, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY+ObjectMother.MAIL_TEMPLATE_FOOTER,emptyAttachmentsMocked);
	}
	
	private void caputureAndVerifyValuesForCreateMessage(List<ContactList> expectedRecipients, String expectedSubject, String expectedBody, List<File> expectedAttachments) throws AddressException, MessagingException {
		verify(webappAndMailhelperMockSystemMailer, times(1)).createMessage(fromCaptor.capture(), contactListCaptor.capture(), bodyCaptor.capture(), subjectCaptor.capture(), attachmentsCaptor.capture(), resultCaptor.capture());
		InternetAddress capturedFrom = fromCaptor.getValue();
		assertEquals("is olat admin email", ObjectMother.OLATADMIN_EMAIL, capturedFrom.getAddress());
		assertEquals("Emails in xyz are correct", expectedRecipients, contactListCaptor.getValue());
		assertEquals("subject", expectedSubject, subjectCaptor.getValue()); 
		assertEquals("body!", expectedBody, bodyCaptor.getValue());
		
		//this is Duplication of code inside Emailer, how to avoid?
		if(expectedAttachments == null || expectedAttachments.isEmpty()){
			assertArrayEquals("null attachements", null, attachmentsCaptor.getValue());
		}else{
			File[] tmp = new File[SIZE];
			assertArrayEquals("compare as arrays", expectedAttachments.toArray(tmp), attachmentsCaptor.getValue());
		}		
	}
	
	@Test
	public void testShouldCreatePrincipalMailerWithTryingInstitutionalEmail(){
		//Setup
		//Exercise
		//Verify
		assertEquals("peter bichsel is the email sender with its institutional email", ObjectMother.PETER_BICHSEL_INSTITUTIONAL_EMAIL, peterBichselEmailer.mailfrom);
	}
	
	@Test
	public void testShouldCreatePrincipalMailerWithoutTryingInstitutionalEmail(){
		//Setup
		OLATPrincipal peterBichsel = ObjectMother.createPeterBichselPrincipal();
		//Exercise
		Emailer emailer = new Emailer(peterBichsel, false, mailTemplateMockForPrincipalMailer, webappAndMailhelperMockPrincipalMailer);
		//Verify
		assertEquals("peter bichsel is the email sender with its private mail.", ObjectMother.PETER_BICHSEL_PRIVATE_EMAIL, emailer.mailfrom);
	}

	@Test
	public void testShouldSendCCMail() throws AddressException, MessagingException{
		//Setup
		OLATPrincipal helene = ObjectMother.createHeleneMeyerPrincipial();
		OLATPrincipal mia = ObjectMother.createMiaBrennerPrincipal();
		OLATPrincipal nicolas = ObjectMother.createNicolas33Principal();
		
		String cc = ObjectMother.getPrivateEmailsAsCSVFor(helene, mia, nicolas);
		InternetAddress[] privateEmailAsInternetAddressesFor = ObjectMother.getPrivateEmailAsInternetAddressesFor( nicolas, mia,helene);
		
		//Exercise
		peterBichselEmailer.sendEmailCC(cc, ObjectMother.AN_EXAMPLE_SUBJECT, ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY, null);
		//Verify
		assertEquals("peter bichsel is the email sender with its private mail.", ObjectMother.PETER_BICHSEL_INSTITUTIONAL_EMAIL, peterBichselEmailer.mailfrom);
		InternetAddress[] nullAddresses = null;
		File[] nullFiles = null;
		verify(webappAndMailhelperMockPrincipalMailer, times(1)).createMessage(any(InternetAddress.class), aryEq(nullAddresses), aryEq(privateEmailAsInternetAddressesFor), aryEq(nullAddresses), eq(ObjectMother.HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY + ObjectMother.MAIL_TEMPLATE_FOOTER), eq(ObjectMother.AN_EXAMPLE_SUBJECT), aryEq(nullFiles), any(MailerResult.class));
	}
	
	@Test
	public void testEmailerInstantiationForSystemMailerAndMailerForUser(){
		new Emailer(mailTemplateMockForSystemMailer);
		new Emailer(ObjectMother.createPeterBichselPrincipal(), true, mailTemplateMockForPrincipalMailer);
		new Emailer(ObjectMother.createPeterBichselPrincipal(), false, mailTemplateMockForPrincipalMailer);
	}
	
	
}
