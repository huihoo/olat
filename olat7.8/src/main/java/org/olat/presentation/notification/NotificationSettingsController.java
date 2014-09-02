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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.LearnServices;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.lms.learn.notification.service.SubscriptionTO;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.table.TableMultiSelectEvent;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 09.03.2012 <br>
 * 
 * @author Christian Guretzki
 */
public class NotificationSettingsController extends BasicController {
    private static final Logger log = LoggerHelper.getLogger();

    private static final int RESULTS_PER_PAGE = 20;

    private final VelocityContainer notificationSettingsVelocityContainer;

    private final TableController notificationSettingsTableController;
    private final NotificationSettingsTableDataModel notificationSettingsTableDataModel;

    private NotificationLearnService notificationLearnService;

    private NotificationSettingsFormController notificationSettingsFormController;

    public NotificationSettingsController(Identity subscriberIdentity, UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        notificationLearnService = getService(LearnServices.notificationLearnService);
        notificationSettingsVelocityContainer = createVelocityContainer("notificationSettings");
        notificationSettingsFormController = createAndInitSettingsFormController(ureq, wControl);
        notificationSettingsTableController = createAndInitSettingsTableController(ureq, wControl);
        notificationSettingsTableDataModel = createAndInitSettingsTableDataModel(subscriberIdentity);
        notificationSettingsTableController.setSortColumn(3, true);
        updateNotificationNewsDataModel(subscriberIdentity);
        listenTo(notificationSettingsTableController);
        putInitialPanel(notificationSettingsVelocityContainer);
    }

    private NotificationSettingsTableDataModel createAndInitSettingsTableDataModel(Identity subscriberIdentity) {
        NotificationSettingsTableDataModel notificationSettingsTableDataModel = new NotificationSettingsTableDataModel(getTranslator());
        notificationSettingsTableDataModel.addTableColumns(notificationSettingsTableController);
        return notificationSettingsTableDataModel;
    }

    private TableController createAndInitSettingsTableController(UserRequest ureq, WindowControl wControl) {
        TableController notificationSettingsTableController = new TableController(getSettingsTableGuiPreferences(), ureq, wControl, getTranslator(), true);
        notificationSettingsTableController.setMultiSelect(true);
        notificationSettingsTableController.addMultiSelectAction("settings.table.unsubscribe", "unsubscribeAction");
        notificationSettingsVelocityContainer.put("notificationSettingsTableController", notificationSettingsTableController.getInitialComponent());
        return notificationSettingsTableController;
    }

    private NotificationSettingsFormController createAndInitSettingsFormController(UserRequest ureq, WindowControl wControl) {
        NotificationSettingsFormController notificationSettingsFormController = new NotificationSettingsFormController(ureq, wControl);
        notificationSettingsVelocityContainer.put("notificationSettingsFormController", notificationSettingsFormController.getInitialComponent());
        return notificationSettingsFormController;
    }

    private TableGuiConfiguration getSettingsTableGuiPreferences() {
        TableGuiConfiguration tableGuiPreferences = new TableGuiConfiguration();
        tableGuiPreferences.setTableEmptyMessage(translate("settings.table.nosubscriptions"));
        tableGuiPreferences.setPageingEnabled(true);
        tableGuiPreferences.setResultsPerPage(RESULTS_PER_PAGE);
        tableGuiPreferences.setPreferencesOffered(true, "NotificationSettingsTableGuiPrefs");
        return tableGuiPreferences;
    }

    void updateNotificationNewsDataModel(Identity subscriberIdentity) {
        List<SubscriptionTO> userNotificationEventTOs = notificationLearnService.getSubscriptions(subscriberIdentity);
        notificationSettingsTableDataModel.setObjects(userNotificationEventTOs);
        notificationSettingsTableController.setTableDataModel(notificationSettingsTableDataModel);
        notificationSettingsTableController.modelChanged(true);
    }

    @Override
    protected void doDispose() {
        // no cleanup necessary

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        log.info("event for component=" + source);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == notificationSettingsTableController) {
            if (event instanceof TableMultiSelectEvent) {
                log.info("TEST handleTableMultiSelectEvent");
                handleTableMultiSelectEvent(event, ureq.getIdentity());
            }
        }
    }

    private void handleTableMultiSelectEvent(Event event, Identity identity) {
        TableMultiSelectEvent multiSelect = (TableMultiSelectEvent) event;
        if (multiSelect.getSelection().isEmpty()) {
            this.showError("settings.message.select.notifications");
        } else {
            List<SubscriptionTO> selectedSubscriptions = notificationSettingsTableDataModel.getObjects(multiSelect.getSelection());
            for (SubscriptionTO subscriptionTO : selectedSubscriptions) {
                log.info("TEST handleTableMultiSelectEvent unSubscribe:" + subscriptionTO.getSubscription());
                notificationLearnService.unSubscribe(subscriptionTO.getSubscription());
            }
            updateNotificationNewsDataModel(identity);
        }
    }
}
