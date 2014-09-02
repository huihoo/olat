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
package org.olat.system.support.mail.service;

/**
 * Contains the common data for an email: to, from, subject, replyTo and cc.
 * 
 * Initial Date: 21.12.2011 <br>
 * 
 * @author guretzki
 */
public abstract class CommonMailTO {

    protected String toMailAddress;
    protected String fromMailAddress;
    protected String subject;
    protected String replyTo;
    protected String ccMailAddress;

    protected CommonMailTO(String toMailAddress, String fromMailAddress, String subject) {
        this.toMailAddress = toMailAddress;
        this.fromMailAddress = fromMailAddress;
        this.subject = subject;
    }

    public void validate() {
        if (toMailAddress == null || toMailAddress.isEmpty()) {
            throw new IllegalArgumentException("toMailAdress is not set.");
        }
        if (fromMailAddress == null || fromMailAddress.isEmpty()) {
            throw new IllegalArgumentException("fromMailAddress is not set.");
        }
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("Mail subject is not set.");
        }
    }

    public String getToMailAddress() {
        return toMailAddress;
    }

    public String getFromMailAddress() {
        return fromMailAddress;
    }

    public String getSubject() {
        return subject;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getCcMailAddress() {
        return ccMailAddress;
    }

    public void setCcMailAddress(String ccMailAddress) {
        this.ccMailAddress = ccMailAddress;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public boolean hasReplyTo() {
        return replyTo != null && !replyTo.isEmpty();
    }

    public boolean hasCcMailAddress() {
        return ccMailAddress != null && !ccMailAddress.isEmpty();
    }

}
