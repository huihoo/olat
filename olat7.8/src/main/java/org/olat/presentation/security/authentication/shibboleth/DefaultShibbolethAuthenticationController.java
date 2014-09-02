package org.olat.presentation.security.authentication.shibboleth;

import java.util.Locale;

import org.olat.lms.security.authentication.LoginModule;
import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.dispatcher.DispatcherAction;
import org.olat.presentation.security.authentication.AuthHelper;
import org.olat.presentation.security.authentication.AuthenticationController;
import org.olat.presentation.security.authentication.LoginAuthprovidersController;
import org.olat.system.commons.WebappHelper;
import org.olat.system.event.Event;
import org.olat.system.exception.OLATSecurityException;

/**
 * Description:<br>
 * Simple ShibbolethAuthenticationController. It just has a link for redirecting the requests to the /shib/.
 * <P>
 * Initial Date: 08.07.2009 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class DefaultShibbolethAuthenticationController extends AuthenticationController {

    private final VelocityContainer loginComp;
    private final Link shibLink;
    private Link guestLink;
    private final Panel mainPanel;

    /**
     * @param ureq
     * @param wControl
     */
    public DefaultShibbolethAuthenticationController(final UserRequest ureq, final WindowControl wControl) {
        super(ureq, wControl);

        // extends authControll which is a BasicController, so we have to set the
        // Manually set translator that uses a fallback translator to the login module
        // Can't use constructor with fallback translator because it gets overriden by setBasePackage call above
        setTranslator(PackageUtil.createPackageTranslator(LoginAuthprovidersController.class, ureq.getLocale(),
                PackageUtil.createPackageTranslator(LoginModule.class, ureq.getLocale())));

        if (!ShibbolethModule.isEnableShibbolethLogins()) {
            throw new OLATSecurityException("Shibboleth is not enabled.");
        }

        loginComp = createVelocityContainer("default_shibbolethlogin");
        shibLink = LinkFactory.createLink("shib.redirect", loginComp, this);

        if (LoginModule.isGuestLoginLinksEnabled()) {
            guestLink = LinkFactory.createLink("menu.guest", loginComp, this);
            guestLink.setCustomEnabledLinkCSS("o_login_guests");
        }

        mainPanel = putInitialPanel(loginComp);
    }

    @Override
    public void changeLocale(final Locale newLocale) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void doDispose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source == shibLink) {
            DispatcherAction.redirectTo(ureq.getHttpResp(), WebappHelper.getServletContextPath() + "/shib/");
        } else if (source == guestLink) {
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

}
