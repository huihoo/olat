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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.olat.data.user.UserConstants;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

/**
 * upgrader for olat version 7.7.2:<br>
 * copy user property 'institutionalUserIdentifier' to either 'institutionalEmployeeNumber' or 'institutionalMatriculationNumber' for all uzh.ch members
 * 
 * <P>
 * Initial Date: 19.07.2012 <br>
 * 
 * @author oliver.buehler@agility-informatik.ch
 */
public final class OLATUpgrade_7_7_2 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String VERSION = "OLAT_7.7.2";

    private static final Pattern employeePattern = Pattern.compile("[0-9]{8}");

    private static final Pattern studentPattern = Pattern.compile("[0-9]{2}-[0-9]{3}-[0-9]{3}");

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
        // nothing to do in pre phase
        return false;
    }

    @Override
    public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
        UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);

        if (uhd == null) {
            // has never been called, initialize
            uhd = new UpgradeHistoryData();
        } else {
            if (uhd.isInstallationComplete()) {
                return false;
            }
        }

        final String taskDone = "user property institutionalUserIdentifier copied";
        if (!uhd.getBooleanDataValue(taskDone)) {
            log.info("Run OLATUpgrade_7_7_2.doPostSystemInitUpgrades...");
            copyUserProperties(upgradeManager, uhd);

            uhd.setBooleanDataValue(taskDone, true);
            uhd.setInstallationComplete(true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
            return true;
        }

        return false;
    }

    private void copyUserProperties(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        boolean migrateData = false;

        final JdbcTemplate template = new JdbcTemplate(upgradeManager.getDataSource());
        final List<Long> migratedItems = template.query(
                "select count(*) migratedItems from o_userproperty where propname in ('institutionalEmployeeNumber','institutionalMatriculationNumber')",
                new LongPropertyMapper("migratedItems"));
        if (migratedItems.get(0) > 0 && !uhd.getBooleanDataValue("ignore already migrated items")) {
            log.info("Already " + migratedItems.get(0) + " migrated items found. Just analyzing data...");
        } else {
            migrateData = true;
        }

        final Set<String> matriculationNumbersSAPRaw = new HashSet<String>();
        final Set<String> matriculationNumbersSAPUnified = new HashSet<String>();
        final Set<String> matriculationNumbersSAPDuplicates = new HashSet<String>();
        final List<String> matriculationNumbersSAP = template.query("select registration_nr from ck_student", new StringPropertyMapper("registration_nr"));
        for (String matriculationNumber : matriculationNumbersSAP) {
            if (!matriculationNumbersSAPRaw.add(matriculationNumber)) {
                matriculationNumbersSAPDuplicates.add(matriculationNumber);
            }
            matriculationNumbersSAPUnified.add(matriculationNumber.replaceAll("-", ""));
        }
        if (!matriculationNumbersSAPDuplicates.isEmpty()) {
            log.info(matriculationNumbersSAPDuplicates.size() + " duplicate matriculationNumbers found.");
        }

        final Set<String> employeeNumbersRaw = new HashSet<String>();
        final Set<String> employeeNumbersTrimmed = new HashSet<String>();
        final Set<String> employeeNumbersRawDuplicates = new HashSet<String>();
        final List<Long> employeeNumbers = template.query("select id from ck_lecturer", new LongPropertyMapper("id"));
        for (Long employeeNumber : employeeNumbers) {
            if (!employeeNumbersRaw.add(employeeNumber.toString())) {
                employeeNumbersRawDuplicates.add(employeeNumber.toString());
            }
            employeeNumbersTrimmed.add(StringUtils.trimLeadingCharacter(employeeNumber.toString(), '0'));
        }

        if (!employeeNumbersRawDuplicates.isEmpty()) {
            log.info(employeeNumbersRawDuplicates.size() + " duplicate employeeNumbers found.");
        }

        final IdentityMapper identityMapper = new IdentityMapper();
        template.query(
                "select fk_user_id, propvalue from o_userproperty where propname = 'institutionalUserIdentifier' and fk_user_id in (select fk_user_id from o_userproperty where propname = 'institutionalName' and propvalue in ('uzh.ch'))",
                identityMapper);
        int counter = 0;
        int employeeCounter = 0;
        int studentCounterSAP = 0;
        int matriculationNumberRaw = 0;
        int matriculationNumberUnified = 0;
        int studentCounter = 0;
        int undefinedCounter = 0;
        for (final Long uzhUserId : identityMapper.userIdentifiers.keySet()) {
            counter++;
            final String userIdentifier = identityMapper.userIdentifiers.get(uzhUserId);
            final String userIdentifierUnified = userIdentifier.replaceAll("-", "");
            if (matriculationNumbersSAPRaw.contains(userIdentifier) || matriculationNumbersSAPUnified.contains(userIdentifierUnified)) {
                studentCounterSAP++;
                if (migrateData) {
                    insertUserProperty(template, uzhUserId, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, userIdentifier);
                }
                if (matriculationNumbersSAPRaw.contains(userIdentifier)) {
                    matriculationNumberRaw++;
                } else {
                    matriculationNumberUnified++;
                }
            } else if (studentPattern.matcher(userIdentifier).matches()) {
                studentCounter++;
                if (migrateData) {
                    insertUserProperty(template, uzhUserId, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, userIdentifier);
                }
            } else if (employeePattern.matcher(userIdentifier).matches()) {
                employeeCounter++;
                if (migrateData) {
                    insertUserProperty(template, uzhUserId, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, userIdentifier);
                }
            } else {
                undefinedCounter++;
                log.warn("OLAT upgrade 7.7.2: User " + uzhUserId + " has unknown '" + UserConstants.INSTITUTIONALUSERIDENTIFIER + "' [" + userIdentifier + "]");
            }

            if (counter % 1000 == 0) {
                log.info(counter + " items out of " + identityMapper.userIdentifiers.size() + " processed...");
            }
        }
        log.info("OLAT upgrade 7.7.2: UZH users processed: " + counter);
        log.info("OLAT upgrade 7.7.2: Students (SAP): " + studentCounterSAP + "[raw=" + matriculationNumberRaw + ", trimmed=" + matriculationNumberUnified + "]");
        log.info("OLAT upgrade 7.7.2: Students (by Pattern dd-ddd-ddd): " + studentCounter + "]");
        log.info("OLAT upgrade 7.7.2: Employees (by Pattern dddddddd): " + employeeCounter);
        log.info("OLAT upgrade 7.7.2: Unknown: " + undefinedCounter);
    }

    private static final void insertUserProperty(final JdbcTemplate template, final Long uzhUserId, final String propName, final String propValue) {
        try {
            template.update("insert into o_userproperty values(?,?,?)", new Object[] { uzhUserId, propName, propValue });
            log.debug("OLAT upgrade 7.7.2: Property '" + propName + "' [" + propValue + "] created for user " + uzhUserId);
        } catch (DataAccessException ex) {
            log.warn("Exception creating user property [user=" + uzhUserId + ", property=" + propName + ", value=" + propValue + "]: " + ex.toString());
            log.debug("Detailed exception:", ex);
        }
    }

    private static final class LongPropertyMapper implements RowMapper<Long> {

        private final String propertyName;

        private LongPropertyMapper(String propName) {
            propertyName = propName;
        }

        @Override
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong(propertyName);
        }

    }

    private static final class StringPropertyMapper implements RowMapper<String> {

        private final String propertyName;

        private StringPropertyMapper(String propName) {
            propertyName = propName;
        }

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(propertyName);
        }

    }

    private final class IdentityMapper implements RowCallbackHandler {

        private Map<Long, String> userIdentifiers = new HashMap<Long, String>();

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            userIdentifiers.put(rs.getLong("fk_user_id"), rs.getString("propvalue"));
        }

    }

}
