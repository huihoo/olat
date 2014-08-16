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

package org.olat.connectors.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.olat.connectors.rest.forum.MessageVO;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DBFactory;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.lms.forum.ForumService;
import org.olat.presentation.forum.ForumNotificationTypeHandler;
import org.olat.system.spring.CoreSpringFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;

@Ignore("ignored to be in sync with pom.xml")
public class ForumITCase extends OlatJerseyTestCase {

    private static Forum forum;
    private static Message m1, m2, m3, m4, m5;
    private static Identity id1;
    /* mock for not-publish event */
    private PublishEventTO publishEventTO;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");

        final ForumService fm = getForumService();
        forum = getForumService().addAForum();

        m1 = fm.createMessage();
        m1.setTitle("Thread-1");
        m1.setBody("Body of Thread-1");
        publishEventTO = createPublishEventTO(id1, PublishEventTO.EventType.NEW, m1);
        fm.addTopMessage(forum, m1, id1, publishEventTO);

        m2 = fm.createMessage();
        m2.setTitle("Thread-2");
        m2.setBody("Body of Thread-2");
        publishEventTO = createPublishEventTO(id1, PublishEventTO.EventType.NEW, m2);
        fm.addTopMessage(forum, m2, id1, publishEventTO);

        DBFactory.getInstance().intermediateCommit();

        m3 = fm.createMessage();
        m3.setTitle("Message-1.1");
        m3.setBody("Body of Message-1.1");
        fm.replyToMessage(id1, m1, m3, publishEventTO);

        m4 = fm.createMessage();
        m4.setTitle("Message-1.1.1");
        m4.setBody("Body of Message-1.1.1");
        fm.replyToMessage(id1, m3, m4, publishEventTO);

        m5 = fm.createMessage();
        m5.setTitle("Message-1.2");
        m5.setBody("Body of Message-1.2");
        fm.replyToMessage(id1, m1, m5, publishEventTO);

        DBFactory.getInstance().intermediateCommit();
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    private PublishEventTO createPublishEventTO(Identity creatorIdentity, PublishEventTO.EventType type, Message message) {
        return PublishEventTO.getValidInstance(ContextType.COURSE, 0L, "AAA_course", 0L, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, 0L, "a_Forum",
                message.getTitle(), creatorIdentity, type);

    }

    @Test
    public void testNewThread() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = getForumUriBuilder().path("threads").queryParam("authorKey", id1.getKey()).queryParam("title", "New thread")
                .queryParam("body", "A very interesting thread").build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        final MessageVO thread = parse(body, MessageVO.class);
        assertNotNull(thread);
        assertNotNull(thread.getKey());
        assertEquals(thread.getForumKey(), forum.getKey());
        assertEquals(thread.getAuthorKey(), id1.getKey());

        // really saved?
        boolean saved = false;
        final ForumService fm = getForumService();
        final List<Message> allMessages = fm.getMessagesByForum(forum);
        for (final Message message : allMessages) {
            if (message.getKey().equals(thread.getKey())) {
                saved = true;
            }
        }
        assertTrue(saved);
    }

    @Test
    public void testNewMessage() throws IOException {
        final HttpClient c = loginWithCookie("administrator", "olat");

        final URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).queryParam("authorKey", id1.getKey()).queryParam("title", "New message")
                .queryParam("body", "A very interesting response in Thread-1").build();
        final PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
        final int code = c.executeMethod(method);
        assertEquals(code, 200);
        final String body = method.getResponseBodyAsString();
        final MessageVO message = parse(body, MessageVO.class);
        assertNotNull(message);
        assertNotNull(message.getKey());
        assertEquals(message.getForumKey(), forum.getKey());
        assertEquals(message.getAuthorKey(), id1.getKey());
        assertEquals(message.getParentKey(), m1.getKey());

        // really saved?
        boolean saved = false;
        final ForumService fm = getForumService();
        final List<Message> allMessages = fm.getMessagesByForum(forum);
        for (final Message msg : allMessages) {
            if (msg.getKey().equals(message.getKey())) {
                saved = true;
            }
        }
        assertTrue(saved);
    }

    private UriBuilder getForumUriBuilder() {
        return UriBuilder.fromUri(getContextURI()).path("repo").path("forums").path(forum.getKey().toString());
    }

    protected List<MessageVO> parseMessageArray(final String body) {
        try {
            final ObjectMapper mapper = new ObjectMapper(jsonFactory);
            return mapper.readValue(body, new TypeReference<List<MessageVO>>() {/* */
            });
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
