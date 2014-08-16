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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.data.user.UserDao;
import org.olat.lms.user.syntaxchecker.UserNameAndPasswordSyntaxChecker;
import org.olat.system.commons.WebappHelper;

/**
 * Performance test for check if email exist
 * 
 * @author Christian Guretzki
 */
public class UserServiceImplTest {

    private UserServiceImpl userServiceImpl;
    private UserDao userDaoMock;
    private UserNameAndPasswordSyntaxChecker userNameAndPasswordSyntaxCheckerMock;
    private PropertyManager propertyManagerMock;

    private Identity identityMock;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        identityMock = mock(Identity.class);

        userServiceImpl = new UserServiceImpl();
        userDaoMock = mock(UserDao.class);
        userServiceImpl.userDao = userDaoMock;
        userNameAndPasswordSyntaxCheckerMock = mock(UserNameAndPasswordSyntaxChecker.class);
        userServiceImpl.userNameAndPasswordSyntaxChecker = userNameAndPasswordSyntaxCheckerMock;
        propertyManagerMock = mock(PropertyManager.class);
        userServiceImpl.propertyManager = propertyManagerMock;
    }

    /**
     * Test method 'getUserCharset' when property does not exist. The method must return than the default-charset Input : identity Output: default-charset
     */
    @Test
    public void testGetUserCharset_PropertyDoesNotExist() {
        // property does not exist => return null
        when(propertyManagerMock.findProperty(identityMock, null, null, null, UserServiceImpl.CHARSET)).thenReturn(null);
        String charsetValue = userServiceImpl.getUserCharset(identityMock);
        assertEquals("Wrong charset when no property exists, should return 'WebappHelper.getDefaultCharset()", charsetValue, WebappHelper.getDefaultCharset());
    }

    /**
     * Test method 'getUserCharset' when charset is not supported. The method must return than the default-charset Input : identity Output: default-charset
     */
    @Test
    public void testGetUserCharset_CharsetIsNotSupported() {
        String notSupportedCharset = "notSupportedCharset";
        PropertyImpl propertyWithNotSupportedCharset = mock(PropertyImpl.class);
        when(propertyWithNotSupportedCharset.getStringValue()).thenReturn(notSupportedCharset);
        // property does not exist => return null
        when(propertyManagerMock.findProperty(identityMock, null, null, null, UserServiceImpl.CHARSET)).thenReturn(propertyWithNotSupportedCharset);
        String charsetValue = userServiceImpl.getUserCharset(identityMock);
        assertEquals("Wrong charset when charset is not supported, should return 'WebappHelper.getDefaultCharset()", charsetValue, WebappHelper.getDefaultCharset());
    }

    /**
     * Test method 'getUserCharset' when charset is not supported. The method must return than the default-charset Input : identity Output: default-charset
     */
    @Test
    public void testGetUserCharset_CharsetIsSupported() {
        String supportedCharset = "UTF-8";
        PropertyImpl propertyWithSupportedCharset = mock(PropertyImpl.class);
        when(propertyWithSupportedCharset.getStringValue()).thenReturn(supportedCharset);
        // property does not exist => return null
        when(propertyManagerMock.findProperty(identityMock, null, null, null, UserServiceImpl.CHARSET)).thenReturn(propertyWithSupportedCharset);
        String charsetValue = userServiceImpl.getUserCharset(identityMock);
        assertEquals("Wrong charset when charset is supported, should return this value", charsetValue, supportedCharset);
    }

}
