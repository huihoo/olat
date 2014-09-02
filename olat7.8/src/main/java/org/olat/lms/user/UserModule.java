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

package org.olat.lms.user;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DB;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.core.notification.impl.UriBuilder;
import org.olat.lms.security.BaseSecurityService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.presentation.framework.common.NewControllerFactory;
import org.olat.presentation.home.HomeContextEntryControllerCreator;
import org.olat.presentation.security.authentication.AfterLoginConfig;
import org.olat.presentation.security.authentication.AfterLoginInterceptionManager;
import org.olat.presentation.user.IdentityContextEntryControllerCreator;
import org.olat.system.commons.configuration.AbstractOLATModule;
import org.olat.system.commons.encoder.Encoder;
import org.olat.system.exception.OLATRuntimeException;
import org.olat.system.exception.StartupException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Desciption: The user module represents an implementation of the OLAT user with its database object, business managers and page actions.
 * 
 * @author Florian Gnägi
 */
public class UserModule extends AbstractOLATModule {
    private List<String> loginBlacklist;
    private static List<String> loginBlacklistChecked = new ArrayList<String>();
    private static boolean hasTestUsers;

    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private BaseSecurityService baseSecurityService;

    private SecurityGroup adminGroup, authorGroup, olatuserGroup, anonymousGroup, groupmanagerGroup, usermanagerGroup;
    private static boolean pwdchangeallowed;
    private static String adminUserName;
    private List<DefaultUser> defaultUsers;
    private List<DefaultUser> testUsers;
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private UserService userService;
    private AfterLoginConfig afterLoginConfig;
    @Autowired
    private AfterLoginInterceptionManager afterLoginInterceptionManager;
    @Autowired
    private DB db;
    @Autowired
    private UriBuilder uriBuilder;

    /**
     * [used by spring]
     * 
     * @param authenticationProviderConstant
     */
    private UserModule() {
        //
    }

    /**
     * [used by spring]
     * 
     * @param afterLoginConfig
     */
    public void setAfterLoginConfig(final AfterLoginConfig afterLoginConfig) {
        this.afterLoginConfig = afterLoginConfig;
    }

    /**
     * [used by spring]
     * 
     * @param loginBlacklist
     */
    public void setLoginBlacklist(final List<String> loginBlacklist) {
        this.loginBlacklist = loginBlacklist;
    }

    /**
     * Check wether a login is on the blacklist.
     * 
     * @param login
     * @return True if login is in blacklist
     */
    public static boolean isLoginOnBlacklist(String login) {
        login = login.toLowerCase();
        for (final Iterator iter = getLoginBlacklist().iterator(); iter.hasNext();) {
            final String regexp = (String) iter.next();
            if (login.matches(regexp)) {
                log.info("Audit:Blacklist entry match for login '" + login + "' with regexp '" + regexp + "'.");
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialize() {
        pwdchangeallowed = getBooleanConfigParameter("passwordChangeAllowed", true);

        int count = 0;
        for (final String regexp : loginBlacklist) {
            try {
                Pattern.compile(regexp);
                loginBlacklistChecked.add(regexp);
            } catch (final PatternSyntaxException pse) {
                log.error("Invalid pattern syntax in blacklist. Pattern: " + regexp + ". Removing from this entry from list ");
            }
            count++;
        }

        log.info("Successfully added " + count + " entries to login blacklist.");

        // Autogeneration of test users
        hasTestUsers = getBooleanConfigParameter("generateTestUsers", true);

        // Check if default users exists, if not create them
        adminGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ADMIN);
        authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
        olatuserGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        anonymousGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
        groupmanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
        usermanagerGroup = securityManager.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);

        // read user editable fields configuration
        if (defaultUsers != null) {
            for (final Iterator iter = defaultUsers.iterator(); iter.hasNext();) {
                final DefaultUser user = (DefaultUser) iter.next();
                createUser(user);
            }
        }
        if (hasTestUsers) {
            // read user editable fields configuration
            if (testUsers != null) {
                for (final Iterator iter = testUsers.iterator(); iter.hasNext();) {
                    final DefaultUser user = (DefaultUser) iter.next();
                    createUser(user);
                }
            }
        }
        // Cleanup, otherwhise this subjects will have problems in normal OLAT
        // operation
        db.intermediateCommit();

        adminUserName = getStringConfigParameter("adminUserName", "administrator", false);

        // Check if user manager is configured properly and has user property
        // handlers for the mandatory user properties used in OLAT
        checkMandatoryUserProperty(UserConstants.FIRSTNAME);
        checkMandatoryUserProperty(UserConstants.LASTNAME);
        checkMandatoryUserProperty(UserConstants.EMAIL);

        // Add controller factory extension point to launch user profile controller
        NewControllerFactory.getInstance().addContextEntryControllerCreator(Identity.class.getSimpleName(), new IdentityContextEntryControllerCreator());

        NewControllerFactory.getInstance().addContextEntryControllerCreator(uriBuilder.getNotificationTabContext(), new HomeContextEntryControllerCreator());

        // Append AfterLoginControllers if any configured
        if (afterLoginConfig != null) {
            afterLoginInterceptionManager.addAfterLoginControllerConfig(afterLoginConfig);
        }
    }

    @Override
    protected void initDefaultProperties() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initFromChangedProperties() {
        // TODO Auto-generated method stub

    }

    private void checkMandatoryUserProperty(final String userPropertyIdentifyer) {
        final List<UserPropertyHandler> propertyHandlers = userService.getUserPropertiesConfig().getAllUserPropertyHandlers();
        boolean propertyDefined = false;
        for (final UserPropertyHandler propertyHandler : propertyHandlers) {
            if (propertyHandler.getName().equals(userPropertyIdentifyer)) {
                propertyDefined = true;
                break;
            }
        }
        if (!propertyDefined) {
            throw new StartupException("The user property handler for the mandatory user property " + userPropertyIdentifyer
                    + " is not defined. Check your olat_userconfig.xml file!");
        }
    }

    /**
     * Method to create a user with the given configuration
     * 
     * @return Identity or null
     */
    protected Identity createUser(final DefaultUser user) {
        Identity identity;
        identity = securityManager.findIdentityByName(user.getUserName());
        if (identity == null) {
            // Create new user and subject
            final User newUser = userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail());
            newUser.getPreferences().setLanguage(I18nManager.getInstance().getLocaleOrDefault(user.getLanguage()));
            newUser.getPreferences().setInformSessionTimeout(true);

            if (!StringUtils.hasText(AUTHENTICATION_PROVIDER_OLAT)) {
                throw new OLATRuntimeException(this.getClass(), "Auth token not set! Please fix! " + AUTHENTICATION_PROVIDER_OLAT, null);
            }

            // Now finally create that user thing on the database with all
            // credentials, person etc. in one transation context!
            identity = baseSecurityService.createAndPersistIdentityAndUser(user.getUserName(), newUser, AUTHENTICATION_PROVIDER_OLAT, user.getUserName(),
                    Encoder.bCryptEncode(user.getPassword()));
            if (identity == null) {
                throw new OLATRuntimeException(this.getClass(), "Error, could not create  user and subject with name " + user.getUserName(), null);
            } else {

                if (user.isGuest()) {
                    securityManager.addIdentityToSecurityGroup(identity, anonymousGroup);
                    log.info("Created anonymous user " + user.getUserName());
                } else {
                    if (user.isAdmin()) {
                        securityManager.addIdentityToSecurityGroup(identity, adminGroup);
                        securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
                        log.info("Created admin user " + user.getUserName());
                    } else if (user.isAuthor()) {
                        securityManager.addIdentityToSecurityGroup(identity, authorGroup);
                        securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
                        log.info("Created author user " + user.getUserName());
                    } else if (user.isUserManager()) {
                        securityManager.addIdentityToSecurityGroup(identity, usermanagerGroup);
                        securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
                        log.info("Created userManager user " + user.getUserName());
                    } else if (user.isGroupManager()) {
                        securityManager.addIdentityToSecurityGroup(identity, groupmanagerGroup);
                        securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
                        log.info("Created groupManager user " + user.getUserName());
                    } else {
                        securityManager.addIdentityToSecurityGroup(identity, olatuserGroup);
                        log.info("Created user " + user.getUserName());
                    }
                }
            }
        }
        return identity;
    }

    /**
     * @return List of logins on blacklist.
     */
    public static List getLoginBlacklist() {
        return loginBlacklistChecked;
    }

    public static boolean isPwdchangeallowed() {
        return pwdchangeallowed;
    }

    public static String getAdminUserName() {
        return adminUserName;
    }

    public void setDefaultUsers(final List<DefaultUser> defaultUsers) {
        this.defaultUsers = defaultUsers;
    }

    public void setTestUsers(final List<DefaultUser> testUsers) {
        this.testUsers = testUsers;
    }

}
