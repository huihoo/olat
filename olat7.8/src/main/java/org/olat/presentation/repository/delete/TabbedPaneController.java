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

import org.olat.lms.coordinate.LockingService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Learning-resource deletion tabbed pane controller.
 * 
 * @author Christian Guretzki
 */
public class TabbedPaneController extends BasicController implements ControllerEventListener {
    // NLS support

    private static final String NLS_ERROR_NOACCESS_TO_USER = "error.noaccess.to.user";

    private VelocityContainer myContent;

    // controllers used in tabbed pane
    private TabbedPane repositoryDeleteTabP;
    private SelectionController selectionCtr;
    private StatusController deleteStatusCtr;
    private ReadyToDeleteController readyToDeleteCtr;

    private LockResult lock;

    /**
     * Constructor that creates a back - link as default
     * 
     * @param ureq
     * @param wControl
     * @param identity
     */
    public TabbedPaneController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        if (ureq.getUserSession().getRoles().isOLATAdmin()) {
            // Acquire lock for hole delete-group workflow
            final OLATResourceable lockResourceable = OresHelper.createOLATResourceableTypeWithoutCheck(this.getClass().getName());
            lock = getLockingService().acquireLock(lockResourceable, ureq.getIdentity(), "deleteGroup");
            if (!lock.isSuccess()) {
                final String text = getTranslator().translate("error.deleteworkflow.locked.by", new String[] { lock.getOwner().getName() });
                final Controller uiInfoMsgCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, null, text);
                listenTo(uiInfoMsgCtrl);// register to let dispose on dispose of this controller
                putInitialPanel(uiInfoMsgCtrl.getInitialComponent());
                return;
            }

            myContent = createVelocityContainer("deleteTabbedPane");
            initTabbedPane(ureq);
            putInitialPanel(myContent);
        } else {
            final String supportAddr = WebappHelper.getMailConfig("mailSupport");
            showWarning(getTranslator().translate(NLS_ERROR_NOACCESS_TO_USER, new String[] { supportAddr }), null);
            putInitialPanel(new Panel("empty"));
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (event.getCommand().equals(TabbedPaneChangedEvent.TAB_CHANGED)) {
            selectionCtr.updateRepositoryEntryList();
            deleteStatusCtr.updateRepositoryEntryList();
            readyToDeleteCtr.updateRepositoryEntryList();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    /**
     * Initialize the tabbed pane according to the users rights and the system configuration
     * 
     * @param identity
     * @param ureq
     */
    private void initTabbedPane(final UserRequest ureq) {
        repositoryDeleteTabP = new TabbedPane("repositoryDeleteTabP", ureq.getLocale());
        repositoryDeleteTabP.addListener(this);

        selectionCtr = new SelectionController(ureq, getWindowControl());
        listenTo(selectionCtr);
        repositoryDeleteTabP.addTab(translate("delete.workflow.tab.start.process"), selectionCtr.getInitialComponent());

        deleteStatusCtr = new StatusController(ureq, getWindowControl());
        listenTo(deleteStatusCtr);
        repositoryDeleteTabP.addTab(translate("delete.workflow.tab.status.email"), deleteStatusCtr.getInitialComponent());

        readyToDeleteCtr = new ReadyToDeleteController(ureq, getWindowControl());
        listenTo(readyToDeleteCtr);
        repositoryDeleteTabP.addTab(translate("delete.workflow.tab.select.delete"), readyToDeleteCtr.getInitialComponent());

        myContent.put("repositoryDeleteTabP", repositoryDeleteTabP);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // childcontrollers are registered with listenTo(..) and are disposed by basiccontroller!
        releaseLock();
    }

    /**
     * Releases the lock for this page if set
     */
    private void releaseLock() {
        if (lock != null) {
            getLockingService().releaseLock(lock);
            lock = null;
        }
    }

}
