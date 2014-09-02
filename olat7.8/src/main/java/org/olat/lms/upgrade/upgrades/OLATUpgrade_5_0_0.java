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

package org.olat.lms.upgrade.upgrades;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Roles;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrades for OLAT 5.0
 * <P>
 * Initial Date: Aug 8, 2006 <br>
 * 
 * @author guido
 */
public class OLATUpgrade_5_0_0 extends OLATUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_5.0.0";
    private static final String TASK_DELETE_UNREFERENCED_REPOENTRIERS = "Delete all repository entries that do not have a reference in the database";
    private static final String TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE = "IM and GUI preferences properties deleted";

    /**
	 */
    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
	 */
    @Override
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        return false;
    }

    /**
	 */
    @Override
    public boolean doPostSystemInitUpgrade(final UpgradeManager upgradeManager) {
        UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (uhd == null) {
            // has never been called, initialize
            uhd = new UpgradeHistoryData();
        } else {
            if (uhd.isInstallationComplete()) {
                return false;
            }
        }
        cleanRepositoryAndDeleteUnreferencedEntries(upgradeManager, uhd);
        cleanupIMAndGUIPreferencesProperties(upgradeManager, uhd);
        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    private void cleanRepositoryAndDeleteUnreferencedEntries(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        /**
         * Due to a bug which was introduced in olat 4.1 there are possile zombie repository folders left on the disk. This can happen if someone tried to create a repo
         * entry out of an existing file like a SCORM zip or an IMS content package and the file was not accepted due to errors in its structure.
         */

        if (!uhd.getBooleanDataValue(TASK_DELETE_UNREFERENCED_REPOENTRIERS)) {
            // begin clean up

            final String bcrootPath = FolderConfig.getCanonicalRoot();
            final File bcRootDir = new File(bcrootPath);
            final File repository = new File(bcRootDir, "repository");
            if (repository.exists()) {
                final String[] repositoryFoldersAndFilesOnDisk = repository.list();
                final List repositoryFoldersOnDisk = new ArrayList(repositoryFoldersAndFilesOnDisk.length);
                // filder for directories only as there are images as well in the repo folder
                for (int i = 0; i < repositoryFoldersAndFilesOnDisk.length; i++) {
                    final String repoId = repositoryFoldersAndFilesOnDisk[i];
                    if (new File(repository, repoId).isDirectory()) {
                        repositoryFoldersOnDisk.add(repositoryFoldersAndFilesOnDisk[i]);
                    }
                }

                // get all repository entries
                final Roles roles = new Roles(true, true, true, true, false, true, false);
                final List inDatabase = RepositoryServiceImpl.getInstance().genericANDQueryWithRolesRestriction(null, null, null, null, roles, null);

                final Set inDatabaseIDs = new HashSet(inDatabase.size());
                for (final Iterator iter = inDatabase.iterator(); iter.hasNext();) {
                    final RepositoryEntry element = (RepositoryEntry) iter.next();
                    inDatabaseIDs.add(element.getOlatResource().getResourceableId());
                }

                // deleting all that are in repositoryFoldersOnDisk and not in the
                // inDatabaseIds
                for (final Iterator iter = repositoryFoldersOnDisk.iterator(); iter.hasNext();) {
                    final String rescourcableId = (String) iter.next();
                    try {
                        if (!inDatabaseIDs.contains(Long.valueOf(rescourcableId))) {
                            FileUtils.deleteDirsAndFiles(new File(repository, rescourcableId), true, true);
                            log.info("Audit:Deleting unreferenced folder in repository with id:" + rescourcableId);
                        }
                    } catch (final NumberFormatException e) {
                        log.info("Audit:Could not delete unreferenced folder in repository with id:" + rescourcableId);
                    }
                }

            } // end file exists

            // clean up finished
            uhd.setBooleanDataValue(TASK_DELETE_UNREFERENCED_REPOENTRIERS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }

    }

    private void cleanupIMAndGUIPreferencesProperties(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        // Due to the package refactoring (separation of classes within framework and olat) the user settings for Instant Messaging and GUI preferences have to be deleted

        /**
         * <org.olat.util.prefs.ImPreferences> --> <org.olat.instantMessaging.ImPreferences> <org.olat.util.prefs.GuiPreferences> --> <org.olat.preferences.DbPrefs>
         */

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE)) {
            final String query = "delete from o_property where name ='guipreferences' or name = 'impreferences';";
            executePlainSQLDBStatement(query, upgradeManager.getDataSource());
            uhd.setBooleanDataValue(TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE, true);

            log.info("Audit:+-------------------------------------------+");
            log.info("Audit:+... Deleting all IM and GUI preferences ...+");
            log.info("Audit:+-------------------------------------------+");

            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    /**
	 */
    @Override
    public String getAlterDbStatements() {
        return null; // till 6.1 was manual upgrade
    }

}
