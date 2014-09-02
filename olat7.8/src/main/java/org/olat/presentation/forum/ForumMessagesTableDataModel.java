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

package org.olat.presentation.forum;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.forum.Message;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Felix Jost
 */
public class ForumMessagesTableDataModel extends DefaultTableDataModel {

    private Set readMsgs;

    public ForumMessagesTableDataModel() {
        super(null);
    }

    public ForumMessagesTableDataModel(final List objects, final Set readMsgs) {
        super(objects);
        this.readMsgs = readMsgs;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    /**
     * 4 columns: first is the title of the message second the (name, firstname) of the creator third lastModifiedDate fourth if it is new or not
     */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final Message m = (Message) getObject(row);
        switch (col) {
        case 0:
            final String title = StringEscapeUtils.escapeHtml(m.getTitle()).toString(); // OLAT-1024: value should be escaped in renderer, but it is escaped in model
            return title;
        case 1:
            return getUserService().getFirstAndLastname(m.getCreator().getUser());
        case 2:
            final Date mod = m.getLastModified();
            return mod;
        case 3: // return if we have read this message before or not
            return (readMsgs.contains(m.getKey()) ? Boolean.TRUE : Boolean.FALSE);
        default:
            return "error";
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
