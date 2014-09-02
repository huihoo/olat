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
package org.olat.lms.core.course.campus.impl.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.course.campus.DaoManager;
import org.olat.data.course.campus.Lecturer;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerMappingReaderTest {
    private LecturerMappingReader lecturerMappingReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        lecturerMappingReaderTestObject = new LecturerMappingReader();
        // Mock for DaoManager
        daoManagerMock = mock(DaoManager.class);
        lecturerMappingReaderTestObject.daoManager = daoManagerMock;
    }

    @Test
    public void destroy_nullLecturersList() {
        when(daoManagerMock.getAllLecturers()).thenReturn(null);
        lecturerMappingReaderTestObject.init();
        lecturerMappingReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyLecturersList() {
        when(daoManagerMock.getAllLecturers()).thenReturn(Collections.emptyList());
        lecturerMappingReaderTestObject.init();
        lecturerMappingReaderTestObject.destroy();
    }

    @Test
    public void read_nullLecturersList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllLecturers()).thenReturn(null);
        lecturerMappingReaderTestObject.init();
        assertNull(lecturerMappingReaderTestObject.read());
    }

    @Test
    public void read_emptyLecturersList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllLecturers()).thenReturn(Collections.emptyList());
        lecturerMappingReaderTestObject.init();
        assertNull(lecturerMappingReaderTestObject.read());
    }

    @Test
    public void read_twoLecturersList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        List<Lecturer> twoLecturersList = new ArrayList<Lecturer>();
        Lecturer lecturerMock1 = mock(Lecturer.class);
        Lecturer lecturerMock2 = mock(Lecturer.class);
        twoLecturersList.add(lecturerMock1);
        twoLecturersList.add(lecturerMock2);
        when(daoManagerMock.getAllLecturers()).thenReturn(twoLecturersList);
        lecturerMappingReaderTestObject.init();
        // The first read delivers the first lecturer
        assertNotNull(lecturerMappingReaderTestObject.read());
        // The second read delivers the second lecturer
        assertNotNull(lecturerMappingReaderTestObject.read());
        // The third read delivers null
        assertNull(lecturerMappingReaderTestObject.read());
    }

}
