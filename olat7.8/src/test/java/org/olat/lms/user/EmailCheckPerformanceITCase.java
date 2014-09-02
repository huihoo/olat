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
package org.olat.lms.user;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Performance test for check if email exist
 * 
 * @author Christian Guretzki
 */
public class EmailCheckPerformanceITCase extends OlatTestCase {
    private static final String TEST_DOMAIN = "@test.test";

    private static final String USERNAME_CONSTANT = "email-test";

    private static final Logger log = LoggerHelper.getLogger();

    private static long createUserTime;
    private static long testExistEmailAddressTime;
    @Autowired
    private UserService userService;
    @Autowired
    private BaseSecurity securityManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        createUsers();
    }

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

    // @Test public void testExistEmailAddress() throws Exception {
    // System.out.println("testExistEmailAddress...");
    // int MAX_LOOP = 100;
    // long startTime = System.currentTimeMillis();
    // for (int i = 0; i<MAX_LOOP; i++) {
    // boolean test = ManagerFactory.getManager().existEmailAddress("test_" + i + "@test.ti");
    // if (test == true) {
    // System.out.println("TEST EMAIL EXIST 1");
    // }
    // }
    // long endTime = System.currentTimeMillis();
    // testExistEmailAddressTime = (endTime - startTime);
    // log.info("testExistEmailAddress takes time=" + (endTime - startTime) );
    // }

    @Test
    public void testUserManger() throws Exception {
        System.out.println("testUserManger start...");
        final int MAX_LOOP = 100;
        final long startTime = System.currentTimeMillis();
        for (int i = 1; i < MAX_LOOP; i++) {
            final Identity test = userService.findIdentityByEmail(i + USERNAME_CONSTANT + TEST_DOMAIN);
            if (test == null) {
                System.out.println("user with email not found: " + i + USERNAME_CONSTANT + TEST_DOMAIN);
            }
            assertNotNull(test);
        }
        final long endTime = System.currentTimeMillis();

        // optimized version

        final long startTime2 = System.currentTimeMillis();
        for (int i = 1; i < MAX_LOOP; i++) {
            final boolean test = userService.userExist(i + USERNAME_CONSTANT + TEST_DOMAIN);
            assertTrue(test);
        }
        final long endTime2 = System.currentTimeMillis();

        log.info("testUserManger takes time=" + (endTime - startTime) + " testUserManger (optimized) takes time=" + (endTime2 - startTime2)
                + " ; testExistEmailAddressTime=" + testExistEmailAddressTime + " ; createUserTime=" + createUserTime);
    }

    private void createUsers() {
        final int numberUsers = 10000;
        String username;
        String institution;
        String gender;

        final long startTime = System.currentTimeMillis();

        // only create users if not yet done
        if (userService.findIdentityByEmail("1" + USERNAME_CONSTANT + TEST_DOMAIN) == null) {
            // create users group
            if (securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS) == null) {
                securityManager.createAndPersistNamedSecurityGroup(Constants.GROUP_OLATUSERS);
            }

            System.out.println("TEST start creating " + numberUsers + " testusers");
            for (int i = 1; i < numberUsers + 1; i++) {
                username = i + USERNAME_CONSTANT;
                if (i % 2 == 0) {
                    institution = "myinst";
                    gender = "m";
                } else {
                    institution = "yourinst";
                    gender = "f";
                }
                final User user = userService.createUser(username + "first", username + "last", username + TEST_DOMAIN);
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
                userService.setUserProperty(user, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, username + "-" + institution);
                securityManager.createAndPersistIdentityAndUserWithUserGroup(username, "hokuspokus", user);

                if (i % 10 == 0) {
                    // flush now to obtimize performance
                    DBFactory.getInstance().closeSession();
                    System.out.print(".");
                }
            }
            final long endTime = System.currentTimeMillis();
            createUserTime = (endTime - startTime);
            System.out.println("TEST created " + numberUsers + " testusers in createUserTime=" + createUserTime);
        }
    }
}
