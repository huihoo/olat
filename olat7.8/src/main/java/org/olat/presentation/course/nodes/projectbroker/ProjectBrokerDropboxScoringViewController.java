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

package org.olat.presentation.course.nodes.projectbroker;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.course.auditing.UserNodeAuditManager;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerReturnboxFullAccessWithoutDeleteCallback;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ta.AbstractDropboxScoringViewBaseController;
import org.olat.presentation.course.nodes.ta.DropboxScoringViewController;
import org.olat.presentation.filebrowser.FolderEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */
public class ProjectBrokerDropboxScoringViewController extends AbstractDropboxScoringViewBaseController {

    private static final Logger log = LoggerHelper.getLogger();

    private final Project project;
    private ProjectBrokerManager projectBrokerManager;

    /**
     * Scoring view of the dropbox.
     * 
     * @param ureq
     * @param wControl
     * @param node
     * @param userCourseEnv
     */
    public ProjectBrokerDropboxScoringViewController(final Project project, final UserRequest ureq, final WindowControl wControl, final CourseNode node,
            final UserCourseEnvironment userCourseEnv) {
        super(ureq, wControl, node, userCourseEnv);
        this.project = project;
        projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        this.setVelocityRoot(PackageUtil.getPackageVelocityRoot(DropboxScoringViewController.class));
        final Translator fallbackTranslator = new PackageTranslator(PackageUtil.getPackageName(this.getClass()), ureq.getLocale());
        final Translator myTranslator = new PackageTranslator(PackageUtil.getPackageName(DropboxScoringViewController.class), ureq.getLocale(), fallbackTranslator);
        this.setTranslator(myTranslator);
        init(ureq);
    }

    /**
     * It is necessary to Override this since the userCourseEnv is the tutor's not the student's, so it is not possible to get the student from there.
     */
    @Override
    protected void logAndSendStudentConfirmationAtReturn(final UserRequest ureq, final FolderEvent folderEvent) {
        final UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
        // log entry for this file
        final Identity coach = ureq.getIdentity();

        // WARNING: next line is wrong, this is why we get the identity name from the folder name.
        // final Identity student = userCourseEnv.getIdentityEnvironment().getIdentity();
        final String folderName = folderEvent.getFolderName();
        final Identity student = getAssessedIdentityFromFolderName(folderName);

        if (student != null) {
            am.appendToUserNodeLog(node, coach, student, "FILE UPLOADED: " + folderEvent.getFilename());
            sendStudentConfirmation(folderEvent, am, coach, student);
        } else {
            log.error("No student found from folderName, so cannot sendStudentConfirmation at returnbox upload for projectbroker! The folderName is: " + folderName);
        }
    }

    @Override
    protected boolean sendStudentConfirmation(Identity student, Identity tutor, String fileName) {
        Long projectId = getProjectId();
        return getConfirmationLearnService().sendTopicReturnStudentConfirmation(student, tutor, userCourseEnv.getCourseEnvironment().getCourseTitle(),
                userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), new Long(node.getIdent()), fileName, projectId);
    }

    @Override
    protected String getDropboxFilePath(final Identity assesseeIdentity) {
        return projectBrokerManager.getDropboxBasePathForProject(this.project, userCourseEnv.getCourseEnvironment(), node);
    }

    @Override
    protected String getReturnboxFilePath(final Identity assesseeIdentity) {
        return projectBrokerManager.getReturnboxBasePathForProject(project, userCourseEnv.getCourseEnvironment(), node);
    }

    @Override
    protected String getDropboxRootFolderName(final String assesseeName) {
        return translate("scoring.dropbox.rootfolder.name");
    }

    @Override
    protected String getReturnboxRootFolderName(final String assesseeName) {
        return translate("scoring.returnbox.rootfolder.name");
    }

    @Override
    protected VFSSecurityCallback getDropboxVfsSecurityCallback() {
        return new ReadOnlyCallback();
    }

    @Override
    protected VFSSecurityCallback getReturnboxVfsSecurityCallback(final String returnboxRelPath) {
        return new ProjectBrokerReturnboxFullAccessWithoutDeleteCallback(returnboxRelPath);
    }

    private Identity getAssessedIdentityFromFolderName(String foldername) {
        String username = foldername;
        if (foldername.indexOf("/") == 0) {
            username = foldername.substring(1);
        }
        Identity assessedIdentity = findIdentity(username);
        return assessedIdentity;
    }

    private Identity findIdentity(String olatUserName) {
        Identity identity = getBaseSecurity().findIdentityByName(olatUserName);
        if (identity != null && identity.getStatus().equals(Identity.STATUS_DELETED)) {
            return null;
        }
        return identity;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    public Long getProjectId() {
        return project.getKey();
    }

}
