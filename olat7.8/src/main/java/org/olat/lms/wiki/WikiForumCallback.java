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

package org.olat.lms.wiki;

import org.olat.lms.core.notification.service.SubscriptionContext;
import org.olat.lms.forum.ForumCallback;

/**
 * Description:<br>
 * Security callback for enabling disabling some functions in the forums used in the wiki
 * <P>
 * Initial Date: May 24, 2006 <br>
 * 
 * @author guido
 */
public class WikiForumCallback implements ForumCallback {

    private final boolean isGuestOnly;
    private final boolean isModerator;

    public WikiForumCallback(final boolean isGuestOnly, final boolean isModerator) {
        this.isGuestOnly = isGuestOnly;
        this.isModerator = isModerator;
    }

    @Override
    public boolean mayOpenNewThread() {
        return !isGuestOnly;
    }

    @Override
    public boolean mayReplyMessage() {
        return !isGuestOnly;
    }

    @Override
    public boolean mayEditMessageAsModerator() {
        return !isGuestOnly && isModerator;
    }

    @Override
    public boolean mayDeleteMessageAsModerator() {
        return !isGuestOnly && isModerator;
    }

    @Override
    public boolean mayArchiveForum() {
        return !isGuestOnly;
    }

    @Override
    public boolean mayFilterForUser() {
        return !isGuestOnly && isModerator;
    }

    @Override
    public SubscriptionContext getSubscriptionContext() {
        // TODO Auto-generated method stub
        return null;
    }

}
