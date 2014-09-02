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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * Upgrades for OLAT 5.1.1
 * <P>
 * Initial Date: March 12, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_5_1_1 extends OLATUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_5.1.1";
    private static final String TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE = "v2guipreferences deleted";
    private static final String TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE = "languages according iso 936 updated";

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

        logDetailsOfUsersAffectedByV2guipreferencesDeletion(upgradeManager);
        cleanupV2guiPreferences(upgradeManager, uhd);

        logDetailsOfUsersAffectedByLanguageUpdate(upgradeManager);
        updateLanguagesAccordingISO639(upgradeManager, uhd);

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    /**
     * Deletes all v2guipreference with textvalues containing '.*<int>2[0-9]</int>.*', since the feature multiselect reduced the number of table columns
     */
    private void cleanupV2guiPreferences(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE)) {
            final String query = "delete from o_property where name = 'v2guipreferences' and textvalue like '%<int>2_</int>%'";
            executePlainSQLDBStatement(query, upgradeManager.getDataSource());
            uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE, true);

            log.info("Audit:+---------------------------------------------------------------------------------------+");
            log.info("Audit:+... Deleted all v2guipreferences with textvalues containing '.*<int>2[0-9]</int>.*' ...+");
            log.info("Audit:+................... (details of affected users are listed above)  .....................+");
            log.info("Audit:+---------------------------------------------------------------------------------------+");

            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void logDetailsOfUsersAffectedByV2guipreferencesDeletion(final UpgradeManager upgradeManager) {

        final String checkVersionQuery = "select count(*) from o_userproperty";

        try {
            final Connection con = upgradeManager.getDataSource().getConnection();
            final Statement selectStmt = con.createStatement();
            selectStmt.executeQuery(checkVersionQuery);
            return;
        } catch (final SQLException e) {
            log.warn("Version before 5.1.1 detected! Continue running upgrade for 5.1.1 ...", e);
        }

        final String query = "select u.firstname, u.lastname, u.email from o_property as p, o_bs_identity as i, o_user as u " + "where p.name = 'v2guipreferences' "
                + "and p.textvalue like '%<int>2_</int>%' " + "and p.identity = i.id " + "and i.fk_user_id = u.user_id;";
        try {
            final Connection con = upgradeManager.getDataSource().getConnection();
            final Statement selectStmt = con.createStatement();
            final ResultSet res = selectStmt.executeQuery(query);

            while (res.next()) {
                log.info(res.getString(1) + ", " + res.getString(2) + ", " + res.getString(3) + " ");
            }

        } catch (final SQLException e) {
            log.warn("Could not execute system upgrade sql query. Query:" + query, e);
            throw new StartupException("Could not execute system upgrade sql query. Query:" + query, e);
        }
    }

    /**
     * updates all languages codes which are saved in the o_user table according the iso 936
     */
    private void updateLanguagesAccordingISO639(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE)) {
            final String query_cn_zh = "update o_user set language='zh' where language='cn';";
            final String query_cz_cs = "update o_user set language='cs' where language='cz';";
            final String query_dk_da = "update o_user set language='da' where language='dk';";
            final String query_gr_el = "update o_user set language='el' where language='gr';";
            final String query_pe_fa = "update o_user set language='fa' where language='pe';";
            final String query_tc_tr = "update o_user set language='tc' where language='tr';";

            Connection con = null;
            Statement updateStmt = null;

            try {
                con = upgradeManager.getDataSource().getConnection();
                updateStmt = con.createStatement();
                updateStmt.addBatch(query_cn_zh);
                updateStmt.addBatch(query_cz_cs);
                updateStmt.addBatch(query_dk_da);
                updateStmt.addBatch(query_gr_el);
                updateStmt.addBatch(query_pe_fa);
                updateStmt.addBatch(query_tc_tr);

                updateStmt.executeBatch();
            } catch (final SQLException e) {
                log.warn("Could not execute system upgrade sql query composed of : " + query_cn_zh + " and " + query_cz_cs + " and " + query_dk_da + " and "
                        + query_gr_el + " and " + query_pe_fa + " and " + query_tc_tr, e);
                throw new StartupException("Could not execute system upgrade sql query composed of : " + query_cn_zh + " and " + query_cz_cs + " and " + query_dk_da
                        + " and " + query_gr_el + " and " + query_pe_fa + " and " + query_tc_tr, e);
            } finally {
                try {
                    updateStmt.close();
                } catch (final SQLException e2) {
                    log.warn("Could not close sql update statement of system upgrade 5.1.1", e2);
                    throw new StartupException("Could not close sql update statement of system upgrade 5.1.1", e2);
                } finally {
                    try {
                        con.close();
                    } catch (final SQLException e3) {
                        log.warn("Could not close db connection.", e3);
                        throw new StartupException("Could not close db connection.", e3);
                    }
                }
            }

            uhd.setBooleanDataValue(TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE, true);

            log.info("Audit:+---------------------------------------------------+");
            log.info("Audit:+....... updated languages according iso 936 .......+");
            log.info("Audit:+...(details of affected users are listed above) ...+");
            log.info("Audit:+---------------------------------------------------+");

            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void logDetailsOfUsersAffectedByLanguageUpdate(final UpgradeManager upgradeManager) {

        final String checkVersionQuery = "select count(*) from o_userproperty";

        try {
            final Connection con = upgradeManager.getDataSource().getConnection();
            final Statement selectStmt = con.createStatement();
            selectStmt.executeQuery(checkVersionQuery);
            return;
        } catch (final SQLException e) {
            log.warn("Version before 5.1.1 detected! Continue running upgrade for 5.1.1 ...", e);
        }

        final String query = "select u.language, u.firstname, u.lastname, i.name " + "from o_user as u, o_bs_identity as i " + "where i.fk_user_id = u.user_id "
                + "and (language='cn' or language='cz' or language='dk' or language='gr' or language='pe' or language='tc') order by language;";

        try {
            final Connection con = upgradeManager.getDataSource().getConnection();
            final Statement selectStmt = con.createStatement();
            final ResultSet res = selectStmt.executeQuery(query);

            while (res.next()) {
                log.info(res.getString(1) + ", " + res.getString(2) + ", " + res.getString(3) + ", " + res.getString(4) + " ");
            }

        } catch (final SQLException e) {
            log.warn("Could not execute system upgrade sql query. Query:" + query, e);
            throw new StartupException("Could not execute system upgrade sql query. Query:" + query, e);
        }
    }

    /**
	 */
    @Override
    public String getAlterDbStatements() {
        return null; // till 6.1 was manual upgrade
    }

}
