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

package org.olat.presentation.group.delete;

import java.util.Date;
import java.util.List;

import org.olat.data.commons.filter.FilterFactory;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.GroupDeletionDao;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Data model for group deletion.
 * 
 * @author Christian Guretzki
 */
public class GroupDeleteTableModel extends DefaultTableDataModel<BusinessGroup> {

    private final Translator translator;

    /**
     * @param objects
     */
    public GroupDeleteTableModel(final List<BusinessGroup> objects, final Translator translator) {
        super(objects);
        this.translator = translator;
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final BusinessGroup businessGroup = getObject(row);
        switch (col) {
        case 0:
            return businessGroup.getName();
        case 1:
            String description = businessGroup.getDescription();
            description = FilterFactory.getHtmlTagsFilter().filter(description);
            return (description == null ? "n/a" : description);
        case 2:
            final String type = businessGroup.getType();
            return (type == null ? "n/a" : translator.translate(type));
        case 3:
            final Date lastUsage = businessGroup.getLastUsage();
            return (lastUsage == null ? "n/a" : lastUsage);
        case 4:
            final Date deleteEmail = LifeCycleManager.createInstanceFor(businessGroup).lookupLifeCycleEntry(GroupDeletionDao.SEND_DELETE_EMAIL_ACTION).getLcTimestamp();
            return (deleteEmail == null ? "n/a" : deleteEmail);
        default:
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 5;
    }
}
