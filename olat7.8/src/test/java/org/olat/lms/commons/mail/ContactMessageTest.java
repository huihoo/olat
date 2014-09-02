package org.olat.lms.commons.mail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.olat.data.basesecurity.Identity;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.ObjectMother;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.security.PrincipalAttributes;

public class ContactMessageTest {

    private ContactMessage contactMessage;
    private ContactList emailList;

    @Before
    public void setup() {
        Identity from = mock(Identity.class);
        contactMessage = new ContactMessage(from);
        emailList = new ContactList("contact list");
    }

    @Test
    public void testContactMessageWithStringEmail() {
        // setup
        emailList.add("test@olat.org");
        emailList.add("test02@olat.org");

        // exercise
        contactMessage.addEmailTo(emailList);

        // verify
        List<ContactList> emailContactLists = contactMessage.getEmailToContactLists();
        assertEquals(1, emailContactLists.size());
    }

    @Test
    public void testContactMessageAddingTheSameListTwice() {
        // setup
        emailList.add("test@olat.org");
        emailList.add("test02@olat.org");

        // exercise
        contactMessage.addEmailTo(emailList);
        contactMessage.addEmailTo(emailList);

        // verify
        List<ContactList> emailContactLists = contactMessage.getEmailToContactLists();
        assertEquals(1, emailContactLists.size());
        ContactList contactList = emailContactLists.get(0);
        ArrayList<String> emailsAsStrings = contactList.getEmailsAsStrings();
        assertEquals(2, emailsAsStrings.size());
    }

    @Test
    public void testContactMessageWithOneIdentity() {
        // Setup
        emailList.add(ObjectMother.createHeidiBirkenstockPrincipal());

        // exercise
        contactMessage.addEmailTo(emailList);

        // verify
        assertEquals(1, contactMessage.getEmailToContactLists().size());
    }

    @Test
    public void testWithADisabledIdentityWhichShouldGetRemovedFromTheContactList() {
        // setup
        OLATPrincipal userPrincipal = mock(OLATPrincipal.class);
        PrincipalAttributes userAttributes = mock(PrincipalAttributes.class);
        when(userAttributes.getEmail()).thenReturn("disabledmailtest@olat.org");
        when(userPrincipal.getAttributes()).thenReturn(userAttributes);
        when(userAttributes.isEmailDisabled()).thenReturn(Boolean.TRUE);
        emailList.add(userPrincipal);

        // exercise
        contactMessage.addEmailTo(emailList);

        // verify
        // as the user has a disabled email he should be removed from the list and added to the disabled ones
        assertEquals(0, contactMessage.getEmailToContactLists().size());
        assertEquals(1, contactMessage.getDisabledIdentities().size());
    }

    @Test
    public void testGettersAndBasics() {
        // setup

        // exercise
        emailList.add("test@olat.org");
        contactMessage.addEmailTo(emailList);
        contactMessage.setBodyText("testing");
        contactMessage.setSubject("moretesting");

        // verify
        assertNotNull(contactMessage.getFrom());
        assertEquals("testing", contactMessage.getBodyText());
        assertEquals("moretesting", contactMessage.getSubject());
    }

}
