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
package org.olat.lms.learn.notification.service;

import org.olat.data.notification.Subscription;

/**
 * Initial Date: 12.04.2012 <br>
 * 
 * @author guretzki
 */
public class SubscriptionTO {

    private final String sourceType;
    private final String courseNodeTitle;
    private final String courseTitle;
    private final Subscription subscription;

    private final String contextUrl; // e.g. http://localhost:8080/olat/url/RepositoryEntry/458762
    private final String publisherSourceUrl; // e.g. http://localhost:8080/olat/url/RepositoryEntry/458762/CourseNode/82817051272135

    public SubscriptionTO(String sourceType, String courseNodeTitle, String publisherSourceUrl, String courseTitle, String contextUrl, Subscription subscription) {
        super();
        this.sourceType = sourceType;
        this.courseNodeTitle = courseNodeTitle;
        this.publisherSourceUrl = publisherSourceUrl;
        this.courseTitle = courseTitle;
        this.contextUrl = contextUrl;
        this.subscription = subscription;

    }

    public String getSourceType() {
        return sourceType;
    }

    public String getCourseNodeTitle() {
        return courseNodeTitle;
    }

    public String getPublisherSourceUrl() {
        return publisherSourceUrl;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public Subscription getSubscription() {
        return subscription;
    }

}
