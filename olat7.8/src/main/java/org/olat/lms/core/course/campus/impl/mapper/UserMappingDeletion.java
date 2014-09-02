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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.course.campus.SapOlatUserDao;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class UserMappingDeletion implements UserDataDeletable {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    SapOlatUserDao sapOlatUserDao;

    // This method will be called when a OLAT-user is deleted via deletion-manager
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUserData(Identity identity, String newDeletedUserName) {
        log.debug("deleteUserData start");
        List<SapOlatUser> sapUsers = sapOlatUserDao.getSapOlatUserListByOlatUserName(identity.getName());
        for (SapOlatUser sapUser : sapUsers) {
            log.info("Delete sap-olat mapping for '" + sapUser + "'");
            sapOlatUserDao.deleteMapping(sapUser);
        }
    }
}
