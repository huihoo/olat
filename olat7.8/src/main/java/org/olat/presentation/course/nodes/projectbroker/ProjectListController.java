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

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.course.nodes.projectbroker.CustomField;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.course.nodes.projectbroker.ProjectBroker;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerManagerFactory;
import org.olat.lms.course.nodes.projectbroker.ProjectBrokerModuleConfiguration;
import org.olat.lms.course.properties.CoursePropertyManager;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.learn.notification.service.ConfirmationLearnService;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.ColumnDescriptor;
import org.olat.presentation.framework.core.components.table.CustomRenderColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.render.Renderer;
import org.olat.presentation.framework.core.render.StringOutput;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.system.commons.OutputEscapeType;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * @author guretzki
 */

public class ProjectListController extends BasicController implements GenericEventListener {

    private static final String OPEN_IDENTITY_CMD = "openID";
    private static final Logger log = LoggerHelper.getLogger();

    // List commands
    private static final String TABLE_ACTION_SHOW_DETAIL = "cmd.show.detail";
    private static final String TABLE_ACTION_ACCOUNT_MANAGER = "cmd.account.manager";
    private static final String TABLE_ACTION_SELECT = "cmd.select";
    private static final String TABLE_ACTION_CANCEL_SELECT = "cmd.cancel.select";

    private final VelocityContainer contentVC;
    private final Panel mainPanel;
    private ProjectListTableModel projectListTableModel;
    private TableController tableController;
    private Controller projectController;

    private Link createNewProjectButton;

    private final Long courseId;
    private final UserCourseEnvironment userCourseEnv;
    private final NodeEvaluation nodeEvaluation;

    private final ProjectBrokerModuleConfiguration moduleConfig;
    private Long projectBrokerId;
    private int numberOfCustomFieldInTable = 0;
    private int numberOfEventInTable = 0;
    private int nbrSelectedProjects;
    private boolean isParticipantInAnyProject;
    private CloseableCalloutWindowController calloutCtrl;

    /**
     * @param ureq
     * @param wControl
     * @param userCourseEnv
     * @param ne
     * @param previewMode
     */
    protected ProjectListController(final UserRequest ureq, final WindowControl wControl, final UserCourseEnvironment userCourseEnv, final NodeEvaluation ne,
            final boolean previewMode) {
        super(ureq, wControl);
        this.userCourseEnv = userCourseEnv;
        this.nodeEvaluation = ne;
        courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
        moduleConfig = new ProjectBrokerModuleConfiguration(ne.getCourseNode().getModuleConfiguration());

        contentVC = createVelocityContainer("project_list");
        // set header info with project-broker run mode [accept.automatically.limited , accept.manually.limited etc.]
        String infoProjectBrokerRunMode = "";
        if (moduleConfig.isAcceptSelectionManually() && moduleConfig.isAutoSignOut()) {
            infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.auto.sign.out", Integer.toString(moduleConfig.getNbrParticipantsPerTopic()));
        } else if (moduleConfig.isAcceptSelectionManually()) {
            if (moduleConfig.getNbrParticipantsPerTopic() == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
                infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.unlimited");
            } else {
                infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.manually.limited", Integer.toString(moduleConfig.getNbrParticipantsPerTopic()));
            }
        } else {
            if (moduleConfig.getNbrParticipantsPerTopic() == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
                infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.automatically.unlimited");
            } else {
                infoProjectBrokerRunMode = translate("info.projectbroker.runmode.accept.automatically.limited",
                        Integer.toString(moduleConfig.getNbrParticipantsPerTopic()));
            }
        }
        contentVC.contextPut("infoProjectBrokerRunMode", infoProjectBrokerRunMode);
        mainPanel = new Panel("projectlist_panel");
        final CoursePropertyManager cpm = userCourseEnv.getCourseEnvironment().getCoursePropertyManager();
        OLATResourceable courseOres = userCourseEnv.getCourseEnvironment().getCourseOLATResourceable();
        if ((ProjectBrokerManagerFactory.getProjectGroupManager().isAccountManager(ureq.getIdentity(), cpm, ne.getCourseNode()) && !previewMode)
                || userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity(), courseOres)
                || ureq.getUserSession().getRoles().isOLATAdmin()) {
            contentVC.contextPut("isAccountManager", true);
            createNewProjectButton = LinkFactory.createButtonSmall("create.new.project.button", contentVC, this);
        } else {
            contentVC.contextPut("isAccountManager", false);
        }
        // push title and learning objectives, only visible on intro page
        contentVC.contextPut("menuTitle", ne.getCourseNode().getShortTitle());
        contentVC.contextPut("displayTitle", ne.getCourseNode().getLongTitle());

        projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, ne.getCourseNode());
        if (projectBrokerId == null) {
            // no project-broker exist => create a new one, happens only once
            final ProjectBroker projectBroker = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
            projectBrokerId = projectBroker.getKey();
            ProjectBrokerManagerFactory.getProjectBrokerManager().saveProjectBrokerId(projectBrokerId, cpm, ne.getCourseNode());
            log.info("no project-broker exist => create a new one projectBrokerId=" + projectBrokerId);
        }

        tableController = this.createTableController(ureq, wControl);

        final OLATResourceable projectBroker = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBroker(projectBrokerId);
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), projectBroker);
        updateProjectListModelOf(tableController, ureq.getIdentity());
        contentVC.put("projectList", tableController.getInitialComponent());
        mainPanel.setContent(contentVC);

        // jump to either the forum or the folder if the business-launch-path says so.
        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            if (log.isDebugEnabled()) {
                log.debug("businesscontrol (for further jumps) would be: " + bc.toString());
            }
            final OLATResourceable ores = ce.getOLATResourceable();
            if (log.isDebugEnabled()) {
                log.debug("OLATResourceable= " + ores.toString());
            }
            final Long resId = ores.getResourceableId();
            if (resId.longValue() != 0) {
                if (log.isDebugEnabled()) {
                    log.debug("projectId=" + ores.getResourceableId().toString());
                }

                final Project currentProject = ProjectBrokerManagerFactory.getProjectBrokerManager().getProject(ores.getResourceableId());
                if (currentProject != null) {
                    activateProjectController(currentProject, ureq);
                } else {
                    // message not found, do nothing. Load normal start screen
                    log.debug("Invalid projectId=" + ores.getResourceableId().toString());
                }
            } else {
                // FIXME:chg: Should not happen, occurs when course-node are called
                if (log.isDebugEnabled()) {
                    log.debug("Invalid projectId=" + ores.getResourceableId().toString());
                }
            }
        }

        putInitialPanel(mainPanel);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == createNewProjectButton) {
            String projectTitle = translate("new.project.title");
            int i = 1;
            while (ProjectBrokerManagerFactory.getProjectBrokerManager().existProjectName(projectBrokerId, projectTitle)) {
                projectTitle = translate("new.project.title") + i++;
            }
            final String projectGroupName = translate("project.member.groupname", projectTitle);
            final String projectGroupDescription = translate("project.member.groupdescription", projectTitle);
            final BusinessGroup projectGroup = ProjectBrokerManagerFactory.getProjectGroupManager().createProjectGroupFor(projectBrokerId, ureq.getIdentity(),
                    projectGroupName, projectGroupDescription, courseId);
            final Project project = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectFor(projectTitle, projectTitle, projectBrokerId,
                    projectGroup);
            ProjectBrokerManagerFactory.getProjectGroupManager().sendGroupChangeEvent(project, courseId, ureq.getIdentity());
            log.debug("Created a new project=" + project);
            projectController = new ProjectController(ureq, this.getWindowControl(), userCourseEnv, nodeEvaluation, project, true, moduleConfig, false);
            projectController.addControllerListener(this);
            mainPanel.pushContent(projectController.getInitialComponent());
        } else if (event.getCommand().equals(OPEN_IDENTITY_CMD)) {
            Link link = (Link) source;
            if (calloutCtrl != null) {
                calloutCtrl.deactivate();
                removeAsListenerAndDispose(calloutCtrl);
                calloutCtrl = null;
            }
            openUserInPopup(ureq, (Identity) link.getUserObject());
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest urequest, final Controller source, final Event event) {
        if ((source == tableController) && (event instanceof TableEvent)) {
            handleTableEvent(urequest, (TableEvent) event);
        } else if ((source == projectController) && (event == Event.BACK_EVENT)) {
            mainPanel.popContent();
        } else if ((source == projectController) && (event instanceof ProjectBrokerEditorEvent)) {
            final ProjectBrokerEditorEvent pbEditEvent = (ProjectBrokerEditorEvent) event;
            if (pbEditEvent.isCancelEvent()) {
                log.info("event form cancelled => delete project");
                ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProject(pbEditEvent.getProject(), true, userCourseEnv.getCourseEnvironment(),
                        nodeEvaluation.getCourseNode());
                mainPanel.popContent();
                updateProjectListModelOf(tableController, urequest.getIdentity());
            } else if (pbEditEvent.isCreateEvent() || pbEditEvent.isDeletedEvent()) {
                mainPanel.popContent();
                updateProjectListModelOf(tableController, urequest.getIdentity());
            }
        }
    }

    private void handleTableEvent(final UserRequest urequest, final TableEvent te) {
        final Project currentProject = (Project) tableController.getTableDataModel().getObject(te.getRowId());
        if (ProjectBrokerManagerFactory.getProjectBrokerManager().existsProject(currentProject.getKey())) {
            handleTableEventForProject(urequest, te, currentProject);
        } else {
            this.showInfo("info.project.nolonger.exist", currentProject.getTitle());
            updateProjectListModelOf(tableController, urequest.getIdentity());
        }
    }

    private void handleTableEventForProject(final UserRequest urequest, final TableEvent te, final Project currentProject) {
        if (te.getActionId().equals(TABLE_ACTION_SHOW_DETAIL)) {
            activateProjectController(currentProject, urequest);
        } else if (te.getActionId().equals(TABLE_ACTION_ACCOUNT_MANAGER)) {
            activateUserController(currentProject, urequest, te);
        } else if (te.getActionId().equals(TABLE_ACTION_SELECT)) {
            handleEnrollAction(urequest, currentProject);
        } else if (te.getActionId().equals(TABLE_ACTION_CANCEL_SELECT)) {
            handleCancelEnrollmentAction(urequest, currentProject);
        } else {
            log.warn("Controller-event-handling: Unkown event=" + te);
        }
        fireEvent(urequest, te);
    }

    private void handleCancelEnrollmentAction(final UserRequest urequest, final Project currentProject) {
        log.debug("start cancelProjectEnrollmentOf identity=" + urequest.getIdentity() + " to project=" + currentProject);
        final boolean cancelledEnrollmend = ProjectBrokerManagerFactory.getProjectBrokerManager().cancelProjectEnrollmentOf(urequest.getIdentity(), currentProject,
                moduleConfig);
        if (cancelledEnrollmend) {
            if (currentProject.isMailNotificationEnabled()) {
                List<Identity> emailRecipients = ProjectBrokerManagerFactory.getProjectBrokerManager().getTopicTutors(currentProject, userCourseEnv);
                getConfirmationLearnService().sendTopicCancelEnrollConfirmation(emailRecipients, urequest.getIdentity(),
                        userCourseEnv.getCourseEnvironment().getCourseTitle(), userCourseEnv.getCourseEnvironment().getRepositoryEntryId(),
                        Long.valueOf(nodeEvaluation.getCourseNode().getIdent()), currentProject.getKey(), currentProject.getTitle());
            }
            ProjectBrokerManagerFactory.getProjectGroupManager().sendGroupChangeEvent(currentProject, courseId, urequest.getIdentity());
        } else {
            showInfo("info.msg.could.not.cancel.enrollment");
        }
        updateProjectListModelOf(tableController, urequest.getIdentity());
    }

    private void handleEnrollAction(final UserRequest urequest, final Project currentProject) {
        log.debug("start enrollProjectParticipant identity=" + urequest.getIdentity() + " to project=" + currentProject);
        final boolean enrolled = ProjectBrokerManagerFactory.getProjectBrokerManager().enrollProjectParticipant(urequest.getIdentity(), currentProject, moduleConfig,
                nbrSelectedProjects, isParticipantInAnyProject);
        if (enrolled) {
            if (currentProject.isMailNotificationEnabled()) {
                List<Identity> emailRecipients = ProjectBrokerManagerFactory.getProjectBrokerManager().getTopicTutors(currentProject, userCourseEnv);
                getConfirmationLearnService().sendTopicEnrollConfirmation(emailRecipients, urequest.getIdentity(), userCourseEnv.getCourseEnvironment().getCourseTitle(),
                        userCourseEnv.getCourseEnvironment().getRepositoryEntryId(), Long.valueOf(nodeEvaluation.getCourseNode().getIdent()), currentProject.getKey(),
                        currentProject.getTitle());
            }
            ProjectBrokerManagerFactory.getProjectGroupManager().sendGroupChangeEvent(currentProject, courseId, urequest.getIdentity());
        } else {
            showInfo("info.msg.could.not.enroll");
        }
        updateProjectListModelOf(tableController, urequest.getIdentity());
    }

    private ConfirmationLearnService getConfirmationLearnService() {
        return CoreSpringFactory.getBean(ConfirmationLearnService.class);
    }

    private void updateProjectListModelOf(final TableController tableController, final Identity identity) {
        final List<Project> projects = new ArrayList<Project>(ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectListBy(projectBrokerId));
        nbrSelectedProjects = ProjectBrokerManagerFactory.getProjectBrokerManager().getNbrSelectedProjects(identity, projects);
        isParticipantInAnyProject = ProjectBrokerManagerFactory.getProjectBrokerManager().isParticipantInAnyProject(identity, projects);
        projectListTableModel = new ProjectListTableModel(projects, identity, getTranslator(), moduleConfig, numberOfCustomFieldInTable, numberOfEventInTable,
                nbrSelectedProjects, isParticipantInAnyProject);
        tableController.setTableDataModel(projectListTableModel);

    }

    private void activateUserController(final Project projectAt, UserRequest urequest, TableEvent tableEvent) {
        if (projectAt.getProjectLeaders().isEmpty()) {
            this.showInfo("show.info.no.project.leader");
        } else if (projectAt.getProjectLeaders().size() > 1) {
            VelocityContainer identityVC = createVelocityContainer("identityCallout");
            List<Identity> allIdents = projectAt.getProjectLeaders();
            ArrayList<Link> identLinks = new ArrayList<Link>(allIdents.size());
            for (Identity identity : allIdents) {
                String linkName = getUserService().getFirstAndLastname(identity.getUser());

                Link idLink = LinkFactory.createCustomLink(linkName, OPEN_IDENTITY_CMD, linkName, Link.NONTRANSLATED, identityVC, this);
                idLink.setUserObject(identity);
                identLinks.add(idLink);
            }
            identityVC.contextPut("identLinks", identLinks);

            int row = tableEvent.getRowId();
            String targetDomID = ProjectManagerColumnRenderer.PROJECTMANAGER_COLUMN_ROW_IDENT + row;
            String title = translate("projectlist.callout.title", projectAt.getTitle());

            removeAsListenerAndDispose(calloutCtrl);
            calloutCtrl = new CloseableCalloutWindowController(urequest, getWindowControl(), identityVC, targetDomID, title, true, null);
            calloutCtrl.activate();
            listenTo(calloutCtrl);
        } else if (projectAt.getProjectLeaders().size() == 1) {
            // no callout, if its only one user
            Identity leader = projectAt.getProjectLeaders().get(0);
            openUserInPopup(urequest, leader);
        }
    }

    private void openUserInPopup(UserRequest ureq, final Identity ident) {
        // did not work to open as popup based on ureq! -> open as tab in same window
        HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
        OLATResourceable ores = hpcm.loadConfigFor(ident.getName());

        DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
        UserInfoMainController uimc = new UserInfoMainController(ureq, dts.getWindowControl(), ident);
        DynamicTabHelper.openResourceTab(ores, ureq, uimc, ident.getName(), null);
    }

    private void activateProjectController(final Project project, final UserRequest urequest) {
        removeAsListenerAndDispose(projectController);
        projectController = new ProjectController(urequest, this.getWindowControl(), userCourseEnv, nodeEvaluation, project, false, moduleConfig, true);
        listenTo(projectController);
        mainPanel.pushContent(projectController.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private TableController createTableController(final UserRequest ureq, final WindowControl wControl) {
        numberOfCustomFieldInTable = 0;
        numberOfEventInTable = 0;
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("projectlist.no.projects"));
        tableConfig.setPreferencesOffered(true, "projectbrokerList");
        tableConfig.setShowAllLinkEnabled(false);// Do not allow show all because many entries takes too long to render

        removeAsListenerAndDispose(tableController);
        tableController = new TableController(tableConfig, ureq, wControl, this.getTranslator(), true);
        listenTo(tableController);

        int dataColumn = 0;
        tableController.addColumnDescriptor(new DefaultColumnDescriptor("projectlist.tableheader.title", dataColumn++, TABLE_ACTION_SHOW_DETAIL, ureq.getLocale(),
                OutputEscapeType.HTML));
        CustomRenderColumnDescriptor projectManagerDescriptor = new CustomRenderColumnDescriptor("projectlist.tableheader.account.manager", dataColumn++,
                TABLE_ACTION_ACCOUNT_MANAGER, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new ProjectManagerColumnRenderer()) {
            /**
             * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#compareTo(int, int)
             */
            @Override
            public int compareTo(int rowa, int rowb) {
                return super.compareTo(rowa, rowb);
            }

            /**
             * @see org.olat.core.gui.components.table.CustomRenderColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput, int,
             *      org.olat.core.gui.render.Renderer)
             */
            @Override
            public void renderValue(StringOutput sb, int row, Renderer renderer) {
                Object val = getModelData(row);
                String rowSt = Integer.toString(row); // to get info about row in Renderer!
                getCustomCellRenderer().render(sb, renderer, val, getLocale(), getAlignment(), rowSt);
            }
        };
        tableController.addColumnDescriptor(projectManagerDescriptor);
        // Custom-Fields
        final List<CustomField> customFieldList = moduleConfig.getCustomFields();
        for (final Iterator iterator = customFieldList.iterator(); iterator.hasNext();) {
            final CustomField customField = (CustomField) iterator.next();
            if (customField.isTableViewEnabled()) {
                numberOfCustomFieldInTable++;
                final DefaultColumnDescriptor columnDescriptor = new DefaultColumnDescriptor(customField.getName(), dataColumn++, null, ureq.getLocale());
                columnDescriptor.setTranslateHeaderKey(false);
                tableController.addColumnDescriptor(columnDescriptor);
            }
        }
        // Project Events
        for (final Project.EventType eventType : Project.EventType.values()) {
            if (moduleConfig.isProjectEventEnabled(eventType) && moduleConfig.isProjectEventTableViewEnabled(eventType)) {
                numberOfEventInTable++;
                tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("projectlist.tableheader.event." + eventType.getI18nKey(), dataColumn++, null, ureq
                        .getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new ProjectEventColumnRenderer()));
            }
        }

        tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("projectlist.tableheader.state", dataColumn++, null, ureq.getLocale(),
                ColumnDescriptor.ALIGNMENT_LEFT, new ProjectStateColumnRenderer()));
        tableController.addColumnDescriptor(new DefaultColumnDescriptor("projectlist.tableheader.numbers", dataColumn++, null, ureq.getLocale()));
        tableController.addColumnDescriptor(new BooleanColumnDescriptor("projectlist.tableheader.select", dataColumn++, TABLE_ACTION_SELECT,
                translate("table.action.select"), "-"));
        tableController.addColumnDescriptor(new BooleanColumnDescriptor("projectlist.tableheader.cancel.select", dataColumn++, TABLE_ACTION_CANCEL_SELECT,
                translate("projectlist.tableheader.cancel.select"), "-"));

        return tableController;

    }

    /**
     * Is called when a project is deleted via group-management (ProjectBrokerManager.deleteGroupDataFor(BusinessGroup group) , DeletableGroupData-interface)
     * 
     */
    @Override
    public void event(final Event event) {
        updateProjectListModelOf(tableController, getIdentity());
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
