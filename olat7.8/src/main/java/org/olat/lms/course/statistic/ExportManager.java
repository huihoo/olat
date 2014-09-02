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

package org.olat.lms.course.statistic;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.ExportUtil;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.LocalFileImpl;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.course.statistic.export.ICourseLogExporter;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * The Manager exports the course log files and statistic file as zip archive.
 * <P>
 * Initial Date: 19.11.2009 <br>
 * 
 * @author bja
 */
public class ExportManager extends BasicManager {

    /** the logging object used in this class **/
    private static final Logger log = LoggerHelper.getLogger();

    /** ExportManager is a singleton, configured by spring **/
    private static ExportManager INSTANCE;

    /**
     * filename used to store courseauthor's activities (personalized)
     */
    private static final String FILENAME_ADMIN_LOG = "course_admin_log.csv";
    /**
     * filename used to store all user's activities (personalized) in the course only visible for OLAT-admins
     */
    private static final String FILENAME_USER_LOG = "course_user_log.csv";
    /**
     * filename used to store all user's activities (anonymized) in the course
     */
    private static final String FILENAME_STATISTIC_LOG = "course_statistic_log.csv";
    /**
     * filename course statistic
     */
    private static final String FILENAME_COURSE_STATISTIC = "course_statistic.csv";
    /**
     * zip filename substring (archive log files)
     */
    public static final String COURSE_LOG_FILES = "CourseLogFiles";
    /**
     * zip filename substring (statistic)
     */
    public static final String COURSE_STATISTIC = "CourseStatistic";

    /** injected via spring **/
    private ICourseLogExporter courseLogExporter;

    /** created via spring **/
    private ExportManager() {
        INSTANCE = this;
    }

    /** injected via spring **/
    public void setCourseLogExporter(final ICourseLogExporter courseLogExporter) {
        this.courseLogExporter = courseLogExporter;
    }

    /**
     * @return Singleton.
     */
    public static final ExportManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ExportManager bean not created via spring. Configuration error!");
        }
        return INSTANCE;
    }

    /**
     * Archives the course log files
     * 
     * @param oresID
     * @param exportDir
     * @param begin
     * @param end
     * @param adminLog
     * @param userLog
     * @param statisticLog
     * @param charset
     * @param locale
     * @param email
     */
    public void archiveCourseLogFiles(final Long oresID, final String exportDir, final Date begin, final Date end, final boolean adminLog, final boolean userLog,
            final boolean statisticLog, final String charset, final Locale locale, final String email) {

        final String zipName = ExportUtil.createFileNameWithTimeStamp(ExportManager.COURSE_LOG_FILES, "zip");
        final Date date = new Date();
        final String tmpDirName = oresID + "-" + date.getTime();
        final VFSContainer tmpDirVFSContainer = new OlatRootFolderImpl(new File(FolderConfig.getRelativeTmpDir(), tmpDirName).getPath(), null);
        final File tmpDir = new File(new File(FolderConfig.getCanonicalRoot(), FolderConfig.getRelativeTmpDir()), tmpDirName);

        final List<VFSItem> logFiles = new ArrayList<VFSItem>();
        if (adminLog) {
            logFiles.add(createLogFile(oresID, begin, end, charset, tmpDirVFSContainer, tmpDir, FILENAME_ADMIN_LOG, true, false));
        }
        if (userLog) {
            logFiles.add(createLogFile(oresID, begin, end, charset, tmpDirVFSContainer, tmpDir, FILENAME_USER_LOG, false, false));
        }
        if (statisticLog) {
            logFiles.add(createLogFile(oresID, begin, end, charset, tmpDirVFSContainer, tmpDir, FILENAME_STATISTIC_LOG, false, true));
        }

        saveFile(exportDir, zipName, tmpDirVFSContainer, logFiles, email);
    }

    /**
     * Create the actual log file.
     * <p>
     * Note: vfsContainer and dir must point to the very same directory. This is necessary to allow converting of the resulting file into a VFSLeaf (which is required by
     * the zip util class) and to have the absolute path name of the vfsContainer for the sql export (there's no getter on the VFSContainer for the absolute path - this
     * is core reason why). This 'hack' is not very nice but we'll live with it for the moment.
     * <p>
     * 
     * @param oresID
     * @param begin
     * @param end
     * @param charset
     * @param vfsContainer
     * @param dir
     * @param filename
     * @param resourceAdminAction
     * @param anonymize
     * @return
     */
    private VFSItem createLogFile(final Long oresID, final Date begin, final Date end, final String charset, final VFSContainer vfsContainer, final File dir,
            final String filename, final boolean resourceAdminAction, final boolean anonymize) {

        final File outFile = new File(dir, filename);
        // trigger the course log exporter - it will store the file to outFile
        log.info("createLogFile: start exporting course log file " + outFile.getAbsolutePath());
        courseLogExporter.exportCourseLog(outFile, charset, oresID, begin, end, resourceAdminAction, anonymize);
        log.info("createLogFile: finished exporting course log file " + outFile.getAbsolutePath());
        final VFSItem logFile = vfsContainer.resolve(filename);
        if (logFile == null) {
            log.warn("createLogFile: could not resolve " + filename);
        }
        return logFile;
    }

    private void saveFile(final String targetDir, final String fileName, final VFSContainer tmpDir, final List<VFSItem> files, final String email) {
        final File file = new File(targetDir, fileName);
        final VFSLeaf exportFile = new LocalFileImpl(file);
        if (!ZipUtil.zip(files, exportFile, true)) {
            // cleanup zip file
            exportFile.delete();
        } else {
            // success
            log.info("file successfully saved - fileName: " + fileName);
        }
        removeTemporaryFolder(tmpDir);
    }

    public File getLatestCourseStatisticFile(final String targetDir) {
        final File courseStatisticsDir = new File(targetDir);
        final File[] exportedCourseStatisticZipFiles = courseStatisticsDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.startsWith(ExportManager.COURSE_STATISTIC) && name.endsWith(".zip");
            }

        });
        if (exportedCourseStatisticZipFiles == null || exportedCourseStatisticZipFiles.length == 0) {
            return null;
        }

        if (exportedCourseStatisticZipFiles.length == 1) {
            return exportedCourseStatisticZipFiles[0];
        }

        // we have more than one - return the newest
        File newestFile = exportedCourseStatisticZipFiles[0];
        for (int i = 0; i < exportedCourseStatisticZipFiles.length; i++) {
            final File file = exportedCourseStatisticZipFiles[i];
            if (file.lastModified() > newestFile.lastModified()) {
                newestFile = file;
            }
        }

        return newestFile;
    }

    /**
     * remove temporary folder
     * 
     * @param tmpDir
     */
    private void removeTemporaryFolder(final VFSContainer tmpDir) {
        tmpDir.delete();
    }

}
