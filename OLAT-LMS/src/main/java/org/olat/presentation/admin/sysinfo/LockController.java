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

package org.olat.presentation.admin.sysinfo;

import java.util.List;

import org.olat.lms.coordinate.LockingService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
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
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.coordinate.LockEntry;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Controller to manage non persisitent locks. Allow to release certain lock manually. Normally locks will be released in dispose method. A lock must release manually
 * only in case of an error (in dispose or shutdown) otherwise a locks should be released after shutdown.
 * 
 * @author Christian Guretzki
 */

public class LockController extends BasicController {

    private final VelocityContainer myContent;
    private final TableController tableCtr;
    private LockTableModel locksTableModel;
    private DialogBoxController dialogController;

    LockEntry lockToRelease;

    /**
     * Controls locks in admin view.
     * 
     * @param ureq
     * @param wControl
     */
    public LockController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        myContent = createVelocityContainer("locks");

        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setDownloadOffered(false);
        tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.key", 0, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.owner", 1, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("lock.aquiretime", 2, null, ureq.getLocale()));
        tableCtr.addColumnDescriptor(new StaticColumnDescriptor("lock.release", "lock.release", translate("lock.release")));
        listenTo(tableCtr);
        resetTableModel();
        myContent.put("locktable", tableCtr.getInitialComponent());
        putInitialPanel(myContent);
    }

    /**
     * Re-initialize this controller. Fetches sessions again.
     */
    public void resetTableModel() {
        final List<LockEntry> locks = getLockingService().adminOnlyGetLockEntries();
        locksTableModel = new LockTableModel(locks);
        tableCtr.setTableDataModel(locksTableModel);
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                lockToRelease = (LockEntry) locksTableModel.getObject(te.getRowId());
                dialogController = activateYesNoDialog(ureq, null, translate("lock.release.sure"), dialogController);

            }
        } else if (source == dialogController) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                getLockingService().releaseLockEntry(lockToRelease);
                lockToRelease = null;
                resetTableModel();
            } else {
                lockToRelease = null;
            }
        }

    }

    @Override
    protected void doDispose() {
        // DialogBoxController and TableController get disposed by BasicController
        locksTableModel = null;
    }
}
