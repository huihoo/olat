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

package org.olat.presentation.group.context;

import java.util.List;

import org.olat.data.group.BusinessGroup;
import org.olat.data.group.context.BGContext;
import org.olat.data.group.context.BGContextDao;
import org.olat.data.group.context.BGContextDaoImpl;
import org.olat.lms.group.context.BusinessGroupContextService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
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
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxController;
import org.olat.presentation.framework.core.control.generic.modal.DialogBoxUIFactory;
import org.olat.presentation.framework.core.control.generic.tool.ToolController;
import org.olat.presentation.framework.core.control.generic.tool.ToolFactory;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.group.BGControllerFactory;
import org.olat.presentation.group.management.BGManagementController;
import org.olat.system.commons.StringHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.MultiUserEvent;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<BR>
 * Management controller for the group context. A group context is a container for groups and learning areas. Group contexts are associated with OLATResources, currently
 * only with courses. Default contexts belong only to one resource, regular contexts can be associated with many courses, they then share the same groupmanagement. With
 * this controller this group contexts can be created and managed.
 * <P>
 * Initial Date: Jan 24, 2005
 * 
 * @author gnaegi
 */
public class BGContextManagementController extends MainLayoutBasicController {

    // Menu commands
    private static final String CMD_INDEX = "cmd.index";
    private static final String CMD_CONTEXTLIST = "cmd.contextlist";
    // Toolbox commands
    private static final String CMD_LG_CONTEXT_CREATE = "cmd.learninggroup.context.create";
    private static final String CMD_RG_CONTEXT_CREATE = "cmd.rightgroup.context.create";
    // List commands
    private static final String CMD_CONTEXT_RUN = "cmd.context.run";
    private static final String CMD_CONTEXT_EDIT = "cmd.context.edit";
    private static final String CMD_CONTEXT_DELETE = "cmd.context.delete";

    private VelocityContainer indexVC, newContextVC, contextListVC;
    private TableController contextListCtr;
    private BGContextTableModel contextTableModel;
    private DialogBoxController confirmDeleteContext;

    // Layout components and controllers

    private final Panel content;
    private final LayoutMain3ColsController columnLayoutCtr;
    private final MenuTree olatMenuTree;
    private ToolController toolC;

    // Managers
    private final BGContextDao contextManager;

    // components
    private BGContextFormController newContextController;

    // Workflow variables
    private BGManagementController groupManagementController;
    private BGContextEditController contextEditCtr;
    private BGContext currentGroupContext;

    /**
     * Constructor for a business group management controller.
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     */
    public BGContextManagementController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        // Initialize managers
        this.contextManager = BGContextDaoImpl.getInstance();
        // Initialize all velocity containers
        initVC();

        // Layout is controlled with generic controller: menu - content - tools
        // Navigation menu
        this.olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel();
        this.olatMenuTree.setTreeModel(tm);
        this.olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
        this.olatMenuTree.addListener(this);
        // Content
        this.content = new Panel("content");
        // Tools
        // 1 create empty Tools and init menuAndToolController
        // 2 set correct tools using setTools method (override step 1)
        this.toolC = ToolFactory.createToolController(getWindowControl());
        this.columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), this.olatMenuTree, this.toolC.getInitialComponent(), this.content,
                "groupcontextmngt");
        columnLayoutCtr.addCssClassToMain("o_groupsmanagement");

        listenTo(this.columnLayoutCtr);

        doIndex(ureq);

        putInitialPanel(this.columnLayoutCtr.getInitialComponent());
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        final String cmd = event.getCommand();
        if (source == this.olatMenuTree) {
            if (cmd.equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                handleMenuCommands(ureq);
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        final String cmd = event.getCommand();
        if (source == this.toolC) {
            handleToolCommands(ureq, cmd);
        } else if (source == this.groupManagementController) {
            if (event == Event.DONE_EVENT) {
                getWindowControl().pop();
            }
        } else if (source == this.confirmDeleteContext) {
            if (DialogBoxUIFactory.isYesEvent(event)) {
                // yes case
                doContextDelete();
                this.content.popContent();
                doContextList(ureq, true);
                final MultiUserEvent mue = new BGContextEvent(BGContextEvent.CONTEXT_DELETED, this.currentGroupContext);
                CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(mue, this.currentGroupContext);
            }
        } else if (source == this.contextListCtr) {
            if (cmd.equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
                final TableEvent te = (TableEvent) event;
                final String actionid = te.getActionId();
                final int rowid = te.getRowId();
                this.currentGroupContext = this.contextTableModel.getGroupContextAt(rowid);
                if (actionid.equals(CMD_CONTEXT_EDIT)) {
                    doContextEdit(ureq);
                } else if (actionid.equals(CMD_CONTEXT_RUN)) {
                    doContextRun(ureq);
                } else if (actionid.equals(CMD_CONTEXT_DELETE)) {
                    doContextDeleteConfirm(ureq);
                }
            }
        } else if (source == this.newContextController) {
            if (event == Event.DONE_EVENT) {
                final BGContext newContext = doContextCreate(ureq);
                if (newContext == null) {
                    throw new AssertException("Could not create new BGContext - unknown reason");
                } else {
                    this.currentGroupContext = newContext;
                    doContextEdit(ureq);
                }
            } else if (event == Event.CANCELLED_EVENT) {
                doIndex(ureq);
            }
        }
    }

    private void handleToolCommands(final UserRequest ureq, final String cmd) {
        if (cmd.equals(CMD_LG_CONTEXT_CREATE)) {
            doContextCreateForm(ureq, BusinessGroup.TYPE_LEARNINGROUP);
        } else if (cmd.equals(CMD_RG_CONTEXT_CREATE)) {
            doContextCreateForm(ureq, BusinessGroup.TYPE_RIGHTGROUP);
        } else if (cmd.equals(CMD_CONTEXT_RUN)) {
            doContextRun(ureq);
        } else if (cmd.equals(CMD_CONTEXT_DELETE)) {
            doContextDeleteConfirm(ureq);
        }

    }

    private void handleMenuCommands(final UserRequest ureq) {

        // remove lock from current context
        removeAsListenerAndDispose(contextEditCtr);

        final TreeNode selTreeNode = this.olatMenuTree.getSelectedNode();
        final String cmd = (String) selTreeNode.getUserObject();

        if (cmd.equals(CMD_INDEX)) {
            doIndex(ureq);
        } else if (cmd.equals(CMD_CONTEXTLIST)) {
            doContextList(ureq, true);
        }
    }

    private TreeModel buildTreeModel() {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(translate("menu.index"));
        root.setUserObject(CMD_INDEX);
        root.setAltText(translate("menu.index.alt"));
        gtm.setRootNode(root);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.allcontexts"));
        gtn.setUserObject(CMD_CONTEXTLIST);
        gtn.setAltText(translate("menu.allcontexts.alt"));
        root.addChild(gtn);

        return gtm;
    }

    private void setTools(final boolean contextSelected) {
        removeAsListenerAndDispose(toolC);
        toolC = ToolFactory.createToolController(getWindowControl());
        listenTo(toolC);

        this.columnLayoutCtr.setCol2(this.toolC.getInitialComponent());
        this.toolC.addHeader(translate("tools.title.contextmanagement"));
        // Generic actions
        this.toolC.addLink(CMD_LG_CONTEXT_CREATE, translate(CMD_LG_CONTEXT_CREATE));
        this.toolC.addLink(CMD_RG_CONTEXT_CREATE, translate(CMD_RG_CONTEXT_CREATE));
        // context specific actions
        if (contextSelected) {
            this.toolC.addHeader(translate("tools.title.context"));
            this.toolC.addLink(CMD_CONTEXT_RUN, translate(CMD_CONTEXT_RUN));
            this.toolC.addLink(CMD_CONTEXT_DELETE, translate(CMD_CONTEXT_DELETE));
        }
    }

    private void initVC() {
        this.indexVC = createVelocityContainer("contextmanagement");
        // Create new context form
        this.newContextVC = createVelocityContainer("newcontext");
        // Context list
        this.contextListVC = createVelocityContainer("contextlist");
    }

    private void doIndex(final UserRequest ureq) {
        this.content.setContent(this.indexVC);
        setTools(false);
    }

    private void doContextCreateForm(final UserRequest ureq, final String type) {

        removeAsListenerAndDispose(this.newContextController);
        this.newContextController = new BGContextFormController(ureq, getWindowControl(), type, ureq.getUserSession().getRoles().isOLATAdmin());
        listenTo(this.newContextController);

        this.newContextVC.put("newContextForm", this.newContextController.getInitialComponent());
        this.content.setContent(this.newContextVC);
    }

    private BGContext doContextCreate(final UserRequest ureq) {
        final String name = this.newContextController.getName();
        final String desc = this.newContextController.getDescription();
        final String type = this.newContextController.getType();
        return getBgContextService().createAndPersistBGContext(name, desc, type, ureq.getIdentity(), false);
    }

    private void doContextEdit(final UserRequest ureq) {

        // create new edit controller
        removeAsListenerAndDispose(contextEditCtr);
        contextEditCtr = new BGContextEditController(ureq, getWindowControl(), this.currentGroupContext);
        listenTo(contextEditCtr);

        if (this.contextEditCtr.isLockAcquired()) {
            this.content.setContent(this.contextEditCtr.getInitialComponent());
            setTools(true);
        }
    }

    private void doContextRun(final UserRequest ureq) {
        removeAsListenerAndDispose(groupManagementController);
        groupManagementController = BGControllerFactory.getInstance().createManagementController(ureq, getWindowControl(), this.currentGroupContext, false);
        listenTo(groupManagementController);

        // FIXME fg: no layout ctr in a modal panel!
        getWindowControl().pushToMainArea(this.groupManagementController.getInitialComponent());
    }

    private void doContextDeleteConfirm(final UserRequest ureq) {
        final List resource = this.contextManager.findOLATResourcesForBGContext(this.currentGroupContext);
        String contextName = StringHelper.escapeHtml(this.currentGroupContext.getName());
        if (resource.size() == 0) {
            this.confirmDeleteContext = activateYesNoDialog(ureq, null, translate("context.delete.used.zero", contextName), this.confirmDeleteContext);
        } else if (resource.size() == 1) {
            this.confirmDeleteContext = activateYesNoDialog(ureq, null, translate("context.delete.used.one", contextName), this.confirmDeleteContext);
        } else {
            this.confirmDeleteContext = activateYesNoDialog(ureq, null,
                    getTranslator().translate("context.delete.used.multi", new String[] { contextName, Integer.toString(resource.size()) }), this.confirmDeleteContext);
        }
    }

    private void doContextDelete() {
        getBgContextService().deleteCompleteBGContext(this.currentGroupContext);
    }

    /**
     * @return
     */
    private BusinessGroupContextService getBgContextService() {
        return CoreSpringFactory.getBean(BusinessGroupContextService.class);
    }

    private void doContextList(final UserRequest ureq, final boolean initializeModel) {
        // Init table only once
        if (this.contextListCtr == null) {
            final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
            tableConfig.setTableEmptyMessage(translate("contextlist.no.contexts"));
            // init group list filter controller
            removeAsListenerAndDispose(contextListCtr);
            contextListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
            listenTo(contextListCtr);

            this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.name", 0, CMD_CONTEXT_RUN, ureq.getLocale()));
            this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.desc", 1, null, ureq.getLocale()));
            this.contextListCtr.addColumnDescriptor(new DefaultColumnDescriptor("contextlist.table.type", 2, null, ureq.getLocale()));
            this.contextListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_CONTEXT_EDIT, "contextlist.table.edit", translate(CMD_CONTEXT_EDIT)));
            this.contextListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_CONTEXT_DELETE, "contextlist.table.delete", translate(CMD_CONTEXT_DELETE)));
            this.contextListVC.put("contextlist", this.contextListCtr.getInitialComponent());
        }

        if (this.contextTableModel == null || initializeModel) {
            final List contexts = this.contextManager.findBGContextsForIdentity(ureq.getIdentity(), false, true);
            this.contextTableModel = new BGContextTableModel(contexts, getTranslator(), true, false);
            this.contextListCtr.setTableDataModel(this.contextTableModel);
        }
        this.content.setContent(this.contextListVC);
        setTools(false);
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // Controllers disposed by BasicController:
    }

    private void cleanupContextLock() {

    }
}
