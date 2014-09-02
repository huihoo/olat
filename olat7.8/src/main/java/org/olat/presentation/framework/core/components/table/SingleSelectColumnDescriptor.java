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
package org.olat.presentation.framework.core.components.table;

import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.system.exception.AssertException;

/**
 * Initial Date: 12.12.2012 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public class SingleSelectColumnDescriptor implements ColumnDescriptor {

    private static final String DOUBLE_QUOTE = "\"";
    private static final String VALUE = "\" value=\"";
    private static final String CLOSE_HTML_TAG = " />";

    private Table table;

    SingleSelectColumnDescriptor() {
        // package visibility for constructor
    }

    @Override
    public void renderValue(final StringOutput sb, final int row, final Renderer renderer) {
        // add checkbox
        int currentPosInModel = table.getSortedRow(row);
        boolean checked = table.getSelectedRowId() == currentPosInModel;
        if (renderer == null) {
            // render for export
            if (checked) {
                sb.append("x");
            }
        } else {
            sb.append("<input type=\"radio\" name=\"" + TableRenderer.TABLE_SINGLESELECT_GROUP + VALUE).append(currentPosInModel).append(DOUBLE_QUOTE);
            if (checked) {
                sb.append(" checked=\"checked\"");
            }
            sb.append(CLOSE_HTML_TAG);
        }
    }

    @Override
    public int compareTo(final int rowa, final int rowb) {
        if (rowa == table.getSelectedRowId()) {
            return -1;
        } else if (rowb == table.getSelectedRowId()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((table == null) ? 0 : table.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SingleSelectColumnDescriptor other = (SingleSelectColumnDescriptor) obj;
        if (table == null) {
            if (other.table != null)
                return false;
        } else if (!table.equals(other.table))
            return false;
        return true;
    }

    @Override
    public String getHeaderKey() {
        return "table.header.multiselect";
    }

    @Override
    public boolean translateHeaderKey() {
        return true;
    }

    @Override
    public int getAlignment() {
        return ColumnDescriptor.ALIGNMENT_CENTER;
    }

    @Override
    public String getAction(final int row) {
        return null;
    }

    @Override
    public HrefGenerator getHrefGenerator() {
        return null;
    }

    @Override
    public String getPopUpWindowAttributes() {
        return null;
    }

    @Override
    public boolean isPopUpWindowAction() {
        return false;
    }

    @Override
    public boolean isSortingAllowed() {
        return false;
    }

    @Override
    public void modelChanged() {
        // nothing to do here
    }

    @Override
    public void otherColumnDescriptorSorted() {
        // nothing to do here
    }

    @Override
    public void setHrefGenerator(final HrefGenerator h) {
        throw new AssertException("Not allowed to set HrefGenerator on MultiSelectColumn.");
    }

    @Override
    public void setTable(final Table table) {
        this.table = table;
    }

    @Override
    public void sortingAboutToStart() {
        // nothing to do here
    }

    @Override
    public String toString(final int rowid) {
        return table.getSelectedRowId() == rowid ? "checked" : "unchecked";
    }

}
