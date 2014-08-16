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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.presentation.notifications;

import java.util.Date;

import org.olat.connectors.rss.RSSUtil;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.home.site.GuestHomeSite;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The subscription and notification controller combines the users subscription management and his personal news into one view.
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationSubscriptionAndNewsController extends BasicController implements Activateable {
    private final Identity subscriberIdentity;
    private final TabbedPane tabbedPane;
    private final Panel subscriptionPanel, rssPanel;
    private NotificationSubscriptionController subscriptionCtr;
    private final NotificationNewsController newsCtr;

    protected NotificationSubscriptionAndNewsController(final Identity subscriberIdentity, final UserRequest ureq, final WindowControl wControl, final Date newsSinceDate) {
        super(ureq, wControl, PackageUtil.createPackageTranslator(GuestHomeSite.class, ureq.getLocale()));
        this.subscriberIdentity = subscriberIdentity;
        tabbedPane = new TabbedPane("tabbedPane", getLocale());
        tabbedPane.addListener(this);

        // Add news view
        newsCtr = NotificationUIFactory.createNewsListingController(subscriberIdentity, ureq, getWindowControl(), newsSinceDate);
        listenTo(newsCtr);
        tabbedPane.addTab(translate("overview.tab.news"), newsCtr.getInitialComponent());
        // Add subscription view, initialize with an empty panel and create real
        // controller only when user clicks the tab
        subscriptionPanel = new Panel("subscriptionPanel");
        tabbedPane.addTab(translate("overview.tab.subscriptions"), subscriptionPanel);
        // Add RSS info page
        rssPanel = new Panel("rssPanel");
        tabbedPane.addTab(translate("overview.tab.rss"), rssPanel);
        //
        putInitialPanel(tabbedPane);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers dispsed by basic controller
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == tabbedPane) {
            if (event instanceof TabbedPaneChangedEvent) {
                final TabbedPaneChangedEvent tabbedEvent = (TabbedPaneChangedEvent) event;
                // Lazy initialize the notification subscription controller when the
                // user clicks the tab the first time
                if (tabbedEvent.getNewComponent() == subscriptionPanel && subscriptionCtr == null) {
                    subscriptionCtr = NotificationUIFactory.createSubscriptionListingController(subscriberIdentity, ureq, getWindowControl());
                    listenTo(subscriptionCtr);
                    subscriptionPanel.setContent(subscriptionCtr.getInitialComponent());
                }
                // Lazy initialize the notification subscription controller when the
                // user clicks the tab the first time
                else if (tabbedEvent.getNewComponent() == rssPanel && rssPanel.getContent() == null) {
                    final VelocityContainer notificationsRssVC = createVelocityContainer("notificationsRSS");
                    final String rssLink = RSSUtil.getPersonalRssLink(ureq);
                    notificationsRssVC.contextPut("rssLink", rssLink);
                    final User user = subscriberIdentity.getUser();
                    final String fullName = getUserService().getFirstAndLastname(user);
                    notificationsRssVC.contextPut("fullName", fullName);
                    rssPanel.setContent(notificationsRssVC);
                }
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == subscriptionCtr) {
            if (event == Event.CHANGED_EVENT) {
                // Reload table model from news controller to reflect change in
                // subscriptions
                newsCtr.updateNewsDataModel();
            }
        }
    }

    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        if (viewIdentifier == null) {
            return;
        }
        if (viewIdentifier.startsWith("[news:0]")) {
            final String selection = viewIdentifier.substring(8);
            newsCtr.activate(ureq, selection);
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
