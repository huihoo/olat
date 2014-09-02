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

package org.olat.presentation.course.config;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * TODO: guido Class Description for CourseEfficencyStatementForm
 */
public class CourseEfficencyStatementForm extends FormBasicController {

    private SelectionElement isOn;
    private final boolean enabled;

    /**
     * @param name
     * @param chatEnabled
     */
    public CourseEfficencyStatementForm(final UserRequest ureq, final WindowControl wControl, final boolean enabled) {
        super(ureq, wControl);
        this.enabled = enabled;
        initForm(ureq);
    }

    /**
     * @return if chat is enabled
     */
    public boolean isEnabledEfficencyStatement() {
        return isOn.isSelected(0);
    }

    public void setEnabledEfficencyStatement(final boolean b) {
        isOn.select("xx", b);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {

        isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.efficency.onoff", formLayout, new String[] { "xx" }, new String[] { "" }, null, 1);
        isOn.select("xx", enabled);

        uifactory.addFormSubmitButton("save", "save", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }

}
