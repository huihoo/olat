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

package org.olat.presentation.note;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.data.note.Note;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.components.table.DefaultTableDataModel;

/**
 * Initial Date: Dec 10, 2004
 * 
 * @author Alexander Schneider
 */

class NoteListTableDataModel extends DefaultTableDataModel {

    private final Locale locale;

    /**
     * @param objects
     * @param locale
     */
    public NoteListTableDataModel(final List<Note> objects, final Locale locale) {
        super(objects);
        this.locale = locale;
    }

    /**
	 */
    @Override
    public final Object getValueAt(final int row, final int col) {
        final Note n = (Note) getObject(row);
        switch (col) {
        case 0:
            final String title = StringEscapeUtils.escapeHtml(n.getNoteTitle()).toString();
            return title;
        case 1:
            final String resType = n.getResourceTypeName();
            return (resType == null ? "n/a" : ControllerFactory.translateResourceableTypeName(resType, locale));
        default:
            return "error";
        }
    }

    /**
	 */
    @Override
    public int getColumnCount() {
        return 2;
    }
}
