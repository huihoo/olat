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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.lms.security.authentication.ldap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import net.fortuna.ical4j.util.TimeZones;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Constants;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.data.user.UserConstants;
import org.olat.lms.security.BaseSecurityService;
import org.olat.lms.user.UserService;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.commons.WebappHelper;
import org.olat.system.commons.taskexecutor.TaskExecutorManager;
import org.olat.system.coordinate.Coordinator;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.event.Event;
import org.olat.system.event.GenericEventListener;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.mail.MailHelper;
import org.olat.system.security.AuthenticationConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: This manager handles communication between LDAP and OLAT. LDAP access is done by JNDI. The synching is done only on node 1 of a cluster.
 * <p>
 * LDAPLoginMangerImpl
 * <p>
 * 
 * @author Maurus Rohrer
 */
public class LDAPLoginManagerImpl extends LDAPLoginManager implements GenericEventListener {

    private static final Logger log = LoggerHelper.getLogger();

    private static final TimeZone UTC_TIME_ZONE;
    private static boolean batchSyncIsRunning = false;
    private static Date lastSyncDate = null; // first sync is always a full sync

    private static final int PAGE_SIZE = 50;
    private static final String PAGED_RESULT_CONTROL_OID = "1.2.840.113556.1.4.319";

    private final Coordinator coordinator;
    private final TaskExecutorManager taskExecutorManager;
    @Autowired
    private BaseSecurity securityManager;

    @Autowired
    private BaseSecurityService baseSecurityService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserDeletionManager userDeletionManager;

    static {
        UTC_TIME_ZONE = TimeZone.getTimeZone(TimeZones.UTC_ID);
    }

    /**
     * Private constructor. Use LDAPLoginManager.getInstance() method instead
     */
    private LDAPLoginManagerImpl(final CoordinatorManager coordinatorManager, final TaskExecutorManager taskExecutorManager) {
        super();
        this.coordinator = coordinatorManager.getCoordinator();
        this.taskExecutorManager = taskExecutorManager;

        coordinator.getEventBus().registerFor(this, null, ldapSyncLockOres);
    }

    @Override
    public void event(final Event event) {
        if (event instanceof LDAPEvent) {
            if (LDAPEvent.SYNCHING.equals(event.getCommand())) {
                batchSyncIsRunning = true;
            } else if (LDAPEvent.SYNCHING_ENDED.equals(event.getCommand())) {
                batchSyncIsRunning = false;
                lastSyncDate = ((LDAPEvent) event).getTimestamp();
            } else if (LDAPEvent.DO_SYNCHING.equals(event.getCommand())) {
                doHandleBatchSync();
            }
        }
    }

    private void doHandleBatchSync() {
        if (WebappHelper.getNodeId() != 1) {
            return;
        }

        final Runnable batchSyncTask = new Runnable() {
            public void run() {
                final LDAPError errors = new LDAPError();
                doBatchSync(errors);
            }
        };
        taskExecutorManager.runTask(batchSyncTask);
    }

    /**
     * Connect to the LDAP server with System DN and Password Configuration: LDAP URL = olatextconfig.xml (property=ldapURL) System DN = olatextconfig.xml
     * (property=ldapSystemDN) System PW = olatextconfig.xml (property=ldapSystemPW)
     * 
     * @return The LDAP connection (LdapContext) or NULL if connect fails
     * @throws NamingException
     */
    public LdapContext bindSystem() {
        // set LDAP connection attributes
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAPLoginModule.getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, LDAPLoginModule.getLdapSystemDN());
        env.put(Context.SECURITY_CREDENTIALS, LDAPLoginModule.getLdapSystemPW());

        // check ssl
        if (LDAPLoginModule.isSslEnabled()) {
            enableSSL(env);
        }

        try {
            final InitialLdapContext ctx = new InitialLdapContext(env, new Control[] {});
            ctx.getConnectControls();
            return ctx;
        } catch (final NamingException e) {
            log.error("NamingException when trying to bind system with DN::" + LDAPLoginModule.getLdapSystemDN() + " and PW::" + LDAPLoginModule.getLdapSystemPW()
                    + " on URL::" + LDAPLoginModule.getLdapUrl(), e);
            return null;
        } catch (final Exception e) {
            log.error("Exception when trying to bind system with DN::" + LDAPLoginModule.getLdapSystemDN() + " and PW::" + LDAPLoginModule.getLdapSystemPW()
                    + " on URL::" + LDAPLoginModule.getLdapUrl(), e);
            return null;
        }

    }

    /**
     * Connect to LDAP with the User-Name and Password given as parameters Configuration: LDAP URL = olatextconfig.xml (property=ldapURL) LDAP Base = olatextconfig.xml
     * (property=ldapBase) LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
     * 
     * @param uid
     *            The users LDAP login name (can't be null)
     * @param pwd
     *            The users LDAP password (can't be null)
     * @return After successful bind Attributes otherwise NULL
     * @throws NamingException
     */
    public Attributes bindUser(final String uid, final String pwd, final LDAPError errors) {
        // get user name, password and attributes
        final String ldapUrl = LDAPLoginModule.getLdapUrl();
        final String[] userAttr = LDAPLoginModule.getUserAttrs();

        if (uid == null || pwd == null) {
            if (log.isDebugEnabled()) {
                log.debug("Error when trying to bind user, missing username or password. Username::" + uid + " pwd::" + pwd);
            }
            errors.insert("Username and password must be selected");
            return null;
        }

        final LdapContext ctx = bindSystem();
        if (ctx == null) {
            errors.insert("LDAP connection error");
            return null;
        }
        final String userDN = searchUserDN(uid, ctx);
        if (userDN == null) {
            log.info("Error when trying to bind user with username::" + uid + " - user not found on LDAP server"
                    + (LDAPLoginModule.isCacheLDAPPwdAsOLATPwdOnLogin() ? ", trying with OLAT login provider" : ""));
            errors.insert("Username or password incorrect");
            return null;
        }

        // Ok, so far so good, user exists. Now try to fetch attributes using the
        // users credentials
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, pwd);
        if (LDAPLoginModule.isSslEnabled()) {
            enableSSL(env);
        }

        try {
            final Control[] connectCtls = new Control[] {};
            final LdapContext userBind = new InitialLdapContext(env, connectCtls);
            final Attributes attributes = userBind.getAttributes(userDN, userAttr);
            userBind.close();
            return attributes;
        } catch (final AuthenticationException e) {
            log.info("Error when trying to bind user with username::" + uid + " - invalid LDAP password");
            errors.insert("Username or password incorrect");
            return null;
        } catch (final NamingException e) {
            log.error("NamingException when trying to get attributes after binding user with username::" + uid, e);
            errors.insert("Username or password incorrect");
            return null;
        }
    }

    /**
     * Change the password on the LDAP server.
     * 
     */
    @Override
    public void changePassword(final Identity identity, final String pwd, final LDAPError errors) {
        final String uid = identity.getName();
        final String ldapUserPasswordAttribute = LDAPLoginModule.getLdapUserPasswordAttribute();
        try {
            final DirContext ctx = bindSystem();
            final String dn = searchUserDN(uid, ctx);

            final ModificationItem[] modificationItems = new ModificationItem[1];

            Attribute userPasswordAttribute;
            if (LDAPLoginModule.isActiveDirectory()) {
                // active directory need the password enquoted and unicoded (but little-endian)
                final String quotedPassword = "\"" + pwd + "\"";
                final char unicodePwd[] = quotedPassword.toCharArray();
                final byte pwdArray[] = new byte[unicodePwd.length * 2];
                for (int i = 0; i < unicodePwd.length; i++) {
                    pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
                    pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
                }
                userPasswordAttribute = new BasicAttribute(ldapUserPasswordAttribute, pwdArray);
            } else {
                userPasswordAttribute = new BasicAttribute(ldapUserPasswordAttribute, pwd);
            }

            modificationItems[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute);
            ctx.modifyAttributes(dn, modificationItems);
            ctx.close();
        } catch (final NamingException e) {
            log.error("NamingException when trying to change password with username::" + uid, e);
            errors.insert("Cannot change the password");
        }
    }

    /**
     * Find the user dn with its uid
     * 
     * @param uid
     * @param ctx
     * @return user's dn
     */
    private String searchUserDN(final String uid, final DirContext ctx) {
        if (ctx == null) {
            return null;
        }

        final List<String> ldapBases = LDAPLoginModule.getLdapBases();
        final String objctClass = LDAPLoginModule.getLdapUserObjectClass();
        final String[] serachAttr = { "dn" };

        final String ldapUserIDAttribute = LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
        final String filter = "(&(objectClass=" + objctClass + ")(" + ldapUserIDAttribute + "=" + uid + "))";
        final SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(serachAttr);

        String userDN = null;
        for (final String ldapBase : ldapBases) {
            try {
                final NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
                while (enm.hasMore()) {
                    final SearchResult result = enm.next();
                    userDN = result.getNameInNamespace();
                }
                if (userDN != null) {
                    break;
                }
            } catch (final NamingException e) {
                log.error("NamingException when trying to bind user with username::" + uid + " on ldapBase::" + ldapBase, e);
            }
        }

        return userDN;
    }

    /**
     * Creates list of all LDAP Users or changed Users since syncTime Configuration: userAttr = olatextconfig.xml (property=userAttrs) LDAP Base = olatextconfig.xml
     * (property=ldapBase)
     * 
     * @param syncTime
     *            The time to search in LDAP for changes since this time. SyncTime has to formatted: JJJJMMddHHmm
     * @param ctx
     *            The LDAP system connection, if NULL or closed NamingExecpiton is thrown
     * @return Returns list of Arguments of found users or empty list if search fails or nothing is changed
     * @throws NamingException
     */
    public List<Attributes> getUserAttributesModifiedSince(final Date syncTime, final LdapContext ctx) {
        final String objctClass = LDAPLoginModule.getLdapUserObjectClass();
        final StringBuilder filter = new StringBuilder();
        if (syncTime == null) {
            filter.append("(objectClass=").append(objctClass).append(")");
        } else {
            final String dateFormat = LDAPLoginModule.getLdapDateFormat();
            final SimpleDateFormat generalizedTimeFormatter = new SimpleDateFormat(dateFormat);
            generalizedTimeFormatter.setTimeZone(UTC_TIME_ZONE);
            final String syncTimeForm = generalizedTimeFormatter.format(syncTime);
            filter.append("(&(objectClass=").append(objctClass).append(")(|(");
            filter.append(LDAPLoginModule.getLdapUserLastModifiedTimestampAttribute()).append(">=").append(syncTimeForm);
            filter.append(")(");
            filter.append(LDAPLoginModule.getLdapUserCreatedTimestampAttribute()).append(">=").append(syncTimeForm);
            filter.append(")))");
        }
        final List<Attributes> ldapUserList = new ArrayList<Attributes>();

        searchInLdap(new LdapVisitor() {
            public void visit(final SearchResult result) {
                ldapUserList.add(result.getAttributes());
            }
        }, filter.toString(), LDAPLoginModule.getUserAttrs(), ctx);

        return ldapUserList;
    }

    /**
     * Delete all Identities in List and removes them from LDAPSecurityGroup
     * 
     * @param identityList
     *            List of Identities to delete
     */
    public void deletIdentities(final List<Identity> identityList) {
        final SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);

        for (final Identity identity : identityList) {
            securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
            userDeletionManager.deleteIdentity(identity);
            DBFactory.getInstance().intermediateCommit();
        }
    }

    /**
     * Sync all OLATPropertys in Map of Identity
     * 
     * @param olatPropertyMap
     *            Map of changed OLAT properties (OLATProperty,LDAPValue)
     * @param identity
     *            Identity to sync
     */
    public void syncUser(final Map<String, String> olatPropertyMap, final Identity identity) {
        if (identity == null) {
            log.warn("Identiy is null - should not happen", null);
            return;
        }
        final User user = identity.getUser();
        // remove user identifyer - can not be changed later
        olatPropertyMap.remove(LDAPConstants.LDAP_USER_IDENTIFYER);
        // remove attributes that are defined as sync-only-on-create
        final Set<String> syncOnlyOnCreateProperties = LDAPLoginModule.getSyncOnlyOnCreateProperties();
        if (syncOnlyOnCreateProperties != null) {
            for (final String syncOnlyOnCreateKey : syncOnlyOnCreateProperties) {
                olatPropertyMap.remove(syncOnlyOnCreateKey);
            }
        }

        for (final Map.Entry<String, String> keyValuePair : olatPropertyMap.entrySet()) {
            final String propName = keyValuePair.getKey();
            final String value = keyValuePair.getValue();
            if (value == null) {
                if (userService.getUserProperty(user, propName) != null) {
                    userService.setUserProperty(user, propName, value);
                }
            } else {
                userService.setUserProperty(user, propName, value);
            }
        }

        // Add static user properties from the configuration
        final Map<String, String> staticProperties = LDAPLoginModule.getStaticUserProperties();
        if (staticProperties != null && staticProperties.size() > 0) {
            for (final Map.Entry<String, String> staticProperty : staticProperties.entrySet()) {
                userService.setUserProperty(user, staticProperty.getKey(), staticProperty.getValue());
            }
        }
    }

    /**
     * Creates User in OLAT and ads user to LDAP securityGroup Required Attributes have to be checked before this method.
     * 
     * @param userAttributes
     *            Set of LDAP Attribute of User to be created
     */
    @SuppressWarnings("unchecked")
    public void createAndPersistUser(final Attributes userAttributes) {
        // Get and Check Config
        final String[] reqAttrs = LDAPLoginModule.checkReqAttr(userAttributes);
        if (reqAttrs != null) {
            log.warn("Can not create and persist user, the following attributes are missing::" + ArrayUtils.toString(reqAttrs), null);
            return;
        }

        final String uid = getAttributeValue(userAttributes.get(LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)));
        final String email = getAttributeValue(userAttributes.get(LDAPLoginModule.mapOlatPropertyToLdapAttribute(UserConstants.EMAIL)));
        // Lookup user
        if (securityManager.findIdentityByName(uid) != null) {
            log.error("Can't create user with username='" + uid + "', does already exist in OLAT database", null);
            return;
        }
        if (!MailHelper.isValidEmailAddress(email)) {
            // needed to prevent possibly an AssertException in findIdentityByEmail breaking the sync!
            log.error("Cannot try to lookup user " + uid + " by email with an invalid email::" + email, null);
            return;
        }
        if (userService.findIdentityByEmail(email) != null) {
            log.error("Can't create user with email='" + email + "', does already exist in OLAT database", null);
            return;
        }

        // Create User (first and lastname is added in next step)
        final User user = userService.createUser(null, null, email);
        // Set User Property's (Iterates over Attributes and gets OLAT Property out
        // of olatexconfig.xml)
        final NamingEnumeration<Attribute> neAttr = (NamingEnumeration<Attribute>) userAttributes.getAll();
        try {
            while (neAttr.hasMore()) {
                final Attribute attr = neAttr.next();
                final String olatProperty = mapLdapAttributeToOlatProperty(attr.getID());
                if (attr.get() != uid) {
                    final String ldapValue = getAttributeValue(attr);
                    if (olatProperty == null || ldapValue == null) {
                        continue;
                    }
                    userService.setUserProperty(user, olatProperty, ldapValue);
                }
            }
            // Add static user properties from the configuration
            final Map<String, String> staticProperties = LDAPLoginModule.getStaticUserProperties();
            if (staticProperties != null && staticProperties.size() > 0) {
                for (final Entry<String, String> staticProperty : staticProperties.entrySet()) {
                    userService.setUserProperty(user, staticProperty.getKey(), staticProperty.getValue());
                }
            }
        } catch (final NamingException e) {
            log.error("NamingException when trying to create and persist LDAP user with username::" + uid, e);
            return;
        } catch (final Exception e) {
            // catch any exception here to properly log error
            log.error("Unknown exception when trying to create and persist LDAP user with username::" + uid, e);
            return;
        }

        // Create Identity
        final Identity identity = baseSecurityService.createAndPersistIdentityAndUser(uid, user, AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP, uid, null);
        // Add to SecurityGroup LDAP
        SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
        securityManager.addIdentityToSecurityGroup(identity, secGroup);
        // Add to SecurityGroup OLATUSERS
        secGroup = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
        securityManager.addIdentityToSecurityGroup(identity, secGroup);
        log.info("Created LDAP user username::" + uid);

    }

    /**
     * Checks if LDAP properties are different then OLAT properties of a User. If they are different a Map (OlatPropertyName,LDAPValue) is returned.
     * 
     * @param attributes
     *            Set of LDAP Attribute of Identity
     * @param identity
     *            Identity to compare
     * @return Map(OlatPropertyName,LDAPValue) of properties Identity, where property has changed. NULL is returned it no attributes have to be synced
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> prepareUserPropertyForSync(final Attributes attributes, final Identity identity) {
        final Map<String, String> olatPropertyMap = new HashMap<String, String>();
        final User user = identity.getUser();
        final NamingEnumeration<Attribute> neAttrs = (NamingEnumeration<Attribute>) attributes.getAll();
        try {
            while (neAttrs.hasMore()) {
                final Attribute attr = neAttrs.next();
                final String olatProperty = mapLdapAttributeToOlatProperty(attr.getID());
                if (olatProperty == null) {
                    continue;
                }
                final String ldapValue = getAttributeValue(attr);
                final String olatValue = userService.getUserProperty(user, olatProperty);
                if (olatValue == null) {
                    // new property or user ID (will always be null, pseudo property)
                    olatPropertyMap.put(olatProperty, ldapValue);
                } else {
                    if (ldapValue.compareTo(olatValue) != 0) {
                        olatPropertyMap.put(olatProperty, ldapValue);
                    }
                }
            }
            if (olatPropertyMap.size() == 1 && olatPropertyMap.get(LDAPConstants.LDAP_USER_IDENTIFYER) != null) {
                return null;
            }
            return olatPropertyMap;

        } catch (final NamingException e) {
            log.error("NamingException when trying to prepare user properties for LDAP sync", e);
            return null;
        }
    }

    /**
     * Maps LDAP Attributes to the OLAT Property Configuration: LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
     * 
     * @param attrID
     *            LDAP Attribute
     * @return OLAT Property
     */
    private String mapLdapAttributeToOlatProperty(final String attrID) {
        final Map<String, String> userAttrMapper = LDAPLoginModule.getUserAttributeMapper();
        final String olatProperty = userAttrMapper.get(attrID);
        return olatProperty;
    }

    /**
     * Extracts Value out of LDAP Attribute
     * 
     * @param attribute
     *            LDAP Naming Attribute
     * @return String value of Attribute, null on Exception
     * @throws NamingException
     */
    private String getAttributeValue(final Attribute attribute) {
        try {
            final String attrValue = (String) attribute.get();
            return attrValue;
        } catch (final NamingException e) {
            log.error("NamingException when trying to get attribute value for attribute::" + attribute, e);
            return null;
        }
    }

    /**
     * Searches for Identity in OLAT.
     * 
     * @param uid
     *            Name of Identity
     * @param errors
     *            LDAPError Object if user exits but not member of LDAPSecurityGroup
     * @return Identity if it's found and member of LDAPSecurityGroup, null otherwise (if user exists but not managed by LDAP, error Object is modified)
     */
    public Identity findIdentyByLdapAuthentication(final String uid, final LDAPError errors) {
        final Identity identity = securityManager.findIdentityByName(uid);
        if (identity == null) {
            return null;
        } else {
            final SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
            if (ldapGroup == null) {
                log.error("Error getting user from OLAT security group '" + LDAPConstants.SECURITY_GROUP_LDAP + "' : group does not exist", null);
                return null;
            }
            if (securityManager.isIdentityInSecurityGroup(identity, ldapGroup)) {
                final Authentication ldapAuth = securityManager.findAuthentication(identity, AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP);
                if (ldapAuth == null) {
                    // BUG Fixe: update the user and test if it has a ldap provider
                    securityManager.createAndPersistAuthentication(identity, AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP, identity.getName(), null);
                }
                return identity;
            } else {
                if (LDAPLoginModule.isConvertExistingLocalUsersToLDAPUsers()) {
                    // Add user to LDAP security group and add the ldap provider
                    securityManager.createAndPersistAuthentication(identity, AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP, identity.getName(), null);
                    securityManager.addIdentityToSecurityGroup(identity, ldapGroup);
                    log.info("Found identity by LDAP username that was not yet in LDAP security group. Converted user::" + uid + " to be an LDAP managed user");
                    return identity;
                } else {
                    errors.insert("findIdentyByLdapAuthentication: User with username::" + uid + " exist but not Managed by LDAP");
                    return null;
                }

            }
        }
    }

    /**
     * Creates list of all OLAT Users which have been deleted out of the LDAP directory but still exits in OLAT Configuration: Required Attributes = olatextconfig.xml
     * (property=reqAttrs) LDAP Base = olatextconfig.xml (property=ldapBase)
     * 
     * @param syncTime
     *            The time to search in LDAP for changes since this time. SyncTime has to formatted: JJJJMMddHHmm
     * @param ctx
     *            The LDAP system connection, if NULL or closed NamingExecpiton is thrown
     * @return Returns list of Identity from the user which have been deleted in LDAP
     * @throws NamingException
     */
    public List<Identity> getIdentitysDeletedInLdap(final LdapContext ctx) {
        if (ctx == null) {
            return null;
        }
        // Find all LDAP Users
        final String userID = LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER);
        final String objctClass = LDAPLoginModule.getLdapUserObjectClass();
        final List<String> ldapList = new ArrayList<String>();

        searchInLdap(new LdapVisitor() {
            public void visit(final SearchResult result) throws NamingException {
                final Attributes attrs = result.getAttributes();
                final NamingEnumeration<? extends Attribute> aEnum = attrs.getAll();
                while (aEnum.hasMore()) {
                    final Attribute attr = aEnum.next();
                    // use lowercase username
                    ldapList.add(attr.get().toString().toLowerCase());
                }
            }
        }, "(objectClass=" + objctClass + ")", new String[] { userID }, ctx);

        if (ldapList.isEmpty()) {
            log.warn("No users in LDAP found, can't create deletionList!!", null);
            return null;
        }

        // Find all User in OLAT, members of LDAPSecurityGroup
        final SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
        if (ldapGroup == null) {
            log.error("Error getting users from OLAT security group '" + LDAPConstants.SECURITY_GROUP_LDAP + "' : group does not exist", null);
            return null;
        }

        final List<Identity> identityListToDelete = new ArrayList<Identity>();
        final List<Identity> olatListIdentity = securityManager.getIdentitiesOfSecurityGroup(ldapGroup);
        for (final Identity ida : olatListIdentity) {
            // compare usernames with lowercase
            if (!ldapList.contains(ida.getName().toLowerCase())) {
                identityListToDelete.add(ida);
            }
        }
        return identityListToDelete;
    }

    private void searchInLdap(final LdapVisitor visitor, final String filter, final String[] returningAttrs, final LdapContext ctx) {
        final SearchControls ctls = new SearchControls();
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        ctls.setReturningAttributes(returningAttrs);
        ctls.setCountLimit(0); // set no limits

        final boolean paging = isPagedResultControlSupported(ctx);
        for (final String ldapBase : LDAPLoginModule.getLdapBases()) {
            int counter = 0;
            try {
                if (paging) {
                    byte[] cookie = null;
                    ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, Control.NONCRITICAL) });
                    do {
                        final NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
                        while (enm.hasMore()) {
                            visitor.visit(enm.next());
                        }
                        cookie = getCookie(ctx);
                    } while (cookie != null);
                } else {
                    final NamingEnumeration<SearchResult> enm = ctx.search(ldapBase, filter, ctls);
                    while (enm.hasMore()) {
                        visitor.visit(enm.next());
                    }
                    counter++;
                }
            } catch (final SizeLimitExceededException e) {
                log.error("SizeLimitExceededException after " + counter
                        + " records when getting all users from LDAP, reconfigure your LDAP server, hints: http://www.ldapbrowser.com/forum/viewtopic.php?t=14", null);
            } catch (final NamingException e) {
                log.error("NamingException when trying to fetch deleted users from LDAP using ldapBase::" + ldapBase + " on row::" + counter, e);
            } catch (final Exception e) {
                log.error("Exception when trying to fetch deleted users from LDAP using ldapBase::" + ldapBase + " on row::" + counter, e);
            }
        }
    }

    private byte[] getCookie(final LdapContext ctx) throws NamingException, IOException {
        byte[] cookie = null;
        // Examine the paged results control response
        final Control[] controls = ctx.getResponseControls();
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    final PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                    cookie = prrc.getCookie();
                }
            }
        }
        // Re-activate paged results
        ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL) });
        return cookie;
    }

    private boolean isPagedResultControlSupported(final LdapContext ctx) {
        try {
            final SearchControls ctl = new SearchControls();
            ctl.setReturningAttributes(new String[] { "supportedControl" });
            ctl.setSearchScope(SearchControls.OBJECT_SCOPE);

            /* search for the rootDSE object */
            final NamingEnumeration<SearchResult> results = ctx.search("", "(objectClass=*)", ctl);

            while (results.hasMore()) {
                final SearchResult entry = results.next();
                final NamingEnumeration<? extends Attribute> attrs = entry.getAttributes().getAll();
                while (attrs.hasMore()) {
                    final Attribute attr = attrs.next();
                    final NamingEnumeration<?> vals = attr.getAll();
                    while (vals.hasMore()) {
                        final String value = (String) vals.next();
                        if (value.equals(PAGED_RESULT_CONTROL_OID)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (final Exception e) {
            log.error("Exception when trying to know if the server support paged results.", e);
            return false;
        }
    }

    /**
     * Execute Batch Sync. Will update all Attributes of LDAP users in OLAt, create new users and delete users in OLAT. Can be configured in olatextconfig.xml
     * 
     * @param LDAPError
     */
    public boolean doBatchSync(final LDAPError errors) {
        if (WebappHelper.getNodeId() != 1) {
            log.warn("Sync happens only on node 1", null);
            return false;
        }

        // o_clusterNOK
        // Synchronize on class so that only one thread can read the
        // batchSyncIsRunning flag Only this read operation is synchronized to not
        // block the whole execution of the do BatchSync method. The method is used
        // in automatic cron scheduler job and also in GUI controllers that can't
        // wait for the concurrent running request to finish first, an immediate
        // feedback about the concurrent job is needed. -> only synchronize on the
        // property read.
        synchronized (LDAPLoginManagerImpl.class) {
            if (batchSyncIsRunning) {
                // don't run twice, skip this execution
                log.info("LDAP user doBatchSync started, but another job is still running - skipping this sync");
                errors.insert("BatchSync already running by concurrent process");
                return false;
            }
        }

        coordinator.getEventBus().fireEventToListenersOf(new LDAPEvent(LDAPEvent.SYNCHING), ldapSyncLockOres);

        LdapContext ctx = null;
        boolean success = false;
        try {
            acquireSyncLock();
            ctx = bindSystem();
            if (ctx == null) {
                errors.insert("LDAP connection ERROR");
                log.error("Error in LDAP batch sync: LDAP connection empty", null);
                freeSyncLock();
                success = false;
                return success;
            }
            final Date timeBeforeSync = new Date();

            // check server capabilities
            // Get time before sync to have a save sync time when sync is successful
            final String sinceSentence = (lastSyncDate == null ? " (full sync)" : " since last sync from " + lastSyncDate);
            doBatchSyncDeletedUsers(ctx, sinceSentence);
            doBatchSyncNewAndModifiedUsers(ctx, sinceSentence, errors);

            // update sync time and set running flag
            lastSyncDate = timeBeforeSync;

            ctx.close();
            success = true;
            return success;
        } catch (final Exception e) {

            errors.insert("Unknown error");
            log.error("Error in LDAP batch sync, unknown reason", e);
            success = false;
            return success;
        } finally {
            freeSyncLock();
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (final NamingException e) {
                    // try but failed silently
                }
            }
            final LDAPEvent endEvent = new LDAPEvent(LDAPEvent.SYNCHING_ENDED);
            endEvent.setTimestamp(new Date());
            endEvent.setSuccess(success);
            endEvent.setErrors(errors);
            coordinator.getEventBus().fireEventToListenersOf(endEvent, ldapSyncLockOres);
        }
    }

    private void doBatchSyncDeletedUsers(final LdapContext ctx, final String sinceSentence) {
        // create User to Delete List
        final List<Identity> deletedUserList = getIdentitysDeletedInLdap(ctx);
        // delete old users
        if (deletedUserList == null || deletedUserList.size() == 0) {
            log.info("LDAP batch sync: no users to delete" + sinceSentence);
        } else {
            if (LDAPLoginModule.isDeleteRemovedLDAPUsersOnSync()) {
                // check if more not more than the defined percentages of
                // users managed in LDAP should be deleted
                // if they are over the percentage, they will not be deleted
                // by the sync job
                final SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
                final List<Identity> olatListIdentity = securityManager.getIdentitiesOfSecurityGroup(ldapGroup);
                if (olatListIdentity.isEmpty()) {
                    log.info("No users managed by LDAP, can't delete users");
                } else {
                    final int prozente = (int) (((float) deletedUserList.size() / (float) olatListIdentity.size()) * 100);
                    if (prozente >= LDAPLoginModule.getDeleteRemovedLDAPUsersPercentage()) {
                        log.info("LDAP batch sync: more than " + LDAPLoginModule.getDeleteRemovedLDAPUsersPercentage()
                                + "% of LDAP managed users should be deleted. Please use Admin Deletion Job. Or increase deleteRemovedLDAPUsersPercentage. " + prozente
                                + "% tried to delete.");
                    } else {
                        // delete users
                        deletIdentities(deletedUserList);
                        log.info("LDAP batch sync: " + deletedUserList.size() + " users deleted" + sinceSentence);
                    }
                }
            } else {
                // Do nothing, only log users to logfile
                final StringBuilder users = new StringBuilder();
                for (final Identity toBeDeleted : deletedUserList) {
                    users.append(toBeDeleted.getName()).append(',');
                }
                log.info("LDAP batch sync: " + deletedUserList.size() + " users detected as to be deleted" + sinceSentence
                        + ". Automatic deleting is disabled in LDAPLoginModule, delete these users manually::[" + users.toString() + "]");
            }
        }
    }

    private void doBatchSyncNewAndModifiedUsers(final LdapContext ctx, final String sinceSentence, final LDAPError errors) {
        // Get new and modified users from LDAP
        int count = 0;
        final List<Attributes> ldapUserList = getUserAttributesModifiedSince(lastSyncDate, ctx);

        // Check for new and modified users
        final List<Attributes> newLdapUserList = new ArrayList<Attributes>();
        final Map<Identity, Map<String, String>> changedMapIdentityMap = new HashMap<Identity, Map<String, String>>();
        for (final Attributes userAttrs : ldapUserList) {
            final String user = getAttributeValue(userAttrs.get(LDAPLoginModule.mapOlatPropertyToLdapAttribute(LDAPConstants.LDAP_USER_IDENTIFYER)));
            final Identity identity = findIdentyByLdapAuthentication(user, errors);
            if (identity != null) {
                final Map<String, String> changedAttrMap = prepareUserPropertyForSync(userAttrs, identity);
                if (changedAttrMap != null) {
                    changedMapIdentityMap.put(identity, changedAttrMap);
                }
            } else if (errors.isEmpty()) {
                final String[] reqAttrs = LDAPLoginModule.checkReqAttr(userAttrs);
                if (reqAttrs == null) {
                    newLdapUserList.add(userAttrs);
                } else {
                    log.warn("Error in LDAP batch sync: can't create user with username::" + user + " : missing required attributes::" + ArrayUtils.toString(reqAttrs),
                            null);
                }
            } else {
                log.warn(errors.get(), null);
            }
            if (++count % 20 == 0) {
                DBFactory.getInstance().intermediateCommit();
            }
        }

        // sync existing users
        if (changedMapIdentityMap == null || changedMapIdentityMap.isEmpty()) {
            log.info("LDAP batch sync: no users to sync" + sinceSentence);
        } else {
            for (final Identity ident : changedMapIdentityMap.keySet()) {
                syncUser(changedMapIdentityMap.get(ident), ident);
                // REVIEW Identity are not saved???
                if (++count % 20 == 0) {
                    DBFactory.getInstance().intermediateCommit();
                }
            }
            log.info("LDAP batch sync: " + changedMapIdentityMap.size() + " users synced" + sinceSentence);
        }

        // create new users
        if (newLdapUserList.isEmpty()) {
            log.info("LDAP batch sync: no users to create" + sinceSentence);
        } else {

            for (final Attributes userAttrs : newLdapUserList) {
                createAndPersistUser(userAttrs);
                if (++count % 20 == 0) {
                    DBFactory.getInstance().intermediateCommit();
                }
            }
            log.info("LDAP batch sync: " + newLdapUserList.size() + " users created" + sinceSentence);
        }
    }

    /**
	 */
    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    /**
     * Internal helper to add the SSL protocol to the environment
     * 
     * @param env
     */
    private void enableSSL(final Hashtable<String, String> env) {
        env.put(Context.SECURITY_PROTOCOL, "ssl");
        System.setProperty("javax.net.ssl.trustStore", LDAPLoginModule.getTrustStoreLocation());
    }

    /**
     * Acquire lock for administration jobs
     */
    public synchronized boolean acquireSyncLock() {
        if (batchSyncIsRunning) {
            return false;
        }
        batchSyncIsRunning = true;
        return true;
    }

    /**
     * Release lock for administration jobs
     */
    public synchronized void freeSyncLock() {
        batchSyncIsRunning = false;
    }

    public interface LdapVisitor {
        public void visit(SearchResult searchResult) throws NamingException;
    }

    /**
	 */
    @Override
    public boolean isControllerAndNotDisposed() {
        return false;
    }

    public boolean canChangePassword(Identity identity) {
        // check if LDAP is enabled + cannot propagate password on LDAP Server
        if (LDAPLoginModule.isLDAPEnabled() && !LDAPLoginModule.isPropagatePasswordChangedOnLdapServer()) {
            // check if the user has a LDAP Authentication
            final Authentication auth = securityManager.findAuthentication(identity, AuthenticationConstants.AUTHENTICATION_PROVIDER_LDAP);
            return auth == null;// if not in LDAP -> can change his password
        }
        return true;
    }

}
