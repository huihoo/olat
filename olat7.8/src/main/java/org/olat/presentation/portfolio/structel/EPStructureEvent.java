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
 * <P>
 * Initial Date: 11 oct. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPStructureEvent extends Event {

    public static final String SELECT = "select";
    public static final String SELECT_WITH_COMMENTS = "selectWithComments";
    public static final String CLOSE = "close";
    public static final String CHANGE = "change";
    public static final String SUBMIT = "submit";

    private final PortfolioStructure structure;

    public EPStructureEvent(final String command, final PortfolioStructure structure) {
        super(command);
        this.structure = structure;
    }

    public PortfolioStructure getStructure() {
        return structure;
    }

    @Override
    public boolean equals(final Object obj) {
        // use same equals
        return super.equals(obj);
    }
}
