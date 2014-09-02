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

import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.registration.RegistrationModule;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.registration.RegistrationServiceImpl;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.lms.user.UserModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.registration.DisclaimerController;
import org.olat.presentation.registration.PwChangeController;
import org.olat.presentation.registration.RegistrationController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 04.08.2004
 * 
 * @author Mike Stock
 */
public class OLATAuthenticationController extends AuthenticationController {

    private static final Logger log = LoggerHelper.getLogger();
    public static final String PARAM_LOGINERROR = "loginerror";

    private final VelocityContainer loginComp;
    private final OLATAuthentcationForm loginForm;
    private Identity authenticatedIdentity;
    private Controller subController;
    private DisclaimerController disclaimerCtr;

    private CloseableModalController cmc;

    private Link pwLink;
    private Link registerLink;
    private Link anoLink;

    /**
	 */
    public OLATAuthenticationController(final UserRequest ureq, final WindowControl winControl) {
        // use fallback translator to registration module
        super(ureq, winControl, PackageUtil.createPackageTranslator(RegistrationServiceImpl.class, ureq.getLocale()));

        loginComp = createVelocityContainer("olatlogin");

        if (UserModule.isPwdchangeallowed()) {
            pwLink = LinkFactory.createLink("menu.pw", loginComp, this);
            pwLink.setCustomEnabledLinkCSS("o_login_pwd");
        }

        if (RegistrationModule.isSelfRegistrationEnabled()) {
            registerLink = LinkFactory.createLink("menu.register", loginComp, this);
            registerLink.setCustomEnabledLinkCSS("o_login_register");
        }

        if (LoginModule.isGuestLoginLinksEnabled()) {
            anoLink = LinkFactory.createLink("menu.guest", loginComp, this);
            anoLink.setCustomEnabledLinkCSS("o_login_guests");
        }

        // prepare login form
        loginForm = new OLATAuthentcationForm(ureq, winControl, getTranslator());
        listenTo(loginForm);

        loginComp.put("loginForm", loginForm.getInitialComponent());

        // Check if form is triggered by external loginworkflow that has been failed
        if (ureq.getParameterSet().contains(PARAM_LOGINERROR)) {
            showError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
        }

        // support email
        loginComp.contextPut("supportmailaddress", WebappHelper.getMailConfig("mailSupport"));
        putInitialPanel(loginComp);
    }

    /**
	 */
    @Override
    public void changeLocale(final Locale newLocale) {
        setLocale(newLocale, true);
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {

        if (source == registerLink) {
            removeAsListenerAndDispose(subController);
            subController = new RegistrationController(ureq, getWindowControl());
            listenTo(subController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();

        } else if (source == pwLink) {

            // double-check if allowed first
            if (!UserModule.isPwdchangeallowed()) {
                throw new OLATSecurityException("chose password to be changed, but disallowed by config");
            }

            removeAsListenerAndDispose(subController);
            subController = new PwChangeController(ureq, getWindowControl());
            listenTo(subController);

            removeAsListenerAndDispose(cmc);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), subController.getInitialComponent());
            listenTo(cmc);

            cmc.activate();

        } else if (source == anoLink) {

            final int loginStatus = AuthHelper.doAnonymousLogin(ureq, ureq.getLocale());
            if (loginStatus == AuthHelper.LOGIN_OK) {
                return;
            } else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                showError("login.notavailable", WebappHelper.getMailConfig("mailSupport"));
            } else {
                showError("login.error", WebappHelper.getMailConfig("mailSupport"));
            }
        }
    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        if (source == loginForm && event == Event.DONE_EVENT) {
            final String login = loginForm.getLogin();
            final String pass = loginForm.getPass();

            // if login is blocked bye, bye
            if (LoginModule.isLoginBlocked(login)) {
                showLoginBlocked(login);
                return;
            }

            AuthenticationService authenticationService = (AuthenticationService) CoreSpringFactory.getBean(AuthenticationService.class);
            authenticatedIdentity = authenticationService.authenticate(login, pass, Provider.OLAT);
            if (authenticatedIdentity == null) {
                if (LoginModule.registerFailedLoginAttempt(login)) {
                    showLoginBlocked(login);
                    return;
                } else {
                    showError("login.error", WebappHelper.getMailConfig("mailSupport"));
                    return;
                }
            }

            LoginModule.clearFailedLoginAttempts(login);

            // Check if disclaimer has been accepted
            if (getRegistrationService().needsToConfirmDisclaimer(authenticatedIdentity)) {
                // accept disclaimer first

                removeAsListenerAndDispose(disclaimerCtr);
                disclaimerCtr = new DisclaimerController(ureq, getWindowControl());
                listenTo(disclaimerCtr);

                removeAsListenerAndDispose(cmc);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), disclaimerCtr.getInitialComponent());
                listenTo(cmc);

                cmc.activate();

            } else {
                // disclaimer acceptance not required
                authenticated(ureq, authenticatedIdentity);
            }
        }

        if (source == disclaimerCtr) {
            cmc.deactivate();
            if (event == Event.DONE_EVENT) {
                // disclaimer accepted
                getRegistrationService().setHasConfirmedDislaimer(authenticatedIdentity);
                authenticated(ureq, authenticatedIdentity);
            }
        }

        if (source == subController && event == Event.CANCELLED_EVENT) {
            cmc.deactivate();
        }
    }

    private void showLoginBlocked(final String login) {
        log.info("Audit:Too many failed login attempts for " + login + ". Login blocked.", null);
        showError("login.blocked", LoginModule.getAttackPreventionTimeoutMin().toString());
        return;
    }

    /**
	 */
    @Override
    protected void doDispose() {
        //
    }

    private RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

}
