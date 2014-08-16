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

package org.olat.presentation.admin.quota;

import org.olat.data.commons.vfs.Quota;
import org.olat.data.commons.vfs.QuotaManager;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * is the controller for
 * 
 * @author Felix Jost
 */
public class QuotaController extends BasicController {

    private final VelocityContainer myContent;
    private final QuotaTableModel quotaTableModel;

    private GenericQuotaEditController quotaEditCtr;
    private final Panel main;
    private final TableController tableCtr;
    private final Link addQuotaButton;

    /**
     * @param ureq
     * @param wControl
     */
    public QuotaController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        if (!getBaseSecurityEBL().isIdentityPermittedOnResourceable(ureq.getIdentity(), OresHelper.lookupType(this.getClass()))) {
            throw new OLATSecurityException("Insufficient permissions to access QuotaController");
        }

        main = new Panel("quotamain");
        myContent = createVelocityContainer("index");
        addQuotaButton = LinkFactory.createButton("qf.new", myContent, this);

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(tableCtr);

        quotaTableModel = new QuotaTableModel();
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.path", 0, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.quota", 1, null, getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.limit", 2, null, getLocale()));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor("qf.edit", "table.action", translate("edit")));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor("qf.del", "table.action", translate("delete")));
        tableCtr.setTableDataModel(quotaTableModel);

        myContent.put("quotatable", tableCtr.getInitialComponent());
        main.setContent(myContent);

        putInitialPanel(main);
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return (BaseSecurityEBL) CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == addQuotaButton) {
            // clean up old controller first
            if (quotaEditCtr != null) {
                removeAsListenerAndDispose(quotaEditCtr);
            }
            // start edit workflow in dedicated quota edit controller
            removeAsListenerAndDispose(quotaEditCtr);
            quotaEditCtr = new GenericQuotaEditController(ureq, getWindowControl(), null);
            listenTo(quotaEditCtr);
            main.setContent(quotaEditCtr.getInitialComponent());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == quotaEditCtr) {
            if (event == Event.CHANGED_EVENT) {
                quotaTableModel.refresh();
                tableCtr.setTableDataModel(quotaTableModel);
            }
            // else cancel event. in any case set content to list
            main.setContent(myContent);
        }

        if (source == tableCtr && event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
            final TableEvent te = (TableEvent) event;
            final Quota q = quotaTableModel.getRowData(te.getRowId());
            if (te.getActionId().equals("qf.edit")) {
                // clean up old controller first
                // start edit workflow in dedicated quota edit controller
                removeAsListenerAndDispose(quotaEditCtr);
                quotaEditCtr = new GenericQuotaEditController(ureq, getWindowControl(), q);
                listenTo(quotaEditCtr);
                main.setContent(quotaEditCtr.getInitialComponent());

            } else if (te.getActionId().equals("qf.del")) {
                // try to delete quota
                final boolean deleted = QuotaManager.getInstance().deleteCustomQuota(q);
                if (deleted) {
                    quotaTableModel.refresh();
                    tableCtr.setTableDataModel(quotaTableModel);
                    showInfo("qf.deleted", q.getPath());
                } else {
                    // default quotas can not be qf.cannot.del.default")deleted
                    showError("qf.cannot.del.default");
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }
}
