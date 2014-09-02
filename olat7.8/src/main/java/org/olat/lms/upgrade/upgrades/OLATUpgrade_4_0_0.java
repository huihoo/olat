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

import java.util.Iterator;
import java.util.List;

import org.olat.data.properties.AssessmentPropertyDao;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.assessment.EfficiencyStatementManager;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * - Creates all efficiency statements for all users for all courses
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
public class OLATUpgrade_4_0_0 extends OLATUpgrade {
    private static final String VERSION = "OLAT_4.0.0";
    private static final String TASK_EFFICIENCY_STATEMENT_DONE = "efficiencyStatementsCreated";

    /**
	 */
    @Override
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        // nothing to do here so far
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

        // BEGIN EFFICIENCY STATEMENT
        // create all efficiency statements for all users in all courses
        if (!uhd.getBooleanDataValue(TASK_EFFICIENCY_STATEMENT_DONE)) {
            final RepositoryService rm = RepositoryServiceImpl.getInstance();
            final EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
            // get all course repository entries
            final List entries = rm.queryByType(CourseModule.ORES_TYPE_COURSE);
            final Iterator iter = entries.iterator();
            while (iter.hasNext()) {
                final RepositoryEntry re = (RepositoryEntry) iter.next();
                // load course from entry
                final ICourse course = CourseFactory.loadCourse(re.getOlatResource());
                AssessmentPropertyDao assessmentPropertyDao = CoreSpringFactory.getBean(AssessmentPropertyDao.class);
                final List identities = assessmentPropertyDao.getAllIdentitiesWithCourseAssessmentData(course.getResourceableTypeName(), course.getResourceableId());
                // now create all statements for this course
                esm.updateEfficiencyStatements(course, identities, false);
            }
            uhd.setBooleanDataValue(TASK_EFFICIENCY_STATEMENT_DONE, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
        // END EFFICIENCY STATEMENT

        uhd.setInstallationComplete(true);
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
	 */
    @Override
    public String getAlterDbStatements() {
        return null; // till 6.1 was manual upgrade
    }

}
