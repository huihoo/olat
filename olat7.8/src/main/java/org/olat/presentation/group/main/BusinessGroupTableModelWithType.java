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

package org.olat.presentation.group.main;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author gnaegi
 */
public class BusinessGroupTableModelWithType extends DefaultTableDataModel implements TableDataModel {
    private static final int COLUMN_COUNT = 5;
    private final Translator trans;

    /**
     * @param owned
     *            list of business groups
     */
    public BusinessGroupTableModelWithType(final List owned, final Translator trans) {
        super(owned);
        this.trans = trans;
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
        final Object[] wrapped = (Object[]) objects.get(row);
        ;
        final BusinessGroup businessGroup = (BusinessGroup) wrapped[0];

        switch (col) {
        case 0:
            return (businessGroup.getType().equalsIgnoreCase(BusinessGroup.TYPE_LEARNINGROUP)) ? getResourceDisplayName(businessGroup) : StringUtils.EMPTY;
        case 1:
            String name = businessGroup.getName();
            name = StringEscapeUtils.escapeHtml(name).toString();
            return name;
        case 2:
            String description = businessGroup.getDescription();
            description = FilterFactory.getHtmlTagsFilter().filter(description);
            description = Formatter.truncate(description, 256);
            return description;
        case 3:
            return trans.translate(businessGroup.getType());
        case 4:
            return wrapped[1];
        case 5:
            return wrapped[2];
        default:
            return "ERROR";
        }
    }

    /**
     * @param owned
     */
    public void setEntries(final List owned) {
        this.objects = owned;
    }

    /**
     * @param row
     * @return the business group at the given row
     */
    public BusinessGroup getBusinessGroupAt(final int row) {
        final Object[] wrapped = (Object[]) objects.get(row);
        return (BusinessGroup) wrapped[0];
    }

    private String getResourceDisplayName(BusinessGroup businessGroup) {
        final List<RepositoryEntry> repoTableModelEntries = getBgContextService().findRepositoryEntriesForBGContext(businessGroup.getGroupContext());
        return (!repoTableModelEntries.isEmpty()) ? repoTableModelEntries.get(0).getDisplayname() : StringUtils.EMPTY;
    }

    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

}
