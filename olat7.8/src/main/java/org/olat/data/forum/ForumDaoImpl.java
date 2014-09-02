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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.data.basesecurity.Identity;
import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.data.marking.MarkDAO;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.commons.resource.OLATResourceable;
import org.olat.system.commons.resource.OresHelper;
import org.olat.system.exception.AssertException;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Use ForumService from lms or presentation layers!
 * 
 * @author Felix Jost
 */
@Repository
public class ForumDaoImpl extends BasicManager implements ForumDao {

    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    private DB db;
    @Autowired
    private MarkDAO markingDao;

    /**
     * [spring]
     */
    private ForumDaoImpl() {
        //
    }

    @Override
    public List<Message> getThread(final Long msgid) {

        // we make a scalar query so that not only the messages, but also the users
        // are loaded into the cache as well.
        // TODO : Otherwise, hibernate will fetch the user for each message (100
        // messages = 101 SQL Queries!)
        // FIXME: use join fetch instead
        long rstart = 0;
        if (log.isDebugEnabled()) {
            rstart = System.currentTimeMillis();
        }
        final List scalar = db.find("select msg, cr, usercr " + "from org.olat.data.forum.MessageImpl as msg" + ", org.olat.data.basesecurity.Identity as cr "
                + ", org.olat.data.user.UserImpl as usercr" + " where msg.creator = cr and cr.user = usercr "
                + " and (msg.key = ? or msg.threadtop.key = ?) order by msg.creationDate", new Object[] { msgid, msgid }, new Type[] { Hibernate.LONG, Hibernate.LONG });
        final int size = scalar.size();
        final List<Message> messages = new ArrayList<Message>(size);
        for (int i = 0; i < size; i++) {
            final Object[] o = (Object[]) scalar.get(i);
            final Message m = (Message) o[0];
            messages.add(m);
        }
        if (log.isDebugEnabled()) {
            final long rstop = System.currentTimeMillis();
            log.debug("time to fetch thread with topmsg_id " + msgid + " :" + (rstop - rstart), null);
        }
        return messages;
    }

    @Override
    public List<Long> getAllForumKeys() {
        final List<Long> tmpRes = db.find("select key from org.olat.data.forum.ForumImpl");
        return tmpRes;
    }

    public List<Message> getMessagesByForum(final Forum forum) {
        return getMessagesByForumID(forum.getKey());
    }

    @Override
    public List<Message> getMessagesByForumID(final Long forum_id) {
        long rstart = 0;
        if (log.isDebugEnabled()) {
            rstart = System.currentTimeMillis();
        }
        final List scalar = db.find("select msg, cr, usercr " + "from org.olat.data.forum.MessageImpl as msg" + ", org.olat.data.basesecurity.Identity as cr "
                + ", org.olat.data.user.UserImpl as usercr" + " where msg.creator = cr and cr.user = usercr and msg.forum.key = ?", forum_id, Hibernate.LONG);
        final int size = scalar.size();
        final List<Message> messages = new ArrayList<Message>(size);
        for (int i = 0; i < size; i++) {
            final Object[] o = (Object[]) scalar.get(i);
            final Message m = (Message) o[0];
            messages.add(m);
        }
        if (log.isDebugEnabled()) {
            final long rstop = System.currentTimeMillis();
            log.debug("time to fetch forum with forum_id " + forum_id + " :" + (rstop - rstart), null);
        }
        return messages;
    }

    @Override
    public Integer countMessagesByForumID(final Long forumkey) {
        final List msgCount = db.find("select count(msg.title) from org.olat.data.forum.MessageImpl as msg where msg.forum.key = ?", forumkey, Hibernate.LONG);
        return new Integer(((Long) msgCount.get(0)).intValue());
    }

    @Override
    public int countReadMessagesByUserAndForum(final Identity identity, final Long forumkey) {
        final List<ReadMessage> itemList = db.find("select msg from msg in class org.olat.data.forum.ReadMessageImpl where msg.identity = ? and msg.forum = ?",
                new Object[] { identity.getKey(), forumkey }, new Type[] { Hibernate.LONG, Hibernate.LONG });
        return itemList.size();
    }

    @Override
    public List<Message> getNewMessageInfo(final Long forumKey, final Date latestRead) {
        // FIXME:fj: lastModified has no index -> test performance with forum with
        // 200 messages
        final String query = "select msg from org.olat.data.forum.MessageImpl as msg"
                + " where msg.forum.key = :forumKey and msg.lastModified > :latestRead order by msg.lastModified desc";
        final DBQuery dbquery = db.createQuery(query);
        dbquery.setLong("forumKey", forumKey.longValue());
        dbquery.setTimestamp("latestRead", latestRead);
        dbquery.setCacheable(true);
        return dbquery.list();
    }

    @Override
    public Forum loadForum(final Long forumKey) {
        final ForumImpl fo = (ForumImpl) db.loadObject(ForumImpl.class, forumKey);
        return fo;
    }

    @Override
    public Forum saveForum(final Forum forum) {
        db.saveObject(forum);
        return forum;
    }

    @Override
    public Message loadMessage(final Message msg) {
        return (Message) db.loadObject(msg);
    }

    @Override
    public void deleteForum(final Long forumKey) {
        final Forum foToDel = loadForum(forumKey);
        if (foToDel == null) {
            throw new AssertException("forum to delete was not found: key=" + forumKey);
        }
        // delete properties, messages and the forum itself
        doDeleteForum(foToDel);
    }

    /**
     * deletes all messages belonging to this forum and the forum entry itself
     * 
     * @param forum
     */
    private void doDeleteForum(final Forum forum) {
        final Long forumKey = forum.getKey();

        // delete read messsages
        db.delete("from readMsg in class org.olat.data.forum.ReadMessageImpl where readMsg.forum = ? ", forumKey, Hibernate.LONG);
        // delete messages
        db.delete("from message in class org.olat.data.forum.MessageImpl where message.forum = ?", forumKey, Hibernate.LONG);
        // delete forum
        db.delete("from forum in class org.olat.data.forum.ForumImpl where forum.key = ?", forumKey, Hibernate.LONG);
        // delete properties

        // delete all flags
        final OLATResourceable ores = OresHelper.createOLATResourceableInstance(Forum.class, forum.getKey());
        markingDao.deleteMark(ores);
    }

    @Override
    public Message loadMessage(final Long messageKey) {
        final Message msg = doloadMessage(messageKey);
        return msg;
    }

    private Message doloadMessage(final Long messageKey) {
        final Message msg = (Message) db.loadObject(MessageImpl.class, messageKey);
        return msg;
    }

    @Override
    public void saveMessage(final Message m) {
        m.setLastModified(new Date());
        db.saveObject(m);
    }

    @Override
    public void updateMessage(final Message m) {
        m.setLastModified(new Date());
        db.updateObject(m);
    }

    /**
     * 
     * @param forumKey
     * @param m
     */
    @Override
    public void deleteMessage(final Long forumKey, Message m) {
        // make sure the message is reloaded if it is not in the hibernate session cache
        m = (Message) db.loadObject(m);
        // delete all properties of one single message
        deleteMessageProperties(forumKey, m);
        db.deleteObject(m);
    }

    @Override
    public boolean hasChildren(final Message m) {
        boolean children = false;

        final Long message_id = m.getKey();
        final String q = " select count(msg) from org.olat.data.forum.MessageImpl msg where msg.parent = :input ";

        final DBQuery query = db.createQuery(q);
        query.setLong("input", message_id.longValue());
        final List result = query.list();
        final int count = ((Long) result.get(0)).intValue();

        if (count > 0) {
            children = true;
        }

        return children;
    }

    @Override
    public List getChildren(Message m) {

        final Long message_id = m.getKey();
        final List messages = db.find("select msg from msg in class org.olat.data.forum.MessageImpl where msg.parent = ?", message_id, Hibernate.LONG);
        return messages;
    }

    /**
     * deletes entry of one message
     */
    private void deleteMessageProperties(final Long forumKey, final Message m) {

        final Long messageKey = m.getKey();

        final StringBuilder query = new StringBuilder();
        query.append("from readMsg in class org.olat.data.forum.ReadMessageImpl ");
        query.append("where readMsg.forum = ? ");
        query.append("and readMsg.message = ? ");

        db.delete(query.toString(), new Object[] { forumKey, messageKey }, new Type[] { Hibernate.LONG, Hibernate.LONG });
    }

    @Override
    public Message findMessage(final Long messageId) {
        return (Message) db.findObject(MessageImpl.class, messageId);
    }

    @Override
    public Set<Long> getReadSet(final Identity identity, final Forum forum) {
        final List<ReadMessage> itemList = db.find("select msg from msg in class org.olat.data.forum.ReadMessageImpl where msg.identity = ? and msg.forum = ?",
                new Object[] { identity.getKey(), forum.getKey() }, new Type[] { Hibernate.LONG, Hibernate.LONG });

        final Set<Long> readSet = new HashSet<Long>();
        final Iterator<ReadMessage> listIterator = itemList.iterator();
        while (listIterator.hasNext()) {
            final Long msgKey = listIterator.next().getMessage().getKey();
            readSet.add(msgKey);
        }
        return readSet;
    }

    @Override
    public void markAsRead(final Identity identity, final Message msg) {
        // Check if the message was not already deleted
        final Message retrievedMessage = findMessage(msg.getKey());
        if (retrievedMessage != null) {
            final ReadMessageImpl readMessage = new ReadMessageImpl();
            readMessage.setIdentity(identity);
            readMessage.setMessage(msg);
            readMessage.setForum(msg.getForum());
            db.saveObject(readMessage);
        }
    }

}
