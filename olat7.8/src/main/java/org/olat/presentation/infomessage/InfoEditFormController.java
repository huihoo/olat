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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.presentation.infomessage;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.Form;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * <P>
 * Initial Date: 26 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoEditFormController extends FormBasicController {

    private TextElement title;
    private TextElement message;

    public InfoEditFormController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        initForm(ureq);
        /** TODO: REVIEW GUI MITTEILUNGEN: bb/18.07.2012 **/
        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        this.flc.add(buttonLayout);
        uifactory.addFormSubmitButton("msg.save", buttonLayout);
        uifactory.addFormCancelButton("msg.cancel", buttonLayout, ureq, getWindowControl());
    }

    public InfoEditFormController(final UserRequest ureq, final WindowControl wControl, final Form mainForm) {
        super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        setFormTitle("edit.title");
        title = uifactory.addTextElement("info_title", "edit.info_title", 512, "", formLayout);
        title.setMandatory(true);
        message = uifactory.addTextAreaElement("edit.info_message", 6, 80, "", formLayout);
        message.setMandatory(true);
        message.setMaxLength(2000);
    }

    @Override
    protected void doDispose() {
        //
    }

    public String getTitle() {
        return title.getValue();
    }

    public void setTitle(final String titleStr) {
        title.setValue(titleStr);
    }

    public String getMessage() {
        return message.getValue();
    }

    public void setMessage(final String messageStr) {
        message.setValue(messageStr);
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
    protected boolean validateFormLogic(final UserRequest ureq) {
        title.clearError();
        message.clearError();
        boolean allOk = true;

        final String t = title.getValue();
        if (!StringHelper.containsNonWhitespace(t)) {
            title.setErrorKey("form.legende.mandatory", new String[] {});
            allOk = false;
        } else if (t.length() > 500) {
            title.setErrorKey("input.toolong", new String[] { "500", Integer.toString(t.length()) });
            allOk = false;
        }

        final String m = message.getValue();
        if (!StringHelper.containsNonWhitespace(m)) {
            message.setErrorKey("form.legende.mandatory", new String[] {});
            allOk = false;
        } else if (m.length() > 2000) {
            message.setErrorKey("input.toolong", new String[] { "2000", Integer.toString(m.length()) });
            allOk = false;
        }

        return allOk && super.validateFormLogic(ureq);
    }

    public FormLayoutContainer getInitialFormItem() {
        return flc;
    }
}
