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

package org.olat.data.registration;

import java.util.Date;

import org.olat.data.commons.database.PersistentObject;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class TemporaryKeyImpl extends PersistentObject implements TemporaryKey {

    private String emailAddress = null;
    private String ipAddress = null;
    private Date lastModified = null;
    private String registrationKey = null;
    private String regAction = null;
    private boolean mailSent = false;

    /**
	 * 
	 */
    protected TemporaryKeyImpl() {
        super();
    }

    /**
     * Temporary key database object.
     * 
     * @param emailaddress
     * @param ipaddress
     * @param registrationKey
     * @param action
     */
    public TemporaryKeyImpl(final String emailaddress, final String ipaddress, final String registrationKey, final String action) {
        this.emailAddress = emailaddress;
        this.ipAddress = ipaddress;
        this.registrationKey = registrationKey;
        this.regAction = action;
    }

    /**
	 */
    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
	 */
    @Override
    public void setEmailAddress(final String string) {
        emailAddress = string;
    }

    /**
	 */
    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    /**
	 */
    @Override
    public void setIpAddress(final String string) {
        ipAddress = string;
    }

    /**
	 */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
	 */
    public Date getLastModified() {
        return lastModified;
    }

    /**
	 */
    @Override
    public String getRegistrationKey() {
        return registrationKey;
    }

    /**
	 */
    @Override
    public void setRegistrationKey(final String string) {
        registrationKey = string;
    }

    /**
	 */
    @Override
    public boolean isMailSent() {
        return mailSent;
    }

    /**
	 */
    @Override
    public void setMailSent(final boolean b) {
        mailSent = b;
    }

    /**
	 */
    public void setLastModified(final Date date) {
        lastModified = date;
    }

    /**
	 */
    @Override
    public String getRegAction() {
        return regAction;
    }

    /**
	 */
    @Override
    public void setRegAction(final String string) {
        regAction = string;
    }

}
