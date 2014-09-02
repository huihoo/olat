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
package org.olat.lms.security;

import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_OLAT;
import static org.olat.system.security.AuthenticationConstants.AUTHENTICATION_PROVIDER_SHIB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.Invitation;
import org.olat.data.basesecurity.PermissionOnResourceable;
import org.olat.data.basesecurity.Roles;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.basesecurity.SecurityResourceTypeEnum;
import org.olat.data.repository.RepositoryEntry;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.commons.i18n.I18nManager;
import org.olat.lms.registration.RegistrationService;
import org.olat.lms.registration.UserPropertyParameter;
import org.olat.lms.repository.RepositoryService;
import org.olat.lms.security.authentication.AuthenticationService;
import org.olat.lms.security.authentication.AuthenticationService.Provider;
import org.olat.lms.security.authentication.OLATAuthManager;
import org.olat.lms.security.authentication.WebDAVAuthManager;
import org.olat.lms.security.authentication.ldap.LDAPError;
import org.olat.lms.security.authentication.ldap.LDAPLoginModule;
import org.olat.lms.security.authentication.shibboleth.ShibbolethHelper;
import org.olat.lms.security.authentication.shibboleth.ShibbolethModule;
import org.olat.lms.user.UserModule;
import org.olat.lms.user.UserService;
import org.olat.lms.user.propertyhandler.UserPropertyHandler;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Class Description for BaseSecurityEBL
 * 
 * <P>
 * Initial Date: 07.09.2011 <br>
 * 
 * @author lavinia
 */
@Component
public class BaseSecurityEBL {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private BaseSecurity baseSecurity;
    @Autowired
    BaseSecurityService baseSecurityService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    RegistrationService registrationService;

    public static final String FEED_TOKEN_PROVIDER = "feed";

    BaseSecurityEBL() {
        // spring
    }

    /**
     * @param ureq
     * @param owner
     * @return
     */
    public SecurityGroup createOwnerGroupWithIdentity(final Identity owner) {
        // create security group
        final SecurityGroup newGroup = baseSecurity.createAndPersistSecurityGroup();
        // member of this group may modify member's membership
        baseSecurity.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
        // members of this group are always authors also
        baseSecurity.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);

        baseSecurity.addIdentityToSecurityGroup(owner, newGroup);
        return newGroup;
    }

    /**
     * @param repositoryDetailsController
     * @param identity
     * @param roles
     * @param repositoryEntry
     * @return
     */
    public IdentityRolesForResource getIdentityRolesWithLoadRepositoryEntry(Identity identity, Roles roles, RepositoryEntry repositoryEntry) {
        IdentityRolesForResource identityRoles;
        if (roles.isOLATAdmin()) {
            identityRoles = new IdentityRolesForResource(true, true, true, false);
        } else {
            // load repositoryEntry again because the hibenate object is 'detached'.Otherwise you become an exception when you check owner-group.
            final boolean isInstitutionalResourceManager = repositoryService.isInstitutionalRessourceManagerFor(repositoryEntry, identity);

            repositoryEntry = repositoryService.loadRepositoryEntry(repositoryEntry);
            identityRoles = new IdentityRolesForResource(false, false, false, false);
            identityRoles.setOwner(baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, repositoryEntry.getOwnerGroup())
                    | isInstitutionalResourceManager);
            identityRoles.setAuthor(roles.isAuthor() | isInstitutionalResourceManager);
            identityRoles.setGuestOnly(roles.isGuestOnly());
            identityRoles.setOlatAdmin(false);
        }
        return identityRoles;
    }

    public boolean isInstitutionalResourceManager(final Identity ident) {
        return baseSecurity.isIdentityInSecurityGroup(ident, baseSecurity.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER));
    }

    public boolean isAnonymous(final Identity ident) {
        final SecurityGroup anonymousSecGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
        return baseSecurity.isIdentityInSecurityGroup(ident, anonymousSecGroup);
    }

    public boolean isOwner(final Identity identity, SecurityGroup ownerGroup) {
        return baseSecurity.isIdentityInSecurityGroup(identity, ownerGroup);
    }

    /**
     * @param identity
     * @param repository
     *            entry
     * @return
     */
    public boolean isRepoEntryEditable(final Identity identity, final RepositoryEntry re) {
        return (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)
                || repositoryService.isOwnerOfRepositoryEntry(identity, re) || repositoryService.isInstitutionalRessourceManagerFor(re, identity));
    }

    /**
     * There are 2 methods with slightly different impl, is this wanted or just happened by chance?
     * 
     * @param identity
     * @param provider
     * @return
     */
    public Authentication findOrCreateAuthenticationWithRandomCredential(final Identity identity, final String provider) {
        final String idKey = identity.getKey().toString();
        Authentication authentication = baseSecurity.findAuthenticationByAuthusername(idKey, provider);
        if (authentication == null) {
            // Create an authentication
            final String token = RandomStringUtils.randomAlphanumeric(6);
            authentication = baseSecurity.createAndPersistAuthentication(identity, provider, idKey, token);
        }
        return authentication;
    }

    /**
     * Authenticates the identity by token
     * 
     * @param identity
     * @param token
     * @return True if authentication is valid
     */
    public boolean hasValidAuthentication(final Identity identity, final String token) {
        boolean valid = false;
        final Authentication authentication = baseSecurity.findAuthenticationByAuthusername(identity.getKey().toString(), FEED_TOKEN_PROVIDER);
        if (authentication != null && authentication.getCredential().equals(token)) {
            valid = true;
        }
        return valid;
    }

    public Authentication findOrCreateAuthenticationWithRandomCredential_2(final Identity identity, final String provider) {
        Authentication auth = baseSecurity.findAuthentication(identity, provider);
        if (auth == null) {
            // no token found - create one
            auth = baseSecurity.createAndPersistAuthentication(identity, provider, identity.getName(), RandomStringUtils.randomAlphanumeric(6));
        }
        return auth;
    }

    public Authentication findOlatAuthentication(Identity identity) {
        final Authentication auth = baseSecurity.findAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT);
        return auth;
    }

    public Authentication findWebDAVAuthentication(Identity identity) {
        final Authentication auth = baseSecurity.findAuthentication(identity, WebDAVAuthManager.PROVIDER_WEBDAV);
        return auth;
    }

    /**
     * TODO: move this? where?
     */
    public Identity findIdentityByNameOrEmail(String emailOrUsername) {
        // Look for user in "Person" and "user" tables
        Identity identity = null;
        // See if the entered value is a username
        identity = baseSecurity.findIdentityByName(emailOrUsername);
        if (identity == null) {
            // Try fallback with email, maybe user used his email address instead
            // only do this, if its really an email, may lead to multiple results else.
            if (MailHelper.isValidEmailAddress(emailOrUsername)) {
                identity = userService.findIdentityByEmail(emailOrUsername);
            }
        }
        return identity;
    }

    /**
     * @param mail
     * @return
     */
    public boolean isEmailAdressAlreadyUsed(String mail) {
        if (StringHelper.containsNonWhitespace(mail) && MailHelper.isValidEmailAddress(mail)) {
            final SecurityGroup allUsers = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
            final Identity currentIdentity = userService.findIdentityByEmail(mail);
            if (currentIdentity != null && baseSecurity.isIdentityInSecurityGroup(currentIdentity, allUsers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    public int getDisabledUsersCount() {
        final int disabled = baseSecurity.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_LOGIN_DENIED).size();
        return disabled;
    }

    /**
     * @param disabled
     * @return
     */
    public int getActiveUsersCount(final int disabled) {
        final SecurityGroup olatuserGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        final int users = baseSecurity.countIdentitiesOfSecurityGroup(olatuserGroup);
        int numActiveUsers = users - disabled;
        return numActiveUsers;
    }

    /**
     * @return
     */
    public int getActiveUsersCount() {
        return getActiveUsersCount(getDisabledUsersCount());
    }

    /**
     * @return
     */
    public int getAuthorsCount() {
        final PermissionOnResourceable[] permissions = { new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR) };
        final List<Identity> authorsList = baseSecurity.getIdentitiesByPowerSearch(null, null, true, null, permissions, null, null, null, null, null, null);
        final int authors = authorsList.size();
        return authors;
    }

    public List<Identity> searchUsers(final String login, final Map<String, String> userPropertiesSearch, final boolean userPropertiesAsIntersectionSearch) {
        return baseSecurity.getVisibleIdentitiesByPowerSearch((login.equals("") ? null : login), userPropertiesSearch, userPropertiesAsIntersectionSearch, null, null,
                null, null, null);// in normal search fields are intersected
    }

    public List<Identity> searchDeletableUsers(final String login, final Map<String, String> userPropertiesSearch, final boolean userPropertiesAsIntersectionSearch) {
        final List<Identity> users = baseSecurity.getVisibleIdentitiesByPowerSearch((login.equals("") ? null : login), userPropertiesSearch,
                userPropertiesAsIntersectionSearch, // in normal search fields are intersected
                null, null, null, null, null);
        final List<Identity> notDeletable = baseSecurity.getIdentitiesByPowerSearch((login.equals("") ? null : login), userPropertiesSearch,
                userPropertiesAsIntersectionSearch, // in normal search fields are intersected
                null, null, null, null, null, null, null, Identity.STATUS_PERMANENT);
        users.removeAll(notDeletable);
        return users;
    }

    public List<Identity> searchUsers(final UserSearchFilter filter) {
        SecurityGroup[] groups = getSecurityGroups(filter.roles);
        List<Identity> identities = baseSecurity.getIdentitiesByPowerSearch(filter.login, filter.userproperties, true, groups, filter.permissionOnResources,
                filter.authProviders, filter.createdAfter, filter.createdBefore, filter.userLoginAfter, filter.userLoginBefore, filter.status);
        // TODO: LD: this is not very nice, any better ideea?
        if (filter.excludeRoles != null) {
            SecurityGroup[] excludeGroups = getSecurityGroups(filter.excludeRoles);
            final List<Identity> identitiesToExclude = baseSecurity.getVisibleIdentitiesByPowerSearch(null, null, true, excludeGroups, null, null, null, null);
            identities.removeAll(identitiesToExclude);
        }
        return identities;
    }

    private SecurityGroup[] getSecurityGroups(final Roles roles) {
        if (roles == null) {
            return null;
        }

        final List<SecurityGroup> groupsList = new ArrayList<SecurityGroup>();
        if (roles.isOLATAdmin()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(org.olat.data.basesecurity.Constants.GROUP_ADMIN);
            groupsList.add(group);
        }
        if (roles.isAuthor()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(org.olat.data.basesecurity.Constants.GROUP_AUTHORS);
            groupsList.add(group);
        }
        if (roles.isGroupManager()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(org.olat.data.basesecurity.Constants.GROUP_GROUPMANAGERS);
            groupsList.add(group);
        }
        if (roles.isUserManager()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(org.olat.data.basesecurity.Constants.GROUP_USERMANAGERS);
            groupsList.add(group);
        }
        if (roles.isInstitutionalResourceManager()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(org.olat.data.basesecurity.Constants.GROUP_INST_ORES_MANAGER);
            groupsList.add(group);
        }
        if (roles.isUserOnly()) {
            final SecurityGroup group = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
            groupsList.add(group);
        }

        final SecurityGroup[] groups = groupsList.toArray(new SecurityGroup[groupsList.size()]);
        return groups;
    }

    public void changeIdentityRoles(Roles myRoles, final Identity identity, Roles newRoles) {
        changeUserType(myRoles, identity, newRoles.isGuestOnly());

        changeGroupManagerRole(myRoles, identity, newRoles.isGroupManager());

        final boolean isAuthor = newRoles.isAuthor() || newRoles.isInstitutionalResourceManager();
        changeAuthorRole(myRoles, identity, isAuthor);

        changeUserManagerRole(myRoles, identity, newRoles.isUserManager());

        changeInstitutionalManagerRole(myRoles, identity, newRoles.isInstitutionalResourceManager());

        changeAdministratorRole(myRoles, identity, newRoles.isOLATAdmin());
    }

    /**
     * @param identity
     * @param status
     * @param iAmOlatAdmin
     * @param baseSecurity
     */
    public void changeIdentityStatus(final Roles myRoles, final Identity identity, Integer status) {
        if (myRoles.isOLATAdmin() && !identity.getStatus().equals(status)) {
            baseSecurity.saveIdentityStatus(identity, status);
            identity.setStatus(status);
        }
    }

    /**
     * @param identity
     * @param isAdmin
     * @param iAmOlatAdmin
     */
    private void changeAdministratorRole(final Roles myRoles, final Identity identity, final boolean isAdmin) {
        if (myRoles.isOLATAdmin()) {
            final SecurityGroup adminGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_ADMIN);
            updateSecurityGroup(identity, adminGroup, isAdmin);
        }
    }

    /**
     * @param identity
     * @param isInstitutionalResourceManager
     * @param iAmOlatAdmin
     * @param iAmUserManager
     */
    private void changeInstitutionalManagerRole(final Roles myRoles, final Identity identity, final boolean isInstitutionalResourceManager) {
        if (myRoles.isUserManager() || myRoles.isOLATAdmin()) {
            final SecurityGroup institutionalResourceManagerGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
            updateSecurityGroup(identity, institutionalResourceManagerGroup, isInstitutionalResourceManager);
        }
    }

    /**
     * @param identity
     * @param isUserManager
     * @param iAmOlatAdmin
     */
    private void changeUserManagerRole(final Roles myRoles, final Identity identity, final boolean isUserManager) {
        if (myRoles.isOLATAdmin()) {
            final SecurityGroup userManagerGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
            updateSecurityGroup(identity, userManagerGroup, isUserManager);
        }
    }

    /**
     * @param myIdentity
     * @param isAuthor
     * @param iAmOlatAdmin
     */
    private void changeAuthorRole(final Roles myRoles, final Identity myIdentity, final boolean isAuthor) {
        boolean isManagerOfAuthors = isAuthorManager(myRoles);
        if (isManagerOfAuthors) {
            final SecurityGroup authorGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_AUTHORS);
            updateSecurityGroup(myIdentity, authorGroup, isAuthor);
        }
    }

    /**
     * @param myIdentity
     * @param isGroupManager
     * @param iAmOlatAdmin
     */
    private void changeGroupManagerRole(final Roles myRoles, final Identity myIdentity, final boolean isGroupManager) {
        boolean isManagerOfGroupManagers = isManagerOfGroupManagers(myRoles);
        if (isManagerOfGroupManagers) {
            final SecurityGroup groupManagerGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
            updateSecurityGroup(myIdentity, groupManagerGroup, isGroupManager);
        }
    }

    public boolean isManagerOfGroupManagers(final Roles roles) {
        final Boolean canGroupmanagerByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
        boolean isManagerOfGroupManagers = canGroupmanagerByConfig.booleanValue() || roles.isOLATAdmin();
        return isManagerOfGroupManagers;
    }

    public boolean isAuthorManager(final Roles roles) {
        final Boolean canAuthorByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS;
        boolean isManagerOfAuthors = canAuthorByConfig.booleanValue() || roles.isOLATAdmin();
        return isManagerOfAuthors;
    }

    public boolean isGuestManager(final Roles myRoles) {
        final Boolean canGuestsByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;
        boolean isGuestManager = canGuestsByConfig.booleanValue() || myRoles.isOLATAdmin();
        return isGuestManager;
    }

    public boolean isManagerOfInstitutionalResourceManagers(final Roles myRoles) {
        final Boolean canInstitutionalResourceManagerByConfig = BaseSecurityModule.USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER;
        return canInstitutionalResourceManagerByConfig.booleanValue() || myRoles.isOLATAdmin();
    }

    /**
     * Anonymous vs. System User.
     * 
     * @param identity
     * @param isAnonymous
     * @param iAmOlatAdmin
     */
    private void changeUserType(final Roles myRoles, final Identity identity, boolean isAnonymous) {
        boolean isGuestManager = isGuestManager(myRoles);
        if (isGuestManager) {
            final SecurityGroup anonymousGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
            updateSecurityGroup(identity, anonymousGroup, isAnonymous);
            // system users - oposite of anonymous users
            final SecurityGroup usersGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
            final boolean isUser = !isAnonymous;
            updateSecurityGroup(identity, usersGroup, isUser);
        }
    }

    /**
     * Update the security group in the database
     * 
     * @param identity
     * @param securityGroup
     * @param hasBeenInGroup
     * @param isNowInGroup
     */
    private void updateSecurityGroup(final Identity identity, final SecurityGroup securityGroup, final boolean isNowInGroup) {
        final boolean hasBeenInGroup = baseSecurity.isIdentityInSecurityGroup(identity, securityGroup);
        if (!hasBeenInGroup && isNowInGroup) {
            // user not yet in security group, add him
            baseSecurity.addIdentityToSecurityGroup(identity, securityGroup);
        } else if (hasBeenInGroup && !isNowInGroup) {
            // user not anymore in security group, remove him
            baseSecurity.removeIdentityFromSecurityGroup(identity, securityGroup);
        }
    }

    /**
     * @param identity
     */
    public boolean removeIdentityFromSecurityGroup(final Identity identity, final SecurityGroup securityGroup) {
        boolean isRemoved = false;
        if (baseSecurity.isIdentityInSecurityGroup(identity, securityGroup)) {
            baseSecurity.removeIdentityFromSecurityGroup(identity, securityGroup);
            isRemoved = true;
        }
        return isRemoved;
    }

    /**
     * @param identity
     * @return
     */
    public boolean addIdentityToSecurityGroup(final Identity identity, final SecurityGroup securityGroup) {
        boolean isAdded = false;
        if (!baseSecurity.isIdentityInSecurityGroup(identity, securityGroup)) {
            baseSecurity.addIdentityToSecurityGroup(identity, securityGroup);
            isAdded = true;
        }
        return isAdded;
    }

    public Boolean isIdentityInSecurityGroup(final Identity identity, final String securityGroupName) {
        final SecurityGroup secGroup = baseSecurity.findSecurityGroupByName(securityGroupName);
        return baseSecurity.isIdentityInSecurityGroup(identity, secGroup);
    }

    public Boolean isIdentityInSecurityGroup(final Identity identity, final SecurityGroup securityGroup) {
        return baseSecurity.isIdentityInSecurityGroup(identity, securityGroup);
    }

    public void createCourseAdminPolicy(RepositoryEntry re) {
        baseSecurity.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());
    }

    public String getUniqueUsername(final String proposedUsername) {
        final List<Identity> identities = baseSecurity.getIdentitiesByPowerSearch(proposedUsername, null, true, null, null, null, null, null, null, null, null);
        if (identities.isEmpty()) {
            return proposedUsername;
        }

        final Set<String> names = new HashSet<String>();
        for (final Identity identity : identities) {
            names.add(identity.getName());
        }

        String nextPropsedUsername = proposedUsername;
        for (int i = 1; names.contains(nextPropsedUsername); i++) {
            nextPropsedUsername = proposedUsername + i;
        }
        return nextPropsedUsername;
    }

    public boolean isUsernameAlreadyUsedOrOnBlackList(String loginName) {
        final Identity s = baseSecurity.findIdentityByName(loginName);
        return s != null || UserModule.isLoginOnBlacklist(loginName);
    }

    public boolean isUsernameAlreadyUsed(final String loginName) {
        final Identity identity = baseSecurity.findIdentityByName(loginName);
        return identity != null;
    }

    public boolean isChangePasswordPermitted(Identity identity) {
        return isIdentityPermittedOnSecurityResource(identity, SecurityResourceTypeEnum.ChangePasswordController);
    }

    public boolean isChangeUserPasswordPermitted(Identity identity) {
        return isIdentityPermittedOnSecurityResource(identity, SecurityResourceTypeEnum.UserChangePasswordController);
    }

    public boolean isCreateUserPermitted(final Identity identity) {
        return isIdentityPermittedOnSecurityResource(identity, SecurityResourceTypeEnum.UserCreateController);
    }

    public boolean isChangePersonalSettingsPermitted(final Identity identity) {
        return isIdentityPermittedOnSecurityResource(identity, SecurityResourceTypeEnum.PersonalSettingsController);
    }

    public boolean isUserAdministrationPermitted(Identity identity) {
        return isIdentityPermittedOnSecurityResource(identity, SecurityResourceTypeEnum.UserAdminController);
    }

    public boolean isManageUserPermitted(final Identity identity, Roles myRoles) {
        final boolean isOlatAdmin = myRoles.isOLATAdmin();
        if (isOlatAdmin) {
            return true;
        }

        // only admins can administrate admin and usermanager users
        final boolean isAdmin = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN);
        final boolean isUserManager = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_USERMANAGER);
        if (isAdmin || isUserManager) {
            return false;
        }
        // if user is author ony allowed to edit if configured
        final boolean isAuthor = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
        final Boolean canManageAuthor = BaseSecurityModule.USERMANAGER_CAN_MANAGE_AUTHORS;
        if (isAuthor && !canManageAuthor.booleanValue()) {
            return false;
        }
        // if user is groupmanager ony allowed to edit if configured
        final boolean isGroupManager = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GROUPMANAGER);
        final Boolean canManageGroupmanager = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GROUPMANAGERS;
        if (isGroupManager && !canManageGroupmanager.booleanValue()) {
            return false;
        }
        // if user is guest ony allowed to edit if configured
        final boolean isGuestOnly = baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY);
        final Boolean canManageGuest = BaseSecurityModule.USERMANAGER_CAN_MANAGE_GUESTS;
        if (isGuestOnly && !canManageGuest.booleanValue()) {
            return false;
        }
        // passed all tests, current user is allowed to edit given identity
        return true;
    }

    private boolean isIdentityPermittedOnSecurityResource(final Identity identity, final SecurityResourceTypeEnum enumeration) {
        return isIdentityPermittedOnResourceable(identity, OresHelper.createOLATResourceableType(enumeration.name()));
    }

    public boolean isIdentityPermittedOnResourceable(Identity identity, OLATResourceable olatResourceable) {
        return baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_ACCESS, olatResourceable);
    }

    public boolean changePassword(Identity principal, Identity authenticatedIdentity, final String newPwd) {
        return OLATAuthManager.changePassword(principal, authenticatedIdentity, newPwd);
    }

    public int changePaswords(BulkPasswordChangeParameter passwordChangeParameter) {
        int c = 0;
        for (final String username : passwordChangeParameter.getUsernames()) {
            if (username.length() == 0) {
                continue;
            }

            try {
                final Identity identity = baseSecurity.findIdentityByName(username);
                if (identity != null) {
                    if (passwordChangeParameter.getPassword() != null && passwordChangeParameter.getPassword().trim().length() > 0) {

                        OLATAuthManager.changePassword(passwordChangeParameter.getInitiatorIdentity(), identity, passwordChangeParameter.getPassword());
                        log.info("changePassword for username: " + username);
                    }
                    if (passwordChangeParameter.isDisclaimerAccepted()) {
                        registrationService.setHasConfirmedDislaimer(identity);
                        log.info("Disclaimer accepted for username: " + username);
                    }
                    if (passwordChangeParameter.isLanguageDE()) {
                        identity.getUser().getPreferences().setLanguage(I18nManager.getInstance().getLocaleOrDefault("de"));
                        userService.updateUserFromIdentity(identity);
                        log.info("Set language German for username: " + username);
                    }

                    c++;

                } else {
                    log.warn("could find user with username: " + username);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                log.error("Failed to change password/settings for username: " + username, e);
            }
        }
        return c;
    }

    public Identity authenticateOlatOrLdap(final String oldPwd, Identity principal) {
        Identity authenticatedIdentity = null;

        if (baseSecurity.findAuthentication(principal, AUTHENTICATION_PROVIDER_LDAP) != null) {
            final LDAPError ldapError = new LDAPError();
            // fallback to OLAT if enabled happen automatically in LDAPAuthenticationController
            authenticatedIdentity = authenticationService.authenticate(principal.getName(), oldPwd, ldapError);
        } else if (baseSecurity.findAuthentication(principal, AUTHENTICATION_PROVIDER_OLAT) != null) {
            authenticatedIdentity = authenticationService.authenticate(principal.getName(), oldPwd, Provider.OLAT);
        }
        return authenticatedIdentity;
    }

    public List<String> getAuthenticationProviders(final Identity identity) {
        final List<String> providers = new ArrayList<String>();
        final List<Authentication> authentications = baseSecurity.getAuthentications(identity);
        final Iterator<Authentication> iter = authentications.iterator();
        while (iter.hasNext()) {
            providers.add((iter.next()).getProvider());
        }
        return providers;
    }

    public AuthenticationTypes getAuthenticationsTypes(Identity identity) {
        boolean hasOlatToken = false;
        boolean hasWebDAVToken = false;
        final List<Authentication> authentications = baseSecurity.getAuthentications(identity);
        for (final Authentication auth : authentications) {
            if (AUTHENTICATION_PROVIDER_OLAT.equals(auth.getProvider())) {
                hasOlatToken = true;
            } else if (WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())) {
                hasWebDAVToken = true;
            }
        }
        AuthenticationTypes params = new AuthenticationTypes(hasOlatToken, hasWebDAVToken);
        return params;
    }

    public boolean isPropagatePasswordChangeOnLDAPServerConfigured() {
        // LDAP Module propagate changes to password
        return LDAPLoginModule.isPropagatePasswordChangedOnLdapServer();
    }

    public boolean isPasswordChangeConfigured() {
        return UserModule.isPwdchangeallowed();
    }

    public Identity createUser(final String lang, final String username, String pwd, List<UserPropertyParameter> parameters) {
        // Create new user and identity and put user to users group
        // Create transient user without firstName,lastName, email
        final User newUser = userService.createUser(null, null, null);

        // Now add data from user fields (firstName,lastName and email are mandatory)
        for (final UserPropertyParameter parameter : parameters) {
            parameter.userPropertyHandler.updateUserFromFormItem(newUser, parameter.formItem);
        }
        // Init preferences
        newUser.getPreferences().setLanguage(I18nManager.getInstance().getLocaleOrDefault(lang));
        newUser.getPreferences().setInformSessionTimeout(true);
        // Save everything in database
        final Identity ident = baseSecurity.createAndPersistIdentityAndUserWithUserGroup(username, pwd, newUser);
        return ident;
    }

    public Identity createUser(Roles roles, ImportableUserParameter importableUserParameter) {
        // use password only when configured to do so
        if (isCreatePasswordPermitted(roles) && !StringHelper.containsNonWhitespace(importableUserParameter.getPassword())) {
            // treat white-space passwords as no-password. This is fine, a password can be set later on
            // pwd = null;
            importableUserParameter.setPassword(null);
        }

        // Create transient user without firstName,lastName, email
        final User newUser = userService.createUser(null, null, null);

        final List<UserPropertyHandler> userProperties = importableUserParameter.getUserPropertyHandlers();
        int col = 0;
        String thisValue = "", stringValue;
        for (final UserPropertyHandler userPropertyHandler : userProperties) {
            thisValue = importableUserParameter.getUserPropertiesInput().get(col);
            stringValue = userPropertyHandler.getStringValue(thisValue, importableUserParameter.getLocale());
            userPropertyHandler.setUserProperty(newUser, stringValue);
            col++;
        }
        // Init preferences
        newUser.getPreferences().setLanguage(I18nManager.getInstance().getLocaleOrDefault(importableUserParameter.getLanguage()));
        newUser.getPreferences().setInformSessionTimeout(true);
        // Save everything in database
        final Identity ident = baseSecurity.createAndPersistIdentityAndUserWithUserGroup(importableUserParameter.getUsername(), importableUserParameter.getPassword(),
                newUser);
        return ident;
    }

    /**
     * TODO: what is the difference between this and BaseSecurityManager.getRoles? I just think it is code duplication.
     */
    public Roles getRoles(final Identity identity) {
        // get user system roles groups from security manager
        final SecurityGroup adminGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_ADMIN);
        boolean isAdmin = baseSecurity.isIdentityInSecurityGroup(identity, adminGroup);

        final SecurityGroup userManagerGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
        boolean isUserManager = baseSecurity.isIdentityInSecurityGroup(identity, userManagerGroup);

        final SecurityGroup authorGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_AUTHORS);
        boolean isAuthor = baseSecurity.isIdentityInSecurityGroup(identity, authorGroup);

        final SecurityGroup groupmanagerGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_GROUPMANAGERS);
        boolean isGroupManager = baseSecurity.isIdentityInSecurityGroup(identity, groupmanagerGroup);

        final SecurityGroup isAnonymous = baseSecurity.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);
        boolean isGuestOnly = baseSecurity.isIdentityInSecurityGroup(identity, isAnonymous);

        final SecurityGroup institutionalResourceManager = baseSecurity.findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER);
        boolean isInstitutionalResourceManager = baseSecurity.isIdentityInSecurityGroup(identity, institutionalResourceManager);
        return new Roles(isAdmin, isUserManager, isGroupManager, isAuthor, isGuestOnly, isInstitutionalResourceManager, false);
    }

    public boolean isAccessToQuotaPermitted(final boolean isOlatAdmin) {
        final Boolean canQuota = BaseSecurityModule.USERMANAGER_ACCESS_TO_QUOTA;
        return canQuota.booleanValue() || isOlatAdmin;
    }

    public Boolean isStartGroupPermitted() {
        return BaseSecurityModule.USERMANAGER_CAN_START_GROUPS;
    }

    public boolean isPoliciesAccessPermitted(final boolean isOlatAdmin) {
        final Boolean canPolicies = BaseSecurityModule.USERMANAGER_ACCESS_TO_POLICIES;
        return canPolicies.booleanValue() || isOlatAdmin;
    }

    public boolean isEditUserPropertiesPermitted(final boolean isOlatAdmin) {
        final Boolean canProp = BaseSecurityModule.USERMANAGER_ACCESS_TO_PROP;
        return canProp.booleanValue() || isOlatAdmin;
    }

    public boolean isEditAuthenticationsPermitted(final boolean isOlatAdmin) {
        final Boolean canAuth = BaseSecurityModule.USERMANAGER_ACCESS_TO_AUTH;
        return canAuth.booleanValue() || isOlatAdmin;
    }

    public boolean isChangeAndCreatePasswordPermitted(final Identity identity, final boolean isOlatAdmin) {
        boolean canChangeAndCreatePwd = BaseSecurityModule.USERMANAGER_CAN_MODIFY_PWD.booleanValue();
        if (canChangeAndCreatePwd || isOlatAdmin) {
            // show pwd form only if user has also right to create new passwords
            // in case
            // of a user that has no password yet
            final Boolean canCreatePwd = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;
            final Authentication OLATAuth = baseSecurity.findAuthentication(identity, AUTHENTICATION_PROVIDER_OLAT);
            if (OLATAuth != null || canCreatePwd.booleanValue() || isOlatAdmin) {
                canChangeAndCreatePwd &= true;
            } else {
                canChangeAndCreatePwd &= false;
            }
        }
        return canChangeAndCreatePwd;
    }

    public boolean isCreatePasswordPermitted(Roles roles) {
        boolean canCreateOLATPassword = false;
        if (roles.isOLATAdmin()) {
            // admin will override configuration
            canCreateOLATPassword = true;
        } else {
            final Boolean canCreatePwdByConfig = BaseSecurityModule.USERMANAGER_CAN_CREATE_PWD;
            canCreateOLATPassword = canCreatePwdByConfig.booleanValue();
        }
        return canCreateOLATPassword;
    }

    public Boolean isEditAllProfileFieldPermitted(final Identity identity) {
        Boolean canEditAllFields = BaseSecurityModule.USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS;
        if (baseSecurity.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)) {
            canEditAllFields = Boolean.TRUE;
        }
        return canEditAllFields;
    }

    public Invitation isInvitationValid(final String invitationToken) {
        final boolean hasPolicies = baseSecurity.hasInvitationPolicies(invitationToken, new Date());
        if (hasPolicies) {
            final Invitation invitation = baseSecurity.findInvitation(invitationToken);
            if (invitation != null) {
                return invitation;
            }
        }

        return null;
    }

    public boolean isOlatUser(final Identity identity) {
        final SecurityGroup allUsers = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        boolean isIdentityInSecurityGroup = baseSecurity.isIdentityInSecurityGroup(identity, allUsers);
        return isIdentityInSecurityGroup;
    }

    public Identity createUserWithRandomUsername(Locale locale, final Invitation invitation) {
        final String tempUsername = UUID.randomUUID().toString();
        final User user = userService.createAndPersistUser(invitation.getFirstName(), invitation.getLastName(), invitation.getMail());
        user.getPreferences().setLanguage(locale.toString());
        final Identity invitedIdentity = baseSecurity.createAndPersistIdentity(tempUsername, user, null, null, null);
        baseSecurity.addIdentityToSecurityGroup(invitedIdentity, invitation.getSecurityGroup());
        return invitedIdentity;
    }

    public Identity createUserViaShibbolethRegistration(String choosenLogin, String email, Map<String, String> shibbolethAttributesMap, String shibbolethUniqueID) {
        Identity identity;
        User user;
        final String firstName = shibbolethAttributesMap.get(ShibbolethModule.getFirstName());
        final String lastName = shibbolethAttributesMap.get(ShibbolethModule.getLastName());

        user = userService.createUser(firstName, lastName, email);
        userService.setUserProperty(user, UserConstants.INSTITUTIONALNAME, shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalName()));
        if (hasEmailInShibAttributes()) {
            final String institutionalEmail = ShibbolethHelper.getFirstValueOf(ShibbolethModule.getInstitutionalEMail(), shibbolethAttributesMap);
            userService.setUserProperty(user, UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
        }
        userService.setUserProperty(user, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER,
                shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalMatriculationNumber()));
        userService.setUserProperty(user, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalEmployeeNumber()));
        identity = baseSecurityService.createAndPersistIdentityAndUser(choosenLogin, user, AUTHENTICATION_PROVIDER_SHIB, shibbolethUniqueID, null);
        final SecurityGroup olatUserGroup = baseSecurity.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        baseSecurity.addIdentityToSecurityGroup(identity, olatUserGroup);
        // tell system that this user did accept the disclaimer
        registrationService.setHasConfirmedDislaimer(identity);
        return identity;
    }

    public void createShibAuthenticationAndUpdateUser(final Identity authenticationedIdentity, Map<String, String> shibbolethAttributesMap, String shibbolethUniqueID) {
        baseSecurity.createAndPersistAuthentication(authenticationedIdentity, AUTHENTICATION_PROVIDER_SHIB, shibbolethUniqueID, null);

        // update user profile
        final User user = authenticationedIdentity.getUser();
        String s = shibbolethAttributesMap.get(ShibbolethModule.getFirstName());
        if (s != null) {
            userService.setUserProperty(user, UserConstants.FIRSTNAME, s);
        }
        s = shibbolethAttributesMap.get(ShibbolethModule.getLastName());
        if (s != null) {
            userService.setUserProperty(user, UserConstants.LASTNAME, s);
        }
        s = shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalName());
        if (s != null) {
            userService.setUserProperty(user, UserConstants.INSTITUTIONALNAME, s);
        }
        s = ShibbolethHelper.getFirstValueOf(ShibbolethModule.getInstitutionalEMail(), shibbolethAttributesMap);
        if (s != null) {
            userService.setUserProperty(user, UserConstants.INSTITUTIONALEMAIL, s);
        }
        s = shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalMatriculationNumber());
        if (s != null) {
            userService.setUserProperty(user, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, s);
        }
        s = shibbolethAttributesMap.get(ShibbolethModule.getInstitutionalEmployeeNumber());
        if (s != null) {
            userService.setUserProperty(user, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, s);
        }
        userService.updateUser(user);
    }

    private boolean hasEmailInShibAttributes() {
        return (ShibbolethModule.getEMail() == null) ? false : true;
    }

    public List<Identity> getIdentitiesByInstitutialUserIdentifier(Map<String, String> userProperties) {
        return baseSecurity.getIdentitiesByPowerSearch(null, userProperties, true, null, null, null, null, null, null, null, null);

    }

    public void updateInstitutionalShibbolethUserProperties(User user, Map<String, String> attributesMap) {
        updateUserPropertyWhenChanged(user, UserConstants.INSTITUTIONALNAME, attributesMap.get(ShibbolethModule.getInstitutionalName()));
        updateUserPropertyWhenChanged(user, UserConstants.INSTITUTIONAL_EMPLOYEE_NUMBER, attributesMap.get(ShibbolethModule.getInstitutionalEmployeeNumber()));
        updateUserPropertyWhenChanged(user, UserConstants.INSTITUTIONAL_MATRICULATION_NUMBER, attributesMap.get(ShibbolethModule.getInstitutionalMatriculationNumber()));
        updateUserPropertyWhenChanged(user, UserConstants.INSTITUTIONALEMAIL, attributesMap.get(ShibbolethModule.getInstitutionalEMail()));
    }

    void updateUserPropertyWhenChanged(User user, String propertyName, String propertyValue) {
        if ((propertyValue != null) && hasPropertyValueChanged(user, propertyName, propertyValue)) {
            log.info("Update user-property for user:" + user + " , propertyName:" + propertyName + " , propertyValue:" + propertyValue);
            userService.setUserProperty(user, propertyName, propertyValue);
            userService.updateUser(user);
        }
    }

    private boolean hasPropertyValueChanged(User user, String propertyName, String attributeValue) {
        return (userService.getUserProperty(user, propertyName) == null) || (!userService.getUserProperty(user, propertyName).equals(attributeValue));
    }

    public List<Identity> getNewIdentityCreated(final Date from) {
        if (from == null) {
            return Collections.emptyList();
        }

        final PermissionOnResourceable[] permissions = { new PermissionOnResourceable(Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY) };
        final List<Identity> guests = baseSecurity.getIdentitiesByPowerSearch(null, null, true, null, permissions, null, from, null, null, null,
                Identity.STATUS_VISIBLE_LIMIT);
        final List<Identity> identities = baseSecurity.getIdentitiesByPowerSearch(null, null, true, null, null, null, from, null, null, null,
                Identity.STATUS_VISIBLE_LIMIT);
        if (!identities.isEmpty() && !guests.isEmpty()) {
            identities.removeAll(guests);
        }

        for (final Iterator<Identity> identityIt = identities.iterator(); identityIt.hasNext();) {
            final Identity identity = identityIt.next();
            if (identity.getCreationDate().before(from)) {
                identityIt.remove();
            }
        }

        return identities;
    }

}
