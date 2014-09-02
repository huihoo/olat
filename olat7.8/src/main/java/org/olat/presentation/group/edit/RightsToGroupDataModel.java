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

package org.olat.presentation.group.edit;

import java.util.List;

import org.olat.lms.group.right.BGRights;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Description:<BR>
 * <P>
 * Initial Date: Aug 30, 2004
 * 
 * @author gnaegi
 */
public class RightsToGroupDataModel extends DefaultTableDataModel {
    List selectedRights;
    BGRights bgRights;

    /**
     * Constructor for the RightsToGroupDataModel
     * 
     * @param bgRights
     *            Available rights
     * @param selectedRights
     *            List of all areas which are associated to the group - meaning where the checkbox will be checked
     */
    public RightsToGroupDataModel(final BGRights bgRights, final List selectedRights) {
        super(bgRights.getRights());
        this.bgRights = bgRights;
        this.selectedRights = selectedRights;
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
            return selectedRights.contains(getRight(row)) ? Boolean.TRUE : Boolean.FALSE;
        } else if (col == 1) {
            return bgRights.transateRight(getRight(row));
        } else {
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the right at the given row
     */
    public String getRight(final int row) {
        return (String) super.getObject(row);
    }
}
