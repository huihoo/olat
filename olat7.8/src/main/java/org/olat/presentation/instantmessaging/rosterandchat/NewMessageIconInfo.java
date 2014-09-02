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

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.olat.presentation.framework.core.components.link.Link;

/**
 * Description:<br>
 * holds all information regarding a new message in one place
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author guido
 */
public class NewMessageIconInfo {

    private final String jabberId;
    private final List<Message> initialMessages = new ArrayList<Message>(2);
    private Link newMessageLink;

    /**
     * @param jabberId
     */
    protected NewMessageIconInfo(final String jabberId, final Message initialMessage) {
        this.jabberId = extractJid(jabberId);
        initialMessages.add(initialMessage);
    }

    /**
     * [used in velocity!]
     * 
     * @return
     */
    public String getJabberId() {
        return jabberId;
    }

    protected List<Message> getInitialMessages() {
        return initialMessages;
    }

    protected void addInitialMessage(final Message m) {
        initialMessages.add(m);
    }

    protected Link getNewMessageLink() {
        return newMessageLink;
    }

    protected void setNewMessageLink(final Link newMessageLink) {
        this.newMessageLink = newMessageLink;
    }

    /**
     * extract the jabber id without the resource appendix
     * 
     * @param jabberId
     * @return
     */
    private String extractJid(final String id) {
        final int pos = id.lastIndexOf("/");
        if (pos > 0) {
            return id.substring(0, id.lastIndexOf("/"));
        }
        return id;
    }

}
