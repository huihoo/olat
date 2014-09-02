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

package org.olat.presentation.user.administration.delete;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.lifecycle.LifeCycleEntry;
import org.olat.data.lifecycle.LifeCycleManager;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.system.spring.CoreSpringFactory;

/**
 * The user table data model for user deletion. This uses a list of Identities and not org.olat.data.user.User to build the list!
 * 
 * @author Christian Guretzki
 */
public class UserDeleteTableModel extends DefaultTableDataModel {

    private final List<UserPropertyHandler> userPropertyHandlers;
    private static final String usageIdentifyer = UserDeleteTableModel.class.getCanonicalName();

    /**
     * @param objects
     * @param locale
     * @param isAdministrativeUser
     */
    public UserDeleteTableModel(final List objects, final Locale locale, final boolean isAdministrativeUser) {
        super(objects);
        setLocale(locale);
        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
    }

    /**
     * Add all column descriptors to this table that are available in the table model. The table contains userPropertyHandlers.size() columns plus a column for the
     * username and one for the last login date.
     * 
     * @param tableCtr
     * @param actionCommand
     *            command fired when the login name is clicked or NULL when no command is used
     */
    public void addColumnDescriptors(final TableController tableCtr, final String actionCommand) {
        // first column is the username
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.login", 0, actionCommand, getLocale()));
        // followed by the users fields
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            final boolean visible = getUserService().isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
            tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i + 1, null, getLocale()));
        }
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.lastlogin", getColumnCount() - 2, actionCommand, getLocale()));
    }

    /**
     * The table contains a suplementary column for the "delete email date".
     * 
     * @param tableCtr
     * @param actionCommand
     * @param deleteEmailKey
     */
    public void addColumnDescriptors(final TableController tableCtr, final String actionCommand, final String deleteEmailKey) {
        addColumnDescriptors(tableCtr, actionCommand);
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor(deleteEmailKey, getColumnCount() - 1, actionCommand, getLocale()));
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final Identity identity = (Identity) getObject(row);
        final User user = identity.getUser();
        if (col == 0) {
            return identity.getName();
        } else if (col < userPropertyHandlers.size() + 1) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 1);
            final String value = userPropertyHandler.getUserProperty(user, getLocale());
            return (value == null ? "n/a" : value);
        } else if (col == userPropertyHandlers.size() + 1) {
            final Date lastLogin = identity.getLastLogin();
            return (lastLogin == null ? "n/a" : lastLogin);
        } else if (col == userPropertyHandlers.size() + 2) {
            final LifeCycleEntry lcEvent = LifeCycleManager.createInstanceFor(identity).lookupLifeCycleEntry(UserDeletionManager.SEND_DELETE_EMAIL_ACTION);
            if (lcEvent == null) {
                return "n/a";
            }
            final Date deleteEmail = lcEvent.getLcTimestamp();
            return (deleteEmail == null ? "n/a" : deleteEmail);
        } else {
            return "error";
        }
    }

    /**
     * The table model contains userPropertyHandlers.size() columns plus a column for the username, one for the last login date, and one for the delete email date.
     * 
     */
    @Override
    public int getColumnCount() {
        return userPropertyHandlers.size() + 3;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
