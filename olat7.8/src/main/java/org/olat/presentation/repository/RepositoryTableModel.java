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

package org.olat.presentation.repository;

import java.util.ArrayList;
import java.util.Locale;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.I18nPackage;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.OutputEscapeType;

/**
 * Initial Date: Mar 31, 2004
 * 
 * @author Mike Stock Comment:
 */
public class RepositoryTableModel extends DefaultTableDataModel implements TableDataModel {

    /**
     * Identifies a table selection event (outer-left column)
     */
    public static final String TABLE_ACTION_SELECT_LINK = "rtbSelectLink";

    /**
     * Identifies a table launch event (if clicked on an item in the name column).
     */
    public static final String TABLE_ACTION_SELECT_ENTRY = "rtbSelectEntry";

    private static final int COLUMN_COUNT = 6;
    Translator translator; // package-local to avoid synthetic accessor method.

    /**
     * Default constructor.
     * 
     * @param translator
     */
    public RepositoryTableModel(final Translator translator) {
        super(new ArrayList<RepositoryEntry>());
        this.translator = translator;
    }

    /**
     * @param tableCtr
     * @param selectButtonLabel
     *            Label of action row or null if no action row should be used
     * @param enableDirectLaunch
     */
    public void addColumnDescriptors(final TableController tableCtr, final String selectButtonLabel, final boolean enableDirectLaunch) {

        tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 0, null, translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 1, enableDirectLaunch ? TABLE_ACTION_SELECT_ENTRY : null, translator
                .getLocale(), OutputEscapeType.HTML));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.access", 3, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.date", 4, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastusage", 5, null, translator.getLocale()));
        if (selectButtonLabel != null) {
            final StaticColumnDescriptor desc = new StaticColumnDescriptor(TABLE_ACTION_SELECT_LINK, selectButtonLabel, selectButtonLabel);
            desc.setTranslateHeaderKey(false);
            tableCtr.addColumnDescriptor(desc);
        }
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
        final RepositoryEntry re = (RepositoryEntry) getObject(row);
        switch (col) {
        case 0:
            return re;
        case 1:
            return getDisplayName(re, translator.getLocale());
        case 2:
            return re.getInitialAuthor();
        case 3: {
            switch (re.getAccess()) {
            case RepositoryEntry.ACC_OWNERS:
                return translator.translate("table.header.access.owner");
            case RepositoryEntry.ACC_OWNERS_AUTHORS:
                return translator.translate("table.header.access.author");
            case RepositoryEntry.ACC_USERS:
                return translator.translate("table.header.access.user");
            case RepositoryEntry.ACC_USERS_GUESTS:
                return translator.translate("table.header.access.guest");
            default:
                // OLAT-6272 in case of broken repo entries with no access code
                // return error instead of nothing
                return "ERROR";
            }
        }
        case 4:
            return re.getCreationDate();
        case 5:
            return re.getLastUsage();
        default:
            return "ERROR";
        }
    }

    /**
     * Get displayname of a repository entry. If repository entry a course and is this course closed then add a prefix to the title.
     */
    private String getDisplayName(final RepositoryEntry repositoryEntry, final Locale locale) {
        String displayName = repositoryEntry.getDisplayname();
        if (repositoryEntry != null && RepositoryServiceImpl.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
            final Translator pT = PackageUtil.createPackageTranslator(I18nPackage.REPOSITORY_, locale);
            displayName = "[" + pT.translate("title.prefix.closed") + "] ".concat(displayName);
        }
        return displayName;
    }

    @Override
    public RepositoryTableModel createCopyWithEmptyList() {
        return new RepositoryTableModel(translator);
    }

}
