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

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResourceManager;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.repository.RepositoryEBL;
import org.olat.lms.repository.RepositoryEntryInputData;
import org.olat.lms.repository.RepositoryService;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 04.06.2012 <br>
 * 
 * @author cg
 */
@Component
public class CourseTemplate {

    @Autowired
    RepositoryEBL repositoryEBL;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    private OLATResourceManager olatResourceManager;

    public CampusCourse createCampusCourseFromTemplate(Long templateCourseResourceableId, Identity owner) {
        // 1. Lookup template
        OLATResourceable templateCourse = CourseFactory.loadCourse(templateCourseResourceableId);
        RepositoryEntry sourceRepositoryEntry = repositoryService.lookupRepositoryEntry(templateCourse, true);
        // 2. Copy Course
        OLATResourceable copyCourseOlatResourcable = CourseFactory.copyCourse(templateCourse, owner);
        olatResourceManager.findOrPersistResourceable(copyCourseOlatResourcable);

        ICourse copyCourse = CourseFactory.loadCourse(copyCourseOlatResourcable.getResourceableId());
        // 3. Copy RepositoryEntry
        RepositoryEntry copyOfRepositoryEntry = copyRepositoryEntry(sourceRepositoryEntry, copyCourseOlatResourcable, owner);
        return new CampusCourse(copyCourse, copyOfRepositoryEntry);
    }

    private RepositoryEntry copyRepositoryEntry(RepositoryEntry sourceRepositoryEntry, OLATResourceable copyOfCourse, Identity owner) {
        RepositoryEntryInputData repositoryEntryInputData = new RepositoryEntryInputData(owner, sourceRepositoryEntry.getResourcename(),
                sourceRepositoryEntry.getDisplayname(), copyOfCourse);
        return repositoryEBL.copyRepositoryEntry(sourceRepositoryEntry, repositoryEntryInputData);
    }

}
