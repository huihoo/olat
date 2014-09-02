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
public class MappingStatistic {

    private int newMappingByEmailCounter;
    private int newMappingByMatrikelNrCounter;
    private int newMappingByPersonalNrCounter;
    private int couldNotMapCounter;
    private int couldBeMappedManuallyCounter;
    private int newMappingByAdditionalPersonalNrCounter;

    public void addMappingResult(MappingResult mappingResult) {
        if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_EMAIL)) {
            newMappingByEmailCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_MATRIKEL_NR)) {
            newMappingByMatrikelNrCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_PERSONAL_NR)) {
            newMappingByPersonalNrCounter++;
        } else if (mappingResult.equals(MappingResult.COULD_NOT_MAP)) {
            couldNotMapCounter++;
        } else if (mappingResult.equals(MappingResult.COULD_BE_MAPPED_MANUALLY)) {
            couldBeMappedManuallyCounter++;
        } else if (mappingResult.equals(MappingResult.NEW_MAPPING_BY_ADDITIONAL_PERSONAL_NR)) {
            newMappingByAdditionalPersonalNrCounter++;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MappedByEmail=");
        builder.append(newMappingByEmailCounter);
        builder.append(" , ");
        builder.append("MappedByMatrikelNumber=");
        builder.append(newMappingByMatrikelNrCounter);
        builder.append(" , ");
        builder.append("MappedByPersonalNumber=");
        builder.append(newMappingByPersonalNrCounter);
        builder.append(" , ");
        builder.append("MappedByAdditionalPersonalNumber=");
        builder.append(newMappingByAdditionalPersonalNrCounter);
        builder.append(" , ");
        builder.append("couldNotMappedBecauseNotRegistered=");
        builder.append(couldNotMapCounter);
        builder.append(" , ");
        builder.append("couldBeMappedManually=");
        builder.append(couldBeMappedManuallyCounter);
        return builder.toString();
    }

    public String toStringForStudentMapping() {
        StringBuilder builder = new StringBuilder();
        builder.append("MappedByMatrikelNumber=");
        builder.append(newMappingByMatrikelNrCounter);
        builder.append(" , ");
        builder.append("MappedByEmail=");
        builder.append(newMappingByEmailCounter);
        builder.append(" , ");
        builder.append("couldNotMappedBecauseNotRegistered=");
        builder.append(couldNotMapCounter);
        builder.append(" , ");
        builder.append("couldBeMappedManually=");
        builder.append(couldBeMappedManuallyCounter);
        return builder.toString();
    }

    public String toStringForLecturerMapping() {
        StringBuilder builder = new StringBuilder();
        builder.append("MappedByPersonalNumber=");
        builder.append(newMappingByPersonalNrCounter);
        builder.append(" , ");
        builder.append("MappedByEmail=");
        builder.append(newMappingByEmailCounter);
        builder.append(" , ");
        builder.append("MappedByAdditionalPersonalNumber=");
        builder.append(newMappingByAdditionalPersonalNrCounter);
        builder.append(" , ");
        builder.append("couldNotMappedBecauseNotRegistered=");
        builder.append(couldNotMapCounter);
        builder.append(" , ");
        builder.append("couldBeMappedManually=");
        builder.append(couldBeMappedManuallyCounter);
        return builder.toString();
    }

    public int getNewMappingByEmailCounter() {
        return newMappingByEmailCounter;
    }

    public int getNewMappingByMatrikelNrCounter() {
        return newMappingByMatrikelNrCounter;
    }

    public int getNewMappingByPersonalNrCounter() {
        return newMappingByPersonalNrCounter;
    }

    public int getCouldNotMapCounter() {
        return couldNotMapCounter;
    }

    public int getCouldBeMappedManuallyCounter() {
        return couldBeMappedManuallyCounter;
    }

}
