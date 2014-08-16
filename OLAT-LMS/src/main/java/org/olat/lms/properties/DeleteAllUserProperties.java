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
package org.olat.lms.properties;

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.properties.PropertyImpl;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UserDataDeletable implementation for cleanup of user properties
 * 
 * <P>
 * Initial Date: 07.07.2011 <br>
 * 
 * @author guido
 */
@Component
public class DeleteAllUserProperties implements UserDataDeletable {

    protected DeleteAllUserProperties() {
    }

    @Autowired
    PropertyService propertyService;
    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Delete all properties of a certain identity.
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        final List<PropertyImpl> userProperterties = propertyService.listProperties(identity);
        for (PropertyImpl property : userProperterties) {
            propertyService.deleteProperty(property);
        }
        log.debug("All properties deleted for identity=" + identity);
    }

}
