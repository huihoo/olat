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
import org.olat.data.course.campus.Student;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class StudentMappingReaderTest {
    private StudentMappingReader studentMappingReaderTestObject;
    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        studentMappingReaderTestObject = new StudentMappingReader();
        // Mock for DaoManager
        daoManagerMock = mock(DaoManager.class);
        studentMappingReaderTestObject.daoManager = daoManagerMock;
    }

    @Test
    public void destroy_nullStudentsList() {
        when(daoManagerMock.getAllStudents()).thenReturn(null);
        studentMappingReaderTestObject.init();
        studentMappingReaderTestObject.destroy();
    }

    @Test
    public void destroy_emptyStudentsList() {
        when(daoManagerMock.getAllStudents()).thenReturn(Collections.emptyList());
        studentMappingReaderTestObject.init();
        studentMappingReaderTestObject.destroy();
    }

    @Test
    public void read_nullStudentsList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllStudents()).thenReturn(null);
        studentMappingReaderTestObject.init();
        assertNull(studentMappingReaderTestObject.read());
    }

    @Test
    public void read_emptyStudentsList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        when(daoManagerMock.getAllStudents()).thenReturn(Collections.emptyList());
        studentMappingReaderTestObject.init();
        assertNull(studentMappingReaderTestObject.read());
    }

    @Test
    public void read_twoStudentsList() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        List<Student> twoStudentsList = new ArrayList<Student>();
        Student studentMock1 = mock(Student.class);
        Student studentMock2 = mock(Student.class);
        twoStudentsList.add(studentMock1);
        twoStudentsList.add(studentMock2);
        when(daoManagerMock.getAllStudents()).thenReturn(twoStudentsList);
        studentMappingReaderTestObject.init();
        // The first read delivers the first student
        assertNotNull(studentMappingReaderTestObject.read());
        // The second read delivers the second student
        assertNotNull(studentMappingReaderTestObject.read());
        // The third read delivers null
        assertNull(studentMappingReaderTestObject.read());
    }

}
