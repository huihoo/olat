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

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.SapOlatUserDao;
import org.olat.data.course.campus.Student;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class StudentMapperTest {

    private StudentMapper studentMapperTestObject;
    private SapOlatUserDao userMappingDaoMock;
    private StudentMappingByMartikelNumber studentMappingByMartikelNumberMock;
    private MappingByFirstNameAndLastName mappingByFirstNameAndLastNameMock;
    private MappingByEmail mappingByEmailMock;
    private Student studentMock;
    private Identity identityMock;

    @Before
    public void setup() {
        studentMapperTestObject = new StudentMapper();
        userMappingDaoMock = mock(SapOlatUserDao.class);
        studentMapperTestObject.userMappingDao = userMappingDaoMock;
        studentMappingByMartikelNumberMock = mock(StudentMappingByMartikelNumber.class);
        mappingByFirstNameAndLastNameMock = mock(MappingByFirstNameAndLastName.class);
        studentMapperTestObject.studentMappingByMartikelNumber = studentMappingByMartikelNumberMock;
        mappingByEmailMock = mock(MappingByEmail.class);
        studentMapperTestObject.mappingByEmail = mappingByEmailMock;
        studentMapperTestObject.mappingByFirstNameAndLastName = mappingByFirstNameAndLastNameMock;

        studentMock = mock(Student.class);
        identityMock = mock(Identity.class);
    }

    @Test
    public void synchronizeStudentMapping_MappingAlreadyExist() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(true);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("", MappingResult.MAPPING_ALREADY_EXIST, result);
    }

    @Test
    public void synchronizeStudentMapping_CouldNotMap() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(studentMappingByMartikelNumberMock.tryToMap(studentMock)).thenReturn(null);
        when(mappingByEmailMock.tryToMap(studentMock)).thenReturn(null);
        when(mappingByFirstNameAndLastNameMock.tryToMap(studentMock.getFirstName(), studentMock.getLastName())).thenReturn(null);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("", MappingResult.COULD_NOT_MAP, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByMartikelNumber() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(studentMappingByMartikelNumberMock.tryToMap(studentMock)).thenReturn(identityMock);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("", MappingResult.NEW_MAPPING_BY_MATRIKEL_NR, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByEmail() {
        when(userMappingDaoMock.existsMappingForSapUserId(studentMock.getId())).thenReturn(false);
        when(studentMappingByMartikelNumberMock.tryToMap(studentMock)).thenReturn(null);
        when(mappingByEmailMock.tryToMap(studentMock)).thenReturn(identityMock);

        MappingResult result = studentMapperTestObject.synchronizeStudentMapping(studentMock);

        assertEquals("", MappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

}
