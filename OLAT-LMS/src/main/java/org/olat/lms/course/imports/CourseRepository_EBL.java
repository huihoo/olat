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
package org.olat.lms.course.imports;

import java.io.File;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.tree.CourseEditorTreeNode;
import org.olat.system.commons.Formatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 13.09.2011 <br>
 * 
 * @author cg
 */
@Component
public class CourseRepository_EBL {

    @Autowired
    private CourseExportEBL courseExportEbl;

    public void saveCourseAndCloseEditSession(ICourse course) {
        CourseFactory.saveCourse(course.getResourceableId());
        CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
    }

    public void setShortAndLongTitle(String displayName, ICourse course) {
        setCourseRunShortAndLongTitle(displayName, course.getRunStructure().getRootNode());
        setCourseEditShortAndLongTitle(displayName, (CourseEditorTreeNode) course.getEditorTreeModel().getRootNode());
    }

    private void setCourseEditShortAndLongTitle(String displayName, CourseEditorTreeNode editorRootNode) {
        editorRootNode.getCourseNode().setShortTitle(Formatter.truncateOnly(displayName, 25)); // do not use truncate!
        editorRootNode.getCourseNode().setLongTitle(displayName);
    }

    private void setCourseRunShortAndLongTitle(String displayName, CourseNode courseNode) {
        courseNode.setShortTitle(Formatter.truncateOnly(displayName, 25)); // do not use truncate!
        courseNode.setLongTitle(displayName);
    }

    public void cleanupExportDataDir(ICourse course) {
        if (course == null) {
            return;
        }
        final File fExportedDataDir = courseExportEbl.getExportDataDir(course);
        if (fExportedDataDir.exists()) {
            FileUtils.deleteDirsAndFiles(fExportedDataDir, true, true);
        }
    }

}
