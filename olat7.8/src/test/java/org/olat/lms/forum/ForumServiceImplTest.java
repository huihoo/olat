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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.basesecurity.Identity;
import org.olat.data.forum.Forum;
import org.olat.data.forum.ForumDao;
import org.olat.data.forum.ForumDaoImpl;
import org.olat.data.forum.ForumImpl;
import org.olat.data.forum.Message;
import org.olat.data.forum.MessageImpl;
import org.olat.data.notification.Publisher.ContextType;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.presentation.forum.ForumNotificationTypeHandler;

/**
 * TODO: Class Description for ForumServiceImplTest
 * 
 * <P>
 * Initial Date: 11.05.2011 <br>
 * 
 * @author lavinia
 */
public class ForumServiceImplTest {

    /**
	 * 
	 */
    private static final Long THREAD_ID_WITH_ONE_MESSAGE = 1672l;
    private static final Long MESSAGE_ID = (long) 456;

    private ForumDao forumManagerMock;
    private ForumServiceImpl forumService;
    private List<Message> emptyList;
    private ArrayList<Message> threadWithOneMessageList;
    private Message oneTopMessage;
    private Message oneMessage;
    private Message moveTargetMessage;
    private Message parentOfMovable;
    private Message movableMessage;
    private Message movableMessageChild1;
    private Message movableMessageChild2;
    private Identity updaterIdentity;
    /* mock for not-publish event */
    private PublishEventTO publishEventTO;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Identity updaterIdentity = mock(Identity.class);

        forumManagerMock = mock(ForumDaoImpl.class);
        forumService = new ForumServiceImpl(forumManagerMock);
        NotificationService notificationService = mock(NotificationService.class);
        forumService.notificationService = notificationService;
        emptyList = new ArrayList<Message>();
        when(forumManagerMock.getThread(null)).thenReturn(emptyList);

        threadWithOneMessageList = new ArrayList<Message>();
        oneTopMessage = mock(Message.class);
        when(oneTopMessage.getKey()).thenReturn(THREAD_ID_WITH_ONE_MESSAGE);

        threadWithOneMessageList.add(oneTopMessage);
        when(forumManagerMock.getThread(THREAD_ID_WITH_ONE_MESSAGE)).thenReturn(threadWithOneMessageList);

        oneMessage = mock(Message.class);
        // oneMessage = new MessageImpl(); //cannot use since cannot set key
        when(oneMessage.getKey()).thenReturn(MESSAGE_ID);
        when(forumManagerMock.findMessage(MESSAGE_ID)).thenReturn(oneMessage);

        // Forum anyForum = mock(Forum.class);
        // Long FORUM_KEY = Long.valueOf(111);
        // when(anyForum.getKey()).thenReturn(FORUM_KEY);

        // setup for exchangeParent
        moveTargetMessage = mock(Message.class);
        when(moveTargetMessage.getKey()).thenReturn(THREAD_ID_WITH_ONE_MESSAGE);
        when(forumManagerMock.loadMessage(moveTargetMessage)).thenReturn(moveTargetMessage);

        parentOfMovable = mock(Message.class);
        when(parentOfMovable.getKey()).thenReturn(MESSAGE_ID);
        // parentOfMovable = new MessageImpl();
        when(forumManagerMock.loadMessage(parentOfMovable)).thenReturn(parentOfMovable);

        movableMessage = new MessageImpl();
        movableMessage.setParent(parentOfMovable);
        movableMessage.setThreadtop(parentOfMovable);
        Identity creator = mock(Identity.class);
        movableMessage.setCreator(creator);
        when(forumManagerMock.loadMessage(movableMessage)).thenReturn(movableMessage);

        movableMessageChild1 = new MessageImpl();
        movableMessageChild1.setParent(movableMessage);
        when(forumManagerMock.loadMessage(movableMessageChild1)).thenReturn(movableMessageChild1);

        movableMessageChild2 = new MessageImpl();
        movableMessageChild2.setParent(movableMessage);
        when(forumManagerMock.loadMessage(movableMessageChild2)).thenReturn(movableMessageChild2);

        /*
         * List <Message> messageList = new ArrayList<Message>(); messageList.add(parentOfMovable); messageList.add(movableMessage);
         * messageList.add(movableMessageChild1); messageList.add(movableMessageChild2); when(forumManagerMock.getThread(MESSAGE_ID)).thenReturn(messageList);
         */

    }

    @Test
    public void getThreadWithNull() {
        List<Message> thread = forumService.getThread(null);
        assertTrue(thread != null);
        assertTrue(thread.size() == 0);
    }

    @Test
    public void getThreadWithExisting() {
        List<Message> thread = forumService.getThread(THREAD_ID_WITH_ONE_MESSAGE);
        assertTrue(thread != null);
        assertTrue(thread.size() == 1);
        assertEquals(oneTopMessage, thread.get(0));
    }

    @Test
    public void getThreadWithNotExisting() {
        List<Message> thread = forumService.getThread(Long.valueOf(123));
        assertTrue(thread != null);
        assertTrue(thread.size() == 0);
    }

    @Test
    public void findMessageWithNull() {
        Message message = forumService.findMessage(null);
        assertTrue(message == null);
    }

    @Test
    public void findMessageExisting() {
        Message message = forumService.findMessage(MESSAGE_ID);
        assertTrue(message != null);
    }

    @Test
    public void addAForum() {
        Forum realForum = forumService.addAForum();
        assertTrue(realForum != null);
    }

    @Test
    public void addTopMessage() {
        Forum mockForum = mock(Forum.class);
        when(mockForum.getResourceableId()).thenReturn(new Long(1234));
        Identity initiatorIdentity = mock(Identity.class);
        Message firstMessage = new MessageMockImpl();

        publishEventTO = createPublishEventTO(initiatorIdentity);
        forumService.addTopMessage(mockForum, firstMessage, initiatorIdentity, publishEventTO);
        assertEquals(mockForum, firstMessage.getForum());
        assertEquals(initiatorIdentity, firstMessage.getCreator());
        assertEquals(null, firstMessage.getParent());
        assertEquals(null, firstMessage.getThreadtop());
    }

    @Test(expected = RuntimeException.class)
    public void addTopMessageWithNullMessage() {
        Forum realForum = new ForumImpl();
        Identity initiatorIdentity = mock(Identity.class);
        Message firstMessage = null;
        publishEventTO = createPublishEventTO(initiatorIdentity);
        forumService.addTopMessage(realForum, firstMessage, initiatorIdentity, publishEventTO);
    }

    private PublishEventTO createPublishEventTO(Identity creatorIdentity) {
        PublishEventTO publishEventTO = PublishEventTO.getValidInstance(ContextType.COURSE, 0L, "AAA_course", 0L, ForumNotificationTypeHandler.FORUM_SOURCE_TYPE, 0L,
                "a_Forum", "a_message", creatorIdentity, PublishEventTO.EventType.NEW);
        publishEventTO.setSourceEntryId("1");
        return publishEventTO;
    }

    @Test
    public void splitThread() {
        Message topMessage = new MessageImpl();
        Message message2 = new MessageImpl();
        message2.setParent(topMessage);

        Message message3 = new MessageImpl();
        message3.setParent(message2);

        Message message4 = new MessageImpl();
        message4.setParent(message3);

        Message newTopMessage = forumService.splitThread(updaterIdentity, message3);
        assertEquals(message3, newTopMessage);

    }

    @Test(expected = RuntimeException.class)
    public void splitThreadWithNull() {
        forumService.splitThread(updaterIdentity, null);
    }

    @Test
    public void cloneMessageAndAssignNewParent() {
        Message movedMessage = forumService.cloneMessageAndAssignNewParent(updaterIdentity, movableMessage, moveTargetMessage);
        assertTrue("Asserts that the movedMessage has a new parent!", movedMessage.getParent() == moveTargetMessage);
        // assertTrue(movableMessageChild1.getParent()==parentOfMovable);
    }

    @Test(expected = RuntimeException.class)
    public void cloneMessageAndAssignNewParentWithNullMessage() {
        forumService.cloneMessageAndAssignNewParent(updaterIdentity, null, mock(Message.class));
    }

    @Test
    public void cloneMessageAndAssignNewParentWithNullTargetMessage() {
        Message movedMessage = forumService.cloneMessageAndAssignNewParent(updaterIdentity, movableMessage, null);
        assertTrue(movedMessage.getParent() == null);
    }

}

class MessageMockImpl extends MessageImpl {
    public Long getKey() {
        return 5L;
    }
}
