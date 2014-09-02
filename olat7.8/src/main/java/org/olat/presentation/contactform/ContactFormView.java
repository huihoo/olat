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
package org.olat.presentation.contactform;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.DefaultController;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicView;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.system.mail.ContactList;

/**
 * @author patrick
 * 
 */
public class ContactFormView extends BasicView {

    private VelocityContainer vcCreateContactMsg;
    private ContactForm cntctForm = null;
    private Panel main;
    private List<ContactList> recipients;
    private String bodyText;
    private String subjectText;
    private boolean hasAtLeastOneAddress;

    public ContactFormView(UserRequest ureq, WindowControl wControl, Identity emailFrom, boolean hasAtLeastOneAddress, boolean isReadonly, boolean isCanceable,
            boolean hasRecipientsEditable) {
        super(ureq, wControl);

        this.hasAtLeastOneAddress = hasAtLeastOneAddress;

        main = new Panel("contactFormMainPanel");
        vcCreateContactMsg = createVelocityContainer("c_contactMsg");

        if (hasAtLeastOneAddress) {
            cntctForm = new ContactForm(ureq, wControl, emailFrom, isReadonly, isCanceable, hasRecipientsEditable);
            vcCreateContactMsg.put("cntctForm", cntctForm.getInitialComponent());
            main.setContent(vcCreateContactMsg);
        }

    }

    public void setRecipients(final List<ContactList> recipList) {
        this.recipients = recipList;
    }

    public void setBody(String bodyText) {
        this.bodyText = bodyText;
    }

    public void setSubject(String subjectText) {
        this.subjectText = subjectText;
    }

    @Override
    public Component getInitialComponent(DefaultController listeningController) {
        if (hasAtLeastOneAddress) {
            cntctForm.setBody(bodyText);
            cntctForm.setSubject(subjectText);
            listenTo(listeningController, cntctForm);
            setRecipientsInContactForm();
            return main;
        } else {
            final Controller mCtr = MessageUIFactory.createInfoMessage(ureq, wControl, null, translate("error.msg.send.no.rcps"));
            listenTo(listeningController, mCtr);
            return mCtr.getInitialComponent();
        }
    }

    private void setRecipientsInContactForm() {
        if (recipients != null && recipients.size() > 0) {
            for (final Iterator<ContactList> iter = recipients.iterator(); iter.hasNext();) {
                final ContactList cl = iter.next();
                if (cl.getEmailsAsStrings().size() > 0) {
                    cntctForm.addEmailTo(cl);
                }
            }
        }
    }

    public boolean is(Controller source) {
        if (cntctForm == null) {
            return false;
        }
        return cntctForm == source;
    }

    public List<File> getAttachments() {
        return cntctForm.getAttachments();
    }

    public List<ContactList> getEmailToContactLists() {
        return cntctForm.getEmailToContactLists();
    }

    public String getSubject() {
        return cntctForm.getSubject();
    }

    public String getBody() {
        return cntctForm.getBody();
    }

    public boolean isTcpFrom() {
        return cntctForm.isTcpFrom();
    }

    public String getEmailFromAsString() {
        return cntctForm.getEmailFrom();
    }

    public void setDisplayOnly(boolean readOnly) {
        cntctForm.setDisplayOnly(readOnly);
    }

    public String getEmailTo() {
        return cntctForm.getEmailTo();
    }

}
