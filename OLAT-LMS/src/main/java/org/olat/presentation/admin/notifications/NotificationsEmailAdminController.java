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
package org.olat.presentation.admin.notifications;

import org.olat.lms.notifications.NotificationHelper;
import org.olat.lms.notifications.NotificationService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;
import org.springframework.scheduling.quartz.CronTriggerBean;

/**
 * Description:<br>
 * Manually trigger sending of notification email which are normally sent only once a day.
 * <P>
 * Initial Date: Dec 19, 2006 <br>
 * 
 * @author guido
 */
@Deprecated
public class NotificationsEmailAdminController extends BasicController {
    private static final String TRIGGER_NOTIFY = "notification.start.button";

    private final VelocityContainer content;
    private final Link startNotifyButton;

    public NotificationsEmailAdminController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        content = createVelocityContainer("index");
        boolean enabled;
        String cronExpression = "";
        try {
            CoreSpringFactory.getBean("notifications.job.enabled");
            enabled = true;
            final CronTriggerBean bean = (CronTriggerBean) CoreSpringFactory.getBean("sendNotificationsEmailTrigger");
            cronExpression = bean.getCronExpression();
        } catch (final Exception e) {
            enabled = false;
        }
        content.contextPut("status", getTranslator().translate("notification.status", new String[] { String.valueOf(enabled), cronExpression }));
        startNotifyButton = LinkFactory.createButton(TRIGGER_NOTIFY, content, this);
        putInitialPanel(content);
    }

    /**
	 */
    @Override
    @SuppressWarnings("unused")
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == startNotifyButton) {
            getNotificationService().notifyAllSubscribersByEmail(translate("rss.title", new String[] { NotificationHelper.getFormatedName(getIdentity()) }));
        }
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do
    }

}
