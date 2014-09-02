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

package org.olat.presentation.group.run;

import java.util.List;

import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.course.nodes.projectbroker.Project;
import org.olat.data.group.BusinessGroup;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.resource.OLATResource;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.OlatResourceableType;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupPropertyManager;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.wiki.WikiManager;
import org.olat.presentation.collaboration.CollaborationTools;
import org.olat.presentation.collaboration.CollaborationToolsFactory;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.course.nodes.iq.AssessmentEvent;
import org.olat.presentation.course.nodes.projectbroker.ProjectController;
import org.olat.presentation.framework.common.ControllerFactory;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
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
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.BusinessGroupProjectControllerFactory;
import org.olat.presentation.group.BusinessGroupProjectControllerFactoryImpl;
import org.olat.presentation.group.edit.BusinessGroupEditController;
import org.olat.presentation.group.edit.BusinessGroupModifiedEvent;
import org.olat.presentation.group.run.BusinessGroupSendToChooserFormUIModel.GroupParameter;
import org.olat.presentation.group.securitygroup.GroupController;
import org.olat.presentation.instantmessaging.groupchat.InstantMessagingGroupChatController;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.repository.RepositoryTableModel;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <BR>
 * Runtime environment for a business group. Use the BGControllerFactory and not the constructor to create an instance of this controller.
 * <P>
 * 
 * @version Initial Date: Aug 11, 2004
 * @author patrick
 */

public class BusinessGroupMainRunController extends MainLayoutBasicController implements GenericEventListener {

    private static final String INITVIEW_TOOLFOLDER = "toolfolder";
    public static final OLATResourceable ORES_TOOLFOLDER = OresHelper.createOLATResourceableType(INITVIEW_TOOLFOLDER);

    private static final String INITVIEW_TOOLFORUM = "toolforum";
    public static final OLATResourceable ORES_TOOLFORUM = OresHelper.createOLATResourceableType(INITVIEW_TOOLFORUM);

    private static final String INITVIEW_TOOLWIKI = WikiManager.WIKI_RESOURCE_FOLDER_NAME;
    public static final OLATResourceable ORES_TOOLWIKI = OresHelper.createOLATResourceableType(INITVIEW_TOOLWIKI);

    private static final String INITVIEW_TOOLPORTFOLIO = "toolportfolio";
    public static final OLATResourceable ORES_TOOLPORTFOLIO = OresHelper.createOLATResourceableType(INITVIEW_TOOLPORTFOLIO);

    public static final String INITVIEW_TOOLCAL = "action.calendar.group";
    public static final OLATResourceable ORES_TOOLCAL = OresHelper.createOLATResourceableType(INITVIEW_TOOLCAL);

    private static final String PACKAGE = PackageUtil.getPackageName(BusinessGroupMainRunController.class);

    // activity identifyers are used as menu user objects and for the user
    // activity events
    // change value with care, used in logfiles etc!!
    /** activity identitfyer: user selected overview in menu * */
    public static final String ACTIVITY_MENUSELECT_OVERVIEW = "MENU_OVERVIEW";
    /** activity identitfyer: user selected information in menu * */
    public static final String ACTIVITY_MENUSELECT_INFORMATION = "MENU_INFORMATION";
    /** activity identitfyer: user selected memberlist in menu * */
    public static final String ACTIVITY_MENUSELECT_MEMBERSLIST = "MENU_MEMBERLIST";
    /** activity identitfyer: user selected contactform in menu * */
    public static final String ACTIVITY_MENUSELECT_CONTACTFORM = "MENU_CONTACTFORM";
    /** activity identitfyer: user selected forum in menu * */
    public static final String ACTIVITY_MENUSELECT_FORUM = "MENU_FORUM";
    /** activity identitfyer: user selected folder in menu * */
    public static final String ACTIVITY_MENUSELECT_FOLDER = "MENU_FOLDER";
    /** activity identitfyer: user selected chat in menu * */
    public static final String ACTIVITY_MENUSELECT_CHAT = "MENU_CHAT";
    /** activity identitfyer: user selected calendar in menu * */
    public static final String ACTIVITY_MENUSELECT_CALENDAR = "MENU_CALENDAR";
    /** activity identitfyer: user selected administration in menu * */
    public static final String ACTIVITY_MENUSELECT_ADMINISTRATION = "MENU_ADMINISTRATION";
    /** activity identitfyer: user selected show resources in menu * */
    public static final String ACTIVITY_MENUSELECT_SHOW_RESOURCES = "MENU_SHOW_RESOURCES";
    public static final String ACTIVITY_MENUSELECT_WIKI = "MENU_SHOW_WIKI";
    /* activity identitfyer: user selected show portoflio in menu */
    public static final String ACTIVITY_MENUSELECT_PORTFOLIO = "MENU_SHOW_PORTFOLIO";

    private final Panel mainPanel;
    private final VelocityContainer main;
    private VelocityContainer vc_sendToChooserForm, resourcesVC;
    private final Identity identity;
    private final PackageTranslator resourceTrans;

    private BusinessGroup businessGroup;

    private final MenuTree bgTree;
    private final LayoutMain3ColsController columnLayoutCtr;
    private final Panel all;

    private Controller collabToolCtr;
    private Controller chatCtr;

    private BusinessGroupEditController bgEditCntrllr;
    private TableController resourcesCtr;

    private BusinessGroupSendToChooserForm sendToChooserForm;

    private GroupController gownersC;
    private GroupController gparticipantsC;
    private GroupController waitingListController;

    private final boolean isAdmin;

    private final BGConfigFlags flags;

    private BusinessGroupPropertyManager bgpm;
    private final UserSession userSession;
    private String adminNodeId; // reference to admin menu item

    // not null indicates tool is enabled
    private GenericTreeNode nodeFolder;
    private GenericTreeNode nodeForum;
    private GenericTreeNode nodeWiki;
    private GenericTreeNode nodeCal;
    private GenericTreeNode nodePortfolio;
    private boolean groupRunDisabled;
    private final OLATResourceable assessmentEventOres;
    private BusinessGroupService businessGroupService;

    private ProjectController projectController;

    /**
     * Do not use this constructor! Use the BGControllerFactory instead!
     * 
     * @param ureq
     * @param control
     * @param currBusinessGroup
     * @param flags
     * @param initialViewIdentifier
     *            supported are null, "toolforum", "toolfolder"
     */
    public BusinessGroupMainRunController(final UserRequest ureq, final WindowControl control, final BusinessGroup currBusinessGroup, final BGConfigFlags flags,
            final String initialViewIdentifier) {
        super(ureq, control);
        addLoggingResourceable(LoggingResourceable.wrap(currBusinessGroup));
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OPEN, getClass());
        this.bgpm = new BusinessGroupPropertyManager(currBusinessGroup);
        this.flags = flags;
        this.businessGroup = currBusinessGroup;
        this.identity = ureq.getIdentity();
        this.userSession = ureq.getUserSession();
        this.assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);

        this.isAdmin = getBusinessGroupEBL().isGroupManagementAdmin(ureq.getIdentity(), businessGroup, flags);

        // Initialize translator:
        // package translator with default group fallback translators and type
        // translator
        setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, currBusinessGroup.getType(), ureq.getLocale()));
        this.resourceTrans = new PackageTranslator(PackageUtil.getPackageName(RepositoryTableModel.class), ureq.getLocale(), getTranslator());

        // main component layed out in panel
        main = createVelocityContainer("bgrun");
        exposeGroupDetailsToVC(currBusinessGroup);

        mainPanel = new Panel("p_buddygroupRun");
        mainPanel.setContent(main);
        //
        bgTree = new MenuTree("bgTree");
        final TreeModel trMdl = buildTreeModel();
        bgTree.setTreeModel(trMdl);
        bgTree.addListener(this);
        //
        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), bgTree, null, mainPanel, "grouprun");
        listenTo(columnLayoutCtr); // cleanup on dispose

        //
        all = putInitialPanel(columnLayoutCtr.getInitialComponent());
        // register for AssessmentEvents triggered by this user
        userSession.getSingleUserEventCenter().registerFor(this, userSession.getIdentity(), assessmentEventOres);
        /*
         * lastUsage, update lastUsage if group is run if you can acquire the lock on the group for a very short time. If this is not possible, then the lastUsage is
         * already up to date within one-day-precision.
         */
        businessGroupService = (BusinessGroupService) CoreSpringFactory.getBean(BusinessGroupService.class);
        businessGroupService.setLastUsageFor(currBusinessGroup);

        // disposed message controller
        // must be created beforehand
        final Panel empty = new Panel("empty");// empty panel set as "menu" and
                                               // "tool"
        final Controller disposedBusinessGroup = new DisposedBusinessGroup(ureq, getWindowControl());
        final LayoutMain3ColsController disposedController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, empty,
                disposedBusinessGroup.getInitialComponent(), "disposed grouprun");
        disposedController.addDisposableChildController(disposedBusinessGroup);
        setDisposedMsgController(disposedController);

        // add as listener to BusinessGroup so we are being notified about
        // changes.
        CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), currBusinessGroup);

        // show disabled message when collaboration is disabled (e.g. in a test)
        if (AssessmentEvent.isAssessmentStarted(ureq.getUserSession())) {
            groupRunDisabled = true;
            this.showError("grouprun.disabled");
        }

        // REVIEW:PB:2009-05-31: consolidate ContextEntry <->
        // initialViewIdentifier Concept -> go for ContextEntry at the end.
        // first step -> if initialViewIdentifier != null -> map
        // initialViewIdentifier to ore with OresHelper
        // how to remove initialViewIdentifier and replace by ContextEntry Path?

        // jump to either the forum or the folder if the business-launch-path
        // says so.
        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            final OLATResourceable ores = ce.getOLATResourceable();
            if (OresHelper.equals(ores, ORES_TOOLFORUM)) {
                // start the forum
                if (nodeForum != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_FORUM);
                    bgTree.setSelectedNode(nodeForum);
                } else { // not enabled
                    final String text = translate("warn.forumnotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (OresHelper.equals(ores, ORES_TOOLFOLDER)) {
                if (nodeFolder != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_FOLDER);
                    bgTree.setSelectedNode(nodeFolder);
                } else { // not enabled
                    final String text = translate("warn.foldernotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (OresHelper.equals(ores, ORES_TOOLWIKI)) {
                if (nodeWiki != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_WIKI);
                    bgTree.setSelectedNode(nodeWiki);
                } else { // not enabled
                    final String text = translate("warn.wikinotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (OresHelper.equals(ores, ORES_TOOLCAL)) {
                if (nodeCal != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_CALENDAR);
                    bgTree.setSelectedNode(nodeCal);
                } else { // not enabled
                    final String text = translate("warn.calnotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (OresHelper.equals(ores, ORES_TOOLPORTFOLIO)) {
                if (nodePortfolio != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_PORTFOLIO);
                    bgTree.setSelectedNode(nodePortfolio);
                } else { // not enabled
                    final String text = translate("warn.portfolionotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            }
        }

        // jump to node if initialViewIdent says so.
        if (initialViewIdentifier != null) {
            if (initialViewIdentifier.equals(INITVIEW_TOOLFORUM)) {
                if (nodeForum != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_FORUM);
                    bgTree.setSelectedNode(nodeForum);
                } else { // not enabled
                    final String text = translate("warn.forumnotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (initialViewIdentifier.equals(INITVIEW_TOOLFOLDER)) {
                if (nodeFolder != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_FOLDER);
                    bgTree.setSelectedNode(nodeFolder);
                } else { // not enabled
                    final String text = translate("warn.foldernotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (initialViewIdentifier.equals(INITVIEW_TOOLWIKI)) {
                if (nodeWiki != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_WIKI);
                    bgTree.setSelectedNode(nodeWiki);
                } else { // not enabled
                    final String text = translate("warn.wikinotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else if (initialViewIdentifier.equals(INITVIEW_TOOLCAL)) {
                if (nodeCal != null) {
                    handleTreeActions(ureq, ACTIVITY_MENUSELECT_CALENDAR);
                    bgTree.setSelectedNode(nodeCal);
                } else { // not enabled
                    final String text = translate("warn.calnotavailable");
                    final Controller mc = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
                    listenTo(mc); // cleanup on dispose
                    mainPanel.setContent(mc.getInitialComponent());
                }
            } else {
                throw new AssertException("unknown initialViewIdentifier:'" + initialViewIdentifier + "'");
            }
        }
    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private void exposeGroupDetailsToVC(final BusinessGroup currBusinessGroup) {
        main.contextPut("BuddyGroup", currBusinessGroup);
        main.contextPut("hasOwners", new Boolean(flags.isEnabled(BGConfigFlags.GROUP_OWNERS)));
    }

    /**
     * org.olat.presentation.framework.components.Component, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // events from menutree
        if (source == bgTree) { // user chose news, contactform, forum, folder
                                // or
            // administration
            if (!groupRunDisabled && event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                final TreeNode selTreeNode = bgTree.getSelectedNode();
                final String cmd = (String) selTreeNode.getUserObject();
                handleTreeActions(ureq, cmd);
            } else if (groupRunDisabled) {
                handleTreeActions(ureq, ACTIVITY_MENUSELECT_OVERVIEW);
                this.showError("grouprun.disabled");
            }
        }
    }

    /**
     * org.olat.presentation.framework.control.Controller, org.olat.system.event.control.Event)
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == collabToolCtr) {
            if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.BACK_EVENT || event == Event.FAILED_EVENT) {
                // In all cases (success or failure) we
                // go back to the group overview page.
                bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
                mainPanel.setContent(main);
            }
        } else if (source == bgEditCntrllr) {
            // changes from the admin controller
            if (event == Event.CHANGED_EVENT) {
                final TreeModel trMdl = buildTreeModel();
                bgTree.setTreeModel(trMdl);
            } else if (event == Event.CANCELLED_EVENT) {
                // could not get lock on business group, back to inital screen
                bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
                mainPanel.setContent(main);
            }

        } else if (source == resourcesCtr) {
            if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                final RepositoryTableModel repoTableModel = (RepositoryTableModel) resourcesCtr.getTableDataModel();
                final RepositoryEntry currentRepoEntry = (RepositoryEntry) repoTableModel.getObject(rowid);
                if (actionid.equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
                    final OLATResource ores = currentRepoEntry.getOlatResource();
                    if (ores == null) {
                        throw new AssertException("repoEntry had no olatresource, repoKey = " + currentRepoEntry.getKey());
                    }

                    addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.genRepoEntry));

                    final String title = currentRepoEntry.getDisplayname();
                    final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
                    final Controller ctrl = ControllerFactory.createLaunchController(ores, null, ureq, dts.getWindowControl(), true);
                    DynamicTabHelper.openRepoEntryTab(currentRepoEntry, ureq, ctrl, title, null);
                }
            }
        } else if (source == sendToChooserForm) {
            if (event == Event.DONE_EVENT) {
                removeAsListenerAndDispose(collabToolCtr);
                collabToolCtr = createContactFormController(ureq);
                listenTo(collabToolCtr);
                mainPanel.setContent(collabToolCtr.getInitialComponent());
            } else if (event == Event.CANCELLED_EVENT) {
                // back to group overview
                bgTree.setSelectedNodeId(bgTree.getTreeModel().getRootNode().getIdent());
                mainPanel.setContent(main);
            }
        }

    }

    /**
     * generates the email adress list.
     * 
     * @param ureq
     * @return a contact form controller for this group
     */
    private ContactFormController createContactFormController(final UserRequest ureq) {
        final BaseSecurity scrtMngr = getBaseSecurity();

        Identity fromIdentity = ureq.getIdentity();

        SecurityGroup owners = businessGroup.getOwnerGroup();
        SecurityGroup participants = businessGroup.getPartipiciantGroup();
        SecurityGroup waitingList = businessGroup.getWaitingGroup();

        boolean sendToGroupOwnersIsConfigured = flags.isEnabled(BGConfigFlags.GROUP_OWNERS);
        boolean isUserRoleAdminAndWaitingListEnabled = isAdmin && businessGroup.getWaitingListEnabled().booleanValue();

        GroupParameter ownerGroupParameter = createOwnerGroupParameter(sendToGroupOwnersIsConfigured, scrtMngr, owners);
        GroupParameter participantGroupParameter = createParticipantGroupParameter(scrtMngr, participants);
        GroupParameter waitingGroupParameter = createWaitingListParameter(isUserRoleAdminAndWaitingListEnabled, scrtMngr, waitingList);

        BusinessGroupSendToChooserFormUIModel sendToChooseFormUIModel = new BusinessGroupSendToChooserFormUIModel(fromIdentity, ownerGroupParameter,
                participantGroupParameter, waitingGroupParameter);
        ContactMessage contactMessage = sendToChooseFormUIModel.getContactMessage();

        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
        final ContactFormController cofocntrllr = collabTools.createContactFormController(ureq, getWindowControl(), contactMessage);
        return cofocntrllr;
    }

    private BusinessGroupSendToChooserFormUIModel.GroupParameter createWaitingListParameter(boolean isUserRoleAdminAndWaitingListEnabled, final BaseSecurity scrtMngr,
            final SecurityGroup waitingList) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter waitingGroupParameter = null;
        if (isUserRoleAdminAndWaitingListEnabled) {
            boolean sendToAllOfWaitingListIsChoosen = sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
            boolean sendToSelectedWaitingListMembers = sendToChooserForm.waitingListChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
            List<Long> selectedWaitingListMemberKeys = sendToChooserForm.getSelectedWaitingKeys();
            final List<Identity> waitingListIdentities = scrtMngr.getIdentitiesOfSecurityGroup(waitingList);

            if (sendToAllOfWaitingListIsChoosen) {
                waitingGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(waitingListIdentities,
                        translate("sendtochooser.form.radio.waitings.all"));
            } else if (sendToSelectedWaitingListMembers) {
                waitingGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(waitingListIdentities, selectedWaitingListMemberKeys,
                        translate("sendtochooser.form.radio.waitings.choose"));
            }
        }
        return waitingGroupParameter;
    }

    private BusinessGroupSendToChooserFormUIModel.GroupParameter createParticipantGroupParameter(final BaseSecurity scrtMngr, final SecurityGroup participants) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter participantGroupParameter = null;
        boolean sendToAllParticipantsIsChoose = sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
        boolean sendToSelectedParticipants = sendToChooserForm.participantChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
        List<Long> selectedParticipantKeys = sendToChooserForm.getSelectedPartipKeys();
        final List<Identity> participantsList = scrtMngr.getIdentitiesOfSecurityGroup(participants);

        if (sendToAllParticipantsIsChoose) {
            participantGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(participantsList, translate("sendtochooser.form.radio.partip.all"));
        } else if (sendToSelectedParticipants) {
            participantGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(participantsList, selectedParticipantKeys,
                    translate("sendtochooser.form.radio.partip.choose"));
        }
        return participantGroupParameter;
    }

    private BusinessGroupSendToChooserFormUIModel.GroupParameter createOwnerGroupParameter(boolean sendToGroupOwnersIsConfigured, final BaseSecurity scrtMngr,
            final SecurityGroup owners) {
        BusinessGroupSendToChooserFormUIModel.GroupParameter ownerGroupParameter = null;
        if (sendToGroupOwnersIsConfigured) {
            boolean sendToAllGroupOwnersIsChoosen = sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_ALL);
            boolean sendToSelectedGroupOwners = sendToChooserForm.ownerChecked().equals(BusinessGroupSendToChooserForm.NLS_RADIO_CHOOSE);
            List<Long> selectedGroupOwnerKeys = sendToChooserForm.getSelectedOwnerKeys();
            final List<Identity> ownerList = scrtMngr.getIdentitiesOfSecurityGroup(owners);

            if (sendToAllGroupOwnersIsChoosen) {
                ownerGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(ownerList, translate("sendtochooser.form.radio.owners.all"));
            } else if (sendToSelectedGroupOwners) {
                ownerGroupParameter = new BusinessGroupSendToChooserFormUIModel.GroupParameter(ownerList, selectedGroupOwnerKeys,
                        translate("sendtochooser.form.radio.owners.choose"));
            }
        }
        return ownerGroupParameter;
    }

    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    /**
     * handles the different tree actions
     * 
     * @param ureq
     * @param cmd
     */
    private void handleTreeActions(final UserRequest ureq, final String cmd) {
        // release edit lock if available
        removeAsListenerAndDispose(bgEditCntrllr);

        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);

        // dispose current tool controller if available except for IM which
        // should be available even while changing collabtool
        if (collabToolCtr instanceof InstantMessagingGroupChatController) {
            //
        } else {
            removeAsListenerAndDispose(collabToolCtr);
        }
        // init new controller according to user click
        if (ACTIVITY_MENUSELECT_OVERVIEW.equals(cmd)) {
            // root node clicked display overview
            mainPanel.setContent(main);
        } else if (ACTIVITY_MENUSELECT_FORUM.equals(cmd)) {
            addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLFORUM, OlatResourceableType.forum));
            // final SubscriptionContext sc = new SubscriptionContext(businessGroup, INITVIEW_TOOLFORUM);
            SubscriptionContext noNotificationSubscriptionContext = null;
            WindowControl bwControl = getWindowControl();
            // calculate the new businesscontext for the forum clicked
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLFORUM);
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, bwControl);

            collabToolCtr = collabTools
                    .createForumController(ureq, bwControl, isAdmin, ureq.getUserSession().getRoles().isGuestOnly(), noNotificationSubscriptionContext);
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_CHAT.equals(cmd)) {

            if (chatCtr != null) {
                collabToolCtr = chatCtr;
            } else {
                collabToolCtr = collabTools.createChatController(ureq, getWindowControl(), this.businessGroup);
                chatCtr = collabToolCtr;
            }

            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_CALENDAR.equals(cmd)) {
            addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLCAL, OlatResourceableType.calendar));

            WindowControl bwControl = getWindowControl();
            // calculate the new businesscontext for the forum clicked
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLCAL);
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, bwControl);

            collabToolCtr = collabTools.createCalendarController(ureq, bwControl, this.businessGroup, isAdmin);
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_INFORMATION.equals(cmd)) {
            collabToolCtr = collabTools.createNewsController(ureq, getWindowControl());
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_FOLDER.equals(cmd)) {
            addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLFOLDER, OlatResourceableType.sharedFolder));

            final SubscriptionContext sc = null;// Switch off subcriptions in groups, old code : new SubscriptionContext(businessGroup, INITVIEW_TOOLFOLDER);

            WindowControl bwControl = getWindowControl();
            // calculate the new businesscontext for the forum clicked
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLFOLDER);
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, bwControl);

            collabToolCtr = collabTools.createFolderController(ureq, bwControl, sc);
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_MEMBERSLIST.equals(cmd)) {
            doShowMembers(ureq);
        } else if (ACTIVITY_MENUSELECT_CONTACTFORM.equals(cmd)) {
            doContactForm(ureq);
        } else if (ACTIVITY_MENUSELECT_ADMINISTRATION.equals(cmd)) {
            doAdministration(ureq);
        } else if (ACTIVITY_MENUSELECT_SHOW_RESOURCES.equals(cmd)) {
            doShowResources(ureq);
        } else if (ACTIVITY_MENUSELECT_WIKI.equals(cmd)) {
            addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLWIKI, OlatResourceableType.wiki));
            WindowControl bwControl = getWindowControl();
            // calculate the new businesscontext for the wiki clicked
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLWIKI);
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, bwControl);
            ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapWikiOres(ce.getOLATResourceable()));

            collabToolCtr = collabTools.createWikiController(ureq, bwControl);
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        } else if (ACTIVITY_MENUSELECT_PORTFOLIO.equals(cmd)) {
            addLoggingResourceable(LoggingResourceable.wrap(ORES_TOOLPORTFOLIO, OlatResourceableType.portfolio));
            WindowControl bwControl = getWindowControl();
            // calculate the new businesscontext for the wiki clicked
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ORES_TOOLPORTFOLIO);
            bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, bwControl);
            ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapPortfolioOres(ce.getOLATResourceable()));

            collabToolCtr = collabTools.createPortfolioController(ureq, bwControl, businessGroup);
            listenTo(collabToolCtr);
            mainPanel.setContent(collabToolCtr.getInitialComponent());
        }
    }

    private void doAdministration(final UserRequest ureq) {
        Project project = businessGroupService.getProjectForBusinessGroup(businessGroup.getKey());
        if (project != null) {
            removeAsListenerAndDispose(projectController);
            BusinessGroupProjectControllerFactory factory = new BusinessGroupProjectControllerFactoryImpl(ureq, getWindowControl(), project);
            projectController = factory.getProjectController();
            listenTo(projectController);
            mainPanel.setContent(projectController.getInitialComponent());
        } else {
            removeAsListenerAndDispose(bgEditCntrllr);
            bgEditCntrllr = BGControllerFactory.getInstance().createEditControllerFor(ureq, getWindowControl(), businessGroup);
            listenTo(bgEditCntrllr);
            mainPanel.setContent(bgEditCntrllr.getInitialComponent());
        }
    }

    private void doContactForm(final UserRequest ureq) {
        if (vc_sendToChooserForm == null) {
            vc_sendToChooserForm = createVelocityContainer("cosendtochooser");
        }
        removeAsListenerAndDispose(sendToChooserForm);
        sendToChooserForm = new BusinessGroupSendToChooserForm(ureq, getWindowControl(), businessGroup, isAdmin);
        listenTo(sendToChooserForm);
        vc_sendToChooserForm.put("vc_sendToChooserForm", sendToChooserForm.getInitialComponent());
        mainPanel.setContent(vc_sendToChooserForm);
    }

    private void doShowMembers(final UserRequest ureq) {
        final VelocityContainer membersVc = createVelocityContainer("ownersandmembers");
        // 1. show owners if configured with Owners
        if (flags.isEnabled(BGConfigFlags.GROUP_OWNERS) && bgpm.showOwners()) {
            removeAsListenerAndDispose(gownersC);
            gownersC = new GroupController(ureq, getWindowControl(), false, true, false, businessGroup.getOwnerGroup(), null);
            listenTo(gownersC);
            membersVc.put("owners", gownersC.getInitialComponent());
            membersVc.contextPut("showOwnerGroups", Boolean.TRUE);
        } else {
            membersVc.contextPut("showOwnerGroups", Boolean.FALSE);
        }
        // 2. show participants if configured with Participants
        if (bgpm.showPartips()) {
            removeAsListenerAndDispose(gparticipantsC);
            gparticipantsC = new GroupController(ureq, getWindowControl(), false, true, false, businessGroup.getPartipiciantGroup(), null);
            listenTo(gparticipantsC);

            membersVc.put("participants", gparticipantsC.getInitialComponent());
            membersVc.contextPut("showPartipsGroups", Boolean.TRUE);
        } else {
            membersVc.contextPut("showPartipsGroups", Boolean.FALSE);
        }
        // 3. show waiting-list if configured
        membersVc.contextPut("hasWaitingList", new Boolean(businessGroup.getWaitingListEnabled()));
        if (bgpm.showWaitingList()) {
            removeAsListenerAndDispose(waitingListController);
            waitingListController = new GroupController(ureq, getWindowControl(), false, true, false, businessGroup.getWaitingGroup(), null);
            listenTo(waitingListController);
            membersVc.put("waitingList", waitingListController.getInitialComponent());
            membersVc.contextPut("showWaitingList", Boolean.TRUE);
        } else {
            membersVc.contextPut("showWaitingList", Boolean.FALSE);
        }
        mainPanel.setContent(membersVc);
    }

    /**
	 */
    @Override
    protected void doDispose() {

        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CLOSED, getClass());

        if (chatCtr != null) {

            final InstantMessagingGroupChatController chat = (InstantMessagingGroupChatController) chatCtr;

            if (chat.isChatWindowOpen()) {
                chat.close();
                getWindowControl().getWindowBackOffice().sendCommandTo(chat.getCloseCommand());
            }

            removeAsListenerAndDispose(chatCtr);
        }

        CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, this.businessGroup);

        userSession.getSingleUserEventCenter().deregisterFor(this, assessmentEventOres);
    }

    /**
	 */
    @Override
    public void event(final Event event) {
        if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
            final OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
            if (!delEvent.targetEquals(businessGroup)) {
                throw new AssertException("receiving a delete event for a olatres we never registered for!!!:" + delEvent.getDerivedOres());
            }
            dispose();

        } else if (event instanceof BusinessGroupModifiedEvent) {
            final BusinessGroupModifiedEvent bgmfe = (BusinessGroupModifiedEvent) event;
            if (event.getCommand().equals(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT)) {
                // reset business group property manager
                this.bgpm = new BusinessGroupPropertyManager(this.businessGroup);
                // update reference to update business group object
                this.businessGroup = businessGroupService.loadBusinessGroup(this.businessGroup);
                main.contextPut("BuddyGroup", this.businessGroup);
                final TreeModel trMdl = buildTreeModel();
                bgTree.setTreeModel(trMdl);
                if (bgEditCntrllr == null) {
                    // change didn't origin by our own edit controller
                    showInfo(translate("grouprun.configurationchanged"));
                    bgTree.setSelectedNodeId(trMdl.getRootNode().getIdent());
                    mainPanel.setContent(main);
                } else {
                    // Activate edit menu item
                    bgTree.setSelectedNodeId(ACTIVITY_MENUSELECT_ADMINISTRATION);
                }
            } else if (bgmfe.wasMyselfRemoved(identity)) {
                // nothing more here!! The message will be created and displayed
                // upon disposing
                dispose();// disposed message controller will be set
            }
        } else if (event instanceof AssessmentEvent) {
            if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STARTED)) {
                groupRunDisabled = true;
            } else if (((AssessmentEvent) event).getEventType().equals(AssessmentEvent.TYPE.STOPPED)) {
                groupRunDisabled = false;
            }
        }
    }

    private void doShowResources(final UserRequest ureq) {
        // always refresh data model, maybe it has changed
        final RepositoryTableModel repoTableModel = new RepositoryTableModel(resourceTrans);
        final List<RepositoryEntry> repoTableModelEntries = getBgContextService().findRepositoryEntriesForBGContext(businessGroup.getGroupContext());
        repoTableModel.setObjects(repoTableModelEntries);
        // init table controller only once
        if (resourcesCtr == null) {
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setTableEmptyMessage(translate("resources.noresources"));
            // removeAsListenerAndDispose(resourcesCtr);
            resourcesCtr = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
            listenTo(resourcesCtr);

            resourcesVC = createVelocityContainer("resources");
            repoTableModel.addColumnDescriptors(resourcesCtr, translate("resources.launch"), false);
            resourcesVC.put("resources", resourcesCtr.getInitialComponent());
        }
        // add table model to table
        resourcesCtr.setTableDataModel(repoTableModel);
        mainPanel.setContent(resourcesVC);
    }

    /**
     * @return
     */
    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    /**
     * Activates the administration menu item. Make sure you have the rights to do this, otherwhise this will throw a nullpointer exception
     * 
     * @param ureq
     */
    public void activateAdministrationMode(final UserRequest ureq) {
        doAdministration(ureq);
        bgTree.setSelectedNodeId(adminNodeId);
    }

    /**
     * @return The menu tree model
     */
    private TreeModel buildTreeModel() {
        GenericTreeNode gtnChild, root;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(businessGroup.getName());
        root.setUserObject(ACTIVITY_MENUSELECT_OVERVIEW);
        root.setAltText(translate("menutree.top.alt") + " " + businessGroup.getName());
        root.setIconCssClass("b_group_icon");
        gtm.setRootNode(root);

        final CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(this.businessGroup);

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_NEWS)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.news"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_INFORMATION);
            gtnChild.setAltText(translate("menutree.news.alt"));
            gtnChild.setIconCssClass("o_news_icon");
            root.addChild(gtnChild);
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.calendar"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_CALENDAR);
            gtnChild.setAltText(translate("menutree.calendar.alt"));
            gtnChild.setIconCssClass("o_calendar_icon");
            root.addChild(gtnChild);
            nodeCal = gtnChild;
        }

        if (flags.isEnabled(BGConfigFlags.SHOW_RESOURCES)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.resources"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_SHOW_RESOURCES);
            gtnChild.setAltText(translate("menutree.resources.alt"));
            gtnChild.setIconCssClass("o_course_icon");
            root.addChild(gtnChild);
        }

        if ((flags.isEnabled(BGConfigFlags.GROUP_OWNERS) && bgpm.showOwners()) || bgpm.showPartips()) {
            // either owners or participants, or both are visible
            // otherwise the node is not visible
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.members"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_MEMBERSLIST);
            gtnChild.setAltText(translate("menutree.members.alt"));
            gtnChild.setIconCssClass("b_group_icon");
            root.addChild(gtnChild);
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_CONTACT)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.contactform"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_CONTACTFORM);
            gtnChild.setAltText(translate("menutree.contactform.alt"));
            gtnChild.setIconCssClass("o_co_icon");
            root.addChild(gtnChild);
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.folder"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_FOLDER);
            gtnChild.setAltText(translate("menutree.folder.alt"));
            gtnChild.setIconCssClass("o_bc_icon");
            root.addChild(gtnChild);
            nodeFolder = gtnChild;
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.forum"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_FORUM);
            gtnChild.setAltText(translate("menutree.forum.alt"));
            gtnChild.setIconCssClass("o_fo_icon");
            root.addChild(gtnChild);
            nodeForum = gtnChild;
        }

        if (InstantMessagingModule.isEnabled() && collabTools.isToolEnabled(CollaborationTools.TOOL_CHAT)
                && (!businessGroup.getType().equals(BusinessGroup.TYPE_LEARNINGROUP) || InstantMessagingModule.isSyncLearningGroups() // whether
                // LearningGroups
                // can have
                // chat or not
                )) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.chat"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_CHAT);
            gtnChild.setAltText(translate("menutree.chat.alt"));
            gtnChild.setIconCssClass("o_chat_icon");
            root.addChild(gtnChild);
        }

        if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI)) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.wiki"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_WIKI);
            gtnChild.setAltText(translate("menutree.wiki.alt"));
            gtnChild.setIconCssClass("o_wiki_icon");
            root.addChild(gtnChild);
            nodeWiki = gtnChild;
        }

        PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
        if (collabTools.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO) && portfolioModule.isEnabled()) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.portfolio"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_PORTFOLIO);
            gtnChild.setAltText(translate("menutree.portfolio.alt"));
            gtnChild.setIconCssClass("o_ep_icon");
            root.addChild(gtnChild);
            nodePortfolio = gtnChild;
        }

        if (isAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translate("menutree.administration"));
            gtnChild.setUserObject(ACTIVITY_MENUSELECT_ADMINISTRATION);
            gtnChild.setIdent(ACTIVITY_MENUSELECT_ADMINISTRATION);
            gtnChild.setAltText(translate("menutree.administration.alt"));
            gtnChild.setIconCssClass("o_admin_icon");
            root.addChild(gtnChild);
            adminNodeId = gtnChild.getIdent();
        }

        return gtm;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return !isDisposed();
    }

}
