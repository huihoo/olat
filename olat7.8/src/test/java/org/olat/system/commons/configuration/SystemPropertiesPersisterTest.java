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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.system.exception.AssertException;

/**
 * 
 * <P>
 * Initial Date: 08.06.2011 <br>
 * 
 * @author guido
 */
public class SystemPropertiesPersisterTest {

    private SystemPropertiesPersister propertyPersister;
    private File testfile = new File(System.getProperty("java.io.tmpdir") + "/test.properties");

    @Before
    public void setup() {

        propertyPersister = new SystemPropertiesPersister(testfile);

    }

    @Test
    public void testExceptionWithoutLoading() {
        try {
            propertyPersister.saveProperty("test", "test");
        } catch (AssertException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testLoading() {
        assertNotNull(propertyPersister.loadProperties());

        propertyPersister.saveProperty("what a nice day", "sunny");

        Properties props = propertyPersister.loadProperties();
        assertEquals("sunny", props.getProperty("what a nice day"));

    }

    @After
    public void cleanup() {
        if (testfile.exists())
            testfile.delete();
    }

}
