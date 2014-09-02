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

import java.util.Set;

/**
 * Utility class.<br>
 * 
 * Initial Date: 19.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusUtils {
    public final static String SEMICOLON_REPLACEMENT = "&Semikolon&";
    public final static String SEMICOLON = ";";

    /**
     * Adds the given id if not already been processed. <br>
     * Returns false if the given Id has been already processed,<br>
     * otherwise adds it to the processedIdsSet and returns true
     * 
     * @param processedIdsSet
     *            the set of already processed ids
     * @param id
     *            the id to be processed
     */
    public static boolean addIfNotAlreadyProcessed(Set<Long> processedIdsSet, Long id) {
        if (processedIdsSet.contains(id)) {
            return false;
        } else {
            processedIdsSet.add(id);
            return true;
        }
    }

}
