package org.olat.lms.security.authentication.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.data.basesecurity.SecurityGroup;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.user.User;
import org.olat.lms.user.UserService;
import org.olat.lms.user.administration.delete.UserDeletionManager;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * LDAP junit tests please import "olattest.ldif" into your configured LDAP directory
 * <P>
 * Initial Date: June 30, 2008 <br>
 * 
 * @author Maurus Rohrer
 */
public class LDAPLoginITCase extends OlatTestCase {

    private UserService userService;
    @Autowired
    private BaseSecurity securityManager;

    @Before
    public void setup() {
        userService = CoreSpringFactory.getBean(UserService.class);
    }

    @Test
    public void testSystemBind() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        // edit olatextconfig.xml for testing
        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
        final LdapContext ctx = ldapManager.bindSystem();
        assertEquals(true, (ctx != null));
    }

    @Test
    public void testCreateUser() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);

        final String uid = "mrohrer";
        final String userPW = "olat";
        final LDAPError errors = new LDAPError();

        final boolean usersSyncedAtStartup = LDAPLoginModule.isLdapSyncOnStartup();
        // user should not exits in OLAT when not synced during startup
        assertEquals(usersSyncedAtStartup, (securityManager.findIdentityByName(uid) != null));
        // bind user
        final Attributes attrs = ldapManager.bindUser(uid, userPW, errors);
        assertEquals(usersSyncedAtStartup, (securityManager.findIdentityByName(uid) != null));
        // user should be created
        ldapManager.createAndPersistUser(attrs);
        assertEquals(true, (securityManager.findIdentityByName(uid) != null));

        // should fail, user is existing
        ldapManager.createAndPersistUser(attrs);
        assertEquals(true, (securityManager.findIdentityByName(uid) != null));
    }

    @Test
    public void testUserBind() throws NamingException {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
        final LDAPError errors = new LDAPError();
        String uid = "mrohrer";
        String userPW = "olat";

        // normal bind, should work
        Attributes attrs = ldapManager.bindUser(uid, userPW, errors);
        assertEquals("Rohrer", attrs.get("sn").get());

        // wrong password, should fail
        userPW = "haha";
        attrs = ldapManager.bindUser(uid, userPW, errors);
        assertEquals("Username or passwort incorrect", errors.get());

        // wrong username, should fail
        uid = "ruedisueli";
        userPW = "olat";
        attrs = ldapManager.bindUser(uid, userPW, errors);
        assertEquals("Username or passwort incorrect", errors.get());

        // no password, should fail
        uid = "mrohrer";
        userPW = null;
        attrs = ldapManager.bindUser(uid, userPW, errors);
        assertEquals("Username and passwort must be selected", errors.get());
    }

    @Test
    public void testCheckUser() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);
        final LDAPError errors = new LDAPError();

        // should create error entry, since Administrator is existing in OLAT but not Managed by LDAP
        String uid = "Administrator";
        Identity identity = ldapManager.findIdentyByLdapAuthentication(uid, errors);
        assertEquals("findIdentyByLdapAuthentication: User with username::Administrator exist but not Managed by LDAP", errors.get());

        // should return null, since user duda is not existing
        uid = "duda";
        identity = ldapManager.findIdentyByLdapAuthentication(uid, errors);
        assertEquals(true, (identity == null));
        assertEquals(true, errors.isEmpty());

        // should return identity, since is existing in OLAT and Managed by LDAP
        uid = "mrohrer";
        identity = ldapManager.findIdentyByLdapAuthentication(uid, errors);
        assertEquals(uid, identity.getName());
        assertEquals(true, errors.isEmpty());
    }

    @Test
    public void testCreateChangedAttrMap() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        // simulate closed session (user adding from startup job)
        DBFactory.getInstance().intermediateCommit();

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);

        String uid = "kmeier";
        final String pwd = "olat";
        final LDAPError errors = new LDAPError();

        final boolean usersSyncedAtStartup = LDAPLoginModule.isLdapSyncOnStartup();
        if (usersSyncedAtStartup) {
            try {
                // create user but with different attributes - must fail since user already exists
                final User user = userService.createUser("klaus", "Meier", "klaus@meier.ch");
                final Identity identity = securityManager.createAndPersistIdentityAndUser("kmeier", user, "LDAP", "kmeier", null);
                final SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
                securityManager.addIdentityToSecurityGroup(identity, secGroup);

                // simulate closed session (user adding from startup job)
                DBFactory.getInstance().intermediateCommit();
                fail("Expected constrant violation becaus of doupliate entry");
            } catch (final Exception e) {
                // success, this is what we expected
            }
            // changedAttrMap empty since already synchronized
            final Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
            final Identity identitys = securityManager.findIdentityByName(uid);
            final Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
            // map is empty - no attributes to sync
            assertNull(changedAttrMap);
        } else {
            // create user but with different attributes - must fail since user already exists
            final User user = userService.createUser("klaus", "Meier", "klaus@meier.ch");
            final Identity identity = securityManager.createAndPersistIdentityAndUser("kmeier", user, "LDAP", "kmeier", null);
            final SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
            securityManager.addIdentityToSecurityGroup(identity, secGroup);
            // simulate closed session (user adding from startup job)
            DBFactory.getInstance().intermediateCommit();

            // changedAttrMap has 2 changes and uid as entrys (Klaus!=klaus, klaus@olat.org!=klaus@meier.ch)
            final Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
            final Identity identitys = securityManager.findIdentityByName(uid);
            final Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
            // result must be 3: 2 changed plus the user ID which is always in the map
            assertEquals(3, changedAttrMap.keySet().size());
        }

        // nothing to change for this user
        uid = "mrohrer";
        final Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
        final Identity identitys = securityManager.findIdentityByName(uid);
        final Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
        assertEquals(true, (changedAttrMap == null));
    }

    @Test
    public void testSyncUser() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);

        Map<String, String> changedMap = new HashMap<String, String>();
        final LDAPError errors = new LDAPError();

        changedMap.put("userID", "kmeier");
        changedMap.put("firstName", "Klaus");
        changedMap.put("email", "kmeier@olat.org");
        changedMap.put("institutionalName", "Informatik");
        changedMap.put("homepage", "http://www.olat.org");
        final Identity identity = securityManager.findIdentityByName("kmeier");
        ldapManager.syncUser(changedMap, identity);

        changedMap.put("userID", "kmeier");
        final Attributes attrs = ldapManager.bindUser("kmeier", "olat", errors);
        changedMap = ldapManager.prepareUserPropertyForSync(attrs, identity);
        assertEquals(true, (changedMap == null));
    }

    @Test
    public void testIdentityDeletedInLDAP() {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        final LDAPLoginManager ldapManager = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);

        List<Identity> deletList;

        // should be empty
        final LdapContext ctx = ldapManager.bindSystem();
        deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
        assertEquals(0, (deletList.size()));

        // simulate closed session (user adding from startup job)
        DBFactory.getInstance().intermediateCommit();

        // create some users in LDAPSecurityGroup
        User user = userService.createUser("grollia", "wa", "gorrila@olat.org");
        Identity identity = securityManager.createAndPersistIdentityAndUser("gorilla", user, "LDAP", "gorrila", null);
        final SecurityGroup secGroup1 = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
        securityManager.addIdentityToSecurityGroup(identity, secGroup1);
        user = userService.createUser("wer", "immer", "immer@olat.org");
        identity = securityManager.createAndPersistIdentityAndUser("der", user, "LDAP", "der", null);
        securityManager.addIdentityToSecurityGroup(identity, secGroup1);
        user = userService.createUser("die", "da", "chaspi@olat.org");
        identity = securityManager.createAndPersistIdentityAndUser("das", user, "LDAP", "das", null);
        securityManager.addIdentityToSecurityGroup(identity, secGroup1);

        // simulate closed session
        DBFactory.getInstance().intermediateCommit();

        // 3 members in LDAP group but not existing in OLAT
        deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
        assertEquals(3, (deletList.size()));

        // delete user in OLAT
        securityManager.removeIdentityFromSecurityGroup(identity, secGroup1);
        UserDeletionManager.getInstance().deleteIdentity(identity);

        // simulate closed session
        DBFactory.getInstance().intermediateCommit();

        // 2 members in LDAP group but not existing in OLAT
        deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
        assertEquals(2, (deletList.size()));
    }

    @Test
    public void testCronSync() throws Exception {
        if (!LDAPLoginModule.isLDAPEnabled()) {
            return;
        }

        LdapContext ctx;
        List<Attributes> ldapUserList;
        List<Attributes> newLdapUserList;
        Map<Identity, Map<String, String>> changedMapIdenityMap;
        List<Identity> deletedUserList;
        String user;
        final LDAPError errors = new LDAPError();

        final LDAPLoginManager ldapMan = (LDAPLoginManager) CoreSpringFactory.getBean(LDAPLoginManager.class);

        // find user changed after 2010,01,09,00,00
        ctx = ldapMan.bindSystem();
        Date syncDate = new Date(110, 00, 10, 00, 00);
        ldapUserList = ldapMan.getUserAttributesModifiedSince(syncDate, ctx);
        assertEquals(1, ldapUserList.size());

        // find all users
        syncDate = null;
        ldapUserList = ldapMan.getUserAttributesModifiedSince(syncDate, ctx);
        assertEquals(6, ldapUserList.size());

        // prepare create- and sync-Lists for each user from defined syncTime
        Identity idenity;
        Map<String, String> changedAttrMap;
        newLdapUserList = new LinkedList<Attributes>();
        changedMapIdenityMap = new HashMap<Identity, Map<String, String>>();
        for (int i = 0; i < ldapUserList.size(); i++) {
            user = getAttributeValue(ldapUserList.get(i).get(LDAPLoginModule.mapOlatPropertyToLdapAttribute("userID")));
            idenity = ldapMan.findIdentyByLdapAuthentication(user, errors);
            if (idenity != null) {
                changedAttrMap = ldapMan.prepareUserPropertyForSync(ldapUserList.get(i), idenity);
                if (changedAttrMap != null) {
                    changedMapIdenityMap.put(idenity, changedAttrMap);
                }
            } else {
                if (errors.isEmpty()) {
                    final String[] reqAttrs = LDAPLoginModule.checkReqAttr(ldapUserList.get(i));
                    if (reqAttrs == null) {
                        newLdapUserList.add(ldapUserList.get(i));
                    } else {
                        System.out.println("Cannot create User " + user + " required Attributes are missing");
                    }
                } else {
                    System.out.println(errors.get());
                }
            }
        }

        // create Users in LDAP Group only existing in OLAT
        User user1 = userService.createUser("hansi", "hürlima", "hansi@hansli.com");
        Identity identity1 = securityManager.createAndPersistIdentityAndUser("hansi", user1, "LDAP", "hansi", null);
        final SecurityGroup secGroup1 = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
        securityManager.addIdentityToSecurityGroup(identity1, secGroup1);
        user1 = userService.createUser("chaspi", "meier", "chaspi@hansli.com");
        identity1 = securityManager.createAndPersistIdentityAndUser("chaspi", user1, "LDAP", "chaspi", null);
        securityManager.addIdentityToSecurityGroup(identity1, secGroup1);

        // create User to Delete List
        deletedUserList = ldapMan.getIdentitysDeletedInLdap(ctx);
        assertEquals(4, (deletedUserList.size()));

        // sync users
        final Iterator<Identity> itrIdent = changedMapIdenityMap.keySet().iterator();
        while (itrIdent.hasNext()) {
            final Identity ident = itrIdent.next();
            ldapMan.syncUser(changedMapIdenityMap.get(ident), ident);
        }

        // create all users
        for (int i = 0; i < newLdapUserList.size(); i++) {
            ldapMan.createAndPersistUser(newLdapUserList.get(i));
        }

        // delete all users
        ldapMan.deletIdentities(deletedUserList);

        // check if users are deleted
        deletedUserList = ldapMan.getIdentitysDeletedInLdap(ctx);
        assertEquals(0, (deletedUserList.size()));

    }

    private String getAttributeValue(final Attribute attribute) throws NamingException {
        final String attrValue = (String) attribute.get();
        return attrValue;
    }
}
