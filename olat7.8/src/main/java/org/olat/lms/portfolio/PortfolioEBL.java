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
package org.olat.lms.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.InvitationImpl;
import org.olat.data.group.BusinessGroup;
import org.olat.data.user.UserConstants;
import org.olat.lms.core.notification.service.MailMessage;
import org.olat.lms.learn.notification.service.MailMessageLearnService;
import org.olat.lms.user.UserService;
import org.olat.system.mail.ContactList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 19.09.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class PortfolioEBL {

    @Autowired
    UserService userService;
    @Autowired
    BaseSecurity securityManager;
    @Autowired
    MailMessageLearnService mailMessageLearnService;

    /**
     * @param ureq
     * @param wrapper
     * @param mailSubject
     * @param mailBodyText
     * @return
     */
    public PortfolioDataEBL sendMail(EmailParameterObject emailParameterObject) {
        boolean success = false;
        ContactList contactList = createContactList(emailParameterObject);
        final ArrayList<ContactList> clList = new ArrayList<ContactList>();
        clList.add(contactList);

        for (ContactList contact : clList) {
            MailMessage mailMessage = new MailMessage(contact.getEmailsAsStrings(), emailParameterObject.getMailSubject(), emailParameterObject.getMailBodyText(),
                    emailParameterObject.getLocale());
            success = mailMessageLearnService.sendMessage(mailMessage);
        }

        return new PortfolioDataEBL(true, success);
    }

    /**
     * @param identitiesToMail
     * @return
     */
    private ContactList createContactList(EmailParameterObject emailParameterObject) {
        List<Identity> mailRecipients = getMailRecipients(emailParameterObject.getePMapPolicy());
        ContactList contactList;
        if (mailRecipients.size() == 1) {
            contactList = new ContactList(userService.getUserProperty(mailRecipients.get(0).getUser(), UserConstants.EMAIL, emailParameterObject.getLocale()));
        } else {
            contactList = new ContactList(emailParameterObject.getContactListNameNonSingleRecipient());
        }
        contactList.addAllIdentites(mailRecipients);
        if (emailParameterObject.isInvitationType()) {
            contactList.add(emailParameterObject.getePMapPolicy().getInvitation().getMail());
        }
        return contactList;
    }

    /**
     * @param type
     * @return
     */
    private List<Identity> getMailRecipients(EPMapPolicy ePMapPolicy) {
        List<Identity> mailRecipients = new ArrayList<Identity>();

        EPMapPolicy.Type shareType = ePMapPolicy.getType();
        if (shareType.equals(EPMapPolicy.Type.allusers)) {
            return mailRecipients;
        } else if (shareType.equals(EPMapPolicy.Type.invitation)) {
            return mailRecipients;
        } else if (shareType.equals(EPMapPolicy.Type.group)) {
            final List<BusinessGroup> groups = ePMapPolicy.getGroups();
            for (final BusinessGroup businessGroup : groups) {
                final List<Identity> partIdents = securityManager.getIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup());
                mailRecipients.addAll(partIdents);
                final List<Identity> ownerIdents = securityManager.getIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup());
                mailRecipients.addAll(ownerIdents);
            }
        } else if (shareType.equals(EPMapPolicy.Type.user)) {
            mailRecipients = ePMapPolicy.getIdentities();
        }
        return mailRecipients;
    }

    public Invitation createAndPersistInvitationWhenNoOneExits(EPMapPolicy ePMapPolicy) {
        Invitation invitation = ePMapPolicy.getInvitation();
        if (invitation == null) {
            invitation = new InvitationImpl();
            ePMapPolicy.setInvitation(invitation);
        }
        return invitation;
    }

}
