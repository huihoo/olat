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
package org.olat.presentation.group.securitygroup.confirmation;

import org.olat.data.basesecurity.Identity;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.nodes.CourseNode;

/**
 * Initial Date: Nov 29, 2012 <br>
 * 
 * @author Branislav Balaz
 */
public class ProjectGroupConfirmationSenderInfo extends AbstractGroupConfirmationSenderInfo {

    private final Long projectBrokerId;
    private final RepositoryEntry repositoryEntry;
    private final CourseNode courseNode;

    public ProjectGroupConfirmationSenderInfo(Identity originatorIdentity, Long projectBrokerId, RepositoryEntry repositoryEntry, CourseNode courseNode) {
        super(originatorIdentity);
        this.projectBrokerId = projectBrokerId;
        this.repositoryEntry = repositoryEntry;
        this.courseNode = courseNode;
    }

    public Long getProjectBrokerId() {
        return projectBrokerId;
    }

    public RepositoryEntry getRepositoryEntry() {
        return repositoryEntry;
    }

    public CourseNode getCourseNode() {
        return courseNode;
    }

}
