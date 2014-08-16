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

package org.olat.presentation.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.data.basesecurity.Identity;
import org.olat.data.calendar.OlatCalendar;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.callbacks.VFSSecurityCallback;
import org.olat.data.commons.vfs.olatimpl.OlatNamedContainerImpl;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.lms.calendar.CalendarConfig;
import org.olat.lms.calendar.CalendarService;
import org.olat.lms.commons.context.BusinessControl;
import org.olat.lms.commons.context.ContextEntry;
import org.olat.lms.commons.mail.ContactMessage;
import org.olat.lms.commons.vfs.securitycallbacks.ReadOnlyCallback;
import org.olat.lms.user.HomePageConfig;
import org.olat.lms.user.HomePageConfigManager;
import org.olat.lms.user.HomePageConfigManagerImpl;
import org.olat.lms.user.UserService;
import org.olat.presentation.calendar.WeeklyCalendarController;
import org.olat.presentation.calendar.components.CalendarRenderWrapper;
import org.olat.presentation.contactform.ContactFormController;
import org.olat.presentation.filebrowser.FolderRunController;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.tree.GenericTreeModel;
import org.olat.presentation.framework.core.components.tree.GenericTreeNode;
import org.olat.presentation.framework.core.components.tree.MenuTree;
import org.olat.presentation.framework.core.components.tree.TreeNode;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.MainLayoutBasicController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.portfolio.EPUIFactory;
import org.olat.system.commons.configuration.ConfigOnOff;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.event.Event;
import org.olat.system.mail.ContactList;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: July 26, 2005
 * 
 * @author Alexander Schneider Comment: TODO as dokumentation
 */
public class UserInfoMainController extends MainLayoutBasicController {

    private static final String CMD_HOMEPAGE = "homepage";
    private static final String CMD_CALENDAR = "calendar";
    private static final String CMD_FOLDER = "folder";
    private static final String CMD_CONTACT = "contact";
    private static final String CMD_WEBLOG = "weblog";
    private static final String CMD_PORTFOLIO = "portfolio";

    private final MenuTree menuTree;
    private VelocityContainer myContent;
    private final Panel main;

    public static final OLATResourceable BUSINESS_CONTROL_TYPE_FOLDER = OresHelper.createOLATResourceableTypeWithoutCheck(FolderRunController.class.getSimpleName());

    private HomePageDisplayController homePageDisplayController;
    private WeeklyCalendarController calendarController;
    private ContactFormController contactFormController;
    private FolderRunController folderRunController;

    private final Identity chosenIdentity;
    private final String firstLastName;
    private Controller weblogController;
    private Controller portfolioController;
    private final CalendarService calendarService;

    /**
     * @param ureq
     * @param wControl
     * @param chosenIdentity
     */
    public UserInfoMainController(final UserRequest ureq, final WindowControl wControl, final Identity chosenIdentity) {
        super(ureq, wControl);
        calendarService = CoreSpringFactory.getBean(CalendarService.class);
        this.chosenIdentity = chosenIdentity;

        main = new Panel("userinfomain");

        main.setContent(createComponent(ureq, CMD_HOMEPAGE, chosenIdentity));

        this.firstLastName = getUserService().getFirstAndLastname(chosenIdentity.getUser());

        // Navigation menu
        this.menuTree = new MenuTree("menuTree");
        final GenericTreeModel tm = buildTreeModel(firstLastName);
        menuTree.setTreeModel(tm);
        menuTree.setSelectedNodeId(tm.getRootNode().getChildAt(0).getIdent());
        menuTree.addListener(this);

        final LayoutMain3ColsController columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, null, main, "userinfomain");
        listenTo(columnLayoutCtr);
        //
        putInitialPanel(columnLayoutCtr.getInitialComponent());

        // Activate child controllers if a usable context entry is found
        final BusinessControl bc = getWindowControl().getBusinessControl();
        final ContextEntry ce = bc.popLauncherContextEntry();
        if (ce != null) { // a context path is left for me
            final OLATResourceable ores = ce.getOLATResourceable();
            if (OresHelper.equals(ores, BUSINESS_CONTROL_TYPE_FOLDER)) {
                // Activate folder controller
                menuTree.setSelectedNode(tm.findNodeByUserObject(CMD_FOLDER));
                main.setContent(createComponent(ureq, CMD_FOLDER, chosenIdentity));
            }
        }

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == menuTree) {
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // goto
                final TreeNode selTreeNode = menuTree.getSelectedNode();
                final String cmd = (String) selTreeNode.getUserObject();
                main.setContent(createComponent(ureq, cmd, chosenIdentity));
            }
        }
        // no events from main
        // no events from intro
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        //
    }

    /**
     * Generates the archiver menu
     * 
     * @return The generated menu tree model
     * @param firstLastName
     */
    private GenericTreeModel buildTreeModel(final String name) {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(name);
        root.setAltText(name);
        root.setAccessible(false);
        gtm.setRootNode(root);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.homepage"));
        gtn.setUserObject(CMD_HOMEPAGE);
        gtn.setAltText(translate("menu.homepage.alt"));
        root.addChild(gtn);

        if (!chosenIdentity.getStatus().equals(Identity.STATUS_DELETED)) {
            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.calendar"));
            gtn.setUserObject(CMD_CALENDAR);
            gtn.setAltText(translate("menu.calendar.alt"));
            root.addChild(gtn);

            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.folder"));
            gtn.setUserObject(CMD_FOLDER);
            gtn.setAltText(translate("menu.folder.alt"));
            root.addChild(gtn);

            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.contact"));
            gtn.setUserObject(CMD_CONTACT);
            gtn.setAltText(translate("menu.contact.alt"));
            root.addChild(gtn);

            final ConfigOnOff portfolioModule = (ConfigOnOff) CoreSpringFactory.getBean("portfolioModule");
            if (portfolioModule.isEnabled()) {
                gtn = new GenericTreeNode();
                gtn.setTitle(translate("menu.portfolio"));
                gtn.setUserObject(CMD_PORTFOLIO);
                gtn.setAltText(translate("menu.portfolio.alt"));
                root.addChild(gtn);
            }

            // TODO:gs only show weblog if user wants this and a weblog exists
            // not yet active
            // gtn = new GenericTreeNode();
            // gtn.setTitle(translate("menu.weblog"));
            // gtn.setUserObject(CMD_WEBLOG);
            // gtn.setAltText(translate("menu.weblog.alt"));
            // root.addChild(gtn);
        }
        return gtm;
    }

    private Component createComponent(final UserRequest ureq, final String menuCommand, final Identity identity) {
        myContent = createVelocityContainer("userinfo");

        if (menuCommand.equals(CMD_HOMEPAGE)) {
            final String userName = identity.getName();
            final HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
            final HomePageConfig homePageConfig = hpcm.loadConfigFor(userName);
            removeAsListenerAndDispose(homePageDisplayController);
            homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), homePageConfig);
            listenTo(homePageDisplayController);
            myContent.put("userinfo", homePageDisplayController.getInitialComponent());

        } else if (menuCommand.equals(CMD_CALENDAR)) {
            OlatCalendar cal = calendarService.getPersonalCalendar(identity);
            final CalendarRenderWrapper calendarWrapper = CalendarRenderWrapper.wrapPersonalCalendar(cal, identity.getName());
            calendarWrapper.setCalendarConfig(new CalendarConfig(identity.getName(), CalendarRenderWrapper.CALENDAR_COLOR_BLUE, true));
            final CalendarConfig config = calendarService.findCalendarConfigForIdentity(calendarWrapper.getCalendar(), ureq.getUserSession().getGuiPreferences());
            if (config != null) {
                calendarWrapper.getCalendarConfig().setCss(config.getCss());
                calendarWrapper.getCalendarConfig().setVis(config.isVis());
            }
            if (ureq.getUserSession().getRoles().isOLATAdmin() || identity.getName().equals(ureq.getIdentity().getName())) {
                calendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_WRITE);
            } else {
                calendarWrapper.setAccess(CalendarRenderWrapper.ACCESS_READ_ONLY);
            }
            final List calendars = new ArrayList();
            calendars.add(calendarWrapper);
            removeAsListenerAndDispose(calendarController);
            calendarController = new WeeklyCalendarController(ureq, getWindowControl(), calendars, WeeklyCalendarController.CALLER_PROFILE, true);
            listenTo(calendarController);
            myContent.put("userinfo", calendarController.getInitialComponent());
        } else if (menuCommand.equals(CMD_FOLDER)) {

            final String chosenUserFolderRelPath = FolderConfig.getUserHome(identity.getName()) + "/public";

            final OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(chosenUserFolderRelPath, null);
            final OlatNamedContainerImpl namedFolder = new OlatNamedContainerImpl(firstLastName, rootFolder);

            // decided in plenum to have read only view in the personal visit card, even for admin
            final VFSSecurityCallback secCallback = new ReadOnlyCallback();
            namedFolder.setLocalSecurityCallback(secCallback);

            removeAsListenerAndDispose(folderRunController);
            folderRunController = new FolderRunController(namedFolder, false, true, ureq, getWindowControl());
            listenTo(folderRunController);
            myContent.put("userinfo", folderRunController.getInitialComponent());

        } else if (menuCommand.equals(CMD_CONTACT)) {
            final ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
            final ContactList emailList = new ContactList(firstLastName);
            emailList.add(identity);
            cmsg.addEmailTo(emailList);
            removeAsListenerAndDispose(contactFormController);
            contactFormController = new ContactFormController(ureq, getWindowControl(), true, true, false, false, cmsg);
            listenTo(contactFormController);
            myContent.put("userinfo", contactFormController.getInitialComponent());
        } else if (menuCommand.equals(CMD_WEBLOG)) {
            // weblogController = new WeblogMainController(ureq, getWindowControl(), chosenIdentity);
            // listenTo(weblogController);
            // myContent.put("userinfo", weblogController.getInitialComponent());
        } else if (menuCommand.equals(CMD_PORTFOLIO)) {
            removeAsListenerAndDispose(portfolioController);
            portfolioController = EPUIFactory.createPortfolioMapsVisibleToOthersController(ureq, getWindowControl(), chosenIdentity);
            listenTo(portfolioController);
            myContent.put("userinfo", portfolioController.getInitialComponent());
        }
        return myContent;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // controllers are disposed by BasicController
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
