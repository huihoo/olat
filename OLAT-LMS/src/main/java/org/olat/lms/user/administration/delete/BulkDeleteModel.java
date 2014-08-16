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
package org.olat.lms.user.administration.delete;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Given an list with usernames, it finds all deletable identities and the list with deletable usernames and the others (not found or already deleted).
 * 
 * @author lavinia
 */
public class BulkDeleteModel {

    private List<Identity> deletableIdentities;
    private List<String> foundUsernames;
    private List<String> notFoundUsername;

    public BulkDeleteModel(final String[] usernames) {
        deletableIdentities = new ArrayList<Identity>();
        foundUsernames = new ArrayList<String>();
        notFoundUsername = new ArrayList<String>();

        getBulkDeleteModel(usernames);
    }

    /**
     * @param usernames
     */
    private void getBulkDeleteModel(final String[] usernames) {
        for (final String login : usernames) {
            if (login.equals("")) {
                continue;
            }
            final Identity ident = getBaseSecurity().findIdentityByName(login);
            if (ident == null || ident.getStatus().intValue() == Identity.STATUS_DELETED.intValue()) {
                addNotFoundUsername(login);
            } else {
                addDeletable(login, ident);
            }
        }
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    public void addDeletable(String userName, Identity identity) {
        if (!foundUsernames.contains(userName)) {
            foundUsernames.add(userName);
            deletableIdentities.add(identity);
        }
    }

    public void addNotFoundUsername(String userName) {
        notFoundUsername.add(userName);
    }

    public boolean hasDeletable() {
        return deletableIdentities.size() > 0;
    }

    public boolean hasNotFound() {
        return notFoundUsername.size() > 0;
    }

    public List<Identity> getDeletable() {
        return deletableIdentities;
    }

    public List<String> getFoundUsernames() {
        return foundUsernames;
    }

    public List<String> getNotFoundUsernames() {
        return notFoundUsername;
    }
}
