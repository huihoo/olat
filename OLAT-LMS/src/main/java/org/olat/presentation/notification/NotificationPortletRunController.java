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

import java.util.List;

import org.olat.lms.commons.LearnServices;
import org.olat.lms.core.notification.impl.UriBuilder;
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.date.DateFilter;
import org.olat.system.commons.date.DateUtil;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Run view controller for the notifications list portlet
 * <P>
 * Initial Date: 15.06.2012 <br>
 * 
 * @author Branislav Balaz
 */

public class NotificationPortletRunController extends BasicController {

    private final VelocityContainer notificationsVC;
    private NotificationLearnService notificationLearnService;
    private final String NOTIFICATION_PORTLET = "notificationPortlet";
    private final String NOTIFICATION_PORTLET_DATA = "notificationPortletData";

    public NotificationPortletRunController(final WindowControl wControl, final UserRequest ureq, final Translator trans, final String portletName) {
        super(ureq, wControl);
        notificationLearnService = getService(LearnServices.notificationLearnService);
        this.notificationsVC = this.createVelocityContainer(NOTIFICATION_PORTLET);
        DateFilter dateFilter = DateUtil.getDateFilterFromDDaysBeforeToToday(notificationLearnService.getNumberOfNewsDays());
        List<UserNotificationEventTO> news = notificationLearnService.getNews(ureq.getIdentity(), dateFilter);

        this.notificationsVC.contextPut(
                NOTIFICATION_PORTLET_DATA,
                new NotificationPortletData(getUriBuilder().getUriToNotificationNews(), trans.translate("notification.news.nonews.label"), !news.isEmpty(), trans
                        .translate("notificationPortlet.news.link.title")));

        putInitialPanel(notificationsVC);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {

    }

    @Override
    protected void doDispose() {

    }

    private UriBuilder getUriBuilder() {
        return CoreSpringFactory.getBean(UriBuilder.class);
    }

}
