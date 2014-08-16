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

package org.olat.presentation.user.administration.importwizzard;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 2005
 * 
 * @author Felix Jost, Roman Haag Description: Table model for user mass import.
 */
public class ImportTableModel extends DefaultTableDataModel {

    private final List<UserPropertyHandler> userPropertyHandlers;
    private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
    private int columnCount = 0;

    public ImportTableModel(final List objects, final int columnCount) {
        super(objects);
        userPropertyHandlers = getUserService().getUserPropertyHandlersFor(usageIdentifyer, true);
        this.columnCount = columnCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        Identity ident = null;
        List<String> userArray = null;
        boolean userExists = false;
        final Object o = getObject(row);
        if (o instanceof Identity) {
            ident = (Identity) o;
            userExists = true;
        } else {
            userArray = (List<String>) o;
        }

        if (col == 0) { // existing
            return (userExists ? Boolean.FALSE : Boolean.TRUE);
        }

        if (col == 1) {
            return (userExists ? ident.getName() : userArray.get(col));
        }

        if (col == 2) {// pwd
            if (userExists) {
                return "-";
            } else {
                return (userArray.get(col) == null ? "-" : "***");
            }
        } else if (col == 3) {// lang
            if (userExists) {
                return ident.getUser().getPreferences().getLanguage();
            } else {
                return userArray.get(col);
            }
        } else if (col > 3 && col < getColumnCount()) {
            if (userExists) {
                // get user property for this column for an already existing user
                final UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - 4);
                final String value = userPropertyHandler.getUserProperty(ident.getUser(), getLocale());
                return (value == null ? "n/a" : value);
            } else {
                return userArray.get(col);
            }
        }

        return "ERROR";

    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
