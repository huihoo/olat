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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrades for OLAT 5.1.0
 * <P>
 * Initial Date: March 1, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_5_1_0 extends OLATUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_5.1.0";
    private static final String TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE = "Publishers and subscribers of returnboxes deleted";

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
        cleanupPublishersAndSubscribersOfReturnBoxes(upgradeManager, uhd);
        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    /**
     * Deletes all publishers and subscribers of the publishertype "ReturnBoxController". The asynchronous notification of users when something changes in their
     * returnboxes is removed, since it is already done synchronously
     */
    private void cleanupPublishersAndSubscribersOfReturnBoxes(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE)) {
            final String query_sub = "delete from o_noti_sub where fk_publisher in (select publisher_id from  o_noti_pub where publishertype='ReturnboxController');";
            final String query_pub = "delete from  o_noti_pub where publishertype='ReturnboxController';";

            Connection con = null;
            Statement deleteStmt = null;
            boolean cleaned = false;

            log.info("Audit:+--------------------------------------------------------------+");
            log.info("Audit:+... Deleting all publishers and subscribers of returnboxes ...+");
            log.info("Audit:+--------------------------------------------------------------+");

            try {
                con = upgradeManager.getDataSource().getConnection();
                deleteStmt = con.createStatement();
                deleteStmt.addBatch(query_sub);
                deleteStmt.addBatch(query_pub);
                deleteStmt.executeBatch();
            } catch (final SQLException e) {
                log.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                log.warn("%%%          Please upgrade your database!          %%%");
                log.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                log.warn("Could not execute system upgrade! Your database does not support the following syntax: 'WHERE experession IN (subquery)'." + "First query: "
                        + query_sub + " Second query: " + query_pub, e);
            } finally {
                try {
                    deleteStmt.close();
                } catch (final SQLException e2) {
                    log.warn("Could not close sql delete statement of system upgrade 5.1.0", e2);
                    throw new StartupException("Could not close sql delete statement of system upgrade 5.1.0", e2);
                } finally {
                    try {
                        con.close();
                    } catch (final SQLException e3) {
                        log.warn("Could not close db connection.", e3);
                        throw new StartupException("Could not close db connection.", e3);
                    }
                }
            }
            cleaned = true;
            uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE, cleaned);

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
