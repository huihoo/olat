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
package org.olat.lms.group;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.lms.user.administration.delete.UserDeletionManager;

/**
 * starting point for testing the business group service
 * 
 * <P>
 * Initial Date: 05.07.2011 <br>
 * 
 * @author guido
 */
public class BusinessGroupServiceTest {

    private BusinessGroupServiceImpl businessGroupService;
    private Identity identityMock;
    private BGContext bgContextMock;
    private BusinessGroupCreateHelper businessGroupCreate;
    private BusinessGroup businessGroupMock;

    @Before
    public void setup() {
        businessGroupService = new BusinessGroupServiceImpl();
        UserDeletionManager deletionManager = mock(UserDeletionManager.class);
        businessGroupService.userDeletionManager = deletionManager;
        BaseSecurity baseSecurity = mock(BaseSecurity.class);
        businessGroupMock = mock(BusinessGroup.class);
        businessGroupService.securityManager = baseSecurity;
        businessGroupCreate = mock(BusinessGroupCreateHelper.class);
        businessGroupService.businessGroupCreate = businessGroupCreate;
        identityMock = mock(Identity.class);
        bgContextMock = mock(BGContext.class);
    }

    @Test
    public void testCreateAndPersistBuinessGroupTypeBuddy() {
        when(businessGroupCreate.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, identityMock, "test", "desc", 3, 10, false, false, bgContextMock))
                .thenReturn(businessGroupMock);
        BusinessGroup group = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, identityMock, "test", "desc", 3, 10, false, false,
                bgContextMock);
        assertNotNull(group);
    }

}
