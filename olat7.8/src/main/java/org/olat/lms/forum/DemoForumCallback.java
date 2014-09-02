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

package org.olat.lms.forum;

import org.olat.lms.core.notification.service.SubscriptionContext;

/**
 * @author schneider
 */
public class DemoForumCallback implements ForumCallback {

    @Override
    public boolean mayOpenNewThread() {
        return true;
    }

    @Override
    public boolean mayReplyMessage() {
        return true;
    }

    @Override
    public boolean mayEditMessageAsModerator() {

        return true;
    }

    @Override
    public boolean mayDeleteMessageAsModerator() {

        return false;
    }

    @Override
    public boolean mayArchiveForum() {

        return true;
    }

    @Override
    public boolean mayFilterForUser() {
        return true;
    }

    /**
	 */
    @Override
    public SubscriptionContext getSubscriptionContext() {
        return null;
    }
}
