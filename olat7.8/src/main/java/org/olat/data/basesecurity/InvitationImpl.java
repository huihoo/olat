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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.data.basesecurity;

import java.util.UUID;

import org.olat.data.commons.database.PersistentObject;

/**
 * Description:<br>
 * Implementation of Invitation
 * <P>
 * Initial Date: 10 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InvitationImpl extends PersistentObject implements Invitation {

    private String token;
    private String firstName;
    private String lastName;
    private String mail;
    private SecurityGroup securityGroup;

    public InvitationImpl() {
        token = UUID.randomUUID().toString();
        securityGroup = new SecurityGroupImpl();
    }

    @Override
    public String getToken() {
        return token;
    }

    // used by Hibernate
    @SuppressWarnings("unused")
    private void setToken(final String token) {
        this.token = token;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getMail() {
        return mail;
    }

    @Override
    public void setMail(final String mail) {
        this.mail = mail;
    }

    @Override
    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    // // used by Hibernate
    @SuppressWarnings("unused")
    private void setSecurityGroup(final SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof InvitationImpl) {
            final InvitationImpl invitation = (InvitationImpl) obj;
            return getKey().equals(invitation.getKey());
        }
        return false;
    }
}
