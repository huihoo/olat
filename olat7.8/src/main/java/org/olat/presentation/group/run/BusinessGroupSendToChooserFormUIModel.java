/**
 * 
 */
package org.olat.presentation.group.run;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.system.mail.ContactList;

/**
 * @author patrick
 * 
 */
public class BusinessGroupSendToChooserFormUIModel {

    private ContactMessage contactMessage;

    public BusinessGroupSendToChooserFormUIModel(Identity fromIdentity, GroupParameter ownerGroup, GroupParameter participantGroup, GroupParameter waitingList) {
        this.contactMessage = new ContactMessage(fromIdentity);
        if (ownerGroup != null) {
            contactMessage.addEmailTo(ownerGroup.asContactList());
        }
        if (participantGroup != null) {
            contactMessage.addEmailTo(participantGroup.asContactList());
        }
        if (waitingList != null) {
            contactMessage.addEmailTo(waitingList.asContactList());
        }
    }

    public ContactMessage getContactMessage() {
        return contactMessage;
    }

    public static class GroupParameter {

        private List<Identity> groupMemberList;
        private String translatedContactListName;

        public GroupParameter(List<Identity> groupMemberList, String translatedContactListName) {
            if (groupMemberList == null || translatedContactListName == null) {
                throw new NullPointerException("no null argument allowed");
            }
            this.groupMemberList = groupMemberList;
            this.translatedContactListName = translatedContactListName;
        }

        public GroupParameter(List<Identity> allMembersList, List<Long> selectedGroupMemberKeys, String translatedContactListName) {
            this(allMembersList, translatedContactListName);
            if (selectedGroupMemberKeys == null) {
                throw new NullPointerException("no null argument allowed");
            }
            this.groupMemberList = removeAllNotSelectedIdentities(allMembersList, selectedGroupMemberKeys);
        }

        private List<Identity> removeAllNotSelectedIdentities(List<Identity> allMembersList, List<Long> selectedGroupMemberKeys) {
            List<Identity> tmpCopy = new ArrayList<Identity>(allMembersList);
            for (Identity identity : tmpCopy) {
                boolean identityIsNotSelected = !selectedGroupMemberKeys.contains(identity.getKey());
                if (identityIsNotSelected) {
                    allMembersList.remove(identity);
                }
            }
            return allMembersList;
        }

        ContactList asContactList() {
            ContactList contactList = new ContactList(translatedContactListName);
            contactList.addAllIdentites(groupMemberList);
            return contactList;
        }

    }

}
