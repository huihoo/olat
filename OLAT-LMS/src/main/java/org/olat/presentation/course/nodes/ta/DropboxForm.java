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

package org.olat.presentation.course.nodes.ta;

import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;

/**
 * Initial Date: 30.08.2004
 * 
 * @author Mike Stock
 */

public class DropboxForm extends FormBasicController {

    private final ModuleConfiguration config;
    private SelectionElement enablemail;
    private TextElement confirmation;

    /**
     * Dropbox configuration form.
     * 
     * @param name
     * @param config
     * @param ureq
     */
    public DropboxForm(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config) {
        super(ureq, wControl);
        this.config = config;
        initForm(ureq);
    }

    /**
     * @return mailEnabled field value
     */
    public boolean mailEnabled() {
        return enablemail.isSelected(0);
    }

    /**
     * @return confirmation field value
     */
    public String getConfirmation() {
        return confirmation.getValue().trim();
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
        if (source == enablemail) {
            confirmation.setMandatory(enablemail.isSelected(0));
            validateFormLogic(ureq);
        }
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        if (confirmation.isMandatory()) {
            if (confirmation.getValue().trim().length() == 0) {
                confirmation.setExampleKey("conf.stdtext.example", null);
                confirmation.setErrorKey("error.nomailbody", null);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("fieldset.dropbox.title");
        setFormContextHelp("org.olat.presentation.course.nodes.ta", "ced-ta-conf.html", "help.hover.ta-dropbox");

        String sConfirmation = (String) config.get(TACourseNode.CONF_DROPBOX_CONFIRMATION);
        if (sConfirmation == null || sConfirmation.length() == 0) {
            // grab standard text
            sConfirmation = translate("conf.stdtext");
            config.set(TACourseNode.CONF_DROPBOX_CONFIRMATION, sConfirmation);
        }

        confirmation = uifactory.addTextAreaElement("confirmation", "form.dropbox.confirmation", 2500, 4, 40, true, sConfirmation != null ? sConfirmation : "",
                formLayout);

        final Boolean enableMail = (Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLEMAIL);
        confirmation.setMandatory(enableMail);
        enablemail = uifactory.addCheckboxesVertical("enablemail", "form.dropbox.enablemail", formLayout, new String[] { "xx" }, new String[] { null }, null, 1);
        enablemail.select("xx", enableMail != null ? enableMail.booleanValue() : true);
        enablemail.addActionListener(this, FormEvent.ONCLICK);

        uifactory.addFormSubmitButton("submit", formLayout);
    }

    @Override
    protected void doDispose() {
        //
    }
}
