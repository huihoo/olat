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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.data.filebrowser;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.version.FolderVersioningConfigurator;
import org.olat.system.commons.Settings;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.commons.configuration.SystemPropertiesLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 13.11.2002
 * 
 * @author Mike Stock
 */
public class FolderModule extends AbstractOLATModule {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CONFIG_ROOT = "Root";
    private static final String CONFIG_LIMITULMB = "LimitULMB";
    private static final String CONFIG_QUOTAMB = "QuotaMB";
    @Autowired
    private FolderVersioningConfigurator versioning;

    /**
     * [used by spring]
     */
	private FolderModule(String configuredFolderRootDir, String configuredUserDataDir) {
		// Set folder root
		String folderRootDir = configuredFolderRootDir;
		if (!StringHelper.containsNonWhitespace(folderRootDir)) {
			// if folderRootDir is not configured use default: ${userdata.dir}/bcroot
			String userDataDir = configuredUserDataDir;
			if (!StringHelper.containsNonWhitespace(userDataDir)) {
				userDataDir = SystemPropertiesLoader.USERDATA_DIR_DEFAULT;
			}
			
			folderRootDir = userDataDir + File.separator + "bcroot";
    }

        if (Settings.isJUnitTest()) {
            // use dummy directory for junit testcases to not conflict with actual data
            // on current server. someone may start junit test and not realize that this
            // can have side effects to a running instance on the same server...
			FolderConfig.setFolderRoot(folderRootDir + "_junittest");
        } else {
			FolderConfig.setFolderRoot(folderRootDir);
        }

        log.info("Folder root set to '" + FolderConfig.getCanonicalRoot() + "'.");
	}

	@Override
	protected void initDefaultProperties() {
        // Set maximum upload filesize
        int maxULMB = getIntConfigParameter(CONFIG_LIMITULMB, 100);
        FolderConfig.setLimitULKB(maxULMB * 1024);

        log.info("Maximum file upload size set to " + FolderConfig.getLimitULKB() + " KB.");

        // Set default quotas
        int quotaMB = getIntConfigParameter(CONFIG_QUOTAMB, 100);
        FolderConfig.setDefaultQuotaKB(quotaMB * 1024);
        log.info("Default user quota set to " + FolderConfig.getDefaultQuotaKB() + " KB.");

        // create tmp directory
        File fTmp = new File(FolderConfig.getCanonicalTmpDir());
        fTmp.mkdirs();

    }

    @Override
    protected void initFromChangedProperties() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        FolderConfig.setVersioningConfigurator(versioning);
    }

}
