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
 * Copyright (c) since 2009 by frentix GmbH, www.frentix.com
 * <p>
 */
package org.olat.presentation.portal.repository;

import java.util.List;
import java.util.Locale;

import org.olat.data.repository.RepositoryEntry;
import org.olat.presentation.framework.core.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.presentation.framework.core.control.generic.portal.PortletEntry;

/**
 * Description:<br>
 * table for the repository data: used in Home-portal and manual repository-sorting
 * <P>
 * Initial Date: 06.03.2009 <br>
 * 
 * @author gnaegi, rhaag
 */
public class RepositoryPortletTableDataModel extends PortletDefaultTableDataModel {
    /**
	 */
    public RepositoryPortletTableDataModel(final List<PortletEntry> objects, final Locale locale) {
        super(objects, 3);
        super.setLocale(locale);
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final RepositoryEntry repoEntry = getRepositoryEntry(row);
        switch (col) {
        case 0:
            return repoEntry.getDisplayname();
        case 1:
            return repoEntry.getDescription();
        case 2:
            return repoEntry;
        default:
            return "error";
        }
    }

    public RepositoryEntry getRepositoryEntry(final int row) {
        final PortletEntry<RepositoryEntry> portletEntry = getObject(row);
        return portletEntry.getValue();
    }
}
