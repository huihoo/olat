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


import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.mockito.ArgumentCaptor;
import org.olat.data.basesecurity.Identity;
import org.olat.system.commons.StringHelper;
import org.olat.system.security.OLATPrincipal;
import org.olat.system.security.PrincipalAttributes;

/**
 * Testdata for email testing.
 * 
 * <P>
 * Initial Date:  Sep 28, 2011 <br>
 * @author patrick
 */
public class ObjectMother {

	public static final String HELLOY_KITTY_THIS_IS_AN_EXAMPLE_BODY = "Helloy Kitty, this is an example body.";
	public static final String AN_EXAMPLE_SUBJECT = "An example subject";
	public static final String OLATINFO_EMAIL = "info@olat.org";
	public static final String OLATADMIN_EMAIL = "olatadmin@olat.org";
	public static final String MAIL_TEMPLATE_FOOTER = "***** mail sent by OLAT *** the footer!";
	
	
	public static final String HELENE_MEYER_PRIVATE_EMAIL = "helene@olat.org";
	public static final String NICOLAS_33_PRIVATE_EMAIL = "nicolas@olat.org";
	public static final String PETER_BICHSEL_PRIVATE_EMAIL = "peter@olat.org";
	public static final String PETER_BICHSEL_INSTITUTIONAL_EMAIL = "peter1@institut.olat.org";
	public static final String MIA_BRENNER_PRIVATE_EMAIL = "mbrenner@institut.olat.org";
	public static final String RETO_ALBRECHT_PRIVATE_EMAIL = "reto@olat.org";
	
	private static final String CONTACT_LIST_DESCRIPTION = "The contact list used in testing.";
	private static final String CONTACT_LIST_NAME = "Contact List Mock";

	private static Long identityAutoIncrement = Long.valueOf(19029);
	
	
	public static ContactList createRecipientsContactList() {
		ContactList returnValue = new ContactList(CONTACT_LIST_NAME, CONTACT_LIST_DESCRIPTION);

		List<OLATPrincipal> listOfIdentities = getSystemUserList();
		returnValue.addAllIdentites(listOfIdentities);
		
		
		return returnValue;
	}

	public static OLATPrincipal createPeterBichselPrincipal(){
		return createUserPrincipalMockFrom("peterbichsel", "Peter", "Bichsel", PETER_BICHSEL_PRIVATE_EMAIL, PETER_BICHSEL_INSTITUTIONAL_EMAIL);
	}
	
	
	public static OLATPrincipal createHeleneMeyerPrincipial(){
		return createUserPrincipalMockFrom("helenemeyer", "Helene", "Meyer", HELENE_MEYER_PRIVATE_EMAIL, null);
	}
		
	public static OLATPrincipal createNicolas33Principal(){
		return createUserPrincipalMockFrom("nicolas33", "Nicolas", null, NICOLAS_33_PRIVATE_EMAIL, null);
	}
	

	public static OLATPrincipal createMiaBrennerPrincipal(){
		return createUserPrincipalMockFrom("miabrenner", null, null, MIA_BRENNER_PRIVATE_EMAIL, "mbrenner@institut.olat.org");
	}
	
	public static OLATPrincipal createRetoAlbrechtPrincipal(){
		return createUserPrincipalMockFrom("retoalbrecht", null, null, RETO_ALBRECHT_PRIVATE_EMAIL, null);
	}
	
	public static OLATPrincipal createHeidiBirkenstockPrincipal() {
		return createUserPrincipalMockFrom("heidi02", "Heidi", "Birkenstock", "heidi.b@olat.org", "heidi.birkenstock@inst2.olat.org");
	}

	public static OLATPrincipal createRuediZimmermannPrincipal() {
		return createUserPrincipalMockFrom("ruediz", "Ruedi", "Zimmermann", "ruedi.zimmermann@inst2.olat.org", "ruedi.zimmermann@inst2.olat.org");
	}

	public static OLATPrincipal createInvalidPrincipal() {
		return createUserPrincipalMockFrom("invalidemailuser", "In", "Valid em ailuser", "invalid @ email", "invalid @ institutemail");
	}
	
	public static String getPrivateEmailsAsCSVFor(OLATPrincipal... principals){
		String[] emails = new String[principals.length];
		for (int i = 0; i < principals.length; i++) {
			OLATPrincipal olatPrincipal = principals[i];
			if(olatPrincipal != null){
				emails[i] = olatPrincipal.getAttributes().getEmail();
			}
			
		}
		return getEmailsAsCSVFor(emails);
	}
	
	public static String getEmailsAsCSVWithInstitionalEmailCheckFor(OLATPrincipal... principals){
		String[] emails = new String[principals.length];
		OLATPrincipal[] principalsWithoutInstitutionalMail = new OLATPrincipal[principals.length];
		Arrays.fill(principalsWithoutInstitutionalMail, null);
		int principalsWithoutCounter = 0;
		for (int i = 0; i < principals.length; i++) {
			OLATPrincipal olatPrincipal = principals[i];
			String addEmail = olatPrincipal.getAttributes().getInstitutionalEmail();
			if (addEmail != null) {
				emails[i] = addEmail;
			}else{
				principalsWithoutInstitutionalMail[principalsWithoutCounter] = olatPrincipal;
			}
		}
		String emailInstitutsPart = getEmailsAsCSVFor(emails);
		String emailPrivatePart = getPrivateEmailsAsCSVFor(principalsWithoutInstitutionalMail);
		String retVal = emailInstitutsPart + ", " + emailPrivatePart;
		return emailInstitutsPart ;
	}

	private static List<OLATPrincipal> getSystemUserList() {
		List<OLATPrincipal> returnValue = new ArrayList<OLATPrincipal>();
		returnValue.add(createPeterBichselPrincipal());
		returnValue.add(createHeleneMeyerPrincipial());
		returnValue.add(createNicolas33Principal());
		returnValue.add(createMiaBrennerPrincipal());
		returnValue.add(createRetoAlbrechtPrincipal());
		return returnValue;
	}

	private static OLATPrincipal createUserPrincipalMockFrom(String userName, final String userFirstName, final String userLastName, final String userEmail, final String userInstitutionalEmail) {
		//userEmail must be set!
		if( ! StringHelper.containsNonWhitespace(userEmail)){
			throw new IllegalArgumentException("OLATPrincipal must have a non-empty user email string");
		}
		
		OLATPrincipal userPrincipal = mock(OLATPrincipal.class);
		when(userPrincipal.getName()).thenReturn(userName);
		PrincipalAttributes userAttributes = new PrincipalAttributes() {
			
			@Override
			public boolean isEmailDisabled() {
				return false;
			}
			
			@Override
			public String getLastName() {
				return userLastName;
			}
			
			@Override
			public String getInstitutionalEmail() {
				return userInstitutionalEmail;
			}
			
			@Override
			public String getFirstName() {
				return userFirstName;
			}
			
			@Override
			public String getEmail() {
				return userEmail;
			}
		};
		
		when(userPrincipal.getAttributes()).thenReturn(userAttributes);
		return userPrincipal;
	}

	public static InternetAddress[] getPrivateEmailAsInternetAddressesFor(OLATPrincipal... principals) throws AddressException {
		InternetAddress[] retVal = new InternetAddress[principals.length];
		for (int i = 0; i < principals.length; i++) {
			OLATPrincipal olatPrincipal = principals[i];
			retVal[i] = new InternetAddress(olatPrincipal.getAttributes().getEmail());
		}
		return retVal;
	}

	public static String getEmailsAsCSVFor(String... emailAddresses) {
		String retVal = "";
		String sep = "";
		for (int i = emailAddresses.length - 1; i >= 0 ; i--) {
			String mail = emailAddresses[i];
			if(mail == null) continue;
			retVal += sep;
			retVal += mail;
			sep = ", ";
		}
		return retVal;
	}

	public static Identity getIdentityFrom(OLATPrincipal principalToWrap) {
		Identity asIdentity = mock(Identity.class);
		when(asIdentity.getKey()).thenReturn(Long.valueOf(identityAutoIncrement));
		identityAutoIncrement = identityAutoIncrement + 1;
		PrincipalAttributes principalAttributes = principalToWrap.getAttributes();
		when(asIdentity.getAttributes()).thenReturn(principalAttributes);
		return asIdentity;
	}




}
