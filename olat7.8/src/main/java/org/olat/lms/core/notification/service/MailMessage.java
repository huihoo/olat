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
package org.olat.lms.core.notification.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.commons.i18n.I18nManager;

/**
 * 
 * A message is either explicitly edited by a user or is a default text stored as translated string. <br>
 * The messageBody, attachments and recipient list could be provided by the user. <br/>
 * 
 * Initial Date: 27.09.2012 <br/>
 * 
 * @author lavinia
 */
public class MailMessage {

    private final List<String> toEmailAddresses;
    private String fromEmailAddress;
    private String fromFirstLastName;
    private String fromFirstLastOlatUserName;
    private Locale locale;
    private boolean withCCToSender;
    private final String subject;
    private final String body;
    private final List<File> attachments; // TODO: define a new abstraction Attachment instead of java.io.File

    /**
     * @param recipient
     * @param fromEmail
     * @param withCCToSender
     * @param subject
     * @param body
     * @param attachments
     */
    public MailMessage(List<String> toEmailAddresses, Identity fromIdentity, boolean withCCToSender, String subject, String body, List<File> attachments) {

        this.toEmailAddresses = toEmailAddresses;
        if (fromIdentity != null) {
            this.fromEmailAddress = fromIdentity.getAttributes().getEmail();
            this.fromFirstLastName = getFirstLastName(fromIdentity);
            this.fromFirstLastOlatUserName = getFirstLastOlatUserName(fromIdentity);
            this.locale = getLocaleForIdentity(fromIdentity);
        }

        this.withCCToSender = withCCToSender;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    public MailMessage(String toEmailAddresse, String subject, String body, Locale locale) {
        this(new ArrayList<String>(), null, false, subject, body, new ArrayList<File>());
        this.toEmailAddresses.add(toEmailAddresse);
        this.locale = locale;
    }

    public MailMessage(List<String> toEmailAddresses, String subject, String body, Locale locale) {
        this(toEmailAddresses, null, false, subject, body, new ArrayList<File>());
        this.locale = locale;
    }

    public MailMessage(String toEmailAddresse, Identity fromIdentity, String subject, String body) {
        this(new ArrayList<String>(), null, false, subject, body, new ArrayList<File>());
        this.toEmailAddresses.add(toEmailAddresse);
        if (fromIdentity != null) {
            this.fromEmailAddress = fromIdentity.getAttributes().getEmail();
            this.fromFirstLastName = getFirstLastName(fromIdentity);
            this.locale = getLocaleForIdentity(fromIdentity);
        }
    }

    private String getFirstLastName(Identity identity) {
        return identity.getAttributes().getFirstName() + " " + identity.getAttributes().getLastName();
    }

    private String getFirstLastOlatUserName(Identity identity) {
        return identity.getAttributes().getFirstName() + " " + identity.getAttributes().getLastName() + " (" + identity.getName() + ")";
    }

    private Locale getLocaleForIdentity(Identity identity) {
        Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
        if (locale == null) {
            locale = new Locale("DE");
            // log.warn("getLocaleForIdentity could not find the locale for identity, so uses the default one.");
        }
        return locale;
    }

    public List<String> getToEmailAddresses() {
        return toEmailAddresses;
    }

    public String getCCEmailAddress() {
        if (withCCToSender) {
            return fromEmailAddress;
        }
        return "";
    }

    public boolean hasCC() {
        return withCCToSender;
    }

    public void setNoCC() {
        withCCToSender = false;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public String getFromFirstLastName() {
        return fromFirstLastName;
    }

    public String getFromFirstLastOlatUserName() {
        return fromFirstLastOlatUserName;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        for (String toEmailAddress : toEmailAddresses) {
            builder.append("toEmailAddress", toEmailAddress);
        }

        return builder.toString();
    }

}
