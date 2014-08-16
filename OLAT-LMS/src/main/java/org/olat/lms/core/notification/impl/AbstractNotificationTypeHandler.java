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
package org.olat.lms.core.notification.impl;

import org.apache.log4j.Logger;
import org.olat.data.notification.Publisher;
import org.olat.lms.core.notification.NotificationTypeHandler;
import org.olat.system.logging.log4j.LoggerHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
public abstract class AbstractNotificationTypeHandler implements NotificationTypeHandler {
    private static final Logger log = LoggerHelper.getLogger();

    @Autowired
    public NotificationSubscriptionContextFactory notificationSubscriptionContextFactory;

    private String sourceType;
    private String publisherDataType;

    protected AbstractNotificationTypeHandler(String sourceType, String publisherDataType) {
        this.sourceType = sourceType;
        this.publisherDataType = publisherDataType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getPublisherDataType() {
        return publisherDataType;
    }

    public String getBusinessPathToSource(Publisher publisher) {
        return "[RepositoryEntry:" + publisher.getContextId() + "][CourseNode:" + publisher.getSubcontextId() + "]";
    }

}
