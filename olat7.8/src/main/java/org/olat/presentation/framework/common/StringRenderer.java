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
package org.olat.presentation.framework.common;

import org.apache.log4j.Logger;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Renders anyString depending on OutputEscapeType.
 * 
 * Initial Date: 18.10.2013 <br>
 * 
 * @author lavinia
 */
public class StringRenderer {

    private static final Logger LOG = LoggerHelper.getLogger();

    /**
     * Appends the escaped/filtered/plain input string to the targetStringOutput, depending on OutputEscapeMode.
     */
    public static void render(String anyString, StringOutput targetStringOutput, OutputEscapeType escapeMode) {
        if (targetStringOutput == null || anyString == null) {
            LOG.debug("Cannot render since the targetStringOutput: " + targetStringOutput + " and anyString: " + anyString);
            return;
        }
        if (escapeMode == null) {
            // no escape, no filtering
            targetStringOutput.append(anyString);
            return;
        }
        if (OutputEscapeType.HTML.equals(escapeMode)) {
            // System.out.println("StringRenderer - OutputEscape.HTML");
            StringHelper.escapeHtml(targetStringOutput, anyString);
        } else if (OutputEscapeType.ANTISAMY.equals(escapeMode)) {
            // System.out.println("StringRenderer - OutputEscape.ANTISAMY");
            targetStringOutput.append(FilterFactory.filterXSS(anyString));
        }
    }

}
