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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.system.security.OLATPrincipal;

/**
 * <P>
 * Initial Date: Oct 4, 2011 <br>
 * 
 * @author patrick
 */
public class ContactListTest {

    // once this test is enabled it must also be checked how to proceed with the , ; : replacement strategy in ContactList
    @Test
    @Ignore("not fixed yet, see OLAT-5802  ContactList contactlist = new ContactList(simple);escape or remove umlaute in to: field since they are not RFC 2822 conform  (OLAT-5760)")
    public void testSimpleCHUmlautContactListNameAndInstitutionalPrioFalse() throws UnsupportedEncodingException {
        // Setup
        String simpleCHUmlaut = "Kurs mit Spezialfällen Ä und é";
        String expectedRFC2047name = javax.mail.internet.MimeUtility.encodeWord(simpleCHUmlaut, "UTF-8", null);
        // Exercise
        ContactList contactlist = new ContactList(simpleCHUmlaut);
        // verify
        verifyNameAndRFC2822NameFor(expectedRFC2047name, contactlist);
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testSimpleContactListNameWithSpacesAndInstitutionalPrioFalse() {
        // Setup
        String simple = "Simple Name With Spaces";
        // exercise
        ContactList contactlist = new ContactList(simple);
        // verify
        verifyNameAndRFC2822NameFor(simple, contactlist);
        assertEquals("description is null", null, contactlist.getDescription());
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    @Ignore("Works on local ubuntu, windows, but not on the fresh (Dez. 2011) staged hudson machines.")
    public void testBadCharColonInContactListNameIsReplacedWithDoubleBarAndInstitutionalPrioFalse() {
        // Setup
        String badName = "WS11:Some course title";
        String expectedCleanedName = "WS11¦Some course title";
        // exercise
        ContactList contactlist = new ContactList(badName);
        // verify
        verifyNameAndRFC2822NameFor(expectedCleanedName, contactlist);
        assertEquals("description is null", null, contactlist.getDescription());
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testBadCharSemiColonInContactListNameIsReplacedWithUnderBarAndInstitutionalPrioFalse() {
        // Setup
        String badName = "WS11;Some course title";
        String expectedCleanedName = "WS11_Some course title";
        // exercise
        ContactList contactlist = new ContactList(badName);
        // verify
        verifyNameAndRFC2822NameFor(expectedCleanedName, contactlist);
        assertEquals("description is null", null, contactlist.getDescription());
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testBadCharCommaInContactListNameIsReplacedWithDashAndInstitutionalPrioFalse() {
        // Setup
        String badName = "WS11,Some course title";
        String expectedCleanedName = "WS11-Some course title";
        // exercise
        ContactList contactlist = new ContactList(badName);
        // verify
        verifyNameAndRFC2822NameFor(expectedCleanedName, contactlist);
        assertEquals("description is null", null, contactlist.getDescription());
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testSimpleContactListNameAndDescriptionNotNullAndInstitutionalPrioFalse() {
        // SetupContactList contactlist = new ContactList(simple);
        String simple = "Simple Name With Spaces";
        String simpleDescripton = "It is a simple contact list with spaces in its name.";
        // exercise
        ContactList contactlist = new ContactList(simple, simpleDescripton);
        // verify
        verifyNameAndRFC2822NameFor(simple, contactlist);
        assertEquals("description is set", simpleDescripton, contactlist.getDescription());
        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testContactListWithValidStringEmailsAddedIsConvertedToCorrectFormatsWithInstitutionalPrioFalse() throws AddressException {
        // Setup
        String simple = "Simple Name With Spaces";
        ContactList contactlist = new ContactList(simple);
        String helenEmail = ObjectMother.HELENE_MEYER_PRIVATE_EMAIL;
        String nioclasEmail = ObjectMother.NICOLAS_33_PRIVATE_EMAIL;
        String peterEmail = ObjectMother.PETER_BICHSEL_PRIVATE_EMAIL;

        // exercise
        contactlist.add(helenEmail);
        contactlist.add(nioclasEmail);
        contactlist.add(peterEmail);

        // verify all different email conversion and formatting of valid addresses
        verifyNameAndRFC2822NameFor(simple, contactlist);

        assertFalse("Institutional Email is not set", contactlist.isEmailPrioInstitutional());

        String expectedEmailString = ObjectMother.getEmailsAsCSVFor(helenEmail, nioclasEmail, peterEmail);
        String emailsAsCSV = contactlist.toString();
        assertEquals("emails as csv", expectedEmailString, emailsAsCSV);

        String expectedNameWithAddresses = simple + ":" + expectedEmailString + ";";
        assertEquals("RFC2822 Name with email addresses", expectedNameWithAddresses, contactlist.getRFC2822NameWithAddresses());

        InternetAddress[] expectedEmailsAsInetAdresses = InternetAddress.parse(expectedEmailString);
        InternetAddress[] emailsAsInetAdresses = contactlist.getEmailsAsAddresses();
        assertArrayEquals("emails als Interned Addresses", expectedEmailsAsInetAdresses, emailsAsInetAdresses);

        Hashtable<String, OLATPrincipal> identityEmails = contactlist.getIdentityEmails();
        assertNotNull("identity emails is not null", identityEmails);
        assertEquals("no identity emails are available", 0, identityEmails.size());
    }

    @Test
    public void testConversionToEmailAddressFormatsDoesNotFailWithoutEmailsAddedAndInstitutionalPrioTrue() throws AddressException {
        // Setup
        String simple = "Simple Name With Spaces";
        ContactList contactlist = new ContactList(simple);
        contactlist.setEmailPrioInstitutional(true);
        // exercise
        contactlist.getEmailsAsAddresses();
        contactlist.getEmailsAsStrings();
        contactlist.getIdentityEmails();
        contactlist.getRFC2822NameWithAddresses();
        // verify all different email conversion and formatting of valid addresses
        verifyNameAndRFC2822NameFor(simple, contactlist);

        assertTrue("Institutional Email is set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testSimpleContactListNameAndInstitutionalPrioTrue() {
        // Setup
        String simple = "Simple Name With Spaces";
        String simpleDescripton = "It is a simple contact list with spaces in its name.";
        // exercise
        ContactList contactlist = new ContactList(simple, simpleDescripton);
        contactlist.setEmailPrioInstitutional(true);
        // verify
        verifyNameAndRFC2822NameFor(simple, contactlist);
        assertEquals("description is set", simpleDescripton, contactlist.getDescription());
        assertTrue("Institutional Email is set", contactlist.isEmailPrioInstitutional());
    }

    private void verifyNameAndRFC2822NameFor(String contactListContstructorName, ContactList contactlist) {
        assertEquals("name getter returns same name", contactListContstructorName, contactlist.getName());
        assertEquals("name as RFC2822 has a colon attached", contactListContstructorName + ":", contactlist.getRFC2822Name());
    }

    @Test
    public void testAddingEmailsWithOlatPrincipialWithoutInstitutionalPrio() {
        // Setup
        String simple = "Simple Name With Spaces";
        ContactList contactlist = new ContactList(simple);
        OLATPrincipal peter = ObjectMother.createPeterBichselPrincipal();
        OLATPrincipal mia = ObjectMother.createMiaBrennerPrincipal();
        OLATPrincipal reto = ObjectMother.createRetoAlbrechtPrincipal();
        OLATPrincipal nicolas = ObjectMother.createNicolas33Principal();
        List<OLATPrincipal> listOfPrincipals = new ArrayList<OLATPrincipal>();
        listOfPrincipals.add(mia);
        listOfPrincipals.add(reto);
        listOfPrincipals.add(nicolas);
        // exercise
        contactlist.add(peter);
        contactlist.addAllIdentites(listOfPrincipals);

        String emailsAsCSV = contactlist.toString();
        // verify
        String expectedEmailCSV = ObjectMother.getPrivateEmailsAsCSVFor(nicolas, peter, mia, reto);
        assertEquals(expectedEmailCSV, emailsAsCSV);

        assertFalse("Institutional Email is set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testRemovingEmailsWithOlatPrincipialWithoutInstitutionalPrio() {
        // Setup
        String simple = "Simple Name With Spaces";
        ContactList contactlist = new ContactList(simple);
        OLATPrincipal peter = ObjectMother.createPeterBichselPrincipal();
        OLATPrincipal mia = ObjectMother.createMiaBrennerPrincipal();
        OLATPrincipal reto = ObjectMother.createRetoAlbrechtPrincipal();
        OLATPrincipal nicolas = ObjectMother.createNicolas33Principal();
        List<OLATPrincipal> listOfPrincipals = new ArrayList<OLATPrincipal>();
        listOfPrincipals.add(mia);
        listOfPrincipals.add(reto);
        listOfPrincipals.add(nicolas);
        // exercise
        contactlist.add(peter);
        contactlist.addAllIdentites(listOfPrincipals);
        String emailsAsCSVBeforeRemoval = contactlist.toString();

        contactlist.remove(peter);
        contactlist.remove(reto);
        String emailsAsCSVAfterRemoval = contactlist.toString();

        // verify
        String expectedEmailCSVBeforeRemoval = ObjectMother.getPrivateEmailsAsCSVFor(nicolas, peter, mia, reto);
        assertEquals(expectedEmailCSVBeforeRemoval, emailsAsCSVBeforeRemoval);

        String expectedEmailCSVAfterRemoval = ObjectMother.getPrivateEmailsAsCSVFor(nicolas, mia);
        assertEquals(expectedEmailCSVAfterRemoval, emailsAsCSVAfterRemoval);

        assertFalse("Institutional Email is set", contactlist.isEmailPrioInstitutional());
    }

    @Test
    public void testAddingAnotherContactListWithInstitutionalPrio() {
        // Setup
        String simple = "Simple Name With Spaces";
        ContactList contactlist = new ContactList(simple);
        OLATPrincipal peter = ObjectMother.createPeterBichselPrincipal();
        contactlist.add(peter);
        contactlist.setEmailPrioInstitutional(true);

        String anotherSimpleName = "Another Contact List";
        ContactList anotherContactList = new ContactList(anotherSimpleName);
        OLATPrincipal mia = ObjectMother.createMiaBrennerPrincipal();
        OLATPrincipal reto = ObjectMother.createRetoAlbrechtPrincipal();
        OLATPrincipal nicolas = ObjectMother.createNicolas33Principal();
        List<OLATPrincipal> listOfPrincipals = new ArrayList<OLATPrincipal>();
        listOfPrincipals.add(mia);
        listOfPrincipals.add(reto);
        listOfPrincipals.add(nicolas);
        anotherContactList.addAllIdentites(listOfPrincipals);

        // exercise
        String emailsAsCSVBeforeAddition = contactlist.toString();
        contactlist.add(anotherContactList);
        String emailsAsCSVAfterAddition = contactlist.toString();

        // verify
        String expectedEmailCSVBeforeAddition = ObjectMother.getEmailsAsCSVWithInstitionalEmailCheckFor(peter);
        assertEquals(expectedEmailCSVBeforeAddition, emailsAsCSVBeforeAddition);

        String expectedEmailCSVAfterAddition = ObjectMother.getEmailsAsCSVWithInstitionalEmailCheckFor(nicolas, peter, reto, mia);
        assertEquals(expectedEmailCSVAfterAddition, emailsAsCSVAfterAddition);

        assertTrue("Institutional Email is set", contactlist.isEmailPrioInstitutional());
    }

}
