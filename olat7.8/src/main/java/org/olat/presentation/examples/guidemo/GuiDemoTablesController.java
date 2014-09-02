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

package org.olat.presentation.examples.guidemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BaseTableDataModelWithoutFilter;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomCellRenderer;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableDataModel;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.dev.controller.SourceViewController;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.event.Event;

public class GuiDemoTablesController extends BasicController {

    VelocityContainer vcMain;
    TableController table;
    TableDataModel model;

    public GuiDemoTablesController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        vcMain = this.createVelocityContainer("guidemo-table");
        final TableGuiConfiguration tgc = new TableGuiConfiguration();
        tgc.setPreferencesOffered(true, "TableGuiDemoPrefs");
        table = new TableController(tgc, ureq, getWindowControl(), getTranslator());
        listenTo(table);
        table.setMultiSelect(true);
        table.addMultiSelectAction("guidemo.table.submit", "submitAction");
        table.addMultiSelectAction("guidemo.table.submit2", "submitAction2");
        table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header1", 0, null, ureq.getLocale()));
        table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header2", 1, null, ureq.getLocale()));
        table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header3", 2, null, ureq.getLocale()));
        table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header4", 3, null, ureq.getLocale()));
        table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header5", 4, null, ureq.getLocale()));
        table.addColumnDescriptor(new CustomRenderColumnDescriptor("guidemo.table.header6", 5, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_CENTER,
                new ImageCellRenderer()));
        table.addColumnDescriptor(new StaticColumnDescriptor("action.select", "guidemo.table.header7", "Select"));
        model = new SampleTableModel();
        table.setTableDataModel(model);
        vcMain.put("table", table.getInitialComponent());

        // add source view control
        final Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), vcMain);
        vcMain.put("sourceview", sourceview.getInitialComponent());

        this.putInitialPanel(vcMain);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    @Override
    protected void doDispose() {
        // table gets disposed by BasicController because we listenTo(table)
    }

}

class ImageCellRenderer implements CustomCellRenderer {

    @Override
    public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
        sb.append("<img src=\"");
        Renderer.renderStaticURI(sb, "images/olat/olatlogo16x16.png");
        sb.append("\" alt=\"An image within a table...\" />");
    }

}

class SampleTableModel extends BaseTableDataModelWithoutFilter implements TableDataModel {

    private final int COLUMN_COUNT = 7;
    private final List entries;

    public SampleTableModel() {
        final int iEntries = 50;
        this.entries = new ArrayList(iEntries);
        for (int i = 0; i < iEntries; i++) {
            final List row = new ArrayList(5);
            row.add("Lorem" + i);
            row.add("Ipsum" + i);
            row.add("Dolor" + i);
            row.add("Sit" + i);
            row.add(Integer.toString(i));
            row.add("");
            entries.add(row);
        }
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        final List entry = (List) entries.get(row);
        return entry.get(col);
    }

}
