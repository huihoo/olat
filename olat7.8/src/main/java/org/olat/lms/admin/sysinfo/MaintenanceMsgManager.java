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
package org.olat.lms.admin.sysinfo;

import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.creator.AutoCreator;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.SyncerCallback;
import org.olat.system.coordinate.SyncerExecutor;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.GenericEventListener;
import org.olat.system.event.MultiUserEvent;

/**
 * Description:<br>
 * Set/get the Info Message property
 * <P>
 * Initial Date: 12.08.2008 <br>
 * 
 * @author guido
 */
public class MaintenanceMsgManager extends BasicManager implements GenericEventListener {

    private static final String INFO_MSG = "InfoMsg";
    private static final String INFO_MSG_NODE_ONLY = "InfoMsgNode-";
    // random long to make sure we create always the same dummy ores
    private static final Long KEY = Long.valueOf(857394857);
    protected static String maintenanceMessage;
    private static String maintenanceMessageNodeOnly;
    private static final OLATResourceable MAINTENANCE_MESSAGE_ORES = OresHelper.createOLATResourceableType(MaintenanceMsgManager.class);
    public static final String EMPTY_MESSAGE = "";
    // identifies a node in the cluster
    private final int nodeId;
    private AutoCreator actionControllerCreator;
    private final CoordinatorManager coordinatorManager;

    /**
     * [used by spring]
     * 
     * @param nodeId
     */
    private MaintenanceMsgManager(final CoordinatorManager coordinatorManager, final int nodeId) {
        this.coordinatorManager = coordinatorManager;
        // it must exist, ensured by LoginModule
        maintenanceMessage = EMPTY_MESSAGE;
        final String currInfoMsg = getInfoMsgProperty(INFO_MSG).getTextValue();
        if (StringHelper.containsNonWhitespace(currInfoMsg)) {
            // set info message on startup OLAT-3539
            maintenanceMessage = currInfoMsg;
        }

        maintenanceMessageNodeOnly = EMPTY_MESSAGE;
        final String currInfoMsgNode = getInfoMsgProperty(INFO_MSG_NODE_ONLY + nodeId).getTextValue();
        if (StringHelper.containsNonWhitespace(currInfoMsgNode)) {
            // set info message on startup OLAT-3539
            maintenanceMessageNodeOnly = currInfoMsgNode;
        }

        coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, MAINTENANCE_MESSAGE_ORES);
        this.nodeId = nodeId;
    }

    /**
     * @return the info message configured in the admin area
     */
    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    /**
     * @param message
     *            The new info message that will show up on the login screen Synchronized to prevent two users creating or updating the info message property at the same
     *            time
     */
    public void setMaintenanceMessage(final String message) { // o_clusterOK synchronized
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(INFO_MSG, KEY);

        coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {

            @Override
            public void execute() {
                final PropertyManager pm = PropertyManager.getInstance();
                PropertyImpl p = pm.findProperty(null, null, null, "_o3_", INFO_MSG);
                if (p == null) {
                    p = pm.createPropertyInstance(null, null, null, "_o3_", INFO_MSG, null, null, null, "");
                    pm.saveProperty(p);
                }
                p.setTextValue(message);
                // set Message in RAM
                MaintenanceMsgManager.maintenanceMessage = message;
                pm.updateProperty(p);
            }

        });// end syncerCallback
        final EventBus eb = coordinatorManager.getCoordinator().getEventBus();
        final MultiUserEvent mue = new MultiUserEvent(message);
        eb.fireEventToListenersOf(mue, MAINTENANCE_MESSAGE_ORES);
    }

    private PropertyImpl getInfoMsgProperty(final String key) {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(INFO_MSG, KEY);

        return coordinatorManager.getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<PropertyImpl>() {

            @Override
            public PropertyImpl execute() {
                final PropertyManager pm = PropertyManager.getInstance();
                PropertyImpl p = pm.findProperty(null, null, null, "_o3_", key);
                if (p == null) {
                    p = pm.createPropertyInstance(null, null, null, "_o3_", key, null, null, null, "");
                    pm.saveProperty(p);
                }
                return p;
            }

        });// end syncerCallback
    }

    @Override
    public void event(final Event event) {
        if (event instanceof MultiUserEvent) {
            final MultiUserEvent mue = (MultiUserEvent) event;
            // do not use setInfoMessage(..) this event comes in from another node, where the infomessage was set.
            MaintenanceMsgManager.maintenanceMessage = mue.getCommand();
        }
    }

    /**
     * set info message on node level only, no need to sync
     * 
     * @param message
     */
    public void setInfoMessageNodeOnly(final String message) {
        final PropertyManager pm = PropertyManager.getInstance();
        PropertyImpl p = pm.findProperty(null, null, null, "_o3_", INFO_MSG_NODE_ONLY + nodeId);
        if (p == null) {
            p = pm.createPropertyInstance(null, null, null, "_o3_", INFO_MSG_NODE_ONLY + nodeId, null, null, null, "");
            pm.saveProperty(p);
        }
        p.setTextValue(message);
        // set Message in RAM
        MaintenanceMsgManager.maintenanceMessageNodeOnly = message;
        pm.updateProperty(p);
    }

    public String getInfoMessageNodeOnly() {
        return maintenanceMessageNodeOnly;
    }

    /**
     * get an controller instance, either the singleVM or the cluster version
     * 
     * @param ureq
     * @param control
     */
    public Controller getMaintenanceMessageController(final UserRequest ureq, final WindowControl control) {
        return actionControllerCreator.createController(ureq, control);
    }

    /**
     * [used by spring]
     */
    public void setActionController(final ControllerCreator actionControllerCreator) {
        this.actionControllerCreator = (AutoCreator) actionControllerCreator;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
