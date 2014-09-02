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
package org.olat.data.forum;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.data.basesecurity.Identity;

/**
 * TODO: Class Description for ForumDao
 * 
 * <P>
 * Initial Date: 01.07.2011 <br>
 * 
 * @author lavinia
 */
public interface ForumDao {

    public abstract List<Message> getThread(final Long msgid);

    public abstract List<Long> getAllForumKeys();

    public abstract List<Message> getMessagesByForumID(final Long forum_id);

    public abstract Integer countMessagesByForumID(final Long forumkey);

    public abstract int countReadMessagesByUserAndForum(final Identity identity, final Long forumkey);

    public abstract List<Message> getNewMessageInfo(final Long forumKey, final Date latestRead);

    public abstract Forum loadForum(final Long forumKey);

    public abstract Forum saveForum(final Forum forum);

    public abstract Message loadMessage(final Message msg);

    public abstract void deleteForum(final Long forumKey);

    public abstract Message loadMessage(final Long messageKey);

    public abstract void saveMessage(final Message m);

    public abstract void updateMessage(final Message m);

    /**
     * 
     * @param forumKey
     * @param m
     */
    public abstract void deleteMessage(final Long forumKey, Message m);

    public abstract boolean hasChildren(final Message m);

    public abstract List getChildren(Message m);

    public abstract Message findMessage(final Long messageId);

    public abstract Set<Long> getReadSet(final Identity identity, final Forum forum);

    public abstract void markAsRead(final Identity identity, final Message msg);

}
