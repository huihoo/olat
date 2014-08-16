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
package org.olat.lms.core.notification.service;

import java.util.Date;

/**
 * The difference between this and NotificationEventTO is that this belongs to the service interface <br/>
 * and should encompass the one event information to be displayed into the user's Home.
 * 
 * Initial Date: 02.03.2012 <br>
 * 
 * @author lavinia
 */
public class UserNotificationEventTO {

    // private String status; //e.g. read/new - this is nice to have
    private final String contextTitle; // e.g. course title
    private final String sourceType; // e.g. forum
    private final String sourceTitle; // e.g. forum title
    private final String sourceEntryTitle; // e.g. message title
    private final String eventSourceUrl; // e.g. http://localhost:8080/olat/url/RepositoryEntry/458762/CourseNode/82817051272135
    private final String eventSourceEntryUrl; // e.g. http://localhost:8080/olat/url/RepositoryEntry/458762/CourseNode/82817051272135/Message/2260993
    private final String contextUrl; // e.g. http://localhost:8080/olat/url/RepositoryEntry/458762

    private final PublishEventTO.EventType eventType; // e.g. new/changed
    private final String creatorFirstLastName;
    private final Date creationDate;

    public UserNotificationEventTO(String contextTitle, String contextUrl, String sourceType, String sourceTitle, String sourceEntryTitle, String eventSourceUrl,
            String eventSourceEntryUrl, PublishEventTO.EventType eventType, String creatorFirstLastName, Date creationDate) {

        this.contextTitle = contextTitle;
        this.contextUrl = contextUrl;
        this.sourceType = sourceType;
        this.sourceTitle = sourceTitle;
        this.sourceEntryTitle = sourceEntryTitle;
        this.eventSourceUrl = eventSourceUrl;
        this.eventSourceEntryUrl = eventSourceEntryUrl;

        this.eventType = eventType;
        this.creatorFirstLastName = creatorFirstLastName;
        this.creationDate = creationDate;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getSourceEntryTitle() {
        return sourceEntryTitle;
    }

    public PublishEventTO.EventType getEventType() {
        return eventType;
    }

    public String getCreatorFirstLastName() {
        return creatorFirstLastName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getEventSourceEntryUrl() {
        return eventSourceEntryUrl;
    }

    public String getEventSourceUrl() {
        return eventSourceUrl;
    }

}
