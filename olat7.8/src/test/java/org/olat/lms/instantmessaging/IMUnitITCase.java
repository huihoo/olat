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
package org.olat.lms.instantmessaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Authentication;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * IM junit tests
 * <P>
 * Initial Date: Nov 16, 2006 <br>
 * 
 * @author guido
 */
public class IMUnitITCase extends OlatTestCase {
    String testUserA = "anIdentity1";
    String testUserB = "anIdentity2";
    String testUserC = "testuser@thankyou2010.com";

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private BaseSecurity securityManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        JunitTestHelper.createAndPersistIdentityAsUser(testUserA);
        JunitTestHelper.createAndPersistIdentityAsUser(testUserB);
        JunitTestHelper.createAndPersistIdentityAsUser(testUserC);
        DBFactory.getInstance().closeSession();
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        try {
            final DB db = DBFactory.getInstance();
            db.closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

    @Test
    public void testIMStuff() {
        if (InstantMessagingModule.isEnabled()) {
            try {
                final String groupIdPlain = "junittestgroup-12343w5234";
                final String groupName = "junittestgroup";
                final InstantMessaging im = InstantMessagingModule.getAdapter();
                final String groupId = im.getNameHelper().getGroupnameForOlatInstance(groupIdPlain);

                // first delete possible accounts and groups on the IM server
                InstantMessagingModule.getAdapter().deleteAccount(testUserA);
                InstantMessagingModule.getAdapter().deleteAccount(testUserB);
                InstantMessagingModule.getAdapter().deleteAccount(testUserC);
                im.deleteRosterGroup(groupId);

                final Authentication authC = securityManager.findAuthenticationByAuthusername(testUserC, ClientManager.PROVIDER_INSTANT_MESSAGING);
                if (authC != null) {
                    securityManager.deleteAuthentication(authC);
                }
                DBFactory.getInstance().intermediateCommit();
                final InstantMessagingClient imClientC = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserC);
                // wait some time as connection process is in background thread
                Thread.sleep(3000);
                assertTrue(imClientC.isConnected());
                imClientC.closeConnection(true);
                assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserC));

                // delete IM passwords, otherwise accounts don't get created
                final Authentication authA = securityManager.findAuthenticationByAuthusername(testUserA, ClientManager.PROVIDER_INSTANT_MESSAGING);
                final Authentication authB = securityManager.findAuthenticationByAuthusername(testUserB, ClientManager.PROVIDER_INSTANT_MESSAGING);

                if (authA != null) {
                    securityManager.deleteAuthentication(authA);
                }
                if (authB != null) {
                    securityManager.deleteAuthentication(authB);
                }

                // get the IM client, it connects automatically to the server (creates an account on the im server)
                final InstantMessagingClient imClientA = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserA);
                assertNotNull(imClientA);
                final InstantMessagingClient imClientB = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserB);
                assertNotNull(imClientB);
                Thread.sleep(1000);
                assertEquals(true, imClientA.isConnected());
                final int groupCountA = imClientA.getRoster().getGroupCount();
                assertEquals(true, imClientB.isConnected());

                assertTrue(im.countConnectedUsers() >= 2); // there is may be as well an admin user connected

                // add user to roster
                im.addUserToFriendsRoster(testUserA, groupId, groupName, testUserB);
                Thread.sleep(1000);
                assertEquals(1, imClientA.getRoster().getGroup(groupName).getEntryCount());
                Thread.sleep(1000);
                im.renameRosterGroup(groupId, groupName + "ABC");
                Thread.sleep(1000);
                assertEquals(1, imClientA.getRoster().getGroup(groupName + "ABC").getEntryCount());
                Thread.sleep(1000);
                im.removeUserFromFriendsRoster(groupId, testUserB);
                Thread.sleep(1000);
                im.deleteRosterGroup(groupId);
                Thread.sleep(1000);
                assertEquals(groupCountA, imClientA.getRoster().getGroupCount());

                // localy we do not have all information we need
                // todo, add dummy values locally as we do not have authUserSessions
                if (CoordinatorManager.getInstance().getCoordinator().isClusterMode()) {
                    final List<ConnectedUsersListEntry> l = im.getAllConnectedUsers(null);
                    final ConnectedUsersListEntry entry = l.get(1);
                    assertNotNull(entry);
                }

                im.getClientManager().destroyInstantMessagingClient(testUserA);
                im.getClientManager().destroyInstantMessagingClient(testUserB);

                // delete the accounts with random passwords and recreate the default ones
                assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserA));
                assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserB));

            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
