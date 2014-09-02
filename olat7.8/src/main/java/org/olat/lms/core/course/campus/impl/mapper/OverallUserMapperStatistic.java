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

/**
 * Initial Date: 28.06.2012 <br>
 * 
 * @author cg
 */
public class OverallUserMapperStatistic {

    private MappingStatistic lecturersMappingStatistic;
    private MappingStatistic studentMappingStatistic;

    /**
     * @param lecturersMappingStatistic
     * @param studentMappingStatistic
     */
    public OverallUserMapperStatistic(MappingStatistic lecturersMappingStatistic, MappingStatistic studentMappingStatistic) {
        this.lecturersMappingStatistic = lecturersMappingStatistic;
        this.studentMappingStatistic = studentMappingStatistic;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Lecturer Mapping Statistic: ");
        builder.append(lecturersMappingStatistic);
        builder.append(" ; ");
        builder.append("Student Mapping Statistic: ");
        builder.append(studentMappingStatistic);
        return builder.toString();
    }

    public int getLecturerCouldNotMapCounter() {
        return lecturersMappingStatistic.getCouldNotMapCounter();
    }

    public int getLecturerMappedByEmailCounter() {
        return lecturersMappingStatistic.getNewMappingByEmailCounter();
    }

    public int getLecturerMappedByPersonalNrCounter() {
        return lecturersMappingStatistic.getNewMappingByPersonalNrCounter();
    }

    public Object getStudentCouldNotMapCounter() {
        return studentMappingStatistic.getCouldNotMapCounter();
    }

    public Object getStudentMappedByEmailCounter() {
        return studentMappingStatistic.getNewMappingByEmailCounter();
    }

    public Object getStudentMappedByMartikelNrCounter() {
        return studentMappingStatistic.getNewMappingByMatrikelNrCounter();
    }

    public MappingStatistic getLecturersMappingStatistic() {
        return lecturersMappingStatistic;
    }

    public MappingStatistic getStudentMappingStatistic() {
        return studentMappingStatistic;
    }

}
