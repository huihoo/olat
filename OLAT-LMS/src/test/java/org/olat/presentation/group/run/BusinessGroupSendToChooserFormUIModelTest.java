/**
 * 
 */
package org.olat.presentation.group.run;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.presentation.group.run.BusinessGroupSendToChooserFormUIModel.GroupParameter;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;

/**
 * @author patrick
 *
 */
public class BusinessGroupSendToChooserFormUIModelTest {

	private static final String IS_OVER_THE_OCEAN = "is over the ocean.";
	private static final String MY_SUBJECT = "My subject";
	private GroupParameter waitingList;
	private GroupParameter participantGroup;
	private GroupParameter ownerGroup;
	private Identity peterBichselAsFromIdentity;
	private List<Identity> ownerGroupList;
	private List<Identity>  participantGroupList;
	private List<Identity> waitingGroupList;
	private static final String ownerContactListName ="Owner Contact List";
	private static final String participantContactListName = "Participant Contact List";
	private static final String waitingContactListName = "Waiting Contact List";

	@Before
	public void setUp(){
		peterBichselAsFromIdentity = ObjectMother.getIdentityFrom(ObjectMother.createPeterBichselPrincipal());
		Identity heidiBirkenstock = ObjectMother.getIdentityFrom(ObjectMother.createHeidiBirkenstockPrincipal());
		Identity heleneMeyer = ObjectMother.getIdentityFrom(ObjectMother.createHeleneMeyerPrincipial());
		Identity miaBrenner = ObjectMother.getIdentityFrom(ObjectMother.createMiaBrennerPrincipal());
		Identity nicolas33 = ObjectMother.getIdentityFrom(ObjectMother.createNicolas33Principal());
		Identity retoAlbrecht = ObjectMother.getIdentityFrom(ObjectMother.createRetoAlbrechtPrincipal());
		Identity ruediZimmermann = ObjectMother.getIdentityFrom(ObjectMother.createRuediZimmermannPrincipal());
		
		ownerGroupList = new ArrayList<Identity>();
		ownerGroupList.add(heidiBirkenstock);
		ownerGroupList.add(heleneMeyer);
		ownerGroup = new GroupParameter(ownerGroupList, ownerContactListName);
				
		participantGroupList = new ArrayList<Identity>();
		participantGroupList.add(miaBrenner);
		participantGroupList.add(nicolas33);
		participantGroupList.add(retoAlbrecht);
		participantGroup = new GroupParameter(participantGroupList, participantContactListName);
		
		waitingGroupList = new ArrayList<Identity>();
		waitingGroupList.add(ruediZimmermann);
		waitingList = new GroupParameter(waitingGroupList, waitingContactListName);
	}
	
	@Test
	public void shouldCreateAContactMessageWithThreeContactlists() {
		//setup
		
		//exercise
		BusinessGroupSendToChooserFormUIModel sendToChooserFormUIModel = new BusinessGroupSendToChooserFormUIModel(peterBichselAsFromIdentity, ownerGroup, participantGroup, waitingList);
		ContactMessage contactMessage = sendToChooserFormUIModel.getContactMessageWithRecipientsAnd(MY_SUBJECT, IS_OVER_THE_OCEAN);

		//verify
		List<ContactList> emailToContactLists = contactMessage.getEmailToContactLists();
		verifyContactListFor(ownerContactListName, ownerGroupList, emailToContactLists);
		verifyContactListFor(participantContactListName, participantGroupList, emailToContactLists);
		verifyContactListFor(waitingContactListName, waitingGroupList, emailToContactLists);
		verifyBasicContactMessageParts(contactMessage);
	}
	

	@Test
	public void shouldCreateAContactMessageWithTwoContactlists() {
		//setup
		waitingList = null;
		
		//exercise
		BusinessGroupSendToChooserFormUIModel sendToChooserFormUIModel = new BusinessGroupSendToChooserFormUIModel(peterBichselAsFromIdentity, ownerGroup, participantGroup, waitingList);
		ContactMessage contactMessage = sendToChooserFormUIModel.getContactMessageWithRecipientsAnd(MY_SUBJECT, IS_OVER_THE_OCEAN);

		//verify
		List<ContactList> emailToContactLists = contactMessage.getEmailToContactLists();
		verifyContactListFor(ownerContactListName, ownerGroupList, emailToContactLists);
		verifyContactListFor(participantContactListName, participantGroupList, emailToContactLists);
		verifyBasicContactMessageParts(contactMessage);
		assertTrue("Contact Message has at least one address", contactMessage.hasAtLeastOneAddress());
	}


	@Test
	public void shouldCreateAContactMessageWithOneContactlists() {
		//setup
		participantGroup = null;
		waitingList = null;
		
		//exercise
		BusinessGroupSendToChooserFormUIModel sendToChooserFormUIModel = new BusinessGroupSendToChooserFormUIModel(peterBichselAsFromIdentity, ownerGroup, participantGroup, waitingList);
		ContactMessage contactMessage = sendToChooserFormUIModel.getContactMessageWithRecipientsAnd(MY_SUBJECT, IS_OVER_THE_OCEAN);

		//verify
		List<ContactList> emailToContactLists = contactMessage.getEmailToContactLists();
		verifyContactListFor(ownerContactListName, ownerGroupList, emailToContactLists);
		verifyBasicContactMessageParts(contactMessage);
		assertTrue("Contact Message has at least one address", contactMessage.hasAtLeastOneAddress());
	}
	

	@Test
	public void shouldCreateAContactMessageWithNoContactlists() {
		//setup
		ownerGroup = null;
		participantGroup = null;
		waitingList = null;
		
		//exercise
		BusinessGroupSendToChooserFormUIModel sendToChooserFormUIModel = new BusinessGroupSendToChooserFormUIModel(peterBichselAsFromIdentity, ownerGroup, participantGroup, waitingList);
		ContactMessage contactMessage = sendToChooserFormUIModel.getContactMessageWithRecipientsAnd(MY_SUBJECT, IS_OVER_THE_OCEAN);

		//verify
		verifyBasicContactMessageParts(contactMessage);
		boolean noAddresses  = ! contactMessage.hasAtLeastOneAddress();
		assertTrue("Contact Message has not one address", noAddresses);
	}
	
	
	private void verifyContactListFor(String contactListSelector, List<Identity> expectedIdentities, List<ContactList> emailToContactLists) {
		boolean isFound = false;
		for (ContactList contactList : emailToContactLists) {
			String contactListName = contactList.getName(); 
			ArrayList<String> emailsAsStrings = contactList.getEmailsAsStrings();
			if(contactListName.equals(contactListSelector)){
				verifyEquality(emailsAsStrings, expectedIdentities);
				isFound = true;
			}
		}
		assertTrue("Required ContactList found:"+contactListSelector, isFound);
	}

	private void verifyEquality(List<String> currentValues, List<Identity> expectedValues) {
		String[] expectedEmails = new String[expectedValues.size()];
		int i  = 0;
		for (Identity identity : expectedValues) {
			expectedEmails[i] = identity.getAttributes().getEmail();
			i++;
		}
		Arrays.sort(expectedEmails);
		
		String[] currentEmails = new String[currentValues.size()];
		currentEmails = currentValues.toArray(currentEmails);
		Arrays.sort(currentEmails);
		
		assertArrayEquals(currentEmails, currentEmails);
	}

	private void verifyBasicContactMessageParts(ContactMessage contactMessage) {
		Identity from = contactMessage.getFrom();
		assertEquals(peterBichselAsFromIdentity, from);
		String subject = contactMessage.getSubject();
		assertEquals(MY_SUBJECT, subject);
		String bodyText = contactMessage.getBodyText();
		assertEquals(IS_OVER_THE_OCEAN, bodyText);
	}

}
