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

package org.olat.presentation.user.administration;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Feb 6, 2006
 * 
 * @author gnaegi Description: The extended identities table model. Currently it displays a user and its creation date. The idea is to extend the functionality further to
 *         display more information using a wrapper object.
 */
public class ExtendedIdentitiesTableDataModel extends DefaultTableDataModel {

    private final boolean actionEnabled;
    private int colCount = 0;
    private final List<UserPropertyHandler> userPropertyHandlers;
    private static final String usageIdentifyer = ExtendedIdentitiesTableDataModel.class.getCanonicalName();

    /**
     * Constructor
     * 
     * @param identities
     *            The list of identities to use in the table
     * @param actionEnabled
     *            true: the action button is enabled; false: list without action button
     */
    ExtendedIdentitiesTableDataModel(final UserRequest ureq, final List identities, final boolean actionEnabled) {
        super(identities);
        this.actionEnabled = actionEnabled;

        final Roles roles = ureq.getUserSession().getRoles();
        final boolean isAdministrativeUser = roles.isAdministrativeUser();
        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
    }

    /**
     * Add all column descriptors to this table that are available in the table model
     * 
     * @param tableCtr
     */
    public void addColumnDescriptors(final TableController tableCtr, final Translator trans) {
        setLocale(trans.getLocale());
        // first column is users login name
        // default rows
        final DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("table.identity.name", 0, (actionEnabled ? ExtendedIdentitiesTableControllerFactory.COMMAND_VCARD
                : null), getLocale());
        cd0.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
        tableCtr.addColumnDescriptor(cd0);
        colCount++;
        // followed by the users fields
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            final boolean visible = getUserService().isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
            tableCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(i + 1, null, getLocale()));
            colCount++;
        }
        // in the end the last login and creation date
        tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.identity.lastlogin", colCount, null, getLocale()));
        colCount++;
        // creation date at the end, enabled by default
        tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.identity.creationdate", colCount, null, getLocale()));
        colCount++;
        if (actionEnabled) {
            tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ExtendedIdentitiesTableControllerFactory.COMMAND_SELECTUSER, "table.identity.action", trans
                    .translate("action.select")));
        }
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final Identity identity = getIdentityAt(row);
        final User user = identity.getUser();
        if (col == 0) {
            return identity.getName();

        } else if (col > 0 && col < userPropertyHandlers.size() + 1) {
            // get user property for this column
            final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 1);
            final String value = userPropertyHandler.getUserProperty(user, getLocale());
            return (value == null ? "n/a" : value);

        } else if (col == userPropertyHandlers.size() + 1) {
            return identity.getLastLogin();
        } else if (col == userPropertyHandlers.size() + 2) {
            return user.getCreationDate();

        } else {
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return colCount;
    }

    /**
     * @param rowid
     * @return The identity at the given position in the table
     */
    public Identity getIdentityAt(final int rowid) {
        final Identity ident = (Identity) getObject(rowid);
        return ident;
    }

    /**
     * @param selection
     * @return All Identities which were selected in a multiselect - table
     */
    public List<Identity> getIdentities(final BitSet selection) {
        final List<Identity> identities = new ArrayList<Identity>();
        for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i + 1)) {
            final Identity identityAt = getIdentityAt(i);
            identities.add(identityAt);
        }
        return identities;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
