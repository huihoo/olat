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

package org.olat.lms.forum.archiver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.commons.tree.TreeVisitor;
import org.olat.lms.forum.ForumCallback;
import org.olat.lms.forum.ForumHelper;
import org.olat.lms.forum.ForumService;
import org.olat.lms.forum.MessageNode;
import org.olat.system.commons.manager.BasicManager;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Initial Date: Nov 11, 2005 <br>
 * 
 * @author Alexander Schneider
 */

public class ForumArchiveManager extends BasicManager {
    private static ForumArchiveManager instance = new ForumArchiveManager();
    private static final Logger log = LoggerHelper.getLogger();

    // TODO spring config
    private ForumArchiveManager() {
        // private since singleton
    }

    /**
     * @return the singleton
     */
    public static ForumArchiveManager getInstance() {
        return instance;
    }

    /**
     * If the forumCallback is null no restriction applies to the forum archiver. (that is it can archive all threads no matter the status)
     * 
     * @param forumFormatter
     * @param forumId
     * @param forumCallback
     * @return
     */
    public String applyFormatter(final ForumFormatter forumFormatter, final long forumId, final ForumCallback forumCallback) {
        log.info("Archiving complete forum: " + forumId);
        final Map metaInfo = new HashMap();
        metaInfo.put(ForumFormatter.MANDATORY_METAINFO_KEY, new Long(forumId));
        // convert forum structure to trees
        final List threadTreesList = convertToThreadTrees(forumId, forumCallback);
        // format forum trees by using the formatter given by the callee
        return formatForum(threadTreesList, forumFormatter, metaInfo);
    }

    /**
     * It is assumed that if the caller of this method is allowed to see the forum thread starting from topMessageId, then he also has the right to archive it, so no need
     * for a ForumCallback.
     * 
     * @param forumFormatter
     * @param forumId
     * @param topMessageId
     * @return the message thread as String formatted
     */
    public String applyFormatterForOneThread(final ForumFormatter forumFormatter, final long forumId, final long topMessageId) {
        log.info("Archiving forum.thread: " + forumId + "." + topMessageId);
        final Map metaInfo = new HashMap();
        metaInfo.put(ForumFormatter.MANDATORY_METAINFO_KEY, new Long(forumId));
        final MessageNode topMessageNode = convertToThreadTree(topMessageId);
        return formatThread(topMessageNode, forumFormatter, metaInfo);
    }

    /**
     * If the forumCallback is null no filtering is executed, else if a thread is hidden and the user doesn't have moderator rights the hidden thread is not included into
     * the archive.
     * 
     * @param forumId
     * @param metaInfo
     * @return all top message nodes together with their children in a list
     */
    private List convertToThreadTrees(final long forumId, final ForumCallback forumCallback) {
        List messages;
        final List topNodeList = new ArrayList();
        final ForumService fm = getForumService();
        final Long l = new Long(forumId);
        final Forum f = fm.loadForum(l);
        messages = fm.getMessagesByForum(f);

        for (final Iterator iterTop = messages.iterator(); iterTop.hasNext();) {
            final Message msg = (Message) iterTop.next();
            if (msg.getParent() == null) {
                iterTop.remove();
                final MessageNode topNode = new MessageNode(msg);
                if (topNode.isHidden() && (forumCallback == null || (forumCallback != null && forumCallback.mayEditMessageAsModerator()))) {
                    addChildren(messages, topNode);
                    topNodeList.add(topNode);
                } else if (!topNode.isHidden()) {
                    addChildren(messages, topNode);
                    topNodeList.add(topNode);
                }
            }
        }
        return getMessagesSorted(topNodeList);
    }

    private ForumService getForumService() {
        return (ForumService) CoreSpringFactory.getBean("forumService");

    }

    /**
     * Sorts the input list by adding the sticky messages first.
     * 
     * @param topNodeList
     * @return the sorted list.
     */
    private List getMessagesSorted(final List<Message> topNodeList) {
        final Comparator messageNodeComparator = ForumHelper.getMessageNodeComparator();
        Collections.sort(topNodeList, messageNodeComparator);
        return topNodeList;
    }

    /**
     * @param messageId
     * @param metaInfo
     * @return the top message node with all its children
     */
    private MessageNode convertToThreadTree(final long topMessageId) {
        List messages;
        MessageNode topNode = null;
        final ForumService fm = getForumService();
        final Long l = new Long(topMessageId);
        messages = fm.getThread(l);
        for (final Iterator iterTop = messages.iterator(); iterTop.hasNext();) {
            final Message msg = (Message) iterTop.next();
            if (msg.getParent() == null) {
                iterTop.remove();
                topNode = new MessageNode(msg);
                addChildren(messages, topNode);
            }
        }
        return topNode;
    }

    private void addChildren(final List messages, final MessageNode mn) {
        for (final Iterator iterMsg = messages.iterator(); iterMsg.hasNext();) {
            final Message msg = (Message) iterMsg.next();
            if ((msg.getParent() != null) && (msg.getParent().getKey() == mn.getKey())) {
                final MessageNode childNode = new MessageNode(msg);
                mn.addChild(childNode);
                // FIXME:as:c next line is not necessary
                childNode.setParent(mn);
                addChildren(messages, childNode);
            }
        }
    }

    /**
     * @param topNodeList
     * @param forumFormatter
     * @param metaInfo
     * @return
     */
    private String formatForum(final List topNodeList, final ForumFormatter forumFormatter, final Map metaInfo) {
        forumFormatter.setForumMetaInformation(metaInfo);
        final StringBuilder formattedForum = new StringBuilder();
        forumFormatter.openForum();
        for (final Iterator iterTop = topNodeList.iterator(); iterTop.hasNext();) {
            final MessageNode mn = (MessageNode) iterTop.next();
            // a new top thread starts, inform formatter
            forumFormatter.openThread();
            final TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
            tv.visitAll();
            // commit
            formattedForum.append(forumFormatter.closeThread());
        }
        return formattedForum.append(forumFormatter.closeForum().toString()).toString();
    }

    /**
     * @param mn
     * @param forumFormatter
     * @param metaInfo
     * @return
     */
    private String formatThread(final MessageNode mn, final ForumFormatter forumFormatter, final Map metaInfo) {
        forumFormatter.setForumMetaInformation(metaInfo);
        final StringBuilder formattedThread = new StringBuilder();
        forumFormatter.openThread();
        final TreeVisitor tv = new TreeVisitor(forumFormatter, mn, false);
        tv.visitAll();
        return formattedThread.append(formattedThread.append(forumFormatter.closeThread())).toString();
    }

}
