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

package org.olat.lms.upgrade;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.upgrade.upgrades.OLATUpgrade;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 * @author guido
 */
public class UpgradeManagerImpl extends UpgradeManager {

    private static final Logger log = LoggerHelper.getLogger();

    /**
     * used by spring
     */
    private UpgradeManagerImpl() {
        //
    }

    /**
     * Execute the pre system init code of all upgrades in the order as they were configured in the configuration file
     */
    @Override
    public void doPreSystemInitUpgrades() {
        final Iterator<OLATUpgrade> iter = upgrades.iterator();
        OLATUpgrade upgrade = null;
        try {
            while (iter.hasNext()) {
                upgrade = iter.next();
                if (upgrade.doPreSystemInitUpgrade(this)) {
                    log.info("Audit:Successfully installed PreSystemInitUpgrade::" + upgrade.getVersion());
                    // no DB Module is initialized in PreSystemInit State - no intermediate commit necessary.
                }
            }
        } catch (final Throwable e) {
            log.warn("Error upgrading PreSystemInitUpgrade::" + upgrade.getVersion(), e);
            abort(e);
        }
    }

    /**
     * Execute the post system init code of all upgrades in the order as they were configured in the configuration file
     */
    @Override
    public void doPostSystemInitUpgrades() {
        log.info("Run PostSystemInitUpgrades...");
        final Iterator<OLATUpgrade> iter = upgrades.iterator();
        OLATUpgrade upgrade = null;
        try {
            while (iter.hasNext()) {
                upgrade = iter.next();
                if (upgrade.doPostSystemInitUpgrade(this)) {
                    log.info("Audit:Successfully installed PostSystemInitUpgrade::" + upgrade.getVersion());
                }
                // just in case a doPostSystemInitUpgrade did forget it.
                DBFactory.getInstance(false).commitAndCloseSession();
            }
        } catch (final Throwable e) {
            DBFactory.getInstance(false).rollbackAndCloseSession();
            log.warn("Error upgrading PostSystemInitUpgrade::" + upgrade.getVersion(), e);
            abort(e);
        }
    }

    /**
	 */
    @Override
    public void runAlterDbStatements() {
        String dialect = "";
        // only run upgrades on mysql or postgresql
        if (dataSource.getUrl().contains("mysql")) {
            dialect = "mysql";
        } else if (dataSource.getUrl().contains("postgresql")) {
            dialect = "postgresql";
        } else if (dataSource.getUrl().contains("hsqldb")) {
            return;
        } else {
            return;
        }

        Statement statement = null;
        try {

            log.info("Audit:+--------------------------------------------------------------+");
            log.info("Audit:+... DB upgrade: Starting alter DB statements ...+");
            log.info("Audit:+ If it fails, do it manually by applying the content of the alter_X_to_Y.sql files.+");
            log.info("Audit:+ For each file you upgraded to add an entry like this to the [pathToOlat]/olatdata/system/installed_upgrades.xml: +");
            log.info("Audit:+ <entry><string>Database update</string><boolean>true</boolean></entry>+");
            log.info("Audit:+--------------------------------------------------------------+");

            statement = dataSource.getConnection().createStatement();

            final Iterator<OLATUpgrade> iter = upgrades.iterator();
            OLATUpgrade upgrade = null;
            while (iter.hasNext()) {
                upgrade = iter.next();
                final String alterDbStatementsFilename = upgrade.getAlterDbStatements();
                if (alterDbStatementsFilename != null) {
                    UpgradeHistoryData uhd = getUpgradesHistory(upgrade.getVersion());
                    if (uhd == null) {
                        // has never been called, initialize
                        uhd = new UpgradeHistoryData();
                    }

                    if (!uhd.getBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE)) {
                        loadAndExecuteSqlStatements(statement, alterDbStatementsFilename, dialect);
                        uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
                        setUpgradesHistory(uhd, upgrade.getVersion());
                        log.info("Audit:Successfully executed alter DB statements for Version::" + upgrade.getVersion());
                    }

                }
            }

        } catch (final SQLException e) {
            log.error("Could not upgrade your database! Please do it manually and add ", e);
            throw new StartupException("Could not execute alter db statements. Please do it manually.", e);

        } catch (final Throwable e) {
            log.warn("Error executing alter DB statements::", e);
            abort(e);

        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (final SQLException e2) {
                log.warn("Could not close sql statement", e2);
                throw new StartupException("Could not close sql statements.", e2);
            }
        }

    }

    /**
     * load file with alter statements and add to batch
     * 
     * @param statements
     * @param alterDbStatements
     */
    private void loadAndExecuteSqlStatements(final Statement statement, final String alterDbStatements, final String dialect) {
        try {
            final Resource setupDatabaseFile = new ClassPathResource("/database/" + dialect + "/" + alterDbStatements);
            if (!setupDatabaseFile.exists()) {
                throw new StartupException("The database upgrade file was not found on the classpath: " + "/database/" + dialect + "/" + alterDbStatements);
            }
            final InputStream in = setupDatabaseFile.getInputStream();
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            final StringBuilder sb = new StringBuilder();
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (strLine.length() > 1 && (!strLine.startsWith("--") && !strLine.startsWith("#"))) {
                    sb.append(strLine.trim());
                }
            }

            final StringTokenizer tokenizer = new StringTokenizer(sb.toString(), ";");
            String sql = null;
            while (tokenizer.hasMoreTokens()) {
                try {
                    sql = tokenizer.nextToken() + ";".toLowerCase();
                    if (sql.startsWith("update") || sql.startsWith("delete") || sql.startsWith("alter") || sql.startsWith("insert")) {
                        statement.executeUpdate(sql);
                    } else {
                        statement.execute(sql);
                    }
                    log.info("Successfully upgraded database with the following sql: " + sql);
                } catch (final SQLException e) {
                    final String msg = e.getMessage();
                    // stop upgrading database if already done
                    if (e.getMessage() != null
                            && (msg.contains("already exists") || msg.contains("Duplicate") || msg.contains("Can't create table") || msg.contains("column/key exists"))) {
                        log.error("Error while trying to upgrade the database with:(" + sql
                                + "). We will continue with upgrading but check the errors manually! Error says:", e);
                    }
                } catch (final Exception e) {
                    // handle non sql errors
                    log.error("Could not upgrade your database!", e);
                    throw new StartupException("Could not add alter db statements to batch.", e);
                }
            }

            in.close();
        } catch (final FileNotFoundException e1) {
            log.error("could not find deleteDatabase.sql file!", e1);
            abort(e1);
        } catch (final IOException e) {
            log.error("could not read deleteDatabase.sql file!", e);
            abort(e);
        }
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

}
