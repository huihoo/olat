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
import org.olat.presentation.framework.core.components.form.flexible.elements.IntegerElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.Submit;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.elements.FormSubmit;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.event.Event;

public class SessionAdminOldestSessionForm extends FormBasicController {
    private IntegerElement nbrSessions;

    public SessionAdminOldestSessionForm(final UserRequest ureq, final WindowControl wControl, final Translator translator) {
        super(ureq, wControl);
        setTranslator(translator);
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final FormLayoutContainer verticalL = FormLayoutContainer.createVerticalFormLayout("verticalL", getTranslator());
        formLayout.add(verticalL);
        nbrSessions = uifactory.addIntegerElement("nbr.session", "nbr.session.label", 0, verticalL);
        final Submit oldestSessionButton = new FormSubmit("save", "oldest.session.button");
        formLayout.add(oldestSessionButton);
    }

    @Override
    protected void doDispose() {
        // empty
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formResetted(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    public int getNbrSessions() {
        return nbrSessions.getIntValue();
    }

}
