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
 * *
 * <p>
 */
package org.olat.presentation.notifications;

import org.olat.data.notifications.Publisher;
import org.olat.data.notifications.Subscriber;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.notifications.NotificationsHandler;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * The subscription table data model displays the users notification subscriptions
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
class NotificationSubscriptionTableDataModel extends DefaultTableDataModel {
    Translator trans;

    NotificationSubscriptionTableDataModel(final Translator translator) {
        super(null); // set at a later stage
        this.trans = translator;
        this.setLocale(trans.getLocale());
    }

    /**
     * Add the column descriptors to the given table controller that matches with this data model
     * 
     * @param subscriptionsTableCtr
     */
    void addTableColumns(final TableController subscriptionsTableCtr) {
        subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.type", 0, "launch", getLocale()));
        subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.resname", 1, null, getLocale()));
        subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.subidentifier", 2, null, getLocale()));
        subscriptionsTableCtr.addColumnDescriptor(new StaticColumnDescriptor("del", "overview.column.action", trans.translate("overview.column.action.cellvalue")));
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final Subscriber sub = getObject(row);
        final Publisher pub = sub.getPublisher();

        switch (col) {
        case 0:
            final String innerType = pub.getType();
            final String typeName = ControllerFactory.translateResourceableTypeName(innerType, getLocale());
            return typeName;
        case 1:
            final String containerType = pub.getResName();
            final String containerTypeTrans = ControllerFactory.translateResourceableTypeName(containerType, getLocale());
            return containerTypeTrans;
        case 2:
            final NotificationsHandler handler = getNotificationService().getNotificationsHandler(pub);
            final String title = handler.createTitleInfo(sub, getLocale());
            if (title == null) {
                return "";
            }
            return title;
        default:
            return "ERROR";
        }
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    /**
	 */
    @Override
    public Subscriber getObject(final int row) {
        return (Subscriber) super.getObject(row);
    }

}
