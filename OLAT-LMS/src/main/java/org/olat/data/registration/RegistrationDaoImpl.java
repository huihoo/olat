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
import java.util.List;

import org.hibernate.Hibernate;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.system.commons.encoder.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:
 * 
 * @author Sabina Jeger, Christian Guretzki
 */
@Repository
public class RegistrationDaoImpl implements RegistrationDao {

    @Autowired
    private DB database;

    private RegistrationDaoImpl() {
    }

    /**
     * A temporary key is created
     * 
     * @param email
     *            address of new user
     * @param ip
     *            address of new user
     * @param action
     *            REGISTRATION or PWCHANGE
     * @return TemporaryKey
     */
    @Override
    public TemporaryKeyImpl createTemporaryKeyByEmail(final String email, final String ip, final String action) {
        TemporaryKeyImpl tk = null;
        // check if the user is already registered
        // we also try to find it in the temporarykey list
        final List tks = database.find("from org.olat.data.registration.TemporaryKeyImpl as r where r.emailAddress = ?", email, Hibernate.STRING);
        if ((tks == null) || (tks.size() != 1)) { // no user found, create a new one
            tk = register(email, ip, action);
        } else {
            tk = (TemporaryKeyImpl) tks.get(0);
        }
        return tk;
    }

    /**
     * deletes a TemporaryKey
     * 
     * @param key
     *            the temporary key to be deleted
     * @return true if successfully deleted
     */
    @Override
    public void deleteTemporaryKey(final TemporaryKeyImpl key) {
        database.deleteObject(key);
    }

    /**
     * returns an existing TemporaryKey by a given email address or null if none found
     * 
     * @param email
     * @return the found temporary key or null if none is found
     */
    @Override
    public TemporaryKeyImpl loadTemporaryKeyByEmail(final String email) {
        final List tks = database.find("from r in class org.olat.data.registration.TemporaryKeyImpl where r.emailAddress = ?", email, Hibernate.STRING);
        if (tks.size() == 1) {
            return (TemporaryKeyImpl) tks.get(0);
        } else {
            return null;
        }
    }

    /**
     * returns an existing list of TemporaryKey by a given action or null if none found
     * 
     * @param action
     * @return the found temporary key or null if none is found
     */
    @Override
    public List<TemporaryKey> loadTemporaryKeyByAction(final String action) {
        final List<TemporaryKey> tks = database.find("from r in class org.olat.data.registration.TemporaryKeyImpl where r.regAction = ?", action, Hibernate.STRING);
        if (tks.size() > 0) {
            return tks;
        } else {
            return null;
        }
    }

    /**
     * Looks for a TemporaryKey by a given registrationkey
     * 
     * @param regkey
     *            the encrypted registrationkey
     * @return the found TemporaryKey or null if none is found
     */
    @Override
    public TemporaryKeyImpl loadTemporaryKeyByRegistrationKey(final String regkey) {
        final List tks = database.find("from r in class org.olat.data.registration.TemporaryKeyImpl where r.registrationKey = ?", regkey, Hibernate.STRING);
        if (tks.size() == 1) {
            return (TemporaryKeyImpl) tks.get(0);
        } else {
            return null;
        }
    }

    /**
     * Creates a TemporaryKey and saves it permanently
     * 
     * @param emailaddress
     * @param ipaddress
     * @param action
     *            REGISTRATION or PWCHANGE
     * @return newly created temporary key
     */
    @Override
    public TemporaryKeyImpl register(final String emailaddress, final String ipaddress, final String action) {
        final String today = new Date().toString();
        final String encryptMe = Encoder.encrypt(emailaddress + ipaddress + today);
        final TemporaryKeyImpl tk = new TemporaryKeyImpl(emailaddress, ipaddress, encryptMe, action);
        database.saveObject(tk);
        return tk;
    }

    /**
     * Get a list of all users that did already confirm the disclaimer
     * 
     * @return
     */
    @Override
    public List<Identity> getIdentitiesWithConfirmedDisclaimer() {
        final List<Identity> identities = database
                .find("select distinct ident from org.olat.data.basesecurity.Identity as ident, org.olat.data.properties.PropertyImpl as prop "
                        + "where prop.identity=ident and prop.category='user' and prop.name='dislaimer_accepted'");
        return identities;
    }

}
