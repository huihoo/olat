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

package org.olat.presentation.group.learn;

import java.util.List;

import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Description:<BR/>
 * Table data model for one string that can be used in a choice list Initial Date: Oct 5, 2004
 * 
 * @author gnaegi
 */
public class StringListTableDataModel extends DefaultTableDataModel {
    List selectedStrings;

    /**
     * @param allStrings
     *            All possible strings
     * @param selectedStrings
     *            The preselected strings
     */
    public StringListTableDataModel(final List allStrings, final List selectedStrings) {
        super(allStrings);
        this.selectedStrings = selectedStrings;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        if (col == 0) {
            return selectedStrings.contains(getString(row)) ? Boolean.TRUE : Boolean.FALSE;
        } else if (col == 1) {
            return getString(row);
        } else {
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return The string at the given row position
     */
    public String getString(final int row) {
        return (String) super.getObject(row);
    }
}
