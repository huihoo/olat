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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.notification;

import org.apache.log4j.Logger;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.core.notification.service.NotificationSubscriptionContext;
import org.olat.lms.core.notification.service.PublisherData;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * Controller having a subscribe / unsubscribe button for notifications depending on users state
 * 
 * @author Felix Jost
 */
public class ContextualSubscriptionController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private VelocityContainer myContent;
    private Link subscribeButton;
    private Link unsubscribeButton;
    private Panel allPanel;
    private Panel detailsPanel;
    private NotificationLearnService newNotificationService;
    private NotificationSubscriptionContext newSubscriptionContext;
    private boolean isSubscribed;

    /**
     * @param ureq
     * @param subscriptionContext
     * @param publisherData
     */
    public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl, SubscriptionContext subscriptionContext, PublisherData publisherData) {
        super(ureq, wControl);

        myContent = createVelocityContainer("consubs");

        if (subscriptionContext == null) {
            // TODO: this will throw AssertException in BasicController
            setInitialComponent(new Panel("empty:nosubscription"));
            return;
        }

        detailsPanel = new Panel("subscription_detail");
        allPanel = new Panel("subscription_all");

        subscribeButton = LinkFactory.createButtonSmall("command.subscribe", myContent, this);
        // subscribeButton.setCustomEnabledLinkCSS("b_noti_subscribe_link");

        this.unsubscribeButton = LinkFactory.createButtonSmall("command.unsubscribe", myContent, this);
        // unsubscribeButton.setCustomEnabledLinkCSS("b_noti_unsubscribe_link");

        log.info("Notification-Service: " + publisherData.getType() + " is running with NEW notification");
        newNotificationService = getService(LearnServices.notificationLearnService);
        subscriptionContext.setContextId(getRepositoryService().getRepositoryEntryIdFromResourceable(subscriptionContext.getResId(), subscriptionContext.getResName())); // TODO:
                                                                                                                                                                         // LD:
                                                                                                                                                                         // move
                                                                                                                                                                         // this
                                                                                                                                                                         // at
                                                                                                                                                                         // construction
                                                                                                                                                                         // of
                                                                                                                                                                         // subscriptionContext
        newSubscriptionContext = newNotificationService.createNotificationSubscriptionContext(ureq.getIdentity(), subscriptionContext, publisherData);
        isSubscribed = newNotificationService.isSubscribed(newSubscriptionContext);

        updateUI();
        myContent.put("detailsPanel", detailsPanel);
        allPanel.setContent(myContent);
        putInitialPanel(allPanel);
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryServiceImpl.class);
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    private void updateUI() {
        myContent.contextPut("subscribed", (isSubscribed ? Boolean.TRUE : Boolean.FALSE));
    }

    /**
	 */
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == subscribeButton) {
            subscribeToNewNotificationService();

            isSubscribed = true;
            updateUI();
            fireEvent(ureq, event);
        } else if (source == unsubscribeButton) {
            unsubscribeFromNewNotificationService();

            isSubscribed = false;
            updateUI();
            fireEvent(ureq, event);
        }
    }

    private void unsubscribeFromNewNotificationService() {
        try {
            newNotificationService.unSubscribe(newSubscriptionContext);
        } catch (RuntimeException e) {
            log.error("unSubscribe failed: ", e);
        }
    }

    private void subscribeToNewNotificationService() {
        try {
            newNotificationService.subscribe(newSubscriptionContext);
        } catch (RuntimeException e) {
            log.error("subscribe failed: ", e);
        }
    }

    @Override
    protected void doDispose() {
        // nothing to do
    }

}
