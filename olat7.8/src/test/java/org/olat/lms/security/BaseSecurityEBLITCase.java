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
package org.olat.lms.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.lms.user.UserService;
import org.olat.test.OlatTestCase;

/**
 * Initial Date: 11.07.2012 <br>
 * 
 * @author cg
 */
// Because BaseSecurityEBL needs 'ShibbolethModule', it must be an OlatTestCase. Would be need Refactoring of ShibbolethModule.
public class BaseSecurityEBLITCase extends OlatTestCase {
    private String propertyName = "testName";
    private String testPropertyValue = "testValue";
    private String testPropertyValueBefore = "valueBefore";

    private BaseSecurityEBL baseSecurityEBLTestObject;
    private User userMock;

    @Before
    public void setup() {
        baseSecurityEBLTestObject = new BaseSecurityEBL();
        baseSecurityEBLTestObject.userService = mock(UserService.class);
        userMock = mock(User.class);
    }

    @Test
    public void updateUserPropertyWhenChanged_nullValue() {
        baseSecurityEBLTestObject.updateUserPropertyWhenChanged(userMock, propertyName, null);
        verify(baseSecurityEBLTestObject.userService, never()).setUserProperty(userMock, propertyName, testPropertyValue);
    }

    @Test
    public void updateUserPropertyWhenChanged_UserPropertyIsNull() {
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, propertyName)).thenReturn(null);
        baseSecurityEBLTestObject.updateUserPropertyWhenChanged(userMock, propertyName, testPropertyValue);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, propertyName, testPropertyValue);
    }

    @Test
    public void updateUserPropertyWhenChanged_ValueNotChanged() {
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, propertyName)).thenReturn(testPropertyValue);
        baseSecurityEBLTestObject.updateUserPropertyWhenChanged(userMock, propertyName, testPropertyValue);
        verify(baseSecurityEBLTestObject.userService, never()).setUserProperty(userMock, propertyName, testPropertyValue);
    }

    @Test
    public void updateUserPropertyWhenChanged_ValueChanged() {
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, propertyName)).thenReturn(testPropertyValueBefore);
        baseSecurityEBLTestObject.updateUserPropertyWhenChanged(userMock, propertyName, testPropertyValue);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, propertyName, testPropertyValue);
    }

    @Test
    public void updateInstitutionalShibbolethUserProperties_updateINSTITUTIONALNAME() {
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put(ShibbolethModule.getInstitutionalName(), testPropertyValue);
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, UserConstants.INSTITUTIONALNAME)).thenReturn(testPropertyValueBefore);
        baseSecurityEBLTestObject.updateInstitutionalShibbolethUserProperties(userMock, attributesMap);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, UserConstants.INSTITUTIONALNAME, testPropertyValue);
    }

    @Test
    public void updateInstitutionalShibbolethUserProperties_updateINSTITUTIONAL_EMPLOYEE_NUMBER() {
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put(ShibbolethModule.getInstitutionalEmployeeNumber(), testPropertyValue);
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER)).thenReturn(testPropertyValueBefore);
        baseSecurityEBLTestObject.updateInstitutionalShibbolethUserProperties(userMock, attributesMap);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, testPropertyValue);
    }

    @Test
    public void updateInstitutionalShibbolethUserProperties_updateINSTITUTIONAL_MATRICULATION_NUMBER() {
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put(ShibbolethModule.getInstitutionalMatriculationNumber(), testPropertyValue);
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER)).thenReturn(testPropertyValueBefore);
        baseSecurityEBLTestObject.updateInstitutionalShibbolethUserProperties(userMock, attributesMap);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, testPropertyValue);
    }

    @Test
    public void updateInstitutionalShibbolethUserProperties_updateINSTITUTIONALEMAIL() {
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put(ShibbolethModule.getInstitutionalEMail(), testPropertyValue);
        when(baseSecurityEBLTestObject.userService.getUserProperty(userMock, UserConstants.INSTITUTIONALEMAIL)).thenReturn(testPropertyValueBefore);
        baseSecurityEBLTestObject.updateInstitutionalShibbolethUserProperties(userMock, attributesMap);
        verify(baseSecurityEBLTestObject.userService, times(1)).setUserProperty(userMock, UserConstants.INSTITUTIONALEMAIL, testPropertyValue);
    }

}
