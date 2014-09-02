/**
 * OLAT - Online Learning and Training<br />
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br />
 * you may not use this file except in compliance with the License.<br />
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br />
 * software distributed under the License is distributed on an "AS IS" BASIS, <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.lms.instantmessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.olat.data.basesecurity.BaseSecurity;
import org.olat.data.basesecurity.Identity;
import org.olat.lms.user.UserService;
import org.olat.presentation.framework.core.components.link.Link;
import org.olat.presentation.framework.core.components.link.LinkFactory;
import org.olat.presentation.framework.core.components.velocity.VelocityContainer;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.commons.Formatter;
import org.olat.system.logging.log4j.LoggerHelper;
import org.olat.system.spring.CoreSpringFactory;

/**
 * Helper class needed for rendering the roster and message stuff Iterator free for velocity. Velocity produces warnings when passing objects that return Iterators for
 * looping over collections. Class currenty only needed for roster rendering in navigation bar.
 * <P>
 * Initial Date: 20.01.2005
 * 
 * @author guido
 */
public class ClientHelper {

    private static final Logger log = LoggerHelper.getLogger();

    private final InstantMessagingClient imc;
    private final Controller controller;
    private final VelocityContainer vc;
    private final Translator translator;

    /**
     * @param username
     * @param locale
     */
    public ClientHelper(final String username, final Controller controller, final VelocityContainer vc, final Translator translator) {
        this.imc = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(username);
        this.controller = controller;
        this.vc = vc;
        this.translator = translator;
    }

    /**
     * @return a List
     */
    public List<RosterGroup> getRoster() {
        if (imc.isConnected()) {
            final List<RosterGroup> groups = new ArrayList<RosterGroup>();
            for (final Iterator<RosterGroup> i = imc.getRoster().getGroups().iterator(); i.hasNext();) {
                groups.add(i.next());
            }
            return groups;
        }
        return new ArrayList<RosterGroup>(0);
    }

    /**
     * used by velocity
     * 
     * @return a list of distinct usernames of the roster
     */
    public List<String> getDistinctRoster() {
        if (imc.isConnected()) {
            return createLinkList(imc.getRoster().getEntries().iterator(), null);
        }
        return new ArrayList<String>(0);
    }

    /**
     * used by velocity
     * 
     * @param groupname
     * @return a List
     */
    public List<String> getRosterGroupEntries(final String groupname) {
        if (imc.isConnected()) {
            return createLinkList(imc.getRoster().getGroup(groupname).getEntries().iterator(), groupname);
        }
        return new ArrayList<String>();
    }

    private List<String> createLinkList(final Iterator<RosterEntry> iter, final String groupname) {
        final List<String> entries = new ArrayList<String>();
        Link link;
        for (final Iterator<RosterEntry> i = iter; i.hasNext();) {
            final RosterEntry entry = i.next();
            final String entryPresence = getUserPresence(entry.getUser());
            if (getShowOfflineBuddies() || entryPresence != "offline") {
                if (groupname != null) {
                    link = LinkFactory.createCustomLink(entry.getUser() + createAppendixFromGroupName(groupname), "cmd.chat", "", Link.NONTRANSLATED, vc, controller);
                } else {
                    link = LinkFactory.createCustomLink(entry.getUser(), "cmd.chat", "", Link.NONTRANSLATED, vc, controller);
                }
                Identity ident = null;
                try {
                    ident = getBaseSecurity().findIdentityByName(entry.getName());
                } catch (Exception e) {
                    // at least avoid the red screen at "Show off-line contacts" and log the error
                    log.error("Cannot find identity: " + entry.getName());
                }
                if (ident != null) {
                    link.setCustomDisplayText(getUserService().getFirstAndLastname(ident.getUser()) + " (" + ident.getName() + ")");
                } else {
                    link.setCustomDisplayText(entry.getName());
                }
                link.setCustomEnabledLinkCSS("o_instantmessaging_" + entryPresence + "_icon");
                link.setUserObject(entry.getUser());
                final StringBuilder sb = new StringBuilder();
                if (!imc.isChatDisabled()) {
                    sb.append(translator.translate("im.status")).append(" ");
                    sb.append(translator.translate("presence." + entryPresence));
                    sb.append("<br />");
                    if (ident != null) {
                        // TODO:gs:a how to get the roster entries presence msg? new clienthelper will work but creates a im client!
                        sb.append(translator.translate("im.status.msg")).append(" ").append("");
                    }
                    sb.append("<br /><br />");
                    sb.append(translator.translate("im.start.chat"));
                } else {
                    sb.append(translator.translate("im.chat.disabled"));
                }
                link.setTooltip(sb.toString(), false);
                link.registerForMousePositionEvent(true);
                entries.add(entry.getUser());
            }
        }
        return entries;

    }

    private BaseSecurity getBaseSecurity() {
        return (BaseSecurity) CoreSpringFactory.getBean(BaseSecurity.class);
    }

    public static String createAppendixFromGroupName(final String groupname) {
        int groupAsInt = 0;
        final char[] letters = groupname.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            groupAsInt = groupAsInt + letters[i];
        }
        return String.valueOf(groupAsInt);
    }

    /**
     * @param groupname
     * @return online buddies for a certain group
     */
    public String buddyCountOnlineForGroup(final String groupname) {
        if (imc.isConnected()) {
            try {
                return imc.buddyCountOnlineForGroup(groupname);
            } catch (final RuntimeException e) {
                log.warn("Error while trying to count buddies for group", e);
                return "(?/?)";
            }
        }
        return "(?/?)";
    }

    /**
     * @param jid
     * @return the presence
     */
    public String getUserPresence(final String jid) {
        try {
            return imc.getUserPresence(jid);
        } catch (final RuntimeException e) {
            log.warn("Error while trying to get user presence. User: " + imc.getUsername(), e);
            return "";
        }
    }

    /**
     * @param xmppAddressWithRessource
     * @return a string like test@testserver.ch
     */
    public String parseJid(final String xmppAddressWithRessource) {
        return imc.parseJid(xmppAddressWithRessource);
    }

    /**
     * @return true if the user is connected
     */
    public boolean isConnected() {
        return imc.isConnected();
    }

    /**
     * @return a number of the online users for a user
     */
    public String buddyCountOnline() {
        if (imc.isConnected()) {
            try {
                return imc.buddyCountOnline();
            } catch (final RuntimeException e) {
                log.warn("Error while trying to count buddies for group", e);
                return "(?/?)";
            }
        }
        return "(?/?)";
    }

    /**
     * @param type
     * @param status
     * @param priority
     * @param mode
     */
    public void sendPresence(final Type type, final String status, final int priority, final Mode mode) {
        imc.sendPresence(type, status, priority, mode);
    }

    /**
     * @return password
     */
    public String getPassword() {
        return imc.getPassword();
    }

    /**
     * @return status
     */
    public String getStatus() {
        return imc.getStatus();
    }

    /**
     * @return status message
     */
    public String getStatusMsg() {
        return imc.getStatusMsg();
    }

    /**
     * @return true if user likes seeing also his offline buddies in the roster
     */
    public boolean getShowOfflineBuddies() {
        return imc.getShowOfflineBuddies();
    }

    /**
     * @param showOfflineBuddies
     */
    public void setShowOfflineBuddies(final boolean showOfflineBuddies) {
        imc.setShowOfflineBuddies(showOfflineBuddies);
    }

    /**
     * @return online time in minutes
     */
    public String getOnlineTime() {
        return imc.getOnlineTime();
    }

    /**
     * @return true if groups should be shown in roster
     */
    public boolean isShowGroupsInRoster() {
        return imc.isShowGroupsInRoster();
    }

    /**
     * @param showGroupsInRoster
     */
    public void setShowGroupsInRoster(final boolean showGroupsInRoster) {
        imc.setShowGroupsInRoster(showGroupsInRoster);
    }

    /**
     * @return the users JID like test@testserver.ch
     */
    public String getJid() {
        return imc.getJid();
    }

    public static String getSendDate(final Message msg, final Locale loc) {
        for (final Iterator iter = msg.getExtensions().iterator(); iter.hasNext();) {
            final PacketExtension extension = (PacketExtension) iter.next();
            if (extension.getNamespace().equals("jabber:x:delay")) {
                final DelayInformation delayInfo = (DelayInformation) extension;
                final Date date = delayInfo.getStamp();
                // why does formatter with this method return a time in the afternoon
                // like 03:24 instead of 15:24 like formatTime does??
                return Formatter.getInstance(loc).formatDateAndTime(date);
            }
        }
        // if no delay time now is returned
        // return Formatter.getInstance(locale).formatTime(new Date());
        final Long receiveTime = (Long) msg.getProperty("receiveTime");
        final Date d = new Date();
        d.setTime(receiveTime.longValue());
        return Formatter.getInstance(loc).formatTime(d);
    }

    /**
     * send a presence packet "available" with a certain mode e.g. "away" to all buddies
     * 
     * @param mode
     */
    public void sendPresenceAvailable(final Presence.Mode mode) {
        imc.sendPresence(Presence.Type.available, null, 0, mode);
    }

    /**
     * send a presence packet "unavailable" to all buddies
     */
    public void sendPresenceUnavailable() {
        imc.sendPresence(Presence.Type.unavailable, null, 0, null);
    }

    /**
     * @return boolean true if user is allowed to chat and false during assessments when chat is disabled
     */
    public boolean isChatDisabled() {
        return imc.isChatDisabled();
    }

    private UserService getUserService() {
        return CoreSpringFactory.getBean(UserService.class);
    }

}
