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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.course.nodes.projectbroker.CustomField;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectEvent;
import org.olat.lms.commons.mediaresource.MediaResource;
import org.olat.lms.commons.mediaresource.NotFoundMediaResource;
import org.olat.lms.commons.mediaresource.VFSMediaResource;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.run.environment.CourseEnvironment;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.download.DisplayOrDownloadComponent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.creator.ControllerCreator;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.dispatcher.mapper.Mapper;
import org.olat.presentation.framework.layout.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */
public class ProjectDetailsDisplayController extends BasicController {

    private static final Logger log = LoggerHelper.getLogger();
    private final String CMD_OPEN_PROJECT_LEADER_DETAIL = "cmd_open_projectleader_detail";
    private final VelocityContainer myContent;
    private Link editProjectButton;
    private Link deleteProjectButton;
    private DialogBoxController deleteConfirmController;
    private final List projectLeaderLinkNameList;
    private final Link attachedFileLink;

    private final Project project;
    private final CourseEnvironment courseEnv;
    private final CourseNode courseNode;
    private DialogBoxController deleteGroupConfirmController;
    private Link changeProjectStateToNotAssignButton;
    private Link changeProjectStateToAssignButton;
    private LockResult lock;
    private final TopicChangeConfirmationSender topicChangeConfirmationSender;

    /**
     * @param ureq
     * @param wControl
     * @param hpc
     */
    public ProjectDetailsDisplayController(final UserRequest ureq, final WindowControl wControl, final Project project, final CourseEnvironment courseEnv,
            final CourseNode courseNode, final ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration) {
        super(ureq, wControl);
        this.project = project;
        this.courseEnv = courseEnv;
        this.courseNode = courseNode;
        this.topicChangeConfirmationSender = new TopicChangeConfirmationSenderImpl(ureq.getIdentity(), project, courseEnv, courseNode);
        // use property handler translator for translating of user fields
        setTranslator(getUserService().getUserPropertiesConfig().getTranslator(getTranslator()));
        myContent = createVelocityContainer("projectdetailsdisplay");
        if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
            myContent.contextPut("keyMaxLabel", "detailsform.places.candidates.label");
        } else {
            myContent.contextPut("keyMaxLabel", "detailsform.places.label");
        }

        if (ProjectBrokerManagerFactory.getProjectGroupManager().isProjectManagerOrAdministrator(ureq, courseEnv, project)) {
            myContent.contextPut("isProjectManager", true);
            editProjectButton = LinkFactory.createButtonSmall("edit.project.button", myContent, this);
            deleteProjectButton = LinkFactory.createButtonSmall("delete.project.button", myContent, this);
            if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
                // ProjectBroker run in accept-manually mode => add button to reset/set project-state
                if (project.getState().equals(Project.STATE_ASSIGNED)) {
                    changeProjectStateToNotAssignButton = LinkFactory.createButtonSmall("change.project.state.not_assign.button", myContent, this);
                } else {
                    changeProjectStateToAssignButton = LinkFactory.createButtonSmall("change.project.state.assign.button", myContent, this);
                }
            }
        } else {
            myContent.contextPut("isProjectManager", false);
        }

        myContent.contextPut("title", project.getTitle());
        // account-Managers
        int i = 0;
        projectLeaderLinkNameList = new ArrayList();
        for (final Iterator iterator = project.getProjectLeaders().iterator(); iterator.hasNext();) {
            final Identity identity = (Identity) iterator.next();
            final String linkName = "projectLeaderLink_" + i;
            final Link projectLeaderLink = LinkFactory.createCustomLink(linkName, CMD_OPEN_PROJECT_LEADER_DETAIL, getUserService()
                    .getFirstAndLastname(identity.getUser()), Link.NONTRANSLATED, myContent, this);
            projectLeaderLink.setUserObject(identity);
            projectLeaderLink.setTarget("_blank");
            projectLeaderLinkNameList.add(linkName);
            i++;
        }
        myContent.contextPut("projectLeaderLinkNameList", projectLeaderLinkNameList);

        myContent.contextPut("description", project.getDescription());
        // Custom-fields
        final List customFieldList = new ArrayList();
        int customFieldIndex = 0;
        for (final Iterator<CustomField> iterator = projectBrokerModuleConfiguration.getCustomFields().iterator(); iterator.hasNext();) {
            final CustomField customField = iterator.next();
            log.debug("customField=" + customField);
            final String name = customField.getName();
            final String value = project.getCustomFieldValue(customFieldIndex++);
            log.debug("customField  name=" + name + "  value=" + value);
            customFieldList.add(new CustomField(name, value));
        }
        myContent.contextPut("customFieldList", customFieldList);

        // events
        final List eventList = new ArrayList();
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (projectBrokerModuleConfiguration.isProjectEventEnabled(eventType)) {
                final ProjectEvent projectEvent = project.getProjectEvent(eventType);
                eventList.add(projectEvent);
                log.debug("eventList add event=" + projectEvent);
            }
        }
        myContent.contextPut("eventList", eventList);

        final String stateValue = getTranslator().translate(
                ProjectBrokerManagerFactory.getProjectBrokerManager().getStateFor(project, ureq.getIdentity(), projectBrokerModuleConfiguration));
        myContent.contextPut("state", stateValue);
        if (project.getMaxMembers() == Project.MAX_MEMBERS_UNLIMITED) {
            myContent.contextPut("projectPlaces", this.getTranslator().translate("detailsform.unlimited.project.members"));
        } else {
            final String placesValue = ProjectBrokerManagerFactory.getProjectBrokerManager().getSelectedPlaces(project) + " "
                    + this.getTranslator().translate("detailsform.places.of") + " " + project.getMaxMembers();
            myContent.contextPut("projectPlaces", placesValue);
        }

        attachedFileLink = LinkFactory.createCustomLink("attachedFileLink", "cmd.donwload.attachment", project.getAttachmentFileName(), Link.NONTRANSLATED, myContent,
                this);
        putInitialPanel(myContent);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == deleteConfirmController) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                // ok group should be deleted, ask to delete group too
                String text = translate("delete.group.confirm", StringHelper.escapeHtml(project.getTitle()));
                deleteGroupConfirmController = this.activateYesNoDialog(ureq, null, text, deleteConfirmController);
            }
        } else if (source == deleteGroupConfirmController) {
            boolean deleteGroup;
            if (DialogBoxUIFactory.isOkEvent(event)) {
                deleteGroup = true;
            } else {
                deleteGroup = false;
            }
            this.topicChangeConfirmationSender.sendTopicDeleteConfirmation();
            ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProject(project, deleteGroup, courseEnv, courseNode);
            ProjectBrokerManagerFactory.getProjectGroupManager().sendGroupChangeEvent(project, courseEnv.getCourseResourceableId(), ureq.getIdentity());
            showInfo("project.deleted.msg", project.getTitle());
            fireEvent(ureq, Event.BACK_EVENT);
            getLockingService().releaseLock(lock);
        }

    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(project.getKey())) {
            if (source == editProjectButton) {
                fireEvent(ureq, new Event("switchToEditMode"));
            } else if (source == deleteProjectButton) {
                final OLATResourceable projectOres = OresHelper.createOLATResourceableInstance(Project.class, project.getKey());
                this.lock = getLockingService().acquireLock(projectOres, ureq.getIdentity(), null);
                if (lock.isSuccess()) {
                    String text = translate("delete.confirm", StringHelper.escapeHtml(project.getTitle()));
                    deleteConfirmController = activateOkCancelDialog(ureq, null, text, deleteConfirmController);
                } else {
                    this.showInfo("info.project.already.edit", project.getTitle());
                }
                return;
            } else if (event.getCommand().equals(CMD_OPEN_PROJECT_LEADER_DETAIL)) {
                if (source instanceof Link) {
                    final Link projectLeaderLink = (Link) source;
                    final Identity identity = (Identity) projectLeaderLink.getUserObject();
                    final ControllerCreator ctrlCreator = new ControllerCreator() {
                        @Override
                        public Controller createController(final UserRequest lureq, final WindowControl lwControl) {
                            return new UserInfoMainController(lureq, lwControl, identity);
                        }
                    };
                    // wrap the content controller into a full header layout
                    final ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
                    // open in new browser window
                    this.openInNewBrowserWindow(ureq, layoutCtrlr);
                }
            } else if (source == attachedFileLink) {
                doFileDelivery(project, courseEnv, courseNode);
            } else if (source == changeProjectStateToNotAssignButton) {
                ProjectBrokerManagerFactory.getProjectBrokerManager().setProjectState(project, Project.STATE_NOT_ASSIGNED);
                myContent.remove(changeProjectStateToNotAssignButton);
                changeProjectStateToAssignButton = LinkFactory.createButtonSmall("change.project.state.assign.button", myContent, this);
            } else if (source == changeProjectStateToAssignButton) {
                ProjectBrokerManagerFactory.getProjectBrokerManager().setProjectState(project, Project.STATE_ASSIGNED);
                myContent.remove(changeProjectStateToAssignButton);
                changeProjectStateToNotAssignButton = LinkFactory.createButtonSmall("change.project.state.not_assign.button", myContent, this);
            }
        } else {
            this.showInfo("info.project.nolonger.exist", project.getTitle());
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

    private void doFileDelivery(final Project project, final CourseEnvironment courseEnv, final CourseNode cNode) {
        // Create a mapper to deliver the auto-download of the file. We have to
        // create a dedicated mapper here
        // and can not reuse the standard briefcase way of file delivering, some
        // very old fancy code
        // Mapper is cleaned up automatically by basic controller
        final String baseUrl = registerMapper(new Mapper() {
            @Override
            public MediaResource handle(final String relPath, final HttpServletRequest request) {
                final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(ProjectBrokerManagerFactory.getProjectBrokerManager().getAttamchmentRelativeRootPath(
                        project, courseEnv, cNode), null);
                final VFSLeaf vfsfile = (VFSLeaf) rootFolder.resolve(relPath);
                if (vfsfile == null) {
                    return new NotFoundMediaResource(relPath);
                } else {
                    return new VFSMediaResource(vfsfile);
                }
            }
        });
        // Trigger auto-download
        log.debug("baseUrl=" + baseUrl);
        final DisplayOrDownloadComponent dordc = new DisplayOrDownloadComponent("downloadcomp", baseUrl + "/" + project.getAttachmentFileName());
        myContent.put("autoDownloadComp", dordc);
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
