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

package org.olat.presentation.repository.delete;

import java.util.List;

import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.delete.RepositoryDeletionManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
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
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.repository.RepositoryEntryTypeColumnDescriptor;
import org.olat.presentation.repository.RepositoryMainController;
import org.olat.system.event.Event;

/**
 * Controller for tab 'Learning-resource selection'
 * 
 * @author Christian Guretzki
 */
public class SelectionController extends BasicController {

    private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
    private static final String ACTION_MULTISELECT_CHOOSE = "msc";

    private final VelocityContainer myContent;
    private final Panel deleteSelectionPanel;
    private SelectionForm selectionForm;
    private TableController tableCtr;
    private RepositoryEntryDeleteTableModel redtm;
    private VelocityContainer selectionListContent;
    private Link editParameterLink;
    private List<RepositoryEntry> selectedRepositoryEntries;
    private CloseableModalController cmc;

    /**
     * @param ureq
     * @param wControl
     * @param cancelbutton
     */
    public SelectionController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        this.setTranslator(PackageUtil.createPackageTranslator(this.getClass(), ureq.getLocale(),
                PackageUtil.createPackageTranslator(RepositoryMainController.class, ureq.getLocale())));
        myContent = createVelocityContainer("panel");
        deleteSelectionPanel = new Panel("deleteSelectionPanel");
        deleteSelectionPanel.addListener(this);
        myContent.put("panel", deleteSelectionPanel);
        initializeTableController(ureq);
        initializeContent();

        putInitialPanel(myContent);
    }

    private void initializeContent() {
        updateRepositoryEntryList();
        selectionListContent = createVelocityContainer("selectionlist");
        selectionListContent.put("repositorylist", tableCtr.getInitialComponent());
        selectionListContent.contextPut("header",
                translate("selection.delete.header", new String[] { Integer.toString(RepositoryDeletionManager.getInstance().getLastUsageDuration()) }));
        editParameterLink = LinkFactory.createButtonXSmall("button.editParameter", selectionListContent, this);
        deleteSelectionPanel.setContent(selectionListContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == editParameterLink) {

            removeAsListenerAndDispose(selectionForm);
            selectionForm = new SelectionForm(ureq, getWindowControl());
            listenTo(selectionForm);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), selectionForm.getInitialComponent());
            listenTo(cmc);

            cmc.activate();
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
                    RepositoryServiceImpl.getInstance().setLastUsageNowFor((RepositoryEntry) redtm.getObject(rowid));
                    updateRepositoryEntryList();
                }
            } else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
                final TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
                if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
                    handleEmailButtonEvent(ureq, tmse);
                }
            }
            initializeContent();
        } else if (source == selectionForm) {
            if (event == Event.DONE_EVENT) {
                RepositoryDeletionManager.getInstance().setLastUsageDuration(selectionForm.getLastUsageDuration());
                RepositoryDeletionManager.getInstance().setDeleteEmailDuration(selectionForm.getDeleteEmailDuration());
                initializeContent();
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, event);
            }
            cmc.deactivate();
        }
    }

    private void handleEmailButtonEvent(final UserRequest ureq, final TableMultiSelectEvent tmse) {
        if (redtm.getObjects(tmse.getSelection()).size() != 0) {
            selectedRepositoryEntries = redtm.getObjects(tmse.getSelection());
            RepositoryDeletionManager.getInstance().sendDeleteEmailTo(selectedRepositoryEntries, ureq.getIdentity());
            updateRepositoryEntryList();
        } else {
            showWarning("nothing.selected.msg");
        }
    }

    private void initializeTableController(final UserRequest ureq) {
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("error.no.repository.found"));

        removeAsListenerAndDispose(tableCtr);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(tableCtr);

        tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 0, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastusage", 3, null, ureq.getLocale()));
        tableCtr.addMultiSelectAction("action.delete.selection", ACTION_MULTISELECT_CHOOSE);
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent.getTranslator().translate("action.activate")));
        tableCtr.setMultiSelect(true);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    public void updateRepositoryEntryList() {
        final List<RepositoryEntry> l = RepositoryDeletionManager.getInstance().getDeletableRepositoryEntries(
                RepositoryDeletionManager.getInstance().getLastUsageDuration());
        redtm = new RepositoryEntryDeleteTableModel(l);
        tableCtr.setTableDataModel(redtm);
    }

}

class SelectionForm extends FormBasicController {

    private IntegerElement lastUsageDuration;
    private IntegerElement emailDuration;

    /**
     * @param name
     * @param cancelbutton
     * @param isAdmin
     *            if true, no field must be filled in at all, otherwise validation takes place
     */
    public SelectionForm(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
    }

    public int getDeleteEmailDuration() {
        return emailDuration.getIntValue();
    }

    public int getLastUsageDuration() {
        return lastUsageDuration.getIntValue();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);

    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        lastUsageDuration = uifactory.addIntegerElement("lastUsageDuration", "edit.parameter.form.lastusage.duration", RepositoryDeletionManager.getInstance()
                .getLastUsageDuration(), formLayout);
        emailDuration = uifactory.addIntegerElement("emailDuration", "edit.parameter.form.email.duration", RepositoryDeletionManager.getInstance()
                .getDeleteEmailDuration(), formLayout);

        lastUsageDuration.setMinValueCheck(1, null);
        emailDuration.setMinValueCheck(1, null);

        lastUsageDuration.setDisplaySize(3);
        emailDuration.setDisplaySize(3);

        final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);

        uifactory.addFormSubmitButton("submit", "edit.parameter.form.submit", buttonGroupLayout);
        uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }
}
