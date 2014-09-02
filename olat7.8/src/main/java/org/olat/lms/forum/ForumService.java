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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.presentation.forum.ForumChangedEvent;

/**
 * <P>
 * Initial Date: 05.05.2011 <br>
 * 
 * @author lavinia
 * @author oliver.buehler@agility-informatik.ch
 */
public interface ForumService {

    /**
     * @param msgid
     *            msg id of the topthread
     * @return List messages
     */
    public abstract List<Message> getThread(final Long msgid);

    public abstract List<Long> getAllForumKeys();

    public abstract List<Message> getMessagesByForum(final Forum forum);

    /**
     * @param forum
     * @return List messages
     */
    public abstract List<Message> getMessagesByForumID(final Long forum_id);

    /**
     * @param forumkey
     * @return the count of all messages by this forum
     */
    public abstract Integer countMessagesByForumID(final Long forumkey);

    /**
     * Implementation with one entry per message.
     * 
     * @param identity
     * @param forumkey
     * @return number of read messages
     */
    public abstract int countReadMessagesByUserAndForum(final Identity identity, final Long forumkey);

    /**
     * @param forumKey
     * @param latestRead
     * @return a List of Object[] with a key(Long), title(String), a creator(Identity), and the lastmodified(Date) of the messages of the forum with the given key and
     *         with last modification after the "latestRead" Date
     */
    public abstract List<Message> getNewMessageInfo(final Long forumKey, final Date latestRead);

    /**
     * @return the newly created and persisted forum
     */
    public abstract Forum addAForum();

    /**
     * @param forumKey
     * @return the forum with the given key
     */
    public abstract Forum loadForum(final Long forumKey);

    /**
     * @param forumKey
     */
    public abstract void deleteForum(final Long forumKey);

    /**
     * sets the parent and threadtop of the message automatically
     * 
     * @param creator
     * @param replyToMessage
     * @param newMessage
     *            the new message which has title and body set
     * @param publishEventTO
     *            - transfer object for creation of notification event
     */
    public abstract void replyToMessage(final Identity creator, final Message replyToMessage, final Message newMessage, PublishEventTO publishEventTO);

    /**
     * @param forum
     * @param topMessage
     * @param creator
     * @param publishEventTO
     *            - transfer object for creation of notification event
     */
    public abstract void addTopMessage(final Forum forum, final Message topMessage, final Identity creator, PublishEventTO publishEventTO);

    /**
     * @param messageKey
     * @return the message with the given messageKey
     */
    public abstract Message loadMessage(final Long messageKey);

    /**
     * creates (in RAM only) a new Message<br>
     * fill the values and use saveMessage to make it persistent
     * 
     * @return the message
     */
    public abstract Message createMessage();

    /**
     * Update message and fire MultiUserEvent, if any provided. If a not null ForumChangedEvent object is provided, then fire event to listeners.
     * 
     * @param updater
     * @param m
     * @param event
     * @param publishEventTO
     */
    public abstract void updateMessage(Identity updater, final Message m, boolean updateLastModifiedDate, final ForumChangedEvent event, PublishEventTO publishEventTO);

    /**
     * @param forumKey
     * @param m
     */
    public abstract void deleteMessageTree(final Long forumKey, final Message m);

    /**
     * @param m
     * @return true if the message has children
     */
    public abstract boolean hasChildren(final Message m);

    public abstract Message findMessage(final Long messageId);

    /**
     * Splits the current thread starting from the current message. It updates the messages of the selected subthread by setting the Parent and the Threadtop.
     * 
     * @param updater
     * 
     * @param msgid
     * @return the top message of the newly created thread.
     */
    public abstract Message splitThread(Identity updater, final Message msg);

    /**
     * Moves the current message from the current thread in another thread.
     * 
     * @param msg
     * @param topMsg
     * @return the moved message
     */
    public abstract Message moveMessage(Identity updater, final Message msg, final Message topMsg);

    /**
     * @param identity
     * @param forum
     * @return a set with the read messages keys for the input identity and forum.
     */
    public abstract Set<Long> getReadSet(final Identity identity, final Forum forum);

    /**
     * Implementation with one entry per forum message. Adds a new entry into the ReadMessage for the input message and identity.
     * 
     * @param msg
     * @param identity
     */
    public abstract void markAsRead(final Identity identity, final Message msg);

    /**
     * TODO: the clients of the ForumService should not get the container but an ForumContainerAPI.
     * 
     * @param forumKey
     * @return
     */
    public VFSContainer getForumContainer(final Long forumKey);

    public String getForumContainerSize(final Long forumKey);

    /**
     * @param forumKey
     * @param messageKey
     * @return the valid container for the attachments to place into
     */
    public VFSContainer getMessageContainer(final Long forumKey, final Long messageKey);

    public VFSContainer getForumArchiveContainer(final Identity identity, final Long forumKey);

    public VFSContainer getTempUploadFolder();

}
