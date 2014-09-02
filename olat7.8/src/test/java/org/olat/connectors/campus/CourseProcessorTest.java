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
package org.olat.connectors.campus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.DaoManager;

/**
 * Initial Date: 17.10.2012 <br>
 * 
 * @author aabouc
 */
public class CourseProcessorTest {
    private CourseProcessor courseProcessor;
    private Course course;

    private DaoManager daoManagerMock;

    @Before
    public void setup() {
        courseProcessor = new CourseProcessor();

        Map<String, String> semesterMap = new HashMap<String, String>();
        semesterMap.put("H", "HS");
        semesterMap.put("F", "FS");

        List<Long> enabledOrgs = new ArrayList<Long>();
        enabledOrgs.add(50000776L);

        // Mock for DaoManager
        daoManagerMock = mock(DaoManager.class);
        courseProcessor.daoManager = daoManagerMock;

        when(daoManagerMock.getIdsOfAllEnabledOrgs()).thenReturn(enabledOrgs);

        courseProcessor.setSemesterMap(semesterMap);
        courseProcessor.init();

        course = new Course();
    }

    @Test
    public void process_Duplicate() throws Exception {
        course.setId(100L);
        course.setTitle("Title");
        course.setSemester(null);
        course.setEnabled("0");

        Course resultCourse = courseProcessor.process(course);
        assertEquals(resultCourse, course);

        Course duplicateCourse = courseProcessor.process(course);
        assertNull(duplicateCourse);
    }

    @Test
    public void process_semesterIsNull() throws Exception {
        course.setId(100L);
        course.setTitle("Title");
        course.setSemester(null);
        course = courseProcessor.process(course);
        assertEquals(course.getTitle(), "Title");
    }

    @Test
    public void process_semesterIsNotNull() throws Exception {
        course.setId(200L);
        course.setTitle("Title");
        course.setSemester("Herbstsemester 2012");
        course = courseProcessor.process(course);
        assertEquals(course.getTitle(), "12HS Title");

        course.setId(300L);
        course.setTitle("Title");
        course.setSemester("Frühjahrssemester 2013");
        course = courseProcessor.process(course);
        assertEquals(course.getTitle(), "13FS Title");
    }

}
