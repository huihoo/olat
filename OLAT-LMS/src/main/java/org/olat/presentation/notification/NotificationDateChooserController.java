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
package org.olat.presentation.notification;

import java.util.Date;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;

/**
 * Initial Date: 15.03.2012 <br>
 * 
 * @author Branislav Balaz
 */
public class NotificationDateChooserController extends FormBasicController {

    private DateChooser dateFromChooser;
    private final String label;

    public NotificationDateChooserController(final UserRequest ureq, final WindowControl wControl, final Date initDate, final String label) {
        super(ureq, wControl);
        this.label = label;
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        dateFromChooser = uifactory.addDateChooser(label, "", formLayout);
        dateFromChooser.setDate(new Date());
        dateFromChooser.addActionListener(this, FormEvent.ONCHANGE);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

}
