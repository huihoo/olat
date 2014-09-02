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

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 31.08.2012 <br>
 * 
 * @author aabouc
 */
@Component
public class MappingByFirstNameAndLastName {

    @Autowired
    BaseSecurity baseSecurity;

    public MappingByFirstNameAndLastName() {
        super();
    }

    /**
     * @return Mapped identity or null when no identity could be found
     */
    public Identity tryToMap(String firstName, String lastName) {
        HashMap<String, String> userProperties = new HashMap<String, String>();
        userProperties.put(UserConstants.FIRSTNAME, firstName);
        userProperties.put(UserConstants.LASTNAME, lastName);
        List<Identity> results = baseSecurity.getVisibleIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null);
        DBFactory.getInstance().commitAndCloseSession();
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

}
