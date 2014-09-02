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

package org.olat.lms.user.administration.delete;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.fileutil.FileUtils;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.course.CourseFactory;
import org.olat.lms.course.ICourse;
import org.olat.lms.course.PersistingCourseImpl;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.TACourseNode;
import org.olat.lms.course.nodes.ta.Dropbox_EBL;
import org.olat.lms.course.nodes.ta.Returnbox_EBL;
import org.olat.lms.ims.qti.editor.QTIEditorPackageEBL;
import org.olat.lms.user.UserDataDeletable;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.taskexecutor.TaskExecutorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manager to delete all files for user-deletion workflow.
 * 
 * @author Christian Guretzki
 */
@Service
public class UserFileDeletionManager extends BasicManager implements UserDataDeletable {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    TaskExecutorManager taskExecutorManager;

    private UserFileDeletionManager() {
        // [Spring]
    }

    public static final AtomicInteger numberOfRunningFileDeletionThreads = new AtomicInteger();

    @Override
    public void deleteUserData(final Identity identity, final String newDeletedUserName) {
        numberOfRunningFileDeletionThreads.incrementAndGet();
        log.debug("deleteUserData numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
        taskExecutorManager.runTask(new Runnable() {

            @Override
            public void run() {
                log.debug("run start numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
                long startTime = 0;
                log.info("Start UserFileDeletionManager thread for identity=" + identity);
                if (log.isDebugEnabled()) {
                    startTime = System.currentTimeMillis();
                }
                /*
                 * commented due to issue OLAT-6493 - should be replaced with another separate job going to be implemented deleteAllDropboxReturnboxFilesOf(identity);
                 */
                if (log.isDebugEnabled()) {
                    log.debug("User-Deletion: deleteAllDropboxFiles takes " + (System.currentTimeMillis() - startTime) + "ms");
                    startTime = System.currentTimeMillis();
                }
                deleteHomesMetaDataOf(identity, newDeletedUserName);
                if (log.isDebugEnabled()) {
                    log.debug("User-Deletion: renameAllFileMetadata takes " + (System.currentTimeMillis() - startTime) + "ms");
                    startTime = System.currentTimeMillis();
                }
                deleteAllTempQtiEditorFilesOf(identity);
                if (log.isDebugEnabled()) {
                    log.debug("User-Deletion: deleteAllTempQtiEditorFiles takes " + (System.currentTimeMillis() - startTime) + "ms");
                }
                log.info("Finished UserFileDeletionManager thread for identity=" + identity);
                UserFileDeletionManager.numberOfRunningFileDeletionThreads.decrementAndGet();
                log.debug("run end numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
            }
        });
    }

    /**
     * @return true if no deletion-thread is running
     */
    public static boolean isReadyToDelete() {
        return numberOfRunningFileDeletionThreads.get() == 0;
    }

    private void deleteAllTempQtiEditorFilesOf(final Identity identity) {
        // Temp QTI-editor File path e.g. /usr/local/olatfs/olat/olatdata/tmp/qtieditor/schuessler
        final File userTempQtiEditorDir = new File(QTIEditorPackageEBL.getTmpBaseDir(), identity.getName());
        if (userTempQtiEditorDir.exists()) {
            FileUtils.deleteDirsAndFiles(userTempQtiEditorDir, true, true);
            log.info("Audit:User-Deletion: identity=" + identity.getName() + " : QTI editor temp files deleted under dir=" + userTempQtiEditorDir.getAbsolutePath());
        }
    }

    private void deleteHomesMetaDataOf(final Identity identity, final String newDeletedUserName) {
        final String metaRootDirPath = FolderConfig.getCanonicalMetaRoot();
        final File metaRootDir = new File(metaRootDirPath);
        final File[] homesDirs = metaRootDir.listFiles(new UserFileFilter(FolderConfig.getUserHomes().substring(1)));
        if ((homesDirs != null) && (homesDirs.length == 1)) {
            final File[] userDirs = homesDirs[0].listFiles(new UserFileFilter(identity.getName()));
            // userDirs can contain only home-dir of deleted user
            if (userDirs.length > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("deleteHomesMetaDataOf: Delete meta-data homes/" + identity.getName() + " dir, process file=" + userDirs[0].getAbsolutePath());
                }
                // the meta-data under home/<USER> can be deleted and must not be renamed
                FileUtils.deleteDirsAndFiles(userDirs[0], true, true);
                log.info("Audit:User-Deletion: Delete meta-data homes directory for identity=" + identity.getName() + " directory=" + userDirs[0].getAbsolutePath());
            } else {
                log.debug("deleteHomesMetaDataOf: Found no '" + identity.getName() + "' directory at directory=" + homesDirs[0].getAbsolutePath());
            }
        }
    }

    /**
     * Delete all 'dropboxes' or 'returnboxes' directories for certain user in the course-file structure.
     * 
     * @param identity
     */
    private void deleteAllDropboxReturnboxFilesOf(final Identity identity) {
        final File courseBaseDir = getCourseBaseContainer();
        // loop over all courses path e.g. olatdata\bcroot\course\78931391428316\dropboxes\78933379704296\deltest
        // ^^^^^^^^^ dirTypeName
        final File[] courseDirs = courseBaseDir.listFiles();
        // 1. loop over all course-id e.g. 78931391428316
        for (int courseIndex = 0; courseIndex < courseDirs.length; courseIndex++) {
            if (log.isDebugEnabled()) {
                log.debug("process dir=" + courseDirs[courseIndex].getAbsolutePath());
            }
            final String currentCourseId = courseDirs[courseIndex].getName();
            if (log.isDebugEnabled()) {
                log.debug("currentCourseId=" + currentCourseId);
            }
            if (courseDirs[courseIndex].isDirectory()) {
                final File[] dropboxReturnboxDirs = courseDirs[courseIndex].listFiles(dropboxReturnboxFilter);
                // 2. loop over all dropbox and returnbox in course-folder
                for (int dropboxIndex = 0; dropboxIndex < dropboxReturnboxDirs.length; dropboxIndex++) {
                    final File[] nodeDirs = dropboxReturnboxDirs[dropboxIndex].listFiles();
                    // 3. loop over all node-id e.g. 78933379704296
                    for (int nodeIndex = 0; nodeIndex < nodeDirs.length; nodeIndex++) {
                        if (log.isDebugEnabled()) {
                            log.debug("process dir=" + nodeDirs[nodeIndex].getAbsolutePath());
                        }
                        final String currentNodeId = nodeDirs[nodeIndex].getName();
                        if (log.isDebugEnabled()) {
                            log.debug("currentNodeId=" + currentNodeId);
                        }
                        final ICourse currentCourse = CourseFactory.loadCourse(Long.parseLong(currentCourseId));
                        if (isTaskNode(currentCourse, currentNodeId)) {
                            if (log.isDebugEnabled()) {
                                log.debug("found TACourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
                            }
                            deleteUserDirectory(identity, nodeDirs[nodeIndex]);
                        } else if (isProjectBrokerNode(currentCourse, currentNodeId)) {
                            if (log.isDebugEnabled()) {
                                log.debug("found ProjectBrokerCourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
                            }
                            // addional loop over project-id
                            final File[] projectDirs = nodeDirs[nodeIndex].listFiles();
                            for (int projectIndex = 0; projectIndex < projectDirs.length; projectIndex++) {
                                deleteUserDirectory(identity, projectDirs[projectIndex]);
                            }
                        } else {
                            log.warn("found dropbox or returnbox and node-type is NO Task- or ProjectBroker-Type courseId=" + currentCourseId + " nodeId="
                                    + currentNodeId, null);
                        }
                    }
                }
            }
        }
    }

    private boolean isProjectBrokerNode(final ICourse currentCourse, final String currentNodeId) {
        return currentCourse.getRunStructure().getNode(currentNodeId) instanceof ProjectBrokerCourseNode;
    }

    private boolean isTaskNode(final ICourse currentCourse, final String currentNodeId) {
        return currentCourse.getRunStructure().getNode(currentNodeId) instanceof TACourseNode;
    }

    private void deleteUserDirectory(final Identity identity, final File directory) {
        final File[] userDirs = directory.listFiles(new UserFileFilter(identity.getName()));
        // 4. loop over all user-dir e.g. deltest (only once)
        if (userDirs.length > 0) {
            if (log.isDebugEnabled()) {
                log.debug("process dir=" + userDirs[0].getAbsolutePath());
            }
            // ok found a directory of a user => delete it
            FileUtils.deleteDirsAndFiles(userDirs[0], true, true);
            log.info("Audit:User-Deletion: identity=" + identity.getName() + " : User file data deleted under dir=" + userDirs[0].getAbsolutePath());
            if (userDirs.length > 1) {
                log.error("Found more than one sub-dir for user=" + identity.getName() + " path=" + userDirs[0].getAbsolutePath(), null);
            }
        }
    }

    private static FilenameFilter dropboxReturnboxFilter = new FilenameFilter() {
        @Override
        public boolean accept(@SuppressWarnings("unused") final File dir, final String name) {
            // don't add overlayLocales as selectable availableLanguages
            // (LocaleStrings_de__VENDOR.properties)
            if (name.equals(Returnbox_EBL.RETURNBOX_DIR_NAME) || name.equals(Dropbox_EBL.DROPBOX_DIR_NAME)) {
                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * @return e.g. olatdata\bcroot\course\
     */
    private File getCourseBaseContainer() {
        final OlatRootFolderImpl courseRootContainer = new OlatRootFolderImpl(File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME + File.separator, null);
        return courseRootContainer.getBasefile();
    }

}

class UserFileFilter implements FilenameFilter {

    private final String username;

    UserFileFilter(final String username) {
        this.username = username;
    }

    @Override
    public boolean accept(@SuppressWarnings("unused") final File dir, final String name) {
        // don't add overlayLocales as selectable availableLanguages
        // (LocaleStrings_de__VENDOR.properties)
        if (name.equals(username)) {
            return true;
        } else {
            return false;
        }
    }
}
