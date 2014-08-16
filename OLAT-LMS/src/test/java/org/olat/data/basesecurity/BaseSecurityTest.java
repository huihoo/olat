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
package org.olat.data.basesecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * TODO: Class Description for BaseSecurityTest
 * 
 * <P>
 * Initial Date: 08.06.2011 <br>
 * 
 * @author lavinia
 */
public class BaseSecurityTest {

    private BaseSecurityManager baseSecurityManager;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        baseSecurityManager = new BaseSecurityManager();
    }

    @Test
    public void createOLATResourceable() {
        // rSystem.out.println(RESOURCE_TYPE_ENUM.GenericQuotaEditController.name());
        assertEquals("GenericQuotaEditController", SecurityResourceTypeEnum.GenericQuotaEditController.name());

        OLATResourceable resourceable = baseSecurityManager.createOLATResourceable(SecurityResourceTypeEnum.GenericQuotaEditController);
        assertNotNull(resourceable);
        assertNotNull(resourceable.getResourceableTypeName());
        assertEquals(resourceable.getResourceableTypeName(), "GenericQuotaEditController");
        assertEquals(resourceable.getResourceableTypeName(), SecurityResourceTypeEnum.GenericQuotaEditController.name());
        assertNotNull(resourceable.getResourceableId());

    }

}
