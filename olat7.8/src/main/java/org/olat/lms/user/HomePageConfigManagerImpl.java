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

package org.olat.lms.user;

import java.io.File;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.xml.XStreamHelper;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

import com.thoughtworks.xstream.XStream;

/**
 * Description: <br>
 * TODO: alex Class Description for HomePageConfigManagerImpl
 * <P>
 * Initial Date: Jun 3, 2005 <br>
 * 
 * @author Alexander Schneider
 */
public class HomePageConfigManagerImpl extends BasicManager implements HomePageConfigManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * Warning, do not edit aliases, they are used in persisted files
     */
    private static final String CONFIG_ALIAS_LEGACY = "org.olat.user.HomePageConfig";
    private static final String CONFIG_ALIAS = "HomePageConfig";

    private static HomePageConfigManagerImpl INSTANCE;
    private XStream xStream;

    /**
     * [spring]
     */
    private HomePageConfigManagerImpl() {
        INSTANCE = this;
        xStream = XStreamHelper.createXStreamInstance();
        xStream.alias(CONFIG_ALIAS_LEGACY, HomePageConfig.class);
        xStream.alias(CONFIG_ALIAS, HomePageConfig.class);
    }

    /**
     * Singleton pattern
     * 
     * @return instance
     */
    public static HomePageConfigManager getInstance() {
        return INSTANCE;
    }

    /**
     * @param userName
     * @return homePageConfig
     */
    @Override
    public HomePageConfig loadConfigFor(final String userName) {
        HomePageConfig retVal = null;
        File configFile = getConfigFile(userName);
        if (!configFile.exists()) {
            // config file does not exist! create one, init the defaults, save it.
            retVal = loadAndSaveDefaults(userName);
        } else {
            // file exists, load it with XStream, resolve version
            try {
                final Object tmp = XStreamHelper.readObject(xStream, configFile);
                if (tmp instanceof HomePageConfig) {
                    retVal = (HomePageConfig) tmp;
                    retVal.resolveVersionIssues();
                    if (!retVal.hasResourceableId()) {
                        retVal.setResourceableId(new Long(CodeHelper.getForeverUniqueID()));
                    }
                    configFile = null;
                    saveConfigTo(userName, retVal);
                }
            } catch (final Exception e) {
                log.error("Error while loading homepage config from path::" + configFile.getAbsolutePath() + ", fallback to default configuration", e);
                if (configFile.exists()) {
                    configFile.delete();
                }
                retVal = loadAndSaveDefaults(userName);
                // show message to user
            }
        }
        return retVal;
    }

    /**
     * Private helper to load and create a default homepage configuration
     * 
     * @param userName
     * @return
     */
    private HomePageConfig loadAndSaveDefaults(final String userName) {
        HomePageConfig retVal;
        retVal = new HomePageConfig();
        retVal.initDefaults();
        retVal.setResourceableId(new Long(CodeHelper.getForeverUniqueID()));
        saveConfigTo(userName, retVal);
        return retVal;
    }

    /**
     * @param userName
     * @param homePageConfig
     */
    @Override
    public void saveConfigTo(final String userName, final HomePageConfig homePageConfig) {
        homePageConfig.setUserName(userName);
        final File configFile = getConfigFile(userName);
        XStreamHelper.writeObject(xStream, configFile, homePageConfig);
    }

    /**
     * the configuration is saved in the user home
     * 
     * @param userName
     * @return the configuration file
     */
    static File getConfigFile(final String userName) {
        final File userHomePage = getUserHomePageDir(userName);

        /*
         * String pathHome = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(userName); File userHome = new File(pathHome); if (!userHome.exists())
         * userHome.mkdir();
         */
        final File homePageConfigFile = new File(userHomePage, HomePageConfigManager.HOMEPAGECONFIG_XML);
        return homePageConfigFile;
    }

    private static File getUserHomePageDir(final String userName) {
        final String pathHomePage = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHomePage(userName);
        final File userHomePage = new File(pathHomePage);
        userHomePage.mkdirs();
        return userHomePage;
    }

    /**
     * Delete home-page config-file of a certain user.
     * 
     */
    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        getConfigFile(identity.getName()).delete();
        FileUtils.deleteDirsAndFiles(getUserHomePageDir(identity.getName()), true, true);
        log.debug("Homepage-config file and homepage-dir deleted for identity=" + identity);
    }

}
