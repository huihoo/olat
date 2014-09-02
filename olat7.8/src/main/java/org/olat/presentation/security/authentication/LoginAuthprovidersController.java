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
package org.olat.presentation.security.authentication;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.admin.sysinfo.MaintenanceMsgManager;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.commons.i18n.I18nModule;
import org.olat.lms.commons.tree.INode;
import org.olat.lms.commons.util.License;
import org.olat.lms.commons.util.collection.ArrayHelper;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.presentation.commons.AjaxSettings;
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
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.framework.layout.fullWebApp.BaseFullWebappController;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.system.commons.Settings;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Description:<br>
 * TODO: patrickb Class Description for LoginAuthprovidersController
 * <P>
 * Initial Date: 02.09.2007 <br>
 * 
 * @author patrickb
 */
public class LoginAuthprovidersController extends MainLayoutBasicController {

    private static final String ACTION_LOGIN = "login";
    public static final String ATTR_LOGIN_PROVIDER = "lp";
    private static final String ACTION_COOKIES = "cookies";
    private static final String ACTION_ABOUT = "about";
    private static final String ACTION_ACCESSIBILITY = "accessibility";
    private static final String ACTION_BROWSERCHECK = "check";
    private static final String ACTION_GUEST = "guest";

    private VelocityContainer content;
    private Controller authController;
    private final Panel dmzPanel;
    private final MenuTree olatMenuTree;
    private final LayoutMain3ColsController columnLayoutCtr;

    public LoginAuthprovidersController(final UserRequest ureq, final WindowControl wControl) {
        // Use fallback translator from full webapp package to translate accessibility stuff
        super(ureq, wControl, PackageUtil.createPackageTranslator(BaseFullWebappController.class, ureq.getLocale()));
        //
        if (ureq.getUserSession().getEntry("error.change.email") != null) {
            wControl.setError(ureq.getUserSession().getEntry("error.change.email").toString());
            ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email");
        }
        if (ureq.getUserSession().getEntry("error.change.email.time") != null) {
            wControl.setError(ureq.getUserSession().getEntry("error.change.email.time").toString());
            ureq.getUserSession().removeEntryFromNonClearedStore("error.change.email.time");
        }

        dmzPanel = new Panel("content");
        content = initLoginContent(ureq, null);
        dmzPanel.pushContent(content);

        // DMZ navigation
        olatMenuTree = new MenuTree("olatMenuTree");
        final TreeModel tm = buildTreeModel();
        olatMenuTree.setTreeModel(tm);
        olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
        olatMenuTree.addListener(this);

        // Activate correct position in menu
        final INode firstChild = tm.getRootNode().getChildAt(0);
        olatMenuTree.setSelectedNodeId(firstChild.getIdent());

        columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, dmzPanel, "useradminmain");
        columnLayoutCtr.addCssClassToMain("o_loginscreen");
        listenTo(columnLayoutCtr); // for later autodisposing
        putInitialPanel(columnLayoutCtr.getInitialComponent());
    }

    private VelocityContainer initLoginContent(final UserRequest ureq, String provider) {
        // in every case we build the container for pages to fill the panel
        final VelocityContainer contentBorn = createVelocityContainer("login");

        // REVIEW:12-2007:CodeCleanup
        // // is this a logout request?
        // if (ureq.getHttpReq().getPathInfo().equals(DispatcherAction.getPathDefault() + AuthHelper.LOGOUT_PAGE)) {
        // content.setPage(VELOCITY_ROOT + "/logout.html");
        // return content;
        // }

        // browser not supported messages
        // true if browserwarning should be showed
        final boolean bwo = AjaxSettings.isBrowserAjaxBlacklisted(ureq);
        contentBorn.contextPut("browserWarningOn", bwo ? Boolean.TRUE : Boolean.FALSE);

        // prepare login
        if (provider == null) {
            provider = LoginModule.getDefaultProviderName();
        }
        final AuthenticationProvider authProvider = LoginModule.getAuthenticationProvider(provider);
        if (authProvider == null) {
            throw new AssertException("Invalid authentication provider: " + provider);
        }

        authController = authProvider.createController(ureq, getWindowControl());
        listenTo(authController);
        contentBorn.put("loginComp", authController.getInitialComponent());
        final Collection<AuthenticationProvider> providers = LoginModule.getAuthenticationProviders();
        final List<AuthenticationProvider> providerSet = new ArrayList<AuthenticationProvider>(providers.size());
        for (final AuthenticationProvider prov : providers) {
            if (prov.isEnabled()) {
                providerSet.add(prov);
            }
        }
        providerSet.remove(authProvider); // remove active authProvider from list of alternate authProviders
        contentBorn.contextPut("providerSet", providerSet);
        contentBorn.contextPut("locale", ureq.getLocale());

        // prepare info message
        final MaintenanceMsgManager mrg = (MaintenanceMsgManager) CoreSpringFactory.getBean(MaintenanceMsgManager.class);
        final String infomsg = mrg.getMaintenanceMessage();
        if (infomsg != null && infomsg.length() > 0) {
            contentBorn.contextPut("infomsg", infomsg);
        }

        final String infomsgNode = mrg.getInfoMessageNodeOnly();
        if (infomsgNode != null && infomsgNode.length() > 0) {
            contentBorn.contextPut("infomsgNode", infomsgNode);
        }

        return contentBorn;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == olatMenuTree) {
            if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { // process menu commands
                final TreeNode selTreeNode = olatMenuTree.getSelectedNode();
                final String cmd = (String) selTreeNode.getUserObject();
                //
                dmzPanel.popContent();
                if (cmd.equals(ACTION_LOGIN)) {
                    content = initLoginContent(ureq, LoginModule.getDefaultProviderName());
                    dmzPanel.pushContent(content);
                } else if (cmd.equals(ACTION_GUEST)) {
                    final int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
                    if (loginStatus == AuthHelper.LOGIN_OK) {
                        return;
                    } else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                        getWindowControl().setError(translate("login.notavailable", WebappHelper.getMailConfig("mailSupport")));
                    } else {
                        getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
                    }
                } else if (cmd.equals(ACTION_BROWSERCHECK)) {
                    final VelocityContainer browserCheck = createVelocityContainer("browsercheck");
                    browserCheck.contextPut("isBrowserAjaxReady", Boolean.valueOf(!AjaxSettings.isBrowserAjaxBlacklisted(ureq)));
                    dmzPanel.pushContent(browserCheck);
                } else if (cmd.equals(ACTION_COOKIES)) {
                    dmzPanel.pushContent(createVelocityContainer("cookies"));
                } else if (cmd.equals(ACTION_ABOUT)) {
                    final VelocityContainer aboutVC = createVelocityContainer("about");
                    // Add version info and licenses
                    aboutVC.contextPut("version", Settings.getFullVersionInfo());
                    aboutVC.contextPut("license", License.getOlatLicense());
                    // Add translator and languages info
                    final I18nManager i18nMgr = I18nManager.getInstance();
                    final Set<String> enabledKeysSet = I18nModule.getEnabledLanguageKeys();
                    final Map<String, String> langNames = new HashMap<String, String>();
                    final Map<String, String> langTranslators = new HashMap<String, String>();
                    final String[] enabledKeys = ArrayHelper.toArray(enabledKeysSet);
                    final String[] names = new String[enabledKeys.length];
                    for (int i = 0; i < enabledKeys.length; i++) {
                        final String key = enabledKeys[i];
                        final String langName = i18nMgr.getLanguageInEnglish(key, I18nModule.isOverlayEnabled());
                        langNames.put(key, langName);
                        names[i] = langName;
                        final String author = i18nMgr.getLanguageAuthor(key);
                        langTranslators.put(key, author);
                    }
                    ArrayHelper.sort(enabledKeys, names, true, true, true);
                    aboutVC.contextPut("enabledKeys", enabledKeys);
                    aboutVC.contextPut("langNames", langNames);
                    aboutVC.contextPut("langTranslators", langTranslators);
                    dmzPanel.pushContent(aboutVC);
                } else if (cmd.equals(ACTION_ACCESSIBILITY)) {
                    final VelocityContainer accessibilityVC = createVelocityContainer("accessibility");
                    dmzPanel.pushContent(accessibilityVC);
                }
            }
        } else if (event.getCommand().equals(ACTION_LOGIN)) {
            // show traditional login page
            dmzPanel.popContent();
            content = initLoginContent(ureq, ureq.getParameter(ATTR_LOGIN_PROVIDER));
            dmzPanel.pushContent(content);
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (event == Event.CANCELLED_EVENT) {
            // is a Form cancelled, show Login Form
            content = initLoginContent(ureq, null);
            dmzPanel.setContent(content);
        } else if (event instanceof AuthenticationEvent) {
            final AuthenticationEvent authEvent = (AuthenticationEvent) event;
            final Identity identity = authEvent.getIdentity();
            final int loginStatus = AuthHelper.doLogin(identity, AUTHENTICATION_PROVIDER_OLAT, ureq);
            if (loginStatus == AuthHelper.LOGIN_OK) {
                return;
            } else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                // getWindowControl().setError(translate("login.notavailable", OLATContext.getSupportaddress()));
                DispatcherAction.redirectToServiceNotAvailable(ureq.getHttpResp());
            } else {
                getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
            }

        }
    }

    private TreeModel buildTreeModel() {
        GenericTreeNode root, gtn;

        final GenericTreeModel gtm = new GenericTreeModel();
        root = new GenericTreeNode();
        root.setTitle(translate("menu.root"));
        root.setUserObject(ACTION_LOGIN);
        root.setAltText(translate("menu.root.alt"));
        gtm.setRootNode(root);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.login"));
        gtn.setUserObject(ACTION_LOGIN);
        gtn.setAltText(translate("menu.login.alt"));
        root.addChild(gtn);
        root.setDelegate(gtn);

        if (LoginModule.isGuestLoginLinksEnabled()) {
            gtn = new GenericTreeNode();
            gtn.setTitle(translate("menu.guest"));
            gtn.setUserObject(ACTION_GUEST);
            gtn.setAltText(translate("menu.guest.alt"));
            root.addChild(gtn);
        }

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.check"));
        gtn.setUserObject(ACTION_BROWSERCHECK);
        gtn.setAltText(translate("menu.check.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.accessibility"));
        gtn.setUserObject(ACTION_ACCESSIBILITY);
        gtn.setAltText(translate("menu.accessibility.alt"));
        root.addChild(gtn);

        gtn = new GenericTreeNode();
        gtn.setTitle(translate("menu.about"));
        gtn.setUserObject(ACTION_ABOUT);
        gtn.setAltText(translate("menu.about.alt"));
        root.addChild(gtn);

        return gtm;
    }

}
