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

package org.olat.presentation.course.nodes.ta;

import org.olat.data.basesecurity.Identity;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.notifications.SubscriptionContext;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Notification handler for course node task. Subscribers get informed about new uploaded file in the returnbox.
 * <P>
 * Initial Date: 23.06.2010 <br />
 * 
 * @author christian guretzki
 */
@Component
public class ReturnboxFileUploadNotificationHandler extends AbstractTaskNotificationHandler {
    private static final String CSS_CLASS_RETURNBOX_ICON = "o_returnbox_icon";

    private ReturnboxFileUploadNotificationHandler() {
        // empty block
    }

    public static SubscriptionContext getSubscriptionContext(final UserCourseEnvironment userCourseEnv, final CourseNode node, final Identity identity) {
        return CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), node, "Returnbox-" + identity.getKey());
    }

    @Override
    protected String getCssClassIcon() {
        return CSS_CLASS_RETURNBOX_ICON;
    }

    @Override
    protected String getNotificationHeaderKey() {
        return "returnbox.notifications.header";
    }

    @Override
    protected String getNotificationEntryKey() {
        return "returnbox.notifications.entry";
    }

    @Override
    public String getType() {
        return "ReturnboxController";
    }

}
