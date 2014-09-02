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
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.database.PersistenceHelper;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Automatic upgrade code for the OLAT 6.1.0 release
 * <P>
 * Initial Date: 01.09.2008 <br>
 * 
 * @author gnaegi
 */
public class OLATUpgrade_6_1_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_6.1.0";
    private static final String TASK_DELETE_OLD_GUEST_USERS = "Old guest users deleted";
    private static final String TASK_CLEANUP_NOTIFICATIONS = "Old notifications cleaned up";
    private static final String TASK_CREATE_DISCLAIMER_CONFIRMATION = "Disclaimer confirmation for existing users created";

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

        // cleanup old guest user accounts - not needed anymore with new i18n system
        deleteOldGuestUsers(upgradeManager, uhd);

        // clean up notification tables
        cleanUpNotifications(upgradeManager);

        // create disclaimer confirmation for the already existing users, this might take a while!
        createDisclaimerConfirmationForExistingUsers(upgradeManager);

        // mark upgrade as finished, never run it again
        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }

    /**
     * Deletes the guest users from 6.0 release, the guest users are now created using other user names using an automated naming schema
     * 
     * @param upgradeManager
     * @param uhd
     */
    private void deleteOldGuestUsers(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_DELETE_OLD_GUEST_USERS)) {
            log.info("Audit:+---------------------------------------------------------------+");
            log.info("Audit:+... Deleting old guest users - OLAT 6.1 uses new gues users ...+");
            log.info("Audit:+---------------------------------------------------------------+");

            final String[] oldGuestUserNames = new String[] { "gast", "guest", "ospite", "invité", "invitado", "episkeptis", "gost", "gosc", "kerencn", "kerentw",
                    "gaest", "host", "svecas", "mehman", "convidadopt", "convidadobr", "misafir", "vend&#233;g", "mysafir", "tamu", "dhaif", "giast", "gas", "oreah",
                    "khachmoi", "zochin" };
            final BaseSecurity secMgr = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            for (final String guestUserName : oldGuestUserNames) {
                final Identity oldGuest = secMgr.findIdentityByName(guestUserName);
                if (oldGuest == null) {
                    // skip this one, seems already to be deleted
                    continue;
                }
                UserDeletionManager.getInstance().deleteIdentity(oldGuest);
            }
            DBFactory.getInstance().intermediateCommit();
            uhd.setBooleanDataValue(TASK_DELETE_OLD_GUEST_USERS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);

        }

    }

    private void cleanUpNotifications(final UpgradeManager upgradeManager) {
        final UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (!uhd.getBooleanDataValue(TASK_CLEANUP_NOTIFICATIONS)) {

            // delete all subscribers where the state is 1
            final String query1 = "delete from o_noti_sub where fk_publisher in (select publisher_id from o_noti_pub where state=1);";
            executePlainSQLDBStatement(query1, upgradeManager.getDataSource());

            // delete subscribers from the deleted wiki entries
            final String query2 = "delete from o_noti_sub where fk_publisher in (select publisher_id from o_noti_pub, o_olatresource where (o_noti_pub.resid=o_olatresource.resid) AND (o_noti_pub.resname='FileResource.WIKI') AND (o_olatresource.resid IS null));";
            executePlainSQLDBStatement(query2, upgradeManager.getDataSource());

            // delete all publishers where the wiki resource is deleted
            final String query3 = "delete from o_noti_pub where resid in (select resid from o_olatresource where (o_olatresource.resid IS null)) AND (o_noti_pub.resname='FileResource.WIKI');";
            executePlainSQLDBStatement(query3, upgradeManager.getDataSource());

            // delete all publishers where the state is 1
            final String query4 = "delete from o_noti_pub where state=1;";
            executePlainSQLDBStatement(query4, upgradeManager.getDataSource());

            uhd.setBooleanDataValue(TASK_CLEANUP_NOTIFICATIONS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);

        }
    }

    private void createDisclaimerConfirmationForExistingUsers(final UpgradeManager upgradeManager) {
        final UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (!uhd.getBooleanDataValue(TASK_CREATE_DISCLAIMER_CONFIRMATION)) {
            // Get all system users
            final BaseSecurity secMgr = CoreSpringFactory.getBean(BaseSecurity.class);
            final RegistrationService registrationService = CoreSpringFactory.getBean(RegistrationService.class);
            final DB db = DBFactory.getInstance();
            // Get all users
            final List<Identity> identities = secMgr.getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, null);
            // Remove the users that did already confirm the disclaimer
            final List<Identity> confirmedIdentities = registrationService.getIdentitiesWithConfirmedDisclaimer();
            PersistenceHelper.removeObjectsFromList(identities, confirmedIdentities);
            // Set the disclaimer property for the remaining users
            for (int i = 0; i < identities.size(); i++) {
                final Identity identity = identities.get(i);
                registrationService.setHasConfirmedDislaimer(identity);
                // write something to the console after each 100 user, this can take a
                // while with many users and it is handy to know that the system is
                // doing something
                if (i % 250 == 0) {
                    log.info("Audit:Busy creating disclaimer confirmation. Done with " + i + " of a total of " + identities.size() + " users. Please wait ...");
                    db.intermediateCommit();
                }
            }
            log.info("Audit:Done with creating disclaimer confirmation for " + identities.size() + " users");

            uhd.setBooleanDataValue(TASK_CREATE_DISCLAIMER_CONFIRMATION, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

}
