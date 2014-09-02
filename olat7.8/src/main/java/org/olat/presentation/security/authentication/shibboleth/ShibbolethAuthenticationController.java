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

package org.olat.presentation.security.authentication.shibboleth;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.lms.security.authentication.shibboleth.SwitchShibbolethAuthenticationConfigurator;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.presentation.security.authentication.AuthenticationController;
import org.olat.presentation.security.authentication.LoginAuthprovidersController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 04.08.2004
 * 
 * @author Mike Stock
 *         <P>
 *         Description:<br>
 *         Replaces the old ShibbolethAuthenticationController which used to have an own WAYF.
 *         <p>
 *         This ShibbolethAuthenticationController uses the EmbeddedWAYF provided by SWITCH (see the shibbolethlogin.html)
 */

public class ShibbolethAuthenticationController extends AuthenticationController {
    protected static final String IDP_HOMESITE_COOKIE = "idpsite-presel";

    private Translator fallbackTranslator;
    private final VelocityContainer loginComp;
    private Link anoLink;

    private static final Logger log = LoggerHelper.getLogger();

    private final Panel mainPanel;

    public ShibbolethAuthenticationController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);
        // extends authControll which is a BasicController, so we have to set the
        // Manually set translator that uses a fallback translator to the login module
        // Can't use constructor with fallback translator because it gets overriden by setBasePackage call above
        setTranslator(PackageUtil.createPackageTranslator(LoginAuthprovidersController.class, ureq.getLocale(),
                PackageUtil.createPackageTranslator(LoginModule.class, ureq.getLocale())));

        if (!ShibbolethModule.isEnableShibbolethLogins()) {
            throw new OLATSecurityException("Tried to access shibboleth wayf but shibboleth is not enabled.");
        }
        loginComp = createVelocityContainer("shibbolethlogin");

        if (LoginModule.isGuestLoginLinksEnabled()) {
            anoLink = LinkFactory.createLink("menu.guest", loginComp, this);
            anoLink.setCustomEnabledLinkCSS("o_login_guests");
        }
        final SwitchShibbolethAuthenticationConfigurator config = (SwitchShibbolethAuthenticationConfigurator) CoreSpringFactory
                .getBean(SwitchShibbolethAuthenticationConfigurator.class);
        loginComp.contextPut("wayfSPEntityID", config.getWayfSPEntityID());
        loginComp.contextPut("wayfSPHandlerURL", config.getWayfSPHandlerURL());
        loginComp.contextPut("wayfSPSamlDSURL", config.getWayfSPSamlDSURL());
        loginComp.contextPut("wayfReturnUrl", config.getWayfReturnUrl());
        loginComp.contextPut("additionalIDPs", config.getAdditionalIdentityProviders());

        // displays warning after logout
        // logout=true is set by the AuthHelper.doLogout(..) as URL param
        // assuming the Shibboleth Authentication Controller is the first on the DMZ
        // this check shows an info message, that for a complete logout the browser must be closed
        // important for users on public work stations
        final String param = ureq.getParameter("logout");
        if (param != null && param.equals("true")) {
            showWarning("info.browser.close");
        }

        mainPanel = putInitialPanel(loginComp);
    }

    /**
	 */
    @Override
    public void changeLocale(final Locale newLocale) {
        getTranslator().setLocale(newLocale);
        fallbackTranslator.setLocale(newLocale);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == anoLink) {
            final int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
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

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to do here.
    }

}
