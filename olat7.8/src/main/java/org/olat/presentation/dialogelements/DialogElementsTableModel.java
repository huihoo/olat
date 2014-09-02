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

package org.olat.presentation.dialogelements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.DialogNodeForumCallback;
import org.olat.lms.dialogelements.DialogElement;
import org.olat.presentation.course.nodes.dialog.DialogConfigForm;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.HrefGenerator;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Description:<br>
 * Table model for run mode of course node "file dialog"
 * <P>
 * Initial Date: 08.11.2005 <br>
 * 
 * @author guido
 */
public class DialogElementsTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {

    private static final int COLUMN_COUNT = 9;
    private List entries = new ArrayList();
    protected Translator translator;
    private final DialogNodeForumCallback callback;
    private final ModuleConfiguration config;

    /**
     * @param translator
     */
    public DialogElementsTableModel(final Translator translator, final DialogNodeForumCallback callback, final ModuleConfiguration config) {
        this.translator = translator;
        this.callback = callback;
        this.config = config;
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

    /**
     * @param num
     * @return
     */
    public DialogElement getEntryAt(final int num) {
        return (DialogElement) this.entries.get(num);
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final DialogElement entry = getEntryAt(row);
        switch (col) {
        case 0:
            if (entry.getFilename().length() > 30) {
                return entry.getFilename().substring(0, 30) + "...";
            }
            return entry.getFilename();
        case 1:
            return entry.getAuthor();
        case 2:
            return entry.getFileSize();
        case 3:
            return entry.getDate();
        case 4:
            return entry.getNewMessages();
        case 5:
            return entry.getMessagesCount();
        default:
            return "ERROR";
        }
    }

    public void addColumnDescriptors(final TableController tableCtr) {
        final Locale loc = translator.getLocale();
        if (callback != null) {
            final DefaultColumnDescriptor coldesc = new DefaultColumnDescriptor("table.header.filename", 0, DialogElementsController.ACTION_SHOW_FILE, loc);
            coldesc.setHrefGenerator(new HrefGenerator() {
                @Override
                public String generate(final int row, final String href) {
                    final DialogElement entry = getEntryAt(row);
                    return "javascript:o_openPopUp('" + href + entry.getFilename() + "','fileview','600','700','no')";
                }
            });
            // coldesc.setIsPopUpWindowAction(true,
            // DefaultColumnDescriptor.DEFAULT_POPUP_ATTRIBUTES);
            tableCtr.addColumnDescriptor(coldesc);
        } else {
            tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.filename", 0, null, loc));
        }
        if (callback != null) {
            final StaticColumnDescriptor statColdesc = new StaticColumnDescriptor(DialogElementsController.ACTION_START_FORUM, "table.header.forum",
                    translator.translate("dialog.start"));
            // if configured open forum as popup
            final String integration = (String) config.get(DialogConfigForm.DIALOG_CONFIG_INTEGRATION);
            if (integration.equals(DialogConfigForm.CONFIG_INTEGRATION_VALUE_POPUP)) {
                statColdesc.setIsPopUpWindowAction(true, DefaultColumnDescriptor.DEFAULT_POPUP_ATTRIBUTES);
            }
            tableCtr.addColumnDescriptor(statColdesc);
        }
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 1, null, loc));
        tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.size", 2, null, loc));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", 3, null, loc));
        if (callback != null) {
            tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.newmessages", 4, null, loc));
        }
        if (callback != null) {
            tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.messages", 5, null, loc));
        }
        // callback is null in course editor environement where we dont need
        // security calls
        if (callback != null && callback.mayDeleteMessageAsModerator()) {
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor(DialogElementsController.ACTION_DELETE_ELEMENT, "table.header.action", translator.translate("delete")));
        }
    }

    /**
     * Set entries to be represented by this table model.
     * 
     * @param entries
     */
    public void setEntries(final List entries) {
        this.entries = entries;
    }

}
