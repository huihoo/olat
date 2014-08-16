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

import java.util.Locale;

/**
 * serves as parameter object comming from presentation
 * 
 * Initial Date: 01.11.2011 <br>
 * 
 * @author Branislav Balaz
 */
public class EmailParameterObject {

    private final Locale locale;
    private final EPMapPolicy ePMapPolicy;
    private final String mailSubject;
    private final String mailBodyText;
    private final String contactListNameNonSingleRecipient;
    private final boolean invitationSend;

    public EmailParameterObject(Locale locale, EPMapPolicy ePMapPolicy, String mailSubject, String mailBodyText, String contactListNameNonSingleRecipient,
            boolean invitationSend) {
        this.locale = locale;
        this.ePMapPolicy = ePMapPolicy;
        this.mailSubject = mailSubject;
        this.mailBodyText = mailBodyText;
        this.contactListNameNonSingleRecipient = contactListNameNonSingleRecipient;
        this.invitationSend = invitationSend;
    }

    public Locale getLocale() {
        return locale;
    }

    public EPMapPolicy getePMapPolicy() {
        return ePMapPolicy;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public String getMailBodyText() {
        return mailBodyText;
    }

    public String getContactListNameNonSingleRecipient() {
        return contactListNameNonSingleRecipient;
    }

    public boolean isInvitationSend() {
        return invitationSend;
    }

    public boolean isInvitationType() {
        return ePMapPolicy.getType().equals(EPMapPolicy.Type.invitation);
    }

}
