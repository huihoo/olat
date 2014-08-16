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

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_SHIB;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.user.User;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.registration.RegistrationModule;
import org.olat.lms.security.BaseSecurityEBL;
import org.olat.lms.security.authentication.shibboleth.ShibbolethHelper;
import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.lms.user.UserService;
import org.olat.presentation.commons.session.UserSession;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.chiefcontrollers.LanguageChangedEvent;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.ChiefController;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.ControllerEventListener;
import org.olat.presentation.framework.core.control.LocaleChangedEvent;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.framework.dispatcher.LocaleNegotiator;
import org.olat.presentation.framework.layout.fullWebApp.LayoutMain3ColsController;
import org.olat.presentation.registration.DisclaimerController;
import org.olat.presentation.registration.LanguageChooserController;
import org.olat.presentation.registration.UserNameCreationInterceptor;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.AssertException;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: 09.08.2004
 * 
 * @author Mike Stock Comment: User wants ShibbolethAuthentication - Basic flow: System asks User for username and create olataccount with ShibbolethAuthentication
 *         Branches: 1. no email in shibbolethAttributesMap - System asks for emailaddress (no institutionalEmail is set !!!) 2. no email in shibbolethAttributesMap and
 *         User already exists in System - System asks for password (no institutionalEmail is set !!!)
 */

public class ShibbolethRegistrationController extends BasicController implements ControllerEventListener {

    private static final String KEY_SHIBATTRIBUTES = "shibattr";
    private static final String KEY_SHIBUNIQUEID = "shibuid";

    private VelocityContainer mainContainer;
    private Panel mainPanel;

    private ShibbolethRegistrationForm regForm;
    private ShibbolethMigrationForm migrationForm;
    private ShibbolethRegistrationWithEmailForm regWithEmailForm;
    private DisclaimerController dclController;
    private LanguageChooserController languageChooserController;

    private Map<String, String> shibbolethAttributesMap;
    private String shibbolethUniqueID;

    private int state = STATE_UNDEFINED;
    private static final int STATE_UNDEFINED = 0;
    private static final int STATE_NEW_SHIB_USER = 1;
    private static final int STATE_MIGRATED_SHIB_USER = 2;
    private String proposedUsername;

    private boolean hasEmailInShibAttr;

    // names found in the velocity templates
    private static final String DISCLAIMER_CONTROLLER_NAME = "dclComp";
    private static final String REGISTRATION_FORM_NAME = "regForm";
    private static final String MIGRATION_FORM_NAME = "migrationForm";
    private static final String REG_WITH_EMAIL_FORM_NAME = "regWithEmailForm";

    /**
     * Implements the shibboleth registration workflow.
     * 
     * @param ureq
     * @param wControl
     */
    public ShibbolethRegistrationController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        shibbolethAttributesMap = (Map<String, String>) ureq.getUserSession().getEntry(KEY_SHIBATTRIBUTES);
        shibbolethUniqueID = (String) ureq.getUserSession().getEntry(KEY_SHIBUNIQUEID);

        if (shibbolethUniqueID == null) {
            /* STATIC_METHOD_REFACTORING direct instantiation instead of static method call */
            final ChiefController msgcc = new MessageWindowController(ureq, new AssertException(
                    "ShibbolethRegistrationController was unable to fetch ShibbolethUniqueID from session."), translate("error.shibboleth.generic"), null);
            msgcc.getWindow().dispatchRequest(ureq, true);
            return;
        }

        if (shibbolethAttributesMap == null) {
            throw new AssertException("ShibbolethRegistrationController was unable to fetch ShibbolethAttribuitesMap from session.");
        }

        hasEmailInShibAttr = hasEmailInShibAttributes();

        Locale locale = (Locale) ureq.getUserSession().getEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
        if (locale == null) {
            final String preferedLanguage = ShibbolethModule.getPreferedLanguage();
            if (preferedLanguage == null) {
                locale = LocaleNegotiator.getPreferedLocale(ureq);
            } else {
                locale = LocaleNegotiator.getNegotiatedLocale(preferedLanguage);
                if (locale == null) {
                    locale = LocaleNegotiator.getPreferedLocale(ureq);
                }
            }
        }
        ureq.getUserSession().setLocale(locale);
        I18nManager.updateLocaleInfoToThread(ureq.getUserSession());
        ureq.getUserSession().putEntry(LocaleNegotiator.NEGOTIATED_LOCALE, locale);

        mainPanel = new Panel("main");
        mainContainer = createVelocityContainer("langchooser");

        languageChooserController = new LanguageChooserController(ureq, wControl, false);
        listenTo(languageChooserController);
        mainContainer.put("select.language", languageChooserController.getInitialComponent());
        mainContainer.contextPut("languageCode", locale.getLanguage());

        if (RegistrationModule.getUsernamePresetBean() != null) {
            final UserNameCreationInterceptor interceptor = RegistrationModule.getUsernamePresetBean();
            proposedUsername = interceptor.getUsernameFor(shibbolethAttributesMap);
            if (proposedUsername == null) {
                if (interceptor.allowChangeOfUsername()) {
                    setRegistrationForm(ureq, wControl, proposedUsername);
                } else {
                    setErrorPage("sm.error.no_username", wControl);
                }
            } else {
                final Identity identity = getBaseSecurity().findIdentityByName(proposedUsername);
                if (identity != null) {
                    if (interceptor.allowChangeOfUsername()) {
                        setRegistrationForm(ureq, wControl, proposedUsername);
                    } else {
                        setErrorPage("sm.error.username_in_use", wControl);
                    }
                } else if (interceptor.allowChangeOfUsername()) {
                    setRegistrationForm(ureq, wControl, proposedUsername);
                } else {
                    if (hasEmailInShibAttr) {
                        state = STATE_NEW_SHIB_USER;
                        mainContainer = createVelocityContainer("disclaimer");
                    } else {
                        regWithEmailForm = new ShibbolethRegistrationWithEmailForm(ureq, wControl, proposedUsername);
                        listenTo(regWithEmailForm);
                        // mainContainer.put("regWithEmailForm", regWithEmailForm);
                        mainContainer = createVelocityContainer("registerwithemail");
                    }
                }
            }
        } else {
            setRegistrationForm(ureq, wControl, null);
        }

        dclController = new DisclaimerController(ureq, getWindowControl());
        listenTo(dclController);
        mainContainer.put(DISCLAIMER_CONTROLLER_NAME, dclController.getInitialComponent());

        mainPanel.setContent(mainContainer);
        // load view in layout
        final LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, mainPanel, null);
        putInitialPanel(layoutCtr.getInitialComponent());
    }

    /**
     * Creates new VelocityContainer, get the components already added in constructor, and adds it to mainPanel.
     * 
     * @param pageName
     * @return
     */
    private VelocityContainer addVelocityContainer(String pageName, VelocityContainer oldVelocityContainer) {
        VelocityContainer velocityContainer = createVelocityContainer(pageName);
        mainPanel.setContent(velocityContainer);
        if (oldVelocityContainer != null) {
            if (oldVelocityContainer.getComponent(DISCLAIMER_CONTROLLER_NAME) != null) {
                velocityContainer.put(DISCLAIMER_CONTROLLER_NAME, oldVelocityContainer.getComponent(DISCLAIMER_CONTROLLER_NAME));
            }
            if (oldVelocityContainer.getComponent(REGISTRATION_FORM_NAME) != null) {
                velocityContainer.put(REGISTRATION_FORM_NAME, oldVelocityContainer.getComponent(REGISTRATION_FORM_NAME));
            }
        }
        return velocityContainer;
    }

    private boolean hasEmailInShibAttributes() {
        return (ShibbolethModule.getEMail() == null) ? false : true;
    }

    private BaseSecurity getBaseSecurity() {
        return CoreSpringFactory.getBean(BaseSecurity.class);
    }

    private void setErrorPage(final String errorKey, final WindowControl wControl) {
        final String error = translate(errorKey);
        wControl.setError(error);

        mainContainer = addVelocityContainer("error", mainContainer);
        mainContainer.contextPut("error_msg", error);

    }

    private void setRegistrationForm(final UserRequest ureq, final WindowControl wControl, final String proposedUsername) {
        regForm = new ShibbolethRegistrationForm(ureq, wControl, proposedUsername);
        listenTo(regForm);
        mainContainer.put(REGISTRATION_FORM_NAME, regForm.getInitialComponent());
    }

    /**
     * Put shibboleth attributes map in reqest for later usage.
     * 
     * @param req
     * @param attributes
     */
    public static final void putShibAttributes(final HttpServletRequest req, final Map<String, String> attributes) {
        UserSession.getUserSession(req).putEntry(KEY_SHIBATTRIBUTES, attributes);
    }

    /**
     * Put shibboleth unique identifier in request for later usage.
     * 
     * @param req
     * @param uniqueID
     */
    public static final void putShibUniqueID(final HttpServletRequest req, final String uniqueID) {
        UserSession.getUserSession(req).putEntry(KEY_SHIBUNIQUEID, uniqueID);
    }

    /**
	 */
    @Override
    public void event(final UserRequest ureq, final Component source, final Event event) {
        if (event instanceof LocaleChangedEvent) {
            final LocaleChangedEvent lce = (LocaleChangedEvent) event;
            final Locale newLocale = lce.getNewLocale();
            this.getTranslator().setLocale(newLocale);
            dclController.changeLocale(newLocale);
        }
    }

    /**
	 */
    @Override
    public void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source == migrationForm) {
            if (event == Event.CANCELLED_EVENT) {
                mainContainer = addVelocityContainer("register", mainContainer);
            } else if (event == Event.DONE_EVENT) {
                state = STATE_MIGRATED_SHIB_USER;
                mainContainer = addVelocityContainer("disclaimer", mainContainer);
            }
        } else if (source == regWithEmailForm) {
            if (event == Event.CANCELLED_EVENT) {
                mainContainer = addVelocityContainer("register", mainContainer);
            } else if (event == Event.DONE_EVENT) {
                state = STATE_NEW_SHIB_USER;
                mainContainer = addVelocityContainer("disclaimer", mainContainer);
            }
        } else if (source == regForm) {
            if (event == Event.DONE_EVENT) {
                final String choosenLogin = regForm.getLogin();
                final BaseSecurity secMgr = getBaseSecurity();
                final Identity identity = secMgr.findIdentityByName(choosenLogin);

                if (identity == null) { // ok, create new user
                    if (!hasEmailInShibAttr) {
                        regWithEmailForm = new ShibbolethRegistrationWithEmailForm(ureq, getWindowControl(), choosenLogin);
                        listenTo(regWithEmailForm);

                        mainContainer = addVelocityContainer("registerwithemail", mainContainer);
                        mainContainer.put(REG_WITH_EMAIL_FORM_NAME, regWithEmailForm.getInitialComponent());
                    } else { // there is an emailaddress
                        state = STATE_NEW_SHIB_USER;
                        mainContainer = addVelocityContainer("disclaimer", mainContainer);
                    }
                } else { // offer identity migration, if OLAT provider exists
                    final Authentication auth = getBaseSecurity().findAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT);
                    if (auth == null) { // no OLAT provider, migration not possible...
                        getWindowControl().setError(translate("sr.error.loginexists", new String[] { WebappHelper.getMailConfig("mailSupport") }));
                    } else { // OLAT provider exists, offer migration...
                        migrationForm = new ShibbolethMigrationForm(ureq, getWindowControl(), auth);
                        listenTo(migrationForm);

                        mainContainer = addVelocityContainer("migration", mainContainer);
                        mainContainer.put(MIGRATION_FORM_NAME, migrationForm.getInitialComponent());
                    }
                }
            }
        } else if (source == languageChooserController) {
            if (event == Event.DONE_EVENT) { // language choosed
                mainContainer = addVelocityContainer("register", mainContainer);
                ureq.getUserSession().removeEntry(LocaleNegotiator.NEGOTIATED_LOCALE);
            } else if (event instanceof LanguageChangedEvent) {
                final LanguageChangedEvent lcev = (LanguageChangedEvent) event;
                getTranslator().setLocale(lcev.getNewLocale());
                dclController.changeLocale(lcev.getNewLocale());
            }
        } else if (source == dclController) {
            if (event == Event.DONE_EVENT) { // disclaimer accepted...
                if (state == STATE_NEW_SHIB_USER) { // ...proceed and create user
                    String choosenLogin;
                    if (regForm == null) {
                        choosenLogin = proposedUsername;
                    } else {
                        choosenLogin = regForm.getLogin();
                    }

                    // check if login has been taken by another user in the meantime...
                    Identity identity = getBaseSecurity().findIdentityByName(choosenLogin);
                    if (identity != null) {
                        getWindowControl().setError(translate("sr.login.meantimetaken"));
                        mainContainer = addVelocityContainer("register", mainContainer);
                        state = STATE_UNDEFINED;
                        return;
                    }

                    String email;
                    if (!hasEmailInShibAttr) {
                        email = regWithEmailForm.getEmail();
                    } else {
                        email = ShibbolethHelper.getFirstValueOf(ShibbolethModule.getEMail(), shibbolethAttributesMap);
                    }

                    User user = getUserService().findUserByEmail(email);
                    if (user != null) {
                        // error, email already exists. should actually not happen if OLAT Authenticator has
                        // been set after removing shibboleth authenticator
                        getWindowControl().setError(translate("sr.error.emailexists", new String[] { WebappHelper.getMailConfig("mailSupport") }));
                        mainContainer = addVelocityContainer("register", mainContainer);
                        state = STATE_UNDEFINED;
                        return;
                    }

                    identity = getBaseSecurityEBL().createUserViaShibbolethRegistration(choosenLogin, email, shibbolethAttributesMap, shibbolethUniqueID);

                    doLogin(identity, ureq);
                    return;
                } else if (state == STATE_MIGRATED_SHIB_USER) { // ...proceed and migrate user
                    // create additional authentication
                    final Authentication auth = migrationForm.getAuthentication();
                    final Identity authenticationedIdentity = auth.getIdentity();

                    getBaseSecurityEBL().createShibAuthenticationAndUpdateUser(authenticationedIdentity, shibbolethAttributesMap, shibbolethUniqueID);
                    doLogin(authenticationedIdentity, ureq);
                    return;
                }
            } else if (event == Event.CANCELLED_EVENT) {
                mainContainer = addVelocityContainer("register", mainContainer);
                getWindowControl().setError(translate("sr.error.disclaimer"));
            }
        }
    }

    private BaseSecurityEBL getBaseSecurityEBL() {
        return CoreSpringFactory.getBean(BaseSecurityEBL.class);
    }

    private void doLogin(final Identity identity, final UserRequest ureq) {
        final int loginStatus = AuthHelper.doLogin(identity, AUTHENTICATION_PROVIDER_SHIB, ureq);
        if (loginStatus != AuthHelper.LOGIN_OK) {
            // REVIEW:2010-01-11:revisited:pb: do not redirect if already MediaResource is set before
            // ureq.getDispatchResult().setResultingMediaResource(resultingMediaResource);
            // instead set the media resource accordingly
            // pb -> provide a DispatcherAction.getDefaultDispatcherRedirectMediaresource();
            // to be used here. (and some more places like CatalogController.
            DispatcherAction.redirectToDefaultDispatcher(ureq.getHttpResp()); // error, redirect to login screen
            return;
        }
        // successfull login
        ureq.getUserSession().getIdentityEnvironment().addAttributes(ShibbolethModule.getAttributeTranslator().translateAttributesMap(shibbolethAttributesMap));
    }

    /**
	 */
    @Override
    protected void doDispose() {

        // child controller disposed by parent
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
