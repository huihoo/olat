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

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Initial Date: Apr 30, 2004
 * 
 * @author Mike Stock
 */
public class MaintenanceMsgForm extends FormBasicController {

    private TextElement msg;
    private final String infomsg;

    /**
     * @param name
     * @param infomsg
     */
    public MaintenanceMsgForm(final UserRequest ureq, final WindowControl wControl, final String infomsg) {
        super(ureq, wControl);
        this.infomsg = infomsg;
        initForm(ureq);
    }

    /**
     * @return the info message
     */
    public String getInfoMsg() {
        return msg.getValue();
    }

    public void reset() {
        msg.setValue("");
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        msg = uifactory.addTextAreaElement("msg", "infomsg", 1024, 20, 60, true, infomsg, formLayout);

        final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);
        uifactory.addFormSubmitButton("submit", "submit", buttonGroupLayout);
        uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

}
