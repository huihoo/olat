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
package org.olat.lms.core.course.campus.impl.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.UserConstants;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 02.07.2012 <br>
 * 
 * @author cg
 */
public class AbstractMappingByInstitutionalIdentifier {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    BaseSecurity baseSecurity;

    DB dBImpl;

    protected AbstractMappingByInstitutionalIdentifier() {
        super();
    }

    /**
     * @return Mapped identity or null when no identity could be found
     */
    protected Identity tryToMap(final String userProperty, final String queryString) {
        // user property is either matriculation number or employee number
        HashMap<String, String> userProperties = new HashMap<String, String>();
        userProperties.put(userProperty, queryString);
        // search only visible user and not deleted
        List<Identity> results = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);

        // TODO remove after INSTITUTIONALUSERIDENTIFIER has been deleted in db (scheduled for 7.8.x)
        // try again with generic INSTITUTIONALUSERIDENTIFIER
        if (results.isEmpty()) {
            userProperties = new HashMap<String, String>();
            userProperties.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, queryString);
            // search only visible user and not deleted
            results = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
        }

        commitDBImplTransaction();
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            log.warn("tryToMap " + userProperties.keySet().iterator().next() + "=" + queryString + " and found more than one matching identity!");
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private void commitDBImplTransaction() {
        if (dBImpl == null) {
            dBImpl = DBFactory.getInstance(false);
        }
        dBImpl.intermediateCommit();
    }

}
