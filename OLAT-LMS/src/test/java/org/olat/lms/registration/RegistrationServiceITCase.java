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

package org.olat.lms.registration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.registration.TemporaryKeyImpl;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class RegistrationServiceITCase extends OlatTestCase {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private RegistrationService registrationService;

    /**
     * Test internal registration.
     */
    @Test
    public void testRegister() {
        final String emailaddress = "sabina@jeger.net";
        final String ipaddress = "130.60.112.10";
        final TemporaryKeyImpl result = registrationService.register(emailaddress, ipaddress, "register");
        assertTrue(result != null);
        assertEquals(emailaddress, result.getEmailAddress());
        assertEquals(ipaddress, result.getIpAddress());
    }

    /**
     * Test load of temp key.
     */
    @Test
    public void testLoadTemporaryKeyByRegistrationKey() {
        final String emailaddress = "christian.guretzki@id.uzh.ch";
        String regkey = "";
        TemporaryKeyImpl result = null;
        final String ipaddress = "130.60.112.12";

        //
        result = registrationService.loadTemporaryKeyByRegistrationKey(regkey);
        assertTrue("not found, as registration key is empty", result == null);

        // now create a temp key
        result = registrationService.createTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationModule.REGISTRATION);
        assertTrue("result not null because key generated", result != null);
        // **
        DBFactory.getInstance().closeSession();
        regkey = result.getRegistrationKey();
        // **

        // check that loading the key by registration key works
        result = null;
        result = registrationService.loadTemporaryKeyByRegistrationKey(regkey);
        assertTrue("we should find the key just created", result != null);
    }

    /**
     * Test load of temp key.
     */
    @Test
    public void testLoadTemporaryKeyEntry() {
        final String emailaddress = "patrickbrunner@uzh.ch";
        TemporaryKeyImpl result = null;
        final String ipaddress = "130.60.112.11";

        // try to load temp key which was not created before
        result = registrationService.loadTemporaryKeyByEmail(emailaddress);
        assertTrue("result should be null, because not found", result == null);

        // now create a temp key
        result = registrationService.createTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationModule.REGISTRATION);
        assertTrue("result not null because key generated", result != null);
        // **
        DBFactory.getInstance().closeSession();
        // **

        // check that loading the key by e-mail works
        result = null;
        result = registrationService.loadTemporaryKeyByEmail(emailaddress);
        assertTrue("we shoult find the key just created", result != null);
    }

    /**
     * Test load of temp key.
     */
    @Test
    public void testCreateTemporaryKeyEntry() {
        String emailaddress = "sabina@jeger.net";
        TemporaryKeyImpl result = null;
        final String ipaddress = "130.60.112.10";

        result = registrationService.createTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationModule.REGISTRATION);
        assertTrue(result != null);

        emailaddress = "sabina@jeger.ch";
        result = registrationService.createTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationModule.REGISTRATION);

        assertTrue(result != null);

        emailaddress = "info@jeger.net";
        result = registrationService.createTemporaryKeyByEmail(emailaddress, ipaddress, RegistrationModule.REGISTRATION);

        assertTrue(result != null);
    }

    /**
     * Acceptance-test for confirm-disclaimer workflow : 1. revoke all confirmed disclaimers 2. set 'hasConfirmedDislaimer = true' for test identity 3. revoke
     * confirmed-disclaimer for test identity
     */
    @Test
    public void acceptanceTestConfirmedDisclaimer() {
        assertTrue("Property 'disclaimerEnabled' must be enabled for this test", RegistrationModule.isDisclaimerEnabled());
        Identity registrationTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("registrationTestIdentity");
        registrationService.revokeAllconfirmedDisclaimers();
        assertTrue("Test identity must confirm disclaimer (check method 'revokeAllconfirmedDisclaimers' and 'needsToConfirmDisclaimer')",
                registrationService.needsToConfirmDisclaimer(registrationTestIdentity));
        registrationService.setHasConfirmedDislaimer(registrationTestIdentity);
        assertFalse("Test identity should not have any disclaimer to confirm (check method 'setHasConfirmedDislaimer')",
                registrationService.needsToConfirmDisclaimer(registrationTestIdentity));
        registrationService.revokeConfirmedDisclaimer(registrationTestIdentity);
        assertTrue("Test identity must confirm disclaimer (check method 'revokeConfirmedDisclaimer')",
                registrationService.needsToConfirmDisclaimer(registrationTestIdentity));
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        DBFactory.getInstance().closeSession();
    }

}
