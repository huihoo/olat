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

package org.olat.lms.course;

import java.util.Iterator;
import java.util.List;

import org.olat.connectors.webdav.WebDAVProvider;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.MergeSource;
import org.olat.data.commons.vfs.NamedContainerImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.system.commons.Formatter;
import org.olat.system.commons.resource.OLATResourceable;

/**
 * Description:<br>
 * TODO: guido Class Description for CoursefolderWebDAVProvider
 */
public class CoursefolderWebDAVProvider implements WebDAVProvider {

    private static final String MOUNTPOINT = "coursefolders";

    @Override
    public String getMountPoint() {
        return MOUNTPOINT;
    }

    // TODO OLAT-6874
    // we need two methods: one for retrieving top level containers (just with title of all courses for identity)
    // and the other for retrieving container with substructure for exactly one course
    @Override
    public VFSContainer getContainer(final Identity identity) {
        final MergeSource cfRoot = new MergeSource(null, null);
        final RepositoryService rm = RepositoryServiceImpl.getInstance();
        final List courseEntries = rm.queryByOwner(identity, CourseModule.getCourseTypeName());

        for (final Iterator iter = courseEntries.iterator(); iter.hasNext();) {
            final RepositoryEntry re = (RepositoryEntry) iter.next();
            final OLATResourceable res = re.getOlatResource();
            final ICourse course = CourseFactory.loadCourse(res.getResourceableId());
            final VFSContainer courseFolder = course.getCourseFolderContainer();
            // NamedContainerImpl cfContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(course.getCourseTitle()), courseFolder);
            NamedContainerImpl cfContainer;
            cfContainer = new NamedContainerImpl(Formatter.makeStringFilesystemSave(course.getCourseTitle()), courseFolder);
            cfRoot.addContainer(cfContainer);
        }
        return cfRoot;
    }

}
