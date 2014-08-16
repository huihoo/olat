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
package org.olat.presentation.notification;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.system.event.Event;

/**
 * Initial Date: 09.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class NotificationController extends BasicController implements Activateable {

    private final Identity subscriberIdentity;
    private final TabbedPane tabbedPane;
    private final NotificationNewsController notificatioNewsController;
    public static NotificationSettingsController notificationSettingsController;

    public NotificationController(Identity subscriberIdentity, UserRequest ureq, WindowControl wControl, int tabId) {
        super(ureq, wControl);
        this.subscriberIdentity = subscriberIdentity;
        tabbedPane = new TabbedPane("tabbedPane", getLocale());
        tabbedPane.addListener(this);

        notificatioNewsController = new NotificationNewsController(this.subscriberIdentity, ureq, wControl);
        listenTo(notificatioNewsController);
        notificationSettingsController = new NotificationSettingsController(this.subscriberIdentity, ureq, wControl);
        tabbedPane.addTab(translate("notification.news.tab.label"), notificatioNewsController.getInitialComponent());
        tabbedPane.addTab(translate("notification.settings.tab.label"), notificationSettingsController.getInitialComponent());
        tabbedPane.setSelectedPane(tabId);
        putInitialPanel(tabbedPane);
    }

    @Override
    public void activate(UserRequest ureq, String viewIdentifier) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {

    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
