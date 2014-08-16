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

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_SHIB;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.security.authentication.WebDAVAuthManager;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.commons.WebappHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * Upgrade to OLAT 6.2: - Migration of old wiki-fields to flexiform Code is already here for every update. Method calls will be commented out step by step when
 * corresponding new controllers are ready. As long as there will be other things to migrate Upgrade won't be set to DONE!
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_3_3 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_6.3.3";

    private boolean migrateOlatAuthToWebDAVAuth;

    private static final String TASK_MIGRATE_TO_WEBDAV_PASSWORDS = "Migrate to WebDAV passwords";

    private static final String TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS = "Migrate wrongly encoded ical links";

    public boolean isMigrateOlatAuthToWebDAVAuth() {
        return migrateOlatAuthToWebDAVAuth;
    }

    public void setMigrateOlatAuthToWebDAVAuth(final boolean migrateOlatAuthToWebDAVAuth) {
        this.migrateOlatAuthToWebDAVAuth = migrateOlatAuthToWebDAVAuth;
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

        // upgrade to webdav password
        migrateToWebDAVPassword(upgradeManager, uhd);

        // OLAT-5736: migrate ical files containing wrong "Â§" characters in node-links (course and group cals effected)
        migrateWronglyEncodedICalLinks(upgradeManager, uhd);

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }

    private void migrateWronglyEncodedICalLinks(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (uhd.getBooleanDataValue(TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS)) {
            // already migrated
            return;
        }

        // need to migrate now
        log.info("Audit:migrateWronglyEncodedICalLinks: START");
        final File userDataDir = new File(WebappHelper.getUserDataRoot());
        final File calendarsDir = new File(userDataDir, "calendars");
        final File courseDir = new File(calendarsDir, "course");
        log.info("Audit:migrateWronglyEncodedICalLinks: Migrating course directory: " + courseDir);
        CalendarXOlatLinkUTF8Fix.migrate(courseDir);
        log.info("Audit:migrateWronglyEncodedICalLinks: Done migrating course directory: " + courseDir);
        final File groupDir = new File(calendarsDir, "group");
        log.info("Audit:migrateWronglyEncodedICalLinks: Migrating group directory: " + groupDir);
        CalendarXOlatLinkUTF8Fix.migrate(groupDir);
        log.info("Audit:migrateWronglyEncodedICalLinks: Done migrating group directory: " + groupDir);

        log.info("Audit:migrateWronglyEncodedICalLinks: DONE");

        uhd.setBooleanDataValue(TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS, true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
    }

    private void migrateToWebDAVPassword(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS)) {
            if (!migrateOlatAuthToWebDAVAuth) {
                // don't migrate the OLAT password
                uhd.setBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS, true);
                upgradeManager.setUpgradesHistory(uhd, VERSION);
                return;
            }

            final BaseSecurity secMgr = (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
            // filter all admins
            final SecurityGroup adminGroup = secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN);
            // get all identities

            int count = 0;
            final List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
            DBFactory.getInstance().intermediateCommit();
            for (final Identity identity : identitiesList) {
                if (count++ % 10 == 0) {
                    DBFactory.getInstance().intermediateCommit();
                }

                final boolean admin = secMgr.isIdentityInSecurityGroup(identity, adminGroup);
                if (admin) {
                    log.info("Audit:No OLAT Auth. provider migrated for admin: " + identity.getName());
                    continue;
                }

                Authentication olatAuth = null, webDAVAuth = null, shibAuth = null;
                final List<Authentication> auths = secMgr.getAuthentications(identity);
                for (final Authentication auth : auths) {
                    if (WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())) {
                        webDAVAuth = auth;
                    } else if (AUTHENTICATION_PROVIDER_OLAT.equals(auth.getProvider())) {
                        olatAuth = auth;
                    } else if (AUTHENTICATION_PROVIDER_SHIB.equals(auth.getProvider())) {
                        shibAuth = auth;
                    }
                }

                if (webDAVAuth == null && olatAuth != null && shibAuth != null) {
                    final String hashedPwd = olatAuth.getCredential();
                    log.info("Audit:Create WebDAV Auth. provider for: " + identity.getName());
                    webDAVAuth = secMgr.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_WEBDAV, identity.getName(), hashedPwd);
                    if (webDAVAuth != null) {
                        log.info("Audit:Delete OLAT Auth. provider for: " + identity.getName());
                        secMgr.deleteAuthentication(olatAuth);
                    }
                }
            }

            uhd.setBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}
