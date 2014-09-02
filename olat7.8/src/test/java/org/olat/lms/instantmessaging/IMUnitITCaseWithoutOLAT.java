package org.olat.lms.instantmessaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Instant Messaging integration JUnit Tests, relying on a working IM OpenFire Jabberserver
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
        "classpath:org/olat/connectors/instantmessaging/_spring/instantMessagingContext.xml",
        "classpath:org/olat/lms/instantmessaging/_spring/instantMessagingContext.xml" })
@Ignore
public class IMUnitITCaseWithoutOLAT extends AbstractJUnit4SpringContextTests {

    @Autowired
    IMConfig config;
    @Autowired
    InstantMessaging im;

    @Test
    public void testNormal() {
        /**
         * Precondition
         */
        assertNotNull(config);
        // only run IM tests if enabled
        assumeTrue(config.isEnabled());

        /**
         * test
         */
        assertNotNull(im);
        assertTrue(im.getConfig().isEnabled());

        final String username = "unittest";
        final String password = "test";
        final String fullname = "test test";
        final String email = "@test.ch";
        String groupId = "testgroup-1234556";
        final String groupname = "testgroupABC";

        groupId = im.getNameHelper().getGroupnameForOlatInstance(groupId);

        // test api functions that do not need OLAT runtime
        final String tmpUsermaster = username + 0;
        for (int j = 0; j < 4; j++) {
            final String tmpUsername = username + j;
            assertFalse(im.hasAccount(tmpUsername));
            assertTrue(im.createAccount(tmpUsername, password, fullname, username + j + email));
            assertTrue(im.hasAccount(tmpUsername));
            assertTrue(im.addUserToFriendsRoster(tmpUsermaster, groupId, groupname, tmpUsername));
        }
        assertTrue(im.renameRosterGroup(groupId, groupname + "CDEF"));
        assertTrue(im.removeUserFromFriendsRoster(groupId, tmpUsermaster));
        assertTrue(im.removeUserFromFriendsRoster(groupId, username + 1));
        assertTrue(im.deleteRosterGroup(groupId));
        for (int j = 0; j < 4; j++) {
            final String tmpUsername = username + j;
            assertTrue(im.deleteAccount(tmpUsername));
        }
    }

}
