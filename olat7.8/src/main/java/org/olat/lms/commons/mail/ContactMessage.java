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

package org.olat.lms.commons.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.system.mail.ContactList;
import org.olat.system.mail.MailHelper;
import org.olat.system.mail.MailerResult;
import org.olat.system.security.OLATPrincipal;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: Jan 22, 2006 <br>
 * 
 * @author patrick
 */
public class ContactMessage {

    private HashMap<String, ContactList> contactLists = new HashMap<String, ContactList>();
    private List<OLATPrincipal> disabledIdentities;
    private String bodyText;
    private String subject;
    private Identity from;

    /**
     * @param from
     */
    public ContactMessage(Identity from) {
        this.from = from;
        disabledIdentities = new ArrayList<OLATPrincipal>();
    }

    public Identity getFrom() {
        return this.from;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public String getBodyText() {
        return bodyText;
    }

    /**
     * add a ContactList as EmailTo:
     * 
     * @param emailList
     */
    public void addEmailTo(ContactList emailList) {
        emailList = cleanListFromDisabledEmailAddresses(emailList);
        if (emailList != null) {
            if (contactLists.containsKey(emailList.getName())) {
                // there is already a ContactList with this name...
                ContactList existing = (ContactList) contactLists.get(emailList.getName());
                // , merge their values.
                existing.add(emailList);
            } else {
                // a new ContactList, put it into contactLists
                contactLists.put(emailList.getName(), emailList);
            }
        }
    }

    /**
     * @return Returns the disabledIdentities.
     */
    public List<OLATPrincipal> getDisabledIdentities() {
        return disabledIdentities;
    }

    private ContactList cleanListFromDisabledEmailAddresses(ContactList emailList) {
        List<OLATPrincipal> identityEmails = new ArrayList<OLATPrincipal>(emailList.getIdentityEmails().values());
        for (OLATPrincipal identity : identityEmails) {
            List<OLATPrincipal> singleIdentityList = new ArrayList<OLATPrincipal>();
            singleIdentityList.add(identity);
            MailerResult result = new MailerResult();
            if (MailHelper.removeDisabledMailAddress(singleIdentityList, result).getFailedIdentites().size() > 0) {
                emailList.remove(identity);
                if (!disabledIdentities.contains(identity)) {
                    disabledIdentities.add(identity);
                }
            }
        }
        if (emailList.getIdentityEmails().size() == 0 && emailList.getEmailsAsStrings().size() == 0) {
            emailList = null;
        }
        return emailList;
    }

    /**
     * a List with ContactLists as elements is returned
     * 
     * @return
     */
    public List<ContactList> getEmailToContactLists() {
        return new ArrayList<ContactList>(contactLists.values());
    }

    public boolean hasAtLeastOneAddress() {
        boolean hasAtLeastOneAddress = false;
        List<ContactList> recipList = getEmailToContactLists();
        if (recipList != null && recipList.size() > 0) {
            for (final Iterator iter = recipList.iterator(); iter.hasNext();) {
                final ContactList cl = (ContactList) iter.next();
                if (!hasAtLeastOneAddress && cl != null && cl.getEmailsAsStrings().size() > 0) {
                    hasAtLeastOneAddress = true;
                }
            }
        }
        return hasAtLeastOneAddress;
    }

}
