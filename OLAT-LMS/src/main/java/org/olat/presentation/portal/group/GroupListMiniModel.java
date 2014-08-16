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

package org.olat.presentation.portal.group;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.group.BusinessGroup;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * Initial Date: 03.08.2005 <br>
 * 
 * @author gnaegi
 */
public class GroupListMiniModel extends DefaultTableDataModel implements TableDataModel {
    private final Translator trans;

    /**
     * @param owned
     *            list of business groups
     */
    public GroupListMiniModel(final List groups, final Translator trans) {
        super(groups);
        this.trans = trans;
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
        final BusinessGroup businessGroup = (BusinessGroup) objects.get(row);
        switch (col) {
        case 0:
            String name = businessGroup.getName();
            name = StringEscapeUtils.escapeHtml(name).toString();
            return name;
        case 1:
            return trans.translate(businessGroup.getType());
        default:
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the business group at the given row
     */
    public BusinessGroup getBusinessGroupAt(final int row) {
        return (BusinessGroup) objects.get(row);
    }

}
