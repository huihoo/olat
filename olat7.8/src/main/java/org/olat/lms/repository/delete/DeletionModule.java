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

package org.olat.lms.repository.delete;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.commons.configuration.SystemPropertiesLoader;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO:cg Documentation Initial Date: 15.06.2006 <br>
 * 
 * @author Christian Guretzki
 */
public class DeletionModule extends AbstractOLATModule {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String CONF_ARCHIVE_ROOT_PATH = "archiveRootPath";
    private static final String CONF_DELETE_EMAIL_RESPONSE_TO_USER_NAME = "deleteEmailResponseToUserName";
    private static final String CONF_ADMIN_USER_NAME = "adminUserName";
    private static final String DEFAULT_ADMIN_USERNAME = "administrator";
    private String archiveRootPath;
    private String emailResponseTo;
    private Identity adminUserIdentity;
    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    private UserService userService;

    /**
     * [used by spring]
     */
    private DeletionModule(String configuredArchiveDir, String configuredUserDataDir) {
        log.debug("Constructor");
        archiveRootPath = configuredArchiveDir;
        if (!StringHelper.containsNonWhitespace(archiveRootPath)) {
            // if archiveDir is not configured use default: ${userdata.dir}/deleted_archive
            String userDataDir = configuredUserDataDir;
            if (!StringHelper.containsNonWhitespace(userDataDir)) {
                userDataDir = SystemPropertiesLoader.USERDATA_DIR_DEFAULT;
            }

            archiveRootPath = userDataDir + File.separator + "deleted_archive";
        }
    }

    @Override
    protected void initDefaultProperties() {
        log.debug("initDefaultProperties");
    }

    public void initialize() {
        // initialization (archiveRootPath) is done in constructor
    }

    /**
     * @return Returns the archiveRootPath.
     */
    public String getArchiveRootPath() {
        return archiveRootPath;
    }

    public static String getArchiveDatePath() {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }

    public Identity getAdminUserIdentity() {
        if (adminUserIdentity != null) {
            return adminUserIdentity;
        } else {
            initializeAdminUserIdentity();
            return adminUserIdentity;
        }
    }

    /**
	 * 
	 */
    private synchronized void initializeAdminUserIdentity() {
        if (adminUserIdentity == null) {
            final String adminUserName = getStringConfigParameter(CONF_ADMIN_USER_NAME, "administrator", false);
            this.log.info("initialize adminUserName=" + adminUserName);
            if (adminUserName != null) {
                this.log.info("lookup for  adminUserName=" + adminUserName);
                adminUserIdentity = baseSecurity.findIdentityByName(adminUserName);
            } else {
                this.log.info("lookup for  UserName=" + DEFAULT_ADMIN_USERNAME);
                adminUserIdentity = baseSecurity.findIdentityByName(DEFAULT_ADMIN_USERNAME);
            }
            log.debug("adminUserIdentity=" + adminUserIdentity);
        }
    }

    @Override
    protected void initFromChangedProperties() {
        // TODO Auto-generated method stub

    }

}
