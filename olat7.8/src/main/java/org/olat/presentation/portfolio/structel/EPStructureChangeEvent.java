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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.portfolio.structel;

import org.olat.data.portfolio.structure.PortfolioStructure;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: rhaag Class Description for EPStructureChangeEvent
 * <P>
 * Initial Date: 25.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPStructureChangeEvent extends Event {

    private final PortfolioStructure portfolioStructure;

    public static final String ADDED = "added";
    public static final String REMOVED = "removed";
    public static final String CHANGED = "changed";
    public static final String SELECTED = "selected";

    public EPStructureChangeEvent(final String command, final PortfolioStructure portStruct) {
        super(command);
        this.portfolioStructure = portStruct;
    }

    /**
     * @return Returns the portfolioStructure.
     */
    public PortfolioStructure getPortfolioStructure() {
        return portfolioStructure;
    }

    @Override
    public boolean equals(final Object obj) {
        // use same equals
        return super.equals(obj);
    }

}
