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

import java.util.Locale;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Encapsulates email address and locale.
 * 
 * Initial Date: 25.07.2012 <br>
 * 
 * @author lavinia
 */
public class RecipientInfo {

    private final String recipientEmail;
    private final Locale recipientLocale;

    /**
     * @param recipientsEmail
     * @param recipientsLocale
     */
    public RecipientInfo(String recipientsEmail, Locale recipientsLocale) {
        this.recipientEmail = recipientsEmail;
        this.recipientLocale = recipientsLocale;
    }

    public String getRecipientsEmail() {
        return recipientEmail;
    }

    public Locale getRecipientsLocale() {
        return recipientLocale;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RecipientInfo))
            return false;
        RecipientInfo theOther = (RecipientInfo) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getRecipientsEmail(), theOther.getRecipientsEmail());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(29, 57);
        builder.append(getRecipientsEmail());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("recipientsEmail", recipientEmail);
        builder.append("recipientsLocale", recipientLocale);
        return builder.toString();
    }

}
