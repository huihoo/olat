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

import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.repository.RepositoryEntryImportExport;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for ExportDataEBL
 * 
 * <P>
 * Initial Date: 02.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class CourseExportEBL {

    /**
     * The folder where nodes export their data to.
     * 
     * @param theCourse
     * @return File
     */
    public File getExportDataDir(final ICourse theCourse) {
        LocalFolderImpl vfsExportDir = (LocalFolderImpl) theCourse.getCourseBaseContainer().resolve(ICourse.EXPORTED_DATA_FOLDERNAME);
        if (vfsExportDir == null) {
            vfsExportDir = (LocalFolderImpl) theCourse.getCourseBaseContainer().createChildContainer(ICourse.EXPORTED_DATA_FOLDERNAME);
        }
        return vfsExportDir.getBasefile();
    }

    public void exportNode(final File exportDirectory, final CourseNode courseNode) {
        final RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
        if (re == null) {
            return;
        }
        final File fExportDirectory = new File(exportDirectory, courseNode.getIdent());
        fExportDirectory.mkdirs();
        final RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
        reie.exportDoExport();
    }

    /*
     * public void exportTANode(final File fExportDirectory, final ICourse course) { // export only this taskfolder's tasks final File fTaskFolder = new
     * File(FolderConfig.getCanonicalRoot() + TACourseNode.getTaskFolderPathRelToFolderRoot(course, this)); final File fNodeExportDir = new File(fExportDirectory,
     * this.getIdent()); fNodeExportDir.mkdirs(); FileUtils.copyDirContentsToDir(fTaskFolder, fNodeExportDir, false, "export task course node"); }
     */

}
