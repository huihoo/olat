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

import org.junit.Test;

/**
 * Initial Date:  Oct 3, 2011 <br>
 * @author patrick
 */
public class EmailAddressValidatorTest {
	
	@Test
	public void validWithSimpleNameAndOneDomainAndOneTopLevel() {
		isValid("info@olat.org");
		isValid("login@w.pl");
		isValid("d@i.ch");
	}
	
	@Test
	public void validWithSimpleNameAndSeveralDomainsAndOneTopLevel() {
		isValid("info@desk.olat.org");
		isValid("login@subsubdomain.subdomain.w.pl");
		isValid("d@i.ch");
	}
	
	@Test
	public void validWithComplexNamesAndOneDomainAndOneTopLevel() {
		isValid("info-desk@olat.org");
		isValid("login.info.message@w.pl");
		isValid("d_i_g_i_t_a_l@i.ch");
	}
	
	@Test
	public void inValidWithUmlaute() {
		isInvalid("änfö@olat.org");
		isInvalid("login.info.message@för.de");
	}
	
	@Test
	public void inValidWithWhitespaces() {
		isInvalid("info @olat.org");
		isInvalid("info@ olat.org");
		isInvalid("info @ olat.org");
		isInvalid("info@olat.org ");
		isInvalid(" info@olat.org");

		isInvalid("info@_olat.org");
		isInvalid("info@olat.org_");		
	}
	

	@Test
	public void inValidWithNullAndEmptyString() {
		isInvalid(null);
		isInvalid("");	
	}

	@Test
	public void inValidWithoutAt() {
		isInvalid("info");
		isInvalid("olat.org");	
	}


	@Test
	public void disabledInOlatIsTheQuotedIdentifier() {
		isInvalid("\"Olat Info\" <info@olat.org>");
	}
	
	@Test
	public void disabledInOlatIsTheDomainLiteralIPhost() {
		isInvalid("info@[127.0.0.1]");
	}
	
	
	
	private boolean isValid(String mailAddress){
		return EmailAddressValidator.isValidEmailAddress(mailAddress);
	}
	
	private boolean isInvalid(String mailAddress){
		return ! isValid(mailAddress); 
	}
	
	
}
