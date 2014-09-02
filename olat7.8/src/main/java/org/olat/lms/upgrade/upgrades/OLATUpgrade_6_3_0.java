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

package org.olat.lms.upgrade.upgrades;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.fileutil.ZipUtil;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.course.statistic.LoggingVersionDaoImpl;
import org.olat.data.forum.Message;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.textservice.TextService;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.PersistingCourseImpl;
import org.olat.lms.forum.ForumService;
import org.olat.lms.repository.RepositoryServiceImpl;
import org.olat.lms.repository.delete.DeletionModule;
import org.olat.lms.upgrade.UpgradeHistoryData;
import org.olat.lms.upgrade.UpgradeManager;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Upgrade to OLAT 6.2: - Migration of old wiki-fields to flexiform Code is already here for every update. Method calls will be commented out step by step when
 * corresponding new controllers are ready. As long as there will be other things to migrate Upgrade won't be set to DONE!
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_3_0 extends OLATUpgrade {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String VERSION = "OLAT_6.3";
    private static final String TASK_CLEANUP_TMP_UPLOAD_FILES_KEY = "cleanupTmpUploadFiles";
    private static final String TASK_MIGRATE_FORUMS_MESSAGES = "Migrate forums messages to add word and character count";
    private static final String TASK_MIGRATE_NOTIFICATIONS = "Migrate notifications publishers";
    private static final String TASK_MIGRATE_COURSE_LOG_FILES = "Migrate course log files to course folders and deleted dir";

    /** filename used to store courseauthor's activities (personalized) - relict from PersistingAuditManager **/
    public static final String FILENAME_ADMIN_LOG = "course_admin_log.txt";

    /** readme filename used by old CourseLogsArchiveManager - relict from CourseLogsArchiveManager **/
    private static final String README = "README.txt";

    /**
     * filename used to store all user's activities (personalized) in the course only visible for OLAT-admins - relict from PersistingAuditManager
     */
    public static final String FILENAME_USER_LOG = "course_user_log.txt";
    /** filename used to store all user's activities (anonymised) in the course - relict from PersistingAuditManager **/
    public static final String FILENAME_STATISTIC_LOG = "course_statistic_log.txt";

    /** from PersistingCourseImpl which has this as private unfortunatelly **/
    private static final String COURSEFOLDER = "coursefolder";

    private static final String LOGS_DIRNAME = "logs";

    private static final String OLD_COURSE_LOGS_DIRNAME = "old_course_logs";
    private static final String OLD_COURSE_LOGS_ZIPFILENAME = "old_course_logs.zip";
    private static final String OLD_COURSE_LOGS_IN_APACHE_FORMAT_ZIPFILENAME = "old_course_logs_apache_format.zip";
    private static final String TASK_CLEANUP_BROKEN_COURSES = "cleanup_broken_courses";

    /** counter for statistics about what went wrong during apache course log migration **/
    private int filesWithApacheConversionErrors_ = 0;
    private final DeletionModule deletionModule;
    private final CourseModule courseModule;
    private final String nodeId;
    private final Object lockObject = new Object();
    @Autowired
    TextService languageService;
    @Autowired
    private ForumService forumService;

    /**
     * [used by spring]
     */
    public OLATUpgrade_6_3_0(final DeletionModule deletionModule, final CourseModule courseModule, final String nodeId) {
        this.deletionModule = deletionModule;
        this.courseModule = courseModule;
        this.nodeId = nodeId;
    }

    /**
	 */
    public boolean doPreSystemInitUpgrade(final UpgradeManager upgradeManager) {
        return false;
    }

    /**
	 */
    public boolean doPostSystemInitUpgrade(final UpgradeManager upgradeManager) {
        UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
        if (uhd == null) {
            // has never been called, initialize
            uhd = new UpgradeHistoryData();
        } else {
            if (uhd.isInstallationComplete()) {
                return false;
            }
        }

        // Cleanup temp upload files that are not deleted properly
        cleanupTmpUploadFiles(upgradeManager, uhd);
        // Migrate forums messages
        migrateMessages(upgradeManager, uhd);
        // Migrate course log files
        migrateCourseLogFiles(upgradeManager, uhd);
        // check and fix broken courses on the filesystem and database
        searchForBrokenCourses(upgradeManager, uhd);

        // set the logging version to 1 starting NOW
        new LoggingVersionDaoImpl().setLoggingVersionStartingNow(1);

        // // now pre and post code was ok, finish installation
        uhd.setInstallationComplete(true);
        // // persist infos
        upgradeManager.setUpgradesHistory(uhd, VERSION);
        return true;
    }

    private void searchForBrokenCourses(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_CLEANUP_BROKEN_COURSES)) {

            final String bcRoot = FolderConfig.getCanonicalRoot();
            final File courseFolder = new File(bcRoot + "/course");
            final String[] courseFolderNames = courseFolder.list(new FilenameFilter() {

                @Override
                public boolean accept(final File dir, final String name) {
                    try {
                        Long.parseLong(name);
                    } catch (final NumberFormatException e) {
                        return false;
                    }
                    return true;
                }
            });
            final List<String> courseList = Arrays.asList(courseFolderNames);
            int counter = 0;
            for (final String string : courseList) {
                final Long courseResId = Long.parseLong(string);
                final RepositoryEntry repoEntry = RepositoryServiceImpl.getInstance().lookupRepositoryEntry(
                        OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);
                if (repoEntry != null) {
                    // try to load course...
                    try {
                        // CourseFactory.loadCourse(courseResId);
                        // check whether the runstructure is there (faster then loading the whole course)
                        final File runstructure = new File(bcRoot + "/course/" + courseResId + "/runstructure.xml");
                        if (!runstructure.exists()) {
                            log.warn("Missing course structure file: " + runstructure.getAbsolutePath());
                        }
                        final File editstructure = new File(bcRoot + "/course/" + courseResId + "/editortreemodel.xml");
                        if (!editstructure.exists()) {
                            log.warn("Missing course structure file: " + editstructure.getAbsolutePath());
                        }
                        final File courseconfig = new File(bcRoot + "/course/" + courseResId + "/CourseConfig.xml");
                        if (!courseconfig.exists()) {
                            log.warn("Missing course structure file: " + courseconfig.getAbsolutePath());
                        }
                    } catch (final Exception e) {
                        log.warn("Could not load course for resId: " + courseResId, e);
                    }
                } else {
                    log.warn("No repositoryEntry found for: " + courseResId);
                }

                if (counter > 0 && counter % 100 == 0) {
                    log.info("Audit:Another 100 courses done");
                    DBFactory.getInstance().intermediateCommit();
                }
                counter++;
            }

            // now by database
            counter = 0;
            final List<RepositoryEntry> entries = RepositoryServiceImpl.getInstance().queryByType(CourseModule.ORES_TYPE_COURSE);
            for (final RepositoryEntry repositoryEntry : entries) {
                final Long courseResId = repositoryEntry.getOlatResource().getResourceableId();

                final File runstructure = new File(bcRoot + "/course/" + courseResId + "/runstructure.xml");
                if (!runstructure.exists()) {
                    log.warn("Course is in DB but not on Filesystem: Missing course structure file: " + runstructure.getAbsolutePath());
                }
                final File editstructure = new File(bcRoot + "/course/" + courseResId + "/editortreemodel.xml");
                if (!editstructure.exists()) {
                    log.warn("Course is in DB but not on Filesystem: Missing course structure file: " + editstructure.getAbsolutePath());
                }
                final File courseconfig = new File(bcRoot + "/course/" + courseResId + "/CourseConfig.xml");
                if (!courseconfig.exists()) {
                    log.warn("Course is in DB but not on Filesystem: Missing course structure file: " + courseconfig.getAbsolutePath());
                }

                if (counter > 0 && counter % 100 == 0) {
                    log.info("Audit:Another 100 courses done");
                    DBFactory.getInstance().intermediateCommit();
                }
                counter++;
            }

            uhd.setBooleanDataValue(TASK_CLEANUP_BROKEN_COURSES, false); // FIXME: set to true when done
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }

    }

    private void migrateMessages(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_FORUMS_MESSAGES)) {
            log.info("Audit:+-----------------------------------------------------------------------------+");
            log.info("Audit:+... Calcualating word and character count in existing forum posts         ...+");
            log.info("Audit:+-----------------------------------------------------------------------------+");

            int counter = 0;
            final List<Long> allForumKeys = forumService.getAllForumKeys();
            if (log.isDebugEnabled()) {
                log.info("Found " + allForumKeys.size() + " forums to migrate.");
            }

            for (final Long forumKey : allForumKeys) {
                final List<Message> allMessages = forumService.getMessagesByForumID(forumKey);
                for (final Message message : allMessages) {
                    try {
                        final String body = message.getBody();
                        final Locale locale = languageService.detectLocale(body);
                        final int characters = languageService.characterCount(body, locale);
                        message.setNumOfCharacters(characters);
                        final int words = languageService.wordCount(body, locale);
                        message.setNumOfWords(words);
                        counter++;

                        DBFactory.getInstance().updateObject(message);

                        if (counter > 0 && counter % 100 == 0) {
                            log.info("Audit:Another 100 messages done");
                            DBFactory.getInstance().intermediateCommit();
                        }
                    } catch (final Exception e) {
                        log.error("Error during Migration: " + e, e);
                        DBFactory.getInstance().rollback();
                    }
                }
            }

            DBFactory.getInstance().intermediateCommit();
            log.info("Audit:**** Migrated " + counter + " messages. ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_FORUMS_MESSAGES, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    private void cleanupTmpUploadFiles(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_CLEANUP_TMP_UPLOAD_FILES_KEY)) {
            log.info("Audit:+-----------------------------------------------------------------------------+");
            log.info("Audit:+... Cleaning up old temporary upload files                                ...+");
            log.info("Audit:+-----------------------------------------------------------------------------+");

            final File tempUploadDir = new File(WebappHelper.getUserDataRoot() + "/tmp/");
            long counter = 0;
            long mem = 0;
            if (tempUploadDir.exists() && tempUploadDir.isDirectory()) {
                // get all files that start with instanceID_NodeID_ followed by a number
                final FileFilter tmpUploadFileFilter = new RegexFileFilter(WebappHelper.getInstanceId() + "_" + nodeId + "_[0-9]*");
                final File[] tmpUploadFiles = tempUploadDir.listFiles(tmpUploadFileFilter);
                for (final File file : tmpUploadFiles) {
                    if (file.isFile() && file.exists()) {
                        mem += file.length();
                        file.delete();
                        counter++;
                    }
                }
            }
            // ok, all done, commit done task to upgrade manager
            uhd.setBooleanDataValue(TASK_CLEANUP_TMP_UPLOAD_FILES_KEY, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
            // some info output
            log.info("Deleted #" + counter + " temporary upload files that consumed a total of " + StringHelper.formatMemory(mem)
                    + " expensive diskspace. Pure happyness for your sysadmin.");
        }
    }

    private void migrateCourseLogFiles(final UpgradeManager upgradeManager, final UpgradeHistoryData uhd) {
        if (!uhd.getBooleanDataValue(TASK_MIGRATE_COURSE_LOG_FILES)) {
            log.info("Audit:+-----------------------------------------------------------------------------+");
            log.info("Audit:+... Migrate the Course Log Files                                          ...+");
            log.info("Audit:+-----------------------------------------------------------------------------+");

            // doing an intermediate commit at the start to make sure we don't have any transaction open
            // further below we don't do anything with the database, it's raw file operations, hence
            // no further intermediatecommit is needed afterwards
            DBFactory.getInstance().intermediateCommit();

            final File globalOldCourseLogsDir = new File(deletionModule.getArchiveRootPath(), OLD_COURSE_LOGS_DIRNAME);
            if (globalOldCourseLogsDir.exists() && !globalOldCourseLogsDir.isDirectory()) {
                log.error("**** !!!! Resource exists but is not a directory - cannot move course log files: " + globalOldCourseLogsDir.getAbsolutePath());
                throw new IllegalStateException("Resource exists but is not a directory - cannot move course log files - owner/permission issue?: "
                        + globalOldCourseLogsDir.getAbsolutePath());
                // globalOldCourseLogsDir = null;
            } else if (!globalOldCourseLogsDir.exists() && !globalOldCourseLogsDir.mkdirs()) {
                log.error("**** !!!! Cannot create directory - cannot move course log files: " + globalOldCourseLogsDir.getAbsolutePath());
                throw new IllegalStateException("Cannot create directory - cannot move course log files - owner/permission issue?: "
                        + globalOldCourseLogsDir.getAbsolutePath());
                // globalOldCourseLogsDir = null;
            }

            final File courseRootDir = new File(FolderConfig.getCanonicalRoot() + File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME);

            final File[] dirs = courseRootDir.listFiles();
            int nonCourseDirs = 0;
            int migratedCourses = 0;
            int zipErrors = 0;
            int moveErrors = 0;
            int coursesWithoutLogsDir = 0;
            for (int i = 0; i < dirs.length; i++) {
                final File aDir = dirs[i];
                nonCourseDirs++;
                if (!aDir.isDirectory()) {
                    continue;
                }
                if (!aDir.getName().matches("[0123456789]*")) {
                    continue;
                }
                if (!aDir.getName().toLowerCase().equals(aDir.getName().toUpperCase())) {
                    // kind of superfluous check, but still...
                    continue;
                }
                nonCourseDirs--;
                final File courseLogDir = new File(aDir, LOGS_DIRNAME);
                if (!courseLogDir.isDirectory() || !courseLogDir.exists()) {
                    coursesWithoutLogsDir++;
                    continue;
                }

                final boolean zipSuccess = zipCourseLogFiles(courseLogDir);
                boolean moveSuccess = false;
                if (zipSuccess && globalOldCourseLogsDir != null) {
                    moveSuccess = moveInvisibleCourseLogFiles(courseLogDir, globalOldCourseLogsDir);
                }

                if (!zipSuccess) {
                    zipErrors++;
                }
                if (!moveSuccess) {
                    moveErrors++;
                }
                migratedCourses++;

                if (migratedCourses > 0 && migratedCourses % 100 == 0) {
                    log.info("Audit:Another 100 course log files migrated, " + migratedCourses + " done. Total-Dirs: " + dirs.length + ", Non-Course-Dirs: "
                            + nonCourseDirs + ". Courses without logs dir: " + coursesWithoutLogsDir + ". Errors: " + zipErrors + " zip errors, " + moveErrors
                            + " move errors, " + filesWithApacheConversionErrors_ + " apache-conversion errors. ****");
                }
            }

            log.info("Audit:**** Migrated " + migratedCourses + " courses. Total-Dirs: " + dirs.length + ", Non-Course-Dirs: " + nonCourseDirs
                    + ". Courses without logs dir: " + coursesWithoutLogsDir + ". Errors: " + zipErrors + " zip errors, " + moveErrors + " move errors, "
                    + filesWithApacheConversionErrors_ + " apache-conversion errors ****");
            uhd.setBooleanDataValue(TASK_MIGRATE_COURSE_LOG_FILES, true);
            upgradeManager.setUpgradesHistory(uhd, VERSION);
        }
    }

    public File createTempDirectory() {
        File temp = null;

        try {
            temp = File.createTempFile("temp_olat_migrate", Long.toString(System.nanoTime()));
        } catch (final IOException ioe) {
            log.error("**** !!!! Could not get temporary file");
        }

        if (!(temp.delete())) {
            log.error("**** !!!! Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            log.error("**** !!!! Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }

    private boolean zipCourseLogFiles(final File courseLogDir) {
        final File[] logFiles = courseLogDir.listFiles();
        final Set<String> toBeZippedFiles = new HashSet<String>();
        final Set<File> toBeDeletedFiles = new HashSet<File>();
        File readMe = null;
        for (int i = 0; i < logFiles.length; i++) {
            final File logFile = logFiles[i];
            final String logFileName = logFile.getName();
            if (logFileName.equals(FILENAME_ADMIN_LOG) && courseModule.isAdminLogVisibleForMigrationOnly()) {
                toBeZippedFiles.add(logFile.getName());
                toBeDeletedFiles.add(logFile);
            } else if (logFileName.equals(FILENAME_USER_LOG) && courseModule.isUserLogVisibleForMigrationOnly()) {
                toBeZippedFiles.add(logFile.getName());
                toBeDeletedFiles.add(logFile);
            } else if (logFileName.equals(FILENAME_STATISTIC_LOG) && courseModule.isStatisticLogVisibleForMigrationOnly()) {
                toBeZippedFiles.add(logFile.getName());
                toBeDeletedFiles.add(logFile);
            } else if (logFileName.equals(README)) {
                readMe = logFile;
            }
        }

        if (readMe != null && toBeZippedFiles.size() > 0) {
            toBeZippedFiles.add(readMe.getName());
            toBeDeletedFiles.add(readMe);
        }

        final File courseFolder = new File(courseLogDir.getParentFile(), COURSEFOLDER);
        if (courseFolder.exists() && !courseFolder.isDirectory()) {
            log.error("**** !!!! Could not migrate course log files for " + courseLogDir.getParentFile().getName() + " as there is a file called '" + COURSEFOLDER
                    + " which was expected to be a directory: " + courseFolder.getAbsolutePath());
            return false;
        }

        if (!courseFolder.exists()) {
            if (!courseFolder.mkdirs()) {
                log.error("**** !!!! Could not create directory " + courseFolder.getAbsolutePath());
                return false;
            }
        }

        final File oldCourseLogsDir = new File(courseFolder, OLD_COURSE_LOGS_DIRNAME);
        if (oldCourseLogsDir.exists()) {
            log.error("**** !!!! " + OLD_COURSE_LOGS_DIRNAME + " alreday existed! Not migrating course. Dir= " + oldCourseLogsDir.getAbsolutePath());
            return false;
        }
        if (!oldCourseLogsDir.mkdirs()) {
            log.error("**** !!!! Could not create directory " + oldCourseLogsDir.getAbsolutePath());
            return false;
        }

        final File oldCourseLogsZip = new File(oldCourseLogsDir, OLD_COURSE_LOGS_ZIPFILENAME);
        if (!ZipUtil.zip(toBeZippedFiles, courseLogDir, oldCourseLogsZip, true)) {
            log.error("**** !!!! Could not zip course log files from " + courseLogDir + ", into " + oldCourseLogsZip.getAbsolutePath());
            return false;
        }

        // now convert those files into apache log format
        final File tempDir = createTempDirectory();
        final Set<String> toBeApacheLoggedFiles = new HashSet<String>();
        for (final Iterator<File> it = toBeDeletedFiles.iterator(); it.hasNext();) {
            final File toBeApacheLoggedFile = it.next();
            if (toBeApacheLoggedFile.getName().equals(README)) {
                // ignore the readme file
                continue;
            }
            final File apacheLogFile = readSequence(toBeApacheLoggedFile, tempDir);
            toBeApacheLoggedFiles.add(apacheLogFile.getName());
        }
        final File oldCourseLogsInApacheFormatZip = new File(oldCourseLogsDir, OLD_COURSE_LOGS_IN_APACHE_FORMAT_ZIPFILENAME);
        if (!ZipUtil.zip(toBeApacheLoggedFiles, tempDir, oldCourseLogsInApacheFormatZip, true)) {
            log.error("**** !!!! Could not zip course log files (those in apache format) from " + tempDir + ", into " + oldCourseLogsInApacheFormatZip.getAbsolutePath());
            if (!FileUtils.deleteDirsAndFiles(tempDir, true, true)) {
                tempDir.deleteOnExit();
            }
            return false;
        }
        if (!FileUtils.deleteDirsAndFiles(tempDir, true, true)) {
            tempDir.deleteOnExit();
        }

        // now delete those files
        for (final Iterator<File> it = toBeDeletedFiles.iterator(); it.hasNext();) {
            final File toBeDeletedFile = it.next();
            if (!toBeDeletedFile.delete()) {
                log.error("**** !!!! Could not delete file " + toBeDeletedFile.getAbsolutePath());
                return false;
            }
        }

        return true;
    }

    private boolean moveInvisibleCourseLogFiles(final File courseLogDir, final File globalOldCourseLogsDir) {
        File[] logFiles = courseLogDir.listFiles();
        final Set<File> toBeMovedFiles = new HashSet<File>();
        for (int i = 0; i < logFiles.length; i++) {
            final File logFile = logFiles[i];
            final String logFileName = logFile.getName();
            if (logFileName.equals(FILENAME_ADMIN_LOG) && !courseModule.isAdminLogVisibleForMigrationOnly()) {
                toBeMovedFiles.add(logFile);
            } else if (logFileName.equals(FILENAME_USER_LOG) && !courseModule.isUserLogVisibleForMigrationOnly()) {
                toBeMovedFiles.add(logFile);
            } else if (logFileName.equals(FILENAME_STATISTIC_LOG) && !courseModule.isStatisticLogVisibleForMigrationOnly()) {
                toBeMovedFiles.add(logFile);
            }
        }

        final File concreteOldCourseLogsDir = new File(globalOldCourseLogsDir, courseLogDir.getParentFile().getName());
        if (concreteOldCourseLogsDir.exists() && !concreteOldCourseLogsDir.isDirectory()) {
            log.error("**** !!!! Resource exists but is not a directory: " + concreteOldCourseLogsDir.getAbsolutePath());
            return false;
        } else if (!concreteOldCourseLogsDir.exists() && !concreteOldCourseLogsDir.mkdirs()) {
            log.error("**** !!!! Could not create directory:: " + concreteOldCourseLogsDir.getAbsolutePath());
            return false;
        }

        for (final Iterator<File> it = toBeMovedFiles.iterator(); it.hasNext();) {
            final File toBeMovedFile = it.next();
            final File targetFile = new File(concreteOldCourseLogsDir, toBeMovedFile.getName());
            if (!toBeMovedFile.renameTo(targetFile)) {
                log.error("**** !!!! Could not move file " + toBeMovedFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return false;
            }
        }

        logFiles = courseLogDir.listFiles();
        if (logFiles != null && logFiles.length > 0) {
            log.warn("**** !!!! Directory is not empty: " + courseLogDir.getAbsolutePath());
            return false;
        }

        if (!courseLogDir.delete()) {
            log.error("**** !!!! Could not delete directory: " + courseLogDir.getAbsolutePath());
            return false;
        }

        return true;
    }

    /** copied from 6.2.x version of CourseLogsArchiveManager and modified file handling to avoid going via VFS **/
    private File readSequence(final File leaf, final File outDir) {

        String line;
        final File resultingFile = new File(outDir, leaf.getName());

        BufferedReader br = null;
        FileOutputStream fos = null;
        BufferedWriter writer = null;
        boolean zeroErrors = true;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(leaf)));
            fos = new FileOutputStream(resultingFile);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            while (null != (line = br.readLine())) {
                line = convertLine(line);
                // <MODIFIED FOR SAFETY>
                if (line.length() == 0) {
                    log.warn("**** !!!! Conversion failed with file: " + leaf);
                    zeroErrors = false;
                }
                // </MODIFIED FOR SAFETY>
                writer.append(line);
                writer.append("\r\n");
            }
        } catch (final IOException e) {
            log.error("**** !!!! Could not convert file to apache format: " + leaf);
            return null;
        } finally {
            if (!zeroErrors) {
                filesWithApacheConversionErrors_++;
            }
            if (br != null) {
                try {
                    br.close();
                } catch (final Exception e) {
                    // this empty catch is ok
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (final Exception e) {
                    // this empty catch is ok
                }
            }
        }
        return resultingFile;
    }

    /** copied 1:1 from 6.2.x version of CourseLogsArchiveManager **/
    private String convertLine(final String line) {
        final StringBuilder sb = new StringBuilder();
        final String[] splitters = line.split("\t");

        // <MODIFIED FOR SAFETY>
        if (splitters.length < 5) {
            log.error("**** !!!! Could not convert line - fewer than 5 fields: " + line);
            return "";
        }
        // </MODIFIED FOR SAFETY>

        sb.append(splitters[2]);
        sb.append(" - ");
        sb.append(splitters[2]);

        final String timeStamp = splitters[0];
        sb.append(" [");
        sb.append(timeStamp.substring(8, 10)); // day
        sb.append("/");
        sb.append(getMonth(timeStamp.substring(5, 7))); // month
        sb.append("/");
        sb.append(timeStamp.substring(0, 4)); // year
        sb.append(":");
        sb.append(timeStamp.substring(11, 16));
        sb.append(" +0000] \"GET /");
        sb.append(splitters[3].trim());
        sb.append("_");
        sb.append(splitters[4].replaceAll(" ", "_"));
        if (splitters.length > 5) {
            sb.append("_");
            sb.append(splitters[5].trim());
        }
        if (splitters.length > 6) {
            sb.append("_");
            sb.append(splitters[6].trim());
        }

        sb.append(" HTTP/1.0\" 200 100");
        return sb.toString();
    }

    /** copied 1:1 from 6.2.x version of CourseLogsArchiveManager **/
    private String getMonth(String num) {
        if (num.startsWith("0")) {
            num = num.substring(1);
        }
        final int i = Integer.parseInt(num);
        final String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        return months[i - 1];
    }

    public String getVersion() {
        return VERSION;
    }

}
