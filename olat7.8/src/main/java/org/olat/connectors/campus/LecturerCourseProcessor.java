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

import java.util.Date;

import org.olat.data.course.campus.LecturerCourse;
import org.olat.data.course.campus.LecturerCoursePK;
import org.springframework.batch.item.ItemProcessor;

/**
 * This is an implementation of {@link ItemProcessor} that converts the input LecturerCoursePK item <br>
 * to the output LecturerCourse item. <br>
 * 
 * Initial Date: 11.06.2012 <br>
 * 
 * @author aabouc
 */
public class LecturerCourseProcessor implements ItemProcessor<LecturerCoursePK, LecturerCourse> {

    /**
     * Converts the input item LecturerCoursePK to the output item LecturerCourse
     * 
     * @param pk
     *            the LecturerCoursePK
     * 
     * @return the LecturerCourse
     */
    public LecturerCourse process(LecturerCoursePK pk) throws Exception {
        LecturerCourse lecturerCourse = new LecturerCourse(pk);
        lecturerCourse.setModifiedDate(new Date());
        return lecturerCourse;
    }

}
