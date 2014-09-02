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
package org.olat.presentation.course.nodes.fo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.data.forum.Forum;
import org.olat.data.forum.Message;
import org.olat.lms.forum.ForumService;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.htmlsite.OlatCmdEvent;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.system.commons.Formatter;
import org.olat.system.event.Event;
import org.olat.system.spring.CoreSpringFactory;

/**
 * <h3>Description:</h3> The forum peekview controller displays the configurable amount of the newest forum messages.
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>OlatCmdEvent to notify that a jump to the course node is desired</li>
 * </ul>
 * <p>
 * Initial Date: 29.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class FOPeekviewController extends BasicController implements Controller {
    // comparator to sort the messages list by creation date
    private static final Comparator<Message> dateSortingComparator = new Comparator<Message>() {
        @Override
        public int compare(final Message m1, final Message m2) {
            return m2.getCreationDate().compareTo(m1.getCreationDate()); // last first
        }
    };
    // the current course node id
    private final String nodeId;

    /**
     * Constructor
     * 
     * @param ureq
     *            The user request
     * @param wControl
     *            The window control
     * @param forum
     *            The forum instance
     * @param nodeId
     *            The course node ID
     * @param itemsToDisplay
     *            number of items to be displayed, must be > 0
     */
    public FOPeekviewController(final UserRequest ureq, final WindowControl wControl, final Forum forum, final String nodeId, final int itemsToDisplay) {
        // Use fallback translator from forum
        super(ureq, wControl, PackageUtil.createPackageTranslator(Forum.class, ureq.getLocale()));
        this.nodeId = nodeId;

        final VelocityContainer peekviewVC = createVelocityContainer("peekview");
        // add items, only as many as configured
        final ForumService foMgr = getForumService();
        final List<Message> allMessages = foMgr.getMessagesByForum(forum);
        // Sort messages by creation date
        Collections.sort(allMessages, dateSortingComparator);
        // only take the configured amount of messages
        final List<PeekviewEntry> peekviewEntries = new ArrayList<PeekviewEntry>();
        for (int i = 0; i < allMessages.size(); i++) {
            if (peekviewEntries.size() == itemsToDisplay) {
                break;
            }
            final Message message = allMessages.get(i);
            peekviewEntries.add(new PeekviewEntry(message, getUserService().getFirstAndLastname(message.getCreator().getUser())));
            // add link to item
            // Add link to jump to course node
            final Link nodeLink = LinkFactory.createLink("nodeLink_" + message.getKey(), peekviewVC, this);
            nodeLink.setCustomDisplayText(message.getTitle());
            nodeLink.setCustomEnabledLinkCSS("b_with_small_icon_left o_forum_message_icon o_gotoNode");
            nodeLink.setUserObject(Long.toString(message.getKey()));
        }
        peekviewVC.contextPut("peekviewEntries", peekviewEntries);
        // Add link to show all items (go to node)
        final Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
        allItemsLink.setCustomEnabledLinkCSS("b_float_right");
        // Add Formatter for proper date formatting
        peekviewVC.contextPut("formatter", Formatter.getInstance(getLocale()));
        //
        this.putInitialPanel(peekviewVC);
    }

    /**
     * @return
     */
    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

    private ForumService getForumService() {
        return CoreSpringFactory.getBean(ForumService.class);

    }

    /**
	 */
    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        if (source instanceof Link) {
            final Link nodeLink = (Link) source;
            final String messageId = (String) nodeLink.getUserObject();
            if (messageId == null) {
                fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
            } else {
                fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId + "/" + messageId));
            }
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        // nothing to dispose
    }

}
