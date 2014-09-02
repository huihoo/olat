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

package org.olat.presentation.group.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.group.BusinessGroup;
import org.olat.data.group.area.BGArea;
import org.olat.data.group.area.BGAreaDao;
import org.olat.data.group.area.BGAreaDaoImpl;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.course.nodes.projectbroker.ProjectController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.StaticColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.BusinessGroupProjectControllerFactory;
import org.olat.presentation.group.BusinessGroupProjectControllerFactoryImpl;
import org.olat.presentation.group.BusinessGroupTableModel;
import org.olat.presentation.group.NewAreaController;
import org.olat.presentation.group.NewBGController;
import org.olat.presentation.group.area.BGAreaEditController;
import org.olat.presentation.group.area.BGAreaFormController;
import org.olat.presentation.group.area.BGAreaTableModel;
import org.olat.presentation.group.context.BGContextEditController;
import org.olat.presentation.group.context.BGContextEvent;
import org.olat.presentation.group.edit.BusinessGroupEditController;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.presentation.group.run.BusinessGroupMainRunController;
import org.olat.presentation.group.run.BusinessGroupSendToChooserForm;
import org.olat.presentation.group.run.BusinessGroupSendToChooserFormUIModel;
import org.olat.presentation.group.run.BusinessGroupSendToChooserFormUIModel.GroupParameter;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.group.securitygroup.UserControllerFactory;
import org.olat.presentation.group.wizard.BGCopyWizardController;
import org.olat.presentation.group.wizard.BGMultipleCopyWizardController;
import org.olat.presentation.group.wizard.MemberListWizardController;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.presentation.user.HomePageDisplayController;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.presentation.user.administration.UserTableDataModel;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR/>
 * This controller provides a complete groupmanagement for a given group context. The provided functionality is configured using the BGConfigurationFlags. If you want to
 * use this class, don't use the constructor but get an instance using the BGControllerFactory
 * <P/>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public class BGManagementController extends MainLayoutBasicController implements GenericEventListener {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String PACKAGE = PackageUtil.getPackageName(BGManagementController.class);

    // Menu commands
    private static final String CMD_OVERVIEW = "cmd.overview";
    private static final String CMD_EDITCONTEXT = "cmd.editcontext";
    private static final String CMD_GROUPLIST = "cmd.grouplist";
    private static final String CMD_AREALIST = "cmd.arealist";
    // Toolbox commands
    private static final String CMD_GROUP_CREATE = "cmd.group.create";
    private static final String CMD_AREA_CREATE = "cmd.area.create";
    private static final String CMD_CLOSE = "cmd.close";
    private static final String CMD_BACK = "cmd.back";
    // List commands
    private static final String CMD_GROUP_RUN = "cmd.group.run";
    private static final String CMD_GROUP_MESSAGE = "cmd.group.message";
    private static final String CMD_GROUP_EDIT = "cmd.group.edit";
    private static final String CMD_GROUP_DELETE = "cmd.group.delete";
    private static final String CMD_GROUP_COPY = "cmd.group.copy";
    private static final String CMD_GROUP_COPY_MULTIPLE = "cmd.group.copy.multiple";
    private static final String CMD_AREA_EDIT = "cmd.area.edit";
    private static final String CMD_AREA_DELETE = "cmd.area.delete";
    // User commands
    private static final String CMD_USER_LIST = "cmd.user.list";
    private static final String CMD_USER_DETAILS = "cmd.user.details";
    private static final String CMD_USER_REMOVE_GROUP_PART = "cmd.user.remove.group.part";
    private static final String CMD_USER_REMOVE_GROUP_OWN = "cmd.user.remove.group.own";
    private static final String CMD_USER_MESSAGE = "cmd.user.message";
    private static final String CMD_OWNERS_MESSAGE = "cmd.owners.message";
    private static final String CMD_PARTICIPANTS_MESSAGE = "cmd.participants.message";
    private static final String CMD_LIST_MEMBERS_WITH_GROUPS = "cmd.list.members.with.groups";
    private static final String CMD_LIST_MEMBERS_WITH_AREAS = "cmd.list.members.with.areas";

    private Translator areaTrans;

    private final Translator userTrans;
    private BGContext bgContext;
    private final String groupType;
    private final BGConfigFlags flags;

    private static final int STATE_OVERVIEW = 1;
    private static final int STATE_CONTEXT_EDIT = 2;
    private static final int STATE_CONTEXT_REMOVED = 3;
    private static final int STATE_GROUP_CREATE_FORM = 100;
    private static final int STATE_GROUP_EDIT = 101;
    private static final int STATE_GROUP_LIST = 102;
    private static final int STATE_AREA_CREATE_FORM = 200;
    private static final int STATE_AREA_EDIT = 201;
    private static final int STATE_AREA_LIST = 202;

    private static final int STATE_USER_LIST = 300;
    private static final int STATE_USER_DETAILS = 301;

    private BusinessGroupEditController groupEditCtr;
    private BGAreaEditController areaEditCtr;
    private VelocityContainer overviewVC, newGroupVC, sendMessageVC, contextEditVC, vc_sendToChooserForm;
    private BusinessGroupSendToChooserForm sendToChooserForm;
    private final Translator businessGroupTranslator;
    private boolean isGMAdminOwner;
    private VelocityContainer newAreaVC, areaListVC, groupListVC, userListVC, userDetailsVC;
    private BusinessGroupTableModel groupListModel;
    private BGAreaTableModel areaListModel;
    private TableController groupListCtr, areaListCtr, ownerListCtr, participantListCtr;
    private UserTableDataModel ownerListModel, participantListModel;
    private HomePageDisplayController homePageDisplayController;
    private DialogBoxController confirmDeleteGroup, confirmDeleteArea;
    private ContactFormController contactCtr;
    private BGCopyWizardController bgCopyWizardCtr;
    private BGMultipleCopyWizardController bgMultipleCopyWizardCtr;
    private BGContextEditController contextEditCtr;
    private TableController resourcesCtr;
    private GroupController contextOwnersCtr;

    // Layout components and controllers
    private final Panel content;
    private final LayoutMain3ColsController columnLayoutCtr;
    private final MenuTree olatMenuTree;
    private ToolController toolC;

    private BusinessGroupService businessGroupService;
    private final BGContextDao contextManager;
    private BGAreaDao areaManager;

    // Workflow variables
    private List areaFilters;
    private BGArea currentAreaFilter;
    private Component backComponent, currentComponent;
    private BusinessGroup currentGroup;
    private BGArea currentArea;
    private Identity currentIdentity;
    private Link backButton;
    private NewBGController groupCreateController;
    private NewAreaController areaCreateController;

    private CloseableModalController closeableModalController;
    private MemberListWizardController memberListWizardController;

    private ProjectController projectController;

    /**
     * Use BGControllerFactrory to create such a controller. DO NOT USE THIS CONSTRUCTOR!
     * 
     * @param ureq
     * @param wControl
     * @param bgContext
     * @param controllerFlags
     */
    public BGManagementController(final UserRequest ureq, final WindowControl wControl, final BGContext bgContext, final BGConfigFlags controllerFlags) {
        super(ureq, wControl);
        this.bgContext = bgContext;
        this.groupType = bgContext.getGroupType();
        this.flags = controllerFlags;

        // Initialize managers
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        contextManager = BGContextDaoImpl.getInstance();
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            areaManager = BGAreaDaoImpl.getInstance();
        }

        businessGroupTranslator = PackageUtil.createPackageTranslator(BusinessGroupMainRunController.class, ureq.getLocale());
        // Initialize translator
        // 1 - package translator with default group fallback translators and type
        // translator
        setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, this.groupType, ureq.getLocale()));
        // 2 - area specific translator
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            // areaTrans = new PackageTranslator(Util.getPackageName(BGAreaForm.class), ureq.getLocale(), trans);
            areaTrans = PackageUtil.createPackageTranslator(BGAreaFormController.class, ureq.getLocale(), getTranslator());
        }
        // user translator
        this.userTrans = PackageUtil.createPackageTranslator(UserInfoMainController.class, ureq.getLocale());

        // initialize all velocity containers
        initVC();

        // Layout is controlled with generic controller: menu - content - tools
        // Navigation menu
        olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel(ureq);
        olatMenuTree.setTreeModel(tm);
        olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
        olatMenuTree.addListener(this);
        // Content
        content = new Panel("content");
        // Tools
        // 1 create empty Tools and init menuAndToolController
        // 2 set correct tools using setTools method (override step 1)
        toolC = ToolFactory.createToolController(getWindowControl());
        listenTo(toolC);
        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, toolC.getInitialComponent(), content, "groupmngt" + bgContext.getKey());
        listenTo(columnLayoutCtr);

        doOverview(ureq);

        putInitialPanel(columnLayoutCtr.getInitialComponent());

        // disposed message controller
        // must be created beforehand
        final Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
        final Controller courseCloser = new DisposedBGAManagementController(ureq, wControl, this);
        listenTo(courseCloser);
        final Controller disposedBGAManagementController = new LayoutMain3ColsController(ureq, wControl, empty, empty, courseCloser.getInitialComponent(), "disposed "
                + "groupmngt" + bgContext.getKey());
        listenTo(disposedBGAManagementController);
        setDisposedMsgController(disposedBGAManagementController);

        // register for changes in this group context
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), this.bgContext);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final String cmd = event.getCommand();
        if (source == olatMenuTree) {
            if (cmd.equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                handleMenuCommands(ureq);
            }
        } else if (source == backButton) {
            doUsersList(ureq, true); // for now init whole table models again
        } else if (source instanceof Link) {
            final Link link = (Link) source;
            final BusinessGroup group = (BusinessGroup) link.getUserObject();
            final String groupKey = group.getKey().toString();
            if (link.getCommand().indexOf(CMD_USER_REMOVE_GROUP_PART) == 0) {
                doRemoveUserFromParticipatingGroup(ureq.getIdentity(), this.currentIdentity, groupKey);
                doUserDetails(ureq);
            } else if (link.getCommand().indexOf(CMD_USER_REMOVE_GROUP_OWN) == 0) {
                doRemoveUserFromOwnedGroup(ureq, groupKey);
                doUserDetails(ureq);
            }
        }
    }

    /**
	 */
    public void event(final Event event) {
        if (event instanceof BGContextEvent) {
            final BGContextEvent contextEvent = (BGContextEvent) event;
            if (contextEvent.getBgContextKey().equals(this.bgContext.getKey())) {
                if (contextEvent.getCommand().equals(BGContextEvent.CONTEXT_DELETED) || contextEvent.getCommand().equals(BGContextEvent.RESOURCE_REMOVED)) {
                    // this results in a screen where the BGManagementController
                    // is no longer functional -> hence only closeable
                    dispose();// disposed message is defined in constructor!
                }
            }

        } else if (event instanceof BusinessGroupModifiedEvent) {
            if (event.getCommand().equals(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT)) {
                // update reference to updated business group object
                final BusinessGroup modifiedGroup = businessGroupService.loadBusinessGroup(this.currentGroup);
                if (groupListModel != null) {
                    final List groups = groupListModel.getObjects();
                    if (groups.contains(this.currentGroup)) {
                        final int i = groups.indexOf(this.currentGroup);
                        groups.set(i, modifiedGroup);
                    }
                }
                this.currentGroup = modifiedGroup;
            }
        }
    }

    private void doGroupMessage(final UserRequest ureq) {
        doContactForm(ureq);
        sendMessageVC.contextPut("title", translate("group.message", this.currentGroup.getName()));
    }

    /**
     * removeAsListenerAndDispose
     * 
     * @param ureq
     */
    private void doContactForm(final UserRequest ureq) {
        if (vc_sendToChooserForm == null) {
            vc_sendToChooserForm = new VelocityContainer("cosendtochooser", BusinessGroupMainRunController.class, "cosendtochooser", businessGroupTranslator, this);
        }
        removeAsListenerAndDispose(sendToChooserForm);
        sendToChooserForm = new BusinessGroupSendToChooserForm(ureq, getWindowControl(), this.currentGroup, getIsGMAdminOwner(ureq));
        listenTo(sendToChooserForm);
        vc_sendToChooserForm.put("vc_sendToChooserForm", sendToChooserForm.getInitialComponent());
        content.setContent(vc_sendToChooserForm);
    }

    /**
     * @param ureq
     * @return
     */
    private boolean getIsGMAdminOwner(final UserRequest ureq) {
        Identity identity = ureq.getIdentity();
        isGMAdminOwner = getBusinessGroupEBL().isGroupManagementAdmin(identity, currentGroup, flags);
        return isGMAdminOwner;
    }

    private void doOwnersMessage(final UserRequest ureq) {
        final List owners = ownerListModel.getObjects();
        doSendMessage(owners, translate("owners.message.to"), ureq);
        sendMessageVC.contextPut("title", translate("owners.message"));
    }

    private void doParticipantsMessage(final UserRequest ureq) {
        final List participants = participantListModel.getObjects();
        doSendMessage(participants, translate("participants.message.to"), ureq);
        sendMessageVC.contextPut("title", translate("participants.message"));
    }

    private void doUserMessage(final UserRequest ureq) {
        final List users = new ArrayList();
        users.add(this.currentIdentity);
        final User user = this.currentIdentity.getUser();
        final Locale loc = I18nManager.getInstance().getLocaleOrDefault(user.getPreferences().getLanguage());
        doSendMessage(users, getUserService().getFirstAndLastname(user), ureq);

        sendMessageVC.contextPut(
                "title",
                getTranslator().translate(
                        "user.message",
                        new String[] { getUserService().getUserProperty(this.currentIdentity.getUser(), UserConstants.FIRSTNAME, getLocale()),
                                getUserService().getUserProperty(this.currentIdentity.getUser(), UserConstants.LASTNAME, getLocale()) }));
    }

    private void doSendMessage(final List identities, final String mailToName, final UserRequest ureq) {
        final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
        addContactListToContactMessage(cmsg, mailToName, identities);

        removeAsListenerAndDispose(contactCtr);
        contactCtr = new ContactFormController(ureq, getWindowControl(), false, true, false, false, cmsg);
        listenTo(contactCtr);
        sendMessageVC.put("contactForm", contactCtr.getInitialComponent());
        setMainContent(sendMessageVC);
    }

    private void doAreaDeleteConfirm(final UserRequest ureq) {
        String text = translate("area.delete", StringHelper.escapeHtml(this.currentArea.getName()));
        confirmDeleteArea = activateYesNoDialog(ureq, null, text, confirmDeleteArea);
    }

    private void doGroupDeleteConfirm(final UserRequest ureq) {
        String confirmDeleteGroupText;
        final List<String> deleteableList = businessGroupService.getDependingDeletablableListFor(currentGroup, ureq.getLocale());
        if (deleteableList.isEmpty()) {
            confirmDeleteGroupText = translate("group.delete", StringHelper.escapeHtml(this.currentGroup.getName()));
        } else {
            final StringBuilder buf = new StringBuilder();
            for (final String element : deleteableList) {
                if (buf.length() > 0) {
                    buf.append(" ,");
                }
                buf.append(StringHelper.escapeHtml(element));
            }
            final String[] args = new String[] { StringHelper.escapeHtml(this.currentGroup.getName()), buf.toString() };
            confirmDeleteGroupText = translate("group.delete.in.use", args);
        }
        confirmDeleteGroup = activateYesNoDialog(ureq, null, confirmDeleteGroupText, confirmDeleteGroup);
    }

    private void doContextEdit(final UserRequest ureq) {
        // check if user is owner of this group context
        boolean isContextOwnerOrAdmin = getBusinessGroupEBL().isContextOwnerOrAdmin(ureq.getIdentity(), ureq.getUserSession().getRoles(), this.bgContext);
        if (isContextOwnerOrAdmin) {
            removeAsListenerAndDispose(contextEditCtr);
            contextEditCtr = new BGContextEditController(ureq, getWindowControl(), this.bgContext);
            listenTo(contextEditCtr);
            contextEditVC.put("contexteditor", contextEditCtr.getInitialComponent());
            contextEditVC.contextPut("editingAllowed", Boolean.TRUE);
        } else {
            // show who is the owner of this context
            removeAsListenerAndDispose(contextOwnersCtr);
            contextOwnersCtr = new GroupController(ureq, getWindowControl(), false, true, false, this.bgContext.getOwnerGroup(), null);
            listenTo(contextOwnersCtr);
            contextEditVC.put("owners", contextOwnersCtr.getInitialComponent());
            contextEditVC.contextPut("editingAllowed", Boolean.FALSE);
        }

        setMainContent(contextEditVC);
        setTools(STATE_CONTEXT_EDIT);
    }

    private void listMembers(final UserRequest ureq, final String cmd) {
        if (CMD_LIST_MEMBERS_WITH_GROUPS.equals(cmd)) {
            if (BGContextDaoImpl.getInstance().getGroupsOfBGContext(bgContext).size() == 0) {
                showError("tools.title.listmembers.warning.noGroups");
                return;
            }
            removeAsListenerAndDispose(memberListWizardController);
            memberListWizardController = new MemberListWizardController(ureq, getWindowControl(), bgContext, MemberListWizardController.GROUPS_MEMBERS);
            listenTo(memberListWizardController);
        } else if (CMD_LIST_MEMBERS_WITH_AREAS.equals(cmd)) {
            if (BGAreaDaoImpl.getInstance().findBGAreasOfBGContext(bgContext).size() == 0) {
                showError("tools.title.listmembers.warning.noAreas");
                return;
            }
            removeAsListenerAndDispose(memberListWizardController);
            memberListWizardController = new MemberListWizardController(ureq, getWindowControl(), bgContext, MemberListWizardController.AREAS_MEMBERS);
            listenTo(memberListWizardController);
        }
        if (memberListWizardController != null) {
            removeAsListenerAndDispose(closeableModalController);
            closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), memberListWizardController.getInitialComponent());
            listenTo(closeableModalController);
            closeableModalController.activate();
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        final String cmd = event.getCommand();
        if (source == toolC) {
            handleToolCommands(ureq, cmd);
        } else if (source == groupEditCtr) {
            if (event == Event.CANCELLED_EVENT) { // when group was locked
                releaseAdminLockAndGroupMUE();
                doBack();
            }
        } else if (source == bgCopyWizardCtr) {
            if (event.equals(Event.DONE_EVENT)) {
                final BusinessGroup newGroup = bgCopyWizardCtr.getNewGroup();
                if (newGroup == null) {
                    throw new AssertException("bgCopyWizardCtr.getNewGroup returned null");
                } else {
                    releaseAdminLockAndGroupMUE();
                    getWindowControl().pop();
                    this.currentGroup = newGroup;
                    doGroupEdit(ureq);
                }
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                getWindowControl().pop();
            }
        } else if (source == bgMultipleCopyWizardCtr) {
            if (event.equals(Event.DONE_EVENT)) {
                releaseAdminLockAndGroupMUE();
                getWindowControl().pop();
                doGroupList(ureq, true);
            } else if (event.equals(Event.CANCELLED_EVENT)) {
                getWindowControl().pop();
            }
        } else if (source == confirmDeleteGroup) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
                releaseAdminLockAndGroupMUE();
                final String deletedGroupName = this.currentGroup.getName();
                final LoggingResourceable lri = LoggingResourceable.wrap(currentGroup);
                doGroupDelete();
                doGroupList(ureq, false);
                // do logging
                ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), lri);
                showInfo("info.group.deleted");
            }
        } else if (source == areaEditCtr) {
            // TODO event: changed area: update models
        } else if (source == confirmDeleteArea) {
            if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
                final String deletedAreaName = this.currentArea.getName();
                final LoggingResourceable lri = LoggingResourceable.wrap(currentArea);
                doAreaDelete();
                doAreaList(ureq, false);
                // do logging
                ThreadLocalUserActivityLogger.log(GroupLoggingAction.AREA_DELETED, getClass(), lri);
            }
        } else if (source == contactCtr) {
            if (event.equals(Event.DONE_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
                doBack();
            }
        } else if (source == groupListCtr) {
            if (event.equals(TableController.EVENT_NOFILTER_SELECTED)) {
                this.currentAreaFilter = null;
                doGroupList(ureq, true);
            } else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
                this.currentAreaFilter = (BGArea) groupListCtr.getActiveFilter();
                doGroupList(ureq, true);
            } else if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentGroup = groupListModel.getBusinessGroupAt(rowid);
                if (actionid.equals(CMD_GROUP_EDIT)) {
                    doGroupEdit(ureq);
                } else if (actionid.equals(CMD_GROUP_RUN)) {
                    doGroupRun(ureq);
                } else if (actionid.equals(CMD_GROUP_DELETE)) {
                    doGroupDeleteConfirm(ureq);
                }
            }
        } else if (source == areaListCtr) {
            if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentArea = areaListModel.getBGAreaAt(rowid);
                if (actionid.equals(CMD_AREA_EDIT)) {
                    doAreaEdit(ureq);
                } else if (actionid.equals(CMD_AREA_DELETE)) {
                    doAreaDeleteConfirm(ureq);
                }
            }
        } else if (source == ownerListCtr) {
            if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentIdentity = ownerListModel.getIdentityAt(rowid);
                if (actionid.equals(CMD_USER_DETAILS)) {
                    doUserDetails(ureq);
                }
            }
        } else if (source == participantListCtr) {
            if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentIdentity = participantListModel.getIdentityAt(rowid);
                if (actionid.equals(CMD_USER_DETAILS)) {
                    doUserDetails(ureq);
                }
            }
        } else if (source == contextEditCtr) {
            if (event == Event.CHANGED_EVENT) {
                // reload context, maybe updated title or something
                this.bgContext = contextManager.loadBGContext(this.bgContext);
            }
        } else if (source == groupCreateController) {
            if (event == Event.DONE_EVENT) {
                releaseAdminLockAndGroupMUE();
                this.currentGroup = groupCreateController.getCreatedGroup();
                doGroupEdit(ureq);
                // do loggin is already done in create controller
            } else if (event == Event.CANCELLED_EVENT) {
                doBack();
            }
        } else if (source == areaCreateController) {
            if (event == Event.DONE_EVENT) {
                releaseAdminLockAndGroupMUE();
                final BGArea createdArea = areaCreateController.getCreatedArea();
                if (createdArea != null) {
                    this.currentArea = createdArea;
                    doAreaEdit(ureq);
                } else {
                    showInfo("error.area.name.exists");
                }
                // do loggin is already done in create controller
            } else if (event == Event.CANCELLED_EVENT) {
                doBack();
            }
        } else if (source == memberListWizardController) {
            closeableModalController.deactivate();
        } else if (source == sendToChooserForm) {
            if (event == Event.DONE_EVENT) {
                removeAsListenerAndDispose(contactCtr);
                contactCtr = createContactFormController(ureq);
                listenTo(contactCtr);
                sendMessageVC.put("contactForm", contactCtr.getInitialComponent());
                setMainContent(sendMessageVC);
            } else if (event == Event.CANCELLED_EVENT) {
                content.setContent(this.currentComponent);
            }
        }
    }

    private void handleToolCommands(final UserRequest ureq, final String cmd) {
        if (cmd.equals(CMD_CLOSE)) {
            releaseAdminLockAndGroupMUE();
            // Send done event to parent controller
            fireEvent(ureq, Event.DONE_EVENT);
        } else if (cmd.equals(CMD_BACK)) {
            releaseAdminLockAndGroupMUE();
            // Send back event to parent controller
            fireEvent(ureq, Event.BACK_EVENT);
        } else if (cmd.equals(CMD_GROUP_CREATE)) {
            createNewGroupController(ureq, getWindowControl());
        } else if (cmd.equals(CMD_AREA_CREATE)) {
            createNewAreaController(ureq, getWindowControl());
        } else if (cmd.equals(CMD_GROUP_RUN)) {
            doGroupRun(ureq);
        } else if (cmd.equals(CMD_GROUP_COPY)) {
            doGroupCopy(ureq);
        } else if (cmd.equals(CMD_GROUP_COPY_MULTIPLE)) {
            doMultipleGroupCopy(ureq);
        } else if (cmd.equals(CMD_GROUP_DELETE)) {
            doGroupDeleteConfirm(ureq);
        } else if (cmd.equals(CMD_GROUP_MESSAGE)) {
            doGroupMessage(ureq);
        } else if (cmd.equals(CMD_AREA_DELETE)) {
            doAreaDeleteConfirm(ureq);
        } else if (cmd.equals(CMD_PARTICIPANTS_MESSAGE)) {
            doParticipantsMessage(ureq);
        } else if (cmd.equals(CMD_OWNERS_MESSAGE)) {
            doOwnersMessage(ureq);
        } else if (cmd.equals(CMD_USER_MESSAGE)) {
            doUserMessage(ureq);
        } else if (cmd.equals(CMD_LIST_MEMBERS_WITH_GROUPS)) {
            listMembers(ureq, CMD_LIST_MEMBERS_WITH_GROUPS);
        } else if (cmd.equals(CMD_LIST_MEMBERS_WITH_AREAS)) {
            listMembers(ureq, CMD_LIST_MEMBERS_WITH_AREAS);
        }

    }

    private void handleMenuCommands(final UserRequest ureq) {
        final TreeNode selTreeNode = olatMenuTree.getSelectedNode();
        final String cmd = (String) selTreeNode.getUserObject();

        releaseAdminLockAndGroupMUE();
        if (cmd.equals(CMD_OVERVIEW)) {
            doOverview(ureq);
        } else if (cmd.equals(CMD_EDITCONTEXT)) {
            doContextEdit(ureq);
        } else if (cmd.equals(CMD_GROUPLIST)) {
            this.currentAreaFilter = null;
            doGroupList(ureq, true);
        } else if (cmd.equals(CMD_AREALIST)) {
            doAreaList(ureq, true);
        } else if (cmd.equals(CMD_USER_LIST)) {
            doUsersList(ureq, true);
        }
    }

    private TreeModel buildTreeModel(final UserRequest ureq) {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(translate("menu.index"));
        root.setUserObject(CMD_OVERVIEW);
        root.setAltText(translate("menu.index.alt"));
        gtm.setRootNode(root);

        if (!this.bgContext.isDefaultContext() || ureq.getUserSession().getRoles().isOLATAdmin()) {
            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.editcontext"));
            gtn.setUserObject(CMD_EDITCONTEXT);
            gtn.setAltText(translate("menu.editcontext.alt"));
            root.addChild(gtn);
        }

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.allgroups"));
        gtn.setUserObject(CMD_GROUPLIST);
        gtn.setAltText(translate("menu.allgroups.alt"));
        root.addChild(gtn);

        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.allareas"));
            gtn.setUserObject(CMD_AREALIST);
            gtn.setAltText(translate("menu.allareas.alt"));
            root.addChild(gtn);
        }

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.allusers"));
        gtn.setUserObject(CMD_USER_LIST);
        gtn.setAltText(translate("menu.allusers.alt"));
        root.addChild(gtn);

        return gtm;
    }

    private void setTools(final int state) {
        removeAsListenerAndDispose(toolC);
        toolC = ToolFactory.createToolController(getWindowControl());
        listenTo(toolC);
        columnLayoutCtr.setCol2(toolC.getInitialComponent());
        if (state == STATE_CONTEXT_REMOVED) {
            toolC.addHeader(translate("tools.title.groupmanagement"));
            toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");
            return;
        }

        // header for generic action. if groups have rights, assueme
        // rightsmanagement
        // otherwhise groupmanagement
        if (flags.isEnabled(BGConfigFlags.RIGHTS)) {
            toolC.addHeader(translate("tools.title.rightmanagement"));
        } else {
            toolC.addHeader(translate("tools.title.groupmanagement"));
        }

        // Generic actions
        if (flags.isEnabled(BGConfigFlags.GROUPS_CREATE)) {
            toolC.addLink(CMD_GROUP_CREATE, translate(CMD_GROUP_CREATE));
        }
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            toolC.addLink(CMD_AREA_CREATE, translate(CMD_AREA_CREATE));
        }
        if (flags.isEnabled(BGConfigFlags.BACK_SWITCH)) {
            toolC.addLink(CMD_BACK, translate(CMD_BACK));
        }
        toolC.addLink(CMD_CLOSE, translate(CMD_CLOSE), null, "b_toolbox_close");

        // TODO: (LD) check where is this displayable.
        toolC.addHeader(translate("tools.title.listmembers"));
        // TODO: (LD) check flags
        toolC.addLink(CMD_LIST_MEMBERS_WITH_GROUPS, translate(CMD_LIST_MEMBERS_WITH_GROUPS));
        toolC.addLink(CMD_LIST_MEMBERS_WITH_AREAS, translate(CMD_LIST_MEMBERS_WITH_AREAS));

        if (state == STATE_GROUP_EDIT) {
            toolC.addHeader(translate("tools.title.group"));
            toolC.addLink(CMD_GROUP_MESSAGE, translate(CMD_GROUP_MESSAGE));
            toolC.addLink(CMD_GROUP_RUN, translate(CMD_GROUP_RUN));
            toolC.addLink(CMD_GROUP_COPY, translate(CMD_GROUP_COPY));
            toolC.addLink(CMD_GROUP_COPY_MULTIPLE, translate(CMD_GROUP_COPY_MULTIPLE));
            if (flags.isEnabled(BGConfigFlags.GROUPS_DELETE)) {
                toolC.addLink(CMD_GROUP_DELETE, translate(CMD_GROUP_DELETE));
            }
        }

        if (state == STATE_AREA_EDIT && flags.isEnabled(BGConfigFlags.AREAS_DELETE)) {
            toolC.addHeader(translate("tools.title.area"));
            toolC.addLink(CMD_AREA_DELETE, translate(CMD_AREA_DELETE));
        }

        if (state == STATE_USER_LIST) {
            toolC.addHeader(translate("tools.title.userlist"));
            if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
                toolC.addLink(CMD_OWNERS_MESSAGE, translate(CMD_OWNERS_MESSAGE));
            }
            toolC.addLink(CMD_PARTICIPANTS_MESSAGE, translate(CMD_PARTICIPANTS_MESSAGE));
        }

        if (state == STATE_USER_DETAILS) {
            toolC.addHeader(translate("tools.title.user"));
            toolC.addLink(CMD_USER_MESSAGE, translate(CMD_USER_MESSAGE));
        }

    }

    private void initVC() {
        // push group type as 'type' for type specific help pages
        // Overview page
        overviewVC = createVelocityContainer("overview");
        overviewVC.contextPut("flags", flags);
        overviewVC.contextPut("type", this.groupType);
        // Context edit container - init anyway, maybe not used
        contextEditVC = createVelocityContainer("contextedit");
        // Create new group form
        newGroupVC = createVelocityContainer("newgroup");
        newGroupVC.contextPut("type", this.groupType);
        // Group list
        groupListVC = createVelocityContainer("grouplist");
        groupListVC.contextPut("type", this.groupType);
        // Group message
        sendMessageVC = createVelocityContainer("sendmessage");
        sendMessageVC.contextPut("type", this.groupType);
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            // Create new area form
            newAreaVC = createVelocityContainer("newarea");
            newAreaVC.contextPut("type", this.groupType);
            // Area list
            areaListVC = createVelocityContainer("arealist");
            areaListVC.contextPut("type", this.groupType);
        }
        // User list
        userListVC = createVelocityContainer("userlist");
        userListVC.contextPut("type", this.groupType);
        // User details
        userDetailsVC = new VelocityContainer("userdetails", PackageUtil.getPackageVelocityRoot(this.getClass()) + "/userdetails.html",
                PackageUtil.createPackageTranslator(HomePageDisplayController.class, getLocale(), getTranslator()), this);
        backButton = LinkFactory.createButtonSmall("back", userDetailsVC, this);
        userDetailsVC.contextPut("type", this.groupType);
    }

    private void doOverview(final UserRequest ureq) {
        setMainContent(overviewVC);
        // number of groups
        overviewVC.contextPut("numbGroups", new Integer(contextManager.countGroupsOfBGContext(bgContext)));
        // number of owners
        if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
            final int total = (contextManager.countBGOwnersOfBGContext(bgContext) + contextManager.countBGParticipantsOfBGContext(bgContext));
            overviewVC.contextPut("numbTotal", new Integer(total));
            overviewVC.contextPut("numbOwners", new Integer(contextManager.countBGOwnersOfBGContext(bgContext)));
        }
        overviewVC.contextPut("numbParticipants", new Integer(contextManager.countBGParticipantsOfBGContext(bgContext)));
        // number of areas
        if (flags.isEnabled(BGConfigFlags.AREAS)) {
            overviewVC.contextPut("numbAreas", new Integer(areaManager.countBGAreasOfBGContext(bgContext)));
        }
        // context name
        if (this.bgContext.isDefaultContext()) {
            overviewVC.contextPut("showContextName", Boolean.FALSE);
        } else {
            overviewVC.contextPut("showContextName", Boolean.TRUE);
            overviewVC.contextPut("contextName", bgContext.getName());
            overviewVC.contextPut("contextDesc", bgContext.getDescription());
        }
        if (this.bgContext.isDefaultContext()) {
            overviewVC.contextPut("isDefaultContext", Boolean.TRUE);
        } else {
            overviewVC.contextPut("isDefaultContext", Boolean.FALSE);
            // other resources that also use this context
            doAddOtherResourcesList(ureq);
        }

        setTools(STATE_OVERVIEW);
    }

    private void doAddOtherResourcesList(final UserRequest ureq) {
        final List repoTableModelEntries = getBgContextService().findRepositoryEntriesForBGContext(this.bgContext);
        if (repoTableModelEntries.size() > 1) {
            final Translator resourceTrans = PackageUtil.createPackageTranslator(RepositoryTableModel.class, ureq.getLocale(), getTranslator());

            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            removeAsListenerAndDispose(resourcesCtr);
            resourcesCtr = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
            listenTo(resourcesCtr);
            final RepositoryTableModel repoTableModel = new RepositoryTableModel(resourceTrans);
            repoTableModel.setObjects(repoTableModelEntries);
            repoTableModel.addColumnDescriptors(resourcesCtr, null, false);
            resourcesCtr.setTableDataModel(repoTableModel);
            overviewVC.put("otherResources", resourcesCtr.getInitialComponent());
            overviewVC.contextPut("usedByOtherResources", Boolean.TRUE);
        } else {
            overviewVC.contextRemove("otherResources");
            overviewVC.contextPut("usedByOtherResources", Boolean.FALSE);
        }
    }

    /**
     * @return
     */
    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    /*
     * create and init controller to create new area(s)
     */
    private void createNewAreaController(final UserRequest ureq, final WindowControl wControl) {
        removeAsListenerAndDispose(areaCreateController);
        areaCreateController = BGControllerFactory.getInstance().createNewAreaController(ureq, wControl, bgContext);
        listenTo(areaCreateController);

        newAreaVC.put("areaCreateForm", areaCreateController.getInitialComponent());
        setMainContent(newAreaVC);
        setTools(STATE_AREA_CREATE_FORM);
    }

    private void createNewGroupController(final UserRequest ureq, final WindowControl wControl) {
        removeAsListenerAndDispose(groupCreateController);
        groupCreateController = BGControllerFactory.getInstance().createNewBGController(ureq, wControl, flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE), bgContext);
        listenTo(groupCreateController);

        newGroupVC.put("groupCreateForm", groupCreateController.getInitialComponent());
        setMainContent(newGroupVC);
        setTools(STATE_GROUP_CREATE_FORM);
    }

    private void doGroupCopy(final UserRequest ureq) {
        removeAsListenerAndDispose(bgCopyWizardCtr);
        bgCopyWizardCtr = new BGCopyWizardController(ureq, getWindowControl(), this.currentGroup, this.flags);
        listenTo(bgCopyWizardCtr);
        removeAsListenerAndDispose(closeableModalController);
        closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), bgCopyWizardCtr.getInitialComponent());
        listenTo(closeableModalController);

        closeableModalController.activate();
    }

    private void doMultipleGroupCopy(final UserRequest ureq) {
        removeAsListenerAndDispose(bgMultipleCopyWizardCtr);
        bgMultipleCopyWizardCtr = new BGMultipleCopyWizardController(ureq, getWindowControl(), this.currentGroup, this.flags);
        listenTo(bgMultipleCopyWizardCtr);
        removeAsListenerAndDispose(closeableModalController);
        closeableModalController = new CloseableModalController(getWindowControl(), translate("close"), bgMultipleCopyWizardCtr.getInitialComponent());
        listenTo(closeableModalController);
        closeableModalController.activate();
    }

    private void doGroupEdit(final UserRequest ureq) {
        Project project = businessGroupService.getProjectForBusinessGroup(this.currentGroup.getKey());
        if (project != null) {
            removeAsListenerAndDispose(projectController);
            BusinessGroupProjectControllerFactory factory = new BusinessGroupProjectControllerFactoryImpl(ureq, getWindowControl(), project);
            projectController = factory.getProjectController();
            listenTo(projectController);
            // add as listener to BusinessGroup so we are being notified about changes.
            CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), this.currentGroup);
            setMainContent(projectController.getInitialComponent());
        } else {
            removeAsListenerAndDispose(groupEditCtr);
            groupEditCtr = BGControllerFactory.getInstance().createEditControllerFor(ureq, getWindowControl(), this.currentGroup);
            listenTo(groupEditCtr);
            // add as listener to BusinessGroup so we are being notified about changes.
            CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), this.currentGroup);

            setMainContent(groupEditCtr.getInitialComponent());
            if (groupEditCtr.isLockAcquired()) {
                setTools(STATE_GROUP_EDIT);
            }
        }
        // else don't change the tools state
    }

    private void doGroupRun(final UserRequest ureq) {
        BGControllerFactory.getInstance().createRunControllerAsTopNavTab(this.currentGroup, ureq, getWindowControl(), true, null);
    }

    private void doGroupDelete() {
        // remove this controller as listener from the group
        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.currentGroup);
        // now delete group and update table model
        businessGroupService.deleteBusinessGroup(this.currentGroup);
        if (groupListModel != null) {
            groupListModel.getObjects().remove(this.currentGroup);
            groupListCtr.modelChanged();
        }
        this.currentGroup = null;
    }

    private void doGroupList(final UserRequest ureq, final boolean initializeModel) {
        // Init table only once
        if (groupListCtr == null) {
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setTableEmptyMessage(translate("grouplist.no.groups"));
            // init group list filter controller

            removeAsListenerAndDispose(groupListCtr);
            groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), this.areaFilters, this.currentAreaFilter, translate("grouplist.areafilter.title"),
                    translate("grouplist.areafilter.nofilter"), getTranslator());
            listenTo(groupListCtr);
            groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.name", 0, CMD_GROUP_RUN, ureq.getLocale()));
            groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("grouplist.table.desc", 1, null, ureq.getLocale()));
            groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_GROUP_EDIT, "grouplist.table.edit", translate(CMD_GROUP_EDIT)));
            if (flags.isEnabled(BGConfigFlags.GROUPS_DELETE)) {
                groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_GROUP_DELETE, "grouplist.table.delete", translate(CMD_GROUP_DELETE)));
            }
            groupListVC.put("groupListTableCtr", groupListCtr.getInitialComponent());

        }
        if (groupListModel == null || initializeModel) {
            // 1. group list model: if area filter is set use only groups from given
            // area
            List groups;
            if (this.currentAreaFilter == null) {
                groups = contextManager.getGroupsOfBGContext(bgContext); // all groups
            } else {
                groups = areaManager.findBusinessGroupsOfArea(this.currentAreaFilter); // filtered
                // groups
            }
            groupListModel = new BusinessGroupTableModel(groups);
            groupListCtr.setTableDataModel(groupListModel);

            // 2. find areas for group list filter
            if (flags.isEnabled(BGConfigFlags.AREAS)) {
                this.areaFilters = areaManager.findBGAreasOfBGContext(bgContext);
                groupListCtr.setFilters(this.areaFilters, this.currentAreaFilter);
            }

        }
        setMainContent(groupListVC);
        setTools(STATE_GROUP_LIST);
    }

    private void doAreaEdit(final UserRequest ureq) {
        removeAsListenerAndDispose(areaEditCtr);
        areaEditCtr = new BGAreaEditController(ureq, getWindowControl(), this.currentArea);
        listenTo(areaEditCtr);

        setMainContent(areaEditCtr.getInitialComponent());
        setTools(STATE_AREA_EDIT);
    }

    private void doAreaDelete() {
        areaManager.deleteBGArea(this.currentArea);
        if (areaListModel != null) {
            areaListModel.getObjects().remove(this.currentArea);
            areaListCtr.modelChanged();
        }
        this.currentArea = null;
    }

    private void doAreaList(final UserRequest ureq, final boolean initializeModel) {
        if (areaListModel == null || initializeModel) {
            final List areas = areaManager.findBGAreasOfBGContext(bgContext);
            areaListModel = new BGAreaTableModel(areas, getTranslator());

            if (areaListCtr != null) {
                areaListCtr.dispose();
            }
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setTableEmptyMessage(translate("arealist.no.areas"));
            removeAsListenerAndDispose(areaListCtr);
            areaListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
            listenTo(areaListCtr);
            areaListCtr.addColumnDescriptor(new DefaultColumnDescriptor("arealist.table.name", 0, null, ureq.getLocale()));
            areaListCtr.addColumnDescriptor(new DefaultColumnDescriptor("arealist.table.desc", 1, null, ureq.getLocale()));
            areaListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_AREA_EDIT, "arealist.table.edit", translate(CMD_AREA_EDIT)));
            if (flags.isEnabled(BGConfigFlags.AREAS_DELETE)) {
                areaListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_AREA_DELETE, "arealist.table.delete", translate(CMD_AREA_DELETE)));
            }
            areaListCtr.setTableDataModel(areaListModel);
            areaListVC.put("arealisttable", areaListCtr.getInitialComponent());
        }
        setMainContent(areaListVC);
        setTools(STATE_AREA_LIST);
    }

    private void doUsersList(final UserRequest ureq, final boolean initializeModel) {
        // 1. init owners list
        if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
            if (ownerListModel == null || initializeModel) {
                final List owners = contextManager.getBGOwnersOfBGContext(bgContext);

                final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
                tableConfig.setPreferencesOffered(true, "ownerListController");
                tableConfig.setTableEmptyMessage(translate("userlist.owners.noOwners"));

                removeAsListenerAndDispose(ownerListCtr);
                ownerListCtr = UserControllerFactory.createTableControllerFor(tableConfig, owners, ureq, getWindowControl(), CMD_USER_DETAILS);
                listenTo(ownerListCtr);

                ownerListModel = (UserTableDataModel) ownerListCtr.getTableDataModel();

                userListVC.put("ownerlisttable", ownerListCtr.getInitialComponent());
            }
            userListVC.contextPut("showOwners", Boolean.TRUE);
        } else {
            userListVC.contextPut("showOwners", Boolean.FALSE);
        }

        // 2. init participants list
        if (participantListModel == null || initializeModel) {
            final List participants = contextManager.getBGParticipantsOfBGContext(bgContext);

            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setPreferencesOffered(true, "participantsListController");
            tableConfig.setTableEmptyMessage(translate("userlist.participants.noParticipants"));

            removeAsListenerAndDispose(participantListCtr);
            participantListCtr = UserControllerFactory.createTableControllerFor(tableConfig, participants, ureq, getWindowControl(), CMD_USER_DETAILS);
            listenTo(participantListCtr);

            participantListModel = (UserTableDataModel) participantListCtr.getTableDataModel();

            userListVC.put("participantlisttable", participantListCtr.getInitialComponent());
        }
        // 3. set content
        setMainContent(userListVC);
        setTools(STATE_USER_LIST);
    }

    private void doUserDetails(final UserRequest ureq) {
        // 1. expose the identity details
        userDetailsVC.contextPut("userFirstAndLastName", getUserService().getFirstAndLastname(this.currentIdentity.getUser()));
        final Translator babel = getUserService().getUserPropertiesConfig().getTranslator(userTrans);

        final HomePageConfig homePageConfig = HomePageConfigManagerImpl.getInstance().loadConfigFor(currentIdentity.getName());
        removeAsListenerAndDispose(homePageDisplayController);
        homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), homePageConfig);
        listenTo(homePageDisplayController);
        userDetailsVC.put("userdetailsform", homePageDisplayController.getInitialComponent());
        // 2. expose the owner groups of the identity
        if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS)) {
            final List ownerGroups = businessGroupService.findBusinessGroupsOwnedBy(bgContext.getGroupType(), this.currentIdentity, bgContext);

            final Link[] ownerGroupLinks = new Link[ownerGroups.size()];
            int ownerNumber = 0;

            for (final Iterator iter = ownerGroups.iterator(); iter.hasNext();) {
                final BusinessGroup group = (BusinessGroup) iter.next();
                final Link tmp = LinkFactory.createCustomLink("cmd.user.remove.group.own." + group.getKey(), "cmd.user.remove.group.own." + group.getKey(),
                        "userdetails.remove", Link.BUTTON_SMALL, userDetailsVC, this);
                tmp.setUserObject(group);
                ownerGroupLinks[ownerNumber] = tmp;
                ownerNumber++;
            }
            userDetailsVC.contextPut("ownerGroupLinks", ownerGroupLinks);
            userDetailsVC.contextPut("noOwnerGroups", (ownerGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
            userDetailsVC.contextPut("showOwnerGroups", Boolean.TRUE);
        } else {
            userDetailsVC.contextPut("showOwnerGroups", Boolean.FALSE);
        }
        // 3. expose the participant groups of the identity
        final List participantGroups = businessGroupService.findBusinessGroupsAttendedBy(bgContext.getGroupType(), this.currentIdentity, bgContext);

        final Link[] participantGroupLinks = new Link[participantGroups.size()];
        int participantNumber = 0;

        for (final Iterator iter = participantGroups.iterator(); iter.hasNext();) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            final Link tmp = LinkFactory.createCustomLink("cmd.user.remove.group.part." + group.getKey(), "cmd.user.remove.group.part." + group.getKey(),
                    "userdetails.remove", Link.BUTTON_SMALL, userDetailsVC, this);
            tmp.setUserObject(group);
            participantGroupLinks[participantNumber] = tmp;
            participantNumber++;
        }
        userDetailsVC.contextPut("noParticipantGroups", (participantGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
        userDetailsVC.contextPut("participantGroupLinks", participantGroupLinks);
        // 4. set content
        setMainContent(userDetailsVC);
        setTools(STATE_USER_DETAILS);
    }

    private void doRemoveUserFromParticipatingGroup(final Identity ureqIdentity, final Identity toRemoveIdentity, final String groupKey) {
        final Long key = Long.valueOf(groupKey);
        final BusinessGroup group = businessGroupService.loadBusinessGroup(key, true);
        final List<Identity> identities = new ArrayList<Identity>(1);
        identities.add(toRemoveIdentity);
        businessGroupService.removeParticipantsAndFireEvent(ureqIdentity, identities, group, flags);
    }

    private void doRemoveUserFromOwnedGroup(final UserRequest ureq, final String groupKey) {
        final Long key = Long.valueOf(groupKey);
        final BusinessGroup group = businessGroupService.loadBusinessGroup(key, true);
        businessGroupService.removeOwnerAndFireEvent(ureq.getIdentity(), currentIdentity, group, flags, false);
    }

    /**
     * generates the email addresses list.
     * 
     * @param ureq
     * @return a contact form controller for this group
     */
    private ContactFormController createContactFormController(final UserRequest ureq) {
        final BaseSecurity scrtMngr = getBaseSecurity();

        Identity fromIdentity = ureq.getIdentity();
        final SecurityGroup owners = currentGroup.getOwnerGroup();
        final SecurityGroup participants = currentGroup.getPartipiciantGroup();
        final SecurityGroup waitingList = currentGroup.getWaitingGroup();

        boolean isAdmin = getIsGMAdminOwner(ureq);
        boolean sendToGroupOwnersIsConfigured = flags.isEnabled(BGConfigFlags.GROUP_OWNERS);
        boolean isUserRoleAdminAndWaitingListEnabled = isAdmin && currentGroup.getWaitingListEnabled().booleanValue();

        GroupParameter ownerGroupParameter = createOwnerGroupParameter(sendToGroupOwnersIsConfigured, scrtMngr, owners);
        GroupParameter participantGroupParameter = createParticipantGroupParameter(scrtMngr, participants);
        GroupParameter waitingGroupParameter = createWaitingListParameter(isUserRoleAdminAndWaitingListEnabled, scrtMngr, waitingList);

        BusinessGroupSendToChooserFormUIModel sendToChooseFormUIModel = new BusinessGroupSendToChooserFormUIModel(fromIdentity, ownerGroupParameter,
                participantGroupParameter, waitingGroupParameter);
        ContactMessage contactMessage = sendToChooseFormUIModel.getContactMessage();

        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(currentGroup);
        final ContactFormController cofocntrllr = collabTools.createContactFormController(ureq, getWindowControl(), contactMessage);
        return cofocntrllr;
    }

    private void addContactListToContactMessage(final ContactMessage contactMessage, String sendToAllOwnersTitle, final List<Identity> identityList) {
        ContactList contactList = new ContactList(sendToAllOwnersTitle);
        contactList.addAllIdentites(identityList);
        contactMessage.addEmailTo(contactList);
    }

    /**
     * @return
     */
    private List<Identity> getSelectedIdentities(final List<Identity> identities, final List<Long> selectedOwnerKeys) {
        for (final Identity identity : new ArrayList<Identity>(identities)) {
            boolean keyIsSelected = false;
            for (final Long key : selectedOwnerKeys) {
                if (key.equals(identity.getKey())) {
                    keyIsSelected = true;
                    break;
                }
            }
            if (!keyIsSelected) {
                identities.remove(identity);
            }
        }
        return identities;
    }

    private GroupParameter createWaitingListParameter(boolean isUserRoleAdminAndWaitingListEnabled, BaseSecurity scrtMngr, SecurityGroup waitingList) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter waitingGroupParameter = null;
        String contactListName = businessGroupTranslator.translate("sendtochooser.form.chckbx.waitingList");

        if (isUserRoleAdminAndWaitingListEnabled) {
            boolean sendToAllOfWaitingListIsChoosen = sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
            boolean sendToSelectedWaitingListMembers = sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
            List<Long> selectedWaitingListMemberKeys = sendToChooserForm.getSelectedWaitingKeys();
            final List<Identity> waitingListIdentities = scrtMngr.getIdentitiesOfSecurityGroup(waitingList);

            if (sendToAllOfWaitingListIsChoosen) {
                waitingGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(waitingListIdentities, contactListName);
            } else if (sendToSelectedWaitingListMembers) {
                waitingGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(waitingListIdentities, selectedWaitingListMemberKeys, contactListName);
            }
        }
        return waitingGroupParameter;

    }

    private GroupParameter createParticipantGroupParameter(BaseSecurity scrtMngr, SecurityGroup participants) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter participantGroupParameter = null;
        String contactListName = businessGroupTranslator.translate("sendtochooser.form.chckbx.partip");

        boolean sendToAllParticipantsIsChoose = sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
        boolean sendToSelectedParticipants = sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
        List<Long> selectedParticipantKeys = sendToChooserForm.getSelectedPartipKeys();
        final List<Identity> participantsList = scrtMngr.getIdentitiesOfSecurityGroup(participants);

        if (sendToAllParticipantsIsChoose) {
            participantGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(participantsList, contactListName);
        } else if (sendToSelectedParticipants) {
            participantGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(participantsList, selectedParticipantKeys, contactListName);
        }
        return participantGroupParameter;
    }

    private GroupParameter createOwnerGroupParameter(boolean sendToGroupOwnersIsConfigured, final BaseSecurity scrtMngr, final SecurityGroup owners) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter ownerGroupParameter = null;

        String contactListName = businessGroupTranslator.translate("sendtochooser.form.chckbx.owners");
        if (sendToGroupOwnersIsConfigured) {
            boolean sendToAllGroupOwnersIsChoosen = sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
            boolean sendToSelectedGroupOwners = sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
            List<Long> selectedGroupOwnerKeys = sendToChooserForm.getSelectedOwnerKeys();
            final List<Identity> ownerList = scrtMngr.getIdentitiesOfSecurityGroup(owners);

            if (sendToAllGroupOwnersIsChoosen) {
                ownerGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(ownerList, contactListName);
            } else if (sendToSelectedGroupOwners) {
                ownerGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(ownerList, selectedGroupOwnerKeys, contactListName);
            }
        }
        return ownerGroupParameter;
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    /**
     * @return
     */
    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    /**
     * Use the flags to configure the runtime behaviour of this controller
     * 
     * @return the configuration flags
     */
    public BGConfigFlags getControllerFlags() {
        return flags;
    }

    private void setMainContent(final Component component) {
        content.setContent(component);
        this.backComponent = this.currentComponent;
        this.currentComponent = component;
    }

    private void doBack() {
        content.setContent(this.backComponent);
        this.currentComponent = this.backComponent;
    }

    @Override
    protected void doDispose() {

        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.bgContext);

        releaseAdminLockAndGroupMUE();
    }

    /**
     * add every Admin child controller which must be disposed. So that all locks on (OLAT)resources are free-ed up on leaving an admin gui area.
     */
    private void releaseAdminLockAndGroupMUE() {
        // deregister for group change events
        if (this.currentGroup != null) {
            CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.currentGroup);
        }
        // this is done by simply disposing the businessgroup managing controller
        removeAsListenerAndDispose(groupEditCtr);
    }

    /**
     * only for disposedBGAmanagementController!
     * 
     * @param ureq
     */
    void fireDoneEvent(final UserRequest ureq) {
        fireEvent(ureq, Event.DONE_EVENT);
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
