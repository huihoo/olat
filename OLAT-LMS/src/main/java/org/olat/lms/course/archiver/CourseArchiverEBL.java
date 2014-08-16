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
package org.olat.lms.course.archiver;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.user.UserService;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for CourseArchiverEBL
 * 
 * <P>
 * Initial Date: 21.09.2011 <br>
 * 
 * @author Branislav Balaz
 */
@Component
public class CourseArchiverEBL {

    @Autowired
    UserService userService;

    public boolean archiveCourseNode(String nodeId, Identity identity, OLATResourceable ores, Locale locale) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final CourseNode node = course.getRunStructure().getNode(nodeId);
        return archiveCourseNodeData(node, course, identity, locale);
    }

    public boolean existsExportDirForCourseLogFiles(Identity identity, String courseTitle) {
        final File exportDir = CourseFactory.getDataExportDirectory(identity, courseTitle);
        boolean exportDirExists = false;
        if (exportDir != null && exportDir.exists() && exportDir.isDirectory()) {
            exportDirExists = true;
        }
        return exportDirExists;
    }

    public CourseArchiverDataObjectEBL createFolderForArchiveCourseLogFiles(Identity identity, OLATResourceable ores) {
        final ICourse course = CourseFactory.loadCourse(ores);
        final String personalFolderDir = CourseFactory.getPersonalDirectory(identity).getPath();
        String targetDir = CourseFactory.getOrCreateDataExportDirectory(identity, course.getCourseTitle()).getPath();
        String relPath = "";

        if (targetDir.startsWith(personalFolderDir)) {

            relPath = targetDir.substring(personalFolderDir.length()).replace("\\", "/");
            targetDir = targetDir.substring(0, personalFolderDir.length());
        }

        final VFSContainer targetFolder = new LocalFolderImpl(new File(targetDir));
        if (relPath.length() != 0) {
            if (!relPath.endsWith("/")) {
                relPath = relPath + "/";
            }

        }
        return new CourseArchiverDataObjectEBL(targetFolder, relPath);

    }

    public String archiveScoreAccounting(Identity identity, OLATResourceable ores, Locale locale) {
        final ICourse course = CourseFactory.loadCourse(ores);
        @SuppressWarnings("unchecked")
        final List<Identity> users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
        @SuppressWarnings("unchecked")
        final List<CourseNode> nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());

        final String result = ScoreAccountingHelper.createCourseResultsOverviewTable(users, nodes, course, locale);

        final String courseTitle = course.getCourseTitle();

        final String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle, "xls");
        final File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(identity, courseTitle);
        final String charset = userService.getUserCharset(identity);

        ExportUtil.writeContentToFile(fileName, result, exportDirectory, charset);
        return fileName;
    }

    public boolean archiveCourseNodeData(CourseNode courseNode, ICourse course, Identity identity, Locale locale) {
        final File exportDir = CourseFactory.getOrCreateDataExportDirectory(identity, course.getCourseTitle());
        final String charset = userService.getUserCharset(identity);
        return courseNode.archiveNodeData(locale, course, exportDir, charset);
    }
}
