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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.system.commons.configuration.PropertyLocator;
import org.olat.system.commons.configuration.SystemPropertiesService;

/**
 * SystemRegistrationWorkerTest
 * 
 * <P>
 * Initial Date: 02.08.2011 <br>
 * 
 * @author guido
 */
public class SystemRegistrationWorkerTest {

    private SystemRegistrationWorker sysReg;
    private String registrationData;

    @Before
    public void setup() {
        File testData = new File(this.getClass().getResource("testData.xml").getFile());
        try {
            registrationData = FileUtils.readFileToString(testData);
        } catch (IOException e) {
            fail();
        }
        sysReg = new SystemRegistrationWorker();
        SystemPropertiesService propertyService = mock(SystemPropertiesService.class);
        when(propertyService.getStringProperty(PropertyLocator.SYSTEM_REG_SECRET_KEY)).thenReturn("secret");
        sysReg.propertyService = propertyService;
    }

    @Test
    @Ignore
    public void testRegistraion() {
        boolean result = sysReg.doTheWork(registrationData, SystemRegistrationManager.REGISTRATION_SERVER, "1.0");
        assertTrue(result);
    }

}
