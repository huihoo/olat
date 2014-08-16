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

package org.olat.lms.course.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.OlatTestCase;

/**
 * Description: <br>
 * TODO: patrick Class Description for CourseConfigManagerImplITCase
 * <P>
 * Initial Date: Jun 3, 2005 <br>
 * 
 * @author patrick
 */
public class CourseConfigManagerImplITCase extends OlatTestCase {
    private static final Logger log = LoggerHelper.getLogger();

    private static ICourse course1;

    private static boolean isSetup = false;
    private static boolean isTearDown = false;

    /**
     * SetUp is called before each test.
     */
    @Before
    public void setUp() {
        if (isSetup) {
            return;
        }
        try {
            // create course and persist as OLATResourceImpl
            final OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitConfigCourse", new Long(System.currentTimeMillis()));
            course1 = CourseFactory.createEmptyCourse(resourceable, "JUnitCourseConfig", "JUnitCourseConfig Long Title", "objective 1 objective 2 objective 3");
            DBFactory.getInstance().closeSession();
            isSetup = true;
        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
            e.printStackTrace();
        }
    }

    @Test
    public void testConfigFileCRUD() {
        /*
         * a new created course gets its configuration upon the first load with default values
         */
        final CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
        CourseConfig cc1 = ccm.loadConfigFor(course1);
        assertNotNull("CourseConfiguration is not null", cc1);
        /*
         * update values
         */
        ccm.saveConfigTo(course1, cc1);
        cc1 = null;
        // check the saved values
        cc1 = ccm.loadConfigFor(course1);
        assertNotNull("CourseConfiguration is not null", cc1);
        /*
         * delete configuration
         */
        ccm.deleteConfigOf(course1);
        final VFSItem cc1File = CourseConfigManagerImpl.getConfigFile(course1);
        assertFalse("CourseConfig file no longer exists.", cc1File != null);
    }

    /**
     * TearDown is called after each test.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        if (isTearDown) {
            return;
        }
        try {
            DBFactory.getInstance().closeSession();
            isTearDown = true;
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
            e.printStackTrace();
            throw e;
        }
    }
}
