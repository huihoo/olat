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

import org.apache.log4j.Logger;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrades for OLAT 6.0.0
 * <P>
 * Initial Date: March 12, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_6_0_0 extends OLATUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_6.0.0";
    private static final String TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE = "dropboxsubscription migrated";
    private static final String TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE = "V2GUI preferences properties deleted";

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

        migrateDropboxSubscription(upgradeManager, uhd);
        cleanupV2GUIPreferencesProperties(upgradeManager, uhd);

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    /**
     * Rename all Dropbox Subscription with resname='DropboxController' to 'CourseModule' because they could not be started from portal.
     */
    private void migrateDropboxSubscription(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE)) {
            final String query = "update o_noti_pub set resname='CourseModule' where resname='DropboxController';";
            executePlainSQLDBStatement(query, upgradeManager.getDataSource());
            uhd.setBooleanDataValue(TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE, true);

            log.info("Audit:+---------------------------------------------------------------------------------------+");
            log.info("Audit:+... Migrated all dropbox subscriptions, rename 'DropboxController' to 'CourseModule'...+");
            log.info("Audit:+---------------------------------------------------------------------------------------+");

            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void cleanupV2GUIPreferencesProperties(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        // Due to the YAMLizing of OLAT the GUI preferences have to be deleted

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE)) {
            final String query = "delete from o_property where name ='v2guipreferences';";
            executePlainSQLDBStatement(query, upgradeManager.getDataSource());
            uhd.setBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE, true);

            log.info("Audit:+--------------------------------------+");
            log.info("Audit:+... Deleting all V2GUI preferences ...+");
            log.info("Audit:+--------------------------------------+");

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
