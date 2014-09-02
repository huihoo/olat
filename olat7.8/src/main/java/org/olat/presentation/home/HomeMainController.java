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

package org.olat.presentation.home;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_SHIB;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.UserConstants;
import org.olat.lms.activitylogging.LoggingResourceable;
import org.olat.lms.commons.context.BusinessControlFactory;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.tree.TreeHelper;
import org.olat.lms.portfolio.EPFrontendManager;
import org.olat.lms.portfolio.PortfolioModule;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.PersonalFolderManager;
import org.olat.lms.user.UserService;
import org.olat.presentation.bookmark.ManageBookmarkController;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.course.assessment.EfficiencyStatementsListController;
import org.olat.presentation.events.SingleIdentityChosenEvent;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.Windows;
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
import org.olat.presentation.framework.core.control.generic.dtabs.DTabs;
import org.olat.presentation.framework.core.control.generic.portal.Portal;
import org.olat.presentation.framework.core.control.generic.portal.PortalMainController;
import org.olat.presentation.framework.core.control.state.ControllerState;
import org.olat.presentation.framework.extensions.ExtManager;
import org.olat.presentation.framework.extensions.Extension;
import org.olat.presentation.framework.extensions.action.ActionExtension;
import org.olat.presentation.framework.extensions.action.GenericActionExtension;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.note.NoteListController;
import org.olat.presentation.notification.NotificationController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.presentation.repository.DynamicTabHelper;
import org.olat.presentation.user.ChangeEMailExecuteController;
import org.olat.presentation.user.PersonalSettingsController;
import org.olat.presentation.user.UserInfoMainController;
import org.olat.presentation.user.administration.UserSearchController;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <!--**************--> <h3>Responsability:</h3> display the first page the user sees after she logged in successfully. This is the users individual dashboard within the
 * learning management system.<br>
 * The guest login has it's own {@link org.olat.presentation.home.GuestHomeMainController first page} !
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * display portal.</li>
 * <li><i>Portal editing:</i><br>
 * Switch portal to edit mode.<br>
 * edit portal.<br>
 * switch to display mode.</li>
 * <li><i>Activate target XYZ:</i><br>
 * display activated component, i.e. jump to the personal briefcase</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Activateable targets:</h3>
 * <ul>
 * <li><i>{@link #MENU_ROOT}</i>:<br>
 * main entry point, the portal view.</li>
 * <li><i>{@link #MENU_BC}</i>:<br>
 * jump to personal briefcase.</li>
 * <li><i>{@link #MENU_NOTE}</i>:<br>
 * list of user notes.</li>
 * <li><i>{@link #MENU_BOOKMARKS}</i>:<br>
 * users bookmarks list.</li>
 * <li><i>{@link #MENU_ADMINNOTIFICATIONS}</i>:<br>
 * notifications list.</li>
 * <li><i>{@link #MENU_OTHERUSERS}</i>:<br>
 * search other users workflow.</li>
 * <li><i>{@link #MENU_EFFICIENCY_STATEMENTS}</i>:<br>
 * list of users efficency statements.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3> TODO:fg:a add here the hints/special notes for the HomeMainController
 * <p>
 * Initial Date: Apr 27, 2004
 * 
 * @author Felix Jost
 */
public class HomeMainController extends MainLayoutBasicController implements Activateable {

    private static final Logger log = LoggerHelper.getLogger();
    // Menu commands. These are also used in velocity containers. When changing
    // the values make sure you performed a full text search first
    private static final String MENU_NOTE = "note";
    private static final String MENU_BC = "bc";
    private static final String MENU_CALENDAR = "cal";
    private static final String MENU_BOOKMARKS = "bookmarks";
    private static final String MENU_EFFICIENCY_STATEMENTS = "efficiencyStatements";
    private static final String MENU_ADMINNOTIFICATIONS = "adminnotifications";
    private static final String MENU_MYSETTINGS = "mysettings";
    private static final String MENU_ROOT = "root";
    private static final String MENU_OTHERUSERS = "otherusers";
    private static final String MENU_PORTFOLIO = "portfolio";
    private static final String MENU_PORTFOLIO_ARTEFACTS = "AbstractArtefact";
    private static final String MENU_PORTFOLIO_MY_MAPS = "portfolioMyMaps";
    private static final String MENU_PORTFOLIO_MY_STRUCTURED_MAPS = "portfolioMyStructuredMaps";
    private static final String MENU_PORTFOLIO_OTHERS_MAPS = "portfolioOthersMaps";
    private static final String PRESENTED_EMAIL_CHANGE_REMINDER = "presentedemailchangereminder";
    private static final String PRESENTED_PASSWORD_CHANGE_REMINDER = "passwordChangeReminder";

    private static boolean extensionLogged = false;

    private final MenuTree olatMenuTree;
    private VelocityContainer welcome;
    private VelocityContainer inclTitle;
    private final Panel content;
    private final LayoutMain3ColsController columnLayoutCtr;
    private Controller resC;
    private String titleStr;
    private AuthenticationService authenticationService;

    /**
     * Constructor of the home main controller
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The current window controller
     */
    public HomeMainController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, updateBusinessPath(ureq, wControl));

        addLoggingResourceable(LoggingResourceable.wrap(ureq.getIdentity()));

        olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel(ureq);
        olatMenuTree.setTreeModel(tm);
        olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
        olatMenuTree.addListener(this);
        // Activate correct position in menu
        olatMenuTree.setSelectedNode(tm.getRootNode());
        setState("root");

        // prepare main panel
        content = new Panel("content");
        content.setContent(createRootComponent(ureq));

        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, content, "homemain");
        listenTo(columnLayoutCtr);// cleanup on dispose
        // add background image to home site
        columnLayoutCtr.addCssClassToMain("o_home");
        putInitialPanel(columnLayoutCtr.getInitialComponent());

        // check if an existing session was killed, this is detected in UserSession.signOn()
        final Object killedExistingSession = ureq.getUserSession().getEntry(UserSession.STORE_KEY_KILLED_EXISTING_SESSION);
        if (killedExistingSession != null && (killedExistingSession instanceof Boolean)) {
            if (((Boolean) killedExistingSession).booleanValue()) {
                this.showInfo("warn.session.was.killed");
                ureq.getUserSession().removeEntry(UserSession.STORE_KEY_KILLED_EXISTING_SESSION);
            }
        }

        // check running of email change workflow
        if (getUserService().getUserProperty(ureq.getIdentity().getUser(), UserConstants.EMAILCHANGE) != null) {
            final ChangeEMailExecuteController mm = new ChangeEMailExecuteController(ureq, wControl);
            if (mm.isLinkTimeUp()) {
                mm.deleteRegistrationKey();
            } else {
                if (mm.isLinkClicked()) {
                    mm.changeEMail(wControl);
                    activateContent(ureq, MENU_MYSETTINGS, null);
                } else {
                    final Boolean alreadySeen = ((Boolean) ureq.getUserSession().getEntry(PRESENTED_EMAIL_CHANGE_REMINDER));
                    if (alreadySeen == null) {
                        getWindowControl().setWarning(mm.getPackageTranslator().translate("email.change.reminder"));
                        ureq.getUserSession().putEntry(PRESENTED_EMAIL_CHANGE_REMINDER, Boolean.TRUE);
                    }
                }
            }
        } else {
            final String value = getUserService().getUserProperty(ureq.getIdentity().getUser(), UserConstants.EMAILDISABLED);
            if (value != null && value.equals("true")) {
                wControl.setWarning(translate("email.disabled"));
            }
        }

        // OLAT-6991
        warnWeakCredential(ureq, wControl);
    }

    /**
     * The users authenticated via AUTHENTICATION_PROVIDER_OLAT are informed if their OLAT password is weak; <br>
     * the users authenticated via AUTHENTICATION_PROVIDER_SHIB are informed if their WebDAV password is weak, <br>
     * until the password change deadline is reached.
     */
    private void warnWeakCredential(final UserRequest ureq, final WindowControl wControl) {
        int noDays = getAuthenticationService().getDaysToChangePasswordDeadline();
        if (noDays <= 0) {// password migration is completed, no need to show warning
            return;
        }

        String authProvider = ureq.getUserSession().getSessionInfo().getAuthProvider();
        boolean informUser = getAuthenticationService().isPasswordTooOld(ureq.getIdentity(), authProvider);
        if (informUser) {
            final Boolean alreadySeen = ((Boolean) ureq.getUserSession().getEntry(PRESENTED_PASSWORD_CHANGE_REMINDER));
            // show only once during this session
            if (alreadySeen == null) {
                String changePasswordDeadline = getAuthenticationService().getChangePasswordDeadlineDate(ureq.getLocale());
                String[] translationArgs = { String.valueOf(noDays), changePasswordDeadline };
                if (AUTHENTICATION_PROVIDER_OLAT.equals(authProvider)) {
                    wControl.setWarning(translate("warn.weak.credential.olat", translationArgs));
                } else if (AUTHENTICATION_PROVIDER_SHIB.equals(authProvider)) {
                    wControl.setWarning(translate("warn.weak.credential.shib", translationArgs));
                }
                ureq.getUserSession().putEntry(PRESENTED_PASSWORD_CHANGE_REMINDER, Boolean.TRUE);
                // activateContent(ureq, MENU_MYSETTINGS, null);
            }
        }
    }

    private AuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = (AuthenticationService) CoreSpringFactory.getBean(AuthenticationService.class);
        }
        return authenticationService;
    }

    private static WindowControl updateBusinessPath(final UserRequest ureq, final WindowControl wControl) {
        final ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ureq.getIdentity());
        final WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
        return bwControl;
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == olatMenuTree) {
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
                // process menu commands
                TreeNode selTreeNode = olatMenuTree.getSelectedNode();
                if (selTreeNode.getDelegate() != null) {
                    selTreeNode = selTreeNode.getDelegate();
                    olatMenuTree.setSelectedNode(selTreeNode); // enable right element
                }
                // test for extension commands
                final Object uObj = selTreeNode.getUserObject();
                activateContent(ureq, uObj, null);
            } else { // FIXME:fj:b what is this...the action was not allowed anymore
                content.setContent(null); // display an empty field (empty panel)
            }
        } else {
            log.warn("Unhandled olatMenuTree event: " + event.getCommand(), null);
        }
    }

    /**
     * Activate the content in the content area based on a user object representing the identifyer of the content
     * 
     * @param ureq
     * @param uObj
     * @param activation
     *            args null or argument that is passed to child
     */
    private void activateContent(final UserRequest ureq, final Object uObj, final String activationArgs) {
        if (uObj instanceof ActionExtension) {
            final ActionExtension ae = (ActionExtension) uObj;
            removeAsListenerAndDispose(resC);
            final Controller extC = ae.createController(ureq, getWindowControl(), null);
            content.setContent(extC.getInitialComponent());
            this.resC = extC;
            listenTo(resC);
            if (resC instanceof Activateable) {
                ((Activateable) resC).activate(ureq, activationArgs);
            }

        } else {
            final String cmd = (String) uObj;
            doActivate(cmd, ureq, activationArgs);
        }
    }

    private void doActivate(final String cmd, final UserRequest ureq, final String activationArgs) {
        setState(cmd);
        if (cmd.equals(MENU_ROOT)) { // check for root node clicked
            content.setContent(createRootComponent(ureq));
        } else { // create a controller
            removeAsListenerAndDispose(resC);
            this.resC = createController(cmd, ureq, getCurrentTabId(activationArgs));
            listenTo(resC);
            // activate certain state on controller
            if (activationArgs != null && resC instanceof Activateable) {
                final Activateable activatableCtr = (Activateable) resC;
                activatableCtr.activate(ureq, activationArgs);
            }
            final Component resComp = resC.getInitialComponent();
            inclTitle = createVelocityContainer("incltitle");
            inclTitle.contextPut("titleString", titleStr);
            inclTitle.contextPut("command", cmd);
            inclTitle.put("exclTitle", resComp);
            content.setContent(inclTitle);
        }
    }

    // this is used for notification tabs to set active tab(news,settings)
    private int getCurrentTabId(String activationArgs) {
        if (activationArgs == null) {
            return 0;
        }
        String[] splitted = activationArgs.split(":");
        if (isValidStringToGetTabId(splitted)) {
            return Integer.parseInt(splitted[1]);
        }
        return 0;
    }

    private boolean isValidStringToGetTabId(String[] splitted) {
        return splitted != null && splitted.length == 2;
    }

    @Override
    protected void adjustState(final ControllerState cstate, final UserRequest ureq) {
        final String cmd = cstate.getSerializedState();
        doActivate(cmd, ureq, null);
        // adjust the menu
        final TreeNode tn = TreeHelper.findNodeByUserObject(cmd, olatMenuTree.getTreeModel().getRootNode());
        olatMenuTree.setSelectedNode(tn);
    }

    /**
     * @param ureq
     * @param source
     * @param event
     */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == resC) {
            // TODO:as:a move to own controller (homepage whatever controller)
            if (event instanceof SingleIdentityChosenEvent) {
                final SingleIdentityChosenEvent foundEvent = (SingleIdentityChosenEvent) event;
                final Identity chosenIdentity = foundEvent.getChosenIdentity();
                if (chosenIdentity != null) {
                    final HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
                    final OLATResourceable ores = hpcm.loadConfigFor(chosenIdentity.getName());

                    final DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDynamicTabs();
                    final UserInfoMainController uimc = new UserInfoMainController(ureq, dts.getWindowControl(), chosenIdentity);
                    DynamicTabHelper.openResourceTab(ores, ureq, uimc, chosenIdentity.getName(), null);
                }
            }
        }
    }

    private Component createRootComponent(final UserRequest ureq) {
        // start screen
        welcome = createVelocityContainer("welcome");

        /** TODO: RSS Icon from home removed (here whole block of code removed) : bb/21.06.2012 **/

        // add portal
        Portal portal = (Portal) CoreSpringFactory.getBean("homeportal");
        Controller portalController = new PortalMainController(ureq, getWindowControl(), portal);
        listenTo(portalController);
        welcome.put("myPortal", portalController.getInitialComponent());

        return welcome;
    }

    private Controller createController(final String uobject, final UserRequest ureq, int tabId) {
        if (uobject.equals(MENU_BC)) {
            titleStr = translate("menu.bc");
            return new FolderRunController(PersonalFolderManager.getInstance().getContainer(ureq.getIdentity()), true, true, ureq, getWindowControl());
        } else if (uobject.equals(MENU_MYSETTINGS)) {
            titleStr = translate("menu.mysettings");
            return new PersonalSettingsController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_CALENDAR)) {
            titleStr = translate("menu.calendar");
            return new HomeCalendarController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_BOOKMARKS)) {
            titleStr = translate("menu.bookmarks");
            return new ManageBookmarkController(ureq, getWindowControl(), true, ManageBookmarkController.SEARCH_TYPE_ALL);
        } else if (uobject.equals(MENU_EFFICIENCY_STATEMENTS)) {
            titleStr = translate("menu.efficiencyStatements");
            return new EfficiencyStatementsListController(getWindowControl(), ureq);
        } else if (uobject.equals(MENU_ADMINNOTIFICATIONS)) {
            titleStr = translate("menu.notifications");
            return new NotificationController(ureq.getIdentity(), ureq, getWindowControl(), tabId);
            // return NotificationUIFactory.createCombinedSubscriptionsAndNewsController(ureq.getIdentity(), ureq, getWindowControl());
        } else if (uobject.equals(MENU_NOTE)) {
            titleStr = translate("menu.note");
            return new NoteListController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_OTHERUSERS)) {
            titleStr = translate("menu.otherusers");
            return new UserSearchController(ureq, getWindowControl(), false);
        } else if (uobject.equals(MENU_PORTFOLIO_ARTEFACTS)) {
            titleStr = "";
            return EPUIFactory.createPortfolioPoolController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_PORTFOLIO_MY_MAPS)) {
            titleStr = "";
            return EPUIFactory.createPortfolioMapsController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_PORTFOLIO_MY_STRUCTURED_MAPS)) {
            titleStr = "";
            return EPUIFactory.createPortfolioStructuredMapsController(ureq, getWindowControl());
        } else if (uobject.equals(MENU_PORTFOLIO_OTHERS_MAPS)) {
            titleStr = "";
            return EPUIFactory.createPortfolioMapsFromOthersController(ureq, getWindowControl());
        }
        return null;
    }

    private TreeModel buildTreeModel(final UserRequest ureq) {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(translate("menu.root"));
        root.setUserObject(MENU_ROOT);
        root.setAltText(translate("menu.root.alt"));
        gtm.setRootNode(root);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.mysettings"));
        gtn.setUserObject(MENU_MYSETTINGS);
        gtn.setAltText(translate("menu.mysettings.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.calendar"));
        gtn.setUserObject(MENU_CALENDAR);
        gtn.setAltText(translate("menu.calendar.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.notifications"));
        gtn.setUserObject(MENU_ADMINNOTIFICATIONS);
        gtn.setAltText(translate("menu.notifications.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.bookmarks"));
        gtn.setUserObject(MENU_BOOKMARKS);
        gtn.setAltText(translate("menu.bookmarks.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.bc"));
        gtn.setUserObject(MENU_BC);
        gtn.setAltText(translate("menu.bc.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.note"));
        gtn.setUserObject(MENU_NOTE);
        gtn.setAltText(translate("menu.note.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.efficiencyStatements"));
        gtn.setUserObject(MENU_EFFICIENCY_STATEMENTS);
        gtn.setAltText(translate("menu.efficiencyStatements.alt"));
        root.addChild(gtn);

        // not yet active
        // gtn = new GenericTreeNode();
        // gtn.setTitle(translate("menu.weblog"));
        // gtn.setUserObject(MENU_WEBLOG);
        // gtn.setAltText(translate("menu.weblog.alt"));
        // root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.otherusers"));
        gtn.setUserObject(MENU_OTHERUSERS);
        gtn.setAltText(translate("menu.otherusers.alt"));
        root.addChild(gtn);

        final PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
        if (portfolioModule.isEnabled()) {
            // node portfolio
            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.portfolio"));
            gtn.setUserObject(MENU_PORTFOLIO);
            gtn.setAltText(translate("menu.portfolio.alt"));
            root.addChild(gtn);

            // my artefacts
            GenericTreeNode pgtn = new GenericTreeNode();
            pgtn.setTitle(translate("menu.portfolio.myartefacts"));
            pgtn.setUserObject(MENU_PORTFOLIO_ARTEFACTS);
            pgtn.setAltText(translate("menu.portfolio.myartefacts.alt"));
            gtn.setDelegate(pgtn);
            gtn.addChild(pgtn);

            // my maps
            pgtn = new GenericTreeNode();
            pgtn.setTitle(translate("menu.portfolio.mymaps"));
            pgtn.setUserObject(MENU_PORTFOLIO_MY_MAPS);
            pgtn.setAltText(translate("menu.portfolio.mymaps.alt"));
            gtn.addChild(pgtn);

            // my exercises
            pgtn = new GenericTreeNode();
            pgtn.setTitle(translate("menu.portfolio.mystructuredmaps"));
            pgtn.setUserObject(MENU_PORTFOLIO_MY_STRUCTURED_MAPS);
            pgtn.setAltText(translate("menu.portfolio.mystructuredmaps.alt"));
            gtn.addChild(pgtn);

            // others maps
            pgtn = new GenericTreeNode();
            pgtn.setTitle(translate("menu.portfolio.othermaps"));
            pgtn.setUserObject(MENU_PORTFOLIO_OTHERS_MAPS);
            pgtn.setAltText(translate("menu.portfolio.othermaps.alt"));
            gtn.addChild(pgtn);
        }

        // add extension menues
        final ExtManager extm = CoreSpringFactory.getBean(ExtManager.class);
        final Class<? extends HomeMainController> extensionPointMenu = this.getClass();
        final int cnt = extm.getExtensionCnt();
        for (int i = 0; i < cnt; i++) {
            final Extension anExt = extm.getExtension(i);
            // check for extensions
            final ActionExtension ae = (ActionExtension) anExt.getExtensionFor(extensionPointMenu.getName());
            if (ae != null) {
                if (anExt.isEnabled()) {
                    gtn = new GenericTreeNode();
                    final String menuText = ae.getActionText(getLocale());
                    gtn.setTitle(menuText);
                    gtn.setUserObject(ae);
                    gtn.setAltText(ae.getDescription(getLocale()));

                    if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getNodeIdentifierIfParent() != null) {
                        gtn.setIdent(((GenericActionExtension) ae).getNodeIdentifierIfParent());
                    }
                    if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getParentTreeNodeIdentifier() != null) {
                        final GenericTreeNode parentNode = (GenericTreeNode) gtm.getNodeById(((GenericActionExtension) ae).getParentTreeNodeIdentifier());
                        if (parentNode == null) {
                            throw new AssertException("could not find parent treeNode: " + ((GenericActionExtension) ae).getParentTreeNodeIdentifier()
                                    + ", make sure it gets loaded before child!");
                        }
                        parentNode.addChild(gtn);
                        if (parentNode.getDelegate() == null) {
                            parentNode.setDelegate(gtn);
                        }
                    } else {
                        root.addChild(gtn);
                    }

                }
            }
        }

        return gtm;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers are disposed in BasicController
    }

    /**
	 */
    @Override
    public void activate(final UserRequest ureq, String viewIdentifier) {

        String subViewIdentifier = null;
        final int firstDot = viewIdentifier.indexOf(".");
        if (firstDot != -1) {
            subViewIdentifier = viewIdentifier.substring(firstDot + 1, viewIdentifier.length());
            viewIdentifier = viewIdentifier.substring(0, firstDot);
        }
        final String[] parsedViewIdentifyers = viewIdentifier.split(":");

        // find the menu node that has the user object that represents the
        // viewIdentifyer
        final TreeNode rootNode = this.olatMenuTree.getTreeModel().getRootNode();
        TreeNode activatedNode = TreeHelper.findNodeByUserObject(parsedViewIdentifyers[0], rootNode);
        if (activatedNode == null) {
            activatedNode = findPortfolioNode(rootNode, parsedViewIdentifyers);
            if (activatedNode != null) {
                subViewIdentifier = parsedViewIdentifyers[1];
            }
        }

        if (activatedNode != null) {
            this.olatMenuTree.setSelectedNodeId(activatedNode.getIdent());
            activateContent(ureq, activatedNode.getUserObject(), subViewIdentifier);
        } else {
            // not found, activate the root node
            this.olatMenuTree.setSelectedNodeId(rootNode.getIdent());
            activateContent(ureq, rootNode.getUserObject(), subViewIdentifier);
        }
    }

    public TreeNode findPortfolioNode(final TreeNode rootNode, final String[] parsedViewIdentifyers) {
        final String context = parsedViewIdentifyers[0];
        if ("EPDefaultMap".equals(context) || "EPStructuredMap".equals(context)) {
            // it's my problem
            final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean(EPFrontendManager.class);

            final Long key = Long.parseLong(parsedViewIdentifyers[1]);
            final OLATResourceable ores = OresHelper.createOLATResourceableInstance(context, key);
            final boolean owner = ePFMgr.isMapOwner(getIdentity(), ores);
            if (owner) {
                if ("EPDefaultMap".equals(context)) {
                    return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_MY_MAPS, rootNode);
                } else if ("EPStructuredMap".equals(context)) {
                    return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_MY_STRUCTURED_MAPS, rootNode);
                } else {
                    log.warn("Unhandled portfolio map type: " + parsedViewIdentifyers, null);
                }
            } else {
                return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_OTHERS_MAPS, rootNode);
            }
        }
        return null;
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
