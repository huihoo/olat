/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.instantmessaging;

import org.jivesoftware.smack.packet.Presence;
import org.olat.lms.instantmessaging.ImPreferences;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SingleSelection;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Initial Date: August 08, 2005
 * 
 * @author Alexander Schneider Comment: Displays a list of radiobuttons with all instant messaging presence status.
 */
public class RosterForm extends FormBasicController {
    private static final String STATUSLIST = "statusList";
    private SingleSelection statusList;
    private final String[] keys, values;
    private final ImPreferences imPrefs;

    public RosterForm(final UserRequest ureq, final WindowControl wControl, final ImPreferences imPrefs) {
        super(ureq, wControl);

        this.imPrefs = imPrefs;

        keys = new String[] { Presence.Mode.available.toString(), Presence.Mode.chat.toString(), Presence.Mode.away.toString(), Presence.Mode.xa.toString(),
                Presence.Mode.dnd.toString(), Presence.Type.unavailable.toString() };

        values = new String[] { translate("presence.available"), translate("presence.chat"), translate("presence.away"), translate("presence.xa"),
                translate("presence.dnd"), translate("presence.unavailable") };

        initForm(ureq);
    }

    /**
     * @param imPrefs
     */
    public void updateImPreferencesFromFormData(final ImPreferences imPrefs) {
        imPrefs.setRosterDefaultStatus(statusList.getSelectedKey());
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
        setFormTitle("title.roster");
        this.setFormContextHelp("org.olat.presentation.user", "home-imsettings-roster.html", "help.hover.imsettings-roster");

        statusList = uifactory.addRadiosVertical(STATUSLIST, "form.defaultstatus", formLayout, keys, values);
        statusList.select(imPrefs.getRosterDefaultStatus(), true);

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        formLayout.add(buttonLayout);
        uifactory.addFormSubmitButton("save", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
    }

    @Override
    protected void doDispose() {
        //
    }

}
