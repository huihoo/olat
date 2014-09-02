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
package org.olat.lms.core.course.campus.impl.syncer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.lms.core.course.campus.impl.syncer.CampuskursCoOwners;

/**
 * Initial Date: 12.07.2012 <br>
 * 
 * @author cg
 */
public class CampuskursCoOwnersTest {

    private CampuskursCoOwners campuskursCoOwnersTestObject;
    private BaseSecurity baseSecurityMock;
    private CampusConfiguration campusConfigurationMock;

    @Before
    public void setup() {
        baseSecurityMock = mock(BaseSecurity.class);
        campusConfigurationMock = mock(CampusConfiguration.class);
        campuskursCoOwnersTestObject = new CampuskursCoOwners();
        campuskursCoOwnersTestObject.baseSecurity = baseSecurityMock;
        campuskursCoOwnersTestObject.campusConfiguration = campusConfigurationMock;

    }

    @Test
    public void getDefaultCoOwners_emptyCoOwnerNames() {
        String EMPTY_CO_OWNER_NAMES = "";
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(EMPTY_CO_OWNER_NAMES);
        // Exercise
        List<Identity> coOwners = campuskursCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, must 0 when value is empty", 0, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_notExistingCoOwnerName() {
        String nonExistingCoOwnerNames = "test1,test2";
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(nonExistingCoOwnerNames);
        // Exercise
        List<Identity> coOwners = campuskursCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, must be 0 when no identities exist", 0, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_oneCoOwnerName() {
        String coOwnerName = "test1";
        Identity coOwnerIdentityMock = mock(Identity.class);
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerName);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campuskursCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners", 1, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_twoCoOwnerName() {
        String coOwnerName = "test1";
        Identity coOwnerIdentityMock = mock(Identity.class);
        String secondCoOwnerName = "test2";
        Identity secondCoOwnerIdentityMock = mock(Identity.class);
        String coOwnerConigValue = coOwnerName + "," + secondCoOwnerName;
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerConigValue);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        when(baseSecurityMock.findIdentityByName(secondCoOwnerName)).thenReturn(secondCoOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campuskursCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners", 2, coOwners.size());
    }

    @Test
    public void getDefaultCoOwners_duplicateCoOwnerName() {
        String coOwnerName = "test3";
        Identity coOwnerIdentityMock = mock(Identity.class);
        String coOwnerConigValue = coOwnerName + "," + coOwnerName;
        when(campusConfigurationMock.getDefaultCoOwnerUserNames()).thenReturn(coOwnerConigValue);
        when(baseSecurityMock.findIdentityByName(coOwnerName)).thenReturn(coOwnerIdentityMock);
        // Exercise
        List<Identity> coOwners = campuskursCoOwnersTestObject.getDefaultCoOwners();
        assertEquals("Wrong number of owners, duplicate identity can be added only once", 1, coOwners.size());
    }

}
