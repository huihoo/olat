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

import java.io.Serializable;
import java.util.Date;

import org.olat.lms.commons.i18n.I18nManager;
import org.olat.system.commons.Formatter;

public class ConnectedUsersListEntry implements Serializable {
    private static final String NOT_VISIBLE = "---";
    private final String username;
    private String name;
    private String prename;
    private String instantMessagingStatus;
    private boolean showAwarenessMessage = false;
    private String awarenessMessage;
    private boolean showOnlineTime = false;
    private String onlineTime;
    private String lastActivity;
    private String jabberId;
    private String resource;
    private boolean isVisible;
    private final Formatter formatter;

    public ConnectedUsersListEntry(final String username, final String locale) {
        this.username = username;
        this.formatter = Formatter.getInstance(I18nManager.getInstance().getLocaleOrDefault(locale));
    }

    /**
     * @return Returns the awarenessMessage.
     */
    public String getAwarenessMessage() {
        if (isShowAwarenessMessage()) {
            return awarenessMessage;
        } else {
            return NOT_VISIBLE;
        }
    }

    /**
     * @param awarenessMessage
     *            The awarenessMessage to set.
     */
    public void setAwarenessMessage(final String awarenessMessage) {
        this.awarenessMessage = awarenessMessage;
    }

    /**
     * @return Returns the instantMessagingStatus.
     */
    public String getInstantMessagingStatus() {
        return instantMessagingStatus;
    }

    /**
     * @param instantMessagingStatus
     *            The instantMessagingStatus to set.
     */
    public void setInstantMessagingStatus(final String instantMessagingStatus) {
        this.instantMessagingStatus = instantMessagingStatus;
    }

    /**
     * @return Returns the lastActivity.
     */
    public String getLastActivity() {
        if (isShowOnlineTime()) {
            return formatter.formatTimeShort(new Date(Long.valueOf(lastActivity).longValue()));
        } else {
            return NOT_VISIBLE;
        }
    }

    /**
     * @param lastActivity
     *            The lastActivity to set.
     */
    public void setLastActivity(final long lastActivity) {
        this.lastActivity = Long.valueOf(lastActivity).toString();
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the onlineTime.
     */
    public String getOnlineTime() {
        if (isShowOnlineTime()) {
            return onlineTime;
        } else {
            return NOT_VISIBLE;
        }
    }

    /**
     * @param logonTime
     *            set it to the time the when the used logged in in milliseconds since epoc the method calculates the time in minutes the user is online
     */
    public void setOnlineTime(final long logonTime) {
        final long diff = System.currentTimeMillis() - logonTime;
        this.onlineTime = new Integer((int) diff / 1000 / 60).toString();
    }

    /**
     * @param logonTime
     *            set the already calculated online time in minutes
     */
    public void setOnlineTime(final String logonTime) {
        this.onlineTime = logonTime;
    }

    /**
     * @return Returns the prename.
     */
    public String getPrename() {
        return prename;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param prename
     *            The prename to set.
     */
    public void setPrename(final String prename) {
        this.prename = prename;
    }

    /**
     * @return Returns the showAwarenessMessage.
     */
    protected boolean isShowAwarenessMessage() {
        return showAwarenessMessage;
    }

    /**
     * @param showAwarenessMessage
     *            The showAwarenessMessage to set.
     */
    public void setShowAwarenessMessage(final boolean showAwarenessMessage) {
        this.showAwarenessMessage = showAwarenessMessage;
    }

    /**
     * @return Returns the showOnlineTime.
     */
    protected boolean isShowOnlineTime() {
        return showOnlineTime;
    }

    /**
     * @param showOnlineTime
     *            The showOnlineTime to set.
     */
    public void setShowOnlineTime(final boolean showOnlineTime) {
        this.showOnlineTime = showOnlineTime;
    }

    /**
     * @return Returns the jabberId.
     */
    public String getJabberId() {
        return jabberId;
    }

    /**
     * @param jabberId
     *            The jabberId to set.
     */
    public void setJabberId(final String jabberId) {
        this.jabberId = jabberId;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(final String resource) {
        this.resource = resource;
    }

    public void setVisibleToOthers(final boolean visibleToOthers) {
        this.isVisible = visibleToOthers;
    }

    public boolean isVisible() {
        return isVisible;
    }
}
