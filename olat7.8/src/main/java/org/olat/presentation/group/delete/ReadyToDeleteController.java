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

import java.util.List;

import org.olat.data.group.BusinessGroup;
import org.olat.lms.group.GroupDeletionService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.main.BGMainController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Controller for 'Ready-to-delete' tab.
 * 
 * @author Christian Guretzki
 */
public class ReadyToDeleteController extends BasicController {
    private static final String PACKAGE_BG_MAIN_CONTROLLER = PackageUtil.getPackageName(BGMainController.class);
    private static final String MY_PACKAGE = PackageUtil.getPackageName(ReadyToDeleteController.class);

    private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
    private static final String ACTION_MULTISELECT_CHOOSE = "msc";

    private final VelocityContainer myContent;
    private final Panel readyToDeletePanel;
    private TableController tableCtr;
    private GroupDeleteTableModel redtm;
    private Link feedbackBackLink;
    private List groupsReadyToDelete;
    private DialogBoxController deleteConfirmController;
    private final PackageTranslator tableModelTypeTranslator;
    private GroupDeletionService groupDeletionService;

    /**
     * @param ureq
     * @param wControl
     * @param cancelbutton
     */
    public ReadyToDeleteController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        /*
         * createBGPackageTranslator creates a Translator for Business Groups and the provided package, e.g. ReadyToDelete.class <ol> <li>used to translate the
         * BusinessGroup.getType() String in the table model from businessgroup default package</li> <li>used to translate keys from ReadyToDeleteController provided in
         * the i18n from this package</li> </ol>
         */
        final PackageTranslator fallbackTrans = new PackageTranslator(PACKAGE_BG_MAIN_CONTROLLER, ureq.getLocale());
        this.setTranslator(new PackageTranslator(MY_PACKAGE, ureq.getLocale(), fallbackTrans));
        // used to translate the BusinessGroup.getType() String in the table model
        tableModelTypeTranslator = BGTranslatorFactory.createBGPackageTranslator(PackageUtil.getPackageName(ReadyToDeleteController.class), /* doesnt matter */
                BusinessGroup.TYPE_BUDDYGROUP, ureq.getLocale());

        myContent = createVelocityContainer("panel");

        readyToDeletePanel = new Panel("readyToDeletePanel");
        readyToDeletePanel.addListener(this);
        myContent.put("panel", readyToDeletePanel);

        groupDeletionService = CoreSpringFactory.getBean(GroupDeletionService.class);

        initializeTableController(ureq);

        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == feedbackBackLink) {
            initializeTableController(ureq);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
                    final int rowid = te.getRowId();
                    groupDeletionService.setLastUsageNowFor((BusinessGroup) redtm.getObject(rowid));
                    updateGroupList();
                }
            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
                    handleDeleteGroupButtonEvent(ureq, tmse);
                }
            }
        } else if (source == deleteConfirmController) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                groupDeletionService.deleteGroups(groupsReadyToDelete);
                showInfo("readyToDelete.deleted.msg");
            }
            updateGroupList();
        }
    }

    private void handleDeleteGroupButtonEvent(final UserRequest ureq, final TableMultiSelectEvent tmse) {
        groupsReadyToDelete = redtm.getObjects(tmse.getSelection());
        if (groupsReadyToDelete.size() != 0) {
            deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm"), deleteConfirmController);
            return;
        } else {
            showWarning("nothing.selected.msg");
        }
    }

    private void initializeTableController(final UserRequest ureq) {
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("error.no.repository.found"));
        tableConfig.setShowAllLinkEnabled(false);

        removeAsListenerAndDispose(tableCtr);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(tableCtr);

        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.bgname", 0, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.description", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.type", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastusage", 3, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleteEmail", 4, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent.getTranslator().translate("action.activate")));

        tableCtr.addMultiSelectAction("action.ready.to.delete", ACTION_MULTISELECT_CHOOSE);

        updateGroupList();
        tableCtr.setMultiSelect(true);
        final VelocityContainer readyToDeleteContent = createVelocityContainer("readyToDelete");
        readyToDeleteContent.put("readyToDelete", tableCtr.getInitialComponent());
        readyToDeleteContent.contextPut("header", translate("ready.to.delete.header", Integer.toString(groupDeletionService.getDeleteEmailDuration())));
        readyToDeletePanel.setContent(readyToDeleteContent);

    }

    protected void updateGroupList() {
        final List l = groupDeletionService.getGroupsReadyToDelete(groupDeletionService.getDeleteEmailDuration());
        redtm = new GroupDeleteTableModel(l, tableModelTypeTranslator);
        tableCtr.setTableDataModel(redtm);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
