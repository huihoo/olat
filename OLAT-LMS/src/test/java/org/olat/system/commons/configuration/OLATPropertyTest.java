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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * <P>
 * Initial Date: 08.06.2011 <br>
 * 
 * @author guido
 */
public class OLATPropertyTest {

    private OLATProperty property;
    private static final String DEFAULT_VALUE = "test-a";

    @Before
    public void setup() {

        property = new OLATProperty("test", DEFAULT_VALUE);

    }

    @Test
    public void testGetComment() {
        property.setComment("a comment");

        assertTrue(property.hasComment());
        assertEquals("a comment", property.getComment());
    }

    @Test
    public void testGetDefaultValue() {
        property.setOverwriteValue("overwrite");

        assertTrue(property.isOverwritten());
        assertEquals(DEFAULT_VALUE, property.getDefaultValue());

    }

    @Test
    public void testOverwriteChain() {
        property.setOverwriteValue("a");
        property.setOverwriteValue("b");
        property.setOverwriteValue("c");

        assertTrue(property.isOverwritten());
        assertEquals(DEFAULT_VALUE, property.getDefaultValue());

        assertEquals("a", property.getOverwriteValues().get(0));
        assertEquals("b", property.getOverwriteValues().get(1));
        assertEquals("c", property.getOverwriteValues().get(2));

    }

    @Test
    public void testSetDelemitedValues() {
        property.setAvailableValues("a,b,c");

        assertEquals("a", property.getAvailableValues().get(0));
        assertEquals("b", property.getAvailableValues().get(1));
        assertEquals("c", property.getAvailableValues().get(2));
    }

}
