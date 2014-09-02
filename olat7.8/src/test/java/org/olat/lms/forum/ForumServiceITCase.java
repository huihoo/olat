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

package org.olat.lms.forum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.forum.MessageImpl;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.user.UserService;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This test actually the ForumService.
 * 
 * @author Felix Jost
 */

public class ForumServiceITCase extends OlatTestCase {

    private static final Logger log = LoggerHelper.getLogger();

    public Identity u1;
    public Identity u2;
    public Identity u3;
    @Autowired
    public UserService userService;
    @Autowired
    public ForumService forumService;
    public Forum fo;

    private Message message1, m3;

    /* mock for not-publish event */
    PublishEventTO publishEventTO;

    /**
     * SetUp is called before each test
     */
    @Before
    public void setup() {
        // create some users with user manager
        try {
            log.info("setUp start ------------------------");

            // um1.resetSession(sess);
            u1 = JunitTestHelper.createAndPersistIdentityAsUser("forum_felix");
            u2 = JunitTestHelper.createAndPersistIdentityAsUser("forum_migros");
            u3 = JunitTestHelper.createAndPersistIdentityAsUser("forum_salat");

            fo = forumService.addAForum();

            message1 = new MessageImpl();
            message1.setTitle("stufe 0: subject 0");
            message1.setBody("body/n dep 0");

            final Message m2 = new MessageImpl();
            m2.setTitle("stufe 0: subject 1");
            m2.setBody("body 2 /n dep 0");

            m3 = new MessageImpl();
            m3.setTitle("stufe 1: subject 2");
            m3.setBody("body 21 /n dep 1");

            final Message m4 = new MessageImpl();
            m4.setTitle("stufe 1: subject 3");
            m4.setBody("body 211 /n dep 2");

            publishEventTO = createPublishEventTO(u1, PublishEventTO.EventType.NEW, message1);
            forumService.addTopMessage(fo, message1, u1, publishEventTO);

            publishEventTO = createPublishEventTO(u2, PublishEventTO.EventType.NEW, m2);
            forumService.addTopMessage(fo, m2, u2, publishEventTO);

            publishEventTO = createPublishEventTO(u3, PublishEventTO.EventType.CHANGED, m3);
            forumService.replyToMessage(u3, m2, m3, publishEventTO);

            publishEventTO = createPublishEventTO(u1, PublishEventTO.EventType.CHANGED, m4);
            forumService.replyToMessage(u1, m3, m4, publishEventTO);

            for (int i = 0; i < 10; i++) {
                final Message m = new MessageImpl();
                m.setTitle("Title" + i);
                m.setBody("Body" + i);
                forumService.replyToMessage(u1, m4, m, publishEventTO);
            }
            log.info("setUp done ------------------------");
        } catch (final Exception e) {
            log.error("Exception in setUp(): " + e);
        }
    }

    private PublishEventTO createPublishEventTO(Identity creatorIdentity, PublishEventTO.EventType type, Message message) {
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(ContextType.COURSE, 0L, "AAA_course", 0L, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, 0L,
                "a_Forum", message.getTitle(), creatorIdentity, type);
        return publishEventTO;
    }

    /**
     * TearDown is called after each test
     */
    @After
    public void tearDown() {
        try {
            DBFactory.getInstance().closeSession();
        } catch (final Exception e) {
            log.error("Exception in tearDown(): " + e);
        }
    }

    @Test
    public void testGetMessagesByForumID() throws Exception {
        log.debug("Start testGetMessagesByForumID()");

        final long start = System.currentTimeMillis();
        final Forum forum = forumService.loadForum(fo.getKey());
        final List messageList = forumService.getMessagesByForum(forum);
        final long stop = System.currentTimeMillis();
        assertNotNull(messageList);
        log.debug("time:" + (stop - start));
        final Iterator it = messageList.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            log.debug("object:" + o);
            final Message msg = (Message) o;
            log.debug("msg:" + msg.getTitle());
        }
        assertEquals("Not the right number of messages for this forum", 14, messageList.size());
    }

    @Test
    public void testCountMessagesByForumID() {
        log.debug("Start testCountMessagesByForumID()");
        assertEquals("Not the right number of messages for this forum", 14, forumService.countMessagesByForumID(fo.getKey()).intValue());
    }

    @Test
    public void testGetThread() {
        log.debug("Start testGetThread()");

        final Long msgidTopThread = message1.getKey();
        List threadMessageList = forumService.getThread(msgidTopThread);
        log.debug("threadMessageList.size()=" + threadMessageList.size());
        assertEquals("Not the right number of messages for this forum", 1, threadMessageList.size());
        // lookup for
        final Long notExistingTopThread = new Long(1234);
        threadMessageList = forumService.getThread(notExistingTopThread);
        log.debug("threadMessageList.size()=" + threadMessageList.size());
        assertEquals("Not the right number of messages for this forum", 0, threadMessageList.size());

    }

    @Test
    public void testGetNewMessageInfo() {
        log.debug("Start testGetNewMessageInfo()");

        final Date now = new Date();
        List msgList = forumService.getNewMessageInfo(fo.getKey(), new Date());
        assertEquals(0, msgList.size());
        final Date before = new Date(now.getTime() - 3600);
        msgList = forumService.getNewMessageInfo(fo.getKey(), before);
        assertEquals(14, msgList.size());
    }

    @Test
    public void testDeleteMessageTree() {
        log.debug("Start testDeleteMessageTree()");
        forumService.deleteMessageTree(fo.getKey(), m3); // throws Exception when failed
    }

    @Test
    public void testDeleteForum() {
        log.debug("Start testDeleteForum()");
        forumService.deleteForum(fo.getKey()); // throws Exception when failed
    }

}
