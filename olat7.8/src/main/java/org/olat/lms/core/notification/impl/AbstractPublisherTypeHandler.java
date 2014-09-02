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

import org.olat.data.notification.Publisher;
import org.olat.lms.core.notification.PublisherTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides implementation for <code>getBusinessPathToSource</code>.
 * 
 * Initial Date: 21.03.2012 <br>
 * 
 * @author guretzki
 */
public abstract class AbstractPublisherTypeHandler implements PublisherTypeHandler {

    @Autowired
    public NotificationSubscriptionContextFactory notificationSubscriptionContextFactory;

    private final String sourceType;
    private final String publisherDataType;

    protected AbstractPublisherTypeHandler(String sourceType, String publisherDataType) {
        this.sourceType = sourceType;
        this.publisherDataType = publisherDataType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getPublisherDataType() {
        return publisherDataType;
    }

    /**
     * This is the same for all types, assuming that publisher belongs to a context (course) and a sub-context (CourseNode).
     */
    public String getBusinessPathToSource(Publisher publisher) {
        return "[RepositoryEntry:" + publisher.getContextId() + "][CourseNode:" + publisher.getSubcontextId() + "]";
    }

}
