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

import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectBroker;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableDeletedEvent;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */
public class ProjectDetailsPanelController extends BasicController {

    private final Panel detailsPanel;
    private ProjectEditDetailsFormController editController;
    private ProjectDetailsDisplayController runController;

    private final Project project;
    private final CourseEnvironment courseEnv;
    private final CourseNode courseNode;
    private final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

    private boolean newCreatedProject;
    private final VelocityContainer editVC;
    private LockResult lock;

    /**
     * @param ureq
     * @param wControl
     * @param hpc
     */
    public ProjectDetailsPanelController(final UserRequest ureq, final WindowControl wControl, final Project project, final boolean newCreatedProject,
            final CourseEnvironment courseEnv, final CourseNode courseNode, final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
        super(ureq, wControl);
        this.project = project;
        this.courseEnv = courseEnv;
        this.courseNode = courseNode;
        this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
        this.newCreatedProject = newCreatedProject;

        detailsPanel = new Panel("projectdetails_panel");
        runController = new ProjectDetailsDisplayController(ureq, wControl, project, courseEnv, courseNode, projectBrokerModuleConfiguration);
        runController.addControllerListener(this);
        detailsPanel.setContent(runController.getInitialComponent());

        editVC = createVelocityContainer("editProject");
        if (newCreatedProject && ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManagerOrAdministrator(ureq, courseEnv, project)) {
            openEditController(ureq);
        }

        putInitialPanel(detailsPanel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // nothing to catch
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if ((source == runController) && event.getCommand().equals("switchToEditMode")) {
            if (newCreatedProject) {
                newCreatedProject = false;
            }
            if (editController != null) {
                editController.doDispose();
            }
            openEditController(ureq);
        } else if ((source == editController) && event == Event.DONE_EVENT) {
            // switch back from edit mode to display-mode
            getLockingService().releaseLock(lock);
            detailsPanel.popContent();
            if (runController != null) {
                runController.dispose();
            }
            runController = new ProjectDetailsDisplayController(ureq, this.getWindowControl(), project, courseEnv, courseNode, projectBrokerModuleConfiguration);
            runController.addControllerListener(this);
            detailsPanel.setContent(runController.getInitialComponent());
            fireEvent(ureq, Event.CHANGED_EVENT);
        } else if ((source == runController) && (event == Event.BACK_EVENT)) {
            // go back to project-list
            fireEvent(ureq, Event.BACK_EVENT);
        } else if ((source == editController) && (event == Event.CANCELLED_EVENT)) {
            if (newCreatedProject) {
                // from cancelled and go back to project-list
                fireEvent(ureq, new CancelNewProjectEvent(project));
            }
            getLockingService().releaseLock(lock);
        }
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    private void openEditController(final UserRequest ureq) {
        if (ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(project.getKey())) {
            final OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
            this.lock = getLockingService().acquireLock(projectOres, ureq.getIdentity(), null);
            if (lock.isSuccess()) {
                editController = new ProjectEditDetailsFormController(ureq, this.getWindowControl(), project, courseEnv, courseNode, projectBrokerModuleConfiguration,
                        newCreatedProject);
                editController.addControllerListener(this);
                editVC.put("editController", editController.getInitialComponent());
                detailsPanel.pushContent(editVC);
            } else {
                this.showInfo("info.project.already.edit", project.getTitle());
            }
        } else {
            this.showInfo("info.project.nolonger.exist", project.getTitle());
            // fire event to update project list
            final ProjectBroker projectBroker = project.getProjectBroker();
            final OLATResourceableDeletedEvent delEv = new OLATResourceableDeletedEvent(projectBroker);
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, projectBroker);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controller sposed by basic controller
        if (lock != null) {
            getLockingService().releaseLock(lock);
        }
    }

}
