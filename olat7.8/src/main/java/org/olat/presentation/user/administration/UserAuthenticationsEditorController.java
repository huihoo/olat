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

package org.olat.presentation.user.administration;

import java.util.List;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Aug 27, 2004
 * 
 * @author Mike Stock
 */
public class UserAuthenticationsEditorController extends BasicController {
    private final TableController tableCtr;
    private AuthenticationsTableDataModel authTableModel;
    private DialogBoxController confirmationDialog;
    private final Identity changeableIdentity;

    /**
     * @param ureq
     * @param wControl
     * @param changeableIdentity
     */
    public UserAuthenticationsEditorController(final UserRequest ureq, final WindowControl wControl, final Identity changeableIdentity) {
        super(ureq, wControl);

        this.changeableIdentity = changeableIdentity;

        // init main view container as initial component
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.provider", 0, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.login", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.credential", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.action", translate("delete")));
        authTableModel = new AuthenticationsTableDataModel(getAuthentications(changeableIdentity));
        tableCtr.setTableDataModel(authTableModel);
        listenTo(tableCtr);

        putInitialPanel(tableCtr.getInitialComponent());
    }

    /**
     * @param identity
     * @return
     */
    private List<Authentication> getAuthentications(final Identity identity) {
        return getBaseSecurity().getAuthentications(identity);
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events to catch
    }

    /**
     * Rebuild the authentications table data model
     */
    public void rebuildAuthenticationsTableDataModel() {
        authTableModel = new AuthenticationsTableDataModel(getAuthentications(changeableIdentity));
        tableCtr.setTableDataModel(authTableModel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == confirmationDialog) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                final Authentication auth = (Authentication) confirmationDialog.getUserObject();
                getBaseSecurity().deleteAuthentication(auth);
                showInfo("authedit.delete.success", new String[] { auth.getProvider(), changeableIdentity.getName() });
                authTableModel.setObjects(getAuthentications(changeableIdentity));
                tableCtr.modelChanged();
            }
        } else if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                if (actionid.equals("delete")) {
                    final int rowid = te.getRowId();
                    final Authentication auth = (Authentication) authTableModel.getObject(rowid);
                    // trusted text, no need to escape
                    confirmationDialog = activateYesNoDialog(ureq, null,
                            getTranslator().translate("authedit.delete.confirm", new String[] { auth.getProvider(), changeableIdentity.getName() }), confirmationDialog);
                    confirmationDialog.setUserObject(auth);
                    return;
                }
            }
        }

    }

    /**
	 */
    @Override
    protected void doDispose() {
        // DialogBoxController and TableController get disposed by BasicController
    }

    /**
	 * 
	 */
    class AuthenticationsTableDataModel extends DefaultTableDataModel {

        /**
         * @param objects
         */
        public AuthenticationsTableDataModel(final List objects) {
            super(objects);
        }

        /**
		 */
        @Override
        public final Object getValueAt(final int row, final int col) {
            final Authentication auth = (Authentication) getObject(row);
            switch (col) {
            case 0:
                return auth.getProvider();
            case 1:
                return auth.getAuthusername();
            case 2: {
                if (auth.getNewCredential() != null) {
                    return auth.getNewCredential();
                }
                return auth.getCredential();
            }

            default:
                return "error";
            }
        }

        /**
		 */
        @Override
        public int getColumnCount() {
            return 3;
        }

    }

}
