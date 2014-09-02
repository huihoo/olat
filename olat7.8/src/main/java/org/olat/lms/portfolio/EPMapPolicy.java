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

package org.olat.lms.portfolio;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.Policy;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.user.UserService;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * A wrapper to embedded the policies with the same logical boundary (permission and duration)
 * <P>
 * Initial Date: 5 nov. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPMapPolicy {

    private Date to;
    private Date from;
    private Type type = Type.user;

    private Invitation invitation;
    private List<Policy> policies;
    private List<Identity> identities = new ArrayList<Identity>();
    private List<BusinessGroup> groups = new ArrayList<BusinessGroup>();

    public Invitation getInvitation() {
        return invitation;
    }

    public void setInvitation(final Invitation invitation) {
        this.invitation = invitation;
    }

    public List<Policy> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<Policy>();
        }
        return policies;
    }

    public void setPolicies(final List<Policy> policies) {
        this.policies = policies;
    }

    public void addPolicy(final Policy policy) {
        if (policies == null) {
            policies = new ArrayList<Policy>();
        }
        policies.add(policy);
    }

    public Date getTo() {
        return to;
    }

    public void setTo(final Date to) {
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(final Date from) {
        this.from = from;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public Map<String, String> getIdentitiesValue() {
        if (identities == null) {
            return new HashMap<String, String>();
        }

        final Map<String, String> values = new HashMap<String, String>();
        for (final Identity identity : identities) {
            final String login = identity.getName();
            values.put(getUserService().getFirstAndLastname(identity.getUser()), login);
        }
        return values;
    }

    public List<Identity> getIdentities() {
        return identities;
    }

    public void setIdentities(final List<Identity> identities) {
        this.identities = identities;
    }

    public void addIdentities(final List<Identity> identitiesToAdd) {
        if (identities == null) {
            identities = new ArrayList<Identity>();
        }
        identities.addAll(identitiesToAdd);
    }

    public Map<String, String> getGroupsValues() {
        if (groups == null) {
            return new HashMap<String, String>();
        }

        final Map<String, String> values = new HashMap<String, String>();
        for (final BusinessGroup group : groups) {
            values.put(group.getName(), group.getKey().toString());
        }
        return values;
    }

    public List<BusinessGroup> getGroups() {
        return groups;
    }

    public void setGroups(final List<BusinessGroup> groups) {
        this.groups = groups;
    }

    public void addGroup(final BusinessGroup group) {
        if (groups == null) {
            groups = new ArrayList<BusinessGroup>();
        }
        for (final BusinessGroup g : groups) {
            if (g.equalsByPersistableKey(group)) {
                return;
            }
        }
        groups.add(group);
    }

    public enum Type {
        user, group, invitation, allusers;

        public static String[] names() {
            final String[] names = new String[values().length];
            int i = 0;
            for (final Type type : values()) {
                names[i++] = type.name();
            }
            return names;
        }
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
