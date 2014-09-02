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
package org.olat.lms.core.course.campus.service;

import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.course.campus.Course;
import org.olat.data.course.campus.SapOlatUser;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.core.CoreService;
import org.olat.lms.core.course.campus.CampusCourseImportTO;
import org.olat.lms.core.course.campus.impl.creator.CampusCourse;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Initial Date: 16.07.2012 <br>
 * 
 * @author cg
 */
public interface CampusCourseCoreService extends CoreService {

    public boolean checkDelegation(Long sapCampusCourseId, Identity creator);

    public CampusCourse createCampusCourseFromTemplate(Long courseResourceableId, Long sapCampusCourseId, Identity creator);

    public CampusCourse continueCampusCourse(Long courseResourceableId, Long sapCampusCourseId, String courseTitle, Identity creator);

    public CampusCourse createCampusCourse(Long resourceableId, Long sapCampusCourseId, Identity creator, CampusCourseImportTO campusCourseImportData);

    public void deleteResourceableIdReference(OLATResourceable res);

    public RepositoryEntry getRepositoryEntryFor(Long sapCourseId);

    /**
     * Get a list of Campus-courses which have resourceableId=null. resourceableId=null means no OLAT course is created in the OLAT course-repository yet.
     */
    public Set<Course> getCampusCoursesWithoutResourceableId(Identity identity, SapOlatUser.SapUserType userType);

    /**
     * Get list of Campus courses which already are created in the OLAT course-repository.
     */
    public Set<Course> getCampusCoursesWithResourceableId(Identity identity, SapOlatUser.SapUserType userType);

    public CampusCourse loadCampusCourse(Long sapCampusCourseId, Long resourceableId);

    public void createDelegation(Identity delegator, Identity delegatee);

    public boolean existDelegation(Identity delegator, Identity delegatee);

    public boolean existResourceableId(Long resourceableId);

    public List<Long> getAllCreatedSapCourcesResourceableIds();

    public List getDelegatees(Identity delegator);

    public void deleteDelegation(Identity delegator, Identity delegatee);

}
