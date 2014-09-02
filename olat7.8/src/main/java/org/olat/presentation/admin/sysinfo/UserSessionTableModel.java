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

package org.olat.presentation.admin.sysinfo;

import java.util.Date;
import java.util.List;

import org.olat.presentation.commons.session.SessionInfo;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Initial Date: 01.09.2004
 * 
 * @author Mike Stock
 */

public class UserSessionTableModel extends DefaultTableDataModel {
    private final Translator trans;

    /**
     * @param userSessions
     */
    public UserSessionTableModel(final List userSessions, final Translator trans) {
        super(userSessions);
        this.trans = trans;
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 7;
    }

    /**
	 */
    @Override
    public Object getValueAt(final int row, final int col) {
        final UserSession usess = (UserSession) getObject(row);
        final SessionInfo sessInfo = usess.getSessionInfo();
        if (usess.isAuthenticated()) {
            switch (col) {
            case 0:
                return sessInfo.getLastname();
            case 1:
                return sessInfo.getFirstname();
            case 2:
                return sessInfo.getLogin();
            case 3:
                return sessInfo.getAuthProvider();
            case 4:
                return sessInfo.getFromFQN();
            case 5:
                try {
                    // from nano to milli!!
                    return new Date(sessInfo.getLastClickTime());
                    // return new Date(sessInfo.getSession().getLastAccessedTime());
                } catch (final Exception ise) {
                    return null; // "Invalidated"; but need to return a date or null
                }
            case 6:
                try {
                    return sessInfo.getSessionDuration() / 1000;
                } catch (final Exception ise) {
                    return -1;
                }
            case 7:
                if (sessInfo.isWebDAV()) {
                    return "WebDAV";
                } else if (sessInfo.isREST()) {
                    return "REST";
                } else {
                    return sessInfo.getWebMode();
                }
            default:
                return "Error";
            }
        } else { // not signed on
            switch (col) {
            case 5:
                return null;
            case 6:
                return null;
            case 7:
                return null;
            default:
                return "-";
            }
        }

    }

}
