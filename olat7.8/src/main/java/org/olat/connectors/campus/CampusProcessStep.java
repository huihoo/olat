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

/**
 * This enumeration defines the different steps involving in the campus batch processing.<br>
 * 
 * Initial Date: 13.07.2012 <br>
 * 
 * @author aabouc
 */
public enum CampusProcessStep {
    /**
     * This step describes the import of the control file which gathers all the exported csv files.
     */
    IMPORT_CONTROLFILE,
    /**
     * This step describes the import of the data of SAP courses (LV).
     */
    IMPORT_COURSES,
    /**
     * This step describes the import of the Students data.
     */
    IMPORT_STUDENTS,
    /**
     * This step describes the import of the lecturers data.
     */
    IMPORT_LECTURERS,
    /**
     * This step describes the import of the contents, materials, etc of the courses data
     */
    IMPORT_TEXTS,
    /**
     * This step describes the import of the events (Termine) data
     */
    IMPORT_EVENTS,
    /**
     * This step describes the import of the relationships data (lecturers - courses)
     */
    IMPORT_LECTURERS_COURSES,
    /**
     * This step describes the import of the organizations data
     */
    IMPORT_ORGS,
    /**
     * This step describes the import of the relationships data (students - courses)
     */
    IMPORT_STUDENTS_COURSES,
    /**
     * This step describes the synchronization of courses and participants.
     */
    CAMPUSSYNCHRONISATION,
    /**
     * 
     */
    STUDENTMAPPING,
    /**
     * 
     */
    LECTURERMAPPING
}
