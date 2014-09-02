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
package org.olat.lms.core.course.campus.impl.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.PermissionOnResourceable;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.course.campus.Lecturer;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class LecturerMappingByPersonalNumberTest {

    private static final String PERSONAL_NUMBER_ONE = "012345";

    LecturerMappingByPersonalNumber lecturerMappingByPersonalNumberTestObject;

    BaseSecurity baseSecurityMock;

    private Lecturer lecturerMock;
    private Identity identityMockOne;
    private Identity identityMockTwo;

    @Before
    public void setup() {
        lecturerMappingByPersonalNumberTestObject = new LecturerMappingByPersonalNumber();
        baseSecurityMock = mock(BaseSecurity.class);
        lecturerMappingByPersonalNumberTestObject.baseSecurity = baseSecurityMock;

        lecturerMock = mock(Lecturer.class);
        when(lecturerMock.getPersonalNr()).thenReturn(Long.valueOf(PERSONAL_NUMBER_ONE));
        identityMockOne = mock(Identity.class);
        User userMockOne = mock(User.class);
        when(identityMockOne.getUser()).thenReturn(userMockOne);
        when(userMockOne.getRawUserProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER)).thenReturn(PERSONAL_NUMBER_ONE);
        identityMockTwo = mock(Identity.class);

        // Mock for DBImpl
        lecturerMappingByPersonalNumberTestObject.dBImpl = mock(DB.class);
    }

    @Test
    public void tryToMap_foundNoMapping() {
        List<Identity> emptyResults = new ArrayList<Identity>();
        when(
                baseSecurityMock.getVisibleIdentitiesByPowerSearch(anyString(), anyMap(), anyBoolean(), any(SecurityGroup[].class),
                        any(PermissionOnResourceable[].class), any(String[].class), any(Date.class), any(Date.class))).thenReturn(emptyResults);

        Identity mappedIdentity = lecturerMappingByPersonalNumberTestObject.tryToMap(lecturerMock.getPersonalNr());
        assertNull("Must return null, when no mapping exists", mappedIdentity);
    }

    @Test
    public void tryToMap_foundMoreThanOneMapping() {
        List<Identity> twoIdentities = new ArrayList<Identity>();
        twoIdentities.add(identityMockOne);
        twoIdentities.add(identityMockTwo);
        when(
                baseSecurityMock.getVisibleIdentitiesByPowerSearch(anyString(), anyMap(), anyBoolean(), any(SecurityGroup[].class),
                        any(PermissionOnResourceable[].class), any(String[].class), any(Date.class), any(Date.class))).thenReturn(twoIdentities);

        Identity mappedIdentity = lecturerMappingByPersonalNumberTestObject.tryToMap(lecturerMock.getPersonalNr());
        assertNull("Must return null, when more than one mapping exists", mappedIdentity);
    }

    @Test
    public void tryToMap_foundOneMapping() {
        List<Identity> oneIdentities = new ArrayList<Identity>();
        oneIdentities.add(identityMockOne);
        when(
                baseSecurityMock.getVisibleIdentitiesByPowerSearch(anyString(), anyMap(), anyBoolean(), any(SecurityGroup[].class),
                        any(PermissionOnResourceable[].class), any(String[].class), any(Date.class), any(Date.class))).thenReturn(oneIdentities);

        Identity mappedIdentity = lecturerMappingByPersonalNumberTestObject.tryToMap(lecturerMock.getPersonalNr());
        assertNotNull("Must return an identity, when only one mapping exists", mappedIdentity);
    }

}
