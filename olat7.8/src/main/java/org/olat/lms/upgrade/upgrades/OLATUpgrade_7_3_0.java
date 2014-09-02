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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.properties.PropertyImpl;
import org.olat.data.properties.PropertyManager;
import org.olat.lms.preferences.PreferencesService;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.w3c.dom.Document;

/**
 * upgrader for olat version 7.3.0
 * 
 * <P>
 * Initial Date: 26.07.2011 <br>
 * 
 * @author guido
 */
public class OLATUpgrade_7_3_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_7.3.0";
    private static final String TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE = "V2GUI preferences properties deleted";
    private static final String TASK_MIGRATE_DATA_FROM_GUI_PROPERTIES_DONE = "migrated subscription info from GUI preferences";

    @Autowired
    BaseSecurity security;
    @Autowired
    PreferencesService preferencesService;
    @Autowired
    PropertyManager propertyManager;

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

        migrateDataFromGUIPreferences(upgradeManager, uhd);

        cleanupV2GUIPreferencesProperties(upgradeManager, uhd);
        return true;
    }

    /**
     * @param string
     */
    private void migrateDataFromGUIPreferences(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {

        if (!uhd.getBooleanDataValue(TASK_MIGRATE_DATA_FROM_GUI_PROPERTIES_DONE)) {
            GUIPreferencesParser parser = new GUIPreferencesParser();
            JdbcTemplate template = new JdbcTemplate(upgradeManager.getDataSource());
            SqlRowSet srs = template
                    .queryForRowSet("SELECT textvalue, identity FROM  o_property WHERE identity IS NOT NULL AND textvalue IS NOT NULL AND textvalue LIKE  '%InfoSubscription::subs%'");
            Long identityKey = 0L;
            int rowCount = 0;
            int counter = 0;
            while (srs.next()) {
                try {

                    String prefsXml = srs.getString("textvalue");
                    identityKey = srs.getLong("identity");
                    Identity identity = security.loadIdentityByKey(identityKey);

                    Document doc = parser.createDocument(prefsXml);

                    List<String> infoSubscriptions = parser.parseDataForInputQuery(doc, parser.queryInfo);
                    persistInfo(infoSubscriptions, "InfoSubscription::subscribed", identity);

                    List<String> calendarSubscriptions = parser.parseDataForInputQuery(doc, parser.queryCal);
                    persistInfo(calendarSubscriptions, "CourseCalendarSubscription::subs", identity);

                    List<String> infoSubscriptionsNot = parser.parseDataForInputQuery(doc, parser.queryInfoNot);
                    persistInfo(infoSubscriptionsNot, "InfoSubscription::notdesired", identity);

                    List<String> calendarSubscriptionsNot = parser.parseDataForInputQuery(doc, parser.queryCalNot);
                    persistInfo(calendarSubscriptionsNot, "CourseCalendarSubscription::notdesired", identity);

                } catch (Exception e) {
                    log.error("could not migrate gui preferences for identity: " + identityKey, e);
                }
                counter++;
                if (counter % 10 == 0) {
                    DBFactory.getInstance().intermediateCommit();
                }

                rowCount++;
            }
            // Final commit
            DBFactory.getInstance().intermediateCommit();

            uhd.setBooleanDataValue(TASK_MIGRATE_DATA_FROM_GUI_PROPERTIES_DONE, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }

    }

    private void persistInfo(final List<String> infoSubscriptions, String key, Identity ident) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < infoSubscriptions.size(); i++) {
            log.info("Audit:Identity: '" + ident.getName() + "' has the following GUI properties('" + key + "') that will be migrated: " + infoSubscriptions.get(i));
            sb.append(infoSubscriptions.get(i));
            if (i < infoSubscriptions.size() - 1) {
                sb.append(",");
            }
        }

        List<PropertyImpl> properties = propertyManager.findProperties(ident, null, null, null, key);

        if (properties.size() == 0l) {
            PropertyImpl p = propertyManager.createPropertyInstance(ident, null, null, null, key, null, null, null, null);
            propertyManager.saveProperty(p);
            properties = propertyManager.findProperties(ident, null, null, null, key);
        }

        PropertyImpl p = properties.get(0);
        p.setTextValue(sb.toString());
        propertyManager.saveProperty(p);

    }

    private void cleanupV2GUIPreferencesProperties(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        // Due to the layering refactoring all preferences have to be resetted

        if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE)) {
            final String query = "delete from o_property where name ='v2guipreferences';";
            executePlainSQLDBStatement(query, upgradeManager.getDataSource());
            uhd.setBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE, true);

            log.info("Audit:+--------------------------------------+");
            log.info("Audit:+... Deleting all V2GUI preferences ...+");
            log.info("Audit:+--------------------------------------+");

            uhd.setInstallationComplete(true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

}
