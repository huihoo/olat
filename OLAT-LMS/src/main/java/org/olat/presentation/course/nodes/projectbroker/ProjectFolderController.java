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
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.lms.commons.ModuleConfiguration;
import org.olat.lms.course.nodes.ProjectBrokerCourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.presentation.course.nodes.ms.MSCourseNodeRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * @author guretzki
 */

public class ProjectFolderController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final ModuleConfiguration config;
    private boolean hasDropbox, hasScoring, hasReturnbox;
    private final VelocityContainer content;
    private ProjectBrokerDropboxController dropboxController;
    private Controller dropboxEditController;
    private ProjectBrokerReturnboxController returnboxController;
    private MSCourseNodeRunController scoringController;

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     * @param previewMode
     */
    public ProjectFolderController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final boolean previewMode, final Project project) {
        super(ureq, wControl);
        this.config = ne.getCourseNode().getModuleConfiguration();
        final ProjectBrokerModuleConfiguration moduleConfig = new ProjectBrokerModuleConfiguration(ne.getCourseNode().getModuleConfiguration());

        content = createVelocityContainer("folder");

        if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectParticipant(ureq.getIdentity(), project)
                || ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManagerOrAdministrator(ureq, userCourseEnv.getCourseEnvironment(), project)) {
            content.contextPut("isParticipant", true);
            readConfig(config);
            // modify hasTask/hasDropbox/hasScoring according to accessability
            // TODO:cg 27.01.2010 ProjectBroker does not support assessement-tool in first version
            // if (hasScoring){
            // hasScoring = ne.isCapabilityAccessible("scoring");
            // }
            hasScoring = false;
            // no call 'ne.isCapabilityAccessible(ProjectBrokerCourseNode.ACCESS_DROPBOX);' because no dropbox/returnbox conditions
            if (!hasDropbox && !hasReturnbox) {
                // nothing to show => Show text message no folder
                content.contextPut("noFolder", Boolean.TRUE);
            } else {
                log.debug("isDropboxAccessible(project, moduleConfig)="
                        + ProjectBrokerManagerFactory.getProjectBrokerManager().isDropboxAccessible(project, moduleConfig));
                if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(ureq.getIdentity(), project)) {
                    dropboxEditController = new ProjectBrokerDropboxScoringViewController(project, ureq, wControl, ne.getCourseNode(), userCourseEnv);
                    content.put("dropboxController", dropboxEditController.getInitialComponent());
                    content.contextPut("hasDropbox", Boolean.TRUE);
                } else {
                    if (hasDropbox) {
                        if (ProjectBrokerManagerFactory.getProjectBrokerManager().isDropboxAccessible(project, moduleConfig)) {
                            dropboxController = new ProjectBrokerDropboxController(ureq, wControl, config, ne.getCourseNode(), userCourseEnv, previewMode, project,
                                    moduleConfig);
                            content.put("dropboxController", dropboxController.getInitialComponent());
                            content.contextPut("hasDropbox", Boolean.TRUE);
                        } else {
                            content.contextPut("hasDropbox", Boolean.FALSE);
                            content.contextPut("DropboxIsNotAccessible", Boolean.TRUE);
                        }
                    }
                    if (hasReturnbox) {
                        if (!ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManager(ureq.getIdentity(), project)) {
                            returnboxController = new ProjectBrokerReturnboxController(ureq, wControl, config, ne.getCourseNode(), userCourseEnv, previewMode, project);
                            content.put("returnboxController", returnboxController.getInitialComponent());
                            content.contextPut("hasReturnbox", Boolean.TRUE);
                        }
                    }
                }
                // TODO:cg 27.01.2010 ProjectBroker does not support assessement-tool in first version
                // if (hasScoring && !previewMode) {
                // scoringController = new MSCourseNodeRunController(ureq, getWindowControl(), userCourseEnv, (AssessableCourseNode) ne.getCourseNode(), false);
                // content.put("scoringController", scoringController.getInitialComponent());
                // content.contextPut("hasScoring", Boolean.TRUE);
                // }
            }
            // push title
            content.contextPut("menuTitle", ne.getCourseNode().getShortTitle());
            content.contextPut("displayTitle", ne.getCourseNode().getLongTitle());

            // learning objectives, only visible on intro page: Adding learning objectives
            // TODO: cg 28.01.2010 : no Leaning objective for project-broker
            // String learningObj = ne.getCourseNode().getLearningObjectives();
            // if (learningObj != null) {
            // Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
            // content.put("learningObjectives", learningObjectives);
            // content.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator
            // }
        } else {
            content.contextPut("isParticipant", false);
        }
        putInitialPanel(content);
    }

    private void readConfig(final ModuleConfiguration modConfig) {
        Boolean bValue = (Boolean) modConfig.get(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED);
        hasDropbox = (bValue != null) ? bValue.booleanValue() : false;
        bValue = (Boolean) modConfig.get(ProjectBrokerCourseNode.CONF_SCORING_ENABLED);
        hasScoring = (bValue != null) ? bValue.booleanValue() : false;
        bValue = (Boolean) modConfig.get(ProjectBrokerCourseNode.CONF_RETURNBOX_ENABLED);
        hasReturnbox = (bValue != null) ? bValue.booleanValue() : false;
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
    }

    @Override
    protected void doDispose() {
        disposeController(dropboxController);
        disposeController(dropboxEditController);
        disposeController(scoringController);
        disposeController(returnboxController);
    }

    private void disposeController(Controller controller) {
        if (controller != null) {
            controller.dispose();
            controller = null;
        }
    }

}
