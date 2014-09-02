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

import java.util.Date;

import org.olat.data.infomessage.InfoMessage;
import org.olat.lms.infomessage.InfoMessageFrontendManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Controller for editing InfoMessages.
 * <P>
 * Initial Date: 24 aug. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoEditController extends FormBasicController {

    private final InfoMessage messageToEdit;
    private final InfoEditFormController editForm;
    private final InfoMessageFrontendManager infoFrontendManager;

    public InfoEditController(final UserRequest ureq, final WindowControl wControl, final InfoMessage messageToEdit) {
        super(ureq, wControl, "edit");

        this.messageToEdit = messageToEdit;
        infoFrontendManager = CoreSpringFactory.getBean(InfoMessageFrontendManager.class);
        editForm = new InfoEditFormController(ureq, wControl, mainForm);
        editForm.setTitle(messageToEdit.getTitle());
        editForm.setMessage(messageToEdit.getMessage());
        listenTo(editForm);

        initForm(ureq);
    }

    @Override
    protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
        final FormLayoutContainer editCont = editForm.getInitialFormItem();

        final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
        editCont.add(buttonLayout);
        uifactory.addFormSubmitButton("submit", buttonLayout);
        uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());

        flc.add("edit", editCont);
    }

    @Override
    protected void doDispose() {
        //
    }

    @Override
    protected boolean validateFormLogic(final UserRequest ureq) {
        return editForm.validateFormLogic(ureq);
    }

    @Override
    protected void formOK(final UserRequest ureq) {
        final String title = editForm.getTitle();
        final String message = editForm.getMessage();

        messageToEdit.setTitle(title);
        messageToEdit.setMessage(message);
        messageToEdit.setModificationDate(new Date());
        messageToEdit.setModifier(getIdentity());
        infoFrontendManager.saveInfoMessage(messageToEdit);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(final UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}
