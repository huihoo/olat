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

package org.olat.presentation.group.context;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.context.BGContext;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.group.learn.DefaultContextTranslationHelper;
import org.olat.system.commons.Formatter;

/**
 * Description:<BR>
 * The business group table model contains a list of business groups and can display the business groups
 * <P>
 * Initial Date: Jan 24, 2005
 * 
 * @author gnaegi
 */
public class BGContextTableModel extends DefaultTableDataModel implements TableDataModel {
    private final Translator trans;
    private final boolean showType;
    private final boolean showDefault;

    /**
     * Constructor for the business group table model
     * 
     * @param groupContexts
     *            The list of group contexts
     * @param trans
     * @param showType
     *            true: show type row
     * @param showDefault
     *            true: show isDefaultContext flag
     */
    public BGContextTableModel(final List groupContexts, final Translator trans, final boolean showType, final boolean showDefault) {
        super(groupContexts);
        this.trans = trans;
        this.showType = showType;
        this.showDefault = showDefault;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        int column = 2;
        if (showType) {
            column++;
        }
        if (showDefault) {
            column++;
        }
        return column;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final BGContext context = (BGContext) objects.get(row);
        switch (col) {
        case 0:
            String name = DefaultContextTranslationHelper.translateIfDefaultContextName(context, trans);
            name = StringEscapeUtils.escapeHtml(name).toString();
            return name;
        case 1:
            String description = context.getDescription();
            description = FilterFactory.getHtmlTagsFilter().filter(description);
            description = Formatter.truncate(description, 256);
            return description;
        case 2:
            if (showType) {
                return trans.translate(context.getGroupType());
            } else {
                return new Boolean(context.isDefaultContext());
            }
        case 3:
            return new Boolean(context.isDefaultContext());
        default:
            return "ERROR";
        }
    }

    /**
     * @param row
     * @return BGContext from given row
     */
    public BGContext getGroupContextAt(final int row) {
        return (BGContext) objects.get(row);
    }

}
