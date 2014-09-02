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
package org.olat.lms.core.course.campus.impl.syncer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.course.campus.DaoManager;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: 08.11.2012 <br>
 * 
 * @author aabouc
 */
public class SynchronizationReaderTest {
    private SynchronizationReader synchronizationReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        synchronizationReaderTestObject = new SynchronizationReader();
        // Mock for DaoManager
        daoManagerMock = mock(DaoManager.class);
        synchronizationReaderTestObject.daoManager = daoManagerMock;
    }

    @Test
    public void destroy_nullCoursesList() {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(null);
        synchronizationReaderTestObject.init();
        synchronizationReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyCoursesList() {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(Collections.emptyList());
        synchronizationReaderTestObject.init();
        synchronizationReaderTestObject.destroy();
    }

    @Test
    public void read_nullCoursesList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(null);
        synchronizationReaderTestObject.init();
        assertNull(synchronizationReaderTestObject.read());
    }

    @Test
    public void read_emptyCoursesList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllCreatedSapCources()).thenReturn(Collections.emptyList());
        synchronizationReaderTestObject.init();
        assertNull(synchronizationReaderTestObject.read());
    }

    @Test
    public void read_twoCoursesList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.chekImportedData()).thenReturn(true);
        List<Long> CreatedSapCourcesIds = new ArrayList<Long>();
        CreatedSapCourcesIds.add(100L);
        CreatedSapCourcesIds.add(200L);
        when(daoManagerMock.getAllCreatedSapCourcesIds()).thenReturn(CreatedSapCourcesIds);

        CampusCourseImportTO courseMock1 = mock(CampusCourseImportTO.class);
        CampusCourseImportTO courseMock2 = mock(CampusCourseImportTO.class);
        when(daoManagerMock.getSapCampusCourse(100L)).thenReturn(courseMock1);
        when(daoManagerMock.getSapCampusCourse(200L)).thenReturn(courseMock2);

        synchronizationReaderTestObject.init();
        // The first read delivers the first course
        assertNotNull(synchronizationReaderTestObject.read());
        // The second read delivers the second course
        assertNotNull(synchronizationReaderTestObject.read());
        // The third read delivers null
        assertNull(synchronizationReaderTestObject.read());
    }
}
