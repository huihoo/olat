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
import java.io.IOException;

import org.jdom.JDOMException;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.lms.scorm.contentpackaging.NoItemFoundException;
import org.olat.lms.scorm.contentpackaging.ScormPackageHandler;
import org.olat.system.exception.AssertException;
import org.olat.system.exception.OLATRuntimeException;

public class SettingsHandlerImpl implements ISettingsHandler {
    private final String storagePath;
    private String filePath;
    private final String username;
    private final String userid;
    private String repoId;
    private String courseId;
    private final String cmi_lesson_mode;
    private final String cmi_credit_mode;
    private final File sequenceFile;
    private final File manifestFile;
    private final int controllerHashCode;

    /**
     * @param repositoryPath
     *            in olat style this means a path like: /home/guido/temp/olatdata/bcroot/repository/71560839208618/
     * @param repoId
     * @param courseId
     * @param userHome
     *            a path to the usersHome
     * @param username
     * @param userid
     * @param lesson_mode
     * @param credit_mode
     */
    public SettingsHandlerImpl(final String repositoryPath, final String repoId, final String courseId, final String storagePath, final String username,
            final String userid, final String lesson_mode, final String credit_mode, final int controllerHashCode) {
        if (repoId == null && courseId == null) {
            throw new AssertException("repositoryId and courseId are null but at leaset one should be a valid id");
        }
        this.controllerHashCode = controllerHashCode;
        this.repoId = repoId;
        this.courseId = courseId;
        this.storagePath = storagePath;
        this.username = username;
        this.userid = userid;
        this.cmi_lesson_mode = lesson_mode;
        this.cmi_credit_mode = credit_mode;
        setFilePath();
        // this file holds information of the SCO's like 'completed' or 'browsed' and ...
        sequenceFile = new File(getFilePath() + "/reload-settings.xml");
        manifestFile = new File(repositoryPath + "/imsmanifest.xml");

    }

    /**
	 */
    @Override
    public File getManifestFile() {
        return manifestFile;
    }

    /**
     * returns the reload-settings.xml file
     * 
     */
    @Override
    public File getScoItemSequenceFile() {
        // check if Scormpackage has been changed so that the reload-settings.xml will be updated.
        try {
            final ScormPackageHandler pm = new ScormPackageHandler(this);
            if (!sequenceFile.exists() || pm.checkIfScormPackageHasChanged()) {
                try {
                    pm.buildSettings();
                } catch (final NoItemFoundException e) {
                    throw new OLATRuntimeException(SettingsHandlerImpl.class, "Problem loading the reload-settings.xml file. No item found in scorm item sequence file!",
                            e);
                }
            }
        } catch (final JDOMException e) {
            throw new OLATRuntimeException(SettingsHandlerImpl.class, "Problem loading the reload-settings.xml file.", e);
        } catch (final IOException e) {
            throw new OLATRuntimeException(SettingsHandlerImpl.class, "Problem loading the reload-settings.xml file.", e);
        }
        return sequenceFile;
    }

    /**
     * @return a file handle path to the sequence file
     */
    @Override
    public String getScoItemSequenceFilePath() {
        return getFilePath() + "/reload-settings.xml";
    }

    /**
     * returns the sco data model (cmi data model) file
     * 
     */
    @Override
    public File getScoDataModelFile(final String itemId) {
        return (new File(getFilePath() + "/" + itemId + ".xml"));
    }

    /**
	 */
    @Override
    public String getStudentName() {
        return username;
    }

    /**
	 */
    @Override
    public String getStudentId() {
        return userid;
    }

    /**
	 */
    @Override
    public String getLessonMode() {
        return cmi_lesson_mode;
    }

    /**
	 */
    @Override
    public String getCreditMode() {
        return cmi_credit_mode;
    }

    /**
     * @return a String to the location the scorm sco data is beeing saved.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return a string either to the userhome or the olat temp dir.
     */
    private void setFilePath() {
        if (cmi_lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE) || cmi_lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW)) {
            if (courseId == null) {
                courseId = "";
            }
            if (repoId == null) {
                repoId = "";
            }
            final StringBuilder tempPath = new StringBuilder();
            tempPath.append(FolderConfig.getCanonicalRoot());
            tempPath.append("/tmp/");
            tempPath.append(controllerHashCode);
            tempPath.append("/");
            tempPath.append(userid);
            tempPath.append("/");
            if (courseId != null) {
                tempPath.append(courseId);
            }
            if (repoId != null) {
                tempPath.append(repoId);
            }
            filePath = tempPath.toString();
        } else {
            final StringBuilder path = new StringBuilder();
            path.append(storagePath);
            path.append("/scorm/");
            path.append(userid);
            path.append("/");
            if (courseId != null) {
                path.append(courseId);
            }
            if (repoId != null) {
                path.append(repoId);
            }
            filePath = path.toString();
        }
    }

    /**
	 * 
	 *
	 */
    public void saveFiles() {
        //
    }
}
