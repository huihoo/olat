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
package org.olat.lms.core.course.campus.impl.creator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.presentation.framework.core.translator.Translator;

/**
 * Initial Date: 31.05.2012 <br>
 * 
 * @author cg
 */
public class CourseDescriptionBuilderTest {

    final static String LECTURE_SOLL = "first_lectureA last_lectureA, first_lectureB last_lectureB";
    final static Long TEST_RESOURCEABLE_ID = 1234L;

    CourseDescriptionBuilder courseDescriptionBuilder;
    private Identity identityLectureA;
    private Identity identityLectureB;
    List<Identity> lecturers;

    @Before
    public void setup() {
        courseDescriptionBuilder = new CourseDescriptionBuilder();
        lecturers = new ArrayList<Identity>();
        identityLectureA = ObjectMother.createIdentity("lectureA");
        lecturers.add(identityLectureA);
        identityLectureB = ObjectMother.createIdentity("lectureB");
        lecturers.add(identityLectureB);
    }

    @Test
    public void getLectureList() {
        String generatedLecturesAsString = courseDescriptionBuilder.getLectureList(lecturers);

        assertEquals("Wrong lecture list", LECTURE_SOLL, generatedLecturesAsString);
    }

    @Test
    public void buildDescriptionFrom() {
        String title = "Example title";
        String semester = "Herbstsemester 2012";
        String eventDescription = "Detail_description";

        String[] argsMock = new String[3];
        argsMock[0] = semester;
        argsMock[1] = LECTURE_SOLL;
        argsMock[2] = eventDescription;

        List<Identity> participants = new ArrayList<Identity>();
        CampusCourseImportTO campusCourseData = new CampusCourseImportTO(title, semester, lecturers, participants, eventDescription, TEST_RESOURCEABLE_ID, null);
        Translator translatorMock = mock(Translator.class);
        courseDescriptionBuilder.translator = translatorMock;
        // Description example :
        // 'Herbstsemester 2012/n/nDozent1_Vorname Dozent1_Nachname, Dozent2_Vorname Dozent2_Nachname, Dozent3_Vorname Dozent3_Nachname/nContent'
        String descriptionSoll = semester + "/n/n" + LECTURE_SOLL + "/n/n" + eventDescription;
        when(translatorMock.translate(CourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock)).thenReturn(descriptionSoll);
        String generatedDescription = courseDescriptionBuilder.buildDescriptionFrom(campusCourseData, "de");

        // Check if argsMock contains the right parameter (argsMock[0] = semester; argsMock[1] = LECTURE_SOLL; argsMock[2] = eventDescription;)
        verify(translatorMock).translate(CourseDescriptionBuilder.KEY_DESCRIPTION_TEMPLATE, argsMock);
    }

}
