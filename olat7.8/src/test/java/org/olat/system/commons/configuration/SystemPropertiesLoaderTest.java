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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date: 08.06.2011 <br>
 * 
 * @author guido
 * @author obuehler, oliver.buehler@agility-informatik.ch, Agility Informatik GmbH
 */
public class SystemPropertiesLoaderTest {

    private static final String ITCASE_DUMMY = PropertyLocator.PROPERTY_LOCATOR_ITCASE_DUMMY.getPropertyName();
    private static final String ITCASE_DUMMY_DEFAULT_VALUE = "default_olat_properties_value";
    private static final String ITCASE_DUMMY_OVERRIDDEN_VALUE = "overridden_olat_properties_value";

    private final String tempDir = System.getProperty("java.io.tmpdir");
    private final String userDataDir = tempDir + File.separator + RandomStringUtils.randomAlphabetic(10);

    private SystemPropertiesLoader propertiesLoader;

    @Before
    public void setup() {
        propertiesLoader = new SystemPropertiesLoader(userDataDir);
        propertiesLoader.init();
    }

    @Test
    public void testDefaultProperty() {
        assertNotNull(propertiesLoader.getProperty(ITCASE_DUMMY));
    }

    @Test
    public void testOverwritePropertyWithOLATLocalProps() {
        assertNotNull(propertiesLoader.getProperty(ITCASE_DUMMY));

        Map<String, OLATProperty> map = propertiesLoader.getMergedPropertiesMap();
        OLATProperty propObject = map.get(ITCASE_DUMMY);
        assertEquals(ITCASE_DUMMY_DEFAULT_VALUE, propObject.getDefaultValue());
        assertEquals(ITCASE_DUMMY_OVERRIDDEN_VALUE, propObject.getValue());
        assertTrue(propObject.isOverwritten());
    }

    @Test
    public void testOverwritePropertyWithMavenBuildProps() {
        assertEquals("Testversion 42", propertiesLoader.getProperty(PropertyLocator.BUILD_VERSION.getPropertyName()));
    }

    @Test
    public void testOverwritePropertyWithGUIManagedProps() {
        // create a property file with a content and save it before creating/loading the loader
        // this should result in a property which is overwritten three times

        File file = new File(userDataDir + File.separator + SystemPropertiesLoader.SYSTEM_CONFIGURATION_DIR + File.separator
                + SystemPropertiesLoader.SYSTEM_CONFIGURATION_FILE);

        Properties props = new Properties();
        props.setProperty(PropertyLocator.DB_VENDOR.getPropertyName(), "unittest");
        try {
            File directory = file.getParentFile();
            if (!directory.exists())
                directory.mkdirs();
            file.createNewFile();
            file.setWritable(true);

            OutputStream outputFile = new FileOutputStream(file);
            props.store(new FileOutputStream(file), null);
            outputFile.close();

            SystemPropertiesLoader propLoader = new SystemPropertiesLoader(userDataDir);
            propLoader.init();
            assertEquals("unittest", propLoader.getProperty(PropertyLocator.DB_VENDOR.getPropertyName()));
        } catch (FileNotFoundException e) {
            fail("FileNotFoundException");
        } catch (IOException e) {
            fail("IOException");
        } finally {
            if (!file.delete()) {
                // there seems to be a problem/bug in the JVM deleting a file after writing to it
                // with a BufferedWriter (Properties.store()) => calling GC should help!
                // http://stackoverflow.com/questions/991489/i-cant-delete-a-file-in-java
                System.gc();
                file.delete();
            }
        }
    }

    @After
    public void cleanup() {
        File tempDirFile = new File(tempDir);
        File file = new File(userDataDir + File.separator + SystemPropertiesLoader.SYSTEM_CONFIGURATION_DIR);
        while (file.exists() && !file.equals(tempDirFile)) {
            if (!file.delete()) {
                System.out.println("Couldn't cleanup file: " + file);
            }
            file = file.getParentFile();
        }
    }
}
