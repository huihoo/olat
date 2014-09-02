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
import org.olat.data.course.campus.Lecturer;
import org.olat.data.course.campus.SapOlatUserDao;

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class LecturerMapperTest {

    private LecturerMapper lecturerMapperTestObject;
    private SapOlatUserDao userMappingDaoMock;
    private LecturerMappingByPersonalNumber lecturerMappingByPersonalNumberMock;
    private MappingByFirstNameAndLastName mappingByFirstNameAndLastNameMock;
    private MappingByEmail mappingByEmailMock;
    private Lecturer lecturerMock;
    private Identity identityMock;

    @Before
    public void setup() {
        lecturerMapperTestObject = new LecturerMapper();
        userMappingDaoMock = mock(SapOlatUserDao.class);
        lecturerMapperTestObject.userMappingDao = userMappingDaoMock;
        lecturerMappingByPersonalNumberMock = mock(LecturerMappingByPersonalNumber.class);
        mappingByFirstNameAndLastNameMock = mock(MappingByFirstNameAndLastName.class);
        lecturerMapperTestObject.mappingByPersonalNumber = lecturerMappingByPersonalNumberMock;
        mappingByEmailMock = mock(MappingByEmail.class);
        lecturerMapperTestObject.mappingByEmail = mappingByEmailMock;
        lecturerMapperTestObject.mappingByFirstNameAndLastName = mappingByFirstNameAndLastNameMock;

        lecturerMock = mock(Lecturer.class);
        when(lecturerMock.getPersonalNr()).thenReturn(1L);
        identityMock = mock(Identity.class);
    }

    @Test
    public void synchronizeStudentMapping_MappingAlreadyExist() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(true);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("", MappingResult.MAPPING_ALREADY_EXIST, result);
    }

    @Test
    public void synchronizeStudentMapping_CouldNotMap() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(lecturerMappingByPersonalNumberMock.tryToMap(lecturerMock.getPersonalNr())).thenReturn(null);
        when(mappingByEmailMock.tryToMap(lecturerMock)).thenReturn(null);
        when(mappingByFirstNameAndLastNameMock.tryToMap(lecturerMock.getFirstName(), lecturerMock.getLastName())).thenReturn(null);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("", MappingResult.COULD_NOT_MAP, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByMartikelNumber() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(lecturerMappingByPersonalNumberMock.tryToMap(lecturerMock.getPersonalNr())).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("", MappingResult.NEW_MAPPING_BY_PERSONAL_NR, result);
    }

    @Test
    public void synchronizeStudentMapping_MappingByEmail() {
        when(userMappingDaoMock.existsMappingForSapUserId(lecturerMock.getPersonalNr())).thenReturn(false);
        when(lecturerMappingByPersonalNumberMock.tryToMap(lecturerMock.getPersonalNr())).thenReturn(null);
        when(mappingByEmailMock.tryToMap(lecturerMock)).thenReturn(identityMock);

        MappingResult result = lecturerMapperTestObject.synchronizeLecturerMapping(lecturerMock);

        assertEquals("", MappingResult.NEW_MAPPING_BY_EMAIL, result);
    }

}
