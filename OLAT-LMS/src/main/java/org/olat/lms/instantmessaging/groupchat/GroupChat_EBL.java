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
package org.olat.lms.instantmessaging.groupchat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.olat.data.commons.database.DBFactory;
import org.olat.lms.instantmessaging.Chat_EBL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <P>
 * Initial Date: 05.09.2011 <br>
 * 
 * @author guretzki
 */
@Component
@Scope("prototype")
public class GroupChat_EBL {

    public static final String NICKNAME_PREFIX = "anonym_";

    private final List<String> rosterList = new ArrayList<String>();

    private String anonymousNickName;

    private MultiUserChat muc;

    private boolean chatWindowOpen = false;

    @Autowired
    private Chat_EBL chatEbl;

    /**
	 * 
	 */
    public GroupChat_EBL() {
        super();
    }

    public boolean isChatWindowOpen() {
        return chatWindowOpen;
    }

    public void setChatWindowOpen(boolean chatWindowOpen) {
        this.chatWindowOpen = chatWindowOpen;
    }

    public void addMeToRosterList(final String fname) {

        synchronized (rosterList) {

            rosterList.remove(getAnonymousNickName());

            if (!rosterList.contains(fname)) {
                rosterList.add(fname);
            }
        }
    }

    public void removeMeFromRosterList(String myFullName) {

        synchronized (rosterList) {
            rosterList.remove(getAnonymousNickName());
            rosterList.remove(myFullName);
        }
    }

    public List<String> getRosterList() {
        Collections.sort(rosterList);
        return rosterList;
    }

    public void updateRosterListFrom(final Presence p) {

        synchronized (rosterList) {

            try {
                final String s = p.getStatus();
                final String t = p.getType().name();
                final String n = chatEbl.getFullUserName(extractUsername(p.getFrom()));

                if ("chatEcho".equals(s)) {
                    if (!rosterList.contains(n)) {
                        rosterList.add(n);
                    }
                } else if ("chatOpen".equals(s)) {
                    if (!rosterList.contains(n)) {
                        rosterList.add(n);
                    }
                    if (chatWindowOpen) {
                        muc.changeAvailabilityStatus("chatEcho", Presence.Mode.available);
                    }
                } else if ("chatClosed".equals(s)) {
                    if (chatWindowOpen) {
                        muc.changeAvailabilityStatus("chatEcho", Presence.Mode.available);
                    }
                    rosterList.remove(n);
                } else if (t.equals("available")) {
                    if (!rosterList.contains(n)) {
                        rosterList.add(n);
                    }
                } else if (t.equals("unavailable")) {
                    rosterList.remove(n);
                }

                if (!chatWindowOpen) {
                    rosterList.remove(getAnonymousNickName());
                }
                DBFactory.getInstance(false).commitAndCloseSession();

            } catch (final Exception e) {
                DBFactory.getInstance(false).rollbackAndCloseSession();
            }

        }
    }

    public String extractUsername(final String from) {
        if (from != null) {
            if (from.contains("/")) {
                return from.substring(from.lastIndexOf("/") + 1, from.length());
            }
        }
        return "chatroom";
    }

    public void setAnonymousNickName(Long identityKey) {
        anonymousNickName = NICKNAME_PREFIX + (int) Math.rint(Math.random() * identityKey);
    }

    public String getAnonymousNickName() {
        return anonymousNickName;
    }

    public MultiUserChat getMultiUserChat() {
        return muc;
    }

    /**
     * @param multiUserChat
     */
    public void setMultiUserChat(MultiUserChat multiUserChat) {
        this.muc = multiUserChat;
    }

}
