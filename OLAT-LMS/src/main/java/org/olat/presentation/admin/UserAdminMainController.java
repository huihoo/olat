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

package org.olat.presentation.admin;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Roles;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.coordinate.LockingService;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.BaseSecurityModule;
import org.olat.lms.security.UserSearchFilter;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeModel;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.core.control.generic.dtabs.Activateable;
import org.olat.presentation.framework.core.control.generic.messages.MessageUIFactory;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.extensions.ExtManager;
import org.olat.presentation.framework.extensions.Extension;
import org.olat.presentation.framework.extensions.action.ActionExtension;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.user.administration.NewUsersNotificationsController;
import org.olat.presentation.user.administration.UserAdminController;
import org.olat.presentation.user.administration.UserCreateController;
import org.olat.presentation.user.administration.UsermanagerUserSearchController;
import org.olat.presentation.user.administration.delete.DirectDeleteController;
import org.olat.presentation.user.administration.delete.TabbedPaneController;
import org.olat.presentation.user.administration.importwizzard.UserImportController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.LockResult;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <pre>
 * Initial Date:  Jan 16, 2006
 * @author Florian Gnaegi
 * 
 * Comment:
 * This controller offers user and system group administrative functionality. The 
 * features can be enabled / disabled in the spring file for the user
 * manager, OLAT administrators always have full access to the tools. 
 * 
 * To append predefined searches use ActionExtensions and register them for UserAdminMainController.EXTENSIONPOINT_MENU_MENUQUERIES.
 * </pre>
 */
public class UserAdminMainController extends MainLayoutBasicController implements Activateable {
    public static final String EXTENSIONPOINT_MENU_MENUQUERIES = ".menu.menuqueries";
    private static boolean extensionLogged = false;
    private static final Logger log = LoggerHelper.getLogger();

    private final MenuTree olatMenuTree;
    private final Panel content;

    private final LayoutMain3ColsController columnLayoutCtr;
    private Controller contentCtr;
    private UserAdminController userAdminCtr;
    private VelocityContainer rolesVC, queriesVC;

    private String activatePaneInDetailView = null;

    private LockResult lock;

    /**
     * Constructor of the home main controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     */
    public UserAdminMainController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel(ureq.getUserSession().getRoles().isOLATAdmin());
        olatMenuTree.setTreeModel(tm);
        final INode firstNode = tm.getRootNode().getChildAt(0);
        olatMenuTree.setSelectedNodeId(firstNode.getIdent());
        olatMenuTree.addListener(this);

        // we always start with a search controller
        contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl());
        listenTo(contentCtr); // auto dispose later

        content = new Panel("content");
        content.setContent(contentCtr.getInitialComponent());

        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, content, "useradminmain");
        columnLayoutCtr.addCssClassToMain("o_useradmin");

        listenTo(columnLayoutCtr); // auto dispose later
        putInitialPanel(columnLayoutCtr.getInitialComponent());
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == olatMenuTree) {
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                final TreeNode selTreeNode = olatMenuTree.getSelectedNode();
                final Object userObject = selTreeNode.getUserObject();
                final Component resComp = initComponentFromMenuCommand(userObject, ureq);
                content.setContent(resComp);
            } else { // the action was not allowed anymore
                content.setContent(null); // display an empty field (empty panel)
            }
        } else {
            log.warn("Unhandled olatMenuTree event: " + event.getCommand());
        }
    }

    /**
	 */
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == contentCtr) {
            if (event instanceof SingleIdentityChosenEvent) {
                final SingleIdentityChosenEvent userChoosenEvent = (SingleIdentityChosenEvent) event;
                final Identity identity = userChoosenEvent.getChosenIdentity();
                // cleanup old userAdminCtr controller
                removeAsListenerAndDispose(userAdminCtr);
                userAdminCtr = new UserAdminController(ureq, getWindowControl(), identity);
                listenTo(userAdminCtr);
                // activate a special pane in the tabbed pane when set
                if (activatePaneInDetailView != null) {
                    userAdminCtr.activate(ureq, activatePaneInDetailView);
                }
                content.setContent(userAdminCtr.getInitialComponent());
                // deactivate back button for user create controller, kames no sense there
                if (contentCtr instanceof UserCreateController) {
                    userAdminCtr.setBackButtonEnabled(false);
                } else {
                    userAdminCtr.setBackButtonEnabled(true);
                }

            }
        } else if (source == userAdminCtr) {
            if (event == Event.BACK_EVENT) {
                removeAsListenerAndDispose(userAdminCtr);
                userAdminCtr = null;
                // update data model of content controller when of type user search
                // to display correct values of identity
                if (contentCtr instanceof UsermanagerUserSearchController) {
                    final UsermanagerUserSearchController userSearchCtr = (UsermanagerUserSearchController) contentCtr;
                    userSearchCtr.reloadFoundIdentity();
                }
                content.setContent(contentCtr.getInitialComponent());
            }
        }
    }

    private Component initComponentFromMenuCommand(final Object uobject, final UserRequest ureq) {
        // in any case release delete user gui lock (reaquired if user deletion is again clicked)
        releaseDeleteUserLock();

        if (uobject instanceof ActionExtension) {
            final ActionExtension ae = (ActionExtension) uobject;
            contentCtr = ae.createController(ureq, getWindowControl(), null);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        }

        // first check if it is node which opens a subtree with further uobject.tree.commands
        if (uobject.equals("menuroles")) {
            if (rolesVC == null) {
                rolesVC = createVelocityContainer("systemroles");
            }
            return rolesVC;
        } else if (uobject.equals("menuqueries")) {
            if (queriesVC == null) {
                queriesVC = createVelocityContainer("predefinedqueries");
            }
            return queriesVC;
        } else if (uobject.equals("menuaccess")) {
            if (queriesVC == null) {
                queriesVC = createVelocityContainer("systemroles");
            }
            return queriesVC;
        } else if (uobject.equals("userdelete")) {
            // creates the user deletin controller
            // if locking fails -> a contentCtrl is created
            // -> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
            return createAndLockUserDeleteController(ureq);
        } else if (uobject.equals("userdelete_direct")) {
            // creates the user deletin controller
            // if locking fails -> a contentCtrl is created
            // -> hence removeAsListenerAndDispose(contentCtr) is delegated to the method called!
            return createAndLockDirectUserDeleteController(ureq);
        }

        // these nodes re-create (not stateful) content Controller (contentCtrl)
        //
        removeAsListenerAndDispose(contentCtr);
        if (uobject.equals("usearch") || uobject.equals("useradmin")) {
            activatePaneInDetailView = null;
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl());
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("ucreate")) {
            activatePaneInDetailView = null;
            boolean canCreateOLATPassword = false;
            if (ureq.getUserSession().getRoles().isOLATAdmin()) {
                // admin will override configuration
                canCreateOLATPassword = true;
            } else {
                final Boolean canCreatePwdByConfig = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;
                canCreateOLATPassword = canCreatePwdByConfig.booleanValue();
            }
            contentCtr = new UserCreateController(ureq, getWindowControl(), canCreateOLATPassword);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("usersimport")) {
            activatePaneInDetailView = null;
            boolean canCreateOLATPassword = getBaseSecurityEBL().isCreatePasswordPermitted(ureq.getUserSession().getRoles());
            contentCtr = new UserImportController(ureq, getWindowControl(), canCreateOLATPassword);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("admingroup")) {
            activatePaneInDetailView = "";

            Roles roles = new Roles.Builder().admin().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("authorgroup")) {
            activatePaneInDetailView = "edit.uroles";

            Roles roles = new Roles.Builder().author().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("coauthors")) {
            activatePaneInDetailView = "edit.uroles";

            // special case: use user search controller and search for all users that have author rights
            // and subtract users that are in the author group to get the co-authors
            Roles excludedRoles = new Roles.Builder().author().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().authorPermissions().excludeRoles(excludedRoles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("resourceowners")) {
            activatePaneInDetailView = "edit.uroles";

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().authorPermissions().build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("groupmanagergroup")) {
            activatePaneInDetailView = "edit.uroles";

            Roles roles = new Roles.Builder().groupManager().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("usermanagergroup")) {
            activatePaneInDetailView = "edit.uroles";

            Roles roles = new Roles.Builder().userManager().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("usergroup")) {
            activatePaneInDetailView = "edit.uroles";

            Roles roles = new Roles.Builder().user().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("anonymousgroup")) {
            activatePaneInDetailView = "edit.uroles";

            Roles roles = new Roles.Builder().guestOnly().build();
            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().roles(roles).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("logondeniedgroup")) {
            activatePaneInDetailView = "edit.uroles";

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().loginDenied().build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("deletedusers")) {
            activatePaneInDetailView = "list.deletedusers";

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().deleted().build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, false);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("created.lastweek")) {
            activatePaneInDetailView = null;
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -7);
            final Date time = cal.getTime();

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().createdAfter(time).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("created.lastmonth")) {
            activatePaneInDetailView = null;
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            final Date time = cal.getTime();

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().createdAfter(time).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("created.sixmonth")) {
            activatePaneInDetailView = null;
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -6);
            final Date time = cal.getTime();

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().createdAfter(time).build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("created.newUsersNotification")) {
            activatePaneInDetailView = null;
            final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(new OLATResourceable() {
                @Override
                public Long getResourceableId() {
                    return 0l;
                }

                @Override
                public String getResourceableTypeName() {
                    return "NewIdentityCreated";
                }
            });
            final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, getWindowControl());
            contentCtr = new NewUsersNotificationsController(ureq, bwControl);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else if (uobject.equals("noauthentication")) {
            activatePaneInDetailView = null;

            UserSearchFilter userSearchFilter = new UserSearchFilter.Builder().noAuthentication().build();
            contentCtr = new UsermanagerUserSearchController(ureq, getWindowControl(), userSearchFilter, true);
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else {
            // to be removed
            throw new AssertException("did not expect to land here in UserAdminMainController this is because uboject is " + uobject.toString());
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    /**
     * Creates a DirectDeleteController and acquire a 'delete-user-lock'. The lock is for both direct-deletion and workflow with email.
     * 
     * @param ureq
     * @return
     */
    private Component createAndLockDirectUserDeleteController(final UserRequest ureq) {
        final Controller lockCtrl = acquireDeleteUserLock(ureq);
        if (lockCtrl == null) {
            // success -> create new User deletion workflow
            removeAsListenerAndDispose(contentCtr);
            contentCtr = new DirectDeleteController(ureq, getWindowControl());
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else {
            // failure -> monolog controller with message that lock failed
            return lockCtrl.getInitialComponent();
        }
    }

    /**
     * Creates a TabbedPaneController (delete workflow with email) and acquire a 'delete-user-lock'. The lock is for both direct-deletion and workflow with email.
     * 
     * @param ureq
     * @return
     */
    private Component createAndLockUserDeleteController(final UserRequest ureq) {
        final Controller lockCtrl = acquireDeleteUserLock(ureq);

        if (lockCtrl == null) {
            // success -> create new User deletion workflow
            activatePaneInDetailView = null;
            removeAsListenerAndDispose(contentCtr);
            contentCtr = new TabbedPaneController(ureq, getWindowControl());
            listenTo(contentCtr);
            return contentCtr.getInitialComponent();
        } else {
            // failure -> monolog controller with message that lock failed
            return lockCtrl.getInitialComponent();
        }
    }

    /**
     * Acquire lock for whole delete-user workflow
     */
    private Controller acquireDeleteUserLock(final UserRequest ureq) {
        final OLATResourceable lockResourceable = OresHelper.createOLATResourceableTypeWithoutCheck(TabbedPaneController.class.getName());
        lock = getLockingService().acquireLock(lockResourceable, ureq.getIdentity(), "deleteGroup");
        if (!lock.isSuccess()) {
            final String text = getTranslator().translate("error.deleteworkflow.locked.by", new String[] { lock.getOwner().getName() });
            final Controller monoCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, text);
            return monoCtr;
        }
        return null;
    }

    private LockingService getLockingService() {
        return CoreSpringFactory.getBean(LockingService.class);
    }

    /**
     * Releases the lock for this page if set
     */
    private void releaseDeleteUserLock() {
        if (lock != null) {
            getLockingService().releaseLock(lock);
            lock = null;
        }
    }

    private TreeModel buildTreeModel(final boolean isOlatAdmin) {
        GenericTreeNode gtnChild, admin;
        final Translator translator = getTranslator();

        final GenericTreeModel gtm = new GenericTreeModel();
        admin = new GenericTreeNode();
        admin.setTitle(translator.translate("menu.useradmin"));
        admin.setUserObject("useradmin");
        admin.setAltText(translator.translate("menu.useradmin.alt"));
        gtm.setRootNode(admin);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.usearch"));
        gtnChild.setUserObject("usearch");
        gtnChild.setAltText(translator.translate("menu.usearch.alt"));
        admin.setDelegate(gtnChild);
        admin.addChild(gtnChild);

        final Boolean canCreate = BaseSecurityModule.USERMANAGER_CAN_CREATE_USER;
        if (canCreate.booleanValue() || isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.ucreate"));
            gtnChild.setUserObject("ucreate");
            gtnChild.setAltText(translator.translate("menu.ucreate.alt"));
            admin.addChild(gtnChild);

            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.usersimport"));
            gtnChild.setUserObject("usersimport");
            gtnChild.setAltText(translator.translate("menu.usersimport.alt"));
            admin.addChild(gtnChild);
        }
        final Boolean canDelete = BaseSecurityModule.USERMANAGER_CAN_DELETE_USER;
        if (canDelete.booleanValue() || isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.userdelete"));
            gtnChild.setUserObject("userdelete");
            gtnChild.setAltText(translator.translate("menu.userdelete.alt"));
            admin.addChild(gtnChild);

            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.userdelete.direct"));
            gtnChild.setUserObject("userdelete_direct");
            gtnChild.setAltText(translator.translate("menu.userdelete.direct.alt"));
            admin.addChild(gtnChild);
        }

        // START submenu access and rights
        GenericTreeNode gtn3 = new GenericTreeNode();
        gtn3.setTitle(translator.translate("menu.menuaccess"));
        gtn3.setUserObject("menuaccess");
        gtn3.setAltText(translator.translate("menu.menuaccess.alt"));
        admin.addChild(gtn3);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.usergroup"));
        gtnChild.setUserObject("usergroup");
        gtnChild.setAltText(translator.translate("menu.usergroup.alt"));
        gtn3.addChild(gtnChild);

        final Boolean canAuthors = BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS;
        if (canAuthors.booleanValue() || isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.authorgroup"));
            gtnChild.setUserObject("authorgroup");
            gtnChild.setAltText(translator.translate("menu.authorgroup.alt"));
            gtn3.addChild(gtnChild);

            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.coauthors"));
            gtnChild.setUserObject("coauthors");
            gtnChild.setAltText(translator.translate("menu.coauthors.alt"));
            gtn3.addChild(gtnChild);

            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.resourceowners"));
            gtnChild.setUserObject("resourceowners");
            gtnChild.setAltText(translator.translate("menu.resourceowners.alt"));
            gtn3.addChild(gtnChild);
        }

        final Boolean canGroupmanagers = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
        if (canGroupmanagers.booleanValue() || isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.groupmanagergroup"));
            gtnChild.setUserObject("groupmanagergroup");
            gtnChild.setAltText(translator.translate("menu.groupmanagergroup.alt"));
            gtn3.addChild(gtnChild);
        }

        // admin group and user manager group always restricted to admins
        if (isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.usermanagergroup"));
            gtnChild.setUserObject("usermanagergroup");
            gtnChild.setAltText(translator.translate("menu.usermanagergroup.alt"));
            gtn3.addChild(gtnChild);

            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.admingroup"));
            gtnChild.setUserObject("admingroup");
            gtnChild.setAltText(translator.translate("menu.admingroup.alt"));
            gtn3.addChild(gtnChild);
        }

        final Boolean canGuests = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;
        if (canGuests.booleanValue() || isOlatAdmin) {
            gtnChild = new GenericTreeNode();
            gtnChild.setTitle(translator.translate("menu.anonymousgroup"));
            gtnChild.setUserObject("anonymousgroup");
            gtnChild.setAltText(translator.translate("menu.anonymousgroup.alt"));
            gtn3.addChild(gtnChild);
        }

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.noauthentication"));
        gtnChild.setUserObject("noauthentication");
        gtnChild.setAltText(translator.translate("menu.noauthentication.alt"));
        gtn3.addChild(gtnChild);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.logondeniedgroup"));
        gtnChild.setUserObject("logondeniedgroup");
        gtnChild.setAltText(translator.translate("menu.logondeniedgroup.alt"));
        gtn3.addChild(gtnChild);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.deletedusers"));
        gtnChild.setUserObject("deletedusers");
        gtnChild.setAltText(translator.translate("menu.deletedusers.alt"));
        gtn3.addChild(gtnChild);

        // END submenu access and rights

        // START other queries
        gtn3 = new GenericTreeNode();
        gtn3.setTitle(translator.translate("menu.menuqueries"));
        gtn3.setUserObject("menuqueries");
        gtn3.setAltText(translator.translate("menu.menuqueries.alt"));
        admin.addChild(gtn3);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.created.lastweek"));
        gtnChild.setUserObject("created.lastweek");
        gtnChild.setAltText(translator.translate("menu.created.lastweek.alt"));
        gtn3.addChild(gtnChild);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.created.lastmonth"));
        gtnChild.setUserObject("created.lastmonth");
        gtnChild.setAltText(translator.translate("menu.created.lastmonth.alt"));
        gtn3.addChild(gtnChild);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.created.sixmonth"));
        gtnChild.setUserObject("created.sixmonth");
        gtnChild.setAltText(translator.translate("menu.created.sixmonth.alt"));
        gtn3.addChild(gtnChild);

        gtnChild = new GenericTreeNode();
        gtnChild.setTitle(translator.translate("menu.created.newUsersNotification"));
        gtnChild.setUserObject("created.newUsersNotification");
        gtnChild.setAltText(translator.translate("menu.created.newUsersNotification.alt"));
        gtn3.addChild(gtnChild);

        // add extension menues as child items
        final ExtManager extm = CoreSpringFactory.getBean(ExtManager.class);
        final int cnt = extm.getExtensionCnt();
        for (int i = 0; i < cnt; i++) {
            final Extension anExt = extm.getExtension(i);
            // 1) general menu extensions
            final ActionExtension ae = (ActionExtension) anExt.getExtensionFor(UserAdminMainController.class.getName() + EXTENSIONPOINT_MENU_MENUQUERIES);
            if (ae != null && anExt.isEnabled()) {
                gtnChild = new GenericTreeNode();
                final String menuText = ae.getActionText(getLocale());
                gtnChild.setTitle(menuText);
                gtnChild.setUserObject(ae);
                gtnChild.setAltText(ae.getDescription(getLocale()));
                gtn3.addChild(gtnChild);
                // inform only once
                if (!extensionLogged) {
                    log.info("added menu entry for locale " + getLocale().toString() + " '" + menuText + "'", null);
                }
            }
        }
        extensionLogged = true;

        // END other queries
        return gtm;
    }

    public void activate(final UserRequest ureq, final String viewIdentifier) {
        if (viewIdentifier.startsWith("notifications") || viewIdentifier.startsWith("NewIdentityCreated")) {
            final GenericTreeModel tm = (GenericTreeModel) olatMenuTree.getTreeModel();
            final TreeNode node = tm.findNodeByUserObject("created.newUsersNotification");
            olatMenuTree.setSelectedNode(node);
            final Component resComp = initComponentFromMenuCommand("created.newUsersNotification", ureq);
            content.setContent(resComp);
        }
    }

    /**
	 */
    protected void doDispose() {
        // controllers disposed in BasicController
        releaseDeleteUserLock();
    }

}
