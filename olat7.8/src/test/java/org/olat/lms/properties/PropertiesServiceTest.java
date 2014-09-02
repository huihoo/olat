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
package org.olat.lms.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;

/**
 * beginning of a service test...
 * 
 * <P>
 * Initial Date: 07.07.2011 <br>
 * 
 * @author guido
 */
public class PropertiesServiceTest {

    private PropertyService propertiesService;
    private Identity identityMock;
    private PropertyManager propertyManager;
    private PropertyImpl property;

    @Before
    public void setup() {
        propertiesService = new PropertyService();
        propertyManager = mock(PropertyManager.class);
        propertiesService.propertyManager = propertyManager;
        identityMock = mock(Identity.class);
        property = mock(PropertyImpl.class);
    }

    @Test
    public void listPropertiesTest() {
        try {
            propertiesService.listProperties(null);
            fail("null should throw exception");
        } catch (Exception e) {
            //
        }
        List<PropertyImpl> l = new ArrayList<PropertyImpl>();
        l.add(property);

        when(propertyManager.listProperties(identityMock, null, null, null, null)).thenReturn(l);
        List<PropertyImpl> result = propertiesService.listProperties(identityMock);
        assertEquals(result.size(), 1);
    }

}
