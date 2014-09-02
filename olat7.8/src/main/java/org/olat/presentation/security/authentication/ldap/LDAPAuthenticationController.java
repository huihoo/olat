package org.olat.presentation.security.authentication.ldap;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.registration.RegistrationServiceImpl;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.lms.security.authentication.LoginModule;
import org.olat.lms.security.authentication.ldap.LDAPError;
import org.olat.lms.security.authentication.ldap.LDAPLoginManager;
import org.olat.lms.security.authentication.ldap.LDAPLoginModule;
import org.olat.lms.user.UserModule;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.closablewrapper.CloseableModalController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.registration.DisclaimerController;
import org.olat.presentation.registration.PwChangeController;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.presentation.security.authentication.AuthenticationController;
import org.olat.presentation.security.authentication.LoginAuthprovidersController;
import org.olat.presentation.security.authentication.OLATAuthentcationForm;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.exception.OLATSecurityException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

public class LDAPAuthenticationController extends AuthenticationController {

    private static final Logger log = LoggerHelper.getLogger();
    private final VelocityContainer loginComp;
    private Link pwLink;
    private Link anoLink;
    private Controller subController;
    private final OLATAuthentcationForm loginForm;
    private DisclaimerController disclaimerCtr;
    private Identity authenticatedIdentity;
    private String provider = null;

    private CloseableModalController cmc;

    public LDAPAuthenticationController(final UserRequest ureq, final WindowControl control) {
        // use fallback translator to login and registration package
        super(ureq, control, PackageUtil.createPackageTranslator(LoginAuthprovidersController.class, ureq.getLocale(),
                PackageUtil.createPackageTranslator(RegistrationServiceImpl.class, ureq.getLocale())));

        loginComp = createVelocityContainer("ldaplogin");

        if (UserModule.isPwdchangeallowed() && LDAPLoginModule.isPropagatePasswordChangedOnLdapServer()) {
            pwLink = LinkFactory.createLink("menu.pw", loginComp, this);
            pwLink.setCustomEnabledLinkCSS("o_login_pwd");
        }
        if (LoginModule.isGuestLoginLinksEnabled()) {
            anoLink = LinkFactory.createLink("menu.guest", loginComp, this);
            anoLink.setCustomEnabledLinkCSS("o_login_guests");
        }

        // Use the standard OLAT login form but with our LDAP translator
        loginForm = new OLATAuthentcationForm(ureq, control, getTranslator());
        listenTo(loginForm);

        loginComp.put("ldapForm", loginForm.getInitialComponent());

        putInitialPanel(loginComp);
    }

    @Override
    public void changeLocale(final Locale newLocale) {
        setLocale(newLocale, true);
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == pwLink) {
            // double-check if allowed first
            if (!UserModule.isPwdchangeallowed() || !LDAPLoginModule.isPropagatePasswordChangedOnLdapServer()) {
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
            if (AuthHelper.doAnonymousLogin(ureq, ureq.getLocale()) == AuthHelper.LOGIN_OK) {
                return;
            } else {
                showError("login.error", WebappHelper.getMailConfig("mailSupport"));
            }
        }
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {

        final LDAPError ldapError = new LDAPError();

        if (source == loginForm && event == Event.DONE_EVENT) {

            final String login = loginForm.getLogin();
            final String pass = loginForm.getPass();
            /* STATIC_METHOD_REFACTORING moved from code below here */
            AuthenticationService authenticationService = (AuthenticationService) CoreSpringFactory.getBean(AuthenticationService.class);
            authenticatedIdentity = authenticationService.authenticate(login, pass, ldapError);

            if (authenticatedIdentity != null) {
                provider = AUTHENTICATION_PROVIDER_LDAP;
            } else {
                // try fallback to OLAT provider if configured
                if (LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin()) {

                    authenticatedIdentity = authenticationService.authenticate(login, pass, Provider.OLAT);
                }
                if (authenticatedIdentity != null) {
                    provider = AUTHENTICATION_PROVIDER_OLAT;
                }
            }
            // Still not found? register for hacking attempts
            if (authenticatedIdentity == null) {
                if (LoginModule.registerFailedLoginAttempt(login)) {
                    log.info("Audit:Too many failed login attempts for " + login + ". Login blocked.", null);
                    showError("login.blocked", LoginModule.getAttackPreventionTimeoutMin().toString());
                    return;
                } else {
                    showError("login.error", ldapError.get());
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
                doLoginAndRegister(authenticatedIdentity, ureq, provider);
            }
        }

        if (source == subController) {
            if (event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT) {
                cmc.deactivate();
            }
        } else if (source == disclaimerCtr) {
            cmc.deactivate();
            if (event == Event.DONE_EVENT) {
                // User accepted disclaimer, do login now
                getRegistrationService().setHasConfirmedDislaimer(authenticatedIdentity);
                doLoginAndRegister(authenticatedIdentity, ureq, provider);
            } else if (event == Event.CANCELLED_EVENT) {
                // User did not accept, workflow ends here
                showWarning("disclaimer.form.cancelled");
            }
        } else if (source == cmc) {
            // User did close disclaimer window, workflow ends here
            showWarning("disclaimer.form.cancelled");
        }
    }

    private RegistrationService getRegistrationService() {
        return CoreSpringFactory.getBean(RegistrationService.class);
    }

    /**
     * Internal helper to perform the real login code and do all necessary steps to register the user session
     * 
     * @param authenticatedIdentity
     * @param ureq
     * @param myProvider
     *            The provider that identified the user
     */
    private void doLoginAndRegister(final Identity authenticatedIdentity, final UserRequest ureq, final String myProvider) {
        if (provider.equals(AUTHENTICATION_PROVIDER_LDAP)) {
            // prepare redirects to home etc, set status
            final int loginStatus = AuthHelper.doLogin(authenticatedIdentity, myProvider, ureq);
            if (loginStatus == AuthHelper.LOGIN_OK) {
                // update last login date and register active user
                UserDeletionManager.getInstance().setIdentityAsActiv(authenticatedIdentity);
            } else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
                DispatcherAction.redirectToServiceNotAvailable(ureq.getHttpResp());
            } else {
                getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailSupport")));
            }
        } else if (provider.equals(AUTHENTICATION_PROVIDER_OLAT)) {
            // delegate login process to OLAT authentication controller
            authenticated(ureq, authenticatedIdentity);
        } else {
            throw new OLATRuntimeException("Unknown login provider::" + myProvider, null);
        }
    }

    @Override
    protected void doDispose() {
        //
    }
}
