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

import org.olat.lms.admin.sysinfo.MaintenanceMsgManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.layout.fullWebApp.util.GlobalStickyMessage;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * edit sysinfo messages which are displayed systemwide on every page or on the login page
 * <P>
 * Initial Date: 16.12.2008 <br>
 * 
 * @author guido
 */
public class MaintenanceMessageControllerSingleVM extends BasicController {

    private final Link loginMaintenanceMsgEditButton, maintenanceMsgEditButton;
    private final VelocityContainer maintenanceMsgView, maintenanceMsgEdit;
    private final MaintenanceMsgForm loginMaintenanceMsgForm, maintenanceMsgForm;
    private final Panel container;

    /**
     * @param ureq
     * @param control
     */
    public MaintenanceMessageControllerSingleVM(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        container = new Panel("container");
        maintenanceMsgView = createVelocityContainer("maintenance_msg");
        maintenanceMsgEdit = createVelocityContainer("maintenance_msgEdit");
        maintenanceMsgView.contextPut("cluster", Boolean.FALSE);
        maintenanceMsgEdit.contextPut("cluster", Boolean.FALSE);

        loginMaintenanceMsgEditButton = LinkFactory.createButton("infomsgEdit", maintenanceMsgView, this);
        maintenanceMsgEditButton = LinkFactory.createButton("maintenancemsgEdit", maintenanceMsgView, this);

        // info message stuff
        final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
        final String infoMsg = mrg.getMaintenanceMessage();
        if (infoMsg != null && infoMsg.length() > 0) {
            maintenanceMsgView.contextPut("infomsg", infoMsg);
        }
        loginMaintenanceMsgForm = new MaintenanceMsgForm(ureq, control, infoMsg);
        listenTo(loginMaintenanceMsgForm);

        maintenanceMsgEdit.put("infoMsgForm", loginMaintenanceMsgForm.getInitialComponent());

        // maintenance message stuff
        final String maintenanceMsg = GlobalStickyMessage.getGlobalStickyMessage(true);
        if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
            maintenanceMsgView.contextPut("maintenanceMsgAllNodes", maintenanceMsg);
        }
        maintenanceMsgForm = new MaintenanceMsgForm(ureq, control, maintenanceMsg);
        listenTo(maintenanceMsgForm);
        maintenanceMsgEdit.put("maintenanceMsgForm", maintenanceMsgForm.getInitialComponent());

        container.setContent(maintenanceMsgView);

        putInitialPanel(container);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == loginMaintenanceMsgEditButton) {
            maintenanceMsgEdit.contextPut("infoEdit", Boolean.TRUE);
            maintenanceMsgEdit.contextPut("cluster", Boolean.FALSE);
            container.pushContent(maintenanceMsgEdit);
        } else if (source == maintenanceMsgEditButton) {
            maintenanceMsgEdit.contextPut("infoEdit", Boolean.FALSE);
            maintenanceMsgEdit.contextPut("cluster", Boolean.FALSE);
            container.pushContent(maintenanceMsgEdit);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == loginMaintenanceMsgForm && event == Event.DONE_EVENT) {
            final String infoMsg = loginMaintenanceMsgForm.getInfoMsg();
            final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
            mrg.setMaintenanceMessage(infoMsg);
            if (infoMsg != null && infoMsg.length() > 0) {
                maintenanceMsgView.contextPut("infomsg", infoMsg);
                getWindowControl().setInfo("New info message activated.");
            } else {
                maintenanceMsgView.contextRemove("infomsg");
            }
            container.popContent();
        } else if (source == maintenanceMsgForm && event == Event.DONE_EVENT) {
            final String maintenanceMsg = maintenanceMsgForm.getInfoMsg();
            GlobalStickyMessage.setGlobalStickyMessage(maintenanceMsg, true);
            if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
                maintenanceMsgView.contextPut("maintenanceMsgAllNodes", maintenanceMsg);
                getWindowControl().setInfo("New maintenance message activated.");
            } else {
                maintenanceMsgView.contextRemove("maintenanceMsgAllNodes");
            }
            container.popContent();
        }

        if (event == Event.CANCELLED_EVENT && (source == loginMaintenanceMsgForm || source == maintenanceMsgForm)) {
            container.popContent();
        }

    }

    protected VelocityContainer getViewContainer() {
        return maintenanceMsgView;
    }

    protected VelocityContainer getEditContainer() {
        return maintenanceMsgEdit;
    }

    protected Panel getMainContainer() {
        return container;
    }

    protected MaintenanceMsgForm getMaintenanceMsgForm() {
        return maintenanceMsgForm;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
