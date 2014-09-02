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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.area.BGArea;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;

/**
 * Description:<BR>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public class BGAreaTableModel extends DefaultTableDataModel implements TableDataModel {
    private static final int COLUMN_COUNT = 3;

    // package-local to avoid synthetic accessor method.
    Translator translator;

    /**
     * @param owned
     *            list of group areas
     * @param translator
     */
    public BGAreaTableModel(final List owned, final Translator translator) {
        super(owned);
        this.translator = translator;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final BGArea area = (BGArea) objects.get(row);
        switch (col) {
        case 0:
            return StringEscapeUtils.escapeHtml(area.getName()).toString();
        case 1:
            String description = area.getDescription();
            description = FilterFactory.getHtmlTagsFilter().filter(description);
            description = Formatter.truncate(description, 256);
            return description;
        default:
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return the area at this position
     */
    public BGArea getBGAreaAt(final int row) {
        return (BGArea) objects.get(row);
    }

}
