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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.vfs.FolderConfig;
import org.olat.data.commons.vfs.VFSContainer;
import org.olat.data.commons.vfs.VFSItem;
import org.olat.data.commons.vfs.VFSLeaf;
import org.olat.data.commons.vfs.olatimpl.OlatRootFolderImpl;
import org.olat.data.forum.Forum;
import org.olat.data.forum.ForumDao;
import org.olat.data.forum.ForumImpl;
import org.olat.data.forum.Message;
import org.olat.data.forum.MessageImpl;
import org.olat.data.marking.MarkDAO;
import org.olat.lms.commons.textservice.TextService;
import org.olat.lms.core.notification.service.NotificationService;
import org.olat.lms.core.notification.service.PublishEventTO;
import org.olat.presentation.forum.ForumChangedEvent;
import org.olat.system.commons.CodeHelper;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.coordinate.CoordinatorManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation class for ForumService
 * 
 * <P>
 * Initial Date: 05.05.2011 <br>
 * 
 * @author lavinia
 */
@Service("forumService")
public class ForumServiceImpl implements ForumService {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private ForumDao forumDao;
    @Autowired
    private TextService txtService;
    @Autowired
    private MarkDAO markingDao;

    @Autowired
    protected NotificationService notificationService;

    /**
     * [spring]
     */
    ForumServiceImpl() {
        //
    }

    /**
     * TODO: just for testing purposes. Check if really needed.
     * 
     * @param forumManagerMock
     */
    ForumServiceImpl(ForumDao forumManagerMock) {
        forumDao = forumManagerMock;
    }

    /**
     * @see org.olat.lms.forum.ForumService#getThread(java.lang.Long)
     */
    @Override
    public List<Message> getThread(Long msgid) {
        return forumDao.getThread(msgid);
    }

    /**
     * @see org.olat.lms.forum.ForumService#getAllForumKeys()
     */
    @Override
    public List<Long> getAllForumKeys() {
        return forumDao.getAllForumKeys();
    }

    /**
     * @see org.olat.lms.forum.ForumService#getMessagesByForum(org.olat.data.forum.Forum)
     */
    @Override
    public List<Message> getMessagesByForum(final Forum forum) {
        return getMessagesByForumID(forum.getKey());
    }

    /**
     * @see org.olat.lms.forum.ForumService#getMessagesByForumID(java.lang.Long)
     */
    @Override
    public List<Message> getMessagesByForumID(Long forum_id) {
        return forumDao.getMessagesByForumID(forum_id);
    }

    /**
     * @see org.olat.lms.forum.ForumService#countMessagesByForumID(java.lang.Long)
     */
    @Override
    public Integer countMessagesByForumID(Long forumkey) {
        return forumDao.countMessagesByForumID(forumkey);
    }

    /**
     * @see org.olat.lms.forum.ForumService#countReadMessagesByUserAndForum(org.olat.data.basesecurity.Identity, java.lang.Long)
     */
    @Override
    public int countReadMessagesByUserAndForum(Identity identity, Long forumkey) {
        return forumDao.countReadMessagesByUserAndForum(identity, forumkey);
    }

    /**
     * @see org.olat.lms.forum.ForumService#getNewMessageInfo(java.lang.Long, java.util.Date)
     */
    @Override
    public List<Message> getNewMessageInfo(Long forumKey, Date latestRead) {
        return forumDao.getNewMessageInfo(forumKey, latestRead);
    }

    /**
     * @see org.olat.lms.forum.ForumService#addAForum()
     */
    @Override
    public Forum addAForum() {
        final Forum fo = createForum();
        forumDao.saveForum(fo);
        return fo;
    }

    /**
     * create (in RAM only) a new Forum
     */
    private ForumImpl createForum() {
        return new ForumImpl();
    }

    /**
     * @see org.olat.lms.forum.ForumService#loadForum(java.lang.Long)
     */
    @Override
    public Forum loadForum(Long forumKey) {
        return forumDao.loadForum(forumKey);
    }

    /**
     * @see org.olat.lms.forum.ForumService#deleteForum(java.lang.Long)
     */
    @Override
    public void deleteForum(Long forumKey) {
        forumDao.deleteForum(forumKey);

        // delete all flags
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forumKey);
        markingDao.deleteMark(ores);

        final VFSContainer fContainer = getForumContainer(forumKey);
        fContainer.delete();
    }

    /**
     * @see org.olat.lms.forum.ForumService#replyToMessage(org.olat.data.basesecurity.Identity, org.olat.data.forum.Message, org.olat.data.forum.Message, PublishEventTO)
     */
    @Override
    public void replyToMessage(Identity creator, Message replyToMessage, Message newMessage, PublishEventTO publishEventTO) {
        newMessage.setForum(replyToMessage.getForum());
        final Message top = replyToMessage.getThreadtop();
        newMessage.setThreadtop((top != null ? top : replyToMessage));
        newMessage.setParent(replyToMessage);
        newMessage.setCreator(creator);
        forumDao.saveMessage(newMessage);
        publishEventTO.setSourceEntryId(newMessage.getKey().toString());
        publishEvent(publishEventTO);
    }

    private void publishEvent(PublishEventTO publishEventTO) {
        try {
            notificationService.publishEvent(publishEventTO);
        } catch (RuntimeException e) {
            // if publishEvent retry fails catch a transient exception
            log.error("publishEvent failed: ", e);
        }
    }

    /**
     * @see org.olat.lms.forum.ForumService#addTopMessage(org.olat.data.forum.Forum, org.olat.data.forum.Message, org.olat.data.basesecurity.Identity, PublishEventTO)
     */
    @Override
    public void addTopMessage(Forum forum, Message topMessage, Identity creator, PublishEventTO publishEventTO) {
        topMessage.setForum(forum);
        topMessage.setParent(null);
        topMessage.setThreadtop(null);
        topMessage.setCreator(creator);
        forumDao.saveMessage(topMessage);
        publishEventTO.setSourceEntryId(topMessage.getKey().toString());
        publishEvent(publishEventTO);
    }

    /**
     * @see org.olat.lms.forum.ForumService#loadMessage(java.lang.Long)
     */
    @Override
    public Message loadMessage(Long messageKey) {
        return forumDao.loadMessage(messageKey);
    }

    /**
     * @see org.olat.lms.forum.ForumService#createMessage()
     */
    @Override
    public Message createMessage() {
        return new MessageImpl();
    }

    public void saveMessage(final Message m) {
        // TODO: think about where maxlenrestriction comes: manager or controller
        updateCounters(m);
        forumDao.saveMessage(m);
    }

    /**
     * @see org.olat.lms.forum.ForumService#updateMessage(org.olat.data.forum.Message, org.olat.presentation.forum.ForumChangedEvent)
     */
    @Override
    public void updateMessage(Identity updater, Message updatedMessage, boolean updateLastModifiedDate, ForumChangedEvent event, PublishEventTO publishEventTO) {
        updateCounters(updatedMessage);
        // OLAT-6295 Only update last modified for the operations edit(update), show, and open.
        // Don't update the last modified date for the operations close, hide, move and split.
        if (updateLastModifiedDate) {
            updatedMessage.setLastModified(new Date());
        }
        forumDao.updateMessage(updatedMessage);
        publishEvent(publishEventTO);
        // notificationService.publishEvent(new PublishEventTO(Publisher.Type.FORUM, updatedMessage.getForum().getResourceableId(), updater, updatedMessage.getTitle(),
        // updatedMessage.getBody()));
        if (event != null) {
            CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new ForumChangedEvent("hide"), updatedMessage.getForum());
        }
    }

    /**
     * Update the counters for words and characters
     * 
     * @param m
     *            the message
     */
    public void updateCounters(final Message m) {
        final String body = m.getBody();
        final String unQuotedBody = new QuoteAndTagFilter().filter(body);
        final Locale suggestedLocale = txtService.detectLocale(unQuotedBody);
        m.setNumOfWords(txtService.wordCount(unQuotedBody, suggestedLocale));
        m.setNumOfCharacters(txtService.characterCount(unQuotedBody, suggestedLocale));
    }

    /**
     * @see org.olat.lms.forum.ForumService#deleteMessageTree(java.lang.Long, org.olat.data.forum.Message)
     */
    @Override
    public void deleteMessageTree(Long forumKey, Message m) {
        deleteMessageRecursion(forumKey, m);
    }

    private void deleteMessageRecursion(final Long forumKey, Message m) {
        final List messages = forumDao.getChildren(m);
        for (final Iterator iter = messages.iterator(); iter.hasNext();) {
            final Message element = (Message) iter.next();
            deleteMessageRecursion(forumKey, element);
        }

        /*
         * if (! db.contains(m)){ log.debug("Message " + m.getKey() + " not in hibernate session, reloading before delete"); m = loadMessage(m.getKey()); }
         */
        forumDao.deleteMessage(forumKey, m);

        // delete all flags
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forumKey);
        markingDao.deleteMark(ores, m.getKey().toString());

        // delete file system resource
        final VFSContainer mContainer = getMessageContainer(forumKey, m.getKey());
        mContainer.delete();

        if (log.isDebugEnabled()) {
            log.debug("Deleting message " + m.getKey().toString());
        }
    }

    /**
     * @see org.olat.lms.forum.ForumService#hasChildren(org.olat.data.forum.Message)
     */
    @Override
    public boolean hasChildren(Message m) {
        return forumDao.hasChildren(m);
    }

    /**
     * @see org.olat.lms.forum.ForumService#findMessage(java.lang.Long)
     */
    @Override
    public Message findMessage(Long messageId) {
        return forumDao.findMessage(messageId);
    }

    /**
     * @see org.olat.lms.forum.ForumService#splitThread(org.olat.data.forum.Message)
     */
    @Override
    public Message splitThread(Identity updater, final Message msg) {
        Message newTopMessage = null;
        if (msg.getThreadtop() == null) {
            newTopMessage = msg;
        } else {
            // it only make sense to split a thread if the current message is not a threadtop message.
            final List<Message> threadList = this.getThread(msg.getThreadtop().getKey());
            final List<Message> subthreadList = new ArrayList<Message>();
            subthreadList.add(msg);
            getSubthread(msg, threadList, subthreadList);

            final Iterator<Message> messageIterator = subthreadList.iterator();
            Message firstMessage = null;

            if (messageIterator.hasNext()) {
                firstMessage = messageIterator.next();
                firstMessage = forumDao.loadMessage(firstMessage);
                firstMessage.setParent(null);
                firstMessage.setThreadtop(null);
                this.updateMessage(updater, firstMessage, false, new ForumChangedEvent("split"), PublishEventTO.getNoPublishInstance());
                newTopMessage = firstMessage;
            }
            while (firstMessage != null && messageIterator.hasNext()) {
                Message message = messageIterator.next();
                message = forumDao.loadMessage(message);
                message.setThreadtop(firstMessage);
                this.updateMessage(updater, message, false, null, PublishEventTO.getNoPublishInstance());
            }
        }
        return newTopMessage;
    }

    /**
     * This is a recursive method. The subthreadList in an ordered list with all descendents of the input msg.
     * 
     * @param msg
     * @param threadList
     * @param subthreadList
     */
    private void getSubthread(final Message msg, final List<Message> threadList, final List<Message> subthreadList) {
        final Iterator<Message> listIterator = threadList.iterator();
        while (listIterator.hasNext()) {
            final Message currMessage = listIterator.next();
            if (currMessage.getParent() != null && currMessage.getParent().getKey().equals(msg.getKey())) {
                subthreadList.add(currMessage);
                getSubthread(currMessage, threadList, subthreadList);
            }
        }
    }

    /**
     * Moves the current message from the current thread in another thread.
     */
    @Override
    public Message moveMessage(Identity updater, final Message msg, final Message topMsg) {
        if (msg.getKey().equals(topMsg.getKey())) {
            log.error("Moved forum message has same id as parent (id=" + msg.getKey() + ".");
        }

        Message message = cloneMessageAndAssignNewParent(updater, msg, topMsg);

        moveMarks(msg, message);

        final Message oldMessage = forumDao.loadMessage(msg);
        moveMessageContainer(oldMessage.getForum().getKey(), oldMessage.getKey(), message.getForum().getKey(), message.getKey());
        deleteMessageTree(oldMessage.getForum().getKey(), oldMessage);
        return message;
    }

    /**
     * Clone the message and assign a new parent and threadtop to the clone, set new parents for the child messages. The child messages won't be moved.
     * 
     * @param msg
     * @param topMsg
     * @return
     */
    protected Message cloneMessageAndAssignNewParent(Identity updater, final Message msg, final Message topMsg) {
        final List<Message> oldThreadList = getThread(msg.getThreadtop().getKey());
        final List<Message> subThreadList = new ArrayList<Message>();
        this.getSubthread(msg, oldThreadList, subThreadList);
        // one has to set a new parent for all children of the moved message
        // first message of sublist has to get the parent from the moved message
        for (Message childMessage : subThreadList) {
            childMessage = forumDao.loadMessage(childMessage);
            childMessage.setParent(msg.getParent());
            updateMessage(updater, childMessage, false, null, PublishEventTO.getNoPublishInstance());
        }
        // now move the message to the choosen thread
        final Message oldMessage = forumDao.loadMessage(msg);
        final Message message = createMessage();
        message.setCreator(oldMessage.getCreator());
        message.setForum(oldMessage.getForum());
        message.setModifier(oldMessage.getModifier());
        message.setTitle(oldMessage.getTitle());
        message.setBody(oldMessage.getBody());
        message.setThreadtop(topMsg);
        message.setParent(topMsg);
        final Status status = Status.getStatus(oldMessage.getStatusCode());
        status.setMoved(true);
        message.setStatusCode(Status.getStatusCode(status));
        forumDao.saveMessage(message);
        return message;
    }

    private void moveMarks(Message oldMessage, Message newMessage) {
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, oldMessage.getForum().getKey());
        markingDao.moveMarks(ores, oldMessage.getKey().toString(), newMessage.getKey().toString());

    }

    private void moveMessageContainer(final Long fromForumKey, final Long fromMessageKey, final Long toForumKey, final Long toMessageKey) {
        // copy message container
        final VFSContainer toMessageContainer = getMessageContainer(toForumKey, toMessageKey);
        final VFSContainer fromMessageContainer = getMessageContainer(fromForumKey, fromMessageKey);
        for (final VFSItem vfsItem : fromMessageContainer.getItems()) {
            toMessageContainer.copyFrom(vfsItem);
        }
    }

    /**
     * @see org.olat.lms.forum.ForumService#getReadSet(org.olat.data.basesecurity.Identity, org.olat.data.forum.Forum)
     */
    @Override
    public Set<Long> getReadSet(Identity identity, Forum forum) {
        return forumDao.getReadSet(identity, forum);
    }

    /**
     * @see org.olat.lms.forum.ForumService#markAsRead(org.olat.data.basesecurity.Identity, org.olat.data.forum.Message)
     */
    @Override
    public void markAsRead(Identity identity, Message msg) {
        forumDao.markAsRead(identity, msg);

    }

    /**
     * @param forumKey
     * @return
     */
    @Override
    public VFSContainer getForumContainer(final Long forumKey) {
        // TODO: using hardcoded java.io.File separator
        final StringBuilder sb = new StringBuilder(FolderConfig.getForumHome());
        sb.append("/").append(forumKey.toString());
        final OlatRootFolderImpl forumContainer = new OlatRootFolderImpl(sb.toString(), null);
        final File baseFile = forumContainer.getBasefile();
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        return forumContainer;
    }

    @Override
    public String getForumContainerSize(final Long forumKey) {
        final VFSContainer forumContainer = getForumContainer(forumKey);
        final VFSLeaf vl = (VFSLeaf) forumContainer.getItems().get(0);
        return StringHelper.formatMemory(vl.getSize());
    }

    /**
     * @see org.olat.lms.forum.ForumService#getMessageContainer(java.lang.Long, java.lang.Long)
     */
    @Override
    public VFSContainer getMessageContainer(Long forumKey, Long messageKey) {
        // TODO: using hardcoded java.io.File separator
        final StringBuilder sb = new StringBuilder(FolderConfig.getForumHome());
        sb.append("/").append(forumKey.toString());
        sb.append("/").append(messageKey.toString());
        final OlatRootFolderImpl messageContainer = new OlatRootFolderImpl(sb.toString(), null);
        final File baseFile = messageContainer.getBasefile();
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        return messageContainer;
    }

    @Override
    public VFSContainer getForumArchiveContainer(final Identity identity, final Long forumKey) {
        VFSContainer container = new OlatRootFolderImpl(FolderConfig.getUserHomes() + File.separator + identity.getName() + "/private/archive", null);
        // append export timestamp to avoid overwriting previous export
        final Date tmp = new Date(System.currentTimeMillis());
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
        final String folder = "forum_" + forumKey.toString() + "_" + formatter.format(tmp);
        VFSItem vfsItem = container.resolve(folder);
        if (vfsItem == null || !(vfsItem instanceof VFSContainer)) {
            vfsItem = container.createChildContainer(folder);
        }
        container = (VFSContainer) vfsItem;
        return container;
    }

    @Override
    public VFSContainer getTempUploadFolder() {
        return new OlatRootFolderImpl(File.separator + "tmp/" + CodeHelper.getGlobalForeverUniqueID() + "/", null);
    }

}
