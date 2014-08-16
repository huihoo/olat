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
package org.olat.lms.scorm;

import java.io.File;

import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFolderImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.lms.commons.fileresource.FileResourceManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial Date: 26.09.2011 <br>
 * 
 * @author guretzki
 */
@Component
public class ScormEBL {

    private static final String IMSMANIFEST_XML = "imsmanifest.xml";
    @Autowired
    private FileResourceManager fileResourceManager;

    /**
     * @param scormOlatResourceable
     * @return
     */
    public String unzipFileResource(OLATResourceable scormOlatResourceable) {
        final File cpRoot = fileResourceManager.unzipFileResource(scormOlatResourceable);
        return cpRoot.getAbsolutePath();
    }

    /**
     * @param scormFolderPath
     * @return
     */
    public boolean imsManifestFileExists(String scormFolderPath) {
        return getManifestFile(scormFolderPath).exists();
    }

    /**
     * @param scormFolderPath
     * @return
     */
    public File getManifestFile(String scormFolderPath) {
        return new File(scormFolderPath, IMSMANIFEST_XML);
    }

    /**
     * @param scormFolderPath
     * @return
     */
    public VFSContainer getScormFolder(String scormFolderPath) {
        return new LocalFolderImpl(new File(scormFolderPath));
    }

    /**
     * @param scorm_lesson_mode
     * @param hashCode
     */
    public void cleanUpCollectedScormData(String scorm_lesson_mode, int hashCode) {
        if (scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE) || scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW)) {
            final String path = FolderConfig.getCanonicalRoot() + File.separator + "tmp" + File.separator + hashCode;
            FileUtils.deleteDirsAndFiles(new File(path), true, true);
        }
    }

}
