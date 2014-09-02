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

package org.olat.lms.security.authentication;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.presentation.security.authentication.AuthenticationProvider;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.coordinate.cache.CacheWrapper;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Initial Date: 04.08.2004
 * 
 * @author Mike Stock
 * @author guido
 */
public class LoginModule extends AbstractOLATModule {

    private static final Logger log = LoggerHelper.getLogger();

    private static final String CONF_ATTACK_ENABLED = "AttackPreventionEnabled";
    private static final String CONF_ATTACK_MAXATTEMPTS = "AttackPreventionMaxattempts";
    private static final String CONF_ATTACK_TIMEOUTMIN = "AttackPreventionTimeoutmin";
    private static final String CONF_GUESTLINKS_ENABLED = "GuestLoginLinksEnabled";
    private static final String CONF_INVITATION_ENABLED = "InvitationEnabled";
    private static final String ALLOW_LOGIN_USING_EMAIL = "allowLoginUsingEmail";

    private static Map<String, AuthenticationProvider> authenticationProviders;
    private static boolean attackPreventionEnabled;
    private static int attackPreventionMaxAttempts;
    private static int attackPreventionTimeout;
    private static boolean guestLoginLinksEnabled;
    private static CacheWrapper failedLoginCache;
    private static String defaultProviderName;
    private static boolean allowLoginUsingEmail;
    private static boolean invitationEnabled;

    /**
     * [used by spring]
     */
    private LoginModule() {
        //
    }

    @Override
    protected void initDefaultProperties() {
        attackPreventionEnabled = getBooleanConfigParameter(CONF_ATTACK_ENABLED, true);
        if (attackPreventionEnabled) {
            log.info("Attack prevention enabled. Max number of attempts: " + attackPreventionMaxAttempts + " , timeout: " + attackPreventionTimeout + " minutes.");
        } else {
            log.info("Attack prevention is disabled.");
        }
        attackPreventionMaxAttempts = getIntConfigParameter(CONF_ATTACK_MAXATTEMPTS, 5);
        attackPreventionTimeout = getIntConfigParameter(CONF_ATTACK_TIMEOUTMIN, 5);

        guestLoginLinksEnabled = getBooleanConfigParameter(CONF_GUESTLINKS_ENABLED, true);
        if (guestLoginLinksEnabled) {
            log.info("Guest login links on login page enabled");
        } else {
            guestLoginLinksEnabled = false;
            log.info("Guest login links on login page disabled or not properly configured. ");
        }
        invitationEnabled = getBooleanConfigParameter(CONF_INVITATION_ENABLED, true);
        if (invitationEnabled) {
            log.info("Invitation login enabled");
        } else {
            log.info("Invitation login disabled");
        }

        allowLoginUsingEmail = getBooleanConfigParameter(ALLOW_LOGIN_USING_EMAIL, true);

    }

    /**
     * [used by spring]
     * 
     * @param authProviders
     */
    public void setAuthenticaionProviders(final Map<String, AuthenticationProvider> authProviders) {
        LoginModule.authenticationProviders = authProviders;
    }

    /**
     * @return The configured default login provider.
     */
    public static String getDefaultProviderName() {
        return defaultProviderName;
    }

    /**
     * @param provider
     * @return AuthenticationProvider implementation.
     */
    public static AuthenticationProvider getAuthenticationProvider(final String provider) {
        return authenticationProviders.get(provider);
    }

    /**
     * @return Collection of available AuthenticationProviders
     */
    public static Collection<AuthenticationProvider> getAuthenticationProviders() {
        return authenticationProviders.values();
    }

    /**
     * Must be called upon each login attempt. Returns true if number of login attempts has reached the set limit.
     * 
     * @param login
     * @return True if further logins will be prevented (i.e. max attempts reached).
     */
    public static final boolean registerFailedLoginAttempt(final String login) {
        if (!attackPreventionEnabled) {
            return false;
        }

        FailedLogin failedLogin = (FailedLogin) failedLoginCache.get(login);

        if (failedLogin == null) { // create new entry
            failedLogin = new FailedLogin(new Integer(1), null);
        } else { // update entry
            failedLogin.setNumAttempts(failedLogin.getNumAttempts() + 1);
        }

        boolean tooManyAttempts = failedLogin.isTooManyAttempts(attackPreventionMaxAttempts);
        if (tooManyAttempts) {
            failedLogin.setBlockedTimestamp(System.currentTimeMillis());
        }

        // do not use putSilent(...) here, since failed login attempts should propagate to all cluster nodes
        // o_clusterREVIEW todo: this is fine, however loading the data (numAttempts == null) ... should be via db e.g properties table,
        // otherwise it cannot be clustersafe
        failedLoginCache.update(login, failedLogin);

        return tooManyAttempts;
    }

    /**
     * Checks if this login name is blocked for the configured timeout.
     */
    public static final boolean isLoginBlocked(final String login) {
        if (!attackPreventionEnabled) {
            return false;
        }
        FailedLogin failedLogin = (FailedLogin) failedLoginCache.get(login);
        if (failedLogin == null) {
            return false;
        }
        return failedLogin.isLoginBlocked(Long.valueOf(attackPreventionTimeout));
    }

    /**
     * Clear all failed login attempts for a given login.
     * 
     * @param login
     */
    public static final void clearFailedLoginAttempts(final String login) {
        if (!attackPreventionEnabled) {
            return;
        }
        // EHCacheManager.getInstance().removeFromCache(failedLoginCache, login);
        failedLoginCache.remove(login);
    }

    /**
     * @return True if guest login kinks must be shown on login screen, false otherwhise
     */
    public static final boolean isGuestLoginLinksEnabled() {
        return guestLoginLinksEnabled;
    }

    public static final boolean isInvitationEnabled() {
        return invitationEnabled;
    }

    /**
     * @return Number of minutes a login gets blocked after too many attempts.
     */
    public static Integer getAttackPreventionTimeoutMin() {
        return new Integer(attackPreventionTimeout);
    }

    /**
     * @return True if login with email is allowed (set in olat.properties)
     */
    public static boolean allowLoginUsingEmail() {
        return allowLoginUsingEmail;
    }

    @Override
    public void initialize() {

        boolean defaultProviderFound = false;
        for (final Iterator<AuthenticationProvider> iterator = authenticationProviders.values().iterator(); iterator.hasNext();) {
            final AuthenticationProvider provider = iterator.next();
            if (provider.isDefault()) {
                defaultProviderFound = true;
                defaultProviderName = provider.getName();
                log.info("Using default authentication provider '" + defaultProviderName + "'.");
            }
        }

        if (!defaultProviderFound) {
            throw new StartupException("Defined DefaultAuthProvider::" + defaultProviderName + " not existent or not enabled. Please fix.");
        }

        // configure timed cache default params: refresh 1 minute, timeout according to configuration
        failedLoginCache = coordinatorManager.getCoordinator().getCacher().getOrCreateCache(this.getClass(), "blockafterfailedattempts");

    }

    @Override
    protected void initFromChangedProperties() {
        // TODO Auto-generated method stub

    }

}
