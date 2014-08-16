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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.basesecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.user.UserService;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> Testclass to create 50'000 users and measures the time used to search for users using the custom fields
 * <p>
 * You might want to set your environment to give enough ram for the testcases: -Xss256k -Xmx1024m
 * <p>
 * Initial Date: 24.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@Ignore("query returns zero results but don't now why. The same query works in the baseSecurityManagerTest")
public class UserPropertiesPerformanceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();
    @Autowired
    private UserService userService;
    @Autowired
    private BaseSecurity securityManager;

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        try {
            final DB db = DBFactory.getInstance();
            db.closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

    /**
     * Create 50'000 users and after each step of 10'000 perform some search queries. The measured times are logged to the console.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateAndSearchUserTime() throws Exception {

        final int numberUsers = 50000;
        final int measureSteps = 10000;

        // create users group by creating a dummy user
        JunitTestHelper.createAndPersistIdentityAsUser("testuser");

        String username;
        String institution;
        String gender;

        log.info("Starting to create " + numberUsers + " OLAT users.");
        final Runtime r = Runtime.getRuntime();
        long startms = System.currentTimeMillis();
        for (int i = 1; i < numberUsers + 1; i++) {
            username = i + "test";
            if (i % 2 == 0) {
                institution = "myinst";
                gender = "m";
            } else {
                institution = "yourinst";
                gender = "f";
            }
            final User user = userService.createUser(username + "first", username + "last", username + "@test.test");
            userService.setUserProperty(user, UserConstants.GENDER, gender);
            userService.setUserProperty(user, UserConstants.BIRTHDAY, "24.07.3007");
            userService.setUserProperty(user, UserConstants.STREET, "Zähringerstrasse 26");
            userService.setUserProperty(user, UserConstants.EXTENDEDADDRESS, null);
            userService.setUserProperty(user, UserConstants.POBOX, null);
            userService.setUserProperty(user, UserConstants.CITY, "Zürich");
            userService.setUserProperty(user, UserConstants.COUNTRY, "Switzerland");
            userService.setUserProperty(user, UserConstants.TELMOBILE, "123456789");
            userService.setUserProperty(user, UserConstants.TELOFFICE, "123456789");
            userService.setUserProperty(user, UserConstants.TELPRIVATE, "123456789");
            userService.setUserProperty(user, UserConstants.INSTITUTIONALEMAIL, username + "@" + institution);
            userService.setUserProperty(user, UserConstants.INSTITUTIONALNAME, institution);
            userService.setUserProperty(user, UserConstants.INSTITUTIONALUSERIDENTIFIER, username + "-" + institution);
            securityManager.createAndPersistIdentityAndUserWithUserGroup(username, "hokuspokus", user);

            if (i % 10 == 0) {
                // flush now to optimize performance
                DBFactory.getInstance().closeSession();
            }

            if (i % measureSteps == 0) {
                DBFactory.getInstance().closeSession();
                final long endms = System.currentTimeMillis();
                final long time = (endms - startms);
                log.info("** Created " + i + " OLAT users in " + (time / 1000) + " seconds (" + time / i + "ms/user) \tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // now query user: find half of the users, the ones that are
                // male
                final Map<String, String> attributes = new HashMap<String, String>();
                attributes.put(UserConstants.GENDER, "m");
                attributes.put(UserConstants.STREET, "Zähringerstrasse");
                attributes.put(UserConstants.TELMOBILE, "123456");
                final String[] providers = new String[] { AUTHENTICATION_PROVIDER_OLAT };
                long querystart = System.currentTimeMillis();
                List result = securityManager.getIdentitiesByPowerSearch(null, attributes, true, null, null, providers, null, null, null, null, null);
                long querytime = System.currentTimeMillis() - querystart;
                DBFactory.getInstance().closeSession();
                assertEquals(i / 2, result.size());
                DBFactory.getInstance().closeSession();
                // check that all data is loaded and not stale
                Identity ident = (Identity) result.get(0);
                assertNotNull(ident.getUser());
                assertNotNull(userService.getUserProperty(ident.getUser(), UserConstants.FIRSTNAME));
                long afterquerytime = System.currentTimeMillis() - querystart - querytime;
                log.info("Found " + i / 2 + " of " + i + ":\t " + querytime + "ms;\tAND Power Search;\tAfter query time: " + afterquerytime + "ms\tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // find all users with power search query. the query will
                // match on all
                // users since the user value search is a like '%value%'
                // search
                querystart = System.currentTimeMillis();
                result = securityManager.getIdentitiesByPowerSearch(null, attributes, false, null, null, providers, null, null, null, null, null);
                querytime = System.currentTimeMillis() - querystart;
                assertEquals(i, result.size());
                DBFactory.getInstance().closeSession();
                // check that all data is loaded and not stale
                ident = (Identity) result.get(0);
                assertNotNull(ident.getUser());
                assertNotNull(userService.getUserProperty(ident.getUser(), UserConstants.FIRSTNAME));
                afterquerytime = System.currentTimeMillis() - querystart - querytime;
                log.info("Found " + i + " of " + i + ":\t " + querytime + "ms;\tOR Power Search;\tAfter query time: " + afterquerytime + "ms\tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // find all users with an empty power search query: this
                // should remove
                // all joins from the query (except the user join)
                querystart = System.currentTimeMillis();
                result = securityManager.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
                querytime = System.currentTimeMillis() - querystart;
                assertEquals(i, result.size());
                DBFactory.getInstance().closeSession();
                // check that all data is loaded and not stale
                ident = (Identity) result.get(0);
                assertNotNull(ident.getUser());
                assertNotNull(userService.getUserProperty(ident.getUser(), UserConstants.FIRSTNAME));
                afterquerytime = System.currentTimeMillis() - querystart - querytime;
                log.info("Found " + i + " of " + i + ":\t " + querytime + "ms;\tNULL Power Search;\tAfter query time: " + afterquerytime + "ms\tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // find one specific user using the power search method. the
                // only
                // attribute that limits the result to one user is the login
                // name
                attributes.put(UserConstants.GENDER, (i % 2 == 0 ? "m" : "f"));
                querystart = System.currentTimeMillis();
                result = securityManager.getIdentitiesByPowerSearch(i / 2 + "test", attributes, true, null, null, providers, null, null, null, null, null);
                querytime = System.currentTimeMillis() - querystart;
                assertEquals(1, result.size());
                DBFactory.getInstance().closeSession();
                // check that all data is loaded and not stale
                ident = (Identity) result.get(0);
                assertNotNull(ident.getUser());
                assertNotNull(userService.getUserProperty(ident.getUser(), UserConstants.FIRSTNAME));
                afterquerytime = System.currentTimeMillis() - querystart - querytime;
                log.info("Found 1 of " + i + ":\t " + querytime + "ms;\tAND Power Search;\tAfter query time: " + afterquerytime + "ms\tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // find one specific user via a dedicated search via login
                // name. No
                // joining is done (automatic by hibernate)
                querystart = System.currentTimeMillis();
                ident = securityManager.findIdentityByName(i / 2 + "test");
                querytime = System.currentTimeMillis() - querystart;
                assertNotNull(ident);
                DBFactory.getInstance().closeSession();
                // check that all data is loaded and not stale
                assertNotNull(ident.getUser());
                assertNotNull(userService.getUserProperty(ident.getUser(), UserConstants.FIRSTNAME));
                afterquerytime = System.currentTimeMillis() - querystart - querytime;
                log.info("Found 1 of " + i + ":\t " + querytime + "ms;\tIdentityByName Search;\tAfter query time: " + afterquerytime + "ms\tused mem: "
                        + StringHelper.formatMemory(r.totalMemory() - r.freeMemory()));

                // do full garbage collection now
                r.gc();

                // don't add search time to creation time, add the time we
                // used for
                // searching to the original start time
                final long searchms = (System.currentTimeMillis() - endms);
                startms = startms + searchms;

            }
        }
        final DB db = DBFactory.getInstance();
        db.closeSession();
    }

}
