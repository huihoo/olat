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

import org.apache.log4j.Logger;
import org.olat.data.notification.Subscriber;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 09.03.2012 <br>
 * 
 * @author Christian Guretzki
 */
public class NotificationSettingsFormController extends FormBasicController {
    private static final Logger log = LoggerHelper.getLogger();
    private MultipleSelectionElement emailDailyCheckbox;
    private boolean currentEmailDailyValue;
    private NotificationLearnService notificationLearnService;

    private final static String[] keys = new String[] { "settings.enabled.yes" };
    private final static String[] values = new String[] { "" };

    public NotificationSettingsFormController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        notificationLearnService = getService(LearnServices.notificationLearnService);
        currentEmailDailyValue = notificationLearnService.isNotificationIntervalFor(ureq.getIdentity(), Subscriber.NotificationInterval.DAILY);
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        emailDailyCheckbox = uifactory.addCheckboxesHorizontal("settings.checkbox.email.daily", formLayout, keys, values, null);
        emailDailyCheckbox.select(keys[0], currentEmailDailyValue);
        emailDailyCheckbox.addActionListener(this, FormEvent.ONCLICK);
    }

    @Override
    @SuppressWarnings("unused")
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == emailDailyCheckbox) {
            if (emailDailyCheckbox.isSelected(0)) {
                notificationLearnService.setNotificationIntervalFor(ureq.getIdentity(), Subscriber.NotificationInterval.DAILY);
            } else {
                notificationLearnService.setNotificationIntervalFor(ureq.getIdentity(), Subscriber.NotificationInterval.NEVER);
            }

        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
