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
package org.olat.lms.learn.campus.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.lms.core.course.campus.service.CampusCourseCoreService;

/**
 * Initial Date: 25.06.2012 <br>
 * 
 * @author aabouc
 */
public class CampusCourseLearnServiceImplTest {
    private CampusCourseLearnServiceImpl campusCourseLearnServiceImplTestObject;

    private Set<Course> coursesWithResourceableId = new HashSet<Course>();
    private Set<Course> coursesWithoutResourceableId = new HashSet<Course>();

    private Identity identityMock;

    @Before
    public void setup() {
        CampusCourseCoreService campusCourseCoreServiceMock = mock(CampusCourseCoreService.class);
        campusCourseLearnServiceImplTestObject = new CampusCourseLearnServiceImpl();
        campusCourseLearnServiceImplTestObject.campusCourseCoreService = campusCourseCoreServiceMock;

        // Course which could be created
        Course course1 = new Course();
        course1.setId(new Long(50550670));
        course1.setTitle("Wahlpflichtmodul MA: Forschungsseminar Policy Analyse, 2-sem. (SA)");
        coursesWithoutResourceableId.add(course1);

        // Course which could be opened
        Course course2 = new Course();
        course2.setId(new Long(50541483));
        course2.setTitle("English Literature: Textual Analysis, Part II (Vorlesung zum Seminar)");
        course2.setResourceableId(new Long(99999));
        coursesWithResourceableId.add(course2);

        // Course which could be created
        Course course3 = new Course();
        course3.setId(new Long(50541762));
        course3.setTitle("Pflichtmodul M.A.-Seminar Sprachwissenschaft (6 KP)");
        coursesWithoutResourceableId.add(course3);

        // Mock for Identity
        identityMock = mock(Identity.class);
        // when(usesrServiceMock.getUserProperty(identityMock, propertyName))
        when(campusCourseCoreServiceMock.getCampusCoursesWithoutResourceableId(any(Identity.class), SapOlatUser.SapUserType.LECTURER)).thenReturn(
                coursesWithoutResourceableId);

    }

    @Ignore
    @Test
    public void getCoursesWhichCouldBeCreated() {
        assertTrue(campusCourseLearnServiceImplTestObject.getCoursesWhichCouldBeCreated(identityMock, SapOlatUser.SapUserType.LECTURER).size() == 1);
    }

}
