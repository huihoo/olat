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
package org.olat.presentation.instantmessaging.rosterandchat;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.olat.lms.instantmessaging.InstantMessagingModule;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.panel.Panel;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.controller.BasicController;
import org.olat.system.event.Event;

/**
 * Description:<br>
 * ChatManagerController: Manages peer to peer chats
 * <P>
 * Initial Date: 05.05.2008 <br>
 * 
 * @author guido
 */
public class ChatManagerController extends BasicController {

    private final VelocityContainer container = createVelocityContainer("chats");
    private final HashMap<String, ChatController> chats = new HashMap<String, ChatController>(2);
    private final HashMap<String, String> histories = new HashMap<String, String>();

    /**
     * @param ureq
     * @param control
     */
    public ChatManagerController(final UserRequest ureq, final WindowControl control) {
        super(ureq, control);
        container.contextPut("chats", chats);
        putInitialPanel(container);
    }

    @Override
    protected void event(final UserRequest ureq, final Controller source, final Event event) {
        if (source instanceof ChatController) {
            final ChatController chatCtr = (ChatController) source;

            // the only event from ChatController is for closing chat
            final String jabberId = event.getCommand();
            histories.put(jabberId, chatCtr.getMessageHistory());
            chats.remove(jabberId);
            final Component c = container.getComponent(jabberId);
            container.remove(c);
            InstantMessagingModule.getAdapter().getClientManager().deregisterControllerListener(getIdentity().getName(), source);
            final Chat chat = chatCtr.getChatManager();
            if (chat != null) {
                final Collection<MessageListener> listeners = chat.getListeners();
                for (final Iterator<MessageListener> iterator = listeners.iterator(); iterator.hasNext();) {
                    chat.removeMessageListener(iterator.next());
                }
            }
            // forward event also to main controller
            fireEvent(ureq, event);
        }
    }

    /**
	 */
    @Override
    protected void doDispose() {
        chats.clear();
    }

    /**
     * @param ureq
     * @param windowControl
     * @param jabberId
     * @param offsetX
     * @param offsetY
     * @param createMsgListener
     * @param initialMessage
     */
    public void createChat(final UserRequest ureq, final WindowControl windowControl, String jabberId, final int offsetX, final int offsetY,
            final boolean createMsgListener, final List<Message> initialMessages) {
        jabberId = extractJid(jabberId);

        if (chats.containsKey(jabberId)) {
            return; // chat with this person is already ongoing
        }

        final ChatController chat = new ChatController(ureq, windowControl, jabberId, offsetX, offsetY, extractMessages(initialMessages));
        listenTo(chat);
        if (createMsgListener) {
            chat.setChatManager(InstantMessagingModule.getAdapter().getClientManager().createChat(getIdentity().getName(), jabberId, chat));
        }

        if (histories.containsKey(jabberId)) {
            chat.setMessageHistory(histories.get(jabberId));
        }

        final Panel p = new Panel("chatholder");
        p.setContent(chat.getInitialComponent());
        container.put(jabberId, p);
        chats.put(jabberId, chat);
    }

    /**
     * @param ureq
     * @param windowControl
     * @param jabberId
     */
    public void createChat(final UserRequest ureq, final WindowControl windowControl, final String jabberId) {
        createChat(ureq, windowControl, jabberId, 100 + (chats.size() * 10), 100 + (chats.size() * 5), false, null);
    }

    /**
     * extract the jabber id without the resource appendix
     * 
     * @param jabberId
     * @return
     */
    private String extractJid(final String jabberId) {
        final int pos = jabberId.lastIndexOf("/");
        if (pos > 0) {
            return jabberId.substring(0, jabberId.lastIndexOf("/"));
        }
        return jabberId;
    }

    /**
     * check whether already a chat is running for this jid
     * 
     * @param jabberId
     * @return
     */
    public boolean hasRunningChat(final String jabberId) {
        return chats.containsKey(extractJid(jabberId));
    }

    @Override
    protected void event(final UserRequest ureq, final Component source, final Event event) {
        // no events
    }

    /**
     * @param initialMessages
     * @return
     */
    private Message extractMessages(final List<Message> initialMessages) {
        if (initialMessages == null) {
            return null;
        }
        if (initialMessages.size() > 1) {
            final Message msg = initialMessages.get(0);
            final StringBuilder sb = new StringBuilder();
            final String from = msg.getFrom();
            for (final Iterator<Message> iterator = initialMessages.iterator(); iterator.hasNext();) {
                final Message message = iterator.next();
                if (message.getFrom().equals(from)) {
                    sb.append(message.getBody()).append("<br/>\n");
                }
            }
            final Message newMsg = new Message();
            newMsg.setBody(sb.toString());
            newMsg.setFrom(from);
            newMsg.setProperty("receiveTime", new Long(new Date().getTime()));
            return newMsg;
        } else {
            return initialMessages.get(0);
        }
    }

}
