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

import org.apache.log4j.Logger;
import org.olat.lms.course.CourseModule;
import org.olat.lms.course.nodes.CourseNode;
import org.olat.lms.course.run.userview.UserCourseEnvironment;
import org.olat.lms.notifications.SubscriptionContext;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.stereotype.Component;

/**
 * Description:<br>
 * Notification handler for course node task. Subscribers get informed about new uploaded file in the dropbox.
 * <P>
 * Initial Date: 23.11.2005 <br />
 * 
 * @author christian guretzki
 */
@Component
public class DropboxFileUploadNotificationHandler extends AbstractTaskNotificationHandler {
    private static final Logger log = LoggerHelper.getLogger();

    private static final String CSS_CLASS_DROPBOX_ICON = "o_dropbox_icon";

    private DropboxFileUploadNotificationHandler() {
        //
    }

    public static SubscriptionContext getSubscriptionContext(final UserCourseEnvironment userCourseEnv, final CourseNode node) {
        return CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), node, node.getIdent());
    }

    @Override
    protected String getCssClassIcon() {
        return CSS_CLASS_DROPBOX_ICON;
    }

    @Override
    protected String getNotificationHeaderKey() {
        return "dropbox.notifications.header";
    }

    @Override
    protected String getNotificationEntryKey() {
        return "dropbox.notifications.entry";
    }

    @Override
    public String getType() {
        return "DropboxController";
    }

}
