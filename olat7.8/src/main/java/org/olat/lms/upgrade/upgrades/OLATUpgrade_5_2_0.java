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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.commons.Formatter;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Upgrades for OLAT 5.2.0
 * 
 * @author Christian Guretzki
 */
public class OLATUpgrade_5_2_0 extends OLATUpgrade {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_5.2.0";
    private static final String TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE = "Migrate login-denied security-group DONE";
    private static final String TASK_INITIALIZE_LAST_LOGIN_DONE = "Initialize identity.lastlogin, group.lastusage DONE";
    private static final String TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE = "v2guipreferences where ajax-beta-on = false deleted";

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
        migrateLoginDeniedSecurityGroup(uhd);
        initializeLastLogin(uhd, upgradeManager);

        logDetailsOfUsersAffectedByV2guipreferencesDeletion(upgradeManager);
        cleanupV2guiPreferences(upgradeManager, uhd);

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);

        return true;
    }

    /**
     * Migrate all user in logon-denied security-group to users with status=login_denied.
     */
    private void migrateLoginDeniedSecurityGroup(final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE)) {
            final BaseSecurity secManager = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            String logonDeniedGroupName = "logondenied";// so far: Constants.GROUP_LOGON_DENIED
            final SecurityGroup loginDeniedGroup = secManager.findSecurityGroupByName(logonDeniedGroupName);
            if (loginDeniedGroup != null) {
                // 1. Migrate users from logon-denied security-group to users with status=login_denied
                final List identitiesList = secManager.getIdentitiesByPowerSearch(null, null, true, new SecurityGroup[] { loginDeniedGroup }, null, null, null, null,
                        null, null, null);
                for (final Iterator iter = identitiesList.iterator(); iter.hasNext();) {
                    final Identity identity = (Identity) iter.next();
                    secManager.saveIdentityStatus(identity, Identity.STATUS_LOGIN_DENIED);
                    secManager.removeIdentityFromSecurityGroup(identity, loginDeniedGroup);
                    log.info("Audit:Identity was in security-group 'Logon_denied' => set status of identity to '" + logonDeniedGroupName + "'; identity=" + identity);
                }

                // 2. Delete named group logon_denied
                secManager.deleteNamedGroup(logonDeniedGroupName);
                // 3. Delete security-group 'Logon_denied'
                secManager.deleteSecurityGroup(loginDeniedGroup);
                log.info("Audit:Delete security-group '" + logonDeniedGroupName + "'");
            }
            uhd.setBooleanDataValue(TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE, true);

            log.info("Audit:+---------------------------------------------------------------------------------+");
            log.info("Audit:+... Migrate all logon-denied users from security-group to status=login_denied ...+");
            log.info("Audit:+................... (details of affected users are listed above)  ...............+");
            log.info("Audit:+---------------------------------------------------------------------------------+");
        }
    }

    private void initializeLastLogin(final UpgradeHistoryData uhd, final UpgradeManager upgradeManager) {

        // Set last-login for all idenitity with last-login == null
        final String initDateLastLogin = Formatter.formatDatetime(new Date());
        final String lastLoginCountQuery = "select count(*) from o_bs_identity where lastlogin IS NULL;";
        final String lastLoginQuery = "update o_bs_identity set lastlogin = '" + initDateLastLogin + "' where lastlogin IS NULL;";
        // Set last-usage for all business-group with last-usage == null
        final String initDateLastUsage = Formatter.formatDatetime(new Date());
        final String lastUsageCountQuery = "select count(*) from o_gp_business where lastusage IS NULL;";
        final String lastUsageQuery = "update o_gp_business set lastusage = '" + initDateLastUsage + "' where lastusage IS NULL;";
        // Set last-usage for all repository-entries with last-usage == null
        final String repositoryLastUsageCountQuery = "select count(*) from o_repositoryentry where lastusage IS NULL;";
        final String repositoryLastUsageQuery = "update o_repositoryentry set lastusage = '" + initDateLastUsage + "' where lastusage IS NULL;";

        int countIdentityLastLoginUpdates;
        int countGroupLastUsageUpdates;
        int countRepositoryLastUsageUpdates;
        try {
            final Connection con = upgradeManager.getDataSource().getConnection();
            final Statement countStmt = con.createStatement();
            ResultSet resultSet = countStmt.executeQuery(lastLoginCountQuery);
            resultSet.next();
            countIdentityLastLoginUpdates = resultSet.getInt(1);
            resultSet = countStmt.executeQuery(lastUsageCountQuery);
            resultSet.next();
            countGroupLastUsageUpdates = resultSet.getInt(1);
            resultSet = countStmt.executeQuery(repositoryLastUsageCountQuery);
            resultSet.next();
            countRepositoryLastUsageUpdates = resultSet.getInt(1);

            final Statement updateStmt = con.createStatement();
            updateStmt.addBatch(lastLoginQuery);
            updateStmt.addBatch(lastUsageQuery);
            updateStmt.addBatch(repositoryLastUsageQuery);
            updateStmt.executeBatch();
        } catch (final SQLException e) {
            log.warn("Could not execute system upgrade sql query. ", e);
            throw new StartupException("Could not execute system upgrade sql query. ", e);
        }

        uhd.setBooleanDataValue(TASK_INITIALIZE_LAST_LOGIN_DONE, true);

        log.info("Audit:+------------------------------------------------------------------------------------+");
        log.info("Audit:+... Initialize all idenitity with last-login == null to start-last-login date    ...+");
        log.info("Audit:+... Update " + countIdentityLastLoginUpdates + " identities with lastlogin-date = '" + initDateLastLogin + "'  ...+");
        log.info("Audit:+... Initialize all business-groups with last-usage == null to date of upgrade    ...+");
        log.info("Audit:+... Update " + countGroupLastUsageUpdates + " groups with lastusage-date = '" + initDateLastUsage + "'  ...+");
        log.info("Audit:+... Initialize all repository-entries with last-usage == null to date of upgrade ...+");
        log.info("Audit:+... Update " + countRepositoryLastUsageUpdates + " repository-entries with lastusage-date = '" + initDateLastUsage + "'  ...+");
        log.info("Audit:+------------------------------------------------------------------------------------+");
    }

    /**
     * Deletes all v2guipreference with textvalues containing '.*ajax-beta-on</string><boolean>false</boolean>'
     */
    private void cleanupV2guiPreferences(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE)) {
            final String query = "delete from o_property where name = 'v2guipreferences' and textvalue like '%ajax-beta-on</string>%<boolean>false%'";

            try {
                Connection con = upgradeManager.getDataSource().getConnection();
                final Statement deleteStmt = con.createStatement();
                deleteStmt.execute(query);

                con.close();
                con = null;

            } catch (final SQLException e) {
                log.warn("Could not execute system upgrade sql query. Query:" + query, e);
                throw new StartupException("Could not execute system upgrade sql query. Query:" + query, e);
            }

            uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE, true);

            log.info("Audit:+---------------------------------------------------------------------------------------+");
            log.info("Audit:+... Deleted all v2guipreferences with textvalues containing 'ajax-beta-on' -> false ...+");
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
            log.warn("Version before 5.2.0 detected! Continue running upgrade for 5.2.0 ...", e);
        }

        final String query = "select u.firstname, u.lastname, u.email from o_property as p, o_bs_identity as i, o_user as u " + "where p.name = 'v2guipreferences' "
                + "and p.textvalue like '%ajax-beta-on</string>%<boolean>false%' " + "and p.identity = i.id " + "and i.fk_user_id = u.user_id;";

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
	 */
    @Override
    public String getAlterDbStatements() {
        return null; // till 6.1 was manual upgrade
    }

}
