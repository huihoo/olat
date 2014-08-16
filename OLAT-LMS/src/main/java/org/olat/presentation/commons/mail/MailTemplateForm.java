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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.commons.mail;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.form.flexible.FormItem;
import org.olat.presentation.framework.core.components.form.flexible.FormItemContainer;
import org.olat.presentation.framework.core.components.form.flexible.elements.SelectionElement;
import org.olat.presentation.framework.core.components.form.flexible.elements.TextElement;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormBasicController;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormEvent;
import org.olat.presentation.framework.core.components.form.flexible.impl.FormLayoutContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.mail.MailTemplate;

/**
 * Description:<br>
 * The MailTemplateForm allows the user to enter a mailtext based on the MailTemplate
 * <p>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailTemplateForm extends FormBasicController {

    private TextElement subjectElem;
    private TextElement bodyElem;
    private SelectionElement sendMail;
    private SelectionElement ccSender;
    private final static String NLS_CONTACT_SEND_CP_FROM = "contact.cp.from";

    private boolean useCancel;
    private MailTemplate template;

    /**
     * Constructor for the mail notification form
     * 
     * @param locale
     * @param template
     *            Default values taken from this template
     * @param useCancel
     * @param listeningController
     *            Controller that listens to form events
     */
    public MailTemplateForm(UserRequest ureq, WindowControl wControl, MailTemplate template, boolean useCancel) {
        super(ureq, wControl);
        this.template = template;
        this.useCancel = useCancel;
        initForm(ureq);
    }

    /**
     * Update the given templates with the values entered in the form
     * 
     * @param template
     */
    public void updateTemplateFromForm(MailTemplate template) {
        template.setSubjectTemplate(subjectElem.getValue());
        template.setBodyTemplate(bodyElem.getValue());
        template.setCpfrom(ccSender.isSelected(0));
    }

    @Override
    protected void formOK(UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        // validate only when sendMail is enabled
        if (sendMail.isSelected(0)) {
            if (subjectElem.getValue().trim().length() == 0) {
                subjectElem.setErrorKey("mailtemplateform.error.emptyfield", null);
                return false;
            }

            if (bodyElem.getValue().trim().length() == 0) {
                bodyElem.setErrorKey("mailtemplateform.error.emptyfield", null);
                return false;
            }

            if (subjectElem.getValue().indexOf("#") != -1) {
                subjectElem.setErrorKey("mailtemplateform.error.velocity", null);
                return false;
            }

            if (bodyElem.getValue().indexOf("#") != -1) {
                bodyElem.setErrorKey("mailtemplateform.error.velocity", null);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

        sendMail = uifactory.addCheckboxesVertical("sendmail", "", formLayout, new String[] { "xx" }, new String[] { translate("mailtemplateform.sendMailSwitchElem") },
                null, 1);
        sendMail.addActionListener(listener, FormEvent.ONCLICK);

        subjectElem = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, template.getSubjectTemplate(), formLayout);
        subjectElem.setDisplaySize(60);
        subjectElem.setMandatory(true);

        bodyElem = uifactory.addTextAreaElement("bodyElem", "mailtemplateform.body", -1, 15, 60, true, template.getBodyTemplate(), formLayout);
        bodyElem.setMandatory(true);

        ccSender = uifactory.addCheckboxesVertical("tcpfrom", "", formLayout, new String[] { "xx" }, new String[] { translate(NLS_CONTACT_SEND_CP_FROM) }, null, 1);

        FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
        formLayout.add(buttonGroupLayout);

        uifactory.addFormSubmitButton("continue", "mailtemplateform.continue", buttonGroupLayout);
        if (useCancel) {
            uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
        }

        update();
    }

    private void update() {
        boolean sm = sendMail.isSelected(0);
        subjectElem.setVisible(sm);
        bodyElem.setVisible(sm);
        ccSender.setVisible(sm);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        update();
    }

    /**
     * @return true: mail switch is enabled; false: otherwise
     */
    public boolean sendMailSwitchEnabled() {
        return (sendMail.isSelected(0));
    }

    @Override
    protected void doDispose() {
        //
    }

}
