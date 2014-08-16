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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.data.user;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.IdentityImpl;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * <h3>Description:</h3> This implementation of the user manager manipulates user objects based on a hibernate implementation
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com, Christian Guretzki
 */
@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    DB db;

    /**
     * [spring]
     */
    private UserDaoImpl() {
    }

    /**
	 * 
	 */
    @Override
    public User createUser(final String firstName, final String lastName, final String eMail) {
        User newUser = new UserImpl(firstName, lastName, eMail);
        return newUser;
    }

    /**
	 */
    @Override
    public User createAndPersistUser(final String firstName, final String lastName, final String email) {
        User user = createUser(firstName, lastName, email);
        db.saveObject(user);
        return user;
    }

    /**
	 */
    // Ist dies Methode am richtigen Ort ? Es wird ja eine Identity gesucht und nicht ein User
    @Override
    public Identity findIdentityByEmail(final String email) {
        if (!MailHelper.isValidEmailAddress(email)) {
            throw new AssertException("Identity cannot be searched by email, if email is not valid. Used address: " + email);
        }

        final StringBuilder sb = new StringBuilder("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
                .append(" inner join identity.user user ").append(" where ");

        // search email
        final StringBuilder emailSb = new StringBuilder(sb);
        emailSb.append(" user.properties['").append(UserConstants.EMAIL).append("'] =:email");
        final DBQuery emailQuery = db.createQuery(emailSb.toString());
        emailQuery.setString("email", email);
        final List<Identity> identities = emailQuery.list();
        if (identities.size() > 1) {
            throw new AssertException("more than one identity found with email::" + email);
        }

        // search institutional email
        final StringBuilder institutionalSb = new StringBuilder(sb);
        institutionalSb.append(" user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("'] =:email");
        final DBQuery institutionalQuery = db.createQuery(institutionalSb.toString());
        institutionalQuery.setString("email", email);
        final List<Identity> instIdentities = institutionalQuery.list();
        if (instIdentities.size() > 1) {
            throw new AssertException("more than one identity found with institutional-email::" + email);
        }

        // check if email found in both fields && identity is not the same
        if ((identities.size() > 0) && (instIdentities.size() > 0) && (identities.get(0) != instIdentities.get(0))) {
            throw new AssertException("found two identites with same email::" + email + " identity1=" + identities.get(0) + " identity2=" + instIdentities.get(0));
        }
        if (identities.size() == 1) {
            return identities.get(0);
        }
        if (instIdentities.size() == 1) {
            return instIdentities.get(0);
        }
        return null;
    }

    /**
	 */
    @Override
    public User findUserByEmail(final String email) {
        if (log.isDebugEnabled()) {
            log.debug("Trying to find user with email '" + email + "'");
        }

        final Identity ident = findIdentityByEmail(email);
        // if no user found return null
        if (ident == null) {
            if (log.isDebugEnabled()) {
                log.debug("Could not find user '" + email + "'");
            }
            return null;
        }
        return ident.getUser();
    }

    @Override
    public boolean userExist(final String email) {
        final StringBuilder sb = new StringBuilder("select distinct count(user) from ").append(UserImpl.class.getName()).append(" user where ");

        // search email
        final StringBuilder emailSb = new StringBuilder(sb);
        emailSb.append(" user.properties['").append(UserConstants.EMAIL).append("'] =:email");
        final DBQuery emailQuery = db.createQuery(emailSb.toString());
        emailQuery.setString("email", email);
        Number count = (Number) emailQuery.uniqueResult();
        if (count.intValue() > 0) {
            return true;
        }
        // search institutional email
        final StringBuilder institutionalSb = new StringBuilder(sb);
        institutionalSb.append(" user.properties['").append(UserConstants.INSTITUTIONALEMAIL).append("'] =:email");
        final DBQuery institutionalQuery = db.createQuery(institutionalSb.toString());
        institutionalQuery.setString("email", email);
        count = (Number) institutionalQuery.uniqueResult();
        if (count.intValue() > 0) {
            return true;
        }
        return false;
    }

    /**
	 */
    @Override
    public User loadUserByKey(final Long key) {
        return (UserImpl) db.loadObject(UserImpl.class, key);
        // User not loaded yet (lazy initialization). Need to access
        // a field first to really load user from database.
    }

    /**
	 */
    @Override
    public void updateUser(final User usr) {
        if (usr == null) {
            throw new AssertException("User object is null!");
        }
        db.updateObject(usr);
    }

    /**
	 */
    @Override
    public void saveUser(final User user) {
        db.saveObject(user);
    }

    /**
	 */
    @Override
    public boolean updateUserFromIdentity(final Identity identity) {
        this.updateUser(identity.getUser());
        return true;
    }

}
