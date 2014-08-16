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

package org.olat.lms.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desciption: jUnit testsuite to test the OLAT user module. Most tests are method tests of the user manager. Currently no tests for actions are available du to missing
 * servlet stuff.
 * 
 * @author Florian Gnägi
 */
public class UserServiceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    // variables for test fixture
    private User u1, u2, u3;
    private Identity i1, i2, i3;
    @Autowired
    private UserService userService;
    @Autowired
    private BaseSecurity securityManager;

    public UserServiceITCase() {
        System.out.println("user test started...: " + this.hashCode());
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        System.out.println("running before...: " + this.hashCode());
        // create some users with user manager
        // set up fixture using the user manager

        if (securityManager.findIdentityByName("judihui") == null) {
            u1 = userService.createUser("judihui", "judihui", "judihui@id.uzh.ch");
            userService.setUserProperty(u1, UserConstants.INSTITUTIONALEMAIL, "instjudihui@id.uzh.ch");
            userService.setUserProperty(u1, UserConstants.INSTITUTIONALNAME, "id.uzh.ch");
            userService.setUserProperty(u1, UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
            i1 = securityManager.createAndPersistIdentityAndUser(getLastName(u1), u1, "OLAT", getLastName(u1), "");
        } else {
            System.out.println("Does not create user, found 'judihui' already in db");
            i1 = securityManager.findIdentityByName("judihui");
            u1 = i1.getUser();
        }
        if (securityManager.findIdentityByName("migros") == null) {
            u2 = userService.createUser("migros", "migros", "migros@id.migros.uzh.ch");
            userService.setUserProperty(u2, UserConstants.INSTITUTIONALEMAIL, "instmigros@id.migros.uzh.ch");
            userService.setUserProperty(u2, UserConstants.INSTITUTIONALNAME, "id.migros.uzh.ch");
            userService.setUserProperty(u2, UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
            i2 = securityManager.createAndPersistIdentityAndUser(getLastName(u2), u2, "OLAT", getLastName(u2), "");
        } else {
            System.out.println("Does not create user, found 'migros' already in db");
            i2 = securityManager.findIdentityByName("migros");
            u2 = i2.getUser();
        }
        if (securityManager.findIdentityByName("salat") == null) {
            u3 = userService.createUser("salat", "salat", "salat@id.salat.uzh.ch");
            userService.setUserProperty(u3, UserConstants.INSTITUTIONALEMAIL, "instsalat@id.salat.uzh.ch");
            userService.setUserProperty(u3, UserConstants.INSTITUTIONALNAME, "id.salat.uzh.ch");
            userService.setUserProperty(u3, UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
            i3 = securityManager.createAndPersistIdentityAndUser(getLastName(u3), u3, "OLAT", getLastName(u3), "");
        } else {
            System.out.println("Does not create user, found 'salat' already in db");
            i3 = securityManager.findIdentityByName("salat");
            u3 = i3.getUser();
        }
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        System.out.println("running after...: " + this.hashCode());
        // do not clean up created users as they are used for all tests
    }

    private User getUser(String userName) {
        Identity identity = securityManager.findIdentityByName(userName);
        assertNotNull("User not found: " + userName, identity);
        return identity.getUser();
    }

    @Test
    public void testEquals() {

        final User user1 = getUser("salat");
        final User user2 = getUser("migros");
        final User user1_2 = getUser("salat");

        assertNotNull("Could not find user with name 'salat'", user1);
        assertNotNull("Could not find user with name 'migros'", user2);
        assertNotNull("Could not find second user with name 'salat'", user1_2);

        assertFalse("Wrong equals implementation, different types are recognized as equals ", user1.equals(new Integer(1)));
        assertFalse("Wrong equals implementation, different users are recognized as equals ", user1.equals(user2));
        assertFalse("Wrong equals implementation, null value is recognized as equals ", user1.equals(null));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", user1.equals(user1));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", user1.equals(user1_2));
    }

    @Test
    public void testEqualsIdentity() {
        final Identity ident1 = userService.findIdentityByEmail("salat@id.salat.uzh.ch");
        final Identity ident2 = userService.findIdentityByEmail("migros@id.migros.uzh.ch");
        final Identity ident1_2 = userService.findIdentityByEmail("salat@id.salat.uzh.ch");

        assertNotNull("Could not find identity with email 'salat@id.salat.uzh.ch'", ident1);
        assertNotNull("Could not find identity with email 'migros@id.migros.uzh.ch'", ident2);
        assertNotNull("Could not find second identity with email 'salat@id.salat.uzh.ch'", ident1_2);

        assertFalse("Wrong equals implementation, different types are recognized as equals ", ident1.equals(new Integer(1)));
        assertFalse("Wrong equals implementation, different users are recognized as equals ", ident1.equals(ident2));
        assertFalse("Wrong equals implementation, null value is recognized as equals ", ident1.equals(null));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", ident1.equals(ident1));
        assertTrue("Wrong equals implementation, same users are NOT recognized as equals ", ident1.equals(ident1_2));
    }

    @Test
    public void testHashCode() {
        final User user1 = getUser("salat");
        final User user2 = getUser("migros");
        final User user1_2 = getUser("salat");

        assertNotNull("Could not find user with name 'salat'", user1);
        assertNotNull("Could not find user with name 'migros'", user2);
        assertNotNull("Could not find second user with name 'salat'", user1_2);

        assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ", user1.hashCode() == user1.hashCode());
        assertFalse("Wrong hashCode implementation, different users have same hash-code", user1.hashCode() == user2.hashCode());
        assertTrue("Wrong hashCode implementation, same users have NOT same hash-code ", user1.hashCode() == user1_2.hashCode());
    }

    /**
     * Test if usermanager.createUser() works
     * 
     * @throws Exception
     */
    @Test
    public void testUmCreateUser() throws Exception {
        // search for user u1 manually. SetUp puts the user in the database
        // so we look if we can find the user in the database
        log.debug("Entering testUmCreateUser()");
        final User found = userService.findIdentityByEmail("judihui@id.uzh.ch").getUser();
        assertTrue(u1.getKey().equals(found.getKey()));
    }

    @Test
    public void testEmailInUse() throws Exception {
        log.debug("Entering testEmailInUse()");
        // find via users email
        boolean found = userService.userExist("judihui@id.uzh.ch");
        assertTrue(found);
        // find via users institutional email
        found = userService.userExist("judihui@id.uzh.ch");
        assertTrue(found);
        // i don't like like
        found = userService.userExist("judihui@id.uzh.ch.ch");
        assertFalse(found);
        // doesn't exists
        found = userService.userExist("judihui@hkfls.com");
        assertFalse(found);
    }

    /**
     * Test if usermanager.createUser() works
     * 
     * @throws Exception
     */
    @Test
    public void testFindIdentityByEmail() throws Exception {
        log.debug("Entering testFindIdentityByEmail()");
        // find via users email
        Identity found = userService.findIdentityByEmail("judihui@id.uzh.ch");
        assertTrue(i1.getKey().equals(found.getKey()));
        // find via users institutional email
        found = userService.findIdentityByEmail("instjudihui@id.uzh.ch");
        assertTrue(i1.getKey().equals(found.getKey()));
        // find must be equals
        found = userService.findIdentityByEmail("instjudihui@id.uzh.ch.ch");
        assertNull(found);
    }

    /**
     * Test if usermanager.findUserByKey() works
     * 
     * @throws Exception
     */
    @Test
    public void testUmFindUserByKey() throws Exception {
        log.debug("Entering testUmFindUserByKey()");
        // find users that do exist
        final User u3test = userService.loadUserByKey(u1.getKey());
        assertTrue(u1.getKey().equals(u3test.getKey()));
    }

    /**
     * if usermanager finds users by institutional user identifier
     * 
     * @throws Exception
     */
    @Test
    public void testUmFindUserByInstitutionalUserIdentifier() throws Exception {
        // u1 to u3 defined with
        // u3.setInstitutionalUserIdentifier("id.uzh.ch");
        // u2.setInstitutionalUserIdentifier("id.uzh.ch");
        // u1.setInstitutionalUserIdentifier("id.uzh.ch");
        //
        final Map<String, String> searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
        final List result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertTrue("must have elements", result != null);
        assertTrue("exactly three elements", result.size() == 3);
        final String instEmailU1 = userService.getUserProperty(((Identity) result.get(0)).getUser(), UserConstants.INSTITUTIONALEMAIL);
        final String instEmailU2 = userService.getUserProperty(((Identity) result.get(1)).getUser(), UserConstants.INSTITUTIONALEMAIL);
        final String instEmailU3 = userService.getUserProperty(((Identity) result.get(2)).getUser(), UserConstants.INSTITUTIONALEMAIL);

        // check that the three found results correspond with the configured
        final boolean found1 = instEmailU1.equals("instjudihui@id.uzh.ch") || instEmailU2.equals("instjudihui@id.uzh.ch") || instEmailU3.equals("instjudihui@id.uzh.ch");
        assertTrue("find instjudihui@id.uzh.ch", found1);

        final boolean found2 = instEmailU1.equals("instmigros@id.migros.uzh.ch") || instEmailU2.equals("instmigros@id.migros.uzh.ch")
                || instEmailU3.equals("instmigros@id.migros.uzh.ch");
        assertTrue("find instmigros@id.migros.uzh.ch", found2);

        final boolean found3 = instEmailU1.equals("instsalat@id.salat.uzh.ch") || instEmailU2.equals("instsalat@id.salat.uzh.ch")
                || instEmailU3.equals("instsalat@id.salat.uzh.ch");
        assertTrue("find instsalat@id.salat.uzh.ch", found3);
    }

    /**
     * persist a user that did not exist previously in the database These is the case if the key of a user is null
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateNewUser() throws Exception {
        final User u5 = userService.createAndPersistUser("newuser", "newuser", "new@user.com");
        userService.setUserProperty(u5, UserConstants.EMAIL, "updated@email.com");
        userService.updateUser(u5);
        userService.loadUserByKey(u5.getKey());
        assertTrue(userService.getUserProperty(u5, UserConstants.EMAIL).equals("updated@email.com"));
    }

    /**
     * test if user profile does work
     * 
     * @throws Exception
     */
    @Test
    public void testSetUserProfile() throws Exception {
        // preferences that are not set to a value must not return null
        // The preferences object itself must not be null - a user always has
        // a preferences object
        final String fs = u1.getPreferences().getLanguage();
        assertTrue(fs != null);
        // change preferences values and look it up (test only one
        // attribute, we assume that getters and setters do work!)
        u1.getPreferences().setLanguage(I18nManager.getInstance().getLocaleOrDefault("de"));
        userService.updateUser(u1);
        final User u1test = userService.loadUserByKey(u1.getKey());
        assertTrue(u1test.getPreferences().getLanguage().matches("de"));
    }

    /**
     * test set and get the user's charset
     * 
     * @throws Exception
     */
    @Test
    public void testUmFindCharsetPropertyByIdentity() throws Exception {
        final User testuser = userService.loadUserByKey(u1.getKey());

        final Identity identity = securityManager.findIdentityByName(userService.getUserProperty(u1, UserConstants.LASTNAME));

        userService.setUserCharset(identity, WebappHelper.getDefaultCharset());

        DBFactory.getInstance().closeSession(); // simulate user clicks
        final String charset = userService.getUserCharset(identity);
        assertTrue(charset.matches(WebappHelper.getDefaultCharset()));
    }

    @Test
    public void testDeleteUserProperty() {

        // search with power search (to compare result later on with same search)
        Map<String, String> searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.INSTITUTIONALEMAIL, "instsalat@id.salat.uzh.ch");
        // find identity 1
        List result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(1, result.size());
        // setting null should remove this property but first reload user
        u3 = userService.loadUserByKey(u3.getKey());
        userService.setUserProperty(u3, UserConstants.INSTITUTIONALEMAIL, "bla@bla.ch");
        userService.updateUser(u3);

        // try to find it via deleted property
        searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.INSTITUTIONALEMAIL, "instsalat@id.salat.uzh.ch");
        // find identity 1
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);

        assertEquals(0, result.size());

        // search via first name
        searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.FIRSTNAME, "salat");
        // find identity 1
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(1, result.size());
        // update user
        userService.setUserProperty(u3, UserConstants.FIRSTNAME, "rotwein");
        userService.updateUser(u3);
        // try to find it via old property
        searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.FIRSTNAME, "salat");
        // find identity 1
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(0, result.size());
        // try to find it via updated property
        searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.FIRSTNAME, "rotwein");
        // find identity 1
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(1, result.size());

        // reset firstname to initial value
        userService.setUserProperty(u3, UserConstants.FIRSTNAME, "salat");
        userService.setUserProperty(u3, UserConstants.INSTITUTIONALEMAIL, "instsalat@id.salat.uzh.ch");
        userService.updateUser(u3);

    }

    /**
     * Test the user delete methods
     * <p>
     * NOTE THAT THIS TEST MUST BE THE LAST TEST IN THIS TESTCLASS SINCE IT DELETES USERS USED IN PREVIOUS TEST CASES!
     */
    @Test
    public void testDeleteUser() {

        // user still exists
        List result = securityManager.getVisibleIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null);
        assertEquals(1, result.size());
        result = securityManager.getIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null, null, null, null);
        assertEquals(1, result.size());
        // search with power search (to compare result later on with same search)
        final Map<String, String> searchValue = new HashMap<String, String>();
        searchValue.put(UserConstants.FIRSTNAME, "judihui");
        searchValue.put(UserConstants.LASTNAME, "judihui");
        searchValue.put(UserConstants.FIRSTNAME, "judihui");
        searchValue.put(UserConstants.INSTITUTIONALEMAIL, "instjudihui@id.uzh.ch");
        searchValue.put(UserConstants.INSTITUTIONALNAME, "id.uzh.ch");
        searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, "id.uzh.ch");
        // find identity 1
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(1, result.size());
        // find identity 1-3 via institutional id
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null, null);
        assertEquals(3, result.size());
        // delete user now
        final UserDeletionManager udm = UserDeletionManager.getInstance();
        udm.deleteIdentity(i1);
        // check if deleted successfully
        result = securityManager.getVisibleIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null);
        assertEquals(0, result.size());
        // not visible, but still there when using power search
        result = securityManager.getIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null, null, null, null);
        assertEquals(
                "Check first your olat.properties. This test runs only with following olat.properties : keepUserEmailAfterDeletion=true, keepUserLoginAfterDeletion=true",
                1, result.size());
        result = securityManager.getIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null, null, null, Identity.STATUS_DELETED);
        assertEquals(1, result.size());
        result = securityManager.getIdentitiesByPowerSearch("judihui", null, true, null, null, null, null, null, null, null, Identity.STATUS_ACTIV);
        assertEquals(0, result.size());
        // test if user attributes have been deleted successfully
        // find identity 1 not anymore
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
        assertEquals(0, result.size());
        // find identity 2-3 via first, last and instuser id (non-deletable fields)
        result = securityManager.getIdentitiesByPowerSearch(null, searchValue, false, null, null, null, null, null, null, null, null);
        assertEquals(2, result.size());

        // check using other methods
        Identity identity = userService.findIdentityByEmail("instjudihui@id.uzh.ch");
        assertNull("Deleted identity with email 'instjudihui@id.uzh.ch' should not be found with 'UserManager.findIdentityByEmail'", identity);
        // this method must find also the deleted identities
        identity = securityManager.findIdentityByName("judihui");
        assertNotNull("Deleted identity with username 'judihui' must be found with 'UserManager.findIdentityByName'", identity);
        // Because 'keepUserEmailAfterDeletion=true, keepUserLoginAfterDeletion=true', deleted user must be found
        identity = userService.findIdentityByEmail("judihui@id.uzh.ch");
        assertNotNull("Deleted identity with email 'judihui@id.uzh.ch' must be found with 'UserManager.findIdentityByEmail'", identity);

    }

    private String getLastName(User user) {
        return userService.getUserProperty(user, UserConstants.LASTNAME, new Locale("en"));
    }

}
