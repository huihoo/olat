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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.commons.mail.MailLoggingAction;
import org.olat.lms.commons.mail.MailTemplateHelper;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.ContactList;
import org.olat.system.security.OLATPrincipal;

/**
 * <b>Fires Event: </b>
 * <UL>
 * <LI><b>Event.DONE_EVENT: </B> <BR>
 * email was sent successfully by the underlying Email subsystem</LI>
 * <LI><b>Event.FAILED_EVENT: </B> <BR>
 * email was not sent correct by the underlying Email subsystem <BR>
 * email may be partially sent correct, but some parts failed.</LI>
 * <LI><b>Event.CANCELLED_EVENT: </B> <BR>
 * user interaction, i.e. canceled message creation</LI>
 * </UL>
 * <p>
 * <b>Consumes Events from: </b>
 * <UL>
 * <LI>ContactForm:</LI>
 * <UL>
 * <LI>Form.EVENT_FORM_CANCELLED</LI>
 * <LI>Form.EVENT_VALIDATION_OK</LI>
 * </UL>
 * </UL>
 * <P>
 * <b>Main Purpose: </b> is to provide an easy interface for <i>contact message creation and sending </i> from within different OLAT bulding blocks.
 * <P>
 * <b>Responsabilites: </b> <br>
 * <UL>
 * <LI>supplies a workflow for creating and sending contact messages</LI>
 * <LI>works with the ContactList encapsulating the e-mail addresses in a mailing list.</LI>
 * <LI>contact messages with pre-initialized subject and/or body</LI>
 * </UL>
 * <P>
 * TODO:pb:b refactor ContactFormController and ContactForm to extract a ContactMessageManager, setSubject(..) setRecipients.. etc. should not be in the controller.
 * Refactor to use ContactMessage!
 * 
 * @author patrick
 */
public class ContactFormController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();

    private DialogBoxController noUsersErrorCtr;
    private ArrayList<String> myButtons;

    private ContactFormView contactView;

    private ContactUIModel contactUIModel;

    /**
     * @param ureq
     * @param windowControl
     * @param useDefaultTitle
     * @param isCanceable
     * @param isReadonly
     * @param hasRecipientsEditable
     * @param cmsg
     */
    public ContactFormController(final UserRequest ureq, final WindowControl windowControl, final boolean useDefaultTitle, final boolean isCanceable,
            final boolean isReadonly, final boolean hasRecipientsEditable, final ContactMessage cmsg) {
        super(ureq, windowControl);

        contactUIModel = new ContactUIModel(cmsg);

        final boolean hasAtLeastOneAddress = contactUIModel.hasAtLeastOneAddress();
        List<OLATPrincipal> disabledIdentities = contactUIModel.getDisabledIdentities();

        contactView = new ContactFormView(ureq, windowControl, cmsg.getFrom(), hasAtLeastOneAddress, isReadonly, isCanceable, hasRecipientsEditable);
        initDisplay(ureq, hasAtLeastOneAddress, disabledIdentities);
    }

    public ContactFormController(final ContactFormView contactFormView, final ContactUIModel contactUIModel) {
        super(contactFormView.getUreq(), contactFormView.getWindowControl());
        this.contactView = contactFormView;
        this.contactUIModel = contactUIModel;
        initDisplay(contactFormView.getUreq(), contactUIModel.hasAtLeastOneAddress(), contactUIModel.getDisabledIdentities());
    }

    private void initDisplay(final UserRequest ureq, final boolean hasAtLeastOneAddress, List<OLATPrincipal> disabledIdentities) {
        contactView.setBody(contactUIModel.getBodyText());
        contactView.setSubject(contactUIModel.getSubject());
        contactView.setRecipients(contactUIModel.getEmailToContactLists());

        putInitialPanel(contactView.getInitialComponent(this));

        if (!hasAtLeastOneAddress | disabledIdentities.size() > 0) {
            showErrorThatNoEmailCouldBeSent(ureq, disabledIdentities);
        }

    }

    private void showErrorThatNoEmailCouldBeSent(final UserRequest ureq, List<OLATPrincipal> disabledIdentities) {
        myButtons = new ArrayList<String>();
        myButtons.add(translate("back"));
        String title = "";
        String message = "";
        if (disabledIdentities.size() > 0) {
            title = MailTemplateHelper.getTitleForFailedUsersError(ureq.getLocale());
            message = MailTemplateHelper.getMessageForFailedUsersError(ureq.getLocale(), disabledIdentities);
        } else {
            title = translate("error.title.nousers");
            message = translate("error.msg.nousers");
        }
        // trusted text, no need to escape
        noUsersErrorCtr = activateGenericDialog(ureq, title, message, myButtons, noUsersErrorCtr);
    }

    /**
	 */
    @Override
    public void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == noUsersErrorCtr) {
            if (event.equals(Event.CANCELLED_EVENT)) {
                // user has clicked the close button in the top-right corner
                fireEvent(ureq, Event.CANCELLED_EVENT);
            } else {
                // user has clicked the cancel button
                final int pos = DialogBoxUIFactory.getButtonPos(event);
                if (pos == 0) {
                    // cancel button has been pressed, fire event to parent
                    fireEvent(ureq, Event.CANCELLED_EVENT);
                }
            }
        } else if (contactView.is(source)) {
            if (event == Event.DONE_EVENT) {

                ContactMessage newContactMessage = createContactMessageFromViewData();

                contactUIModel.setContactMessage(newContactMessage);
                contactUIModel.setAttachements(contactView.getAttachments());

                boolean isMessageSent = contactUIModel.sendCurrentMessageToRecipients(contactView.isTcpFrom());

                contactView.setDisplayOnly(true);
                if (isMessageSent) {
                    // do logging
                    ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
                    fireEvent(ureq, Event.DONE_EVENT);
                } else {
                    fireEvent(ureq, Event.FAILED_EVENT);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                fireEvent(ureq, Event.CANCELLED_EVENT);
            }
        }
    }

    private ContactMessage createContactMessageFromViewData() {
        ContactMessage newContactMessage = new ContactMessage(contactUIModel.getFrom());
        List<ContactList> emailToContactLists = contactView.getEmailToContactLists();
        for (ContactList contactList : emailToContactLists) {
            newContactMessage.addEmailTo(contactList);
        }
        newContactMessage.setBodyText(contactView.getBody());
        newContactMessage.setSubject(contactView.getSubject());
        return newContactMessage;
    }

    @Override
    public void event(final UserRequest ureq, final Component source, final Event event) {
        //
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

}
