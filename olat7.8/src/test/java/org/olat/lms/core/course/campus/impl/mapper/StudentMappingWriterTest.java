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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.course.campus.Student;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Initial Date: 27.11.2012 <br>
 * 
 * @author aabouc
 */
public class StudentMappingWriterTest {
    private StudentMappingWriter studentMappingWriterTestObject;
    private StudentMapper studentMapperMock;
    private List<Student> twoStudentsList = new ArrayList<Student>();

    @Before
    public void setup() {
        studentMappingWriterTestObject = new StudentMappingWriter();
        // Mock for StudentMapper
        studentMapperMock = mock(StudentMapper.class);
        studentMappingWriterTestObject.studentMapper = studentMapperMock;
        // Test MappingStatistic
        studentMappingWriterTestObject.setMappingStatistic(new MappingStatistic());

        // Mock for Student
        Student studentMock1 = mock(Student.class);
        Student studentMock2 = mock(Student.class);
        twoStudentsList.add(studentMock1);
        twoStudentsList.add(studentMock2);

        when(studentMapperMock.synchronizeStudentMapping(studentMock1)).thenReturn(MappingResult.NEW_MAPPING_BY_EMAIL);
        when(studentMapperMock.synchronizeStudentMapping(studentMock2)).thenReturn(MappingResult.NEW_MAPPING_BY_MATRIKEL_NR);
    }

    @Test
    public void write_emptyStudentsList() throws Exception {
        studentMappingWriterTestObject.write(Collections.emptyList());
        assertEquals(
                studentMappingWriterTestObject.getMappingStatistic().toString(),
                "MappedByEmail=0 , MappedByMatrikelNumber=0 , MappedByPersonalNumber=0 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

    @Test
    public void write_twoStudentsList() throws Exception {
        studentMappingWriterTestObject.write(twoStudentsList);
        assertEquals(
                studentMappingWriterTestObject.getMappingStatistic().toString(),
                "MappedByEmail=1 , MappedByMatrikelNumber=1 , MappedByPersonalNumber=0 , MappedByAdditionalPersonalNumber=0 , couldNotMappedBecauseNotRegistered=0 , couldBeMappedManually=0");
    }

}
