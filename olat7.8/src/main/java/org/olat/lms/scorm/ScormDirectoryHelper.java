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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.lms.scorm;

import java.io.File;

import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.lms.course.nodes.ScormCourseNode;
import org.olat.lms.course.run.environment.CourseEnvironment;

/**
 * <P>
 * Initial Date: 14 august 2009 <br>
 * 
 * @author srosse
 */
public class ScormDirectoryHelper {

    /**
     * Return the SCORM Root folder
     */
    public static VFSContainer getScormRootFolder() {
        final VFSContainer canonicalRoot = new LocalFolderImpl(new File(FolderConfig.getCanonicalRoot()));
        return (VFSContainer) canonicalRoot.resolve("scorm");
    }

    /**
     * Return the container where the LMS save the datas for a user.
     * 
     * @param username
     * @param courseEnv
     * @param node
     * @return
     */
    public static VFSContainer getScoDirectory(final String username, final CourseEnvironment courseEnv, final ScormCourseNode node) {
        final Long courseId = courseEnv.getCourseResourceableId();
        final VFSItem userFolder = ScormDirectoryHelper.getScormRootFolder().resolve(username);
        if (userFolder != null) {
            final VFSItem scoFolder = userFolder.resolve(courseId.toString() + "-" + node.getIdent());
            if (scoFolder instanceof VFSContainer) {
                return (VFSContainer) scoFolder;
            }
        }
        return null;
    }
}
