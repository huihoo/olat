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

package org.olat.presentation.group.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.group.BusinessGroup;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.activitylogging.ThreadLocalUserActivityLogger;
import org.olat.lms.commons.change.ChangeManager;
import org.olat.lms.commons.tree.TreeHelper;
import org.olat.lms.group.BGConfigFlags;
import org.olat.lms.group.BusinessGroupEBL;
import org.olat.lms.group.BusinessGroupService;
import org.olat.lms.group.GroupLoggingAction;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.table.BooleanColumnDescriptor;
import org.olat.presentation.framework.core.components.table.DefaultColumnDescriptor;
import org.olat.presentation.framework.core.components.table.Table;
import org.olat.presentation.framework.core.components.table.TableController;
import org.olat.presentation.framework.core.components.table.TableEvent;
import org.olat.presentation.framework.core.components.table.TableGuiConfiguration;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeEvent;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.BGTranslatorFactory;
import org.olat.presentation.group.BusinessGroupFormController;
import org.olat.presentation.group.delete.TabbedPaneController;
import org.olat.presentation.group.run.BusinessGroupMainRunController;
import org.olat.system.commons.StringHelper;
import org.olat.system.event.Event;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description: <br>
 * Controller to list all groups where the user is owner or participant. This controller does also feature create and delete methods for groups of type buddyGroup <br>
 * 
 * <pre>
 *  Possible activation messages:
 *  &quot;cmd.menu.index&quot; : show groups overview
 *  &quot;cmd.menu.buddy&quot; : show all buddy groups 
 *  &quot;cmd.menu.learn&quot; : show all leanringgroups 
 *  &quot;cmd.menu.right&quot; : show all right groups
 *  &quot;addBuddyGroup&quot; : start add group workflow
 * </pre>
 * <P>
 * Initial Date: Aug 5, 2004
 * 
 * @author patrick
 */

public class BGMainController extends MainLayoutBasicController implements Activateable {

    private static final Logger log = LoggerHelper.getLogger();
    private static final String PACKAGE = PackageUtil.getPackageName(BGMainController.class);
    /*
     * things a controller needs during its lifetime
     */
    private final VelocityContainer main;
    private final LayoutMain3ColsController columnLayoutCtr;
    private final ToolController mainToolC;
    private final MenuTree menuTree;

    private static final String ACTION_ADD_BUDDYGROUP = "addBuddyGroup";
    private static final String ACTION_DELETE_UNUSEDGROUP = "deleteunusedgroup";

    private TableController groupListCtr;
    private BusinessGroupTableModelWithType groupListModel;
    private BusinessGroupFormController createBuddyGroupController;
    private BusinessGroup currBusinessGroup;
    private final Identity identity;
    private TabbedPaneController deleteTabPaneCtr;
    private CloseableModalController cmc;
    private DialogBoxController deleteDialogBox;
    private DialogBoxController leaveDialogBox;

    private BusinessGroupService businessGroupService;

    // group list table rows
    private static final String TABLE_ACTION_LEAVE = "bgTblLeave";
    private static final String TABLE_ACTION_DELETE = "bgTblDelete";
    private static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
    private static final String CMD_MENU_INDEX = "cmd.menu.index";
    private static final String CMD_MENU_BUDDY = "cmd.menu.buddy";
    private static final String CMD_MENU_LEARN = "cmd.menu.learn";
    private static final String CMD_MENU_RIGHT = "cmd.menu.right";

    /**
     * @param ureq
     * @param wControl
     * @param flags
     *            configuration flags
     * @param initialViewIdentifier
     */
    public BGMainController(final UserRequest ureq, final WindowControl wControl, final String initialViewIdentifier) {
        super(ureq, wControl);

        identity = ureq.getIdentity();
        setTranslator(BGTranslatorFactory.createBGPackageTranslator(PACKAGE, BusinessGroup.TYPE_BUDDYGROUP, ureq.getLocale()));
        businessGroupService = CoreSpringFactory.getBean(BusinessGroupService.class);

        // main component layed out in panel
        main = createVelocityContainer("index");
        // toolboxes
        mainToolC = ToolFactory.createToolController(getWindowControl());
        listenTo(mainToolC);
        mainToolC.addHeader(translate("tools.add.header"));
        mainToolC.addLink(ACTION_ADD_BUDDYGROUP, translate("tools.add.buddygroup"));
        if (ureq.getUserSession().getRoles().isOLATAdmin()) {
            mainToolC.addHeader(translate("tools.delete.header"));
            mainToolC.addLink(ACTION_DELETE_UNUSEDGROUP, translate("tools.delete.unusedgroup"));
        }
        // menu
        menuTree = new MenuTree("buddyGroupTree");
        menuTree.setTreeModel(buildTreeModel());
        menuTree.setSelectedNodeId(menuTree.getTreeModel().getRootNode().getIdent());
        menuTree.addListener(this);
        // layout
        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainToolC.getInitialComponent(), main, "groumain");
        columnLayoutCtr.addCssClassToMain("o_groups");

        listenTo(columnLayoutCtr);
        putInitialPanel(columnLayoutCtr.getInitialComponent());

        // start with list of all groups
        doAllGroupList(ureq, getWindowControl());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == menuTree) {
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                final TreeEvent te = (TreeEvent) event;
                final TreeNode clickedNode = menuTree.getTreeModel().getNodeById(te.getNodeId());
                final Object userObject = clickedNode.getUserObject();
                activateContent(ureq, userObject);
            }
        }
    }

    /**
     * Activate the content in the content area based on a user object representing the identifyer of the content
     * 
     * @param ureq
     * @param userObject
     */
    private void activateContent(final UserRequest ureq, final Object userObject) {
        if (userObject.equals(CMD_MENU_INDEX)) {
            doAllGroupList(ureq, getWindowControl());
        } else if (userObject.equals(CMD_MENU_BUDDY)) {
            doBuddyGroupList(ureq, getWindowControl());
        } else if (userObject.equals(CMD_MENU_LEARN)) {
            doLearningGroupList(ureq, getWindowControl());
        } else if (userObject.equals(CMD_MENU_RIGHT)) {
            doRightGroupList(ureq, getWindowControl());
        }
        setState(userObject.toString());
    }

    @Override
    protected void adjustState(final ControllerState cstate, final UserRequest ureq) {
        final String cmd = cstate.getSerializedState();
        activateContent(ureq, cmd);
        // adjust the menu
        final TreeNode rootNode = this.menuTree.getTreeModel().getRootNode();
        final TreeNode activatedNode = TreeHelper.findNodeByUserObject(cmd, rootNode);
        this.menuTree.setSelectedNode(activatedNode);
    }

    /**
     * @param ureq
     * @param event
     */
    private void handleEventsGroupTables(final UserRequest ureq, final Event event) {
        if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
            final TableEvent te = (TableEvent) event;
            final String actionid = te.getActionId();
            final int rowid = te.getRowId();
            currBusinessGroup = groupListModel.getBusinessGroupAt(rowid);
            final String trnslP = StringHelper.escapeHtml(currBusinessGroup.getName());

            if (actionid.equals(TABLE_ACTION_LAUNCH)) {
                if (businessGroupService.isIdentityInBusinessGroup(identity, currBusinessGroup)) {
                    BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), false, null);
                } else {
                    showWarning("table.action.run.notInGroup");
                }
            } else if (actionid.equals(TABLE_ACTION_DELETE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
                // only for buddygroups allowed
                deleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.delete.text", trnslP), deleteDialogBox);
            } else if (actionid.equals(TABLE_ACTION_LEAVE) && currBusinessGroup.getType().equals(BusinessGroup.TYPE_BUDDYGROUP)) {
                // only for buddygroups allowed
                leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", trnslP), leaveDialogBox);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == groupListCtr) {
            // an action from the groupList was clicked
            // e.g. LEAVE, DELETE, LAUNCH
            handleEventsGroupTables(ureq, event);
        } else if (source == mainToolC) {
            if (event.getCommand().startsWith(ACTION_ADD_BUDDYGROUP)) {
                initAddBuddygroupWorkflow(ureq);
            } else if (event.getCommand().startsWith(ACTION_DELETE_UNUSEDGROUP)) {
                initDeleteGroupWorkflow(ureq);
            }
        } else if (source == deleteDialogBox) {
            if (DialogBoxUIFactory.isOkEvent(event)) {
                doBuddyGroupDelete(ureq);
            }// else cancel was clicked or box closed
        } else if (source == leaveDialogBox) {
            if (event != Event.CANCELLED_EVENT) {
                if (DialogBoxUIFactory.isYesEvent(event)) {
                    doBuddyGroupLeave(ureq);
                }
            }// else dialog was simply closed
        } else if (source == this.createBuddyGroupController) {
            this.cmc.deactivate(); // remove modal dialog
            removeAsListenerAndDispose(this.cmc);
            if (event == Event.DONE_EVENT) {
                // create new buddy group with the specified values
                // values are taken from the createBuddyGroupForm
                this.currBusinessGroup = createBuddyGroup(ureq);
                updateGroupListModelAll();
                ChangeManager.changed(ChangeManager.ACTION_CREATE, this.currBusinessGroup);

                // after successfully creating a buddygroup 'launch' it
                final BusinessGroupMainRunController groupRunCtr = BGControllerFactory.getInstance().createRunControllerAsTopNavTab(this.currBusinessGroup, ureq,
                        getWindowControl(), false, null);
                if (groupRunCtr != null) {
                    groupRunCtr.activateAdministrationMode(ureq);
                }
            } else if (event == Event.FAILED_EVENT) {
                this.cmc = new CloseableModalController(getWindowControl(), translate("close"), this.createBuddyGroupController.getInitialComponent(), true,
                        translate("create.form.title"));
                this.cmc.activate();
                listenTo(this.cmc);
            } else if (event == Event.CANCELLED_EVENT) {
                // notthing to do
            }
        }
    }

    private void initDeleteGroupWorkflow(final UserRequest ureq) {
        removeAsListenerAndDispose(deleteTabPaneCtr);
        deleteTabPaneCtr = new TabbedPaneController(ureq, getWindowControl());
        listenTo(deleteTabPaneCtr);
        main.setPage(PackageUtil.getPackageVelocityRoot(this.getClass()) + "/delete.html");
        main.put("deleteTabs", deleteTabPaneCtr.getInitialComponent());
    }

    /**
	 * 
	 */
    private void initAddBuddygroupWorkflow(final UserRequest ureq) {
        final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();

        if (this.createBuddyGroupController != null) {
            removeAsListenerAndDispose(this.createBuddyGroupController);
        }
        this.createBuddyGroupController = new BusinessGroupFormController(ureq, getWindowControl(), null, flags.isEnabled(BGConfigFlags.GROUP_MINMAX_SIZE));
        listenTo(this.createBuddyGroupController);
        this.cmc = new CloseableModalController(getWindowControl(), translate("close"), this.createBuddyGroupController.getInitialComponent(), true,
                translate("create.form.title"));
        this.cmc.activate();
        listenTo(this.cmc);
    }

    private void doBuddyGroupDelete(final UserRequest ureq) {
        // 1) send notification mails to users
        String ownersListTitle = translate("userlist.owners.title");
        String participantsListTitle = translate("userlist.participants.title");

        boolean isDeleted = getBusinessGroupEBL().deleteBuddyGroup(ureq.getIdentity(), currBusinessGroup, ownersListTitle, participantsListTitle);
        if (!isDeleted) {
            log.warn("User tried to delete a group but he was not owner of the group", null);
            return;
        }

        // do Logging
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), LoggingResourceable.wrap(currBusinessGroup));
        // 4) update Tables
        doAllGroupList(ureq, getWindowControl());

        showInfo("info.group.deleted");
    }

    private void doBuddyGroupLeave(final UserRequest ureq) {
        boolean cannotRemoveLastOwner = getBusinessGroupEBL().leaveBuddyGroup(ureq.getIdentity(), currBusinessGroup);
        if (cannotRemoveLastOwner) {
            getWindowControl().setError(translate("msg.atleastone"));
            return;
        }
        // update model
        updateGroupListModelAll();

        // update Tables
        doAllGroupList(ureq, getWindowControl());
    }

    /**
     * @return
     */
    private BusinessGroupEBL getBusinessGroupEBL() {
        return CoreSpringFactory.getBean(BusinessGroupEBL.class);
    }

    /**
     * Creates a new business group of type buddy group and adds this.identity as owner to the new group.
     * 
     * @return BusinessGroup
     */
    private BusinessGroup createBuddyGroup(final UserRequest ureq) {
        final String bgName = this.createBuddyGroupController.getGroupName();
        final String bgDesc = this.createBuddyGroupController.getGroupDescription();
        final Integer bgMin = this.createBuddyGroupController.getGroupMin();
        final Integer bgMax = this.createBuddyGroupController.getGroupMax();
        /*
         * this creates a BusinessGroup as BuddyGroup with the specified name and description and also the CollaborationTools are enabled during creation. The
         * GroupContext is null in the case of BuddyGroups.
         */
        final BusinessGroup newGroup = businessGroupService.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, identity, bgName, bgDesc, bgMin, bgMax, null,
                null, null);
        // create buddylist for group
        // 2. Add user to group, fire events, do loggin etc.
        final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
        // do Logging
        ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(newGroup));
        ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CREATED, getClass());
        businessGroupService.addOwnerAndFireEvent(ureq.getIdentity(), ureq.getIdentity(), newGroup, flags, true);
        return newGroup;
    }

    /**
     * Prepare everything and show all groups
     * 
     * @param ureq
     * @param wControl
     */
    private void doAllGroupList(final UserRequest ureq, final WindowControl wControl) {
        // 1) initialize list controller and datamodel
        initGroupListCtrAndModel(true, ureq);
        // 2) load data into model
        updateGroupListModelAll();
        // 3) set correct page
        main.setPage(PackageUtil.getPackageVelocityRoot(this.getClass()) + "/index.html");
        // 4) update toolboxe
        columnLayoutCtr.hideCol2(false);
    }

    /**
     * Prepare everything and show all buddy groups
     * 
     * @param ureq
     * @param wControl
     */
    private void doBuddyGroupList(final UserRequest ureq, final WindowControl wControl) {
        // 1) initialize list controller and datamodel
        initGroupListCtrAndModel(true, ureq);
        // 2) load data into model
        updateGroupListModelBuddygroups();
        // 3) set correct page
        main.setPage(PackageUtil.getPackageVelocityRoot(this.getClass()) + "/buddy.html");
        // 4) update toolboxe
        columnLayoutCtr.hideCol2(false);
    }

    /**
     * Prepare everything and show all learning groups
     * 
     * @param ureq
     * @param wControl
     */
    private void doLearningGroupList(final UserRequest ureq, final WindowControl wControl) {
        // 1) initialize list controller and datamodel
        initGroupListCtrAndModel(false, ureq);
        // 2) load data into model
        updateGroupListModelLearninggroups();
        // 3) set correct page
        main.setPage(PackageUtil.getPackageVelocityRoot(this.getClass()) + "/learning.html");
        // 4) update toolboxe
        columnLayoutCtr.hideCol2(true);
    }

    /**
     * Prepare everything and show all right groups
     * 
     * @param ureq
     * @param wControl
     */
    private void doRightGroupList(final UserRequest ureq, final WindowControl wControl) {
        // 1) initialize list controller and datamodel
        initGroupListCtrAndModel(false, ureq);
        // 2) load data into model
        updateGroupListModelRightgroups();
        // 3) set correct page
        main.setPage(PackageUtil.getPackageVelocityRoot(this.getClass()) + "/right.html");
        // 4) update toolboxe
        columnLayoutCtr.hideCol2(true);
    }

    /**
     * Initialize the group list controller and the group list model given.
     * 
     * @param withLeaveAndDelete
     *            config flag: true: leave and delete button are showed, false: not showed
     * @param ureq
     */
    private void initGroupListCtrAndModel(final boolean withLeaveAndDelete, final UserRequest ureq) {
        // 1) init listing controller
        final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
        tableConfig.setTableEmptyMessage(translate("index.table.nogroup"));
        removeAsListenerAndDispose(groupListCtr);
        groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
        listenTo(groupListCtr);

        groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course", 0, null, getLocale()));
        groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.bgname", 1, TABLE_ACTION_LAUNCH, getLocale()));
        groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.description", 2, null, getLocale()));
        groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.type", 3, null, getLocale()));
        if (withLeaveAndDelete) {
            groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.leave", 4, TABLE_ACTION_LEAVE, translate("table.header.leave"), null));
            groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.delete", 5, TABLE_ACTION_DELETE, translate("table.header.delete"), null));

        }
        // 2) init list model
        groupListModel = new BusinessGroupTableModelWithType(new ArrayList(), getTranslator());
        groupListCtr.setTableDataModel(groupListModel);
        main.put("groupList", groupListCtr.getInitialComponent());
    }

    /**
     * Get most recent data from the database and init the group list model with data for all groups
     */
    private void updateGroupListModelAll() {
        final List wrapped = new ArrayList();
        // buddy groups
        List groups = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, Boolean.TRUE, Boolean.TRUE));
        }
        groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, Boolean.TRUE, null));
        }
        // learning groups
        groups = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        // right groups
        groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        groupListModel.setEntries(wrapped);
        groupListCtr.modelChanged();
    }

    /**
     * Get most recent data from the database and init the group list model with data for buddy groups
     */
    private void updateGroupListModelBuddygroups() {
        final List wrapped = new ArrayList();
        // buddy groups
        List groups = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, Boolean.TRUE, Boolean.TRUE));
        }
        groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_BUDDYGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, Boolean.TRUE, null));
        }
        groupListModel.setEntries(wrapped);
        groupListCtr.modelChanged();
    }

    /**
     * Get most recent data from the database and init the group list model with data for learning groups
     */
    private void updateGroupListModelLearninggroups() {
        final List wrapped = new ArrayList();
        // learning groups
        List groups = businessGroupService.findBusinessGroupsOwnedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_LEARNINGROUP, identity, null);
        iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        groupListModel.setEntries(wrapped);
        groupListCtr.modelChanged();
    }

    /**
     * Get most recent data from the database and init the group list model with data for right groups
     */
    private void updateGroupListModelRightgroups() {
        final List wrapped = new ArrayList();
        // buddy groups
        // right groups
        final List groups = businessGroupService.findBusinessGroupsAttendedBy(BusinessGroup.TYPE_RIGHTGROUP, identity, null);
        final Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            final BusinessGroup group = (BusinessGroup) iter.next();
            wrapped.add(wrapGroup(group, null, null));
        }
        groupListModel.setEntries(wrapped);
        groupListCtr.modelChanged();
    }

    /**
     * Wrapps a group and some data into an object[] that can be displayed by the group list model
     * 
     * @param group
     * @param allowLeave
     *            true: user can leave
     * @param allowDelete
     *            true: user can delete
     * @return Object[]
     */
    private Object[] wrapGroup(final BusinessGroup group, final Boolean allowLeave, final Boolean allowDelete) {
        return new Object[] { group, allowLeave, allowDelete };
    }

    /**
     * @return TreeModel
     */
    private TreeModel buildTreeModel() {
        final GenericTreeModel gtm = new GenericTreeModel();

        final GenericTreeNode rootNode = new GenericTreeNode();
        rootNode.setTitle(translate("menu.index"));
        rootNode.setUserObject(CMD_MENU_INDEX);
        rootNode.setAltText(translate("menu.index.alt"));
        gtm.setRootNode(rootNode);

        GenericTreeNode myEntriesTn = new GenericTreeNode();
        myEntriesTn.setTitle(translate("menu.buddygroups"));
        myEntriesTn.setUserObject(CMD_MENU_BUDDY);
        myEntriesTn.setAltText(translate("menu.buddygroups.alt"));
        rootNode.addChild(myEntriesTn);

        myEntriesTn = new GenericTreeNode();
        myEntriesTn.setTitle(translate("menu.learninggroups"));
        myEntriesTn.setUserObject(CMD_MENU_LEARN);
        myEntriesTn.setAltText(translate("menu.learninggroups.alt"));
        rootNode.addChild(myEntriesTn);

        myEntriesTn = new GenericTreeNode();
        myEntriesTn.setTitle(translate("menu.rightgroups"));
        myEntriesTn.setUserObject(CMD_MENU_RIGHT);
        myEntriesTn.setAltText(translate("menu.rightgroups.alt"));
        rootNode.addChild(myEntriesTn);

        return gtm;
    }

    /**
	 */
    @Override
    public void activate(final UserRequest ureq, final String viewIdentifier) {
        // find the menu node that has the user object that represents the
        // viewIdentifyer
        final GenericTreeNode rootNode = (GenericTreeNode) this.menuTree.getTreeModel().getRootNode();
        final TreeNode activatedNode = TreeHelper.findNodeByUserObject(viewIdentifier, rootNode);
        if (activatedNode != null) {
            this.menuTree.setSelectedNodeId(activatedNode.getIdent());
            activateContent(ureq, activatedNode.getUserObject());
        } else {
            // not found, activate the root node
            this.menuTree.setSelectedNodeId(rootNode.getIdent());
            activateContent(ureq, rootNode.getUserObject());
            // cehck for toolbox activation points
            if (viewIdentifier.equals(ACTION_ADD_BUDDYGROUP)) {
                initAddBuddygroupWorkflow(ureq);
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }
}
