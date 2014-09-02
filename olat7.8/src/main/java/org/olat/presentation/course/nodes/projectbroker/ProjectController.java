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
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.core.notification.impl.UriBuilder;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.repository.RepositoryService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.tabbedpane.TabbedPane;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */

public class ProjectController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer contentVC;
    private final TabbedPane myTabbedPane;
    private final ProjectDetailsPanelController detailsController;
    private final ProjectFolderController projectFolderController;
    private ProjectGroupController projectGroupController;
    private Link backLink;

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     * @param previewMode
     */
    public ProjectController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final Project project, final boolean newCreatedProject, final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration, boolean hasBackLink) {
        super(ureq, wControl);

        contentVC = createVelocityContainer("project");
        contentVC.contextPut("menuTitle", ne.getCourseNode().getShortTitle());

        if (hasBackLink) {
            backLink = LinkFactory.createLinkBack(contentVC, this);
        }
        myTabbedPane = new TabbedPane("projectTabbedPane", ureq.getLocale());
        detailsController = new ProjectDetailsPanelController(ureq, wControl, project, newCreatedProject, userCourseEnv.getCourseEnvironment(), ne.getCourseNode(),
                projectBrokerModuleConfiguration);
        detailsController.addControllerListener(this);
        myTabbedPane.addTab(translate("tab.project.details"), detailsController.getInitialComponent());
        projectFolderController = new ProjectFolderController(ureq, wControl, userCourseEnv, ne, false, project);
        myTabbedPane.addTab(translate("tab.project.folder"), projectFolderController.getInitialComponent());
        listenTo(projectFolderController);
        if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManagerOrAdministrator(ureq, userCourseEnv.getCourseEnvironment(), project)) {
            RepositoryEntry repositoryEntry = getRepositoryService().lookupRepositoryEntry(userCourseEnv.getCourseEnvironment().getRepositoryEntryId());
            projectGroupController = new ProjectGroupController(ureq, wControl, project, projectBrokerModuleConfiguration, repositoryEntry, ne.getCourseNode());
            myTabbedPane.addTab(translate("tab.project.members"), projectGroupController.getInitialComponent());
        }
        // inlineEditDetailsFormController = new InlineEditDetailsFormController(ureq, wControl, project, newCreatedProject, userCourseEnv.getCourseEnvironment(),
        // ne.getCourseNode(), projectBrokerModuleConfiguration);
        // myTabbedPane.addTab(translate("tab.project.details.inline"), inlineEditDetailsFormController.getInitialComponent());

        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.getCurrentContextEntry();

        if (ce != null && getUriBuilder().getTopicAssignmentTabContext().equals(ce.getOLATResourceable().getResourceableTypeName())) {
            myTabbedPane.setSelectedPane(ce.getOLATResourceable().getResourceableId().intValue());
        }
        contentVC.put("projectTabbedPane", myTabbedPane);
        putInitialPanel(contentVC);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == backLink) {
            fireEvent(ureq, Event.BACK_EVENT);
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        log.debug("event" + event);
        if ((source == detailsController) && (event == Event.CHANGED_EVENT)) {
            if (backLink == null) {
                backLink = LinkFactory.createLinkBack(contentVC, this);
            }
        }
        // pass event
        fireEvent(urequest, event);
    }

    /**
	 */
    @Override
    protected void doDispose() {

    }

    private UriBuilder getUriBuilder() {
        return CoreSpringFactory.getBean(UriBuilder.class);
    }

    private RepositoryService getRepositoryService() {
        return CoreSpringFactory.getBean(RepositoryService.class);
    }

}
