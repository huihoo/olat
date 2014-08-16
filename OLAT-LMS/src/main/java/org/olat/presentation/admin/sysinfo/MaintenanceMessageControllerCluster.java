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
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.layout.fullWebApp.util.GlobalStickyMessage;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Provides a maintenace message which is visible only on one node in the cluster
 * <P>
 * Initial Date: 16.12.2008 <br>
 * 
 * @author guido
 */
public class MaintenanceMessageControllerCluster extends MaintenanceMessageControllerSingleVM {

    private final Link loginMaintenanceMsgEditButtonCluster, maintenanceMsgEditButtonCluster;
    private final MaintenanceMsgForm loginMaintenanceMsgFormCluster, maintenanceMsgFormCluster;

    /**
     * @param ureq
     * @param control
     */
    public MaintenanceMessageControllerCluster(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);

        getViewContainer().contextPut("cluster", Boolean.TRUE);

        loginMaintenanceMsgEditButtonCluster = LinkFactory.createButton("infomsgEditCluster", getViewContainer(), this);
        maintenanceMsgEditButtonCluster = LinkFactory.createButton("maintenancemsgEditCluster", getViewContainer(), this);

        // login message stuff (login page on all cluster-nodes)
        final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
        final String infoMsg = mrg.getInfoMessageNodeOnly();
        if (infoMsg != null && infoMsg.length() > 0) {
            getViewContainer().contextPut("infoMsgCluster", infoMsg);
        }
        loginMaintenanceMsgFormCluster = new MaintenanceMsgForm(ureq, control, infoMsg);
        listenTo(loginMaintenanceMsgFormCluster);
        getEditContainer().put("infoMsgFormCluster", loginMaintenanceMsgFormCluster.getInitialComponent());

        // maintenance message stuff (all pages on all cluster-nodes)
        final String maintenanceMsg = GlobalStickyMessage.getGlobalStickyMessage(false);
        if (maintenanceMsg != null && maintenanceMsg.length() > 0) {
            getViewContainer().contextPut("maintenanceMsgThisNodeOnly", maintenanceMsg);
        }
        maintenanceMsgFormCluster = new MaintenanceMsgForm(ureq, control, maintenanceMsg);
        listenTo(maintenanceMsgFormCluster);
        getEditContainer().put("maintenanceMsgFormCluster", maintenanceMsgFormCluster.getInitialComponent());

        setTranslator(super.getTranslator());
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        super.event(ureq, source, event);

        if (source == loginMaintenanceMsgEditButtonCluster) {
            getEditContainer().contextPut("infoEdit", Boolean.TRUE);
            getEditContainer().contextPut("cluster", Boolean.TRUE);
            getMainContainer().pushContent(getEditContainer());
        } else if (source == maintenanceMsgEditButtonCluster) {
            getEditContainer().contextPut("infoEdit", Boolean.FALSE);
            getEditContainer().contextPut("cluster", Boolean.TRUE);
            getMainContainer().pushContent(getEditContainer());
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        super.event(ureq, source, event);

        if (source == loginMaintenanceMsgFormCluster && event == Event.DONE_EVENT) {
            final String infoMsg = loginMaintenanceMsgFormCluster.getInfoMsg();
            final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
            mrg.setInfoMessageNodeOnly(infoMsg);
            if (infoMsg != null && infoMsg.length() > 0) {
                getViewContainer().contextPut("infoMsgCluster", infoMsg);
                getWindowControl().setInfo("New info message activated. Only on this node!");
            } else {
                getViewContainer().contextRemove("infoMsgCluster");
            }
            getMainContainer().popContent();
        } else if (source == maintenanceMsgFormCluster && event == Event.DONE_EVENT) {
            final String infoMsg = maintenanceMsgFormCluster.getInfoMsg();
            GlobalStickyMessage.setGlobalStickyMessage(infoMsg, false);
            if (infoMsg != null && infoMsg.length() > 0) {
                getViewContainer().contextPut("maintenanceMsgThisNodeOnly", infoMsg);
                getWindowControl().setInfo("New maintenance message activated. Only on this node!");
                getMaintenanceMsgForm().reset();
            } else {
                getViewContainer().contextRemove("maintenanceMsgThisNodeOnly");
            }
            getMainContainer().popContent();

        }

        if (event == Event.CANCELLED_EVENT && (source == loginMaintenanceMsgFormCluster || source == maintenanceMsgFormCluster)) {
            getMainContainer().popContent();
        }

    }

}
