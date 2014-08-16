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

package org.olat.presentation.campusmgnt;

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
 * TODO: schneider Class Description for KeyWithColumnForm
 * <P>
 * Initial Date: 19.12.2005 <br>
 * 
 * @author Alexander Schneider
 */
public class KeyWithColumnForm extends FormBasicController {

    private SingleSelection keySelElement;
    private SingleSelection colSelElement;

    private final String[] cKeys, cValues;
    private final String[] oKeys, oValues;

    /**
     * @param name
     * @param trans
     */
    public KeyWithColumnForm(final UserRequest ureq, final WindowControl wControl, final List olatKeys, final List columns) {
        super(ureq, wControl);

        final int sizeCols = columns.size();
        cKeys = new String[sizeCols];
        cValues = new String[sizeCols];
        int j = 0;
        for (final Iterator iter = columns.iterator(); iter.hasNext();) {
            cKeys[j] = Integer.toString(j);
            cValues[j] = (String) iter.next();
            j++;
        }

        final int sizeOlKs = olatKeys.size();
        oKeys = new String[sizeOlKs];
        oValues = new String[sizeOlKs];
        int i = 0;
        for (final Iterator iter = olatKeys.iterator(); iter.hasNext();) {
            oKeys[i] = Integer.toString(i);
            oValues[i] = (String) iter.next();
            i++;
        }

        initForm(ureq);
    }

    /**
     * @return selected olatKey
     */
    public String getSelectedOlatKey() {
        return keySelElement.getSelectedKey();
    }

    /**
     * @return selected value of olatKey
     */
    public String getSelectedValueOfOlatKey(final int key) {
        return keySelElement.getValue(key);
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

        colSelElement = uifactory.addDropdownSingleselect("colSelElement", "form.step2.columns", formLayout, cKeys, cValues, null);
        colSelElement.select(cKeys[0], true);

        keySelElement = uifactory.addDropdownSingleselect("keySelElement", "form.step2.olatkeys", formLayout, oKeys, oValues, null);
        keySelElement.select(oKeys[0], true);

        uifactory.addFormSubmitButton("next", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

}
