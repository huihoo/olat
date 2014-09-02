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
package org.olat.presentation.framework.core.components.table;

import org.olat.system.event.Event;

/**
 * Initial Date: 12.12.2012 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class TableSingleSelectEvent extends Event {

    private final String action;

    private final int selection;

    /**
     * @param command
     * @param rowId
     * @param actionId
     */
    public TableSingleSelectEvent(final String command, final String action, final int selection) {
        super(Table.COMMAND_SINGLESELECT);
        this.action = action;
        this.selection = selection;
    }

    public String getAction() {
        return action;
    }

    public int getSelection() {
        return selection;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + selection;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableSingleSelectEvent other = (TableSingleSelectEvent) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (selection != other.selection)
            return false;
        return true;
    }

}
