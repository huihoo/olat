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
package org.olat.lms.core.course.campus.impl.syncer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.course.campus.CampusConfiguration;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 12.07.2012 <br>
 * 
 * @author cg
 */
@Component
public class CampuskursCoOwners {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String DELIMITER = ",";

    @Autowired
    CampusConfiguration campusConfiguration;
    @Autowired
    BaseSecurity baseSecurity;

    private List<Identity> identites;

    public List<Identity> getDefaultCoOwners() {
        if (identites != null) {
            return identites;
        } else {
            identites = initCoOwnerIdentities();
            return identites;
        }

    }

    private List<Identity> initCoOwnerIdentities() {
        String defaultCoOwnerUserNamesPropertyValue = campusConfiguration.getDefaultCoOwnerUserNames();
        List<Identity> identites = new ArrayList<Identity>();
        StringTokenizer tok = new StringTokenizer(defaultCoOwnerUserNamesPropertyValue, DELIMITER);
        while (tok.hasMoreTokens()) {
            String identityName = tok.nextToken();
            Identity identity = baseSecurity.findIdentityByName(identityName);
            if (identity != null) {
                if (!identites.contains(identity)) {
                    identites.add(identity);
                }
            } else {
                log.warn("getDefaultCoOwners: Could not found an OLAT identity for username:'" + identityName + "' , check Campuskurs configuration-value:'"
                        + defaultCoOwnerUserNamesPropertyValue + "'");
            }
        }
        return identites;
    }

}
