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
import org.olat.lms.core.notification.service.UserNotificationEventTO;
import org.olat.lms.learn.notification.service.NotificationLearnService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.date.DateFilter;
import org.olat.system.commons.date.DateUtil;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 09.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class NotificationNewsController extends BasicController {
    private static final Logger log = LoggerHelper.getLogger();

    private static final int RESULTS_PER_PAGE = 20;

    private final VelocityContainer notificationNewsVelocityContainer;

    private final TableController notificationNewsTableController;
    private final NotificationNewsTableDataModel notificationNewsTableDataModel;
    private NotificationLearnService notificationLearnService;

    public NotificationNewsController(Identity subscriberIdentity, UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        notificationLearnService = getService(LearnServices.notificationLearnService);
        notificationNewsVelocityContainer = createVelocityContainer("notificationNews");

        TableGuiConfiguration tableGuiPreferences = getSettingsTableGuiPreferences();
        notificationNewsTableController = new TableController(tableGuiPreferences, ureq, wControl, getTranslator(), true);

        notificationNewsTableDataModel = new NotificationNewsTableDataModel(getTranslator());
        notificationNewsTableDataModel.addTableColumns(notificationNewsTableController);
        notificationNewsTableController.setTableDataModel(notificationNewsTableDataModel);
        notificationNewsTableController.setSortColumn(3, false);

        updateNotificationNewsDataModel(subscriberIdentity);
        listenTo(notificationNewsTableController);
        notificationNewsVelocityContainer.put("notificationNewsTableController", notificationNewsTableController.getInitialComponent());
        putInitialPanel(notificationNewsVelocityContainer);
    }

    private TableGuiConfiguration getSettingsTableGuiPreferences() {
        TableGuiConfiguration tableGuiPreferences = new TableGuiConfiguration();
        tableGuiPreferences.setTableEmptyMessage(translate("notification.news.nonews.label"));
        tableGuiPreferences.setPageingEnabled(true);
        tableGuiPreferences.setResultsPerPage(RESULTS_PER_PAGE);
        tableGuiPreferences.setPreferencesOffered(true, "NotificationNewsTableGuiPrefs"); // TODO: check this out
        return tableGuiPreferences;
    }

    /**
     * Update the table model
     * 
     * @param subscriberIdentity
     * 
     * @param ureq
     */
    void updateNotificationNewsDataModel(Identity subscriberIdentity) {
        DateFilter dateFilter = DateUtil.getDateFilterFromDDaysBeforeToToday(notificationLearnService.getNumberOfNewsDays());
        List<UserNotificationEventTO> userNotificationEventTOs = notificationLearnService.getNews(subscriberIdentity, dateFilter);

        notificationNewsTableDataModel.setObjects(userNotificationEventTOs);
        notificationNewsTableController.modelChanged(true);
    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == notificationNewsTableController) { // process table actions
            final TableEvent tableEvent = (TableEvent) event;
            UserNotificationEventTO notificationEvent = (UserNotificationEventTO) notificationNewsTableController.getTableDataModel().getObject(tableEvent.getRowId());
            // TODO
        }
    }

}
