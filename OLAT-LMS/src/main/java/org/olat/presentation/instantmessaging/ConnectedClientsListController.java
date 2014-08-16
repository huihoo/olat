/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.instantmessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.lms.instantmessaging.ConnectedUsersListEntry;
import org.olat.lms.instantmessaging.InstantMessaging;
import org.olat.lms.instantmessaging.InstantMessagingClient;
import org.olat.lms.instantmessaging.InstantMessagingConstants;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.instantmessaging.rosterandchat.ChatManagerController;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.event.EventBus;
import org.olat.system.event.MultiUserEvent;

/**
 * Controller for a user list, where all users are shown together with real name and last status message. What is visible or not is controlled by the users preferences
 * settings.<br />
 * 
 * @author Guido Schnider Initial Date: 15.08.2004 <br />
 */

public class ConnectedClientsListController extends BasicController {
    private final VelocityContainer content = createVelocityContainer("connectedclientslist");
    private final Link refreshButton;
    private final Locale locale;
    private final Formatter f;
    private final Map<String, String> lastActivity = new HashMap<String, String>();
    private final List<ConnectedUsersListEntry> entries = new ArrayList<ConnectedUsersListEntry>();

    private TableController tableCtr;
    private ConnectedUsersTableModel tableModel;
    private final ChatManagerController chatMgrCtrl;
    private final InstantMessaging im = InstantMessagingModule.getAdapter();

    private List allChats;
    private final EventBus singleUserEventCenter;

    /**
     * @param ureq
     * @param wControl
     */
    public ConnectedClientsListController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // this controller gets rendered in a new window, we have to make sure
        // polling works also in this window
        final JSAndCSSComponent jsComp = new JSAndCSSComponent("pollintervall", this.getClass(), null, null, false, null, InstantMessagingModule.getIDLE_POLLTIME());
        content.put("polling", jsComp);
        locale = ureq.getLocale();
        f = Formatter.getInstance(locale);
        refreshButton = LinkFactory.createButtonSmall("command.refresh", content, this);
        updateUI(ureq, true);
        chatMgrCtrl = new ChatManagerController(ureq, wControl);
        content.put("chats", chatMgrCtrl.getInitialComponent());
        listenTo(chatMgrCtrl);

        this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
        allChats = (List) ureq.getUserSession().getEntry("chats");
        if (allChats == null) {
            allChats = new ArrayList();
            ureq.getUserSession().putEntry("chats", allChats);
        }
        allChats.add(Integer.toString(hashCode()));
        singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("ChatWindowOpened"), OresHelper.createOLATResourceableType(InstantMessaging.class));

        putInitialPanel(content);
    }

    /**
     * rebuild the user list with the latest entries
     */
    private void updateUI(final UserRequest ureq, final boolean init) {
        if (init) {
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            removeAsListenerAndDispose(tableCtr);
            tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
            listenTo(tableCtr);
            tableModel = new ConnectedUsersTableModel(getTranslator(), InstantMessagingModule.isEnabled());
            tableModel.addColumnDescriptors(tableCtr);
            tableCtr.setTableDataModel(tableModel);
            tableCtr.setSortColumn(1, true);
            content.put("usertable", tableCtr.getInitialComponent());
        }

        entries.clear();
        int invisibleUsers = 0;
        final List<ConnectedUsersListEntry> l = im.getAllConnectedUsers(ureq.getIdentity());

        if (l == null) {
            content.contextPut("invisibleUsers", 0);
            return;
        }

        final List<ConnectedUsersListEntry> m = new ArrayList<ConnectedUsersListEntry>();
        for (final Iterator<ConnectedUsersListEntry> it = l.iterator(); it.hasNext();) {
            final ConnectedUsersListEntry entry = it.next();
            if (!entry.isVisible()) {
                invisibleUsers++;
            } else {
                m.add(entry);
            }
        }
        tableModel.setEntries(m);
        tableCtr.modelChanged();
        // TODO:gs TODO get invisible users by looping in GUI
        content.contextPut("invisibleUsers", invisibleUsers);
        content.contextPut("havelist", Boolean.TRUE);

    }

    /**
     * translate the default status messages for the list of logged in users
     * 
     * @param statusMsg
     * @return
     */
    private String translatedDefautStatusMsg(final String statusMsg) {
        if (statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_AVAILABLE)) {
            return translate("presence.available");
        } else if (statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_CHAT)) {
            return translate("presence.chat");
        } else if (statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_AWAY)) {
            return translate("presence.away");
        } else if (statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_XAWAY)) {
            return translate("presence.xa");
        } else if (statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_DND)) {
            return translate("presence.dnd");
        }
        return statusMsg;
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == tableCtr) {
            final InstantMessagingClient client = im.getClientManager().getInstantMessagingClient(ureq.getIdentity().getName());
            boolean chattingAllowed = false;
            if (client != null && !client.isChatDisabled()) {
                chattingAllowed = true;
            }

            if (chattingAllowed) {
                final TableEvent te = (TableEvent) event;
                final int row = te.getRowId();
                final ConnectedUsersListEntry entry = tableModel.getEntryAt(row);
                chatMgrCtrl.createChat(ureq, getWindowControl(), entry.getJabberId());
            } else {
                showInfo("im.chat.forbidden");
            }
        }
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (event.getCommand().equals("close")) {
            doDispose();
            return;
        }
        if (source == refreshButton) {
            updateUI(ureq, false);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        allChats.remove(Integer.toString(hashCode()));
        singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("ChatWindowClosed"), OresHelper.createOLATResourceableType(InstantMessaging.class));
    }

}
