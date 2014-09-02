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
package org.olat.lms.core.course.campus.impl.metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date: 13.07.2012 <br>
 * 
 * @author aabouc
 */
public class CampusImportMetricTest {

    private CampusMetric campusImportMetric;
    CampusStatistics campusStatistics;

    @Before
    public void setup() {
        campusImportMetric = new CampusMetric();
        campusStatistics = new CampusStatistics(true);
        campusImportMetric.update(campusStatistics);
    }

    @Test
    public void getExportStatus_null() {
        assertNull(campusImportMetric.getExportStatus());
    }

    @Test
    public void getExportStatus_OK() {
        campusStatistics = new CampusStatistics(CampusStatistics.EXPORT_STATUS.OK);
        campusImportMetric.update(campusStatistics);
        assertEquals(campusImportMetric.getExportStatus(), CampusStatistics.EXPORT_STATUS.OK.name());
    }

    @Test
    public void getExportStatus_NO_EXPORT() {
        campusStatistics = new CampusStatistics(CampusStatistics.EXPORT_STATUS.NO_EXPORT);
        campusImportMetric.update(campusStatistics);
        assertEquals(campusImportMetric.getExportStatus(), CampusStatistics.EXPORT_STATUS.NO_EXPORT.name());
    }

    @Test
    public void getExportStatus_INCOMPLETE_EXPORT() {
        campusStatistics = new CampusStatistics(CampusStatistics.EXPORT_STATUS.INCOMPLETE_EXPORT);
        campusImportMetric.update(campusStatistics);
        assertEquals(campusImportMetric.getExportStatus(), CampusStatistics.EXPORT_STATUS.INCOMPLETE_EXPORT.name());
    }

    // @Test
    // public void getControlFileInconsistencies_noInconsistencies() {
    // assertEquals(campusImportMetric.getImportProcessSkipsForControlFile(), CampusMetric.NO_INCONSISTENCIES);
    // }
    //
    // @Test
    // public void getControlFileInconsistencies_inconsistencies() {
    // Inconsistency inconsistency = new Inconsistency(CampusProcessStep.IMPORT_CONTROLFILE, 1, 0, 0);
    // campusStatistics = new CampusStatistics(inconsistency);
    // campusImportMetric.update(campusStatistics);
    // assertEquals(campusImportMetric.getImportProcessSkipsForControlFile(), "[readSkipCount=1, writeSkipCount=0, processSkipCount=0]");
    // }

}
