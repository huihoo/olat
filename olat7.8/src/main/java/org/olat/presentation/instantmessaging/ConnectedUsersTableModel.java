/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.instantmessaging;

import java.util.ArrayList;
import java.util.List;

import org.olat.lms.instantmessaging.ConnectedUsersListEntry;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCssCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description: Table model for the connected users list
 * 
 * @author guido
 */
public class ConnectedUsersTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {
    /**
     * Identifies a table launch event (if clicked on an item in the name, prename or username column).
     */
    public static final String TABLE_ACTION_LAUNCH_CLIENT = "startClient";
    private static final int COLUMN_COUNT = 8;
    private List entries = new ArrayList();
    protected Translator translator;
    private final boolean chatEnabled;

    /**
     * Default constructor.
     * 
     * @param translator
     */
    public ConnectedUsersTableModel(final Translator translator, final boolean chatEnabled) {
        this.translator = translator;
        this.chatEnabled = chatEnabled;
    }

    /**
     * class for rendering an icon inside a table cell
     */
    private class IMStatusIconRenderer extends CustomCssCellRenderer {

        @Override
        protected String getHoverText(final Object val) {
            final String altText = translator.translate("presence." + val.toString());
            return "Instant Messaging: " + altText;
        }

        @Override
        protected String getCellValue(final Object val) {
            return "";
        }

        @Override
        protected String getCssClass(final Object val) {
            // use small icon and create icon class for im status, e.g: o_instantmessaging_available_icon
            return "b_small_icon " + "o_instantmessaging_" + ((String) val).replace(".", "-") + "_icon";
        }
    }

    /**
     * @param tableCtr
     * @param selectButtonLabel
     *            Label of action row or null if no action row should be used
     * @param enableDirectLaunch
     */
    public void addColumnDescriptors(final TableController tableCtr) {
        if (chatEnabled) {
            tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.username", 0, TABLE_ACTION_LAUNCH_CLIENT, translator.getLocale()));
        } else {
            tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.username", 0, null, translator.getLocale()));
        }
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.prename", 1, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.name", 2, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.IMstatus", 3, null, translator.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
                new IMStatusIconRenderer()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.resource", 4, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.awareness", 5, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.onlineTime", 6, null, translator.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastActivity", 7, null, translator.getLocale()));
    }

    /**
     * Set entries to be represented by this table model.
     * 
     * @param entries
     */
    public void setEntries(final List entries) {
        this.entries = entries;
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
    public int getRowCount() {
        return entries.size();
    }

    ConnectedUsersListEntry getEntryAt(final int num) {
        return (ConnectedUsersListEntry) this.entries.get(num);
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final ConnectedUsersListEntry entry = getEntryAt(row);
        switch (col) {
        case 0:
            return entry.getUsername();
        case 1:
            return entry.getPrename();
        case 2:
            return entry.getName();
        case 3:
            return entry.getInstantMessagingStatus();
        case 4:
            return entry.getResource();
        case 5:
            return entry.getAwarenessMessage();
        case 6:
            return entry.getOnlineTime();
        case 7:
            return entry.getLastActivity();

        default:
            return "ERROR";
        }
    }

}
