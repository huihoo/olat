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
import org.hibernate.ObjectDeletedException;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.notifications.Publisher;
import org.olat.lms.notifications.NotificationService;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.commons.StringHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

public class OLATUpgrade_7_1_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String TASK_CHECK_NOTIFICATIONS = "Check notifications publishers";

    private static final String VERSION = "OLAT_7.1.0";

    private boolean portletRepositoryStudentEnabled;
    private boolean portletRepositoryTeacherEnabled;

    /**
     * [used by Spring]
     * 
     * @param portletRepositoryStudentEnabled
     */
    public void setPortletRepositoryStudentEnabled(final boolean portletRepositoryStudentEnabled) {
        this.portletRepositoryStudentEnabled = portletRepositoryStudentEnabled;
    }

    /**
     * [used by Spring]
     * 
     * @param portletRepositoryTeacherEnabled
     */
    public void setPortletRepositoryTeacherEnabled(final boolean portletRepositoryTeacherEnabled) {
        this.portletRepositoryTeacherEnabled = portletRepositoryTeacherEnabled;
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

        migrateNotificationPublishers(upgradeManager, uhd);

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        log.info("Audit:Finished OLATUpgrade_7_1_0 successfully!");
        return true;
    }

    /**
	 */
    @Override
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        return false;
    }

    private void migrateNotificationPublishers(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_CHECK_NOTIFICATIONS)) {
            log.info("Audit:+-----------------------------------------------------------------------------+");
            log.info("Audit:+... Check the businesspath for the publishers (notifications)             ...+");
            log.info("Audit:+-----------------------------------------------------------------------------+");
            if (!portletRepositoryTeacherEnabled && !portletRepositoryStudentEnabled) {
                log.info("Audit:**** Repository portlets disabled: don't need to check publishers. ****");
                uhd.setBooleanDataValue(TASK_CHECK_NOTIFICATIONS, true);
                upgradeManager.setUpgradesHistory(uhd, VERSION);
                return;
            }

            int counter = 0;
            final NotificationService notificationMgr = getNotificationService();
            final List<Publisher> allPublishers = notificationMgr.getAllPublisher();
            if (log.isDebugEnabled()) {
                log.info("Found " + allPublishers.size() + " publishers to check.");
            }

            for (final Publisher publisher : allPublishers) {
                if (publisher != null && StringHelper.containsNonWhitespace(publisher.getBusinessPath())
                        && (publisher.getBusinessPath().startsWith("[Identity") || publisher.getBusinessPath().startsWith("ROOT[Identity"))) {
                    try {
                        final String businessPath = publisher.getBusinessPath();
                        final int startIndex = businessPath.indexOf("[Identity");
                        final int stopIndex = businessPath.indexOf("]", startIndex);
                        final int wide = stopIndex - startIndex;
                        if (wide > 30) {
                            // Identity:326394598 cannot be too wide
                            continue;
                        } else if (stopIndex + 1 >= businessPath.length()) {
                            // only identity
                            continue;
                        }

                        final String correctPath = businessPath.substring(stopIndex + 1);
                        publisher.setBusinessPath(correctPath);
                        DBFactory.getInstance().updateObject(publisher);
                    } catch (final ObjectDeletedException e) {
                        log.warn("Publisher was already deleted, no update possible! Publisher key: " + publisher.getKey());
                    } catch (final Exception e) {
                        log.warn("Publisher was already deleted, no update possible! Publisher key: " + publisher.getKey());
                    }
                    counter++;
                }
                if (counter > 0 && counter % 100 == 0) {
                    log.info("Audit:Another 100 publishers done");
                    DBFactory.getInstance().intermediateCommit();
                }
            }

            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Checked " + counter + " publishers. ****");

            uhd.setBooleanDataValue(TASK_CHECK_NOTIFICATIONS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private static NotificationService getNotificationService() {
        return (NotificationService) CoreSpringFactory.getBean(NotificationService.class);
    }

    /**
	 */
    @Override
    public String getVersion() {
        return VERSION;
    }
}
