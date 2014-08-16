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

import org.olat.data.group.area.BGArea;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Description:<BR>
 * <P>
 * Initial Date: Aug 30, 2004
 * 
 * @author gnaegi
 */
public class AreasToGroupDataModel extends DefaultTableDataModel {
    List selectedAreas;

    /**
     * Constructor for the AreasToGroupDataModel
     * 
     * @param allAreas
     *            List of all available areas
     * @param selectedAreas
     *            List of all areas which are associated to the group - meaning where the checkbox will be checked
     */
    public AreasToGroupDataModel(final List allAreas, final List selectedAreas) {
        super(allAreas);
        this.selectedAreas = selectedAreas;
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
            return selectedAreas.contains(getArea(row)) ? Boolean.TRUE : Boolean.FALSE;
        } else if (col == 1) {
            return getArea(row).getName();
        } else {
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the area at the given row
     */
    public BGArea getArea(final int row) {
        return (BGArea) super.getObject(row);
    }
}
