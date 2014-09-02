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
package org.olat.connectors.campus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Initial Date: 20.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusUtilsTest {
    private static Set<Long> processedIdsSet;

    @BeforeClass
    public static void setup() {
        processedIdsSet = new HashSet<Long>();
    }

    @Test
    public void addIfNotAlreadyProcessed_added() {
        assertTrue(CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, new Long(100)));
    }

    @Test
    public void addIfNotAlreadyProcessed_notAdded() {
        assertFalse(CampusUtils.addIfNotAlreadyProcessed(processedIdsSet, new Long(100)));
    }

}
