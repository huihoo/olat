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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.olat.system.mail.DefaultStaticDependenciesDelegator.StaticDelegatorToMailHelper;

/**
 * verify that enabling/disabling of mail host in properties works as expected.
 * 
 * <P>
 * Initial Date: Sep 28, 2011 <br>
 * 
 * @author patrick
 */
@RunWith(Theories.class)
public class StaticDelegateToWebappHelperAndMailHelperTest {

	private DefaultStaticDependenciesDelegator staticDelegate;
	private StaticDelegatorToMailHelper mailHelperDelegateMock;
	
	

	@Before
	public void setupStaticDelegate() {
		staticDelegate = new DefaultStaticDependenciesDelegator();

		mailHelperDelegateMock = mock(StaticDelegatorToMailHelper.class);

		staticDelegate.mailHelperDelegate = mailHelperDelegateMock;
	}

	
	public static @DataPoints Object[] candidates = { null, "", new Integer(1), "disabled", "DISabled" };
	@Theory
	public void testEmailingIsDisabledWithDisablingMailhostPropertyValues(Object propertyValue) {
		// behavior
		when(mailHelperDelegateMock.getMailhost()).thenReturn(propertyValue);

		// verify
		assertTrue("email is disabled",
				staticDelegate.isEmailFunctionalityDisabled());
	}

	@Test
	public void testEmailingIsEnabledWithAnIPValue() {
		// behavior
		when(mailHelperDelegateMock.getMailhost()).thenReturn("127.1.0.1");

		// verify
		assertFalse("email is enabled with IP Value",	staticDelegate.isEmailFunctionalityDisabled());
	}


	@Test
	public void testEmailingIsEnabledWithAnHostNameValue() {
		// behavior
		when(mailHelperDelegateMock.getMailhost()).thenReturn("smtp.olat.org");

		// verify
		assertFalse("email is enabled with hostname value",	staticDelegate.isEmailFunctionalityDisabled());
	}
	
}
