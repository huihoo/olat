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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test for user-service.
 * 
 * @author Christian Guretzki
 */
public class CharsetUserServiceITCase extends OlatTestCase {
    @Autowired
    private UserService userService;
    private Identity userServiceItTestIdentity;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        userServiceItTestIdentity = JunitTestHelper.createAndPersistIdentityAsUser("user_service_it_test");
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
    }

    /**
     * Test method 'setUserCharset' and 'getUserCharset'. Input : Set charset 'UTF-8' for test-identity Output: Get charset of test-identity
     */
    @Test
    public void testSetAndGetUserCharset_UTF_8() {
        testCharset("UTF-8");
    }

    /**
     * Test method 'setUserCharset' and 'getUserCharset'. Input : Set charset 'ISO-8859-1' for test-identity Output: Get charset of test-identity
     */
    @Test
    public void testSetAndGetUserCharset_ISO_8859_1() {
        testCharset("ISO-8859-1");
    }

    /**
     * @param string
     */
    private void testCharset(String testCharset) {
        userService.setUserCharset(userServiceItTestIdentity, testCharset);
        String charsetValue = userService.getUserCharset(userServiceItTestIdentity);
        assertEquals("Get not the right charset, could be setUserCharset or getUserCharset", charsetValue, testCharset);
    }

}
