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

package org.olat.presentation.group.area;

import java.util.List;

import org.olat.data.group.BusinessGroup;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Description:<BR>
 * Initial Date: Aug 30, 2004
 * 
 * @author gnaegi
 */
public class GroupsToAreaDataModel extends DefaultTableDataModel {
    List inAreaGroups;

    /**
     * Constructor for the GroupsToAreaDataModel
     * 
     * @param allGroups
     *            All available groups
     * @param inAreaGroups
     *            All groups that are associated to the group area. The checked rows.
     */
    public GroupsToAreaDataModel(final List allGroups, final List inAreaGroups) {
        super(allGroups);
        this.inAreaGroups = inAreaGroups;
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
            return inAreaGroups.contains(getGroup(row)) ? Boolean.TRUE : Boolean.FALSE;
        } else if (col == 1) {
            return getGroup(row).getName();
        } else {
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the group at the given position
     */
    public BusinessGroup getGroup(final int row) {
        return (BusinessGroup) super.getObject(row);
    }
}
