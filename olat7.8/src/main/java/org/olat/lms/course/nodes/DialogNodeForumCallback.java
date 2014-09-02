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

package org.olat.lms.course.nodes;

import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.course.run.userview.NodeEvaluation;
import org.olat.lms.forum.ForumCallback;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogNodeForumCallback
 * <P>
 * Initial Date: 21.11.2005 <br>
 * 
 * @author guido
 */
public class DialogNodeForumCallback implements ForumCallback {

    private final NodeEvaluation ne;
    private final boolean isOlatAdmin;
    private final boolean isGuestOnly;
    private final SubscriptionContext subscriptionContext;

    /**
     * @param ne
     *            the nodeevaluation for this coursenode
     * @param isOlatAdmin
     *            true if the user is olat-admin
     * @param isGuestOnly
     *            true if the user is olat-guest
     * @param subscriptionContext
     */
    public DialogNodeForumCallback(final NodeEvaluation ne, final boolean isOlatAdmin, final boolean isGuestOnly, final SubscriptionContext subscriptionContext) {
        this.ne = ne;
        this.isOlatAdmin = isOlatAdmin;
        this.isGuestOnly = isGuestOnly;
        this.subscriptionContext = subscriptionContext;
    }

    /**
	 */
    @Override
    public boolean mayOpenNewThread() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("poster") || ne.isCapabilityAccessible("moderator") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean mayReplyMessage() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("poster") || ne.isCapabilityAccessible("moderator") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean mayEditMessageAsModerator() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean mayDeleteMessageAsModerator() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public boolean mayArchiveForum() {
        if (isGuestOnly) {
            return false;
        } else {
            return true;
        }
    }

    /**
	 */
    @Override
    public boolean mayFilterForUser() {
        if (isGuestOnly) {
            return false;
        }
        return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
    }

    /**
	 */
    @Override
    public SubscriptionContext getSubscriptionContext() {
        return (isGuestOnly ? null : subscriptionContext);
    }

}
