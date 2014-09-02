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
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.presentation.user.administration.groups;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.group.BusinessGroup;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * model for details about a group an user is in. Details are: type of group, groupname, role of user in group (participant, owner, on waiting list), date of joining the
 * group
 */
class GroupOverviewModel extends DefaultTableDataModel {

    private int columnCount = 0;

    /**
     * @param objects
     */
    public GroupOverviewModel(final List<Object[]> objects, final int columnCount) {
        super(objects);
        this.columnCount = columnCount;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return columnCount;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final Object o = getObject(row);
        Object[] dataArray = null;
        dataArray = (Object[]) o;

        final Object groupColItem = dataArray[col];

        switch (col) {
        case 0:
            return groupColItem;
        case 1:
            return groupColItem;
        case 2:
            String name = ((BusinessGroup) groupColItem).getName();
            name = StringEscapeUtils.escapeHtml(name).toString();
            return name;
        case 3:
            return groupColItem;
        case 4:
            return groupColItem;
        default:
            return "error";
        }
    }

    /**
     * method to get the BusinessGroup-Object which is in the model, but getValueAt() would only return the name of the group.
     * 
     * @param row
     * @return BusinessGroup from a certain row in model
     */
    protected BusinessGroup getBusinessGroupAtRow(final int row) {
        final Object o = getObject(row);
        Object[] dataArray = null;
        dataArray = (Object[]) o;
        return (BusinessGroup) dataArray[2];
    }

}
