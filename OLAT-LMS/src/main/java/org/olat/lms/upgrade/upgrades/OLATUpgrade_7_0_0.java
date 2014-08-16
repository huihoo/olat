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
 * OLATUpgrade_7_0_0
 * <P>
 * Initial Date: 25.03.2010 <br>
 * 
 * @author guido
 */
public class OLATUpgrade_7_0_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_7.0";

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

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        log.info("Audit:Finished OLATUpgrade_7_0_0 successfully!");
        return true;
    }

    /**
	 */
    @Override
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
	 */
    @Override
    public String getVersion() {
        return VERSION;
    }

}
