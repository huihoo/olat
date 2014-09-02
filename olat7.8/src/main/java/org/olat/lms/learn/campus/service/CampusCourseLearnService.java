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
package org.olat.lms.learn.campus.service;

import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.lms.learn.LearnService;

/**
 * This is called from presentation.
 * 
 * Initial Date: 30.11.2011 <br>
 * 
 * @author guretzki
 */
public interface CampusCourseLearnService extends LearnService {

    public boolean checkDelegation(Long sapCampusCourseId, Identity creator);

    /**
     * Create a new campus-course from a course template. Copy template and update title, description, owner and participants.
     */
    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator);

    /**
     * Get a list of SAP campus-course which an identity could create. The courses must be not created and the identity must be owner of the courses.
     */
    public List<SapCampusCourseTo> getCoursesWhichCouldBeCreated(Identity identity, SapOlatUser.SapUserType userType);

    /**
     * Get a list of SAP campus-courses which are already created and identity is owner or participant.
     */
    public List<SapCampusCourseTo> getCoursesWhichCouldBeOpened(Identity identity, SapOlatUser.SapUserType userType);

    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    public void createDelegation(Identity delegator, Identity delegatee);

    public boolean existDelegation(Identity delegator, Identity delegatee);

    public boolean existResourceableId(Long resourceableId);

    public List<Long> getAllCreatedSapCourcesResourceableIds();

    public List getDelegatees(Identity delegator);

    public void deleteDelegation(Identity delegator, Identity delegatee);

}
