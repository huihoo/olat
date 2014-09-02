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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.repository.RepositoryEntry;
import org.olat.lms.activitylogging.ActionType;
import org.olat.lms.core.notification.service.TopicCandidateGroupConfirmationInfo;
import org.olat.lms.core.notification.service.TopicManagerGroupConfirmationInfo;
import org.olat.lms.core.notification.service.TopicUserGroupConfirmationInfo;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupAddResponse;
import org.olat.lms.group.BusinessGroupService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.IdentitiesAddEvent;
import org.olat.presentation.group.securitygroup.IdentitiesMoveEvent;
import org.olat.presentation.group.securitygroup.IdentitiesRemoveEvent;
import org.olat.presentation.group.securitygroup.WaitingGroupController;
import org.olat.presentation.group.securitygroup.confirmation.AbstractGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.AbstractWaitingListGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.TopicCandidateGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.TopicGroupConfirmationSenderInfo;
import org.olat.presentation.group.securitygroup.confirmation.TopicManagerGroupConfirmationSender;
import org.olat.presentation.group.securitygroup.confirmation.TopicUserGroupConfirmationSender;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */
public class ProjectGroupController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final GroupController projectLeaderController;
    private final GroupController projectMemberController;
    private WaitingGroupController projectCandidatesController;

    private final Project project;
    private BusinessGroupService businessGroupService;
    private final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

    /**
     * @param ureq
     * @param wControl
     * @param nodeId
     * @param courseRepositoryEntry
     * @param hpc
     */
    public ProjectGroupController(final UserRequest ureq, final WindowControl wControl, final Project project,
            final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration, RepositoryEntry repositoryEntry, CourseNode courseNode) {
        super(ureq, wControl);
        businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);
        getUserActivityLogger().setStickyActionType(ActionType.admin);
        this.project = project;
        this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;

        final VelocityContainer myContent = createVelocityContainer("projectgroup_management");

        // Project Leader Management
        AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicManagerGroupConfirmationInfo> topicManagerGroupConfirmationSender = getTopicManagerGroupConfirmationSender(
                ureq, project, repositoryEntry, courseNode);
        projectLeaderController = new GroupController(ureq, getWindowControl(), true, true, true, project.getProjectLeaderGroup(), topicManagerGroupConfirmationSender);
        listenTo(projectLeaderController);
        myContent.put("projectLeaderController", projectLeaderController.getInitialComponent());

        // Project Member Management
        AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicUserGroupConfirmationInfo> topicUserGroupConfirmationSender = getTopicUserGroupConfirmationSender(
                ureq, project, repositoryEntry, courseNode);
        projectMemberController = new GroupController(ureq, getWindowControl(), true, false, true, project.getProjectParticipantGroup(), topicUserGroupConfirmationSender);
        listenTo(projectMemberController);
        myContent.put("projectMemberController", projectMemberController.getInitialComponent());

        // Project Candidates Management
        if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
            AbstractWaitingListGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicCandidateGroupConfirmationInfo> confirmationSender = getTopicCandidateGroupConfirmationSender(
                    ureq, project, repositoryEntry, courseNode);
            projectCandidatesController = new WaitingGroupController(ureq, getWindowControl(), true, false, true, project.getCandidateGroup(), confirmationSender);
            listenTo(projectCandidatesController);
            myContent.contextPut("isProjectCandidatesListEmpty", ProjectBrokerManagerFactory.getProjectGroupManager().isCandidateListEmpty(project.getCandidateGroup()));
            myContent.put("projectCandidatesController", projectCandidatesController.getInitialComponent());
        }

        putInitialPanel(myContent);
    }

    private AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicUserGroupConfirmationInfo> getTopicUserGroupConfirmationSender(UserRequest ureq,
            Project project, RepositoryEntry repositoryEntry, CourseNode courseNode) {
        TopicGroupConfirmationSenderInfo confirmationSenderInfo = new TopicGroupConfirmationSenderInfo(ureq.getIdentity(), project, repositoryEntry, courseNode);
        AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicUserGroupConfirmationInfo> confirmationSender = new TopicUserGroupConfirmationSender(
                confirmationSenderInfo);
        return confirmationSender;
    }

    private AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicManagerGroupConfirmationInfo> getTopicManagerGroupConfirmationSender(UserRequest ureq,
            Project project, RepositoryEntry repositoryEntry, CourseNode courseNode) {
        TopicGroupConfirmationSenderInfo confirmationSenderInfo = new TopicGroupConfirmationSenderInfo(ureq.getIdentity(), project, repositoryEntry, courseNode);
        AbstractGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicManagerGroupConfirmationInfo> confirmationSender = new TopicManagerGroupConfirmationSender(
                confirmationSenderInfo);
        return confirmationSender;
    }

    private AbstractWaitingListGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicCandidateGroupConfirmationInfo> getTopicCandidateGroupConfirmationSender(
            final UserRequest ureq, final Project project, RepositoryEntry repositoryEntry, CourseNode courseNode) {
        TopicGroupConfirmationSenderInfo confirmationSenderInfo = new TopicGroupConfirmationSenderInfo(ureq.getIdentity(), project, repositoryEntry, courseNode);
        AbstractWaitingListGroupConfirmationSender<TopicGroupConfirmationSenderInfo, TopicCandidateGroupConfirmationInfo> confirmationSender = new TopicCandidateGroupConfirmationSender(
                confirmationSenderInfo);
        return confirmationSender;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
    }

    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if (ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(project.getKey())) {
            if (source == projectLeaderController) {
                handleProjectLeaderGroupEvent(urequest, event);
            } else if (source == projectMemberController) {
                handleProjectMemberGroupEvent(urequest, event);
            } else if (source == projectCandidatesController) {
                handleCandidateGroupEvent(urequest, event);
            }
        } else {
            this.showInfo("info.project.nolonger.exist", project.getTitle());
        }
    }

    private void handleCandidateGroupEvent(final UserRequest urequest, final Event event) {
        if (event instanceof IdentitiesAddEvent) {
            final IdentitiesAddEvent identitiesAddEvent = (IdentitiesAddEvent) event;
            final List<Identity> addedIdentities = ProjectBrokerManagerFactory.getProjectGroupManager().addCandidates(identitiesAddEvent.getAddIdentities(), project);
            identitiesAddEvent.setIdentitiesAddedEvent(addedIdentities);
            if (!addedIdentities.equals(identitiesAddEvent.getAddIdentities())) {
                final List<Identity> identitiesAlreadyParticipants = new ArrayList<Identity>(identitiesAddEvent.getAddIdentities());
                identitiesAlreadyParticipants.removeAll(addedIdentities);
                final StringBuilder infoMessage = new StringBuilder();
                for (final Identity identity : identitiesAlreadyParticipants) {
                    infoMessage.append(translate("projectgroup.candidates.alreadyMember", identity.getName())).append("<br />");
                }
                getWindowControl().setInfo(infoMessage.toString());
            }
            fireEvent(urequest, Event.CHANGED_EVENT);
        } else if (event instanceof IdentitiesRemoveEvent) {
            ProjectBrokerManagerFactory.getProjectGroupManager().removeCandidates(((IdentitiesRemoveEvent) event).getRemovedIdentities(), project);
            fireEvent(urequest, Event.CHANGED_EVENT);
        } else if (event instanceof IdentitiesMoveEvent) {
            final IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
            final List<Identity> acceptedCandidates = ProjectBrokerManagerFactory.getProjectGroupManager().acceptCandidates(identitiesMoveEvent.getChosenIdentities(),
                    project, urequest.getIdentity(), projectBrokerModuleConfiguration.isAutoSignOut(), projectBrokerModuleConfiguration.isAcceptSelectionManually());
            identitiesMoveEvent.setMovedIdentities(acceptedCandidates);

            // possibly not all the chosen users could have been moved
            List<Identity> notMovedCandidates = new ArrayList<Identity>();
            if (!acceptedCandidates.equals(identitiesMoveEvent.getChosenIdentities())) {
                notMovedCandidates = new ArrayList<Identity>(identitiesMoveEvent.getChosenIdentities());
                notMovedCandidates.removeAll(acceptedCandidates);
            }
            // user message will be displayed in WaitingGroupController is this is not empty
            identitiesMoveEvent.setNotMovedIdentities(notMovedCandidates);
            fireEvent(urequest, Event.CHANGED_EVENT);
            // Participant and waiting-list were changed => reload both
            // Do only reload data in case of IdentitiesMoveEvent (IdentitiesAddEvent and reload data result in duplicate values)
            projectMemberController.reloadData();
            projectCandidatesController.reloadData();
        }
    }

    private void handleProjectMemberGroupEvent(final UserRequest urequest, final Event event) {
        final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
        if (event instanceof IdentitiesAddEvent) {
            final IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
            final BusinessGroupAddResponse response = businessGroupService.addParticipantsAndFireEvent(urequest.getIdentity(), identitiesAddedEvent.getAddIdentities(),
                    project.getProjectGroup(), flags);
            identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
            identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
            identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
            log.info("Add users as project-members");
            fireEvent(urequest, Event.CHANGED_EVENT);
        } else if (event instanceof IdentitiesRemoveEvent) {
            businessGroupService.removeParticipantsAndFireEvent(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(),
                    project.getProjectGroup(), flags);
            log.info("Remove users as account-managers");
            fireEvent(urequest, Event.CHANGED_EVENT);
        }
    }

    private void handleProjectLeaderGroupEvent(final UserRequest urequest, final Event event) {
        final BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
        if (event instanceof IdentitiesAddEvent) {
            final IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
            final BusinessGroupAddResponse response = businessGroupService.addOwnersAndFireEvent(urequest.getIdentity(), identitiesAddedEvent.getAddIdentities(),
                    project.getProjectGroup(), flags);
            identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
            identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
            identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());
            log.info("Add users as project-leader");
            fireEvent(urequest, Event.CHANGED_EVENT);
        } else if (event instanceof IdentitiesRemoveEvent) {
            businessGroupService.removeOwnersAndFireEvent(urequest.getIdentity(), ((IdentitiesRemoveEvent) event).getRemovedIdentities(), project.getProjectGroup(),
                    flags);
            log.info("Remove users as account-managers");
            fireEvent(urequest, Event.CHANGED_EVENT);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // child controller disposed by basic controller
    }

}
