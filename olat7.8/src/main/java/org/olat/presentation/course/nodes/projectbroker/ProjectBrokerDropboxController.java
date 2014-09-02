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

import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManager;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ta.AbstractDropboxBaseController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author Christian Guretzki
 */

public class ProjectBrokerDropboxController extends AbstractDropboxBaseController {

    private static final Logger log = LoggerHelper.getLogger();
    private final Project project;
    private final ProjectBrokerModuleConfiguration moduleConfig;
    private ProjectBrokerManager projectBrokerManager;

    public ProjectBrokerDropboxController(final UserRequest ureq, final WindowControl wControl, final ModuleConfiguration config, final CourseNode node,
            final UserCourseEnvironment userCourseEnv, final boolean previewMode, final Project project, final ProjectBrokerModuleConfiguration moduleConfig) {
        super(ureq, wControl, config, node, userCourseEnv);
        this.project = project;
        this.moduleConfig = moduleConfig;
        projectBrokerManager = CoreSpringFactory.getBean(ProjectBrokerManager.class);
        init(ureq, wControl, previewMode, false);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (projectBrokerManager.isDropboxAccessible(project, moduleConfig)) {
            super.event(ureq, source, event);
        } else {
            log.debug("Dropbos is no longer accessible");
            this.showInfo("dropbox.is.not.accessible");
        }
    }

    /**
     * Return dropbox base-path. e.g. course/<COURSE_ID>/dropbox/<NODE_id>/<USER_NAME>
     */
    @Override
    protected String getDropboxFolderForIdentity(final Identity identity) {
        return projectBrokerManager.getDropboxPathForProjectAndIdentity(this.project, identity, userCourseEnv.getCourseEnvironment(), node);
    }

    protected boolean isEmailToTutorWanted() {
        Boolean sendEmailToOwnerAndTutor = (Boolean) config.get(ProjectBrokerCourseNode.CONF_DROPBOX_CONFIRMATION_REQUESTED);
        if (sendEmailToOwnerAndTutor == null) {
            sendEmailToOwnerAndTutor = Boolean.FALSE;
        }
        return sendEmailToOwnerAndTutor;
    }

    @Override
    protected boolean sendStudentConfirmation(Identity identity, String fileName) {
        return sendStudentConfirmationAtDropTopic(identity, fileName);
    }

    private boolean sendStudentConfirmationAtDropTopic(Identity identity, String fileName) {
        return getConfirmationLearnService().sendTopicDropStudentConfirmation(identity, userCourseEnv.getCourseEnvironment().getCourseTitle(),
                userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), new Long(node.getIdent()), fileName, project.getKey());
    }

    @Override
    protected boolean sendTutorConfirmation(List<Identity> tutors, Identity identity, String fileName) {
        return sendTutorConfirmationAtDropTopic(tutors, identity, fileName, project.getKey());
    }

    private boolean sendTutorConfirmationAtDropTopic(List<Identity> recipientIdentities, Identity originatorIdentity, String fileName, Long projectId) {
        return getConfirmationLearnService().sendTopicDropTutorConfirmation(recipientIdentities, originatorIdentity,
                userCourseEnv.getCourseEnvironment().getCourseTitle(), userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), new Long(node.getIdent()), fileName,
                projectId);
    }

    @Override
    protected List<Identity> getTutors() {
        return getTopicTutorRecipients(project);
    }

    private List<Identity> getTopicTutorRecipients(Project project) {
        return projectBrokerManager.getTopicTutors(project, userCourseEnv);
    }

}
