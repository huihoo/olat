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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class LecturerMappingByPersonalNumber extends AbstractMappingByInstitutionalIdentifier {
    private static final Logger log = LoggerHelper.getLogger();

    public Identity tryToMap(Long personalNr) {
        // append '%' because personal-number starts with 0 e.g. 012345
        Identity mappedIdentity = tryToMap(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, "%" + personalNr.toString());
        if (mappedIdentity != null) {
            String personalNumber = mappedIdentity.getUser().getRawUserProperty(UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER);
            try {
                Long personalNumberAsLong = Long.valueOf(personalNumber);
                if (personalNumberAsLong.equals(personalNr)) {
                    return mappedIdentity;
                }
                log.warn("User-Property as Long (" + personalNumberAsLong + ") has not the same value as lecturer personal-number=" + personalNr);
            } catch (NumberFormatException ex) {
                log.warn("Could not convert personal-number to Long");
            }
        }
        return null;

    }

}
