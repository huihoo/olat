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
package org.olat.system.commons.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * TODO: Class Description for PersistedPropertiesServiceITCase
 * 
 * <P>
 * Initial Date: 24.05.2011 <br>
 * 
 * @author guido
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/olat/system/commons/configuration/_spring/persistedPropertiesContext.xml",
        "/org/olat/system/commons/configuration/olatPropTestCont.xml" })
public class SystemPropertiesServiceITCase {

    @Value("${db.vendor}")
    String testProperty;

    @Autowired
    SystemPropertiesService propertiesService;

    @Autowired
    DummyPropertiesChangeMessageListener messageListener;

    @Test
    public void testResolvedProperty() {
        assertEquals("test", testProperty);

    }

    @Test
    public void testResolvedPropertyWithSPEL() {
        // TODO
        assertEquals("test", testProperty);

    }

    @Test
    public void testSetProperty() {
        try {
            Thread.sleep(1000); // wait until jms is ready
            String value = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date());
            propertiesService.setProperty(PropertyLocator.DB_VENDOR, value);
            String result = propertiesService.getStringProperty(PropertyLocator.DB_VENDOR);

            // reset property to default value
            propertiesService.setProperty(PropertyLocator.DB_VENDOR, "test");

            assertEquals(value, result);
            Thread.sleep(1000);

            // count the messages the listener should have received
            // this simulates the message broadcast in the cluster
            assertEquals(2, messageListener.messageCount);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

    }

}
