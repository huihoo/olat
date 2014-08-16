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
package org.olat.lms.commentandrate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commentandrate.UserComment;
import org.olat.data.commentandrate.UserRating;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBFactory;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * Test class for user comments package
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsAndRatingsITCase extends OlatTestCase {
    private static final Logger log = LoggerHelper.getLogger();

    private static boolean isInitialized = false;
    private static Identity ident1, ident2, ident3;
    private final static String identityTest1Name = "identityTest1";
    private final static String identityTest2Name = "identityTest2";
    private final static String identityTest3Name = "identityTest3";

    private static final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testresource", Long.valueOf(1234l));
    private static CommentAndRatingService service, serviceWithSubPath;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setup() throws Exception {
        if (isInitialized == false) {
            ident1 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest1Name);
            ident2 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest2Name);
            ident3 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest3Name);
            DBFactory.getInstance().closeSession();
            //
            final CommentAndRatingService service = applicationContext.getBean(CommentAndRatingService.class);
            if (service != null) {
                service.init(ident1, ores, null, true, false);
            }
            final CommentAndRatingService serviceWithSubPath = applicationContext.getBean(CommentAndRatingService.class);
            if (serviceWithSubPath != null) {
                serviceWithSubPath.init(ident1, ores, "blubli", true, false);
            }
            //
            isInitialized = true;
        }
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
    public void testCRUDComment() {
        if (service == null) {
            return;
        }
        assertEquals(Long.valueOf(0), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(0), serviceWithSubPath.getUserCommentsManager().countComments());

        // add comments
        final UserCommentsManager ucm = service.getUserCommentsManager();
        final UserCommentsManager ucm2 = serviceWithSubPath.getUserCommentsManager();

        UserComment comment1 = ucm.createComment(ident1, "Hello World");
        UserComment comment2 = ucm2.createComment(ident1, "Hello World with subpath");
        // count must be 1 now. count without subpath should not include the results with subpath
        assertEquals(Long.valueOf(1), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(1), serviceWithSubPath.getUserCommentsManager().countComments());
        //
        final UserComment comment3 = ucm.createComment(ident2, "Hello World");
        final UserComment comment4 = ucm2.createComment(ident2, "Hello World with subpath");
        assertEquals(Long.valueOf(2), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(2), serviceWithSubPath.getUserCommentsManager().countComments());
        // Same with get method
        final List<UserComment> commentList = ucm.getComments();
        assertEquals(2, commentList.size());
        final List<UserComment> commentList2 = ucm2.getComments();
        assertEquals(2, commentList2.size());
        // Create a reply to the first comments
        ucm.replyTo(comment1, ident2, "Reply 1");
        ucm.replyTo(comment2, ident2, "Reply 1 with subpath");
        assertEquals(Long.valueOf(3), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(3), serviceWithSubPath.getUserCommentsManager().countComments());
        // Delete first created coment with one reply each
        ucm.deleteComment(comment1, true);
        ucm2.deleteComment(comment2, true);
        assertEquals(Long.valueOf(1), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(1), serviceWithSubPath.getUserCommentsManager().countComments());
        // Create reply to a comment that does not exis anymore -> should not create anything
        assertNull(ucm.replyTo(comment1, ident2, "Reply 1"));
        assertNull(ucm.replyTo(comment2, ident2, "Reply 1 with subpath"));
        // Recreate first comment
        comment1 = ucm.createComment(ident1, "Hello World");
        comment2 = ucm2.createComment(ident1, "Hello World with subpath");
        // Recreate a reply to the first comments
        ucm.replyTo(comment1, ident2, "Reply 1");
        ucm.replyTo(comment2, ident2, "Reply 1 with subpath");
        assertEquals(Long.valueOf(3), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(3), serviceWithSubPath.getUserCommentsManager().countComments());
        // Delete first created coment without the reply
        ucm.deleteComment(comment1, false);
        ucm2.deleteComment(comment2, false);
        assertEquals(Long.valueOf(2), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(2), serviceWithSubPath.getUserCommentsManager().countComments());
        // Delete all comments
        assertEquals(2, ucm.deleteAllComments());
        assertEquals(Long.valueOf(0), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(2), serviceWithSubPath.getUserCommentsManager().countComments());
        // Delete ignoring subpath
        comment1 = ucm.createComment(ident1, "Hello World");
        assertEquals(Long.valueOf(1), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(2), serviceWithSubPath.getUserCommentsManager().countComments());
        assertEquals(3, ucm2.deleteAllCommentsIgnoringSubPath());
        assertEquals(Long.valueOf(0), service.getUserCommentsManager().countComments());
        assertEquals(Long.valueOf(0), serviceWithSubPath.getUserCommentsManager().countComments());
    }

    @Test
    public void testCRUDRating() {
        if (service == null) {
            return;
        }
        // add comments
        final UserRatingsManager urm = service.getUserRatingsManager();
        final UserRatingsManager urm2 = serviceWithSubPath.getUserRatingsManager();

        assertEquals(Float.valueOf(0), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(0), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(0), urm.countRatings());
        assertEquals(Long.valueOf(0), urm2.countRatings());

        UserRating r1 = urm.createRating(ident1, 2);
        UserRating r2 = urm2.createRating(ident1, 2);
        assertEquals(Float.valueOf(2), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(2), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(1), urm.countRatings());
        assertEquals(Long.valueOf(1), urm2.countRatings());
        //
        final UserRating r3 = urm.createRating(ident2, 4);
        final UserRating r4 = urm2.createRating(ident2, 4);
        assertEquals(Float.valueOf(3), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(3), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(2), urm.countRatings());
        assertEquals(Long.valueOf(2), urm2.countRatings());
        //
        final UserRating r5 = urm.createRating(ident3, 1);
        final UserRating r6 = urm2.createRating(ident3, 1);
        assertEquals(Float.valueOf(2.5f), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(2.5f), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(3), urm.countRatings());
        assertEquals(Long.valueOf(3), urm2.countRatings());
        //
        assertNotNull(urm.getRating(ident1));
        assertNotNull(urm.getRating(ident2));
        assertNotNull(urm.getRating(ident3));
        // can not create two ratings per person
        r1 = urm.createRating(ident1, 2);
        r2 = urm2.createRating(ident1, 2);
        assertEquals(Float.valueOf(2.5f), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(2.5f), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(3), urm.countRatings());
        assertEquals(Long.valueOf(3), urm2.countRatings());
        // Delete
        urm.deleteAllRatings();
        assertEquals(Float.valueOf(0), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(2.5f), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(0), urm.countRatings());
        assertEquals(Long.valueOf(3), urm2.countRatings());
        // Recreate and delete ignoring subpath
        r1 = urm.createRating(ident1, 2);
        r2 = urm2.createRating(ident1, 2);
        assertEquals(Float.valueOf(2), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(2.5f), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(1), urm.countRatings());
        assertEquals(Long.valueOf(3), urm2.countRatings());
        urm.deleteAllRatingsIgnoringSubPath();
        assertEquals(Float.valueOf(0), urm.calculateRatingAverage());
        assertEquals(Float.valueOf(0), urm2.calculateRatingAverage());
        assertEquals(Long.valueOf(0), urm.countRatings());
        assertEquals(Long.valueOf(0), urm2.countRatings());
        //
    }

}
