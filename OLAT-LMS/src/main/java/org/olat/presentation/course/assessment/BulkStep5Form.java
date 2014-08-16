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

package org.olat.presentation.course.assessment;

import java.util.Iterator;
import java.util.List;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: schneider Class Description for ColWithBulkActionForm
 * <P>
 * Initial Date: 19.12.2005 <br>
 * 
 * @author Alexander Schneider
 */
class BulkStep5Form extends FormBasicController {

    private SingleSelection colSelElement;
    private SingleSelection bulkSelElement;

    String[] cKeys;
    String[] cValues;

    String[] fKeys;
    String[] fValues;

    /**
     * @param name
     * @param trans
     */
    public BulkStep5Form(final UserRequest ureq, final WindowControl wControl, final List columns, final List bulkActions) {
        super(ureq, wControl);

        final int sizeCols = columns.size();
        cKeys = new String[sizeCols];
        cValues = new String[sizeCols];
        int i = 0;
        for (final Iterator iter = columns.iterator(); iter.hasNext();) {
            cKeys[i] = Integer.toString(i);
            cValues[i] = (String) iter.next();
            i++;
        }

        initForm(ureq);
    }

    /**
     * @return selected bulkAction
     */
    public String getSelectedBulkAction() {
        return bulkSelElement.getSelectedKey();
    }

    /**
     * @return selected column
     */
    public String getSelectedColumn() {
        return colSelElement.getSelectedKey();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        colSelElement = uifactory.addDropdownSingleselect("colSelElement", "form.step5.columns", formLayout, cKeys, cValues, null);
        colSelElement.select("0", true);

        uifactory.addFormSubmitButton("next", "next", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
