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

package org.olat.presentation.calendar;

import java.util.Date;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.DateChooser;
import org.olat.presentation.framework.core.components.form.flexible.elements.FormLink;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

public class GotoDateCalendarsForm extends FormBasicController {
    private DateChooser gotoDate;
    private FormLink gotoDateLink;

    public GotoDateCalendarsForm(final UserRequest ureq, final WindowControl wControl, final Translator translator) {
        super(ureq, wControl);
        setTranslator(translator);
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final FormLayoutContainer horizontalL = FormLayoutContainer.createHorizontalFormLayout("horiz", getTranslator());
        formLayout.add(horizontalL);
        gotoDate = uifactory.addDateChooser("goto", null, "", horizontalL);// null because no label is desired.
        gotoDate.setMandatory(false);
        gotoDate.setDate(new Date());
        gotoDateLink = new FormLinkImpl("cal.search.gotodate.button");
        horizontalL.add(gotoDateLink);
    }

    @Override
    protected void doDispose() {
        // empty
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        final boolean isInputValid = true;
        if (gotoDate.hasError() || (getGotoDate() == null)) {
            gotoDate.setErrorKey("error.goto.date", new String[0]);
            return false;
        }
        return isInputValid;
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == gotoDateLink) {
            this.flc.getRootForm().submit(ureq);
        }
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formResetted(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    public Date getGotoDate() {
        return gotoDate.getDate();
    }

}
