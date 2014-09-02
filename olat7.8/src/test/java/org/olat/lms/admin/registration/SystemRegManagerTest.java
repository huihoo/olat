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
package org.olat.lms.admin.registration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * SystemRegManagerTest
 * 
 * <P>
 * Initial Date: 02.08.2011 <br>
 * 
 * @author guido
 */
@Ignore("Test depends on Internet Connection and is testing the Google API")
public class SystemRegManagerTest {

    private SystemRegistrationManager systemRegManager;

    @Before
    public void setup() {
        systemRegManager = new SystemRegistrationManager();
    }

    @Test
    public void testCoordinateLookup() {
        String loc = systemRegManager.getLocationCoordinates("Zürich");
        assertEquals("47.3673470,8.5500025", loc);
    }

}
